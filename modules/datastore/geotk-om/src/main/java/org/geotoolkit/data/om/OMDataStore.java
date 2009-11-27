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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotoolkit.data.AbstractDataStore;
import org.geotoolkit.data.DataUtilities;
import org.geotoolkit.data.DefaultFeatureCollection;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.AttributeTypeBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.simple.DefaultSimpleFeatureType;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.referencing.CRS;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @module pending
 */
public class OMDataStore extends AbstractDataStore {

    private final static Name SAMPLINGPOINT = new DefaultName("http://www.opengis.net/sampling/1.0", "SamplingPoint");

    private final Map<Name, SimpleFeatureType> types = new HashMap<Name,SimpleFeatureType>();

    private final Connection connection;

    private PreparedStatement getAllSamplingPoint;

    private static final Name DESC     = new DefaultName("http://www.opengis.net/gml", "description");
    private static final Name NAME     = new DefaultName("http://www.opengis.net/gml", "name");
    private static final Name SAMPLED  = new DefaultName("http://www.opengis.net/sampling/1.0", "sampledFeature");
    private static final Name POSITION = new DefaultName("http://www.opengis.net/sampling/1.0", "position");

    private static final GeometryFactory GF = new GeometryFactory();

    public OMDataStore(Connection connection) {
        //not transactional for the moment
        super(false);
        this.connection = connection;
        initTypes();
        initStatement();
    }

    private void initStatement() {
        try {
            getAllSamplingPoint = connection.prepareStatement("SELECT * FROM \"observation\".\"sampling_points\"");
        } catch (SQLException ex) {
           LOGGER.severe("SQL Exception while requesting the O&M database:" + ex.getMessage());
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
    protected Map<Name, SimpleFeatureType> getTypes() throws IOException {
        return Collections.unmodifiableMap(types);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query) throws IOException {
        final Name name = query.getTypeName();
        final SimpleFeatureType sft = types.get(name);
        final FeatureCollection<SimpleFeatureType,SimpleFeature> collection = getFeatureCollection(sft, name.getLocalPart());
        return DataUtilities.wrapToReader(sft, collection.features());
    }
    
   private FeatureCollection<SimpleFeatureType,SimpleFeature> getFeatureCollection(SimpleFeatureType sft, String typeName) throws IOException {
        
        final FeatureCollection<SimpleFeatureType,SimpleFeature> collection = new DefaultFeatureCollection(typeName + "-collection", sft);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(sft);
        try {
            ResultSet result = getAllSamplingPoint.executeQuery();
            boolean firstCRS = true;
            while (result.next()) {
                if (firstCRS) {
                    String srsName = result.getString("point_srsname");
                    CoordinateReferenceSystem crs;
                    try {
                        crs = CRS.decode(srsName);
                        ((DefaultSimpleFeatureType)sft).setCoordinateReferenceSystem(crs);
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
                collection.add(builder.buildFeature(id));
            }
            result.close();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQL exception while reading the record of samplingPoint table", ex);
        }
        return collection;
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
            LOGGER.info("SQL Exception while closing O&M datastore");
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected JTSEnvelope2D getBounds(Query query) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected int getCount(Query query) throws IOException {
        return -1;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        throw new IOException("Schema creation not supported.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
        throw new IOException("Schema update not supported.");
    }

}
