/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Johann Sorel
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
package org.geotoolkit.gui.swing.go2.control.creation;


import java.awt.Component;

import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.gui.swing.go2.CanvasHandler;
import org.geotoolkit.gui.swing.go2.Map2D;

/**
 *
 * @author eclesia
 */
public class DefaultEditionHandler implements CanvasHandler {
    
    private final DefaultEditionDecoration deco = new DefaultEditionDecoration();
    private Map2D map;

    public DefaultEditionHandler(Map2D map) {
        this.map = map;
    }

    public void setMap(Map2D map){
        this.map = map;
    }

    public Map2D getMap() {
        return map;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void install(Component component) {
        deco.reset();
        deco.getMouseListener().install(component);
        map.addDecoration(0,deco);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void uninstall(Component component) {
        deco.getMouseListener().uninstall(component);
        map.removeDecoration(deco);
    }

    @Override
    public J2DCanvas getCanvas() {
        return map.getCanvas();
    }

}
