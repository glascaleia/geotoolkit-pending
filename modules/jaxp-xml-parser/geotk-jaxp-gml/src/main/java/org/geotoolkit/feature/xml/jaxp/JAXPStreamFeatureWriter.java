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

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.collection.FeatureIterator;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.geometry.isoonjts.JTSUtils;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.xml.Namespaces;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Geometry;
import org.opengis.referencing.FactoryException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JAXPStreamFeatureWriter extends JAXPFeatureWriter {

    public JAXPStreamFeatureWriter() throws JAXBException {
        super();
    }

     @Override
    public void write(SimpleFeature sf, Writer writer) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(writer);

            // the XML header
            streamWriter.writeStartDocument("UTF-8", "1.0");

            writeFeature(sf, streamWriter, true);

            streamWriter.flush();
            streamWriter.close();

        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream exception while writing the feature: " + ex.getMessage());
        }
    }

    @Override
    public void write(SimpleFeature sf, OutputStream out) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(out);

            // the XML header
            streamWriter.writeStartDocument("UTF-8", "1.0");

            writeFeature(sf, streamWriter, true);

            streamWriter.flush();
            streamWriter.close();

        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream exception while writing the feature: " + ex.getMessage());
        }
    }

    @Override
    public String write(SimpleFeature feature) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            StringWriter sw = new StringWriter();
            XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(sw);

            // the XML header
            streamWriter.writeStartDocument("UTF-8", "1.0");

            writeFeature(feature, streamWriter, true);
            
            streamWriter.flush();
            streamWriter.close();
            return sw.toString();

        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream exception while writing the feature: " + ex.getMessage());
        }
        return null;
    }

    private void writeFeature(SimpleFeature feature, XMLStreamWriter streamWriter, boolean root) {
        try {

            //the root element of the xml document (type of the feature)
            FeatureType type = feature.getType();
            String namespace = type.getName().getNamespaceURI();
            String localPart = type.getName().getLocalPart();
            if (namespace != null) {
                String prefix = Namespaces.getPreferredPrefix(namespace, null);
                streamWriter.writeStartElement(prefix, localPart, namespace);
                streamWriter.writeAttribute("gml", "http://www.opengis.net/gml", "id", feature.getID());
                if (root) {
                    streamWriter.writeNamespace("gml", "http://www.opengis.net/gml");
                    if (!namespace.equals("http://www.opengis.net/gml")) {
                        streamWriter.writeNamespace(prefix, namespace);

                    }
                }
            } else {
                streamWriter.writeStartElement(localPart);
                streamWriter.writeAttribute("gml", "http://www.opengis.net/gml", "id", feature.getID());
            }
            


            //the simple nodes (attributes of the feature)
            Name geometryName = null;
            for (Property a : feature.getProperties()) {
                if (a.getValue() instanceof Collection && !(a.getType() instanceof GeometryType)) {
                    String namespaceProperty = a.getName().getNamespaceURI();
                    for (Object value : (Collection)a.getValue()) {
                        if (namespaceProperty != null) {
                            streamWriter.writeStartElement(namespaceProperty, a.getName().getLocalPart());
                        } else {
                            streamWriter.writeStartElement(a.getName().getLocalPart());
                        }
                        streamWriter.writeCharacters(Utils.getStringValue(value));
                        streamWriter.writeEndElement();
                    }

                } else if (a.getValue() instanceof Map && !(a.getType() instanceof GeometryType)) {
                    String namespaceProperty = a.getName().getNamespaceURI();
                    Map map = (Map)a.getValue();
                    for (Object key : map.keySet()) {
                        if (namespaceProperty != null) {
                            streamWriter.writeStartElement(namespaceProperty, a.getName().getLocalPart());
                        } else {
                            streamWriter.writeStartElement(a.getName().getLocalPart());
                        }
                        streamWriter.writeAttribute("name", (String)key);
                        streamWriter.writeCharacters(Utils.getStringValue(map.get(key)));
                        streamWriter.writeEndElement();
                    }

                } else if (!(a.getType() instanceof GeometryType)) {
                    
                    String value = Utils.getStringValue(a.getValue());
                    if (value != null || a.isNillable()) {
                        String namespaceProperty = a.getName().getNamespaceURI();
                        if (namespaceProperty != null) {
                            streamWriter.writeStartElement(namespaceProperty, a.getName().getLocalPart());
                        } else {
                            streamWriter.writeStartElement(a.getName().getLocalPart());
                        }
                        if (value != null) {
                            streamWriter.writeCharacters(value);
                        }
                        streamWriter.writeEndElement();
                    }
                } else {
                    geometryName = a.getName();
                }
            }

            // we add the geometry
            if (feature.getDefaultGeometry() != null) {

                String namespaceProperty = geometryName.getNamespaceURI();
                if (namespaceProperty != null) {
                    streamWriter.writeStartElement(geometryName.getNamespaceURI(), geometryName.getLocalPart());
                } else {
                    streamWriter.writeStartElement(geometryName.getLocalPart());
                }
                Geometry isoGeometry = JTSUtils.toISO((com.vividsolutions.jts.geom.Geometry) feature.getDefaultGeometry(), feature.getFeatureType().getCoordinateReferenceSystem());
                try {
                    marshaller.marshal(factory.buildAnyGeometry(isoGeometry), streamWriter);
                } catch (JAXBException ex) {
                    LOGGER.severe("JAXB Exception while marshalling the iso geometry: " + ex.getMessage());
                }
                streamWriter.writeEndElement();
            }

            streamWriter.writeEndElement();

        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while writing the feature: " + ex.getMessage(), ex);

        }
    }

    @Override
    public void write(FeatureCollection fc, Writer writer) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter streamWriter    = outputFactory.createXMLStreamWriter(writer);

            writeFeatureCollection(fc, streamWriter);

        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while writing the feature: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void write(FeatureCollection fc, OutputStream out) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter streamWriter    = outputFactory.createXMLStreamWriter(out);

            writeFeatureCollection(fc, streamWriter);

        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while writing the feature: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String write(FeatureCollection featureCollection) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            StringWriter sw                = new StringWriter();
            XMLStreamWriter streamWriter   = outputFactory.createXMLStreamWriter(sw);

            writeFeatureCollection(featureCollection, streamWriter);
            return sw.toString();

        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while writing the feature: " + ex.getMessage(), ex);
        }
        return null;
    }

    private void writeFeatureCollection(FeatureCollection featureCollection, XMLStreamWriter streamWriter) {
        try {
            // the XML header
            streamWriter.writeStartDocument("UTF-8", "1.0");

            // the root Element
            streamWriter.writeStartElement("wfs", "FeatureCollection", "http://www.opengis.net/wfs");
            streamWriter.writeAttribute("gml", "http://www.opengis.net/gml", "id", featureCollection.getID());
            streamWriter.writeNamespace("gml", "http://www.opengis.net/gml");
            streamWriter.writeNamespace("wfs", "http://www.opengis.net/wfs");

            FeatureType type = featureCollection.getSchema();
            String namespace = type.getName().getNamespaceURI();
            if (namespace != null && !namespace.equals("http://www.opengis.net/gml")) {
                String prefix    = Namespaces.getPreferredPrefix(namespace, null);
                streamWriter.writeNamespace(prefix, namespace);
            }
             /*
             * The boundedby part
             */
            writeBounds(featureCollection.getBounds(), streamWriter);
            
            // we write each feature member of the collection
            FeatureIterator iterator =featureCollection.features();
            while (iterator.hasNext()) {
                final SimpleFeature f = (SimpleFeature) iterator.next();

                streamWriter.writeStartElement("gml", "featureMember", "http://www.opengis.net/gml");
                writeFeature(f, streamWriter, false);
                streamWriter.writeEndElement();
            }

            streamWriter.writeEndElement();
            streamWriter.writeEndDocument();
            // we close the stream
            iterator.close();
            streamWriter.flush();
            streamWriter.close();

        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while writing the feature: " + ex.getMessage(), ex);
        }
    }

    private void writeBounds(JTSEnvelope2D bounds, XMLStreamWriter streamWriter) throws XMLStreamException {
        if (bounds != null) {
            String srsName = null;
            if (bounds.getCoordinateReferenceSystem() != null) {
                try {
                    srsName = CRS.lookupIdentifier(bounds.getCoordinateReferenceSystem(), true);
                } catch (FactoryException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            streamWriter.writeStartElement("gml", "boundedBy", "http://www.opengis.net/gml");
            streamWriter.writeStartElement("gml", "Envelope", "http://www.opengis.net/gml");
            if (srsName != null) {
                streamWriter.writeAttribute("srsName", srsName);
            }

            // lower corner
            streamWriter.writeStartElement("gml", "lowerCorner", "http://www.opengis.net/gml");
            String lowValue = bounds.getLowerCorner().getOrdinate(0) + " " + bounds.getLowerCorner().getOrdinate(1);
            streamWriter.writeCharacters(lowValue);
            streamWriter.writeEndElement();

            // upper corner
            streamWriter.writeStartElement("gml", "upperCorner", "http://www.opengis.net/gml");
            String uppValue = bounds.getUpperCorner().getOrdinate(0) + " " + bounds.getUpperCorner().getOrdinate(1);
            streamWriter.writeCharacters(uppValue);
            streamWriter.writeEndElement();

            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
        }
    }
}
