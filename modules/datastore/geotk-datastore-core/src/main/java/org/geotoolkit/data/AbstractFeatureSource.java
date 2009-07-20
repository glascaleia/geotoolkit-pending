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
package org.geotoolkit.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotoolkit.data.concurrent.Transaction;
import org.geotoolkit.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotoolkit.data.crs.ReprojectFeatureResults;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryCapabilities;
import org.geotoolkit.data.store.EmptyFeatureCollection;
import org.geotoolkit.feature.collection.FeatureCollection;
import org.geotoolkit.feature.SchemaException;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This is a starting point for providing your own FeatureSource<SimpleFeatureType, SimpleFeature> implementation.
 *
 * <p>
 * Subclasses must implement:
 * </p>
 *
 * <ul>
 * <li>
 * getDataStore()
 * </li>
 * <li>
 * getSchema()
 * </li>
 * <li>
 * addFeatureListener()
 * </li>
 * <li>
 * removeFeatureListener()
 * </li>
 * </ul>
 *
 * <p>
 * You may find a FeatureSource<SimpleFeatureType, SimpleFeature> implementations that is more specific to your needs - such as
 * JDBCFeatureSource.
 * </p>
 *
 * <p>
 * For an example of this class customized for use please see MemoryDataStore.
 * </p>
 *
 * @author Jody Garnett, Refractions Research Inc
 * @source $URL$
 */
public abstract class AbstractFeatureSource implements FeatureSource<SimpleFeatureType, SimpleFeature> {

    protected Set hints = Collections.EMPTY_SET;
    protected QueryCapabilities queryCapabilities = new QueryCapabilities();

    public AbstractFeatureSource() {
        // just to keep the default constructor around
    }

    /**
     * Overrides to explicitly type narrow the return type to {@link DataStore}
     */
    @Override
    public abstract DataStore getDataStore();

    /**
     * Returns the same name than the feature type (ie,
     * {@code getSchema().getName()} to honor the simple feature land common
     * practice of calling the same both the Features produces and their types
     * 
     * @since 2.5
     * @see FeatureSource#getName()
     */
    @Override
    public Name getName() {
        return getSchema().getName();
    }

    /**
     * This constructors allows to set the supported hints 
     * @param hints
     */
    public AbstractFeatureSource(final Set hints) {
        this.hints = Collections.unmodifiableSet(new HashSet(hints));
    }

