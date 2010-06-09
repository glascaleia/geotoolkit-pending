package org.geotoolkit.data.model.xal;

import java.util.List;

/**
 *
 * @author Samuel Andrés
 */
public class LargeMailUserDefault implements LargeMailUser {

    private final List<GenericTypedGrPostal> addressLines;
    private final List<LargeMailUserName> largeMailUserNames;
    private final LargeMailUserIdentifier largeMailUserIdentifier;
    private final List<BuildingName> buildingNames;
    private final Department department;
    private final PostBox postBox;
    private final Thoroughfare thoroughfare;
    private final PostalCode postalCode;
    private final String type;

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
     */
    public LargeMailUserDefault(List<GenericTypedGrPostal> addressLines,
            List<LargeMailUserName> largeMailUserNames, LargeMailUserIdentifier largeMailUserIdentifier,
            List<BuildingName> buildingNames, Department department, PostBox postBox,
            Thoroughfare thoroughfare, PostalCode postalCode, String type){
        this.addressLines = addressLines;
        this.largeMailUserNames = largeMailUserNames;
        this.largeMailUserIdentifier = largeMailUserIdentifier;
        this.buildingNames = buildingNames;
        this.department = department;
        this.postBox = postBox;
        this.thoroughfare = thoroughfare;
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
    public List<LargeMailUserName> getLargeMailUserNames() {return this.largeMailUserNames;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public LargeMailUserIdentifier getLargeMailUserIdentifier() {return this.largeMailUserIdentifier;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<BuildingName> getBuildingNames() {return this.buildingNames;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public Department getDepartment() {return this.department;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public PostBox getPostBox() {return this.postBox;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public Thoroughfare getThoroughfare() {return this.thoroughfare;}

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
