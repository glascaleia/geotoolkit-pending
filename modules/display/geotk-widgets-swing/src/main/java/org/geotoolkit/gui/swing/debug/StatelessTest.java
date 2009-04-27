/*
 * GoFrame.java
 *
 * Created on 14 mai 2008, 15:29
 */

package org.geotoolkit.gui.swing.debug;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.geotoolkit.gui.swing.contexttree.JContextTree;
import org.geotoolkit.gui.swing.contexttree.JContextTreePopup;
import org.geotoolkit.gui.swing.contexttree.column.VisibleTreeTableColumn;
import org.geotoolkit.gui.swing.contexttree.popup.ContextActiveItem;
import org.geotoolkit.gui.swing.contexttree.popup.ContextPropertyItem;
import org.geotoolkit.gui.swing.contexttree.popup.CopyItem;
import org.geotoolkit.gui.swing.contexttree.popup.CutItem;
import org.geotoolkit.gui.swing.contexttree.popup.DeleteItem;
import org.geotoolkit.gui.swing.contexttree.popup.DuplicateItem;
import org.geotoolkit.gui.swing.contexttree.popup.LayerFeatureItem;
import org.geotoolkit.gui.swing.contexttree.popup.LayerPropertyItem;
import org.geotoolkit.gui.swing.contexttree.popup.PasteItem;
import org.geotoolkit.gui.swing.contexttree.popup.SeparatorItem;
import org.geotoolkit.gui.swing.propertyedit.LayerCRSPropertyPanel;
import org.geotoolkit.gui.swing.propertyedit.LayerFilterPropertyPanel;
import org.geotoolkit.gui.swing.propertyedit.LayerGeneralPanel;
import org.geotoolkit.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotoolkit.gui.swing.propertyedit.PropertyPane;
import org.geotoolkit.gui.swing.propertyedit.filterproperty.JCQLPropertyPanel;
import org.geotoolkit.gui.swing.propertyedit.styleproperty.JSimpleStylePanel;

import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

/**
 *
 * @author  sorel
 */
public class StatelessTest extends javax.swing.JFrame {
    
    private MapContext context = ContextBuilder.buildRealCityContext();
    
    /** Creates new form GoFrame */
    public StatelessTest() {
        initComponents();        
        initTree(guiContextTree);        
        
        guiContextTree.addContext(context);
              
        setSize(1024,768);
        setLocationRelativeTo(null);             
    }

    
    
    
    private void initTree(JContextTree tree) {
        JContextTreePopup popup = tree.getPopupMenu();

        popup.addItem(new SeparatorItem());
        popup.addItem(new LayerFeatureItem());              //layer
        popup.addItem(new ContextActiveItem(tree));         //context
        popup.addItem(new SeparatorItem());
        popup.addItem(new CutItem(tree));                   //all
        popup.addItem(new CopyItem(tree));                  //all
        popup.addItem(new PasteItem(tree));                 //all
        popup.addItem(new DuplicateItem(tree));             //all        
        popup.addItem(new SeparatorItem());
        popup.addItem(new DeleteItem(tree));                //all
        popup.addItem(new SeparatorItem());

        LayerPropertyItem property = new LayerPropertyItem();
        List<PropertyPane> lstproperty = new ArrayList<PropertyPane>();
        lstproperty.add(new LayerGeneralPanel());
        lstproperty.add(new LayerCRSPropertyPanel());

        LayerFilterPropertyPanel filters = new LayerFilterPropertyPanel();
        filters.addPropertyPanel(new JCQLPropertyPanel());
        lstproperty.add(filters);

        LayerStylePropertyPanel styles = new LayerStylePropertyPanel();
        styles.addPropertyPanel(new JSimpleStylePanel());
        lstproperty.add(styles);

        property.setPropertyPanels(lstproperty);
        
        popup.addItem(property);             //layer
        popup.addItem(new ContextPropertyItem());           //context

        tree.addColumn(new VisibleTreeTableColumn());


        tree.revalidate();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        guiContextTree = new org.geotoolkit.gui.swing.contexttree.JContextTree();
        panGeneral = new javax.swing.JPanel();
        guiMap = new org.jdesktop.swingx.JXImagePanel();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Go-2 Java2D Renderer");

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setLeftComponent(guiContextTree);

        panGeneral.setLayout(new java.awt.BorderLayout());
        panGeneral.add(guiMap, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(panGeneral);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jToolBar1, gridBagConstraints);

        jButton3.setText("Go test");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new java.awt.GridBagConstraints());

