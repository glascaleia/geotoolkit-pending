/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.feature.simple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.type.DefaultFeatureType;
import org.geotoolkit.util.collection.UnmodifiableArrayList;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

/**
 * Implementation fo SimpleFeatureType, subtypes must be atomic and are stored
 * in a list.
 *
 * @author Justin
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 * @module pending
 */
public class DefaultSimpleFeatureType extends DefaultFeatureType implements SimpleFeatureType {

    private final AttributeType[] types;
    private final List<AttributeType> typesList;
    final Map<Object, Integer> index;

    public DefaultSimpleFeatureType(final Name name, final List<AttributeDescriptor> schema,
            final GeometryDescriptor defaultGeometry, final boolean isAbstract,
            final List<Filter> restrictions, final AttributeType superType,
            final InternationalString description){
        // Note intentional circumvention of generics type checking;
        // this is only valid if schema is not modified.
        super(name, (List) schema, defaultGeometry, isAbstract, restrictions,
                superType, description);
        index = buildIndex(this);


        //for faster access
        types = new AttributeType[descriptors.length];
        for(int i=0; i<descriptors.length;i++){
            types[i] = (AttributeType) descriptors[i].getType();
        }
        typesList = UnmodifiableArrayList.wrap(types);
    }

    /**
     * @see org.opengis.feature.simple.SimpleFeatureType#getAttributeDescriptors()
     */
    @Override
    public final List<AttributeDescriptor> getAttributeDescriptors() {
        // TODO we should find a way to use generic and avoid casts on this method
        // and all getDescriptor methods.
        return (List) getDescriptors();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeType> getTypes() {
        return typesList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeType getType(final Name name) {
        final AttributeDescriptor attribute = getDescriptor(name);
        if (attribute != null) {
            return attribute.getType();
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeType getType(final String name) {
        final AttributeDescriptor attribute = getDescriptor(name);
        if (attribute != null) {
            return attribute.getType();
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeType getType(final int index) {
        return types[index];
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeDescriptor getDescriptor(final Name name) {
        return (AttributeDescriptor) super.getDescriptor(name);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeDescriptor getDescriptor(final String name) {
        return (AttributeDescriptor) super.getDescriptor(name);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeDescriptor getDescriptor(final int index) {
        return (AttributeDescriptor) descriptors[index];
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int indexOf(final Name name) {
        if (name.getNamespaceURI() == null) {
            return indexOf(name.getLocalPart());
        }
        // otherwise do a full scan
        for (int i=0; i<descriptors.length; i++){
            PropertyDescriptor descriptor = descriptors[i];
            if (descriptor.getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int indexOf(final String name) {
        Integer idx = index.get(name);
        return (idx != null) ? idx : -1;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getAttributeCount() {
        return descriptors.length;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getTypeName() {
        return name.getLocalPart();
    }

    /**
     * Builds the name -> position index used by simple features for fast attribute lookup
     * @param featureType
     * @return
     */
    static Map<Object, Integer> buildIndex(final SimpleFeatureType featureType) {

        // build an index of attribute name to index
        final Map<Object, Integer> index = new HashMap<Object, Integer>();

        final List<AttributeDescriptor> descs = featureType.getAttributeDescriptors();
        final int n = descs.size();
        //must iterate backward to make first attribut with same local part first
        for(int i=n-1; i>=0; i--){
            final AttributeDescriptor ad = descs.get(i);
            index.put(ad.getName(), i);
            index.put(new DefaultName(ad.getName().getLocalPart()), i);
            //must add possible string combinaison
            index.put(ad.getName().getLocalPart(), i);
            index.put(DefaultName.toJCRExtendedForm(ad.getName()), i);
            index.put(DefaultName.toExtendedForm(ad.getName()), i);
        }

        final GeometryDescriptor geomDesc = featureType.getGeometryDescriptor();
        if (geomDesc != null) {
            index.put(null, index.get(geomDesc.getLocalName()));
        }
        return index;
    }
}
