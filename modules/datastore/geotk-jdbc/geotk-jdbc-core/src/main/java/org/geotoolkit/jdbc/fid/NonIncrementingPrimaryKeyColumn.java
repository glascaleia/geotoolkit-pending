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
package org.geotoolkit.jdbc.fid;


/**
 * Represents a column of a primary key which is not auto incrementing.
 * <p>
 * If the type of the column is integral, new values are generated by selecting
 * the max value of the key and adding 1 to it. If the column is character based
 * a random string is generated.
 * </p>
 * @author Justin Deoliveira, OpenGEO
 *
 * @module pending
 */
public class NonIncrementingPrimaryKeyColumn extends PrimaryKeyColumn {

    public NonIncrementingPrimaryKeyColumn(final String name, final Class type) {
        super(name, type);
    }
}
