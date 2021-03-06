/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.ogc.xml.v110;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.util.Utilities;


/**
 * <p>Java class for Temporal_CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Temporal_CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TemporalOperands" type="{http://www.opengis.net/ogc}TemporalOperandsType"/>
 *         &lt;element name="TemporalOperators" type="{http://www.opengis.net/ogc}TemporalOperatorsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Temporal_CapabilitiesType", propOrder = {
    "temporalOperands",
    "temporalOperators"
})
public class TemporalCapabilitiesType {

    @XmlElement(name = "TemporalOperands", required = true)
    private TemporalOperandsType temporalOperands;
    @XmlElement(name = "TemporalOperators", required = true)
    private TemporalOperatorsType temporalOperators;

    /**
     * Gets the value of the temporalOperands property.
     * 
     */
    public TemporalOperandsType getTemporalOperands() {
        return temporalOperands;
    }

    /**
     * Sets the value of the temporalOperands property.
     * 
     */
    public void setTemporalOperands(final TemporalOperandsType value) {
        this.temporalOperands = value;
    }

    /**
     * Gets the value of the temporalOperators property.
     * 
     */
    public TemporalOperatorsType getTemporalOperators() {
        return temporalOperators;
    }

    /**
     * Sets the value of the temporalOperators property.
     * 
     */
    public void setTemporalOperators(final TemporalOperatorsType value) {
        this.temporalOperators = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[TemporalCapabilitiesType]").append("\n");
        if (temporalOperands != null) {
            sb.append("temporalOperands: ").append(temporalOperands).append('\n');
        }
        if (temporalOperators != null) {
            sb.append("temporalOperators: ").append(temporalOperators).append('\n');
        }
        return sb.toString();
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

       if (object instanceof TemporalCapabilitiesType) {
           final TemporalCapabilitiesType that = (TemporalCapabilitiesType) object;
       
            return Utilities.equals(this.temporalOperands, that.temporalOperands) &&
                   Utilities.equals(this.temporalOperators, that.temporalOperators);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.temporalOperands != null ? this.temporalOperands.hashCode() : 0);
        hash = 23 * hash + (this.temporalOperators != null ? this.temporalOperators.hashCode() : 0);
        return hash;
    }
}
