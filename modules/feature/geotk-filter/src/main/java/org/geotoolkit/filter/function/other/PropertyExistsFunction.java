/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.geotoolkit.filter.function.AbstractFunction;
import org.geotoolkit.util.Utilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * A new function to check if a property exists.
 * @module pending
 */
public class PropertyExistsFunction extends AbstractFunction {

    public PropertyExistsFunction(final Expression parameter) {
        super(OtherFunctionFactory.PROPERTY_EXISTS, new Expression []{parameter}, null);
    }

    private String getPropertyName() {
        Expression expr = (Expression) getParameters().get(0);

        return getPropertyName(expr);
    }

    private String getPropertyName(final Expression expr) {
        String propertyName;

        if (expr instanceof Literal) {
            propertyName = String.valueOf(((Literal) expr).getValue());
        } else if (expr instanceof PropertyName) {
            propertyName = ((PropertyName) expr).getPropertyName();
        } else {
            throw new IllegalStateException("Not a property name expression: " + expr);
        }

        return propertyName;
    }

    /**
     * @return {@link Boolean#TRUE} if the <code>feature</code>'s
     *         {@link FeatureType} contains an attribute named as the property
     *         name passed as this function argument, {@link Boolean#FALSE}
     *         otherwise.
     */
    public Object evaluate(final SimpleFeature feature) {
        String propName = getPropertyName();
        AttributeDescriptor attributeType = feature.getFeatureType().getDescriptor(propName);

        return attributeType != null;
    }

    /**
     * @return {@link Boolean#TRUE} if the Class of the object passed as
     *         argument defines a property names as the property name passed as
     *         this function argument, following the standard Java Beans naming
     *         conventions for getters. {@link Boolean#FALSE} otherwise.
     */
    @Override
    public Object evaluate(final Object bean) {
        if (bean instanceof SimpleFeature) {
            return evaluate((SimpleFeature) bean);
        }

        final String propName = getPropertyName();

        Boolean propertyExists = Boolean.TRUE;

        try {
            PropertyUtils.getProperty(bean, propName);
        } catch (NoSuchMethodException e) {
            propertyExists = Boolean.FALSE;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return propertyExists;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PropertyExists('");
        sb.append(getPropertyName());
        sb.append("')");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PropertyExistsFunction)) {

            return false;
        }
        PropertyExistsFunction other = (PropertyExistsFunction) obj;

        if (other.getParameters().size() != this.getParameters().size()) {
            return false;
        }
        if (other.getParameters().size() > 0) {
            final String propName = getPropertyName();
            final Expression otherPropNameExpr = (Expression) other.getParameters().get(0);
            final String otherPropName = getPropertyName(otherPropNameExpr);

            return Utilities.equals(propName, otherPropName);
        } else {
            return true;
        }
    }
}
