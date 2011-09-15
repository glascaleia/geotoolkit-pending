/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2011, Geomatys
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
package org.geotoolkit.data.shapefile.fix;


import org.geotoolkit.data.shapefile.FeatureIDReader;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.data.shapefile.ShpFiles;
import org.geotoolkit.data.shapefile.indexed.RecordNumberTracker;
import org.geotoolkit.data.shapefile.shp.ShapefileReader;
import org.geotoolkit.internal.io.IOUtilities;

import static org.geotoolkit.data.shapefile.ShpFileType.FIX;
import static org.geotoolkit.data.shapefile.ShapefileDataStoreFactory.*;

/**
 * This object reads from a file the fid of a feature in a shapefile.
 * 
 * @author Jesse
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class IndexedFidReader implements FeatureIDReader {
    private ReadableByteChannel readChannel;
    private ByteBuffer buffer;
    private long count;
    private String typeName;
    private boolean done;
    private int removes;
    private int currentShxIndex = -1;
    private RecordNumberTracker reader;
    private long currentId;
    /**
     * move the reader to the recno-th entry in the file.
     * 
     * @param recno
     * @throws IOException
     */
    private long bufferStart = Long.MIN_VALUE;

    public IndexedFidReader(final ShpFiles shpFiles) throws IOException {
        init( shpFiles, shpFiles.getReadChannel(FIX, this) );
    }

    public IndexedFidReader(final ShpFiles shpFiles, final RecordNumberTracker reader)
            throws IOException {
        this(shpFiles);
        this.reader = reader;
    }

    public IndexedFidReader( final ShpFiles shpFiles, final ReadableByteChannel in ) throws IOException {
        init(shpFiles, in);
    }

    private void init( final ShpFiles shpFiles, final ReadableByteChannel in ) throws IOException {
        this.typeName = shpFiles.getTypeName() + ".";
        this.readChannel = in;
        getHeader(shpFiles);

        buffer = ByteBuffer.allocateDirect(IndexedFidWriter.RECORD_SIZE * 1024);
        buffer.position(buffer.limit());
    }

    private void getHeader(final ShpFiles shpFiles) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(IndexedFidWriter.HEADER_SIZE);
        ShapefileReader.fill(buffer, readChannel);

        if (buffer.position() == 0) {
            done = true;
            count = 0;
            return;
        }

        buffer.position(0);

        byte version = buffer.get();

        if (version != 1) {
            throw new IOException(
                    "File is not of a compatible version for this reader or file is corrupt.");
        }

        this.count = buffer.getLong();
        this.removes = buffer.getInt();
        if (removes > getCount() / 2) {
            URL url = shpFiles.acquireRead(FIX, this);
            try {
                IOUtilities.toFile(url, ENCODING).deleteOnExit();
            } finally {
                shpFiles.unlockRead(url, this);
            }
        }
    }

    /**
     * Returns the number of Fid Entries in the file.
     * 
     * @return Returns the number of Fid Entries in the file.
     */
    public long getCount() {
        return count;
    }

    /**
     * Returns the number of features that have been removed since the fid index
     * was regenerated.
     * 
     * @return Returns the number of features that have been removed since the
     *         fid index was regenerated.
     */
    public int getRemoves() {
        return removes;
    }

    /**
     * Returns the offset to the location in the SHX file that the fid
     * identifies. This search take logN time.
     * 
     * @param fid
     *                the fid to find.
     * 
     * @return Returns the record number of the record in the SHX file that the
     *         fid identifies. Will return -1 if the fid was not found.
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     *                 DOCUMENT ME!
     */
    public long findFid(final String fid) throws IOException {
        try {
            final long desired;
            if(fid.startsWith(typeName)){
                try{
                    desired =  Long.parseLong(fid.substring(typeName.length()), 10);
                }catch(NumberFormatException e){
                    return -1;
                }
            }else{
                return -1;
            }

            if (desired < 0) {
                return -1;
            }

            if (desired < count) {
                return search(desired, -1, this.count, desired - 1);
            } else {
                return search(desired, -1, this.count, count - 1);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Fid is not recognized as a fid for this shapefile: {0}", typeName);
            return -1;
        }
    }

    /**
     * Searches for the desired record.
     * 
     * @param desired
     *                the id of the desired record.
     * @param minRec
     *                the last record that is known to be <em>before</em> the
     *                desired record.
     * @param maxRec
     *                the first record that is known to be <em>after</em> the
     *                desired record.
     * @param predictedRec
     *                the record that is predicted to be the desired record.
     * 
     * @return returns the record number of the feature in the shx file.
     * 
     * @throws IOException
     */
    long search(final long desired, final long minRec, final long maxRec, final long predictedRec)
            throws IOException {
        if (minRec == maxRec) {
            return -1;
        }

        goTo(predictedRec);
        hasNext(); // force data reading
        next();
        buffer.limit(buffer.capacity());
        if (currentId == desired) {
            return currentShxIndex;
        }

        if (maxRec - minRec < 10) {
            return search(desired, minRec + 1, maxRec, minRec + 1);
        } else {
            long newOffset = desired - currentId;
            long newPrediction = predictedRec + newOffset;

            if (newPrediction <= minRec) {
                newPrediction = minRec + ((predictedRec - minRec) / 2);
            }

            if (newPrediction >= maxRec) {
                newPrediction = predictedRec + ((maxRec - predictedRec) / 2);
            }

            if (newPrediction == predictedRec) {
                return -1;
            }

            if (newPrediction < predictedRec) {
                return search(desired, minRec, predictedRec, newPrediction);
            } else {
                return search(desired, predictedRec, maxRec, newPrediction);
            }
        }
    }

    public void goTo(final long recno) throws IOException {
        assert recno<count;
        if (readChannel instanceof FileChannel) {
            long newPosition = IndexedFidWriter.HEADER_SIZE
                    + (recno * IndexedFidWriter.RECORD_SIZE);
            if (newPosition >= bufferStart + buffer.limit()
                    || newPosition < bufferStart) {
                FileChannel fc = (FileChannel) readChannel;
                fc.position(newPosition);
                buffer.limit(buffer.capacity());
                buffer.position(buffer.limit());
            } else {
                buffer.position((int) (newPosition - bufferStart));
            }
        } else {
            throw new IOException(
                    "Read Channel is not a File Channel so this is not possible.");
        }
    }

    public void close() throws DataStoreRuntimeException {
        try {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException ex) {
                    throw new DataStoreRuntimeException(ex);
                }
            }
        } finally {
            try {
                readChannel.close();
            } catch (IOException ex) {
                throw new DataStoreRuntimeException(ex);
            }
        }
    }

    public boolean hasNext() throws DataStoreRuntimeException {
        if (done) {
            return false;
        }

        if (buffer.position() == buffer.limit()) {
            buffer.position(0);

            final FileChannel fc = (FileChannel) readChannel;

            final int read;
            try {
                bufferStart = fc.position();
                read = ShapefileReader.fill(buffer, readChannel);
            } catch (IOException ex) {
                throw new DataStoreRuntimeException(ex);
            }

            if (read != 0) {
                buffer.position(0);
            }
        }

        return buffer.remaining() != 0;
    }

    public String next() throws DataStoreRuntimeException {
        if (reader != null) {
            try {
                goTo(reader.getRecordNumber() - 1);
            } catch (IOException ex) {
                throw new DataStoreRuntimeException(ex);
            }
        }

        if (!hasNext()) {
            throw new NoSuchElementException("Feature could not be read; a the index may be invalid");
        }

        currentId = buffer.getLong();
        currentShxIndex = buffer.getInt();

        return typeName + currentId;
    }

    /**
     * Returns the record number of the feature in the shx or shp that is
     * identified by the the last fid returned by next().
     * 
     * @return Returns the record number of the feature in the shx or shp that
     *         is identified by the the last fid returned by next().
     * 
     * @throws NoSuchElementException DOCUMENT ME!
     */
    public int currentSHXIndex() {
        if (currentShxIndex == -1) {
            throw new NoSuchElementException("Next must be called before there exists a current element.");
        }

        return currentShxIndex;
    }

    /**
     * Returns the index that is appended to the typename to construct the fid.
     * 
     * @return the index that is appended to the typename to construct the fid.
     */
    public long getCurrentFIDIndex(){
        return currentId;
    }
    
}
