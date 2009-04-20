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
package org.geotools.gui.swing.style;

import org.geotools.gui.swing.resource.MessageBundle;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotoolkit.map.MapLayer;
import org.opengis.style.LabelPlacement;

/**
 * Label placement panel
 * 
 * @author Johann Sorel
 */
public class JLabelPlacementPane extends StyleElementEditor<LabelPlacement> {

    private MapLayer layer = null;
    private LabelPlacement placement = null;

    /** Creates new form JPointPlacementPanel */
    public JLabelPlacementPane() {
        initComponents();
    }

    @Override
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiLine.setLayer(layer);
        guiPoint.setLayer(layer);
    }

    @Override
    public MapLayer getLayer() {
        return layer;
    }

    @Override
    public void parse(LabelPlacement target) {
        placement = target;

//        if (placement != null) {
//
//            if(target instanceof LinePlacement){
//                jrbLine.setSelected(true);
//                guiLine.setEdited( (LinePlacement)target);
//            }else if(target instanceof PointPlacement){
//                jrbPoint.setSelected(true);
//                guiPoint.setEdited( (PointPlacement)target);
//            }
//        }

    }

    @Override
    public LabelPlacement create() {

//        if (placement == null) {
//            placement = new StyleBuilder().createPointPlacement();
//        }

        apply();
        return placement;
    }

    public void apply() {
//        if (placement != null) {
//            if(jrbLine.isSelected()){
//                placement = guiLine.getEdited();
//            }else{
//                placement = guiPoint.getEdited();
//            }
//        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpType = new ButtonGroup();
        guiPoint = new JPointPlacementPane();
        guiLine = new JLinePlacementPane();
        jrbLine = new JRadioButton();
        jrbPoint = new JRadioButton();
        jSeparator1 = new JSeparator();

        setOpaque(false);

        grpType.add(jrbLine);
        jrbLine.setSelected(true);
        jrbLine.setText(MessageBundle.getString("lineplacement")); // NOI18N
        jrbLine.setOpaque(false);

        grpType.add(jrbPoint);
        jrbPoint.setText(MessageBundle.getString("pointplacement")); // NOI18N
        jrbPoint.setOpaque(false);

        jSeparator1.setOrientation(SwingConstants.VERTICAL);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(guiLine, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jrbLine))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jrbPoint)
                    .addComponent(guiPoint, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jrbLine)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiLine, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
            .addComponent(jSeparator1, GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jrbPoint)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiPoint, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup grpType;
    private JLinePlacementPane guiLine;
    private JPointPlacementPane guiPoint;
    private JSeparator jSeparator1;
    private JRadioButton jrbLine;
    private JRadioButton jrbPoint;
    // End of variables declaration//GEN-END:variables
}
