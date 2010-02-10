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


package org.geotoolkit.data.session;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.memory.MemoryDataStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.Feature;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SessionTest extends TestCase{

    private static final FilterFactory FF = FactoryFinder.getFilterFactory(null);

    final MemoryDataStore store = new MemoryDataStore();


    public SessionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();

        //create the schema
        final Name name = new DefaultName("http://test.com", "TestSchema1");
        builder.reset();
        builder.setName(name);
        builder.add("string", String.class);
        builder.add("double", Double.class);
        builder.add("date", Date.class);
        final SimpleFeatureType type = builder.buildFeatureType();
        store.createSchema(name,type);
        final QueryBuilder qb = new QueryBuilder(name);

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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSessionReader() throws Exception {
        final Name name = store.getNames().iterator().next();
        final QueryBuilder qb = new QueryBuilder();
        Query query;
        
        //create an asynchrone session
        final Session session = store.createSession(true);


        //test simple reader with no modification ------------------------------
        qb.setTypeName(name);
        query = qb.buildQuery();

        assertEquals(store.getCount(query),3);
        assertEquals(session.getCount(query),3);
        assertFalse(session.hasPendingChanges());

        //test an iterator------------------------------------------------------
        FeatureIterator reader = session.getFeatureIterator(QueryBuilder.sorted(name, new SortBy[]{FF.sort("string", SortOrder.ASCENDING)}));
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

            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }

        //----------------------------------------------------------------------
        //test adding new features----------------------------------------------
        //----------------------------------------------------------------------
        final SimpleFeatureBuilder sfb = new SimpleFeatureBuilder((SimpleFeatureType) store.getFeatureType(name));
        sfb.set("string", "hop4");
        sfb.set("double", 2.5d);
        sfb.set("date", new Date(100L));

        session.addFeatures(name, Collections.singletonList(sfb.buildFeature("temporary")));

        //check that he new feature is available in the session but not in the datastore
        qb.reset();
        qb.setTypeName(name);
        query = qb.buildQuery();

        assertEquals(store.getCount(query),3);
        assertEquals(session.getCount(query),4);
        assertTrue(session.hasPendingChanges());

        reader = session.getFeatureIterator(QueryBuilder.filtered(name, FF.equals(FF.literal("hop4"), FF.property("string"))));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop4");
            assertEquals(sf.getAttribute("double"),2.5d);
            assertEquals(sf.getAttribute("date"),new Date(100L));

            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }

        //check that the sorting order has been preserve within the session

        reader = session.getFeatureIterator(QueryBuilder.sorted(name,new SortBy[]{FF.sort("double", SortOrder.ASCENDING)}));
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
            assertEquals(sf.getAttribute("double"),2.5d);

            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("double"),3d);

            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }


        //check that he new feature is available in the session and in the datastore
        session.commit();

        assertEquals(store.getCount(query),4);
        assertEquals(session.getCount(query),4);
        assertFalse(session.hasPendingChanges());

        //make a more deep test to find our feature
        reader = session.getFeatureIterator(QueryBuilder.filtered(name, FF.equals(FF.literal("hop4"), FF.property("string"))));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop4");
            assertEquals(sf.getAttribute("double"),2.5d);
            assertEquals(sf.getAttribute("date"),new Date(100L));

            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }

        reader = store.getFeatureReader(QueryBuilder.filtered(name, FF.equals(FF.literal("hop4"), FF.property("string"))));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop4");
            assertEquals(sf.getAttribute("double"),2.5d);
            assertEquals(sf.getAttribute("date"),new Date(100L));

            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }

        //----------------------------------------------------------------------
        //test removing feature-------------------------------------------------
        //----------------------------------------------------------------------

        //check that the feature exist
        reader = session.getFeatureIterator(QueryBuilder.filtered(name, FF.equals(FF.literal("hop4"), FF.property("string"))));
        try{
            SimpleFeature sf;
            reader.hasNext();
            sf = (SimpleFeature) reader.next();
            assertEquals(sf.getAttribute("string"),"hop4");
            assertEquals(sf.getAttribute("double"),2.5d);
            assertEquals(sf.getAttribute("date"),new Date(100L));

            assertFalse(reader.hasNext());
        }finally{
            reader.close();
        }

        assertFalse(session.hasPendingChanges());

        //remove the feature
        session.removeFeatures(name, FF.equals(FF.literal("hop4"), FF.property("string")));

        //check that the feature is removed in the session but not in the datastore
        qb.reset();
        qb.setTypeName(name);
        query = qb.buildQuery();

        assertEquals(store.getCount(query),4);
        assertEquals(session.getCount(query),3);
        assertTrue(session.hasPendingChanges());

        qb.reset();
        qb.setTypeName(name);
        qb.setFilter(FF.equals(FF.literal("hop4"), FF.property("string")));
        query = qb.buildQuery();

        assertEquals(store.getCount(query),1);
        assertEquals(session.getCount(query),0);

        //check that he new feature is available in the session and in the datastore
        session.commit();

        assertEquals(store.getCount(query),0);
        assertEquals(session.getCount(query),0);
        assertFalse(session.hasPendingChanges());

        //sanity check, count everything
        qb.reset();
        qb.setTypeName(name);
        query = qb.buildQuery();

        assertEquals(store.getCount(query),3);
        assertEquals(session.getCount(query),3);
        assertFalse(session.hasPendingChanges());


        //----------------------------------------------------------------------
        //test modifying feature------------------------------------------------
        //----------------------------------------------------------------------
        final Map<AttributeDescriptor,Object> values = new HashMap<AttributeDescriptor, Object>();
        values.put( ((SimpleFeatureType)store.getFeatureType(name)).getDescriptor("double"), 15d);

        session.updateFeatures(name, FF.equals(FF.property("double"), FF.literal(2d)), values);

        //check we have a modification
        qb.reset();
        qb.setTypeName(name);
        query = qb.buildQuery();

        assertEquals(store.getCount(query),3);
        assertEquals(session.getCount(query),3);
        assertTrue(session.hasPendingChanges());

        session.rollback();
        assertFalse(session.hasPendingChanges());


        //check that two modification doesnt conflict eachother
        qb.reset();
        qb.setTypeName(name);
        query = qb.buildQuery();
        AttributeDescriptor desc = ((SimpleFeatureType)store.getFeatureType(name)).getDescriptor("double");

        FeatureIterator ite = session.getFeatureIterator(query);
        FeatureId id1 = ite.next().getIdentifier();
        FeatureId id2 = ite.next().getIdentifier();
        ite.close();

        session.updateFeatures(name, FF.id(Collections.singleton(id1)), desc, 50d);
        session.updateFeatures(name, FF.id(Collections.singleton(id2)), desc, 100d);

        qb.reset();
        qb.setTypeName(name);
        qb.setFilter(FF.id(Collections.singleton(id1)));
        query = qb.buildQuery();

        ite = session.getFeatureIterator(query);
        int count = 0;
        while(ite.hasNext()){
            Feature f = ite.next();
            assertTrue(f.getIdentifier().equals(id1));
            assertTrue(f.getProperty("double").getValue().equals(50d));
            count++;
        }
        ite.close();

        assertEquals(1, count);


        qb.reset();
        qb.setTypeName(name);
        qb.setFilter(FF.id(Collections.singleton(id2)));
        query = qb.buildQuery();

        ite = session.getFeatureIterator(query);
        count = 0;
        while(ite.hasNext()){
            Feature f = ite.next();
            assertTrue(f.getIdentifier().equals(id2));
            assertTrue(f.getProperty("double").getValue().equals(100d));
            count++;
        }
        ite.close();

        assertEquals(1, count);


        //todo must handle count and envelope before testing more

        //check the modification is visible only in the session
//        qb.reset();
//        qb.setTypeName(name);
//        qb.setFilter(FF.equals(FF.property("double"), FF.literal(15d)));
//        query = qb.buildQuery();
//
//        assertEquals(store.getCount(query),0);
//        assertEquals(session.getCount(query),1);
//        assertTrue(session.hasPendingChanges());

        //check the modification is visible only in the session
//        qb.reset();
//        qb.setTypeName(name);
//        qb.setFilter(FF.equals(FF.property("double"), FF.literal(2d)));
//        query = qb.buildQuery();
//
//        assertEquals(store.getCount(query),1);
//        assertEquals(session.getCount(query),0);
//        assertTrue(session.hasPendingChanges());

    }

}