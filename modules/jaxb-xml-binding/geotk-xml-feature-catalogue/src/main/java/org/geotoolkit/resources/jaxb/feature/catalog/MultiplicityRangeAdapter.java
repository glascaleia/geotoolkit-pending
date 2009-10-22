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

import org.geotoolkit.util.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB adapter in order to map implementing class with the GeoAPI MultiplicityRange. See
 * package documentation for more information about JAXB and MultiplicityRange.
 *
 * @module pending
 * @since 2.5
 * @author Guilhem Legal
 */
public class MultiplicityRangeAdapter extends XmlAdapter<MultiplicityRangeAdapter, MultiplicityRange> {
    
    private MultiplicityRange multiplicityRange;
    
    /**
     * Empty constructor for JAXB only.
     */
    private MultiplicityRangeAdapter() {
    }

    /**
     * Wraps an MultiplicityRange value with a {@code SV_MultiplicityRange} tags at marshalling-time.
     *
     * @param multiplicityRange The MultiplicityRange value to marshall.
     */
    protected MultiplicityRangeAdapter(final MultiplicityRange multiplicityRange) {
        this.multiplicityRange = multiplicityRange;
    }

    /**
     * Returns the MultiplicityRange value covered by a {@code SV_MultiplicityRange} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the MultiplicityRange value.
     */
    protected MultiplicityRangeAdapter wrap(final MultiplicityRange value) {
        return new MultiplicityRangeAdapter(value);
    }

    /**
     * Returns the {@link MultiplicityRangeImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MultiplicityRange", namespace = "http://www.isotc211.org/2005/gco")
    public MultiplicityRange getMultiplicityRange() {
        return multiplicityRange;
    }

    /**
     * Sets the value for the {@link MultiplicityRangeImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setMultiplicityRange(final MultiplicityRange multiplicityRange) {
        this.multiplicityRange = multiplicityRange;
    }

    /**
     * Does the link between metadata red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return A java object which represents the metadata value.
     */
    @Override
    public MultiplicityRange unmarshal(MultiplicityRangeAdapter value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.multiplicityRange;
    }

    /**
     * Does the link between java object and the way they will be marshalled into
     * an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The bound type value, here the MultiplicityRange.
     * @return The adapter for this MultiplicityRange.
     */
    @Override
    public MultiplicityRangeAdapter marshal(MultiplicityRange value) throws Exception {
        return new MultiplicityRangeAdapter(value);
    }

    
    

}