    @Override
    public ResourceInfo getInfo() {
        return new ResourceInfo() {

            final Set<String> words = new HashSet<String>();

            {
                words.add("features");
                words.add(AbstractFeatureSource.this.getSchema().getTypeName());
            }

            @Override
            public JTSEnvelope2D getBounds() {
                try {
                    return AbstractFeatureSource.this.getBounds();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            public CoordinateReferenceSystem getCRS() {
                return AbstractFeatureSource.this.getSchema().getCoordinateReferenceSystem();
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public Set<String> getKeywords() {
                return words;
            }

            @Override
            public String getName() {
                return AbstractFeatureSource.this.getSchema().getTypeName();
            }

            @Override
            public URI getSchema() {
                final Name name = AbstractFeatureSource.this.getSchema().getName();
                try {
                    final URI namespace = new URI(name.getNamespaceURI());
                    return namespace;
                } catch (URISyntaxException e) {
                    return null;
                }
            }

            @Override
            public String getTitle() {
                final Name name = AbstractFeatureSource.this.getSchema().getName();
                return name.getLocalPart();
            }
        };
    }

    public QueryCapabilities getQueryCapabilities() {
        return queryCapabilities;
    }

    /**
     * Retrieve the Transaction this FeatureSource<SimpleFeatureType, SimpleFeature> is operating against.
     *
     * <p>
     * For a plain FeatureSource<SimpleFeatureType, SimpleFeature> that cannot modify this will always be Transaction.AUTO_COMMIT.
     * </p>
     *
     * @return Transacstion FeatureSource<SimpleFeatureType, SimpleFeature> is operating against
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }

    /**
     * Provides an interface to for the Results of a Query.
     *
     * <p>
     * Various queries can be made against the results, the most basic being to retrieve Features.
     * </p>
     *
     * @param query
     *
     *
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    @Override
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(final Query query) throws IOException {
        final SimpleFeatureType schema = getSchema();
        final String typeName = schema.getTypeName();

        if (query.getTypeName() == null) { // typeName unspecified we will "any" use a default
            final DefaultQuery defaultQuery = new DefaultQuery(query);
            defaultQuery.setTypeName(typeName);
        } else if (!typeName.equals(query.getTypeName())) {
            return (FeatureCollection<SimpleFeatureType, SimpleFeature>) new EmptyFeatureCollection(schema);
        }

        final QueryCapabilities queryCapabilities = getQueryCapabilities();
        if (!queryCapabilities.supportsSorting(query.getSortBy())) {
            throw new DataSourceException("DataStore cannot provide the requested sort order");
        }

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = new DefaultFeatureResults(this, query);
        if (collection.getSchema().getGeometryDescriptor() == null) {
            return collection; // no geometry no reprojection needed
        }

        if (false) { // we need to have our CRS forced
            if (query.getCoordinateSystem() != null) {
                try {
                    collection = new ForceCoordinateSystemFeatureResults(collection, query.getCoordinateSystem());
                } catch (SchemaException e) {
                    throw new IOException("Could not force CRS " + query.getCoordinateSystem());
                }
            }
        }
        if (false) { // we need our data reprojected
            if (query.getCoordinateSystemReproject() != null) {
                try {
                    collection = new ReprojectFeatureResults(collection, query.getCoordinateSystemReproject());
                } catch (Exception e) {
                    throw new IOException("Could not reproject to " + query.getCoordinateSystemReproject());
                }
            }
        }
        return collection;
    }

    /**
     * Retrieve all Feature matching the Filter.
     *
     * @param filter Indicates features to retrieve
     *
     * @return FeatureResults indicating features matching filter
     *
     * @throws IOException If results could not be obtained
     */
    @Override
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(final Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(getSchema().getTypeName(), filter));
    }

    /**
     * Retrieve all Features.
     *
     * @return FeatureResults of all Features in FeatureSource
     *
     * @throws IOException If features could not be obtained
     */
    @Override
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures() throws IOException {
        return getFeatures(Filter.INCLUDE);
    }

    /**
     * Retrieve Bounds of all Features.
     *
     * <p>
     * Currently returns null, consider getFeatures().getBounds() instead.
     * </p>
     *
     * <p>
     * Subclasses may override this method to perform the appropriate optimization for this result.
     * </p>
     *
     * @return null representing the lack of an optimization
     *
     * @throws IOException DOCUMENT ME!
     */
    @Override
    public JTSEnvelope2D getBounds() throws IOException {
//        return getBounds(Query.ALL); // DZ should this not return just the bounds for this type?
        return getBounds(getSchema() == null ? Query.ALL : new DefaultQuery(getSchema().getTypeName()));
    }

    /**
     * Retrieve Bounds of Query results.
     *
     * <p>
     * Currently returns null, consider getFeatures( query ).getBounds() instead.
     * </p>
     *
     * <p>
     * Subclasses may override this method to perform the appropriate optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the bounds of
     *
     * @return null representing the lack of an optimization
     *
     * @throws IOException DOCUMENT ME!
     */
    @Override
    public JTSEnvelope2D getBounds(final Query query) throws IOException {
        if (query.getFilter() == Filter.EXCLUDE) {
            return new JTSEnvelope2D(new Envelope(), getSchema().getCoordinateReferenceSystem());
        }

        final DataStore dataStore = getDataStore();

        if ((dataStore == null) || !(dataStore instanceof AbstractDataStore)) {
            // too expensive
            return null;
        } else {
            // ask the abstract data store
            return ((AbstractDataStore) dataStore).getBounds(namedQuery(query));
        }
    }

    /**
     * Ensure query modified with typeName.
     * <p>
     * This method will make copy of the provided query, using
     * DefaultQuery, if query.getTypeName is not equal to
     * getSchema().getTypeName().
     * </p>
     * @param query Original query
     * @return Query with getTypeName() equal to getSchema().getTypeName()
     */
    protected Query namedQuery(final Query query) {
        final String typeName = getSchema().getTypeName();
        if (query.getTypeName() == null ||
                !query.getTypeName().equals(typeName)) {

            return new DefaultQuery(
                    typeName,
                    query.getFilter(),
                    query.getMaxFeatures(),
                    query.getPropertyNames(),
                    query.getHandle());
        }
        return query;
    }

    /**
     * Retrieve total number of Query results.
     *
     * <p>
     * Currently returns -1, consider getFeatures( query ).getCount() instead.
     * </p>
     *
     * <p>
     * Subclasses may override this method to perform the appropriate optimization for this result.
     * </p>
     *
     * @param query Query we are requesting the count of
     *
     * @return -1 representing the lack of an optimization
     */
    @Override
    public int getCount(final Query query) throws IOException {
        if (query.getFilter() == Filter.EXCLUDE) {
            return 0;
        }

        final DataStore dataStore = (DataStore) getDataStore();
        if ((dataStore == null) || !(dataStore instanceof AbstractDataStore)) {
            // too expensive
            return -1;
        }
        // ask the abstract data store
        final Transaction t = getTransaction();

        final int nativeCount = ((AbstractDataStore) dataStore).getCount(namedQuery(query));
        if (nativeCount == -1) {
            return -1;
        }

        //State state = t.getState(dataStore);
        int delta = 0;
        if (t != Transaction.AUTO_COMMIT) {
            if (t.getState(dataStore) == null) {
                return nativeCount;
            }

            if (!(t.getState(dataStore) instanceof TransactionStateDiff)) {
                //we cannot proceed; abort!
                return -1;
            }
            final Diff diff = ((AbstractDataStore) dataStore).state(t).diff(namedQuery(query).getTypeName());
            synchronized (diff) {
                Iterator it = diff.added.values().iterator();
                while (it.hasNext()) {
                    final Object feature = it.next();
                    if (query.getFilter().evaluate(feature)) {
                        delta++;
                    }
                }

                it = diff.modified2.values().iterator();
                while (it.hasNext()) {
                    final Object feature = it.next();

                    if (feature == TransactionStateDiff.NULL && query.getFilter().evaluate(feature)) {
                        delta--;
                    }
                }
            }
        }

        return nativeCount + delta;
    }

    /**
     * By default, no Hints are supported
     */
    @Override
    public Set getSupportedHints() {
        return hints;
    }
}
