/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2010, Geomatys
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

package org.geotoolkit.index.quadtree.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

/**
 * A utility class to access file contents by using a single scrolling
 * buffer reading file contents with a minimum of 8kb per access
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class ScrollingBuffer {

    private final FileChannel channel;
    private final ByteOrder order;
    private ByteBuffer buffer;
    /** the initial position of the buffer in the channel */
    private long bufferStart;

    public ScrollingBuffer(FileChannel channel, ByteOrder order)
            throws IOException {
        this.channel = channel;
        this.order = order;
        this.bufferStart = channel.position();

        // start with an 8kb buffer
        this.buffer = ByteBuffer.allocateDirect(8 * 1024);
        this.buffer.order(order);
        channel.read(buffer);
        buffer.flip();
    }

    public int getInt() throws IOException {
        if (buffer.remaining() < 4) {
            refillBuffer(4);
        }
        return buffer.getInt();
    }

    public double getDouble() throws IOException {
        if (buffer.remaining() < 8) {
            refillBuffer(8);
        }
        return buffer.getDouble();
    }

    public void getIntArray(int[] array) throws IOException {
        final int size = array.length * 4;
        if (buffer.remaining() < size) {
            refillBuffer(size);
        }
        // read the array using a view
        final IntBuffer intView = buffer.asIntBuffer();
        intView.limit(array.length);
        intView.get(array);
        // don't forget to update the original buffer position, since the
        // view is independent
        buffer.position(buffer.position() + size);
    }

    /**
     *
     * @param requiredSize
     * @throws IOException
     */
    void refillBuffer(int requiredSize) throws IOException {
        // compute the actual position up to we have read something
        final long currentPosition = bufferStart + buffer.position();
        // if the buffer is not big enough enlarge it
        if (buffer.capacity() < requiredSize) {
            int size = buffer.capacity();
            while (size < requiredSize) {
                size *= 2;
            }
            buffer = ByteBuffer.allocateDirect(size);
            buffer.order(order);
        }
        readBuffer(currentPosition);
    }

    private void readBuffer(long currentPosition) throws IOException {
        channel.position(currentPosition);
        buffer.clear();
        channel.read(buffer);
        buffer.flip();
        bufferStart = currentPosition;
    }

    /**
     * Jumps the buffer to the specified position in the file
     *
     * @param newPosition
     * @throws IOException
     */
    public void goTo(long newPosition) throws IOException {
        // if the new position is already in the buffer, just move the
        // buffer position
        // otherwise we have to reload it
        if (newPosition >= bufferStart && newPosition <= bufferStart + buffer.limit()) {
            buffer.position((int) (newPosition - bufferStart));
        } else {
            readBuffer(newPosition);
        }
    }

    /**
     * Returns the absolute position of the next byte that will be read
     *
     * @return
     */
    public long getPosition() {
        return bufferStart + buffer.position();
    }

}
