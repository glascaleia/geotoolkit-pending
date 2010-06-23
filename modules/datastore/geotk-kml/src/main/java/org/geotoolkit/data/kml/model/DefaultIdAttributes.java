package org.geotoolkit.data.kml.model;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultIdAttributes implements IdAttributes{

    private String id;
    private String targetId;

    /**
     * 
     */
    public DefaultIdAttributes(){}

    /**
     *
     * @param id
     * @param targetId
     */
    public DefaultIdAttributes(String id, String targetId){
        this.id = id;
        this.targetId = targetId;
    }

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public String getId(){return this.id;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public String getTargetId(){return this.targetId;}

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public void getId(String id) {
        this.id = id;
    }

    /**
     *
     * @{@inheritDoc}
     */
    @Override
    public void getTargetId(String targetId) {
        this.targetId = targetId;
    }
}
