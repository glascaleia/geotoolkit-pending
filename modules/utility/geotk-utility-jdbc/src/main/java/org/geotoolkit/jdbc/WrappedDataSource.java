/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Geomatys
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

/**
 * Wrap a ConnectionPoolDataSource in a DataSource interface.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class WrappedDataSource implements DataSource{

    private final ConnectionPoolDataSource pool;

    public WrappedDataSource(ConnectionPoolDataSource pool){
        this.pool = pool;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return pool.getPooledConnection().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return pool.getPooledConnection(username,password).getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return pool.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        pool.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        pool.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return pool.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if(isWrapperFor(iface)){
            return (T) pool;
        }else{
            throw new SQLException("WrappedDataSource does wrap object of type : "+iface);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(pool);
    }

}
