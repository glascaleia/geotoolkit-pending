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
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotoolkit.map.MapLayer;
import org.opengis.style.ExternalGraphic;

/**
 * External graphic panel
 *
 * @author Johann Sorel
 */
public class JExternalGraphicPane extends StyleElementEditor<ExternalGraphic> {

    private MapLayer layer = null;
    private ExternalGraphic external = null;

    /** Creates new form JDisplacementPanel */
    public JExternalGraphicPane() {
        initComponents();
        init();
    }

    private void init() {
    }

    @Override
    public void setLayer(MapLayer layer) {
        this.layer = layer;
    }

    @Override
    public MapLayer getLayer() {
        return layer;
    }

    @Override
    public void parse(ExternalGraphic ext) {
        this.external = ext;

        if (external != null) {
            //TODO : not handled yet
            //external.getCustomProperties();
            guiMime.setText(external.getFormat());
            guiURL.setText(external.getOnlineResource().toString());
           
        }
    }

    @Override
    public ExternalGraphic create() {

        try {
            external = getStyleFactory().externalGraphic(new URL(guiURL.getText()), guiMime.getText());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return external;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        guiMime = new JTextField();
        guiURL = new JTextField();

        setOpaque(false);



        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel2.setText(MessageBundle.getString("mime")); // NOI18N
        jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel3.setText(MessageBundle.getString("url")); // NOI18N
        guiMime.setOpaque(false);

        guiURL.setOpaque(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiMime, GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiURL, GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel2, jLabel3});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(guiMime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(guiURL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField guiMime;
    private JTextField guiURL;
    private JLabel jLabel2;
    private JLabel jLabel3;
    // End of variables declaration//GEN-END:variables

}
