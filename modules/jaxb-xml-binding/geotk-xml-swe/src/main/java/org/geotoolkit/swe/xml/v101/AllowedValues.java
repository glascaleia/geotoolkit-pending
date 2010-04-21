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
package org.geotoolkit.swe.xml.v101;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.swe.xml.AbstractAllowedValues;
import org.geotoolkit.util.Utilities;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;choice>
 *           &lt;element name="min" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *           &lt;element name="max" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="interval" type="{http://www.opengis.net/swe/1.0.1}decimalPair"/>
 *           &lt;element name="valueList" type="{http://www.opengis.net/swe/1.0.1}decimalList"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "min",
    "max",
    "intervalOrValueList"
})
@XmlRootElement(name = "AllowedValues")
public class AllowedValues implements AbstractAllowedValues {

    private Double min;
    private Double max;
    @XmlElementRefs({
        @XmlElementRef(name = "interval", namespace = "http://www.opengis.net/swe/1.0.1", type = JAXBElement.class),
        @XmlElementRef(name = "valueList", namespace = "http://www.opengis.net/swe/1.0.1", type = JAXBElement.class)
    })
    private List<JAXBElement<List<Double>>> intervalOrValueList;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    private String id;

    public AllowedValues() {

    }

    public AllowedValues(AbstractAllowedValues av) {
        if (av != null) {
            this.id = av.getId();
            this.max = av.getMax();
            this.min = av.getMin();
            throw new IllegalArgumentException("AllowedValues are not yet convertible");
        }
    }

    /**
     * Gets the value of the min property.
     */
    public Double getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     */
    public void setMin(Double value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     */
    public Double getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     */
    public void setMax(Double value) {
        this.max = value;
    }

    /**
     * Gets the value of the intervalOrValueList property.
     */
    public List<JAXBElement<List<Double>>> getIntervalOrValueList() {
        if (intervalOrValueList == null) {
            intervalOrValueList = new ArrayList<JAXBElement<List<Double>>>();
        }
        return this.intervalOrValueList;
    }

    public List<Double> getInterval() {
        for (JAXBElement<List<Double>> jb : getIntervalOrValueList()) {
            if (jb.getName().getLocalPart().equals("interval")) {
                return jb.getValue();
            }
        }
        return null;
    }

    public void setInterval(List<Double> interval) {
        if (interval != null) {
            if (this.intervalOrValueList == null) {
                this.intervalOrValueList = new ArrayList<JAXBElement<List<Double>>>();
            }
            ObjectFactory factory = new ObjectFactory();
            this.intervalOrValueList.add(factory.createAllowedValuesInterval(interval));
        }
    }

    public void setInterval(Double interval) {
        if (interval != null) {
            if (this.intervalOrValueList == null) {
                this.intervalOrValueList = new ArrayList<JAXBElement<List<Double>>>();
            }
            ObjectFactory factory = new ObjectFactory();
            this.intervalOrValueList.add(factory.createAllowedValuesInterval(Arrays.asList(interval)));
        }
    }

    public List<Double> getValueList() {
        for (JAXBElement<List<Double>> jb : getIntervalOrValueList()) {
            if (jb.getName().getLocalPart().equals("valueList")) {
                return jb.getValue();
            }
        }
        return null;
    }

    public void setValueList(List<Double> valueList) {
        if (valueList != null) {
            if (this.intervalOrValueList == null) {
                this.intervalOrValueList = new ArrayList<JAXBElement<List<Double>>>();
            }
            ObjectFactory factory = new ObjectFactory();
            this.intervalOrValueList.add(factory.createAllowedValuesValueList(valueList));
        }
    }

    public void setValueList(Double valueList) {
        if (valueList != null) {
            if (this.intervalOrValueList == null) {
                this.intervalOrValueList = new ArrayList<JAXBElement<List<Double>>>();
            }
            ObjectFactory factory = new ObjectFactory();
            this.intervalOrValueList.add(factory.createAllowedValuesValueList(Arrays.asList(valueList)));
        }
    }
    
    /**
     * Gets the value of the id property.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Verify that the object is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof AllowedValues) {
            final AllowedValues that = (AllowedValues) object;
            boolean intervalOrValue = false;
            if (this.intervalOrValueList != null && that.intervalOrValueList != null) {
                if (this.intervalOrValueList.size() != that.intervalOrValueList.size()) {
                    intervalOrValue = false;
                } else {
                    intervalOrValue = true;
                    for (int i = 0; i < this.intervalOrValueList.size(); i++) {
                        JAXBElement<List<Double>> thisJB = this.intervalOrValueList.get(i);
                        JAXBElement<List<Double>> thatJB = that.intervalOrValueList.get(i);
                        if (!Utilities.equals(thisJB.getValue(), thatJB.getValue())) {
                            intervalOrValue = false;
                        }
                    }
                }
            } else if (this.intervalOrValueList == null && that.intervalOrValueList == null) {
                intervalOrValue = true;
            }
            return Utilities.equals(this.id,  that.id) &&
                   Utilities.equals(this.max, that.max) &&
                   Utilities.equals(this.min, that.min) &&
                   intervalOrValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.min != null ? this.min.hashCode() : 0);
        hash = 59 * hash + (this.max != null ? this.max.hashCode() : 0);
        hash = 59 * hash + (this.intervalOrValueList != null ? this.intervalOrValueList.hashCode() : 0);
        hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
