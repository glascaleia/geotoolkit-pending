package org.geotoolkit.data.model.kml;

/**
 *
 * @author Samuel Andrés
 */
public class SimpleDataDefault implements SimpleData {

    private final String name;
    private final String content;

    /**
     *
     * @param name
     * @param content
     */
    public SimpleDataDefault(String name, String content){
        this.name = name;
        this.content = content;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getName() {return this.name;}

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public String getContent() {return this.content;}

}
