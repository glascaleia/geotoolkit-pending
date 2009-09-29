/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.resources.jaxb.feature.catalog;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.geotoolkit.feature.catalog.FeatureAttributeImpl;
import org.opengis.feature.catalog.FeatureAttribute;

/**
 * JAXB adapter in order to map implementing class with the GeoAPI FeatureOperation. See
 * package documentation for more information about JAXB and FeatureOperation.
 *
 * @since 2.5
 * @source $URL: http://svn.geotools.org/trunk/modules/library/metadata/src/main/java/org/geotools/resources/jaxb/metadata/FeatureOperationAdapter.java $
 * @author Guilhem Legal
 */
public class FeatureAttributeAdapter extends XmlAdapter<FeatureAttributeAdapter, FeatureAttribute> {
    
    private FeatureAttribute feature;
    
    /**
     * Empty constructor for JAXB only.
     */
    private FeatureAttributeAdapter() {
    }

    /**
     * Wraps an FeatureOperation value with a {@code SV_FeatureOperation} tags at marshalling-time.
     *
     * @param feature The FeatureOperation value to marshall.
     */
    protected FeatureAttributeAdapter(final FeatureAttribute feature) {
        this.feature = feature;
    }

    /**
     * Returns the FeatureOperation value covered by a {@code SV_FeatureOperation} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the FeatureOperation value.
     */
    protected FeatureAttributeAdapter wrap(final FeatureAttribute value) {
        return new FeatureAttributeAdapter(value);
    }

    /**
     * Returns the {@link FeatureOperationImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "FC_FeatureAttribute")
    public FeatureAttributeImpl getFeatureAttribute() {
        if (feature == null) 
            return null;
        return (feature instanceof FeatureAttributeImpl) ?
            (FeatureAttributeImpl)feature : new FeatureAttributeImpl(feature);
    }

    /**
     * Sets the value for the {@link FeatureOperationImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setFeatureOperation(final FeatureAttributeImpl feature) {
        this.feature = feature;
    }

    /**
     * Does the link between metadata red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return A java object which represents the metadata value.
     */
    @Override
    public FeatureAttribute unmarshal(FeatureAttributeAdapter value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.feature;
    }

    /**
     * Does the link between java object and the way they will be marshalled into
     * an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The bound type value, here the FeatureOperation.
     * @return The adapter for this FeatureOperation.
     */
    @Override
    public FeatureAttributeAdapter marshal(FeatureAttribute value) throws Exception {
        return new FeatureAttributeAdapter(value);
    }

    
    

}