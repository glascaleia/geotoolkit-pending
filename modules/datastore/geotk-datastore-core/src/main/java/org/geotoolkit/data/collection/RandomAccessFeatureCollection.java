/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.collection;

import java.util.NoSuchElementException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Access Feature content using Feature "Id".
 * <p>
 * Many FeatureCollection<SimpleFeatureType, SimpleFeature> classes will make use of this
 * API to avoid unnecessary caching of content. Supporting
 * this interface will allow SubCollections to occur based
 * on FeatureIds, with a suitable improvement in memory
 * consumption.
 * </p>
 * <p>
 * For an addition improvement in memory comsumption SubCollections
 * may use of a sparse reprsentation where only (beginId,endId] ranges
 * are kept in memory.
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 * @module pending
 */
public interface RandomAccessFeatureCollection extends FeatureCollection<SimpleFeatureType, SimpleFeature> {
    /**
     * Access Feature content by feature id.
     *
     * @param id
     * @return Feature with the indicated or id
     * @throws NoSuchElementException if a Feature with the indicated id is not present
     */
    SimpleFeature getFeatureMember(String id) throws NoSuchElementException;

    /** Optional Method */
    SimpleFeature removeFeatureMember(String id);
}