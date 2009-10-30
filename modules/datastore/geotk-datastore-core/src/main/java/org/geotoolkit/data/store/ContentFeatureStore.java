/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.data.store;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.data.query.DefaultQuery;
import org.geotoolkit.data.concurrent.FeatureLock;
import org.geotoolkit.data.FeatureLockException;
import org.geotoolkit.data.concurrent.FeatureLocking;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.InProcessLockingManager;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.concurrent.LockingManager;
import org.geotoolkit.data.query.Query;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

/**
 * Abstract implementation of FeatureStore.
 * <p>
 * List its base class {@link ContentFeatureSource}, this feature store works off
 * of operations provided by {@link FeatureCollection}.
 * </p>
 * <p>
 * The {@link #addFeatures(java.util.Collection)} method is used to add features to
 * the feature store. The method should return the "persistent" feature id's
 * which are generated after the feature has been added to persistent storage.
 * Often the persistent fid is different from the fid specified by the actual
 * feature being inserted. For this reason {@link SimpleFeature#getUserData()} is
 * used to report back persistent fids. It is up to the implementor of the
 * feature collection to report this value back after a feature has been inserted.
 * As an example, consider an implementation of {@link FeatureCollection#add(org.opengis.feature.Feature)}.
 * <pre>
 *  boolean add( Object o ) {
 *    SimpleFeature feature = (SimpleFeature) o;
 *
 *    //1.add the feature to storage
 *    ...
 *
 *    //2. derive the persistent fid
 *    String fid = ...;
 *
 *    //3. set the user data
 *    feature.getUserData().put( "fid", fid );
 *
 *  }
 * </pre>
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 * @module pending
 */
