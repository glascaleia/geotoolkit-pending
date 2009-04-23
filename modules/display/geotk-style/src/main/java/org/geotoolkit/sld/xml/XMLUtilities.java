/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotoolkit.sld.xml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.sld.DefaultSLDFactory;
import org.geotoolkit.sld.MutableSLDFactory;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.sld.StyledLayerDescriptor;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.Rule;
import org.opengis.style.Style;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

/**
 * Utility class to handle XML reading and writing for OGC SLD, SE and Filter.
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class XMLUtilities {

    private static final NamespacePrefixMapperImpl SLD_NAMESPACE = new NamespacePrefixMapperImpl("http://www.opengis.net/sld");
    private static final NamespacePrefixMapperImpl SE_NAMESPACE = new NamespacePrefixMapperImpl("http://www.opengis.net/se");
    private static final NamespacePrefixMapperImpl OGC_NAMESPACE = new NamespacePrefixMapperImpl("http://www.opengis.net/ogc");

    private static final JAXBContext jaxbContext100;
    private static final JAXBContext jaxbContext110;

    private final FilterFactory2 filterFactory;
    private final MutableStyleFactory styleFactory;
    private final MutableSLDFactory sldFactory;

    private final org.geotoolkit.internal.jaxb.v100.sld.ObjectFactory factory_sld_100 = new org.geotoolkit.internal.jaxb.v100.sld.ObjectFactory();
    private final org.geotoolkit.internal.jaxb.v110.sld.ObjectFactory factory_sld_110 = new org.geotoolkit.internal.jaxb.v110.sld.ObjectFactory();
    private final org.geotoolkit.internal.jaxb.v110.se.ObjectFactory factory_se_110 = new org.geotoolkit.internal.jaxb.v110.se.ObjectFactory();
    private final org.geotoolkit.internal.jaxb.v100.ogc.ObjectFactory factory_ogc_100 = new org.geotoolkit.internal.jaxb.v100.ogc.ObjectFactory();
    private final org.geotoolkit.internal.jaxb.v110.ogc.ObjectFactory factory_ogc_110 = new org.geotoolkit.internal.jaxb.v110.ogc.ObjectFactory();
    
    private SLD100toGTTransformer TRANSFORMER_GT_V100 = null;
    private SLD110toGTTransformer TRANSFORMER_GT_V110 = null;
    private GTtoSLD100Transformer TRANSFORMER_XML_V100 = null;
    private GTtoSLD110Transformer TRANSFORMER_XML_V110 = null;
    
    static{
        JAXBContext temp = null;
        try{
            temp = JAXBContext.newInstance(org.geotoolkit.internal.jaxb.v100.sld.StyledLayerDescriptor.class);
        }catch(JAXBException ex){
            ex.printStackTrace();
        }
        jaxbContext100 = temp;
        
        temp = null;
        try{
            temp = JAXBContext.newInstance(org.geotoolkit.internal.jaxb.v110.sld.StyledLayerDescriptor.class);
        }catch(JAXBException ex){
            ex.printStackTrace();
        }
        jaxbContext110 = temp;
    }

    public XMLUtilities() {
        final Hints hints = new Hints();
        hints.put(Hints.STYLE_FACTORY, MutableStyleFactory.class);
        hints.put(Hints.FILTER_FACTORY, FilterFactory2.class);
        this.styleFactory = (MutableStyleFactory)FactoryFinder.getStyleFactory(hints);
        this.filterFactory = (FilterFactory2) FactoryFinder.getFilterFactory(hints);
        this.sldFactory = new DefaultSLDFactory();
    }

    public XMLUtilities(FilterFactory2 filterFactory, MutableStyleFactory styleFactory, MutableSLDFactory sldFactory) {
        this.filterFactory = filterFactory;
        this.styleFactory = styleFactory;
        this.sldFactory = sldFactory;
    }
    
    private final Object unmarshall(final Object source, final Unmarshaller unMarshaller) 
            throws JAXBException{
        if(source instanceof File){
            return unMarshaller.unmarshal( (File)source );
        }else if(source instanceof InputSource){
            return unMarshaller.unmarshal( (InputSource)source );
        }else if(source instanceof InputStream){
            return unMarshaller.unmarshal( (InputStream)source );
        }else if(source instanceof Node){
            return unMarshaller.unmarshal( (Node)source );
        }else if(source instanceof Reader){
            return unMarshaller.unmarshal( (Reader)source );
        }else if(source instanceof Source){
            return unMarshaller.unmarshal( (Source)source );
        }else if(source instanceof URL){
            return unMarshaller.unmarshal( (URL)source );
        }else if(source instanceof XMLEventReader){
            return unMarshaller.unmarshal( (XMLEventReader)source );
        }else if(source instanceof XMLStreamReader){
            return unMarshaller.unmarshal( (XMLStreamReader)source );
        }else if(source instanceof OnLineResource){
            final OnLineResource online = (OnLineResource) source;
            try {
                final URL url = online.getLinkage().toURL();
                return unMarshaller.unmarshal(url);
            } catch (MalformedURLException ex) {
                Logger.getLogger(XMLUtilities.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            
        }else{
            throw new IllegalArgumentException("Source object is not a valid class :" + source.getClass());
        }
        
    }
    
    private final Object unmarshallV100(final Object source) throws JAXBException{
        if (TRANSFORMER_GT_V100 == null) {
            TRANSFORMER_GT_V100 = new SLD100toGTTransformer(filterFactory, styleFactory, sldFactory);
        }
        final Unmarshaller unMarshaller = jaxbContext100.createUnmarshaller();
        return unmarshall(source, unMarshaller);
    }
    
    private final Object unmarshallV110(final Object source) throws JAXBException{
        if (TRANSFORMER_GT_V110 == null) {
            TRANSFORMER_GT_V110 = new SLD110toGTTransformer(filterFactory, styleFactory, sldFactory);
        }
        final Unmarshaller unMarshaller = jaxbContext110.createUnmarshaller();
        return unmarshall(source, unMarshaller);
    }
    
    private final void marshall(final Object target, final Object jaxbElement, 
            final Marshaller marshaller) throws JAXBException{
        if(target instanceof File){
            marshaller.marshal(jaxbElement, (File)target );
        }else if(target instanceof ContentHandler){
            marshaller.marshal(jaxbElement, (ContentHandler)target );
        }else if(target instanceof OutputStream){
            marshaller.marshal(jaxbElement, (OutputStream)target );
        }else if(target instanceof Node){
            marshaller.marshal(jaxbElement, (Node)target );
        }else if(target instanceof Writer){
            marshaller.marshal(jaxbElement, (Writer)target );
        }else if(target instanceof Result){
            marshaller.marshal(jaxbElement, (Result)target );
        }else if(target instanceof XMLEventWriter){
            marshaller.marshal(jaxbElement, (XMLEventWriter)target );
        }else if(target instanceof XMLStreamWriter){
            marshaller.marshal(jaxbElement, (XMLStreamWriter)target );
        }else{
            throw new IllegalArgumentException("target object is not a valid class :" + target.getClass());
        }
    }
    
    private final void marshallV100(final Object target, final Object jaxElement, 
            final NamespacePrefixMapperImpl namespace) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(org.geotoolkit.internal.jaxb.v100.sld.StyledLayerDescriptor.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",namespace);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshall(target, jaxElement, marshaller);
    }
    
    /**
     * This method do the same marshalling process like the first marshallV100 method with an option to format or not the output.
     * @param target
     * @param jaxElement
     * @param namespace
     * @param isformatted
     * @throws javax.xml.bind.JAXBException
     */
    private final void marshallV100(final Object target, final Object jaxElement, 
            final NamespacePrefixMapperImpl namespace, boolean isformatted) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(org.geotoolkit.internal.jaxb.v100.sld.StyledLayerDescriptor.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",namespace);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, isformatted);
        marshall(target, jaxElement, marshaller);
    }
    
    private final void marshallV110(final Object target, final Object jaxElement, 
            final NamespacePrefixMapperImpl namespace) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(org.geotoolkit.internal.jaxb.v110.sld.StyledLayerDescriptor.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",namespace);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshall(target, jaxElement, marshaller);
    }
    
    
    // Styled Layer Descriptor -------------------------------------------------
    
    /**
     * Read a SLD source and parse it in GT SLD object.
     * Source can be : File, InputSource, InputStream, Node, Reader, Source, URL, 
     * XMLEventReader, XMLStreamReader or OnLineResource
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final MutableStyledLayerDescriptor readSLD(final Object source, 
            final Specification.StyledLayerDescriptor version) throws JAXBException{
        
        if(source == null || version == null) throw new NullPointerException("Source and version can not be null");
        
        final Object obj;
        
        switch(version){
            case V_1_0_0 :
                obj = unmarshallV100(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v100.sld.StyledLayerDescriptor){
                    return TRANSFORMER_GT_V100.visit( (org.geotoolkit.internal.jaxb.v100.sld.StyledLayerDescriptor) obj);
                }else{
                    throw new JAXBException("Source is not a valid OGC SLD v1.0.0");
                }
            case V_1_1_0 :
                obj = unmarshallV110(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v110.sld.StyledLayerDescriptor){
                    return TRANSFORMER_GT_V110.visit( (org.geotoolkit.internal.jaxb.v110.sld.StyledLayerDescriptor) obj);
                }else{
                    throw new JAXBException("Source is not a valid OGC SLD v1.1.0");
                }
            default :
                throw new IllegalArgumentException("Unable to read source, specified version is not supported");
        }
        
    }
    
    /**
     * Write a GT SLD.
     * Target can be : File, ContentHandler, OutputStream, Node, Writer, Result, 
     * XMLEventWriter, XMLStreamWriter
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final void writeSLD(final Object target, final StyledLayerDescriptor sld, 
            final Specification.StyledLayerDescriptor version) throws JAXBException{
        if(target == null || sld == null || version == null) throw new NullPointerException("Source, SLD and version can not be null");
        
        final Object jax;
        
        switch(version){
            case V_1_0_0 :
                if (TRANSFORMER_XML_V100 == null) TRANSFORMER_XML_V100 = new GTtoSLD100Transformer();
                jax = TRANSFORMER_XML_V100.visit(sld, null);
                marshallV100(target,jax,SLD_NAMESPACE);
                break;
            case V_1_1_0 :
                if (TRANSFORMER_XML_V110 == null) TRANSFORMER_XML_V110 = new GTtoSLD110Transformer();
                jax = TRANSFORMER_XML_V110.visit(sld, null);
                marshallV110(target,jax,SLD_NAMESPACE);
                break;
            default :
                throw new IllegalArgumentException("Unable to write object, specified version is not supported");
        }
        
    }
    
    /**
     * Write a GT SLD with an option to format the output result.
     * Target can be : File, ContentHandler, OutputStream, Node, Writer, Result, 
     * XMLEventWriter, XMLStreamWriter
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final void writeSLD(final Object target, final StyledLayerDescriptor sld, 
            final Specification.StyledLayerDescriptor version, boolean isformatted) throws JAXBException{
        if(target == null || sld == null || version == null) throw new NullPointerException("Source, SLD and version can not be null");
        
        final Object jax;
        
        switch(version){
            case V_1_0_0 :
                if (TRANSFORMER_XML_V100 == null) TRANSFORMER_XML_V100 = new GTtoSLD100Transformer();
                jax = TRANSFORMER_XML_V100.visit(sld, null);
                marshallV100(target,jax,SLD_NAMESPACE, isformatted);
                break;
            case V_1_1_0 :
                if (TRANSFORMER_XML_V110 == null) TRANSFORMER_XML_V110 = new GTtoSLD110Transformer();
                jax = TRANSFORMER_XML_V110.visit(sld, null);
                marshallV110(target,jax,SLD_NAMESPACE);
                break;
            default :
                throw new IllegalArgumentException("Unable to write object, specified version is not supported");
        }
    }
    
    
    // Symbology Encoding ------------------------------------------------------
    
    /**
     * Read a SLD UserStyle source and parse it in GT Style object.
     * Source can be : File, InputSource, InputStream, Node, Reader, Source, URL, 
     * XMLEventReader, XMLStreamReader or OnLineResource
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final MutableStyle readStyle(final Object source, 
            final Specification.SymbologyEncoding version) throws JAXBException{
        if(source == null || version == null) throw new NullPointerException("Source and version can not be null");
        
        final Object obj;
        
        switch(version){
            case SLD_1_0_0 :
                obj = unmarshallV100(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v100.sld.UserStyle){
                    return TRANSFORMER_GT_V100.visitUserStyle( (org.geotoolkit.internal.jaxb.v100.sld.UserStyle) obj);
                }else{
                    throw new JAXBException("Source is not a valid OGC SLD UserStyle v1.0.0");
                }
            case V_1_1_0 :
                obj = unmarshallV110(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v110.sld.UserStyle){
                    return TRANSFORMER_GT_V110.visitUserStyle( (org.geotoolkit.internal.jaxb.v110.sld.UserStyle) obj);
                }else{
                    throw new JAXBException("Source is not a valid OGC SLD UserStyle v1.1.0");
                }
            default :
                throw new IllegalArgumentException("Unable to read source, specified version is not supported");
        }
        
    }
    
    /**
     * Write a GT Style.
     * Target can be : File, ContentHandler, OutputStream, Node, Writer, Result, 
     * XMLEventWriter, XMLStreamWriter
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final void writeStyle(final Object target, final Style style, 
            final Specification.StyledLayerDescriptor version) throws JAXBException{
        if(target == null || style == null || version == null) throw new NullPointerException("Source, Style and version can not be null");
        
        final Object jax;
        
        switch(version){
            case V_1_0_0 :
                if (TRANSFORMER_XML_V100 == null) TRANSFORMER_XML_V100 = new GTtoSLD100Transformer();
                jax = TRANSFORMER_XML_V100.visit(style, null);
                marshallV100(target,jax,SLD_NAMESPACE);
                break;
            case V_1_1_0 :
                if (TRANSFORMER_XML_V110 == null) TRANSFORMER_XML_V110 = new GTtoSLD110Transformer();
                jax = TRANSFORMER_XML_V110.visit(style, null);
                marshallV110(target,jax,SLD_NAMESPACE);
                break;
            default :
                throw new IllegalArgumentException("Unable to write object, specified version is not supported");
        }
        
    }
    
    /**
     * Read a SE FeatureTypeStyle source and parse it in GT FTS object.
     * Source can be : File, InputSource, InputStream, Node, Reader, Source, URL, 
     * XMLEventReader, XMLStreamReader or OnLineResource
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final MutableFeatureTypeStyle readFeatureTypeStyle(final Object source, 
            final Specification.SymbologyEncoding version) throws JAXBException{
        if(source == null || version == null) throw new NullPointerException("Source and version can not be null");
        
        final Object obj;
        
        switch(version){
            case SLD_1_0_0 :
                obj = unmarshallV100(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v100.sld.FeatureTypeStyle){
                    return TRANSFORMER_GT_V100.visitFTS( (org.geotoolkit.internal.jaxb.v100.sld.FeatureTypeStyle) obj);
                }else{
                    throw new JAXBException("Source is not a valid OGC SLD FeatureTypeStyle v1.0.0");
                }
            case V_1_1_0 :
                obj = unmarshallV110(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v110.se.FeatureTypeStyleType){
                    return TRANSFORMER_GT_V110.visitFTS(obj);
                }else if(obj instanceof JAXBElement<?>&& (
                        ((JAXBElement<?>)obj).getValue() instanceof org.geotoolkit.internal.jaxb.v110.se.OnlineResourceType ||
                        ((JAXBElement<?>)obj).getValue() instanceof org.geotoolkit.internal.jaxb.v110.se.FeatureTypeStyleType ) ){
                    return TRANSFORMER_GT_V110.visitFTS( ((JAXBElement<?>)obj).getValue() );
                }else{
                    throw new JAXBException("Source is not a valid OGC SE FeatureTypeStyle v1.1.0");
                }
            default :
                throw new IllegalArgumentException("Unable to read source, specified version is not supported");
        }
        
    }
    
    /**
     * Write a GT FeatureTypeStyle.
     * Target can be : File, ContentHandler, OutputStream, Node, Writer, Result, 
     * XMLEventWriter, XMLStreamWriter
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final void writeFeatureTypeStyle(final Object target, final FeatureTypeStyle fts, 
            final Specification.SymbologyEncoding version) throws JAXBException{
        if(target == null || fts == null || version == null) throw new NullPointerException("Source, FTS and version can not be null");
        
        Object jax;
        
        switch(version){
            case SLD_1_0_0 :
                if (TRANSFORMER_XML_V100 == null) TRANSFORMER_XML_V100 = new GTtoSLD100Transformer();
                org.geotoolkit.internal.jaxb.v100.sld.FeatureTypeStyle jaxfts = TRANSFORMER_XML_V100.visit(fts, null);
                marshallV100(target,jaxfts,SLD_NAMESPACE);
                break;
            case V_1_1_0 :
                if (TRANSFORMER_XML_V110 == null) TRANSFORMER_XML_V110 = new GTtoSLD110Transformer();
                jax = TRANSFORMER_XML_V110.visit(fts, null);
                if(jax instanceof org.geotoolkit.internal.jaxb.v110.se.FeatureTypeStyleType){
                    jax = factory_se_110.createFeatureTypeStyle((org.geotoolkit.internal.jaxb.v110.se.FeatureTypeStyleType) jax);
                }else if(jax instanceof org.geotoolkit.internal.jaxb.v110.se.CoverageStyleType){
                    jax = factory_se_110.createCoverageStyle( (org.geotoolkit.internal.jaxb.v110.se.CoverageStyleType) jax);
                }
                marshallV110(target,jax,SE_NAMESPACE);
                break;
            default :
                throw new IllegalArgumentException("Unable to write object, specified version is not supported");
        }
        
    }
    
    /**
     * Read a SE Rule source and parse it in GT Rule object.
     * Source can be : File, InputSource, InputStream, Node, Reader, Source, URL, 
     * XMLEventReader, XMLStreamReader or OnLineResource
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final MutableRule readRule(final Object source, 
            final Specification.SymbologyEncoding version) throws JAXBException{
        if(source == null || version == null) throw new NullPointerException("Source and version can not be null");
        
        final Object obj;
        
        switch(version){
            case SLD_1_0_0 :
                obj = unmarshallV100(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v100.sld.Rule){
                    return TRANSFORMER_GT_V100.visitRule( (org.geotoolkit.internal.jaxb.v100.sld.Rule) obj);
                }else{
                    throw new JAXBException("Source is not a valid OGC SLD Rule v1.0.0");
                }
            case V_1_1_0 :
                obj = unmarshallV110(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v110.se.RuleType){
                    return TRANSFORMER_GT_V110.visitRule(obj);
                }else if(obj instanceof JAXBElement<?> && (
                        ((JAXBElement<?>)obj).getValue() instanceof org.geotoolkit.internal.jaxb.v110.se.OnlineResourceType ||
                        ((JAXBElement<?>)obj).getValue() instanceof org.geotoolkit.internal.jaxb.v110.se.RuleType ) ){
                    return TRANSFORMER_GT_V110.visitRule( ((JAXBElement<?>)obj).getValue() );
                }else{
                    throw new JAXBException("Source is not a valid OGC SE Rule v1.1.0");
                }
            default :
                throw new IllegalArgumentException("Unable to read source, specified version is not supported");
        }
        
    }
    
    /**
     * Write a GT Rule.
     * Target can be : File, ContentHandler, OutputStream, Node, Writer, Result, 
     * XMLEventWriter, XMLStreamWriter
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final void writeRule(final Object target, final Rule rule, 
            final Specification.SymbologyEncoding version) throws JAXBException{
        if(target == null || rule == null || version == null) throw new NullPointerException("Source, FTS and version can not be null");
        
        Object jax;
        
        switch(version){
            case SLD_1_0_0 :
                if (TRANSFORMER_XML_V100 == null) TRANSFORMER_XML_V100 = new GTtoSLD100Transformer();
                org.geotoolkit.internal.jaxb.v100.sld.Rule jaxRule = TRANSFORMER_XML_V100.visit(rule, null);
                marshallV100(target,jaxRule,SLD_NAMESPACE);
                break;
            case V_1_1_0 :
                if (TRANSFORMER_XML_V110 == null) TRANSFORMER_XML_V110 = new GTtoSLD110Transformer();
                jax = TRANSFORMER_XML_V110.visit(rule, null);
                if(jax instanceof org.geotoolkit.internal.jaxb.v110.se.RuleType){
                    jax = factory_se_110.createRule( (org.geotoolkit.internal.jaxb.v110.se.RuleType) jax);
                }
                marshallV110(target,jax,SE_NAMESPACE);
                break;
            default :
                throw new IllegalArgumentException("Unable to write object, specified version is not supported");
        }
        
    }
    
    
    // Filter ------------------------------------------------------------------
    
    /**
     * Read a Filter source and parse it in GT Filter object.
     * Source can be : File, InputSource, InputStream, Node, Reader, Source, URL, 
     * XMLEventReader, XMLStreamReader or OnLineResource
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final Filter readFilter(final Object source, 
            final Specification.Filter version) throws JAXBException{
        if(source == null || version == null) throw new NullPointerException("Source and version can not be null");
        
        final Object obj;
        
        switch(version){
            case V_1_0_0 :
                obj = unmarshallV100(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v100.ogc.FilterType){
                    return TRANSFORMER_GT_V100.visitFilter( (org.geotoolkit.internal.jaxb.v100.ogc.FilterType) obj);
                }else if(obj instanceof JAXBElement<?> && 
                        ((JAXBElement<?>)obj).getValue() instanceof org.geotoolkit.internal.jaxb.v100.ogc.FilterType){
                    return TRANSFORMER_GT_V100.visitFilter( (org.geotoolkit.internal.jaxb.v100.ogc.FilterType) ((JAXBElement<?>)obj).getValue() );
                }else{
                    throw new JAXBException("Source is not a valid OGC Filter v1.0.0");
                }
            case V_1_1_0 :
                obj = unmarshallV110(source);
                if(obj instanceof org.geotoolkit.internal.jaxb.v110.ogc.FilterType){
                    return TRANSFORMER_GT_V110.visitFilter( (org.geotoolkit.internal.jaxb.v110.ogc.FilterType) obj);
                }else if(obj instanceof JAXBElement<?> && 
                         ((JAXBElement<?>)obj).getValue() instanceof org.geotoolkit.internal.jaxb.v110.ogc.FilterType){
                    return TRANSFORMER_GT_V110.visitFilter( (org.geotoolkit.internal.jaxb.v110.ogc.FilterType) ((JAXBElement<?>)obj).getValue() );
                }else{
                    throw new JAXBException("Source is not a valid OGC Filter v1.1.0");
                }
            default :
                throw new IllegalArgumentException("Unable to read source, specified version is not supported");
        }
        
    }
    
    /**
     * Write a GT Filter.
     * Target can be : File, ContentHandler, OutputStream, Node, Writer, Result, 
     * XMLEventWriter, XMLStreamWriter
     * 
     * @throws javax.xml.bind.JAXBException
     */
    public final void writeFilter(final Object target, final Filter filter, 
            final Specification.Filter version) throws JAXBException{
        if(target == null || filter == null || version == null) throw new NullPointerException("Source, FTS and version can not be null");
        
        Object jax;
        
        switch(version){
            case V_1_0_0 :
                if (TRANSFORMER_XML_V100 == null) TRANSFORMER_XML_V100 = new GTtoSLD100Transformer();
                jax = TRANSFORMER_XML_V100.visit(filter);
                if(jax instanceof org.geotoolkit.internal.jaxb.v100.ogc.FilterType){
                    jax = factory_ogc_100.createFilter( (org.geotoolkit.internal.jaxb.v100.ogc.FilterType) jax);
                }
                marshallV100(target, jax, OGC_NAMESPACE);
                break;
            case V_1_1_0 :
                if (TRANSFORMER_XML_V110 == null) TRANSFORMER_XML_V110 = new GTtoSLD110Transformer();
                jax = TRANSFORMER_XML_V110.visit(filter);
                if(jax instanceof org.geotoolkit.internal.jaxb.v110.ogc.FilterType){
                    jax = factory_ogc_110.createFilter( (org.geotoolkit.internal.jaxb.v110.ogc.FilterType) jax);
                }
                marshallV110(target,jax,OGC_NAMESPACE);
                break;
            default :
                throw new IllegalArgumentException("Unable to write object, specified version is not supported");
        }
        
    }
    
}
