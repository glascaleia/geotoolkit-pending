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
package org.geotoolkit.gui.swing.style;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.StyleConstants;
import org.opengis.filter.expression.Expression;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class JLineCapExpressionPane extends StyleElementEditor<Expression>{

    private static final ImageIcon ICON_CAP_ROUND = IconBundle.getInstance().getIcon("16_linecap_round");
    private static final ImageIcon ICON_CAP_SQUARE = IconBundle.getInstance().getIcon("16_linecap_square");
    private static final ImageIcon ICON_CAP_BUTT = IconBundle.getInstance().getIcon("16_linecap_butt");

    /** Creates new form JColorExpressionPane */
    public JLineCapExpressionPane() {
        initComponents();
        guiRound.setIcon(ICON_CAP_ROUND);
        guiSquare.setIcon(ICON_CAP_SQUARE);
        guiButt.setIcon(ICON_CAP_BUTT);
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

        capGroup = new ButtonGroup();
        guiSpecial = new JSpecialExpressionButton();
        guiRound = new JToggleButton();
        guiSquare = new JToggleButton();
        guiButt = new JToggleButton();

        setOpaque(false);

        guiSpecial.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                guiSpecialPropertyChange(evt);
            }
        });

        capGroup.add(guiRound);
        guiRound.setSelected(true);
        guiRound.setBorderPainted(false);
        guiRound.setIconTextGap(0);
        guiRound.setMargin(new Insets(2, 2, 2, 2));
        guiRound.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiRoundActionPerformed(evt);
            }
        });

        capGroup.add(guiSquare);
        guiSquare.setBorderPainted(false);
        guiSquare.setIconTextGap(0);
        guiSquare.setMargin(new Insets(2, 2, 2, 2));
        guiSquare.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiSquareActionPerformed(evt);
            }
        });

        capGroup.add(guiButt);
        guiButt.setBorderPainted(false);
        guiButt.setIconTextGap(0);
        guiButt.setMargin(new Insets(2, 2, 2, 2));
        guiButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiButtActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(guiRound)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiSquare)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiButt)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiSpecial, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {guiButt, guiRound, guiSpecial, guiSquare});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
                .addComponent(guiSpecial, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                .addGroup(Alignment.LEADING, layout.createParallelGroup(Alignment.TRAILING, false)
                    .addComponent(guiRound, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guiSquare, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guiButt, Alignment.LEADING)))
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiButt, guiRound, guiSpecial, guiSquare});

    }// </editor-fold>//GEN-END:initComponents

private void guiSpecialPropertyChange(PropertyChangeEvent evt) {//GEN-FIRST:event_guiSpecialPropertyChange
    if(evt.getPropertyName().equals(JSpecialExpressionButton.EXPRESSION_PROPERTY)) {
        parse(guiSpecial.get());
    }
}//GEN-LAST:event_guiSpecialPropertyChange

private void guiRoundActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiRoundActionPerformed
    parse(StyleConstants.STROKE_CAP_ROUND);
}//GEN-LAST:event_guiRoundActionPerformed

private void guiSquareActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiSquareActionPerformed
    parse(StyleConstants.STROKE_CAP_SQUARE);
}//GEN-LAST:event_guiSquareActionPerformed

private void guiButtActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiButtActionPerformed
    parse(StyleConstants.STROKE_CAP_BUTT);
}//GEN-LAST:event_guiButtActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup capGroup;
    private JToggleButton guiButt;
    private JToggleButton guiRound;
    private JSpecialExpressionButton guiSpecial;
    private JToggleButton guiSquare;
    // End of variables declaration//GEN-END:variables

    @Override
    public void parse(Expression target) {
        if(target != null){
            if(StyleConstants.STROKE_CAP_BUTT.equals(target)){
                guiSpecial.parse(null);
                guiButt.setSelected(true);
            }else if(StyleConstants.STROKE_CAP_ROUND.equals(target)){
                guiSpecial.parse(null);
                guiRound.setSelected(true);
            }else if(StyleConstants.STROKE_CAP_SQUARE.equals(target)){
                guiSpecial.parse(null);
                guiSquare.setSelected(true);
            }else{
                guiSpecial.parse(target);
                capGroup.clearSelection();
            }
        }else{
            guiSpecial.parse(null);
            guiButt.setSelected(true);
            guiRound.setSelected(false);
            guiSquare.setSelected(false);
        }
    }

    @Override
    public Expression create() {
        final Expression special = guiSpecial.get();
        if(special != null){
            return special;
        }else{
            if(guiRound.isSelected())       return StyleConstants.STROKE_CAP_ROUND;
            else if(guiSquare.isSelected()) return StyleConstants.STROKE_CAP_SQUARE;
            else                            return StyleConstants.STROKE_CAP_BUTT;
        }
    }

}
