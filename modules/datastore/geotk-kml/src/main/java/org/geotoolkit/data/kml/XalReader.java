package org.geotoolkit.data.kml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.geotoolkit.data.model.XalFactory;
import org.geotoolkit.data.model.XalFactoryDefault;
import org.geotoolkit.data.model.xal.AddressDetails;
import org.geotoolkit.data.model.xal.AddressIdentifier;
import org.geotoolkit.data.model.xal.AddressLines;
import org.geotoolkit.data.model.xal.AdministrativeArea;
import org.geotoolkit.data.model.xal.AfterBeforeEnum;
import org.geotoolkit.data.model.xal.AfterBeforeTypeNameEnum;
import org.geotoolkit.data.model.xal.BuildingName;
import org.geotoolkit.data.model.xal.Country;
import org.geotoolkit.data.model.xal.CountryNameCode;
import org.geotoolkit.data.model.xal.Department;
import org.geotoolkit.data.model.xal.DependentLocality;
import org.geotoolkit.data.model.xal.DependentLocalityNumber;
import org.geotoolkit.data.model.xal.DependentThoroughfare;
import org.geotoolkit.data.model.xal.DependentThoroughfares;
import org.geotoolkit.data.model.xal.Firm;
import org.geotoolkit.data.model.xal.GenericTypedGrPostal;
import org.geotoolkit.data.model.xal.GrPostal;
import org.geotoolkit.data.model.xal.LargeMailUser;
import org.geotoolkit.data.model.xal.LargeMailUserIdentifier;
import org.geotoolkit.data.model.xal.LargeMailUserName;
import org.geotoolkit.data.model.xal.Locality;
import org.geotoolkit.data.model.xal.MailStop;
import org.geotoolkit.data.model.xal.MailStopNumber;
import org.geotoolkit.data.model.xal.OddEvenEnum;
import org.geotoolkit.data.model.xal.PostBox;
import org.geotoolkit.data.model.xal.PostBoxNumber;
import org.geotoolkit.data.model.xal.PostBoxNumberExtension;
import org.geotoolkit.data.model.xal.PostBoxNumberPrefix;
import org.geotoolkit.data.model.xal.PostBoxNumberSuffix;
import org.geotoolkit.data.model.xal.PostOffice;
import org.geotoolkit.data.model.xal.PostOfficeNumber;
import org.geotoolkit.data.model.xal.PostTown;
import org.geotoolkit.data.model.xal.PostTownSuffix;
import org.geotoolkit.data.model.xal.PostalCode;
import org.geotoolkit.data.model.xal.PostalCodeNumberExtension;
import org.geotoolkit.data.model.xal.PostalRoute;
import org.geotoolkit.data.model.xal.PostalRouteNumber;
import org.geotoolkit.data.model.xal.PostalServiceElements;
import org.geotoolkit.data.model.xal.Premise;
import org.geotoolkit.data.model.xal.PremiseLocation;
import org.geotoolkit.data.model.xal.PremiseName;
import org.geotoolkit.data.model.xal.PremiseNumber;
import org.geotoolkit.data.model.xal.PremiseNumberPrefix;
import org.geotoolkit.data.model.xal.PremiseNumberRange;
import org.geotoolkit.data.model.xal.PremiseNumberRangeFrom;
import org.geotoolkit.data.model.xal.PremiseNumberRangeTo;
import org.geotoolkit.data.model.xal.PremiseNumberSuffix;
import org.geotoolkit.data.model.xal.SingleRangeEnum;
import org.geotoolkit.data.model.xal.SortingCode;
import org.geotoolkit.data.model.xal.SubAdministrativeArea;
import org.geotoolkit.data.model.xal.SubPremise;
import org.geotoolkit.data.model.xal.SubPremiseLocation;
import org.geotoolkit.data.model.xal.SubPremiseName;
import org.geotoolkit.data.model.xal.SubPremiseNumber;
import org.geotoolkit.data.model.xal.SubPremiseNumberPrefix;
import org.geotoolkit.data.model.xal.SubPremiseNumberSuffix;
import org.geotoolkit.data.model.xal.Thoroughfare;
import org.geotoolkit.data.model.xal.ThoroughfareNumber;
import org.geotoolkit.data.model.xal.ThoroughfareNumberFrom;
import org.geotoolkit.data.model.xal.ThoroughfareNumberPrefix;
import org.geotoolkit.data.model.xal.ThoroughfareNumberRange;
import org.geotoolkit.data.model.xal.ThoroughfareNumberSuffix;
import org.geotoolkit.data.model.xal.ThoroughfareNumberTo;
import org.geotoolkit.data.model.xal.Xal;
import org.geotoolkit.data.model.xal.XalException;
import org.geotoolkit.xml.StaxStreamReader;
import static org.geotoolkit.data.model.XalModelConstants.*;

/**
 *
 * @author Samuel Andrés
 */
public class XalReader extends StaxStreamReader{

    private Xal root;
    private static final XalFactory xalFactory = new XalFactoryDefault();
    
    public XalReader() {
        super();
    }

    public void setReader(XMLStreamReader reader){this.reader = reader;}

    public XMLStreamReader getReader(){return this.reader;}

