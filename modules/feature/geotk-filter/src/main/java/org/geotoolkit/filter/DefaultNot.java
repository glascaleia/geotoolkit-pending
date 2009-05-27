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

import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Not;

/**
 * Immutable "not" filter.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultNot implements Not{

    private final Filter filter;

    public DefaultNot(Filter filter) {
        if(filter == null){
            throw new NullPointerException("Fitler can not be null");
        }
        this.filter = filter;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Filter getFilter() {
        return filter;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean evaluate(Object object) {
        return !filter.evaluate(object);
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
        return "Not:"+filter.toString();
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
        final DefaultNot other = (DefaultNot) obj;
        if (this.filter != other.filter && (this.filter == null || !this.filter.equals(other.filter))) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.filter != null ? this.filter.hashCode() : 0);
        return hash;
    }

}
