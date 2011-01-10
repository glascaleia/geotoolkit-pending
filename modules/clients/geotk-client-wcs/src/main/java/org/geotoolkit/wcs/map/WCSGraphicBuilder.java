/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotoolkit.wcs.map;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.GridCoverageFactory;
import org.geotoolkit.display.canvas.ReferencedCanvas2D;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.canvas.VisitFilter;
import org.geotoolkit.display.canvas.control.CanvasMonitor;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display.primitive.SearchArea;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.AbstractGraphicJ2D;
import org.geotoolkit.display2d.primitive.GraphicJ2D;
import org.geotoolkit.map.GraphicBuilder;
import org.geotoolkit.map.MapLayer;

import org.opengis.display.canvas.Canvas;
import org.opengis.display.primitive.Graphic;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Render WCS layer in default geotoolkit rendering engine.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
final class WCSGraphicBuilder implements GraphicBuilder<GraphicJ2D>{

    /**
     * One instance for all WCS map layers. Object is concurrent.
     */
    static final WCSGraphicBuilder INSTANCE = new WCSGraphicBuilder();

    private WCSGraphicBuilder(){};

    @Override
    public Collection<GraphicJ2D> createGraphics(final MapLayer layer, final Canvas canvas) {
        if(layer instanceof WCSMapLayer && canvas instanceof J2DCanvas){
            return Collections.singleton((GraphicJ2D)
                    new WCSGraphic((J2DCanvas)canvas, (WCSMapLayer)layer));
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public Class<GraphicJ2D> getGraphicType() {
        return GraphicJ2D.class;
    }

    @Override
    public Image getLegend(final MapLayer layer) throws PortrayalException {
        //no legend in WCS specification
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    private static class WCSGraphic extends AbstractGraphicJ2D{

        private final WCSMapLayer layer;

        private WCSGraphic(final J2DCanvas canvas, final WCSMapLayer layer){
            super(canvas,canvas.getObjectiveCRS2D());
            this.layer = layer;
        }

        @Override
        public void paint(final RenderingContext2D context2D) {
            final CanvasMonitor monitor = context2D.getMonitor();


            CoordinateReferenceSystem queryCrs = context2D.getObjectiveCRS2D();
            Envelope env = context2D.getCanvasObjectiveBounds();

            final Dimension dim = context2D.getCanvasDisplayBounds().getSize();
            final URL url;
            try {
                url = layer.query(env, dim);
            } catch (Exception ex) {
                monitor.exceptionOccured(ex, Level.WARNING);
                return;
            }

            getLogger().log(Level.WARNING, "[WCSMapLayer] : GETCOVERAGE request : {0}", url);

            final BufferedImage image;
            try {
                image = ImageIO.read(url);
            } catch (IOException io) {
                monitor.exceptionOccured(new PortrayalException(io), Level.WARNING);
                return;
            }

            if (image == null) {
                monitor.exceptionOccured(new PortrayalException("WCS server did not return an image."), Level.WARNING);
                return;
            }

            final GridCoverageFactory gc = new GridCoverageFactory();
            final GridCoverage2D coverage = gc.create("wcs", image, env);
            try {
                GO2Utilities.portray(context2D, coverage);
            } catch (PortrayalException ex) {
                monitor.exceptionOccured(ex, Level.WARNING);
                return;
            }
        }

        @Override
        public List<Graphic> getGraphicAt(final RenderingContext context, final SearchArea mask, final VisitFilter filter, final List<Graphic> graphics) {
            return graphics;
        }

    }

}
