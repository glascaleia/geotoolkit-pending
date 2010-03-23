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
package org.geotoolkit.feature.simple;

import java.util.Collections;

import junit.framework.TestCase;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.type.DefaultFeatureTypeFactory;
import org.geotoolkit.feature.type.DefaultSchema;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Schema;

import com.vividsolutions.jts.geom.Point;
import org.geotoolkit.feature.FeatureTypeBuilder;


public class SimpleTypeBuilderTest extends TestCase {

    static final String URI = "gopher://localhost/test";
    FeatureTypeBuilder builder;

    @Override
    protected void setUp() throws Exception {
        Schema schema = new DefaultSchema("test");

        DefaultFeatureTypeFactory typeFactory = new DefaultFeatureTypeFactory();
        AttributeType pointType =
                typeFactory.createGeometryType(new DefaultName("test", "pointType"), Point.class, null, false, false, Collections.EMPTY_LIST, null, null);
        schema.put(new DefaultName("test", "pointType"), pointType);

        AttributeType intType =
                typeFactory.createAttributeType(new DefaultName("test", "intType"), Integer.class, false, false, Collections.EMPTY_LIST, null, null);
        schema.put(new DefaultName("test", "intType"), intType);

        builder = new FeatureTypeBuilder(new DefaultFeatureTypeFactory());
        builder.setBindings(schema);
    }

    public void testSanity() {
        builder.setName("testNamespaceURI", "testName");
        builder.add(new DefaultName("point"), Point.class, DefaultGeographicCRS.WGS84);
        builder.add(new DefaultName("integer"), Integer.class);

        SimpleFeatureType type = builder.buildSimpleFeatureType();
        assertNotNull(type);

        assertEquals(2, type.getAttributeCount());

        AttributeType t = type.getType("point");
        assertNotNull(t);
        assertEquals(Point.class, t.getBinding());

        t = type.getType("integer");
        assertNotNull(t);
        assertEquals(Integer.class, t.getBinding());

        t = type.getGeometryDescriptor().getType();
        assertNotNull(t);
        assertEquals(Point.class, t.getBinding());
    }

    public void testCRS() {
        //todo strange test, should not test it that way
//        builder.setName("testNamespaceURI", "testName");
//
//        builder.setCRS(DefaultGeographicCRS.WGS84);
//        builder.crs(null).add("point", Point.class);
//        builder.add(new DefaultName("point2"), Point.class, DefaultGeographicCRS.WGS84);
//        builder.setDefaultGeometry("point");
//        SimpleFeatureType type = builder.buildSimpleFeatureType();
//        assertEquals(DefaultGeographicCRS.WGS84, type.getCoordinateReferenceSystem());
//
//        assertNull(type.getGeometryDescriptor().getType().getCoordinateReferenceSystem());
//        assertEquals(DefaultGeographicCRS.WGS84, ((GeometryType) type.getType("point2")).getCoordinateReferenceSystem());
    }
}
