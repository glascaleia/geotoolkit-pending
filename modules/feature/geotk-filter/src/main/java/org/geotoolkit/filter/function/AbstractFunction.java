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
package org.geotoolkit.filter.function;

import java.util.List;
import org.geotoolkit.filter.AbstractExpression;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * Immutable abstract function.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractFunction extends AbstractExpression implements Function {

    protected final String name;
    protected final List<Expression> parameters;
    protected final Literal fallback;

    public AbstractFunction(String name, Expression[] parameters, Literal fallback) {
        if(name == null){
            throw new NullPointerException("name can not be null");
        }
        this.name = name;
        this.parameters = UnmodifiableArrayList.wrap(parameters);
        this.fallback = fallback;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Expression> getParameters() {
        return parameters;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Literal getFallbackValue() {
        return fallback;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object accept(ExpressionVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 89 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        hash = 89 * hash + (this.fallback != null ? this.fallback.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractFunction other = (AbstractFunction) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
            return false;
        }
        if (this.fallback != other.fallback && (this.fallback == null || !this.fallback.equals(other.fallback))) {
            return false;
        }
        return true;
    }

}
