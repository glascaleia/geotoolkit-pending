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
package org.geotoolkit.process.jts.buffer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.jts.AbstractProcessTest;

import org.opengis.parameter.ParameterValueGroup;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit test of Buffer process
 * @author Quentin Boileau
 * @module pending
 */
public class BufferTest extends AbstractProcessTest{

   
    public BufferTest() {
        super("buffer");
    }

    @Test
    public void testBuffer() {
        
        GeometryFactory fact = new GeometryFactory();
        
        // Inputs first
        final Geometry geom = fact.createPoint(new Coordinate(0, 0));
        final double distance = 1.5;
        final int segments = 5;
        final int capStype = BufferOp.CAP_SQUARE;
        
        
        // Process
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("jts", "buffer");
        final org.geotoolkit.process.Process proc = desc.createProcess();

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("geom").setValue(geom);
        in.parameter("distance").setValue(distance);
        in.parameter("segments").setValue(segments);
        in.parameter("endstyle").setValue(capStype);
        proc.setInput(in);
        proc.run();

        //result
        final Geometry result = (Geometry) proc.getOutput().parameter("result_geom").getValue();
       
        
        final Geometry expected = geom.buffer(distance, segments, capStype);
        
        assertTrue(expected.equals(result));
    }
    
}
