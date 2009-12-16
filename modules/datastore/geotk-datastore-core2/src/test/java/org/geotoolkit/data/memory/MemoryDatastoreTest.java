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

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import junit.framework.TestCase;

import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MemoryDatastoreTest extends TestCase{

    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);

    public MemoryDatastoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateDataStore() throws IOException {
        MemoryDataStore store = new MemoryDataStore();
    }

    @Test
    public void testSchemas() throws IOException {
        final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        final MemoryDataStore store = new MemoryDataStore();
        Set<Name> names;

        names = store.getNames();
        assertEquals(names.size(), 0);

        //test creation of one schema ------------------------------------------
        Name name = new DefaultName("http://test.com", "TestSchema1");
        builder.reset();
        builder.setName(name);
        builder.add("att1", String.class);
        final SimpleFeatureType type1 = builder.buildFeatureType();

        store.createSchema(name,type1);

        names = store.getNames();
        assertEquals(names.size(), 1);
        Name n = names.iterator().next();

        assertEquals(n.getLocalPart(), "TestSchema1");
        assertEquals(n.getNamespaceURI(), "http://test.com");

        SimpleFeatureType t = (SimpleFeatureType) store.getSchema(n);
        assertEquals(t, type1);

        try{
            store.getSchema(new DefaultName("http://not", "exist"));
            throw new Exception("Asking for a schema that doesnt exist should have raised an error");
        }catch(Exception ex){
            //ok
        }

        //test update schema ---------------------------------------------------
        builder.reset();
        builder.setName("http://test.com", "TestSchema1");
        builder.add("att1", String.class);
        builder.add("att2", Double.class);
        SimpleFeatureType type2 = builder.buildFeatureType();

        store.updateSchema(name, type2);

        names = store.getNames();
        assertEquals(names.size(), 1);
        n = names.iterator().next();

        assertEquals(n.getLocalPart(), "TestSchema1");
        assertEquals(n.getNamespaceURI(), "http://test.com");

        t = (SimpleFeatureType) store.getSchema(n);
        assertEquals(t, type2);


        try{
            store.updateSchema(new DefaultName("http://not", "exist"),type2);
            throw new Exception("Updating a schema that doesnt exist should have raised an error");
        }catch(Exception ex){
            //ok
        }

        //test delete schema ---------------------------------------------------

        names = store.getNames();
        assertEquals(names.size(), 1);

        store.deleteSchema(name);

        names = store.getNames();
        assertEquals(names.size(), 0);

        try{
            store.deleteSchema(new DefaultName("http://not", "exist"));
            throw new Exception("Deleting a schema that doesnt exist should have raised an error");
        }catch(Exception ex){
            //ok
        }

    }

    @Test
    public void testFeatures() throws IOException {
        final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        final MemoryDataStore store = new MemoryDataStore();

        //create the schema
        Name name = new DefaultName("http://test.com", "TestSchema1");
        builder.reset();
        builder.setName(name);
        builder.add("att1", String.class);
        final SimpleFeatureType type = builder.buildFeatureType();
        store.createSchema(name,type);


        //test reader with no features in datastore
        FeatureReader reader = store.getFeatureReader(QueryBuilder.all(name));
        int count = 0;
        try{
            while(reader.hasNext()){
                reader.next();
                count++;
            }
        }finally{
            reader.close();
        }
        assertEquals(count, 0);
        assertEquals(store.getCount(QueryBuilder.all(name)), 0);



        //test a non existant type
        try{
            FeatureReader reader2 = store.getFeatureReader(QueryBuilder.all(new DefaultName("http://not", "exist")));
            throw new Exception("Deleting a schema that doesnt exist should have raised an error");
        }catch(Exception ex){
            //ok
        }

        //create a few features
        FeatureWriter writer = store.getFeatureWriterAppend(name);
        try{
            for(int i=0;i<10;i++){
                SimpleFeature f = (SimpleFeature) writer.next();
                f.setAttribute("att1", "hop"+i);
                writer.write();
            }
        }finally{
            writer.close();
        }

        //check that we really have 10 features now
        reader = store.getFeatureReader(QueryBuilder.all(name));
        count = 0;
        try{
            while(reader.hasNext()){
                SimpleFeature f = (SimpleFeature) reader.next();
                assertEquals(f.getAttribute("att1"),"hop"+count);
                count++;
            }
        }finally{
            reader.close();
        }
        assertEquals(count, 10);
        assertEquals(store.getCount(QueryBuilder.all(name)), 10);

        //check updating features
        writer = store.getFeatureWriter(name, org.opengis.filter.Filter.INCLUDE);
        count = 0;
        try{
            while(writer.hasNext()){
                SimpleFeature f = (SimpleFeature) writer.next();
                f.setAttribute("att1", "hop"+count*count);
                writer.write();
                count++;
            }
        }finally{
            writer.close();
        }

        //check that all 10 features where updated
        reader = store.getFeatureReader(QueryBuilder.all(name));
        count = 0;
        try{
            while(reader.hasNext()){
                SimpleFeature f = (SimpleFeature) reader.next();
                assertEquals(f.getAttribute("att1"),"hop"+(count*count));
                count++;
            }
        }finally{
            reader.close();
        }
        assertEquals(count, 10);
        assertEquals(store.getCount(QueryBuilder.all(name)), 10);

        //check deleting features
        writer = store.getFeatureWriter(name, org.opengis.filter.Filter.INCLUDE);
        try{
            while(writer.hasNext()){
                SimpleFeature f = (SimpleFeature) writer.next();
                writer.remove();
            }
        }finally{
            writer.close();
        }

        reader = store.getFeatureReader(QueryBuilder.all(name));
        count = 0;
        try{
            while(reader.hasNext()){
                reader.next();
                count++;
            }
        }finally{
            reader.close();
        }
        assertEquals(count, 0);
        assertEquals(store.getCount(QueryBuilder.all(name)), 0);
    }

    @Test
    public void testQuerySupport() throws IOException {
        final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        final MemoryDataStore store = new MemoryDataStore();

        //create the schema
        final Name name = new DefaultName("http://test.com", "TestSchema1");
        builder.reset();
        builder.setName(name);
        builder.add("string", String.class);
        builder.add("double", Double.class);
        builder.add("date", Date.class);
        final SimpleFeatureType type = builder.buildFeatureType();
        store.createSchema(name,type);

        //create a few features
        FeatureWriter writer = store.getFeatureWriterAppend(name);
        try{
            SimpleFeature f = (SimpleFeature) writer.next();
            f.setAttribute("string", "hop3");
            f.setAttribute("double", 3d);
            f.setAttribute("date", new Date(1000L));
            writer.write();

            f = (SimpleFeature) writer.next();
            f.setAttribute("string", "hop1");
            f.setAttribute("double", 1d);
            f.setAttribute("date", new Date(100000L));
            writer.write();

            f = (SimpleFeature) writer.next();
            f.setAttribute("string", "hop2");
            f.setAttribute("double", 2d);
            f.setAttribute("date", new Date(10000L));
            writer.write();

        }finally{
            writer.close();
        }

        //quick count check
        FeatureReader reader = store.getFeatureReader(QueryBuilder.all(name));
        int count = 0;
        try{
            while(reader.hasNext()){
                reader.next();
                count++;
            }
        }finally{
            reader.close();
        }
        assertEquals(count, 3);
        assertEquals(store.getCount(QueryBuilder.all(name)), 3);


        //ASCENDING ORDER ------------------------------------------------------

        //test sort by on string
        reader = store.getFeatureReader(QueryBuilder.sorted(name, new SortBy[]{FF.sort("string", SortOrder.ASCENDING)}));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop1");
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop2");
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop3");
        }finally{
            reader.close();
        }

        //test sort by on double
        reader = store.getFeatureReader(QueryBuilder.sorted(name, new SortBy[]{FF.sort("double", SortOrder.ASCENDING)}));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),1d);
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),2d);
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),3d);
        }finally{
            reader.close();
        }

        //test sort by on date
        reader = store.getFeatureReader(QueryBuilder.sorted(name, new SortBy[]{FF.sort("date", SortOrder.ASCENDING)}));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("date"),new Date(1000L));
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("date"),new Date(10000L));
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("date"),new Date(100000L));
        }finally{
            reader.close();
        }

        //DESCENDING ORDER ------------------------------------------------------

        //test sort by on string
        reader = store.getFeatureReader(QueryBuilder.sorted(name, new SortBy[]{FF.sort("string", SortOrder.DESCENDING)}));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop3");
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop2");
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop1");
        }finally{
            reader.close();
        }

        //test sort by on double
        reader = store.getFeatureReader(QueryBuilder.sorted(name, new SortBy[]{FF.sort("double", SortOrder.DESCENDING)}));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),3d);
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),2d);
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),1d);
        }finally{
            reader.close();
        }

        //test sort by on date
        reader = store.getFeatureReader(QueryBuilder.sorted(name, new SortBy[]{FF.sort("date", SortOrder.DESCENDING)}));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("date"),new Date(100000L));
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("date"),new Date(10000L));
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("date"),new Date(1000L));
        }finally{
            reader.close();
        }


        //TEST FILTER ----------------------------------------------------------
        //test on date
        Filter filter = FF.equals(FF.property("date"), FF.literal(new Date(10000L)));
        Query query = QueryBuilder.filtered(name,filter);
        assertEquals(store.getCount(query),1);
        
        reader = store.getFeatureReader(query);
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("date"),new Date(10000L));
            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }

        //test on double
        filter = FF.equals(FF.property("double"), FF.literal(2d));
        query = QueryBuilder.filtered(name,filter);
        assertEquals(store.getCount(query),1);

        reader = store.getFeatureReader(query);
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),2d);
            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }

        //test on string
        filter = FF.equals(FF.property("string"), FF.literal("hop1"));
        query = QueryBuilder.filtered(name,filter);
        assertEquals(store.getCount(query),1);

        reader = store.getFeatureReader(query);
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop1");
            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }


        

    }
    
}