/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Geomatys
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
package org.geotoolkit.coverage.filestore;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.BitSet;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.geotoolkit.coverage.GridMosaic;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.storage.DataStoreException;
import org.opengis.geometry.Envelope;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLMosaic implements GridMosaic{
        
    //written values
    double scale;
    double upperleftX;
    double upperleftY;
    int gridWidth;
    int gridHeight;
    int tileWidth;
    int tileHeight;
    String completion;

    @XmlTransient
    XMLPyramid pyramid = null;
    @XmlTransient
    BitSet tileExist;
    
    void initialize(XMLPyramid pyramid){
        this.pyramid = pyramid;
        if(completion == null){
            completion = "";
        }
        tileExist = new BitSet(gridWidth*gridHeight);
        String packed = completion.replaceAll("\n", "");
        packed = packed.replaceAll("\t", "");
        packed = packed.replaceAll(" ", "");
        for(int i=0,n=packed.length();i<n;i++){
            final char c = packed.charAt(i);
            tileExist.set(i, c!='0');
        }
    }
    
    private void updateCompletionString(){
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for(int y=0,l=getGridSize().height;y<l;y++){
            sb.append('\n');
            for(int x=0,n=getGridSize().width;x<n;x++){
                sb.append( tileExist.get(index)?'1':'0' );
                index++;
            }
        }
        sb.append('\n');
        completion = sb.toString();
    }
    
    /**
     * Id equals scale string value
     */
    @Override
    public String getId() {
        return String.valueOf(scale);
    }

    public File getFolder(){
        return new File(getPyramid().getFolder(),getId());
    }
    
    @Override
    public XMLPyramid getPyramid() {
        return pyramid;
    }
    
    @Override
    public Point2D getUpperLeftCorner() {
        return new Point2D.Double(upperleftX, upperleftY);
    }

    @Override
    public Dimension getGridSize() {
        return new Dimension(gridWidth, gridHeight);
    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public Dimension getTileSize() {
        return new Dimension(tileWidth, tileHeight);
    }
    
    public Envelope getEnvelope(){
        final double minX = getUpperLeftCorner().getX();
        final double maxY = getUpperLeftCorner().getY();
        final double spanX = getTileSize().width * getGridSize().width * scale;
        final double spanY = getTileSize().height* getGridSize().height* scale;
        
        final GeneralEnvelope envelope = new GeneralEnvelope(
                getPyramid().getCoordinateReferenceSystem());
        envelope.setRange(0, minX, minX + spanX);
        envelope.setRange(1, maxY - spanY, maxY );
        
        return envelope;
    }
    
    @Override
    public Envelope getEnvelope(int col, int row) {
        final double minX = getUpperLeftCorner().getX();
        final double maxY = getUpperLeftCorner().getY();
        final double spanX = getTileSize().width * scale;
        final double spanY = getTileSize().height * scale;
        
        final GeneralEnvelope envelope = new GeneralEnvelope(
                getPyramid().getCoordinateReferenceSystem());
        envelope.setRange(0, minX + col*spanX, minX + (col+1)*spanX);
        envelope.setRange(1, maxY - (row+1)*spanY, maxY - row*spanY);
        
        return envelope;
    }

    @Override
    public boolean isMissing(int col, int row) {
        return !tileExist.get(getTileIndex(col, row));
    }

    @Override
    public RenderedImage getTile(int col, int row, Map hints) throws DataStoreException {
        try {
            return ImageIO.read(getTileFile(col, row));
        } catch (IOException ex) {
            throw new DataStoreException(ex.getMessage(),ex);
        }
    }

    @Override
    public InputStream getTileStream(int col, int row, Map hints) throws DataStoreException {
        try {
            return new FileInputStream(getTileFile(col, row));
        } catch (FileNotFoundException ex) {
            throw new DataStoreException(ex.getMessage(),ex);
        }
    }
    
    public File getTileFile(int col, int row) throws DataStoreException{
        checkPosition(col, row);
        return new File(getFolder(),col+"_"+row+"."+getPyramid().getPostfix());
    }

    void createTile(int col, int row, RenderedImage image) throws DataStoreException {
        checkPosition(col, row);
        final File f = getTileFile(col, row);
        f.getParentFile().mkdirs();
        try {
            final ImageOutputStream out = ImageIO.createImageOutputStream(f);
            final ImageWriter writer = ImageIO.getImageWritersByFormatName("PNG").next();
            writer.setOutput(out);
            writer.write(image);
            writer.dispose();
            tileExist.set(getTileIndex(col, row), true);
            updateCompletionString();
        } catch (IOException ex) {
            throw new DataStoreException(ex.getMessage(),ex);
        }
    }
    
    private void checkPosition(int col, int row) throws DataStoreException{
        if(col >= getGridSize().width || row >=getGridSize().height){
            throw new DataStoreException("Tile position is outside the grid : " + col +" "+row);
        }
    }
    
    private int getTileIndex(int col, int row){
        final int index = row*getGridSize().width + col;
        return index;
    }
    
}