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

import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;


/**
 * Tests that geometry types can be properly created and retrieved back from the
 * database. You might need to override some of the tests method to fix the expectations
 * for specific geometry class types.
 * @module pending
 */
public abstract class JDBCGeometryTest extends JDBCTestSupport {

    @Override
    protected abstract JDBCGeometryTestSetup createTestSetup();

    public void testPoint() throws Exception {
        assertEquals(Point.class, checkGeometryType(Point.class));
    }
    
    public void testLineString() throws Exception {
        assertEquals(LineString.class, checkGeometryType(LineString.class));
    }
    
    public void testLinearRing() throws Exception {
        assertEquals(LinearRing.class, checkGeometryType(LinearRing.class));
    }
    
    public void testPolygon() throws Exception {
        assertEquals(Polygon.class, checkGeometryType(Polygon.class));
    }
    
    public void testMultiPoint() throws Exception {
        assertEquals(MultiPoint.class, checkGeometryType(MultiPoint.class));
    }
    
    public void testMultiLineString() throws Exception {
        assertEquals(MultiLineString.class, checkGeometryType(MultiLineString.class));
    }
    
    public void testMultiPolygon() throws Exception {
        assertEquals(MultiPolygon.class, checkGeometryType(MultiPolygon.class));
    }
    
    /**
     * Sometimes the source cannot anticipate the geometry type, can we cope with this?
     * @throws Exception
     */
    public void testGeometry() throws Exception {
        assertEquals(Geometry.class, checkGeometryType(Geometry.class));
    }
    
    /**
     * Same goes for heterogeneous collections 
     * @throws Exception
     */
    public void testGeometryCollection() throws Exception {
        assertEquals(GeometryCollection.class, checkGeometryType(GeometryCollection.class));
    }

    protected Class checkGeometryType(final Class geomClass) throws Exception {
        // we just prefix the table name with "t" otherwise we go beyond
        // the Oracle identifier max length limit (oh my my...)
        String featureTypeName = tname("t" + geomClass.getSimpleName());

        // create a featureType and write it to PostGIS
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");

        FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName(featureTypeName);
        ftb.add(aname("id"), Integer.class,1,1,false,FeatureTypeBuilder.PRIMARY_KEY);
        ftb.add(aname("name"), String.class);
        ftb.add(aname("geom"), geomClass, crs);

        SimpleFeatureType newFT = ftb.buildSimpleFeatureType();
        dataStore.createSchema(newFT.getName(), newFT);

        SimpleFeatureType newSchema = (SimpleFeatureType) dataStore.getFeatureType(featureTypeName);
        assertNotNull(newSchema);
        assertEquals(3, newSchema.getAttributeCount());
        return newSchema.getGeometryDescriptor().getType().getBinding();
    }

}
