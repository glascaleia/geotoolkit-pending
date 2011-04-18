/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009-2011, Johann Sorel
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
package org.geotoolkit.gui.swing.go2.control.edition;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.Feature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.gui.swing.go2.JMap2D;
import org.geotoolkit.gui.swing.propertyedit.JFeatureOutLine;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.operation.transform.AffineTransform2D;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.util.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import static org.geotoolkit.util.ArgumentChecks.*;

/**
 *
 * @author Johann Sorel
 * @module pending
 */
public class EditionHelper {

    private static final Logger LOGGER = Logging.getLogger(EditionHelper.class);
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
                                                new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));
    public static final Coordinate[] EMPTY_COORDINATE_ARRAY = new Coordinate[0];
    

    public static class EditionContext{
        public Feature feature = null;
        public Geometry geometry = null;
        public final List<Geometry> subGeometries =  new ArrayList<Geometry>();
        public int subGeometryIndex = -1;
        public int[] nodes = null;
        public final List<Coordinate> coords = new ArrayList<Coordinate>();
        public boolean modified = false;
        public boolean added = false;

        public void reset(){
            feature = null;
            geometry = null;
            subGeometries.clear();
            subGeometryIndex = -1;
            nodes = null;
            modified = false;
            added = false;
            coords.clear();
        }
    }

    private final JMap2D map;
    private final FeatureMapLayer editedLayer;

    EditionHelper(final JMap2D map, final FeatureMapLayer editedLayer) {
        this.map = map;
        this.editedLayer = editedLayer;
    }

    /**
     * transform a mouse coordinate in JTS Geometry using the CRS of the map context
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS geometry (corresponding to a square of 6x6 pixel around mouse coordinate)
     */
    public Polygon mousePositionToGeometry(final int mx, final int my) throws NoninvertibleTransformException {
        final Coordinate[] coord = new Coordinate[5];
        int taille = 4;

        coord[0] = toCoord(mx - taille, my - taille);
        coord[1] = toCoord(mx - taille, my + taille);
        coord[2] = toCoord(mx + taille, my + taille);
        coord[3] = toCoord(mx + taille, my - taille);
        coord[4] = coord[0];

        final LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
        return GEOMETRY_FACTORY.createPolygon(lr1, null);
    }

    public Point toJTS(final int x, final int y){
        final Coordinate coord = toCoord(x, y);
        final Point geom = GEOMETRY_FACTORY.createPoint(coord);
        return geom;
    }

    public Coordinate toCoord(final int x, final int y){
        final AffineTransform2D trs = map.getCanvas().getController().getTransform();
        AffineTransform dispToObj;
        try {
            dispToObj = trs.createInverse();
        } catch (NoninvertibleTransformException ex) {
            dispToObj = new AffineTransform();
            LOGGER.log(Level.WARNING, null, ex);
        }
        final double[] crds = new double[]{x,y};
        dispToObj.transform(crds, 0, crds, 0, 1);
        return new Coordinate(crds[0], crds[1]);
    }

    public Feature grabFeature(final int mx, final int my, final boolean style) {

        if(editedLayer == null) return null;

        Feature candidate = null;

        FeatureCollection<Feature> editgeoms = null;
        FeatureIterator<Feature> fi = null;
        try {
            final Polygon geo = mousePositionToGeometry(mx, my);
            final Filter flt = toFilter(geo, editedLayer);
            editgeoms = (FeatureCollection<Feature>) editedLayer.getCollection().subCollection(
                    QueryBuilder.filtered(editedLayer.getCollection().getFeatureType().getName(), flt));

            fi = editgeoms.iterator();
            if (fi.hasNext()) {
                Feature sf = fi.next();
                return sf;
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }finally{
            if(fi != null){
                fi.close();
            }
        }
        
        return candidate;
    }

    public boolean grabGeometrynode(final Point pt, final int mx, final int my){
        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);
            return pt.intersects(mouseGeo);
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
            return false;
        }
    }

    /**
     * grab a node in the given geometry.
     * int[0] == subgeometry index
     * int[1] == grabbed coordinate index
     * int[2] == grabbed coordinate index
     * there might be two coordinate grab in the case of polygon last point
     */
    public int[] grabGeometryNode(final Geometry geo, final int mx, final int my) {
        final int[] indexes = new int[]{-1,-1,-1};

        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);

            for (int i=0,n=geo.getNumGeometries(); i<n; i++) {
                final Geometry subgeo = geo.getGeometryN(i);

                if (subgeo.intersects(mouseGeo)) {
                    //this geometry intersect the mouse
                    indexes[0] = i;

                    final Coordinate[] coos = subgeo.getCoordinates();
                    for (int j=0,m=coos.length; j<m; j++) {
                        final Coordinate coo = coos[j];
                        final Point p = createPoint(coo);
                        if (p.intersects(mouseGeo)) {

                            if ((j==0 || j==m-1) && (geo instanceof Polygon || geo instanceof MultiPolygon)) {
                                //first and last coordinate index are the same point
                                indexes[1] = 0;
                                indexes[2] = m-1;
                            } else {
                                //coordinate is in the middle of the geometry
                                indexes[1] = j;
                                indexes[2] = j;
                            }
                        }
                    }
                    break;
                }

            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return indexes;
    }

    public void dragGeometryNode(final EditionContext context, final int mx, final int my) {
        final Coordinate mouseCoord = toCoord(mx, my);

        final Geometry subgeo = context.subGeometries.get(0);
        if(subgeo == null) return;

        final int[] nodeIndexes = context.nodes;
        if(nodeIndexes == null) return;

        context.modified = true;

        if(context.geometry instanceof Point){
            Point pt = (Point) context.geometry;
            pt.getCoordinate().x = mouseCoord.x;
            pt.getCoordinate().y = mouseCoord.y;
        }else{
            for (int index : nodeIndexes) {
                subgeo.getCoordinates()[index].x = mouseCoord.x;
                subgeo.getCoordinates()[index].y = mouseCoord.y;
            }
        }


        subgeo.geometryChanged();
        context.geometry.geometryChanged();
    }

    public void moveGeometry(final Geometry geo, final int dx, final int dy) {

        try{
            final Point2D pt0 = map.getCanvas().getController().getTransform().inverseTransform(new Point2D.Double(0, 0), null);
            final Point2D pt = map.getCanvas().getController().getTransform().inverseTransform(new Point2D.Double(dx, dy), null);
            pt.setLocation(pt.getX()-pt0.getX(), pt.getY()-pt0.getY());

            for (int i=0,n=geo.getNumGeometries(); i<n; i++) {
                final Geometry subgeo = geo.getGeometryN(i);
                final Coordinate[] coos = subgeo.getCoordinates();

                for (int j=0,m=coos.length; j<m; j++) {
                    final Coordinate coo = coos[j];
                    coo.x += pt.getX();
                    coo.y += pt.getY();
                }
                subgeo.geometryChanged();
            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }
        geo.geometryChanged();

    }

    public void moveSubGeometry(final Geometry geo, final int indice, final int dx, final int dy) {

        try{
            final Point2D pt0 = map.getCanvas().getController().getTransform().inverseTransform(new Point2D.Double(0, 0), null);
            final Point2D pt = map.getCanvas().getController().getTransform().inverseTransform(new Point2D.Double(dx, dy), null);
            pt.setLocation(pt.getX()-pt0.getX(), pt.getY()-pt0.getY());

            final Geometry subgeo = geo.getGeometryN(indice);
            final Coordinate[] coos = subgeo.getCoordinates();

            for (int j=0,m=coos.length; j<m; j++) {
                final Coordinate coo = coos[j];
                coo.x += pt.getX();
                coo.y += pt.getY();
            }
            subgeo.geometryChanged();

        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }
        geo.geometryChanged();

    }


    public Geometry insertNode(final Polygon geo, final int mx, final int my) {
        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);
            final Geometry mousePoint = toJTS(mx, my);

            if (geo.intersects(mouseGeo)) {
                //this geometry intersect the mouse

                final Coordinate[] coos = geo.getCoordinates();
                for (int j=0,m=coos.length-1; j<m; j++) {
                    //find the segment that intersect
                    final Coordinate coo1 = coos[j];
                    final Coordinate coo2 = coos[j+1];
                    final Geometry segment = createLine(coo1,coo2);

                    if(mouseGeo.intersects(segment) && segment.getEnvelope().intersects(mousePoint)){
                        //we must add the new node on this segment

                        final List<Coordinate> ncs = new ArrayList<Coordinate>();
                        for (int d=0,p=coos.length; d<p; d++) {
                            ncs.add(coos[d]);
                            if(d==j){
                                //we must add the new node here
                                ncs.add(mousePoint.getCoordinate());
                            }
                         }

                        return createPolygon(ncs);
                    }
                }
            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return geo;
    }

    public Geometry insertNode(final LineString geo, final int mx, final int my) {
        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);
            final Geometry mousePoint = toJTS(mx, my);

            if (geo.intersects(mouseGeo)) {
                //this geometry intersect the mouse

                final Coordinate[] coos = geo.getCoordinates();
                for (int j=0,m=coos.length-1; j<m; j++) {
                    //find the segment that intersect
                    final Coordinate coo1 = coos[j];
                    final Coordinate coo2 = coos[j+1];
                    final Geometry segment = createLine(coo1,coo2);

                    if(mouseGeo.intersects(segment) && segment.getEnvelope().intersects(mousePoint)){
                        //we must add the new node on this segment

                        final List<Coordinate> ncs = new ArrayList<Coordinate>();
                        for (int d=0,p=coos.length; d<p; d++) {
                            ncs.add(coos[d]);
                            if(d==j){
                                //we must add the new node here
                                ncs.add(mousePoint.getCoordinate());
                            }
                         }

                        return createLine(ncs);
                    }
                }
            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return geo;
    }


    public Geometry insertNode(final GeometryCollection geo, final int mx, final int my) {
        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);
            final Geometry mousePoint = toJTS(mx, my);

            for (int i=0,n=geo.getNumGeometries(); i<n; i++) {
                final Geometry subgeo = geo.getGeometryN(i);

                if (subgeo.intersects(mouseGeo)) {
                    //this geometry intersect the mouse

                    final Coordinate[] coos = subgeo.getCoordinates();
                    for (int j=0,m=coos.length-1; j<m; j++) {
                        //find the segment that intersect
                        final Coordinate coo1 = coos[j];
                        final Coordinate coo2 = coos[j+1];
                        final Geometry segment = createLine(coo1,coo2);

                        if(mouseGeo.intersects(segment) && segment.getEnvelope().intersects(mousePoint)){
                            //we must add the new node on this segment

                            final List<Geometry> subs = new ArrayList<Geometry>();
                            for (int k=0,l=geo.getNumGeometries(); k<l; k++) {
                                if(k==i){
                                    //this subgeo must be changed
                                    final List<Coordinate> ncs = new ArrayList<Coordinate>();
                                    for (int d=0,p=coos.length; d<p; d++) {
                                        ncs.add(coos[d]);
                                        if(d==j){
                                            //we must add the new node here
                                            ncs.add(mousePoint.getCoordinate());
                                        }
                                    }

                                    if(geo instanceof MultiLineString && ncs.size() >1){
                                         subs.add(createLine(ncs));
                                    }else if(geo instanceof MultiPolygon && ncs.size() >2){
                                         subs.add(createPolygon(ncs));
                                    }
                                }else{
                                    subs.add(geo.getGeometryN(k));
                                }
                            }

                            if(geo instanceof MultiLineString && !subs.isEmpty()){
                                 return createMultiLine(subs);
                            }else if(geo instanceof MultiPolygon && !subs.isEmpty()){
                                 return createMultiPolygon(subs);
                            }else{
                                return null;
                            }
                        }

                    }
                    break;
                }

            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return geo;
    }

    public Geometry deleteNode(final Polygon geo, final int mx, final int my) {

        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);

            if (geo.intersects(mouseGeo)) {
                //this geometry intersect the mouse

                final Coordinate[] coos = geo.getCoordinates();
                for (int j=0,m=coos.length; j<m; j++) {
                    final Coordinate coo = coos[j];
                    final Point p = createPoint(coo);
                    if (p.intersects(mouseGeo)) {
                        //delete this node

                        final List<Coordinate> ncs = new ArrayList<Coordinate>();
                        for (int d=0,z=coos.length; d<z; d++) {
                            if(d!=j){
                                ncs.add(coos[d]);
                            }
                        }

                        if(ncs.size() > 2){
                            return createPolygon(ncs);
                        }
                        break;
                    }
                }
            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return geo;
    }

    public Geometry deleteNode(final LineString geo, final int mx, final int my) {

        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);

            if (geo.intersects(mouseGeo)) {
                //this geometry intersect the mouse

                final Coordinate[] coos = geo.getCoordinates();
                for (int j=0,m=coos.length; j<m; j++) {
                    final Coordinate coo = coos[j];
                    final Point p = createPoint(coo);
                    if (p.intersects(mouseGeo)) {
                        //delete this node

                        final List<Coordinate> ncs = new ArrayList<Coordinate>();
                        for (int d=0,z=coos.length; d<z; d++) {
                            if(d!=j){
                                ncs.add(coos[d]);
                            }
                        }

                        if(ncs.size() > 1){
                            return createLine(ncs);
                        }
                        break;
                    }
                }
            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return geo;
    }


    public Geometry deleteNode(final GeometryCollection geo, final int mx, final int my) {

        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);

            for (int i=0,n=geo.getNumGeometries(); i<n; i++) {
                final Geometry subgeo = geo.getGeometryN(i);

                if (subgeo.intersects(mouseGeo)) {
                    //this geometry intersect the mouse

                    final Coordinate[] coos = subgeo.getCoordinates();
                    for (int j=0,m=coos.length; j<m; j++) {
                        final Coordinate coo = coos[j];
                        final Point p = createPoint(coo);
                        if (p.intersects(mouseGeo)) {
                            //delete this node

                            final List<Geometry> subs = new ArrayList<Geometry>();
                            for (int k=0,l=geo.getNumGeometries(); k<l; k++) {
                                if(k==i){
                                    //this subgeo must be changed
                                    final List<Coordinate> ncs = new ArrayList<Coordinate>();
                                    for (int d=0,z=coos.length; d<z; d++) {
                                        if(d!=j){
                                            ncs.add(coos[d]);
                                        }
                                    }
                                    if(geo instanceof MultiLineString && ncs.size() >1){
                                         subs.add(createLine(ncs));
                                    }else if(geo instanceof MultiPolygon && ncs.size() >2){
                                         subs.add(createPolygon(ncs));
                                    }
                                }else{
                                    subs.add(geo.getGeometryN(k));
                                }
                            }

                            if(geo instanceof MultiLineString && !subs.isEmpty()){
                                 return createMultiLine(subs);
                            }else if(geo instanceof MultiPolygon && !subs.isEmpty()){
                                 return createMultiPolygon(subs);
                            }else{
                                return null;
                            }

                        }
                    }
                    break;
                }

            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return geo;
    }

    public Geometry deleteSubGeometry(final GeometryCollection geo, final int mx, final int my) {

        try{
            //transform our mouse in a geometry
            final Geometry mouseGeo = mousePositionToGeometry(mx, my);

            for (int i=0,n=geo.getNumGeometries(); i<n; i++) {
                final Geometry subgeo = geo.getGeometryN(i);

                if (subgeo.intersects(mouseGeo)) {
                    //this geometry intersect the mouse

                    final List<Geometry> subs = new ArrayList<Geometry>();
                    for (int k=0,l=geo.getNumGeometries(); k<l; k++) {
                        if(k!=i){
                            subs.add(geo.getGeometryN(k));
                        }
                    }

                    if(geo instanceof MultiLineString && !subs.isEmpty()){
                         return createMultiLine(subs);
                    }else if(geo instanceof MultiPolygon && !subs.isEmpty()){
                         return createMultiPolygon(subs);
                    }else if(geo instanceof MultiPoint && !subs.isEmpty()){
                         return createMultiPoint(subs);
                    }else{
                        return null;
                    }
                }

            }
        }catch(final Exception ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        return geo;
    }


    public Geometry toObjectiveCRS(final Feature sf){
        final Object obj = sf.getDefaultGeometryProperty().getValue();

        if (obj instanceof Geometry) {
            try{
                Geometry geom = (Geometry) obj;

                final MathTransform trs = CRS.findMathTransform(
                        editedLayer.getCollection().getFeatureType().getCoordinateReferenceSystem(),
                        map.getCanvas().getObjectiveCRS(), true);

                geom = JTS.transform(geom, trs);
                return geom;
            }catch(final Exception ex){
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
            }
        }
        return null;
    }

    /**
     *
     * @param geom : in canvas objective CRS
     * @param layer : target layer filter
     * @return geometry filter
     */
    public Filter toFilter(final Geometry poly, final FeatureMapLayer fl) throws FactoryException, MismatchedDimensionException, TransformException{

        final String geoStr = fl.getCollection().getFeatureType().getGeometryDescriptor().getLocalName();
        final Expression geomField = FF.property(geoStr);

        final CoordinateReferenceSystem dataCrs = fl.getCollection().getFeatureType().getCoordinateReferenceSystem();

        final Geometry dataPoly = JTS.transform(poly, CRS.findMathTransform(map.getCanvas().getObjectiveCRS(), dataCrs,true));
        dataPoly.setSRID(SRIDGenerator.toSRID(dataCrs, SRIDGenerator.Version.V1));

        final Expression geomData = FF.literal(dataPoly);
        final Filter f = FF.intersects(geomField, geomData);

        return f;
    }

    
    //manipulating the feature source, transaction -----------------------------

    public void sourceAddGeometry(Geometry geom) {

        if (editedLayer != null) {

            final FeatureType featureType = (FeatureType) editedLayer.getCollection().getFeatureType();
            final CoordinateReferenceSystem dataCrs = featureType.getCoordinateReferenceSystem();
            final Feature feature = FeatureUtilities.defaultFeature(featureType, UUID.randomUUID().toString());

            try {
                geom = JTS.transform(geom, CRS.findMathTransform(map.getCanvas().getObjectiveCRS(), dataCrs, true));
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }

            feature.getDefaultGeometryProperty().setValue(geom);
            JFeatureOutLine.show(feature);

            if(editedLayer.getCollection().isWritable()){
                try {
                    ((FeatureCollection)editedLayer.getCollection()).add(feature);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
                }
            }

            map.getCanvas().getController().repaint();
        }

    }

    public void sourceModifyFeature(final Feature feature, final Geometry geo){

        final String ID = feature.getIdentifier().getID();

        ensureNonNull("geometry", geo);
        ensureNonNull("id", ID);

        if (editedLayer != null && editedLayer.getCollection().isWritable()) {

            final Filter filter = FF.id(Collections.singleton(FF.featureId(ID)));
            final FeatureType featureType = editedLayer.getCollection().getFeatureType();
            final AttributeDescriptor geomAttribut = featureType.getGeometryDescriptor();
            final CoordinateReferenceSystem dataCrs = featureType.getCoordinateReferenceSystem();

            try {
                final Geometry geom = JTS.transform(geo, CRS.findMathTransform(map.getCanvas().getObjectiveCRS(), dataCrs,true));
                
                editedLayer.getCollection().update(filter, geomAttribut,geom);
            } catch (final Exception ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
            } finally {
                map.getCanvas().getController().repaint();
            }

        }

    }

    public void sourceRemoveFeature(final Feature feature){
        sourceRemoveFeature(feature.getIdentifier().getID());
    }

    public void sourceRemoveFeature(final String ID) {
        ensureNonNull("id", ID);

        if (editedLayer != null && editedLayer.getCollection().isWritable()) {

            Filter filter = FF.id(Collections.singleton(FF.featureId(ID)));

            try {
                editedLayer.getCollection().remove(filter);
            } catch (final Exception ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
            }

            map.getCanvas().getController().repaint();
        }

    }


    //staic helper methods -----------------------------------------------------

    public static Geometry createGeometry(final List<Coordinate> coords) {
        int size = coords.size();

        switch (size) {
            case 0:
                return null;
            case 1:
                return createPoint(coords.get(0));
            case 2:
                return createLine(coords);
            default:
                return createLine(coords);
        }
    }

    public static Point createPoint(final Coordinate coord) {
        return GEOMETRY_FACTORY.createPoint(coord);
    }

    public static MultiPoint createMultiPoint(final List<? extends Geometry> geoms) {
        List<Point> lst = new ArrayList<Point>();
        for (Geometry go : geoms) {
            if (go instanceof Point) {
                lst.add((Point) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiPoint(lst.toArray(new Point[lst.size()]));
    }

    public static LineString createLine(final Coordinate ... coords) {
        return GEOMETRY_FACTORY.createLineString(coords);
    }
    public static LineString createLine(final List<Coordinate> coords) {
        return GEOMETRY_FACTORY.createLineString(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    public static LinearRing createLinearRing(List<Coordinate> coords) {
        coords = new ArrayList<Coordinate>(coords);
        if (!(coords.get(0).equals2D(coords.get(coords.size() - 1)))) {
            Coordinate coo = new Coordinate(coords.get(0));
            coords.add(coo);
        }
        if(coords.size() == 3){
            Coordinate coo = new Coordinate(coords.get(0));
            coords.add(coo);
        }

        return GEOMETRY_FACTORY.createLinearRing(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    public static Polygon createPolygon(final List<Coordinate> coords) {
        LinearRing ring = createLinearRing(coords);
        return GEOMETRY_FACTORY.createPolygon(ring, null);
    }

    public static MultiPolygon createMultiPolygon(final List<Geometry> geoms) {
        List<Polygon> lst = new ArrayList<Polygon>();
        for (Geometry go : geoms) {
            if (go instanceof Polygon) {
                lst.add((Polygon) go);
            }else{
                throw new IllegalArgumentException("Found an unexpected geometry type while building multipolygon : " + go.getClass());
            }
        }
        return GEOMETRY_FACTORY.createMultiPolygon(lst.toArray(new Polygon[lst.size()]));
    }

    public static MultiLineString createMultiLine(final List<Geometry> geoms) {
        List<LineString> lst = new ArrayList<LineString>();
        for (Geometry go : geoms) {
            if (go instanceof LineString) {
                lst.add((LineString) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiLineString(lst.toArray(new LineString[lst.size()]));
    }


}