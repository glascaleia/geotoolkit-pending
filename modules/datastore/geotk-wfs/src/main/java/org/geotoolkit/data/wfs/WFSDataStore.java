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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.apache.bcel.generic.CHECKCAST;

import org.geotoolkit.data.AbstractDataStore;
import org.geotoolkit.data.DataStoreException;
import org.geotoolkit.data.DefaultFeatureCollection;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.memory.GenericEmptyFeatureIterator;
import org.geotoolkit.data.memory.GenericWrapFeatureIterator;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.AttributeTypeBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.ows.xml.v100.WGS84BoundingBoxType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.wfs.xml.v110.FeatureTypeListType;
import org.geotoolkit.wfs.xml.v110.FeatureTypeType;
import org.geotoolkit.wfs.xml.v110.WFSCapabilitiesType;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * WFS Datastore, This implementation is read only.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class WFSDataStore extends AbstractDataStore{

    private static final AtomicLong NS_INC = new AtomicLong();

    private final WebFeatureServer server;
    private final WFSCapabilitiesType capabilities;
    private final List<Name> typeNames = new ArrayList<Name>();
    private final Map<Name,SimpleFeatureType> types = new HashMap<Name,SimpleFeatureType>();
    private final Map<Name,Envelope> bounds = new HashMap<Name, Envelope>();
    private final Map<String,String> prefixes = new HashMap<String, String>();

    private Request lastRequest = null;
    private SoftReference<FeatureCollection<SimpleFeature>> lastCollection = null;


    public WFSDataStore(URI serverURI) throws MalformedURLException{

        this.server = new WebFeatureServer(serverURI.toURL(), "1.1.0");
        this.capabilities = server.getCapabilities();

        final FeatureTypeListType lst = capabilities.getFeatureTypeList();
        for(final FeatureTypeType ftt : lst.getFeatureType()){

            //extract the name -------------------------------------------------
            QName typeName = ftt.getName();
            String prefix = typeName.getPrefix();
            String uri = typeName.getNamespaceURI();
            String localpart = typeName.getLocalPart();
            if(prefix == null || prefix.isEmpty()){
                prefix = "geotk" + NS_INC.incrementAndGet();
            }

            prefixes.put(uri, prefix);
            final Name name = new DefaultName(uri, localpart);
            typeName = new QName(uri, localpart, prefix);

            //extract the feature type -----------------------------------------
            CoordinateReferenceSystem crs;
            SimpleFeatureType sft;
            try {
                crs = CRS.decode(ftt.getDefaultSRS(),true);
                sft = requestType(typeName);                
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, null, ex);
                continue;
            } catch (FactoryException ex) {
                getLogger().log(Level.SEVERE, null, ex);
                continue;
            }

            final SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
            sftb.setName(sft.getName());

            for(AttributeDescriptor desc : sft.getAttributeDescriptors()){
                if(desc instanceof GeometryDescriptor){
                    final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
                    final AttributeTypeBuilder atb = new AttributeTypeBuilder();
                    atb.copy(desc.getType());
                    atb.setCRS(crs);
                    adb.copy(desc);
                    adb.setType(atb.buildGeometryType());
                    sftb.add(adb.buildDescriptor());
                }else{
                    sftb.add(desc);
                }
            }
            sftb.setDefaultGeometry(sft.getGeometryDescriptor().getLocalName());
            sft = sftb.buildFeatureType();

            CoordinateReferenceSystem val = sft.getGeometryDescriptor().getCoordinateReferenceSystem();
            if(val == null){
                throw new IllegalArgumentException("CRS should not be null");
            }

            types.put(name, sft);
            typeNames.add(name);

            //extract the bounds -----------------------------------------------
            final WGS84BoundingBoxType bbox = ftt.getWGS84BoundingBox().get(0);
            try {
                crs = CRS.decode(bbox.getCrs(),true);
                final GeneralEnvelope env = new GeneralEnvelope(crs);
                final BigInteger dims = bbox.getDimensions();
                final List<Double> upper = bbox.getUpperCorner();
                final List<Double> lower = bbox.getLowerCorner();

                for(int i=0,n=dims.intValue();i<n;i++){
                    env.setRange(i, lower.get(i), upper.get(i));
                }
                bounds.put(name, env);
            } catch (FactoryException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }

        }

    }

    @Override
    public boolean isWritable(Name typeName) throws DataStoreException {
        this.typeCheck(typeName);
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getNames() throws DataStoreException {
        return new HashSet<Name>(types.keySet());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType getFeatureType(Name typeName) throws DataStoreException {
        FeatureType ft = types.get(typeName);

        if(ft == null){
            throw new DataStoreException("Type : "+ typeName + " doesn't exist in this datastore.");
        }

        return ft;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Envelope getEnvelope(Query query) throws DataStoreException {
        final Name typeName = query.getTypeName();
        typeCheck(typeName);
        return bounds.get(typeName);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object getQueryCapabilities() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    ////////////////////////////////////////////////////////////////////////////
    // schema manipulation /////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc }
     */
    @Override
    public void createSchema(Name typeName, FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Schema creation not supported.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateSchema(Name typeName, FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Schema update not supported.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteSchema(Name typeName) throws DataStoreException {
        throw new DataStoreException("Schema deletion not supported.");
    }

    ////////////////////////////////////////////////////////////////////////////
    // read & write ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureReader<SimpleFeatureType,SimpleFeature> getFeatureReader(Query query) throws DataStoreException {
        final Name name = query.getTypeName();
        //will raise an error if typename in unknowned
        final SimpleFeatureType sft = (SimpleFeatureType) getFeatureType(name);

        final QName q = new QName(name.getNamespaceURI(), name.getLocalPart(), prefixes.get(name.getNamespaceURI()));
        final FeatureCollection<SimpleFeature> collection;
        try {
            collection = requestFeature(q, query);
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }

        if(collection == null){
            return GenericEmptyFeatureIterator.createReader(sft);
        }else{
            return GenericWrapFeatureIterator.wrapToReader(collection.iterator(), sft);
        }
    }

    /**
     * Writer that fall back on add,remove, update methods.
     */
    @Override
    public FeatureWriter getFeatureWriter(Name typeName, Filter filter) throws DataStoreException {
        return handleWriter(typeName, filter);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<FeatureId> addFeatures(Name groupName, Collection<? extends Feature> newFeatures) throws DataStoreException {
        final TransactionRequest request = server.createTransaction();
        final Insert insert = server.createInsertElement();
        insert.setInputFormat("text/xml; subtype=gml/3.1.1");

        final FeatureCollection col;
        if(newFeatures instanceof FeatureCollection){
            col = (FeatureCollection) newFeatures;
        }else{
            col = new DefaultFeatureCollection("", null, Feature.class);
            col.addAll(newFeatures);
        }
        insert.setFeatures(col);

        request.elements().add(insert);

        try {
            final InputStream response = request.getResponse();
            response.close();
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }

        //todo read response
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateFeatures(Name groupName, Filter filter, Map<? extends PropertyDescriptor, ? extends Object> values) throws DataStoreException {
        final TransactionRequest request = server.createTransaction();
        final Update update = server.createUpdateElement();
        update.setInputFormat("text/xml; subtype=gml/3.1.1");
        
        update.setFilter(filter);
        update.setTypeName(groupName);
        for(Map.Entry<? extends PropertyDescriptor,? extends Object> entry : values.entrySet()){
            update.updates().put(entry.getKey().getName(), entry.getValue());
        }

        request.elements().add(update);

        try {
            final InputStream response = request.getResponse();
            response.close();
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeFeatures(Name groupName, Filter filter) throws DataStoreException {
        final TransactionRequest request = server.createTransaction();
        final Delete delete = server.createDeleteElement();
        
        delete.setTypeName(groupName);
        delete.setFilter(filter);

        request.elements().add(delete);

        try {
            final InputStream response = request.getResponse();
            response.close();
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // read & write ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private SimpleFeatureType requestType(QName typeName) throws IOException{
        final DescribeFeatureTypeRequest request = server.createDescribeFeatureType();
        request.setTypeNames(Collections.singletonList(typeName));

        try {
            final JAXBFeatureTypeReader reader = new JAXBFeatureTypeReader();
            getLogger().log(Level.INFO, "[WFS Client] request type : " + request.getURL());
            final List<FeatureType> types = reader.read(request.getURL().openStream());
            return (SimpleFeatureType) types.get(0);

        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }

    }

    private FeatureCollection<SimpleFeature> requestFeature(QName typeName, Query query) throws IOException {
        final Name name = new DefaultName(typeName);
        final SimpleFeatureType sft = types.get(name);

        final GetFeatureRequest request = server.createGetFeature();
        request.setTypeName(typeName);

        if(query != null){

            final Filter filter = query.getFilter();
            if(filter == null){
                request.setFilter(Filter.INCLUDE);
            }else{
                request.setFilter(filter);
            }

            final Integer max = query.getMaxFeatures();
            if(max != null){
                request.setMaxFeatures(max);
            }

            request.setPropertyNames(query.getPropertyNames());
        }

        if(request.equals(lastRequest)){
            FeatureCollection<SimpleFeature> col = lastCollection.get();
            if(col != null){
                return col;
            }
        }

        XmlFeatureReader reader = null;
        try {
            reader = new JAXPStreamFeatureReader(sft);
            final URL url = request.getURL();
            getLogger().log(Level.INFO, "[WFS Client] request feature : " + url);
            final Object result = reader.read(url.openStream());

            lastRequest = request;

            if(result instanceof SimpleFeature){
                final SimpleFeature sf = (SimpleFeature) result;
                final FeatureCollection<SimpleFeature> col = new DefaultFeatureCollection<SimpleFeature>("id", sft, SimpleFeature.class);
                col.add(sf);

                lastCollection = new SoftReference<FeatureCollection<SimpleFeature>>(col);

                return col;
            }else if(result instanceof FeatureCollection){
                final FeatureCollection<SimpleFeature> col = (FeatureCollection<SimpleFeature>) result;
                lastCollection = new SoftReference<FeatureCollection<SimpleFeature>>(col);
                return col;
            }else{
                final FeatureCollection<SimpleFeature> col = new DefaultFeatureCollection<SimpleFeature>("", sft, SimpleFeature.class);
                lastCollection = new SoftReference<FeatureCollection<SimpleFeature>>(col);
                return col;
//                throw new IOException("unexpected type : " + result);
            }

        } catch (JAXBException ex) {
            throw new IOException(ex);
        }finally{
            if(reader != null){
                reader.dispose();
            }
        }

    }



}
