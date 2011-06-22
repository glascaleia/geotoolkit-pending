/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.osmtms.map;

import org.geotoolkit.geometry.DirectPosition2D;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public final class OSMTMSUtilities {
    
    public static final double BASE_TILE_SIZE = 256d;
    
    public static final CoordinateReferenceSystem GOOGLE_MERCATOR;
    
    public static DirectPosition UPPER_LEFT_CORNER;
    
    static {
        try {
            GOOGLE_MERCATOR = CRS.decode("EPSG:3857");
            
            //X goes from 0 (left edge is 180 °W) to 2^zoom -1 (right edge is 180 °E) 
            //Y goes from 0 (top edge is 85.0511 °N) to 2^zoom -1 (bottom edge is 85.0511 °S) in a Mercator projection

            DirectPosition lonlatupper = new DirectPosition2D(DefaultGeographicCRS.WGS84, -180, 85.0511);

            final MathTransform trs = CRS.findMathTransform(DefaultGeographicCRS.WGS84, OSMTMSUtilities.GOOGLE_MERCATOR);
            UPPER_LEFT_CORNER = trs.transform(lonlatupper, null);
            
        } catch (NoSuchAuthorityCodeException ex) {
            throw new RuntimeException(ex);
        } catch (FactoryException ex) {
            throw new RuntimeException(ex);
        } catch (TransformException ex) {
            throw new RuntimeException(ex);
        }        
    }
    
    private OSMTMSUtilities(){}
    
}
