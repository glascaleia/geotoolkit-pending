package org.geotoolkit.data.xal.model;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultMailStopNumber implements MailStopNumber {

    private final String content;
    private final String nameNumberSeparator;
    private final GrPostal grPostal;

    /**
     *
     * @param content
     * @param nameNumberSeparator
     * @param grPostal
     */
    public DefaultMailStopNumber(String nameNumberSeparator, GrPostal grPostal, String content){
        this.content = content;
        this.nameNumberSeparator = nameNumberSeparator;
        this.grPostal = grPostal;
    }

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
    public String getNameNumberSeparator() {return this.nameNumberSeparator;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public GrPostal getGrPostal() {return this.grPostal;}


}
