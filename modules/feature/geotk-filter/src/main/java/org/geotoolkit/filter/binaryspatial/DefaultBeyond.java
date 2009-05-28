/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009, Geomatys
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
package org.geotoolkit.filter.binaryspatial;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.Beyond;

/**
 * Immutable "beyond" filter.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultBeyond extends AbstractBinarySpatialOperator<Expression,Expression> implements Beyond {

    private final double distance;
    private final String unit;

    public DefaultBeyond(Expression left, Expression right, double distance, String unit) {
        super(left,right);
        this.distance = distance;
        this.unit = unit;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public double getDistance() {
        return distance;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getDistanceUnits() {
        return unit.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean evaluate(Object object) {
        final Geometry leftGeom = left.evaluate(object, Geometry.class);
        final Geometry rightGeom = right.evaluate(object, Geometry.class);

        if(leftGeom == null || rightGeom == null){
            return false;
        }

        // TODO we can not handle units with JTS geometries
        // we need a way to obtain both geometry CRS to be able to make a correct
        // unit usage

        return !leftGeom.isWithinDistance(rightGeom, distance);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object accept(FilterVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return new StringBuilder("Beyond{")
                .append(left).append(',')
                .append(right).append(',')
                .append(distance).append(',')
                .append(unit).append('}')
                .toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultBeyond other = (DefaultBeyond) obj;
        if (this.left != other.left && !this.left.equals(other.left)) {
            return false;
        }
        if (this.right != other.right && !this.right.equals(other.right)) {
            return false;
        }
        if (this.distance != other.distance) {
            return false;
        }
        if (!this.unit.equals(other.unit)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = 16;
        hash = 71 * hash + this.left.hashCode();
        hash = 71 * hash + this.right.hashCode();
        hash = 71 * hash + (int)this.distance;
        hash = 71 * hash + this.unit.hashCode();
        return hash;
    }

}
