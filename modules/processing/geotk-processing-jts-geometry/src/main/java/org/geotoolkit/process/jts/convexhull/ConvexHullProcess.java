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
package org.geotoolkit.process.jts.convexhull;

import com.vividsolutions.jts.geom.Geometry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.process.AbstractProcess;
import org.opengis.parameter.ParameterValueGroup;

import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import static org.geotoolkit.process.jts.convexhull.ConvexHullDescriptor.*;
import static org.geotoolkit.parameter.Parameters.*;
import org.opengis.util.FactoryException;
/**
 * Compute input geometry convex hull.
 * The returned convexHull geometry keep input geometry CoordinateReferenceSystem.
 * @author Quentin Boileau (Geomatys)
 * @module pending
 */
public class ConvexHullProcess extends AbstractProcess{
    
    public ConvexHullProcess(final ParameterValueGroup input){
        super(INSTANCE,input);
    }
    
    @Override
    public ParameterValueGroup call() {
        
        try {
            final Geometry geom = value(GEOM, inputParameters); 
            
            final CoordinateReferenceSystem geomCRS = JTS.findCoordinateReferenceSystem(geom);
            
            final Geometry result =  geom.convexHull();
            JTS.setCRS(result, geomCRS);
            
            getOrCreate(RESULT_GEOM, outputParameters).setValue(result); 
            
        } catch (NoSuchAuthorityCodeException ex) {
            Logger.getLogger(ConvexHullProcess.class.getName()).log(Level.WARNING, null, ex);
        } catch (FactoryException ex) {
            Logger.getLogger(ConvexHullProcess.class.getName()).log(Level.WARNING, null, ex);
        }
        
        return outputParameters;
    }
    
}
