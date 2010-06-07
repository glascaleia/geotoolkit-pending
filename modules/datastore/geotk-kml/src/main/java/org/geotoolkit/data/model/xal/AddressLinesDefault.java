package org.geotoolkit.data.model.xal;

import java.util.List;
import static java.util.Collections.*;

/**
 *
 * @author Samuel Andrés
 */
public class AddressLinesDefault implements AddressLines {

    private final List<GenericTypedGrPostal> addressLines;
    
    public AddressLinesDefault(List<GenericTypedGrPostal> addressLines){
        this.addressLines = (addressLines == null) ? EMPTY_LIST : addressLines;
    }
    
    /**
     * 
     * @{@inheritDoc }
     */
    @Override
    public List<GenericTypedGrPostal> getAddressLines() {return this.addressLines;}

}
