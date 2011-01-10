/*
 *    GeotoolKit - An Open source Java GIS Toolkit
 *    http://geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.shapefile.indexed;


import org.geotoolkit.data.shapefile.ShpFiles;
import org.geotoolkit.data.shapefile.AbstractTestCaseSupport;

import java.io.File;
import java.io.IOException;

public abstract class FIDTestCase extends AbstractTestCaseSupport {
    protected FIDTestCase( final String name ) throws IOException {
        super(name);
    }

    protected final String TYPE_NAME = "archsites";

    protected File backshp;
    protected File backdbf;
    protected File backshx;
    protected File backprj;
    protected File backqix;
    String filename;
    protected File fixFile;

    protected ShpFiles shpFiles;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        backshp = copyShapefiles("shapes/" + TYPE_NAME + ".shp");

        backdbf = sibling(backshp, "dbf");
        backshx = sibling(backshp, "shx");
        backprj =  sibling(backshp, "prj");
        backqix =  sibling(backshp, "qix");
        
        fixFile =  sibling(backshp, "fix");

        shpFiles = new ShpFiles(backshx);

    }

}
