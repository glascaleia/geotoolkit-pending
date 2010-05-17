/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import com.vividsolutions.jts.geom.Geometry;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.simple.DefaultSimpleSchema;
import org.geotoolkit.feature.type.BasicFeatureTypes;
import org.geotoolkit.feature.type.DefaultFeatureTypeFactory;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.converter.Classes;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.feature.type.Schema;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * A builder for feature types.
 * <p>
 * Simple Usage:
 * <pre>
 *  <code>
 *  //create the builder
 *  FeatureTypeBuilder builder = new FeatureTypeBuilder();
 *
 *  //set global state
 *  builder.setName( "testType" );
 *
 *  //add attributes
 *  builder.add( "intProperty", Integer.class );
 *  builder.add( "stringProperty", String.class );
 *  builder.add( "pointProperty", Point.class );
 * *
 *  //build the type
 *  FeatureType featureType = builder.buildFeatureType();
 *  </code>
 * </pre>
 * <p>
 * A default geometry for the feature type can be specified explictly via
 * {@link #setDefaultGeometry(String)}. However if one is not set the first
 * geometric attribute ({@link GeometryType}) added will be resulting default.
 * So if only specifying a single geometry for the type there is no need to
 * call the method. However if specifying multiple geometries then it is good
 * practice to specify the name of the default geometry type. For instance:
 * <code>
 * 	<pre>
 *  builder.add( "pointProperty", Point.class, crs );
 *  builder.add( "lineProperty", LineString.class, crs );
 *  builder.add( "polygonProperty", "polygonProperty", crs );
 *
 *  builder.setDefaultGeometry( "lineProperty" );
 * 	</pre>
 * </code>
 * </p>
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class FeatureTypeBuilder {

    /**
     * Map of user data that can be used when creating a property descriptor
     * to notify that the field should be used as a primary key. not all datastore
     * can handle this.
     */
    public static final Map PRIMARY_KEY = Collections.singletonMap(HintsPending.PROPERTY_IS_IDENTIFIER, Boolean.TRUE);

    /*
     * Factories an builders.
     */
    protected final FeatureTypeFactory factory;
    /**
     * Map of java class bound to properties types.
     */
    protected final Map<Class,AttributeType> bindings = new HashMap<Class, AttributeType>();

    protected final List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>(){

        @Override
        public boolean add(PropertyDescriptor e) {
            if(e == null){
                throw new NullPointerException("Property descriptor can not be null.");
            }
            return super.add(e);
        }

        @Override
        public void add(int i, PropertyDescriptor e) {
            if(e == null){
                throw new NullPointerException("Property descriptor can not be null.");
            }
            super.add(i, e);
        }

        @Override
        public boolean addAll(Collection<? extends PropertyDescriptor> clctn) {
            for(PropertyDescriptor att : clctn){
                if(att == null){
                    throw new NullPointerException("Property descriptor can not be null.");
                }
            }
            return super.addAll(clctn);
        }

        @Override
        public boolean addAll(int i, Collection<? extends PropertyDescriptor> clctn) {
            for(PropertyDescriptor att : clctn){
                if(att == null){
                    throw new NullPointerException("Property descriptor can not be null.");
                }
            }
            return super.addAll(i, clctn);
        }

    };
    protected final List<Filter> restrictions = new ArrayList<Filter>();
    protected Name name = null;
    protected InternationalString description;
    protected Name defaultGeometry;
    protected boolean isAbstract = false;
    protected AttributeType superType;

    /**
     * Constructs the builder.
     */
    public FeatureTypeBuilder() {
        this(null);
    }

    /**
     * Constructs the builder specifying the factory for creating feature and
     * feature collection types.
     */
    public FeatureTypeBuilder(final FeatureTypeFactory factory) {
        if(factory == null){
            this.factory = new DefaultFeatureTypeFactory();
        }else{
            this.factory = factory;
        }

        setBindings(new DefaultSimpleSchema());
        reset();
    }

    public FeatureTypeFactory getFeatureTypeFactory(){
        return factory;
    }

    /**
     * Initializes the builder with state from a pre-existing feature type.
     */
    public void copy(final FeatureType type) {
        if (type == null) {
            throw new NullPointerException("Can not copy information from a Null type.");
        }

        name = type.getName();
        description = type.getDescription();
        
        restrictions.clear();
        restrictions.addAll(type.getRestrictions());

        properties.clear();
        properties.addAll(type.getDescriptors());

        isAbstract = type.isAbstract();
        superType = type.getSuper();
    }

    /**
     * Completely resets all builder state.
     *
     */
    public void reset() {
        name = null;
        description = null;
        restrictions.clear();
        properties.clear();
        isAbstract = false;
        superType = null;
    }

    public void setName(String localPart){
        setName(new DefaultName(localPart));
    }

    public void setName(String namespace, String localPart){
        setName(new DefaultName(namespace,localPart));
    }

    public void setName(String namespace, String separator, String localPart){
        setName(new DefaultName(namespace,separator,localPart));
    }

    /**
     * Sets the local name and namespace uri of the built type.
     */
    public void setName(final Name name) {
        this.name = name;
    }

    /**
     * The name of the built type.
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the description of the built type.
     */
    public void setDescription(final InternationalString description) {
        this.description = description;
    }

    /**
     * The description of the built type.
     */
    public InternationalString getDescription() {
        return description;
    }

    /**
     * Sets the name of the default geometry attribute of the built type.
     * You should use the the method with Name parameter instead.
     * This method will create a Name without namespace and refer to the first
     * attribut which local name part match this string.
     */
    public void setDefaultGeometry(final String defaultGeometryName) {
        for(PropertyDescriptor desc : properties){
            if(desc.getName().getLocalPart().equals(defaultGeometryName)){
                this.defaultGeometry = desc.getName();
                return;
            }
        }
        throw new IllegalArgumentException("No matching property for name "+ defaultGeometryName);
    }

    /**
     * Sets the name of the default geometry attribute of the built type.
     */
    public void setDefaultGeometry(final Name defaultGeometryName) {
        for(PropertyDescriptor desc : properties){
            if(desc.getName().equals(defaultGeometryName)){
                this.defaultGeometry = desc.getName();
                return;
            }
        }
        throw new IllegalArgumentException("No matching attributs for name "+ defaultGeometryName);
    }

    /**
     * The name of the default geometry attribute of the built type.
     */
    public Name getDefaultGeometry() {
        return defaultGeometry;
    }

    /**
     * Sets the flag controlling if the resulting type is abstract.
     */
    public void setAbstract(final boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    /**
     * The flag controlling if the resulting type is abstract.
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * Sets the super type of the built type.
     */
    public void setSuperType(final AttributeType superType) {
        this.superType = superType;
    }

    /**
     * The super type of the built type.
     */
    public AttributeType getSuperType() {
        return superType;
    }

    /**
     * Specifies an attribute type binding.
     * <p>
     * This method is used to associate an attribute type with a java class.
     * The class is retreived from <code>type.getBinding()</code>. When the
     * {@link #add(String, Class)} method is used to add an attribute to the
     * type being built, this binding is used to locate the attribute type.
     * </p>
     *
     * @param type The attribute type.
     */
    public void addBinding(final AttributeType type) {
        bindings.put(type.getBinding(), type);
    }

    /**
     * Specifies a number of attribute type bindings.
     *
     * @param schema The schema containing the attribute types.
     *
     * @see #addBinding(org.opengis.feature.type.AttributeType)
     */
    public void addBindings(final Schema schema) {
        for (Iterator itr = schema.values().iterator(); itr.hasNext();) {
            AttributeType type = (AttributeType) itr.next();
            addBinding(type);
        }
    }

    /**
     * Specifies a number of attribute type bindings clearing out all existing
     * bindings.
     *
     * @param schema The schema contianing attribute types.
     *
     * @see #addBinding(org.opengis.feature.type.AttributeType)
     */
    public void setBindings(final Schema schema) {
        bindings.clear();
        addBindings(schema);
    }

    /**
     * Looks up an attribute type which has been bound to a class.
     *
     * @param binding The class.
     *
     * @return AttributeType The bound attribute type.
     */
    public AttributeType getBinding(final Class<?> binding) {
        return bindings.get(binding);
    }

    public void add(final String name, final Class binding){
        add(new DefaultName(name),binding);
    }

    public void add(final String name, final Class binding, int min, int max,
            boolean nillable, Map<Object,Object> userData) {
        add(new DefaultName(name),binding,min,max,nillable,userData);
    }

    /**
     * Adds a new attribute w/ provided name and class.
     *
     * <p>
     * The provided class is used to locate an attribute type binding previously
     * specified by {@link #addBinding(AttributeType)},{@link #addBindings(Schema)},
     * or {@link #setBindings(Schema)}.
     * </p>
     * <p>
     * If not such binding exists then an attribute type is created on the fly.
     * </p>
     * @param name The name of the attribute.
     * @param binding The class the attribute is bound to.
     */
    public void add(final Name name, final Class binding) {
        add(name,binding,1,1,true,null);
    }

    public void add(final Name name, final Class binding, int min, int max,
            boolean nillable, Map<Object,Object> userData) {
        add(name,binding,null,min,max,nillable,userData);
    }

    public void add(final String name, final Class binding, final CoordinateReferenceSystem crs){
        add(new DefaultName(name),binding,crs);
    }

    /**
     * Adds a new geometric attribute w/ provided name, class, and spatial
     * reference system identifier
     * <p>
     * The <tt>srs</tt> parameter may be <code>null</code>.
     * </p>
     * @param name The name of the attribute.
     * @param binding The class that the attribute is bound to.
     * @param srs The srs of of the geometry, can not be <code>null</code>.
     */
    public void add(final Name name, final Class binding, final String srs) {
        add(name, binding, decode(srs));
    }

    /**
     * Adds a new geometric attribute w/ provided name, class, and spatial
     * reference system identifier
     * <p>
     * The <tt>srid</tt> parameter may be <code>null</code>.
     * </p>
     * @param name The name of the attribute.
     * @param binding The class that the attribute is bound to.
     * @param srid The srid of of the geometry, may be <code>null</code>.
     */
    public void add(final Name name, final Class binding, final Integer srid) {
        add(name, binding, decode("EPSG:" + srid));
    }

    /**
     * Adds a new geometric attribute w/ provided name, class, and coordinate
     * reference system.
     * <p>
     * The <tt>crs</tt> parameter may be <code>null</code>.
     * </p>
     * @param name The name of the attribute.
     * @param binding The class that the attribute is bound to.
     * @param crs The crs of of the geometry, can not be <code>null</code>.
     */
    public void add(final Name name, final Class binding, final CoordinateReferenceSystem crs) {
        add(name,binding,crs,1,1,true,null);
    }

    public void add(final Name name, final Class binding, final CoordinateReferenceSystem crs,
            int min, int max, boolean nillable, Map<Object,Object> userData) {

        final PropertyType at;
        if(Geometry.class.isAssignableFrom(binding) ||
                org.opengis.geometry.Geometry.class.isAssignableFrom(binding)){
            at = factory.createGeometryType(name, binding, crs, false, false, null, null, null);
        }
//TODO must check that we can allow collection as simple attribut types
//        else if(Collection.class.isAssignableFrom(binding) ||
//                org.opengis.geometry.Geometry.class.isAssignableFrom(binding)){
//            throw new IllegalArgumentException("Binding class is : "+ binding +" this is a Complex type. Create a complex type using the factory");
//        }
        else{
            //non geometric field
            at = factory.createAttributeType(name, binding, false, false, null, null, null);
        }

        add(at,name,crs,min,max,nillable,userData);
    }

    public void add(final PropertyType at, final Name name, final CoordinateReferenceSystem crs,
            int min, int max, boolean nillable, Map<Object,Object> userData){
        Object defaultValue = null;
        if(!nillable){
            //search for the best default value.
            try {
                defaultValue = FeatureUtilities.defaultValue(at.getBinding());
            } catch (Exception e) {
                //do nothing
            }
        }

        final PropertyDescriptor desc;
        if(at instanceof GeometryType){
            desc = factory.createGeometryDescriptor((GeometryType)at, name, min, max, nillable, defaultValue);
        }else if(at instanceof AttributeType){
            desc = factory.createAttributeDescriptor((AttributeType)at, name, min, max, nillable, defaultValue);
        }else{
            throw new IllegalArgumentException("Property type is : "+ at.getClass() +" This type is not supported yet.");
        }

        if(userData != null){
            desc.getUserData().putAll(userData);
        }

        add(desc);
    }

    /**
     * Adds a descriptor directly to the builder.
     * <p>
     * Use of this method is discouraged. Consider using {@link #add(String, Class)}.
     * </p>
     */
    public void add(final PropertyDescriptor descriptor) {
        properties.add(descriptor);
    }

    /**
     * Adds a descriptor to the builder by index.
     * <p>
     * Use of this method is discouraged. Consider using {@link #add(String, Class)}.
     * </p>
     */
    public void add(final int index, final PropertyDescriptor descriptor) {
        properties.add(index, descriptor);
    }

    /**
     * Adds a list of descriptors directly to the builder.
     * <p>
     * Use of this method is discouraged. Consider using {@link #add(String, Class)}.
     * </p>
     */
    public void addAll(final List<? extends PropertyDescriptor> descriptors) {
        for (PropertyDescriptor ad : descriptors) {
            add(ad);
        }
    }

    /**
     * Adds an array of descriptors directly to the builder.
     * <p>
     * Use of this method is discouraged. Consider using {@link #add(String, Class)}.
     * </p>
     */
    public void addAll(final PropertyDescriptor ... descriptors) {
        for (PropertyDescriptor ad : descriptors) {
            add(ad);
        }
    }

    /**
     * Removes an attribute from the builder
     *
     * @param attributeName the name of the AttributeDescriptor to remove
     *
     * @return the AttributeDescriptor with the name attributeName
     * @throws IllegalArgumentException if there is no AttributeDescriptor with the name attributeName
     */
    public PropertyDescriptor remove(final String attributeName) {
        for(final PropertyDescriptor att : properties){
            if(att.getName().getLocalPart().equals(attributeName)){
                properties.remove(att);
                return att;
            }
        }

        throw new IllegalArgumentException(attributeName + " is not an existing attribute descriptor in this builder");
    }

    /**
     * Directly sets the list of attributes.
     * @param properties the new list of attributes, or null to reset the list
     */
    public void setProperties(final List<PropertyDescriptor> properties) {
        this.properties.clear();
        if (properties != null) {
            this.properties.addAll(properties);
        }
    }

    /**
     * Directly sets the list of attributes.
     * @param attributes the new list of attributes, or null to reset the list
     */
    public void setProperties(final PropertyDescriptor ... attributes) {
        setProperties(Arrays.asList(attributes));
    }

    /**
     * Builds a feature type from compiled state.
     * <p>
     * After the type is built the running list of attributes is cleared.
     * </p>
     * @return The built feature type.
     */
    public FeatureType buildFeatureType() {
        return buildFeatureType(false);
    }

    public SimpleFeatureType buildSimpleFeatureType() {
        return (SimpleFeatureType) buildFeatureType(true);
    }

    private FeatureType buildFeatureType(boolean simple) {
        GeometryDescriptor defaultGeometry = null;

        if(superType == null){
            superType = BasicFeatureTypes.FEATURE;
        }

        //was a default geometry set?
        if (this.defaultGeometry != null) {

            for(final PropertyDescriptor desc : properties){
                if(desc.getName().equals(this.defaultGeometry)){
                    //ensure the attribute is a geometry attribute
                    if(!(desc instanceof GeometryDescriptor)){
                        throw new IllegalStateException("Default geometry : "+ this.defaultGeometry + " is not a GeometryDescriptor.");
                    }
                    defaultGeometry = (GeometryDescriptor) desc;
                    break;
                }
            }

            if(defaultGeometry == null){
                throw new IllegalStateException("Default geometry : "+ this.defaultGeometry + " can not be found in the attributs list.");
            }
        }

        if (defaultGeometry == null) {
            //none was set by name, look for first geometric type
            for (final PropertyDescriptor att : properties) {
                if (att instanceof GeometryDescriptor) {
                    defaultGeometry = (GeometryDescriptor) att;
                    break;
                }
            }
        }

        //verify if we have a simple feature type
        boolean isSimple = true;
        for(PropertyDescriptor desc : properties){
            //to be simple property must have min = 1 and max 1
            if(desc.getMinOccurs() != 1 || desc.getMaxOccurs() != 1){
                if(simple){
                    throw new IllegalArgumentException("Property "+desc.getName()+"must have min = 1 and max = 1");
                }
                isSimple = false;
            }

            //to be simple property must be an attribut
            final Set<Class<?>> ints = Classes.getAllInterfaces(desc.getType().getClass());
            boolean found = false;
            for(Class<?> c : ints){
                if(AttributeType.class.isAssignableFrom(c)){
                    if(found){
                        isSimple = false;
                        break;
                    }else{
                        if(!(GeometryType.class.isAssignableFrom(c))){
                            found = true;
                        }
                    }
                }
            }
            if(!found){
                isSimple = false;
                break;
            }
        }

        if(simple && !isSimple){
            throw new IllegalArgumentException("Property descriptors are not all Attribut Descriptor. Can not create a simple type");
        }

        if(isSimple){
            final List<AttributeDescriptor> descs = new ArrayList<AttributeDescriptor>();
            for(PropertyDescriptor desc : properties){
                descs.add((AttributeDescriptor)desc);
            }
            return factory.createSimpleFeatureType(
                    name, descs, defaultGeometry, isAbstract,
                    restrictions, superType, description);
        }else{
            return factory.createFeatureType(
                name, properties, defaultGeometry, isAbstract,
                restrictions, superType, description);
        }
    }

    public ComplexType buildType(){
        return factory.createComplexType(name,properties,true,isAbstract,
                restrictions,superType,description);
    }

    /**
     * Decodes a srs, supplying a useful error message if there is a problem.
     */
    private static CoordinateReferenceSystem decode(final String srs) {
        try {
            return CRS.decode(srs);
        } catch (Exception e) {
            String msg = "SRS '" + srs + "' unknown:" + e.getLocalizedMessage();
            throw (IllegalArgumentException) new IllegalArgumentException(msg).initCause(e);
        }
    }

    /**
     * @param original
     * @param types
     * @return SimpleFeatureType
     */
    public static FeatureType retype(final FeatureType original, final Name[] types) {
        final FeatureTypeBuilder b = new FeatureTypeBuilder();

        //initialize the builder
        b.copy(original);

        //clear the attributes
        b.properties.clear();

        //add attributes in order
        for (int i=0; i<types.length; i++) {
            if(types[i] == null){
                throw new NullPointerException("Retype can not ask for 'null' property names.");
            }

            final PropertyDescriptor desc = original.getDescriptor(types[i]);

            if(desc != null){
                b.add(desc);
            }else{
                final StringBuilder sb = new StringBuilder();
                sb.append("Could not retype, property : ").append(types[i]);
                sb.append(" could not be found in originale feature type,");
                sb.append("\n Original type : "+ original);
                throw new IllegalArgumentException(sb.toString());
            }
        }

        if(original instanceof SimpleFeatureType){
            return b.buildSimpleFeatureType();
        }else{
            return b.buildFeatureType();
        }
        
    }

}
