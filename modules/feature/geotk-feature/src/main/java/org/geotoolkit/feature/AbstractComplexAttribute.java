/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2010, Geomatys
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotoolkit.util.collection.UnmodifiableArrayList;

import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.Identifier;

/**
 * Default implementation of a complexeAttribut.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public abstract class AbstractComplexAttribute<I extends Identifier> extends DefaultAttribute<Collection<Property>,AttributeDescriptor,I>
        implements ComplexAttribute {

    protected AbstractComplexAttribute(AttributeDescriptor descriptor, I id) {
        super( null , descriptor, id );
    }

    protected abstract Property[] getPropertiesInternal();

    /**
     * {@inheritDoc }
     */
    @Override
    public ComplexType getType() {
        return (ComplexType)descriptor.getType();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<Property> getProperties() {
        if(value == null){
            value = UnmodifiableArrayList.wrap(getPropertiesInternal());
        }
    	return value;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<Property> getProperties(Name name) {
        //we size it to 1, in most of the cases there is always a single property for a name.
        final List<Property> matches = new ArrayList<Property>(1);
        for(Property prop : getPropertiesInternal()){
            if(prop.getName().equals(name)){
                matches.add(prop);
            }
        }
        return matches;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<Property> getProperties(String name) {
        //we size it to 1, in most of the cases there is always a single property for a name.
        final List<Property> matches = new ArrayList<Property>(1);
        for(Property prop : getPropertiesInternal()){
            if(prop.getName().getLocalPart().equals(name)){
                matches.add(prop);
            }
        }
        return matches;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Property getProperty(Name name) {
        //TODO find a faster way, hashmap ?
        for(Property prop : getPropertiesInternal()){
            if(prop.getName().equals(name)){
                return prop;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Property getProperty(String name) {
        //TODO find a faster way, hashmap ?
        for(Property prop : getPropertiesInternal()){
            if(prop.getName().getLocalPart().equals(name)){
                return prop;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setValue(Object newValue) throws IllegalArgumentException,
            IllegalStateException {
        setValue((Collection<Property>)newValue);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setValue(Collection<Property> newValues) {
        if(this.getPropertiesInternal().length != newValues.size()){
            throw new IllegalArgumentException("Expected size of the collection is " 
                    + this.getPropertiesInternal().length +" but the provided size is " +newValues.size());
        }
        newValues.toArray(this.getPropertiesInternal());
    }

}