    /**
     * <p>This method reads the xAL document assigned to the XalReader.</p>
     *
     * @return The XalS object mapping the document.
     */
    public Xal read() {

        try {

            while (reader.hasNext()) {

                switch (reader.next()) {

                    case XMLStreamConstants.START_ELEMENT:
                        final String eName = reader.getLocalName();
                        final String eUri = reader.getNamespaceURI();

                        if (URI_XAL.equals(eUri)) {
                            if (TAG_XAL.equals(eName)) {
                                this.root = this.readXal();
                            }
                        }
                        break;
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(KmlReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XalException ex) {
            System.out.println("XAL EXCEPTION : " + ex.getMessage());
        }
        return this.root;
    }

    /**
     * 
     * @return
     * @throws XMLStreamException
     * @throws XalException
     */
    private Xal readXal() throws XMLStreamException, XalException {
        String version = reader.getAttributeValue(null, ATT_VERSION);
        List<AddressDetails> addressDetails = new ArrayList<AddressDetails>();

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_DETAILS.equals(eName)) {
                            addressDetails.add(this.readAddressDetails());
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_XAL.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }

        }

        return XalReader.xalFactory.createXal(addressDetails, version);
    }

    /**
     *
     * @return
     * @throws XMLStreamException
     * @throws XalException
     */
    public AddressDetails readAddressDetails() throws XMLStreamException, XalException{
        PostalServiceElements postalServiceElements = null;
        Object localisation = null;
        String addressType = reader.getAttributeValue(null, ATT_ADDRESS_TYPE);
        String currentStatus = reader.getAttributeValue(null, ATT_CURRENT_STATUS);
        String validFromDate = reader.getAttributeValue(null, ATT_VALID_FROM_DATE);
        String validToDate = reader.getAttributeValue(null, ATT_VALID_TO_DATE);
        String usage = reader.getAttributeValue(null, ATT_USAGE);
        GrPostal grPostal = this.readGrPostal();
        String addressDetailsKey = reader.getAttributeValue(null, ATT_ADDRESS_DETAILS_KEY);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_POSTAL_SERVICE_ELEMENTS.equals(eName)) {
                            postalServiceElements = this.readPostalServiceElements();
                        } else if (TAG_ADDRESS.equals(eName)){
                            localisation = this.readGenericTypedGrPostal();
                        } else if (TAG_ADDRESS_LINES.equals(eName)){
                            localisation = this.readAddressLines();
                        } else if (TAG_COUNTRY.equals(eName)){
                            localisation = this.readCountry();
                        } else if (TAG_ADMINISTRATIVE_AREA.equals(eName)){
                            localisation = this.readAdministrativeArea();
                        } else if (TAG_LOCALITY.equals(eName)){
                            localisation = this.readLocality();
                        } else if (TAG_THOROUGHFARE.equals(eName)){
                            localisation = this.readThoroughfare();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_ADDRESS_DETAILS.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }

        }

