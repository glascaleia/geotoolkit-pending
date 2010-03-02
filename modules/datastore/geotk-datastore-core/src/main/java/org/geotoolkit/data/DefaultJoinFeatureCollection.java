/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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

import java.util.Map;
import java.util.Map.Entry;

import org.geotoolkit.data.query.Join;
import org.geotoolkit.data.query.JoinType;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.query.QueryUtilities;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;
import org.geotoolkit.util.collection.CloseableIterator;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.PropertyName;

/**
 * FeatureCollection that takes it'es source from a join query.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class DefaultJoinFeatureCollection extends AbstractFeatureCollection<Feature>{

    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);

    private final Query query;
    private final FeatureCollection leftCollection;
    private final FeatureCollection rightCollection;
    private FeatureType type = null;

    public DefaultJoinFeatureCollection(String id, Query query){
        super(id,query.getSource());

        if(!(query.getSource() instanceof Join)){
            throw new IllegalArgumentException("Query must have a join source.");
        }

        if(!QueryUtilities.isAbsolute(getSource())){
            throw new IllegalArgumentException("Query source must be absolute.");
        }

        this.query = query;

        //todo must parse the filter on each selector
        leftCollection = QueryUtilities.evaluate("left", QueryBuilder.all(getSource().getLeft()));
        rightCollection = QueryUtilities.evaluate("right", QueryBuilder.all(getSource().getRight()));

    }

    @Override
    public Join getSource() {
        return (Join) super.getSource();
    }

    @Override
    public synchronized FeatureType getFeatureType() {
        if(type == null){
            final FeatureType leftType = leftCollection.getFeatureType();
            final FeatureType rightType = rightCollection.getFeatureType();

            final SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
            sftb.setName("combine");

            for(final PropertyDescriptor desc : leftType.getDescriptors()){
                sftb.add((AttributeDescriptor) desc);
            }
            for(final PropertyDescriptor desc : rightType.getDescriptors()){
                sftb.add((AttributeDescriptor) desc);
            }

            type = sftb.buildFeatureType();
        }

        return type;
    }

    @Override
    public FeatureCollection<Feature> subCollection(Query query) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CloseableIterator<FeatureCollectionRow> getRows() throws DataStoreException {
        final JoinType jt = getSource().getJoinType();

        if(jt == JoinType.INNER){
            return new JoinInnerRowIterator(null);
        }else if(jt == JoinType.LEFT_OUTER){
            throw new UnsupportedOperationException("Not supported yet.");
        }else if(jt == JoinType.RIGHT_OUTER){
            throw new UnsupportedOperationException("Not supported yet.");
        }else{
            throw new IllegalArgumentException("Unknowned Join type : " + jt);
        }
    }

    @Override
    public FeatureIterator<Feature> iterator(Hints hints) throws DataStoreRuntimeException {
        try {
            return new RowToFeatureIterator(getRows());
        } catch (DataStoreException ex) {
            throw new DataStoreRuntimeException(ex);
        }
    }

    @Override
    public void update(Filter filter, Map<? extends AttributeDescriptor, ? extends Object> values) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(Filter filter) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Iterate on both collections with an Inner join condition.
     */
    private class JoinInnerRowIterator implements CloseableIterator<FeatureCollectionRow>{

        private final CloseableIterator<FeatureCollectionRow> leftIterator;
        private CloseableIterator<FeatureCollectionRow> rightIterator;
        private FeatureCollectionRow leftRow;
        private FeatureCollectionRow nextRow;

        JoinInnerRowIterator(Hints hints) throws DataStoreException{
            leftIterator = leftCollection.getRows();
        }

        @Override
        public FeatureCollectionRow next() {
            try {
                searchNext();
            } catch (DataStoreException ex) {
                throw new DataStoreRuntimeException(ex);
            }
            FeatureCollectionRow f = nextRow;
            nextRow = null;
            return f;
        }
        
        @Override
        public void close() {
            leftIterator.close();
            if(rightIterator != null){
                rightIterator.close();
            }
        }

        @Override
        public boolean hasNext() {
            try {
                searchNext();
            } catch (DataStoreException ex) {
                throw new DataStoreRuntimeException(ex);
            }
            return nextRow != null;
        }

        private void searchNext() throws DataStoreException{
            if(nextRow != null) return;

            final PropertyIsEqualTo equal = getSource().getJoinCondition();
            final PropertyName leftProperty = (PropertyName) equal.getExpression1();
            final PropertyName rightProperty = (PropertyName) equal.getExpression2();

            //we might have several right features for one left
            if(leftRow != null && rightIterator != null){
                while(nextRow== null && rightIterator.hasNext()){
                    final FeatureCollectionRow rightRow = rightIterator.next();
                    nextRow = checkValid(leftRow, rightRow);
                }
            }

            while(nextRow==null && leftIterator.hasNext()){
                rightIterator = null;
                leftRow = leftIterator.next();

                final Feature leftFeature = toFeature(leftRow);
                final Object leftValue = leftProperty.evaluate(leftFeature);

                if(rightIterator == null){
                    QueryBuilder qb = new QueryBuilder();
                    qb.setSource(getSource().getRight());
                    qb.setFilter(FF.equals(rightProperty, FF.literal(leftValue)));
                    Query rightQuery = qb.buildQuery();
                    rightIterator = rightCollection.subCollection(rightQuery).getRows();
                }

                while(nextRow== null && rightIterator.hasNext()){
                    final FeatureCollectionRow rightRow = rightIterator.next();
                    nextRow = checkValid(leftRow, rightRow);
                }

            }

        }

        private FeatureCollectionRow checkValid(FeatureCollectionRow left, FeatureCollectionRow right) throws DataStoreException{
            final Feature candidate = toFeature(left,right);

            if(query.getFilter().evaluate(candidate)){
                //combine both rows
                final FeatureCollectionRow row = new DefaultFeatureCollectionRow();
                row.getFeatures().putAll(left.getFeatures());
                row.getFeatures().putAll(right.getFeatures());
                return row;
            }else{
                //not a valid combinaison
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet on join queries.");
        }

    }

    /**
     * Iterate on both collections with an outer left join condition.
     */
    private class JoinOuterLeftRowIterator implements CloseableIterator<FeatureCollectionRow>{

        private final CloseableIterator<FeatureCollectionRow> leftIterator;
        private CloseableIterator<FeatureCollectionRow> rightIterator;
        private FeatureCollectionRow leftRow;
        private FeatureCollectionRow nextRow;

        JoinOuterLeftRowIterator(Hints hints) throws DataStoreException{
            leftIterator = leftCollection.getRows();
        }

        @Override
        public FeatureCollectionRow next() {
            try {
                searchNext();
            } catch (DataStoreException ex) {
                throw new DataStoreRuntimeException(ex);
            }
            FeatureCollectionRow f = nextRow;
            nextRow = null;
            return f;
        }

        @Override
        public void close() {
            leftIterator.close();
            if(rightIterator != null){
                rightIterator.close();
            }
        }

        @Override
        public boolean hasNext() {
            try {
                searchNext();
            } catch (DataStoreException ex) {
                throw new DataStoreRuntimeException(ex);
            }
            return nextRow != null;
        }

        private void searchNext() throws DataStoreException{
            if(nextRow != null) return;

            final PropertyIsEqualTo equal = getSource().getJoinCondition();
            final PropertyName leftProperty = (PropertyName) equal.getExpression1();
            final PropertyName rightProperty = (PropertyName) equal.getExpression2();

            //we might have several right features for one left
            if(leftRow != null && rightIterator != null){
                while(nextRow== null && rightIterator.hasNext()){
                    final FeatureCollectionRow rightRow = rightIterator.next();
                    nextRow = checkValid(leftRow, rightRow);
                }
            }

            while(nextRow==null && leftIterator.hasNext()){
                rightIterator = null;
                leftRow = leftIterator.next();

                final Feature leftFeature = toFeature(leftRow);
                final Object leftValue = leftProperty.evaluate(leftFeature);

                if(rightIterator == null){
                    QueryBuilder qb = new QueryBuilder();
                    qb.setSource(getSource().getRight());
                    qb.setFilter(FF.equals(rightProperty, FF.literal(leftValue)));
                    Query rightQuery = qb.buildQuery();
                    rightIterator = rightCollection.subCollection(rightQuery).getRows();
                }

                while(nextRow== null && rightIterator.hasNext()){
                    final FeatureCollectionRow rightRow = rightIterator.next();
                    nextRow = checkValid(leftRow, rightRow);
                }

            }

        }

        private FeatureCollectionRow checkValid(FeatureCollectionRow left, FeatureCollectionRow right) throws DataStoreException{
            final Feature candidate = toFeature(left,right);

            if(query.getFilter().evaluate(candidate)){
                //combine both rows
                final FeatureCollectionRow row = new DefaultFeatureCollectionRow();
                row.getFeatures().putAll(left.getFeatures());
                row.getFeatures().putAll(right.getFeatures());
                return row;
            }else{
                //not a valid combinaison
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet on join queries.");
        }

    }


    private class RowToFeatureIterator implements FeatureIterator<Feature>{

        private final CloseableIterator<FeatureCollectionRow> ite;

        public RowToFeatureIterator(CloseableIterator<FeatureCollectionRow> rows){
            this.ite = rows;
        }

        @Override
        public Feature next() {
            try {
                return toFeature(ite.next());
            } catch (DataStoreException ex) {
                throw new DataStoreRuntimeException(ex);
            }
        }

        @Override
        public void close() {
            ite.close();
        }

        @Override
        public boolean hasNext() {
            return ite.hasNext();
        }

        @Override
        public void remove() {
            ite.remove();
        }

    }

    private Feature toFeature(FeatureCollectionRow ... rows) throws DataStoreException{

        if(rows.length == 1 && rows[0].getFeatures().size() == 1){
            //avoid creating a new feature
            return rows[0].getFeatures().values().iterator().next();
        }

        final SimpleFeatureBuilder sfb = new SimpleFeatureBuilder((SimpleFeatureType) getFeatureType());
        //build the id with all features
        final StringBuilder sb = new StringBuilder();

        for(FeatureCollectionRow row : rows){
            for(Entry<String,Feature> entry : row.getFeatures().entrySet()){
                final Feature feature = entry.getValue();
                if(sb.length()>0) sb.append(" / ");
                sb.append(feature.getIdentifier().getID());

                //configure the properties
                for(Property prop : feature.getProperties()){
                    sfb.set(prop.getName(), prop.getValue());
                }
            }
        }
        final String id = sb.toString();
        return sfb.buildFeature(id);
    }

}