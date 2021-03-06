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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotoolkit.jdbc.dialect.SQLDialect;


/**
 * Sets up the test harness for a database.
 * <p>
 * The responsibilities of the test harness are the following:
 * <ol>
 *   <li>Create and configure the {@link DataSource} used to connect to the
 *   underlying database
 *   <li>Provide the dialect used to communicate with the underlying database
 *   <li>Populate the underlying database with the data used by the tests.
 * </ol>
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @module pending
 */
public abstract class JDBCTestSetup {
    static final Logger LOGGER = org.geotoolkit.util.logging.Logging.getLogger(
            "org.geotoolkit.data.jdbc");
    private DataSource dataSource = null;

    public DataSource getDataSource() throws IOException {
        if(dataSource == null)
            dataSource = createDataSource();
        return dataSource;
    }

    public void setUp() throws Exception {
        //
    }

    protected void initializeDatabase() throws Exception {
    }

    protected void setUpData() throws Exception {
    }

    protected void setUpDataStore(final JDBCDataStore dataStore) {
    }

    public void tearDown() throws Exception {
        if(dataSource instanceof BasicDataSource) {
            ((BasicDataSource) dataSource).close();
        } else if(dataSource instanceof ManageableDataSource) {
            ((ManageableDataSource) dataSource).close();
        }
    }
    
    /**
     * Runs an sql string aginst the database.
     *
     * @param input The sql.
     */
    protected void run(final String input) throws Exception {
        run(new ByteArrayInputStream(input.getBytes()));
    }
    
    /**
     * Executes {@link #run(String)} ignoring any exceptions.
    */
    protected void runSafe( final String input ) { 
        try {
            run( input );
        }
        catch( Exception ignore ) {
            ignore.printStackTrace();
        }
    }

    /**
     * Runs an sql script against the database.
     *
     * @param script Input stream to the sql script to run.
     */
    protected void run(final InputStream script) throws Exception {
        //load the script
        BufferedReader reader = new BufferedReader(new InputStreamReader(script));

        //connect
        Connection conn = getDataSource().getConnection();
        
        try {
            Statement st = conn.createStatement();

            try {
                String line = null;

                while ((line = reader.readLine()) != null) {
                    LOGGER.fine(line);
                    st.execute(line);
                }

                reader.close();
            } finally {
                st.close();
            }
        } finally {
            conn.close();
        }
    }

    /**
     * This method is used whenever referencing the name
     * of a feature type / table name.
     * <p>
     * Subclasses should override this is in case where databases
     * can not respect case properly and need to force either 
     * upper or lower case. 
     * </p>
     *
     */
    protected String typeName( final String raw ) {
        return raw;
    }
    
    /**
     * This method is used whenever referencing the name
     * of an attribute / column name.
     * <p>
     * Subclasses should override this is in case where databases
     * can not respect case properly and need to force either 
     * upper or lower case. 
     * </p>
     */
    protected String attributeName( final String raw ) {
        return raw;
    }
    
    /**
     * Creates a data source by reading properties from a file called 'db.properties', 
     * located paralell to the test setup instance.
     */
    private DataSource createDataSource() throws IOException {
        Properties db = new Properties();
        fillConnectionProperties(db);

        BasicDataSource dataSource = new BasicDataSource();
        
        dataSource.setDriverClassName(db.getProperty( "driver") );
        dataSource.setUrl( db.getProperty( "url") );
        
        if ( db.containsKey( "username") ) {
            dataSource.setUsername(db.getProperty("username"));    
        }
        if ( db.containsKey( "password") ) {
            dataSource.setPassword(db.getProperty("password"));
        }
        
        dataSource.setPoolPreparedStatements(true);
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(4);
        // if we cannot get a connection within 5 seconds give up
        dataSource.setMaxWait(5000);
        
        initializeDataSource( dataSource, db );
        
        // return a closeable data source (DisposableDataSource interface)
        // so that the connection pool will be tore down on datastore dispose
        return new DBCPDataSource(dataSource);
    }

    /**
     * Fills in the connection properties. Default implementation uses a db.properties
     * file to be located in the same package as the test class.
     * @param db
     * @throws IOException
     */
    protected void fillConnectionProperties(final Properties db) throws IOException {
        db.load( getClass().getResourceAsStream( "db.properties") );
    }
    
    protected void initializeDataSource( final BasicDataSource ds, final Properties db ) {
        
    }

    protected abstract JDBCDataStoreFactory createDataStoreFactory();
    
    protected final SQLDialect createSQLDialect(final JDBCDataStore dataStore) {
        return null;
    }
}
