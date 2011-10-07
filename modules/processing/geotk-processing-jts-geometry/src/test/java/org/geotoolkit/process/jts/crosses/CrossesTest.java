/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.process.jts.crosses;

import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.process.jts.union.UnionProcess;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.geotoolkit.process.ProcessException;
import org.opengis.util.NoSuchIdentifierException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.jts.AbstractProcessTest;

import org.opengis.parameter.ParameterValueGroup;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit test of Crosses process
 * @author Quentin Boileau
 * @module pending
 */
public class CrossesTest extends AbstractProcessTest {

    public CrossesTest() {
        super("crosses");
    }

    @Test
    public void testCrosses() throws NoSuchIdentifierException, ProcessException {

        GeometryFactory fact = new GeometryFactory();

        // Inputs first
        final LinearRing ring = fact.createLinearRing(new Coordinate[]{
                    new Coordinate(0.0, 0.0),
                    new Coordinate(0.0, 10.0),
                    new Coordinate(5.0, 10.0),
                    new Coordinate(5.0, 0.0),
                    new Coordinate(0.0, 0.0)
                });

        final Geometry geom1 = fact.createPolygon(ring, null);


        final Geometry geom2 = fact.createLineString(new Coordinate[]{
                    new Coordinate(5, 5),
                    new Coordinate(-5, -5)
                });

        // Process
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("jts", "crosses");

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("geom1").setValue(geom1);
        in.parameter("geom2").setValue(geom2);
        final org.geotoolkit.process.Process proc = desc.createProcess(in);

        //result
        final Boolean result = (Boolean) proc.call().parameter("result").getValue();


        final Boolean expected = geom1.crosses(geom2);


        assertTrue(expected.equals(result));
    }

    @Test
    public void testCrossesCRS() throws NoSuchIdentifierException, ProcessException {

        GeometryFactory fact = new GeometryFactory();

        // Inputs first
        final LinearRing ring = fact.createLinearRing(new Coordinate[]{
                    new Coordinate(0.0, 0.0),
                    new Coordinate(0.0, 10.0),
                    new Coordinate(5.0, 10.0),
                    new Coordinate(5.0, 0.0),
                    new Coordinate(0.0, 0.0)
                });

        final Geometry geom1 = fact.createPolygon(ring, null);
        CoordinateReferenceSystem crs1 = null;
        try {
            crs1 = CRS.decode("EPSG:4326");
            JTS.setCRS(geom1, crs1);
        } catch (FactoryException ex) {
            Logger.getLogger(UnionProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        Geometry geom2 = fact.createLineString(new Coordinate[]{
                    new Coordinate(5, 5),
                    new Coordinate(-5, -5)
                });

        CoordinateReferenceSystem crs2 = null;
        try {
            crs2 = CRS.decode("EPSG:2154");
            JTS.setCRS(geom2, crs2);
        } catch (FactoryException ex) {
            Logger.getLogger(UnionProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Process
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("jts", "crosses");

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("geom1").setValue(geom1);
        in.parameter("geom2").setValue(geom2);
        final org.geotoolkit.process.Process proc = desc.createProcess(in);

        //result
        final Boolean result = (Boolean) proc.call().parameter("result").getValue();


        MathTransform mt = null;
        try {
            mt = CRS.findMathTransform(crs2, crs1);
            geom2 = JTS.transform(geom2, mt);
        } catch (FactoryException ex) {
            Logger.getLogger(UnionProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformException ex) {
            Logger.getLogger(UnionProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        final Boolean expected = geom1.crosses(geom2);


        assertTrue(expected.equals(result));
    }
}
