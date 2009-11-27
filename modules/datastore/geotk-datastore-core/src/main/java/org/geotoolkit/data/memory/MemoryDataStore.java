/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.memory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.geotoolkit.data.AbstractDataStore;
import org.geotoolkit.data.DataSourceException;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.concurrent.Transaction;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.collection.FeatureIterator;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This is an example implementation of a DataStore used for testing.
 * 
 * <p>
 * It serves as an example implementation of:
 * </p>
 * 
 * <ul>
 * <li>
 * FeatureListenerManager use: allows handling of FeatureEvents
 * </li>
 * </ul>
 * 
 * <p>
 * This class will also illustrate the use of In-Process locking when the time comes.
 * </p>
 *
 * @author jgarnett
 * @module pending
 */
public final class MemoryDataStore extends AbstractDataStore {

    /** Memory holds Map of Feature by fid by typeName. */
    private final Map<Name, Map<String, SimpleFeature>> memory = new HashMap<Name, Map<String, SimpleFeature>>();

    /** Schema holds FeatureType by typeName */
    private final Map<Name, SimpleFeatureType> schema = new HashMap<Name, SimpleFeatureType>();

    public MemoryDataStore() {
        super(true);
    }

    /**
     * Construct an MemoryDataStore around an empty collection of the provided SimpleFeatureType
     * @param featureType An empty feature collection of this type will be made available
     */
    public static DataStore create(SimpleFeatureType featureType) {
        final MemoryDataStore store = new MemoryDataStore();
        final Map<String, SimpleFeature> featureMap = new HashMap<String, SimpleFeature>();
        final Name typeName = featureType.getName();
        store.schema.put(typeName, featureType);
        store.memory.put(typeName, featureMap);
        return store;
    }

    public static DataStore create(FeatureCollection<SimpleFeatureType, SimpleFeature> collection) {
        final MemoryDataStore store = new MemoryDataStore();
        store.addFeatures(collection);
        return store;
    }

    public static DataStore create(SimpleFeature[] array) {
        final MemoryDataStore store = new MemoryDataStore();
        store.addFeatures(array);
        return store;
    }

    public static DataStore create(FeatureReader<SimpleFeatureType, SimpleFeature> reader) throws IOException {
        final MemoryDataStore store = new MemoryDataStore();
        store.addFeatures(reader);
        return store;
    }

    public static DataStore create(FeatureIterator<SimpleFeature> reader) throws IOException {
        final MemoryDataStore store = new MemoryDataStore();
        store.addFeatures(reader);
        return store;
    }

    /**
     * Configures MemoryDataStore with FeatureReader.
     *
     * @param reader New contents to add
     *
     * @throws IOException If problems are encountered while adding
     * @throws DataSourceException See IOException
     */
    public void addFeatures(FeatureReader<SimpleFeatureType, SimpleFeature> reader) throws IOException {
        try {
            SimpleFeatureType featureType;
            // use an order preserving map, so that features are returned in the same
            // order as they were inserted. This is important for repeatable rendering
            // of overlapping features.
            final Map<String, SimpleFeature> featureMap = new LinkedHashMap<String, SimpleFeature>();
            Name typeName;
            SimpleFeature feature;

            feature = reader.next();

            if (feature == null) {
                throw new IllegalArgumentException("Provided  FeatureReader<SimpleFeatureType, SimpleFeature> is closed");
            }

            featureType = feature.getFeatureType();
            typeName = featureType.getName();

            featureMap.put(feature.getID(), feature);

            while (reader.hasNext()) {
                feature = reader.next();
                featureMap.put(feature.getID(), feature);
            }

            schema.put(typeName, featureType);
            memory.put(typeName, featureMap);
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Problem using reader", e);
        } finally {
            reader.close();
        }
    }

    /**
     * Configures MemoryDataStore with FeatureReader.
     *
     * @param reader New contents to add
     *
     * @throws IOException If problems are encountered while adding
     * @throws DataSourceException See IOException
     */
    public void addFeatures(FeatureIterator<SimpleFeature> reader) throws IOException {
        try {
            SimpleFeatureType featureType;
            final Map<String, SimpleFeature> featureMap = new HashMap<String, SimpleFeature>();
            Name typeName;
            SimpleFeature feature;

            feature = reader.next();

            if (feature == null) {
                throw new IllegalArgumentException("Provided  FeatureReader<SimpleFeatureType, SimpleFeature> is closed");
            }

            featureType = feature.getFeatureType();
            typeName = featureType.getName();

            featureMap.put(feature.getID(), feature);

            while (reader.hasNext()) {
                feature = reader.next();
                featureMap.put(feature.getID(), feature);
            }

            schema.put(typeName, featureType);
            memory.put(typeName, featureMap);
        } finally {
            reader.close();
        }
    }

