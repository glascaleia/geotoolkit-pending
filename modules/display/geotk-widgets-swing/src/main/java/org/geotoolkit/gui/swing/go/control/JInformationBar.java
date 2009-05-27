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
package org.geotoolkit.gui.swing.go.control;


import java.awt.Component;
import org.geotoolkit.gui.swing.go.control.information.InformationAction;
import javax.swing.JToolBar;

import org.geotoolkit.gui.swing.go.GoMap2D;
import org.geotoolkit.gui.swing.go.control.information.MesureAreaAction;
import org.geotoolkit.gui.swing.go.control.information.MesureLenghtAction;

/**
 * Information bar
 *
 * @author johann sorel (Puzzle-GIS)
 */
public class JInformationBar extends JToolBar implements MapControlBar{

    private final MesureLenghtAction actionLenght = new MesureLenghtAction();
    private final MesureAreaAction actionArea = new MesureAreaAction();
    private final InformationAction actionInfo = new InformationAction();
    private GoMap2D map = null;

    public JInformationBar() {
        this(null);
    }

    /**
     * Creates a new instance of JMap2DControlBar
     * @param pane : related Map2D or null
     */
    public JInformationBar(GoMap2D pane) {
        add(actionLenght);
        add(actionArea);
        add(actionInfo);
        setMap(pane);
    }

    /**
     * set the related Map2D
     * @param map2d : related Map2D
     */
    public void setMap(GoMap2D map2d) {
        map = map2d;
        actionLenght.setMap(map);
        actionArea.setMap(map);
        actionInfo.setMap(map);
    }

    @Override
    public GoMap2D getMap() {
        return map;
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
