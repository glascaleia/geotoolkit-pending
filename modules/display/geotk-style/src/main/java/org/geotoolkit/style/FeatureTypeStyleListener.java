/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.style;

import java.beans.PropertyChangeListener;

import org.opengis.feature.type.Name;
import org.opengis.style.SemanticType;

/**
 * Listener for FeatureTypeStyle.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public interface FeatureTypeStyleListener extends PropertyChangeListener{
    
    /**
     * Called when a change occurs in the living rule collection.
     */
    void ruleChange(CollectionChangeEvent<MutableRule> event);
    
    /**
     * Called when a change occurs in the living feature type name collection.
     */
    void featureTypeNameChange(CollectionChangeEvent<Name> event);
    
    /**
     * Called when a change occurs in the living semantic collection.
     */
    void semanticTypeChange(CollectionChangeEvent<SemanticType> event);
    
}
