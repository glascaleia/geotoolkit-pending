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
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.geotoolkit.data.FeatureListener;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureSource;
import org.geotoolkit.data.FilteringFeatureReader;
import org.geotoolkit.data.MaxFeatureReader;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryCapabilities;
import org.geotoolkit.data.ReTypeFeatureReader;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.concurrent.Transaction;
import org.geotoolkit.data.crs.ReprojectFeatureReader;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.query.QueryUtilities;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;
import org.geotoolkit.filter.visitor.PropertyNameResolvingVisitor;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.util.NullProgressListener;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * Abstract implementation of FeatureSource.
 * <p>
 * This feature source works off of operations provided by {@link FeatureCollection}.
 * </p>
 * <p>
 * Even though a feature source is read-only, this class is transaction aware.
 * (see {@link #setTransaction(org.geotoolkit.data.concurrent.Transaction)}. The transaction is taken into
 * account during operations such as {@link #getCount(org.geotoolkit.data.query.Query)} and {@link #getBounds()}
 * since these values may be affected by another operation (like writing to 
 * a FeautreStore) working against the same transaction. 
 * </p>
 * <p>
 * Subclasses must also implement the {@link #buildFeatureType()} method which 
 * builds the schema for the feature source.
 * </p>
 *
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 * @module pending
 */
public abstract class ContentFeatureSource implements FeatureSource<SimpleFeatureType, SimpleFeature> {

    /**
     * The entry for the feature source.
     */
    protected final ContentEntry entry;
    /**
     * The transaction to work from
     */
    protected Transaction transaction;
    /**
     * hints
     */
    protected Set<Hints.Key> hints;
    /**
     * The query defining the feature source
     */
    protected final Query query;
    /**
     * cached feature type (only set if this instance is a view)
     */
    protected SimpleFeatureType schema;
    /**
     * The query capabilities returned by this feature source
     */
    protected QueryCapabilities queryCapabilities;

    /**
     * Creates the new feature source from a query.
     * <p>
     * The <tt>query</tt> is taken into account for any operations done against
     * the feature source. For example, when getReader(Query) is called the 
     * query specified is "joined" to the query specified in the constructor. 
     * The <tt>query</tt> parameter may be <code>null</code> to specify that the 
     * feature source represents the entire set of features. 
     * </p>
     */
    public ContentFeatureSource(final ContentEntry entry, final Query query) {
        this.entry = entry;
        this.query = query;

        //set up hints
        hints = new HashSet<Hints.Key>();
//        hints.add( Hints.JTS_GEOMETRY_FACTORY );
//        hints.add( Hints.JTS_COORDINATE_SEQUENCE_FACTORY );

        //add subclass specific hints
        addHints(hints);

        //make hints unmodifiable
        hints = Collections.unmodifiableSet(hints);
    }

    /**
     * The entry for the feature source.
     */
    public ContentEntry getEntry() {
        return entry;
    }

    /**
     * The current transaction the feature source is working against.
     * <p>
     * This transaction is used to derive the state for the feature source. A
     * <code>null</code> value for a transaction represents the auto commit
     * transaction: {@link Transaction#AUTO_COMMIT}.
     * </p>
     * @see #getState() 
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * Sets the current transaction the feature source is working against.
     * <p>
     * <tt>transaction</tt> may be <code>null</code>. This signifies that the 
     * auto-commit transaction is used: {@link Transaction#AUTO_COMMIT}.
     * </p>
     * @param transaction The new transaction, or <code>null</code>.
     */
    public void setTransaction(final Transaction transaction) {
        this.transaction = transaction;
    }

    /**
     * The current state for the feature source.
     * <p>
     * This value is derived from current transaction of the feature source.
     * </p>
     * 
     * @see #setTransaction(org.geotoolkit.data.concurrent.Transaction)
     */
    public ContentState getState() {
        return entry.getState(transaction);
    }

    /**
     * The datastore that this feature source originated from.
     * <p>
     * Subclasses may wish to extend this method in order to type narrow its 
     * return type.
     * </p>
     */
    @Override
    public ContentDataStore getDataStore() {
        return entry.getDataStore();
    }

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
     * Returns the feature type or the schema of the feature source.
     * <p>
     * This method delegates to {@link #buildFeatureType()}, which must be 
     * implemented by subclasses. The result is cached in 
     * {@link ContentState#getFeatureType()}.
     * </p>
     */
    @Override
    public final SimpleFeatureType getSchema() {

        //check schema override 
        if (schema != null) {
            return schema;
        }

        final SimpleFeatureType featureType = getAbsoluteSchema();

        //this may be a view
        if (query != null && !query.retrieveAllProperties()) {
            synchronized (this) {
                if (schema == null) {
                    schema = SimpleFeatureTypeBuilder.retype(featureType, query.getPropertyNames());
                }
            }

            return schema;
        }

        return featureType;
    }

    /**
     * Helper method for returning the underlying schema of the feature source.
     * This is a non-view this is the same as calling getSchema(), but in the 
     * view case the underlying "true" schema is returned. 
     */
    protected final SimpleFeatureType getAbsoluteSchema() {
        //load the type from the state shared among feature sources
        final ContentState state = entry.getState(transaction);
        SimpleFeatureType featureType = state.getFeatureType();

        if (featureType == null) {
            //build and cache it
            synchronized (state) {
                try {
                    featureType = buildFeatureType();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                state.setFeatureType(featureType);
            }
        }

        return featureType;
    }

    /**
     * Returns the bounds of the entire feature source.
     * <p>
     * This method delegates to {@link #getBounds(Query)}:
     * <pre>
     *   <code>return getBounds(Query.ALL)</code>.
     * </pre>
     * </p>
     */
    @Override
    public final JTSEnvelope2D getBounds() throws IOException {

        //return all(entry.getState(transaction)).getBounds();
        return getBounds(QueryBuilder.all(new DefaultName(entry.getTypeName())));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public JTSEnvelope2D getBounds(final Filter filter) throws IOException {
        return getBounds(QueryBuilder.filtered(getSchema().getName(), filter));
    }

    /**
     * Returns the bounds of the results of the specified query against the 
     * feature source.
     * <p>
     * This method calls through to {@link #getBoundsInternal(Query)} which 
     * subclasses must implement. It also contains optimizations which check 
     * state for cached values.
     * </p>
     */
    @Override
    public final JTSEnvelope2D getBounds(Query query) throws IOException {
        query = joinQuery(query);
        query = resolvePropertyNames(query);
        /*
        if ( query == Query.ALL ) {
        //check the cache
        //TODO: there should be a check for a view here
        if ( getState().getBounds() != null ) {
        return getState().getBounds();
        }
        }
         */

        //
        //calculate the bounds
        //
        final JTSEnvelope2D bounds = getBoundsInternal(query);

        /*
        if ( query == Query.ALL ) {
        //update the cache
        synchronized (getState()) {
        getState().setBounds(bounds);
        }
        }
         */
        return bounds;
    }

    /**
     * Calculates the bounds of a specified query. Subclasses must implement this
     * method.
     */
    protected abstract JTSEnvelope2D getBoundsInternal(final Query query) throws IOException;

    /**
     * {@inheritDoc }
     */
    @Override
    public int getCount() throws IOException {
        return getCount(Filter.INCLUDE);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getCount(final Filter filter) throws IOException {
        return getCount(QueryBuilder.filtered(getSchema().getName(), filter));
    }

    /**
     * Returns the count of the number of features of the feature source.
     * <p>
     * This method calls through to {@link #getCount(Query)} which 
     * subclasses must implement. It also contains optimizations which check 
     * state for cached values.
     * </p>
     */
    @Override
    public final int getCount(Query query) throws IOException {
        query = joinQuery(query);
        query = resolvePropertyNames(query);
        /*
        if ( query == Query.ALL ) {
        //check the cache
        if ( getState().getCount() != -1 ) {
        return getState().getCount();
        }
        }
         */

        //calculate the count
        //TODO: figure out if we need to calculate manually based on canFilter
        final int count = getCountInternal(query);

        /*
        if ( query == Query.ALL ) {
        //update the cache
        synchronized (getState()) {
        getState().setCount( count );
        }
        }
         */

        return count;
    }
    //    return filtered(entry.getState(transaction), query.getFilter()).size();
    //}

    /**
     * Calculates the number of features of a specified query. Subclasses must 
     * implement this method.
     */
    protected abstract int getCountInternal(final Query query) throws IOException;

    /**
     * Returns the feature collection of all the features of the feature source.
     */
    @Override
    public final ContentFeatureCollection getFeatures() throws IOException {
        final Query query = joinQuery(QueryBuilder.all(new DefaultName(entry.getTypeName())));
        return new ContentFeatureCollection(this, query);
        //return getFeatures(Query.ALL);
    }

    /**
     * Returns a feature reader for all features.
     * <p>
     * This method calls through to {@link #getReader(Query)}.
     * </p>
     */
    public final FeatureReader<SimpleFeatureType, SimpleFeature> getReader() throws IOException {
        return getReader(QueryBuilder.all(new DefaultName(entry.getTypeName())));
    }

    /**
     * Returns the feature collection if the features of the feature source which 
     * meet the specified query criteria.
     */
    @Override
    public final ContentFeatureCollection getFeatures(Query query) throws IOException {
        query = joinQuery(query);

        return new ContentFeatureCollection(this, query);
    }

    /**
     * Returns a reader for the features specified by a query.
     */
    public final FeatureReader<SimpleFeatureType, SimpleFeature> getReader(Query query) throws IOException {
        query = joinQuery(query);
        query = resolvePropertyNames(query);

        // see if we need to enable native sorting in order to support stable paging
        final int offset = query.getStartIndex() != null ? query.getStartIndex() : 0;
        if (offset > 0 & query.getSortBy() == null) {
            if (!getQueryCapabilities().supportsSorting(query.getSortBy())) {
                throw new IOException("Feature source does not support this sorting " +
                        "so there is no way a stable paging (offset/limit) can be performed");
            }

            query = new QueryBuilder().copy(query).setSortBy(new SortBy[]{SortBy.NATURAL_ORDER}).buildQuery();
        }

        FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReaderInternal(query);

        //
        //apply wrappers based on subclass capabilities
        //
        //filtering
        if (!canFilter()) {
            if (query.getFilter() != null && query.getFilter() != Filter.INCLUDE) {
                reader = new FilteringFeatureReader<SimpleFeatureType, SimpleFeature>(reader, query.getFilter());
            }
        }

        // reprojection
        if (!canReproject()) {
            if (query.getCoordinateSystemReproject() != null) {
                try {
                    reader = new ReprojectFeatureReader(reader, query.getCoordinateSystemReproject());
                } catch (Exception e) {
                    if (e instanceof IOException) {
                        throw (IOException) e;
                    } else {
                        throw (IOException) new IOException("Error occurred trying to reproject data").initCause(e);
                    }
                }
            }
        }

        // offset
        if (!canOffset() && offset > 0) {
            // skip the first n records
            for (int i = 0; i < offset && reader.hasNext(); i++) {
                reader.next();
            }
        }

        // max feature limit
        if (!canLimit()) {
            if (query.getMaxFeatures() != -1 && query.getMaxFeatures() < Integer.MAX_VALUE) {
                reader = new MaxFeatureReader<SimpleFeatureType, SimpleFeature>(reader, query.getMaxFeatures());
            }
        }

        //sorting
        if (!canSort()) {
            if (query.getSortBy() != null && query.getSortBy().length != 0) {
                throw new UnsupportedOperationException("sorting unsupported");
            }
        }

        //retyping
        if (!canRetype()) {
            if (!query.retrieveAllProperties()) {
                //rebuild the type and wrap the reader
                final SimpleFeatureType target =
                        SimpleFeatureTypeBuilder.retype(getSchema(), query.getPropertyNames());

                // do an equals check because we may have needlessly retyped (that is,
                // the subclass might be able to only partially retype)
                if (!target.equals(reader.getFeatureType())) {
                    reader = new ReTypeFeatureReader(reader, target, false);
                }
            }
        }

        return reader;
    }

    /**
     * Visit the features matching the provided query.
     * <p>
     * The default information will use getReader( query ) and pass each feature to the provided visitor.
     * Subclasses should override this method to optimise common visitors.
     * Often in the case of Filter.INCLUDES the information can be determined from a file header or metadata table.
     * <p>
     * 
     * @param visitor Visitor called for each feature 
     * @param progress Used to report progress; and errors on a feature by feature basis
     * @throws IOException
     */
    public void accepts(final Query query, final org.opengis.feature.FeatureVisitor visitor,
            org.opengis.util.ProgressListener progress) throws IOException {

        if (progress == null) {
            progress = new NullProgressListener();
        }

        final FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader(query);
        try {
            float size = (progress instanceof NullProgressListener) ? 0.0f : (float) getCount(query);
            float position = 0;
            progress.started();
            while (reader.hasNext()) {
                if (size > 0) {
                    progress.progress(position++ / size);
                }
                try {
                    final SimpleFeature feature = reader.next();
                    visitor.visit(feature);
                } catch (Exception erp) {
                    progress.exceptionOccurred(erp);
                }
            }
        } finally {
            progress.complete();
            reader.close();
        }
    }

    /**
     * Subclass method for returning a native reader from the datastore.
     * <p>
     * It is important to note that if the native reader intends to handle any 
     * of the following natively:
     * <ul>
     *   <li>reprojection</li>
     *   <li>filtering</li>
     *   <li>max feature limiting</li>
     *   <li>sorting<li>
     * </ul>
     * Then it <b>*must*</b> set the corresonding flags to <code>true</code>:
     * <ul>
     *   <li>{@link #canReproject()}</li>
     *   <li>{@link #canFilter()}</li>
     *   <li>{@link #canLimit()}</li>
     *   <li>{@link #canSort()}<li>
     * </ul>
     * </p>
     * 
     */
    protected abstract FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(final Query query) throws IOException;

    /**
     * Determines if the datastore can natively perform reprojection..
     * <p>
     * If the subclass can handle reprojection natively then it should override
     * this method to return <code>true</code>. In this case it <b>must</b> do 
     * the reprojection or throw an exception. 
     * </p>
     * <p>
     * Not overriding this method or returning <code>false</code> will case the
     * feature reader created by the subclass to be wrapped in a reprojecting 
     * decorator when the query specifies a coordinate system reproject.
     * </p>
     * TODO: link to decorating feature reader
     */
    protected boolean canReproject() {
        return false;
    }

    /**
     * Determines if the datastore can natively limit the number of features 
     * returned in a query.
     * <p>
     * If the subclass can handle a map feature cap natively then it should override
     * this method to return <code>true</code>. In this case it <b>must</b> do 
     * the cap or throw an exception. 
     * </p>
     * <p>
     * Not overriding this method or returning <code>false</code> will case the
     * feature reader created by the subclass to be wrapped in a max feature capping 
     * decorator when the query specifies a max feature cap. 
     * </p>
     * @see MaxFeatureReader
     */
    protected boolean canLimit() {
        return false;
    }

    /**
     * Determines if the datastore can natively skip the first <code>offset</code> number of features 
     * returned in a query.
     * <p>
     * If the subclass can handle a map feature cap natively then it should override
     * this method to return <code>true</code>. In this case it <b>must</b> do 
     * the cap or throw an exception. 
     * </p>
     * <p>
     * Not overriding this method or returning <code>false</code> will case the
     * feature reader created by the subclass to be be accesset offset times before
     * being returned to the caller. 
     * </p>
     */
    protected boolean canOffset() {
        return false;
    }

    /**
     * Determines if the datastore can natively perform a filtering.
     * <p>
     * If the subclass can handle filtering natively it should override this method 
     * to return <code>true</code>. In this case it <b>must</b> do the filtering
     * or throw an exception. This includes the case of partial native filtering
     * where the datastore can only handle part of the filter natively. In these
     * cases it is up to the subclass to apply a decorator to the reader it returns
     * which will handle any part of the filter can was not applied natively. See
     * {@link FilteringFeatureReader}. 
     * </p>
     * <p>
     * Not overriding this method or returning <code>false</code> will cause the
     * feature reader created by the subclass to be wrapped in a filtering feature
     * reader when the query specifies a filter. See {@link FilteringFeatureReader}.
     * </p>
     */
    protected boolean canFilter() {
        return false;
    }

    /**
     * Determines if the datasatore can natively perform "retyping" which includes 
     * limiting the number of attributes returned and reordering of those attributes
     * <p>
     * If the subclass can handle retyping natively it should override this method 
     * to return <code>true</code>. In this case it <b>must</b> do the retyping
     * or throw an exception. 
     * </p>
     * <p>
     * Not overriding this method or returning <code>false</code> will cause the
     * feature reader created by the subclass to be wrapped in a retyping feature
     * reader when the query specifies a retype.
     * </p>
     * TODO: link to feature decorator
     */
    protected boolean canRetype() {
        return false;
    }

    /**
     * Determines if the datastore can natively perform sorting.
     * <p>
     * If the subclass can handle retyping natively it should override this method 
     * to return <code>true</code>. In this case it <b>must</b> do the retyping
     * or throw an exception. 
     * </p>
     * <p>
     * Not overriding this method or returning <code>false</code> will cause an 
     * exception to be thrown when the query specifies sorting.
     */
    protected boolean canSort() {
        return false;
    }

    /**
     * Creates a new feature source for the specified query.
     * <p>
     * If the current feature source already has a defining query it is joined
     * to the specified query.
     * </p>
     * @param query
     * @return ContentFeatureSource
     * @throws IOException
     */
    public final ContentFeatureSource getView(Query query) throws IOException {
        query = joinQuery(query);
        query = resolvePropertyNames(query);

        //reflectively create subclass
        final Class clazz = getClass();

        try {
            final Constructor c = clazz.getConstructor(ContentEntry.class, Query.class);
            final ContentFeatureSource source = (ContentFeatureSource) c.newInstance(getEntry(), query);

            //set the transaction
            source.setTransaction(transaction);
            return source;
        } catch (Exception e) {
            String msg = "Subclass must implement Constructor(ContentEntry,Query)";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    /**
     * Returns the feature collection for the features which match the specified
     * filter.
     * <p>
     * This method calls through to {@link #getFeatures(Query)}.
     * </p>
     */
    @Override
    public final ContentFeatureCollection getFeatures(final Filter filter)
            throws IOException {
        return getFeatures(QueryBuilder.filtered(getSchema().getName(), filter));
    }

    /**
     * Returns a reader for features specified by a particular filter.
     * <p>
     * This method calls through to {@link #getReader(Query)}.
     * </p>
     */
    public final FeatureReader<SimpleFeatureType, SimpleFeature> getReader(final Filter filter)
            throws IOException {
        return getReader(QueryBuilder.filtered(getSchema().getName(), filter));
    }

    public final ContentFeatureSource getView(Filter filter) throws IOException {
        return getView(QueryBuilder.filtered(getSchema().getName(), filter));
    }

    /**
     * Adds an listener or observer to the feature source.
     * <p>
     * Listeners are stored on a per-transaction basis. 
     * </p>
     */
    @Override
    public final void addFeatureListener(final FeatureListener listener) {
        entry.getState(transaction).addListener(listener);
    }

    /**
     * Removes a listener from the feature source.
     */
    @Override
    public final void removeFeatureListener(final FeatureListener listener) {
        entry.getState(transaction).removeListener(listener);
    }

    /**
     * The hints provided by the feature store.
     * <p>
     * Subclasses should implement {@link #addHints(Set)} to provide additional
     * hints.
     * </p>
     * 
     * @see FeatureSource#getSupportedHints()
     */
    @Override
    public final Set getSupportedHints() {
        return hints;
    }
    //
    // Internal API
    //

    /**
     * Subclass hook too add additional hints.
     * <p>
     * By default, the followings are already present:
     * <ul>
     *   <li>{@link HintsPending#JTS_COORDINATE_SEQUENCE_FACTORY}
     *   <li>{@link HintsPending#JTS_GEOMETRY_FACTORY}
     * </ul>
     * 
     * </p>
     * @param hints The set of hints supported by the feature source.
     */
    protected void addHints(final Set<Hints.Key> hints) {
    }

    /**
     * Convenience method for joining a query with the definining query of the 
     * feature source.
     */
    protected Query joinQuery(final Query query) {
        // if the defining query is unset or ALL just return the query passed in
        if (this.query == null || this.query.equals(QueryBuilder.all(new DefaultName(entry.getTypeName())))) {
            return query;
        }

        // if the query passed in is unset or ALL return the defining query
        if (query == null) {
            return this.query;
        }

        // join the queries
        return QueryUtilities.mixQueries(this.query, query, null);
    }

    /**
     * This method changes the query object so that all propertyName references are resolved
     * to simple attribute names against the schema of the feature source.
     * <p>
     * For example, this method ensures that propertyName's such as "gml:name" are rewritten as
     * simply "name".
     *</p>
     */
    protected Query resolvePropertyNames(final Query query) {
        Filter resolved = resolvePropertyNames(query.getFilter());
        if (resolved == query.getFilter()) {
            return query;
        }

        return new QueryBuilder().copy(query).setFilter(resolved).buildQuery();
    }

    /**
     * Transform provided filter; resolving property names
     */
    protected Filter resolvePropertyNames(final Filter filter) {
        if (filter == null || filter == Filter.INCLUDE || filter == Filter.EXCLUDE) {
            return filter;
        }

        return (Filter) filter.accept(
                new PropertyNameResolvingVisitor(getSchema()), null);
    }

    /**
     * Creates the feature type or schema for the feature source.
     * <p>
     * Implementations should use {@link SimpleFeatureTypeBuilder} to build the 
     * feature type. Also, the builder should be injected with the feature factory
     * which has been set on the datastore (see {@link ContentDataStore#getFeatureFactory()}.
     * Example:
     * <pre>
     *   SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
     *   b.setFeatureTypeFactory( getDataStore().getFeatureTypeFactory() );
     *   
     *   //build the feature type
     *   ...
     * </pre>
     * </p>
     */
    protected abstract SimpleFeatureType buildFeatureType() throws IOException;

    /**
     * Builds the query capabilities for this feature source. The default 
     * implementation returns a newly built QueryCapabilities, subclasses
     * are advised to build their own.
     * @return QueryCapabilities
     * @throws IOException
     */
    protected QueryCapabilities buildQueryCapabilities() {
        return new QueryCapabilities();
    }

    @Override
    public QueryCapabilities getQueryCapabilities() {
        // lazy initialization, so that the subclass has all its data structures ready
        // when the method is called (it might need to consult them in order to decide
        // what query capabilities are really supported)
        if (queryCapabilities == null) {
            queryCapabilities = buildQueryCapabilities();
        }
        return queryCapabilities;
    }
}
