/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotoolkit.index.quadtree.fs;

import org.geotoolkit.index.quadtree.Node;
import com.vividsolutions.jts.geom.Envelope;
import java.io.IOException;
import org.geotoolkit.index.quadtree.DataReader;
import org.geotoolkit.index.Data;
import org.geotoolkit.index.DataDefinition;
import java.io.File;
import java.util.Iterator;
import org.geotoolkit.index.CloseableCollection;
import org.geotoolkit.index.DefaultData;
import org.geotoolkit.index.quadtree.QuadTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author jsorel
 */
public class FileSystemNodeTest {

    public static final DataDefinition DATA_DEFINITION = new DataDefinition("US-ASCII", Integer.class, Long.class);

    public FileSystemNodeTest() {
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

    /**
     * Just test it doesn't raise an error.
     * TODO : move tests from shapefile.
     */
    @Test
    public void testNoerror() throws Exception {
        final File file = new File("src/test/resources/org/geotoolkit/index/sample.qix");
        final FileSystemIndexStore store = new FileSystemIndexStore(file);

        final DataReader reader = new DataReader() {
            @Override
            public Data create(final int id) throws IOException {
                return new DefaultData(DATA_DEFINITION){
                    @Override
                    public String toString() {
                        return Integer.toString(id);
                    }
                };
            }
            @Override
            public void close() throws IOException {
            }
        };


        final QuadTree tree = store.load(reader);
        assertEquals(10,tree.getMaxDepth());
        assertEquals(new Envelope(-8.86966023318779,3.188061808903407,36.113981340792286,43.55971524165336),
                   tree.getRoot().getBounds(new Envelope()));
        assertEquals(3602,tree.getNumShapes());

        final Node root = tree.getRoot();
        System.out.println(root);

        for(int i=0;i<4;i++){
            root.getSubNode(i);
        }

        CloseableCollection col = tree.search(new Envelope(-8,3,37,40));
        Iterator ite = col.iterator();
        while(ite.hasNext()){
            System.out.println(ite.next());
        }

    }

}