package org.geotoolkit.data.model.kml;

import java.util.List;
import org.geotoolkit.data.model.xsd.SimpleType;

/**
 * <p>This interface maps a kml element</p>
 *
 * <pre>
 * &lt;element name="kml" type="kml:KmlType"/>
 *
 * &lt;complexType name="KmlType" final="#all">
 *  &lt;sequence>
 *      &lt;element ref="kml:NetworkLinkControl" minOccurs="0"/>
 *      &lt;element ref="kml:AbstractFeatureGroup" minOccurs="0"/>
 *      &lt;element ref="kml:KmlSimpleExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *      &lt;element ref="kml:KmlObjectExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *  &lt;/sequence>
 *  &lt;attribute name="hint" type="string"/>
 * &lt;/complexType>
 *
 * &lt;lement name="KmlSimpleExtensionGroup" abstract="true" type="anySimpleType"/>
 * &lt;element name="KmlObjectExtensionGroup" abstract="true" substitutionGroup="kml:AbstractObjectGroup"/>
 * </pre>
 *
 * @author Samuel Andrés
 */
public interface Kml {

    /**
     *
     * @return The Kml NetworkLinkControl.
     */
    public NetworkLinkControl getNetworkLinkControl();

    /**
     *
     * @return The Kml AbstractFeature.
     */
    public AbstractFeature getAbstractFeature();

    /**
     *
     * @return The Kml list of simple extensions.
     */
    public List<SimpleType> getKmlSimpleExtensions();

    /**
     *
     * @return The Kml list of object extensions.
     */
    public List<AbstractObject> getKmlObjectExtensions();

}
