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
package org.geotoolkit.process.vector.maxlimit;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.memory.GenericMaxFeatureIterator;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.vector.VectorDescriptor;

import org.opengis.feature.Feature;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Limit a FeatureCollection returns to a maximum
 * @author Quentin Boileau
 * @module pending
 */
public class MaxLimit extends AbstractProcess {

    /**
     * Default constructor
     */
    public MaxLimit(final ParameterValueGroup input) {
        super(MaxLimitDescriptor.INSTANCE,input);
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public ParameterValueGroup call() {
        fireStartEvent(new ProcessEvent(this));
        final FeatureCollection<Feature> inputFeatureList = Parameters.value(MaxLimitDescriptor.FEATURE_IN, inputParameters);
        final int max = Parameters.value(MaxLimitDescriptor.MAX_IN, inputParameters);
        
        final FeatureCollection resultFeatureList = GenericMaxFeatureIterator.wrap(inputFeatureList, max);

        outputParameters.parameter(VectorDescriptor.FEATURE_OUT.getName().getCode()).setValue(resultFeatureList);
        fireEndEvent(new ProcessEvent(this,null,100));
        return outputParameters;
    }
}
