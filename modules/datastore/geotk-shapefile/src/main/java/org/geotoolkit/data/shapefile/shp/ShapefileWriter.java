/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2010, Geomatys
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
package org.geotoolkit.data.shapefile.shp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.geotoolkit.data.shapefile.shx.ShxWriter;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.resources.NIOUtilities;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * ShapefileWriter allows for the storage of geometries in esris shp format.
 * During writing, an index will also be created. To create a ShapefileWriter,
 * do something like<br>
 * <code>
 *   GeometryCollection geoms;
 *   File shp = new File("myshape.shp");
 *   File shx = new File("myshape.shx");
 *   ShapefileWriter writer = new ShapefileWriter(
 *     shp.getChannel(),shx.getChannel()
 *   );
 *   writer.write(geoms,ShapeType.ARC);
 * </code>
 * This example assumes that each shape in the collection is a LineString.
 *
 * @author jamesm
 * @author aaime
 * @author Ian Schneider
 * @module pending
 */
public class ShapefileWriter {

    final ShxWriter shx;

    FileChannel shpChannel;
    ByteBuffer shapeBuffer;
    ShapeHandler handler;
    ShapeType type;
    int offset;
    int lp;
    int cnt;

    /**
     * Creates a new instance of ShapeFileWriter
     *
     * @throws IOException
     */
    public ShapefileWriter(FileChannel shpChannel, FileChannel shxChannel)
            throws IOException {
        this.shpChannel = shpChannel;
        this.shx = new ShxWriter(shxChannel);
    }

    /**
     * Allocate some buffers for writing.
     */
    private void allocateBuffers() {
        shapeBuffer = ByteBuffer.allocateDirect(16 * 1024);
    }

    /**
     * Make sure our buffer is of size.
     */
    private void checkShapeBuffer(int size) {
        if (shapeBuffer.capacity() < size) {
            if (shapeBuffer != null)
                NIOUtilities.clean(shapeBuffer);
            shapeBuffer = ByteBuffer.allocateDirect(size);
        }
    }

    /**
     * Drain internal buffers into underlying channels.
     */
    private void drain() throws IOException {
        shapeBuffer.flip();
        while (shapeBuffer.remaining() > 0)
            shpChannel.write(shapeBuffer);
        shapeBuffer.flip().limit(shapeBuffer.capacity());
    }

    private void writeHeaders(GeometryCollection geometries, ShapeType type)
            throws IOException {
        // ShapefileHeader header = new ShapefileHeader();
        // Envelope bounds = geometries.getEnvelopeInternal();
        // header.write(shapeBuffer, type, geometries.getNumGeometries(),
        // fileLength / 2,
        // bounds.getMinX(),bounds.getMinY(), bounds.getMaxX(),bounds.getMaxY()
        // );
        // header.write(indexBuffer, type, geometries.getNumGeometries(), 50 + 4
        // * geometries.getNumGeometries(),
        // bounds.getMinX(),bounds.getMinY(), bounds.getMaxX(),bounds.getMaxY()
        // );
        int fileLength = 100;
        // int largestShapeSize = 0;
        for (int i = geometries.getNumGeometries() - 1; i >= 0; i--) {
            // shape length + record (2 ints)
            int size = handler.getLength(geometries.getGeometryN(i)) + 8;
            fileLength += size;
            // if (size > largestShapeSize)
            // largestShapeSize = size;
        }
        writeHeaders(geometries.getEnvelopeInternal(), type, geometries.getNumGeometries(), fileLength);
    }

    /**
     * Write the headers for this shapefile including the bounds, shape type,
     * the number of geometries and the total fileLength (in actual bytes, NOT
     * 16 bit words).
     */
    public void writeHeaders(Envelope bounds, ShapeType type,
            int numberOfGeometries, int fileLength) throws IOException {

        try {
            handler = type.getShapeHandler();
        } catch (DataStoreException se) {
            throw new RuntimeException("unexpected Exception", se);
        }
        if (shapeBuffer == null)
            allocateBuffers();

        ShapefileHeader.write(shapeBuffer, type, fileLength / 2,
                bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());

        shx.moveToHeaderStart();
        shx.writeHeader(type, 50 + 4 * numberOfGeometries, bounds.getMinX(),
                bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());

        offset = 50;
        this.type = type;
        cnt = 0;

        shpChannel.position(0);
        drain();
    }

    /**
     * Allocate internal buffers and position the channels to the beginning or
     * the record section of the shapefile. The headers MUST be rewritten after
     * this operation, or the file may be corrupt...
     */
    public void skipHeaders() throws IOException {
        if (shapeBuffer == null)
            allocateBuffers();
        shpChannel.position(100);
        shx.moveToRecordStart();
    }

    /**
     * Write a single Geometry to this shapefile. The Geometry must be
     * compatable with the ShapeType assigned during the writing of the headers.
     */
    public void writeGeometry(Geometry g) throws IOException {
        if (shapeBuffer == null)
            throw new IOException("Must write headers first");
        lp = shapeBuffer.position();
        int length = handler.getLength(g);

        // must allocate enough for shape + header (2 ints)
        checkShapeBuffer(length + 8);

        length /= 2;

        shapeBuffer.order(ByteOrder.BIG_ENDIAN);
        shapeBuffer.putInt(++cnt);
        shapeBuffer.putInt(length);
        shapeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        shapeBuffer.putInt(type.id);
        handler.write(shapeBuffer, g);

        assert (length * 2 == (shapeBuffer.position() - lp) - 8);

        lp = shapeBuffer.position();

        // write to the shx
        shx.writeRecord(offset, length);
        offset += length + 4;

        drain();
        assert (shapeBuffer.position() == 0);
    }

    /**
     * Close the underlying Channels.
     */
    public void close() throws IOException {
        try {
            if (shpChannel != null && shpChannel.isOpen()) {
                shpChannel.close();
            }
        } finally {
            shx.close();
        }
        shpChannel = null;
        handler = null;
        if (shapeBuffer instanceof MappedByteBuffer) {
            NIOUtilities.clean(shapeBuffer);
        }
        shapeBuffer = null;
    }

    /**
     * Bulk write method for writing a collection of (hopefully) like geometries
     * of the given ShapeType.
     */
    public void write(GeometryCollection geometries, ShapeType type)
            throws IOException, DataStoreException {
        handler = type.getShapeHandler();

        writeHeaders(geometries, type);

        lp = shapeBuffer.position();
        for (int i = 0, ii = geometries.getNumGeometries(); i < ii; i++) {
            Geometry g = geometries.getGeometryN(i);

            writeGeometry(g);
        }

        close();
    }

}
