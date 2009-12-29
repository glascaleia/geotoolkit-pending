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
package org.geotoolkit.data.shapefile.shp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import org.geotoolkit.data.shapefile.ShpFileType;
import org.geotoolkit.data.shapefile.ShpFiles;
import org.geotoolkit.resources.NIOUtilities;
import org.geotoolkit.util.logging.Logging;

/**
 * IndexFile parser for .shx files.<br>
 * For now, the creation of index files is done in the ShapefileWriter. But this
 * can be used to access the index.<br>
 * For details on the index file, see <br>
 * <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf"><b>"ESRI(r)
 * Shapefile - A Technical Description"</b><br> * <i>'An ESRI White Paper .
 * May 1997'</i></a>
 * 
 * @author Ian Schneider
 * @module pending
 */
public class IndexFile {
    private static final Logger LOGGER = Logging.getLogger("org.geotoolkit.data.shapefile");
    private static final int RECS_IN_BUFFER = 2000;

    private boolean useMemoryMappedBuffer;
    private FileChannel channel;
    private int channelOffset;
    private ByteBuffer buf = null;
    private int lastIndex = -1;
    private int recOffset;
    private int recLen;
    private ShapefileHeader header = null;
    private int[] content;

    private volatile boolean closed = false;

    /**
     * Load the index file from the given channel.
     * 
     * @param shpFiles The channel to read from.
     * @throws IOException If an error occurs.
     */
    public IndexFile(ShpFiles shpFiles, boolean useMemoryMappedBuffer)
            throws IOException {
        this.useMemoryMappedBuffer = useMemoryMappedBuffer;
        final ReadableByteChannel byteChannel = shpFiles.getReadChannel(ShpFileType.SHX, this);

        try {
            readHeader(byteChannel);
            if (byteChannel instanceof FileChannel) {

                this.channel = (FileChannel) byteChannel;
                if (useMemoryMappedBuffer) {
                    LOGGER.finest("Memory mapping file...");
                    this.buf = this.channel.map(FileChannel.MapMode.READ_ONLY,
                            0, this.channel.size());

                    this.channelOffset = 0;
                } else {
                    LOGGER.finest("Reading from file...");
                    this.buf = ByteBuffer.allocateDirect(8 * RECS_IN_BUFFER);
                    this.channelOffset = 100;
                }

            } else {
                LOGGER.finest("Loading all shx...");
                readRecords(byteChannel);
                byteChannel.close();
            }
        } catch (Throwable e) {
            if (byteChannel != null) {
                byteChannel.close();
            }
            throw (IOException)
                    new IOException(e.getLocalizedMessage()).initCause(e);
        }
    }

    /**
     * Get the header of this index file.
     * 
     * @return The header of the index file.
     */
    public ShapefileHeader getHeader() {
        return header;
    }

    private void check() {
        if (closed) {
            throw new IllegalStateException("Index file has been closed");
        }

    }

    private void readHeader(ReadableByteChannel channel) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(100);
        while (buffer.remaining() > 0) {
            channel.read(buffer);
        }
        buffer.flip();
        header = ShapefileHeader.read(buffer, true);

        NIOUtilities.clean(buffer);
    }

    private void readRecords(ReadableByteChannel channel) throws IOException {
        check();
        final int remaining = (header.getFileLength() * 2) - 100;
        final ByteBuffer buffer = ByteBuffer.allocateDirect(remaining);
        buffer.order(ByteOrder.BIG_ENDIAN);
        while (buffer.remaining() > 0) {
            channel.read(buffer);
        }
        buffer.flip();
        final int records = remaining / 4;
        content = new int[records];
        final IntBuffer ints = buffer.asIntBuffer();
        ints.get(content);
        NIOUtilities.clean(buffer);
    }

    private void readRecord(int index) throws IOException {
        check();
        final int pos = 100 + index * 8;
        if (this.useMemoryMappedBuffer) {

        } else {
            if (pos - this.channelOffset < 0
                    || this.channelOffset + buf.limit() <= pos
                    || this.lastIndex == -1) {
                LOGGER.finest("Filling buffer...");
                this.channelOffset = pos;
                this.channel.position(pos);
                buf.clear();
                this.channel.read(buf);
                buf.flip();
            }
        }

        buf.position(pos - this.channelOffset);
        this.recOffset = buf.getInt();
        this.recLen = buf.getInt();
        this.lastIndex = index;
    }

    public void close() throws IOException {
        closed = true;
        if (channel != null && channel.isOpen()) {
            channel.close();

            if (buf instanceof MappedByteBuffer) {
                NIOUtilities.clean(buf);
            } else {
                buf.clear();
            }
        }
        this.buf = null;
        this.content = null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    /**
     * Get the number of records in this index.
     * 
     * @return The number of records.
     */
    public int getRecordCount() {
        return (header.getFileLength() * 2 - 100) / 8;
    }

    /**
     * Get the offset of the record (in 16-bit words).
     * 
     * @param index The index, from 0 to getRecordCount - 1
     * @return The offset in 16-bit words.
     * @throws IOException
     */
    public int getOffset(int index) throws IOException {

        if (this.channel != null) {
            if (this.lastIndex != index) {
                this.readRecord(index);
            }
            return this.recOffset;
        } else {
            return content[2 * index];
        }
    }

    /**
     * Get the offset of the record (in real bytes, not 16-bit words).
     * 
     * @param index The index, from 0 to getRecordCount - 1
     * @return The offset in bytes.
     * @throws IOException
     */
    public int getOffsetInBytes(int index) throws IOException {
        return this.getOffset(index) * 2;
    }

    /**
     * Get the content length of the given record in bytes, not 16 bit words.
     * 
     * @param index The index, from 0 to getRecordCount - 1
     * @return The lengh in bytes of the record.
     * @throws IOException
     */
    public int getContentLength(int index) throws IOException {

        if (this.channel != null) {
            if (this.lastIndex != index) {
                this.readRecord(index);
            }
            return this.recLen;
        } else {
            return content[2 * index + 1];
        }
    }

}