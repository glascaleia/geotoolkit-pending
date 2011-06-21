/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009-2010, Geomatys
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
package org.geotoolkit.wms;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.client.AbstractServer;
import org.geotoolkit.security.ClientSecurity;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wms.v111.GetCapabilities111;
import org.geotoolkit.wms.v111.GetFeatureInfo111;
import org.geotoolkit.wms.v111.GetLegend111;
import org.geotoolkit.wms.v111.GetMap111;
import org.geotoolkit.wms.v130.GetCapabilities130;
import org.geotoolkit.wms.v130.GetFeatureInfo130;
import org.geotoolkit.wms.v130.GetLegend130;
import org.geotoolkit.wms.v130.GetMap130;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WMSBindingUtilities;
import org.geotoolkit.wms.xml.WMSVersion;


/**
 * Generates WMS requests objects on a WMS server.
 *
 * @author Olivier Terral (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @module pending
 */
public class WebMapServer extends AbstractServer{

    private static final Logger LOGGER = Logging.getLogger(WebMapServer.class);

    private final WMSVersion version;
    private AbstractWMSCapabilities capabilities;

    /**
     * The request header map for this server
     * that contains a set of key-value for HTTP header fields (user-agent, referer, accept-language...)
     */
    private final Map<String,String> requestHeaderMap = new HashMap<String, String>();

    /**
     * Builds a web map server with the given server url and version.
     *
     * @param serverURL The server base url.
     * @param version A string representation of the service version.
     * @throws IllegalArgumentException if the version specified is not applyable.
     */
    public WebMapServer(final URL serverURL, final String version) {
        this(serverURL, WMSVersion.getVersion(version));
    }

    /**
     * Builds a web map server with the given server url and version.
     *
     * @param serverURL The server base url.
     * @param version The service version.
     */
    public WebMapServer(final URL serverURL, final WMSVersion version) {
        this(serverURL, version, null);
    }
    
    /**
     * Builds a web map server with the given server url and version.
     *
     * @param serverURL The server base url.
     * @param security The server security.
     * @param version The service version.
     */
    public WebMapServer(final URL serverURL, final ClientSecurity security,final WMSVersion version) {
        this(serverURL, security, version, null);
    }

    /**
     * Builds a web map server with the given server url, version and getCapabilities response.
     *
     * @param serverURL The server base url.
     * @param version A string representation of the service version.
     * @param capabilities A getCapabilities response.
     * @throws IllegalArgumentException if the version specified is not applyable.
     */
    public WebMapServer(final URL serverURL, final String version, final AbstractWMSCapabilities capabilities) {
        this(serverURL, WMSVersion.getVersion(version), capabilities);
    }

    /**
     * Builds a web map server with the given server url, version and getCapabilities response.
     *
     * @param serverURL The server base url.
     * @param version A string representation of the service version.
     * @param capabilities A getCapabilities response.
     */
    public WebMapServer(final URL serverURL, final WMSVersion version, 
            final AbstractWMSCapabilities capabilities) {
        this(serverURL,null,version,capabilities);
    }
    
    /**
     * Builds a web map server with the given server url, version and getCapabilities response.
     *
     * @param serverURL The server base url.
     * @param security The server security.
     * @param version A string representation of the service version.
     * @param capabilities A getCapabilities response.
     */
    public WebMapServer(final URL serverURL, final ClientSecurity security, 
            final WMSVersion version, final AbstractWMSCapabilities capabilities) {
        super(serverURL,security);
        this.version = version;
        this.capabilities = capabilities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getURI(){
        try {
            return serverURL.toURI();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public URL getURL() {
        return serverURL;
    }

    /**
     * Returns the {@linkplain AbstractWMSCapabilities capabilities} response for this
     * request.
     */
    public AbstractWMSCapabilities getCapabilities() {

        if (capabilities != null) {
            return capabilities;
        }
        //Thread to prevent infinite request on a server
        final Thread thread = new Thread() {
            @Override
            public void run() {
                final GetCapabilitiesRequest getCaps = createGetCapabilities();

                //Filling the request header map from the map of the layer's server
                final Map<String, String> headerMap = getRequestHeaderMap();
                getCaps.getHeaderMap().putAll(headerMap);

                try {
                    System.out.println(getCaps.getURL());
                    capabilities = WMSBindingUtilities.unmarshall(getCaps.getResponseStream(), version);
                } catch (Exception ex) {
                    capabilities = null;
                    try {
                        LOGGER.log(Level.WARNING, "Wrong URL, the server doesn't answer : " +
                                createGetCapabilities().getURL().toString(), ex);
                    } catch (MalformedURLException ex1) {
                        LOGGER.log(Level.WARNING, "Malformed URL, the server doesn't answer. ", ex1);
                    }
                }
            }
        };
        thread.start();
        final long start = System.currentTimeMillis();
        try {
            thread.join(10000);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING, "The thread to obtain GetCapabilities doesn't answer.", ex);
        }
        if ((System.currentTimeMillis() - start) > 10000) {
            LOGGER.log(Level.WARNING, "TimeOut error, the server takes too much time to answer. ");
        }

        return capabilities;
    }

    /**
     * Returns the request version.
     */
    public WMSVersion getVersion() {
        return version;
    }

    /**
     * Returns the request object, in the version chosen.
     *
     * @throws IllegalArgumentException if the version requested is not supported.
     */
    public GetMapRequest createGetMap() {
        switch (version) {
            case v111:
                return new GetMap111(serverURL.toString(),securityManager);
            case v130:
                return new GetMap130(serverURL.toString(),securityManager);
            default:
                throw new IllegalArgumentException("Version was not defined");
        }
    }

    /**
     * Returns the request object, in the version chosen.
     *
     * @throws IllegalArgumentException if the version requested is not supported.
     */
    public GetCapabilitiesRequest createGetCapabilities() {
        switch (version) {
            case v111:
                return new GetCapabilities111(serverURL.toString(),securityManager);
            case v130:
                return new GetCapabilities130(serverURL.toString(),securityManager);
            default:
                throw new IllegalArgumentException("Version was not defined");
        }
    }

    /**
     * Returns the request object, in the version chosen.
     *
     * @throws IllegalArgumentException if the version requested is not supported.
     */
    public GetLegendRequest createGetLegend(){
        switch (version) {
            case v111:
                return new GetLegend111(serverURL.toString(),securityManager);
            case v130:
                return new GetLegend130(serverURL.toString(),securityManager);
            default:
                throw new IllegalArgumentException("Version was not defined");
        }
    }

    /**
     * Returns the request object, in the version chosen.
     *
     * @throws IllegalArgumentException if the version requested is not supported.
     */
    public GetFeatureInfoRequest createGetFeatureInfo() {
        switch (version) {
            case v111:
                return new GetFeatureInfo111(serverURL.toString(),securityManager);
            case v130:
                return new GetFeatureInfo130(serverURL.toString(),securityManager);
            default:
                throw new IllegalArgumentException("Version was not defined");
        }
    }

    /**
     * Returns the request header map for this server.
     * @return
     */
    public Map<String,String> getRequestHeaderMap() {
        return requestHeaderMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WebMapServer[");
        sb.append("serverUrl: ").append(serverURL).append(", ")
          .append("version: ").append(version).append("]");
        return sb.toString();
    }
}
