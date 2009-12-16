/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotoolkit.map;

import java.io.IOException;
import junit.framework.TestCase;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.FeatureSource;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.memory.MemoryDataStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.query.QueryUtilities;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.feature.simple.SimpleFeatureTypeBuilder;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.style.DefaultStyleFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

/**
 *
 * @author sorel
 */
public class MapLayerTest extends TestCase{

    public MapLayerTest() {
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
    public void testContext() {

        try{
            MapBuilder.createContext(null);
            throw new IllegalArgumentException("Creating mapcontext with null crs shoudl raise an error");
        }catch(Exception ex){
            //ok
        }

        MapContext context = MapBuilder.createContext(DefaultGeographicCRS.WGS84);
        assertNotNull(context);

        assertNotNull(context.getCoordinateReferenceSystem());

    }

    @Test
    public void testLayer() throws IOException {

        try{
            MapBuilder.createFeatureLayer((FeatureSource<SimpleFeatureType, SimpleFeature>)null, null);
            throw new IllegalArgumentException("Creating maplayer with null source should raise an error");
        }catch(Exception ex){
            //ok
        }

        try{
            MapBuilder.createFeatureLayer((FeatureCollection<SimpleFeatureType, SimpleFeature>)null, null);
            throw new IllegalArgumentException("Creating maplayer with null source should raise an error");
        }catch(Exception ex){
            //ok
        }

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("test");

        DataStore ds = MemoryDataStore.create(builder.buildFeatureType());
        FeatureSource<SimpleFeatureType, SimpleFeature> fs = ds.getFeatureSource(ds.getTypeNames()[0]);


        FeatureMapLayer layer = MapBuilder.createFeatureLayer(fs, new DefaultStyleFactory().style());
        assertNotNull(layer);

        Query query = layer.getQuery();
        assertNotNull(query);
        assertTrue( QueryUtilities.queryAll(query) );

        try{
            layer.setQuery(null);
            throw new IllegalArgumentException("Can not set a null query");
        }catch(Exception ex){
            //ok
        }

        try{
            layer.setQuery(QueryBuilder.filtered(fs.getSchema().getName(), Filter.EXCLUDE));
        }catch(Exception ex){
            throw new IllegalArgumentException("Should be able to set this query");
        }

    }


}