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
package org.geotoolkit.data.diff;

import java.io.IOException;

import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.data.FeatureEvent.Type;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotoolkit.data.ATestReader;
import org.geotoolkit.data.FeatureReader;


public class DiffWriterTest extends TestCase {

    DiffFeatureWriter writer;
    private Point geom;
    private SimpleFeatureType type;

    protected void setUp() throws Exception {
        type = FeatureTypeUtilities.createType("default", "name:String,*geom:Geometry");
        GeometryFactory fac = new GeometryFactory();
        geom = fac.createPoint(new Coordinate(10, 10));

        Diff diff = new Diff();
        diff.add("1", SimpleFeatureBuilder.build(type, new Object[]{"diff1", geom}, "1"));
        diff.update("original", SimpleFeatureBuilder.build(type, new Object[]{"diff2", geom}, "original"));
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = new ATestReader(type, SimpleFeatureBuilder.build(type, new Object[]{"original", geom}, "original"));
        writer = new DiffFeatureWriter(reader, diff) {

            @Override
            protected void fireNotification(Type eventType, JTSEnvelope2D bounds) {
            }
        };
    }

    public void testRemove() throws Exception {
        writer.next();
        SimpleFeature feature = writer.next();
        writer.remove();
        assertNull(writer.diff.added.get(feature.getID()));
    }

    public void testHasNext() throws Exception {
        assertTrue(writer.hasNext());
        assertEquals(2, writer.diff.added.size() + writer.diff.modified2.size());
        writer.next();
        assertTrue(writer.hasNext());
        assertEquals(2, writer.diff.added.size() + writer.diff.modified2.size());
        writer.next();
        assertFalse(writer.hasNext());
        assertEquals(2, writer.diff.added.size() + writer.diff.modified2.size());
    }

    public void testWrite() throws IOException, Exception {
        while (writer.hasNext()) {
            writer.next();
        }

        SimpleFeature feature = writer.next();
        feature.setAttribute("name", "new1");

        writer.write();
        assertEquals(2, writer.diff.added.size());
        feature = writer.next();
        feature.setAttribute("name", "new2");

        writer.write();

        assertEquals(3, writer.diff.added.size());
    }
}