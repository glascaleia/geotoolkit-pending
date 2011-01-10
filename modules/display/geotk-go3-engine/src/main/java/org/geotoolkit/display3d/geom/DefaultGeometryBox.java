/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Johann Sorel
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.display3d.geom;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.shape.Box;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class DefaultGeometryBox extends Box{

    public DefaultGeometryBox(final Geometry geom, final double miny, final double maxy) {
        super();
        Envelope env = geom.getEnvelopeInternal();
        this.setData(   new Vector3(env.getMinX(), miny, env.getMinY()),
                        new Vector3(env.getMaxX(), maxy, env.getMaxY()));
    }

}
