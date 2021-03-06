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
package org.geotoolkit.process.vector.filter;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.memory.GenericFilterFeatureIterator;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.vector.VectorDescriptor;

import org.opengis.feature.Feature;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Adding on the fly attributes of Feature contents.
 * @author Quentin Boileau
 * @module pending
 */
public class Filter extends AbstractProcess {

    /**
     * Default constructor
     */
    public Filter(final ParameterValueGroup input) {
        super(FilterDescriptor.INSTANCE,input);
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public ParameterValueGroup call() {
        fireStartEvent(new ProcessEvent(this));
        final FeatureCollection<Feature> inputFeatureList = Parameters.value(FilterDescriptor.FEATURE_IN, inputParameters);
        final org.opengis.filter.Filter filter = Parameters.value(FilterDescriptor.FILTER_IN, inputParameters);
        
        final FeatureCollection resultFeatureList = GenericFilterFeatureIterator.wrap(inputFeatureList, filter);

        outputParameters.parameter(VectorDescriptor.FEATURE_OUT.getName().getCode()).setValue(resultFeatureList);
        fireEndEvent(new ProcessEvent(this,null,100));
        return outputParameters;
    }
}
