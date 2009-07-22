/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotoolkit.data.jdbc.FilterToSQL;
import org.geotoolkit.jdbc.BasicSQLDialect;
import org.geotoolkit.jdbc.JDBCDataStore;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class PostGISDialect extends BasicSQLDialect {

    private static final Map<String, Class> TYPE_TO_CLASS_MAP = new HashMap<String, Class>() {
        {
            put("GEOMETRY", Geometry.class);
            put("POINT", Point.class);
            put("POINTM", Point.class);
            put("LINESTRING", LineString.class);
            put("LINESTRINGM", LineString.class);
            put("POLYGON", Polygon.class);
            put("POLYGONM", Polygon.class);
            put("MULTIPOINT", MultiPoint.class);
            put("MULTIPOINTM", MultiPoint.class);
            put("MULTILINESTRING", MultiLineString.class);
            put("MULTILINESTRINGM", MultiLineString.class);
            put("MULTIPOLYGON", MultiPolygon.class);
            put("MULTIPOLYGONM", MultiPolygon.class);
            put("GEOMETRYCOLLECTION", GeometryCollection.class);
            put("GEOMETRYCOLLECTIONM", GeometryCollection.class);
        }
    };

    private static final Map<Class, String> CLASS_TO_TYPE_MAP = new HashMap<Class, String>() {
        {
            put(Geometry.class, "GEOMETRY");
            put(Point.class, "POINT");
            put(LineString.class, "LINESTRING");
            put(Polygon.class, "POLYGON");
            put(MultiPoint.class, "MULTIPOINT");
            put(MultiLineString.class, "MULTILINESTRING");
            put(MultiPolygon.class, "MULTIPOLYGON");
            put(GeometryCollection.class, "GEOMETRYCOLLECTION");
        }
    };

    private static final int SCHEMA_NAME = 2;
    private static final int TABLE_NAME = 3;
    private static final int COLUMN_NAME = 4;
    private static final int TYPE_NAME = 6;

    public PostGISDialect(final JDBCDataStore dataStore) {
        super(dataStore);
    }

    boolean looseBBOXEnabled = false;

    boolean estimatedExtentsEnabled = false;

    public boolean isLooseBBOXEnabled() {
        return looseBBOXEnabled;
    }

    public void setLooseBBOXEnabled(final boolean looseBBOXEnabled) {
        this.looseBBOXEnabled = looseBBOXEnabled;
    }

    @Override
    public boolean includeTable(final String schemaName, final String tableName, final Connection cx)
                                throws SQLException
    {
        if (tableName.equals("geometry_columns")) {
            return false;
        } else if (tableName.startsWith("spatial_ref_sys")) {
            return false;
        }

        // others?
        return true;
    }

    ThreadLocal<WKBAttributeIO> wkbReader = new ThreadLocal<WKBAttributeIO>();
//    WKBAttributeIO reader;
    @Override
    public Geometry decodeGeometryValue(final GeometryDescriptor descriptor, final ResultSet rs,
            final String column, final GeometryFactory factory, final Connection cx)
            throws IOException, SQLException
    {
        WKBAttributeIO reader = wkbReader.get();
        if (reader == null) {
            reader = new WKBAttributeIO(factory);
            wkbReader.set(reader);
        }

        return (Geometry) reader.read(rs, column);
    }

    @Override
    public Geometry decodeGeometryValue(final GeometryDescriptor descriptor, final ResultSet rs,
            final int column, final GeometryFactory factory, final Connection cx)
            throws IOException, SQLException{
        WKBAttributeIO reader = wkbReader.get();
        if (reader == null) {
            reader = new WKBAttributeIO(factory);
            wkbReader.set(reader);
        }

        return (Geometry) reader.read(rs, column);
    }

    @Override
    public void encodeGeometryColumn(final GeometryDescriptor gatt, final int srid,
                                     final StringBuffer sql){
        final CoordinateReferenceSystem crs = gatt.getCoordinateReferenceSystem();
        final int dimensions = (crs == null) ? 2 : crs.getCoordinateSystem().getDimension();
        sql.append("encode(");
        if (dimensions > 2) {
            sql.append("asEWKB(");
            encodeColumnName(gatt.getLocalName(), sql);
        } else {
            sql.append("asBinary(");
            sql.append("force_2d(");
            encodeColumnName(gatt.getLocalName(), sql);
            sql.append(")");
        }
        sql.append(",'XDR'),'base64')");
    }

    @Override
    public void encodeGeometryEnvelope(final String tableName, final String geometryColumn,
            final StringBuffer sql){
        if (estimatedExtentsEnabled) {
            sql.append("estimated_extent(");
            sql.append("'" + tableName + "','" + geometryColumn + "'))));");
        } else {
            sql.append("AsText(force_2d(Envelope(");
            sql.append("Extent(\"" + geometryColumn + "\"))))");
        }
    }

    @Override
    public Envelope decodeGeometryEnvelope(final ResultSet rs, final int column,
            final Connection cx) throws SQLException, IOException{
        try {
            final String envelope = rs.getString(column);
            if (envelope != null) {
                return new WKTReader().read(envelope).getEnvelopeInternal();
            } else {
                // empty one
                final Envelope env = new Envelope();
                env.init(0, 0, 0, 0);
                return env;
            }
        } catch (ParseException e) {
            throw (IOException) new IOException(
                    "Error occurred parsing the bounds WKT").initCause(e);
        }
    }

    @Override
    public Class<?> getMapping(final ResultSet columnMetaData, final Connection cx)
            throws SQLException {

        if (!columnMetaData.getString(TYPE_NAME).equals("geometry")) {
            return null;
        }

        // grab the information we need to proceed
        final String tableName = columnMetaData.getString(TABLE_NAME);
        final String columnName = columnMetaData.getString(COLUMN_NAME);
        final String schemaName = columnMetaData.getString(SCHEMA_NAME);

        // first attempt, try with the geometry metadata
        Statement statement = null;
        ResultSet result = null;
        String gType = null;
        try {
            final StringBuilder sb = new StringBuilder("SELECT TYPE FROM GEOMETRY_COLUMNS WHERE ");
            sb.append("F_TABLE_SCHEMA = '").append(schemaName).append("' ");
            sb.append("AND F_TABLE_NAME = '").append(tableName).append("' ");
            sb.append("AND F_GEOMETRY_COLUMN = '").append(columnName).append('\'');
            final String sqlStatement = sb.toString();
            LOGGER.log(Level.FINE, "Geometry type check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                gType = result.getString(1);
            }
        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }

        // TODO: add the support code needed to infer from the first geometry
        // if (gType == null) {
        // // no geometry_columns entry, try grabbing a feature
        // StringBuffer sql = new StringBuffer();
        // sql.append("SELECT encode(AsBinary(force_2d(\"");
        // sql.append(columnName);
        // sql.append("\"), 'XDR'),'base64') FROM \"");
        // sql.append(schemaName);
        // sql.append("\".\"");
        // sql.append(tableName);
        // sql.append("\" LIMIT 1");
        // result = statement.executeQuery(sql.toString());
        // if (result.next()) {
        // AttributeIO attrIO = getGeometryAttributeIO(null, null);
        // Object object = attrIO.read(result, 1);
        // if (object instanceof Geometry) {
        // Geometry geom = (Geometry) object;
        // geometryType = geom.getGeometryType().toUpperCase();
        // type = geom.getClass();
        // srid = geom.getSRID(); // will return 0 unless we support
        // // EWKB
        // }
        // }
        // result.close();
        // }
        // statement.close();

        // decode the type into
        Class geometryClass = (Class) TYPE_TO_CLASS_MAP.get(gType);
        if (geometryClass == null) {
            geometryClass = Geometry.class;
        }
        return geometryClass;
    }

    @Override
    public Integer getGeometrySRID(String schemaName, final String tableName, final String columnName,
            final Connection cx) throws SQLException{

        // first attempt, try with the geometry metadata
        Statement statement = null;
        ResultSet result = null;
        Integer srid = null;
        try {
            if (schemaName == null) {
                schemaName = "public";
            }
            final StringBuilder sb = new StringBuilder("SELECT SRID FROM GEOMETRY_COLUMNS WHERE ");
            sb.append("F_TABLE_SCHEMA = '").append(schemaName).append("' ");
            sb.append("AND F_TABLE_NAME = '").append(tableName).append("' ");
            sb.append("AND F_GEOMETRY_COLUMN = '").append(columnName).append('\'');
            final String sqlStatement = sb.toString();

            LOGGER.log(Level.FINE, "Geometry type check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                srid = result.getInt(1);
            }
        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }

        // TODO: implement inference from the first feature
        // try asking the first feature for its srid
        // sql = new StringBuffer();
        // sql.append("SELECT SRID(\"");
        // sql.append(geometryColumnName);
        // sql.append("\") FROM \"");
        // if (schemaEnabled && dbSchema != null && dbSchema.length() > 0) {
        // sql.append(dbSchema);
        // sql.append("\".\"");
        // }
        // sql.append(tableName);
        // sql.append("\" LIMIT 1");
        // sqlStatement = sql.toString();
        // result = statement.executeQuery(sqlStatement);
        // if (result.next()) {
        // int retSrid = result.getInt(1);
        // JDBCUtils.close(statement);
        // return retSrid;
        // }

        return srid;
    }

    @Override
    public String getSequenceForColumn(final String schemaName, final String tableName,
            final String columnName, final Connection cx) throws SQLException
    {
        final Statement st = cx.createStatement();
        try {
            // pg_get_serial_sequence oddity: table name needs to be
            // escaped with "", whilst column name, doesn't...
            final StringBuilder sb = new StringBuilder("SELECT pg_get_serial_sequence('\"");
            if (schemaName != null && !"".equals(schemaName))
                sb.append(schemaName).append("\".\"");
            sb.append(tableName).append("\"', '").append(columnName).append("')");
            final String sql = sb.toString();
            dataStore.getLogger().fine(sql);
            final ResultSet rs = st.executeQuery(sql);
            try {
                if (rs.next()) {
                    return rs.getString(1);
                }
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(st);
        }

        return null;
    }

    @Override
    public Object getNextSequenceValue(final String schemaName, final String sequenceName,
            final Connection cx) throws SQLException
    {
        final Statement st = cx.createStatement();
        try {
            final String sql = "SELECT nextval('" + sequenceName + "')";

            dataStore.getLogger().fine(sql);
            final ResultSet rs = st.executeQuery(sql);
            try {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(st);
        }

        return null;
    }

    @Override
    public Object getNextAutoGeneratedValue(final String schemaName, final String tableName,
            final String columnName, final Connection cx) throws SQLException
    {
        return null;

        // the code to grab the current sequence value is here,
        // but it will work only _after_ the insert occurred

        // Statement st = cx.createStatement();
        // try {
        // String sql = "SELECT currval(pg_get_serial_sequence('" + tableName +
        // "', '" + columnName + "'))";
        //
        // dataStore.getLogger().fine( sql);
        // ResultSet rs = st.executeQuery( sql);
        // try {
        // if ( rs.next() ) {
        // return rs.getLong(1);
        // }
        // } finally {
        // dataStore.closeSafe(rs);
        // }
        // }
        // finally {
        // dataStore.closeSafe(st);
        // }
        //
        // return null;
    }

    @Override
    public void registerClassToSqlMappings(final Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);

        // jdbc metadata for geom columns reports DATA_TYPE=1111=Types.OTHER
        mappings.put(Geometry.class, Types.OTHER);
    }

    @Override
    public void registerSqlTypeNameToClassMappings(final Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);

        mappings.put("geometry", Geometry.class);
    }

    @Override
    public void registerSqlTypeToSqlTypeNameOverrides(final Map<Integer, String> overrides) {
        overrides.put(Types.VARCHAR, "VARCHAR");
        overrides.put(Types.BOOLEAN, "BOOL");
    }

    @Override
    public String getGeometryTypeName(final Integer type) {
        return "geometry";
    }

    @Override
    public void encodePrimaryKey(final String column, final StringBuffer sql) {
        encodeColumnName(column, sql);
        sql.append(" SERIAL PRIMARY KEY");
    }

    /**
     * Creates GEOMETRY_COLUMN registrations and spatial indexes for all
     * geometry columns
     */
    @Override
    public void postCreateTable(String schemaName, final SimpleFeatureType featureType,
            final Connection cx) throws SQLException
    {
        if (schemaName == null) {
            schemaName = "public";
        }
        final String tableName = featureType.getName().getLocalPart();

        Statement st = null;
        try {
            st = cx.createStatement();

            // register all geometry columns in the database
            for (AttributeDescriptor att : featureType.getAttributeDescriptors()) {
                if (att instanceof GeometryDescriptor) {
                    final GeometryDescriptor gd = (GeometryDescriptor) att;

                    // lookup or reverse engineer the srid
                    int srid = -1;
                    if (gd.getUserData().get(JDBCDataStore.JDBC_NATIVE_SRID) != null) {
                        srid = (Integer) gd.getUserData().get(
                                JDBCDataStore.JDBC_NATIVE_SRID);
                    } else if (gd.getCoordinateReferenceSystem() != null) {
                        try {
                            final Integer result = CRS.lookupEpsgCode(gd.getCoordinateReferenceSystem(), true);
                            if (result != null) {
                                srid = result;
                            }
                        } catch (FactoryException e) {
                            LOGGER.log(Level.FINE, "Error looking up the "
                                    + "epsg code for metadata "
                                    + "insertion, assuming -1", e);
                        }
                    }

                    // assume 2 dimensions, but ease future customisation
                    final int dimensions = 2;

                    // grab the geometry type
                    String geomType = CLASS_TO_TYPE_MAP.get(gd.getType().getBinding());
                    if (geomType == null)
                        geomType = "GEOMETRY";

                    // register the geometry type, first remove and eventual
                    // leftover, then write out the real one
                    StringBuilder sb = new StringBuilder("DELETE FROM GEOMETRY_COLUMNS");
                    sb.append(" WHERE f_table_catalog=''");
                    sb.append(" AND f_table_schema = '").append(schemaName).append('\'');
                    sb.append(" AND f_table_name = '").append(tableName).append('\'');
                    sb.append(" AND f_geometry_column = '").append(gd.getLocalName()).append('\'');
                    String sql = sb.toString();

                    LOGGER.fine( sql );
                    st.execute( sql );

                    sb = new StringBuilder("INSERT INTO GEOMETRY_COLUMNS VALUES ('',");
                    sb.append('\'').append(schemaName).append("',");
                    sb.append('\'').append(tableName).append("',");
                    sb.append('\'').append(gd.getLocalName()).append("',");
                    sb.append(dimensions).append(',');
                    sb.append(srid).append(',');
                    sb.append('\'').append(geomType).append("')");
                    sql = sb.toString();
                    LOGGER.fine( sql );
                    st.execute( sql );

                    // add srid checks
                    if (srid > -1) {
                        sb = new StringBuilder("ALTER TABLE \"").append(tableName).append('"');
                        sb.append(" ADD CONSTRAINT \"enforce_srid_");
                        sb.append(gd.getLocalName()).append('"');
                        sb.append(" CHECK (SRID(");
                        sb.append('"').append(gd.getLocalName()).append('"');
                        sb.append(") = ").append(srid).append(')');
                        sql = sb.toString();
                        LOGGER.fine( sql );
                        st.execute(sql);
                    }

                    // add dimension checks

                    sb = new StringBuilder("ALTER TABLE \"").append(tableName).append('"');
                    sb.append(" ADD CONSTRAINT \"enforce_dims_");
                    sb.append(gd.getLocalName()).append('"');
                    sb.append(" CHECK (ndims(\"").append(gd.getLocalName()).append("\") = 2)");
                    sql = sb.toString();
                    LOGGER.fine(sql);
                    st.execute(sql);

                    // add geometry type checks
                    if (!geomType.equals("GEOMETRY")) {
                        sb = new StringBuilder("ALTER TABLE \"").append(tableName);
                        sb.append('"');
                        sb.append(" ADD CONSTRAINT \"enforce_geotype_");
                        sb.append(gd.getLocalName()).append('"');
                        sb.append(" CHECK (geometrytype(");
                        sb.append('"').append(gd.getLocalName()).append('"');
                        sb.append(") = '").append(geomType).append("'::text OR \"");
                        sb.append(gd.getLocalName()).append('"').append(" IS NULL)");
                        sql = sb.toString();
                        LOGGER.fine(sql);
                        st.execute(sql);
                    }

                    // add the spatial index
                    sb = new StringBuilder("CREATE INDEX \"spatial_").append(tableName);
                    sb.append('_').append(gd.getLocalName().toLowerCase()).append('"');
                    sb.append(" ON ");
                    sb.append('"').append(tableName).append('"');
                    sb.append(" USING GIST (");
                    sb.append('"').append(gd.getLocalName()).append('"').append(')');
                    sql = sb.toString();
                    LOGGER.fine(sql);
                    st.execute(sql);
                }
            }
            cx.commit();
        } finally {
            dataStore.closeSafe(st);
        }
    }

    @Override
    public void encodeGeometryValue(Geometry value, final int srid, final StringBuffer sql) throws IOException {
        if (value == null) {
            sql.append("NULL");
        } else {
            if (value instanceof LinearRing) {
                //postgis does not handle linear rings, convert to just a line string
                value = value.getFactory().createLineString(((LinearRing) value).getCoordinateSequence());
            }

            sql.append("GeomFromText('" + value.toText() + "', " + srid + ")");
        }
    }

    @Override
    public FilterToSQL createFilterToSQL() {
        final PostgisFilterToSQL sql = new PostgisFilterToSQL(this);
        sql.setLooseBBOXEnabled(looseBBOXEnabled);
        return sql;
    }

    @Override
    public boolean isLimitOffsetSupported() {
        return true;
    }

    @Override
    public void applyLimitOffset(final StringBuffer sql, final int limit, final int offset) {
        if (limit > 0 && limit < Integer.MAX_VALUE) {
            sql.append(" LIMIT ").append(limit);
            if (offset > 0) {
                sql.append(" OFFSET ").append(offset);
            }
        } else if (offset > 0) {
            sql.append(" OFFSET ").append(offset);
        }
    }

    @Override
    public CoordinateReferenceSystem createCRS(final int srid, final Connection cx) throws SQLException {
        if (srid == 4326) {
            LOGGER.fine("POSTGIS SRID 4326 : Inverting axes ");
            return DefaultGeographicCRS.WGS84;
        }
        return super.createCRS(srid, cx);
    }



}
