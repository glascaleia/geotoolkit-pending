/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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

package org.geotoolkit.jdbc.reverse;

import java.util.Collection;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Collections;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.geotoolkit.feature.AttributeTypeBuilder;
import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.jdbc.DefaultJDBCDataStore;
import org.geotoolkit.jdbc.JDBCDataStore;
import org.geotoolkit.jdbc.dialect.SQLDialect;
import org.geotoolkit.jdbc.fid.PrimaryKeyColumn;
import org.geotoolkit.jdbc.fid.PrimaryKey;
import org.geotoolkit.jdbc.fid.AutoGeneratedPrimaryKeyColumn;
import org.geotoolkit.jdbc.fid.NullPrimaryKey;
import org.geotoolkit.jdbc.fid.NonIncrementingPrimaryKeyColumn;
import org.geotoolkit.jdbc.fid.SequencedPrimaryKeyColumn;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.geotoolkit.jdbc.reverse.MetaDataConstants.*;
import static org.geotoolkit.jdbc.AbstractJDBCDataStore.*;

/**
 * Represent the structure of the database. The work done here is similar to
 * reverse engineering.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class DataBaseModel {

    /**
     * Custom factory where types can be modified after they are created.
     */
    private static final FeatureTypeFactory FTF = new ModifiableFeatureTypeFactory();
    /**
     * Dummy type which will be replaced dynamically in the reverse engineering process.
     */
    private static final ComplexType FLAG_TYPE = FTF.createComplexType(
            new DefaultName("flag"), Collections.EMPTY_LIST, false, false, Collections.EMPTY_LIST, null, null);


    private final DefaultJDBCDataStore store;
    private final Map<Name,PrimaryKey> pkIndex = new HashMap<Name, PrimaryKey>();
    private final Map<Name,FeatureType> typeIndex = new HashMap<Name, FeatureType>();
    private Map<String,SchemaMetaModel> schemas = null;
    private Set<Name> nameCache = null;

    //metadata getSuperTable query is not implemented on all databases
    private Boolean handleSuperTableMetadata = null;

    public DataBaseModel(final DefaultJDBCDataStore store){
        this.store = store;
    }

    public Collection<SchemaMetaModel> getSchemaMetaModels() throws DataStoreException {
        if(schemas == null){
            analyze();
        }
        return schemas.values();
    }

    public SchemaMetaModel getSchemaMetaModel(String name) throws DataStoreException{
        if(schemas == null){
            analyze();
        }
        return schemas.get(name);
    }

    /**
     * Clear the model cache. A new database analyze will be made the next time
     * it is needed.
     */
    public synchronized void clearCache(){
        pkIndex.clear();
        typeIndex.clear();
        nameCache = null;
        schemas = null;
    }

    public PrimaryKey getPrimaryKey(final Name featureTypeName) throws DataStoreException{
        if(schemas == null){
            analyze();
        }
        return pkIndex.get(featureTypeName);
    }

    public synchronized Set<Name> getNames() throws DataStoreException {
        Set<Name> ref = nameCache;
        if(ref == null){
            analyze();
            ref = Collections.unmodifiableSet(new HashSet<Name>(typeIndex.keySet()));
            nameCache = ref;
        }
        return ref;
    }

    public FeatureType getFeatureType(final Name typeName) throws DataStoreException {
        if(schemas == null){
            analyze();
        }
        return typeIndex.get(typeName);
    }

    /**
     * Explore all tables and views then recreate a complex feature model from
     * relations.
     */
    private synchronized void analyze() throws DataStoreException{
        if(schemas != null){
            //already analyzed
            return;
        }

        clearCache();
        schemas = new HashMap<String, SchemaMetaModel>();

        Connection cx = null;
        ResultSet schemaSet = null;
        try {
            cx = store.getDataSource().getConnection();

            final DatabaseMetaData metadata = cx.getMetaData();
            schemaSet = metadata.getSchemas();
            while (schemaSet.next()) {
                final String SchemaName = schemaSet.getString(Schema.TABLE_SCHEM);
                final SchemaMetaModel schema = analyzeSchema(SchemaName);
                schemas.put(schema.name, schema);
            }

        } catch (SQLException e) {
            throw new DataStoreException("Error occurred analyzing database model.", e);
        } finally {
            store.closeSafe(schemaSet);
            store.closeSafe(cx);
        }

        reverseSimpleFeatureTypes();
        reverseComplexFeatureTypes();

        //build indexes---------------------------------------------------------
        final String baseSchemaName = store.getDatabaseSchema();

        final Collection<SchemaMetaModel> candidates;
        if(baseSchemaName == null){
            //take all schemas
            candidates = getSchemaMetaModels();
        }else{
            candidates = Collections.singleton(getSchemaMetaModel(baseSchemaName));
        }

        for(SchemaMetaModel schema : candidates){
            for(TableMetaModel table : schema.tables.values()){
                final SimpleFeatureType sft = table.simpleType;
                final Name name = sft.getName();
                typeIndex.put(name, sft);
                pkIndex.put(name, table.key);
            }
        }

    }

    private SchemaMetaModel analyzeSchema(final String schemaName) throws DataStoreException{

        final SchemaMetaModel schema = new SchemaMetaModel(schemaName);

        Connection cx = null;
        ResultSet tableSet = null;
        try {
            cx = store.getDataSource().getConnection();

            final DatabaseMetaData metadata = cx.getMetaData();
            tableSet = metadata.getTables(null, schemaName, "%",
                    new String[]{Table.VALUE_TYPE_TABLE, Table.VALUE_TYPE_VIEW});

            while (tableSet.next()) {
                final TableMetaModel table = analyzeTable(tableSet);
                schema.tables.put(table.name, table);
            }

        } catch (SQLException e) {
            throw new DataStoreException("Error occurred analyzing database model.", e);
        } finally {
            store.closeSafe(tableSet);
            store.closeSafe(cx);
        }

        return schema;
    }

    private TableMetaModel analyzeTable(final ResultSet tableSet) throws DataStoreException, SQLException{
        final SQLDialect dialect = store.getDialect();
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder(FTF);

        final String schemaName = tableSet.getString(Table.TABLE_SCHEM);
        final String tableName = tableSet.getString(Table.TABLE_NAME);

        final TableMetaModel table = new TableMetaModel(tableName);
        
        Connection cx = null;
        ResultSet result = null;
        try {
            cx = store.getDataSource().getConnection();
            final DatabaseMetaData metadata = cx.getMetaData();

            //explore all columns ----------------------------------------------
            result = metadata.getColumns(null, schemaName, tableName, "%");
            while (result.next()) {
                final PropertyDescriptor desc = analyzeColumn(result);
                ftb.add(analyzeColumn(result));
            }
            store.closeSafe(result);

            //find primary key -------------------------------------------------
            final List<PrimaryKeyColumn> cols = new ArrayList();
            result = metadata.getPrimaryKeys(null, schemaName, tableName);
            while (result.next()) {
                final String columnName = result.getString(Column.COLUMN_NAME);

                //look up the type ( should only be one row )
                final ResultSet columns = metadata.getColumns(null, schemaName, tableName, columnName);
                columns.next();

                final int binding = columns.getInt(Column.DATA_TYPE);
                Class columnType = dialect.getMapping(binding);

                if (columnType == null) {
                    store.getLogger().log(Level.WARNING, "No class for sql type {0}", binding);
                    columnType = Object.class;
                }

                //determine which type of primary key we have
                PrimaryKeyColumn col = null;

                //1. Auto Incrementing?
                final String str = columns.getString(Column.IS_AUTOINCREMENT);
                if(Column.VALUE_YES.equalsIgnoreCase(str)){
                    col = new AutoGeneratedPrimaryKeyColumn(columnName, columnType);
                }

                //2. Has a sequence?
                if (col == null) {
                    final String sequenceName = dialect.getSequenceForColumn(
                            schemaName, tableName, columnName, cx);
                    if (sequenceName != null) {
                        col = new SequencedPrimaryKeyColumn(columnName, columnType, sequenceName);
                    }
                }

                if (col == null) {
                    col = new NonIncrementingPrimaryKeyColumn(columnName, columnType);
                }

                cols.add(col);
            }

            if (cols.isEmpty()) {
                store.getLogger().log(Level.INFO, "No primary key found for {0}.", tableName);
                table.key = new NullPrimaryKey(tableName);
            } else {
                table.key = new PrimaryKey(tableName, cols);
            }
            store.closeSafe(result);

            //find imported keys -----------------------------------------------
            result = metadata.getImportedKeys(null, schemaName, tableName);
            while (result.next()) {
                final String localColumn = result.getString(ImportedKey.FKCOLUMN_NAME);
                final String refSchemaName = result.getString(ImportedKey.PKTABLE_SCHEM);
                final String refTableName = result.getString(ImportedKey.PKTABLE_NAME);
                final String refColumnName = result.getString(ImportedKey.PKCOLUMN_NAME);
                table.importedKeys.add(new RelationMetaModel(localColumn,
                        refSchemaName, refTableName, refColumnName, true));
            }
            store.closeSafe(result);

            //find exported keys -----------------------------------------------
            result = metadata.getExportedKeys(null, schemaName, tableName);
            while (result.next()) {
                final String localColumn = result.getString(ExportedKey.PKCOLUMN_NAME);
                final String refSchemaName = result.getString(ExportedKey.FKTABLE_SCHEM);
                final String refTableName = result.getString(ExportedKey.FKTABLE_NAME);
                final String refColumnName = result.getString(ExportedKey.FKCOLUMN_NAME);
                table.exportedKeys.add(new RelationMetaModel(localColumn,
                        refSchemaName, refTableName, refColumnName, false));
            }
            store.closeSafe(result);

            //find parent table if any -----------------------------------------
            if(handleSuperTableMetadata == null || handleSuperTableMetadata){
                try{
                    result = metadata.getSuperTables(null, schemaName, tableName);
                    while (result.next()) {
                        final String parentTable = result.getString(SuperTable.SUPERTABLE_NAME);
                        table.parents.add(parentTable);
                    }
                }catch(final SQLException ex){
                    //not implemented by database
                    handleSuperTableMetadata = Boolean.FALSE;
                    store.getLogger().log(Level.INFO, "Database does not handle getSuperTable, feature type hierarchy will be ignored.");
                }finally{
                    store.closeSafe(result);
                }
            }

        } catch (SQLException e) {
            throw new DataStoreException("Error occurred analyzing table : " + tableName, e);
        } finally {
            store.closeSafe(result);
            store.closeSafe(cx);
        }

        ftb.setName(tableName);
        table.baseType = ftb.buildType();
        return table;
    }

    private AttributeDescriptor analyzeColumn(final ResultSet columnSet) throws SQLException, DataStoreException{
        final SQLDialect dialect = store.getDialect();
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder(FTF);
        final AttributeTypeBuilder atb = new AttributeTypeBuilder(FTF);

        final String schemaName     = columnSet.getString(Column.TABLE_SCHEM);
        final String tableName      = columnSet.getString(Column.TABLE_NAME);
        final String columnName     = columnSet.getString(Column.COLUMN_NAME);
        final int columnDataType    = columnSet.getInt(Column.DATA_TYPE);
        final String columnTypeName = columnSet.getString(Column.TYPE_NAME);
        final String columnNullable = columnSet.getString(Column.IS_NULLABLE);

        atb.setName(columnName);
        adb.setName(columnName);

        Connection cx = null;
        try {
            cx = store.getDataSource().getConnection();
            dialect.buildMapping(atb, cx, columnTypeName, columnDataType,
                    schemaName, tableName, columnName);
        } catch (SQLException e) {
            throw new DataStoreException("Error occurred analyzing column : " + columnName, e);
        } finally {
            store.closeSafe(cx);
        }

        //table values are always min 1, max 1
        adb.setMinOccurs(1);
        adb.setMaxOccurs(1);

        //nullability
        adb.setNillable(!Column.VALUE_NO.equalsIgnoreCase(columnNullable));

        if(Geometry.class.isAssignableFrom(atb.getBinding())){
            adb.setType(atb.buildGeometryType());
        }else{
            adb.setType(atb.buildType());
        }
        adb.findBestDefaultValue();
        return adb.buildDescriptor();
    }

    /**
     * Analyze the metadata of the ResultSet to rebuild a feature type.
     *
     * @param result
     * @param name
     * @return FeatureType
     * @throws SQLException
     */
    public FeatureType analyzeResult(final ResultSet result, final String name) throws SQLException, DataStoreException{
        final SQLDialect dialect = store.getDialect();
        final String namespace = store.getNamespaceURI();
        
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder(FTF);
        ftb.setName(ensureGMLNS(namespace, name));

        final ResultSetMetaData metadata = result.getMetaData();
        final int nbcol = metadata.getColumnCount();

        for(int i=1; i<=nbcol; i++){
            final String columnName = metadata.getColumnName(i);
            final String typeName = metadata.getColumnTypeName(i);
            final String schemaName = metadata.getSchemaName(i);
            final String tableName = metadata.getTableName(i);
            final int type = metadata.getColumnType(i);

            //search if we already have this minute
            PropertyDescriptor desc = null;
            final SchemaMetaModel schema = getSchemaMetaModel(schemaName);
            if(schema != null){
                TableMetaModel table = schema.getTable(tableName);
                if(table != null){
                    desc = table.getSimpleType().getDescriptor(columnName);
                }
            }

            if(desc == null){
                //could not find the original type
                //this column must be calculated
                final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder(FTF);
                final AttributeTypeBuilder atb = new AttributeTypeBuilder(FTF);

                adb.setName(ensureGMLNS(namespace, columnName));
                adb.setMinOccurs(1);
                adb.setMaxOccurs(1);

                final int nullable = metadata.isNullable(i);
                adb.setNillable(nullable == metadata.columnNullable);


                atb.setName(ensureGMLNS(namespace, columnName));
                Connection cx = null;
                try {
                    cx = store.getDataSource().getConnection();
                    dialect.buildMapping(atb, cx, typeName, type,
                            schemaName, tableName, columnName);
                } catch (SQLException e) {
                    throw new DataStoreException("Error occurred analyzing column : " + columnName, e);
                } finally {
                    store.closeSafe(cx);
                }

                if(Geometry.class.isAssignableFrom(atb.getBinding())){
                    adb.setType(atb.buildGeometryType());
                }else{
                    adb.setType(atb.buildType());
                }
                
                adb.findBestDefaultValue();
                desc = adb.buildDescriptor();
            }

            ftb.add(desc);
        }

        return ftb.buildFeatureType();
    }

    /**
     * Rebuild simple feature types for each table.
     */
    private void reverseSimpleFeatureTypes(){
        final SQLDialect dialect = store.getDialect();

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder(FTF);
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder(FTF);
        final AttributeTypeBuilder atb = new AttributeTypeBuilder(FTF);

        for(final SchemaMetaModel schema : schemas.values()){
            for(final TableMetaModel table : schema.tables.values()){
                final String tableName = table.name;
                
                //add flag for primary key fields-------------------------------
                final PrimaryKey pk = table.key;
                for(PrimaryKeyColumn column : pk.getColumns()){
                    final String attName = column.getName();
                    final PropertyDescriptor base = table.baseType.getDescriptor(attName);
                    base.getUserData().put(HintsPending.PROPERTY_IS_IDENTIFIER, Boolean.TRUE);
                }


                //fill the namespace--------------------------------------------
                ftb.reset();
                ftb.copy(table.baseType);
                final String namespace = store.getNamespaceURI();
                ftb.setName(namespace, ftb.getName().getLocalPart());


                final List<PropertyDescriptor> descs = ftb.getProperties();

                for(int i=0,n=descs.size(); i<n; i++){
                    final PropertyDescriptor desc = descs.get(i);
                    final PropertyType type = desc.getType();
                    final String name = desc.getName().getLocalPart();

                    adb.reset();
                    adb.copy((AttributeDescriptor) desc);
                    adb.setName(ensureGMLNS(namespace,name));
                    atb.reset();
                    atb.copy((AttributeType) type);
                    atb.setName(ensureGMLNS(namespace,name));
                    adb.setType(atb.buildType());

                    //Set the CRS if it's a geometry
                    if (Geometry.class.isAssignableFrom(type.getBinding())) {
                        //add the attribute as a geometry, try to figure out
                        // its srid first
                        Integer srid = null;
                        CoordinateReferenceSystem crs = null;
                        Connection cx = null;
                        try {
                            cx = store.getDataSource().getConnection();
                            srid = dialect.getGeometrySRID(store.getDatabaseSchema(), tableName, name, cx);
                            if(srid != null)
                                crs = dialect.createCRS(srid, cx);
                        } catch (SQLException e) {
                            String msg = "Error occured determing srid for " + tableName + "."+ name;
                            store.getLogger().log(Level.WARNING, msg, e);
                        } finally{
                            store.closeSafe(cx);
                        }

                        atb.setCRS(crs);
                        if(srid != null){
                            adb.addUserData(JDBCDataStore.JDBC_NATIVE_SRID, srid);
                        }
                        adb.setType(atb.buildGeometryType());
                        adb.findBestDefaultValue();
                    }

                    descs.set(i, adb.buildDescriptor());
                }

                table.simpleType = ftb.buildSimpleFeatureType();
            }
        }
        
    }

    /**
     * Rebuild complex feature types using foreign key relations.
     */
    private void reverseComplexFeatureTypes(){
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder(FTF);
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder(FTF);
        
        //result map
        final Map<String,ModifiableType> builded = new HashMap<String, ModifiableType>();

        //first pass to create the real types but without relations types-------
        //since we must have all of them before creating relations
        for(final SchemaMetaModel schema : schemas.values()){
            for(final TableMetaModel table : schema.tables.values()){
                final String code = schema.name +"."+table.name;

                //create the complex model by replacing descriptors
                final ComplexType baseType = table.simpleType;
                ftb.reset();
                ftb.copy(baseType);

                // replace 0:1 relations----------------------------------------
                for(final RelationMetaModel relation : table.importedKeys){

                    //find the descriptor to replace
                    final List<PropertyDescriptor> descs = ftb.getProperties();
                    int index = -1;
                    for(int i=0,n=descs.size();i<n;i++){
                        final PropertyDescriptor pd = descs.get(i);
                        if(pd.getName().getLocalPart().equals(relation.currentColumn)){
                            index = i;
                        }
                    }

                    //create the new descriptor derivated
                    final PropertyDescriptor baseDescriptor = descs.get(index);
                    adb.reset();
                    adb.copy((AttributeDescriptor) baseDescriptor);
                    adb.setType(FLAG_TYPE);
                    adb.setDefaultValue(null);
                    final PropertyDescriptor newDescriptor = adb.buildDescriptor();
                    descs.set(index, newDescriptor);
                }

                // create N:1 relations-----------------------------------------
                for(final RelationMetaModel relation : table.exportedKeys){

                    adb.reset();
                    adb.setName(relation.foreignTable);
                    adb.setType(FLAG_TYPE);
                    adb.setMinOccurs(0);
                    adb.setMaxOccurs(Integer.MAX_VALUE);
                    adb.setNillable(false);
                    adb.setDefaultValue(null);
                    ftb.add(adb.buildDescriptor());
                }

                final FeatureType ft = ftb.buildFeatureType();
                builded.put(code, (ModifiableType) ft);

                table.complexType = ft;
            }
        }

        //second pass to fill relations-----------------------------------------
        for(final SchemaMetaModel schema : schemas.values()){
            for(final TableMetaModel table : schema.tables.values()){
                final String code = schema.name +"."+table.name;

                //modify the properties which are relations
                final ModifiableType candidate = builded.get(code);

                //replace 0:1 relations types-----------------------------------
                for(final RelationMetaModel relation : table.importedKeys){
                    final String relCode = relation.foreignSchema +"."+relation.foreignTable;
                    final ComplexType relType = builded.get(relCode);
                    
                    //find the descriptor to replace
                    final List<PropertyDescriptor> descs = candidate.getDescriptors();
                    int index = -1;
                    for(int i=0,n=descs.size();i<n;i++){
                        final PropertyDescriptor pd = descs.get(i);
                        if(pd.getName().getLocalPart().equals(relation.currentColumn)){
                            index = i;
                        }
                    }

                    //create the new descriptor derivated
                    final PropertyDescriptor baseDescriptor = descs.get(index);
                    adb.reset();
                    adb.copy((AttributeDescriptor) baseDescriptor);
                    adb.setType(relType);
                    final PropertyDescriptor newDescriptor = adb.buildDescriptor();
                    candidate.changeProperty(index, newDescriptor);
                }

                //replace N:1 relations types-----------------------------------
                for(final RelationMetaModel relation : table.exportedKeys){
                    final String relCode = relation.foreignSchema +"."+relation.foreignTable;
                    final ComplexType relType = builded.get(relCode);

                    //find the descriptor to replace
                    final List<PropertyDescriptor> descs = candidate.getDescriptors();
                    int index = -1;
                    for(int i=0,n=descs.size();i<n;i++){
                        final PropertyDescriptor pd = descs.get(i);
                        if(pd.getName().getLocalPart().equals(relation.foreignTable)){
                            index = i;
                        }
                    }

                    //create the new descriptor derivated
                    final PropertyDescriptor baseDescriptor = descs.get(index);
                    adb.reset();
                    adb.copy((AttributeDescriptor) baseDescriptor);
                    adb.setType(relType);
                    final PropertyDescriptor newDescriptor = adb.buildDescriptor();
                    candidate.changeProperty(index, newDescriptor);
                }
                                
                //et parents ---------------------------------------------------
                final Collection<String> parents = table.parents;
                if(!parents.isEmpty()){
                    //we can only set one parent on feature types
                    //still better then nothing
                    final String parent = parents.iterator().next();
                    final TableMetaModel tmd = schema.getTable(parent);
                    if(tmd != null){
                        candidate.changeParent(tmd.complexType);
                    }
                }               
                
            }
        }

    }

}