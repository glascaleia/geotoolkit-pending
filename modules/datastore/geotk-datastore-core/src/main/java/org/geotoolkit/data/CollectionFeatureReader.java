/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotoolkit.data.collection.FeatureCollection;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * FeatureReader<SimpleFeatureType, SimpleFeature> that reads features from a java.util.collection of features,
 * an array of features or a FeatureCollection.
 *
 * @author jones
 * @module pending
 */
public class CollectionFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
    private final Iterator features;
    private final SimpleFeatureType type;
    private boolean closed = false;

    /**
     * Create a new instance.
     *
     * @param featuresArg a colleciton of features.  <b>All features must be of the same FeatureType</b>
     * @param typeArg the Feature type of of the features.
     */
    public CollectionFeatureReader(final Collection featuresArg, final SimpleFeatureType typeArg) {
        assert !featuresArg.isEmpty();

        if (featuresArg instanceof FeatureCollection) {
            collection = (FeatureCollection<SimpleFeatureType, SimpleFeature>) featuresArg;
        }

        this.features = featuresArg.iterator();
        this.type = typeArg;
    }

    /**
     * Create a new instance.
     *
     * @param featuresArg a FeatureCollection.  <b>All features must be of the same FeatureType</b>
     * @param typeArg the Feature type of of the features.
     */
    public CollectionFeatureReader(FeatureCollection<SimpleFeatureType, SimpleFeature> featuresArg,
            SimpleFeatureType typeArg) {
        assert !featuresArg.isEmpty();
        collection = featuresArg;
        this.features = featuresArg.iterator();
        this.type = typeArg;
    }

    /**
     * Create a new instance.
     *
     * @param featuresArg an of features.  <b>All features must be of the same FeatureType</b>
     */
    public CollectionFeatureReader(SimpleFeature[] featuresArg) {
        assert featuresArg.length > 0;
        this.features = Arrays.asList(featuresArg).iterator();
        type = featuresArg[0].getFeatureType();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public SimpleFeatureType getFeatureType() {
        return type;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public SimpleFeature next()
            throws IOException, IllegalAttributeException, NoSuchElementException {
        if (closed) {
            throw new NoSuchElementException("Reader has been closed");
        }

        return (SimpleFeature) features.next();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hasNext() throws IOException {
        return features.hasNext() && !closed;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() throws IOException {
        closed = true;

        if (collection != null) {
            collection.close(features);
        }
    }
}
