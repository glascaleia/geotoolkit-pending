/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004 - 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.display2d.primitive.jts;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A thin wrapper that adapts a JTS geometry to the Shape interface so that the geometry can be used
 * by java2d without coordinate cloning.
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @version 2.9
 * @module pending
 */
public class JTSGeometryJ2D implements Shape, Cloneable {

    protected JTSGeometryIterator<? extends Geometry> iterator = null;

    /** The wrapped JTS geometry */
    protected Geometry geometry;

    /** An additional AffineTransform */
    protected final AffineTransform transform;

    public JTSGeometryJ2D(Geometry geom) {
        this(geom, new AffineTransform());
    }

    /**
     * Creates a new GeometryJ2D object.
     *
     * @param geom - the wrapped geometry
     */
    public JTSGeometryJ2D(Geometry geom, AffineTransform trs) {
        this.geometry = geom;
        this.transform = trs;
    }

    /**
     * Sets the geometry contained in this lite shape. Convenient to reuse this
     * object instead of creating it again and again during rendering
     *
     * @param g
     */
    public void setGeometry(Geometry g) {
        this.geometry = g;

        //change iterator only if necessary
        if(iterator != null && geometry != null){
            if (this.geometry.isEmpty() && iterator instanceof JTSEmptyIterator) {
                //nothing to do
            }else if (this.geometry instanceof Point && iterator instanceof JTSPointIterator) {
                ((JTSPointIterator)iterator).setGeometry((Point)geometry);
            } else if (this.geometry instanceof Polygon && iterator instanceof JTSPolygonIterator) {
                ((JTSPolygonIterator)iterator).setGeometry((Polygon)geometry);
            } else if (this.geometry instanceof LineString && iterator instanceof JTSLineIterator) {
                ((JTSLineIterator)iterator).setGeometry((LineString)geometry);
            } else if (this.geometry instanceof GeometryCollection && iterator instanceof JTSGeomCollectionIterator) {
                ((JTSGeomCollectionIterator)iterator).setGeometry((GeometryCollection)geometry);
            }else{
                //iterator does not match the new geometry type
                iterator = null;
            }
        }

    }

    /**
     * @return the current wrapped geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Rectangle2D r) {
        Geometry rect = createRectangle(
                r.getMinX(),
                r.getMinY(),
                r.getWidth(),
                r.getHeight());
        return geometry.contains(rect);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Point2D p) {
        Coordinate coord = new Coordinate(p.getX(), p.getY());
        Geometry point = geometry.getFactory().createPoint(coord);
        return geometry.contains(point);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(double x, double y) {
        Coordinate coord = new Coordinate(x, y);
        Geometry point = geometry.getFactory().createPoint(coord);
        return geometry.contains(point);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(double x, double y, double w, double h) {
        Geometry rect = createRectangle(x, y, w, h);
        return geometry.contains(rect);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Rectangle getBounds() {
        Envelope env = geometry.getEnvelopeInternal();
        return new Rectangle(
                (int)(env.getMinX()),
                (int)(env.getMinY()),
                (int)(env.getWidth()),
                (int)(env.getHeight()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Rectangle2D getBounds2D() {
        Envelope env = geometry.getEnvelopeInternal();
        return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at) {

        final AffineTransform concat;
        if(at == null){
            concat = transform;
        }else{
            concat = (AffineTransform) transform.clone();
            concat.concatenate(at);
        }

        if(iterator == null){
            if (this.geometry.isEmpty()) {
                iterator = JTSEmptyIterator.INSTANCE;
            }else if (this.geometry instanceof Point) {
                iterator = new JTSPointIterator((Point) geometry, concat);
            } else if (this.geometry instanceof Polygon) {
                iterator = new JTSPolygonIterator((Polygon) geometry, concat);
            } else if (this.geometry instanceof LineString) {
                iterator = new JTSLineIterator((LineString)geometry, concat);
            } else if (this.geometry instanceof GeometryCollection) {
                iterator = new JTSGeomCollectionIterator((GeometryCollection)geometry,concat);
            }
        }else{
            iterator.setTransform(concat);
        }

        return iterator;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        Geometry rect = createRectangle(
                r.getMinX(),
                r.getMinY(),
                r.getWidth(),
                r.getHeight());
        return geometry.intersects(rect);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean intersects(double x, double y, double w, double h) {
        Geometry rect = createRectangle(x, y, w, h);
        return geometry.intersects(rect);
    }

    /**
     * Creates a jts Geometry object representing a rectangle with the given
     * parameters
     *
     * @param x left coordinate
     * @param y bottom coordinate
     * @param w width
     * @param h height     *
     * @return a rectangle with the specified position and size
     */
    private Geometry createRectangle(double x, double y, double w, double h) {
        Coordinate[] coords = {
            new Coordinate(x, y), new Coordinate(x, y + h),
            new Coordinate(x + w, y + h), new Coordinate(x + w, y),
            new Coordinate(x, y)
        };
        LinearRing lr = geometry.getFactory().createLinearRing(coords);
        return geometry.getFactory().createPolygon(lr, null);
    }

}
