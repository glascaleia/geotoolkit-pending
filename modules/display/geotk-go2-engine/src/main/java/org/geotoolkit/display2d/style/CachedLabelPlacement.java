/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2008, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.display2d.style;

import org.opengis.style.LabelPlacement;

/**
 * Superclass for CachedPointPlacement and CachedLinePlacement.
 * 
 * @author Johann Sorel (Geomatys)
 */
public abstract class CachedLabelPlacement<T extends LabelPlacement> extends Cache<T>{

    public CachedLabelPlacement(T element){
        super(element);
    }
    

}
