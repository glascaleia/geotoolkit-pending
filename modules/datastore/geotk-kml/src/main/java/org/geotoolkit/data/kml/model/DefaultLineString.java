package org.geotoolkit.data.kml.model;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.List;
import org.geotoolkit.data.kml.xsd.SimpleType;
import static org.geotoolkit.data.kml.xml.KmlModelConstants.*;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultLineString extends com.vividsolutions.jts.geom.LineString implements LineString {

    private IdAttributes idAttributes;
    private Extensions extensions = new Extensions();
    private boolean extrude;
    private boolean tessellate;
    private AltitudeMode altitudeMode;

    /**
     * 
     * @param coordinates
     * @param factory
     */
    public DefaultLineString(Coordinates coordinates, GeometryFactory factory) {
        super(coordinates, factory);
        this.extrude = DEF_EXTRUDE;
        this.tessellate = DEF_TESSELLATE;
        this.altitudeMode = DEF_ALTITUDE_MODE;
    }

    /**
     * 
     * @param objectSimpleExtensions
     * @param idAttributes
     * @param abstractGeometrySimpleExtensions
     * @param abstractGeometryObjectExtensions
     * @param extrude
     * @param tessellate
     * @param altitudeMode
     * @param coordinates
     * @param lineStringSimpleExtensions
     * @param lineStringObjectExtensions
     * @param factory
     */
    public DefaultLineString(List<SimpleType> objectSimpleExtensions,
            IdAttributes idAttributes,
            List<SimpleType> abstractGeometrySimpleExtensions,
            List<AbstractObject> abstractGeometryObjectExtensions,
            boolean extrude, boolean tessellate,
            AltitudeMode altitudeMode,
            Coordinates coordinates,
            List<SimpleType> lineStringSimpleExtensions,
            List<AbstractObject> lineStringObjectExtensions,
            GeometryFactory factory) {
        super(coordinates, factory);
        this.idAttributes = idAttributes;
        if (objectSimpleExtensions != null) {
            this.extensions().simples(Extensions.Names.OBJECT).addAll(objectSimpleExtensions);
        }
        if (abstractGeometrySimpleExtensions != null) {
            this.extensions().simples(Extensions.Names.GEOMETRY).addAll(abstractGeometrySimpleExtensions);
        }
        if (abstractGeometryObjectExtensions != null) {
            this.extensions().complexes(Extensions.Names.GEOMETRY).addAll(abstractGeometryObjectExtensions);
        }
        this.extrude = extrude;
        this.tessellate = tessellate;
        this.altitudeMode = altitudeMode;
        if (lineStringSimpleExtensions != null) {
            this.extensions().simples(Extensions.Names.LINE_STRING).addAll(lineStringSimpleExtensions);
        }
        if (lineStringObjectExtensions != null) {
            this.extensions().complexes(Extensions.Names.LINE_STRING).addAll(lineStringObjectExtensions);
        }

    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public Coordinates getCoordinateSequence() {
        return (Coordinates) super.getCoordinateSequence();
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public boolean getExtrude() {
        return this.extrude;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public boolean getTessellate() {
        return this.tessellate;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public AltitudeMode getAltitudeMode() {
        return this.altitudeMode;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setExtrude(boolean extrude) {
        this.extrude = extrude;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setTessellate(boolean tessellate) {
        this.tessellate = tessellate;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setAltitudeMode(AltitudeMode altitudeMode) {
        this.altitudeMode = altitudeMode;
    }

    @Override
    public Extensions extensions() {
        return this.extensions;
    }

    @Override
    public IdAttributes getIdAttributes() {
        return this.idAttributes;
    }

    @Override
    public void setIdAttributes(IdAttributes idAttributes) {
        this.idAttributes = idAttributes;
    }
}
