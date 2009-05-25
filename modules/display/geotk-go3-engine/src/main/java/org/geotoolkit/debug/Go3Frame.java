/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotoolkit.debug;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.geotoolkit.display3d.canvas.A3DCanvas;
import org.geotoolkit.gui.swing.go3.control.JNavigationBar;
import org.geotoolkit.gui.swing.maptree.JContextTree;
import org.geotoolkit.map.MapContext;


/**
 * @author Johann Sorel (Puzzle-GIS)
 */
public class Go3Frame extends JFrame{

    private final A3DCanvas gui3DPane;
    private final JContextTree guiTree = new JContextTree();
    private final JNavigationBar guiNavBar = new JNavigationBar();

    public Go3Frame() throws Exception {
        final MapContext context = ContextBuilder.buildRealCityContext();
        gui3DPane = new A3DCanvas(context.getCoordinateReferenceSystem(), null);
        gui3DPane.getContainer2().setContext(context);
        guiTree.setContext(context);
        guiNavBar.setFloatable(false);
        guiNavBar.setMap(gui3DPane);

        final JSplitPane splitTree = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        final JPanel pan3D = new JPanel(new BorderLayout());
        pan3D.add(BorderLayout.NORTH,guiNavBar);
        pan3D.add(BorderLayout.CENTER,gui3DPane.getComponent());
        splitTree.setLeftComponent(guiTree);
        splitTree.setRightComponent(pan3D);

        final JMenuBar bar = new JMenuBar();
        final JMenu menu = new JMenu("File");
        final JMenuItem item = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        bar.add(menu);
        menu.add(item);

        gui3DPane.getController().setCameraSpeed(100);


        setJMenuBar(bar);
        setContentPane(splitTree);
        setSize(1280,1024);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

    }

    public static void main(String[] args) throws Exception {
        new Go3Frame();
    }
}
