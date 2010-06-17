package org.geotoolkit.data.model.atom;

import java.net.URI;
import java.util.List;
import static java.util.Collections.*;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultAtomPersonConstruct implements AtomPersonConstruct{

    private List<Object> params;

    /**
     * 
     */
    public DefaultAtomPersonConstruct(){
        this.params = EMPTY_LIST;
    }
    
    /**
     *
     * @param names
     * @param uris
     * @param emails
     */
    public DefaultAtomPersonConstruct(final List<Object> params){
        this.params = (params == null) ? EMPTY_LIST : verifParams(params);
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<Object> getParams() {return this.params;}


    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setParams(final List<Object> params) {this.params = this.verifParams(params);}

    private List<Object> verifParams(List<Object> params){
        for (Object param : params){
            if(!(param instanceof String) && !(param instanceof AtomEmail) && !(param instanceof URI))
                throw new IllegalArgumentException("This list must content only String, URI or AtomEmail instances.");
        }
        return params;
    }

}
