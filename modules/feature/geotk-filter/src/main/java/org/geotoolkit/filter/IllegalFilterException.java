/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009, Geomatys
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
package org.geotoolkit.filter;


/**
 * Defines an exception for illegal filters.
 *
 * TODO: JD: Changed this exception to runtime exception. Go through all methods
 * that throw this expception and reflect the new geoapi method throws it with
 * a javadoc.
 * @module pending
 */
public class IllegalFilterException extends RuntimeException {

    /**
     * Constructor with a message.
     *
     * @param message information on the error.
     */
    public IllegalFilterException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of <code>IllegalFilterException</code> with the
     * specified root cause.
     *
     * @param cause the root cause of the exceptions.
     */
    public IllegalFilterException(final Exception cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>IllegalFilterException</code> with the
     * specified detail message and root cause.
     *
     * @param msg the detail message.
     * @param cause the root cause of the exceptions.
     */
    public IllegalFilterException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
