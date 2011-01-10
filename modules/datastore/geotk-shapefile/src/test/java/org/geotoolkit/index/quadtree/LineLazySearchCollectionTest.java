/*
 *    GeotoolKit - An Open source Java GIS Toolkit
 *    http://geotoolkit.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.index.quadtree;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.geotoolkit.data.shapefile.ShpFiles;
import org.geotoolkit.data.shapefile.AbstractTestCaseSupport;
import org.geotoolkit.data.shapefile.indexed.IndexDataReader;
import org.geotoolkit.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotoolkit.data.shapefile.shx.ShxReader;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.index.quadtree.fs.FileSystemIndexStore;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Jesse
 * @module pending
 */
public class LineLazySearchCollectionTest extends AbstractTestCaseSupport {

    private File file;
    private IndexedShapefileDataStore ds;
    private QuadTree tree;
    private DataReader dr;
    private Iterator iterator;
    private CoordinateReferenceSystem crs;

    public LineLazySearchCollectionTest() throws IOException {
        super("LazySearchIteratorTest");
    }

    protected void setUp() throws Exception {
        super.setUp();
        file = copyShapefiles("shapes/streams.shp");
        ds = new IndexedShapefileDataStore(file.toURL());
        ds.buildQuadTree(0);
        final Object[] v = openQuadTree(file);
        tree = (QuadTree) v[0];
        dr = (DataReader) v[1];
        crs = ds.getFeatureType(ds.getNames().iterator().next()).getCoordinateReferenceSystem();
    }

    public static Object[] openQuadTree(final File file) throws StoreException {
        File qixFile = sibling(file, "qix");
        FileSystemIndexStore store = new FileSystemIndexStore(qixFile);
        try {
            ShpFiles shpFiles = new ShpFiles(qixFile);

            ShxReader indexFile = new ShxReader(shpFiles, false);
            DataReader dr = new IndexDataReader(indexFile);
            return new Object[]{store.load(),dr};

        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    protected void tearDown() throws Exception {
        if (iterator != null)
            tree.close(iterator);
        tree.close();
        super.tearDown();
        file.getParentFile().delete();
    }

    public void testGetAllFeatures() throws Exception {
        JTSEnvelope2D env = new JTSEnvelope2D(585000, 610000,
                4910000, 4930000, crs);
        LazySearchCollection collection = new LazySearchCollection(tree,dr, env);
        assertEquals(116, collection.size());
    }

    public void testGetOneFeatures() throws Exception {
        JTSEnvelope2D env = new JTSEnvelope2D(588993, 589604,
                4927443, 4927443, crs);
        LazySearchCollection collection = new LazySearchCollection(tree,dr, env);
        assertEquals(33, collection.size());

    }

    public void testGetNoFeatures() throws Exception {
        JTSEnvelope2D env = new JTSEnvelope2D(592211, 597000,
                4910947, 4913500, crs);
        LazySearchCollection collection = new LazySearchCollection(tree,dr, env);
        assertEquals(0, collection.size());
    }
}
