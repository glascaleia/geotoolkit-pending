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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.geotoolkit.util.logging.Logging;

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

    public AffineTransform getInverse(){
        try {
            return transform.createInverse();
        } catch (NoninvertibleTransformException ex) {
            Logging.getLogger(JTSGeometryJ2D.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Rectangle2D r) {
        return contains(r.getMinX(),r.getMinY(),r.getWidth(),r.getHeight());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Point2D p) {
        final AffineTransform inverse = getInverse();
        inverse.transform(p, p);
        final Coordinate coord = new Coordinate(p.getX(), p.getY());
        final Geometry point = geometry.getFactory().createPoint(coord);
        return geometry.contains(point);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(double x, double y) {
        final Point2D p = new Point2D.Double(x, y);
        return contains(p);
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
        final Envelope env = geometry.getEnvelopeInternal();
        final Point2D p1 = new Point2D.Double(env.getMinX(), env.getMinY());
        transform.transform(p1, p1);
        final Point2D p2 = new Point2D.Double(env.getMaxX(), env.getMaxY());
        transform.transform(p2, p2);
        return new Rectangle(
                (int)p1.getX(), (int)p1.getY(),
                (int)(p2.getX()-p1.getX()),
                (int)(p2.getY()-p1.getY()));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Rectangle2D getBounds2D() {
        final Envelope env = geometry.getEnvelopeInternal();
        final Point2D p1 = new Point2D.Double(env.getMinX(), env.getMinY());
        transform.transform(p1, p1);
        final Point2D p2 = new Point2D.Double(env.getMaxX(), env.getMaxY());
        transform.transform(p2, p2);
        return new Rectangle2D.Double(
                p1.getX(),p1.getY(),
                p2.getX()-p1.getX(),
                p2.getY()-p1.getY());
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
            concat.preConcatenate(at);
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
        final AffineTransform inverse = getInverse();
        final Point2D p1 = new Point2D.Double(x, y);
        inverse.transform(p1, p1);
        final Point2D p2 = new Point2D.Double(x+w, y+h);
        inverse.transform(p2, p2);

        final Coordinate[] coords = {
            new Coordinate(p1.getX(), p1.getY()),
            new Coordinate(p1.getX(), p2.getY()),
            new Coordinate(p2.getX(), p2.getY()),
            new Coordinate(p2.getX(), p1.getY()),
            new Coordinate(p1.getX(), p1.getY())
        };
        final LinearRing lr = geometry.getFactory().createLinearRing(coords);
        return geometry.getFactory().createPolygon(lr, null);
    }

}
