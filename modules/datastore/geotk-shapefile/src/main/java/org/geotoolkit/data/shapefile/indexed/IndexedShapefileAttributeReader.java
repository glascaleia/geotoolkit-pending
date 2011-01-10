/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.shapefile.indexed;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.geotoolkit.data.shapefile.ShapefileAttributeReader;
import org.geotoolkit.data.dbf.IndexedDbaseFileReader;
import org.geotoolkit.data.shapefile.indexed.IndexDataReader.ShpData;
import org.geotoolkit.data.shapefile.shp.ShapefileReader;
import org.geotoolkit.index.CloseableCollection;

import org.opengis.feature.type.PropertyDescriptor;

/**
 * An AttributeReader implementation for shape. Pretty straightforward. <BR/>The
 * default geometry is at position 0, and all dbf columns follow. <BR/>The dbf
 * file may not be necessary, if not, just pass null as the DbaseFileReader
 * @module pending
 */
public class IndexedShapefileAttributeReader <T extends Iterator<ShpData>> extends ShapefileAttributeReader
        implements RecordNumberTracker {

    protected final T goodRecs;
    private final CloseableCollection<ShpData> closeableCollection;
    private int recno;
    private ShpData next;

    public IndexedShapefileAttributeReader( final List<? extends PropertyDescriptor> attributes,
            final ShapefileReader shp, final IndexedDbaseFileReader dbf, final CloseableCollection<ShpData> col,
            final T goodRecs) {
        this(attributes, shp, dbf, col, goodRecs,null);
    }

    public IndexedShapefileAttributeReader( final List<? extends PropertyDescriptor> attributes,
            final ShapefileReader shp, final IndexedDbaseFileReader dbf, final CloseableCollection<ShpData> col,
            final T goodRecs, final double[] estimateRes) {
        this(attributes.toArray(new PropertyDescriptor[attributes.size()]), shp, dbf, col, goodRecs,estimateRes);
    }

    /**
     * Create the shape reader
     * 
     * @param atts - the attributes that we are going to read.
     * @param shp - the shape reader, required
     * @param dbf - the dbf file reader. May be null, in this case no
     *              attributes will be read from the dbf file
     * @param goodRecs Collection of good indexes that match the query.
     */
    public IndexedShapefileAttributeReader(final PropertyDescriptor[] atts,
            final ShapefileReader shp, final IndexedDbaseFileReader dbf,
            final CloseableCollection<ShpData> col, final T goodRecs, final double[] estimateRes) {
        super(atts, shp, dbf,estimateRes);
        this.goodRecs = goodRecs;
        this.closeableCollection = col;
        this.recno = 0;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if( closeableCollection!=null ){
                closeableCollection.closeIterator(goodRecs);
                closeableCollection.close();
            }
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return hasNextInternal();
    }

    private boolean hasNextInternal() throws IOException{
        if (this.goodRecs != null) {
            if (next != null)
                return true;
            if (this.goodRecs.hasNext()) {
                next = goodRecs.next();
                recno = next.v1;
                return true;
            }
            return false;
        }

        return super.hasNext();
    }

    @Override
    public void next() throws IOException {
        moveToNextShape();
        moveToNextDbf();
    }

    protected void moveToNextShape() throws IOException{
        if (!hasNextInternal()){
            throw new IndexOutOfBoundsException("No more features in reader");
        }

        if (this.goodRecs != null) {
            shp.goTo((int) next.v2);
            next = null;
            nextShape();
        } else {
            this.recno++;
            super.next();
        }

    }

    protected void moveToNextDbf() throws IOException{
        if (this.goodRecs != null && dbf != null) {
            ((IndexedDbaseFileReader) dbf).goTo(this.recno);
            nextDbf();
        }
    }

    @Override
    public int getRecordNumber() {
        return this.recno;
    }

}
