/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.filter.function.math;

import org.geotoolkit.filter.function.AbstractFunction;
import org.opengis.filter.expression.Expression;


public class AbsFunction extends AbstractFunction {

    public AbsFunction(final Expression expression) {
        super(DefaultMathFunctionFactory.ABS, new Expression[] {expression}, null);
    }

    @Override
    public Object evaluate(Object feature) {
        final Number number = parameters.get(0).evaluate(feature, Number.class);
        if (number == null) {
            throw new IllegalArgumentException(
                    "Filter Function problem for function abs argument #0 - expected type number");
        }

        if (number instanceof Integer) {
            return Math.abs(number.intValue());
        }
        if (number instanceof Double) {
            return Math.abs(number.doubleValue());
        }
        if (number instanceof Float) {
            return Math.abs(number.floatValue());
        }
        if (number instanceof Long) {
            return Math.abs(number.longValue());
        }
        if (number instanceof Short) {
            return Math.abs(number.shortValue());
        }
        throw new IllegalArgumentException(
                    "Filter Function problem for function abs argument #0 - expected type number");
    }
}
