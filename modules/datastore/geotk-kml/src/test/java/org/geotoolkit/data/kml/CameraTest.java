/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.data.kml;

import org.geotoolkit.data.kml.xml.KmlReader;
import org.geotoolkit.data.kml.xml.KmlWriter;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.kml.model.AbstractFeature;
import org.geotoolkit.data.kml.model.AbstractView;
import org.geotoolkit.data.kml.model.EnumAltitudeMode;
import org.geotoolkit.data.kml.model.Camera;
import org.geotoolkit.data.kml.model.Kml;
import org.geotoolkit.data.kml.model.KmlException;
import org.geotoolkit.data.kml.model.PhotoOverlay;
import org.geotoolkit.xml.DomCompare;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;
import static org.geotoolkit.data.kml.xml.KmlConstants.*;

/**
 *
 * @author Samuel Andrés
 */
public class CameraTest {

    private static final double DELTA = 0.000000000001;
    private static final String pathToTestFile = "src/test/resources/org/geotoolkit/data/kml/camera.kml";

    public CameraTest() {
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
    public void cameraReadTest() throws IOException, XMLStreamException {

        final KmlReader reader = new KmlReader();
        reader.setInput(new File(pathToTestFile));
        final Kml kmlObjects = reader.read();
        reader.dispose();

        final AbstractFeature feature = kmlObjects.getAbstractFeature();
        assertTrue(feature instanceof PhotoOverlay);
        final AbstractView view = ((PhotoOverlay) feature).getView();
        assertTrue(view instanceof Camera);

        final Camera camera = (Camera) view;
        assertEquals(4, camera.getLongitude(), DELTA);
        assertEquals(43, camera.getLatitude(), DELTA);
        assertEquals(625, camera.getAltitude(), DELTA);
        assertEquals(2, camera.getHeading(), DELTA);
        assertEquals(1, camera.getTilt(), DELTA);
        assertEquals(2, camera.getRoll(), DELTA);
        assertEquals(EnumAltitudeMode.RELATIVE_TO_GROUND, camera.getAltitudeMode());

    }

    @Test
    public void cameraWriteTest() throws KmlException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        final KmlFactory kmlFactory = new DefaultKmlFactory();

        double longitude = 4;
        double latitude = 43;
        double altitude = 625;
        double heading = 2;
        double tilt = 1.0;
        double roll = 2.0;

        final Camera camera = kmlFactory.createCamera();
        camera.setLongitude(longitude);
        camera.setLatitude(latitude);
        camera.setAltitude(altitude);
        camera.setHeading(heading);
        camera.setTilt(tilt);
        camera.setRoll(roll);
        camera.setAltitudeMode(EnumAltitudeMode.RELATIVE_TO_GROUND);
        final PhotoOverlay photoOverlay = kmlFactory.createPhotoOverlay();
        photoOverlay.setView(camera);
        photoOverlay.setVisibility(DEF_VISIBILITY);
        photoOverlay.setOpen(DEF_OPEN);
        photoOverlay.setDrawOrder(DEF_DRAW_ORDER);
        photoOverlay.setRotation(DEF_ROTATION);
        final Kml kml = kmlFactory.createKml(null, photoOverlay, null, null);

        File temp = File.createTempFile("testCamera", ".kml");
        temp.deleteOnExit();

        KmlWriter writer = new KmlWriter();
        writer.setOutput(temp);
        writer.write(kml);
        writer.dispose();

        DomCompare.compare(
                new File(pathToTestFile), temp);

    }
}
