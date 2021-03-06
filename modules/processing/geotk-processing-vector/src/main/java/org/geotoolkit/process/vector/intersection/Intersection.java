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
package org.geotoolkit.process.vector.intersection;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.vector.VectorDescriptor;
import org.geotoolkit.process.vector.VectorProcessUtils;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 * Generate a FeatureCollection where each Feature are the intersections of the two input
 * FeatureCollection's geometries.It is usually called "Spatial AND".
 * @author Quentin Boileau
 * @module pending
 */
public class Intersection extends AbstractProcess {

    /**
     * Default constructor
     */
    public Intersection(final ParameterValueGroup input) {
        super(IntersectionDescriptor.INSTANCE,input);
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public ParameterValueGroup call() {
        fireStartEvent(new ProcessEvent(this));
        final FeatureCollection<Feature> inputFeatureList = Parameters.value(IntersectionDescriptor.FEATURE_IN, inputParameters);
        final FeatureCollection<Feature> inputFeatureIntersectionList = Parameters.value(IntersectionDescriptor.FEATURE_INTER, inputParameters);
        final String inputGeometryName = Parameters.value(IntersectionDescriptor.GEOMETRY_NAME, inputParameters);

        final FeatureCollection resultFeatureList = new IntersectionFeatureCollection(inputFeatureList, inputFeatureIntersectionList, inputGeometryName);

        outputParameters.parameter(VectorDescriptor.FEATURE_OUT.getName().getCode()).setValue(resultFeatureList);
        fireEndEvent(new ProcessEvent(this, null, 100));
        return outputParameters;
    }

    /**
     * Intersection a feature with the FeatureCollection's geometries
     * @param oldFeature Feature
     * @param newType the new FeatureType for the Feature
     * @param featureClippingList FeatureCollection used to clip
     * @return Feature
     */
    public static FeatureCollection intersetFeature(final Feature oldFeature, final FeatureType newType,
            final FeatureCollection<Feature> featureClippingList, final String geometryName)
            throws FactoryException, MismatchedDimensionException, TransformException, ProcessException {
        
        return VectorProcessUtils.intersectionFeatureToColl(oldFeature, featureClippingList, geometryName);
    }
}
