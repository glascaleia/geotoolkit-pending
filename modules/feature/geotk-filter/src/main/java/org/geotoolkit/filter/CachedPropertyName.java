/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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

package org.geotoolkit.filter;

import org.geotoolkit.filter.accessor.Accessors;
import org.geotoolkit.filter.accessor.PropertyAccessor;

import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.PropertyName;

import static org.geotoolkit.util.ArgumentChecks.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
class CachedPropertyName extends AbstractExpression implements PropertyName{

    private final String property;

    private final PropertyAccessor accessor;

    CachedPropertyName(final String property, final Class clazz) {
        ensureNonNull("property name", property);
        this.property = property;
        this.accessor = Accessors.getAccessor(clazz,property,null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getPropertyName() {
        return property;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object evaluate(final Object candidate) {
        return accessor.get(candidate, property, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object accept(final ExpressionVisitor visitor, final Object extraData) {
        return visitor.visit(this, extraData);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return property;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CachedPropertyName other = (CachedPropertyName) obj;
        if ((this.property == null) ? (other.property != null) : !this.property.equals(other.property)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.property != null ? this.property.hashCode() : 0);
        return hash;
    }

}
