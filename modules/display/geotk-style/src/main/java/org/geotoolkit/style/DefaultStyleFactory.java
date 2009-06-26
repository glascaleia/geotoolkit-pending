/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.style;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import javax.swing.Icon;

import org.geotoolkit.factory.Factory;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.style.function.Categorize;
import org.geotoolkit.style.function.DefaultCategorize;
import org.geotoolkit.style.function.DefaultInterpolate;
import org.geotoolkit.style.function.DefaultInterpolationPoint;
import org.geotoolkit.style.function.Interpolate;
import org.geotoolkit.style.function.InterpolationPoint;
import org.geotoolkit.style.function.Method;
import org.geotoolkit.style.function.Mode;
import org.geotoolkit.style.function.ThreshholdsBelongTo;
import org.geotoolkit.util.SimpleInternationalString;

import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.AnchorPoint;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ColorReplacement;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.ContrastMethod;
import org.opengis.style.Description;
import org.opengis.style.Displacement;
import org.opengis.style.ExtensionSymbolizer;
import org.opengis.style.ExternalGraphic;
import org.opengis.style.ExternalMark;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Fill;
import org.opengis.style.Font;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicFill;
import org.opengis.style.GraphicLegend;
import org.opengis.style.GraphicStroke;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.Halo;
import org.opengis.style.LabelPlacement;
import org.opengis.style.LinePlacement;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.PointPlacement;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Rule;
import org.opengis.style.SelectedChannelType;
import org.opengis.style.SemanticType;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Stroke;
import org.opengis.style.Symbolizer;
import org.opengis.style.TextSymbolizer;
import org.opengis.util.InternationalString;

import static org.geotoolkit.style.StyleConstants.*;

