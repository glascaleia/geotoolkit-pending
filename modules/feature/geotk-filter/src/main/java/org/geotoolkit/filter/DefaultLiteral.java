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
package org.geotoolkit.filter;

import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.AbstractJTSGeometry;
import org.geotoolkit.util.Converters;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Literal;
import org.opengis.geometry.Geometry;

/**
 * Immutable generic Literal.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class DefaultLiteral<T> extends AbstractExpression implements Literal{

    private final T value;

    public DefaultLiteral(final T value) {
        this.value = value;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public T evaluate(final Object feature) {
        return value;
    }

    @Override
    public <T> T evaluate(final Object candidate, final Class<T> target) {

        if(value instanceof Geometry && com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(target)){
            final Geometry geo = (Geometry) value;
            if(geo instanceof AbstractJTSGeometry) {
                com.vividsolutions.jts.geom.Geometry jts = ((AbstractJTSGeometry)geo).getJTSGeometry();
                return Converters.convert(jts, target);
            }
        }
        return super.evaluate(candidate, target);
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
    public T getValue() {
        return value;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return value == null ? "NULL" : value.toString();
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
        final DefaultLiteral<T> other = (DefaultLiteral<T>) obj;

        if (this.value == null && other.value == null) {
            return true;
        }
        if (this.value == null) {
            return false;
        }
        if (other.value == null) {
            return false;
        }

        final Object otherVal = Converters.convert(other.value, this.value.getClass());
        if (!this.value.equals(otherVal)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

}
