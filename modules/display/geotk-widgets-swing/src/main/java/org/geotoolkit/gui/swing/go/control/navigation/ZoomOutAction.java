/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotoolkit.gui.swing.go.control.navigation;

import java.awt.event.ActionEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.geotoolkit.gui.swing.go.GoMap2D;
import org.geotoolkit.gui.swing.resource.IconBundle;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ZoomOutAction extends AbstractAction {

    private static final ImageIcon ICON_ZOOM_OUT = IconBundle.getInstance().getIcon("16_zoom_out");

    private GoMap2D map = null;

    public ZoomOutAction() {
        super("",ICON_ZOOM_OUT);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (map != null ) {
            try {
                map.getCanvas().getController().scale(0.5d);
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(ZoomAllAction.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }

    public GoMap2D getMap() {
        return map;
    }

    public void setMap(GoMap2D map) {
        this.map = map;
        setEnabled(map != null);
    }
}
