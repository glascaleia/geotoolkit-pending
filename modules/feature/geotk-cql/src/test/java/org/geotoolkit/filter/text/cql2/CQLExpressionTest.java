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
package org.geotoolkit.filter.text.cql2;

import org.geotoolkit.filter.text.commons.CompilerUtil;
import org.geotoolkit.filter.text.commons.Language;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;


/**
 * Expression Test
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @module pending
 * @since 2.6
 */
public class CQLExpressionTest {

    protected Language language;

    public CQLExpressionTest() {
        language = Language.CQL;
    }

    @Test
    public void attributeName() throws CQLException {

        Expression expression = CompilerUtil.parseExpression(this.language,
                "attName");
        Assert.assertNotNull(expression);
        Assert.assertTrue(expression instanceof PropertyName);
        Assert.assertEquals("attName", ((PropertyName) expression)
                .getPropertyName());

    }

    /**
     * Bad identifier
     *
     * @throws CQLException
     */
    @Test(expected = CQLException.class)
    public void badPropertyName() throws CQLException {

        String cqlExpression = "1A=2";
        CompilerUtil.parseFilter(language, cqlExpression);
    }

    @Test
    public void add() throws CQLException {
        Expression expression = CompilerUtil.parseExpression(language,
                "a + b + x.y.z");
        Assert.assertNotNull(expression);
        Assert.assertTrue(expression instanceof Add);

        Add add = (Add) expression;
        Expression e1 = add.getExpression1();
        Expression e2 = add.getExpression2();

        Assert.assertTrue(e1 instanceof Add);
        Assert.assertTrue(e2 instanceof PropertyName);
        Assert.assertEquals("x/y/z", ((PropertyName) e2).getPropertyName());

    }

    @Test(expected = CQLException.class)
    public final void testGetSyntaxError() throws CQLException {
        final String malformedExp = "12 / ] + 4";
        CompilerUtil.parseExpression(language, malformedExp);
    }

    /**
     * Bas argument
     * @throws CQLException
     */
    @Test(expected = CQLException.class)
    public void badArgumentInFuncion() throws CQLException {

        String cqlExpression = "strConcat(A, \".\")"; // should be simple quote
        CompilerUtil.parseExpression(this.language, cqlExpression);
    }
}
