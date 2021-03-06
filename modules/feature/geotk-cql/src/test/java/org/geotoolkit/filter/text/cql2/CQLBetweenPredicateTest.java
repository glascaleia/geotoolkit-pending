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
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.expression.Expression;

import static org.geotoolkit.filter.ExpUtils.*;

/**
 * Unit test for between predicate
 * <p>
 * <pre>
 *  This cql clause is an extension for convenience.
 *  &lt;between predicate &gt; ::=
 *  &lt;attribute name &gt; [ NOT ] BETWEEN  &lt;literal&amp; #62; AND  &lt; literal  &gt;
 * </pre>
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @module pending
 * @since 2.5
 */
public class CQLBetweenPredicateTest {

    protected Language language;

    public CQLBetweenPredicateTest(){
        language = Language.CQL;
    }

    /**
     * BETWEEN predicate test
     */
    @Test
    public void propertyBetweenLiterals() throws Exception {
        // between
        Filter expected = FilterCQLSample.getSample( FilterCQLSample.BETWEEN_FILTER );

        testBetweenPredicate( FilterCQLSample.BETWEEN_FILTER, expected );
    }

    /**
     * NOT BETWEEN predicate test
     */
    @Test
    public void notBetweenPredicate() throws Exception{

        // not between
        Filter expected = FilterCQLSample.getSample(FilterCQLSample.NOT_BETWEEN_FILTER);

        testBetweenPredicate(FilterCQLSample.NOT_BETWEEN_FILTER, expected);
    }

    /**
     * test for between predicate with compound attribute
     *
     * @throws Exception
     */
    @Test
    public void compoundAttributeInBetweenPredicate() throws Exception{

        // test compound attribute gmd:aa:bb.gmd:cc.gmd:dd
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        Filter resultFilter = CQL.toFilter(prop + " BETWEEN 100 AND 200 ");

        Assert.assertTrue("PropertyIsBetween filter was expected",
            resultFilter instanceof PropertyIsBetween);

        PropertyIsBetween filter = (PropertyIsBetween) resultFilter;
        Expression property = filter.getExpression();

        Assert.assertEquals(propExpected, stringValue(property));
    }

    /**
     * Execute the test with the provided sample
     *
     * @param samplePredicate
     * @throws Exception
     */
    protected void testBetweenPredicate(final String samplePredicate, final Filter expected) throws Exception{

        Filter actual = CompilerUtil.parseFilter(this.language, samplePredicate);

        Assert.assertNotNull("expects a not null filter", actual);

        Assert.assertEquals("between filter error", expected, actual);
    }


}
