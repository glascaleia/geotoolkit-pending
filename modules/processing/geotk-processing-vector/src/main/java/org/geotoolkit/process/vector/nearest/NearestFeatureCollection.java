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
package org.geotoolkit.process.vector.nearest;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.process.vector.WrapFeatureCollection;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * FeatureCollection for Nearest process
 * @author Quentin Boileau
 * @module pending
 */
public class NearestFeatureCollection extends WrapFeatureCollection {

    private final FeatureType newFeatureType;

    /**
     * Connect to the original FeatureConnection
     * @param originalFC FeatureCollection
     * @param clippingList 
     */
    public NearestFeatureCollection(final FeatureCollection<Feature> originalFC) {
        super(originalFC);
        this.newFeatureType = super.getFeatureType();
    }

    /**
     * Return the new FeatureType
     * @return FeatureType
     */
    @Override
    public FeatureType getFeatureType() {
        return newFeatureType;
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    protected Feature modify(final Feature original) {
        return original;
    }
}