        jButton4.setText("Streaming test");
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new java.awt.GridBagConstraints());

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jMenu1.setText("File");

        jMenuItem1.setText("Quit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    System.exit(0);
}//GEN-LAST:event_jMenuItem1ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

    Dimension canvasDimension = new Dimension(640,480);
    org.opengis.geometry.Envelope dataEnvelope = null;
    try {
        dataEnvelope = context.getBounds();
    } catch (IOException ex) {
        ex.printStackTrace();
    }


    long before = System.nanoTime();

    Image buffer = null;
    try {
        buffer = DefaultPortrayalService.portray(context, dataEnvelope, canvasDimension,false);
    } catch (PortrayalException ex) {
        Logger.getLogger(StatelessTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    

    long after = System.nanoTime();

    System.out.println("--------------------------------------------------------");
    System.out.println("time to render = " + (float) ((after - before) / 1000000000f) + "sec.");

    if (buffer != null) 
        guiMap.setImage(buffer);

    
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

//    Dimension canvasDimension = new Dimension(640,480);
//    Envelope dataEnvelope = null;
//    try {
//        dataEnvelope = context.getLayerBounds();
//    } catch (IOException ex) {
//        ex.printStackTrace();
//    }
//
//    final Rectangle rect = new Rectangle(canvasDimension.width, canvasDimension.height);
//    Envelope env = fixAspectRatio(rect, dataEnvelope);
//
//    long before = System.nanoTime();
//
//    StreamingRenderer renderer = new StreamingRenderer();
//    renderer.setContext(context);
//    
//    BufferedImage buffer = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_ARGB);
//    
//    renderer.paint((Graphics2D) buffer.getGraphics(), rect, env);
////        buffer = service.portray(context, dataEnvelope, canvasDimension);
//    
//
//    long after = System.nanoTime();
//
//    System.out.println("--------------------------------------------------------");
//    System.out.println("time to render = " + (float) ((after - before) / 1000000000f) + "sec.");
//
//    if (buffer != null) 
//        guiMap.setImage(buffer);
    
    
    
    
}//GEN-LAST:event_jButton4ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StatelessTest().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private org.geotoolkit.gui.swing.contexttree.JContextTree guiContextTree;
    private org.jdesktop.swingx.JXImagePanel guiMap;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel panGeneral;
    // End of variables declaration//GEN-END:variables

    
    protected Envelope fixAspectRatio(Rectangle rect, Envelope area) {

        double mapWidth = area.getWidth(); /* get the extent of the map */
        double mapHeight = area.getHeight();
        double scaleX = rect.getWidth() / area.getWidth(); /*
         * calculate the new
         * scale
         */

        double scaleY = rect.getHeight() / area.getHeight();
        double scale = 1.0; // stupid compiler!

        if (scaleX < scaleY) { /* pick the smaller scale */
            scale = scaleX;
        } else {
            scale = scaleY;
        }

        /* calculate the difference in width and height of the new extent */
        double deltaX = /* Math.abs */ ((rect.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */ ((rect.getHeight() / scale) - mapHeight);


        /* create the new extent */
        Coordinate ll = new Coordinate(area.getMinX() - (deltaX / 2.0), area.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(area.getMaxX() + (deltaX / 2.0), area.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }
    
    
}
