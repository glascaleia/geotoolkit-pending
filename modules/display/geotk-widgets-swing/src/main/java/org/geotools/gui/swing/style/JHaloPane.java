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
import java.awt.Component;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotoolkit.map.MapLayer;
import org.opengis.style.Halo;

/**
 * Halo panel
 * 
 * @author Johann Sorel
 */
public class JHaloPane extends StyleElementEditor<Halo> {

    private Halo halo = null;
    private MapLayer layer = null;

    /** Creates new form JHaloPanel */
    public JHaloPane() {
        initComponents();
        init();
    }

    private void init() {
        guiRadius.setModel(1d, 0d, Double.MAX_VALUE, 1);
    }

    @Override
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiFill.setLayer(layer);
        guiRadius.setLayer(layer);
    }

    @Override
    public MapLayer getLayer() {
        return layer;
    }

    @Override
    public void parse(Halo halo) {
        this.halo = halo;

//        if (halo != null) {
//            guiFill.setEdited(halo.getFill());
//            guiRadius.setExpression(halo.getRadius());
//        }
    }

    @Override
    public Halo create() {
//        if (halo == null) {
//            halo = new StyleBuilder().createHalo();
//        }

        apply();
        return halo;
    }

    public void apply() {
//        if (halo != null) {
//            halo.setFill(guiFill.getEdited());
//            halo.setRadius(guiRadius.getExpression());
//
//        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiFill = new JFillPane();
        jLabel1 = new JLabel();
        guiRadius = new JNumberExpressionPane();

        setOpaque(false);


        jLabel1.setText(MessageBundle.getString("radius")); // NOI18N
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiRadius, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(guiFill, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(guiRadius, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiFill, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiRadius, jLabel1});

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JFillPane guiFill;
    private JNumberExpressionPane guiRadius;
    private JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