    /**
     * Configures MemoryDataStore with Collection.
     * 
     * <p>
     * You may use this to create a MemoryDataStore from a FeatureCollection.
     * </p>
     *
     * @param collection Collection of features to add
     *
     * @throws IllegalArgumentException If provided collection is empty
     */
    public void addFeatures(Collection collection) {
        if ((collection == null) || collection.isEmpty()) {
            throw new IllegalArgumentException("Provided FeatureCollection<SimpleFeatureType, SimpleFeature> is empty");
        }

        synchronized (memory) {
            for (final Iterator i = collection.iterator(); i.hasNext();) {
                addFeatureInternal((SimpleFeature) i.next());
            }
        }
    }

    public void addFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Provided FeatureCollection<SimpleFeatureType, SimpleFeature> is empty");
        }
        synchronized (memory) {
            try {
                collection.accepts(new FeatureVisitor() {

                    @Override
                    public void visit(Feature feature) {
                        addFeatureInternal((SimpleFeature) feature);
                    }
                }, null);
            } catch (IOException ignore) {
                getLogger().log(Level.FINE, "Unable to add all features", ignore);
            }
        }
    }

    /**
     * Configures MemoryDataStore with feature array.
     *
     * @param features Array of features to add
     *
     * @throws IllegalArgumentException If provided feature array is empty
     */
    public void addFeatures(SimpleFeature[] features) {
        if ((features == null) || (features.length == 0)) {
            throw new IllegalArgumentException("Provided features are empty");
        }

        synchronized (memory) {
            for (int i = 0; i < features.length; i++) {
                addFeatureInternal(features[i]);
            }
        }
    }

    /**
     * Adds a single Feature to the correct typeName entry.
     * 
     * <p>
     * This is an internal opperation used for setting up MemoryDataStore - please use
     * FeatureWriter for generatl use.
     * </p>
     * 
     * <p>
     * This method is willing to create new FeatureTypes for MemoryDataStore.
     * </p>
     *
     * @param feature Individual feature to add
     */
    public void addFeature(SimpleFeature feature) {
        synchronized (memory) {
            addFeatureInternal(feature);
        }
    }

    private void addFeatureInternal(SimpleFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Provided Feature is empty");
        }

        final SimpleFeatureType featureType = feature.getFeatureType();
        final Name typeName = featureType.getName();

        if (!memory.containsKey(typeName)) {
            try {
                createSchema(featureType);
            } catch (IOException e) {
                // this should not of happened ?!?
                // only happens if typeNames is taken and
                // we just checked                    
            }
        }

        final Map featuresMap = memory.get(typeName);
        featuresMap.put(feature.getID(), feature);
    }

    /**
     * Access featureMap for typeName.
     *
     * @param typeName
     *
     * @return A Map of Features by FID
     *
     * @throws IOException If typeName cannot be found
     */
    protected Map features(Name typeName) throws IOException {
        synchronized (memory) {
            if (memory.containsKey(typeName)) {
                return (Map) memory.get(typeName);
            }
        }

        throw new IOException("Type name " + typeName + " not found");
    }

    /**
     * Adds support for a new featureType to MemoryDataStore.
     * 
     * <p>
     * FeatureTypes are stored by typeName, an IOException will be thrown if the requested typeName
     * is already in use.
     * </p>
     *
     * @param featureType SimpleFeatureType to be added
     *
     * @throws IOException If featureType already exists
     *
     * @see DataStore#createSchema(org.opengis.feature.type.FeatureType)
     */
    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        final Name typeName = featureType.getName();

        if (memory.containsKey(typeName)) {
            // we have a conflict
            throw new IOException(typeName + " already exists");
        }
        // insertion order preserving map
        final Map featuresMap = new LinkedHashMap();
        schema.put(typeName, featureType);
        memory.put(typeName, featuresMap);
    }

    /**
     * Provides FeatureWriter over the entire contents of <code>typeName</code>.
     * 
     * <p>
     * Implements getFeatureWriter contract for AbstractDataStore.
     * </p>
     *
     * @param typeName name of FeatureType we wish to modify
     *
     * @return FeatureWriter of entire contents of typeName
     *
     * @throws IOException If writer cannot be obtained for typeName
     * @throws DataSourceException See IOException
     *
     * @see org.geotoolkit.data.AbstractDataStore#getFeatureSource(java.lang.String)
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> createFeatureWriter(final String typeName, final Transaction transaction)
            throws IOException {
        return new FeatureWriter<SimpleFeatureType, SimpleFeature>() {

            SimpleFeatureType featureType = getSchema(typeName);
            Map contents = features(featureType.getName());
            Iterator iterator = contents.values().iterator();
            SimpleFeature live = null;
            SimpleFeature current = null; // current Feature returned to user

            @Override
            public SimpleFeatureType getFeatureType() {
                return featureType;
            }

            @Override
            public SimpleFeature next() throws IOException, NoSuchElementException {
                if (hasNext()) {
                    // existing content
                    live = (SimpleFeature) iterator.next();

                    try {
                        current = SimpleFeatureBuilder.copy(live);
                    } catch (IllegalAttributeException e) {
                        throw new DataSourceException("Unable to edit " + live.getID() + " of " + typeName);
                    }
                } else {
                    // new content
                    live = null;

                    try {
                        current = SimpleFeatureBuilder.template(featureType, null);
                    } catch (IllegalAttributeException e) {
                        throw new DataSourceException("Unable to add additional Features of " + typeName);
                    }
                }

                return current;
            }

            @Override
            public void remove() throws IOException {
                if (contents == null) {
                    throw new IOException("FeatureWriter has been closed");
                }

                if (current == null) {
                    throw new IOException("No feature available to remove");
                }

                if (live != null) {
                    // remove existing content
                    iterator.remove();
                    listenerManager.fireFeaturesRemoved(typeName, transaction,
                            new JTSEnvelope2D(live.getBounds()), true);
                    live = null;
                    current = null;
                } else {
                    // cancel add new content
                    current = null;
                }
            }

            @Override
            public void write() throws IOException {
                if (contents == null) {
                    throw new IOException("FeatureWriter has been closed");
                }

                if (current == null) {
                    throw new IOException("No feature available to write");
                }

                if (live != null) {
                    if (live.equals(current)) {
                        // no modifications made to current
                        //
                        live = null;
                        current = null;
                    } else {
                        // accept modifications
                        //
                        try {
                            live.setAttributes(current.getAttributes());
                        } catch (Exception e) {
                            throw new DataSourceException("Unable to accept modifications to " + live.getID() + " on " + typeName);
                        }

                        final JTSEnvelope2D bounds = new JTSEnvelope2D();
                        bounds.expandToInclude(new JTSEnvelope2D(live.getBounds()));
                        bounds.expandToInclude(new JTSEnvelope2D(current.getBounds()));
                        listenerManager.fireFeaturesChanged(typeName, transaction,
                                bounds, true);
                        live = null;
                        current = null;
                    }
                } else {
                    // add new content
                    //
                    contents.put(current.getID(), current);
                    listenerManager.fireFeaturesAdded(typeName, transaction,
                            new JTSEnvelope2D(current.getBounds()), true);
                    current = null;
                }
            }

            @Override
            public boolean hasNext() throws IOException {
                if (contents == null) {
                    throw new IOException("FeatureWriter has been closed");
                }

                return (iterator != null) && iterator.hasNext();
            }

            @Override
            public void close() {
                if (iterator != null) {
                    iterator = null;
                }

                if (featureType != null) {
                    featureType = null;
                }

                contents = null;
                current = null;
                live = null;
            }
        };
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected JTSEnvelope2D getBounds(Query query)
            throws IOException {
        final Name typeName = query.getTypeName();
        final Map contents = features(typeName);
        final Iterator iterator = contents.values().iterator();

        JTSEnvelope2D envelope = null;

        if (iterator.hasNext()) {
            int count = 1;
            final Filter filter = query.getFilter();
            final SimpleFeature first = (SimpleFeature) iterator.next();
            final Envelope env = ((Geometry) first.getDefaultGeometry()).getEnvelopeInternal();
            envelope = new JTSEnvelope2D(env, first.getType().getCoordinateReferenceSystem());

            final Integer max = query.getMaxFeatures();
            while (iterator.hasNext() && ( max == null || count < max)) {
                final SimpleFeature feature = (SimpleFeature) iterator.next();

                if (filter.evaluate(feature)) {
                    count++;
                    envelope.expandToInclude(((Geometry) feature.getDefaultGeometry()).getEnvelopeInternal());
                }
            }
        }

        return envelope;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected int getCount(Query query)
            throws IOException {
        final Name typeName = query.getTypeName();
        final Map contents = features(typeName);
        final Iterator iterator = contents.values().iterator();

        int count = 0;

        final Filter filter = query.getFilter();

        final Integer max = query.getMaxFeatures();
        while (iterator.hasNext() && ( max == null || count < max)) {
            if (filter.evaluate((SimpleFeature) iterator.next())) {
                count++;
            }
        }

        return count;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected Map<Name, SimpleFeatureType> getTypes() throws IOException {
        synchronized (memory) {
            return Collections.unmodifiableMap(schema);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query) throws IOException {
        final Name typeName = query.getTypeName();
        return new FeatureReader<SimpleFeatureType, SimpleFeature>() {

            SimpleFeatureType featureType = getSchema(typeName);
            Iterator iterator = features(typeName).values().iterator();

            @Override
            public SimpleFeatureType getFeatureType() {
                return featureType;
            }

            @Override
            public SimpleFeature next()
                    throws IOException, IllegalAttributeException, NoSuchElementException {
                if (iterator == null) {
                    throw new IOException("Feature Reader has been closed");
                }

                try {
                    return SimpleFeatureBuilder.copy((SimpleFeature) iterator.next());
                } catch (NoSuchElementException end) {
                    throw new DataSourceException("There are no more Features", end);
                }
            }

            @Override
            public boolean hasNext() {
                return (iterator != null) && iterator.hasNext();
            }

            @Override
            public void close() {
                if (iterator != null) {
                    iterator = null;
                }

                if (featureType != null) {
                    featureType = null;
                }
            }
        };
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
        throw new IOException("Update schema not supported.");
    }

}
