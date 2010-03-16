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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.display2d.service.DefaultGlyphService;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.util.RandomStyleFactory;
import org.jdesktop.swingx.JXTree;

import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class JStyleTree<T> extends JXTree implements DragGestureListener, DragSourceListener, DropTargetListener {

    private static final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(null);

    private static final Icon ICON_STYLE = IconBundle.getInstance().getIcon("16_style");
    private static final Icon ICON_FTS = IconBundle.getInstance().getIcon("16_style_fts");
    private static final Icon ICON_RULE = IconBundle.getInstance().getIcon("16_style_rule");
    private static final Icon ICON_NEW = IconBundle.getInstance().getIcon("16_add_data");
    private static final Icon ICON_DUPLICATE = IconBundle.getInstance().getIcon("16_duplicate");
    private static final Icon ICON_DELETE = IconBundle.getInstance().getIcon("16_delete");
    
    private T style = null;
    private StyleTreeModel<T> treemodel = null;
    /** Variables needed for DnD */
    private DragSource dragSource = null;

    public JStyleTree() {
        super();        
        setModel(treemodel);
        setEditable(false);

        setCellRenderer(new StyleCellRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setComponentPopupMenu(new StylePopup(this));

        dragSource = DragSource.getDefaultDragSource();
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_COPY_OR_MOVE, this);
        dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
        DropTarget dropTarget = new DropTarget(this, this);
    }

    public T getStyleElement() {
        return style;
    }

    public void setStyleElement(T style) {

        this.style = style;

        if (style != null) {
            treemodel = new StyleTreeModel(style);
            setModel(treemodel);
            revalidate();
        }
        expandAll();
    }

    //-------------Drag & drop -------------------------------------------------
    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
        final TreePath path = getSelectionModel().getSelectionPath();
        final DefaultMutableTreeNode dragNode = (DefaultMutableTreeNode) path.getLastPathComponent();

        if (dragNode != null) {
            final Transferable transferable = new StringSelection("");
            e.startDrag(null, transferable);
        }
    }

    //--------------------drag events-------------------------------------------
    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    //--------------------drop events-------------------------------------------
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        final TreePath originPath = getSelectionModel().getSelectionPath();
        final Point loc = dtde.getLocation();
        final TreePath targetPath = getPathForLocation(loc.x, loc.y);

        if (targetPath != null && originPath != null) {
            final DefaultMutableTreeNode dragNode = (DefaultMutableTreeNode) originPath.getLastPathComponent();
            final DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) targetPath.getLastPathComponent();

            treemodel.moveNode(dragNode, targetNode);

            final DefaultMutableTreeNode dragNodeParent = (DefaultMutableTreeNode) dragNode.getParent();
            final DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) dragNode.getParent();
            final Object parentObj = targetNode.getUserObject();
            final Transferable trans = dtde.getTransferable();

            setStyleElement(style);
        }

    }

    //-------------private classes----------------------------------------------
    class StyleCellRenderer extends DefaultTreeCellRenderer {
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            final Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (comp instanceof JLabel) {
                final JLabel lbl = (JLabel) comp;
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                final Object val = node.getUserObject();

                if (val instanceof MutableStyle) {
                    final MutableStyle style = (MutableStyle) val;
                    lbl.setText(style.getDescription().getTitle().toString());
                    lbl.setIcon(ICON_STYLE);
                } else if (val instanceof MutableFeatureTypeStyle) {
                    final MutableFeatureTypeStyle fts = (MutableFeatureTypeStyle) val;
                    lbl.setText(fts.getDescription().getTitle().toString());
                    lbl.setIcon(ICON_FTS);
                } else if (val instanceof MutableRule) {
                    final MutableRule r = (MutableRule) val;
                    lbl.setText(r.getDescription().getTitle().toString());
                    lbl.setIcon(ICON_RULE);
                } else if (val instanceof Symbolizer) {
                    final Symbolizer symb = (Symbolizer) val;
                    final BufferedImage img = new BufferedImage(30, 22, BufferedImage.TYPE_INT_ARGB);
                    DefaultGlyphService.render(symb, new Rectangle(30,22),img.createGraphics(),null);
                    final Icon ico = new ImageIcon(img);
                    lbl.setText("");
                    lbl.setIcon(ico);
                }
            }
            return comp;
        }
    }

    class StylePopup extends JPopupMenu {

        private final JTree tree;

        StylePopup(JTree tree) {
            super();
            this.tree = tree;
        }

        @Override
        public void setVisible(boolean visible) {
            final TreePath path = tree.getSelectionModel().getSelectionPath();

            if (path != null && visible) {
                removeAll();

                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                final Object val = node.getUserObject();

                if (val instanceof MutableStyle) {
                    final MutableStyle style = (MutableStyle) val;
                    add(new NewFTSItem(style));
                    add(new JSeparator(SwingConstants.HORIZONTAL));
                    add(new ExpandSubNodes(node));
                    add(new CollapseSubNodes(node));
                } else if (val instanceof MutableFeatureTypeStyle) {
                    final MutableFeatureTypeStyle fts = (MutableFeatureTypeStyle) val;
                    add(new NewRuleItem(fts));
                    add(new JSeparator(SwingConstants.HORIZONTAL));
                    add(new ExpandSubNodes(node));
                    add(new CollapseSubNodes(node));
                    add(new ChangeRuleScaleNodes(fts));
                    add(new JSeparator(SwingConstants.HORIZONTAL));
                    add(new DuplicateItem(node));
                } else if (val instanceof MutableRule) {
                    final MutableRule rule = (MutableRule) val;
                    add(new NewPointSymbolizerItem(rule));
                    add(new NewLineSymbolizerItem(rule));
                    add(new NewPolygonSymbolizerItem(rule));
                    add(new NewRasterSymbolizerItem(rule));
                    add(new NewTextSymbolizerItem(rule));
                    add(new JSeparator(SwingConstants.HORIZONTAL));
                    add(new ExpandSubNodes(node));
                    add(new CollapseSubNodes(node));
                    add(new JSeparator(SwingConstants.HORIZONTAL));
                    add(new DuplicateItem(node));
                } else if (val instanceof Symbolizer) {
                    final Symbolizer symb = (Symbolizer) val;
                    add(new DuplicateItem(node));
                }
                                
                if(treemodel.isDeletable(node)){
                    add(new JSeparator(SwingConstants.HORIZONTAL));
                    add(new DeleteItem(node));
                }
                
            }

            super.setVisible(visible);
        }
    }

    class CollapseSubNodes extends JMenuItem{

        private final DefaultMutableTreeNode parentNode;

        CollapseSubNodes(DefaultMutableTreeNode node) {
            this.parentNode = node;
            setText("Collapse sub nodes.");
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(int i=0,n=parentNode.getChildCount(); i<n; i++){
                        collapsePath(new TreePath(treemodel.getPathToRoot(parentNode.getChildAt(i))));
                    }
                }
            });
        }
    }

    class ExpandSubNodes extends JMenuItem{

        private final DefaultMutableTreeNode parentNode;

        ExpandSubNodes(DefaultMutableTreeNode node) {
            this.parentNode = node;
            setText("Expand sub nodes.");
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(int i=0,n=parentNode.getChildCount(); i<n; i++){
                        TreeNode child = parentNode.getChildAt(i);
                        for(int k=0,l=child.getChildCount(); k<l; k++){
                            expandPath(new TreePath(treemodel.getPathToRoot(child.getChildAt(k))));
                        }
                    }
                }
            });
        }
    }


    class ChangeRuleScaleNodes extends JMenuItem{

        private final MutableFeatureTypeStyle fts;

        ChangeRuleScaleNodes(MutableFeatureTypeStyle cdt) {
            this.fts = cdt;
            setText("Change rules valid scale.");
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JPanel pan = new JPanel();
                    pan.add(new JLabel(" Min scale : "));
                    JSpinner spiMin = new JSpinner(new SpinnerNumberModel());
                    spiMin.setPreferredSize(new Dimension(150, spiMin.getPreferredSize().height));
                    pan.add(spiMin);
                    pan.add(new JLabel(" Max scale : "));
                    JSpinner spiMax = new JSpinner(new SpinnerNumberModel());
                    spiMax.setPreferredSize(new Dimension(150, spiMax.getPreferredSize().height));
                    spiMax.setValue(Double.MAX_VALUE);
                    pan.add(spiMax);

                    JOptionPane jop = new JOptionPane(pan);
                    JDialog dialog = jop.createDialog("Change scale");
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);

                    double min = ((Number)spiMin.getValue()).doubleValue();
                    double max = ((Number)spiMax.getValue()).doubleValue();

                    for(MutableRule rule : fts.rules()){
                        rule.setMinScaleDenominator(min);
                        rule.setMaxScaleDenominator(max);
                    }
                }
            });
        }
    }

    class NewFTSItem extends JMenuItem {

        private final MutableStyle style;

        NewFTSItem(MutableStyle cdt) {
            this.style = cdt;
            setText("new FTS");
            setIcon(ICON_NEW);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    style.featureTypeStyles().add(SF.featureTypeStyle(RandomStyleFactory.createPointSymbolizer()));
                }
            });
        }
    }

    class NewRuleItem extends JMenuItem {

        private final MutableFeatureTypeStyle fts;

        NewRuleItem(MutableFeatureTypeStyle cdt) {
            this.fts = cdt;
            setText("New Rule");
            setIcon(ICON_NEW);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fts.rules().add(SF.rule(RandomStyleFactory.createPointSymbolizer()));
                }
            });
        }
    }

    class DuplicateItem extends JMenuItem {

        private final DefaultMutableTreeNode parentNode;

        DuplicateItem(DefaultMutableTreeNode node) {
            this.parentNode = node;
            setText("Duplicate");
            setIcon(ICON_DUPLICATE);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    treemodel.duplicateNode(parentNode);
                }
            });
        }
    }
    
    class DeleteItem extends JMenuItem {
        
        private final DefaultMutableTreeNode parentNode;

        DeleteItem(DefaultMutableTreeNode node) {
            this.parentNode = node;
            setText("Delete");
            setIcon(ICON_DELETE);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    treemodel.deleteNode(parentNode);
                }
            });
        }
        
    }

    class NewPointSymbolizerItem extends JMenuItem {

        private final MutableRule rule;

        NewPointSymbolizerItem(MutableRule cdt) {
            this.rule = cdt;
            setText("Point Symbolizer");
            setIcon(ICON_NEW);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    rule.symbolizers().add(RandomStyleFactory.createPointSymbolizer());
                }
            });
        }
    }

    class NewLineSymbolizerItem extends JMenuItem {

        private final MutableRule rule;

        NewLineSymbolizerItem(MutableRule cdt) {
            this.rule = cdt;
            setText("Line Symbolizer");
            setIcon(ICON_NEW);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rule.symbolizers().add(RandomStyleFactory.createLineSymbolizer());
                }
            });
        }
    }

    class NewPolygonSymbolizerItem extends JMenuItem {

        private final MutableRule rule;

        NewPolygonSymbolizerItem(MutableRule cdt) {
            this.rule = cdt;
            setText("Polygon Symbolizer");
            setIcon(ICON_NEW);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    rule.symbolizers().add(RandomStyleFactory.createPolygonSymbolizer());
                }
            });
        }
    }
    
    class NewTextSymbolizerItem extends JMenuItem {

        private final MutableRule rule;

        NewTextSymbolizerItem(MutableRule cdt) {
            this.rule = cdt;
            setText("Text Symbolizer");
            setIcon(ICON_NEW);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    rule.symbolizers().add(SF.textSymbolizer());
                    
                }
            });
        }
    }

    class NewRasterSymbolizerItem extends JMenuItem {

        private final MutableRule rule;

        NewRasterSymbolizerItem(MutableRule cdt) {
            this.rule = cdt;
            setText("Raster Symbolizer");
            setIcon(ICON_NEW);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    rule.symbolizers().add(SF.rasterSymbolizer());
                }
            });
        }
    }
    
}
