/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.display2d.container.statefull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.display.canvas.VisitFilter;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.primitive.GraphicJ2D;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.primitive.SearchArea;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.style.CachedRule;
import org.geotoolkit.display2d.style.CachedSymbolizer;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.container.stateless.StatelessMapLayerJ2D;
import org.geotoolkit.display2d.primitive.DefaultSearchAreaJ2D;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.geometry.jts.transform.CoordinateSequenceMathTransformer;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.GraphicBuilder;
import org.geotoolkit.referencing.operation.transform.AffineTransform2D;

import org.opengis.display.primitive.Graphic;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class StatefullCoverageLayerJ2D extends StatelessMapLayerJ2D<CoverageMapLayer>{

    private final StatefullProjectedCoverage projectedCoverage;

    //compare values to update caches if necessary
    private final StatefullContextParams params;
    private CoordinateReferenceSystem lastObjectiveCRS = null;

    public StatefullCoverageLayerJ2D(final J2DCanvas canvas, final CoverageMapLayer layer){
        super(canvas, layer, true);

        try {
            final GeneralGridGeometry ggg = layer.getCoverageReader().getGridGeometry(0);
            if(ggg == null){
                Logger.getLogger(StatefullCoverageLayerJ2D.class.getName()).log(
                        Level.WARNING, "Could not access envelope of layer {0}", layer.getName());
            }
            
        } catch (CoverageStoreException ex) {
            Logger.getLogger(StatefullCoverageLayerJ2D.class.getName()).log(Level.WARNING, null, ex);
        }


        params = new StatefullContextParams(canvas,null);
        this.projectedCoverage = new StatefullProjectedCoverage(params, layer);
    }

    private synchronized void updateCache(final RenderingContext2D context){
        params.objectiveCRS = context.getObjectiveCRS();
        params.displayCRS = context.getDisplayCRS();
        params.context = context;
        boolean objectiveCleared = false;

        //clear objective cache is objective crs changed -----------------------
        //todo use only the 2D CRS, the transform parameters are only used for the border
        //geometry if needed, the gridcoverageReader will handle itself the transform
        final CoordinateReferenceSystem objectiveCRS2D = context.getObjectiveCRS2D();
        if(objectiveCRS2D != lastObjectiveCRS){
            params.objectiveToDisplay.setToIdentity();
            lastObjectiveCRS = objectiveCRS2D;
            objectiveCleared = true;
            projectedCoverage.clearObjectiveCache();
        }

        //clear display cache if needed ----------------------------------------
        final AffineTransform2D objtoDisp = context.getObjectiveToDisplay();

        if(!objtoDisp.equals(params.objectiveToDisplay)){
            params.objectiveToDisplay.setTransform(objtoDisp);
            ((CoordinateSequenceMathTransformer)params.objToDisplayTransformer.getCSTransformer())
                    .setTransform(objtoDisp);

            if(!objectiveCleared){
                //no need to clear the display cache if the objective clear has already been called
                projectedCoverage.clearDisplayCache();
            }

        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void paintLayer(final RenderingContext2D renderingContext) {
                     
        final Name coverageName = item.getCoverageName();
        final CachedRule[] rules = GO2Utilities.getValidCachedRules(item.getStyle(),
                renderingContext.getSEScale(), coverageName,null);

        //we perform a first check on the style to see if there is at least
        //one valid rule at this scale, if not we just continue.
        if (rules.length == 0) {
            return;
        }

        paintRaster(item, rules, renderingContext);
    }

    private void paintRaster(final CoverageMapLayer item, final CachedRule[] rules,
            final RenderingContext2D context) {
        updateCache(context);

        //search for a special graphic renderer
        final GraphicBuilder<GraphicJ2D> builder = (GraphicBuilder<GraphicJ2D>) item.getGraphicBuilder(GraphicJ2D.class);
        if(builder != null){
            //this layer has a special graphic rendering, use it instead of normal rendering
            final Collection<GraphicJ2D> graphics = builder.createGraphics(item, canvas);
            for(GraphicJ2D gra : graphics){
                gra.paint(context);
            }
            return;
        }
        
        if(!intersects(context.getCanvasObjectiveBounds2D())){
            //grid not in the envelope, we have finisehd
            return;
        }
        
        for(final CachedRule rule : rules){
            for(final CachedSymbolizer symbol : rule.symbolizers()){
                try {
                    GO2Utilities.portray(projectedCoverage, symbol, context);
                } catch (PortrayalException ex) {
                    context.getMonitor().exceptionOccured(ex, Level.WARNING);
                }
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Graphic> getGraphicAt(final RenderingContext context, final SearchArea mask, final VisitFilter filter, List<Graphic> graphics) {

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
            graphics = searchAt(item,rules,renderingContext,(SearchAreaJ2D)mask,filter,graphics);
        }else{
            graphics = searchAt(item,rules,renderingContext,new DefaultSearchAreaJ2D(mask),filter,graphics);
        }
        

        return graphics;
    }

    private List<Graphic> searchAt(final CoverageMapLayer layer, final CachedRule[] rules,
            final RenderingContext2D renderingContext, final SearchAreaJ2D mask, final VisitFilter filter, List<Graphic> graphics) {
        updateCache(renderingContext);

        final GraphicBuilder<GraphicJ2D> builder = (GraphicBuilder<GraphicJ2D>) layer.getGraphicBuilder(GraphicJ2D.class);
        if(builder != null){
            //this layer hasa special graphic rendering, use it instead of normal rendering
            final Collection<GraphicJ2D> gras = builder.createGraphics(layer, canvas);
            for(final GraphicJ2D gra : gras){
                graphics = gra.getGraphicAt(renderingContext, mask, filter,graphics);
            }
            return graphics;
        }
       

        for (final CachedRule rule : rules) {
            for (final CachedSymbolizer symbol : rule.symbolizers()) {
                if(GO2Utilities.hit(projectedCoverage, symbol, renderingContext, mask, filter)){
                    graphics.add(projectedCoverage);
                    break;
                }
            }
        }

        return graphics;
    }

    @Override
    public void dispose() {
        projectedCoverage.dispose();
        super.dispose();
    }

}