        return XalReader.xalFactory.createAddressDetails(postalServiceElements, localisation,
                addressType, currentStatus, validFromDate, validToDate, usage, grPostal, addressDetailsKey);
    }

    private PostalServiceElements readPostalServiceElements() throws XMLStreamException{
        List<AddressIdentifier> addressIdentifiers = new ArrayList<AddressIdentifier>();
        GenericTypedGrPostal endorsementLineCode = null;
        GenericTypedGrPostal keyLineCode = null;
        GenericTypedGrPostal barCode = null;
        SortingCode sortingCode = null;
        GenericTypedGrPostal addressLatitude = null;
        GenericTypedGrPostal addressLatitudeDirection = null;
        GenericTypedGrPostal addressLongitude = null;
        GenericTypedGrPostal addressLongitudeDirection = null;
        List<GenericTypedGrPostal> supplementaryPostalServiceData = new ArrayList<GenericTypedGrPostal>();
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_IDENTIFIER.equals(eName)) {
                            addressIdentifiers.add(this.readAddressIdentifier());
                        } else if (TAG_ENDORSEMENT_LINE_CODE.equals(eName)){
                            endorsementLineCode = this.readGenericTypedGrPostal();
                        } else if (TAG_KEY_LINE_CODE.equals(eName)){
                            keyLineCode = this.readGenericTypedGrPostal();
                        } else if (TAG_BARCODE.equals(eName)){
                            barCode = this.readGenericTypedGrPostal();
                        } else if (TAG_SORTING_CODE.equals(eName)){
                            sortingCode = this.readSortingCode();
                        } else if (TAG_ADDRESS_LATITUDE.equals(eName)){
                            addressLatitude = this.readGenericTypedGrPostal();
                        } else if (TAG_ADDRESS_LATITUDE_DIRECTION.equals(eName)){
                            addressLatitudeDirection = this.readGenericTypedGrPostal();
                        } else if (TAG_ADDRESS_LONGITUDE.equals(eName)){
                            addressLongitude = this.readGenericTypedGrPostal();
                        } else if (TAG_ADDRESS_LONGITUDE_DIRECTION.equals(eName)){
                            addressLongitudeDirection = this.readGenericTypedGrPostal();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_POSTAL_SERVICE_ELEMENTS.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }

        }

        return XalReader.xalFactory.createPostalServiceElements(addressIdentifiers, endorsementLineCode,
                keyLineCode, barCode, sortingCode, addressLatitude, addressLatitudeDirection,
                addressLongitude, addressLongitudeDirection, supplementaryPostalServiceData, type);
    }

    /**
     *
     * @return
     * @throws XMLStreamException
     */
    private GenericTypedGrPostal readGenericTypedGrPostal() throws XMLStreamException{
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createGenericTypedGrPostal(type, grPostal, content);
    }

    /**
     * 
     * @return
     * @throws XMLStreamException
     */
    private SortingCode readSortingCode() throws XMLStreamException{
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        return XalReader.xalFactory.createSortingCode(type, grPostal);
    }

    /**
     * 
     * @return
     * @throws XMLStreamException
     */
    private AddressIdentifier readAddressIdentifier() throws XMLStreamException{
        String content = this.reader.getElementText();
        String identifierType = reader.getAttributeValue(null, ATT_IDENTIFIER_TYPE);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        return XalReader.xalFactory.createAddressIdentifier(content, identifierType, type, grPostal);
    }

    /**
     *
     * @return
     * @throws XMLStreamException
     */
    private AddressLines readAddressLines() throws XMLStreamException{
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_ADDRESS_LINES.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }

        }

        return XalReader.xalFactory.createAddressLines(addressLines);
    }

    /**
     *
     * @return
     */
    public GrPostal readGrPostal(){
        return XalReader.xalFactory.createGrPostal(reader.getAttributeValue(null, ATT_CODE));
    }

    /**
     *
     * @return
     * @throws XMLStreamException
     * @throws XalException
     */
    private Country readCountry() throws XMLStreamException, XalException{
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<CountryNameCode> countryNameCodes = new ArrayList<CountryNameCode>();
        List<GenericTypedGrPostal> countryNames = new ArrayList<GenericTypedGrPostal>();
        Object localisation = null;

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_COUNTRY_NAME_CODE.equals(eName)){
                            countryNameCodes.add(this.readCountryNameCode());
                        } else if (TAG_COUNTRY_NAME.equals(eName)){
                            countryNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_ADMINISTRATIVE_AREA.equals(eName)){
                            localisation = this.readAdministrativeArea();
                        } else if (TAG_LOCALITY.equals(eName)){
                            localisation = this.readLocality();
                        } else if (TAG_THOROUGHFARE.equals(eName)){
                            localisation = this.readThoroughfare();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_COUNTRY.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }

        }
        return XalReader.xalFactory.createCountry(addressLines, countryNameCodes, countryNames, localisation);
    }

    private CountryNameCode readCountryNameCode() throws XMLStreamException{
        String scheme = reader.getAttributeValue(null, ATT_SCHEME);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createCountryNameCode(scheme, grPostal, content);
    }

    private AdministrativeArea readAdministrativeArea() throws XMLStreamException, XalException{
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> administrativeAreaNames = new ArrayList<GenericTypedGrPostal>();
        SubAdministrativeArea subAdministrativeArea = null;
        Object localisation = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String usageType = reader.getAttributeValue(null, ATT_USAGE_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_ADMINISTRATIVE_AREA_NAME.equals(eName)){
                            administrativeAreaNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_SUB_ADMINISTRATIVE_AREA.equals(eName)){
                            subAdministrativeArea = this.readSubAdministrativeArea();
                        } else if (TAG_LOCALITY.equals(eName)){
                            localisation = this.readLocality();
                        } else if (TAG_POST_OFFICE.equals(eName)){
                            localisation = this.readPostOffice();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            localisation = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_ADMINISTRATIVE_AREA.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }

        }
        return XalReader.xalFactory.createAdministrativeArea(addressLines, administrativeAreaNames,
                subAdministrativeArea, localisation, type, usageType, indicator);
    }

    private SubAdministrativeArea readSubAdministrativeArea() throws XMLStreamException, XalException{
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> subAdministrativeAreaNames = new ArrayList<GenericTypedGrPostal>();
        Object localisation = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String usageType = reader.getAttributeValue(null, ATT_USAGE_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_SUB_ADMINISTRATIVE_AREA_NAME.equals(eName)){
                            subAdministrativeAreaNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_LOCALITY.equals(eName)){
                            localisation = this.readLocality();
                        } else if (TAG_POST_OFFICE.equals(eName)){
                            localisation = this.readPostOffice();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            localisation = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_SUB_ADMINISTRATIVE_AREA.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }

        }
        return XalReader.xalFactory.createSubAdministrativeArea(addressLines, subAdministrativeAreaNames,
                localisation, type, usageType, indicator);
    }

    private Locality readLocality() throws XMLStreamException, XalException{
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> localityNames = new ArrayList<GenericTypedGrPostal>();
        Object postal = null;
        Thoroughfare thoroughfare = null;
        Premise premise = null;
        DependentLocality dependentLocality = null;
        PostalCode postalCode = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String usageType = reader.getAttributeValue(null, ATT_USAGE_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_LOCALITY_NAME.equals(eName)){
                            localityNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POST_BOX.equals(eName)){
                            postal = this.readPostBox();
                        } else if (TAG_LARGE_MAIL_USER.equals(eName)){
                            postal = this.readLargeMailUser();
                        } else if (TAG_POST_OFFICE.equals(eName)){
                            postal = this.readPostOffice();
                        } else if (TAG_POSTAL_ROUTE.equals(eName)){
                            postal = this.readPostalRoute();
                        } else if (TAG_THOROUGHFARE.equals(eName)){
                            thoroughfare = this.readThoroughfare();
                        } else if (TAG_PREMISE.equals(eName)){
                            premise = this.readPremise();
                        } else if (TAG_DEPENDENT_LOCALITY.equals(eName)){
                            dependentLocality = this.readDependentLocality();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_LOCALITY.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createLocality(addressLines, localityNames,
                postal, thoroughfare, premise, dependentLocality,
                postalCode, type, usageType, indicator);
    }

    private PostBox readPostBox() throws XMLStreamException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        PostBoxNumber postBoxNumber = null;
        PostBoxNumberPrefix postBoxNumberPrefix = null;
        PostBoxNumberSuffix postBoxNumberSuffix = null;
        PostBoxNumberExtension postBoxNumberExtension = null;
        Firm firm = null;
        PostalCode postalCode = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POST_BOX_NUMBER.equals(eName)){
                            postBoxNumber = this.readPostBoxNumber();
                        } else if (TAG_POST_BOX_NUMBER_PREFIX.equals(eName)){
                            postBoxNumberPrefix = this.readPostBoxNumberPrefix();
                        } else if (TAG_POST_BOX_NUMBER_SUFFIX.equals(eName)){
                            postBoxNumberSuffix = this.readPostBoxNumberSuffix();
                        } else if (TAG_POST_BOX_NUMBER_EXTENSION.equals(eName)){
                            postBoxNumberExtension = this.readPostBoxNumberExtension();
                        } else if (TAG_FIRM.equals(eName)){
                            firm = this.readFirm();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_POST_BOX.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPostBox(addressLines, postBoxNumber,
                postBoxNumberPrefix, postBoxNumberSuffix, postBoxNumberExtension,
                firm, postalCode, type, indicator);

    }

    private PostBoxNumber readPostBoxNumber() throws XMLStreamException{
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostBoxNumber(grPostal, content);
    }

    private PostBoxNumberPrefix readPostBoxNumberPrefix() throws XMLStreamException {
        GrPostal grPostal = this.readGrPostal();
        String numberPrefixSeparator = reader.getAttributeValue(null, ATT_NUMBER_PREFIX_SEPARATOR);
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostBoxNumberPrefix(numberPrefixSeparator, grPostal, content);
    }

    private PostBoxNumberSuffix readPostBoxNumberSuffix() throws XMLStreamException {
        GrPostal grPostal = this.readGrPostal();
        String numberSuffixSeparator = reader.getAttributeValue(null, ATT_NUMBER_SUFFIX_SEPARATOR);
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostBoxNumberSuffix(numberSuffixSeparator, grPostal, content);
    }

    private PostBoxNumberExtension readPostBoxNumberExtension() throws XMLStreamException {
        String numberExtensionSeparator = reader.getAttributeValue(null, ATT_NUMBER_EXTENSION_SEPARATOR);
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostBoxNumberExtension(numberExtensionSeparator, content);
    }

    private Firm readFirm() throws XMLStreamException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> firmNames = new ArrayList<GenericTypedGrPostal>();
        List<Department> departments = new ArrayList<Department>();
        MailStop mailStop = null;
        PostalCode postalCode = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_FIRM_NAME.equals(eName)){
                            firmNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_DEPARTMENT.equals(eName)){
                            departments.add(this.readDepartment());
                        } else if (TAG_MAIL_STOP.equals(eName)){
                            mailStop = this.readMailStop();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_FIRM.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createFirm(addressLines, firmNames, departments, mailStop, postalCode, type);
    }

    private Department readDepartment() throws XMLStreamException{
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> departmentNames = new ArrayList<GenericTypedGrPostal>();
        MailStop mailStop = null;
        PostalCode postalCode = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_DEPARTMENT_NAME.equals(eName)){
                            departmentNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_MAIL_STOP.equals(eName)){
                            mailStop = this.readMailStop();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_DEPARTMENT.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createDepartment(addressLines, departmentNames, mailStop, postalCode, type);
    }

    private MailStop readMailStop() throws XMLStreamException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> mailStopNames = new ArrayList<GenericTypedGrPostal>();
        MailStopNumber mailStopNumber = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_MAIL_STOP_NAME.equals(eName)){
                            mailStopNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_MAIL_STOP_NUMBER.equals(eName)){
                            mailStopNumber = this.readMailStopNumber();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_MAIL_STOP.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createMailStop(addressLines, mailStopNames, mailStopNumber, type);
    }

    private MailStopNumber readMailStopNumber() throws XMLStreamException {
        String nameNumberSeparator = reader.getAttributeValue(null, ATT_NAME_NUMBER_SEPARATOR);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createMailStopNumber(nameNumberSeparator, grPostal, content);
    }

    private PostalCode readPostalCode() throws XMLStreamException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> postalCodeNumbers = new ArrayList<GenericTypedGrPostal>();
        List<PostalCodeNumberExtension> postalCodeNumberExtensions = new ArrayList<PostalCodeNumberExtension>();
        PostTown postTown = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POSTAL_CODE_NUMBER.equals(eName)){
                            postalCodeNumbers.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POSTAL_CODE_NUMBER_EXTENSION.equals(eName)){
                            postalCodeNumberExtensions.add(this.readPostalCodeNumberExtension());
                        } else if (TAG_POST_TOWN.equals(eName)){
                            postTown = this.readPostTown();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_POSTAL_CODE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPostalCode(addressLines, postalCodeNumbers, postalCodeNumberExtensions, postTown, type);
    }

    private PostalCodeNumberExtension readPostalCodeNumberExtension() throws XMLStreamException{
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String numberExtensionSeparator = reader.getAttributeValue(null, ATT_NUMBER_EXTENSION_SEPARATOR);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostalCodeNumberExtension(type, numberExtensionSeparator, grPostal, content);
    }


    private PostTown readPostTown() throws XMLStreamException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> postTownNames = new ArrayList<GenericTypedGrPostal>();
        PostTownSuffix postTownSuffix = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POST_TOWN_NAME.equals(eName)){
                            postTownNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POST_TOWN_SUFFIX.equals(eName)){
                            postTownSuffix = this.readPostTownSuffix();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_POST_TOWN.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPostTown(addressLines, postTownNames, postTownSuffix, type);

    }

    private PostTownSuffix readPostTownSuffix() throws XMLStreamException {
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostTownSuffix(grPostal, content);
    }

    private LargeMailUser readLargeMailUser() throws XMLStreamException, XalException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<LargeMailUserName> largeMailUserNames = new ArrayList<LargeMailUserName>();
        LargeMailUserIdentifier largeMailUserIdentifier = null;
        List<BuildingName> buildingNames = new ArrayList<BuildingName>();
        Department department = null;
        PostBox postBox = null;
        Thoroughfare thoroughfare = null;
        PostalCode postalCode = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        
        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_LARGE_MAIL_USER_NAME.equals(eName)){
                            largeMailUserNames.add(this.readLargeMailUserName());
                        } else if (TAG_LARGE_MAIL_USER_IDENTIFIER.equals(eName)){
                            largeMailUserIdentifier = this.readLargeMailUserIdentifier();
                        } else if (TAG_BUILDING_NAME.equals(eName)){
                            buildingNames.add(this.readBuildingName());
                        } else if (TAG_DEPARTMENT.equals(eName)){
                            department = this.readDepartment();
                        } else if (TAG_POST_BOX.equals(eName)){
                            postBox = this.readPostBox();
                        } else if (TAG_THOROUGHFARE.equals(eName)){
                            thoroughfare = this.readThoroughfare();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_LARGE_MAIL_USER.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createLargeMailUser(addressLines, largeMailUserNames,
                largeMailUserIdentifier, buildingNames, department, postBox, thoroughfare, postalCode, type);
    }

    private BuildingName readBuildingName() throws XMLStreamException{
        String type = reader.getAttributeValue(null, ATT_TYPE);
        AfterBeforeEnum typeOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_TYPE_OCCURRENCE));
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createBuildingName(type, typeOccurrence, grPostal, content);
    }

    private LargeMailUserName readLargeMailUserName() throws XMLStreamException{
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String code = reader.getAttributeValue(null, ATT_CODE);
        String content = reader.getElementText();
        return XalReader.xalFactory.createLargeMailUserName(type, code, content);
    }

    private LargeMailUserIdentifier readLargeMailUserIdentifier() throws XMLStreamException{
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createLargeMailUserIdentifier(type, indicator, grPostal, content);
    }

    private PostalRoute readPostalRoute() throws XMLStreamException, XalException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> postalRouteNames = new ArrayList<GenericTypedGrPostal>();
        PostalRouteNumber postalRouteNumber = null;
        Object localisation = null;
        PostBox postBox = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POSTAL_ROUTE_NAME.equals(eName)){
                            postalRouteNames.add(this.readGenericTypedGrPostal());
                            localisation = postalRouteNames;
                        } else if (TAG_POSTAL_ROUTE_NUMBER.equals(eName)){
                            postalRouteNumber = this.readPostalRouteNumber();
                            localisation = postalRouteNumber;
                        } else if (TAG_POST_BOX.equals(eName)){
                            postBox = this.readPostBox();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_POSTAL_ROUTE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPostalRoute(addressLines, localisation, postBox, type);
    }

    private PostalRouteNumber readPostalRouteNumber() throws XMLStreamException {
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostalRouteNumber(grPostal, content);
    }

    private PostOffice readPostOffice() throws XMLStreamException, XalException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> postOfficeNames = new ArrayList<GenericTypedGrPostal>();
        PostOfficeNumber postOfficeNumber = null;
        Object localisation = null;
        PostalRoute postalRoute = null;
        PostBox postBox = null;
        PostalCode postalCode = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_POST_OFFICE_NAME.equals(eName)){
                            postOfficeNames.add(this.readGenericTypedGrPostal());
                            localisation = postOfficeNames;
                        } else if (TAG_POST_OFFICE_NUMBER.equals(eName)){
                            postOfficeNumber = this.readPostOfficeNumber();
                            localisation = postOfficeNumber;
                        } else if (TAG_POSTAL_ROUTE.equals(eName)){
                            postalRoute = this.readPostalRoute();
                        } else if (TAG_POST_BOX.equals(eName)){
                            postBox = this.readPostBox();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_POST_OFFICE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPostOffice(addressLines, localisation, postalRoute, postBox, postalCode, type, indicator);
    }

     private PostOfficeNumber readPostOfficeNumber() throws XMLStreamException {
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);
        GrPostal grPostal = this.readGrPostal();
        AfterBeforeEnum indicatorOccurence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_INDICATOR_OCCURRENCE));
        String content = reader.getElementText();
        return XalReader.xalFactory.createPostOfficeNumber(indicator, indicatorOccurence, grPostal, content);
    }

     private DependentLocalityNumber readDependentLocalityNumber() throws XMLStreamException{
         GrPostal grPostal = this.readGrPostal();
         AfterBeforeEnum nameNumberOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_NAME_NUMBER_OCCURRENCE));
         String content = reader.getElementText();
         return XalReader.xalFactory.createDependentLocalityNumber(nameNumberOccurrence, grPostal, content);
     }

    private DependentLocality readDependentLocality() throws XMLStreamException, XalException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<GenericTypedGrPostal> dependentLocalityNames = new ArrayList<GenericTypedGrPostal>();
        DependentLocalityNumber dependentLocalityNumber = null;
        Object localisation = null;
        Thoroughfare thoroughfare = null;
        Premise premise = null;
        DependentLocality dependentLocality = null;
        PostalCode postalCode = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String usageType = reader.getAttributeValue(null, ATT_USAGE_TYPE);
        String connector = reader.getAttributeValue(null, ATT_CONNECTOR);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_DEPENDENT_LOCALITY_NAME.equals(eName)) {
                            dependentLocalityNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_DEPENDENT_LOCALITY_NUMBER.equals(eName)){
                            dependentLocalityNumber = this.readDependentLocalityNumber();
                        } else if (TAG_POST_BOX.equals(eName)){
                            localisation = this.readPostBox();
                        } else if (TAG_LARGE_MAIL_USER.equals(eName)){
                            localisation = this.readLargeMailUser();
                        } else if (TAG_POST_OFFICE.equals(eName)){
                            localisation = this.readPostOffice();
                        } else if (TAG_POSTAL_ROUTE.equals(eName)){
                            localisation = this.readPostalRoute();
                        } else if (TAG_THOROUGHFARE.equals(eName)){
                            thoroughfare = this.readThoroughfare();
                        } else if (TAG_PREMISE.equals(eName)){
                            premise = this.readPremise();
                        } else if (TAG_DEPENDENT_LOCALITY.equals(eName)){
                            dependentLocality = this.readDependentLocality();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_DEPENDENT_LOCALITY.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createDependentLocality(addressLines, dependentLocalityNames,
                dependentLocalityNumber, localisation, thoroughfare, premise,
                dependentLocality, postalCode, type, usageType, connector, indicator);
    }

    private Premise readPremise() throws XMLStreamException, XalException {

        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<PremiseName> premiseNames = new ArrayList<PremiseName>();
        PremiseLocation premiseLocation = null;
        List<PremiseNumber> premiseNumbers = new ArrayList<PremiseNumber>();
        PremiseNumberRange premiseNumberRange = null;
        Object location = null;
        List<PremiseNumberPrefix> premiseNumberPrefixes = new ArrayList<PremiseNumberPrefix>();
        List<PremiseNumberSuffix> premiseNumberSuffixes = new ArrayList<PremiseNumberSuffix>();
        List<BuildingName> buildingNames = new ArrayList<BuildingName>();
        List<SubPremise> subPremises = new ArrayList<SubPremise>();
        Firm firm = null;
        Object sub = null;
        MailStop mailStop = null;
        PostalCode postalCode = null;
        Premise premise = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String premiseDependency = reader.getAttributeValue(null, ATT_PREMISE_DEPENDENCY);
        String premiseDependencyType = reader.getAttributeValue(null, ATT_PREMISE_DEPENDENCY_TYPE);
        String premiseThoroughfareConnector = reader.getAttributeValue(null, ATT_PREMISE_THOROUGHFARE_CONNECTOR);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_PREMISE_NAME.equals(eName)) {
                            premiseNames.add(this.readPremiseName());
                        } else if (TAG_PREMISE_LOCATION.equals(eName)){
                            premiseLocation = this.readPremiseLocation();
                            if (location == null) location = premiseLocation;
                        } else if (TAG_PREMISE_NUMBER.equals(eName)){
                            premiseNumbers.add(this.readPremiseNumber());
                            if (location == null) location = premiseNumbers;
                        } else if (TAG_PREMISE_NUMBER_RANGE.equals(eName)){
                            premiseNumberRange = this.readPremiseNumberRange();
                            if (location == null) location = premiseNumberRange;
                        } else if (TAG_PREMISE_NUMBER_PREFIX.equals(eName)){
                            premiseNumberPrefixes.add(this.readPremiseNumberPrefix());
                        } else if (TAG_PREMISE_NUMBER_SUFFIX.equals(eName)){
                            premiseNumberSuffixes.add(this.readPremiseNumberSuffix());
                        } else if (TAG_BUILDING_NAME.equals(eName)){
                            buildingNames.add(this.readBuildingName());
                        } else if (TAG_SUB_PREMISE.equals(eName)){
                            subPremises.add(this.readSubPremise());
                            if (sub == null) sub = subPremises;
                        } else if (TAG_FIRM.equals(eName)){
                            firm = this.readFirm();
                            if (sub == null) sub = firm;
                        } else if (TAG_MAIL_STOP.equals(eName)){
                            mailStop = this.readMailStop();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        } else if (TAG_PREMISE.equals(eName)){
                            premise = this.readPremise();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_PREMISE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPremise(addressLines, premiseNames, location,
                premiseNumberPrefixes, premiseNumberSuffixes, buildingNames,
                sub, mailStop, postalCode, premise,
                type, premiseDependency, premiseDependencyType, premiseThoroughfareConnector);

    }

    private PremiseName readPremiseName() throws XMLStreamException {
        String type = reader.getAttributeValue(null, ATT_TYPE);
        AfterBeforeEnum typeOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_TYPE_OCCURRENCE));
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPremiseName(type, typeOccurrence, grPostal, content);
    }

    private PremiseLocation readPremiseLocation() throws XMLStreamException {
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPremiseLocation(grPostal, content);
    }

    private PremiseNumber readPremiseNumber() throws XMLStreamException {
        SingleRangeEnum numberType = SingleRangeEnum.transform(reader.getAttributeValue(null, ATT_NUMBER_TYPE));
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);
        AfterBeforeEnum indicatorOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_INDICATOR_OCCURRENCE));
        AfterBeforeEnum numberTypeOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_NUMBER_TYPE_OCCURRENCE));
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPremiseNumber(numberType, type, indicator,
                indicatorOccurrence, numberTypeOccurrence, grPostal, content);
    }

    private PremiseNumberRange readPremiseNumberRange() throws XMLStreamException {
        String rangeType = reader.getAttributeValue(null, ATT_RANGE_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);
        String separator = reader.getAttributeValue(null, ATT_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        AfterBeforeEnum indicatorOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_INDICATOR_OCCURRENCE));
        AfterBeforeTypeNameEnum numberRangeOccurrence = AfterBeforeTypeNameEnum.transform(reader.getAttributeValue(null, ATT_NUMBER_RANGE_OCCURRENCE));
        PremiseNumberRangeFrom premiseNumberRangeFrom = null;
        PremiseNumberRangeTo premiseNumberRangeTo = null;

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_PREMISE_NUMBER_RANGE_FROM.equals(eName)) {
                            premiseNumberRangeFrom = this.readPremiseNumberRangeFrom();
                        } else if (TAG_PREMISE_NUMBER_RANGE_TO.equals(eName)) {
                            premiseNumberRangeTo = this.readPremiseNumberRangeTo();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_PREMISE_NUMBER_RANGE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPremiseNumberRange(premiseNumberRangeFrom,
                premiseNumberRangeTo, rangeType, indicator, separator,
                type, indicatorOccurrence, numberRangeOccurrence);
    }

    private PremiseNumberRangeFrom readPremiseNumberRangeFrom() throws XMLStreamException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<PremiseNumberPrefix> premiseNumberPrefixes = new ArrayList<PremiseNumberPrefix>();
        List<PremiseNumber> premiseNumbers = new ArrayList<PremiseNumber>();
        List<PremiseNumberSuffix> premiseNumberSuffixes = new ArrayList<PremiseNumberSuffix>();

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_PREMISE_NUMBER_PREFIX.equals(eName)){
                            premiseNumberPrefixes.add(this.readPremiseNumberPrefix());
                        } else if (TAG_PREMISE_NUMBER.equals(eName)){
                            premiseNumbers.add(this.readPremiseNumber());
                        } else if (TAG_PREMISE_NUMBER_SUFFIX.equals(eName)){
                            premiseNumberSuffixes.add(this.readPremiseNumberSuffix());
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_PREMISE_NUMBER_RANGE_FROM.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPremiseNumberRangeFrom(addressLines,
                premiseNumberPrefixes, premiseNumbers, premiseNumberSuffixes);
    }

    private PremiseNumberRangeTo readPremiseNumberRangeTo() throws XMLStreamException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<PremiseNumberPrefix> premiseNumberPrefixes = new ArrayList<PremiseNumberPrefix>();
        List<PremiseNumber> premiseNumbers = new ArrayList<PremiseNumber>();
        List<PremiseNumberSuffix> premiseNumberSuffixes = new ArrayList<PremiseNumberSuffix>();

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_PREMISE_NUMBER_PREFIX.equals(eName)){
                            premiseNumberPrefixes.add(this.readPremiseNumberPrefix());
                        } else if (TAG_PREMISE_NUMBER.equals(eName)){
                            premiseNumbers.add(this.readPremiseNumber());
                        } else if (TAG_PREMISE_NUMBER_SUFFIX.equals(eName)){
                            premiseNumberSuffixes.add(this.readPremiseNumberSuffix());
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_PREMISE_NUMBER_RANGE_TO.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createPremiseNumberRangeTo(addressLines,
                premiseNumberPrefixes, premiseNumbers, premiseNumberSuffixes);
    }

    private PremiseNumberPrefix readPremiseNumberPrefix() throws XMLStreamException {
        String numberPrefixSeparator = reader.getAttributeValue(null, ATT_NUMBER_PREFIX_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPremiseNumberPrefix(numberPrefixSeparator, type, grPostal, content);
    }

    private PremiseNumberSuffix readPremiseNumberSuffix() throws XMLStreamException {
        String numberSuffixSeparator = reader.getAttributeValue(null, ATT_NUMBER_SUFFIX_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createPremiseNumberSuffix(numberSuffixSeparator, type, grPostal, content);
    }

    private SubPremise readSubPremise() throws XMLStreamException, XalException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<SubPremiseName> subPremiseNames = new ArrayList<SubPremiseName>();
        SubPremiseLocation subPremiseLocation = null;
        List<SubPremiseNumber> subPremiseNumbers = new ArrayList<SubPremiseNumber>();
        Object location = null;
        List<SubPremiseNumberPrefix> subPremiseNumberPrefixes = new ArrayList<SubPremiseNumberPrefix>();
        List<SubPremiseNumberSuffix> subPremiseNumberSuffixes = new ArrayList<SubPremiseNumberSuffix>();
        List<BuildingName> buildingNames = new ArrayList<BuildingName>();
        Firm firm = null;
        MailStop mailStop = null;
        PostalCode postalCode = null;
        SubPremise subPremise = null;
        String type = reader.getAttributeValue(null, ATT_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_SUB_PREMISE_NAME.equals(eName)) {
                            subPremiseNames.add(this.readSubPremiseName());
                        } else if (TAG_SUB_PREMISE_LOCATION.equals(eName)){
                            subPremiseLocation = this.readSubPremiseLocation();
                            if (location == null) location = subPremiseLocation;
                        } else if (TAG_SUB_PREMISE_NUMBER.equals(eName)){
                            subPremiseNumbers.add(this.readSubPremiseNumber());
                            if (location == null) location = subPremiseNumbers;
                        } else if (TAG_SUB_PREMISE_NUMBER_PREFIX.equals(eName)){
                            subPremiseNumberPrefixes.add(this.readSubPremiseNumberPrefix());
                        } else if (TAG_SUB_PREMISE_NUMBER_SUFFIX.equals(eName)){
                            subPremiseNumberSuffixes.add(this.readSubPremiseNumberSuffix());
                        } else if (TAG_BUILDING_NAME.equals(eName)){
                            buildingNames.add(this.readBuildingName());
                        } else if (TAG_FIRM.equals(eName)){
                            firm = this.readFirm();
                        } else if (TAG_MAIL_STOP.equals(eName)){
                            mailStop = this.readMailStop();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            postalCode = this.readPostalCode();
                        } else if (TAG_SUB_PREMISE.equals(eName)){
                            subPremise = this.readSubPremise();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_SUB_PREMISE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createSubPremise(addressLines, subPremiseNames,
                location, subPremiseNumberPrefixes, subPremiseNumberSuffixes,
                buildingNames, firm, mailStop, postalCode, subPremise, type);
    }

    private SubPremiseNumberPrefix readSubPremiseNumberPrefix() throws XMLStreamException {
        String numberPrefixSeparator = reader.getAttributeValue(null, ATT_NUMBER_PREFIX_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createSubPremiseNumberPrefix(numberPrefixSeparator, type, grPostal, content);
    }

    private SubPremiseNumberSuffix readSubPremiseNumberSuffix() throws XMLStreamException {
        String numberSuffixSeparator = reader.getAttributeValue(null, ATT_NUMBER_SUFFIX_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createSubPremiseNumberSuffix(numberSuffixSeparator, type, grPostal, content);
    }

    private SubPremiseName readSubPremiseName() throws XMLStreamException {
        String type = reader.getAttributeValue(null, ATT_TYPE);
        AfterBeforeEnum typeOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_TYPE_OCCURRENCE));
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createSubPremiseName(type, typeOccurrence, grPostal, content);
    }

    private SubPremiseLocation readSubPremiseLocation() throws XMLStreamException {
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createSubPremiseLocation(grPostal, content);
    }

    private SubPremiseNumber readSubPremiseNumber() throws XMLStreamException {
        String premiseNumberSeparator = reader.getAttributeValue(null, ATT_PREMISE_NUMBER_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);
        AfterBeforeEnum indicatorOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_INDICATOR_OCCURRENCE));
        AfterBeforeEnum numberTypeOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_NUMBER_TYPE_OCCURRENCE));
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createSubPremiseNumber(indicator, indicatorOccurrence,
                numberTypeOccurrence, premiseNumberSeparator, type, grPostal, content);
    }


    private Thoroughfare readThoroughfare() throws XMLStreamException, XalException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        List<Object> thoroughfareNumbers = new ArrayList<Object>();
        List<ThoroughfareNumberPrefix> thoroughfareNumberPrefixes = new ArrayList<ThoroughfareNumberPrefix>();
        List<ThoroughfareNumberSuffix> thoroughfareNumberSuffixes = new ArrayList<ThoroughfareNumberSuffix>();
        GenericTypedGrPostal thoroughfarePreDirection = null;
        GenericTypedGrPostal thoroughfareLeadingType = null;
        List<GenericTypedGrPostal> thoroughfareNames = new ArrayList<GenericTypedGrPostal>();
        GenericTypedGrPostal thoroughfareTrailingType = null;
        GenericTypedGrPostal thoroughfarePostDirection = null;
        DependentThoroughfare dependentThoroughfare = null;
        Object location = null;
        String type = null;
        DependentThoroughfares dependentThoroughfares =  DependentThoroughfares.transform(reader.getAttributeValue(null, ATT_DEPENDENT_THOROUGHFARES));
        String dependentThoroughfaresIndicator =  reader.getAttributeValue(null, ATT_DEPENDENT_THOROUGHFARES_INDICATOR);
        String dependentThoroughfaresConnector =  reader.getAttributeValue(null, ATT_DEPENDENT_THOROUGHFARES_CONNECTOR);
        String dependentThoroughfaresType = reader.getAttributeValue(null, ATT_DEPENDENT_THOROUGHFARES_TYPE);

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_THOROUGHFARE_NUMBER.equals(eName)) {
                            thoroughfareNumbers.add(this.readThoroughfareNumber());
                        } else if (TAG_THOROUGHFARE_NUMBER_RANGE.equals(eName)){
                            thoroughfareNumbers.add(this.readThoroughfareNumberRange());
                        } else if (TAG_THOROUGHFARE_NUMBER_PREFIX.equals(eName)){
                            thoroughfareNumberPrefixes.add(this.readThoroughfareNumberPrefix());
                        } else if (TAG_THOROUGHFARE_NUMBER_SUFFIX.equals(eName)){
                            thoroughfareNumberSuffixes.add(this.readThoroughfareNumberSuffix());
                        } else if (TAG_THOROUGHFARE_PRE_DIRECTION.equals(eName)){
                            thoroughfarePreDirection = this.readGenericTypedGrPostal();
                        } else if (TAG_THOROUGHFARE_LEADING_TYPE.equals(eName)){
                            thoroughfareLeadingType = this.readGenericTypedGrPostal();
                        } else if (TAG_THOROUGHFARE_NAME.equals(eName)){
                            thoroughfareNames.add(this.readGenericTypedGrPostal());
                        } else if (TAG_THOROUGHFARE_TRAILING_TYPE.equals(eName)){
                            thoroughfareTrailingType = this.readGenericTypedGrPostal();
                        } else if (TAG_THOROUGHFARE_POST_DIRECTION.equals(eName)){
                            thoroughfarePostDirection = this.readGenericTypedGrPostal();
                        } else if (TAG_DEPENDENT_THOROUGHFARE.equals(eName)){
                            dependentThoroughfare = this.readDependentThoroughfare();
                        } else if (TAG_DEPENDENT_LOCALITY.equals(eName)){
                            location = this.readDependentLocality();
                        } else if (TAG_PREMISE.equals(eName)){
                            location = this.readPremise();
                        } else if (TAG_FIRM.equals(eName)){
                            location = this.readFirm();
                        } else if (TAG_POSTAL_CODE.equals(eName)){
                            location = this.readPostalCode();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_THOROUGHFARE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }
        return XalReader.xalFactory.createThoroughfare(addressLines, thoroughfareNumbers,
                thoroughfareNumberPrefixes, thoroughfareNumberSuffixes, thoroughfarePreDirection,
                thoroughfareLeadingType, thoroughfareNames, thoroughfareTrailingType,
                thoroughfarePostDirection, dependentThoroughfare, location, type,
                dependentThoroughfares, dependentThoroughfaresIndicator,
                dependentThoroughfaresConnector, dependentThoroughfaresType);
    }

    private ThoroughfareNumber readThoroughfareNumber() throws XMLStreamException {
        SingleRangeEnum numberType = SingleRangeEnum.transform(reader.getAttributeValue(null, ATT_NUMBER_TYPE));
        String type = reader.getAttributeValue(null, ATT_TYPE);
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);
        AfterBeforeEnum indicatorOccurence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_INDICATOR_OCCURRENCE));
        AfterBeforeTypeNameEnum numberOccurrence = AfterBeforeTypeNameEnum.transform(reader.getAttributeValue(null, ATT_NUMBER_OCCURRENCE));
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createThoroughfareNumber(numberType, type,
                indicator, indicatorOccurence, numberOccurrence, grPostal, content);
    }

    private ThoroughfareNumberRange readThoroughfareNumberRange() throws XMLStreamException, XalException {
        List<GenericTypedGrPostal> addressLines = new ArrayList<GenericTypedGrPostal>();
        ThoroughfareNumberFrom thoroughfareNumberFrom = null;
        ThoroughfareNumberTo thoroughfareNumberTo = null;
        OddEvenEnum rangeType = OddEvenEnum.transform(reader.getAttributeValue(null, ATT_RANGE_TYPE));
        String indicator = reader.getAttributeValue(null, ATT_INDICATOR);
        String separator = reader.getAttributeValue(null, ATT_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        AfterBeforeEnum indicatorOccurrence = AfterBeforeEnum.transform(reader.getAttributeValue(null, ATT_INDICATOR_OCCURRENCE));
        AfterBeforeTypeNameEnum numberRangeOccurrence = AfterBeforeTypeNameEnum.transform(reader.getAttributeValue(null, ATT_NUMBER_RANGE_OCCURRENCE));

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            addressLines.add(this.readGenericTypedGrPostal());
                        } else if (TAG_THOROUGHFARE_NUMBER_FROM.equals(eName)) {
                            thoroughfareNumberFrom = this.readThoroughfareNumberFrom();
                        } else if (TAG_THOROUGHFARE_NUMBER_TO.equals(eName)){
                            thoroughfareNumberTo = this.readThoroughfareNumberTo();
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_THOROUGHFARE_NUMBER_RANGE.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }

        return XalReader.xalFactory.createThoroughfareNumberRange(addressLines,
                thoroughfareNumberFrom, thoroughfareNumberTo, rangeType, indicator,
                separator, type, indicatorOccurrence, numberRangeOccurrence);
    }

    private ThoroughfareNumberFrom readThoroughfareNumberFrom() throws XMLStreamException, XalException {
        List<Object> content = new ArrayList<Object>();
        GrPostal grPostal = this.readGrPostal();

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            content.add(this.readGenericTypedGrPostal());
                        } else if (TAG_THOROUGHFARE_NUMBER_PREFIX.equals(eName)) {
                            content.add(this.readThoroughfareNumberPrefix());
                        } else if (TAG_THOROUGHFARE_NUMBER.equals(eName)) {
                            content.add(this.readThoroughfareNumber());
                        } else if (TAG_THOROUGHFARE_NUMBER_SUFFIX.equals(eName)){
                            content.add(this.readThoroughfareNumberSuffix());
                        }
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    String textNode = reader.getText();
                    textNode = textNode.replaceAll("^\\s*","").replaceAll("\\s*$","");
                    if (!textNode.equals("")) content.add(textNode);
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_THOROUGHFARE_NUMBER_FROM.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }

        return XalReader.xalFactory.createThoroughfareNumberFrom(content, grPostal);
    }

    private ThoroughfareNumberTo readThoroughfareNumberTo() throws XMLStreamException, XalException {
        List<Object> content = new ArrayList<Object>();
        GrPostal grPostal = this.readGrPostal();

        boucle:
        while (reader.hasNext()) {

            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    final String eName = reader.getLocalName();
                    final String eUri = reader.getNamespaceURI();

                    if (URI_XAL.equals(eUri)) {
                        if (TAG_ADDRESS_LINE.equals(eName)) {
                            content.add(this.readGenericTypedGrPostal());
                        } else if (TAG_THOROUGHFARE_NUMBER_PREFIX.equals(eName)) {
                            content.add(this.readThoroughfareNumberPrefix());
                        } else if (TAG_THOROUGHFARE_NUMBER.equals(eName)) {
                            content.add(this.readThoroughfareNumber());
                        } else if (TAG_THOROUGHFARE_NUMBER_SUFFIX.equals(eName)){
                            content.add(this.readThoroughfareNumberSuffix());
                        }
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    String textNode = reader.getText();
                    textNode = textNode.replaceAll("^\\s*","").replaceAll("\\s*$","");
                    if (!textNode.equals("")) content.add(textNode);
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (TAG_THOROUGHFARE_NUMBER_TO.equals(reader.getLocalName()) && URI_XAL.contains(reader.getNamespaceURI())) {
                        break boucle;
                    }
                    break;
            }
        }

        return XalReader.xalFactory.createThoroughfareNumberTo(content, grPostal);
    }

    private ThoroughfareNumberPrefix readThoroughfareNumberPrefix() throws XMLStreamException {
        String numberPrefixSeparator = reader.getAttributeValue(null, ATT_NUMBER_PREFIX_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createThoroughfareNumberPrefix(numberPrefixSeparator, type, grPostal, content);
    }

    private ThoroughfareNumberSuffix readThoroughfareNumberSuffix() throws XMLStreamException {
        String numberSuffixSeparator = reader.getAttributeValue(null, ATT_NUMBER_SUFFIX_SEPARATOR);
        String type = reader.getAttributeValue(null, ATT_TYPE);
        GrPostal grPostal = this.readGrPostal();
        String content = reader.getElementText();
        return XalReader.xalFactory.createThoroughfareNumberSuffix(numberSuffixSeparator, type, grPostal, content);
    }

    private DependentThoroughfare readDependentThoroughfare() {
return null;
    }



}
