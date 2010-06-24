package org.geotoolkit.data.kml.model;

import java.util.List;
import org.geotoolkit.data.kml.xsd.SimpleType;
import static java.util.Collections.*;
import static org.geotoolkit.data.kml.xml.KmlModelConstants.*;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultPair extends DefaultAbstractObject implements Pair{

    private StyleState key;
    private String styleUrl;
    private AbstractStyleSelector styleSelector;
    private List<SimpleType> pairSimpleExtensions;
    private List<AbstractObject> pairObjectExtensions;

    public DefaultPair(){
        this.key = DEF_STYLE_STATE;
        this.pairSimpleExtensions = EMPTY_LIST;
        this.pairObjectExtensions = EMPTY_LIST;
    }

    /**
     *
     * @param objectSimpleExtensions
     * @param idAttributes
     * @param key
     * @param styleUrl
     * @param styleSelector
     * @param pairSimpleExtensions
     * @param pairObjectExtensions
     */
    public DefaultPair(List<SimpleType> objectSimpleExtensions, IdAttributes idAttributes,
            StyleState key, String styleUrl, AbstractStyleSelector styleSelector,
            List<SimpleType> pairSimpleExtensions,
            List<AbstractObject> pairObjectExtensions){
        super(objectSimpleExtensions, idAttributes);
        this.key = key;
        this.styleUrl = styleUrl;
        this.styleSelector = styleSelector;
        this.pairSimpleExtensions = (pairSimpleExtensions == null) ? EMPTY_LIST : pairSimpleExtensions;
        this.pairObjectExtensions = (pairObjectExtensions == null) ? EMPTY_LIST : pairObjectExtensions;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public StyleState getKey() {return this.key;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getStyleUrl() {return this.styleUrl;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public AbstractStyleSelector getAbstractStyleSelector() {return this.styleSelector;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<SimpleType> getPairSimpleExtensions() {return this.pairSimpleExtensions;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<AbstractObject> getPairObjectExtensions() {return this.pairObjectExtensions;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setKey(StyleState key) {
        this.key = key;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setStyleUrl(String styleUrl) {
        this.styleUrl = styleUrl;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setAbstractStyleSelector(AbstractStyleSelector styleSelector) {
        this.styleSelector = styleSelector;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setPairSimpleExtensions(List<SimpleType> pairSimpleExtensions) {
        this.pairSimpleExtensions = pairSimpleExtensions;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setPairObjectExtensions(List<AbstractObject> pairObjectExtensions) {
        this.pairObjectExtensions = pairObjectExtensions;
    }

}
