/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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
package org.geotoolkit.data.kml.model;

import java.util.List;

/**
 * <p>This interface maps StyleMap element.</p>
 *
 * <pre>
 * &lt;element name="StyleMap" type="kml:StyleMapType" substitutionGroup="kml:AbstractStyleSelectorGroup"/>
 *
 * &lt;complexType name="StyleMapType" final="#all">
 *  &lt;complexContent>
 *      &lt;extension base="kml:AbstractStyleSelectorType">
 *          &lt;sequence>
 *              &lt;element ref="kml:Pair" minOccurs="0" maxOccurs="unbounded"/>
 *              &lt;element ref="kml:StyleMapSimpleExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *              &lt;element ref="kml:StyleMapObjectExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *          &lt;/sequence>
 *      &lt;/extension>
 *  &lt;/complexContent>
 * &lt;/complexType>
 *
 * &lt;element name="StyleMapSimpleExtensionGroup" abstract="true" type="anySimpleType"/>
 * &lt;element name="StyleMapObjectExtensionGroup" abstract="true" substitutionGroup="kml:AbstractObjectGroup"/>
 * </pre>
 *
 * @author Samuel Andrés
 */
public interface StyleMap extends AbstractStyleSelector {

    /**
     *
     * @return
     */
    public List<Pair> getPairs();

    /**
     *
     * @param pairs
     */
    public void setPairs(List<Pair> pairs);

}
