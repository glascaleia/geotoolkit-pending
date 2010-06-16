package org.geotoolkit.data.model.xal;

import java.util.List;
import static java.util.Collections.*;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultPostalCode implements PostalCode {

    private final List<GenericTypedGrPostal> addressLines;
    private final List<GenericTypedGrPostal> postalCodeNumbers;
    private final List<PostalCodeNumberExtension> postalCodeNumberExtensions;
    private final PostTown postTown;
    private final String type;

    /**
     *
     * @param addressLines
     * @param postalCodeNumbers
     * @param postalCodeNumberExtensions
     * @param postTown
     * @param type
     */
    public DefaultPostalCode(List<GenericTypedGrPostal> addressLines, List<GenericTypedGrPostal> postalCodeNumbers,
            List<PostalCodeNumberExtension> postalCodeNumberExtensions, PostTown postTown, String type){
        this.addressLines = (addressLines == null) ? EMPTY_LIST : addressLines;
        this.postalCodeNumbers = (postalCodeNumbers == null) ? EMPTY_LIST : postalCodeNumbers;
        this.postalCodeNumberExtensions = (postalCodeNumberExtensions == null) ? EMPTY_LIST : postalCodeNumberExtensions;
        this.postTown = postTown;
        this.type = type;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<GenericTypedGrPostal> getAddressLines() {return this.addressLines;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<GenericTypedGrPostal> getPostalCodeNumbers() {return this.postalCodeNumbers;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<PostalCodeNumberExtension> getPostalCodeNumberExtensions() {return this.postalCodeNumberExtensions;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public PostTown getPostTown() {return this.postTown;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getType() {return this.type;}

}
