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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.swe.xml.AbstractDataValueProperty;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.util.Utilities;


/**
 * <p>Java class for DataArrayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataArrayType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/swe/1.0.1}AbstractDataArrayType">
 *       &lt;sequence>
 *         &lt;element name="elementType" type="{http://www.opengis.net/swe/1.0.1}DataComponentPropertyType"/>
 *         &lt;group ref="{http://www.opengis.net/swe/1.0.1}EncodedValuesGroup" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataArrayType", propOrder = {
    "elementType",
    "encoding",
    "values"
})
@XmlRootElement(name = "DataArray")
public class DataArrayType extends AbstractDataArrayType implements DataArray {

    @XmlElement(required = true)
    private DataComponentPropertyType elementType;
    private AbstractEncodingPropertyType encoding;
    private String values;

    /**
     * An empty constructor used by JAXB.
     */
    DataArrayType() {
        
    }

    /**
     * Clone a new data array.
     */
    public DataArrayType(final DataArray array) {
        super(array);
        if (array != null) {
            if (array.getPropertyElementType() != null) {
                this.elementType = new DataComponentPropertyType(array.getPropertyElementType());
            }
            if (array.getEncoding() != null) {
                this.encoding = new AbstractEncodingPropertyType(array.getEncoding());
            }
            this.values = array.getValues();
        }

    }

    /**
     * Build a new data array.
     */
    public DataArrayType(final String id, final int count, final AbstractDataRecordType elementType,
            final AbstractEncodingType encoding, final String values) {
        super(id, count);
        this.elementType = new DataComponentPropertyType(elementType, id);
        this.encoding    = new AbstractEncodingPropertyType(encoding);
        this.values      = values;
        
    }
    
    /**
     * Gets the value of the elementType property.
     */
    public AbstractDataRecordType getElementType() {
        if (elementType != null) {
            return elementType.getAbstractRecord();
        }
        return null;
    }
    
    public DataComponentPropertyType getPropertyElementType(){
        return elementType;
    }

    public void setPropertyElementType(final DataComponentPropertyType elementType){
        this.elementType = elementType;
    }

    /**
     * Gets the value of the encoding property.
     */
    public AbstractEncodingType getEncoding() {
        if (encoding != null) {
            return encoding.getEncoding();
        }
        return null;
    }
    
    public AbstractEncodingPropertyType getPropertyEncoding(){
        return encoding;
    }
    
    public void setPropertyEncoding(final AbstractEncodingPropertyType encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the value of the values property.
     */
    public String getValues() {
        return values;
    }
    
    /**
     * Sets the value of the values property.
     */
    public void setValues(final String values) {
        this.values = values;
    }

    public void updateArray(final String values, final int nbValues) {
        this.values = values;
        this.setElementCount(nbValues);
    }
    
    public AbstractDataValueProperty getDataValues() {
        return null;
    }
    
    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof DataArrayType && super.equals(object)) {
            final DataArrayType that = (DataArrayType) object;
            return Utilities.equals(this.elementType,   that.elementType)   &&
                   Utilities.equals(this.encoding,    that.encoding)    &&
                   Utilities.equals(this.values,       that.values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.elementType != null ? this.elementType.hashCode() : 0);
        hash = 29 * hash + (this.encoding != null ? this.encoding.hashCode() : 0);
        hash = 29 * hash + (this.values != null ? this.values.hashCode() : 0);
        return hash;
    }
    
    /**
     * Return a string representing the dataArray.
     */
    @Override
    public String toString() {
        StringBuilder s    = new StringBuilder(super.toString());
        char lineSeparator = '\n';
        if (elementType != null)
            s.append(" elementType=").append(elementType.toString()).append(lineSeparator);
        if (encoding != null)
            s.append(" encoding:").append(encoding.toString()).append(lineSeparator);
        if (values != null) {
            //we format a little the result
            String formatedValues = values;
            formatedValues        = formatedValues.replace("\t", " ");
            formatedValues        = formatedValues.replace("\n", " "); 
            while (formatedValues.indexOf("  ") != -1) {
                formatedValues    = formatedValues.replace("  ", "");
            }
            s.append("values=").append(formatedValues).append(lineSeparator);
        }
        return s.toString();
    }

    
}