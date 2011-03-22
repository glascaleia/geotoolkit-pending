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
package org.geotoolkit.process.vector.clipgeometry;

import com.vividsolutions.jts.geom.Geometry;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.vector.VectorDescriptor;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Process to clip a FeatureCollection using a geometry
 * @author Quentin Boileau
 * @module pending
 */
public class ClipGeometry extends AbstractProcess {

    ParameterValueGroup result;

    /**
     * Default constructor
     */
    public ClipGeometry() {
        super(ClipGeometryDescriptor.INSTANCE);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ParameterValueGroup getOutput() {
        return result;
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public void run() {
        final FeatureCollection<Feature> inputFeatureList = Parameters.value(ClipGeometryDescriptor.FEATURE_IN, inputParameters);
        final Geometry inputClippingGeometry = Parameters.value(ClipGeometryDescriptor.CLIP_GEOMETRY_IN, inputParameters);

        final ClipGeometryFeatureCollection resultFeatureList = new ClipGeometryFeatureCollection(inputFeatureList,inputClippingGeometry);

        result = super.getOutput();
        result.parameter(VectorDescriptor.FEATURE_OUT.getName().getCode()).setValue(resultFeatureList);
    }

    /**
     * Clip the feature with the Geometry Clipping
     * @param oldFeature Feature
     * @param newType the new FeatureType for the Feature
     * @return Feature
     */
    public static Feature clipFeature(final Feature oldFeature, final FeatureType newType, final Geometry clipGeometry) {

        final Feature resultFeature = FeatureUtilities.defaultFeature(newType, oldFeature.getIdentifier().getID());


        for (Property property : oldFeature.getProperties()) {
            if (property.getDescriptor() instanceof GeometryDescriptor) {
                final Geometry interGeometry = testClipping((Geometry) property.getValue(), clipGeometry);

                //test clipping
                if (interGeometry != null) {

                    resultFeature.getProperty(property.getName()).setValue(interGeometry);
                } else {
                    return null;
                }
            } else {
                resultFeature.getProperty(property.getName()).setValue(property.getValue());
            }
        }

        return resultFeature;
    }

    /**
     * Test clipping between the feature's geometry and the clipping geometry
     * @param featureGeometry Geometry
     * @param clippingGeometry Geometry
     * @return Geometry
     */
    public static Geometry testClipping(final Geometry featureGeometry, final Geometry clippingGeometry) {
        if(featureGeometry == null || clippingGeometry == null) return null;
        
        if(featureGeometry.intersects(clippingGeometry)){
            return featureGeometry.intersection(clippingGeometry);
        }else{
            return null;
        }
    }
}
