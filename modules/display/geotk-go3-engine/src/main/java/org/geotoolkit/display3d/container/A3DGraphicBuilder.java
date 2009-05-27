/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Geomatys
 *    (C) 2009, Johann Sorel
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
package org.geotoolkit.display3d.container;

import com.ardor3d.scenegraph.shape.Box;
import java.util.ArrayList;
import java.util.Collection;
import org.geotoolkit.display3d.canvas.A3DCanvas;
import org.geotoolkit.display3d.primitive.A3DGraphic;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.DynamicMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.GraphicBuilder;
import org.geotoolkit.map.MapLayer;
import org.opengis.display.canvas.Canvas;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 */
public class A3DGraphicBuilder implements GraphicBuilder<A3DGraphic>{

    @Override
    public Collection<A3DGraphic> createGraphics(MapLayer layer, Canvas canvas) {

        if(canvas == null || !(canvas instanceof A3DCanvas)){
            throw new IllegalArgumentException("Canvas must be an A3DCanvas");
        }

        final A3DCanvas a3dcanvas = (A3DCanvas) canvas;

        final Collection<A3DGraphic> graphics = new ArrayList<A3DGraphic>();

        if(layer instanceof FeatureMapLayer){
            graphics.add(new FeatureLayerNode(a3dcanvas, (FeatureMapLayer)layer));
        }else if(layer instanceof CoverageMapLayer){
            graphics.add(new CoverageLayerNode(a3dcanvas, (CoverageMapLayer)layer));
        }else if(layer instanceof DynamicMapLayer){
            //TODO not handle yet
        }

        return graphics;
    }

    @Override
    public Class<A3DGraphic> getGraphicType() {
        return A3DGraphic.class;
    }

}
