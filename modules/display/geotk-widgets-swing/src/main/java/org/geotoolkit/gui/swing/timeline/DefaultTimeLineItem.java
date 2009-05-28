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
package org.geotoolkit.gui.swing.timeline;

import java.awt.Color;
import java.awt.Image;
import java.util.Date;

/**
 *
 * @author johann sorel
 */
public class DefaultTimeLineItem implements TimeLineItem{

    private final Date date;
    private final String toolTip;
    private final Color color;
    private final Image normalImage;
    private final Image selectedImage;
    
    public DefaultTimeLineItem(Date d,String tooltip,Color color,Image img,Image selected){
        date = d;
        toolTip = tooltip;
        this.color = color;
        normalImage = img;
        selectedImage = selected;
    }
    
    public Date getDate() {
        return date;
    }

    public String getToolTip() {
        return toolTip;
    }

    public Color getColor() {
        return color;
    }

    public Image getImage() {
        return normalImage;
    }

    public int compareTo(TimeLineItem o) {
        
        if(o.getImage() != null){
            if(normalImage == null){
                return -1;
            }else{
                return date.compareTo(o.getDate());
            }
            
        }else{
            if(normalImage != null){
                return 1;
            }else{
                return date.compareTo(o.getDate());
            }
            
        }
        
    }

    public Image getSelectedImage() {
        return selectedImage;
    }

}
