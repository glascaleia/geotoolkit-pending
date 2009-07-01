/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009 Geomatys
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

package org.geotoolkit.gui.swing.propertyedit.styleproperty;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.StyleConstants;
import org.geotools.data.DefaultQuery;
import org.geotools.feature.FeatureIterator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.style.Fill;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.PolygonSymbolizer;
import org.opengis.style.Stroke;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Analyze extends AbstractTableModel{

    private static NumberFormat FORMAT = NumberFormat.getNumberInstance();

    private static final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);

    public static final PropertyName NO_VALUE = FF.property("-");

    public static enum METHOD{
        EL("el"),
        QANTILE("qantile"),
        MANUAL("manual");

        private final String title;

        METHOD(String key){
            this.title = MessageBundle.getString(key);
        }

        public String getTitle(){
            return title;
        }

    };

    private FeatureMapLayer layer;
    private PropertyName classification;
    private PropertyName normalize;
    private int nbClasses = 5;
    private double[] values = new double[0];
    private Double[] allValues = new Double[0];
    private METHOD method = METHOD.EL;

    private boolean genericAnalyze = false;
    private boolean analyze = false;
    private final List<PropertyName> properties = new ArrayList<PropertyName>();
    private long count = 0;
    private double minimum = 0;
    private double maximum = 0;
    private double sum = 0;
    private double mean = 0;
    private double median = 0;
    private Symbolizer template = null;
    private Class<? extends Symbolizer> expectedType = null;

    public Analyze() {
    }

    public long getCount() {
        analyze();
        return count;
    }

    public double getSum() {
        analyze();
        return sum;
    }

    public double getMinimum() {
        analyze();
        return minimum;
    }

    public double getMaximum() {
        analyze();
        return maximum;
    }

    public double getMean() {
        analyze();
        return mean;
    }

    public double getMedian() {
        analyze();
        return median;
    }

    public double[] getValues() {
        analyze();
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    private void reset(){
        analyze = false;
    }

    public void setLayer(FeatureMapLayer layer) {
        this.layer = layer;
        genericAnalyze = false;
        reset();
        isIntervalStyle(layer.getStyle());
    }

    public boolean isIntervalStyle(MutableStyle style){

        if(style.featureTypeStyles().size() != 1) return false;

        MutableFeatureTypeStyle fts = style.featureTypeStyles().get(0);

        if(fts.rules().isEmpty()) return false;

        for(MutableRule r : fts.rules()){

            Filter f = r.getFilter();

            if(f == null || r.isElseFilter()) return false;

            if(r.symbolizers().size() != 1) return false;

            if(f instanceof And){
                And and = (And) f;

                if(and.getChildren().size() != 2) return false;

                Filter op1 = and.getChildren().get(0);
                Filter op2 = and.getChildren().get(1);

                if(op2 instanceof PropertyIsGreaterThanOrEqualTo){
                    //flip order
                    op1 = op2;
                    op2 = and.getChildren().get(0);
                }

                if(op1 instanceof PropertyIsGreaterThanOrEqualTo){
                    PropertyIsGreaterThanOrEqualTo under = (PropertyIsGreaterThanOrEqualTo) op1;
                    Expression exp1 = under.getExpression1();
                    Expression exp2 = under.getExpression2();

                    if(exp1 instanceof Divide){
                        Divide div = (Divide) exp1;
                        if(!properties.contains(div.getExpression1())) return false;
                        if(!properties.contains(div.getExpression2())) return false;
                    }else if(exp1 instanceof PropertyName){
                        PropertyName name = (PropertyName) exp1;
                        if(!properties.contains(name)) return false;
                    }else{
                        return false;
                    }

                    if(!(exp2 instanceof Literal)){
                        return false;
                    }

                    if(op2 instanceof PropertyIsLessThan || op2 instanceof PropertyIsLessThanOrEqualTo){
                        BinaryComparisonOperator bc = (BinaryComparisonOperator)op2;
                        Expression ex1 = under.getExpression1();
                        Expression ex2 = under.getExpression2();

                        if(ex1 instanceof Divide){
                            Divide div = (Divide) ex1;
                            if(!properties.contains(div.getExpression1())) return false;
                            if(!properties.contains(div.getExpression2())) return false;
                        }else if(ex1 instanceof PropertyName){
                            PropertyName name = (PropertyName) ex1;
                            if(!properties.contains(name)) return false;
                        }else{
                            return false;
                        }

                        if(!(ex2 instanceof Literal)){
                            return false;
                        }


                    }else{
                        return false;
                    }

                }else{
                    return false;
                }


            }

            template = r.symbolizers().get(0);
        }

        method = METHOD.MANUAL;
        nbClasses = fts.rules().size()+1;

        return true;
    }

    public FeatureMapLayer getLayer() {
        return layer;
    }

    public void setClassification(PropertyName classification) {
        this.classification = classification;
        genericAnalyze = false;
        reset();
    }

    public void setNormalize(PropertyName normalize) {
        this.normalize = normalize;
        genericAnalyze = false;
        reset();
    }

    public void setMethod(METHOD method) {
        this.method = method;
        reset();
    }

    public METHOD getMethod() {
        return method;
    }

    public void setNbClasses(int nbClasses) {
        this.nbClasses = nbClasses;
        reset();
    }

    public int getNbClasses() {
        return nbClasses;
    }

    public List<PropertyName> getProperties() {
        analyze();
        return properties;
    }

    public Symbolizer getTemplate() {
        analyze();
        return template;
    }

    public void setTemplate(Symbolizer template) {
        this.template = template;
    }

    private void genericAnalyze(){
        if(genericAnalyze) return;
        genericAnalyze = true;
        properties.clear();

        minimum = Double.POSITIVE_INFINITY;
        maximum = Double.NEGATIVE_INFINITY;
        count = 0;
        sum = 0;
        median = 0;
        mean = 0;


        //search the different numeric attributs
        SimpleFeatureType schema = layer.getFeatureSource().getSchema();

        for(PropertyDescriptor desc : schema.getDescriptors()){
            Class<?> type = desc.getType().getBinding();

            if(Number.class.isAssignableFrom(type)){
                properties.add(FF.property(desc.getName().getLocalPart()));
            }
        }

        //find the geometry class for template
        GeometryDescriptor geo = schema.getGeometryDescriptor();
        Class<?> geoClass = geo.getType().getBinding();

        if(Polygon.class.isAssignableFrom(geoClass) || MultiPolygon.class.isAssignableFrom(geoClass)){
            Stroke stroke = SF.stroke(Color.BLACK, 1);
            Fill fill = SF.fill(Color.BLUE);
            template = SF.polygonSymbolizer(stroke,fill,null);
            expectedType = PolygonSymbolizer.class;
        }else if(LineString.class.isAssignableFrom(geoClass) || MultiLineString.class.isAssignableFrom(geoClass)){
            Stroke stroke = SF.stroke(Color.BLUE, 2);
            template = SF.lineSymbolizer(stroke,null);
            expectedType = LineSymbolizer.class;
        }else{
            Stroke stroke = SF.stroke(Color.BLACK, 1);
            Fill fill = SF.fill(Color.BLUE);
            List<GraphicalSymbol> symbols = new ArrayList<GraphicalSymbol>();
            symbols.add(SF.mark(StyleConstants.MARK_CIRCLE, fill, stroke));
            Graphic gra = SF.graphic(symbols, FF.literal(1), FF.literal(12), FF.literal(0), SF.anchorPoint(), SF.displacement());
            template = SF.pointSymbolizer(gra, null);
            expectedType = PointSymbolizer.class;
        }


        //search the extreme values
        final DefaultQuery query = new DefaultQuery();

        if(classification == null || layer == null) return;

            if(!properties.contains(classification)) return;

        if(normalize == null || normalize.equals(NO_VALUE)){
            query.setPropertyNames(new String[]{classification.getPropertyName()});
        }else{
            if(!properties.contains(normalize)) return;
            query.setPropertyNames(new String[]{classification.getPropertyName(),
                                                normalize.getPropertyName()});
        }

        FeatureIterator<SimpleFeature> features = null;
        try{
            features = layer.getFeatureSource().getFeatures(query).features();
            List<Double> values = new ArrayList<Double>();

            while(features.hasNext()){
                SimpleFeature sf = features.next();
                count++;

                Number classifValue = classification.evaluate(sf, Number.class);
                double value;

                if(normalize == null || normalize.equals(NO_VALUE)){
                    value = classifValue.doubleValue();
                }else{
                    Number normalizeValue = normalize.evaluate(sf, Number.class);
                    value = classifValue.doubleValue() / normalizeValue.doubleValue();
                }

                values.add(value);
                sum += value;

                if(value < minimum){
                    minimum = value;
                }
                if(value > maximum){
                    maximum = value;
                }

            }

            mean = (minimum+maximum) / 2;

            //find the median
            allValues = values.toArray(new Double[values.size()]);
            Arrays.sort(allValues);

            if (values.size() % 2 == 0) {
                median = (allValues[(allValues.length / 2) - 1] + allValues[allValues.length / 2]) / 2.0;
            } else {
                median = allValues[allValues.length / 2];
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }finally{
            if(features != null){
                features.close();
            }
        }


    }

    private void analyze(){
        if(analyze) return;
        reset();
        genericAnalyze();
        analyze = true;

        if(method == METHOD.EL){
            values = new double[nbClasses+1];
            for(int i=0;i<values.length;i++){
                values[i] = minimum + (float)i / (values.length-1) * (maximum-minimum);
            }
            
        }else if(method == METHOD.QANTILE){
            values = new double[nbClasses+1];
            for(int i=0;i<values.length;i++){
                values[i] = allValues[i*(allValues.length-1)/(values.length-1)];
            }
        }else{
            if(values.length != nbClasses+1){
                values = Arrays.copyOf(values, nbClasses+1);
            }
        }


        analyze = true;
    }

    private Symbolizer createSymbolizer(IntervalPalette palette,double step){
        return derivateSymbolizer(template, palette.interpolate(step));
    }

    /**
     * Derivate a symbolizer with a new color.
     */
    private Symbolizer derivateSymbolizer(Symbolizer symbol, Color color){

        if(symbol instanceof PolygonSymbolizer){
            PolygonSymbolizer ps = (PolygonSymbolizer)symbol;
            Fill fill = SF.fill(SF.literal(color),ps.getFill().getOpacity());
            return SF.polygonSymbolizer(ps.getName(), ps.getGeometryPropertyName(),
                    ps.getDescription(), ps.getUnitOfMeasure(),
                    ps.getStroke(),fill,ps.getDisplacement(),ps.getPerpendicularOffset());
        }else if(symbol instanceof LineSymbolizer){
            LineSymbolizer ls = (LineSymbolizer) symbol;
            Stroke oldStroke = ls.getStroke();
            Stroke stroke = SF.stroke(SF.literal(color),oldStroke.getOpacity(),oldStroke.getWidth(),
                    oldStroke.getLineJoin(),oldStroke.getLineCap(),oldStroke.getDashArray(),oldStroke.getDashOffset());
            return SF.lineSymbolizer(ls.getName(), ls.getGeometryPropertyName(),
                    ls.getDescription(), ls.getUnitOfMeasure(), stroke, ls.getPerpendicularOffset());
        }else if(symbol instanceof PointSymbolizer){
            PointSymbolizer ps = (PointSymbolizer) symbol;
            Graphic oldGraphic = ps.getGraphic();
            Mark oldMark = (Mark) oldGraphic.graphicalSymbols().get(0);
            Fill fill = SF.fill(SF.literal(color),oldMark.getFill().getOpacity());
            List<GraphicalSymbol> symbols = new ArrayList<GraphicalSymbol>();
            symbols.add(SF.mark(oldMark.getWellKnownName(), fill, oldMark.getStroke()));
            Graphic graphic = SF.graphic(symbols, oldGraphic.getOpacity(),oldGraphic.getSize(),
                    oldGraphic.getRotation(),oldGraphic.getAnchorPoint(),oldGraphic.getDisplacement());
            return SF.pointSymbolizer(graphic,ps.getGeometryPropertyName());
        }else{
            throw new IllegalArgumentException("unexpected symbolizer type : " + symbol);
        }

    }

    public List<MutableRule> generateRules(IntervalPalette palette){
        analyze();
        List<MutableRule> rules = new ArrayList<MutableRule>();

        final Expression exp;
        if(normalize == null || normalize.equals(NO_VALUE)){
            exp = classification;
        }else{
            exp = FF.divide(classification, normalize);
        }

        for(int i=1;i<values.length;i++){
            final MutableRule rule = SF.rule();

            double start = values[i-1];
            double end = values[i];

            //create the filter and title
            final Filter interval;
            final String title;
            if(i == values.length-1){
                //last element
                Filter above = FF.greaterOrEqual(exp, FF.literal(start));
                Filter under = FF.lessOrEqual(exp, FF.literal(end));
                interval = FF.and(above, under);
                title = "[ " + FORMAT.format(start) + " -> " + FORMAT.format(end) + " ]";
            }else{
                Filter above = FF.greaterOrEqual(exp, FF.literal(start));
                Filter under = FF.less(exp, FF.literal(end));
                interval = FF.and(above, under);
                title = "[ " + FORMAT.format(start) + " -> " + FORMAT.format(end) + " [";
            }
            rule.setFilter(interval);
            rule.setName(title);
            rule.setDescription(SF.description(title, title));

            //create the style
            Symbolizer symbol = createSymbolizer(palette,(double)(i-1)/(values.length-2));
            rule.symbolizers().add(symbol);

            rules.add(rule);
        }

        return rules;
    }

    public JFreeChart getChart(int nbDivision){
        analyze();

        XYSeries series = new XYSeries( "Data" ) ;

        double before = minimum;
        for(float i=1;i<=nbDivision;i++){
            final double localmin = before;
            final double localmax = minimum + (i/nbDivision)*(maximum-minimum);
            before = localmax;
            long localsum = 0;

            for(Double d : allValues){
                if(i == 100){
                    //last element
                    if(d>=localmin && d<=localmax){
                        localsum++;
                    }
                }else{
                    if(d>=localmin && d<localmax){
                        localsum++;
                    }
                }
            }
            series.add( minimum+(localmin+localmax)/2, localsum) ;
        }

	XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYBarChart(
		"",
		"",
		false,
		"",
		dataset,
		PlotOrientation.VERTICAL,
		false,
		false,
		false
		);

        XYPlot plot = chart.getXYPlot();
        ((XYBarRenderer)plot.getRenderer()).setShadowVisible(false);
        ((XYBarRenderer)plot.getRenderer()).setUseYInterval(false);
        ((XYBarRenderer)plot.getRenderer()).setMargin(0);

        chart.getPlot().setBackgroundAlpha(0);
        chart.setBackgroundPaint(new Color(0f,0f,0f,0f));

	return chart ;
    }

    ////////////// TABLE MODEL /////////////////////////////////////////////////

    @Override
    public int getRowCount() {
        return nbClasses;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        analyze();
        return values[rowIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return method == METHOD.MANUAL;
    }

}
