package org.geotoolkit.data.model.kml;

import java.awt.Color;
import java.util.List;
import org.geotoolkit.data.model.xsd.SimpleType;
import static java.util.Collections.*;

/**
 *
 * @author Samuel Andrés
 */
public abstract class DefaultAbstractColorStyle extends DefaultAbstractSubStyle implements AbstractColorStyle {

    protected Color color;
    protected ColorMode colorMode;
    protected List<SimpleType> colorStyleSimpleExtensions;
    protected List<AbstractObject> colorStyleObjectExtensions;

    protected DefaultAbstractColorStyle(){}
    
    /**
     *
     * @param objectSimpleExtensions
     * @param idAttributes
     * @param subStyleSimpleExtensions
     * @param subStyleObjectExtensions
     * @param color
     * @param colorMode
     * @param colorStyleSimpleExtensions
     * @param colorStyleObjectExtensions
     */
    protected DefaultAbstractColorStyle(List<SimpleType> objectSimpleExtensions, IdAttributes idAttributes,
            List<SimpleType> subStyleSimpleExtensions, List<AbstractObject> subStyleObjectExtensions,
            Color color, ColorMode colorMode,
            List<SimpleType> colorStyleSimpleExtensions, List<AbstractObject> colorStyleObjectExtensions){
        super(objectSimpleExtensions, idAttributes, subStyleSimpleExtensions, subStyleObjectExtensions);
        this.color = color;
        this.colorMode = colorMode;
        this.colorStyleSimpleExtensions = (colorStyleSimpleExtensions == null) ? EMPTY_LIST : colorStyleSimpleExtensions;
        this.colorStyleObjectExtensions = (colorStyleObjectExtensions == null) ? EMPTY_LIST : colorStyleObjectExtensions;
    }

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public Color getColor() {return this.color;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public ColorMode getColorMode() {return this.colorMode;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public List<SimpleType> getColorStyleSimpleExtensions() {return this.colorStyleSimpleExtensions;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public List<AbstractObject> getColorStyleObjectExtensions() {return this.colorStyleObjectExtensions;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public void setColor(Color color){this.color = color;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public void setColorMode(ColorMode colorMode){this.colorMode = colorMode;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public void setColorStyleSimpleExtensions(List<SimpleType> colorStyleSimpleExtensions){
        this.colorStyleSimpleExtensions = colorStyleSimpleExtensions;
    }

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public void setColorStyleObjectExtensions(List<AbstractObject> colorStyleObjectExtensions){
        this.colorStyleObjectExtensions = colorStyleObjectExtensions;
    }

    @Override
    public String toString(){
        String resultat = super.toString()+
                "\n\tAbstractColorStyleDefault : "+
                "\n\tcolor : "+this.color+
                "\n\tcolorMode : "+this.colorMode;
        return resultat;
    }

}
