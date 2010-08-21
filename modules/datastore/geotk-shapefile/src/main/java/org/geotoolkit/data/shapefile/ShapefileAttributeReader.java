/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.shapefile;

import java.io.IOException;
import java.util.List;

import org.geotoolkit.data.AbstractPropertyReader;
import org.geotoolkit.data.dbf.DbaseFileHeader;
import org.geotoolkit.data.dbf.DbaseFileReader;
import org.geotoolkit.data.shapefile.shp.ShapefileReader;
import org.geotoolkit.util.Converters;

import org.opengis.feature.type.PropertyDescriptor;

/**
 * An AttributeReader implementation for Shapefile. Pretty straightforward.
 * <BR/>The default geometry is at position 0, and all dbf columns follow.
 * <BR/>The dbf file may not be necessary, if not, just pass null as the
 * DbaseFileReader
 * @module pending
 */
public class ShapefileAttributeReader extends AbstractPropertyReader {

    protected final boolean[] narrowing;
    protected final int[] attributIndex;
    protected ShapefileReader shp;
    protected DbaseFileReader dbf;
    protected DbaseFileReader.Row row;
    protected ShapefileReader.Record record;

    public ShapefileAttributeReader(List<? extends PropertyDescriptor> atts,
            ShapefileReader shp, DbaseFileReader dbf) {
        this(atts.toArray(new PropertyDescriptor[atts.size()]), shp, dbf);
    }

    /**
     * Create the shapefile reader
     * 
     * @param atts - the attributes that we are going to read.
     * @param shp - the shapefile reader, required
     * @param dbf - the dbf file reader. May be null, in this case no
     *              attributes will be read from the dbf file
     */
    public ShapefileAttributeReader(PropertyDescriptor[] atts,
            ShapefileReader shp, DbaseFileReader dbf) {
        super(atts);
        this.shp = shp;
        this.dbf = dbf;

        //the attribut descriptor might define types that are mare restrictive
        //then what the readers can do.
        narrowing = new boolean[atts.length];
        attributIndex = new int[atts.length];
        if(dbf != null){
            final DbaseFileHeader header = dbf.getHeader();
            attLoop:
            for(int i=0;i<atts.length;i++){
                final String attName = atts[i].getName().getLocalPart();
                //attribut field
                for(int k=0;k<header.getNumFields();k++){
                    final String fieldName = header.getFieldName(k);
                    if(fieldName.equals(attName)){
                        narrowing[i] = (atts[i].getType().getBinding() != header.getFieldClass(k));
                        attributIndex[i] = k;
                        continue attLoop;
                    }
                }
                //geom field
                attributIndex[i] = -1;
            }
        }else{
            if(atts.length != 1){
                throw new IllegalStateException("Reader has been asked to read "+atts.length+" attributs, but no dbf reader given.");
            }
            //geom field
            attributIndex[0] = -1;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() throws IOException {
        try {
            if (shp != null) {
                shp.close();
            }

            if (dbf != null) {
                dbf.close();
            }
        } finally {
            row = null;
            record = null;
            shp = null;
            dbf = null;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hasNext() throws IOException {

        if(shp.hasNext()){
            if(dbf != null){
                if(dbf.hasNext()){
                    return true;
                }else{
                    //shape files has more data then the dbf ? file or reader is corrupted
                    throw new IOException("Shp has extra record");
                }
            }else{
                //no attributs, only shapes
                return true;
            }
        }else{
            if(dbf != null){
                if(!dbf.hasNext()){
                    return false;
                }else{
                    //dbf has more data then the shape ? file or reader is corrupted
                    throw new IOException("Dbf has extra record");
                }
            }else{
                //no attributs, only shapes
                return false;
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void next() throws IOException {
        nextShape();
        nextDbf();
    }

    protected void nextShape() throws IOException {
        record = shp.nextRecord();
    }

    protected void nextDbf() throws IOException {
        if (dbf != null) {
            row = dbf.readRow();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object read(int param) throws IOException,IndexOutOfBoundsException {

        final int index = attributIndex[param];
        if(index == -1){
            return record.shape();
        }else if(row != null) {
            if(narrowing[param]){
                //must procede to a retype
                return Converters.convert(row.read(index), metaData[param].getType().getBinding());
            }else{
                return row.read(index);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void read(Object[] buffer) throws IOException {
        for(int i=0,n=getPropertyCount();i<n;i++){
            buffer[i] = read(i);
        }
    }
}
