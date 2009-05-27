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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.gui.swing.resource.MessageBundle;


/**
 * Property panel
 * 
 * @author Johann Sorel
 */
public class JPropertyDialog extends JDialog{
        
    private JButton apply = new JButton(MessageBundle.getString("property_apply"));
    private JButton revert = new JButton(MessageBundle.getString("property_revert"));
    private JButton close = new JButton(MessageBundle.getString("property_close"));
    
    private JTabbedPane tabs = new JTabbedPane();    
    private PropertyPane activePanel = null;    
    private ArrayList<PropertyPane> panels = new ArrayList<PropertyPane>();
    
    /** Creates a new instance of ASDialog */
    private JPropertyDialog() {
        super();
        setModal(true);
        setTitle(MessageBundle.getString("property_properties"));
        
        JToolBar bas = new JToolBar();
        bas.setFloatable(false);
        bas.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        bas.add(apply);
        bas.add(revert);
        bas.add(close);
        
        apply.setIcon(IconBundle.getInstance().getIcon("16_apply"));
        revert.setIcon(IconBundle.getInstance().getIcon("16_reload"));
        close.setIcon(IconBundle.getInstance().getIcon("16_close"));
        
        
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                activePanel = (PropertyPane)tabs.getSelectedComponent();
            }
        });
        
        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(PropertyPane edit : panels){
                    edit.apply();
                }
            }
        });
        
        revert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(activePanel != null)
                    activePanel.reset();
            }
        });
        
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(PropertyPane edit : panels){
                    edit.apply();
                }
                dispose();
            }
        });
        
        setLayout( new BorderLayout());
        add(BorderLayout.SOUTH,bas);
        
    }
    
    public void addEditPanel(PropertyPane pan){
        panels.add(pan);        
        tabs.addTab(pan.getTitle(),pan.getIcon(),pan.getComponent(),pan.getToolTip());        
    }
    

    @Override
    public void setVisible(boolean b) {
        if(b){
            if(panels.size()>1){
                add(BorderLayout.CENTER,tabs);
            }else if(panels.size() == 1){
                add(BorderLayout.CENTER,(JComponent)panels.get(0));
            }
        }      
        super.setVisible(b);
    }
    
    public static void showDialog(List<PropertyPane> lst, Object target){
        JPropertyDialog dia = new JPropertyDialog();
        
        for(PropertyPane pro : lst){
            pro.setTarget(target);
            dia.addEditPanel(pro);
        }
        
        dia.setSize(700,500);
        dia.setLocationRelativeTo(null);
        dia.setVisible(true);
    }
   
    
}
