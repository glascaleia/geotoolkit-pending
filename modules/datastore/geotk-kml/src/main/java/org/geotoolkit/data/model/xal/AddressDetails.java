package org.geotoolkit.data.model.xal;

/**
 * <p>This interface maps AddressDetails type.</p>
 *
 * <p>This container defines the details of the address.
 * Can define multiple addresses including tracking address history.</p>
 *
 * <pre>
 * &lt;xs:element name="AddressDetails" type="AddressDetails">...
 * &lt;/xs:element>
 *
 * &lt;xs:complexType name="AddressDetails">
 *  &lt;xs:sequence>
 *      &lt;xs:element name="PostalServiceElements" minOccurs="0">...
 *      &lt;/xs:element>
 *      &lt;xs:choice minOccurs="0">
 *          &lt;xs:element name="Address">...
 *          &lt;/xs:element>
 *          &lt;xs:element name="AddressLines" type="AddressLinesType">...
 *          &lt;/xs:element>
 *          &lt;xs:element name="Country">...
 *          &lt;/xs:element>
 *          &lt;xs:element ref="AdministrativeArea"/>
 *          &lt;xs:element ref="Locality"/>
 *          &lt;xs:element ref="Thoroughfare"/>
 *      &lt;/xs:choice>
 *      &lt;xs:any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>
 *  &lt;/xs:sequence>
 *  &lt;xs:attribute name="AddressType">...
 *  &lt;/xs:attribute>
 *  &lt;xs:attribute name="CurrentStatus">...
 *  &lt;/xs:attribute>
 *  &lt;xs:attribute name="ValidFromDate">...
 *  &lt;/xs:attribute>
 *  &lt;xs:attribute name="ValidToDate">...
 *  &lt;/xs:attribute>
 *  &lt;xs:attribute name="Usage">...
 *  &lt;/xs:attribute>
 *  &lt;xs:attributeGroup ref="grPostal"/>
 *  &lt;xs:attribute name="AddressDetailsKey">...
 *  &lt;/xs:attribute>
 *  &lt;xs:anyAttribute namespace="##other"/>
 * &lt;/xs:complexType>
 * </pre>
 *
 * @author Samuel Andrés
 */
public interface AddressDetails {

    /**
     *
     * @return
     */
    public PostalServiceElements getPostalServiceElements();

    /*
     * === CHOICE: ===
     * Use the most suitable option. Country contains the most detailed
     information while Locality is missing Country and AdminArea.
     */

    /**
     *
     * @return
     */
    public GenericTypedGrPostal getAddress();

    /**
     *
     * @return
     */
    public AddressLines getAddressLines();

    /**
     *
     * @return
     */
    public Country getCountry();

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
     * === END OF THE CHOICE ===
     *
     * === ATTRIBUTES: ===
     */

    /**
     * <p>Type of address. Example: Postal, residential,business, primary, secondary, etc.</p>
     * 
     * @return
     */
    public String getAddressType();

    /**
     * <p>Moved, Living, Investment, Deceased, etc...</p>
     * @return
     */
    public String getCurrentStatus();

    /**
     * <p>Start Date of the validity of address</p>
     *
     * @return
     */
    public String getValidFromDate();

    /**
     * <p>End date of the validity of address</p>
     *
     * @return
     */
    public String getValidToDate();

    /**
     * <p>Communication, Contact, etc.</p>
     * 
     * @return
     */
    public String getUsage();

    /**
     *
     * @return
     */
    public GrPostal getGrPostal();

    /**
     * <p>Key identifier for the element for not reinforced references
     * from other elements. Not required to be unique for the document
     * to be valid, but application may get confused if not unique.
     * Extend this schema adding unique contraint if needed.</p>
     *
     * @return
     */
    public String getAddressDetailsKey();
    
}
