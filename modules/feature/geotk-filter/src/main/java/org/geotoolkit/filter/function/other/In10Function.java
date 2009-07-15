/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.filter.function.other;

import org.geotoolkit.filter.function.AbstractFunction;
import org.geotoolkit.filter.function.geometry.StaticGeometry;
import org.opengis.filter.expression.Expression;


public class In10Function extends AbstractFunction {

    public In10Function(Expression expr1, Expression expr2, Expression expr3, Expression expr4,
                               Expression expr5, Expression expr6, Expression expr7, Expression expr8,
                               Expression expr9, Expression expr10, Expression expr11)
    {
        super("in10", new Expression[]{expr1,expr2,expr3,expr4,expr5,expr6,expr7,expr8,expr9,expr10,expr11}, null);
    }

    @Override
    public Object evaluate(Object feature) {
        Object arg0;
        Object arg1;
        Object arg2;
        Object arg3;
        Object arg4;
        Object arg5;
        Object arg6;
        Object arg7;
        Object arg8;
        Object arg9;
        Object arg10;

        try { // attempt to get value and perform conversion
            arg0 = (Object) parameters.get(0).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #0 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg1 = (Object) parameters.get(1).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #1 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg2 = (Object) parameters.get(2).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #2 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg3 = (Object) parameters.get(3).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #3 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg4 = (Object) parameters.get(4).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #4 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg5 = (Object) parameters.get(5).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #5 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg6 = (Object) parameters.get(6).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #6 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg7 = (Object) parameters.get(7).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #7 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg8 = (Object) parameters.get(8).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #8 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg9 = (Object) parameters.get(9).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #9 - expected type Object");
        }

        try { // attempt to get value and perform conversion
            arg10 = (Object) parameters.get(10).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function in10 argument #10 - expected type Object");
        }

        return new Boolean(StaticGeometry.in10(arg0, arg1, arg2, arg3, arg4,
                arg5, arg6, arg7, arg8, arg9, arg10));
    }
}
