/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.filter.spatial;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Touches;


public class DefaultTouches extends AbstractBinarySpatialOperator<Expression,Expression> implements Touches {

    public DefaultTouches(Expression left, Expression right) {
        super(left,right);
    }

    @Override
    public boolean evaluate(Object object) {
        final Geometry leftGeom = left.evaluate(object, Geometry.class);
        final Geometry rightGeom = right.evaluate(object, Geometry.class);

        if(leftGeom == null || rightGeom == null){
            return false;
        }

        return leftGeom.touches(rightGeom);
    }

    @Override
    public Object accept(FilterVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

}
