package org.geotoolkit.data.model;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.data.model.xal.AddressDetails;
import org.geotoolkit.data.model.xal.AddressDetailsDefault;
import org.geotoolkit.data.model.xal.GrPostal;
import org.geotoolkit.data.model.xal.PostalServiceElements;
import org.geotoolkit.data.model.xal.Xal;
import org.geotoolkit.data.model.xal.XalDefault;
import org.geotoolkit.data.model.xal.XalException;

/**
 *
 * @author Samuel Andrés
 */
public class XalFactoryDefault implements XalFactory {

    @Override
    public Xal createXal(List<AddressDetails> addressDetails, String version) {
        return new XalDefault(addressDetails, version);
    }

    @Override
    public AddressDetails createAddressDetails(PostalServiceElements postalServiceElements,
            Object localisation, String addressType, String currentStatus, String validFromDate,
            String validToDate, String usage, GrPostal grPostal, String AddressDetailsKey) throws XalException {
        return new AddressDetailsDefault(postalServiceElements, localisation,
                addressType, currentStatus, validFromDate, validToDate, usage, grPostal, AddressDetailsKey);
    }

}
