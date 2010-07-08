package org.geotoolkit.data.kml;

import com.vividsolutions.jts.geom.Coordinate;
import java.net.URISyntaxException;
import org.geotoolkit.data.kml.xml.KmlReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.kml.model.AbstractObject;
import org.geotoolkit.data.kml.model.Change;
import org.geotoolkit.data.kml.model.Coordinates;
import org.geotoolkit.data.kml.model.Kml;
import org.geotoolkit.data.kml.model.KmlException;
import org.geotoolkit.data.kml.model.NetworkLinkControl;
import org.geotoolkit.data.kml.model.Point;
import org.geotoolkit.data.kml.model.Update;
import org.geotoolkit.data.kml.xml.KmlWriter;
import org.geotoolkit.xml.DomCompare;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés
 */
public class ChangeTest {

    private static final double DELTA = 0.000000000001;
    private static final String pathToTestFile = "src/test/resources/org/geotoolkit/data/kml/change.kml";

    public ChangeTest() {
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
    public void changeReadTest() throws IOException, XMLStreamException {

        final KmlReader reader = new KmlReader();
        reader.setInput(new File(pathToTestFile));
        final Kml kmlObjects = reader.read();
        reader.dispose();

        final NetworkLinkControl networkLinkControl = kmlObjects.getNetworkLinkControl();
        final Update update = networkLinkControl.getUpdate();
        final URI targetHref = update.getTargetHref();
        assertEquals("http://www/~sam/January14Data/Point.kml", targetHref.toString());

        assertEquals(1, update.getUpdates().size());
        assertTrue(update.getUpdates().get(0) instanceof Change);
        Change change = (Change) update.getUpdates().get(0);

        assertEquals(1, change.getObjects().size());
        assertTrue(change.getObjects().get(0) instanceof Point);

        Point point = (Point) change.getObjects().get(0);
        point.getIdAttributes();
        assertEquals("point123", point.getIdAttributes().getTargetId());

        Coordinates coordinates = point.getCoordinateSequence();
        assertEquals(1, point.getCoordinateSequence().size());
        Coordinate coordinate = point.getCoordinateSequence().getCoordinate(0);
        assertEquals(-95.48, coordinate.x, DELTA);
        assertEquals(40.43, coordinate.y, DELTA);
        assertEquals(0, coordinate.z, DELTA);

    }

    @Test
    public void changeWriteTest() throws KmlException, IOException, XMLStreamException, ParserConfigurationException, SAXException, URISyntaxException {
        final KmlFactory kmlFactory = new DefaultKmlFactory();

        Coordinate coordinate = kmlFactory.createCoordinate(-95.48, 40.43, 0);
        Coordinates coordinates = kmlFactory.createCoordinates(Arrays.asList(coordinate));

        Point point = kmlFactory.createPoint(coordinates);
        point.setIdAttributes(kmlFactory.createIdAttributes(null, "point123"));

        Change change = kmlFactory.createChange(Arrays.asList((AbstractObject) point));

        Update update = kmlFactory.createUpdate();
        update.setUpdates(Arrays.asList((Object) change));
        update.setTargetHref(new URI("http://www/~sam/January14Data/Point.kml"));

        NetworkLinkControl networkLinkControl = kmlFactory.createNetworkLinkControl();
        networkLinkControl.setUpdate(update);


        final Kml kml = kmlFactory.createKml(networkLinkControl, null, null, null);

        File temp = File.createTempFile("testChange", ".kml");
        temp.deleteOnExit();

        KmlWriter writer = new KmlWriter();
        writer.setOutput(temp);
        writer.write(kml);
        writer.dispose();

        DomCompare.compare(
                new File(pathToTestFile), temp);

    }
}
