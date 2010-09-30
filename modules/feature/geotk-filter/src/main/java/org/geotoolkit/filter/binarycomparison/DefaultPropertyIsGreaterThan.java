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
package org.geotoolkit.filter.binarycomparison;

import org.geotoolkit.util.StringUtilities;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.expression.Expression;

/**
 * Immutable "is greater than" filter.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class DefaultPropertyIsGreaterThan extends AbstractBinaryComparisonOperator<Expression,Expression> implements PropertyIsGreaterThan{

    public DefaultPropertyIsGreaterThan(Expression left, Expression right, boolean match) {
        super(left,right,match);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean evaluate(Object object) {
        final Integer v = compare(object);
        return (v == null) ? false : (v > 0) ;
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
        final StringBuilder sb = new StringBuilder("PropertyIsGreaterThan (matchcase=");
        sb.append(match).append(")\n");
        sb.append(StringUtilities.toStringTree(left,right));
        return sb.toString();
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
        final AbstractBinaryComparisonOperator other = (AbstractBinaryComparisonOperator) obj;
        if (this.left != other.left && !this.left.equals(other.left)) {
            return false;
        }
        if (this.right != other.right && !this.right.equals(other.right)) {
            return false;
        }
        if (this.match != other.match) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = 8;
        hash = 23 * hash + this.left.hashCode();
        hash = 23 * hash + this.right.hashCode() ;
        hash = 23 * hash + (this.match ? 1 : 0);
        return hash;
    }

}