/**
 * Factory for creating Styles.
 * Symbolizer and underneath objects are immutable
 * while Style, FeatureTypeStyle and Rule are mutable.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultStyleFactory extends Factory implements MutableStyleFactory {

    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);

    // TEMPORARY FIX ///////////////////////////////////////////////////////////

    @Override
    public LineSymbolizer lineSymbolizer(String name, Expression geometry, Description description, Unit<?> unit, Stroke stroke, Expression offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PointSymbolizer pointSymbolizer(String name, Expression geometry, Description description, Unit<?> unit, Graphic graphic) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PolygonSymbolizer polygonSymbolizer(String name, Expression geometry, Description description, Unit<?> unit, Stroke stroke, Fill fill, Displacement displacement, Expression offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RasterSymbolizer rasterSymbolizer(String name, Expression geometry, Description description, Unit<?> unit, Expression opacity, ChannelSelection channelSelection, OverlapBehavior overlapsBehaviour, ColorMap colorMap, ContrastEnhancement contrast, ShadedRelief shaded, Symbolizer outline) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TextSymbolizer textSymbolizer(String name, Expression geometry, Description description, Unit<?> unit, Expression label, Font font, LabelPlacement placement, Halo halo, Fill fill) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    

    @Override
    public Literal literal(final Color color) {
        if (color == null) {
            return null;
        }

        String redCode = Integer.toHexString(color.getRed());
        String greenCode = Integer.toHexString(color.getGreen());
        String blueCode = Integer.toHexString(color.getBlue());

        if (redCode.length() == 1) {
            redCode = "0" + redCode;
        }

        if (greenCode.length() == 1) {
            greenCode = "0" + greenCode;
        }

        if (blueCode.length() == 1) {
            blueCode = "0" + blueCode;
        }

        String colorCode = "#" + redCode + greenCode + blueCode;

        return FF.literal(colorCode.toUpperCase());
    }


    //--------------------------------------------------------------------------
    // Style creation methods --------------------------------------------------
    //--------------------------------------------------------------------------
    
    @Override
    public MutableStyle style(){
        return new DefaultMutableStyle();
    }

    @Override
    public MutableStyle style(final Symbolizer symbol){
        final MutableFeatureTypeStyle fts = featureTypeStyle(symbol);
        final MutableStyle catalog = new DefaultMutableStyle();
        catalog.featureTypeStyles().add(fts);
        return catalog;
    }

    @Override
    public MutableStyle style(final Symbolizer[] symbols){
        final MutableFeatureTypeStyle fts = featureTypeStyle(symbols);
        final MutableStyle catalog = new DefaultMutableStyle();
        catalog.featureTypeStyles().add(fts);
        return catalog;
    }

    @Override
    public MutableStyle style(String name, Description description, boolean isDefault,
            List<FeatureTypeStyle> featureTypeStyles, Symbolizer defaultSymbolizer) {
        final MutableStyle style = new DefaultMutableStyle();
        style.setName(name);
        style.setDescription(description);
        style.setDefault(isDefault);

        for(FeatureTypeStyle fts : featureTypeStyles){
            if(fts instanceof MutableFeatureTypeStyle){
                style.featureTypeStyles().add((MutableFeatureTypeStyle)fts);
            }else{
                throw new IllegalArgumentException("This factory implementation requiere a list of MutableFeatureTypeStyle");
            }
        }

        style.setDefaultSpecification(defaultSymbolizer);
        return style;
    }

    @Override
    public MutableFeatureTypeStyle featureTypeStyle(){
        return new DefaultMutableFeatureTypeStyle();
    }

    @Override
    public MutableFeatureTypeStyle featureTypeStyle(final Symbolizer symbol){
        final MutableRule rule = rule(symbol);
        final MutableFeatureTypeStyle fts = new DefaultMutableFeatureTypeStyle();
        fts.rules().add(rule);
        return fts;
    }

    @Override
    public MutableFeatureTypeStyle featureTypeStyle(final Symbolizer[] symbol){
        final MutableRule rule = rule(symbol);
        final DefaultMutableFeatureTypeStyle fts = new DefaultMutableFeatureTypeStyle();
        fts.rules().add(rule);
        return fts;
    }

    @Override
    public MutableFeatureTypeStyle featureTypeStyle(String name, Description description,
            Id definedFor, Set<Name> featureTypeNames, Set<SemanticType> types, List<Rule> rules) {
        final MutableFeatureTypeStyle fts = new DefaultMutableFeatureTypeStyle();
        fts.setName(name);
        fts.setDescription(description);
        fts.setFeatureInstanceIDs(definedFor);
        if(featureTypeNames != null){
            fts.featureTypeNames().addAll(featureTypeNames);
        }
        if(types != null){
            fts.semanticTypeIdentifiers().addAll(types);
        }

        for(Rule rule : rules){
            if(fts instanceof MutableFeatureTypeStyle){
                fts.rules().add((MutableRule)rule);
            }else{
                throw new IllegalArgumentException("This factory implementation requiere a list of MutableRule");
            }
        }
        
        return fts;
    }

    @Override
    public MutableRule rule(){
        return new DefaultMutableRule();
    }

    @Override
    public MutableRule rule(final Symbolizer symbol){
        final MutableRule rule = new DefaultMutableRule();
        rule.symbolizers().add(symbol);  
        return rule;
    }

    @Override
    public MutableRule rule(final Symbolizer[] symbols){
        final MutableRule rule = new DefaultMutableRule();
        for(final Symbolizer s : symbols){
            rule.symbolizers().add(s);
        }
        return rule;
    }

    @Override
    public MutableRule rule(String name, Description description, GraphicLegend legend,
            double min, double max, List<Symbolizer> symbolizers, Filter filter) {
        final MutableRule rule = new DefaultMutableRule();
        rule.setName(name);
        rule.setDescription(description);
        rule.setLegendGraphic(legend);
        rule.setMinScaleDenominator(min);
        rule.setMaxScaleDenominator(max);
        rule.symbolizers().addAll(symbolizers);
        rule.setFilter(filter);
        return rule;
    }
    
    //--------------------------------------------------------------------------
    // Symbolizer creation methods ---------------------------------------------
    //--------------------------------------------------------------------------

    @Override
    public PointSymbolizer pointSymbolizer(){
        return DEFAULT_POINT_SYMBOLIZER;
    }

    @Override
    public PointSymbolizer pointSymbolizer(final Graphic graphic, final String geometryPropertyName){
        return new DefaultPointSymbolizer(
                graphic, 
                DEFAULT_UOM, 
                geometryPropertyName, 
                null, 
                DEFAULT_DESCRIPTION);
    }

    @Override
    public PointSymbolizer pointSymbolizer(String name, String geom, Description desc,
            Unit<?> unit, Graphic graphic) {
        return new DefaultPointSymbolizer(graphic, unit, geom, name, desc);
    }

    @Override
    public LineSymbolizer lineSymbolizer(){
        return DEFAULT_LINE_SYMBOLIZER;
    }

    @Override
    public LineSymbolizer lineSymbolizer(final Stroke stroke, final String geometryPropertyName){
        return new DefaultLineSymbolizer(
                stroke, 
                FF.literal(0),
                DEFAULT_UOM, 
                geometryPropertyName, 
                null, 
                DEFAULT_DESCRIPTION);
    }
    
    @Override
    public LineSymbolizer lineSymbolizer(String name, String geom, Description desc,
            Unit<?> uom, Stroke stroke, Expression offset) {
        return new DefaultLineSymbolizer(stroke, offset, uom, geom, name, desc);
    }

    @Override
    public PolygonSymbolizer polygonSymbolizer(){
        return DEFAULT_POLYGON_SYMBOLIZER;
    }

    @Override
    public PolygonSymbolizer polygonSymbolizer(final Stroke stroke, final Fill fill,
        final String geometryPropertyName){
        return new DefaultPolygonSymbolizer(
                stroke, 
                fill, 
                DEFAULT_DISPLACEMENT, 
                FF.literal(0),
                DEFAULT_UOM, 
                geometryPropertyName, 
                null, 
                DEFAULT_DESCRIPTION);
    }

    @Override
    public PolygonSymbolizer polygonSymbolizer(String name, String geom, Description desc,
            Unit<?> uom, Stroke stroke, Fill fill, Displacement disp, Expression offset) {
        return new DefaultPolygonSymbolizer(stroke, fill, disp, offset, uom, geom, name, desc);
    }

    @Override
    public TextSymbolizer textSymbolizer(){
        return DEFAULT_TEXT_SYMBOLIZER;
    }

    @Override
    public TextSymbolizer textSymbolizer(final Fill fill, final Font font, final Halo halo,
        final Expression label, final LabelPlacement labelPlacement, final String geometryPropertyName){
        return new DefaultTextSymbolizer(
                label, 
                font, 
                labelPlacement, 
                halo, 
                fill, 
                DEFAULT_UOM, 
                geometryPropertyName, 
                null, 
                DEFAULT_DESCRIPTION);
    }

    @Override
    public TextSymbolizer textSymbolizer(String name, String geom, Description desc,
            Unit<?> uom, Expression label, Font font, LabelPlacement placement, Halo halo, Fill fill) {
        return new DefaultTextSymbolizer(label, font, placement, halo, fill, uom, geom, name, desc);
    }


    @Override
    public RasterSymbolizer rasterSymbolizer(){
        return DEFAULT_RASTER_SYMBOLIZER;
    }

    @Override
    public RasterSymbolizer rasterSymbolizer(final String geometryPropertyName, final Expression opacity,
        final ChannelSelection channel, final OverlapBehavior overlap, final ColorMap colorMap, final ContrastEnhancement ce,
        final ShadedRelief relief, final Symbolizer outline){
        return new DefaultRasterSymbolizer(
                opacity,
                channel,
                overlap,
                colorMap,
                ce,
                relief,
                outline,
                DEFAULT_UOM,
                geometryPropertyName,
                null,
                DEFAULT_DESCRIPTION);
    }

    @Override
    public RasterSymbolizer rasterSymbolizer(String name, String geom, Description desc,
            Unit<?> uom, Expression opacity, ChannelSelection selection,
            OverlapBehavior overlap, ColorMap colorMap, ContrastEnhancement enchance,
            ShadedRelief relief, Symbolizer outline) {
        return new DefaultRasterSymbolizer(opacity, selection, overlap, colorMap,
                enchance, relief, outline, uom, geom, name, desc);
    }

    @Override
    public ExtensionSymbolizer extensionSymbolizer(String name, String geometry, Description description, Unit<?> unit, String extensionName, Map<String, Expression> parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    //--------------------------------------------------------------------------
    // Underneath Immutable objects creation methods ---------------------------
    //--------------------------------------------------------------------------

    @Override
    public AnchorPoint  anchorPoint(){
        return DEFAULT_ANCHOR_POINT;
    }    

    @Override
    public AnchorPoint  anchorPoint(final double x, final double y){
        return new DefaultAnchorPoint(FF.literal(x), FF.literal(y));
    }

    @Override
    public AnchorPoint  anchorPoint(final Expression x, final Expression y){
        return new DefaultAnchorPoint(x,y);
    }

    @Override
    public Description  description(){
        return DEFAULT_DESCRIPTION;
    }

    @Override
    public Description  description(final String title, final String abs) {
        return description(new SimpleInternationalString(title),
                new SimpleInternationalString(abs));
    }

    @Override
    public Description  description(final InternationalString title, final InternationalString abs){
        return new DefaultDescription(title, abs); 
    }

    @Override
    public Displacement displacement(){
        return DEFAULT_DISPLACEMENT;
    }

    @Override
    public Displacement displacement(final double x, final double y){
        return new DefaultDisplacement(FF.literal(x), FF.literal(y));
    }

    @Override
    public Displacement displacement(final Expression x, final Expression y){
        return new DefaultDisplacement(x, y); 
    }

    @Override
    public Graphic graphic(){
        return DEFAULT_GRAPHIC;
    }

    @Override
    public Mark mark(){
        return DEFAULT_GRAPHICAL_SYMBOL;
    }

    @Override
    public ColorMap colorMap(){
        return DEFAULT_RASTER_COLORMAP;
    }

    @Override
    public Stroke stroke(){
        return DEFAULT_STROKE;
    }

    @Override
    public Fill fill(){
        return DEFAULT_FILL;
    }

    @Override
    public Font font(){
        return DEFAULT_FONT;
    }
    
    @Override
    public PointPlacement pointPlacement(){
        return DEFAULT_POINTPLACEMENT;
    }
    
    @Override
    public ContrastEnhancement contrastEnhancement(){
        return DEFAULT_CONTRAST_ENHANCEMENT;
    }
        
    
    @Override
    public Fill fill(final Color color){
        Fill fill = new DefaultFill(
                null, 
                FF.literal(color),
                null);
        return fill;
    }

    @Override
    public Stroke stroke(final Color color, final double width){
        Stroke stroke = new DefaultStroke(
                literal(color),
                null, 
                FF.literal(width),
                null,
                null,
                null, 
                null);
        return stroke;
    }

    @Override
    public Stroke stroke(final Color color, final double width, final float[] dashes){
        Stroke stroke = new DefaultStroke(
                literal(color),
                null, 
                FF.literal(width),
                null,
                null,
                dashes,
                null);
        return stroke;
    }

    @Override
    public Halo halo(final Color color, final double width){
        Halo halo = new DefaultHalo(
                fill(color),
                FF.literal(width));
        return halo;
    }

    @Override
    public LabelPlacement labelPlacement(){
        return DEFAULT_POINTPLACEMENT;
    }

    @Override
    public Font font(final int size){
        Font font = new DefaultFont(
                new ArrayList<Expression>(), 
                null, 
                null, 
                FF.literal(size));
        return font;
    }

    @Override
    public Mark getCircleMark(){
        Mark m = new DefaultMark(
                MARK_CIRCLE, 
                null, 
                null);
        return m;
    }

    @Override
    public Mark getXMark(){
        Mark m = new DefaultMark(
                MARK_X, 
                null, 
                null);
        return m;
    }

    @Override
    public Mark getStarMark(){
        Mark m = new DefaultMark(
                MARK_STAR, 
                null,
                null);
        return m;
    }

    @Override
    public Mark getSquareMark(){
        Mark m = new DefaultMark(
                MARK_SQUARE, 
                null, 
                null);
        return m;
    }

    @Override
    public Mark getCrossMark(){
        Mark m = new DefaultMark(
                MARK_CROSS, 
                null, 
                null);
        return m;
    }

    @Override
    public Mark getTriangleMark(){
        Mark m = new DefaultMark(
                MARK_TRIANGLE, 
                null, 
                null);
        return m;
    }

    @Override
    public Mark mark(final Expression wellKnownName, final Stroke stroke, final Fill fill){
        Mark m = new DefaultMark(wellKnownName, fill, stroke);
        return m;
    }

    @Override
    public LinePlacement linePlacement(final Expression offset){
        LinePlacement lp = new DefaultLinePlacement(
                offset, 
                FF.literal(0),
                FF.literal(0),
                false, 
                false, 
                false);
        return lp;
    }

    @Override
    public Fill fill(final Expression color, final Expression opacity){
        Fill fill = new DefaultFill(null, color, opacity);
        return fill;
    }

    @Override
    public Fill fill(final Expression color){
        Fill fill = new DefaultFill(null, color, null);
        return fill;
    }

    @Override
    public Stroke stroke(final Expression color, final Expression width){
        Stroke stroke = new  DefaultStroke(
                color, 
                null, 
                width, 
                null, 
                null, 
                null, 
                null);
        return stroke;
    }

    @Override
    public Stroke stroke(final Expression color, final Expression width, final Expression opacity){
        Stroke stroke = new  DefaultStroke(
                color, 
                opacity, 
                width, 
                null, 
                null, 
                null, 
                null);
        return stroke;
    }

    @Override
    public Font font(final Expression fontFamily, final Expression fontStyle, final Expression fontWeight,
        Expression fontSize){
        Font f = new DefaultFont(Collections.singletonList(fontFamily), fontStyle, fontWeight, fontSize);
        return f;
    }

    @Override
    public ContrastEnhancement contrastEnhancement(final Expression gammaValue){
        ContrastEnhancement ce = new DefaultContrastEnhancement(null, gammaValue);
        return ce;
    }

    @Override
    public SelectedChannelType selectedChannelType(final String name, final Expression gammaValue){
        SelectedChannelType sct = new DefaultSelectedChannelType(name, contrastEnhancement(gammaValue));
        return sct;
    }

    @Override
    public ShadedRelief shadedRelief(final Expression reliefFactor){
        ShadedRelief relief = new DefaultShadedRelief(false, reliefFactor);
        return relief;
    }

    @Override
    public ShadedRelief shadedRelief(Expression relief, boolean bright) {
        return new DefaultShadedRelief(bright, relief);
    }

    @Override
    public ColorMap colorMap(Expression propertyName, Expression... mapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ColorMap colorMap(final Function function){
        ColorMap color = new DefaultColorMap(function);
        return color; 
    }

    @Override
    public ColorReplacement colorReplacement(Expression propertyName, Expression... mapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ColorReplacement colorReplacement(final Function recode){
        ColorReplacement cr = new DefaultColorReplacement(recode);
        return cr;
    }

    @Override
    public ContrastEnhancement contrastEnhancement(Expression gamma, ContrastMethod method) {
        ContrastEnhancement ce = new DefaultContrastEnhancement(method, gamma);
        return ce;
    }

    @Override
    public ExternalGraphic externalGraphic(Icon icon, Collection<ColorReplacement> replaces) {
        return new DefaultExternalGraphic(icon, replaces);
    }

    @Override
    public ExternalGraphic externalGraphic(final URL url, final String format){
        URI uri = null;
        try { uri = url.toURI();
        } catch (URISyntaxException ex) {
            Logger.getLogger(DefaultStyleFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        ExternalGraphic ext = new DefaultExternalGraphic(
                onlineResource(uri),
                format,
                null);

        return ext;
    }

    @Override
    public ExternalGraphic externalGraphic(final String uri, final String format){
        URI uri2 = null;
        try { uri2 = new URI(uri);
        } catch (URISyntaxException ex) {
            Logger.getLogger(DefaultStyleFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        ExternalGraphic ext = new DefaultExternalGraphic(
                onlineResource(uri2),
                format,
                null);
        return ext;
    }

    @Override
    public ExternalGraphic externalGraphic(final OnLineResource resource, final String format, final Collection<ColorReplacement> replaces){
        ExternalGraphic eg = new DefaultExternalGraphic(resource, format, replaces);
        return eg;
    }

    @Override
    public ExternalMark externalMark(Icon icon) {
        return new DefaultExternalMark(icon);
    }

    @Override
    public ExternalMark externalMark(final OnLineResource online, final String format, final int index){
        ExternalMark em = new DefaultExternalMark(online, format, index);
        return em;
    }

    @Override
    public Fill fill(final GraphicFill fill, final Expression color, final Expression opacity){
        return new DefaultFill(fill,color,opacity); 
    }

    @Override
    public Font font(final List<Expression> family, final Expression style, final Expression weight, final Expression size){
        return new DefaultFont(family, style, weight, size);
    }

    @Override
    public Graphic graphic(final List<GraphicalSymbol> symbols,
            final Expression opacity, 
            final Expression size, 
            final Expression rotation, 
            final AnchorPoint anchor, 
            final Displacement disp){
        return new DefaultGraphic(symbols, opacity, size, rotation, anchor, disp);
    }

    @Override
    public GraphicFill graphicFill(final List<GraphicalSymbol> symbols,
            final Expression opacity, 
            final Expression size, 
            final Expression rotation, 
            final AnchorPoint anchor, 
            final Displacement disp){
        GraphicFill fill = new DefaultGraphicFill(symbols, opacity, size, rotation, anchor, disp);
        return fill;
    }

    @Override
    public GraphicStroke graphicStroke(final List<GraphicalSymbol> symbols,
            final Expression opacity, 
            final Expression size, 
            final Expression rotation, 
            final AnchorPoint anchor, 
            final Displacement disp,
            final Expression initial, 
            final Expression gap){
        return new DefaultGraphicStroke(symbols, opacity, size, rotation, anchor, disp, initial, gap);
    }

    @Override
    public Halo halo(final Fill fill, final Expression radius){
        Halo halo = new DefaultHalo(fill, radius);
        return halo;
    }

    @Override
    public LinePlacement linePlacement(final Expression offset,
            final Expression initial, 
            final Expression gap, 
            final boolean repeated, 
            final boolean aligned, 
            final boolean generalize){
        
        return new DefaultLinePlacement(offset, initial, gap, repeated, aligned, generalize);
    }


    @Override
    public Mark mark(final Expression wkn, final Fill fill, final Stroke stroke){
        Mark mark = new DefaultMark(wkn, fill, stroke);
        return mark;
    }

    @Override
    public Mark mark(final ExternalMark external, final Fill fill, final Stroke stroke){
        Mark mark = new DefaultMark(external, fill, stroke);
        return mark;
    }

    @Override
    public OnLineResource onlineResource(final URI uri){
        OnLineResource or = new DefaultOnlineResource(uri,null,null,null,null,null);
        return or;
    }

    @Override
    public PointPlacement pointPlacement(final AnchorPoint anchor, final Displacement disp, final Expression rotation){
        PointPlacement pp = new DefaultPointPlacement(anchor, disp, rotation);
        return pp;
    }

    @Override
    public SelectedChannelType selectedChannelType(final String name, final ContrastEnhancement enchance){
        SelectedChannelType sct = new DefaultSelectedChannelType(name, enchance);
        return sct;
    }
    
    @Override
    public Stroke stroke(
            final Expression color, 
            final Expression opacity, 
            final Expression width, 
            final Expression join, 
            final Expression cap, 
            final float[] dashes, 
            final Expression offset){
        Stroke str = new DefaultStroke(color, opacity, width, join, cap, dashes, offset);
        return str; 
    }

    @Override
    public Stroke stroke(
            final GraphicStroke stroke, 
            final Expression color, 
            final Expression opacity, 
            final Expression width, 
            final Expression join, 
            final Expression cap, 
            final float[] dashes, 
            final Expression offset){
        Stroke str = new DefaultStroke(stroke, color, opacity, width, join, cap, dashes, offset);
        return str; 
    }

    @Override
    public Stroke stroke(
            final GraphicFill fill, 
            final Expression color, 
            final Expression opacity, 
            final Expression width, 
            final Expression join, 
            final Expression cap, 
            final float[] dashes, 
            final Expression offset){
        Stroke str = new DefaultStroke(fill, color, opacity, width, join, cap, dashes, offset);
        return str; 
    }

    @Override
    public GraphicFill graphicFill(final Graphic graphic) {
        return new DefaultGraphicFill(graphic.graphicalSymbols(), 
                graphic.getOpacity(), graphic.getSize(), graphic.getRotation(), 
                graphic.getAnchorPoint(),graphic.getDisplacement());
    }

    @Override
    public GraphicStroke graphicStroke(final Graphic graphic) {
        return new DefaultGraphicStroke(graphic.graphicalSymbols(), 
                graphic.getOpacity(), graphic.getSize(), graphic.getRotation(), 
                graphic.getAnchorPoint(),graphic.getDisplacement(),
                FF.literal(0),FF.literal(0));
    }

    @Override
    public GraphicStroke graphicStroke(final Graphic graphic, final Expression gap, final Expression initialGap) {
        return new DefaultGraphicStroke(graphic.graphicalSymbols(), 
                graphic.getOpacity(), graphic.getSize(), graphic.getRotation(), 
                graphic.getAnchorPoint(),graphic.getDisplacement(),
                initialGap,gap);
    }

    @Override
    public GraphicLegend graphicLegend(List<GraphicalSymbol> symbols, Expression opacity,
            Expression size, Expression rotation, AnchorPoint anchorPoint, Displacement displacement) {
        return new DefaultGraphicLegend(symbols,
                opacity, size, rotation,
                anchorPoint, displacement);
    }
    
    @Override
    public GraphicLegend graphicLegend(final Graphic graphic) {
        return new DefaultGraphicLegend(graphic.graphicalSymbols(), 
                graphic.getOpacity(), graphic.getSize(), graphic.getRotation(), 
                graphic.getAnchorPoint(),graphic.getDisplacement());
    }

    @Override
    public ChannelSelection channelSelection(SelectedChannelType red, SelectedChannelType green, SelectedChannelType blue) {
        ChannelSelection selection = new DefaultChannelSelection(red,green,blue);
        return selection;
    }

    @Override
    public ChannelSelection channelSelection(final SelectedChannelType gray) {
        ChannelSelection selection = new DefaultChannelSelection(gray);
        return selection; 
    }

    
    //Functions-----------------------------------------------------------------
    @Override
    public Categorize categorizeFunction(final Expression lookup, final Map<Expression,Expression> values,
            final ThreshholdsBelongTo belongs, final Literal fallback) {
        return new DefaultCategorize(lookup,
                                    values,
                                    belongs,
                                    fallback);
    }

    @Override
    public Interpolate interpolateFunction(final Expression lookup, final List<InterpolationPoint> values,
            final Method method, final Mode mode, final Literal fallback) {
        return new DefaultInterpolate(lookup, values, method, mode, fallback);
    }

    @Override
    public InterpolationPoint interpolationPoint(final Expression value, final double data){
        return new DefaultInterpolationPoint(value, data);
    }
}
