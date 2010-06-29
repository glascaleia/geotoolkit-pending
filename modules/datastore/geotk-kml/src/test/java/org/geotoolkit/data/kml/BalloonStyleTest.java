package org.geotoolkit.data.kml;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.kml.model.AbstractFeature;
import org.geotoolkit.data.kml.model.AbstractStyleSelector;
import org.geotoolkit.data.kml.model.BalloonStyle;
import org.geotoolkit.data.kml.model.Coordinate;
import org.geotoolkit.data.kml.model.Coordinates;
import org.geotoolkit.data.kml.model.Document;
import org.geotoolkit.data.kml.model.IdAttributes;
import org.geotoolkit.data.kml.model.Kml;
import org.geotoolkit.data.kml.model.KmlException;
import org.geotoolkit.data.kml.model.Placemark;
import org.geotoolkit.data.kml.model.Point;
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
public class BalloonStyleTest {

    private static final double DELTA = 0.000000000001;
    private static final String pathToTestFile = "src/test/resources/org/geotoolkit/data/kml/balloonStyle.kml";

    public BalloonStyleTest() {
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
    public void balloonStyleReadTest() throws IOException, XMLStreamException {

        final KmlReader reader = new KmlReader();
        reader.setInput(new File(pathToTestFile));
        final Kml kmlObjects = reader.read();
        reader.dispose();

        final AbstractFeature feature = kmlObjects.getAbstractFeature();
        assertTrue(feature instanceof Document);
        final Document document = (Document) feature;
        assertEquals("BalloonStyle.kml", document.getName());
        assertTrue(document.getOpen());
        assertEquals(1, document.getStyleSelectors().size());
        assertTrue(document.getStyleSelectors().get(0) instanceof Style);
        Style style = (Style) document.getStyleSelectors().get(0);
        assertEquals("exampleBalloonStyle", style.getIdAttributes().getId());
        BalloonStyle balloonStyle = style.getBalloonStyle();
        assertEquals(new Color(187, 255, 255, 255), balloonStyle.getBgColor());
        String text = //"<![CDATA["+
"\n      <b><font color=\"#CC0000\" size=\"+3\">$[name]</font></b>\n"+
"      <br/><br/>\n"+
"      <font face=\"Courier\">$[description]</font>\n"+
"      <br/><br/>\n"+
"      Extra text that will appear in the description balloon\n"+
"      <br/><br/>\n"+
"      $[geDirections]\n"+
"      ";//]]>";

        //text= "salut";

        assertEquals(text, balloonStyle.getText());

        assertEquals(1, document.getAbstractFeatures().size());
        assertTrue(document.getAbstractFeatures().get(0) instanceof Placemark);
        Placemark placemark = (Placemark) document.getAbstractFeatures().get(0);
        assertEquals("BalloonStyle", placemark.getName());
        assertEquals("An example of BalloonStyle", placemark.getDescription());
        assertEquals("#exampleBalloonStyle",placemark.getStyleUrl());

        assertTrue(placemark.getAbstractGeometry() instanceof Point);
        final Point point = (Point) placemark.getAbstractGeometry();
        Coordinates coordinates = point.getCoordinates();
        assertEquals(1, coordinates.getCoordinates().size());
        Coordinate coordinate = coordinates.getCoordinate(0);
        assertEquals(-122.370533, coordinate.getGeodeticLongitude(), DELTA);
        assertEquals(37.823842, coordinate.getGeodeticLatitude(), DELTA);
        assertEquals(0, coordinate.getAltitude(), DELTA);

    }

    @Test
    public void balloonStyleWriteTest() throws KmlException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        final KmlFactory kmlFactory = new DefaultKmlFactory();

        Coordinate coordinate = kmlFactory.createCoordinate(-122.370533,37.823842,0.0);
        Coordinates coordinates = kmlFactory.createCoordinates(Arrays.asList(coordinate));
        Point point = kmlFactory.createPoint();
        point.setCoordinates(coordinates);

        Placemark placemark = kmlFactory.createPlacemark();
        placemark.setName("BalloonStyle");
        placemark.setDescription("An example of BalloonStyle");
        placemark.setStyleUrl("#exampleBalloonStyle");
        placemark.setAbstractGeometry(point);

        BalloonStyle balloonStyle = kmlFactory.createBalloonStyle();
        String text = //"<![CDATA["+
"\n      <b><font color=\"#CC0000\" size=\"+3\">$[name]</font></b>\n"+
"      <br/><br/>\n"+
"      <font face=\"Courier\">$[description]</font>\n"+
"      <br/><br/>\n"+
"      Extra text that will appear in the description balloon\n"+
"      <br/><br/>\n"+
"      $[geDirections]\n"+
"      ";//]]>";
        balloonStyle.setText(text);
        balloonStyle.setBgColor(new Color(187, 255, 255, 255));

        IdAttributes idAttributes = kmlFactory.createIdAttributes("exampleBalloonStyle", null);

        Style style = kmlFactory.createStyle();
        style.setIdAttributes(idAttributes);
        style.setBalloonStyle(balloonStyle);

        Document document = kmlFactory.createDocument();
        document.setName("BalloonStyle.kml");
        document.setOpen(true);
        document.setStyleSelectors(Arrays.asList((AbstractStyleSelector) style));
        document.setAbstractFeatures(Arrays.asList((AbstractFeature) placemark));

        final Kml kml = kmlFactory.createKml(null, document, null, null);

        File temp = File.createTempFile("testBalloonStyle", ".kml");
        temp.deleteOnExit();

        KmlWriter writer = new KmlWriter();
        writer.setOutput(temp);
        writer.write(kml);
        writer.dispose();

        DomCompare.compare(
                new File(pathToTestFile), temp);

    }
}