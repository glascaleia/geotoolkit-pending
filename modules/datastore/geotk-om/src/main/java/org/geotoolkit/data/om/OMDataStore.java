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

package org.geotoolkit.data.om;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.data.AbstractDataStore;
import org.geotoolkit.data.DataStoreException;
import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.query.DefaultQueryCapabilities;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryCapabilities;
import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.AttributeTypeBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.simple.DefaultSimpleFeatureType;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;
import org.geotoolkit.feature.type.DefaultGeometryDescriptor;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.referencing.CRS;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @module pending
 */
public class OMDataStore extends AbstractDataStore {

    private Logger LOGGER = Logger.getLogger("org.geotoolkit.data.om");
    
    private final static Name SAMPLINGPOINT = new DefaultName("http://www.opengis.net/sampling/1.0", "SamplingPoint");

    private final Map<Name, SimpleFeatureType> types = new HashMap<Name,SimpleFeatureType>();

    private final Connection connection;

    private PreparedStatement getAllSamplingPoint;

    private PreparedStatement writeSamplingPoint;

    private PreparedStatement getLastIndentifier;

    protected static final Name DESC     = new DefaultName("http://www.opengis.net/gml", "description");
    protected static final Name NAME     = new DefaultName("http://www.opengis.net/gml", "name");
    protected static final Name SAMPLED  = new DefaultName("http://www.opengis.net/sampling/1.0", "sampledFeature");
    protected static final Name POSITION = new DefaultName("http://www.opengis.net/sampling/1.0", "position");

    private static final GeometryFactory GF = new GeometryFactory();

    private final QueryCapabilities capabilities = new DefaultQueryCapabilities(false);
    private CoordinateReferenceSystem defaultCRS;

    public OMDataStore(Connection connection) {
        super();
        this.connection = connection;
        try {
            defaultCRS = CRS.decode("EPSG:27582");
            
        } catch (NoSuchAuthorityCodeException ex) {
            getLogger().log(Level.WARNING, null, ex);
        } catch (FactoryException ex) {
            getLogger().log(Level.WARNING, null, ex);
        }
        initTypes();
        initStatement();
    }

    private void initStatement() {
        try {
            getAllSamplingPoint = connection.prepareStatement("SELECT * FROM \"observation\".\"sampling_points\"");
            writeSamplingPoint  = connection.prepareStatement("INSERT INTO \"observation\".\"sampling_points\" VALUES(?,?,?,?,?,?,?,?,?)");
            getLastIndentifier  = connection.prepareStatement("SELECT COUNT(*) FROM \"observation\".\"sampling_points\"");
        } catch (SQLException ex) {
           getLogger().severe("SQL Exception while requesting the O&M database:" + ex.getMessage());
        }
    }

