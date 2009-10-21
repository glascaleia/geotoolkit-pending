/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.feature.type;

import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.Name;

/**
 * Default implementation of a asociation descriptor
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class DefaultAssociationDescriptor extends DefaultPropertyDescriptor<AssociationType>
        implements AssociationDescriptor {

    public DefaultAssociationDescriptor(AssociationType type, Name name, int min, int max, boolean isNillable) {
        super(type, name, min, max, isNillable);
    }
    
}
