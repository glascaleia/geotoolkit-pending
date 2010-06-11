package org.geotoolkit.data.model.xal;

/**
 *
 * @author Samuel Andrés
 */
public class ThoroughfareNumberSuffixDefault implements ThoroughfareNumberSuffix {

    private final String numberSuffixSeparator;
    private final String type;
    private final GrPostal grPostal;
    private final String content;

    /**
     * 
     * @param numberSuffixSeparator
     * @param type
     * @param grPostal
     * @param content
     */
    public ThoroughfareNumberSuffixDefault(String numberSuffixSeparator,
            String type, GrPostal grPostal, String content){
        this.numberSuffixSeparator = numberSuffixSeparator;
        this.type = type;
        this.grPostal = grPostal;
        this.content = content;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getNumberSuffixSeparator() {return this.numberSuffixSeparator;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getContent() {return this.content;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getType() {return this.type;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public GrPostal getGrPostal() {return this.grPostal;}

}
