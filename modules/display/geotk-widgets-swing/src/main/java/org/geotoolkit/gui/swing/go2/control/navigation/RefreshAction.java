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
package org.geotoolkit.gui.swing.go2.control.navigation;

import java.awt.event.ActionEvent;

import org.geotoolkit.gui.swing.go2.control.AbstractMapAction;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.gui.swing.resource.MessageBundle;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class RefreshAction extends AbstractMapAction {

    public RefreshAction() {
        putValue(SMALL_ICON, IconBundle.getIcon("16_data_reload"));
        putValue(SHORT_DESCRIPTION, MessageBundle.getString("map_refresh"));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void actionPerformed(final ActionEvent arg0) {
        if (map != null) {
            map.getCanvas().getController().repaint();
        }
    }

}
