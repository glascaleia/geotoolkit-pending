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

import com.vividsolutions.jts.geom.Point;
import java.util.logging.Level;
import org.geotoolkit.filter.DefaultLiteral;
import org.geotoolkit.filter.DefaultPropertyName;
import org.geotoolkit.filter.text.commons.CompilerUtil;
import org.geotoolkit.filter.text.commons.Language;
import org.geotoolkit.filter.text.cql2.CQLException;
import org.geotoolkit.filter.text.cql2.CQLGeoOperationTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;


/**
 * ECQL Geo Operation Test
 * <p>
 * Execute all cql test for spatial relation using the ECQL compiler
 * </p>
 * <p>
 *
 * <pre>
 *   &lt;routine invocation &gt; ::=
 *           &lt;geoop name &gt; &lt;georoutine argument list &gt;
 *       |   &lt;relgeoop name &gt; &lt;relgeoop argument list &gt;
 *       |   &lt;routine name &gt; &lt;argument list &gt;
 *   &lt;geoop name &gt; ::=
 *           EQUALS | DISJOINT | INTERSECTS | TOUCHES | CROSSES |
 *           WITHIN | CONTAINS |OVERLAPS | RELATE
 *   That rule is extended with bbox for convenience.
 *   &lt;bbox argument list &gt;::=
 *       &quot;(&quot;  &lt;attribute &gt; &quot;,&quot; &lt;min X &gt; &quot;,&quot; &lt;min Y &gt; &quot;,&quot; &lt;max X &gt; &quot;,&quot; &lt;max Y &gt;[&quot;,&quot;  &lt;srs &gt;] &quot;)&quot;
 *       &lt;min X &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;min Y &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;max X &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;max Y &gt; ::=  &lt;signed numerical literal &gt;
 *       &lt;srs &gt; ::=
 *
 * </pre>
 *</p>
 * <p>
 * Note: the symbols of geoop names were changed to adjust these name to geoapi filter.
 *
 *
 * </p>
 * @author Mauricio Pazos (Axios Engineering)
 * @module pending
 * @since 2.6
 */
public final class ECQLGeoOperationTest extends CQLGeoOperationTest{

    public ECQLGeoOperationTest(){
        super();
        language = Language.ECQL;
    }

    @Override
    @Test
    public void disjoint() throws CQLException{


        Filter resultFilter = CompilerUtil.parseFilter(language, "DISJOINT(the_geom, POINT(1 2))");

        Assert.assertTrue("Disjoint was expected", resultFilter instanceof Disjoint);
    }

    @Override
    @Test
    public void Intersects() throws CQLException {
        Filter resultFilter = CompilerUtil.parseFilter(language,"INTERSECTS(the_geom, POINT(1 2))");

        Assert.assertTrue("Intersects was expected", resultFilter instanceof Intersects);

    }


    @Override
    @Test
    public void touches() throws CQLException{
        Filter resultFilter = CompilerUtil.parseFilter(language,"TOUCHES(the_geom, POINT(1 2))");

        Assert.assertTrue("Touches was expected", resultFilter instanceof Touches);
    }

    @Override
    public void crosses() throws CQLException {
        Filter resultFilter = CompilerUtil.parseFilter(language,"CROSSES(the_geom, POINT(1 2))");

        Assert.assertTrue("Crosses was expected", resultFilter instanceof Crosses);

    }

    @Override
    @Test
    public void contains() throws CQLException      {
        Filter resultFilter = CompilerUtil.parseFilter(language,"CONTAINS(the_geom, POINT(1 2))");

        Assert.assertTrue("Contains was expected", resultFilter instanceof Contains);
        Contains cfilter = (Contains) resultFilter;
        
        Assert.assertEquals(new DefaultPropertyName("the_geom"), cfilter.getExpression2());
        
        Assert.assertTrue("Literal was expected but was "+ cfilter.getExpression1().getClass().getName(), cfilter.getExpression1() instanceof DefaultLiteral);
        DefaultLiteral lit = (DefaultLiteral) cfilter.getExpression1();
        Assert.assertTrue("Point was expected but was "+ lit.getValue().getClass().getName(), lit.getValue() instanceof Point);
        Point p = (Point) lit.getValue();
        Assert.assertEquals(1, p.getX(), 0);
        Assert.assertEquals(2, p.getY(), 0);
    }
    
