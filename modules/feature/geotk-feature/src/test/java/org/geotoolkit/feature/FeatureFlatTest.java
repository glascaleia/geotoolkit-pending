/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2011, Geomatys
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
package org.geotoolkit.feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.test.Assert;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.IllegalAttributeException;

public class FeatureFlatTest extends TestCase {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = org.geotoolkit.util.logging.Logging.getLogger("org.geotoolkit.defaultcore");

    /** Feature on which to preform tests */
    private SimpleFeature testFeature = null;

    TestSuite suite = null;

    public FeatureFlatTest(final String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureFlatTest.class);
        return suite;
    }

    public void setUp() {
        testFeature = SampleFeatureFixtures.createFeature();
    }

    public void testRetrieve() {
        GeometryFactory gf = new GeometryFactory();
        assertTrue(
            "geometry retrieval and match",
            ((Point) testFeature.getAttribute("testGeometry")).equals(
                gf.createPoint(new Coordinate(1, 2))));
        assertTrue(
            "boolean retrieval and match",
            ((Boolean) testFeature.getAttribute("testBoolean")).equals(new Boolean(true)));
        assertTrue(
            "character retrieval and match",
            ((Character) testFeature.getAttribute("testCharacter")).equals(new Character('t')));
        assertTrue("byte retrieval and match", ((Byte) testFeature.getAttribute("testByte")).equals(new Byte("10")));
        assertTrue(
            "short retrieval and match",
            ((Short) testFeature.getAttribute("testShort")).equals(new Short("101")));
        assertTrue(
            "integer retrieval and match",
            ((Integer) testFeature.getAttribute("testInteger")).equals(new Integer(1002)));
        assertTrue("long retrieval and match", ((Long) testFeature.getAttribute("testLong")).equals(new Long(10003)));
        assertTrue(
            "float retrieval and match",
            ((Float) testFeature.getAttribute("testFloat")).equals(new Float(10000.4)));
        assertTrue(
            "double retrieval and match",
            ((Double) testFeature.getAttribute("testDouble")).equals(new Double(100000.5)));
        assertTrue(
            "string retrieval and match",
            ((String) testFeature.getAttribute("testString")).equals("test string data"));

        //test serialize
        Assert.assertSerializable(testFeature);

    }

    public void testBogusCreation() throws Exception {
        final AttributeTypeBuilder atb = new AttributeTypeBuilder();
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
        final FeatureTypeBuilder tb = new FeatureTypeBuilder();
        tb.setName( "test1" );

        atb.reset();
        atb.setBinding(String.class);
        adb.reset();
        adb.setNillable(false);
        adb.setName("billy");
        adb.setType(atb.buildType());
        tb.add(adb.buildDescriptor());

        atb.reset();
        atb.setBinding(String.class);
        adb.reset();
        adb.setNillable(false);
        adb.setName("jimmy");
        adb.setType(atb.buildType());
        tb.add(adb.buildDescriptor());

        SimpleFeatureType test = tb.buildSimpleFeatureType();

        try {
            SimpleFeatureBuilder.build(test, new Object[32],null);
            fail("no error");
        } catch (Exception e) {
        }

    }

    public void testBounds() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        Geometry[] g = new Geometry[4];
        g[0] = gf.createPoint(new Coordinate(0, 0));
        g[1] = gf.createPoint(new Coordinate(0, 10));
        g[2] = gf.createPoint(new Coordinate(10, 0));
        g[3] = gf.createPoint(new Coordinate(10, 10));

        GeometryCollection gc = gf.createGeometryCollection(g);
        FeatureTypeBuilder tb = new FeatureTypeBuilder();
        tb.setName( "bounds" );

        tb.add("p1", Point.class, DefaultGeographicCRS.WGS84);
        tb.add("p2", Point.class, DefaultGeographicCRS.WGS84);
        tb.add("p3", Point.class, DefaultGeographicCRS.WGS84);
        tb.add("p4", Point.class, DefaultGeographicCRS.WGS84);
        SimpleFeatureType t = tb.buildSimpleFeatureType();

        SimpleFeature f = SimpleFeatureBuilder.build(t, g, null);
        assertEquals(gc.getEnvelopeInternal().getMinX(), f.getBounds().getMinX());
        assertEquals(gc.getEnvelopeInternal().getMinY(), f.getBounds().getMinY());
        assertEquals(gc.getEnvelopeInternal().getMaxX(), f.getBounds().getMaxX());
        assertEquals(gc.getEnvelopeInternal().getMaxY(), f.getBounds().getMaxY());

        g[1].getCoordinate().y = 20;
        g[2].getCoordinate().x = 20;
        f.setAttribute(1, g[1]);
        f.setAttribute(2, g[2]);
        gc = gf.createGeometryCollection(g);
        assertEquals(gc.getEnvelopeInternal().getMinX(), f.getBounds().getMinX());
        assertEquals(gc.getEnvelopeInternal().getMinY(), f.getBounds().getMinY());
        assertEquals(gc.getEnvelopeInternal().getMaxX(), f.getBounds().getMaxX());
        assertEquals(gc.getEnvelopeInternal().getMaxY(), f.getBounds().getMaxY());

        //test serialize
        Assert.assertSerializable(f);
    }

    public void testClone() {
        SimpleFeature f = SampleFeatureFixtures.createFeature();
        SimpleFeature c = FeatureUtilities.copy( f );
        for (int i = 0, ii = c.getAttributeCount(); i < ii; i++) {
            assertEquals(c.getAttribute(i), f.getAttribute(i));
        }
    }

    public void testClone2() throws Exception {
        SimpleFeatureType type = SampleFeatureFixtures.createTestType();
        Object[] attributes = SampleFeatureFixtures.createAttributes();
        SimpleFeature feature = SimpleFeatureBuilder.build(type, attributes, "fid");
        SimpleFeature clone = FeatureUtilities.deepCopy(feature);
        assertTrue("Clone was not equal", feature.equals(clone));
    }

    public void testToStringWontThrow() throws IllegalAttributeException {
        final SimpleFeature f = (SimpleFeature)SampleFeatureFixtures.createFeature();
        f.setAttributes(new Object[f.getAttributeCount()]);
        String s = f.toString();
    }

    static AttributeDescriptor newAtt(final String name, final Class c) {
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
        final AttributeTypeBuilder ab = new AttributeTypeBuilder();
        ab.setBinding(c);
        adb.setType(ab.buildType());
        adb.setName(name);
        return adb.buildDescriptor();
    }

    static AttributeDescriptor newAtt(final String name, final Class c, final boolean nillable) {
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
        final AttributeTypeBuilder ab = new AttributeTypeBuilder();
        ab.setBinding( c );

        adb.setNillable(nillable);
        adb.setType(ab.buildType());
        adb.setName(name);
        return adb.buildDescriptor();
    }

    public void testModify() throws IllegalAttributeException {
        String newData = "new test string data";
        testFeature.setAttribute("testString", newData);
        assertEquals("match modified (string) attribute", testFeature.getAttribute("testString"), newData);

        GeometryFactory gf = new GeometryFactory();
        Point newGeom = gf.createPoint(new Coordinate(3, 4));
        testFeature.setAttribute("testGeometry", newGeom);
        assertEquals("match modified (geometry) attribute", testFeature.getAttribute("testGeometry"), newGeom);

        testFeature.setDefaultGeometry(newGeom);
        assertEquals("match modified (geometry) attribute", testFeature.getAttribute("testGeometry"), newGeom);

    }

    public void testEquals() throws Exception {
        SimpleFeature f1 = SampleFeatureFixtures.createFeature();
        SimpleFeature f2 = SampleFeatureFixtures.createFeature();
        assertTrue(f1.equals(f1));
        assertTrue(f2.equals(f2));
        assertTrue(!f1.equals(f2));
        assertTrue(!f1.equals(null));

        FeatureTypeBuilder tb = new FeatureTypeBuilder();
        tb.setName( "different" );
        tb.add( "name", String.class );
        SimpleFeatureType type = tb.buildSimpleFeatureType();

        assertTrue(!f1.equals(SimpleFeatureBuilder.build(type, new Object[1], null)));
    }

}
