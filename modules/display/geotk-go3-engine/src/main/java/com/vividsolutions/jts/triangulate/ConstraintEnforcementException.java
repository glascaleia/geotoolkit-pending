/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.vividsolutions.jts.triangulate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Indicates a failure during constraint enforcement.
 * 
 * @author Martin Davis
 * @version 1.0
 * @module pending
 */
public class ConstraintEnforcementException extends RuntimeException {

    private static final long serialVersionUID = 386496846550080140L;

    private static String msgWithCoord(final String msg, final Coordinate pt) {
        if (pt != null)
            return msg + " [ " + WKTWriter.toPoint(pt) + " ]";
        return msg;
    }

    private Coordinate pt = null;

    /**
     * Creates a new instance with a given message.
     * 
     * @param msg a string
     */
    public ConstraintEnforcementException(final String msg) {
        super(msg);
    }

    /**
     * Creates a new instance with a given message and approximate location.
     * 
     * @param msg a string
     * @param pt the location of the error
     */
    public ConstraintEnforcementException(final String msg, final Coordinate pt) {
        super(msgWithCoord(msg, pt));
        this.pt = new Coordinate(pt);
    }

    /**
     * Gets the approximate location of this error.
     * 
     * @return a location
     */
    public Coordinate getCoordinate() {
        return pt;
    }
}
