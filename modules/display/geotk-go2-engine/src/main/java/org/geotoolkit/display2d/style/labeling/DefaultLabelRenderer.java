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
package org.geotoolkit.display2d.style.labeling;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.style.j2d.TextStroke;

import org.opengis.referencing.operation.TransformException;

/**
 * Default implementation of label renderer.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class DefaultLabelRenderer implements LabelRenderer{

    private final List<LabelLayer> layers = new ArrayList<LabelLayer>();
    private RenderingContext2D context = null;
    
    public DefaultLabelRenderer() {
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void setRenderingContext(RenderingContext2D context){
        if(context == null) throw new NullPointerException("Rendering context can not be null");
        this.context = context;
    }

    /**
     * {@inheritDoc }
     */ 
    @Override
    public RenderingContext2D getRenderingContext() {
        return context;
    }
    
    /**
     * {@inheritDoc }
     */ 
    @Override
    public void append(final LabelLayer layer) {
        layers.add(layer);
    }
    
    /**
     * {@inheritDoc }
     */ 
    @Override
    public void portrayLabels(){
        final Graphics2D g2 = context.getGraphics();
        //enable antialiasing for labels
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for(final LabelLayer layer : layers){

            for(LabelDescriptor label : layer.labels()){
                if(label instanceof PointLabelDescriptor){
                    portray(g2, (PointLabelDescriptor)label);
                }else if(label instanceof LinearLabelDescriptor){
                    portray(g2, (LinearLabelDescriptor)label);
                }
            }
        }
    }

    private void portray(Graphics2D g2, PointLabelDescriptor label){
        context.switchToDisplayCRS();

        final FontMetrics metric = g2.getFontMetrics(label.getTextFont());
        final int textHeight = metric.getHeight();
        final int textWidth = metric.stringWidth(label.getText());

        final Shape geom;
        try {
            geom = label.getGeometry().getDisplayShape();
        } catch (TransformException ex) {
            Logger.getLogger(DefaultLabelRenderer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        float refX = (float) geom.getBounds2D().getCenterX();
        float refY = (float) geom.getBounds2D().getCenterY();

        //adjust displacement---------------------------------------------------
        //displacement is oriented above and to the right
        refX = refX + label.getDisplacementX();
        refY = refY - label.getDisplacementY();

        //rotation--------------------------------------------------------------
        final float rotate = (float) Math.toRadians(label.getRotation());
        g2.rotate(rotate, refX, refY);

        //adjust anchor---------------------------------------------------------
        refX = refX - (label.getAnchorX()*textWidth);
        //text is draw above reference point so use +
        refY = refY + (label.getAnchorY()*textHeight);

        //paint halo------------------------------------------------------------
        final FontRenderContext fontContext = g2.getFontRenderContext();
        final GlyphVector glyph = label.getTextFont().createGlyphVector(fontContext, label.getText());
        final Shape shape = glyph.getOutline(refX,refY);
        g2.setPaint(label.getHaloPaint());
        g2.setStroke(new BasicStroke(label.getHaloWidth()*2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g2.draw(shape);

        //paint text------------------------------------------------------------
        g2.setPaint(label.getTextPaint());
        g2.setFont(label.getTextFont());
        g2.drawString(label.getText(), refX, refY);

    }

    private void portray(Graphics2D g2, LinearLabelDescriptor label){
        context.switchToDisplayCRS();

        final TextStroke stroke = new TextStroke(label.getText(), label.getTextFont(), label.isRepeated(),
                label.getOffSet(), label.getInitialGap(), label.getGap());

        final Shape geom;
        try {
            geom = label.getGeometry().getDisplayShape();
        } catch (TransformException ex) {
            Logger.getLogger(DefaultLabelRenderer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        final Shape shape = stroke.createStrokedShape(geom);

        //paint halo
        g2.setStroke(new BasicStroke(label.getHaloWidth(),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND) );
        g2.setPaint(label.getHaloPaint());
        g2.draw(shape);

        //paint text
        g2.setStroke(new BasicStroke(0));
        g2.setPaint(label.getTextPaint());
        g2.fill(shape);
    }
        
}
