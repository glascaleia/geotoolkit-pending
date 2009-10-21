/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.display2d.canvas;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.VolatileImage;

import org.geotoolkit.display.canvas.CanvasController2D;
import org.geotoolkit.display.canvas.DefaultController2D;
import org.geotoolkit.display.container.AbstractContainer;
import org.geotoolkit.display.container.AbstractContainer2D;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.display.shape.XRectangle2D;

import org.geotoolkit.display2d.canvas.painter.SolidColorPainter;
import org.opengis.display.canvas.RenderingState;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Canvas based on a VolatileImage.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class J2DCanvasVolatile extends J2DCanvas{

    private final GraphicsConfiguration GC;
    private final DrawingThread thread;
    private CanvasController2D controller = new DefaultController2D(this);
    private final DefaultRenderingContext2D context2D = new DefaultRenderingContext2D(this);
    private VolatileImage buffer0;
    private Dimension dim;
    private boolean mustupdate = false;

    private final Area dirtyArea = new Area();
    
    public J2DCanvasVolatile(CoordinateReferenceSystem crs, Dimension dim){
        this(crs,dim,null);
    }
    
    public J2DCanvasVolatile(CoordinateReferenceSystem crs, Dimension dim, Hints hints){
        super(crs,hints);
        thread = new DrawingThread();
        thread.start();
        if(dim == null){
            dim = new Dimension(1, 1);
        }else if(dim.getHeight() <=0 || dim.getWidth() <=0){
            dim = new Dimension(1, 1);
        }
        setDisplayBounds(new Rectangle(dim));
        this.dim = dim;

        GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        painter = new SolidColorPainter(Color.WHITE);
    }

    @Override
    public void clearCache() {
        super.clearCache();
        buffer0 = null;

    }

    @Override
    public void dispose(){
        super.dispose();
        thread.dispose();
        buffer0 = null;
        controller = null;
        dim = null;
    }

    /**
     * Resize the volatile image, this will set to null the buffer.
     * A new one will be created when repaint is called.
     */
    public void resize(Dimension dim){
        if(dim == null){
            dim = new Dimension(1, 1);
        }else if(dim.getHeight() <=0 || dim.getWidth() <=0){
            dim = new Dimension(1, 1);
        }
        this.dim = dim;
        setDisplayBounds(new Rectangle(dim));
        buffer0 = null;

        if(getController().isAutoRepaint()){
            repaint();
        }

    }

    private synchronized VolatileImage createBackBuffer() {
        return GC.createCompatibleVolatileImage(dim.width, dim.height, VolatileImage.OPAQUE);
    }


    @Override
    public CanvasController2D getController() {
        return controller;
    }
        
    /**
     * Returns the display bounds in terms of {@linkplain #getDisplayCRS display CRS}.
     * If no bounds were {@linkplain #setDisplayBounds explicitly set}, then this method
     * returns the {@linkplain Component#getBounds() widget bounds}.
     */
    @Override
    public synchronized Shape getDisplayBounds() {
        Shape bounds = super.getDisplayBounds();
        if (bounds.equals(XRectangle2D.INFINITY)) {
            bounds = new Rectangle(dim);
        }
        return bounds;
    }


    private void render(Shape paintingDisplayShape){

        if(paintingDisplayShape == null) paintingDisplayShape = getDisplayBounds();

        final Graphics2D output;
        if(buffer0 == null){
            //create the buffer at the last possible moment
            //or create a new one if we are already rendering
            //TODO : find a way to stop previous thread
            VolatileImage buffer = createBackBuffer();
            buffer.setAccelerationPriority(1);

            output = (Graphics2D) buffer.getGraphics();
            output.setComposite( AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            output.fillRect(0,0,dim.width,dim.height);

            buffer0 = buffer;

        }else{
            //we clear the buffer part if it exists
            output = (Graphics2D) buffer0.getGraphics();
            output.setComposite( AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            output.fill(paintingDisplayShape);
//            output.setColor(Color.WHITE);
//            output.fill(paintingDisplayShape);
        }

        output.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));

        monitor.renderingStarted();
        fireRenderingStateChanged(RenderingState.RENDERING);

//        Rectangle clipBounds = output.getClipBounds();
//        /*
//         * Sets a flag for avoiding some "refresh()" events while we are actually painting.
//         * For example some implementation of the GraphicPrimitive2D.paint(...) method may
//         * detects changes since the last rendering and invokes some kind of invalidate(...)
//         * methods before the graphic rendering begin. Invoking those methods may cause in some
//         * indirect way a call to GraphicPrimitive2D.refresh(), which will trig an other widget
//         * repaint. This second repaint is usually not needed, since Graphics usually managed
//         * to update their informations before they start their rendering. Consequently,
//         * disabling repaint events while we are painting help to reduces duplicated rendering.
//         */
//        final Rectangle displayBounds = getDisplayBounds().getBounds();
//        Rectangle2D dirtyArea = XRectangle2D.INFINITY;
//        if (clipBounds == null) {
//            clipBounds = displayBounds;
//        } else if (displayBounds.contains(clipBounds)) {
//            dirtyArea = clipBounds;
//        }
//        output.setClip(clipBounds);
//        paintStarted(dirtyArea);
        output.setClip(paintingDisplayShape);

        final DefaultRenderingContext2D context = prepareContext(context2D, output, paintingDisplayShape);

        //paint background if there is one.
        if(painter != null){
            painter.paint(context2D);
        }

        final AbstractContainer renderer         = getContainer();
        if(renderer != null && renderer instanceof AbstractContainer2D){
            final AbstractContainer2D renderer2D = (AbstractContainer2D) renderer;
            render(context, renderer2D.getSortedGraphics());
        }
        
        //End painting
        output.dispose();
        fireRenderingStateChanged(RenderingState.ON_HOLD);
        monitor.renderingFinished();
    }
    
    @Override
    public void repaint(){
        repaint(getDisplayBounds().getBounds());
    }
    
    @Override
    public Image getSnapShot(){
        if(buffer0 != null){
            return buffer0.getSnapshot();
        }
        return null;
    }

    public VolatileImage getVolatile(){
        return buffer0;
    }

    @Override
    protected RenderingContext getRenderingContext() {
        return context2D;
    }

    @Override
    public synchronized void repaint(Shape displayArea) {
        synchronized(dirtyArea){
           this.dirtyArea.add(new Area(displayArea));
        }
        mustupdate = true;
        thread.wake();
    }


    private class DrawingThread extends Thread {

        private boolean dispose = false;

        public void dispose(){
            dispose = true;
            wake();
        }

        @Override
        public void run() {

            while (!dispose) {
                while (mustupdate) {
                    mustupdate = false;

                    final Shape copy;
                    synchronized(dirtyArea){
                        copy = (dirtyArea.isEmpty()) ? null : new Area(dirtyArea);
                        dirtyArea.reset();
                    }

                    render(copy);
                }
                block();
            }
        }

        public synchronized void wake() {
            notifyAll();
        }

        private synchronized void block() {
            try {
                wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
