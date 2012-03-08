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
package org.geotoolkit.googlemaps.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.geotoolkit.coverage.AbstractGridMosaic;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.TileReference;
import org.geotoolkit.storage.DataStoreException;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class GoogleMapsMosaic extends AbstractGridMosaic{

    private final int scaleLevel;
    
    public GoogleMapsMosaic(Pyramid pyramid, Point2D upperLeft, Dimension gridSize,
            Dimension tileSize, double scale, int scaleLevel) {
        super(pyramid,upperLeft,gridSize,tileSize,scale);
        this.scaleLevel = scaleLevel;
    }

    public int getScaleLevel() {
        return scaleLevel;
    }

    @Override
    public TileReference getTile(int col, int row, Map hints) throws DataStoreException {
        return ((GoogleMapsPyramidSet)getPyramid().getPyramidSet()).getTile(this, col, row, hints);
    }
    
    @Override
    public BlockingQueue<Object> getTiles(Collection<? extends Point> positions, Map hints) throws DataStoreException {
        return ((GoogleMapsPyramidSet)getPyramid().getPyramidSet()).getTiles(this, positions, hints);
    }
    
}
