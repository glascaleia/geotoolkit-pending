/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Johann Sorel
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

package org.geotoolkit.display2d.ext.grid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.display.axis.Graduation;
import org.geotoolkit.display.axis.NumberGraduation;
import org.geotoolkit.display.axis.TickIterator;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.DefaultProjectedGeometry;
import org.geotoolkit.display2d.style.labeling.DefaultLabelLayer;
import org.geotoolkit.display2d.style.labeling.DefaultLinearLabelDescriptor;
import org.geotoolkit.display2d.style.labeling.LabelLayer;
import org.geotoolkit.display2d.style.labeling.LabelRenderer;
import org.geotoolkit.display2d.style.labeling.LinearLabelDescriptor;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * Utility class to render grid on J2DCanvas.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class J2DGridUtilities {

    private J2DGridUtilities() {
    }

    public static void paint(RenderingContext2D context, GridTemplate template){

        CoordinateReferenceSystem gridCRS = template.getCRS();
        //use context crs if gridcrs is not defined
        if(gridCRS == null) gridCRS = context.getObjectiveCRS();

        final Graphics2D g = context.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final Shape shp = context.getCanvasObjectiveShape();
        final List<Coordinate> coords = new ArrayList<Coordinate>();
        final PathIterator ite = shp.getPathIterator(new AffineTransform());

        final double[] vals = new double[3];
        while(!ite.isDone()){
            ite.currentSegment(vals);
            coords.add( new Coordinate(vals[0],vals[1]));
            ite.next();
        }

        final double[] gridResolution = context.getResolution(gridCRS);
        final GeometryFactory fact = new GeometryFactory();
        final LinearRing ring = fact.createLinearRing(coords.toArray(new Coordinate[coords.size()]));
        final Polygon bounds = fact.createPolygon(ring, new LinearRing[0]);

        final LabelRenderer renderer = context.getLabelRenderer(true);
        final LabelLayer layer = new DefaultLabelLayer(false, true);

        final RenderingHints tickHint = new RenderingHints(null);
        tickHint.put(Graduation.VISUAL_AXIS_LENGTH, context.getCanvasDisplayBounds().width);
        tickHint.put(Graduation.VISUAL_TICK_SPACING, 200);

        try{
            final Envelope gridBounds = CRS.transform(context.getCanvasObjectiveBounds(), gridCRS);
            final MathTransform gridToObj = CRS.findMathTransform(gridCRS, context.getObjectiveCRS(), true);
            final MathTransform inverse = gridToObj.inverse();
            final MathTransform objToDisp = context.getObjectiveToDisplay();

            //grid on X axis ---------------------------------------------------

            final NumberGraduation graduationX = new NumberGraduation(null);
            graduationX.setRange(gridBounds.getMinimum(0), gridBounds.getMaximum(0),
                    gridBounds.getCoordinateReferenceSystem().getCoordinateSystem().getAxis(0).getUnit());

            TickIterator tickIte = graduationX.getTickIterator(tickHint, null);

            while(!tickIte.isDone()){
                tickIte.next();
                final String label = tickIte.currentLabel();
                final double d = tickIte.currentPosition();

                final ArrayList<Coordinate> lineCoords = new ArrayList<Coordinate>();
                final double maxY = gridBounds.getMaximum(1);
                for(double k= gridBounds.getMinimum(1); k<maxY; k+=gridResolution[1]){
                    lineCoords.add(new Coordinate(d, k));
                }
                lineCoords.add(new Coordinate(d, maxY));

                Geometry ls = fact.createLineString(lineCoords.toArray(new Coordinate[lineCoords.size()]));
                ls = JTS.transform(ls, gridToObj);

                if(ls == null) continue;

                final Geometry geom = ls.intersection(bounds);
                final DefaultProjectedGeometry pg = new DefaultProjectedGeometry(geom);
                pg.setObjToDisplay(objToDisp);

                final LinearLabelDescriptor desc;
                if(tickIte.isMajorTick()){
                    desc = new DefaultLinearLabelDescriptor(
                        label, template.getMainLabelFont(), template.getMainLabelPaint(),
                        template.getMainHaloWidth(), template.getMainHaloPaint(),
                        0, 10, 3,
                        false, false, false,
                        pg);
                }else{
                    desc = new DefaultLinearLabelDescriptor(
                        label, template.getLabelFont(), template.getLabelPaint(),
                        template.getHaloWidth(), template.getHaloPaint(),
                        0, 10, 3,
                        false, false, false,
                        pg);
                }
                layer.labels().add(desc);

                if(tickIte.isMajorTick()){
                    g.setPaint(template.getMainLinePaint());
                    g.setStroke(template.getMainLineStroke());
                }else{
                    g.setPaint(template.getLinePaint());
                    g.setStroke(template.getLineStroke());
                }
                
                g.draw(pg.getDisplayShape());
            }

            //grid on Y axis ---------------------------------------------------

            final NumberGraduation graduationY = new NumberGraduation(null);
            graduationY.setRange(gridBounds.getMinimum(1), gridBounds.getMaximum(1),
                    gridBounds.getCoordinateReferenceSystem().getCoordinateSystem().getAxis(1).getUnit());

            tickIte = graduationY.getTickIterator(tickHint, null);

            while(!tickIte.isDone()){
                tickIte.next();
                final String label = tickIte.currentLabel();
                final double d = tickIte.currentPosition();

                final ArrayList<Coordinate> lineCoords = new ArrayList<Coordinate>();
                final double maxX = gridBounds.getMaximum(0);
                for(double k= gridBounds.getMinimum(0); k<maxX; k+=gridResolution[1]){
                    lineCoords.add(new Coordinate(k, d));
                }
                lineCoords.add(new Coordinate(maxX, d));

                Geometry ls = fact.createLineString(lineCoords.toArray(new Coordinate[lineCoords.size()]));
                ls = JTS.transform(ls, gridToObj);

                final Geometry geom = ls.intersection(bounds);
                final DefaultProjectedGeometry pg = new DefaultProjectedGeometry(geom);
                pg.setObjToDisplay(objToDisp);

                final LinearLabelDescriptor desc;
                if(tickIte.isMajorTick()){
                    desc = new DefaultLinearLabelDescriptor(
                        label, template.getMainLabelFont(), template.getMainLabelPaint(),
                        template.getMainHaloWidth(), template.getMainHaloPaint(),
                        0, 10, 3,
                        false, false, false,
                        pg);
                }else{
                    desc = new DefaultLinearLabelDescriptor(
                        label, template.getLabelFont(), template.getLabelPaint(),
                        template.getHaloWidth(), template.getHaloPaint(),
                        0, 10, 3,
                        false, false, false,
                        pg);
                }

                layer.labels().add(desc);

                if(tickIte.isMajorTick()){
                    g.setPaint(template.getMainLinePaint());
                    g.setStroke(template.getMainLineStroke());
                }else{
                    g.setPaint(template.getLinePaint());
                    g.setStroke(template.getLineStroke());
                }
                
                g.draw(pg.getDisplayShape());
            }


        }catch(Exception ex){
            ex.printStackTrace();
        }

        renderer.portrayImmidiately(layer);
    }


}
