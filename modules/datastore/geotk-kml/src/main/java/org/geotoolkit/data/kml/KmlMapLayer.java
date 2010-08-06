/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.data.kml;

import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import org.geotoolkit.data.kml.model.AbstractGeometry;
import org.geotoolkit.data.kml.model.AbstractObject;
import org.geotoolkit.data.kml.model.AbstractStyleSelector;
import org.geotoolkit.data.kml.model.BalloonStyle;
import org.geotoolkit.data.kml.model.BasicLink;
import org.geotoolkit.data.kml.model.Icon;
import org.geotoolkit.data.kml.model.IconStyle;
import org.geotoolkit.data.kml.model.Kml;
import org.geotoolkit.data.kml.model.KmlModelConstants;
import org.geotoolkit.data.kml.model.LabelStyle;
import org.geotoolkit.data.kml.model.LatLonAltBox;
import org.geotoolkit.data.kml.model.LatLonBox;
import org.geotoolkit.data.kml.model.LineString;
import org.geotoolkit.data.kml.model.LineStyle;
import org.geotoolkit.data.kml.model.LinearRing;
import org.geotoolkit.data.kml.model.MultiGeometry;
import org.geotoolkit.data.kml.model.Pair;
import org.geotoolkit.data.kml.model.Point;
import org.geotoolkit.data.kml.model.PolyStyle;
import org.geotoolkit.data.kml.model.Polygon;
import org.geotoolkit.data.kml.model.Region;
import org.geotoolkit.data.kml.model.Style;
import org.geotoolkit.data.kml.model.StyleMap;
import org.geotoolkit.data.kml.model.StyleState;
import org.geotoolkit.data.kml.model.Vec2;
import org.geotoolkit.data.kml.xsd.Cdata;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.jts.JTSGeometryJ2D;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.map.AbstractMapLayer;
import org.geotoolkit.map.DynamicMapLayer;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.style.MutableStyle;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés
 * @module pending
 */
public class KmlMapLayer extends AbstractMapLayer implements DynamicMapLayer {

    private static final GeometryFactory GF = new GeometryFactory();
    private final Kml kml;
    private RenderingContext2D context2d;
    private Map<String,Style> styles = new HashMap<String, Style>();
    private Map<String,StyleMap> styleMaps = new HashMap<String, StyleMap>();
    private final Font FONT = new Font("KmlMapLayerFont", Font.ROMAN_BASELINE, 10);

    public KmlMapLayer(MutableStyle style, Kml kml) {
        super(style);
        this.kml = kml;
    }

    @Override
    public Envelope getBounds() {
        return this.kml.getAbstractFeature().getBounds();
    }

    @Override
    public Object query(RenderingContext context) throws PortrayalException {
        return null;
    }

    @Override
    public Image getLegend() throws PortrayalException {
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphic = (Graphics2D) image.getGraphics();
//        this.legendAbstractFeature(this.kml.getAbstractFeature(), graphic);
        graphic.setColor(Color.red);
        graphic.draw3DRect(20, 20, 20, 20, true);
        return image;
    }
//
//    public void legendAbstractFeature(Feature abstractFeature, Graphics2D graphic) throws IOException{
//        if (FeatureTypeUtilities.isDecendedFrom(abstractFeature.getType(), KmlModelConstants.TYPE_CONTAINER)){
//            this.legendAbstractContainer(abstractFeature, graphic);
//        } else if (FeatureTypeUtilities.isDecendedFrom(abstractFeature.getType(), KmlModelConstants.TYPE_OVERLAY)){
//            this.legendAbstractOverlay(abstractFeature, graphic);
//        } else if (abstractFeature.getType().equals(KmlModelConstants.TYPE_PLACEMARK)){
//            this.legendPlacemark(abstractFeature, graphic);
//        }
//    }
//
//    private void legendAbstractContainer(Feature abstractContainer, Graphics2D graphic) throws IOException {
//        if (abstractContainer.getType().equals(KmlModelConstants.TYPE_FOLDER)){
//            this.legendFolder(abstractContainer, graphic);
//        } else if (abstractContainer.getType().equals(KmlModelConstants.TYPE_DOCUMENT)){
//            this.legendDocument(abstractContainer, graphic);
//        }
//    }
//
//    private void legendCommonAbstractContainer(Feature abstractContainer, Graphics2D graphic) {
//        this.legendCommonAbstractFeature(abstractContainer, graphic);
//    }
//
//    /**
//     *
//     * @param folder
//     * @throws IOException
//     */
//    private void legendFolder(Feature folder, Graphics2D graphic) throws IOException {
//        Iterator i;
//        this.portrayCommonAbstractContainer(folder);
//        if(folder.getProperties(KmlModelConstants.ATT_FOLDER_FEATURES.getName()) != null){
//            i = folder.getProperties(KmlModelConstants.ATT_FOLDER_FEATURES.getName()).iterator();
//            while(i.hasNext()){
//                this.legendAbstractFeature((Feature) ((Property) i.next()).getValue(), graphic);
//            }
//        }
//    }
//
//    /**
//     *
//     * @param document
//     * @throws IOException
//     */
//    private void legendDocument(Feature document, Graphics2D graphic) throws IOException {
//        Iterator i;
//        this.portrayCommonAbstractContainer(document);
//        if(document.getProperties(KmlModelConstants.ATT_DOCUMENT_FEATURES.getName()) != null){
//            i = document.getProperties(KmlModelConstants.ATT_DOCUMENT_FEATURES.getName()).iterator();
//            while(i.hasNext()){
//                this.legendAbstractFeature((Feature) ((Property) i.next()).getValue(), graphic);
//            }
//        }
//    }

