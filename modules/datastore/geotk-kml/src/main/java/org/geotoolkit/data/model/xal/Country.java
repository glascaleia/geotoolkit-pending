package org.geotoolkit.data.model.xal;

import java.util.List;

/**
 * <p>This interface maps Country element.</p>
 *
 * <p>Specification of a country.</p>
 *
 * <pre>
 * &lt;xs:element name="Country">
 *  &lt;xs:complexType>
 *      &lt;xs:sequence>
 *          &lt;xs:element ref="AddressLine" minOccurs="0" maxOccurs="unbounded"/>
 *          &lt;xs:element name="CountryNameCode" minOccurs="0" maxOccurs="unbounded">...
 *          &lt;/xs:element>
 *          &lt;xs:element ref="CountryName" minOccurs="0" maxOccurs="unbounded"/>
 *          &lt;xs:choice minOccurs="0">
 *              &lt;xs:element ref="AdministrativeArea"/>
 *              &lt;xs:element ref="Locality"/>
 *              &lt;xs:element ref="Thoroughfare"/>
 *          &lt;/xs:choice>
 *          &lt;xs:any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>
 *      &lt;/xs:sequence>
 *      &lt;xs:anyAttribute namespace="##other"/>
 *  &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 *
 * @author Samuel Andrés
 */
public interface Country {

    /**
     *
     * @return
     */
    public List<GenericTypedGrPostal> getAddressLines();

    /**
     *
     * @return
     */
    public List<CountryNameCode> getCountryNameCodes();

    /**
     * <p>Specification of the name of a country.</p>
     *
     * @return
     */
    public List<GenericTypedGrPostal> getCountryNames();

    /*
     * === CHOICE ===
     */

    /**
     *
     * @return
     */
    public AdministrativeArea getAdministrativeArea();

    /**
     *
     * @return
     */
    public Locality getLocality();

    /**
     * 
     * @return
     */
    public Thoroughfare getThoroughfare();

    /*
     * === END OF CHOICE ===
     */
}
