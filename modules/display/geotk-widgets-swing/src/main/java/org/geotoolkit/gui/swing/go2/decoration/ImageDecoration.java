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
package org.geotoolkit.gui.swing.go2.decoration;

import javax.swing.JComponent;

import org.geotoolkit.gui.swing.go2.JMap2D;
import org.jdesktop.swingx.JXImagePanel;

/**
 * Image Decoration
 * 
 * @author Johann Sorel
 * @module pending
 */
public class ImageDecoration extends JXImagePanel implements MapDecoration{

    
    public ImageDecoration(){}
    
    @Override
    public void refresh() {
        revalidate();
        repaint();
    }

    @Override
    public JComponent geComponent() {
        return this;
    }
    
    @Override
    public void setMap2D(JMap2D map) {
        
    }

    @Override
    public JMap2D getMap2D() {
        return null;
    }

    @Override
    public void dispose() {
    }
}
