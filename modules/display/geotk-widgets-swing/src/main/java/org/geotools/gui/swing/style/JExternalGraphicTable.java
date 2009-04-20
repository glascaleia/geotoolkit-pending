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

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.geotools.gui.swing.resource.IconBundle;
import org.geotoolkit.map.MapLayer;

import org.opengis.style.ExternalGraphic;

/**
 * External graphic panel
 * 
 * @author Johann Sorel
 */
public class JExternalGraphicTable extends StyleElementEditor<ExternalGraphic[]> {

    private static final Icon ICO_UP = IconBundle.getInstance().getIcon("16_uparrow");
    private static final Icon ICO_DOWN = IconBundle.getInstance().getIcon("16_downarrow");
    private static final Icon ICO_NEW = IconBundle.getInstance().getIcon("16_add_data");
    private static final Icon ICO_DELETE = IconBundle.getInstance().getIcon("16_delete");
    
    private MapLayer layer = null;
    private final ExternalGraphicModel model = new ExternalGraphicModel(new ExternalGraphic[]{});
    private final ExternalGraphicEditor editor = new ExternalGraphicEditor();

    /** Creates new form JFontsPanel */
    public JExternalGraphicTable() {
        initComponents();
        init();
    }

    private void init() {
        tabMarks.setTableHeader(null);
        tabMarks.setModel(model);
        tabMarks.getColumnModel().getColumn(0).setCellEditor(editor);
        tabMarks.setDefaultRenderer(ExternalGraphic.class, new ExternalGraphicRenderer());
        tabMarks.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setLayer(MapLayer layer) {
        editor.setLayer(layer);
        this.layer = layer;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MapLayer getLayer() {
        return layer;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void parse(ExternalGraphic[] externals) {
        model.setExternalGraphics(externals);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ExternalGraphic[] create() {
        return model.getExternalGraphics();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new JScrollPane();
        tabMarks = new JTable();
        guiUp = new JButton();
        guiDown = new JButton();
        guiNew = new JButton();
        guiDelete = new JButton();

        setOpaque(false);

        jScrollPane1.setViewportView(tabMarks);

        guiUp.setIcon(ICO_UP);
        guiUp.setBorderPainted(false);
        guiUp.setContentAreaFilled(false);
        guiUp.setMargin(new Insets(2, 2, 2, 2));
        guiUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiUpActionPerformed(evt);
            }
        });

        guiDown.setIcon(ICO_DOWN);
        guiDown.setBorderPainted(false);
        guiDown.setContentAreaFilled(false);
        guiDown.setMargin(new Insets(2, 2, 2, 2));
        guiDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiDownActionPerformed(evt);
            }
        });

        guiNew.setIcon(ICO_NEW);
        guiNew.setBorderPainted(false);
        guiNew.setContentAreaFilled(false);
        guiNew.setMargin(new Insets(2, 2, 2, 2));
        guiNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiNewActionPerformed(evt);
            }
        });

        guiDelete.setIcon(ICO_DELETE);
        guiDelete.setBorderPainted(false);
        guiDelete.setContentAreaFilled(false);
        guiDelete.setMargin(new Insets(2, 2, 2, 2));
        guiDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiDeleteActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(guiNew)
                    .addComponent(guiUp)
                    .addComponent(guiDown)
                    .addComponent(guiDelete)))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {guiDelete, guiDown, guiNew, guiUp});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(guiUp)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiDown)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiNew)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiDelete)
                .addContainerGap(41, Short.MAX_VALUE))
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void guiUpActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiUpActionPerformed
        int index = tabMarks.getSelectionModel().getMinSelectionIndex();

        if (index >= 0) {
            ExternalGraphic e = (ExternalGraphic) model.getValueAt(index, 0);
            model.moveUp(e);
        }
}//GEN-LAST:event_guiUpActionPerformed

    private void guiDownActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiDownActionPerformed
        int index = tabMarks.getSelectionModel().getMinSelectionIndex();

        if (index >= 0) {
            ExternalGraphic e = (ExternalGraphic) model.getValueAt(index, 0);
            model.moveDown(e);
        }
}//GEN-LAST:event_guiDownActionPerformed

    private void guiNewActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiNewActionPerformed
        model.newExternalGraphic();
}//GEN-LAST:event_guiNewActionPerformed

    private void guiDeleteActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiDeleteActionPerformed
        int index = tabMarks.getSelectionModel().getMinSelectionIndex();

        if (index >= 0) {
            model.deleteExternalGraphic(index);
        }
    }//GEN-LAST:event_guiDeleteActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton guiDelete;
    private JButton guiDown;
    private JButton guiNew;
    private JButton guiUp;
    private JScrollPane jScrollPane1;
    private JTable tabMarks;
    // End of variables declaration//GEN-END:variables

    class ExternalGraphicModel extends AbstractTableModel {

        private List<ExternalGraphic> externals = new ArrayList<ExternalGraphic>();

        ExternalGraphicModel(ExternalGraphic[] externals) {
            for (ExternalGraphic m : externals) {
                this.externals.add(m);
            }
        }

        public void newExternalGraphic() {
            ExternalGraphic m = getStyleFactory().externalGraphic("", "");

            externals.add(m);
            int last = externals.size() - 1;
            fireTableRowsInserted(last, last);
        }

        public void deleteExternalGraphic(int index) {
            externals.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public void moveUp(ExternalGraphic m) {
            int index = externals.indexOf(m);
            if (index != 0) {
                externals.remove(m);
                externals.add(index - 1, m);
                fireTableDataChanged();
            }
        }

        public void moveDown(ExternalGraphic m) {
            int index = externals.indexOf(m);
            if (index != externals.size() - 1) {
                externals.remove(m);
                externals.add(index + 1, m);
                fireTableDataChanged();
            }
        }

        public void setExternalGraphics(ExternalGraphic[] externals) {
            this.externals.clear();

            if (externals != null) {
                for (ExternalGraphic m : externals) {
                    this.externals.add(m);
                }
            }
            fireTableDataChanged();
        }

        public ExternalGraphic[] getExternalGraphics() {
            return externals.toArray(new ExternalGraphic[externals.size()]);
        }

        @Override
        public int getRowCount() {
            return externals.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return ExternalGraphic.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return externals.get(rowIndex);
        }
    }

    class ExternalGraphicRenderer extends DefaultTableCellRenderer {

        private String text = "SsIiGg84";

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);

            ExternalGraphic m = (ExternalGraphic) value;
            lbl.setText(m.getFormat());
            return lbl;
        }
    }

    class ExternalGraphicEditor extends AbstractCellEditor implements TableCellEditor {//implements TableCellEditor{

        private MapLayer layer = null;
        private JExternalGraphicPane editpane = new JExternalGraphicPane();
        private JButton but = new JButton("SsIiGg84");
        private ExternalGraphic mark = null;

        public ExternalGraphicEditor() {
            super();
            but.setBorderPainted(false);

            but.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (mark != null) {
                        JDialog dia = new JDialog();

                        //panneau d'edition           
                        editpane.parse(mark);

                        dia.setContentPane(editpane);
                        dia.setLocationRelativeTo(but);
                        dia.pack();
                        dia.setModal(true);
                        dia.setVisible(true);

                        mark = editpane.create();
                    }
                }
            });
        }

        public void setLayer(MapLayer layer) {
            this.layer = layer;
        }

        public MapLayer getLayer() {
            return layer;
        }

        @Override
        public Object getCellEditorValue() {
            return mark;
        }

//    public boolean isCellEditable(EventObject e) {
//        return true;
//    }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

            if (value != null && value instanceof ExternalGraphic) {
                mark = (ExternalGraphic) value;
                but.setText(mark.getFormat());
            } else {
                but.setText("????");
                mark = null;
            }
            return but;
        }//    public boolean shouldSelectCell(EventObject anEvent) {
//        return true;
//    }
//
//    public boolean stopCellEditing() {
//        return true;
//    }
//
//    public void cancelCellEditing() {
//    }
//
//    public void addCellEditorListener(CellEditorListener l) {        
//    }
//    
//    public void removeCellEditorListener(CellEditorListener l) {
//    }
    }

}
