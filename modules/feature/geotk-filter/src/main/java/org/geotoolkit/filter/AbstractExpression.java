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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlTransient;

import org.geotoolkit.util.Converters;

import org.opengis.filter.expression.Expression;

/**
 * Override evaluate(Object,Class) by using the converters system.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
@XmlTransient
public abstract class AbstractExpression implements Expression,Serializable {

    /**
     * {@inheritDoc }
     * Use the converters utility class to convert the default result object
     * to the wished class.
     */
    @Override
    public <T> T evaluate(final Object candidate, final Class<T> target) {
        return Converters.convert(evaluate(candidate), target);
    }

}
