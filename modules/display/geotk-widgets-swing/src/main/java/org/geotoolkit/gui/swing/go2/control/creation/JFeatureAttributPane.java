/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Johann Sorel
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

package org.geotoolkit.gui.swing.go2.control.creation;

import com.vividsolutions.jts.geom.Geometry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/**
 *
 * @author Johann Sorel
 * @module pending
 */
public class JFeatureAttributPane<T extends SimpleFeature> extends javax.swing.JPanel {

    private final T feature;

    /** Creates new form JFeatureAttributPane */
    public JFeatureAttributPane(final T feature) {
        this.feature = feature;
        initComponents();

        tab_data.setModel(new FeatureSourceModel((List<SimpleFeature>)Collections.singletonList(feature)));
        tab_data.setEditable(true);
        tab_data.setColumnControlVisible(false);
        tab_data.setHorizontalScrollEnabled(true);
        tab_data.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);

        tab_data.setHighlighters(new Highlighter[]{HighlighterFactory.createAlternateStriping(Color.white, HighlighterFactory.QUICKSILVER, 5)});
        tab_data.setShowGrid(true, true);
        tab_data.setGridColor(Color.GRAY.brighter());

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                tab_data.packAll();
            }
        });

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
        tab_data = new org.jdesktop.swingx.JXTable();

        tab_data.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tab_data);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTable tab_data;
    // End of variables declaration//GEN-END:variables

    private T getFeature() {
        return feature;
    }

    public static <S extends SimpleFeature> S configure(final S feature){

        final JDialog dialog = new JDialog();
        dialog.setModal(true);

        final JPanel pan = new JPanel(new BorderLayout());
        final JFeatureAttributPane<S> edit = new JFeatureAttributPane<S>(feature);
        final JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new JButton(new AbstractAction("Ok") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        }));

        pan.add(BorderLayout.CENTER,edit);
        pan.add(BorderLayout.SOUTH,bottom);

        dialog.setContentPane(pan);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);

        return edit.getFeature();
    }

    public class FeatureSourceModel extends DefaultTableModel {

        private final ArrayList<AttributeDescriptor> columns = new ArrayList<AttributeDescriptor>();
        private final List<SimpleFeature> features;

        /** Creates a new instance of BasicTableModel
         * @param tab
         * @param layer
         */
        public FeatureSourceModel(final List<SimpleFeature> layers) {
            super();
            this.features = layers;
            init();
        }

        public void init() {
            columns.clear();

            SimpleFeatureType ft = features.get(0).getType();

            for(AttributeDescriptor desc : ft.getAttributeDescriptors()){
                if(!Geometry.class.isAssignableFrom(desc.getType().getBinding())){
                    columns.add(desc);
                }
            }

        }


        @Override
        public int getColumnCount() {
            return columns.size()+1;
        }

        @Override
        public Class getColumnClass(final int column) {
            if(column == 0) return String.class;
            return columns.get(column-1).getType().getBinding();
        }

        @Override
        public String getColumnName(final int column) {
            if(column == 0) return "id";
            return columns.get(column-1).getName().toString();
        }

        @Override
        public int getRowCount() {
            if(features != null){
                return features.size();
            }else{
                return 0;
            }
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return columnIndex != 0;
        }

        public Feature getFeatureAt(final int rowIndex){
            return features.get(rowIndex);
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            if(columnIndex == 0) return "-";
            return features.get(rowIndex).getAttribute(columns.get(columnIndex-1).getLocalName());
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            if(columnIndex == 0) return;
            features.get(rowIndex).setAttribute(columns.get(columnIndex-1).getLocalName(), aValue);
        }

    }


}
