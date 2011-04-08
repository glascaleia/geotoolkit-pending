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

package org.geotoolkit.data.wfs;

import java.net.MalformedURLException;

import java.net.URI;
import java.util.WeakHashMap;

import org.geotoolkit.data.AbstractDataStoreFactory;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;

import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Datastore factory for WFS client.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class WFSDataStoreFactory extends AbstractDataStoreFactory{

    /**
     * Mandatory - server uri
     */
    public static final ParameterDescriptor<URI> SERVER_URI =
            new DefaultParameterDescriptor<URI>("server uri","server uri",URI.class,null,true);
    /**
     * Optional -post request
     */
    public static final ParameterDescriptor<Boolean> POST_REQUEST =
            new DefaultParameterDescriptor<Boolean>("post request","post request",Boolean.class,false,false);

    public static final ParameterDescriptorGroup PARAMETERS_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("WFSParameters",
                SERVER_URI, POST_REQUEST);

    private static final WeakHashMap<URI,WFSDataStore> STORES = new WeakHashMap<URI, WFSDataStore>();

    /**
     * {@inheritDoc }
     */
    @Override
    public String getDescription() {
        return "OGC Web Feature Service datastore factory";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ParameterDescriptorGroup getParametersDescriptor() {
        return PARAMETERS_DESCRIPTOR;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized WFSDataStore createDataStore(final ParameterValueGroup params) throws DataStoreException {
        final URI serverURI       = (URI) params.parameter(SERVER_URI.getName().getCode()).getValue();
        final boolean postrequest = params.parameter(POST_REQUEST.getName().getCode()).booleanValue();

        WFSDataStore store = STORES.get(serverURI);

        if(store == null){
            try {
                store = new WFSDataStore(serverURI, postrequest);
            } catch (MalformedURLException ex) {
                throw new DataStoreException(ex);
            }
            STORES.put(serverURI, store);
        }

        return store;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public DataStore createNewDataStore(final ParameterValueGroup params) throws DataStoreException {
        throw new DataStoreException("Can not create any new WFS DataStore");
    }

}
