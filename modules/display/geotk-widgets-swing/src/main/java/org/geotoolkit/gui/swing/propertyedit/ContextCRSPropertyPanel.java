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
package org.geotoolkit.gui.swing.propertyedit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotoolkit.gui.swing.crschooser.JCRSList;
import org.geotoolkit.gui.swing.misc.LoadingLockableUI;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.io.wkt.UnformattableObjectException;
import org.geotoolkit.util.converter.Classes;
import org.geotoolkit.resources.Vocabulary;
import org.jdesktop.swingx.JXBusyLabel;

import org.opengis.util.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * ContextCRS property panel
 * 
 * @author  Johann Sorel
 * @module pending
 */
public class ContextCRSPropertyPanel extends javax.swing.JPanel implements PropertyPane {

    private MapContext context;
    private JCRSList liste = null;
    private CoordinateReferenceSystem crs = null;

    /** 
     * Creates new form DefaultMapContextCRSEditPanel 
     */
    public ContextCRSPropertyPanel() {
        initComponents();

        final JXBusyLabel lbl = new JXBusyLabel();
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setVerticalAlignment(SwingConstants.CENTER);
        lbl.setHorizontalTextPosition(SwingConstants.CENTER);
        lbl.setBusyPainter(LoadingLockableUI.createDefaultBusyPainter());
        lbl.setBusy(true);
        pan_list.add(BorderLayout.CENTER,lbl);

        new Thread(){
            @Override
            public void run() {
                liste = new JCRSList();

                liste.addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        IdentifiedObject item;
                        try {
                            item = liste.getSelectedItem();
                        } catch (FactoryException ex) {
                            String message = ex.getLocalizedMessage();
                            if (message == null) {
                                message = Classes.getShortClassName(ex);
                            }
                            setErrorMessage(message);
                            return;
                        }
                        setIdentifiedObject(item);
                    }
                });

                lbl.setBusy(false);
                pan_list.removeAll();
                pan_list.add(BorderLayout.CENTER, liste);
                pan_list.revalidate();
                pan_list.repaint();
                if(crs != null){
                    liste.setCRS(crs);
                }
            }
        }.start();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new JTabbedPane();
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        gui_jtf_crs = new JTextField();
        pan_list = new JPanel();
        jPanel2 = new JPanel();
        jScrollPane1 = new JScrollPane();
        wktArea = new JTextArea();

        jLabel1.setText("Coordinate Reference Systems :");

        gui_jtf_crs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                gui_jtf_crsActionPerformed(evt);
            }
        });
        gui_jtf_crs.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                gui_jtf_crsKeyTyped(evt);
            }
        });

        pan_list.setLayout(new BorderLayout());

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(pan_list, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addComponent(gui_jtf_crs, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(21, 21, 21)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(gui_jtf_crs, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(pan_list, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("List", jPanel1);

        wktArea.setColumns(20);
        wktArea.setEditable(false);
        wktArea.setRows(5);
        jScrollPane1.setViewportView(wktArea);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("WKT", jPanel2);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jTabbedPane1, GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jTabbedPane1, GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void gui_jtf_crsActionPerformed(final ActionEvent evt) {//GEN-FIRST:event_gui_jtf_crsActionPerformed
        if(liste!=null)liste.searchCRS(gui_jtf_crs.getText());
    }//GEN-LAST:event_gui_jtf_crsActionPerformed

    private void gui_jtf_crsKeyTyped(final KeyEvent evt) {//GEN-FIRST:event_gui_jtf_crsKeyTyped
        if(liste!=null)liste.searchCRS(gui_jtf_crs.getText());
    }//GEN-LAST:event_gui_jtf_crsKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField gui_jtf_crs;
    private JLabel jLabel1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JTabbedPane jTabbedPane1;
    private JPanel pan_list;
    private JTextArea wktArea;
    // End of variables declaration//GEN-END:variables
    @Override
    public void setTarget(final Object target) {
        context = (MapContext) target;
        init();
    }

    @Override
    public boolean canHandle(Object target) {
        return target instanceof MapContext;
    }
    
    @Override
    public void apply() {
        if(liste!=null)context.setCoordinateReferenceSystem(liste.getCRS());
    }

    @Override
    public String getTitle() {
        return "CRS";
    }

    @Override
    public ImageIcon getIcon() {
        return IconBundle.EMPTY_ICON;
    }

    @Override
    public String getToolTip() {
        return "Projection";
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private void init() {
        String epsg = context.getCoordinateReferenceSystem().getName().toString();
        gui_jtf_crs.setText(epsg);

        if(liste!=null){
            liste.setCRS(context.getCoordinateReferenceSystem());
        }else{
            this.crs = context.getCoordinateReferenceSystem();
        }
        
        setIdentifiedObject(context.getCoordinateReferenceSystem());
    }
    
    @Override
    public void reset() {
        init();
    }
    
    private void setIdentifiedObject(final IdentifiedObject item) {
        String text;
        try {
            text = item.toWKT();
        } catch (UnsupportedOperationException e) {
            text = e.getLocalizedMessage();
            if (text == null) {
                text = Classes.getShortClassName(e);
            }
            final String lineSeparator = System.getProperty("line.separator", "\n");
            if (e instanceof UnformattableObjectException) {
                text = Vocabulary.format(Vocabulary.Keys.WARNING) + ": " + text +
                        lineSeparator + lineSeparator + item + lineSeparator;
            } else {
                text = Vocabulary.format(Vocabulary.Keys.ERROR) + ": " + text + lineSeparator;
            }
        }
        wktArea.setText(text);
    }

    /**
     * Sets an error message to display instead of the current identified object.
     *
     * @param message The error message.
     */
    private void setErrorMessage(final String message) {
        wktArea.setText(Vocabulary.format(Vocabulary.Keys.ERROR_$1, message));
    }
    
}
