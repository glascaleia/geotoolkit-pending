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
package org.geotoolkit.feature;

import org.geotoolkit.util.Utilities;
import org.geotoolkit.util.Converters;

import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.identity.Identifier;

/**
 * Default Attribut implementation.
 *
 * @author Johann Sorel (Geomatys)
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @author Ian Schneider
 * @author Jody Garnett
 * @author Gabriel Roldan
 * @module pending
 */
public class DefaultAttribute<V extends Object, D extends AttributeDescriptor, I extends Identifier>
        extends DefaultProperty<V,D> implements Attribute {
    
    /**
     * id of the attribute.
     */
    protected I id;

    public DefaultAttribute(final V content, final D descriptor, final I id) {
        super(content, descriptor);
        this.id = id;
    }

    /**
     * Protected constructor, used by subclass which initialize the content after some
     * processing.
     * 
     * @param descriptor
     * @param id
     */
    protected DefaultAttribute(final D descriptor, final I id) {
        super(descriptor);
        this.id = id;
    }

    /**
     * This contructor is only available for complex types,
     * Complex objects are the only ones allowed to have a property type
     * whitout descriptor since they may be top level object.
     * A Descriptor is only necessary if the property is defined inside another
     * type.
     * @param type
     */
    protected DefaultAttribute(final V content, final AttributeType type, final I id){
        super(content, type);
        this.id = id;
    }

    /**
     * This contructor is only available for complex types,
     * Complex objects are the only ones allowed to have a property type
     * whitout descriptor since they may be top level object.
     * A Descriptor is only necessary if the property is defined inside another
     * type.
     * @param type
     */
    protected DefaultAttribute(final AttributeType type, final I id){
        super(type);
        this.id = id;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public I getIdentifier() {
        return id;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeType getType() {
        return (AttributeType)super.getType();
    }

    /**
     * Override of setValue to convert the newValue to specified type if need be.
     */
    @Override
    public void setValue(final Object newValue) throws IllegalArgumentException, IllegalStateException {
        super.setValue(newValue);

        //todo, do we perform a validation here ? seems to cost time when reusing features
        //this should be leaved to the user concern, by calling validate on the feature is necessary
        //newValue = checkType(newValue);
        //super.setValue(newValue);
    }

    /**
     * Override of hashCode.
     *
     * @return hashCode for this object.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + (37 * (id == null ? 0 : id.hashCode()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof DefaultAttribute)) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        final DefaultAttribute att = (DefaultAttribute) obj;

        return Utilities.equals(id, att.id);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void validate() {
        FeatureValidationUtilities.validate(this, this.getValue());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(":");
        sb.append(getName());
        if (id != null) {
            sb.append("(id=").append(id).append(')');
        }
        sb.append("=");
        sb.append(value);
        return sb.toString();
    }

    /**
     * Allows this Attribute to convert an argument to its prefered storage
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
    protected Object checkType(Object value) throws IllegalArgumentException {
        if (value != null) {
            final Class<?> target = getType().getBinding();
            if (!target.isAssignableFrom(value.getClass())) {
                // attempt to convert
                final Object converted = Converters.convert(value, target);
                if (converted != null) {
                    value = converted;
                }
            }
        }

        return value;
    }
}
