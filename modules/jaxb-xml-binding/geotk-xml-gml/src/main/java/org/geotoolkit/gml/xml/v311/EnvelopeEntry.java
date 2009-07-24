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
package org.geotoolkit.gml.xml.v311;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.util.Utilities;


/**
 * Envelope defines an extent using a pair of positions defining opposite corners in arbitrary dimensions. The first direct 
 * 			position is the "lower corner" (a coordinate position consisting of all the minimal ordinates for each dimension for all points within the envelope), 
 * 			the second one the "upper corner" (a coordinate position consisting of all the maximal ordinates for each dimension for all points within the 
 * 			envelope).
 * 
 * <p>Java class for EnvelopeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EnvelopeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="lowerCorner" type="{http://www.opengis.net/gml}DirectPositionType"/>
 *           &lt;element name="upperCorner" type="{http://www.opengis.net/gml}DirectPositionType"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}coord" maxOccurs="2" minOccurs="2"/>
 *         &lt;element ref="{http://www.opengis.net/gml}pos" maxOccurs="2" minOccurs="2"/>
 *         &lt;element ref="{http://www.opengis.net/gml}coordinates"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.opengis.net/gml}SRSReferenceGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnvelopeType", propOrder = {
    "id",
    "lowerCorner",
    "upperCorner",
    "pos",
    "coordinates"
})
@XmlSeeAlso({
    EnvelopeWithTimePeriodType.class
})
public class EnvelopeEntry {

    private String id;
    protected DirectPositionType lowerCorner;
    protected DirectPositionType upperCorner;
    protected List<DirectPositionType> pos;
    protected CoordinatesType coordinates;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String srsName;
    @XmlAttribute
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger srsDimension;
    @XmlAttribute
    protected List<String> axisLabels;
    @XmlAttribute
    protected List<String> uomLabels;

    /**
     * An empty constructor used by JAXB.
     */
    protected EnvelopeEntry(){}

    /**
     * build a new envelope.
     */
    public EnvelopeEntry(String id, DirectPositionType lowerCorner, DirectPositionType upperCorner, String srsName) {
        this.lowerCorner = lowerCorner;
        this.upperCorner = upperCorner;
        this.id          = id;
        this.srsName     = srsName;
    }

    /**
     * build a new envelope.
     */
    public EnvelopeEntry(List<DirectPositionType> pos, String srsName) {
        this.srsName      = srsName;
        this.pos          = pos;
        this.srsDimension = null;
    }

    public String getId() {
        return id;
    }

    /**
     * Gets the value of the lowerCorner property.
     * 
     * @return
     *     possible object is
     *     {@link DirectPositionType }
     *     
     */
    public DirectPositionType getLowerCorner() {
        return lowerCorner;
    }

    /**
     * Sets the value of the lowerCorner property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectPositionType }
     *     
     */
    public void setLowerCorner(DirectPositionType value) {
        this.lowerCorner = value;
    }

    /**
     * Gets the value of the upperCorner property.
     * 
     * @return
     *     possible object is
     *     {@link DirectPositionType }
     *     
     */
    public DirectPositionType getUpperCorner() {
        return upperCorner;
    }

    /**
     * Sets the value of the upperCorner property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectPositionType }
     *     
     */
    public void setUpperCorner(DirectPositionType value) {
        this.upperCorner = value;
    }

    /**
     * Deprecated with GML version 3.1. Use the explicit properties "lowerCorner" and "upperCorner" instead.Gets the value of the pos property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pos property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPos().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DirectPositionType }
     * 
     * 
     */
    public List<DirectPositionType> getPos() {
        if (pos == null) {
            pos = new ArrayList<DirectPositionType>();
        }
        return this.pos;
    }

    /**
     * Deprecated with GML version 3.1.0. Use the explicit properties "lowerCorner" and "upperCorner" instead.
     * 
     * @return
     *     possible object is
     *     {@link CoordinatesType }
     *     
     */
    public CoordinatesType getCoordinates() {
        return coordinates;
    }

    /**
     * Deprecated with GML version 3.1.0. Use the explicit properties "lowerCorner" and "upperCorner" instead.
     * 
     * @param value
     *     allowed object is
     *     {@link CoordinatesType }
     *     
     */
    public void setCoordinates(CoordinatesType value) {
        this.coordinates = value;
    }

    /**
     * Gets the value of the srsName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * Sets the value of the srsName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrsName(String value) {
        this.srsName = value;
    }

    /**
     * Gets the value of the srsDimension property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSrsDimension() {
        return srsDimension;
    }

    /**
     * Sets the value of the srsDimension property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSrsDimension(BigInteger value) {
        this.srsDimension = value;
    }

    /**
     * Gets the value of the axisLabels property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the axisLabels property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAxisLabels().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAxisLabels() {
        if (axisLabels == null) {
            axisLabels = new ArrayList<String>();
        }
        return this.axisLabels;
    }

    /**
     * Gets the value of the uomLabels property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the uomLabels property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUomLabels().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUomLabels() {
        if (uomLabels == null) {
            uomLabels = new ArrayList<String>();
        }
        return this.uomLabels;
    }

    /**
     * Verifie si cette entree est identique a l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof EnvelopeEntry) {
            final EnvelopeEntry that = (EnvelopeEntry) object;

            return Utilities.equals(this.axisLabels,      that.axisLabels)    &&
                   Utilities.equals(this.coordinates,     that.coordinates)   &&
                   Utilities.equals(this.id,              that.id)            &&
                   Utilities.equals(this.lowerCorner,     that.lowerCorner)   &&
                   Utilities.equals(this.pos,             that.pos)           &&
                   Utilities.equals(this.srsDimension,    that.srsDimension)  &&
                   Utilities.equals(this.uomLabels,       that.uomLabels)     &&
                   Utilities.equals(this.upperCorner,     that.upperCorner)   &&
                   Utilities.equals(this.srsName,         that.srsName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 79 * hash + (this.lowerCorner != null ? this.lowerCorner.hashCode() : 0);
        hash = 79 * hash + (this.upperCorner != null ? this.upperCorner.hashCode() : 0);
        hash = 79 * hash + (this.pos != null ? this.pos.hashCode() : 0);
        hash = 79 * hash + (this.coordinates != null ? this.coordinates.hashCode() : 0);
        hash = 79 * hash + (this.srsName != null ? this.srsName.hashCode() : 0);
        hash = 79 * hash + (this.srsDimension != null ? this.srsDimension.hashCode() : 0);
        hash = 79 * hash + (this.axisLabels != null ? this.axisLabels.hashCode() : 0);
        hash = 79 * hash + (this.uomLabels != null ? this.uomLabels.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (id != null) {
            s.append("id:").append(id).append(" ");
        }
        if (srsDimension != null) {
            s.append("srsDImension:").append(srsDimension).append(" ");
        }
        if (srsName != null) {
            s.append("srsName:").append(srsName).append(" ");
        }
        if (lowerCorner != null) {
            s.append('\n').append("lowerCorner:").append(lowerCorner.toString());
        }
        if (upperCorner != null) {
            s.append('\n').append("upperCorner:").append(upperCorner.toString());
        }
        if (pos != null) {
            int i = 0;
            for (DirectPositionType posi: pos) {
                s.append('\n').append("pos").append(i).append(":").append(posi.toString());
                i++;
            }
            s.append('\n');
        }
        if (coordinates != null) {
            s.append("coordinates:").append(coordinates.toString());
        }
        if (axisLabels != null) {
            int i = 0;
            for (String axis: axisLabels) {
                s.append('\n').append("axis").append(i).append(":").append(axis);
                i++;
            }
            s.append('\n');
        }
        if (uomLabels != null) {
            int i = 0;
            for (String uom: uomLabels) {
                s.append('\n').append("uom").append(i).append(":").append(uom);
                i++;
            }
            s.append('\n');
        }
        return s.toString();
    }
}
