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
package org.geotoolkit.jdbc;

import java.util.Collections;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotoolkit.data.DataStoreException;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.jdbc.fid.AutoGeneratedPrimaryKeyColumn;
import org.geotoolkit.jdbc.fid.NonIncrementingPrimaryKeyColumn;
import org.geotoolkit.jdbc.fid.PrimaryKey;
import org.geotoolkit.jdbc.fid.SequencedPrimaryKeyColumn;


public abstract class JDBCPrimaryKeyTest extends JDBCTestSupport {

    @Override
    protected abstract JDBCPrimaryKeyTestSetup createTestSetup();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        dataStore.setDatabaseSchema(null);
    }
    
    public void testAutoGeneratedPrimaryKey() throws DataStoreException {
        PrimaryKey pkey = dataStore.getPrimaryKey(dataStore.getSchema(nsname("auto")));

        assertEquals( 1, pkey.getColumns().size() );
        assertTrue( pkey.getColumns().get(0) instanceof AutoGeneratedPrimaryKeyColumn );
        
        FeatureCollection features = dataStore.createSession(false).features(QueryBuilder.all(nsname("auto")));
        assertPrimaryKeyValues(features, 3);
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder( (SimpleFeatureType) features.getSchema() );
        b.add("four");
        b.add(new GeometryFactory().createPoint( new Coordinate(4,4) ));
        features.add( b.buildFeature(null) );
        
        assertPrimaryKeyValues(features,4);
    }
    
    public void testSequencedPrimaryKey() throws DataStoreException {
        PrimaryKey pkey = dataStore.getPrimaryKey(dataStore.getSchema(nsname("seq")));
        
        assertEquals( 1, pkey.getColumns().size() );
        assertTrue( pkey.getColumns().get(0) instanceof SequencedPrimaryKeyColumn );
        
        FeatureCollection features = dataStore.createSession(false).features(QueryBuilder.all(nsname("seq")));
        assertPrimaryKeyValues(features, 3);
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder( (SimpleFeatureType) features.getSchema() );
        b.add("four");
        b.add(new GeometryFactory().createPoint( new Coordinate(4,4) ));
        features.add( b.buildFeature(null) );
        
        assertPrimaryKeyValues(features,4);
    }

    public void testNonIncrementingPrimaryKey() throws DataStoreException {
        PrimaryKey pkey = dataStore.getPrimaryKey(dataStore.getSchema(nsname("noninc")));
        
        assertEquals( 1, pkey.getColumns().size() );
        assertTrue( pkey.getColumns().get(0) instanceof NonIncrementingPrimaryKeyColumn );
        
        FeatureCollection features = dataStore.createSession(false).features(QueryBuilder.all(nsname("noninc")));
        assertPrimaryKeyValues(features, 3);
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder( (SimpleFeatureType) features.getSchema() );
        b.add("four");
        b.add( new GeometryFactory().createPoint( new Coordinate(4,4) ) );
        features.add( b.buildFeature(null) );
        
        assertPrimaryKeyValues(features,4);
    }
    
    void assertPrimaryKeyValues( FeatureCollection features, int count ) throws DataStoreException {
        FeatureIterator i = features.iterator();
       
        for ( int j = 1; j <= count; j++ ) {
            assertTrue( i.hasNext() );
            SimpleFeature f = (SimpleFeature) i.next();
            
            assertEquals( tname(features.getSchema().getName().getLocalPart()) + "." + j , f.getID() );
        }
        i.close();
        
    }
    
    public void testMultiColumnPrimaryKey() throws DataStoreException {
        PrimaryKey pkey = dataStore.getPrimaryKey(dataStore.getSchema(nsname("multi")));
        
        assertEquals( 2, pkey.getColumns().size() );
                
        FeatureCollection features = dataStore.createSession(false).features(QueryBuilder.all(nsname("multi")));
        FeatureIterator i = features.iterator();
        
        String[] xyz = new String[]{"x","y","z"};
        for ( int j = 1; j <= 3; j++ ) {
            assertTrue( i.hasNext() );
            SimpleFeature f = (SimpleFeature) i.next();
            
            assertEquals( tname("multi") + "." + j + "." + xyz[j-1], f.getID() );
        }
        i.close();
        
        SimpleFeatureBuilder b = new SimpleFeatureBuilder( (SimpleFeatureType) features.getSchema() );
        b.add("four");
        b.add(new GeometryFactory().createPoint(new Coordinate(4,4)));
        features.add( b.buildFeature(null) );
        
        i = features.iterator();
        for ( int j = 0; j < 3; j++ ) {
            i.hasNext();
            i.next();
        }
        
        assertTrue( i.hasNext() );
        SimpleFeature f = (SimpleFeature) i.next();
        assertTrue( f.getID().startsWith( tname("multi") + ".4.") );

        i.close();
        
        //test with a filter
        FilterFactory ff = dataStore.getFilterFactory();
        
        Id id = ff.id( Collections.singleton( ff.featureId( tname("multi") + ".1.x") ) );
        features = dataStore.createSession(false).features(QueryBuilder.filtered(nsname("multi"),id));
        assertEquals( 1, features.size() );
    }
    
    public void testNullPrimaryKey() throws DataStoreException {
        assertFalse( dataStore.isWriteable(nsname("nokey")));
    }
}
