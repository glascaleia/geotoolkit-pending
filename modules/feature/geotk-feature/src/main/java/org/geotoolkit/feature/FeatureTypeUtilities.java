/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2011, Geomatys
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
package org.geotoolkit.feature;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.geotoolkit.factory.FactoryRegistryException;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.simple.DefaultSimpleFeatureType;
import org.geotoolkit.filter.function.other.LengthFunction;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.util.Utilities;
import org.geotoolkit.feature.type.DefaultAttributeDescriptor;
import org.geotoolkit.feature.type.DefaultAttributeType;
import org.geotoolkit.feature.type.DefaultGeometryDescriptor;
import org.geotoolkit.feature.type.DefaultGeometryType;
import org.geotoolkit.filter.visitor.FilterAttributeExtractor;
import org.geotoolkit.metadata.iso.citation.Citations;
import org.geotoolkit.referencing.CRS;

import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.feature.Property;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Collection;
import org.opengis.feature.type.*;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 * Utility methods for working against the FeatureType interface.
 * <p>
 * Many methods from DataUtilities should be refractored here.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Schema construction from String spec
 * <li>Schema Force CRS
 * </ul>
 *
 * @author Jody Garnett, Refractions Research
 * @author Johann Sorel, Geomatys
 * @module pending
 * @since 2.1.M3
 */
public final class FeatureTypeUtilities {

    /** the default namespace for feature types */
    //public static final URI = GMLSchema.NAMESPACE;
    public static final URI DEFAULT_NAMESPACE;

    /** abstract base type for all feature types */
    public static final SimpleFeatureType ABSTRACT_FEATURE_TYPE;

    private static final Map<String, Class> TYPE_MAP = new HashMap<String, Class>();
    private static final Map<Class, String> TYPE_ENCODE = new HashMap<Class, String>();

    static {
        TYPE_ENCODE.put(String.class, "String");
        TYPE_MAP.put("String", String.class);
        TYPE_MAP.put("string", String.class);
        TYPE_MAP.put("\"\"", String.class);

        TYPE_ENCODE.put(Integer.class, "Integer");
        TYPE_MAP.put("Integer", Integer.class);
        TYPE_MAP.put("int", Integer.class);
        TYPE_MAP.put("0", Integer.class);

        TYPE_ENCODE.put(Double.class, "Double");
        TYPE_MAP.put("Double", Double.class);
        TYPE_MAP.put("double", Double.class);
        TYPE_MAP.put("0.0", Double.class);

        TYPE_ENCODE.put(Float.class, "Float");
        TYPE_MAP.put("Float", Float.class);
        TYPE_MAP.put("float", Float.class);
        TYPE_MAP.put("0.0f", Float.class);

        TYPE_ENCODE.put(Boolean.class, "Boolean");
        TYPE_MAP.put("Boolean", Boolean.class);
        TYPE_MAP.put("true", Boolean.class);
        TYPE_MAP.put("false", Boolean.class);

        TYPE_ENCODE.put(Geometry.class, "Geometry");
        TYPE_MAP.put("Geometry", Geometry.class);

        TYPE_ENCODE.put(Point.class, "Point");
        TYPE_MAP.put("Point", Point.class);

        TYPE_ENCODE.put(LineString.class, "LineString");
        TYPE_MAP.put("LineString", LineString.class);

        TYPE_ENCODE.put(Polygon.class, "Polygon");
        TYPE_MAP.put("Polygon", Polygon.class);

        TYPE_ENCODE.put(MultiPoint.class, "MultiPoint");
        TYPE_MAP.put("MultiPoint", MultiPoint.class);

        TYPE_ENCODE.put(MultiLineString.class, "MultiLineString");
        TYPE_MAP.put("MultiLineString", MultiLineString.class);

        TYPE_ENCODE.put(MultiPolygon.class, "MultiPolygon");
        TYPE_MAP.put("MultiPolygon", MultiPolygon.class);

        TYPE_ENCODE.put(GeometryCollection.class, "GeometryCollection");
        TYPE_MAP.put("GeometryCollection", GeometryCollection.class);

        TYPE_ENCODE.put(Date.class, "Date");
        TYPE_MAP.put("Date", Date.class);

        try {
            DEFAULT_NAMESPACE = new URI("http://www.opengis.net/gml");
        } catch (URISyntaxException e) {
            //will never happen
            throw new IllegalStateException("Failed to parse URI.");
        }

        SimpleFeatureType featureType = null;
        try {
            featureType = newFeatureType(null, "Feature", new URI("http://www.opengis.net/gml"), true);
        } catch (Exception e) {
            //shold not happen
        }
        ABSTRACT_FEATURE_TYPE = featureType;
    }

    /** default feature collection name */
    public static final DefaultName DEFAULT_TYPENAME =
            new DefaultName("AbstractFeatureCollectionType", DEFAULT_NAMESPACE.toString());

