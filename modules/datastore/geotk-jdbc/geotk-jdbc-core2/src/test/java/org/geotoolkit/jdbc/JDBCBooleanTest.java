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

import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public abstract class JDBCBooleanTest extends JDBCTestSupport {

    @Override
    protected abstract JDBCBooleanTestSetup createTestSetup();

    public void testGetSchema() throws Exception {
        SimpleFeatureType ft = (SimpleFeatureType) dataStore.getSchema( tname("b") );
        assertEquals( Boolean.class, ft.getDescriptor("boolProperty").getType().getBinding() );
    }
    
    public void testGetFeatures() throws Exception {
        Query query = QueryBuilder.all(dataStore.getSchema("b").getName());
        FeatureReader r = dataStore.getFeatureReader( query );
        r.hasNext();
        
        SimpleFeature f = (SimpleFeature) r.next();
        assertEquals( Boolean.FALSE, f.getAttribute( "boolProperty" ) );
        
        r.hasNext();
        f = (SimpleFeature) r.next();
        assertEquals( Boolean.TRUE, f.getAttribute( "boolProperty" ) );
        
        r.close();
       
    }
}