    @Override
    public void portray(RenderingContext context) throws PortrayalException {
        if (!(context instanceof RenderingContext2D)) {
            return;
        }

        context2d = (RenderingContext2D) context;
        try {
            this.portrayKml(this.kml);
        } catch (IOException ex) {
            Logger.getLogger(KmlMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param kml
     * @throws IOException
     */
    private void portrayKml(Kml kml) throws IOException{
        if (kml.getAbstractFeature() != null){
            this.portrayAbstractFeature(kml.getAbstractFeature());
        }
    }

    /**
     * 
     * @param abstractFeature
     * @throws IOException
     */
    private void portrayAbstractFeature(Feature abstractFeature) throws IOException{
        if (FeatureTypeUtilities.isDecendedFrom(abstractFeature.getType(), KmlModelConstants.TYPE_CONTAINER)){
            this.portrayAbstractContainer(abstractFeature);
        } else if (FeatureTypeUtilities.isDecendedFrom(abstractFeature.getType(), KmlModelConstants.TYPE_OVERLAY)){
            this.portrayAbstractOverlay(abstractFeature);
        } else if (abstractFeature.getType().equals(KmlModelConstants.TYPE_PLACEMARK)){
            this.portrayPlacemark(abstractFeature);
        }
    }

    /**
     *
     * @param abstractContainer
     * @throws IOException
     */
    private void portrayAbstractContainer(Feature abstractContainer) throws IOException {
        if (abstractContainer.getType().equals(KmlModelConstants.TYPE_FOLDER)){
            this.portrayFolder(abstractContainer);
        } else if (abstractContainer.getType().equals(KmlModelConstants.TYPE_DOCUMENT)){
            this.portrayDocument(abstractContainer);
        }
    }

    /**
     *
     * @param placemark
     * @throws IOException
     */
    private void portrayPlacemark(Feature placemark) throws IOException{
        this.portrayCommonAbstractFeature(placemark);

        context2d.switchToObjectiveCRS();
        Graphics2D graphic = context2d.getGraphics();

        // Apply styles
        Style s = this.retrieveStyle(placemark);

        // display geometries
        if (placemark.getProperty(KmlModelConstants.ATT_PLACEMARK_GEOMETRY.getName()) != null){
            AbstractGeometry geometry = (AbstractGeometry) placemark.getProperty(KmlModelConstants.ATT_PLACEMARK_GEOMETRY.getName()).getValue();
            if(geometry != null){
                this.portrayAbstractGeometry(geometry,s);
            }
        }

        Region region = ((Region) placemark.getProperty(KmlModelConstants.ATT_REGION.getName()).getValue());
        if(region != null){
            LatLonAltBox latLonAltBox = region.getLatLonAltBox();
            portrayBalloonStyle((latLonAltBox.getEast()+latLonAltBox.getWest())/2,
                    (latLonAltBox.getNorth()+latLonAltBox.getSouth())/2,
                    s, false);
            portrayLabelStyle((latLonAltBox.getEast()+latLonAltBox.getWest())/2,
                    (latLonAltBox.getNorth()+latLonAltBox.getSouth())/2,
                    s,
                    (String) placemark.getProperty(KmlModelConstants.ATT_NAME.getName()).getValue());
        }
    }

    /**
     *
     * @param abstractFeature
     */
    private void portrayCommonAbstractFeature(Feature abstractFeature){
        Iterator i;
        if (abstractFeature.getProperty(KmlModelConstants.ATT_STYLE_SELECTOR.getName()) != null){
            i = abstractFeature.getProperties(KmlModelConstants.ATT_STYLE_SELECTOR.getName()).iterator();
            while(i.hasNext()){
                this.indexAbstractStyleSelector((AbstractStyleSelector) ((Property) i.next()).getValue());
            }
        }
    }

    /**
     *
     * @param abstractGeometry
     * @param style
     * @throws IOException
     */
    private void portrayAbstractGeometry(AbstractGeometry abstractGeometry, Style style) throws IOException{
        if (abstractGeometry instanceof MultiGeometry){
            this.portrayMultiGeometry((MultiGeometry) abstractGeometry, style);
        } else if (abstractGeometry instanceof LineString){
            this.portrayLineString((LineString) abstractGeometry, style);
        } else if (abstractGeometry instanceof Polygon){
            this.portrayPolygon((Polygon) abstractGeometry, style);
        } else if (abstractGeometry instanceof Point){
            this.portrayPoint((Point) abstractGeometry, style);
        } else if (abstractGeometry instanceof LinearRing){
            this.portrayLinearRing((LinearRing) abstractGeometry, style);
        }
    }

    /**
     *
     * @param abstractObject
     */
    public void portrayCommonAbstractObject(AbstractObject abstractObject){
    }

    /**
     *
     * @param abstractGeometry
     */
    public void portrayCommonAbstractGeometry(AbstractGeometry abstractGeometry){

        this.portrayCommonAbstractObject(abstractGeometry);
    }

    /**
     *
     * @param lineString
     * @param style
     */
    private void portrayLineString(LineString lineString, Style style){
        this.portrayCommonAbstractGeometry(lineString);

        // MathTransform
        MathTransform transform = null;
        context2d.switchToDisplayCRS();
        Graphics2D graphic = context2d.getGraphics();
        com.vividsolutions.jts.geom.Geometry ls = null;

        try {
            transform = context2d.getMathTransform(DefaultGeographicCRS.WGS84, context2d.getDisplayCRS());
            ls = JTS.transform((com.vividsolutions.jts.geom.LineString) lineString, transform);
        } catch (MismatchedDimensionException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        } catch (TransformException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        } catch (FactoryException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        }

        LineStyle lineStyle = style.getLineStyle();
        if(lineStyle != null){
            graphic.setColor(lineStyle.getColor());
            graphic.setStroke(new BasicStroke((float) (lineStyle.getWidth())));
        }

        Shape shape = new JTSGeometryJ2D(ls);
        graphic.draw(shape);
    }

    /**
     *
     * @param multiGeometry
     * @param style
     * @throws IOException
     */
    private void portrayMultiGeometry(MultiGeometry multiGeometry, Style style) throws IOException {
        this.portrayCommonAbstractGeometry(multiGeometry);
        for (AbstractGeometry abstractGeometry : multiGeometry.getGeometries()){
            this.portrayAbstractGeometry(abstractGeometry, style);
        }
    }

    /**
     *
     * @param polygon
     * @param style
     */
    private void portrayPolygon(Polygon polygon, Style style) {
        this.portrayCommonAbstractGeometry(polygon);

        // MathTransform
        MathTransform transform = null;
        context2d.switchToDisplayCRS();
        Graphics2D graphic = context2d.getGraphics();
        com.vividsolutions.jts.geom.Geometry pol = null;

        try {
            transform = context2d.getMathTransform(DefaultGeographicCRS.WGS84, context2d.getDisplayCRS());
            pol = JTS.transform((com.vividsolutions.jts.geom.Polygon) polygon, transform);
        } catch (MismatchedDimensionException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        } catch (TransformException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        } catch (FactoryException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        }

        Shape shape = new JTSGeometryJ2D(pol);

        // Apply styles
        PolyStyle polyStyle = style.getPolyStyle();
        if (polyStyle != null){
            graphic.setColor(polyStyle.getColor());
            graphic.setStroke(new BasicStroke((float) 0.05));
            if(style.getPolyStyle().getFill()){
                graphic.fill(shape);
            }
        }

        graphic.draw(shape);
    }

    /**
     *
     * @param point
     * @param style
     * @throws IOException
     */
    private void portrayPoint(Point point, Style style) throws IOException {
        this.portrayCommonAbstractGeometry(point);

        // MathTransform
        MathTransform transform;
        try {
            transform = context2d.getMathTransform(DefaultGeographicCRS.WGS84, context2d.getDisplayCRS());
        } catch (FactoryException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        }

        context2d.switchToDisplayCRS();
        Graphics2D graphic = context2d.getGraphics();

        // Apply styles
        IconStyle iconStyle = style.getIconStyle();
        if (iconStyle != null){
            graphic.setColor(iconStyle.getColor());
            BasicLink icon = iconStyle.getIcon();
            File img = new File(icon.getHref());
            BufferedImage image = ImageIO.read(img);
            com.vividsolutions.jts.geom.Point p = (com.vividsolutions.jts.geom.Point) point;
            double[] tab = new double[]{p.getX(), p.getY()};
            try {
                transform.transform(tab, 0, tab, 0, 1);
            } catch (TransformException ex) {
                context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
                return;
            }
            graphic.drawImage(image, (int) tab[0], (int) tab[1], null);
        }
    }

    /**
     *
     * @param linearRing
     * @param style
     */
    private void portrayLinearRing(LinearRing linearRing, Style style) {
        this.portrayCommonAbstractGeometry(linearRing);

        // MathTransform
        MathTransform transform = null;
        context2d.switchToDisplayCRS();
        Graphics2D graphic = context2d.getGraphics();
        com.vividsolutions.jts.geom.Geometry lr = null;

        try {
            transform = context2d.getMathTransform(DefaultGeographicCRS.WGS84, context2d.getDisplayCRS());    
            lr = JTS.transform((com.vividsolutions.jts.geom.LinearRing) linearRing, transform);
        } catch (MismatchedDimensionException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        } catch (TransformException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        } catch (FactoryException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        }

        LineStyle lineStyle = style.getLineStyle();
        if(lineStyle != null){
            graphic.setColor(lineStyle.getColor());
            graphic.setStroke(new BasicStroke((float) (lineStyle.getWidth())));
        }

        Shape shape = new JTSGeometryJ2D(lr);
        graphic.draw(shape);
    }

    /**
     *
     * @param abstractContainer
     */
    private void portrayCommonAbstractContainer(Feature abstractContainer) {
        this.portrayCommonAbstractFeature(abstractContainer);
    }

    /**
     *
     * @param folder
     * @throws IOException
     */
    private void portrayFolder(Feature folder) throws IOException {
        Iterator i;
        this.portrayCommonAbstractContainer(folder);
        if(folder.getProperties(KmlModelConstants.ATT_FOLDER_FEATURES.getName()) != null){
            i = folder.getProperties(KmlModelConstants.ATT_FOLDER_FEATURES.getName()).iterator();
            while(i.hasNext()){
                this.portrayAbstractFeature((Feature) ((Property) i.next()).getValue());
            }
        }
    }

    /**
     *
     * @param document
     * @throws IOException
     */
    private void portrayDocument(Feature document) throws IOException {
        Iterator i;
        this.portrayCommonAbstractContainer(document);
        if(document.getProperties(KmlModelConstants.ATT_DOCUMENT_FEATURES.getName()) != null){
            i = document.getProperties(KmlModelConstants.ATT_DOCUMENT_FEATURES.getName()).iterator();
            while(i.hasNext()){
                this.portrayAbstractFeature((Feature) ((Property) i.next()).getValue());
            }
        }
    }

    /**
     *
     * @param abstractStyleSelector
     */
    private void indexAbstractStyleSelector(AbstractStyleSelector abstractStyleSelector) {
        if (abstractStyleSelector instanceof Style){
            this.indexStyle((Style)abstractStyleSelector);
        } else if (abstractStyleSelector instanceof StyleMap){
            this.indexStyleMap((StyleMap)abstractStyleSelector);
        }
    }

    /**
     *
     * @param style
     */
    private void indexStyle(Style style) {
        if (style.getIdAttributes().getId() != null){
            this.styles.put("#"+style.getIdAttributes().getId(), style);
        }
    }

    /**
     *
     * @param styleMap
     */
    private void indexStyleMap(StyleMap styleMap) {
        if (styleMap.getIdAttributes().getId() != null){
            this.styleMaps.put("#"+styleMap.getIdAttributes().getId(), styleMap);
        }
//        for(Pair pair : styleMap.getPairs()){
//            pair.
//        }
    }

    /**
     *
     * @param abstractOverlay
     * @throws IOException
     */
    private void portrayAbstractOverlay(Feature abstractOverlay) throws IOException {
        if (abstractOverlay.getType().equals(KmlModelConstants.TYPE_GROUND_OVERLAY)){
            this.portrayGroundOverlay(abstractOverlay);
        } else if (abstractOverlay.getType().equals(KmlModelConstants.TYPE_SCREEN_OVERLAY)){
            this.portrayScreenOverlay(abstractOverlay);
//        } else if (abstractOverlay.getType().equals(KmlModelConstants.TYPE_PHOTO_OVERLAY)){
//            this.portrayPhotoOverlay(abstractOverlay);
        }
    }

    /**
     *
     * @param groundOverlay
     * @throws IOException
     */
    private void portrayGroundOverlay(Feature groundOverlay) throws IOException {
        this.portrayCommonAbstractOverlay(groundOverlay);

        // MathTransform
        MathTransform transform;
        try {
            transform = context2d.getMathTransform(DefaultGeographicCRS.WGS84, context2d.getDisplayCRS());
        } catch (FactoryException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        }

        context2d.switchToDisplayCRS();
        Graphics2D graphic = context2d.getGraphics();

        // Display image
        File img = new File(
                ((Icon) groundOverlay.getProperty(
                KmlModelConstants.ATT_OVERLAY_ICON.getName()).getValue())
                .getHref());
        BufferedImage image = ImageIO.read(img);
        LatLonBox latLonBox = (LatLonBox) groundOverlay.getProperty(
                KmlModelConstants.ATT_GROUND_OVERLAY_LAT_LON_BOX.getName()).getValue();
        double n = latLonBox.getNorth();
        double e = latLonBox.getEast();
        double s = latLonBox.getSouth();
        double w = latLonBox.getWest();

        double[] tab = new double[]{
            w, n,
            e, n,
            e, s,
            w, s};

        try {
            transform.transform(tab, 0, tab, 0, 4);
        } catch (TransformException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        }

        graphic.drawImage(image,
                (int) tab[0],
                (int) tab[1],
                (int) (tab[2]-tab[0]),
                (int) (tab[5]- tab[3]),
                null);

        // Apply styles
        Style sty = this.retrieveStyle(groundOverlay);
        portrayBalloonStyle((e+w)/2, (n+s)/2, sty, false);
    }

    /**
     *
     * @param abstractOverlay
     */
    private void portrayCommonAbstractOverlay(Feature abstractOverlay) {
        this.portrayCommonAbstractFeature(abstractOverlay);
    }

    /**
     *
     * @param screenOverlay
     * @throws IOException
     */
    private void portrayScreenOverlay(Feature screenOverlay) throws IOException {
        this.portrayCommonAbstractOverlay(screenOverlay);
        File img = new File(
                ((Icon) screenOverlay.getProperty(KmlModelConstants.ATT_OVERLAY_ICON.getName()).getValue())
                .getHref());
        context2d.switchToDisplayCRS();

        BufferedImage image = ImageIO.read(img);
        Graphics2D graphic = context2d.getGraphics();
        Vec2 overlayXY = (Vec2) screenOverlay.getProperty(KmlModelConstants.ATT_SCREEN_OVERLAY_OVERLAYXY.getName()).getValue();
        Vec2 screenXY = (Vec2) screenOverlay.getProperty(KmlModelConstants.ATT_SCREEN_OVERLAY_SCREENXY.getName()).getValue();
        Vec2 size = (Vec2) screenOverlay.getProperty(KmlModelConstants.ATT_SCREEN_OVERLAY_SIZE.getName()).getValue();
       
        int width = (int) size.getX();
        int height = (int) size.getY();
        int x = (int) screenXY.getX();
        int y = context2d.getCanvasDisplayBounds().height - (int) screenXY.getY() - height;
        BufferedImage imageResult = image.getSubimage((int) overlayXY.getX(), (int) overlayXY.getY(),
                width, height);
        graphic.drawImage(imageResult, x, y, width, height, null);

        // Apply styles
        Style s = this.retrieveStyle(screenOverlay);
        portrayBalloonStyle(x+width, y, s, true);
    }

    /**
     *
     * @param x
     * @param y
     * @param style
     * @param font
     */
    private void portrayBalloonStyle(double x, double y, Style style, boolean fixedToScreen){
        
        // MathTransform
        MathTransform transform = null;

        //Fixed to screen for ScrenOverlays
        if(!fixedToScreen){
            try {
                transform = context2d.getMathTransform(DefaultGeographicCRS.WGS84, context2d.getDisplayCRS());
            } catch (FactoryException ex) {
                context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
                return;
            }
        }

        context2d.switchToDisplayCRS();
        Graphics2D graphic = context2d.getGraphics();

        graphic.setFont(FONT);
        FontMetrics fm = graphic.getFontMetrics();

        int linewidth = 40;
        BalloonStyle balloonStyle = style.getBalloonStyle();

        if(balloonStyle != null){
            int length = balloonStyle.getText().toString().length();
            int begin = 0, interligne = 0, balloonWidth = 0, balloonLines = 0;
            boolean cdata = balloonStyle.getText() instanceof Cdata;
            if(balloonStyle.getBgColor().equals(new Color(255,255,0)))
                System.out.println("MARSEILLE : "+cdata);
            JLabel jep = null;

            // balloon dimensions
            do{
                int end = Math.min(begin+linewidth, length);
                balloonWidth = Math.max(balloonWidth,
                        fm.stringWidth(balloonStyle.getText().toString().substring(begin, end)));
                begin+= linewidth;
                balloonLines++;
            }while (begin+linewidth < length);
            if(begin < length){
                balloonLines++;
            }
     
            double[] tab = new double[]{x, y};
            //Fixed to screen for ScrenOverlays
            if(!fixedToScreen){
                try {
                    transform.transform(tab, 0, tab, 0, 1);
                } catch (TransformException ex) {
                    context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
                    return;
                }
            }

            int left;
            int right;
            int top;
            int bottom;
            int VExc;
            int HExc;

            if(cdata){
                String s = "";
                if("<html>".equals(balloonStyle.getText().toString().substring(0, 5))){
                    jep = new JLabel(balloonStyle.getText().toString());
                }else{
                    jep = new JLabel("<html>"+balloonStyle.getText().toString()+"</html>");
                }
                jep.setOpaque(false);
                Dimension preferredDim = jep.getPreferredSize();
                jep.setSize(preferredDim);
                left = (int)tab[0]-(preferredDim.width+2)/2;
                right = left+preferredDim.width+1;
                top = (int) tab[1]-2*(preferredDim.height+2);
                bottom = top+preferredDim.height+1;
                right = left+preferredDim.width+1;
                VExc = (preferredDim.width+2)/10;
                HExc = (preferredDim.height+2)/7;
            }else{
                left = (int)tab[0]-(balloonWidth+2)/2;
                right = (int)tab[0]+(balloonWidth+2)/2;
                top = (int) tab[1]-((2*balloonLines+1)*fm.getHeight()+2);
                bottom = (int)tab[1]-(balloonLines+1)*fm.getHeight()+1;
                VExc = (balloonWidth+2)/10;
                HExc = (balloonLines*fm.getHeight()+2)/7;
            }

            // Print balloon structure
            graphic.setColor(balloonStyle.getBgColor());
            Path2D p = new java.awt.geom.Path2D.Double();
            p.moveTo(left, top);

            //top and right sides
            p.curveTo(left+(right-left)/4, top-VExc, left+3*(right-left)/4, top-VExc, right, top);
            p.curveTo(right+HExc, bottom+5*(top-bottom)/6, right+HExc, bottom+(top-bottom)/6, right, bottom);

            //bottom
            p.curveTo(left+8*(right-left)/9, bottom+VExc, left+7*(right-left)/9, bottom, left+2*(right-left)/3, bottom+VExc);
            p.lineTo((int)tab[0], (int)tab[1]);
            //p.curveTo(left+7*(right-left)/12, bottom+(bottom-top)/3, left+7*(right-left)/12, bottom+2*(bottom-top)/3, (int)tab[0], (int)tab[1]);
            p.lineTo(left+(right-left)/3, bottom+VExc);
            //p.curveTo(left+5*(right-left)/12, bottom+(bottom-top)/2, left+5*(right-left)/12, bottom+(bottom-top)/2, left+(right-left)/3, bottom+VExc);
            p.curveTo(left+2*(right-left)/9, bottom, left+(right-left)/9, bottom+VExc, left, bottom);

            // left side
            p.curveTo(left-HExc, bottom+(top-bottom)/6, left-HExc, bottom+5*(top-bottom)/6, left, top);
            p.closePath();
            graphic.fill(p);

            // grey border
            graphic.setColor(new Color(40,40,40));
            graphic.draw(p);
            
            graphic.setColor(balloonStyle.getTextColor());

            // print balloon text
            if(cdata){
                graphic.translate(left, top);
                jep.paint(graphic);
                graphic.translate(-left, -top);
            }else{
                begin = 0; interligne = fm.getHeight();
                while (begin+linewidth < length){
                    graphic.drawString(
                            balloonStyle.getText().toString().substring(
                            begin, begin+linewidth), left+1, top+interligne);
                    begin+= linewidth;
                    interligne+= fm.getHeight();
                }
                if(begin < length){
                    graphic.drawString(
                            balloonStyle.getText().toString().substring(
                            begin, length), left+1, top+interligne);
                }
            }
        }
    }

    /**
     *
     * @param x
     * @param y
     * @param style
     * @param content
     */
    private void portrayLabelStyle(double x, double y, Style style, String content){
        // MathTransform
        MathTransform transform;
        try {
            transform = context2d.getMathTransform(DefaultGeographicCRS.WGS84, context2d.getDisplayCRS());
        } catch (FactoryException ex) {
            context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
            return;
        }
        Graphics2D graphic = context2d.getGraphics();
        LabelStyle labelStyle = style.getLabelStyle();
        if(labelStyle != null){
            graphic.setFont(new Font(FONT.getName(),
                    FONT.getStyle(), FONT.getSize() * (int) labelStyle.getScale()));
            graphic.setColor(labelStyle.getColor());
            double[] tab = new double[]{x, y};
            try {
                transform.transform(tab, 0, tab, 0, 1);
            } catch (TransformException ex) {
                context2d.getMonitor().exceptionOccured(ex, Level.WARNING);
                return;
            }
            graphic.drawString(content, (int) tab[0], (int) tab[1]);
        }
    }

    /**
     *
     * @param feature
     * @return
     */
    private Style retrieveStyle(Feature feature){
        Style styleSelector = null;

        if(feature.getProperty(KmlModelConstants.ATT_STYLE_SELECTOR.getName())!= null){
            if(feature.getProperty(KmlModelConstants.ATT_STYLE_SELECTOR.getName()).getValue() instanceof Style){
                styleSelector = (Style) feature.getProperty(KmlModelConstants.ATT_STYLE_SELECTOR.getName()).getValue();
            }
            else if(styleSelector instanceof StyleMap){
                StyleMap styleMap = (StyleMap) styleSelector;
                styleSelector = retrieveStyle(styleMap, StyleState.NORMAL);
            }
        } else {
            styleSelector = this.styles.get(((URI) feature.getProperty(KmlModelConstants.ATT_STYLE_URL.getName()).getValue()).toString());
            if(styleSelector == null){
                StyleMap styleMap = this.styleMaps.get(((URI) feature.getProperty(KmlModelConstants.ATT_STYLE_URL.getName()).getValue()).toString());
                styleSelector = retrieveStyle(styleMap, StyleState.NORMAL);
            }
        }
        return styleSelector;
    }

    private Style retrieveStyle(StyleMap styleMap, StyleState styleState){
        Style s = null;
        for(Pair pair : styleMap.getPairs()){
            if(styleState.equals(pair.getKey())){
                AbstractStyleSelector styleSelector = pair.getAbstractStyleSelector();
                if(styleSelector instanceof StyleMap){
                    s = retrieveStyle((StyleMap) styleSelector, styleState);
                } else if(styleSelector != null){
                    s = (Style) styleSelector;
                    break;
                }

                if(s == null){
                    s = styles.get(pair.getStyleUrl().toString());
                    if(s == null
                            && this.styleMaps.get(pair.getStyleUrl().toString()) != null){
                        s = retrieveStyle(this.styleMaps.get(pair.getStyleUrl().toString()), styleState);
                    }
                }
            }
        }
        return s;
    }

}
