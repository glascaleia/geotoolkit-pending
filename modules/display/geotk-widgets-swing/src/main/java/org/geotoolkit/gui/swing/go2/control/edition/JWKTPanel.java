/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.gui.swing.go2.control.edition;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.geotoolkit.gui.swing.resource.MessageBundle;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class JWKTPanel extends javax.swing.JPanel {

    public static final String GEOMETRY_PROPERTY = "geometry";
    
    private final WKTReader reader = new WKTReader();
    private final WKTWriter writer = new WKTWriter();
    
    private Geometry original = null;
    private Geometry current = null;
    
    public JWKTPanel() {
        initComponents();
    }
    
    public void setGeometry(Geometry geom){
        this.original = geom;
        if(original != null){
            guiText.setText(writer.write(original));
        }else{
            guiText.setText("");
        }
        guiError.setText("");
        this.current = null;
    }
    
    public Geometry getGeometry(){
        if(current == null){
            return original;
        }else{
            return current;
        }
    }
    
    private boolean checkWKT(){
        String txt = guiText.getText().trim();
        
        try {
            current = reader.read(txt);
            guiError.setText(null);
            return true;
        } catch (Exception ex) {
            current = null;
            guiError.setText(ex.getLocalizedMessage());
            return false;
        }
        
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        guiText = new javax.swing.JTextArea();
        guiRollback = new javax.swing.JButton();
        guiApply = new javax.swing.JButton();
        guiError = new javax.swing.JLabel();

        guiText.setColumns(20);
        jScrollPane1.setViewportView(guiText);

        guiRollback.setText(MessageBundle.getString("cancel")); // NOI18N
        guiRollback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiRollbackActionPerformed(evt);
            }
        });

        guiApply.setText(MessageBundle.getString("apply")); // NOI18N
        guiApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiApplyActionPerformed(evt);
            }
        });

        guiError.setForeground(new java.awt.Color(255, 0, 0));
        guiError.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(guiApply, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(guiRollback, javax.swing.GroupLayout.Alignment.TRAILING)))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(guiError, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiApply, guiRollback});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(guiApply)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiRollback, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiError))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void guiApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiApplyActionPerformed
        final Geometry old = getGeometry();
        if(checkWKT()){
            firePropertyChange(GEOMETRY_PROPERTY, old, getGeometry());
        }
    }//GEN-LAST:event_guiApplyActionPerformed

    private void guiRollbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiRollbackActionPerformed
        if(original != null){
            guiText.setText(writer.write(original));
            guiError.setText("");
        }
        final Geometry old = getGeometry();
        current = null;
        firePropertyChange(GEOMETRY_PROPERTY, old, getGeometry());
    }//GEN-LAST:event_guiRollbackActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton guiApply;
    private javax.swing.JLabel guiError;
    private javax.swing.JButton guiRollback;
    private javax.swing.JTextArea guiText;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}