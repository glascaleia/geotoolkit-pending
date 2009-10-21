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
package org.geotoolkit.display2d.style;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Icon;

import org.opengis.feature.Feature;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.ExternalGraphic;

/**
 * Cached External graphic
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class CachedExternal extends Cache<ExternalGraphic>{

    //Cached values
    private BufferedImage cachedImage = null;

    public CachedExternal(ExternalGraphic external){
        super(external);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void evaluate(){
        if(!isNotEvaluated) return;
        
        if(!evaluateExternal()){
            //no valid image we can clear the cache, nothing to paint, nothing
            // visible
            cachedImage = null;
            isStaticVisible = VisibilityState.UNVISIBLE;
        }else{
            isStaticVisible = VisibilityState.VISIBLE;
        }
        
        requieredAttributs = EMPTY_ATTRIBUTS;
        isStatic = true;
        isNotEvaluated = false;
    }

    /**
     * 
     * @return true if is visible, false if something says there's nothing to paint
     */
    private boolean evaluateExternal(){

        //try to grab the inline image
        final Icon inline = styleElement.getInlineContent();
        if(inline != null){
            cachedImage = new BufferedImage(inline.getIconWidth(), inline.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) cachedImage.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            inline.paintIcon(null, g2, 0,0);
            g2.dispose();
        }
        
        
        //if no inline image then use the online image
        if(cachedImage == null){
            OnLineResource online = styleElement.getOnlineResource();
            if(online != null && online.getLinkage() != null){
                
                final URI path = styleElement.getOnlineResource().getLinkage();

                if (path != null) {
                    File imageFile = new File(path);
                    if (imageFile != null && imageFile.exists()) {
                        try {
                            cachedImage = ImageIO.read(imageFile);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        
        return ! (cachedImage == null);        
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isVisible(Feature feature) {
        return isValid();
    }

    /**
     * 
     * @return true if this externam image is valid.
     */
    public boolean isValid(){
        evaluate();
        return cachedImage != null;
    }

    /**
     * Create or generate the image at a giver size.
     * 
     * @param size : image size
     * @return BufferedImage
     */
    public BufferedImage getImage(final Float size, RenderingHints hints){
        evaluate();

        //resize image if necessary
        if(cachedImage != null && size != null){
            final float aspect = (float)(cachedImage.getHeight()) / size.floatValue() ;
            final float maxwidth = cachedImage.getWidth() / aspect;

            BufferedImage buffer = new BufferedImage( (int)(maxwidth+0.5f), (int)(size.floatValue()), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) buffer.getGraphics();
            if(hints != null) g2.setRenderingHints(hints);
            g2.drawImage(cachedImage, 0, 0,buffer.getWidth(), buffer.getHeight(), 0, 0, cachedImage.getWidth(), cachedImage.getHeight(),null);
            g2.dispose();
            return buffer;
        }else{
            return cachedImage;
        }

    }

}
