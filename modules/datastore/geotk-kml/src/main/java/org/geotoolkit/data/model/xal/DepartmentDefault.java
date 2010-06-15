package org.geotoolkit.data.model.xal;

import java.util.List;
import static java.util.Collections.*;

/**
 *
 * @author Samuel Andrés
 */
public class DepartmentDefault implements Department {

    private final List<GenericTypedGrPostal> addressLines;
    private final List<GenericTypedGrPostal> departmentNames;
    private final MailStop mailStop;
    private final PostalCode postalCode;
    private final String type;

    /**
     *
     * @param addressLines
     * @param departmentNames
     * @param mailStop
     * @param postalCode
     * @param type
     */
    public DepartmentDefault(List<GenericTypedGrPostal> addressLines, List<GenericTypedGrPostal> departmentNames,
            MailStop mailStop, PostalCode postalCode, String type){
        this.addressLines = (addressLines == null) ? EMPTY_LIST : addressLines;
        this.departmentNames = (departmentNames == null) ? EMPTY_LIST : departmentNames;
        this.mailStop = mailStop;
        this.postalCode = postalCode;
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
    public List<GenericTypedGrPostal> getDepartmentNames() {return this.departmentNames;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public MailStop getMailStop() {return this.mailStop;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public PostalCode getPostalCode() {return this.postalCode;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getType() {return this.type;}

}
