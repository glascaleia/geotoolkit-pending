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
package org.geotoolkit.data.postgis.ps;

import org.geotoolkit.data.postgis.PostGISBooleanTestSetup;
import org.geotoolkit.jdbc.JDBCBooleanTest;
import org.geotoolkit.jdbc.JDBCBooleanTestSetup;


public class PostGISBooleanTest extends JDBCBooleanTest {

    @Override
    protected JDBCBooleanTestSetup createTestSetup() {
        return new PostGISBooleanTestSetup(new PostGISPSTestSetup());
    }

}
