/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Johann Sorel
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

import com.vividsolutions.jts.geom.Geometry;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.gui.swing.propertyedit.featureeditor.*;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;
import org.geotoolkit.gui.swing.tree.MutableTreeNode;
import org.geotoolkit.util.Converters;
import org.geotoolkit.util.converter.Classes;
import org.jdesktop.swingx.table.DatePickerCellEditor;
import org.netbeans.swing.outline.*;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Property editor, can edit Feature/Complex attribut or single properties.
 * Additionaly Parameter can be edited since their model is close.
 * 
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class JFeatureOutLine extends Outline{
    
    public static interface PropertyEditor{
        
        boolean canHandle(PropertyType candidate);
        
        TableCellEditor getEditor(PropertyType type);
        
        TableCellRenderer getRenderer(PropertyType type);
    }
    
    private static final ImageIcon ICON_ADD = IconBundle.getIcon("16_smallgray");
    private static final ImageIcon ICON_REMOVE = IconBundle.getIcon("16_smallgreen");
    private static final ImageIcon ICON_OCC_ADD = IconBundle.getIcon("16_occurence_add");
    private static final ImageIcon ICON_OCC_REMOVE = IconBundle.getIcon("16_occurence_remove");

    private final List<JFeatureOutLine.PropertyEditor> editors = new CopyOnWriteArrayList<JFeatureOutLine.PropertyEditor>();
    private final JFeatureOutLine.PropertyRowModel rowModel = new JFeatureOutLine.PropertyRowModel();
    private FeatureTreeModel treeModel = null;
    private Property edited = null;
    
    public JFeatureOutLine(){
        setRenderDataProvider(new JFeatureOutLine.PropertyDataProvider());
        setShowHorizontalLines(false);
        setColumnSelectionAllowed(false);
        setFillsViewportHeight(true);
        setBackground(Color.WHITE);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        editors.addAll(createDefaultEditorList());
    }
    
    /**
     * Set the property to display in this component.
     */
    public void setEdited(final Property property){
        this.edited = property;
        treeModel = new FeatureTreeModel(property);
        setModel(DefaultOutlineModel.createOutlineModel(treeModel, rowModel));
        setRootVisible(!(property instanceof ComplexAttribute));
        getColumnModel().getColumn(0).setMinWidth(100);
        getColumnModel().getColumn(2).setMaxWidth(26);
    }
    
    /**
     * Get the property displayed in this component.
     */
    public Property getEdited(){
        return edited;
    }
    
    /**
     * Set the property to display in this component. Parameters are not the
     * natural model expected, but since Parameters are close to Features. An
     * automatic translation is done.
     */
    public void setEdited(final ParameterValueGroup parameter) {
        setEdited(FeatureUtilities.toFeature(parameter));
    }

    /**
     * Return the edited property as a Parameter
     *
     * @param desc parameter descriptor
     * @return ParameterValueGroup
     */
    public ParameterValueGroup getEditedAsParameter(final ParameterDescriptorGroup desc) {
        return FeatureUtilities.toParameter((ComplexAttribute) edited, desc);
    }
    
    /**
     * @return live list of property editors.
     */
    public List<JFeatureOutLine.PropertyEditor> getEditors() {
        return editors;
    }

    @Override
    public TableCellEditor getCellEditor(final int row, final int column) {
        if(column == 2){
            return new JFeatureOutLine.ActionCellEditor();
        }

        final MutableTreeNode node = (MutableTreeNode) getValueAt(row, 0);
        PropertyType type = null;
        final Object obj = node.getUserObject();
        if(obj instanceof Property){
            type = ((Property)obj).getType();
        }
        
        if(column == 1){
            final JFeatureOutLine.PropertyEditor edit = getEditor(type);
            if(edit != null){
                return edit.getEditor(type);
            }
        }
        
        //fallback on default java editor.
        final Class c = type.getBinding();
        return getDefaultEditor(c);
    }
    
    private JFeatureOutLine.PropertyEditor getEditor(PropertyType type){
        if(type != null){
            for(JFeatureOutLine.PropertyEditor edit : editors){
                if(edit.canHandle(type)){
                    return edit;
                }
            }
        }
        return null;
    }

    @Override
    public TableCellEditor getDefaultEditor(final Class<?> columnClass) {
        if(columnClass != null && Date.class.isAssignableFrom(columnClass)){
            return new DatePickerCellEditor();
        }
        return super.getDefaultEditor(columnClass);
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        if(column == 2){
            return new JFeatureOutLine.ActionCellRenderer();
        }

        final MutableTreeNode node = (MutableTreeNode) getValueAt(row, 0);
        PropertyType type = null;
        final Object obj = node.getUserObject();
        if(obj instanceof Property){
            type = ((Property)obj).getType();
        }
        
        if(column == 1 && type != null){
            final JFeatureOutLine.PropertyEditor edit = getEditor(type);
            if(edit != null){
                return edit.getRenderer(type);
            }
        }
        
        //fallback on default java editor.        
        final Object value = getValueAt(row, column);
        if(value instanceof Geometry || value instanceof org.opengis.geometry.Geometry){
            return new JFeatureOutLine.GeometryCellRenderer();
        }
        return super.getCellRenderer(row, column);
    }
    
    private class PropertyRowModel implements RowModel{
        
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueFor(final Object o, final int i) {
            final MutableTreeNode node = (MutableTreeNode) o;
            final Object candidate = node.getUserObject();

            if(i==0){
                //first column, property value
                if(candidate instanceof Property && !(candidate instanceof ComplexAttribute)){
                    return ((Property)candidate).getValue();
                }else{
                    return null;
                }
            }else{
                //second column, actions
                return node;
            }

        }

        @Override
        public Class getColumnClass(final int i) {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(final Object o, final int i) {
            if(i==1) return true;

            //property value, check it's editable
            final MutableTreeNode node = (MutableTreeNode) o;

            if(node.getUserObject() instanceof Property){
                final Property prop = (Property) node.getUserObject();
                final Class type = prop.getType().getBinding();
                return !(prop instanceof ComplexAttribute)
                      && getEditor(prop.getType()) != null || getDefaultEditor(type) != getDefaultEditor(Object.class);
            }else{
                return false;
            }
        }

        @Override
        public void setValueFor(final Object o, final int i, final Object value) {
            if(i==1)return; //action column

            final MutableTreeNode node = (MutableTreeNode) o;
            final Property prop = (Property) node.getUserObject();
            prop.setValue(Converters.convert(value, prop.getType().getBinding()));
        }

        @Override
        public String getColumnName(final int i) {
            return "";
        }
        
    }
    
    private static class PropertyDataProvider implements RenderDataProvider {

        @Override
        public java.awt.Color getBackground(final Object o) {
            return null;
        }

        @Override
        public String getDisplayName(final Object o) {
            final MutableTreeNode node = (MutableTreeNode) o;
            final Object candidate = node.getUserObject();

            

            final Name name;
            if(candidate instanceof Property){
                name = ((Property)candidate).getName();
            }else if(candidate instanceof PropertyDescriptor){
                name = ((PropertyDescriptor)candidate).getName();
            }else{
                name = null;
            }

            String text;
            if(name != null){
                text = name.getLocalPart();
            }else{
                text = "";
            }

            final StringBuilder sb = new StringBuilder();

            if(candidate instanceof Property){
                PropertyDescriptor desc = ((Property)candidate).getDescriptor();
                if(desc != null &&desc.getMaxOccurs() > 1){
                    //we have to find this property index
                    final int index = node.getParent().getIndex(node);
                    text = "["+index+"] "+text;
                }
            }

            if(candidate instanceof ComplexAttribute){
                sb.append("<b>");
                sb.append(text);
                sb.append("</b>");
            }else if(candidate instanceof PropertyDescriptor){
                final PropertyDescriptor desc = (PropertyDescriptor) candidate;

                sb.append("<i>");
                if(desc.getMaxOccurs() > 1){
                    sb.append("[~] ");
                }
                sb.append(text);
                sb.append("</i>");
            }else{
                sb.append(text);
            }

            return sb.toString();
        }

        @Override
        public java.awt.Color getForeground(final Object o) {
            final MutableTreeNode node = (MutableTreeNode) o;
            final Object candidate = node.getUserObject();

            if(candidate instanceof PropertyDescriptor){
                final PropertyDescriptor desc = (PropertyDescriptor) candidate;
                final int nb = node.getChildCount();

                if(nb == 0){
                    return Color.LIGHT_GRAY;
                }

            }

            return null;
        }
        
        @Override
        public javax.swing.Icon getIcon(final Object o) {
            final MutableTreeNode node = (MutableTreeNode) o;
            final Object prop = node.getUserObject();
            if(prop instanceof ComplexAttribute){
                return IconBundle.EMPTY_ICON;
            }else{
                return IconBundle.EMPTY_ICON;
            }            
        }

        @Override
        public String getTooltipText(final Object o) {
            final MutableTreeNode node = (MutableTreeNode) o;
            final Object userObject = node.getUserObject();

            if(userObject instanceof Property){
                String tooltip = DefaultName.toJCRExtendedForm( ((Property) node.getUserObject()).getName());
                tooltip += " ("+Classes.getShortName(((Property) node.getUserObject()).getType().getBinding()) +")";
                return tooltip;
            }else{
                return null;
            }
            
        }

        @Override
        public boolean isHtmlDisplayName(final Object o) {
            return true;
        }
    }

    private final class GeometryCellRenderer extends DefaultOutlineCellRenderer{

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final JLabel lbl = (JLabel) super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            if(value instanceof Geometry || value instanceof org.opengis.geometry.Geometry){
                lbl.setText("~");
            }
            return lbl;
        }

    }

    private final class ActionCellRenderer extends DefaultOutlineCellRenderer {
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            return getActionComponent((JFeatureOutLine)table,value);
        }
    }

    private final class ActionCellEditor extends AbstractCellEditor implements TableCellEditor{
        @Override
        public Component getTableCellEditorComponent(final JTable table, final Object value,
                final boolean isSelected, final int row, final int column) {
            Component comp = getActionComponent((JFeatureOutLine)table, value);
            if(comp instanceof JButton){
                ((JButton)comp).addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fireEditingStopped();
                    }
                });
            }

            return comp;
        }
        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    public Component getActionComponent(final JFeatureOutLine outline, final Object value) {
        
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        
        if(node == null){
            return new JLabel();
        }

        final TreePath path = new TreePath(node.getPath());
        final Object obj = node.getUserObject();

        if(obj instanceof PropertyDescriptor){
            final PropertyDescriptor desc = (PropertyDescriptor) obj;
            final int nbProp = node.getChildCount();

            if(desc.getMaxOccurs() > nbProp){
                final int max = desc.getMaxOccurs();
                final ImageIcon icon = (max>1) ? ICON_OCC_ADD : ICON_ADD;
                final JButton butAdd = new JButton(icon);
                butAdd.setBorderPainted(false);
                butAdd.setContentAreaFilled(false);
                butAdd.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        treeModel.createProperty(path);
                    }
                });
                return butAdd;
            }

        }else if(obj instanceof Property){
            final Property prop = (Property) obj;
            final PropertyDescriptor desc = prop.getDescriptor();

            if(desc != null && desc.getMinOccurs() == 0){                
                final int max = desc.getMaxOccurs();
                final ImageIcon icon = (max>1) ? ICON_OCC_REMOVE : ICON_REMOVE;
                final JButton butRemove = new JButton(icon);
                butRemove.setBorderPainted(false);
                butRemove.setContentAreaFilled(false);
                butRemove.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        treeModel.removeProperty(path);
                    }
                });
                return butRemove;
            }
        }

        return new JLabel();
    }

    public static List<JFeatureOutLine.PropertyEditor> createDefaultEditorList(){
        final List<JFeatureOutLine.PropertyEditor> lst = new ArrayList<JFeatureOutLine.PropertyEditor>();
        lst.add(new BooleanEditor());
        lst.add(new CRSEditor());
        lst.add(new CharsetEditor());
        lst.add(new NumberEditor());
        lst.add(new StringEditor());
        lst.add(new URLEditor());
        lst.add(new FileEditor());
        lst.add(new UnitEditor());
        lst.add(new EnumEditor());
        return lst;
    }
    
    public static void show(final Property candidate){
        final JDialog dialog = new JDialog();
        final JFeatureOutLine outline = new JFeatureOutLine();
        outline.setEdited(candidate);
        dialog.setContentPane(new JScrollPane(outline));
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}
