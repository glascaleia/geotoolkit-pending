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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.geotoolkit.gui.swing.style.StyleElementEditor;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.expression.Expression;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class JNumberExpressionPane extends StyleElementEditor<Expression>{

    /** Creates new form JColorExpressionPane */
    public JNumberExpressionPane() {
        initComponents();
    }

    public void setModel(final double value, final double min, final double max, final double step){
        guiNumber.setModel(new SpinnerNumberModel(value, min, max, step));
    }
    
    public void setModel(final int value, final int min, final int max, final int step){
        guiNumber.setModel(new SpinnerNumberModel(value, min, max, step));
    }
    
    @Override
    public void setLayer(final MapLayer layer) {
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

        guiSpecial = new JSpecialExpressionButton();
        guiNumber = new JSpinner();

        setOpaque(false);

        guiSpecial.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                guiSpecialPropertyChange(evt);
            }
        });

        guiNumber.setModel(new SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));
        guiNumber.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                guiNumberStateChanged(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(guiNumber, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiSpecial, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(guiNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(guiSpecial, GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void guiSpecialPropertyChange(final PropertyChangeEvent evt) {//GEN-FIRST:event_guiSpecialPropertyChange
    if(evt.getPropertyName().equals(JSpecialExpressionButton.EXPRESSION_PROPERTY)) {
        parse(guiSpecial.get());
    }
}//GEN-LAST:event_guiSpecialPropertyChange

private void guiNumberStateChanged(final ChangeEvent evt) {//GEN-FIRST:event_guiNumberStateChanged
    parse(  getFilterFactory().literal( ((SpinnerNumberModel)guiNumber.getModel()).getNumber() ));
}//GEN-LAST:event_guiNumberStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JSpinner guiNumber;
    private JSpecialExpressionButton guiSpecial;
    // End of variables declaration//GEN-END:variables

    @Override
    public void parse(final Expression target) {
        if(target != null){
            if(isStatic(target)){
                final Number value = target.evaluate(null, Number.class);
                if(value != null){
                    guiSpecial.parse(null);
                    guiNumber.setValue(value.doubleValue());
                }else{
                    guiSpecial.parse(target);
                }
            }else{
                guiSpecial.parse(target);
            }
        }else{
            guiSpecial.parse(null);
        }
    }

    @Override
    public Expression create() {
        final Expression special = guiSpecial.get();
        if(special != null){
            return special;
        }else{
            return getFilterFactory().literal( ((SpinnerNumberModel)guiNumber.getModel()).getNumber());
        }
    }
    
}
