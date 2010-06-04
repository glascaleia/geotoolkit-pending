package org.geotoolkit.data.model.kml;

import java.util.List;
import org.geotoolkit.data.model.xsd.SimpleType;

/**
 * <p>This interface maps Location element.</p>
 *
 * <pre>
 * &lt;element name="Location" type="kml:LocationType" substitutionGroup="kml:AbstractObjectGroup"/>
 *
 * &lt;complexType name="LocationType" final="#all">
 *  &lt;complexContent>
 *      &lt;extension base="kml:AbstractObjectType">
 *          &lt;sequence>
 *              &lt;element ref="kml:longitude" minOccurs="0"/>
 *              &lt;element ref="kml:latitude" minOccurs="0"/>
 *              &lt;element ref="kml:altitude" minOccurs="0"/>
 *              &lt;element ref="kml:LocationSimpleExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *              &lt;element ref="kml:LocationObjectExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *          &lt;/sequence>
 *      &lt;/extension>
 *  &lt;/complexContent>
 * &lt;/complexType>
 *
 * &lt;element name="LocationSimpleExtensionGroup" abstract="true" type="anySimpleType"/>
 * &lt;element name="LocationObjectExtensionGroup" abstract="true" substitutionGroup="kml:AbstractObjectGroup"/>
 * </pre>
 *
 * @author Samuel Andrés
 */
public interface Location extends AbstractObject {

    /**
     *
     * @return
     */
    public Angle180 getLongitude();

    /**
     *
     * @return
     */
    public Angle90 getLatitude();

    /**
     *
     * @return
     */
    public double getAltitude();

    /**
     *
     * @return the list of Location simple extensions
     */
    public List<SimpleType> getLocationSimpleExtensions();

    /**
     *
     * @return the list of Location object extensions
     */
    public List<AbstractObject> getLocationObjectExtensions();

}