public abstract class ContentFeatureStore extends ContentFeatureSource implements
        FeatureStore<SimpleFeatureType, SimpleFeature>,
        FeatureLocking<SimpleFeatureType, SimpleFeature> {

    /**
     * writer flags
     */
    protected final int WRITER_ADD = ContentDataStore.WRITER_ADD;
    protected final int WRITER_UPDATE = ContentDataStore.WRITER_UPDATE;
    /**
     * current feature lock
     */
    protected FeatureLock lock = FeatureLock.TRANSACTION;

    /**
     * Creates the content feature store.
     *
     * @param entry The entry for the feature store.
     * @param query The defining query.
     */
    public ContentFeatureStore(final ContentEntry entry, final Query query) {
        super(entry, query);
    }

    /**
     * Returns a writer over features specified by a filter.
     *
     * @param filter The filter
     */
    public final FeatureWriter<SimpleFeatureType, SimpleFeature> getWriter(final Filter filter)
            throws IOException {
        return getWriter(filter, WRITER_ADD | WRITER_UPDATE);
    }

    /**
     * Returns a writer over features specified by a filter.
     *
     * @param filter The filter
     * @param flags flags specifying writing mode
     */
    public final FeatureWriter<SimpleFeatureType, SimpleFeature> getWriter(final Filter filter,
            final int flags) throws IOException {
        return getWriter(new DefaultQuery(getSchema().getTypeName(), filter), flags);
    }

    /**
     * Returns a writer over features specified by a query.
     *
     * @param query The query
     */
    public final FeatureWriter<SimpleFeatureType, SimpleFeature> getWriter(final Query query)
            throws IOException {
        return getWriter(query, WRITER_ADD | WRITER_UPDATE);
    }

    /**
     * Returns a writer over features specified by a query.
     *
     * @param query The query
     * @param flags flags specifying writing mode
     */
    public final FeatureWriter<SimpleFeatureType, SimpleFeature> getWriter(Query query,
            final int flags) throws IOException {
        query = joinQuery(query);
        query = resolvePropertyNames(query);

        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = getWriterInternal(query, flags);

        //TODO: apply wrappers

        //TODO: turn locking on / off
        final LockingManager lockingManager = getDataStore().getLockingManager();
        return ((InProcessLockingManager) lockingManager).checkedWriter(writer, transaction);

    }

    /**
     *
     * Subclass method for returning a native writer from the datastore.
     * <p>
     * It is important to note that if the native writer intends to handle any
     * of the following natively:
     * <ul>
     *   <li>reprojection</li>
     *   <li>filtering</li>
     *   <li>max feature limiting</li>
     *   <li>sorting<li>
     * </ul>
     * Then it <b>*must*</b> set the corresponding flags to <code>true</code>:
     * <ul>
     *   <li>{@link #canReproject()}</li>
     *   <li>{@link #canFilter()}</li>
     *   <li>{@link #canLimit()}</li>
     *   <li>{@link #canSort()}<li>
     * </ul>
     * </p>
     *
     */
    protected abstract FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(final Query query,
            final int flags) throws IOException;

    /**
     * Adds a collection of features to the store.
     * <p>
     * This method operates by getting an appending feature writer and writing
     * all the features in <tt>collection</tt> to it. Directly after a feature
     * is written its id is obtained and added to the returned set.
     * </p>
     */
    public final List<FeatureId> addFeatures(final Collection collection) throws IOException {

        //gather up id's
        final List<FeatureId> ids = new LinkedList<FeatureId>();

        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = getWriter(Filter.INCLUDE, WRITER_ADD);
        try {
            for (Iterator f = collection.iterator(); f.hasNext();) {
                final SimpleFeature feature = (SimpleFeature) f.next();

                // grab next feature and populate it
                // JD: worth a note on how we do this... we take a "pull" approach
                // because the raw schema we are inserting into may not match the
                // schema of the features we are inserting
                final SimpleFeature toWrite = writer.next();
                for (int i = 0; i < toWrite.getAttributeCount(); i++) {
                    final String name = toWrite.getType().getDescriptor(i).getLocalName();
                    toWrite.setAttribute(name, feature.getAttribute(name));
                }

                //perform the write
                writer.write();

                //add the id to the set of inserted
                ids.add(toWrite.getIdentifier());
            }
        } finally {
            writer.close();
        }

        return ids;
    }

    /**
     * Adds a collection of features to the store.
     * <p>
     * This method calls through to {@link #addFeatures(Collection)}.
     * </p>
     */
    @Override
    public final List<FeatureId> addFeatures(final FeatureCollection<SimpleFeatureType, SimpleFeature> collection)
            throws IOException {
        //gather up id's
        final List<FeatureId> ids = new LinkedList<FeatureId>();

        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = getWriter(Filter.INCLUDE, WRITER_ADD);
        final Iterator f = collection.iterator();
        try {
            while (f.hasNext()) {
                final SimpleFeature feature = (SimpleFeature) f.next();

                // grab next feature and populate it
                // JD: worth a note on how we do this... we take a "pull" approach
                // because the raw schema we are inserting into may not match the
                // schema of the features we are inserting
                final SimpleFeature toWrite = writer.next();
                for (int i = 0; i < toWrite.getAttributeCount(); i++) {
                    String name = toWrite.getType().getDescriptor(i).getLocalName();
                    toWrite.setAttribute(name, feature.getAttribute(name));
                }

                //perform the write
                writer.write();

                //add the id to the set of inserted
                ids.add(toWrite.getIdentifier());
            }
        } finally {
            writer.close();
            collection.close(f);
        }
        return ids;
    }

    /**
     * Sets the feature of the source.
     * <p>
     * This method operates by first clearing the contents of the feature
     * store ({@link #removeFeatures(Filter)}), and then obtaining an appending
     * feature writer and writing all features from <tt>reader</tt> to it.
     * </p>
     */
    @Override
    public final List<FeatureId> addFeatures(final FeatureReader<SimpleFeatureType, SimpleFeature> reader) throws IOException {
        final List<FeatureId> ids = new LinkedList<FeatureId>();

        //grab a feature writer for insert
        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = getWriter(Filter.INCLUDE, WRITER_ADD);
        try {
            while (reader.hasNext()) {
                final SimpleFeature feature = reader.next();

                // grab next feature and populate it
                // JD: worth a note on how we do this... we take a "pull" approach
                // because the raw schema we are inserting into may not match the
                // schema of the features we are inserting
                final SimpleFeature toWrite = writer.next();
                for (int i=0; i<toWrite.getAttributeCount(); i++) {
                    final String name = toWrite.getType().getDescriptor(i).getLocalName();
                    toWrite.setAttribute(name, feature.getAttribute(name));
                }

                //perform the write
                writer.write();
                ids.add(toWrite.getIdentifier());
            }
        } finally {
            writer.close();
        }

        return ids;
    }

    /**
     * Modifies/updates the features of the store which match the specified filter.
     * <p>
     * This method operates by obtaining an updating feature writer based on the
     * specified <tt>filter</tt> and writing the updated values to it.
     * </p>
     * <p>
     * The <tt>filter</tt> must not be <code>null</code>, in this case this method
     * will throw an {@link IllegalArgumentException}.
     * </p>
     */
    @Override
    public void updateFeatures(final AttributeDescriptor[] type, final Object[] value,
            Filter filter) throws IOException {
        if (filter == null) {
            final String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException(msg);
        }
        filter = resolvePropertyNames(filter);

        //grab a feature writer
        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = getWriter(filter, WRITER_UPDATE);
        try {
            while (writer.hasNext()) {
                final SimpleFeature toWrite = writer.next();

                for (int i = 0; i < type.length; i++) {
                    toWrite.setAttribute(type[i].getName(), value[i]);
                }

                writer.write();
            }

        } finally {
            writer.close();
        }
    }

    /**
     * Calls through to {@link #updateFeatures(org.opengis.feature.type.AttributeDescriptor[], java.lang.Object[], org.opengis.filter.Filter)}.
     */
    @Override
    public final void updateFeatures(final AttributeDescriptor type, final Object value,
            final Filter filter) throws IOException {

        updateFeatures(new AttributeDescriptor[]{type}, new Object[]{value}, filter);
    }

    /**
     * Removes the features from the store which match the specified filter.
     * <p>
     * This method operates by obtaining an updating feature writer based on
     * the specified <tt>filter</tt> and removing every feature from it.
     * </p>
     * <p>
     * The <tt>filter</tt> must not be <code>null</code>, in this case this method
     * will throw an {@link IllegalArgumentException}.
     * </p>
     */
    @Override
    public void removeFeatures(Filter filter) throws IOException {
        if (filter == null) {
            final String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException(msg);
        }
        filter = resolvePropertyNames(filter);

        //grab a feature writer
        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = getWriter(filter, WRITER_UPDATE);
        try {
            //remove everything
            while (writer.hasNext()) {
                writer.next();
                writer.remove();
                writer.write();
            }

        } finally {
            writer.close();
        }
    }

    /**
     * Sets the feature lock of the feature store.
     */
    @Override
    public final void setFeatureLock(final FeatureLock lock) {
        this.lock = lock;
    }

    /**
     * Locks all features.
     * <p>
     * This method calls through to {@link #lockFeatures(Filter)}.
     * </p>
     */
    @Override
    public final int lockFeatures() throws IOException {
        return lockFeatures(Filter.INCLUDE);
    }

    /**
     * Locks features specified by a query.
     * <p>
     * This method calls through to {@link #lockFeatures(Filter)}.
     * </p>
     */
    @Override
    public final int lockFeatures(final Query query) throws IOException {
        return lockFeatures(query.getFilter());
    }

    /**
     * Locks features specified by a filter.
     */
    @Override
    public final int lockFeatures(final Filter filter) throws IOException {
        final Logger logger = getDataStore().getLogger();

        final String typeName = getSchema().getTypeName();

        final FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader(filter);
        try {
            int locked = 0;
            while (reader.hasNext()) {
                SimpleFeature feature = reader.next();
                try {
                    getDataStore().getLockingManager().lockFeatureID(typeName, feature.getID(), transaction, lock);

                    logger.fine("Locked feature: " + feature.getID());
                    locked++;
                } catch (FeatureLockException e) {
                    //ignore
                    final String msg = "Unable to lock feature:" + feature.getID() + "." +
                            " Change logging to FINEST for stack trace";
                    logger.fine(msg);
                    logger.log(Level.FINEST, "Unable to lock feature: " + feature.getID(), e);
                }
            }

            return locked;
        } finally {
            reader.close();
        }
    }

    /**
     * Unlocks all features.
     * <p>
     * This method calls through to {@link #unLockFeatures(Filter)}.
     * </p>
     */
    @Override
    public final void unLockFeatures() throws IOException {
        unLockFeatures(Filter.INCLUDE);
    }

    /**
     * Unlocks features specified by a query.
     * <p>
     * This method calls through to {@link #unLockFeatures(Filter)}.
     * </p>
     */
    @Override
    public final void unLockFeatures(final Query query) throws IOException {
        unLockFeatures(query.getFilter());
    }

    /**
     * Unlocks features specified by a filter.
     */
    @Override
    public final void unLockFeatures(Filter filter) throws IOException {
        filter = resolvePropertyNames(filter);
        final String typeName = getSchema().getTypeName();

        final FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader(filter);
        try {
            while (reader.hasNext()) {
                final SimpleFeature feature = reader.next();
                getDataStore().getLockingManager().unLockFeatureID(typeName, feature.getID(), transaction, lock);
            }
        } finally {
            reader.close();
        }
    }
}
