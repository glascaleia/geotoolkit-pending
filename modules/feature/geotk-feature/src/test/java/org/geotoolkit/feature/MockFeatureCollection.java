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
 *
 *    Created on August 12, 2003, 7:29 PM
 */
package org.geotoolkit.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import org.geotoolkit.feature.collection.CollectionListener;
import org.geotoolkit.feature.collection.FeatureCollection;
import org.geotoolkit.feature.collection.FeatureIterator;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.opengis.feature.FeatureVisitor;
import org.opengis.util.ProgressListener;

/**
 * @author jamesm
 * @module pending
 */
public class MockFeatureCollection implements FeatureCollection<SimpleFeatureType, SimpleFeature> {

    /** Creates a new instance of MockFeatureCollection */
    public MockFeatureCollection() {
    }

    public void accepts(FeatureVisitor visitor, ProgressListener progress)
            throws IOException {
    }
    
    public void addListener(CollectionListener listener)
            throws NullPointerException {
    }

    public void close(FeatureIterator<SimpleFeature> close) {
    }

    public void close(Iterator close) {
    }

    public FeatureIterator<SimpleFeature> features() {
        return null;
    }

    public SimpleFeatureType getSchema() {
        return null;
    }

    public void removeListener(CollectionListener listener)
            throws NullPointerException {
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> sort(SortBy order) {
        return null;
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection(Filter filter) {
        return null;
    }

    public Iterator iterator() {
        return null;
    }

    public void purge() {
    }

    public boolean add(SimpleFeature o) {
        return false;
    }

    public boolean addAll(Collection c) {
        return false;
    }
    public boolean addAll(
		FeatureCollection<? extends SimpleFeatureType, ? extends SimpleFeature> resource) {
    	return false;
    }
    public void clear() {
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean containsAll(Collection c) {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean removeAll(Collection c) {
        return false;
    }

    public boolean retainAll(Collection c) {
        return false;
    }

    public int size() {
        return 0;
    }

    public Object[] toArray() {
        return null;
    }

    public Object[] toArray(Object[] a) {
        return null;
    }

    public JTSEnvelope2D getBounds() {
        return null;
    }

    public String getID() {
        return null;
    }
   
}