    /** represent an unbounded field length */
    public static final int ANY_LENGTH = -1;

    /** An feature type with no attributes */
    public static final SimpleFeatureType EMPTY = new DefaultSimpleFeatureType(
            new DefaultName("Empty"), Collections.EMPTY_LIST, null, false, Collections.EMPTY_LIST, null, null);

    
    private FeatureTypeUtilities() {}

    /**
     * Remove properties that are used for generating the primary key.
     */
    public static FeatureType excludePrimaryKeyFields(final FeatureType ft) throws SchemaException{
        final List<Name> pkeys = new ArrayList<Name>();
        for(PropertyDescriptor desc : ft.getDescriptors()){
            if(!isPartOfPrimaryKey(desc)) pkeys.add(desc.getName());
        }

        if(pkeys.isEmpty()){
            return ft;
        }else{
            return createSubType(ft, pkeys.toArray(new Name[pkeys.size()]));
        }

    }

    /**
     * Create a derived FeatureType
     *
     * <p></p>
     *
     * @param featureType
     * @param properties - if null, every property of the feature type in input will be used
     * @param override
     *
     *
     * @throws SchemaException
     */
    public static FeatureType createSubType(final FeatureType featureType,
            final Name[] properties, final CoordinateReferenceSystem override) throws SchemaException{
        URI namespaceURI = null;
        if (featureType.getName().getNamespaceURI() != null) {
            try {
                namespaceURI = new URI(featureType.getName().getNamespaceURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return createSubType(featureType, properties, override, featureType.getName().getLocalPart(), namespaceURI);
    }

    public static FeatureType createSubType(final FeatureType featureType,
            Name[] properties, final CoordinateReferenceSystem override, String typeName, URI namespace)
            throws SchemaException {

        if ((properties == null) && (override == null)) {
            return featureType;
        }

        final int propCount = featureType.getDescriptors().size();

        if (properties == null) {
            properties = new Name[propCount];
            int i=0;
            for(PropertyDescriptor desc : featureType.getDescriptors()) {
                properties[i] = desc.getName();
                i++;
            }
        }

        final String namespaceURI = namespace != null ? namespace.toString() : null;
        boolean same = (propCount == properties.length) &&
                featureType.getName().getLocalPart().equals(typeName) &&
                Utilities.equals(featureType.getName().getNamespaceURI(), namespaceURI);


        int i=0;
        if(same){
            for(PropertyDescriptor desc : featureType.getDescriptors()){
                same = desc.getName().equals(properties[i]) && (((override != null) && desc instanceof GeometryDescriptor)
                        ? assertEquals(override, ((GeometryDescriptor) desc).getCoordinateReferenceSystem())
                        : true);
                if(!same)break;
                i++;
            }
        }

        if (same) {
            return featureType;
        }


        final FeatureTypeBuilder tb = new FeatureTypeBuilder();
        final AttributeTypeBuilder ab = new AttributeTypeBuilder();
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();

        final PropertyDescriptor[] types = new AttributeDescriptor[properties.length];
        boolean crsSetted = false;

        for (i=0; i<properties.length; i++) {
            types[i] = featureType.getDescriptor(properties[i]);

            if (types[i] == null) {
                throw new IllegalArgumentException("No property "+ properties[i] +
                        " for feature type :\n"+ featureType.toString());
            }
            if ((override != null) && types[i] instanceof GeometryDescriptor) {
                ab.reset();
                ab.copy((AttributeType) types[i].getType());
                ab.setCRS(override);
                crsSetted = true;
                adb.reset();
                adb.copy((AttributeDescriptor) types[i]);
                adb.setType(ab.buildGeometryType());
                types[i] = adb.buildDescriptor();
            }
        }

        if (typeName == null) {
            typeName = featureType.getName().getLocalPart();
        }
        if (namespace == null && featureType.getName().getNamespaceURI() != null) {
            try {
                namespace = new URI(featureType.getName().getNamespaceURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }



        tb.setName(new DefaultName(namespaceURI, typeName));
        tb.addAll(types);

        FeatureType result = tb.buildFeatureType();

        if (!crsSetted && result instanceof DefaultSimpleFeatureType) {
            ((DefaultSimpleFeatureType)result).setCoordinateReferenceSystem(override);
        }
        return result;
    }

    public static FeatureType createSubType(final FeatureType featureType,
            final String[] properties) throws SchemaException{
        if (properties == null) {
            return featureType;
        }        
        final Name[] props = new Name[properties.length];
        for(int i=0; i<properties.length; i++){
            props[i] = DefaultName.valueOf(properties[i]);
        }
        return createSubType(featureType, props);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param featureType DOCUMENT ME!
     * @param properties DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SchemaException DOCUMENT ME!
     */
    public static FeatureType createSubType(final FeatureType featureType,
            final Name[] properties) throws SchemaException{
        if (properties == null) {
            return featureType;
        }

        final int size = featureType.getDescriptors().size();
        boolean same = (size == properties.length);

        if(same){
            int i=0;
            for(PropertyDescriptor desc : featureType.getDescriptors()){
                if(properties[i].getNamespaceURI() == null){
                    same = desc.getName().getLocalPart().equals(properties[i].getLocalPart());
                }else{
                    same = desc.getName().equals(properties[i]);
                }
                if(!same) break;
                i++;
            }
        }

        if (same) {
            return featureType;
        }

        final FeatureTypeBuilder tb = new FeatureTypeBuilder();
        tb.setName(featureType.getName());

        for (int i=0; i<properties.length; i++) {
            PropertyDescriptor desc = featureType.getDescriptor(properties[i]);
            if(desc == null){
                throw new SchemaException("Property not found : " + properties[i] +'\n'+featureType);
            }
            tb.add(desc);
        }
        return tb.buildFeatureType();
    }

    /**
     * Utility method for FeatureType construction.
     * <p>
     * Will parse a String of the form: <i>"name:Type,name2:Type2,..."</i>
     * </p>
     *
     * <p>
     * Where <i>Type</i> is defined by createAttribute.
     * </p>
     *
     * <p>
     * You may indicate the default Geometry with an astrix: "*geom:Geometry". You
     * may also indicate the srid (used to look up a EPSG code).
     * </p>
     *
     * <p>
     * Examples:
     * <ul>
     * <li><code>name:"",age:0,geom:Geometry,centroid:Point,url:java.io.URL"</code>
     * <li><code>id:String,polygonProperty:Polygon:srid=32615</code>
     * </ul>
     * </p>
     *
     * @param identification identification of FeatureType:
     *        (<i>namesapce</i>).<i>typeName</i>
     * @param typeSpec Specification for FeatureType
     *
     *
     * @throws SchemaException
     */
    public static SimpleFeatureType createType(final String identification, final String typeSpec)
            throws SchemaException
    {
        final int split = identification.lastIndexOf('.');
        final String namespace = (split == -1) ? null
                : identification.substring(0, split);
        final String typeName = (split == -1) ? identification
                : identification.substring(split + 1);

        return createType(namespace, typeName, typeSpec);
    }

    /**
     * Utility method for FeatureType construction.
     * <p>
     * Will parse a String of the form: <i>"name:Type,name2:Type2,..."</i>
     * </p>
     *
     * <p>
     * Where <i>Type</i> is defined by createAttribute.
     * </p>
     *
     * <p>
     * You may indicate the default Geometry with an astrix: "*geom:Geometry". You
     * may also indicate the srid (used to look up a EPSG code).
     * </p>
     *
     * <p>
     * Examples:
     * <ul>
     * <li><code>name:"",age:0,geom:Geometry,centroid:Point,url:java.io.URL"</code>
     * <li><code>id:String,polygonProperty:Polygon:srid=32615</code>
     * </ul>
     * </p>
     *
     * @param namespace
     * @param typeName
     * @param typeSpec Specification for FeatureType
     *
     * @throws SchemaException
     */
    public static SimpleFeatureType createType(final String namespace, final String typeName,
            final String typeSpec) throws SchemaException
    {
        final FeatureTypeBuilder tb = new FeatureTypeBuilder();
        tb.setName(new DefaultName(namespace, typeName));

        final String[] types = typeSpec.split(",");

        AttributeDescriptor attributeType;

        for (int i = 0; i < types.length; i++) {
            final boolean defaultGeometry = types[i].startsWith("*");
            if (types[i].startsWith("*")) {
                types[i] = types[i].substring(1);
            }

            attributeType = createAttribute(namespace,types[i]);
            tb.add(attributeType);

            if (defaultGeometry) {
                tb.setDefaultGeometry(attributeType.getLocalName());
            }
        }

        return tb.buildSimpleFeatureType();
    }


    /**
     * Returns AttributeType based on String specification (based on UML).
     *
     * <p>
     * Will parse a String of the form: <i>"name:Type:hint"</i>
     * </p>
     *
     * <p>
     * Where <i>Type</i> is:
     * </p>
     *
     * <ul>
     * <li>
     * 0,Interger,int: represents Interger
     * </li>
     * <li>
     * 0.0, Double, double: represents Double
     * </li>
     * <li>
     * "",String,string: represents String
     * </li>
     * <li>
     * Geometry: represents Geometry
     * </li>
     * <li>
     * <i>full.class.path</i>: represents java type
     * </li>
     * </ul>
     *
     * <p>
     * Where <i>hint</i> is "hint1;hint2;...;hintN", in which "hintN" is one
     * of:
     * <ul>
     *  <li><code>nillable</code></li>
     *  <li><code>srid=<#></code></li>
     * </ul>
     * </p>
     *
     * @param typeSpec
     *
     *
     * @throws SchemaException If typeSpect could not be interpreted
     */
    private static AttributeDescriptor createAttribute(final String namespace, final String typeSpec) throws SchemaException {
        final int split = typeSpec.indexOf(':');

        final String name;
        final String type;
        String hint = null;

        if (split == -1) {
            name = typeSpec;
            type = "String";
        } else {
            name = typeSpec.substring(0, split);

            final int split2 = typeSpec.indexOf(':', split + 1);

            if (split2 == -1) {
                type = typeSpec.substring(split + 1);
            } else {
                type = typeSpec.substring(split + 1, split2);
                hint = typeSpec.substring(split2 + 1);
            }
        }

        try {
            boolean nillable = true;
            CoordinateReferenceSystem crs = null;

            if (hint != null) {
                final StringTokenizer st = new StringTokenizer(hint, ";");
                while (st.hasMoreTokens()) {
                    String h = st.nextToken();
                    h = h.trim();

                    //nillable?
                    //JD: i am pretty sure this hint is useless since the
                    // default is to make attributes nillable
                    if (h.equals("nillable")) {
                        nillable = true;
                    }
                    //spatial reference identieger?
                    if (h.startsWith("srid=")) {
                        final String srid = h.split("=")[1];
                        Integer.parseInt(srid);
                        try {
                            crs = CRS.decode("EPSG:" + srid);
                        } catch (Exception e) {
                            final String msg = "Error decoding srs: " + srid;
                            throw new SchemaException(msg, e);
                        }
                    }
                }
            }

            final Class clazz = type(type);
            if (Geometry.class.isAssignableFrom(clazz)) {
                final GeometryType at = new DefaultGeometryType(new DefaultName(namespace,name), clazz, crs, false, false,
                                                                Collections.EMPTY_LIST, null, null);
                return new DefaultGeometryDescriptor(at, new DefaultName(namespace,name), 1, 1, nillable, null);
            } else {
                final AttributeType at = new DefaultAttributeType(new DefaultName(namespace,name), clazz, false, false,
                                                                  Collections.EMPTY_LIST, null, null);
                return new DefaultAttributeDescriptor(at, new DefaultName(namespace,name), 1, 1, nillable, null);
            }
        } catch (ClassNotFoundException e) {
            throw new SchemaException("Could not type " + name + " as:" + type, e);
        }
    }


    /**
     * A "quick" String representation of a FeatureType.
     * <p>
     * This string representation may be used with createType( name, spec ).
     * </p>
     * @param featureType FeatureType to represent
     *
     * @return The string "specification" for the featureType
     */
    public static String spec(final SimpleFeatureType featureType) {
        final List types = featureType.getAttributeDescriptors();

        final StringBuilder buf = new StringBuilder();

        for (int i = 0; i < types.size(); i++) {
            final AttributeDescriptor type = (AttributeDescriptor) types.get(i);
            buf.append(type.getLocalName());
            buf.append(":");
            buf.append(typeMap(type.getType().getBinding()));
            if (type instanceof GeometryDescriptor) {
                final GeometryDescriptor gd = (GeometryDescriptor) type;
                if (gd.getCoordinateReferenceSystem() != null && gd.getCoordinateReferenceSystem().getIdentifiers() != null) {
                    for (Iterator<ReferenceIdentifier> it = gd.getCoordinateReferenceSystem().getIdentifiers().iterator(); it.hasNext();) {
                        final ReferenceIdentifier id = (ReferenceIdentifier) it.next();

                        if ((id.getAuthority() != null) && id.getAuthority().getTitle().equals(Citations.EPSG.getTitle())) {
                            buf.append(":srid=" + id.getCode());
                            break;
                        }

                    }
                }
            }

            if (i < (types.size() - 1)) {
                buf.append(",");
            }
        }

        return buf.toString();
    }

    private static Class type(final String typeName) throws ClassNotFoundException {
        if (TYPE_MAP.containsKey(typeName)) {
            return (Class) TYPE_MAP.get(typeName);
        }

        return Class.forName(typeName);
    }

    private static String typeMap(final Class type) {
        if (TYPE_ENCODE.containsKey(type)) {
            return TYPE_ENCODE.get(type);
        }
        /*
        SortedSet<String> choose = new TreeSet<String>();
        for (Iterator i = typeMap.entrySet().iterator(); i.hasNext();) {
        Map.Entry entry = (Entry) i.next();

        if (entry.getValue().equals(type)) {
        choose.add( (String) entry.getKey() );
        }
        }
        if( !choose.isEmpty() ){
        return choose.last();
        }
         */
        return type.getName();
    }

    private static boolean assertEquals(final Object o1, final Object o2) {
        return o1 == null && o2 == null ? true : (o1 != null ? o1.equals(o2) : false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // INFORMATIONS ABOUT ATTRIBUTS and TYPES //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This is a 'suitable replacement for extracting the expected field length of an attribute
     * absed on its "facets" (ie Filter describing type restrictions);
     * <p>
     * This code is copied from the ShapefileDataStore where it was written (probably by dzwiers).
     * Cholmes is providing documentation.
     * </p>
     *
     * @param descriptor the AttributeDescriptor
     *
     * @return an int indicating the max length of field in characters, or ANY_LENGTH
     */
    public static int getFieldLength(final AttributeDescriptor descriptor) {

        AttributeType type = descriptor.getType();
        while (type != null) {
            // TODO: We should really go through all the restrictions and find
            // the minimum of all the length restrictions; for now we assume an
            // override behaviour.
            for (Filter f : type.getRestrictions()) {
                if (f != null && f != Filter.EXCLUDE && f != Filter.INCLUDE && (f instanceof PropertyIsLessThan || f instanceof PropertyIsLessThanOrEqualTo)) {
                    try {
                        final BinaryComparisonOperator cf = (BinaryComparisonOperator) f;
                        if (cf.getExpression1() instanceof LengthFunction) {
                            return Integer.parseInt(((Literal) cf.getExpression2()).getValue().toString());
                        } else if (cf.getExpression2() instanceof LengthFunction) {
                            return Integer.parseInt(((Literal) cf.getExpression1()).getValue().toString());
                        } else {
                            return ANY_LENGTH;
                        }
                    } catch (NumberFormatException e) {
                        return ANY_LENGTH;
                    }
                }
            }
            type = type.getSuper();
        }
        return ANY_LENGTH;
    }

    /**
     * Forces the specified CRS on all geometry attributes
     * @param schema the original schema
     * @param crs the forced crs
     * @return {@link SimpleFeatureType}
     * @throws SchemaException
     */
    public static FeatureType transform(final FeatureType schema, final CoordinateReferenceSystem crs)
            throws SchemaException{
        return transform(schema, crs, false);
    }

    /**
     * Forces the specified CRS on geometry attributes (all or some, depends on the parameters).
     * @param schema the original schema
     * @param crs the forced crs
     * @param forceOnlyMissing if true, will force the specified crs only on the attributes that
     *        do miss one
     * @return {@link SimpleFeatureType}
     * @throws SchemaException
     */
    public static FeatureType transform(final FeatureType schema, final CoordinateReferenceSystem crs,
            final boolean forceOnlyMissing) throws SchemaException{
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName(schema.getName());
        ftb.setAbstract(schema.isAbstract());

        for(PropertyDescriptor attributeType : schema.getDescriptors()){
            if (attributeType instanceof GeometryDescriptor) {
                final GeometryDescriptor geometryType = (GeometryDescriptor) attributeType;

                final AttributeTypeBuilder tb = new AttributeTypeBuilder();
                tb.copy(geometryType.getType());

                if (!forceOnlyMissing || geometryType.getCoordinateReferenceSystem() == null) {
                    tb.setCRS(crs);
                }

                final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
                adb.copy(geometryType);
                adb.setType(tb.buildGeometryType());

                ftb.add(adb.buildDescriptor());
            } else {
                ftb.add(attributeType);
            }
        }

        if (schema.getGeometryDescriptor() != null) {
            ftb.setDefaultGeometry(schema.getGeometryDescriptor().getLocalName());
        }

        ftb.setSuperType(schema.getSuper());
        return ftb.buildFeatureType();
    }

    /**
     * Applies transform to all geometry attribute.
     *
     * @param feature Feature to be transformed
     * @param schema Schema for target transformation - transform( schema, crs )
     * @param transform MathTransform used to transform coordinates - reproject( crs, crs )
     * @return transformed Feature of type schema
     * @throws TransformException
     * @throws MismatchedDimensionException
     * @throws IllegalAttributeException
     */
    public static SimpleFeature transform(SimpleFeature feature, final SimpleFeatureType schema, final MathTransform transform)
            throws MismatchedDimensionException, TransformException, SimpleIllegalAttributeException{
        feature = FeatureUtilities.copy(feature);

        final GeometryDescriptor geomType = schema.getGeometryDescriptor();
        Geometry geom = (Geometry) feature.getAttribute(geomType.getLocalName());

        geom = JTS.transform(geom, transform);

        feature.setAttribute(geomType.getLocalName(), geom);

        return feature;
    }

    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType(final AttributeDescriptor[] types, final String name,
            final URI ns, final boolean isAbstract, final SimpleFeatureType[] superTypes)
            throws FactoryRegistryException, SchemaException{
        return newFeatureType(types, name, ns, isAbstract, superTypes, null);
    }

    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType(final AttributeDescriptor[] types, final String name,
            final URI ns, final boolean isAbstract, final SimpleFeatureType[] superTypes,
            final AttributeDescriptor defaultGeometry) throws FactoryRegistryException, SchemaException{

        final FeatureTypeBuilder tb = new FeatureTypeBuilder();
        tb.setName(new DefaultName(ns.toString(), name));
        tb.setAbstract(isAbstract);
        tb.addAll(types);

        if (defaultGeometry != null) {
            //make sure that the default geometry was one of the types specified
            boolean add = true;
            for (int i = 0; i < types.length; i++) {
                if (types[i] == defaultGeometry) {
                    add = false;
                    break;
                }
            }
            if (add) {
                tb.add(defaultGeometry);
            }
            tb.setDefaultGeometry(defaultGeometry.getLocalName());
        }
        if (superTypes != null && superTypes.length > 0) {
            if (superTypes.length > 1) {
                throw new SchemaException("Can only specify a single super type");
            }
            tb.setSuperType(superTypes[0]);

        } else {
            //use the default super type
            tb.setSuperType(ABSTRACT_FEATURE_TYPE);
        }
        return tb.buildSimpleFeatureType();
    }

    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType(final AttributeDescriptor[] types, final String name,
            final URI ns, final boolean isAbstract, final SimpleFeatureType[] superTypes,
            final GeometryDescriptor defaultGeometry) throws FactoryRegistryException, SchemaException{
        return newFeatureType(types, name, ns, isAbstract, superTypes, (AttributeDescriptor) defaultGeometry);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for calling
     * <code>newFeatureType(types,name,ns,isAbstract,null)</code>.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType(final AttributeDescriptor[] types, final String name,
            final URI ns, final boolean isAbstract) throws FactoryRegistryException, SchemaException{
        return newFeatureType(types, name, ns, isAbstract, null);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for calling
     * <code>newFeatureType(types,name,ns,false,null)</code>.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType(final AttributeDescriptor[] types, final String name,
            final URI ns) throws FactoryRegistryException, SchemaException{
        return newFeatureType(types, name, ns, false);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for calling
     * <code>newFeatureType(types,name,null,false,null)</code>. Useful for test cases or
     * datasources which may not allow a namespace.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType(final AttributeDescriptor[] types, final String name)
            throws FactoryRegistryException, SchemaException{
        return newFeatureType(types, name, DEFAULT_NAMESPACE, false);
    }

    /**
     * Walks up the type hierachy of the feature returning all super types of the specified feature
     * type.
     */
    public static List<PropertyType> getAncestors(PropertyType candidate) {
        final List<PropertyType> ancestors = new ArrayList<PropertyType>();
        PropertyType ancestor;
        while ((ancestor = candidate.getSuper()) != null) {
            ancestors.add(ancestor);
            candidate = ancestor;
        }
        return ancestors;
    }

    /**
     * A query of the the types ancestor information.
     * <p>
     * This utility method may be used as common implementation for
     * <code>FeatureType.isDecendedFrom( namespace, typeName )</code>, however for specific uses,
     * such as GML, an implementor may be able to provide a more efficient implemenation based on
     * prior knolwege.
     * </p>
     * <p>
     * This is a proper check, if the provided FeatureType matches the given namespace and typename
     * it is <b>not </b> considered to be decended from itself.
     * </p>
     *
     * @param featureType
     *            typeName with parentage in question
     * @param namespace
     *            namespace to match against, or null for a "wildcard"
     * @param typeName
     *            typename to match against, or null for a "wildcard"
     * @return true if featureType is a decendent of the indicated namespace & typeName
     */
    public static boolean isDecendedFrom(final PropertyType featureType, final URI namespace,
            final String typeName){

        if (featureType == null) {
            return false;
        }
        final List<PropertyType> ancestors = getAncestors(featureType);
        for (PropertyType superType : ancestors) {
            if (namespace == null) {
                // dont match on namespace
                if (Utilities.equals(superType.getName().getLocalPart(), typeName)) {
                    return true;
                }
            } else {
                if (Utilities.equals(superType.getName().getNamespaceURI(), namespace.toString()) &&
                    Utilities.equals(superType.getName().getLocalPart(), typeName)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isDecendedFrom(final PropertyType featureType, final PropertyType isParentType) {
        try {
            return isDecendedFrom(featureType, new URI(isParentType.getName().getNamespaceURI()),
                    isParentType.getName().getLocalPart());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /** Exact equality based on typeNames, namespace, attributes and ancestors */
    public static boolean equals(final FeatureType typeA, final FeatureType typeB) {
        if (typeA == typeB) {
            return true;
        }

        if (typeA == null || typeB == null) {
            return false;
        }
        return equalsId(typeA, typeB) && 
               equals(typeA.getDescriptors(), typeB.getDescriptors()) &&
               equalsAncestors(typeA, typeB);
    }
    

    public static boolean equals(final Collection attributesA, final Collection attributesB) {
        return equals(
                (PropertyDescriptor[]) attributesA.toArray(new PropertyDescriptor[attributesA.size()]),
                (PropertyDescriptor[]) attributesB.toArray(new PropertyDescriptor[attributesB.size()]));
    }

    public static boolean equals(final PropertyDescriptor[] attributesA, 
            final PropertyDescriptor[] attributesB) {
        if (attributesA.length != attributesB.length) {
            return false;
        }

        for (int i = 0, length = attributesA.length; i < length; i++) {
            if (!equals(attributesA[i], attributesB[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method depends on the correct implementation of FeatureType equals
     * <p>
     * We may need to write an implementation that can detect cycles,
     * </p>
     *
     * @param typeA
     * @param typeB
     */
    public static boolean equalsAncestors(final FeatureType typeA, final FeatureType typeB) {
        return ancestors(typeA).equals(typeB);
    }

    public static Set ancestors(final FeatureType featureType) {
        if (featureType == null || getAncestors(featureType).isEmpty()) {
            return Collections.EMPTY_SET;
        }
        return new HashSet(getAncestors(featureType));
    }

    public static boolean equals(final PropertyDescriptor a, final PropertyDescriptor b) {
        return a == b || (a != null && a.equals(b));
    }
    
    /** Quick check of namespace and typename */
    public static boolean equalsId(final FeatureType typeA, final FeatureType typeB) {
        if (typeA == typeB) {
            return true;
        }

        if (typeA == null || typeB == null) {
            return false;
        }

        final String typeNameA = typeA.getName().getLocalPart();
        final String typeNameB = typeB.getName().getLocalPart();
        if (typeNameA == null && typeNameB != null) {
            return false;
        } else if (!typeNameA.equals(typeNameB)) {
            return false;
        }

        final String namespaceA = typeA.getName().getNamespaceURI();
        final String namespaceB = typeB.getName().getNamespaceURI();
        if (namespaceA == null && namespaceB != null) {
            return false;
        } else if (!namespaceA.equals(namespaceB)) {
            return false;
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // about attribut types ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////


    public static Name[] attributeNames(final SimpleFeatureType featureType) {
        final Name[] names = new Name[featureType.getAttributeCount()];
        final int count = featureType.getAttributeCount();
        for (int i = 0; i < count; i++) {
            names[i] = featureType.getDescriptor(i).getName();
        }

        return names;
    }

    /**
     * Traverses the filter and returns any encoutered property names.
     * <p>
     * The feautre type is supplied as contexts used to lookup expressions in cases where the
     * attributeName does not match the actual name of the type.
     * </p>
     */
    public static Name[] attributeNames(final Filter filter, final SimpleFeatureType featureType) {
        if (filter == null) {
            return new Name[0];
        }
        final FilterAttributeExtractor attExtractor = new FilterAttributeExtractor(featureType);
        filter.accept(attExtractor, null);
        final Name[] attributeNames = attExtractor.getAttributeNames();
        return attributeNames;
    }

    /**
     * Traverses the expression and returns any encoutered property names.
     * <p>
     * The feautre type is supplied as contexts used to lookup expressions in cases where the
     * attributeName does not match the actual name of the type.
     * </p>
     */
    public static Name[] attributeNames(final Expression expression, final SimpleFeatureType featureType) {
        if (expression == null) {
            return new Name[0];
        }
        final FilterAttributeExtractor attExtractor = new FilterAttributeExtractor(featureType);
        expression.accept(attExtractor, null);
        final Name[] attributeNames = attExtractor.getAttributeNames();
        return attributeNames;
    }

    public static Object[] defaultValues(final SimpleFeatureType featureType)
            throws IllegalAttributeException {
        return defaultValues(featureType, null);
    }

    public static Object[] defaultValues(final SimpleFeatureType featureType,
            Object[] values) throws IllegalAttributeException {
        if (values == null) {
            values = new Object[featureType.getAttributeCount()];
        } else if (values.length != featureType.getAttributeCount()) {
            throw new ArrayIndexOutOfBoundsException("values");
        }

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            values[i] = defaultValue(featureType.getDescriptor(i));
        }

        return values;
    }

    /**
     * Provides a defautlValue for attributeType.
     *
     * <p>
     * Will return null if attributeType isNillable(), or attempt to use
     * Reflection, or attributeType.parse( null )
     * </p>
     *
     * @param attributeType
     *
     * @return null for nillable attributeType, attempt at reflection
     *
     * @throws IllegalAttributeException If value cannot be constructed for
     *         attribtueType
     */
    public static Object defaultValue(final PropertyDescriptor attributeType)
            throws IllegalAttributeException {

        if(attributeType instanceof AttributeDescriptor){

            final Object value = ((AttributeDescriptor)attributeType).getDefaultValue();

            if (value == null && !attributeType.isNillable()) {
                return null; // sometimes there is no valid default value :-(
                // throw new IllegalAttributeException("Got null default value for non-null type.");
            }
            return value;
        }else{
            return null;
        }
    }

    public static Property defaultProperty(final PropertyDescriptor desc){
        final Object value = defaultValue(desc);
        if(desc instanceof GeometryDescriptor){
            return new DefaultGeometryAttribute(value, (GeometryDescriptor)desc, null);
        }else if(desc instanceof AttributeDescriptor){
            return new DefaultAttribute(value, (AttributeDescriptor)desc, null);
        }else{
            return new DefaultProperty(value, desc);
        }
    }
    
    /**
     * Constructs an empty feature to use as a Template for new content.
     *
     * <p>
     * We may move this functionality to FeatureType.create( null )?
     * </p>
     *
     * @param featureType Type of feature we wish to create
     *
     * @return A new Feature of type featureType
     *
     * @throws IllegalAttributeException if we could not create featureType
     *         instance with acceptable default values
     */
    public static SimpleFeature template(final SimpleFeatureType featureType)
            throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType, defaultValues(featureType), null);
    }

    public static SimpleFeature template(final SimpleFeatureType featureType, final String featureID)
            throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType, defaultValues(featureType), featureID);
    }

    public static SimpleFeature template(final SimpleFeatureType featureType, final Object[] atts)
            throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType, defaultValues(featureType, atts), null);
    }

    public static SimpleFeature template(final SimpleFeatureType featureType, final String featureID,
            final Object[] atts) throws IllegalAttributeException {
        return SimpleFeatureBuilder.build(featureType, defaultValues(featureType, atts), featureID);
    }

   /**
     * Check if the property descriptor is defined as a primary key.
     * @param desc PropertyDescriptor
     * @return true if the descriptor is part of the primary key.
     */
    public static boolean isPartOfPrimaryKey(final PropertyDescriptor desc){
        Map params = desc.getUserData();
        if(params == null){
            return false;
        }

        final Boolean primary = (Boolean) params.get(HintsPending.PROPERTY_IS_IDENTIFIER);
        if(primary != null) return primary;

        return false;
    }

    /**
     * Creates duplicate of feature adjusted to the provided featureType.
     *
     * @param featureType FeatureType requested
     * @param feature Origional Feature from DataStore
     *
     * @return An instance of featureType based on feature
     *
     * @throws IllegalAttributeException If opperation could not be performed
     */
    public static SimpleFeature reType(final SimpleFeatureType featureType, final SimpleFeature feature)
            throws IllegalAttributeException {
        final SimpleFeatureType origional = feature.getFeatureType();

        if (featureType.equals(origional)) {
            return FeatureUtilities.copy(feature);
        }

        final String id = feature.getID();
        final int numAtts = featureType.getAttributeCount();
        final Object[] attributes = new Object[numAtts];
        String xpath;

        for (int i = 0; i < numAtts; i++) {
            final AttributeDescriptor curAttType = featureType.getDescriptor(i);
            xpath = curAttType.getLocalName();
            attributes[i] = FeatureUtilities.duplicate(feature.getAttribute(xpath));
        }

        return SimpleFeatureBuilder.build(featureType, attributes, id);
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // PARAMETERS API MAPPING OPERATIONS ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    public static ComplexType toPropertyType(final ParameterDescriptorGroup descriptor){
        return (ComplexType) toPropertyType((GeneralParameterDescriptor)descriptor);
    }
    
    public static PropertyType toPropertyType(final GeneralParameterDescriptor descriptor){
        
        if(descriptor instanceof ParameterDescriptor){
            final ParameterDescriptor desc = (ParameterDescriptor) descriptor;
            
            final AttributeTypeBuilder atb = new AttributeTypeBuilder();
            atb.setName(desc.getName().getCode());
            atb.setBinding(desc.getValueClass());
            final AttributeType at = atb.buildType();
            
            return at;
            
        }else if (descriptor instanceof ParameterDescriptorGroup){
            final ParameterDescriptorGroup desc = (ParameterDescriptorGroup) descriptor;
            
            final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
            ftb.setName(desc.getName().getCode());
            
            for(GeneralParameterDescriptor sd : desc.descriptors()){
                final PropertyType pt = toPropertyType(sd);
                
                final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
                adb.setName(pt.getName());
                adb.setType(pt);
                adb.setMinOccurs(sd.getMinimumOccurs());
                adb.setMaxOccurs(sd.getMaximumOccurs());
                
                if(sd instanceof ParameterDescriptor){
                    adb.setDefaultValue( ((ParameterDescriptor)sd).getDefaultValue() );                    
                }
                
                ftb.add(adb.buildDescriptor());                
            }
            
            return ftb.buildType();
        }else{
            throw new IllegalArgumentException("Unsupported type : " + descriptor.getClass());
        }
        
    }
    
}
