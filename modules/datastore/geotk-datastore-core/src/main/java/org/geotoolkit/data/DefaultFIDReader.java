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

import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A Default FIDReader.  Just auto-increments an index.   May be sufficient for
 * files, representing rows in a file.  For jdbc datasources a
 * ResultSetFIDReader should be used.
 *
 * @author Chris Holmes
 * @version $Id$
 * @module pending
 */
public class DefaultFIDReader implements FIDReader {

    protected static final String CLOSE_MESG = "Close has already been called on this FIDReader";
    private int len;
    protected int index = 0;
    protected final StringBuilder buffer;

    public DefaultFIDReader(String typeName) {
        buffer = new StringBuilder(typeName);
        buffer.append('.');
        len = typeName.length() + 1;
    }

    public DefaultFIDReader(SimpleFeatureType featureType) {
        this(featureType.getTypeName());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() {
        index = -1;
    }

    /**
     * Does another set of attributes exist in this reader?
     *
     * @return <code>true</code> if more attributes exist
     *
     * @throws IOException If closed
     */
    @Override
    public boolean hasNext() throws IOException {
        if (index < 0) {
            throw new IOException(CLOSE_MESG);
        }

        return index < Integer.MAX_VALUE;
    }

    /**
     * Read the attribute at the given index.
     *
     * @return Attribute at index
     *
     * @throws IOException If closed
     */
    @Override
    public String next() throws IOException {
        if (index < 0) {
            throw new IOException(CLOSE_MESG);
        }

        buffer.setLength(len);
        buffer.append(++index);

        return buffer.toString();
    }
}
