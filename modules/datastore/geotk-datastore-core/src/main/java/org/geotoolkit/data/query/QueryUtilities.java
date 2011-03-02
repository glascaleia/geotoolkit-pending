/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2010, Geomatys
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.DataStoreJoinFeatureCollection;
import org.geotoolkit.data.DefaultJoinFeatureCollection;
import org.geotoolkit.data.DefaultSelectorFeatureCollection;
import org.geotoolkit.data.DefaultTextStmtFeatureCollection;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.util.NullArgumentException;

import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class QueryUtilities {

    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);
    
    private QueryUtilities(){}

    /**
     * A source is considered absolute when all selector in the source have
     * a session defined. That implies we can use a query with this source
     * directly on a EvaluatedFeatureCollection.
     * 
     * @param source
     * @return true if the source is absolute
     */
    public static boolean isAbsolute(final Source source){
        if(source instanceof Join){
            final Join j = (Join) source;
            return isAbsolute(j.getLeft()) && isAbsolute(j.getRight());
        }else if (source instanceof Selector){
            return ((Selector)source).getSession() != null;
        }else if (source instanceof TextStatement){
            return ((TextStatement)source).getSession() != null;
        }else{
            throw new IllegalStateException("Source type is unknowned : " + source +
                    "\n valid types ares Join and Selector");
        }
    }

    /**
     * When a source is not yet absolute, you can reconfigure it to be so.
     * every Selector that doesn't have a session configure will be replaced by
     * the given one.
     *
     * @param source
     * @param session
     * @return an absolute source
     */
    public static Source makeAbsolute(final Source source, final Session session){
        
        final Source absolute;
        if(source instanceof Join){
            final Join j = (Join) source;

            if(isAbsolute(j)){
                absolute = j;
            }else{
                final Source left = makeAbsolute(j.getLeft(), session);
                final Source right = makeAbsolute(j.getLeft(), session);
                absolute = new DefaultJoin(left, right, j.getJoinType(), j.getJoinCondition());
            }
        }else if (source instanceof Selector){
            final Selector select = (Selector) source;
            if (select.getSession() == null){
                if(session == null){
                    throw new NullPointerException("Session can not be null.");
                }

                absolute = new DefaultSelector(session, select.getFeatureTypeName(), select.getSelectorName());
            }else{
                absolute = source;
            }
        }else if (source instanceof TextStatement){
            final TextStatement select = (TextStatement) source;
            if (select.getSession() == null){
                if(session == null){
                    throw new NullPointerException("Session can not be null.");
                }

                absolute = new DefaultTextStatement(select.getStatement(), session);
            }else{
                absolute = source;
            }
        }else{
            throw new IllegalStateException("Source type is unknowned : " + source +
                    "\n valid types ares Join and Selector");
        }

        return absolute;
    }

    public static Query makeAbsolute(final Query query, final Session session){
        Source source = query.getSource();
        if(isAbsolute(source)){
            //nothing to change, query is absolute already
            return query;
        }

        source = makeAbsolute(source, session);
        QueryBuilder qb = new QueryBuilder(query);
        qb.setSource(source);
        return qb.buildQuery();
    }

    public static FeatureCollection evaluate(final String id, final Query query){
        return evaluate(id,query, null);
    }

    /**
     * Create a feature collection for the given query.
     * This method will try to use the datastore query capabilities if several
     * selector/join source use the same Session.
     * Otherwise a generic (slower) implementation will be returned.
     *
     * @param id : feature collection id.
     * @param query : query
     * @param session : use this session if the query is not absolute
     * @return feature collection
     */
    public static FeatureCollection evaluate(final String id, Query query, final Session session){
        query = QueryUtilities.makeAbsolute(query, session);


        final String language = query.getLanguage();

        if(query.GEOTK_QOM.equalsIgnoreCase(language)){
            final Source s = query.getSource();
            if(s instanceof Selector){
                return new DefaultSelectorFeatureCollection(id, query);
            }else if(s instanceof Join){
                final Collection<Session> sessions = getSessions(s, null);

                if(sessions.size() == 1 && sessions.iterator().next().getDataStore().getQueryCapabilities().handleCrossQuery()){
                    //the datastore can handle our join query, it will be much more efficient then a generic implementation
                    return new DataStoreJoinFeatureCollection(id, query);
                }else{
                    //can't optimize it, use the generic implementation
                    return new DefaultJoinFeatureCollection(id, query);
                }
            }else{
                throw new IllegalArgumentException("Query source is an unknowned type : " + s);
            }
        }else{
            //custom language query, let the datastore handle it
            return new DefaultTextStmtFeatureCollection(id, query);
        }
    }

    /**
     * Explore all source and check that the type is writable.
     * 
     * @param source
     * @return true if all source are writable
     */
    public static boolean isWritable(final Source source) throws DataStoreException{
        if(source instanceof Join){
            final Join j = (Join) source;
            return isWritable(j.getLeft()) && isWritable(j.getRight());

        }else if(source instanceof Selector){
            final Selector select = (Selector) source;
            final Session session = select.getSession();

            if(session == null){
                throw new IllegalArgumentException("Source must be absolute to verify if it's writable");
            }
            
            return session.getDataStore().isWritable(select.getFeatureTypeName());
        }else if(source instanceof TextStatement){
            return false;
        }else{
            throw new IllegalStateException("Source type is unknowned : " + source +
                    "\n valid types ares Join and Selector");
        }
    }

    /**
     * Explore the source and return a collection of all session used in this
     * source.
     *
     * @param source : source to explore
     * @param buffer : a collection buffer, can be null
     * @return a collection of sessions, never null but can be empty.
     */
    public static Collection<Session> getSessions(final Source source, Collection<Session> buffer){
        if(buffer == null){
            buffer = new HashSet<Session>();
        }

        if(source instanceof Selector){
            final Session s = ((Selector)source).getSession();
            if(s != null){
                buffer.add(s);
            }
        }else if(source instanceof Join){
            final Join j = (Join) source;
            getSessions(j.getLeft(), buffer);
            getSessions(j.getRight(), buffer);
        }

        return buffer;
    }

    public static boolean queryAll(final Query query){

        if(query.getSource() instanceof TextStatement){
            return true;
        }

        return     query.retrieveAllProperties()
                && query.getCoordinateSystemReproject() == null
                && query.getCoordinateSystemReproject() == null
                && query.getFilter() == Filter.INCLUDE
                && query.getMaxFeatures() == null
                && query.getSortBy() == null
                && query.getStartIndex() == 0;
    }

    /**
     * Combine two queries in the way that the resulting query act
     * as if it was a sub query result. 
     * For example if the original query has a start index of 10 and the
     * sub-query a start index of 5, the resulting startIndex will be 15.
     * The type name of the first query will override the one of the second.
     * 
     * @param original
     * @param second
     * @return sub query
     */
    public static Query subQuery(final Query original, final Query second){
        if ( original==null || second==null ) {
            throw new NullArgumentException("Both query must not be null.");
        }

        final QueryBuilder qb = new QueryBuilder();
        qb.setSource(original.getSource());

        //use the more restrictive max features field---------------------------
        Integer max = original.getMaxFeatures();
        if(second.getMaxFeatures() != null){
            if(max == null){
                max = second.getMaxFeatures();
            }else{
                max = Math.min(max, second.getMaxFeatures());
            }
        }
        qb.setMaxFeatures(max);

        //join attributes names-------------------------------------------------
        final Name[] propNames = retainAttributes(
                original.getPropertyNames(),
                second.getPropertyNames());
        qb.setProperties(propNames);

        //use second crs over original crs--------------------------------------
        if(second.getCoordinateSystemReproject() != null){
            qb.setCRS(second.getCoordinateSystemReproject());
        }else{
            qb.setCRS(original.getCoordinateSystemReproject());
        }

        //join filters----------------------------------------------------------
        Filter filter = original.getFilter();
        Filter filter2 = second.getFilter();

        if ( filter.equals(Filter.INCLUDE) ){
            filter = filter2;
        } else if ( !filter2.equals(Filter.INCLUDE) ){
            filter = FF.and(filter, filter2);
        }
        qb.setFilter(filter);

        //group start index ----------------------------------------------------
        int start = original.getStartIndex() + second.getStartIndex();
        qb.setStartIndex(start);

        //ordering -------------------------------------------------------------
        final List<SortBy> sorts = new ArrayList<SortBy>();
        SortBy[] sts = original.getSortBy();
        if(sts != null){
            sorts.addAll(Arrays.asList(sts));
        }

        sts = second.getSortBy();
        if(sts != null){
            sorts.addAll(Arrays.asList(sts));
        }

        if(sorts != null){
            qb.setSortBy(sorts.toArray(new SortBy[sorts.size()]));
        }

        //hints of the second query---------------------------------------------
        qb.setHints(second.getHints());

        //copy the resolution parameter-----------------------------------------
        final double[] resFirst = original.getResolution();
        final double[] resSecond = second.getResolution();
        if(resFirst == null || Double.isNaN(resFirst[0])){
            qb.setResolution(resSecond);
        }else{
            qb.setResolution(resFirst);
        }

        return qb.buildQuery();
    }

    /**
     * Takes two {@link Query}objects and produce a new one by mixing the
     * restrictions of both of them.
     *
     * <p>
     * The policy to mix the queries components is the following:
     *
     * <ul>
     * <li>
     * typeName: type names MUST match (not checked if some or both queries
     * equals to <code>Query.ALL</code>)
     * </li>
     * <li>
     * handle: you must provide one since no sensible choice can be done
     * between the handles of both queries
     * </li>
     * <li>
     * maxFeatures: the lower of the two maxFeatures values will be used (most
     * restrictive)
     * </li>
     * <li>
     * attributeNames: the attributes of both queries will be joined in a
     * single set of attributes. IMPORTANT: only <b><i>explicitly</i></b>
     * requested attributes will be joint, so, if the method
     * <code>retrieveAllProperties()</code> of some of the queries returns
     * <code>true</code> it does not means that all the properties will be
     * joined. You must create the query with the names of the properties you
     * want to load.
     * </li>
     * <li>
     * filter: the filters of both queries are or'ed
     * </li>
     * <li>
     * <b>any other query property is ignored</b> and no guarantees are made of
     * their return values, so client code shall explicitly care of hints, startIndex, etc.,
     * if needed.
     * </li>
     * </ul>
     * </p>
     *
     * @param firstQuery first query
     * @param secondQuery second query
     *
     * @return Query restricted to the limits of definitionQuery
     *
     * @throws NullPointerException if some of the queries is null
     * @throws IllegalArgumentException if the type names of both queries do
     *         not match
     */
    public static Query mixQueries(final Query firstQuery, final Query secondQuery) {
        if ( firstQuery==null || secondQuery==null ) {
            throw new NullArgumentException("Both query must not be null.");
        }

        if ((firstQuery.getTypeName() != null) && (secondQuery.getTypeName() != null)) {
            if (!firstQuery.getTypeName().equals(secondQuery.getTypeName())) {
                String msg = "Type names do not match: " + firstQuery.getTypeName() + " != " + secondQuery.getTypeName();
                throw new IllegalArgumentException(msg);
            }
        }


        //none of the queries equals Query.ALL, mix them
        //use the more restrictive max features field
        final int maxFeatures = Math.min(firstQuery.getMaxFeatures(),
                secondQuery.getMaxFeatures());

        //join attributes names
        final Name[] propNames = joinAttributes(firstQuery.getPropertyNames(),
                secondQuery.getPropertyNames());

        //join filters
        Filter filter = firstQuery.getFilter();
        Filter filter2 = secondQuery.getFilter();

        if ((filter == null) || filter.equals(Filter.INCLUDE)) {
            filter = filter2;
        } else if ((filter2 != null) && !filter2.equals(Filter.INCLUDE)) {
            filter = FF.and(filter, filter2);
        }

        int start = firstQuery.getStartIndex() + secondQuery.getStartIndex();
        //build the mixed query
        final Name typeName = firstQuery.getTypeName() != null ?
            firstQuery.getTypeName() :
            secondQuery.getTypeName();

        final QueryBuilder builder = new QueryBuilder();
        builder.setTypeName(typeName);
        builder.setFilter(filter);
        builder.setMaxFeatures(maxFeatures);
        builder.setProperties(propNames);
        builder.setStartIndex(start);
        
        return builder.buildQuery();
    }

    /**
     * Creates a set of attribute names from the two input lists of names,
     * maintaining the order of the first list and appending the non repeated
     * names of the second.
     * <p>
     * In the case where both lists are <code>null</code>, <code>null</code>
     * is returned.
     * </p>
     *
     * @param atts1 the first list of attribute names, who's order will be
     *        maintained
     * @param atts2 the second list of attribute names, from wich the non
     *        repeated names will be appended to the resulting list
     *
     * @return Set of attribute names from <code>atts1</code> and
     *         <code>atts2</code>
     */
    private static Name[] joinAttributes(final Name[] atts1, final Name[] atts2) {
        if (atts1 == null && atts2 == null) {
            return null;
        }

        final List atts = new LinkedList();

        if (atts1 != null) {
            atts.addAll(Arrays.asList(atts1));
        }

        if (atts2 != null) {
            for (int i = 0; i < atts2.length; i++) {
                if (!atts.contains(atts2[i])) {
                    atts.add(atts2[i]);
                }
            }
        }

        final Name[] propNames = new Name[atts.size()];
        atts.toArray(propNames);

        return propNames;
    }

    /**
     * Creates a set of attribute names from the two input lists of names,
     * while keep only the attributes from the second list
     */
    private static Name[] retainAttributes(final Name[] atts1, final Name[] atts2) {
        if (atts1 == null && atts2 == null) {
            return null;
        }

        if(atts1 == null){
            return atts2;
        }
        if(atts2 == null){
            return atts1;
        }


        final List atts = new LinkedList();

        for (int i = 0; i < atts2.length; i++) {
            if (Arrays.binarySearch(atts1, atts2[i]) >= 0) {
                atts.add(atts2[i]);
            }
        }

        final Name[] propNames = new Name[atts.size()];
        atts.toArray(propNames);

        return propNames;
    }


}
