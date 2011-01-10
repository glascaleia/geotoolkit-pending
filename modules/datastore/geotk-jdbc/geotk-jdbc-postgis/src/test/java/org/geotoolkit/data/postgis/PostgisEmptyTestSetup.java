/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
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
package org.geotoolkit.data.postgis;

import org.geotoolkit.jdbc.JDBCEmptyTestSetup;
import org.geotoolkit.jdbc.JDBCTestSetup;


public class PostgisEmptyTestSetup extends JDBCEmptyTestSetup {

    public PostgisEmptyTestSetup(final JDBCTestSetup delegate) {
        super(delegate);
        
    }

    @Override
    protected void createEmptyTable() throws Exception {
        run("CREATE TABLE \"empty\"(\"key\" serial primary key)");
        run("SELECT AddGeometryColumn('empty', 'geom', -1, 'GEOMETRY', 2)");
    }

    @Override
    protected void dropEmptyTable() throws Exception {
        runSafe("DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'empty'");
        runSafe("DROP TABLE IF EXISTS \"empty\"");
    }

}
