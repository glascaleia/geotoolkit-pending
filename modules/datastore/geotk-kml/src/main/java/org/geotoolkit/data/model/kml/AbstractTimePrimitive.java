package org.geotoolkit.data.model.kml;

import java.util.List;
import org.geotoolkit.data.model.xsd.SimpleType;

/**
 * <p>This interface maps AbstractTimePrimitiveGroup element.</p>
 *
 * <pre>
 * &lt;element name="AbstractTimePrimitiveGroup" type="kml:AbstractTimePrimitiveType" abstract="true" substitutionGroup="kml:AbstractObjectGroup"/>
 *
 * &lt;complexType name="AbstractTimePrimitiveType" abstract="true">
 *  &lt;complexContent>
 *      &lt;extension base="kml:AbstractObjectType">
 *          &lt;sequence>
 *              &lt;element ref="kml:AbstractTimePrimitiveSimpleExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *              &lt;element ref="kml:AbstractTimePrimitiveObjectExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *          &lt;/sequence>
 *      &lt;/extension>
 *  &lt;/complexContent>
 * &lt;/complexType>
 *
 * &lt;element name="AbstractTimePrimitiveSimpleExtensionGroup" abstract="true" type="anySimpleType"/>
 * &lt;element name="AbstractTimePrimitiveObjectExtensionGroup" abstract="true" substitutionGroup="kml:AbstractObjectGroup"/>
 *</pre>
 *
 * @author Samuel Andrés
 */
public interface AbstractTimePrimitive extends AbstractObject {

    /**
     *
     * @return the list of AbstractTimePrimitive simple extensions.
     */
    public List<SimpleType> getAbstractTimePrimitiveSimpleExtensions();

    /**
     *
     * @return the list of AbtractTimePrimitive object extensions.
     */
    public List<AbstractObject> getAbstractTimePrimitiveObjectExtensions();

}
