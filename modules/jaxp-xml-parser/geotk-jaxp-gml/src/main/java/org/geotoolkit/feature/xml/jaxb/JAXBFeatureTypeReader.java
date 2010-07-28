/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Geomatys
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

package org.geotoolkit.feature.xml.jaxb;

import com.vividsolutions.jts.geom.Geometry;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.SchemaException;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xsd.xml.v2001.ComplexContent;
import org.geotoolkit.xsd.xml.v2001.Element;
import org.geotoolkit.xsd.xml.v2001.ExplicitGroup;
import org.geotoolkit.xsd.xml.v2001.ExtensionType;
import org.geotoolkit.xsd.xml.v2001.ObjectFactory;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.xsd.xml.v2001.TopLevelComplexType;
import org.geotoolkit.xsd.xml.v2001.TopLevelElement;

import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class JAXBFeatureTypeReader implements XmlFeatureTypeReader {

    private static final Logger LOGGER = Logger.getLogger("org.geotoolkit.feature.xml.jaxp");
    
    private static MarshallerPool marshallpool;
    static {
        try {
            marshallpool = new MarshallerPool(ObjectFactory.class);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "JAXB Exception while initalizing the marshaller pool", ex);
        }
    }

    private final FeatureTypeBuilder builder = new FeatureTypeBuilder();

    public JAXBFeatureTypeReader() throws JAXBException {
    }

     /**
     * {@inheritDoc }
     */
    @Override
    public List<FeatureType> read(String xml) throws JAXBException {
        builder.reset();
        final Unmarshaller unmarshaller = marshallpool.acquireUnmarshaller();
        final Schema schema             = (Schema) unmarshaller.unmarshal(new StringReader(xml));
        try {
            return getAllFeatureTypeFromSchema(schema);
        } catch (SchemaException ex) {
            throw new JAXBException(ex);
        }
    }

     /**
     * {@inheritDoc }
     */
    @Override
    public List<FeatureType> read(InputStream in) throws JAXBException {
        builder.reset();
        final Unmarshaller unmarshaller = marshallpool.acquireUnmarshaller();
        final Schema schema             = (Schema) unmarshaller.unmarshal(in);
        try {
            return getAllFeatureTypeFromSchema(schema);
        } catch (SchemaException ex) {
            throw new JAXBException(ex);
        }
    }

     /**
     * {@inheritDoc }
     */
    @Override
    public List<FeatureType> read(Reader reader) throws JAXBException {
        builder.reset();
        final Unmarshaller unmarshaller = marshallpool.acquireUnmarshaller();
        final Schema schema             = (Schema) unmarshaller.unmarshal(reader);
        try {
            return getAllFeatureTypeFromSchema(schema);
        } catch (SchemaException ex) {
            throw new JAXBException(ex);
        }
    }

     /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType read(String xml, String name) throws JAXBException {
        builder.reset();
        final Unmarshaller unmarshaller = marshallpool.acquireUnmarshaller();
        final Schema schema             = (Schema) unmarshaller.unmarshal(new StringReader(xml));
        try {
            return getFeatureTypeFromSchema(schema, name);
        } catch (SchemaException ex) {
            throw new JAXBException(ex);
        }
    }

     /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType read(InputStream in, String name) throws JAXBException {
        builder.reset();
        final Unmarshaller unmarshaller = marshallpool.acquireUnmarshaller();
        final Schema schema             = (Schema) unmarshaller.unmarshal(in);
        try {
            return getFeatureTypeFromSchema(schema, name);
        } catch (SchemaException ex) {
            throw new JAXBException(ex);
        }
    }

     /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType read(Reader reader, String name) throws JAXBException {
        builder.reset();
        final Unmarshaller unmarshaller = marshallpool.acquireUnmarshaller();
        final Schema schema             = (Schema) unmarshaller.unmarshal(reader);
        try {
            return getFeatureTypeFromSchema(schema, name);
        } catch (SchemaException ex) {
            throw new JAXBException(ex);
        }
    }


    private List<FeatureType> getAllFeatureTypeFromSchema(Schema schema) throws SchemaException {
        final List<FeatureType> result = new ArrayList<FeatureType>();
        for (TopLevelElement element : schema.getElements()) {
            final QName typeName = element.getType();
            builder.setName(new DefaultName(typeName.getNamespaceURI(), element.getName()));
            final TopLevelComplexType type = schema.getComplexTypeByName(typeName.getLocalPart());
            result.add(getFeatureTypeFromSchema(type, typeName.getNamespaceURI()));
        }
        return result;
    }
    
    
    private FeatureType getFeatureTypeFromSchema(Schema schema, String name) throws SchemaException {
        final TopLevelElement element = schema.getElementByName(name);
        if (element != null) {
            final QName typeName = element.getType();
            if (typeName != null) {
                builder.setName(new DefaultName(typeName.getNamespaceURI(), name));
                final TopLevelComplexType type = schema.getComplexTypeByName(typeName.getLocalPart());
                return getFeatureTypeFromSchema(type, typeName.getNamespaceURI());
            } else {
                LOGGER.log(Level.WARNING, "the element:{0} has no type", name);
            }
        }
        return null;
    }
    
    private FeatureType getFeatureTypeFromSchema(TopLevelComplexType type, String namespace) throws SchemaException {
        if (type != null) {
            final ComplexContent content = type.getComplexContent();
            if (content != null) {
                final ExtensionType ext = content.getExtension();
                if (ext != null) {
                    // TODO handle base
                    final ExplicitGroup sequence = ext.getSequence();
                    if (sequence != null) {
                        for (Element attributeElement : sequence.getElements()) {
                            final QName elementType  = attributeElement.getType();
                            final String elementName = attributeElement.getName();
                            final Integer minAtt = attributeElement.getMinOccurs();
                            final String maxxAtt = attributeElement.getMaxOccurs();
                            final boolean nillable = attributeElement.isNillable();
                            final int min = (minAtt==null)? 1 : minAtt;
                            final int max;
                            if(maxxAtt == null){
                                max = 1;
                            }else if(maxxAtt.equalsIgnoreCase("unbounded")){
                                max = Integer.MAX_VALUE;
                            }else{
                                max = Integer.parseInt(maxxAtt);
                            }
                            CoordinateReferenceSystem crs = null;

                            final Class c = Utils.getTypeFromQName(elementType);
                            if (c == null) {
                                throw new SchemaException("The attribute:" + elementName + " does no have a declared type.");
                            }
                            if(Geometry.class.isAssignableFrom(c) || org.opengis.geometry.Geometry.class.isAssignableFrom(c)){
                                builder.add(new DefaultName(namespace, elementName), c, crs, min, max, nillable, null);
                            }else{
                                builder.add(new DefaultName(namespace, elementName), c, min, max, nillable, null);
                            }
                        }
                    }
                }
            }
        }

        return builder.buildFeatureType();
    }
}
