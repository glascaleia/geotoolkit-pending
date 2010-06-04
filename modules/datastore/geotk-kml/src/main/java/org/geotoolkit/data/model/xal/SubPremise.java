package org.geotoolkit.data.model.xal;

import java.util.List;

/**
 * <p>This interface maps SubpremiseType type.</p>
 *
 * <br />&lt;xs:complexType name="SubPremiseType">
 * <br />&lt;xs:sequence>
 * <br />&lt;xs:element ref="AddressLine" minOccurs="0" maxOccurs="unbounded"/>
 * <br />&lt;xs:element name="SubPremiseName" minOccurs="0" maxOccurs="unbounded">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:choice minOccurs="0">
 * <br />&lt;xs:element name="SubPremiseLocation">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:element name="SubPremiseNumber" minOccurs="0" maxOccurs="unbounded">...
 * <br />&lt;/xs:element>
 * <br />&lt;/xs:choice>
 * <br />&lt;xs:element name="SubPremiseNumberPrefix" minOccurs="0" maxOccurs="unbounded">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:element name="SubPremiseNumberSuffix" minOccurs="0" maxOccurs="unbounded">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:element name="BuildingName" type="BuildingNameType" minOccurs="0" maxOccurs="unbounded">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:element name="Firm" type="FirmType" minOccurs="0">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:element name="MailStop" type="MailStopType" minOccurs="0">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:element ref="PostalCode" minOccurs="0"/>
 * <br />&lt;xs:element name="SubPremise" type="SubPremiseType" minOccurs="0">...
 * <br />&lt;/xs:element>
 * <br />&lt;xs:any namespace="##other" minOccurs="0" maxOccurs="unbounded"/>
 * <br />&lt;/xs:sequence>
 * <br />&lt;xs:attribute name="Type"/>
 * <br />&lt;xs:anyAttribute namespace="##other"/>
 * <br />&lt;/xs:complexType>
 *
 * @author Samuel Andrés
 */
public interface SubPremise {

    /**
     * 
     * @return
     */
    public List<GenericTypedGrPostal> getAddressLines();

    /**
     * <p>Name of the SubPremise.</p>
     * @return
     */
    public List<SubPremiseName> getSubPremiseNames();

    /*
     * === CHOICE: ===
     */

    public SubPremiseLocation getSubPremiseLocation();

    /**
     * 
     * @return
     */
    public List<SubPremiseNumber> getSubPremiseNumbers();

    /*
     * === END OF CHOICE ===
     */

    /**
     * <p>Prefix of the sub premise number. eg. A in A-12.</p>
     *
     * @return
     */
    public List<SubPremiseNumberPrefix> getSubPremiseNumbersPrefixes();

    /**
     * <p>Suffix of the sub premise number. eg. A in 12A.</p>
     *
     * @return
     */
    public List<SubPremiseNumberSuffix> getSubPremiseNumberSuffixes();

    /**
     * <p>Name of the building.</p>
     *
     * @return
     */
    public List<BuildingName> getBuildingNames();

    /**
     * <p>Specification of a firm, company, organization, etc.
     * It can be specified as part of an address that contains a street or a postbox.
     * It is therefore different from a large mail user address,
     * which contains no street.</p>
     *
     * @return
     */
    public Firm getFirm();

    /**
     * <p>A MailStop is where the the mail is delivered to within a premise/subpremise/firm or a facility.</p>
     *
     * @return
     */
    public MailStop getMailStop();

    /**
     *
     * @return
     */
    public PostalCode getPostalCode();

    /**
     * <p>Specification of a single sub-premise.
     * Examples of sub-premises are apartments and suites.
     * Each sub-premise should be uniquely identifiable.
     * SubPremiseType: Specification of the name of a sub-premise type.
     * Possible values not limited to: Suite, Appartment, Floor,
     * Unknown Multiple levels within a premise by recursively
     * calling SubPremise Eg. Level 4, Suite 2, Block C</p>
     *
     * @return
     */
    public List<SubPremise> getSubPremises();

    /**
     * 
     * @return
     */
    public String getType();
}
