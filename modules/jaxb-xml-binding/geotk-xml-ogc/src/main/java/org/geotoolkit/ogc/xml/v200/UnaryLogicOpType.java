/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2011, Geomatys
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


package org.geotoolkit.ogc.xml.v200;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UnaryLogicOpType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UnaryLogicOpType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/fes/2.0}LogicOpsType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;group ref="{http://www.opengis.net/fes/2.0}FilterPredicates"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnaryLogicOpType", propOrder = {
    "comparisonOps",
    "spatialOps",
    "temporalOps",
    "logicOps",
    "extensionOps",
    "function",
    "id"
})
public class UnaryLogicOpType
    extends LogicOpsType
{

    @XmlElementRef(name = "comparisonOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    private JAXBElement<? extends ComparisonOpsType> comparisonOps;
    @XmlElementRef(name = "spatialOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    private JAXBElement<? extends SpatialOpsType> spatialOps;
    @XmlElementRef(name = "temporalOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    private JAXBElement<? extends TemporalOpsType> temporalOps;
    @XmlElementRef(name = "logicOps", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    private JAXBElement<? extends LogicOpsType> logicOps;
    private ExtensionOpsType extensionOps;
    @XmlElement(name = "Function")
    private FunctionType function;
    @XmlElementRef(name = "_Id", namespace = "http://www.opengis.net/fes/2.0", type = JAXBElement.class)
    private List<JAXBElement<? extends AbstractIdType>> id;

    /**
     * Gets the value of the comparisonOps property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link PropertyIsBetweenType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ComparisonOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsLikeType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNilType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNullType }{@code >}
     *     
     */
    public JAXBElement<? extends ComparisonOpsType> getComparisonOps() {
        return comparisonOps;
    }

    /**
     * Sets the value of the comparisonOps property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link PropertyIsBetweenType }{@code >}
     *     {@link JAXBElement }{@code <}{@link ComparisonOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsLikeType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNilType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryComparisonOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PropertyIsNullType }{@code >}
     *     
     */
    public void setComparisonOps(JAXBElement<? extends ComparisonOpsType> value) {
        this.comparisonOps = ((JAXBElement<? extends ComparisonOpsType> ) value);
    }

    /**
     * Gets the value of the spatialOps property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BBOXType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SpatialOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     
     */
    public JAXBElement<? extends SpatialOpsType> getSpatialOps() {
        return spatialOps;
    }

    /**
     * Sets the value of the spatialOps property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BBOXType }{@code >}
     *     {@link JAXBElement }{@code <}{@link SpatialOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinarySpatialOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link DistanceBufferType }{@code >}
     *     
     */
    public void setSpatialOps(JAXBElement<? extends SpatialOpsType> value) {
        this.spatialOps = ((JAXBElement<? extends SpatialOpsType> ) value);
    }

    /**
     * Gets the value of the temporalOps property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TemporalOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     
     */
    public JAXBElement<? extends TemporalOpsType> getTemporalOps() {
        return temporalOps;
    }

    /**
     * Sets the value of the temporalOps property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TemporalOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryTemporalOpType }{@code >}
     *     
     */
    public void setTemporalOps(JAXBElement<? extends TemporalOpsType> value) {
        this.temporalOps = ((JAXBElement<? extends TemporalOpsType> ) value);
    }

    /**
     * Gets the value of the logicOps property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LogicOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     
     */
    public JAXBElement<? extends LogicOpsType> getLogicOps() {
        return logicOps;
    }

    /**
     * Sets the value of the logicOps property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LogicOpsType }{@code >}
     *     {@link JAXBElement }{@code <}{@link UnaryLogicOpType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BinaryLogicOpType }{@code >}
     *     
     */
    public void setLogicOps(JAXBElement<? extends LogicOpsType> value) {
        this.logicOps = ((JAXBElement<? extends LogicOpsType> ) value);
    }

    /**
     * Gets the value of the extensionOps property.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionOpsType }
     *     
     */
    public ExtensionOpsType getExtensionOps() {
        return extensionOps;
    }

    /**
     * Sets the value of the extensionOps property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionOpsType }
     *     
     */
    public void setExtensionOps(ExtensionOpsType value) {
        this.extensionOps = value;
    }

    /**
     * Gets the value of the function property.
     * 
     * @return
     *     possible object is
     *     {@link FunctionType }
     *     
     */
    public FunctionType getFunction() {
        return function;
    }

    /**
     * Sets the value of the function property.
     * 
     * @param value
     *     allowed object is
     *     {@link FunctionType }
     *     
     */
    public void setFunction(FunctionType value) {
        this.function = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the id property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link ResourceIdType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractIdType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractIdType>> getId() {
        if (id == null) {
            id = new ArrayList<JAXBElement<? extends AbstractIdType>>();
        }
        return this.id;
    }

}
