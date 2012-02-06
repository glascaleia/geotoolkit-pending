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
package org.geotoolkit.display2d.container.stateless;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.geotoolkit.coverage.GridMosaic;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidSet;
import org.geotoolkit.coverage.PyramidalModel;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.GridCoverageFactory;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.processing.Operations;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.canvas.VisitFilter;
import org.geotoolkit.display.canvas.control.CanvasMonitor;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display.primitive.SearchArea;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.display2d.style.CachedRule;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.operation.transform.AffineTransform2D;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.logging.Logging;
import org.opengis.display.primitive.Graphic;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Graphic for pyramidal coverage layers.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class StatelessPyramidalCoverageLayerJ2D extends StatelessMapLayerJ2D<CoverageMapLayer>{

    private class TileReference{
        GridMosaic mosaic;
        String mimeType;
        int col;
        int row;
        MathTransform gridToCRS;
        Map hints;
    }
    
    private final PyramidalModel model;
    private final double tolerance;

    public StatelessPyramidalCoverageLayerJ2D(final J2DCanvas canvas, final CoverageMapLayer layer){
        super(canvas, layer, true);
        
        model = (PyramidalModel)layer.getCoverageReference();
        tolerance = 0.1; // in % , TODO use a flag to allow change value
    }

    /**
     * {@inheritDoc }
     * @param context2D 
     */
    @Override
    public void paintLayer(final RenderingContext2D context2D) {
                     
        final Name coverageName = item.getCoverageName();
        final CachedRule[] rules = GO2Utilities.getValidCachedRules(item.getStyle(),
                context2D.getSEScale(), coverageName,null);

        //we perform a first check on the style to see if there is at least
        //one valid rule at this scale, if not we just continue.
        if (rules.length == 0) {
            return;
        }

        final CanvasMonitor monitor = context2D.getMonitor();

        final Envelope canvasEnv = context2D.getCanvasObjectiveBounds2D();   
        final PyramidSet pyramidSet;
        try {
            pyramidSet = model.getPyramidSet();
        } catch (DataStoreException ex) {
            monitor.exceptionOccured(ex, Level.WARNING);
            return;
        }
        
        final Pyramid pyramid = findOptimalPyramid(pyramidSet, canvasEnv.getCoordinateReferenceSystem());
                
        if(pyramid == null){
            //no reliable pyramid
            return;
        }
        
        final CoordinateReferenceSystem pyramidCRS = pyramid.getCoordinateReferenceSystem();
        final GeneralEnvelope wantedEnv;
        try {
            wantedEnv = new GeneralEnvelope(CRS.transform(canvasEnv, pyramidCRS));
        } catch (TransformException ex) {
            monitor.exceptionOccured(ex, Level.WARNING);
            return;
        }

        //ensure we don't go out of the crs envelope
        final Envelope maxExt = CRS.getEnvelope(pyramidCRS);
        if(maxExt != null){
            wantedEnv.intersect(maxExt);
            if(Double.isNaN(wantedEnv.getMinimum(0))){ wantedEnv.setRange(0, maxExt.getMinimum(0), wantedEnv.getMaximum(0));  }
            if(Double.isNaN(wantedEnv.getMaximum(0))){ wantedEnv.setRange(0, wantedEnv.getMinimum(0), maxExt.getMaximum(0));  }
            if(Double.isNaN(wantedEnv.getMinimum(1))){ wantedEnv.setRange(1, maxExt.getMinimum(1), wantedEnv.getMaximum(1));  }
            if(Double.isNaN(wantedEnv.getMaximum(1))){ wantedEnv.setRange(1, wantedEnv.getMinimum(1), maxExt.getMaximum(1));  }
        }
        
        //the wanted image resolution
        final double wantedResolution;
        try {
            wantedResolution = GO2Utilities.pixelResolution(context2D, wantedEnv);
        } catch (TransformException ex) {
            monitor.exceptionOccured(ex, Level.WARNING);
            return;
        }

        final GridMosaic mosaic = findOptimalMosaic(pyramid, wantedResolution, tolerance, wantedEnv);
        if(mosaic == null){
            //no reliable mosaic
            return;
        }
        
        
        //we definitly do not want some NaN values
        if(Double.isNaN(wantedEnv.getMinimum(0))){ wantedEnv.setRange(0, Double.NEGATIVE_INFINITY, wantedEnv.getMaximum(0));  }
        if(Double.isNaN(wantedEnv.getMaximum(0))){ wantedEnv.setRange(0, wantedEnv.getMinimum(0), Double.POSITIVE_INFINITY);  }
        if(Double.isNaN(wantedEnv.getMinimum(1))){ wantedEnv.setRange(1, Double.NEGATIVE_INFINITY, wantedEnv.getMaximum(1));  }
        if(Double.isNaN(wantedEnv.getMaximum(1))){ wantedEnv.setRange(1, wantedEnv.getMinimum(1), Double.POSITIVE_INFINITY);  }
        
        

        final double tileMatrixMinX = mosaic.getUpperLeftCorner().getX();
        final double tileMatrixMaxY = mosaic.getUpperLeftCorner().getY();
        final Dimension gridSize = mosaic.getGridSize();
        final Dimension tileSize = mosaic.getTileSize();
        final double scale = mosaic.getScale();
        final double tileSpanX = scale * tileSize.width;
        final double tileSpanY = scale * tileSize.height;
        final int gridWidth = gridSize.width;
        final int gridHeight = gridSize.height;
        final double tileWidth = tileSize.width;
        final double tileHeight = tileSize.height;

        //find all the tiles we need --------------------------------------

        final double epsilon = 1e-6;
        final double bBoxMinX = wantedEnv.getMinimum(0);
        final double bBoxMaxX = wantedEnv.getMaximum(0);
        final double bBoxMinY = wantedEnv.getMinimum(1);
        final double bBoxMaxY = wantedEnv.getMaximum(1);
        double tileMinCol = Math.floor( (bBoxMinX - tileMatrixMinX) / tileSpanX + epsilon);
        double tileMaxCol = Math.floor( (bBoxMaxX - tileMatrixMinX) / tileSpanX - epsilon)+1;
        double tileMinRow = Math.floor( (tileMatrixMaxY - bBoxMaxY) / tileSpanY + epsilon);
        double tileMaxRow = Math.floor( (tileMatrixMaxY - bBoxMinY) / tileSpanY - epsilon)+1;

        //ensure we dont go out of the grid
        if(tileMinCol < 0) tileMinCol = 0;
        if(tileMaxCol > gridWidth) tileMaxCol = gridWidth;
        if(tileMinRow < 0) tileMinRow = 0;
        if(tileMaxRow > gridHeight) tileMaxRow = gridHeight;
                
        //don't render layer if it requieres more then 100 queries
        if( (tileMaxCol-tileMinCol) * (tileMaxRow-tileMinRow) > 100 ){
            System.out.println("Too much tiles requiered to render layer at this scale.");
            return;
        }
        
        //tiles to render         
        final Collection<TileReference> queries = new ArrayList<TileReference>();
        final Map hints = new HashMap(item.getUserProperties());
        
        loopCol:
        for(int tileCol=(int)tileMinCol; tileCol<tileMaxCol; tileCol++){
            
            loopRow:
            for(int tileRow=(int)tileMinRow; tileRow<tileMaxRow; tileRow++){

                if(mosaic.isMissing(tileCol, tileRow)){
                    //tile not available
                    continue;
                }
                
                //tile bbox
                final double leftX  = tileMatrixMinX + tileCol * tileSpanX ;
                final double upperY = tileMatrixMaxY - tileRow * tileSpanY;
                final double rightX = tileMatrixMinX + (tileCol+1) * tileSpanX;
                final double lowerY = tileMatrixMaxY - (tileRow+1) * tileSpanY;

                final double scaleX = (rightX - leftX) / tileWidth ;
                final double scaleY = (upperY - lowerY) / tileHeight ;

                final TileReference ref = new TileReference();
                ref.mosaic = mosaic;
                ref.col = tileCol;
                ref.row = tileRow;                
                ref.gridToCRS = new AffineTransform2D(
                        scaleX, 0, 0, -scaleY, leftX, upperY);
                ref.hints = hints;
                
                queries.add(ref);
            }
        }

        paintTiles(context2D, queries);
    }

    /**
     * {@inheritDoc }
     * @param context 
     * @param mask
     * @param filter
     * @param graphics  
     */
    @Override
    public List<Graphic> getGraphicAt(final RenderingContext context, 
            final SearchArea mask, final VisitFilter filter, List<Graphic> graphics) {

        if(!(context instanceof RenderingContext2D) ) return graphics;
        if(!item.isSelectable())                     return graphics;
        if(!item.isVisible())                        return graphics;

        final RenderingContext2D renderingContext = (RenderingContext2D) context;

        final Name coverageName = item.getCoverageName();
        final CachedRule[] rules = GO2Utilities.getValidCachedRules(item.getStyle(),
                renderingContext.getSEScale(), coverageName,null);

        //we perform a first check on the style to see if there is at least
        //one valid rule at this scale, if not we just continue.
        if (rules.length == 0) {
            return graphics;
        }

        if(graphics == null) graphics = new ArrayList<Graphic>();
        if(mask instanceof SearchAreaJ2D){
            //TODO
        }else{
            //TODO
        }

        return graphics;
    }

    
    /**
     * Render each query response as a coverage with the given gridToCRS and CRS.
     * @param context 
     * @param tileRefs 
     */
    private static void paintTiles(final RenderingContext2D context, 
            final Collection<TileReference>  tileRefs) {
        final CanvasMonitor monitor = context.getMonitor();
        
        //bypass all if no queries
        if(tileRefs.isEmpty()){
            return;
        }
        
        //bypass thread creation when only a single tile
        if(tileRefs.size() == 1){
            paintTile(context, tileRefs.iterator().next());            
            return;
        }
        
        final ExecutorService executor = Executors.newFixedThreadPool(6);
        
        for(final TileReference entry : tileRefs){            
            if(monitor.stopRequested()){
                return;
            }
            
            final Runnable call = new Runnable() {
                @Override
                public void run() {
                    paintTile(context, entry);
                }
            };
            
            executor.execute(call);            
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logging.getLogger(StatelessPyramidalCoverageLayerJ2D.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        
    }
    
    private static void paintTile(final RenderingContext2D context, 
            final TileReference tileRef){        
        final CanvasMonitor monitor = context.getMonitor();
        final CoordinateReferenceSystem objCRS2D = context.getObjectiveCRS2D();
                
        if(monitor.stopRequested()){
            return;
        }
        
        
        RenderedImage image;
        try {
            image = tileRef.mosaic.getTile(tileRef.col, tileRef.row, tileRef.hints);
        } catch (DataStoreException ex) {
            monitor.exceptionOccured(ex, Level.WARNING);
            return;
        }
                
        if (image == null) {
            //no tile at this position
            return;
        }

        final GridCoverageFactory gc = new GridCoverageFactory();
        GridCoverage2D coverage;
        
        //check the crs
        final CoordinateReferenceSystem tileCRS = tileRef.mosaic.getPyramid().getCoordinateReferenceSystem();
        if(!CRS.equalsIgnoreMetadata(tileCRS,objCRS2D) ){
            
            //will be reprojected, we must check that image has alpha support
            //otherwise we will have black borders after reprojection
            if(!image.getColorModel().hasAlpha()){
                final BufferedImage buffer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                buffer.createGraphics().drawRenderedImage(image, new AffineTransform());
                image = buffer;
            }
            
            coverage = gc.create("tile", image,
            tileCRS, tileRef.gridToCRS, null, null, null);            
            coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage.view(ViewType.NATIVE), objCRS2D);
            
        }else{
            coverage = gc.create("tile", image,
            tileCRS, tileRef.gridToCRS, null, null, null);
        }
        
        try {
            GO2Utilities.portray(context, coverage);
        } catch (PortrayalException ex) {
            monitor.exceptionOccured(ex, Level.WARNING);
            return;
        }
    }
    
    private static Pyramid findOptimalPyramid(final PyramidSet set, final CoordinateReferenceSystem crs){
        
        Pyramid result = null;
        for(Pyramid pyramid : set.getPyramids()){
            
            if(result == null){
                result = pyramid;
            }
            
            if(CRS.equalsApproximatively(pyramid.getCoordinateReferenceSystem(),crs)){
                //we found a pyramid for this crs
                result = pyramid;
                break;
            }
            
        }
        
        return result;
    }
    
    private static GridMosaic findOptimalMosaic(final Pyramid pyramid, final double resolution, 
            final double tolerance, final Envelope env){
        
        GridMosaic result = null;        
        final double[] scales = pyramid.getScales();
        
        for(int i=0;i<scales.length;i++){
            final double scale = scales[i];            
                        
            final GridMosaic candidate = pyramid.getMosaic(i);            
            if(result == null){
                result = candidate;
            }
            
            //check if it will not requiere too much tiles
            final Dimension tileSize = candidate.getTileSize();
            double nbtileX = env.getSpan(0) / (tileSize.width*scale);
            double nbtileY = env.getSpan(1) / (tileSize.height*scale);
            
            //if the envelope has some NaN, we presume it's a square
            if(Double.isNaN(nbtileX) || Double.isInfinite(nbtileX)){
                nbtileX = nbtileY;
            }else if(Double.isNaN(nbtileY) || Double.isInfinite(nbtileY)){
                nbtileY = nbtileX;
            }
            
            if(nbtileX*nbtileY > 100){
                //we haven't reach the best resolution, it would requiere
                //too much tiles, we use the previous scale level
                break;
            }
            
            result = candidate;
            
            if( (scale * (1-tolerance)) < resolution){                      
                //we found the most accurate resolution
                break;
            }           
        }
                
        return result;
    }
        
}
