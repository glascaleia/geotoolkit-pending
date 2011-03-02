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
package org.geotoolkit.filter.binarycomparison;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import org.geotoolkit.util.Converters;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.expression.Expression;

import static org.geotoolkit.util.ArgumentChecks.*;

/**
 * Immutable abstract "binary comparison operator".
 *
 * @author Johann Sorel (Geomatys)
 * @param <E> Expression or subclass
 * @param <F> Expression or subclass
 * @module pending
 */
public abstract class AbstractBinaryComparisonOperator<E extends Expression,F extends Expression> 
                                                implements BinaryComparisonOperator,Serializable{

    protected final E left;
    protected final F right;
    protected final boolean match;

    public AbstractBinaryComparisonOperator(final E left, final F right, final boolean match) {
        ensureNonNull("left", left);
        ensureNonNull("right", right);
        this.left = left;
        this.right = right;
        this.match = match;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public E getExpression1() {
        return left;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public F getExpression2() {
        return right;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isMatchingCase() {
        return match;
    }

    protected Integer compare(final Object object){
        Object objleft = left.evaluate(object);

        if(!(objleft instanceof Comparable)){
            return null;
        }

        //see if the right type might be more appropriate for test
        if( !(objleft instanceof Date) ){
            Object objright = right.evaluate(object);

            if(objright instanceof Date){
                //object right class is more appropriate

                Object cdLeft = Converters.convert(objleft, Date.class);
                if(cdLeft != null){
                    return ((Comparable)cdLeft).compareTo(objright);
                }

            }

        }

        Object objright = right.evaluate(object,objleft.getClass());

        if (objleft instanceof java.sql.Date && objright instanceof java.sql.Date) {
            final Calendar cal1 = Calendar.getInstance();
            cal1.setTime((java.sql.Date)objleft);
            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);
            
            final Calendar cal2 = Calendar.getInstance();
            cal2.setTime((java.sql.Date)objright);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);

            return cal1.compareTo(cal2);
        }

        if(objright == null){
            return null;
        }
        return ((Comparable)objleft).compareTo(objright);
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.left != null ? this.left.hashCode() : 0);
        hash = 61 * hash + (this.right != null ? this.right.hashCode() : 0);
        hash = 61 * hash + (this.match ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractBinaryComparisonOperator<E, F> other = (AbstractBinaryComparisonOperator<E, F>) obj;
        if (this.left != other.left && (this.left == null || !this.left.equals(other.left))) {
            return false;
        }
        if (this.right != other.right && (this.right == null || !this.right.equals(other.right))) {
            return false;
        }
        if (this.match != other.match) {
            return false;
        }
        return true;
    }

}
