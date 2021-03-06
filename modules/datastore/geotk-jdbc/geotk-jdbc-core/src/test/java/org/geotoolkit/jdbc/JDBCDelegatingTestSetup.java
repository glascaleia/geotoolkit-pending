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

import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;


public class JDBCDelegatingTestSetup extends JDBCTestSetup {

    JDBCTestSetup delegate;
    
    protected JDBCDelegatingTestSetup( final JDBCTestSetup delegate ) {
        this.delegate = delegate;
    }

    @Override
    public void setUp() throws Exception {
        // make sure we don't forget to run eventual extra stuff
        delegate.setUp();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        delegate.tearDown();
    }
    
    @Override
    protected final void initializeDatabase() throws Exception {
        delegate.initializeDatabase();
    }

    @Override
    protected void initializeDataSource(final BasicDataSource ds, final Properties db) {
        delegate.initializeDataSource(ds, db);
    }

    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return delegate.createDataStoreFactory();
    }
    
    @Override
    protected void setUpDataStore(final JDBCDataStore dataStore) {
        delegate.setUpDataStore(dataStore);
    }

    @Override
    protected String typeName(final String raw) {
        return delegate.typeName(raw);
    }
    
    @Override
    protected String attributeName(final String raw) {
        return delegate.attributeName(raw);
    }
    
}
