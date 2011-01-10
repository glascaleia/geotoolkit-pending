/*
 *    GeotoolKit - An Open source Java GIS Toolkit
 *    http://geotoolkit.org
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
package org.geotoolkit.index.rtree.fs;

import junit.framework.TestCase;
import org.geotoolkit.index.DataDefinition;
import org.geotoolkit.index.TreeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

/**
 * DOCUMENT ME!
 * 
 * @author Tommaso Nolli
 * @module pending
 */
public class FileSystemPageStoreTest extends TestCase {
    /**
     * Constructor for FileSystemPageStoreTest.
     * 
     * @param arg0
     */
    public FileSystemPageStoreTest(final String arg0) {
        super(arg0);
    }

    /*
     * Test for void FileSystemPageStore(File)
     */
    public void testFileSystemPageStoreFile() throws Exception {
        File file = File.createTempFile("geotoolkit2e", ".grx");
        file.deleteOnExit();

        try {
            new FileSystemPageStore(file);
            fail("Cannot create a FileSystemPageStore without a "
                    + "DataDefinition");
        } catch (TreeException e) {
            // Ok, the file must exist
        }
    }

    /*
     * Test for void FileSystemPageStore(File, DataDefinition)
     */
    public void testFileSystemPageStoreFileDataDefinition() throws Exception {
        File file = File.createTempFile("geotoolkitf2", ".grx");
        file.deleteOnExit();
        DataDefinition dd = new DataDefinition("US-ASCII");

        try {
            new FileSystemPageStore(file, dd);
            fail("Cannot use an empty DataDefinition");
        } catch (TreeException e) {
            // OK
        }

        dd.addField(Integer.class);

        FileSystemPageStore fps = new FileSystemPageStore(file, dd);
        fps.close();
    }

    /*
     * Test for void FileSystemPageStore(File, DataDefinition, int, int, short)
     */
    public void testFileSystemPageStoreFileDataDefinitionintintshort()
            throws Exception {
        File file = File.createTempFile("geotoolkit2g", ".grx");
        file.deleteOnExit();
        DataDefinition dd = new DataDefinition("US-ASCII");
        dd.addField(Integer.class);

        FileSystemPageStore fps = null;

        try {
            fps = new FileSystemPageStore(file, dd, 10, 10,
                    FileSystemPageStore.SPLIT_LINEAR);
            fail("MinNodeEntries must be <= MaxNodeEntries / 2");
        } catch (TreeException e) {
            // OK
        }

        try {
            fps = new FileSystemPageStore(file, dd, 10, 5, (short) 1000);
            fail("SplitAlgorithm not supported");
        } catch (TreeException e) {
            // OK
        }

        fps = new FileSystemPageStore(file, dd, 50, 25,
                FileSystemPageStore.SPLIT_QUADRATIC);
        fps.close();

        // write garbage into the file
        OutputStream out = new FileOutputStream(file);
        byte[] bytes = new byte[50];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        out.write(bytes);
        out.close();

        try {
            fps = new FileSystemPageStore(file, dd, 10, 5,
                    FileSystemPageStore.SPLIT_QUADRATIC);
            fail("File must not exist");
        } catch (TreeException e) {
            // OK
        }
    }
}
