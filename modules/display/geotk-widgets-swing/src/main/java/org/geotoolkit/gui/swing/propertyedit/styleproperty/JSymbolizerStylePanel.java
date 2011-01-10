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
package org.geotoolkit.gui.swing.propertyedit.styleproperty;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;

import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.gui.swing.propertyedit.PropertyPane;
import org.geotoolkit.gui.swing.style.JLineSymbolizerPane;
import org.geotoolkit.gui.swing.style.JPointSymbolizerPane;
import org.geotoolkit.gui.swing.style.JPolygonSymbolizerPane;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.gui.swing.style.JRasterSymbolizerPane;
import org.geotoolkit.gui.swing.style.JTextSymbolizerPane;
import org.geotoolkit.gui.swing.style.StyleElementEditor;

import org.opengis.style.LineSymbolizer;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Symbolizer;
import org.opengis.style.TextSymbolizer;

/**
 * Simple style panel
 * 
 * @author Johann Sorel
 * @module pending
 */
public class JSymbolizerStylePanel extends JPanel implements PropertyPane {

    private MapLayer layer;
    private StyleElementEditor<? extends Symbolizer> detail = null;

    /** Creates new form XMLStylePanel */
    public JSymbolizerStylePanel() {
        setLayout(new BorderLayout());
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void apply() {
    }

    @Override
    public ImageIcon getIcon() {
        return IconBundle.getIcon("16_simple_style");
    }

    @Override
    public String getTitle() {
        return MessageBundle.getString("property_style_simple");
    }

    @Override
    public void setTarget(final Object layer) {

        if (layer instanceof MapLayer) {
            this.layer = (MapLayer) layer;
            if(detail != null){
                detail.setLayer(this.layer);
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public String getToolTip() {
        return "";
    }

    public void setSymbolizer(final Symbolizer symbol){
        removeAll();

        if (symbol instanceof PolygonSymbolizer) {
            detail = new JPolygonSymbolizerPane();
            ((StyleElementEditor<PolygonSymbolizer>)detail).parse((PolygonSymbolizer) symbol);
        } else if (symbol instanceof LineSymbolizer) {
            detail = new JLineSymbolizerPane();
            ((StyleElementEditor<LineSymbolizer>)detail).parse((LineSymbolizer) symbol);
        } else if (symbol instanceof PointSymbolizer) {
            detail = new JPointSymbolizerPane();
            ((StyleElementEditor<PointSymbolizer>)detail).parse((PointSymbolizer) symbol);
        } else if (symbol instanceof RasterSymbolizer) {
            detail = new JRasterSymbolizerPane();
            ((StyleElementEditor<RasterSymbolizer>)detail).parse((RasterSymbolizer) symbol);
        } else if (symbol instanceof TextSymbolizer) {
            detail = new JTextSymbolizerPane();
            ((StyleElementEditor<TextSymbolizer>)detail).parse((TextSymbolizer) symbol);            
        } else {
            detail = null;
        }

        if(detail != null){
            detail.setLayer(layer);
            final JScrollPane jsp = new JScrollPane(detail);
            add(BorderLayout.CENTER, jsp );
        }else{
            add(BorderLayout.CENTER,new JLabel("<b>" + MessageBundle.getString("property_style_unknown_simplestyle") + "</b>"));
        }

        revalidate();
        repaint();
    }

    public Symbolizer getSymbolizer(){
        return detail.create();
    }

    
}
