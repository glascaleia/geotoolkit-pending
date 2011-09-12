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
package org.geotoolkit.data.postgis.wkb;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import net.iharder.Base64;


/**
 * An attribute IO implementation that can manage the WKB
 *
 * @author Andrea Aime
 * @module pending
 * @since 2.4.1
 */
public final class WKBAttributeIO {

    private final WKBReader wkbr;
    private final ByteArrayInStream inStream = new ByteArrayInStream();

    public WKBAttributeIO() {
        wkbr = new WKBReader();
    }

    public WKBAttributeIO(final GeometryFactory gf) {
        wkbr = new WKBReader(gf);
    }

    /**
     * This method will convert a Well Known Binary representation to a
     * JTS  Geometry object.
     *
     * @param wkb the wkb encoded byte array
     *
     * @return a JTS Geometry object that is equivalent to the WTB
     *         representation passed in by parameter wkb
     *
     * @throws IOException if more than one geometry object was found in  the
     *         WTB representation, or if the parser could not parse the WKB
     *         representation.
     */
    private Geometry wkb2Geometry(final byte[] wkbBytes) throws IOException {
        if (wkbBytes == null) {
            //DJB: null value from database --> null geometry (the same behavior as WKT).
            //NOTE: sending back a GEOMETRYCOLLECTION(EMPTY) is also a possibility, but this is not the same as NULL
            return null;
        }
        inStream.setBytes(wkbBytes);
        try {
            return wkbr.read(inStream);
        } catch (ParseException e) {
            throw new IOException("An exception occurred while parsing WKB data", e);
        }
    }

    public Object read(final ResultSet rs, final String columnName) throws IOException {
        final byte bytes[];
        try {
            bytes = rs.getBytes(columnName);
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred while reading the geometry.", e);
        }
        
        if (bytes == null) {
            // ie. its a null column -> return a null geometry!
            return null;
        }
        return wkb2Geometry(Base64.decode(bytes));
    }

    public Object read(final ResultSet rs, final int columnIndex) throws IOException {
        final byte bytes[];
        try {
            bytes = rs.getBytes(columnIndex);
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred while reading the geometry.", e);
        }
        
        if (bytes == null) {
            // ie. its a null column -> return a null geometry!
            return null;
        }
        return wkb2Geometry(Base64.decode(bytes));
    }

    public void write(final PreparedStatement ps, final int position, final Object value)
            throws IOException {
        try {
            if (value == null) {
                ps.setNull(position, Types.OTHER);
            } else {
                ps.setBytes(position, new WKBWriter().write((Geometry)value));
            }
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred while reading the geometry.", e);
        }
    }

    /**
     * Turns a char that encodes four bits in hexadecimal notation into a byte
     *
     * @param c
     */
    public static byte getFromChar(final char c) {
        if (c <= '9') {
            return (byte) (c - '0');
        } else if (c <= 'F') {
            return (byte) (c - 'A' + 10);
        } else {
            return (byte) (c - 'a' + 10);
        }
    }

    private byte[] hexToBytes(final String wkb) {
        // convert the String of hex values to a byte[]
        final byte[] wkbBytes = new byte[wkb.length() / 2];

        for (int i = 0; i < wkbBytes.length; i++) {
            byte b1 = getFromChar(wkb.charAt(i * 2));
            byte b2 = getFromChar(wkb.charAt((i * 2) + 1));
            wkbBytes[i] = (byte) ((b1 << 4) | b2);
        }

        return wkbBytes;
      }

    /**
     * Accelerates data loading compared to the plain InStream shipped along with JTS
     * @author Andrea Aime - TOPP
     */
    private static class ByteArrayInStream implements InStream {

        byte[] buffer;
        int position;

        public void setBytes(final byte[] buffer) {
            this.buffer = buffer;
            this.position = 0;
        }

        @Override
        public void read(final byte[] buf) throws IOException {
            final int size = buf.length;
            System.arraycopy(buffer, position, buf, 0, size);
            position += size;
        }

    }
}

