/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009 Geomatys
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
package org.geotoolkit.feature.type;

import java.util.List;

import org.geotoolkit.util.converter.Classes;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;


/**
 * Base class for attribute types.
 *
 * @author Justin Deoliveira, The Open Planning Project
 * @module pending
 */
public class DefaultAttributeType<T extends AttributeType> extends DefaultPropertyType<T> implements AttributeType {

    protected final boolean identified;

    public DefaultAttributeType(final Name name, final Class<?> binding, final boolean identified,
            final boolean isAbstract, final List<Filter> restrictions, final T superType,
            final InternationalString description){
        super(name, binding, isAbstract, restrictions, superType, description);

//        TODO need the exact definition of a simple feature type to make this verification
//        if(!(this instanceof ComplexType)){
//            //only make this check if we are sure to be a simple attribut type
//            if(Collection.class.isAssignableFrom(binding)){
//                throw new IllegalArgumentException("Binding class is : "+ binding +" a Complex type should have been used.");
//            }
//        }

        this.identified = identified;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isIdentified() {
        return identified;
    }

    /**
     * Allows this AttributeType to convert an argument to its prefered storage
     * type. If no parsing is possible, returns the original value. If a parse
     * is attempted, yet fails (i.e. a poor decimal format) throw the Exception.
     * This is mostly for use internally in Features, but implementors should
     * simply follow the rules to be safe.
     *
     * @param value
     *            the object to attempt parsing of.
     *
     * @return <code>value</code> converted to the preferred storage of this
     *         <code>AttributeType</code>. If no parsing was possible then
     *         the same object is returned.
     *
     * @throws IllegalArgumentException
     *             if parsing is attempted and is unsuccessful.
     */
    public Object parse(Object value) throws IllegalArgumentException {
        //do nothing, sublcasses should override
        return value;
    }

    public Object createDefaultValue() {
        return null;
    }

    /**
     * Override of hashcode.
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ Boolean.valueOf(identified).hashCode();

    }

    /**
     * Override of equals.
     *
     * @param other
     *            the object to be tested for equality.
     *
     * @return whether other is equal to this attribute Type.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AttributeType)) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        AttributeType att = (AttributeType) other;

        if (identified != att.isIdentified()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Classes.getShortClassName(this));
        sb.append(" ");
        sb.append(getName());
        if (isAbstract()) {
            sb.append(" abstract");
        }
        if (isIdentified()) {
            sb.append(" identified");
        }
        if (superType != null) {
            sb.append(" extends ");
            sb.append(superType.getName().getLocalPart());
        }
        if (binding != null) {
            sb.append("<");
            sb.append(Classes.getShortName(binding));
            sb.append(">");
        }
        if (description != null) {
            sb.append("\n\tdescription=");
            sb.append(description);
        }
        if (restrictions != null && !restrictions.isEmpty()) {
            sb.append("\nrestrictions=");
            boolean first = true;
            for (Filter filter : restrictions) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" AND ");
                }
                sb.append(filter);
            }
        }
        return sb.toString();
    }
}
