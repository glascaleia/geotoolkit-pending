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
package org.geotoolkit.csw;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.geotoolkit.client.AbstractRequest;
import org.geotoolkit.util.logging.Logging;


/**
 * Abstract implementation of {@link TransactionRequest}, which defines the
 * parameters for a transaction request.
 *
 * @author Cédric Briançon (Geomatys)
 * @module pending
 */
public class AbstractHarvest extends AbstractRequest implements HarvestRequest {
    /**
     * Default logger for all GetRecords requests.
     */
    protected static final Logger LOGGER = Logging.getLogger(AbstractGetRecords.class);

    /**
     * The version to use for this webservice request.
     */
    protected final String version;

    private String namespace = null;
    private String source = null;
    private String resourceType = null;
    private String resourceFormat = null;
    private String responseHandler = null;
    private String harvestInterval = null;

    /**
     * Defines the server url and the service version for this kind of request.
     *
     * @param serverURL The server url.
     * @param version The version of the request.
     */
    protected AbstractHarvest(final String serverURL, final String version){
        super(serverURL);
        this.version = version;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getResourceType() {
        return resourceType;
    }

    @Override
    public void setResourceType(String resourceType) {
        this.resourceType =  resourceType;
    }

    @Override
    public String getResourceFormat() {
        return resourceFormat;
    }

    @Override
    public void setResourceFormat(String resourceFormat) {
        this.resourceFormat = resourceFormat;
    }

    @Override
    public String getResponseHandler() {
        return responseHandler;
    }

    @Override
    public void setResponseHandler(String responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public String getHarvestInterval() {
        return harvestInterval;
    }

    @Override
    public void setHarvestInterval(String harvestInterval) {
        this.harvestInterval = harvestInterval;
    }

    @Override
    public URL getURL() throws MalformedURLException {
        if (source == null) {
            throw new IllegalArgumentException("The parameter \"SOURCE\" is not defined");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("The parameter \"RESOURCETYPE\" is not defined");
        }

        requestParameters.put("SERVICE",      "CSW");
        requestParameters.put("REQUEST",      "Harvest");
        requestParameters.put("VERSION",      version);
        requestParameters.put("SOURCE",       source);
        requestParameters.put("RESOURCETYPE", resourceType);

        if (namespace != null) {
            requestParameters.put("NAMESPACE", namespace);
        }
        if (resourceFormat != null) {
            requestParameters.put("RESOURCEFORMAT", resourceFormat);
        }
        if (responseHandler != null) {
            requestParameters.put("RESPONSEHANDLER", responseHandler);
        }
        if (harvestInterval != null) {
            requestParameters.put("HARVESTINTERVAL", harvestInterval);
        }

        return super.getURL();
    }

    @Override
    public InputStream getSOAPResponse() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}