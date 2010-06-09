package org.geotoolkit.data.model;

import java.util.List;
import org.geotoolkit.data.model.xal.AddressDetails;
import org.geotoolkit.data.model.xal.AddressIdentifier;
import org.geotoolkit.data.model.xal.AddressLines;
import org.geotoolkit.data.model.xal.AdministrativeArea;
import org.geotoolkit.data.model.xal.AfterBeforeEnum;
import org.geotoolkit.data.model.xal.BuildingName;
import org.geotoolkit.data.model.xal.Country;
import org.geotoolkit.data.model.xal.CountryNameCode;
import org.geotoolkit.data.model.xal.Department;
import org.geotoolkit.data.model.xal.DependentLocality;
import org.geotoolkit.data.model.xal.DependentLocalityNumber;
import org.geotoolkit.data.model.xal.Firm;
import org.geotoolkit.data.model.xal.GenericTypedGrPostal;
import org.geotoolkit.data.model.xal.GrPostal;
import org.geotoolkit.data.model.xal.LargeMailUser;
import org.geotoolkit.data.model.xal.LargeMailUserIdentifier;
import org.geotoolkit.data.model.xal.LargeMailUserName;
import org.geotoolkit.data.model.xal.Locality;
import org.geotoolkit.data.model.xal.MailStop;
import org.geotoolkit.data.model.xal.MailStopNumber;
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
import org.geotoolkit.data.model.xal.SortingCode;
import org.geotoolkit.data.model.xal.SubAdministrativeArea;
import org.geotoolkit.data.model.xal.Thoroughfare;
import org.geotoolkit.data.model.xal.Xal;
import org.geotoolkit.data.model.xal.XalException;

/**
 *
 * @author Samuel Andrés
 */
public interface XalFactory {

    /**
     *
     * @param addressDetails
     * @param version
     * @return
     */
    public Xal createXal(List<AddressDetails> addressDetails, String version);

    /**
     *
     * @param postalServiceElements
     * @param localisation
     * @param addressType
     * @param currentStatus
     * @param validFromDate
     * @param validToDate
     * @param usage
     * @param grPostal
     * @param AddressDetailsKey
     * @return
     * @throws XalException
     */
    public AddressDetails createAddressDetails(PostalServiceElements postalServiceElements, Object localisation,
            String addressType, String currentStatus, String validFromDate, String validToDate,
            String usage, GrPostal grPostal, String AddressDetailsKey) throws XalException;

    /**
     *
     * @param addressLines
     * @return
     */
    public AddressLines createAddressLines(List<GenericTypedGrPostal> addressLines);

    /**
     * 
     * @param type
     * @param grPostal
     * @param Content
     * @return
     */
    public GenericTypedGrPostal createGenericTypedGrPostal(String type, GrPostal grPostal, String Content);

    /**
     * 
     * @param code
     * @return
     */
    public GrPostal createGrPostal(String code);

    /**
     * 
     * @param addressIdentifiers
     * @param endorsementLineCode
     * @param keyLineCode
     * @param barCode
     * @param sortingCode
     * @param addressLatitude
     * @param addressLatitudeDirection
     * @param addressLongitude
     * @param addressLongitudeDirection
     * @param supplementaryPostalServiceData
     * @param type
     * @return
     */
    public PostalServiceElements createPostalServiceElements(List<AddressIdentifier> addressIdentifiers, GenericTypedGrPostal endorsementLineCode,
            GenericTypedGrPostal keyLineCode, GenericTypedGrPostal barCode, SortingCode sortingCode, GenericTypedGrPostal addressLatitude,
            GenericTypedGrPostal addressLatitudeDirection, GenericTypedGrPostal addressLongitude, GenericTypedGrPostal addressLongitudeDirection,
            List<GenericTypedGrPostal> supplementaryPostalServiceData, String type);

    /**
     * 
     * @param type
     * @param grPostal
     * @return
     */
    public SortingCode createSortingCode(String type, GrPostal grPostal);

    /**
     *
     * @param content
     * @param identifierType
     * @param type
     * @param grPostal
     * @return
     */
    public AddressIdentifier createAddressIdentifier(String content, String identifierType, String type, GrPostal grPostal);

    /**
     * 
     * @param addressLines
     * @param countryNameCodes
     * @param countryNames
     * @param localisation
     * @return
     * @throws XalException
     */
    public Country createCountry(List<GenericTypedGrPostal> addressLines,
            List<CountryNameCode> countryNameCodes, List<GenericTypedGrPostal> countryNames, Object localisation) throws XalException;

