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
package org.geotoolkit.mapfile.process;

import java.awt.Color;
import java.util.List;
import java.util.Collection;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.geotoolkit.filter.DefaultFilterFactory2;
import org.geotoolkit.mapfile.MapfileReader;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.sld.DefaultSLDFactory;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableSLDFactory;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.Graphic;
import org.opengis.style.Mark;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.AnchorPoint;
import org.opengis.style.TextSymbolizer;
import org.opengis.style.Font;
import org.opengis.style.Halo;
import org.opengis.style.LabelPlacement;
import org.opengis.style.Description;
import org.opengis.style.Displacement;
import org.opengis.style.Fill;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.Stroke;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Symbolizer;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.Feature;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.mapfile.process.MapfileToSLDDescriptor.*;
import static org.geotoolkit.parameter.Parameters.*;
import static org.geotoolkit.mapfile.MapfileTypes.*;
import static org.geotoolkit.style.StyleConstants.*;

/**
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class MapfileToSLDProcess extends AbstractProcess{
    
    private static final MutableSLDFactory SLDF = new DefaultSLDFactory();
    private static final MutableStyleFactory SF = new DefaultStyleFactory();
    private static final FilterFactory FF = new DefaultFilterFactory2();
    
    private Feature mapfileFeature = null;
    
    public MapfileToSLDProcess(final ParameterValueGroup input){
        super(INSTANCE, input);
    }
    
    @Override
    public ParameterValueGroup call() throws ProcessException{
        
        final File mapfile  = value(IN_FILE, inputParameters);
        final File sldfile  = value(IN_OUTPUT, inputParameters);
        
        final MapfileReader reader = new MapfileReader();
        reader.setInput(mapfile);
        try {
            mapfileFeature = reader.read();            
            final MutableStyledLayerDescriptor sld = SLDF.createSLD();
            
            //convert it
            convert(sld, mapfileFeature);
            
            //avoid memory leak
            mapfileFeature = null;
            
            //write the sld
            final XMLUtilities utils = new XMLUtilities();
            utils.writeSLD(sldfile, sld, StyledLayerDescriptor.V_1_1_0);            
        } catch (IOException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        } catch (JAXBException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        } finally {
        }
        
        return outputParameters;
    }
    
    private void convert(final MutableStyledLayerDescriptor sld, final Feature feature) throws ProcessException{
        
        final Collection<Property> layers = feature.getProperties(MAP_LAYER.getName());
        
        for(final Property p : layers){
            //create an sld layer
            final ComplexAttribute mflayer = (ComplexAttribute) p;            
            final MutableNamedLayer sldLayer = SLDF.createNamedLayer();
            sld.layers().add(sldLayer);
            
            final String name = String.valueOf(mflayer.getProperty(LAYER_NAME.getName()).getValue());
            sldLayer.setName(name);
            sldLayer.setDescription(SF.description(name, name));
            
            //create the style
            final MutableStyle sldStyle = SF.style();
            sldLayer.styles().add(sldStyle);
            final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
            sldStyle.featureTypeStyles().add(fts);
            
            final Double minscale = getValue(mflayer,LAYER_MINSCALEDENOM,Double.class);
            final Double maxscale = getValue(mflayer,LAYER_MAXSCALEDENOM,Double.class);       
            final Collection<Property> classes = mflayer.getProperties(LAYER_CLASS.getName());
                        
            for(final Property pp : classes){
                final ComplexAttribute clazz = (ComplexAttribute) pp;
                final MutableRule rule = createRule(mflayer, minscale, maxscale, clazz);
                fts.rules().add(rule);
            }
                        
        }
        
    }
    
    private <T> T getValue(final ComplexAttribute cpx, final PropertyDescriptor desc, final Class<T> clazz){
        final Property prop = cpx.getProperty(desc.getName());
        if(prop != null){
            return (T)prop.getValue();
        }
        return null;
    }
    
    private MutableRule createRule(final ComplexAttribute mflayer, 
            final Double minScale, final Double maxscale, final ComplexAttribute clazz) throws ProcessException{
        
        //mapfile type is similar to se symbolizer type
        final String type = getValue(mflayer,LAYER_TYPE,String.class);
            
        final MutableRule rule = SF.rule();
        rule.setMinScaleDenominator(minScale);
        rule.setMaxScaleDenominator(maxscale);
        
        // Class can act as filter, the classItem is the propertyname on which the class
        // Expression is evaluated
        final PropertyName classItem = getValue(mflayer,LAYER_CLASSITEM,PropertyName.class);
        final String classExpression = getValue(clazz,CLASS_EXPRESSION,String.class);
        if(classItem != null && classExpression != null){
            // equivalant to OGC filter : PropertyEquals(name,value)
            final Filter filter = toFilter(classItem, classExpression);
            rule.setFilter(filter);
        }else{
            //filter
            //not handle yet
        }
        
        final Collection<Property> styles = clazz.getProperties(CLASS_STYLE.getName());
        final Collection<Property> labels = clazz.getProperties(CLASS_LABEL.getName());
        
        for(final Property pp : styles){
            final ComplexAttribute style = (ComplexAttribute) pp;
            
            if("POLYGON".equalsIgnoreCase(type)){
                rule.symbolizers().addAll(createPolygonSymbolizer(style));
            }else if("LINE".equalsIgnoreCase(type)){
                rule.symbolizers().addAll(createLineSymbolizer(style));
            }else if("ANNOTATION".equalsIgnoreCase(type)){
                rule.symbolizers().addAll(createPointSymbolizer(style));
            }
        }
        
        for(final Property pp : labels){
            final ComplexAttribute style = (ComplexAttribute) pp;
            
            //this property contain the label to place in the text symbolizer
            Expression labelProp = getValue(mflayer,LAYER_LABELITEM,PropertyName.class);
            Expression labelOverride = getValue(mflayer,CLASS_TEXT,Expression.class);
            if(labelProp == null || labelOverride != null){
                //Class Text take priority over label item
                labelProp = labelOverride;
            }
            
            rule.symbolizers().addAll(createTextSymbolizer(labelProp,style));
        }
        
        return rule;
    }
    
    private List<Symbolizer> createPolygonSymbolizer(final ComplexAttribute style){
        
        Expression expColor = getValue(style, STYLE_COLOR, Expression.class);
        Expression expOpacity = getValue(style, STYLE_WIDTH, Expression.class);
        
        if(expOpacity == null){
            expOpacity = DEFAULT_FILL_OPACITY;
        }
        if(expColor == null){
            expColor = DEFAULT_FILL_COLOR;
        }
        
        Fill fill = null;
        Stroke stroke = null;
        fill = SF.fill(expColor,expOpacity);
        
        final List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
        
        //general informations
        final String name = "";
        final Description desc = DEFAULT_DESCRIPTION;
        final String geometry = null; //use the default geometry of the feature
        final Unit unit = NonSI.PIXEL;
        final Displacement disp = DEFAULT_DISPLACEMENT;
        final Expression offset = LITERAL_ZERO_FLOAT;

        //stroke element
        //final Expression color = SF.literal(Color.BLUE);
        //final Expression width = FF.literal(4);
        //final Expression opacity = LITERAL_ONE_FLOAT;
        //final Stroke stroke = SF.stroke(color,width,opacity);


        final PolygonSymbolizer symbolizer = SF.polygonSymbolizer(name,geometry,desc,unit,stroke,fill,disp,offset);
        symbolizers.add(symbolizer);
        
        return symbolizers;
    }
    
    private List<Symbolizer> createLineSymbolizer(final ComplexAttribute style){
        
        Expression expColor = getValue(style, STYLE_COLOR, Expression.class);
        Expression expWidth = getValue(style, STYLE_WIDTH, Expression.class);
        Expression expOpacity = getValue(style, STYLE_OPACITY, Expression.class);
        
        if(expColor == null){
            expColor = DEFAULT_STROKE_COLOR;
        }        
        if(expOpacity == null){
            expOpacity = DEFAULT_STROKE_OPACITY;
        }        
        if(expWidth == null){
            expWidth = DEFAULT_STROKE_WIDTH;
        }
                
        final List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
        
        //general informations
        final String name = "";
        final Description desc = DEFAULT_DESCRIPTION;
        final String geometry = null; //use the default geometry of the feature
        final Unit unit = NonSI.PIXEL;
        final Expression offset = LITERAL_ZERO_FLOAT;

        //stroke element
        final Stroke stroke = SF.stroke(expColor,expWidth,expOpacity);


        final LineSymbolizer symbolizer = SF.lineSymbolizer(name,geometry,desc,unit,stroke,offset);
        symbolizers.add(symbolizer);
        
        return symbolizers;
    }
    
    private List<Symbolizer> createTextSymbolizer(final Expression label, final ComplexAttribute lblStyle){
                
        Expression expLabelColor = getValue(lblStyle, LABEL_COLOR, Expression.class);
        Expression expLabelSize  = getValue(lblStyle, LABEL_SIZE, Expression.class);
        
        Expression expHaloColor = getValue(lblStyle, LABEL_OUTLINECOLOR, Expression.class);
        Integer valHaloWidth = getValue(lblStyle, LABEL_OUTLINEWIDTH, Integer.class);
        
        if(expLabelColor == null){
            expLabelColor = SF.literal(Color.BLACK);
        }
        if(expLabelSize == null){
            expLabelSize = DEFAULT_FONT_SIZE;
        }
        if(expHaloColor == null){
            expHaloColor = SF.literal(Color.WHITE);
        }        
        if(valHaloWidth == null){
            valHaloWidth = 0;
        }
        
        
        Expression expHaloWidth = FF.literal(valHaloWidth);
                
        final List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
        
        //general informations
        final String name = "";
        final Description desc = DEFAULT_DESCRIPTION;
        final String geometry = null; //use the default geometry of the feature
        final Unit unit = NonSI.PIXEL;
        final Font font = SF.font(
                FF.literal("Arial"),
                FONT_STYLE_NORMAL,
                FONT_WEIGHT_NORMAL,
                expLabelSize);
        final LabelPlacement placement = SF.pointPlacement();
        final Halo halo = SF.halo(SF.fill(expHaloColor), expHaloWidth);
        final Fill fill = SF.fill(expLabelColor);

        final TextSymbolizer symbol = SF.textSymbolizer(name, geometry, desc, unit, label, font, placement, halo, fill);
        symbolizers.add(symbol);
        
        return symbolizers;
    }
    
    private List<Symbolizer> createPointSymbolizer(final ComplexAttribute style){
        
        final String symbolName = getValue(style, STYLE_SYMBOL, String.class);
        Expression expSize = getValue(style, STYLE_SIZE, Expression.class);
        Expression expOpacity = getValue(style, STYLE_OPACITY, Expression.class);
        Expression expFillColor = getValue(style, STYLE_COLOR, Expression.class);
        Expression expStrokeColor = getValue(style, STYLE_OUTLINECOLOR, Expression.class);
        Expression expStrokeWidth = getValue(style, STYLE_WIDTH, Expression.class);
        
        if(expFillColor == null){
            expFillColor = DEFAULT_FILL_COLOR;
        }
        if(expStrokeColor == null){
            expStrokeColor = DEFAULT_STROKE_COLOR;
        }
        if(expStrokeWidth == null){
            expStrokeWidth = FF.literal(0);
        }
        if(expOpacity == null){
            expOpacity = DEFAULT_GRAPHIC_OPACITY;
        }        
        if(expSize == null){
            expSize = DEFAULT_GRAPHIC_SIZE;
        }
        
        final List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
        
        final ComplexAttribute symbol = getSymbol(symbolName);
        if(symbol == null){
            //no symbol found for this name
            return symbolizers;
        }
        
        final Stroke stroke = SF.stroke(expStrokeColor, expStrokeWidth);
        final Fill fill = SF.fill(expFillColor);
        
        final String symbolTypeName = getValue(symbol, SYMBOL_TYPE, String.class);
        
        final Mark mark;
        if("ellipse".equals(symbolTypeName)){
            mark = SF.mark(MARK_CIRCLE, fill, stroke);
        }else if("hatch".equals(symbolTypeName)){
            //TODO
            mark = SF.mark(MARK_SQUARE, fill, stroke);
        }else if("pixmap".equals(symbolTypeName)){
            //TODO
            mark = SF.mark(MARK_SQUARE, fill, stroke);
        }else if("simple".equals(symbolTypeName)){
            //TODO
            mark = SF.mark(MARK_SQUARE, fill, stroke);
        }else if("truetype".equals(symbolTypeName)){
            //TODO
            mark = SF.mark(MARK_SQUARE, fill, stroke);
        }else if("vector".equals(symbolTypeName)){
            //TODO
            mark = SF.mark(MARK_SQUARE, fill, stroke);
        }else{
            //can not build symbol
            return symbolizers;
        }
        
        
        //general informations
        final String name = "";
        final Description desc = DEFAULT_DESCRIPTION;
        final String geometry = null; //use the default geometry of the feature
        final Unit unit = NonSI.PIXEL;

        //the visual element
        final Expression opacity = LITERAL_ONE_FLOAT;
        final Expression rotation = LITERAL_ZERO_FLOAT;
        final AnchorPoint anchor = DEFAULT_ANCHOR_POINT;
        final Displacement disp = DEFAULT_DISPLACEMENT;

        final List<GraphicalSymbol> symbols = new ArrayList<GraphicalSymbol>();
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, opacity, expSize, rotation, anchor, disp);

        final PointSymbolizer symbolizer = SF.pointSymbolizer(name,geometry,desc,unit, graphic);
        symbolizers.add(symbolizer);
        
        return symbolizers;
    }
    
    /**
     * 
     * @param name : symbol name
     * @return the symbol which has the given name 
     */
    private ComplexAttribute getSymbol(final String name){
        
        if(name == null){
            return null;
        }
        
        final Collection<Property> symbols = mapfileFeature.getProperties(MAP_SYMBOL.getName());
        
        for(final Property p : symbols){
            final ComplexAttribute ca = (ComplexAttribute) p;
            if(name.equals(ca.getProperty(SYMBOL_NAME.getName()).getValue())){
                return ca;
            }
        }        
        
        return null;
    }
    
    private static Filter toFilter(final Expression ref, final String text) throws ProcessException{
        final ProcessDescriptor desc = MapfileFilterToOGCFilterDescriptor.INSTANCE;
        final ParameterValueGroup input = desc.getInputDescriptor().createValue();
        getOrCreate(MapfileFilterToOGCFilterDescriptor.IN_TEXT, input).setValue(text);
        getOrCreate(MapfileFilterToOGCFilterDescriptor.IN_REFERENCE, input).setValue(ref);
        
        final org.geotoolkit.process.Process process = desc.createProcess(input);        
        final ParameterValueGroup output = process.call();
        final Filter result = (Filter) value(MapfileFilterToOGCFilterDescriptor.OUT_OGC, output);
        return result;
    }
}
