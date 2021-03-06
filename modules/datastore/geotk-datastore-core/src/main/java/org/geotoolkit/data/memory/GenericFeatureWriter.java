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

package org.geotoolkit.data.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.feature.AbstractFeature;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.util.converter.Classes;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;

/**
 * Basic support for a  FeatureWriter that redicts it's calls to
 * the more casual methods : addFeatures, removeFeatures and updateFeatures
 * of the datastore.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class GenericFeatureWriter<T extends FeatureType, F extends Feature> implements FeatureWriter<T,F> {

    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);

    protected final DataStore store;
    protected final Name typeName;
    protected final FeatureReader<T,F> reader;
    protected final T type;
    protected F currentFeature = null;
    protected F modified = null;
    private boolean remove = false;

    private GenericFeatureWriter(final DataStore store, final Name typeName, final Filter filter) throws DataStoreException {
        this.store = store;
        this.typeName = typeName;
        reader = store.getFeatureReader(QueryBuilder.filtered(typeName, filter));
        type = (T) store.getFeatureType(typeName);
    }


    @Override
    public T getFeatureType() throws DataStoreRuntimeException{
        return type;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public F next() throws DataStoreRuntimeException {
        remove = false;
        if(hasNext()){
            currentFeature = reader.next();
            modified = (F) FeatureUtilities.copy(currentFeature);
        }else{
            currentFeature = null;
            modified = (F) FeatureUtilities.defaultFeature(type, "");
        }

        return modified;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() throws DataStoreRuntimeException {
        reader.close();
        write();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hasNext() throws DataStoreRuntimeException {
        return reader.hasNext();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void remove() {
        remove = true;
        write();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void write() throws DataStoreRuntimeException {
        if(currentFeature != null){
            final Filter filter = FF.id(Collections.singleton(currentFeature.getIdentifier()));
            if(remove){
                //it's a remove operation
                try {
                    store.removeFeatures(typeName, filter);
                } catch (DataStoreException ex) {
                    throw new DataStoreRuntimeException(ex);
                }
            }else{
                //it's a modify operation
                final Map<PropertyDescriptor,Object> values = new HashMap<PropertyDescriptor, Object>();

                for(PropertyDescriptor desc : type.getDescriptors()){
                    final Object original = currentFeature.getProperty(desc.getName()).getValue();
                    final Object mod = modified.getProperty(desc.getName()).getValue();
                    //check if the values was modified
                    if(!safeEqual(original, mod)){
                        //value has changed
                        values.put(desc, mod);
                    }
                }

                if(!values.isEmpty()){
                    try {
                        store.updateFeatures(typeName, filter, values);
                    } catch (DataStoreException ex) {
                        throw new DataStoreRuntimeException(ex);
                    }
                }
            }

        }else{
            if(modified != null){
                //it's an add operation
                try {
                    final List<FeatureId> res = store.addFeatures(typeName, Collections.singleton(modified));
                    if(modified instanceof AbstractFeature){
                        ((AbstractFeature)modified).setId(res.get(0));
                    }
                } catch (DataStoreException ex) {
                    throw new DataStoreRuntimeException(ex);
                }
                modified = null;
            }
        }

        remove = false;
    }

    private boolean safeEqual(final Object o1, final Object o2){
        if(o1 == null && o2 == null){
            return true;
        }else if(o1 != null){
            return o1.equals(o2);
        }else{
            return o2.equals(o1);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(Classes.getShortClassName(this));
        sb.append('\n');
        String subIterator = "\u2514\u2500\u2500" + reader.toString(); //move text to the right
        subIterator = subIterator.replaceAll("\n", "\n\u00A0\u00A0\u00A0"); //move text to the right
        sb.append(subIterator);
        return sb.toString();
    }

    public static <T extends FeatureType, F extends Feature> FeatureWriter<T,F> wrap(
            final DataStore store, final Name typeName, final Filter filter) throws DataStoreException{
        return new GenericFeatureWriter<T, F>(store, typeName, filter);
    }

    public static <T extends FeatureType, F extends Feature> FeatureWriter<T,F> wrapAppend(
            final DataStore store, final Name typeName) throws DataStoreException{
        return wrap(store,typeName,Filter.EXCLUDE);
    }

}
