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
package org.geotoolkit.process.coverage;

import java.util.Collection;

import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.util.SimpleInternationalString;

import org.opengis.feature.Feature;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 * Input and output descriptor for raster process.
 * Inputs :
 * <ul>
 *     <li>FEATURE_IN "reader_in" input GridCoverageReader</li>
 * </ul>
 * Outputs :
 * <ul>
 *     <li>FEATURE_OUT "feature_out" Features Collection </li>
 * </ul>
 * @author Quentin Boileau
 * @module pending
 */
public abstract class RasterDescriptor extends AbstractProcessDescriptor {

    /**
     * Mandatory - CoverageReader
     */
    public static final ParameterDescriptor<GridCoverageReader> READER_IN =
            new DefaultParameterDescriptor("reader_in", "Inpute GridCoverageReader", GridCoverageReader.class, null, true);
    /**
     * Mandatory - Resulting Feature Collection
     */
    public static final ParameterDescriptor<Collection<Feature>> FEATURE_OUT =
            new DefaultParameterDescriptor("feature_out", "Outpute Feature", Collection.class, null, true);

    /**
     * Default constructor
     * @param name : process descriptor name
     * @param msg  : process descriptor message
     * @param input : process input
     * @param output : process output
     */
    protected RasterDescriptor(String name, String msg,
            ParameterDescriptorGroup input, ParameterDescriptorGroup output) {

        super(name, CoverageProcessFactory.IDENTIFICATION,
                new SimpleInternationalString(msg),
                input, output);
    }
}