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
package org.geotoolkit.data.dbf;

import org.geotoolkit.storage.DataStoreException;


/**
 * Thrown when an error relating to the shapefile occurs.
 * 
 * @module pending
 */
public class DbaseFileException extends DataStoreException {

    private static final long serialVersionUID = -6890880438911014652L;

    public DbaseFileException(final String s) {
        super(s);
    }

    public DbaseFileException(final String s, final Throwable cause) {
        super(s, cause);
    }
}
