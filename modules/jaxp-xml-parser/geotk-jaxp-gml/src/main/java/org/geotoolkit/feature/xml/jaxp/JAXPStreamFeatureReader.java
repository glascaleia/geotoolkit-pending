/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotoolkit.feature.xml.jaxp;

import com.vividsolutions.jts.geom.Geometry;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.geotoolkit.data.FeatureCollectionUtilities;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.JTSGeometry;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.internal.jaxb.ObjectFactory;
import org.geotoolkit.util.Converters;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JAXPStreamFeatureReader implements XmlFeatureReader {

    private static final Logger LOGGER = Logger.getLogger("org.geotoolkit.feature.xml.jaxp");
    
    private static MarshallerPool pool;
    static {
        try {
            pool = new MarshallerPool(ObjectFactory.class);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB Exception while initalizing the marshaller pool", ex);
        }
    }

    private FeatureType featureType;

     private SimpleFeatureBuilder builder;

    private final Unmarshaller unmarshaller;

    public JAXPStreamFeatureReader(FeatureType featureType) throws JAXBException {
         this.builder      = new SimpleFeatureBuilder((SimpleFeatureType) featureType);
         this.featureType  = featureType;
         this.unmarshaller = pool.acquireUnmarshaller();
    }

    @Override
    public Object read(String xml)  {
        try {
            XMLInputFactory XMLfactory = XMLInputFactory.newInstance();
            XMLfactory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);

            XMLStreamReader streamReader = XMLfactory.createXMLStreamReader(new StringReader(xml));
            return read(streamReader);
        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream initializing the event Reader: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public Object read(InputStream in) {
        try {
            XMLInputFactory XMLfactory = XMLInputFactory.newInstance();
            XMLfactory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);

            XMLStreamReader streamReader = XMLfactory.createXMLStreamReader(in);
            return read(streamReader);
        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream initializing the event Reader: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public Object read(Reader reader) {
        try {
            XMLInputFactory XMLfactory = XMLInputFactory.newInstance();
            XMLfactory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);

            XMLStreamReader streamReader = XMLfactory.createXMLStreamReader(reader);
            return read(streamReader);
        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream initializing the event Reader: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Start to read An object from the XML datasource.
     *
     * @param eventReader The XML event reader.
     * @return A feature or featureCollection described in the XML stream.
     */
    private Object read(XMLStreamReader streamReader) {
        try {
            while (streamReader.hasNext()) {
                int event = streamReader.next();


                //we are looking for the root mark
                if (event == XMLEvent.START_ELEMENT) {
                    
                    Name name  = Utils.getNameFromQname(streamReader.getName());
                    String id  = streamReader.getAttributeValue(0);

                    if (name.getLocalPart().equals("FeatureCollection")) {
                        return readFeatureCollection(streamReader, id);

                    } else if (featureType.getName().equals(name)) {
                        return readFeature(streamReader, id);
                    } else {
                        throw new IllegalArgumentException("The xml does not describte the same type of feature: \n " +
                                                           "Expected: " + featureType.getName() + '\n'                  +
                                                           "But was: "  + name);
                    }
                }
            }
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while reading the feature: " + ex.getMessage(), ex);
        }
        return null;
    }

    private Object readFeatureCollection(XMLStreamReader streamReader, String id) {
        FeatureCollection collection = FeatureCollectionUtilities.createCollection(id, (SimpleFeatureType) featureType);
        try {
            while (streamReader.hasNext()) {
                int event = streamReader.next();
               

                //we are looking for the root mark
                if (event == XMLEvent.START_ELEMENT) {
                    Name name  = Utils.getNameFromQname(streamReader.getName());

                    String fid = null;
                    if (streamReader.getAttributeCount() > 0) {
                        fid = streamReader.getAttributeValue(0);
                    }
                    
                    if (name.getLocalPart().equals("featureMember")) {
                        continue;

                    } else if (name.getLocalPart().equals("boundedBy")) {
                         while (streamReader.hasNext()) {
                            event = streamReader.next();
                            if (event == XMLEvent.START_ELEMENT) break;
                         }
                        String srsName = null;
                        if (streamReader.getAttributeCount() > 0) {
                            srsName = streamReader.getAttributeValue(0);
                        }
                        JTSEnvelope2D bounds = readBounds(streamReader, srsName);


                    } else if (featureType.getName().equals(name)) {
                        collection.add(readFeature(streamReader, fid));
                    } else {
                        throw new IllegalArgumentException("The xml does not describe the same type of feature: \n " +
                                                           "Expected: " + featureType.getName() + '\n'                  +
                                                           "But was: "  + name);
                    }
                }
            }
            return collection;
        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream exception while reading the feature: " + ex.getMessage());
        }
        return null;
    }

    public SimpleFeature readFeature(XMLStreamReader streamReader, String id) {
        builder.reset();
        String geometryName = featureType.getGeometryDescriptor().getName().getLocalPart();
        try {
            int nbAttribute         = 0;
            List<Object> values     = new ArrayList<Object>();

            while (streamReader.hasNext()) {
                int event = streamReader.next();

                if (event == XMLEvent.START_ELEMENT) {
                    nbAttribute++;
                    QName q                 = streamReader.getName();

                    if (!q.getLocalPart().equals(geometryName)) {
                        int contentEvent = streamReader.next();
                        if (contentEvent == XMLEvent.CHARACTERS) {
                            String content =streamReader.getText();
                            
                            PropertyDescriptor pdesc = featureType.getDescriptor(Utils.getNameFromQname(q));
                            if (pdesc != null) {
                                Class propertyType       = pdesc.getType().getBinding();
                                builder.set(q.getLocalPart(), Converters.convert(content, propertyType));
                            } else {
                                StringBuilder exp = new StringBuilder("expected ones are:").append('\n');
                                for (PropertyDescriptor pd : featureType.getDescriptors()) {
                                    exp.append(pd.getName().getLocalPart()).append('\n');
                                }
                                throw new IllegalArgumentException("unexpected attribute:" + q.getLocalPart() + '\n' + exp.toString());
                            }
                        } else {
                            LOGGER.severe("unexpected event");
                        }

                    } else {
                        event = streamReader.next();
                        while (event != XMLEvent.START_ELEMENT) {
                            event = streamReader.next();
                        }
                        
                        try {
                            JTSGeometry isoGeom = (JTSGeometry) ((JAXBElement)unmarshaller.unmarshal(streamReader)).getValue();
                            Geometry jtsGeom = isoGeom.getJTSGeometry();
                            builder.set(geometryName, jtsGeom);
                        } catch (JAXBException ex) {
                            LOGGER.severe("JAXB exception while reading the feature geometry: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }

                } else if (event == XMLEvent.END_ELEMENT) {
                    QName q             = streamReader.getName();
                    if (q.getLocalPart().equals("featureMember")) {
                        break;
                    }
                }
            }

            return builder.buildFeature(id);

            
        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream exception while reading the feature: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Extract An envelope from the BoundedBy XML mark of a feature collection.
     *
     * @param eventReader The XML event reader.
     * @param srsName The extracted CRS identifier.
     *
     * @return An envelope of the collection bounds.
     * @throws XMLStreamException
     */
    private JTSEnvelope2D readBounds(XMLStreamReader streamReader, String srsName) throws XMLStreamException {
       JTSEnvelope2D bounds = null;
       while (streamReader.hasNext()) {
            int event = streamReader.next();
            if (event == XMLEvent.END_ELEMENT) {
                QName endElement = streamReader.getName();
                if (endElement.getLocalPart().equals("boundedBy")) {
                    return null;
                }
            }

       }
        return bounds;
    }

    public void setFeatureType(FeatureType featureType) {
        this.featureType = featureType;
        this.builder     = new SimpleFeatureBuilder((SimpleFeatureType) featureType);
    }

    public void dispose() {
        pool.release(unmarshaller);
    }
}