    @Test
    public void containsPT3D() throws CQLException      {
        Filter resultFilter = CompilerUtil.parseFilter(language,"CONTAINS(the_geom, POINT3D(1 2 3))");

        Assert.assertTrue("Contains was expected", resultFilter instanceof Contains);
        Contains cfilter = (Contains) resultFilter;
        LOGGER.log(Level.INFO, "ECQL EXP1: {0}", cfilter.getExpression1());
        LOGGER.log(Level.INFO, "ECQL EXP2: {0}", cfilter.getExpression2());
        
        Assert.assertEquals(new DefaultPropertyName("the_geom"), cfilter.getExpression2());
        
        Assert.assertTrue("Literal was expected but was "+ cfilter.getExpression1().getClass().getName(), cfilter.getExpression1() instanceof DefaultLiteral);
        DefaultLiteral lit = (DefaultLiteral) cfilter.getExpression1();
        Assert.assertTrue("Point was expected but was "+ lit.getValue().getClass().getName(), lit.getValue() instanceof Point);
        Point p = (Point) lit.getValue();
        Assert.assertEquals(1, p.getX(), 0);
        Assert.assertEquals(2, p.getY(), 0);
        Assert.assertEquals(3, p.getCoordinate().z, 0);

    }

    @Override
    @Test
    public void overlaps() throws Exception {
        Filter resultFilter;


        resultFilter = CompilerUtil.parseFilter(language,"OVERLAPS(the_geom, POINT(1 2))");

        Assert.assertTrue("Overlaps was expected", resultFilter instanceof Overlaps);
    }

    @Override
    @Test
    public void equals() throws CQLException{
        // EQUALS
        Filter resultFilter = CompilerUtil.parseFilter(language,"EQUALS(the_geom, POINT(1 2))");

        Assert.assertTrue("not an instance of Equals", resultFilter instanceof Equals);

        resultFilter = CompilerUtil.parseFilter(language,"WITHIN(the_geom, POLYGON((1 2, 1 10, 5 10, 1 2)) )");

        Assert.assertTrue("Within was expected", resultFilter instanceof Within);

    }


    @Test
    public void functionAsFirstArgument() throws CQLException {

        Filter resultFilter = CompilerUtil.parseFilter(language,"INTERSECTS(centroid(the_geom), POINT(1 2))");

        Assert.assertTrue("Intersects was expected", resultFilter instanceof Intersects);
    }

    @Test
    public void functionAsSecondArgument() throws CQLException {

        Filter resultFilter = CompilerUtil.parseFilter(language,"INTERSECTS(the_geom, buffer(POINT(1 2),10))");

        Assert.assertTrue("Intersects was expected", resultFilter instanceof Intersects);


        resultFilter = CompilerUtil.parseFilter(language,"INTERSECTS(the_geom, buffer(the_geom,10))");

        Assert.assertTrue("Intersects was expected", resultFilter instanceof Intersects);
    }

    @Test
    public void functionAsFirstAndSecondArgument() throws CQLException {

        Filter resultFilter = CompilerUtil.parseFilter(language,"INTERSECTS(centroid(the_geom), buffer(POINT(1 2) ,10))");

        Assert.assertTrue("Intersects was expected", resultFilter instanceof Intersects);

    }

    @Test // TODO should be in the superclass but is required improve the cql parser like ECQL
    public void relate() throws CQLException {

        Filter resultFilter = CompilerUtil.parseFilter(language,"RELATE(geom1, POINT(1 2))");

        Assert.assertTrue( resultFilter instanceof PropertyIsEqualTo);

        PropertyIsEqualTo eq = (PropertyIsEqualTo) resultFilter;

        Function expr1 = (Function) eq.getExpression1();

        Assert.assertEquals(expr1.getName(), "relate");

        Literal expr2 = (Literal) eq.getExpression2();

        Assert.assertEquals(expr2.getValue() , true );
    }
//TODO this could be an alternative to relate like pattern sentence
//    @Test
//    public void relateWhithPattern() throws CQLException {
//
//        Filter resultFilter = CompilerUtil.parseFilter(language,"RELATE(geom1, POINT(1 2), '2FFF1FFF2')");
//
//        Assert.assertTrue(resultFilter instanceof PropertyIsEqualTo);
//
//        PropertyIsEqualTo eq = (PropertyIsEqualTo) resultFilter;
//
//        Function expr1 = (Function) eq.getExpression1();
//
//        Assert.assertEquals(expr1.getName(), "relate");
//
//        Literal expr2 = (Literal) eq.getExpression2();
//
//        Assert.assertEquals(expr2.getValue() , true );
//    }

    /**
     * This test require an extension of bbox filter
     */
    @Ignore
    public void bboxWithFunctionOnFirstArgument(){

        //  TODO function on first arg BBOX( buffer( the_geom , 10), 10,20,30,40 )
        //  TODO function on second argument BBOX( buffer( the_geom , 10), buffer( POINT( 15,15), 10) )
        //  TODO Geometry on first and second arguments BBOX( POLYGON( .... ), POLYGON( .... ), 10) )
        //  TODO Proposal in geotoolkit wiki for this
    }



}
