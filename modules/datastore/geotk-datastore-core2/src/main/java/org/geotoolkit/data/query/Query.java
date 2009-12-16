/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.query;

import org.geotoolkit.factory.Hints;

import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.feature.type.Name;

/**
 * Encapsulates a data request.
 *
 * <p>
 * The query object is used by the FeatureSource.getFeatures(Query) to
 * encapsulate a request. For this use it the
 * FeatureSource.getSchema().getTypeName() should match the one provided by
 * the Query, or the Query should not provide one.
 * </p>
 *
 * <p>
 * Suggested Extensions (Jody):
 * </p>
 *
 * <ul>
 * <li>
 * Transient CoordianteSystem override done getCoordianteSystem()
 * </li>
 * <li>
 * Transient Geometry reproject to an alternate CoordinateSystem - done
 * getCoordinateSystemReproject()
 * </li>
 * <li>
 * Consider Namespace, FeatueType name override - not done considered evil
 * </li>
 * <li>
 * DataStore.getFeatureReader( Query, Transaction )
 * </li>
 * <li>
 * DataStore.getView( Query ) - prototype in AbstractDataStore (not really
 * ready for primetime, see Expr)
 * </li>
 * </ul>
 *
 *
 * @author Chris Holmes
 * @version $Id$
 * @module pending
 */
public interface Query {

    /**
     * The typeName attribute is used to indicate the name of the feature type
     * to be queried.  If no typename is specified, then the default typeName
     * should be returned from the dataStore.  If the datasstore only supports
     * one feature type then this part of the query may be ignored.
     *
     * @return the name of the feature type to be returned with this query.
     */
    Name getTypeName();

    /**
     * The Filter can be used to define constraints on a query.  If no Filter
     * is present then the query is unconstrained and all feature instances
     * should be retrieved.
     *
     * @return The filter that defines constraints on the query.
     */
    Filter getFilter();

    /**
     * The properties array is used to specify the attributes that should be
     * selected for the return feature collection.
     *
     * <ul>
     * <li>
     * ALL_NAMES: <code>null</code><br>
     * If no properties are specified (getProperties returns ALL_NAMES or
     * null) then the full schema should  be used (all attributes).
     * </li>
     * <li>
     * NO_NAMES: <code>new String[0]</code><br>
     * If getProperties returns an array of size 0, then the datasource should
     * return features with no attributes, only their ids.
     * </li>
     * </ul>
     *
     * <p>
     * The available properties can be determined with a getSchema call from
     * the DataSource interface.  A datasource can use {@link
     * #retrieveAllProperties()} as a shortcut to determine if all its
     * available properties should be returned (same as checking to see if
     * getProperties is ALL_NAMES, but clearer)
     * </p>
     *
     * <p>
     * If properties that are not part of the datasource's schema are requested
     * then the datasource shall throw an exception.
     * </p>
     *
     * <p>
     * This replaces our funky setSchema method of retrieving select
     * properties.  It makes it easier to understand how to get certain
     * properties out of the datasource, instead of having users get the
     * schema and then compose a new schema using the attributes that they
     * want.  The old way had problems because one couldn't have multiple
     * object reuse the same datasource object, since some other object could
     * come along and change its schema, and would then return the wrong
     * properties.
     * </p>
     *
     * @return the attributes to be used in the returned FeatureCollection.
     *
     * @todo : make a FidProperties object, instead of an array size 0.
     *       I think Query.FIDS fills this role to some degree.
     *       Query.FIDS.equals( filter ) would meet this need?
     */
    String[] getPropertyNames();

    /**
     * Convenience method to determine if the query should use the full schema
     * (all properties) of the data source for the features returned.  This
     * method is equivalent to if (query.getProperties() == null), but allows
     * for more clarity on the part of datasource implementors, so they do not
     * need to examine and use null values.  All Query implementations should
     * return true for this function if getProperties returns null.
     *
     * @return if all datasource attributes should be included in the schema of
     *         the returned FeatureCollection.
     */
    boolean retrieveAllProperties();

    Integer getStartIndex();

    /**
     * The optional maxFeatures can be used to limit the number of features
     * that a query request retrieves.  If no maxFeatures is specified then
     * all features should be returned.
     *
     * <p>
     * This is the only method that is not directly out of the Query element in
     * the WFS spec.  It is instead a part of a GetFeature request, which can
     * hold one or more queries.  But each of those in turn will need a
     * maxFeatures, so it is needed here.
     * </p>
     *
     * @return the max features the getFeature call should return.
     */
    Integer getMaxFeatures();

    /**
     * Request data reprojection.
     *
     * <p>
     * Gets the coordinate System to reproject the data contained in the
     * backend datastore to.
     * </p>
     *
     * <p>
     * If the DataStore can optimize the reprojection it should, if not then a
     * decorator on the reader should perform the reprojection on the fly.
     * </p>
     *
     * <p>
     * If the datastore has the wrong CS then {@link #getCoordinateSystem()} should be set to
     * the CS to be used, this will perform the reprojection on that.
     * </p>
     *
     * @return The coordinate system that Features from the datasource should
     *         be reprojected to.
     */
    CoordinateReferenceSystem getCoordinateSystemReproject();

    /**
     * SortBy results according to indicated property and order.
     * <p>
     * SortBy is part of the Filter 1.1 specification, it is referenced
     * by WFS1.1 and Catalog 2.0.x specifications and is used to organize
     * results.
     * </p>
     * The SortBy's are ment to be applied in order:
     * <ul>
     * <li>SortBy( year, ascending )
     * <li>SortBy( month, decsending )
     * </ul>
     * Would produce something like: <pre><code>
     * [year=2002 month=4],[year=2002 month=3],[year=2002 month=2],
     * [year=2002 month=1],[year=2003 month=12],[year=2002 month=4],
     * </code></pre>
     * </p>
     * <p>
     *
     * SortBy should be considered at the same level of abstraction as Filter,
     * and like Filter you may sort using properties not listed in
     * getPropertyNames.
     * </p>
     *
     * <p>
     * At a technical level the interface SortBy2 is used to indicate the
     * additional requirements of a GeoToolkit implementation. The pure
     * WFS 1.1 specification itself is limited to SortBy.
     * </p>
     *
     * @return SortBy array or order of application
     */
    SortBy[] getSortBy();

    /**
     * Specifies some hints to drive the query execution and results build-up.
     * Hints examples can be the GeometryFactory to be used, a generalization
     * distance to be applied right in the data store, to data store specific
     * things such as the fetch size to be used in JDBC queries.
     * The set of hints supported can be fetched by calling
     * {@link FeatureSource#getSupportedHints()}.
     * Depending on the actual values of the hints, the data store is free to ignore them.
     * No mechanism is in place, at the moment, to figure out which hints where
     * actually used during the query execution.
     * @return the Hints the data store should try to use when executing the query
     *         (eventually empty but never null).
     */
    Hints getHints();
    
}