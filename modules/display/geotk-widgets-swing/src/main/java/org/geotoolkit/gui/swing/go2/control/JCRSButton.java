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
package org.geotoolkit.gui.swing.go2.control;

import javax.swing.JButton;
import org.geotoolkit.gui.swing.go2.GoMap2D;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class JCRSButton extends JButton{

    private final CRSAction ACTION_CRS = new CRSAction();
    private GoMap2D map = null;

    public JCRSButton(){
        super();
        setAction(ACTION_CRS);
        setText("CRS");
    }

    /**
     * set the related Map2D
     * @param map2d : related Map2D
     */
    public void setMap(GoMap2D map2d) {
        map = map2d;
        ACTION_CRS.setMap(map);
    }

}
