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
package org.geotoolkit.wps.xml.v100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.wps.xml.WPSResponse;


/**
 * WPS operation response base, for all WPS operations except GetCapabilities. 
 * 
 * <p>Java class for ResponseBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseBaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="WPS" />
 *       &lt;attribute name="version" use="required" type="{http://www.opengis.net/ows/1.1}VersionType" fixed="1.0.0" />
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang use="required""/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseBaseType")
@XmlSeeAlso({
    ProcessDescriptions.class,
    ExecuteResponse.class
})
public class ResponseBaseType implements WPSResponse {

    @XmlAttribute(required = true)
    protected String service;
    @XmlAttribute(required = true)
    protected String version;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace", required = true)
    protected String lang;

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getService() {
        if (service == null) {
            return "WPS";
        } else {
            return service;
        }
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setService(final String value) {
        this.service = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        if (version == null) {
            return "1.0.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(final String value) {
        this.version = value;
    }

    /**
     * RFC 4646 language code of the human-readable text (e.g. "en-CA").
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * RFC 4646 language code of the human-readable text (e.g. "en-CA").
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(final String value) {
        this.lang = value;
    }

}