    /**
     * 
     * @param sheme
     * @param grPostal
     * @param content
     * @return
     */
    public CountryNameCode createCountryNameCode(String sheme, GrPostal grPostal, String content);

    /**
     * 
     * @param addressLines
     * @param administrativeAreaNames
     * @param subAdministrativeArea
     * @param localisation
     * @param type
     * @param usageType
     * @param indicator
     * @return
     * @throws XalException
     */
    public AdministrativeArea createAdministrativeArea(List<GenericTypedGrPostal> addressLines,
            List<GenericTypedGrPostal> administrativeAreaNames, SubAdministrativeArea subAdministrativeArea,
            Object localisation, String type, String usageType, String indicator) throws XalException;

    /**
     * 
     * @param addressLines
     * @param subAdministrativeAreaNames
     * @param localisation
     * @param type
     * @param usageType
     * @param indicator
     * @return
     * @throws XalException
     */
    public SubAdministrativeArea createSubAdministrativeArea(List<GenericTypedGrPostal> addressLines,
            List<GenericTypedGrPostal> subAdministrativeAreaNames,
            Object localisation, String type, String usageType, String indicator) throws XalException;

    /**
     * 
     * @param addressLines
     * @param localityNames
     * @param postal
     * @param thoroughfare
     * @param premise
     * @param dependentLocality
     * @param postalCode
     * @param type
     * @param usageType
     * @param indicator
     * @return
     * @throws XalException
     */
    public Locality createLocality(List<GenericTypedGrPostal> addressLines, List<GenericTypedGrPostal> localityNames,
            Object postal,
            Thoroughfare thoroughfare, Premise premise, DependentLocality dependentLocality, PostalCode postalCode,
            String type, String usageType, String indicator) throws XalException;

    /**
     * 
     * @param grPostal
     * @param content
     * @return
     */
    public PostBoxNumber createPostBoxNumber(GrPostal grPostal, String content);

    /**
     * 
     * @param numberPrefixSeparator
     * @param grPostal
     * @param content
     * @return
     */
    public PostBoxNumberPrefix createPostBoxNumberPrefix(String numberPrefixSeparator, GrPostal grPostal, String content);

    /**
     * 
     * @param numberSuffixSeparator
     * @param grPostal
     * @param content
     * @return
     */
    public PostBoxNumberSuffix createPostBoxNumberSuffix(String numberSuffixSeparator, GrPostal grPostal, String content);

    /**
     * 
     * @param numberExtensionSeparator
     * @param content
     * @return
     */
    public PostBoxNumberExtension createPostBoxNumberExtension(String numberExtensionSeparator, String content);

    /**
     * 
     * @param addressLines
     * @param firmNames
     * @param departments
     * @param mailStop
     * @param postalCode
     * @param type
     * @return
     */
    public Firm createFirm(List<GenericTypedGrPostal> addressLines, List<GenericTypedGrPostal> firmNames,
            List<Department> departments, MailStop mailStop, PostalCode postalCode, String type);

    /**
     * 
     * @param addressLines
     * @param postBoxNumber
     * @param postBoxNumberPrefix
     * @param postBoxNumberSuffix
     * @param postBoxNumberExtension
     * @param firm
     * @param postalCode
     * @param type
     * @param indicator
     * @return
     */
    public PostBox createPostBox(List<GenericTypedGrPostal> addressLines, PostBoxNumber postBoxNumber,
            PostBoxNumberPrefix postBoxNumberPrefix, PostBoxNumberSuffix postBoxNumberSuffix,
            PostBoxNumberExtension postBoxNumberExtension, Firm firm,
            PostalCode postalCode, String type, String indicator);

    /**
     * 
     * @param addressLines
     * @param departmentNames
     * @param mailStop
     * @param postalCode
     * @param type
     * @return
     */
    public Department createDepartment(List<GenericTypedGrPostal> addressLines, List<GenericTypedGrPostal> departmentNames,
            MailStop mailStop, PostalCode postalCode, String type);

    /**
     *
     * @param addressLines
     * @param mailStopNames
     * @param mailStopNumber
     * @param type
     * @return
     */
    public MailStop createMailStop(List<GenericTypedGrPostal> addressLines, List<GenericTypedGrPostal> mailStopNames,
            MailStopNumber mailStopNumber, String type);

    /**
     * 
     * @param nameNumberSeparator
     * @param grPostal
     * @param content
     * @return
     */
    public MailStopNumber createMailStopNumber(String nameNumberSeparator, GrPostal grPostal, String content);

