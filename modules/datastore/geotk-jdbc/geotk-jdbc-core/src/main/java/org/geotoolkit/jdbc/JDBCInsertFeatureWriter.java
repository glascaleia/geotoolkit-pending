/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.jdbc.fid.PrimaryKey;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

public class JDBCInsertFeatureWriter extends JDBCFeatureReader implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

    private final boolean batchInsert;
    private final Collection<SimpleFeature> toAdd;
    ResultSetFeature last;

    public JDBCInsertFeatureWriter(final String sql, final Connection cx, final JDBCDataStore store,
            final Name groupName, final SimpleFeatureType type, final PrimaryKey pkey, final Hints hints)
            throws SQLException, IOException, DataStoreException {
        super(sql, cx, store, groupName, type, pkey, hints);
        last = new ResultSetFeature( rs, cx );        
        batchInsert = Boolean.FALSE.equals(hints.get(HintsPending.UPDATE_ID_ON_INSERT));
        toAdd = (batchInsert) ? new ArrayList<SimpleFeature>() : null;
    }

    public JDBCInsertFeatureWriter(final PreparedStatement ps, final Connection cx, final JDBCDataStore store,
            final Name groupName, final SimpleFeatureType type, final PrimaryKey pkey, final Hints hints)
            throws SQLException, IOException, DataStoreException {
        super( ps, cx, store, groupName, type, pkey, hints );
        last = new ResultSetFeature( rs, ps.getConnection() );
        batchInsert = Boolean.FALSE.equals(hints.get(HintsPending.UPDATE_ID_ON_INSERT));
        toAdd = (batchInsert) ? new ArrayList<SimpleFeature>() : null;
    }

    public JDBCInsertFeatureWriter(final JDBCUpdateFeatureWriter other) {
        super(other);
        last = other.last;
        batchInsert = false;
        toAdd = null;
    }

    @Override
    public boolean hasNext() throws DataStoreRuntimeException {
        return false;
    }

    @Override
    public SimpleFeature next() throws DataStoreRuntimeException {
        //init, setting id to null explicity since the feature is yet to be
        // inserted
        last.init(null);
        return last;
    }

    @Override
    public void remove() throws DataStoreRuntimeException {
        //noop
    }

    @Override
    public void write() throws DataStoreRuntimeException {
        
        if(batchInsert){
            toAdd.add(FeatureUtilities.copy(last));
            if(toAdd.size() > 1000){
                try {
                    dataStore.insert(toAdd, featureType, st.getConnection());
                } catch (DataStoreException e) {
                    throw new DataStoreRuntimeException(e);
                } catch (SQLException e) {
                    throw new DataStoreRuntimeException(e);
                }
                toAdd.clear();
            }
        }else{
            try {
                dataStore.insert(last, featureType, st.getConnection());

                //the datastore sets as userData, grab it and update the fid
                String fid = (String) last.getUserData().get( "fid" );
                last.setID( fid );
            } catch (DataStoreException e) {
                throw new DataStoreRuntimeException(e);
            } catch (SQLException e) {
                throw new DataStoreRuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws DataStoreRuntimeException {

        if(batchInsert && !toAdd.isEmpty()){
            try {
                //do the insert
                dataStore.insert(toAdd, featureType, st.getConnection());

                //the datastore sets as userData, grab it and update the fid
                //String fid = (String) last.getUserData().get( "fid" );
                //last.setID( fid );
            } catch (DataStoreException e) {
                throw new DataStoreRuntimeException(e);
            } catch (SQLException e) {
                throw new DataStoreRuntimeException(e);
            }
            toAdd.clear();
        }
        
        super.close();
                
        if ( last != null ) {
            last.close();
            last = null;
        }
    }
}
