package org.geotoolkit.data.model.kml;

import java.util.List;

/**
 * <p>This interface maps Delete element.</p>
 *
 * <pre>
 * &lt;element name="Delete" type="kml:DeleteType"/>
 * 
 * &lt;complexType name="DeleteType">
 *  &lt;sequence>
 *      &lt;element ref="kml:AbstractFeatureGroup" minOccurs="0" maxOccurs="unbounded"/>
 *  &lt;/sequence>
 * &lt;/complexType>
 * </pre>
 *
 * @author Samuel Andrés
 */
public interface Delete {

    /**
     *
     * @return AbstractFeature list.
     */
    public List<AbstractFeature> getFeatures();

}