    /**
     * 
     * @param addressLines
     * @param postalCodeNumbers
     * @param postalCodeNumberExtensions
     * @param postTown
     * @param type
     * @return
     */
    public PostalCode createPostalCode(List<GenericTypedGrPostal> addressLines, List<GenericTypedGrPostal> postalCodeNumbers,
            List<PostalCodeNumberExtension> postalCodeNumberExtensions, PostTown postTown, String type);

    /**
     * 
     * @param type
     * @param numberExtensionSeparator
     * @param grPostal
     * @param content
     * @return
     */
    public PostalCodeNumberExtension createPostalCodeNumberExtension(String type, String numberExtensionSeparator,
            GrPostal grPostal, String content);

    /**
     * 
     * @param grPostal
     * @param content
     * @return
     */
    public PostTownSuffix createPostTownSuffix(GrPostal grPostal, String content);

    /**
     * 
     * @param addressLines
     * @param postTownNames
     * @param postTownSuffix
     * @param type
     * @return
     */
    public PostTown createPostTown(List<GenericTypedGrPostal> addressLines,
            List<GenericTypedGrPostal> postTownNames, PostTownSuffix postTownSuffix, String type);

    /**
     *
     * @param type
     * @param indicator
     * @param grPostal
     * @param content
     * @return
     */
    public LargeMailUserIdentifier createLargeMailUserIdentifier(String type,
            String indicator, GrPostal grPostal, String content);

    /**
     *
     * @param type
     * @param code
     * @param content
     * @return
     */
    public LargeMailUserName createLargeMailUserName(String type, String code, String content);

    /**
     * 
     * @param addressLines
     * @param largeMailUserNames
     * @param largeMailUserIdentifier
     * @param buildingNames
     * @param department
     * @param postBox
     * @param thoroughfare
     * @param postalCode
     * @param type
     * @return
     */
    public LargeMailUser createLargeMailUser(List<GenericTypedGrPostal> addressLines,
            List<LargeMailUserName> largeMailUserNames, LargeMailUserIdentifier largeMailUserIdentifier,
            List<BuildingName> buildingNames, Department department, PostBox postBox,
            Thoroughfare thoroughfare, PostalCode postalCode, String type);

    /**
     * 
     * @param type
     * @param typeOccurrence
     * @param grPostal
     * @param content
     * @return
     */
    public BuildingName createBuildingName(String type, AfterBeforeEnum typeOccurrence,
            GrPostal grPostal, String content);

    /**
     *
     * @param grPostal
     * @param content
     * @return
     */
    public PostalRouteNumber createPostalRouteNumber(GrPostal grPostal, String content);

    /**
     * 
     * @param addressLines
     * @param localisation
     * @param postBox
     * @param type
     * @return
     * @throws XalException
     */
    public PostalRoute createPostalRoute(List<GenericTypedGrPostal> addressLines,
            Object localisation, PostBox postBox, String type) throws XalException;

    /**
     * 
     * @param addressLines
     * @param localisation
     * @param postalRoute
     * @param postBox
     * @param postalCode
     * @param type
     * @param indicator
     * @return
     * @throws XalException
     */
    public PostOffice createPostOffice(List<GenericTypedGrPostal> addressLines, Object localisation,
            PostalRoute postalRoute, PostBox postBox, PostalCode postalCode, String type, String indicator) throws XalException;

    /**
     * 
     * @param indicator
     * @param indicatorOccurence
     * @param grPostal
     * @param content
     * @return
     */
    public PostOfficeNumber createPostOfficeNumber(String indicator,
            AfterBeforeEnum indicatorOccurence, GrPostal grPostal, String content);

    /**
     * 
     * @param nameNumberOccurence
     * @param grPostal
     * @param content
     * @return
     */
    public DependentLocalityNumber createDependentLocalityNumber(
            AfterBeforeEnum nameNumberOccurence, GrPostal grPostal, String content);

    /**
     *
     * @param addressLines
     * @param dependentLocalityNames
     * @param dependentLocalityNumber
     * @param localisation
     * @param thoroughfare
     * @param premise
     * @param dependentLocality
     * @param postalCode
     * @param type
     * @param usageType
     * @param connector
     * @param indicator
     * @return
     * @throws XalException
     */
    public DependentLocality createDependentLocality(List<GenericTypedGrPostal> addressLines,
            List<GenericTypedGrPostal> dependentLocalityNames,
            DependentLocalityNumber dependentLocalityNumber,
            Object localisation, Thoroughfare thoroughfare, Premise premise,
            DependentLocality dependentLocality, PostalCode postalCode,
            String type, String usageType, String connector, String indicator) throws XalException;
}
