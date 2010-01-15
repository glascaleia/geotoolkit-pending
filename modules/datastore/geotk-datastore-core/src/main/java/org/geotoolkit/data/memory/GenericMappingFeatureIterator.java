/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Johann Sorel
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

package org.geotoolkit.data.memory;


import java.util.List;
import java.util.Map;

import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.memory.mapping.DefaultFeatureMapper;
import org.geotoolkit.data.memory.mapping.FeatureMapper;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * Basic support for a FeatureIterator that moves attributs to a new type definition
 * using a mapping objet.
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class GenericMappingFeatureIterator<F extends Feature> implements FeatureIterator<F>{

    private final FeatureIterator<F> ite;
    private final FeatureMapper mapper;

    public GenericMappingFeatureIterator(FeatureIterator<F> ite,
            FeatureType originalType, FeatureType newType,
            Map<PropertyDescriptor,List<PropertyDescriptor>> mapping,
            Map<PropertyDescriptor,Object> defaults){
        this.ite = ite;
        mapper = new DefaultFeatureMapper((SimpleFeatureType)originalType,(SimpleFeatureType)newType,mapping,defaults);
    }

    @Override
    public F next() {
        return (F) mapper.transform(ite.next());
    }

    @Override
    public void close() {
        ite.close();
    }

    @Override
    public boolean hasNext() {
        return ite.hasNext();
    }

    @Override
    public void remove() {
        throw new DataStoreRuntimeException("Not writable.");
    }

}
