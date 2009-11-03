/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Geomatys
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

package org.geotoolkit.display2d.ext.rastermask;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.style.CachedSymbolizer;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;
import org.geotoolkit.util.NumberRange;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CachedRasterMaskSymbolizer extends CachedSymbolizer<RasterMaskSymbolizer>{

    private static final BufferedImage TRANSLUCENT = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public CachedRasterMaskSymbolizer(RasterMaskSymbolizer symbol){
        super(symbol);
    }

    public Map<SimpleFeature,List<CachedSymbolizer>> getMasks(GridCoverage2D coverage) throws IOException, TransformException{
        final Map<SimpleFeature,List<CachedSymbolizer>> features = new HashMap<SimpleFeature, List<CachedSymbolizer>>();
        final Map<NumberRange,List<CachedSymbolizer>> styles = new HashMap<NumberRange, List<CachedSymbolizer>>();
        final Map<Expression, List<Symbolizer>> categorizes = styleElement.getThredholds();
        final Expression[] steps = categorizes.keySet().toArray(new Expression[categorizes.size()]);

        //fill the numberranges ------------------------------------------------
        double last = Double.NEGATIVE_INFINITY;
        double end = Double.POSITIVE_INFINITY;
        NumberRange interval;
        int i=0;
        for(;i<steps.length-1;i++){
            end = steps[i+1].evaluate(null,Double.class);
            interval = NumberRange.create(last,end);
            styles.put(interval, getCached(categorizes.get(steps[i])));
            last = end;
        }

        //last element
        end = Double.POSITIVE_INFINITY;
        styles.put(NumberRange.create(last,end),
                getCached(categorizes.get(steps[i])) );

        //calculate the polygons -----------------------------------------------
        final Map<NumberRange,Geometry> polygons = RasterMaskUtilies.toPolygon(coverage, styles.keySet(),0);


        //build the features ---------------------------------------------------
        final SimpleFeatureTypeBuilder sftBuilder = new SimpleFeatureTypeBuilder();
        final String geometryField = "geometry";
        sftBuilder.setName("DynamicFeature");
        sftBuilder.setCRS(coverage.getCoordinateReferenceSystem());
        sftBuilder.add(geometryField, Geometry.class);
        sftBuilder.setDefaultGeometry(geometryField);
        final SimpleFeatureType sft = sftBuilder.buildFeatureType();

        final SimpleFeatureBuilder sfBuilder = new SimpleFeatureBuilder(sft);
        int id = 0;
        for(final Entry<NumberRange,Geometry> entry : polygons.entrySet()){
            sfBuilder.reset();
            sfBuilder.set(geometryField, entry.getValue());
            final SimpleFeature sf = sfBuilder.buildFeature(String.valueOf(id++));

            features.put(sf, styles.get(entry.getKey()));
        }

        return features;
    }

    private static List<CachedSymbolizer> getCached(List<Symbolizer> symbols){
        final List<CachedSymbolizer> cached = new ArrayList<CachedSymbolizer>();

        for(final Symbolizer sy : symbols){
            cached.add(GO2Utilities.getCached(sy));
        }

        return cached;
    }

    @Override
    public float getMargin(Feature feature, float coeff) {
        return 0;
    }

    @Override
    protected void evaluate() {
    }

    @Override
    public boolean isVisible(Feature feature) {
        return false;
    }

}
