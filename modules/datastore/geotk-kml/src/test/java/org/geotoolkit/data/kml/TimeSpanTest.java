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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.kml.model.AbstractFeature;
import org.geotoolkit.data.kml.model.Kml;
import org.geotoolkit.data.kml.model.KmlException;
import org.geotoolkit.data.kml.model.Placemark;
import org.geotoolkit.data.kml.model.TimeSpan;
import org.geotoolkit.data.kml.xml.KmlReader;
import org.geotoolkit.data.kml.xml.KmlWriter;
import org.geotoolkit.temporal.object.FastDateParser;
import org.geotoolkit.xml.DomCompare;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import static org.junit.Assert.*;

/**
 *
 * @author Samuel Andrés
 */
public class TimeSpanTest {

    private static final double DELTA = 0.000000000001;
    private static final String pathToTestFile = "src/test/resources/org/geotoolkit/data/kml/timeSpan.kml";

    public TimeSpanTest() {
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
    public void timeSpanReadTest() throws IOException, XMLStreamException, ParseException {

        final KmlReader reader = new KmlReader();
        reader.setInput(new File(pathToTestFile));
        final Kml kmlObjects = reader.read();
        reader.dispose();

        final AbstractFeature feature = kmlObjects.getAbstractFeature();
        assertTrue(feature instanceof Placemark);
        Placemark placemark = (Placemark) feature;
        assertEquals("Colorado", placemark.getName());

        assertTrue(placemark.getTimePrimitive() instanceof TimeSpan);
        TimeSpan timeSpan = (TimeSpan) placemark.getTimePrimitive();
        String begin = "1876-08-02T22:31:54.543+01:00";

        FastDateParser du = new FastDateParser();
        Calendar calendarBegin = du.getCalendar(begin);
        assertEquals(calendarBegin, timeSpan.getBegin());
        assertEquals(begin, KmlUtilities.getFormatedString(calendarBegin,false));
    }

    @Test
    public void timeSpanWriteTest() throws KmlException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        final KmlFactory kmlFactory = new DefaultKmlFactory();

        Calendar begin = Calendar.getInstance();
        begin.set(Calendar.YEAR, 1876);
        begin.set(Calendar.MONTH, 7);
        begin.set(Calendar.DAY_OF_MONTH, 2);
        begin.set(Calendar.HOUR_OF_DAY, 22);
        begin.set(Calendar.MINUTE, 31);
        begin.set(Calendar.SECOND, 54);
        begin.set(Calendar.MILLISECOND, 543);
        begin.set(Calendar.ZONE_OFFSET, 3600000);

        TimeSpan timeSpan = kmlFactory.createTimeSpan();
        timeSpan.setBegin(begin);

        Placemark placemark = kmlFactory.createPlacemark();
        placemark.setName("Colorado");
        placemark.setTimePrimitive(timeSpan);
        final Kml kml = kmlFactory.createKml(null, placemark, null, null);

        File temp = File.createTempFile("timeSpanTest", ".kml");
        temp.deleteOnExit();

        KmlWriter writer = new KmlWriter();
        writer.setOutput(temp);
        writer.write(kml);
        writer.dispose();

        DomCompare.compare(
                new File(pathToTestFile), temp);

    }

}