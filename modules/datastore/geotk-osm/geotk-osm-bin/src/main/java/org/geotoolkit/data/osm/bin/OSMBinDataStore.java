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

package org.geotoolkit.data.osm.bin;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotoolkit.data.AbstractDataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.query.DefaultQueryCapabilities;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryCapabilities;
import org.geotoolkit.factory.Hints;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

/**
 * Open Street Map datastore for binary files (*.obm)
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class OSMBinDataStore extends AbstractDataStore{

    private final QueryCapabilities capabilities = new DefaultQueryCapabilities(false);

    public OSMBinDataStore(final URL filePath){
        super(null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getNames() throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType getFeatureType(final Name typeName) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public QueryCapabilities getQueryCapabilities() {
        return capabilities;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isWritable(final Name typeName) throws DataStoreException {
        //will raise an error if not exist.
        typeCheck(typeName);
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureReader getFeatureReader(final Query query) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Schema manipulation /////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc }
     * Not supported yet.
     */
    @Override
    public void createSchema(final Name typeName, final FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     * Not supported yet.
     */
    @Override
    public void updateSchema(final Name typeName, final FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     * Not supported yet.
     */
    @Override
    public void deleteSchema(final Name typeName) throws DataStoreException {
        throw new DataStoreException("Not supported yet.");
    }

    ////////////////////////////////////////////////////////////////////////////
    // modification manipulation ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc }
     * Not supported yet.
     */
    @Override
    public List<FeatureId> addFeatures(final Name groupName, final Collection<? extends Feature> newFeatures, 
            final Hints hints) throws DataStoreException {
        return handleAddWithFeatureWriter(groupName, newFeatures, hints);
    }

    /**
     * {@inheritDoc }
     * Not supported yet.
     */
    @Override
    public void updateFeatures(final Name groupName, final Filter filter, final Map<? extends PropertyDescriptor, ? extends Object> values) throws DataStoreException {
        handleUpdateWithFeatureWriter(groupName, filter, values);
    }

    /**
     * {@inheritDoc }
     * Not supported yet.
     */
    @Override
    public void removeFeatures(final Name groupName, final Filter filter) throws DataStoreException {
        handleRemoveWithFeatureWriter(groupName, filter);
    }

    /**
     * {@inheritDoc }
     * Not supported yet.
     */
    @Override
    public FeatureWriter getFeatureWriter(final Name typeName, final Filter filter, 
            final Hints hints) throws DataStoreException {
        if(isWritable(typeName)){
            throw new DataStoreException("Writer not handled yet.");
        }else{
            throw new DataStoreException("Writing not supported.");
        }
    }

}
