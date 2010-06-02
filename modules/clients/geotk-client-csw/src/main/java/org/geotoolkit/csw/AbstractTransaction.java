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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.client.AbstractRequest;
import org.geotoolkit.csw.xml.v202.DeleteType;
import org.geotoolkit.csw.xml.v202.InsertType;
import org.geotoolkit.csw.xml.v202.TransactionType;
import org.geotoolkit.csw.xml.v202.UpdateType;
import org.geotoolkit.ebrim.xml.EBRIMClassesContext;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;


/**
 * Abstract implementation of {@link TransactionRequest}, which defines the
 * parameters for a transaction request.
 *
 * @author Cédric Briançon (Geomatys)
 * @module pending
 */
public class AbstractTransaction extends AbstractRequest implements TransactionRequest {
    /**
     * Default logger for all GetRecords requests.
     */
    protected static final Logger LOGGER = Logging.getLogger(AbstractTransaction.class);

    private static final MarshallerPool POOL;
    static {
        MarshallerPool temp = null;
        try {
            temp = new MarshallerPool(EBRIMClassesContext.getAllClasses());
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        POOL = temp;
    }

    /**
     * The version to use for this webservice request.
     */
    protected final String version;

    private DeleteType delete = null;
    private InsertType insert = null;
    private UpdateType update = null;

    /**
     * Defines the server url and the service version for this kind of request.
     *
     * @param serverURL The server url.
     * @param version The version of the request.
     */
    protected AbstractTransaction(final String serverURL, final String version){
        super(serverURL);
        this.version = version;
    }

    @Override
    public DeleteType getDelete() {
        return delete;
    }

    @Override
    public void setDelete(DeleteType delete) {
        this.delete = delete;
    }

    @Override
    public InsertType getInsert() {
        return insert;
    }

    @Override
    public void setInsert(InsertType insert) {
        this.insert = insert;
    }

    @Override
    public UpdateType getUpdate() {
        return update;
    }

    @Override
    public void setUpdate(UpdateType update) {
        this.update = update;
    }

    /**
     * The transaction CSW requests do not support the KVP encondig, regarding the standards.
     * This method will always return an {@link UnsupportedOperationException}.
     * Do not call it.
     */
    @Override
    public URL getURL() throws MalformedURLException {
        throw new UnsupportedOperationException("Transaction requests do not support KVP encoding.");
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

        Marshaller marsh = null;
        try {
            marsh = POOL.acquireMarshaller();
            final TransactionType transactXml;
            if (delete != null) {
                transactXml = new TransactionType("CSW", version, delete);
            } else if (insert != null) {
                transactXml = new TransactionType("CSW", version, insert);
            } else if (update != null) {
                transactXml = new TransactionType("CSW", version, update);
            } else {
                throw new IllegalArgumentException("The specified requests is not valid. " +
                        "It does not contain any insert, delete or update request.");
            }
            marsh.marshal(transactXml, stream);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        } finally {
            if (POOL != null && marsh != null) {
                POOL.release(marsh);
            }
        }
        stream.close();
        return conec.getInputStream();
    }
}
