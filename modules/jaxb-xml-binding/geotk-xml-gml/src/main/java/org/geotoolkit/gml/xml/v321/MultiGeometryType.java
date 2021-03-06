/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2012, Geomatys
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


package org.geotoolkit.gml.xml.v321;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MultiGeometryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MultiGeometryType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractGeometricAggregateType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}geometryMember" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}geometryMembers" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiGeometryType", propOrder = {
    "geometryMember",
    "geometryMembers"
})
public class MultiGeometryType
    extends AbstractGeometricAggregateType
{

    private List<GeometryPropertyType> geometryMember;
    private GeometryArrayPropertyType geometryMembers;

    /**
     * Gets the value of the geometryMember property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geometryMember property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeometryMember().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeometryPropertyType }
     * 
     * 
     */
    public List<GeometryPropertyType> getGeometryMember() {
        if (geometryMember == null) {
            geometryMember = new ArrayList<GeometryPropertyType>();
        }
        return this.geometryMember;
    }

    /**
     * Gets the value of the geometryMembers property.
     * 
     * @return
     *     possible object is
     *     {@link GeometryArrayPropertyType }
     *     
     */
    public GeometryArrayPropertyType getGeometryMembers() {
        return geometryMembers;
    }

    /**
     * Sets the value of the geometryMembers property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeometryArrayPropertyType }
     *     
     */
    public void setGeometryMembers(GeometryArrayPropertyType value) {
        this.geometryMembers = value;
    }

}
