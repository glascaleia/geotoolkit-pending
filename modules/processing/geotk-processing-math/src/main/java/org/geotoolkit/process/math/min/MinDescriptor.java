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
package org.geotoolkit.process.math.min;

import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.math.MathProcessFactory;
import org.geotoolkit.util.SimpleInternationalString;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 * @author Quentin Boileau (Geomatys)
 * @module pending
 */
public class MinDescriptor extends AbstractProcessDescriptor{
        
    /**Process name : min */
    public static final String NAME = "min";
    
    /**
     * Input parameters
     */
    public static final ParameterDescriptor<Double[]> SET =
            new DefaultParameterDescriptor("set", "Set of double", Double[].class, null, true);
    
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SET});
    
    /**
     * OutputParameters
     */
    public static final ParameterDescriptor<Double> RESULT_NUMBER =
            new DefaultParameterDescriptor("result", "Min double result", Double.class, null, true);
    public static final ParameterDescriptorGroup OUTPUT_DESC =
            new DefaultParameterDescriptorGroup("OutputParameters",
            new GeneralParameterDescriptor[]{RESULT_NUMBER});

    /** Instance */
    public static final ProcessDescriptor INSTANCE = new MinDescriptor();

    private MinDescriptor() {
        super(NAME, MathProcessFactory.IDENTIFICATION,
                new SimpleInternationalString("Return the min double from an array of double"),
                INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public Process createProcess() {
        return new MinProcess();
    }
    
}
