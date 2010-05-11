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
package org.geotoolkit.sos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.client.AbstractRequest;
import org.geotoolkit.sos.xml.v100.EventTime;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest.Location;
import org.geotoolkit.xml.MarshallerPool;


/**
 * Abstract implementation of {@link GetFeatureOfInterestRequest}, which defines the
 * parameters for a GetFeatureOfInterest request.
 *
 * @author Cédric Briançon (Geomatys)
 * @module pending
 */
public abstract class AbstractGetFeatureOfInterest extends AbstractRequest implements GetFeatureOfInterestRequest {
    /**
     * The version to use for this webservice request.
     */
    protected final String version;

    private String featureOfInterestId = null;
    private EventTime eventTime = null;
    private GetFeatureOfInterest.Location location = null;

    /**
     * Defines the server url and the service version for this kind of request.
     *
     * @param serverURL The server url.
     * @param version The version of the request.
     */
    protected AbstractGetFeatureOfInterest(final String serverURL, final String version) {
        super(serverURL);
        this.version = version;
    }

    @Override
    public EventTime getEventTime() {
        return eventTime;
    }

    @Override
    public String getFeatureOfInterestId() {
        return featureOfInterestId;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setEventTime(EventTime eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public void setFeatureOfInterestId(String featureOfInterestId) {
        this.featureOfInterestId = featureOfInterestId;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public URL getURL() throws MalformedURLException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResponseStream() throws IOException {
        final URL url = new URL(serverURL);
        final URLConnection conec = url.openConnection();

        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/xml");

        final OutputStream stream = conec.getOutputStream();

        MarshallerPool pool = null;
        Marshaller marsh = null;
        try {
            pool = new MarshallerPool("org.geotoolkit.sos.xml.v100:" +
                                      "org.geotoolkit.gml.xml.v311:" +
                                      "org.geotoolkit.swe.xml.v100:" +
                                      "org.geotoolkit.swe.xml.v101:" +
                                      "org.geotoolkit.observation.xml.v100:" +
                                      "org.geotoolkit.sampling.xml.v100:" +
                                      "org.geotoolkit.sml.xml.v100:" +
                                      "org.geotoolkit.sml.xml.v101");
            marsh = pool.acquireMarshaller();
            final GetFeatureOfInterest observXml;
            if (featureOfInterestId != null) {
                observXml = new GetFeatureOfInterest(version, "SOS", featureOfInterestId);
            } else if (location != null) {
                observXml = new GetFeatureOfInterest(version, "SOS", location);
            } else {
                throw new IllegalArgumentException("Either location or featureOfInterestId should have a value!");
            }
            marsh.marshal(observXml, stream);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            if (pool != null && marsh != null) {
                pool.release(marsh);
            }
        }
        stream.close();
        return conec.getInputStream();
    }

}
