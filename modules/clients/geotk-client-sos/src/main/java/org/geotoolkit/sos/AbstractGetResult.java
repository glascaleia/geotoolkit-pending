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
import java.util.Arrays;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.sos.xml.v100.EventTime;
import org.geotoolkit.sos.xml.v100.GetResult;


/**
 * Abstract implementation of {@link GetObservationByIdRequest}, which defines the
 * parameters for a GetObservation request.
 *
 * @author Cédric Briançon (Geomatys)
 * @module pending
 */
public abstract class AbstractGetResult extends AbstractSOSRequest implements GetResultRequest {

    /**
     * The version to use for this webservice request.
     */
    protected final String version;

    private String observationTemplateId = null;
    private EventTime[] eventTimes = null;

    /**
     * Defines the server url and the service version for this kind of request.
     *
     * @param server The server url.
     * @param version The version of the request.
     */
    protected AbstractGetResult(final SensorObservationServiceServer server, final String version) {
        super(server);
        this.version = version;
    }

    @Override
    public EventTime[] getEventTimes() {
        return eventTimes;
    }

    @Override
    public String getObservationTemplateId() {
        return observationTemplateId;
    }

    @Override
    public void setEventTimes(final EventTime[] eventTimes) {
        this.eventTimes = eventTimes;
    }

    @Override
    public void setObservationTemplateId(final String observationTemplateId) {
        this.observationTemplateId = observationTemplateId;
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
        if (observationTemplateId == null) {
            throw new IllegalArgumentException("The parameter \"observationTemplateId\" is not defined");
        }
        final URL url = new URL(serverURL);
        URLConnection conec = url.openConnection();
        conec = security.secure(conec);

        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/xml");

        OutputStream stream = conec.getOutputStream();
        stream = security.encrypt(stream);

        Marshaller marsh = null;
        try {
            marsh = POOL.acquireMarshaller();
            final GetResult observXml = new GetResult(observationTemplateId,
                    (eventTimes != null) ? Arrays.asList(eventTimes) : null, version);
            marsh.marshal(observXml, stream);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            if (POOL != null && marsh != null) {
                POOL.release(marsh);
            }
        }
        stream.close();
        return security.decrypt(conec.getInputStream());
    }

}
