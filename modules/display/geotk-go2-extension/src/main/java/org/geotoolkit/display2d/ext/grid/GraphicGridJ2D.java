/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Geomatys
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

package org.geotoolkit.display2d.ext.grid;


import java.util.List;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.canvas.VisitFilter;
import org.geotoolkit.display.primitive.SearchArea;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.canvas.RenderingContext2D;

import org.geotoolkit.display2d.primitive.AbstractGraphicJ2D;
import org.opengis.display.primitive.Graphic;

import static org.geotoolkit.util.ArgumentChecks.*;

/**
 * Graphic decoration to paint a grid.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class GraphicGridJ2D extends AbstractGraphicJ2D{

    private final GridTemplate template;

    public GraphicGridJ2D(final J2DCanvas canvas, final GridTemplate template){
        super(canvas,canvas.getObjectiveCRS());
        ensureNonNull("template", template);
        this.template = template;
    }

    @Override
    public void paint(final RenderingContext2D context) {
        J2DGridUtilities.paint(context, template);
    }

    @Override
    public List<Graphic> getGraphicAt(final RenderingContext context, final SearchArea mask, final VisitFilter filter, final List<Graphic> graphics) {
        return graphics;
    }


}
