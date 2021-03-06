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

import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.gui.swing.style.JTextExpressionPane;
import org.geotoolkit.util.SimpleInternationalString;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapLayer;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

/**
 * layer general information panel
 * 
 * @author Johann Sorel
 * @module pending
 */
public class LayerGeneralPanel extends javax.swing.JPanel implements PropertyPane {

    private MapLayer layer = null;
    private final String title;

    /** Creates new form LayerGeneralPanel */
    public LayerGeneralPanel() {
        initComponents();
        title = MessageBundle.getString("property_general_title");        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {





        jLabel15 = new JLabel();
        gui_jtf_name = new JTextField();
        paneTemp = new JPanel();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        guiStartTemp = new JTextExpressionPane();
        guiEndTemp = new JTextExpressionPane();
        paneTemp1 = new JPanel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        guiStartElev = new JTextExpressionPane();
        guiEndElev = new JTextExpressionPane();

        jLabel15.setFont(jLabel15.getFont().deriveFont(jLabel15.getFont().getStyle() | Font.BOLD));
        jLabel15.setText(MessageBundle.getString("property_title")); // NOI18N
        paneTemp.setBorder(BorderFactory.createTitledBorder(MessageBundle.getString("temporal_configuration"))); // NOI18N
        jLabel1.setText(MessageBundle.getString("temporal_start")); // NOI18N
        jLabel2.setText(MessageBundle.getString("temporal_end")); // NOI18N
        GroupLayout paneTempLayout = new GroupLayout(paneTemp);
        paneTemp.setLayout(paneTempLayout);



        paneTempLayout.setHorizontalGroup(
            paneTempLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(paneTempLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paneTempLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(paneTempLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(guiStartTemp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(paneTempLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(guiEndTemp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        paneTempLayout.setVerticalGroup(
            paneTempLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(paneTempLayout.createSequentialGroup()
                .addGroup(paneTempLayout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guiStartTemp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(paneTempLayout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guiEndTemp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        paneTemp1.setBorder(BorderFactory.createTitledBorder(MessageBundle.getString("elevation_configuration"))); // NOI18N
        jLabel3.setText(MessageBundle.getString("temporal_start")); // NOI18N
        jLabel4.setText(MessageBundle.getString("temporal_end")); // NOI18N
        GroupLayout paneTemp1Layout = new GroupLayout(paneTemp1);
        paneTemp1.setLayout(paneTemp1Layout);
        paneTemp1Layout.setHorizontalGroup(
            paneTemp1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(paneTemp1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paneTemp1Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(paneTemp1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(guiStartElev, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(paneTemp1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(guiEndElev, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        paneTemp1Layout.setVerticalGroup(
            paneTemp1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(paneTemp1Layout.createSequentialGroup()
                .addGroup(paneTemp1Layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guiStartElev, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(paneTemp1Layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(jLabel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guiEndElev, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(gui_jtf_name, GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(paneTemp1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(paneTemp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(gui_jtf_name, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(paneTemp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(paneTemp1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void parse() {
        guiStartTemp.setLayer(layer);
        guiEndTemp.setLayer(layer);
        guiStartElev.setLayer(layer);
        guiEndElev.setLayer(layer);

        if (layer != null) {
            gui_jtf_name.setText(layer.getDescription().getTitle().toString());

        } else {
            gui_jtf_name.setText("");
        }

        paneTemp.setVisible(layer instanceof FeatureMapLayer);

        if(layer instanceof FeatureMapLayer){
            Expression[] temp = ((FeatureMapLayer)layer).getTemporalRange();
            Expression[] elev = ((FeatureMapLayer)layer).getElevationRange();
            guiStartTemp.parse(temp[0]);
            guiEndTemp.parse(temp[1]);
            guiStartElev.parse(elev[0]);
            guiEndElev.parse(elev[1]);
        }else{
            guiStartTemp.parse(null);
            guiEndTemp.parse(null);
            guiStartElev.parse(null);
            guiEndElev.parse(null);
        }

    }

    @Override
    public void setTarget(final Object target) {
        if (target instanceof MapLayer) {
            layer = (MapLayer) target;
        } else {
            layer = null;
        }
        parse();
    }

    @Override
    public void apply() {
        if (layer != null) {
            layer.setDescription(FactoryFinder.getStyleFactory(null).description(
                    new SimpleInternationalString(gui_jtf_name.getText()),
                    new SimpleInternationalString("")));

            if(layer instanceof FeatureMapLayer){
                Expression t1 = guiStartTemp.create();
                Expression t2 = guiEndTemp.create();
                if(t1 instanceof Literal && ((Literal)t1).getValue().equals("") ) t1 = null;
                if(t2 instanceof Literal && ((Literal)t2).getValue().equals("") ) t2 = null;
                ((FeatureMapLayer)layer).setTemporalRange(t1,t2);

                Expression e1 = guiStartElev.create();
                Expression e2 = guiEndElev.create();
                if(e1 instanceof Literal && ((Literal)e1).getValue().equals("") ) e1 = null;
                if(e2 instanceof Literal && ((Literal)e2).getValue().equals("") ) e2 = null;
                ((FeatureMapLayer)layer).setElevationRange(e1,e2);

            }

        }
    }

    @Override
    public void reset() {
        parse();
    }

    @Override
    public boolean canHandle(Object target) {
        return true;
    }
    
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public String getToolTip() {
        return title;
    }

    @Override
    public Component getComponent() {
        return this;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextExpressionPane guiEndElev;
    private JTextExpressionPane guiEndTemp;
    private JTextExpressionPane guiStartElev;
    private JTextExpressionPane guiStartTemp;
    private JTextField gui_jtf_name;
    private JLabel jLabel1;
    private JLabel jLabel15;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JPanel paneTemp;
    private JPanel paneTemp1;
    // End of variables declaration//GEN-END:variables
}
