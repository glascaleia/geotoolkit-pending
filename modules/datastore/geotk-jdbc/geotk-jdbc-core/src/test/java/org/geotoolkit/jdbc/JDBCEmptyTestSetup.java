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
package org.geotoolkit.jdbc;

import java.sql.SQLException;


/**
 * Test setup for executing tests on tables with no data. 
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 * @module pending
 */
public abstract class JDBCEmptyTestSetup extends JDBCDelegatingTestSetup {

    protected JDBCEmptyTestSetup(final JDBCTestSetup delegate) {
        super(delegate);
    }

    @Override
    protected final void setUpData() throws Exception {
        //kill all the data
        try {
            dropEmptyTable();
        } catch (SQLException e) {
        }

        //create all the data
        createEmptyTable();
    }

    /**
     * Creates a table with the following schema:
     * <p>
     * empty( id:Integer; geom: Geometry)
     * </p>
     * <p>
     * The table should be empty.
     * </p>
     */
    protected abstract void createEmptyTable() throws Exception;

    /**
     * Drops the "empty" table previously created
     */
    protected abstract void dropEmptyTable() throws Exception;
    
}
