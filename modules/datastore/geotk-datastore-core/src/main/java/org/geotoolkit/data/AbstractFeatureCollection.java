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

package org.geotoolkit.data;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.geotoolkit.data.query.QueryUtilities;
import org.geotoolkit.data.query.Selector;
import org.geotoolkit.data.query.Source;
import org.geotoolkit.data.query.TextStatement;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.geometry.Envelope;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public abstract class AbstractFeatureCollection<F extends Feature> extends AbstractCollection<F>
        implements FeatureCollection<F>, StorageListener{

    private final Set<StorageListener> listeners = new HashSet<StorageListener>();
    private final StorageListener.Weak weakListener = new Weak(this);

    protected String id;
    protected final Source source;

    public AbstractFeatureCollection(final String id, final Source source){

        if(id == null){
            throw new NullPointerException("Feature collection ID must not be null.");
        }
        if(source == null){
            throw new NullPointerException("Feature collection source must not be null.");
        }

        this.id = id;
        this.source = source;

        final Collection<Session> sessions = QueryUtilities.getSessions(source, null);
        for (Session s : sessions) {
            weakListener.registerSource(s);
        }

    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getID() {
        return id;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Session getSession() {
        if(source instanceof Selector){
            return ((Selector)source).getSession();
        }else if(source instanceof TextStatement){
            return ((TextStatement)source).getSession();
        }else{
            return null;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Source getSource() {
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isWritable() throws DataStoreRuntimeException {
        try {
            return QueryUtilities.isWritable(source);
        } catch (DataStoreException ex) {
            throw new DataStoreRuntimeException(ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final FeatureIterator<F> iterator(){
        return iterator(null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Envelope getEnvelope() throws DataStoreException{
        try{
            return DataUtilities.calculateEnvelope(iterator());
        }catch(DataStoreRuntimeException ex){
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int size() throws DataStoreRuntimeException{
        return (int) DataUtilities.calculateCount(iterator());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void update(final Filter filter, final AttributeDescriptor desc, final Object value) throws DataStoreException {
        update(filter, Collections.singletonMap(desc, value));
    }

    ////////////////////////////////////////////////////////////////////////////
    // listeners methods ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Forward event to listeners by changing source.
     */
    @Override
    public void structureChanged(StorageManagementEvent event){
        final FeatureType currentType = getFeatureType();

        //forward events only if the collection is typed and match the type name
        if(currentType != null && currentType.getName().equals(event.getFeatureTypeName())){
            event = StorageManagementEvent.resetSource(this, event);
            final StorageListener[] lst;
            synchronized (listeners) {
                lst = listeners.toArray(new StorageListener[listeners.size()]);
            }
            for (final StorageListener listener : lst) {
                listener.structureChanged(event);
            }
        }
    }

    /**
     * Forward event to listeners by changing source.
     */
    @Override
    public void contentChanged(final StorageContentEvent event){
        final FeatureType currentType = getFeatureType();

        //forward events only if the collection is typed and match the type name
        if(currentType != null && currentType.getName().equals(event.getFeatureTypeName())){
            sendEvent(StorageContentEvent.resetSource(this, event));
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void addStorageListener(final StorageListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeStorageListener(final StorageListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Fires a features add event.
     *
     * @param name of the schema where features where added.
     * @param ids modified feature ids.
     */
    protected void fireFeaturesAdded(final Name name, final Id ids){
        sendEvent(StorageContentEvent.createAddEvent(this, name,ids));
    }

    /**
     * Fires a features update event.
     *
     * @param name of the schema where features where updated.
     * @param ids modified feature ids.
     */
    protected void fireFeaturesUpdated(final Name name, final Id ids){
        sendEvent(StorageContentEvent.createUpdateEvent(this, name, ids));
    }

    /**
     * Fires a features delete event.
     *
     * @param name of the schema where features where deleted
     * @param ids modified feature ids.
     */
    protected void fireFeaturesDeleted(final Name name, final Id ids){
        sendEvent(StorageContentEvent.createDeleteEvent(this, name, ids));
    }

    /**
     * Forward a features event to all listeners.
     * @param event , event to send to listeners.
     */
    protected void sendEvent(final StorageContentEvent event) {
        final StorageListener[] lst;
        synchronized (listeners) {
            lst = listeners.toArray(new StorageListener[listeners.size()]);
        }
        for (final StorageListener listener : lst) {
            listener.contentChanged(event);
        }
    }

}
