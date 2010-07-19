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

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.kml.model.AbstractFeature;
import org.geotoolkit.data.kml.model.AbstractStyleSelector;
import org.geotoolkit.data.kml.model.Coordinates;
import org.geotoolkit.data.kml.model.Document;
import org.geotoolkit.data.kml.model.IdAttributes;
import org.geotoolkit.data.kml.model.Kml;
import org.geotoolkit.data.kml.model.KmlException;
import org.geotoolkit.data.kml.model.LineString;
import org.geotoolkit.data.kml.model.LineStyle;
import org.geotoolkit.data.kml.model.Placemark;
import org.geotoolkit.data.kml.model.Style;
import org.geotoolkit.data.kml.xml.KmlReader;
import org.geotoolkit.data.kml.xml.KmlWriter;
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
public class LineStyleTest {

    private static final double DELTA = 0.000000000001;
    private static final String pathToTestFile = "src/test/resources/org/geotoolkit/data/kml/lineStyle.kml";

    public LineStyleTest() {
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
    public void lineStyleReadTest() throws IOException, XMLStreamException, URISyntaxException {

        final KmlReader reader = new KmlReader();
        reader.setInput(new File(pathToTestFile));
        final Kml kmlObjects = reader.read();
        reader.dispose();

        final AbstractFeature feature = kmlObjects.getAbstractFeature();
        assertTrue(feature instanceof Document);
        final Document document = ((Document) feature);
        assertEquals("LineStyle.kml", document.getFeatureName());
        assertTrue(document.getOpen());

        List<AbstractStyleSelector> styleSelectors = document.getStyleSelectors();
        assertEquals(1, styleSelectors.size());

        assertTrue(styleSelectors.get(0) instanceof Style);
        Style style = (Style) styleSelectors.get(0);
        assertEquals("linestyleExample", style.getIdAttributes().getId());
            LineStyle lineStyle = style.getLineStyle();
            assertEquals(new Color(255, 0, 0, 127), lineStyle.getColor());
            assertEquals(4, lineStyle.getWidth(), DELTA);

        assertEquals(1, document.getAbstractFeatures().size());
        assertTrue(document.getAbstractFeatures().get(0) instanceof Placemark);
        Placemark placemark = (Placemark) document.getAbstractFeatures().get(0);
        assertEquals("LineStyle Example", placemark.getFeatureName());
        assertEquals(new URI("#linestyleExample"), placemark.getStyleUrl());
        assertTrue(placemark.getAbstractGeometry() instanceof LineString);
        LineString lineString = (LineString) placemark.getAbstractGeometry();
        assertTrue(lineString.getExtrude());
        assertTrue(lineString.getTessellate());
        Coordinates coordinates = lineString.getCoordinateSequence();
        assertEquals(2, coordinates.size());
        Coordinate coordinate0 = coordinates.getCoordinate(0);
        assertEquals(-122.364383, coordinate0.x, DELTA);
        assertEquals(37.824664, coordinate0.y, DELTA);
        assertEquals(0, coordinate0.z, DELTA);
        Coordinate coordinate1 = coordinates.getCoordinate(1);
        assertEquals(-122.364152, coordinate1.x, DELTA);
        assertEquals(37.824322, coordinate1.y, DELTA);
        assertEquals(0, coordinate1.z, DELTA);
    }

    @Test
    public void lineStyleWriteTest() throws KmlException, IOException, XMLStreamException, ParserConfigurationException, SAXException, URISyntaxException {
        final KmlFactory kmlFactory = new DefaultKmlFactory();

        Coordinate coordinate0 = kmlFactory.createCoordinate(-122.364383, 37.824664, 0);
        Coordinate coordinate1 = kmlFactory.createCoordinate(-122.364152, 37.824322, 0);
        Coordinates coordinates = kmlFactory.createCoordinates(Arrays.asList(coordinate0, coordinate1));
        LineString lineString = kmlFactory.createLineString(coordinates);
        lineString.setTessellate(true);
        lineString.setExtrude(true);

        Placemark placemark = kmlFactory.createPlacemark();
        placemark.setFeatureName("LineStyle Example");
        placemark.setStyleUrl(new URI("#linestyleExample"));
        placemark.setAbstractGeometry(lineString);

        Style style = kmlFactory.createStyle();
            LineStyle lineStyle = kmlFactory.createLineStyle();
            lineStyle.setWidth(4);
            lineStyle.setColor(new Color(255, 0, 0, 127));
        style.setLineStyle(lineStyle);
        IdAttributes idAttributes = kmlFactory.createIdAttributes("linestyleExample", null);
        style.setIdAttributes(idAttributes);

        Document document = kmlFactory.createDocument();
        document.setStyleSelectors(Arrays.asList((AbstractStyleSelector) style));
        document.setAbstractFeatures(Arrays.asList((AbstractFeature) placemark));
        document.setFeatureName("LineStyle.kml");
        document.setOpen(true);

        final Kml kml = kmlFactory.createKml(null, document, null, null);

        File temp = File.createTempFile("testLineStyle", ".kml");
        temp.deleteOnExit();

        KmlWriter writer = new KmlWriter();
        writer.setOutput(temp);
        writer.write(kml);
        writer.dispose();

        DomCompare.compare(
                new File(pathToTestFile), temp);

    }
}