/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.filter.text.ecql;

import org.geotoolkit.filter.text.commons.BuildResultStack;
import org.geotoolkit.filter.text.cql2.CQLException;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;


/**
 * Builds a Relate Pattern function using the expressions presents in the result stack.
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @module pending
 * @since 2.6
 */
final class RelatePatternBuilder extends FunctionBuilder {
    /**
     * @param resultStack
     * @param filterFactory
     */
    public RelatePatternBuilder(final BuildResultStack resultStack, final FilterFactory filterFactory) {
        super(resultStack, filterFactory);
    }

    @Override
    public Function build() throws CQLException {
        final Expression[] args = buildParameters();
        return getFilterFactory().function("relatePattern", args);
    }

    /**
     *
     * @return
     * @throws CQLException
     */
    private Expression[] buildParameters() throws CQLException {
        return new Expression[] {
            getResultStack().popExpression(),
            getResultStack().popExpression(),
            getResultStack().popExpression()
        };
    }
}
