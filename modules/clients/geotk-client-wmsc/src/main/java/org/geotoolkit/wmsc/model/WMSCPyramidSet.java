/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.wmsc.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import org.geotoolkit.client.map.CachedPyramidSet;
import org.geotoolkit.coverage.GridMosaic;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.wms.GetMapRequest;
import org.geotoolkit.wms.xml.v111.Capability;
import org.geotoolkit.wms.xml.v111.VendorSpecificCapabilities;
import org.geotoolkit.wmsc.WebMapServerCached;
import org.geotoolkit.wmsc.xml.v111.TileSet;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class WMSCPyramidSet extends CachedPyramidSet{
    
    private final WebMapServerCached server;
    private final String layer;
    
    public WMSCPyramidSet(final WebMapServerCached server, final String layer) {
        this.server = server;
        this.layer = layer;
        
        //WMSC is a WMS 1.1.1
        final Capability capas = (Capability) server.getCapabilities().getCapability();        
        final VendorSpecificCapabilities vendor = capas.getVendorSpecificCapabilities();
        
        if(vendor == null){
            return;
        }
        
        final List<TileSet> sets = vendor.getTileSet();
        
        if(sets == null){
            return;
        }
        
        //find tileset definition for this layer
        for(final TileSet set : sets){
            for(String layerName : set.getLayers()){
                if(!layer.equals(layerName)){
                    continue;
                }
                            
                try {
                    final WMSCPyramid pyramid = new WMSCPyramid(this, set);
                    getPyramids().add(pyramid);
                } catch (NoSuchAuthorityCodeException ex) {
                    LOGGER.log(Level.INFO, ex.getMessage(),ex);
                } catch (FactoryException ex) {
                    LOGGER.log(Level.INFO, ex.getMessage(),ex);
                }
                
            }
        }        
    }

    public String getLayer() {
        return layer;
    }

    @Override
    protected InputStream download(GridMosaic mosaic, String mimeType, int col, int row) throws DataStoreException {
        final GetMapRequest request = server.createGetMap();
        request.setLayers(layer);
        request.setEnvelope(mosaic.getEnvelope(col, row));
        request.setDimension(mosaic.getTileSize());
        request.setFormat(((WMSCPyramid)mosaic.getPyramid()).getTileset().getFormat());
        try {
            return request.getResponseStream();
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }
    }
        
}
