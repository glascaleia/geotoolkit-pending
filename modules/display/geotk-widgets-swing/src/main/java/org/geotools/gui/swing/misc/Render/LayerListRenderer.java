/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.misc.Render;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.geotools.gui.swing.resource.IconBundle;
import org.geotoolkit.map.MapLayer;

/**
 * layer list renderer
 * 
 * @author Johann Sorel (Puzzle-GIS)
 */
public class LayerListRenderer implements ListCellRenderer{

    private static final ImageIcon ICON_LAYER_VISIBLE = IconBundle.getInstance().getIcon("16_maplayer_visible");
    private final Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY,1);
    private final Border nullborder = BorderFactory.createEmptyBorder(1,1,1,1);
    
    JLabel lbl = new JLabel();
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        
        if(value instanceof MapLayer){
            MapLayer layer = (MapLayer) value;
            lbl.setText(layer.getDescription().getTitle().toString());
            lbl.setIcon(getIcon(layer));
        }else{
            lbl.setText(value.toString());
            lbl.setIcon(null);
        }
        
        if(isSelected ){
            lbl.setBorder(border);
        }else{
            lbl.setBorder(nullborder);
        }
                
        return lbl;
    }
    
    private ImageIcon getIcon(MapLayer layer){
//        DataStore ds = (DataStore) layer.getFeatureSource().getDataStore();
//
//        if (layer.getFeatureSource().getSchema().getName().getLocalPart().equals("GridCoverage")) {
//            return ICON_LAYER_FILE_RASTER_VISIBLE ;
//        } else if (AbstractFileDataStore.class.isAssignableFrom(ds.getClass())) {
//            return ICON_LAYER_FILE_VECTOR_VISIBLE ;
//        } else if (JDBC1DataStore.class.isAssignableFrom(ds.getClass())) {
//            return ICON_LAYER_DB_VISIBLE;
//        } else {
            return ICON_LAYER_VISIBLE;
//        }
    }

}
