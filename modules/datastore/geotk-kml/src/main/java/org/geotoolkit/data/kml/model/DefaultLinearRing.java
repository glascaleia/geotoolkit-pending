/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.data.kml.model;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.List;
import org.geotoolkit.data.kml.xsd.SimpleTypeContainer;
import static org.geotoolkit.data.kml.xml.KmlConstants.*;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultLinearRing extends com.vividsolutions.jts.geom.LinearRing implements LinearRing {

    protected IdAttributes idAttributes;
    private final Extensions exts = new Extensions();
    private boolean extrude;
    private boolean tessellate;
    private AltitudeMode altitudeMode;

    /**
     *
     * @param coordinates
     * @param factory
     */
    public DefaultLinearRing(Coordinates coordinates, GeometryFactory factory) {
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
    public DefaultLinearRing(List<SimpleTypeContainer> objectSimpleExtensions,
            IdAttributes idAttributes,
            List<SimpleTypeContainer> abstractGeometrySimpleExtensions,
            List<Object> abstractGeometryObjectExtensions,
            boolean extrude, boolean tessellate,
            AltitudeMode altitudeMode,
            Coordinates coordinates,
            List<SimpleTypeContainer> lineStringSimpleExtensions,
            List<Object> lineStringObjectExtensions,
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
            this.extensions().simples(Extensions.Names.LINEAR_RING).addAll(lineStringSimpleExtensions);
        }
        if (lineStringObjectExtensions != null) {
            this.extensions().complexes(Extensions.Names.LINEAR_RING).addAll(lineStringObjectExtensions);
        }
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
    public Coordinates getCoordinateSequence() {
        return (Coordinates) super.getCoordinateSequence();
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
    public void setTessellate(boolean tesselate) {
        this.tessellate = tesselate;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setAltitudeMode(AltitudeMode altitudeMode) {
        this.altitudeMode = altitudeMode;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public Extensions extensions() {
        return this.exts;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public IdAttributes getIdAttributes() {
        return this.idAttributes;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setIdAttributes(IdAttributes idAttributes) {
        this.idAttributes = idAttributes;
    }
}
