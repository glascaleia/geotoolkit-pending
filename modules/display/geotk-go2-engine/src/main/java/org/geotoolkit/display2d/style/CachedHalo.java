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

import org.geotoolkit.display2d.GO2Utilities;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.RenderingHints;
import org.opengis.feature.Feature;
import org.opengis.style.Halo;

/**
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class CachedHalo extends Cache<Halo>{

    private final CachedFill cachedFill;
    private float cachedWidth = Float.NaN;
    
    private CachedHalo(Halo halo){
        super(halo);
        cachedFill = CachedFill.cache(halo.getFill());
    }
    
    @Override
    protected void evaluate() {
        if(!isNotEvaluated) return;
        //cache what is needed

        isNotEvaluated = false;
    }

    public Composite getJ2DComposite(Feature feature){
        return cachedFill.getJ2DComposite(feature);
    }
    
    public Paint getJ2DPaint(Feature feature,int x, int y, RenderingHints hints){
        return cachedFill.getJ2DPaint(feature, x, y, 1f, hints);
    }
    
    public float getWidth(Feature feature){
        float j2dWidth = GO2Utilities.evaluate(styleElement.getRadius(), null, Float.class, 1f);
        return j2dWidth;
    }
    
    @Override
    public boolean isVisible(Feature feature) {
        evaluate();
        
        return (cachedWidth >0);
    }

    public static CachedHalo cache(Halo halo){
        return new CachedHalo(halo);
    }
}
