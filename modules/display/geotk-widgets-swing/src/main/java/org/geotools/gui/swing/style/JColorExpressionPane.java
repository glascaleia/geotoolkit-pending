/*
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.expression.Expression;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 */
public class JColorExpressionPane extends StyleElementEditor<Expression>{

    /** Creates new form JColorExpressionPane */
    public JColorExpressionPane() {
        initComponents();
    }

    @Override
    public void setLayer(MapLayer layer) {
        super.setLayer(layer);
        guiSpecial.setLayer(layer);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiColor = new JButton();
        guiSpecial = new JSpecialExpressionButton();

        setOpaque(false);

        guiColor.setBackground(new Color(255, 0, 0));
        guiColor.setText("...");
        guiColor.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        guiColor.setContentAreaFilled(false);
        guiColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiColorActionPerformed(evt);
            }
        });

        guiSpecial.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                guiSpecialPropertyChange(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(guiColor, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiSpecial, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(guiColor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(guiSpecial, GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void guiColorActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiColorActionPerformed
    final Color color = JColorChooser.showDialog(null, "", guiColor.getBackground());
    parse(getStyleFactory().literal(color));
}//GEN-LAST:event_guiColorActionPerformed

private void guiSpecialPropertyChange(PropertyChangeEvent evt) {//GEN-FIRST:event_guiSpecialPropertyChange
    if(evt.getPropertyName().equals(JSpecialExpressionButton.EXPRESSION_PROPERTY)){
        parse(guiSpecial.get());
    }
}//GEN-LAST:event_guiSpecialPropertyChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton guiColor;
    private JSpecialExpressionButton guiSpecial;
    // End of variables declaration//GEN-END:variables

    @Override
    public void parse(Expression target) {
        if(target != null){
            if(isStatic(target)){
                final Color color = target.evaluate(null, Color.class);
                if(color != null){
                    guiSpecial.parse(null);
                    guiColor.setBackground(color);
                    guiColor.setOpaque(true);
                }else{
                    guiSpecial.parse(target);
                    guiColor.setBackground(Color.RED);
                    guiColor.setOpaque(false);
                }
            }else{
                guiSpecial.parse(target);
                guiColor.setBackground(Color.RED);
                guiColor.setOpaque(false);
            }
        }else{
            guiSpecial.parse(null);
            guiColor.setBackground(Color.RED);
            guiColor.setOpaque(true);
        }
    }

    @Override
    public Expression create() {
        final Expression special = guiSpecial.get();
        if(special != null){
            return special;
        }else{
            return getStyleFactory().literal(guiColor.getBackground());
        }
    }

}
