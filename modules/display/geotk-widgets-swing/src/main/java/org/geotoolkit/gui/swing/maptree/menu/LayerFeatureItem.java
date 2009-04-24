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
package org.geotoolkit.gui.swing.maptree.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.geotoolkit.gui.swing.maptree.TreePopupItem;
import org.geotoolkit.gui.swing.propertyedit.JPropertyDialog;
import org.geotoolkit.gui.swing.propertyedit.LayerFeaturePropertyPanel;
import org.geotoolkit.gui.swing.propertyedit.PropertyPane;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.map.FeatureMapLayer;


/**
 * Default popup control for property page of MapLayer, use for JContextTreePopup
 * 
 * @author Johann Sorel (Puzzle-GIS)
 */
public class LayerFeatureItem extends JMenuItem implements TreePopupItem{
    
    private WeakReference<FeatureMapLayer> layerRef;
    
    /** 
     * Creates a new instance of DefaultContextPropertyPop 
     */
    public LayerFeatureItem() {
        super( MessageBundle.getString("contexttreetable_feature_table")  );
        init();
    }
        
    private void init(){
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(layerRef == null) return;

                FeatureMapLayer layer = layerRef.get();
                if(layer == null) return;

                ArrayList<PropertyPane> lst = new ArrayList<PropertyPane>();
                lst.add(new LayerFeaturePropertyPanel());
                JPropertyDialog.showDialog(lst, layer);
                
            }
        }
        );
    }
    
    @Override
    public boolean isValid(TreePath[] selection) {
        if (selection != null && selection.length == 1 && selection[0].getLastPathComponent() instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selection[0].getLastPathComponent();
            return ( node.getUserObject() instanceof FeatureMapLayer ) ;
        }
        return false;
        
    }

    @Override
    public Component getComponent(TreePath[] selection) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selection[0].getLastPathComponent();
        layerRef = new WeakReference<FeatureMapLayer>((FeatureMapLayer) node.getUserObject());
        return this;
    }
    
}
