/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.client.map;

import java.awt.geom.Point2D;
import java.util.UUID;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.opengis.geometry.Envelope;

/**
 * Default mosaic grid.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class DefaultGridMosaic implements GridMosaic{

    private final String id = UUID.randomUUID().toString();
    private final Pyramid pyramid;
    private final Point2D upperLeft;
    private final int width;
    private final int height;
    private final int tileHeight;
    private final int tileWidth;
    private final double tileSpanX;
    private final double tileSpanY;

    public DefaultGridMosaic(Pyramid pyramid, Point2D upperLeft, int width, 
            int height, int tileHeight, int tileWidth, double tileSpanX, double tileSpanY) {
        this.pyramid = pyramid;
        this.upperLeft = upperLeft;
        this.width = width;
        this.height = height;
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
        this.tileSpanX = tileSpanX;
        this.tileSpanY = tileSpanY;
    }

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public Pyramid getPyramid() {
        return pyramid;
    }

    @Override
    public Point2D getUpperLeftCorner() {
        return upperLeft;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public double getTileSpanX() {
        return tileSpanX;
    }

    @Override
    public double getTileSpanY() {
        return tileSpanY;
    }

    @Override
    public int getTileWidth() {
        return tileWidth;
    }

    @Override
    public int getTileHeight() {
        return tileHeight;
    }

    @Override
    public Envelope getEnvelope(int row, int col) {
        final double minX = getUpperLeftCorner().getX();
        final double maxY = getUpperLeftCorner().getY();
        final double spanX = getTileSpanX();
        final double spanY = getTileSpanY();
        
        final GeneralEnvelope envelope = new GeneralEnvelope(
                getPyramid().getCoordinateReferenceSystem());
        envelope.setRange(0, minX + col*spanX, minX + (col+1)*spanX);
        envelope.setRange(1, maxY - (row+1)*spanY, maxY - row*spanY);
        
        return envelope;
    }
    
    @Override
    public boolean isMissing(int col, int row) {
        return false;
    }

}
