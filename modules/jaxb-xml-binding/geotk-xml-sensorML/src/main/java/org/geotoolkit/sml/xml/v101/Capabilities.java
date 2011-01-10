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
package org.geotoolkit.sml.xml.v101;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.sml.xml.AbstractCapabilities;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.DataRecord;
import org.geotoolkit.swe.xml.SimpleDataRecord;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordEntry;
import org.geotoolkit.swe.xml.v101.AbstractDataRecordEntry;
import org.geotoolkit.swe.xml.v101.DataRecordType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element ref="{http://www.opengis.net/swe/1.0.1}AbstractDataRecord"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}token" />
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
    "abstractDataRecord"
})
@XmlRootElement(name = "capabilities")
public class Capabilities implements AbstractCapabilities {

    @XmlElementRef(name = "AbstractDataRecord", namespace = "http://www.opengis.net/swe/1.0.1", type = JAXBElement.class)
    private JAXBElement<? extends AbstractDataRecordEntry> abstractDataRecord;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    private String name;
    @XmlAttribute(namespace = "http://www.opengis.net/gml")
    @XmlSchemaType(name = "anyURI")
    private String remoteSchema;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String type;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String href;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String role;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String arcrole;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String title;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String show;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String actuate;

    public Capabilities() {

    }

    public Capabilities(final DataRecordType dataRecord) {
        org.geotoolkit.swe.xml.v101.ObjectFactory facto = new org.geotoolkit.swe.xml.v101.ObjectFactory();
        this.abstractDataRecord = facto.createDataRecord(dataRecord);
    }

    public Capabilities(final AbstractCapabilities capa) {
        if (capa != null) {
            if (capa.getDataRecord() != null) {
                AbstractDataRecord record = capa.getDataRecord();
                org.geotoolkit.swe.xml.v101.ObjectFactory factory = new org.geotoolkit.swe.xml.v101.ObjectFactory();
                if (record instanceof SimpleDataRecord) {
                    abstractDataRecord = factory.createSimpleDataRecord(new SimpleDataRecordEntry((SimpleDataRecord)record));
                } else if (record instanceof DataRecord) {
                    abstractDataRecord = factory.createDataRecord(new DataRecordType((DataRecord)record));
                } else {
                    System.out.println("UNINPLEMENTED CASE:" + record);
                }
            }
            this.actuate = capa.getActuate();
            this.arcrole = capa.getArcrole();
            this.href    = capa.getHref();
            this.name    = capa.getName();
            this.remoteSchema = capa.getRemoteSchema();
            this.role    = capa.getRole();
            this.show    = capa.getShow();
            this.title   = capa.getTitle();
            this.type    = capa.getType();
        }
    }

    /**
     * Gets the value of the abstractDataRecord property.
     *     
     */
    public JAXBElement<? extends AbstractDataRecordEntry> getAbstractDataRecord() {
        return abstractDataRecord;
    }

    public AbstractDataRecordEntry getDataRecord() {
        if (abstractDataRecord != null) {
            return abstractDataRecord.getValue();
        }
        return null;
    }

    /**
     * Sets the value of the abstractDataRecord property.
     * 
     */
    public void setAbstractDataRecord(final JAXBElement<? extends AbstractDataRecordEntry> value) {
        this.abstractDataRecord = ((JAXBElement<? extends AbstractDataRecordEntry> ) value);
    }

    /**
     * Gets the value of the name property.
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the remoteSchema property.
     * 
     */
    public String getRemoteSchema() {
        return remoteSchema;
    }

    /**
     * Sets the value of the remoteSchema property.
     * 
    */
    public void setRemoteSchema(final String value) {
        this.remoteSchema = value;
    }

    /**
     * Gets the value of the type property.
     * 
    */
    public String getType() {
        return type;
     }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(final String value) {
        this.type = value;
    }

    /**
     * Gets the value of the href property.
     * 
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     */
    public void setHref(final String value) {
        this.href = value;
    }

    /**
     * Gets the value of the role property.
     * 
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     */
    public void setRole(final String value) {
        this.role = value;
    }

    /**
     * Gets the value of the arcrole property.
     * 
     */
    public String getArcrole() {
        return arcrole;
    }

    /**
     * Sets the value of the arcrole property.
     * 
     */
    public void setArcrole(final String value) {
        this.arcrole = value;
    }

    /**
     * Gets the value of the title property.
     * 
    */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     */
    public void setTitle(final String value) {
        this.title = value;
    }

    /**
     * Gets the value of the show property.
     * 
     */
    public String getShow() {
        return show;
    }

    /**
     * Sets the value of the show property.
     * 
    */
    public void setShow(final String value) {
        this.show = value;
    }

    /**
     * Gets the value of the actuate property.
     * 
    */
    public String getActuate() {
        return actuate;
    }

    /**
     * Sets the value of the actuate property.
     * 
    */
    public void setActuate(final String value) {
        this.actuate = value;
    }

}