    private void initTypes() {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        final AttributeDescriptorBuilder attributeDescBuilder   = new AttributeDescriptorBuilder();
        final AttributeTypeBuilder attributeTypeBuilder   = new AttributeTypeBuilder();

        featureTypeBuilder.setName(SAMPLINGPOINT);
        
        // gml:description
        attributeTypeBuilder.reset();
        attributeTypeBuilder.setBinding(String.class);
        attributeDescBuilder.reset();
        attributeDescBuilder.setName(DESC);
        attributeDescBuilder.setMaxOccurs(1);
        attributeDescBuilder.setMinOccurs(0);
        attributeDescBuilder.setNillable(true);
        attributeDescBuilder.setType(attributeTypeBuilder.buildType());
        featureTypeBuilder.add(attributeDescBuilder.buildDescriptor());

        // gml:name
        attributeTypeBuilder.reset();
        attributeTypeBuilder.setBinding(String.class);
        attributeDescBuilder.reset();
        attributeDescBuilder.setName(NAME);
        attributeDescBuilder.setMaxOccurs(Integer.MAX_VALUE);
        attributeDescBuilder.setMinOccurs(1);
        attributeDescBuilder.setNillable(false);
        attributeDescBuilder.setType(attributeTypeBuilder.buildType());
        featureTypeBuilder.add(attributeDescBuilder.buildDescriptor());

        // To see BoundedBy

        // sa:sampledFeature href?
        attributeTypeBuilder.reset();
        attributeTypeBuilder.setBinding(String.class);
        attributeDescBuilder.reset();
        attributeDescBuilder.setName(SAMPLED);
        attributeDescBuilder.setMaxOccurs(Integer.MAX_VALUE);
        attributeDescBuilder.setMinOccurs(1);
        attributeDescBuilder.setNillable(true);
        attributeDescBuilder.setType(attributeTypeBuilder.buildType());
        featureTypeBuilder.add(attributeDescBuilder.buildDescriptor());

        // sa:Position
        attributeTypeBuilder.reset();
        attributeTypeBuilder.setBinding(Point.class);
        attributeDescBuilder.reset();
        attributeDescBuilder.setName(POSITION);
        attributeDescBuilder.setMaxOccurs(1);
        attributeDescBuilder.setMinOccurs(1);
        attributeDescBuilder.setNillable(false);
        attributeDescBuilder.setType(attributeTypeBuilder.buildGeometryType());
        featureTypeBuilder.add(attributeDescBuilder.buildDescriptor());

        featureTypeBuilder.setDefaultGeometry(POSITION.getLocalPart());

        types.put(SAMPLINGPOINT, featureTypeBuilder.buildFeatureType());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query) throws DataStoreException {
        try {
            final Name name = query.getTypeName();
            final SimpleFeatureType sft = types.get(name);
            return handleRemaining(new OMReader(sft), query);
        } catch (SQLException ex) {
            throw new DataStoreException(ex);
        }
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        super.dispose();
        try {
            getAllSamplingPoint.close();
            connection.close();
        } catch (SQLException ex) {
            getLogger().info("SQL Exception while closing O&M datastore");
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureWriter getFeatureWriterAppend(Name typeName) throws DataStoreException {
        return handleWriterAppend(typeName);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getNames() throws DataStoreException {
        return types.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void createSchema(Name typeName, FeatureType featureType) throws DataStoreException {
        types.put(typeName, (SimpleFeatureType) featureType);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateSchema(Name typeName, FeatureType featureType) throws DataStoreException {
        types.put(typeName, (SimpleFeatureType) featureType);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteSchema(Name typeName) throws DataStoreException {
        types.remove(typeName);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType getFeatureType(Name typeName) throws DataStoreException {
        return types.get(typeName);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public QueryCapabilities getQueryCapabilities() {
        return capabilities;
    }

    /**
     * {@inheritDoc }
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
        List<FeatureId> result = new ArrayList<FeatureId>();
        FeatureType featureType = types.get(groupName);
        if (featureType != null) {
            try {
                for (Feature feature : newFeatures) {
                    if (feature instanceof SimpleFeature) {
                        SimpleFeature simpleFeature = (SimpleFeature) feature;

                        FeatureId identifier = simpleFeature.getIdentifier();
                        if (identifier == null || identifier.getID().equals("")) {
                            identifier = getNewFeatureId();
                        }
                        writeSamplingPoint.setString(1, identifier.getID());
                        writeSamplingPoint.setString(2, (String)simpleFeature.getAttribute(OMDataStore.DESC));
                        writeSamplingPoint.setString(3, (String)simpleFeature.getAttribute(OMDataStore.NAME));
                        writeSamplingPoint.setString(4, (String)simpleFeature.getAttribute(OMDataStore.SAMPLED));
                        writeSamplingPoint.setNull(5, java.sql.Types.VARCHAR);
                        Object geometry = simpleFeature.getDefaultGeometry();
                        if (geometry instanceof Point) {
                            Point pt = (Point) geometry;
                            String crsIdentifier = null;
                            if (featureType.getCoordinateReferenceSystem() != null) {
                                try {
                                    crsIdentifier = CRS.lookupIdentifier(featureType.getCoordinateReferenceSystem(), true);
                                } catch (FactoryException ex) {
                                    LOGGER.log(Level.SEVERE, null, ex);
                                }
                            }
                            if (identifier != null) {
                                writeSamplingPoint.setString(6, crsIdentifier);
                            } else {
                                writeSamplingPoint.setNull(6, java.sql.Types.VARCHAR);
                            }
                            writeSamplingPoint.setInt(7, 2);
                            writeSamplingPoint.setDouble(8, pt.getX());
                            writeSamplingPoint.setDouble(9, pt.getY());
                        } else {
                            writeSamplingPoint.setNull(6, java.sql.Types.VARCHAR);
                            writeSamplingPoint.setNull(7, java.sql.Types.INTEGER);
                            writeSamplingPoint.setNull(8, java.sql.Types.DOUBLE);
                            writeSamplingPoint.setNull(9, java.sql.Types.DOUBLE);
                        }
                        writeSamplingPoint.executeUpdate();
                        result.add(identifier);
                    }
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, writeSamplingPoint.toString(), ex);
            }
        } else {
            throw new DataStoreException("there is no such featureType: " + groupName);
        }
        return result;
    }

    public FeatureId getNewFeatureId() {
        String identifier = "sampling-point-";
        try {
            ResultSet result = getLastIndentifier.executeQuery();
            if (result.next()) {
                int nb = result.getInt(1) + 1;
                identifier = identifier + nb;
                return new DefaultFeatureId(identifier);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateFeatures(Name groupName, Filter filter, Map<? extends PropertyDescriptor, ? extends Object> values) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeFeatures(Name groupName, Filter filter) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class OMReader implements FeatureReader{

        private boolean firstCRS = true;
        private final SimpleFeatureType type;
        private final SimpleFeatureBuilder builder;
        private final ResultSet result;
        private SimpleFeature next = null;

        private OMReader(SimpleFeatureType type) throws SQLException{
            this.type = type;
            builder = new SimpleFeatureBuilder(type);
            result = getAllSamplingPoint.executeQuery();
        }

        @Override
        public FeatureType getFeatureType() {
            return type;
        }

        @Override
        public Feature next() throws DataStoreRuntimeException {
            try {
                getNext();
            } catch (Exception ex) {
                throw new DataStoreRuntimeException(ex);
            }
            SimpleFeature candidate = next;
            next = null;
            return candidate;
        }

        @Override
        public boolean hasNext() throws DataStoreRuntimeException {
            try {
                getNext();
            } catch (Exception ex) {
                throw new DataStoreRuntimeException(ex);
            }
            return next != null;
        }

        private void getNext() throws Exception{
            if(next != null) return;

            if(!result.next()){
                return;
            }


            if (firstCRS) {
                try {
                    CoordinateReferenceSystem crs = CRS.decode(result.getString("point_srsname"));
                    if (type instanceof DefaultSimpleFeatureType) {
                        ((DefaultSimpleFeatureType) type).setCoordinateReferenceSystem(crs);
                    }
                    if (type.getGeometryDescriptor() instanceof DefaultGeometryDescriptor) {
                        ((DefaultGeometryDescriptor) type.getGeometryDescriptor()).setCoordinateReferenceSystem(crs);
                    }
                    firstCRS = false;
                } catch (NoSuchAuthorityCodeException ex) {
                    throw new IOException(ex);
                } catch (FactoryException ex) {
                    throw new IOException(ex);
                }
            }
            builder.reset();
            String id = result.getString("id");
            builder.set(DESC, result.getString("description"));
            builder.set(NAME, result.getString("name"));
            builder.set(SAMPLED, result.getString("sampled_feature"));

            double x         = result.getDouble("x_value");
            double y         = result.getDouble("y_value");
            Coordinate coord = new Coordinate(x, y);
            builder.set(POSITION, GF.createPoint(coord));
            next = builder.buildFeature(id);
        }

        @Override
        public void close() {
            try {
                result.close();
            } catch (SQLException ex) {
                throw new DataStoreRuntimeException(ex);
            }
        }

        @Override
        public void remove() throws DataStoreRuntimeException{
            throw new DataStoreRuntimeException("Not supported.");
        }

    }

}