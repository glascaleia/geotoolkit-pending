/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotoolkit.filter.binarylogic;

import java.util.List;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Or;

/**
 * Immutable "or" filter.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultOr extends AbstractBinaryLogicOperator implements Or {

    public DefaultOr(List<Filter> filters) {
        super(filters);
    }

    public DefaultOr(Filter filter1, Filter filter2){
        super(filter1,filter2);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean evaluate(Object object) {
        for (Filter filter : filters) {
            if (filter.evaluate(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object accept(FilterVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

}
