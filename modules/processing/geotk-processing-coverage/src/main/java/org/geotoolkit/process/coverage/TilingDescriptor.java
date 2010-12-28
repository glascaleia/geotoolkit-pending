/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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

import java.io.File;

import org.geotoolkit.image.io.mosaic.TileManager;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.util.SimpleInternationalString;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Description of a coverage to polygon process.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public final class TilingDescriptor extends AbstractProcessDescriptor{

    public static final String NAME = "tyling";

    /**
     * Mandatory - Coverage to process
     */
    public static final GeneralParameterDescriptor IN_SOURCE_FILE =
            new DefaultParameterDescriptor("source","Coverage to tyle.",File.class,null,true);

    /**
     * Mandatory - Output folder
     */
    public static final GeneralParameterDescriptor IN_TILES_FOLDER =
            new DefaultParameterDescriptor("target","Folder where tiles will be stored.",File.class,null,true);


    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup(NAME+"InputParameters",
                new GeneralParameterDescriptor[]{IN_SOURCE_FILE,IN_TILES_FOLDER});

    /**
     * Mandatory - Resulting tile manager
     */
    public static final GeneralParameterDescriptor OUT_TILE_MANAGER =
            new DefaultParameterDescriptor("manager","Tile manager.",TileManager.class,null,true);

    /**
     * Optional - Coordinate Reference system of the tyle manager.
     */
    public static final GeneralParameterDescriptor OUT_CRS =
            new DefaultParameterDescriptor("crs","Tile manager's coordinate reference system.",CoordinateReferenceSystem.class,null,false);


    public static final ParameterDescriptorGroup OUTPUT_DESC =
            new DefaultParameterDescriptorGroup(NAME+"OutputParameters",
                new GeneralParameterDescriptor[]{OUT_TILE_MANAGER,OUT_CRS});
    
    public static final ProcessDescriptor INSTANCE = new TilingDescriptor();


    private TilingDescriptor(){
        super(NAME, CoverageProcessFactory.IDENTIFICATION,
                new SimpleInternationalString("Create a pyramid/mosaic from the given"
                + "source. Created tiles are stored in the given folder."),
                INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public Process createProcess() {
        return new TilingProcess();
    }

}