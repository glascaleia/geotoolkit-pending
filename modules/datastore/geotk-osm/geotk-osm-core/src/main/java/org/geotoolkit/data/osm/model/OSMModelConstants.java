/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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

package org.geotoolkit.data.osm.model;

import com.vividsolutions.jts.geom.Point;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;

import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.LenientFeatureFactory;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.FeatureFactory;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Global OSM constants, defines the namespace and Feature types.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public final class OSMModelConstants {

    static final FeatureFactory FF = FactoryFinder.getFeatureFactory(
            new Hints(Hints.FEATURE_FACTORY, LenientFeatureFactory.class));

    public static final CoordinateReferenceSystem OSM_CRS = DefaultGeographicCRS.WGS84;

    public static final String OSM_NAMESPACE = "http://openstreetmap.org";

    public static final AttributeDescriptor ATT_ID;
    public static final AttributeDescriptor ATT_VERSION;
    public static final AttributeDescriptor ATT_CHANGESET;
    public static final AttributeDescriptor ATT_USER;
    public static final AttributeDescriptor ATT_TIMESTAMP;
    public static final AttributeDescriptor ATT_TAG;
    public static final AttributeDescriptor ATT_RELATION_MEMBER;
    public static final GeometryDescriptor  ATT_NODE_POINT;
    public static final AttributeDescriptor ATT_WAY_NODES;
    public static final AttributeDescriptor ATT_K;
    public static final AttributeDescriptor ATT_V;
    public static final AttributeDescriptor ATT_USER_ID;
    public static final AttributeDescriptor ATT_USER_NAME;
    public static final AttributeDescriptor ATT_MEMBER_ROLE;
    public static final AttributeDescriptor ATT_MEMBER_TYPE;
    public static final AttributeDescriptor ATT_MEMBER_REF;

    public static final ComplexType TYPE_USER;
    public static final ComplexType TYPE_TAG;
    public static final ComplexType TYPE_RELATION_MEMBER;    
    public static final FeatureType TYPE_IDENTIFIED;
    public static final FeatureType TYPE_NODE;
    public static final FeatureType TYPE_WAY;
    public static final FeatureType TYPE_RELATION;

    static final AttributeDescriptor DESC_IDENTIFIED;
    static final AttributeDescriptor DESC_NODE;
    static final AttributeDescriptor DESC_WAY;
    static final AttributeDescriptor DESC_RELATION;

    static {
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();

        ATT_ID = adb.create(new DefaultName(OSM_NAMESPACE, "id"), Long.class,1,1,false,null);
        ATT_VERSION = adb.create(new DefaultName(OSM_NAMESPACE, "version"), Integer.class,1,1,false,null);
        ATT_CHANGESET = adb.create(new DefaultName(OSM_NAMESPACE, "changeset"), Integer.class,1,1,false,null);
        ATT_TIMESTAMP = adb.create(new DefaultName(OSM_NAMESPACE, "timestamp"), Integer.class,1,1,false,null);

        //------------------- USER TYPE ----------------------------------------
        ATT_USER_ID = adb.create(new DefaultName(OSM_NAMESPACE, "id"), Integer.class, 1,1,false,null);
        ATT_USER_NAME = adb.create(new DefaultName(OSM_NAMESPACE, "name"), String.class, 1,1,true,null);

        ftb.reset();
        ftb.setName(OSM_NAMESPACE, "User");
        ftb.add(ATT_USER_ID);
        ftb.add(ATT_USER_NAME);
        TYPE_USER = ftb.buildType();
        ATT_USER = adb.create(TYPE_USER,new DefaultName(OSM_NAMESPACE, "user"),null,1,1,false,null);

        //------------------- TAG TYPE -----------------------------------------
        ATT_K = adb.create(new DefaultName(OSM_NAMESPACE, "k"), String.class, 1,1,true,null);
        ATT_V = adb.create(new DefaultName(OSM_NAMESPACE, "v"), String.class, 1,1,true,null);

        ftb.reset();
        ftb.setName(OSM_NAMESPACE, "Tag");
        ftb.add(ATT_K);
        ftb.add(ATT_V);
        TYPE_TAG = ftb.buildType();
        ATT_TAG = adb.create(TYPE_TAG,new DefaultName(OSM_NAMESPACE, "tags"),null,0,Integer.MAX_VALUE,true,null);

        //------------------- IDENTIFIED TYPE ----------------------------------
        ftb.reset();
        ftb.setName(OSM_NAMESPACE, "Identified");
        ftb.add(ATT_ID);
        ftb.add(ATT_VERSION);
        ftb.add(ATT_CHANGESET);
        ftb.add(ATT_USER);
        ftb.add(ATT_TIMESTAMP);
        ftb.add(ATT_TAG);
        TYPE_IDENTIFIED = ftb.buildFeatureType();

        //------------------- NODE TYPE ----------------------------------------
        ATT_NODE_POINT = (GeometryDescriptor) adb.create(new DefaultName(OSM_NAMESPACE, "point"), Point.class, OSM_CRS,1,1,false,null);
        ftb.reset();
        ftb.setSuperType(TYPE_IDENTIFIED);
        ftb.setName(OSM_NAMESPACE, "Node");
        ftb.add(ATT_ID);
        ftb.add(ATT_VERSION);
        ftb.add(ATT_CHANGESET);
        ftb.add(ATT_USER);
        ftb.add(ATT_TIMESTAMP);
        ftb.add(ATT_TAG);
        ftb.add(ATT_NODE_POINT);
        ftb.setDefaultGeometry(ATT_NODE_POINT.getName());
        TYPE_NODE = ftb.buildFeatureType();
        

        //------------------- WAY TYPE -----------------------------------------
        ATT_WAY_NODES = adb.create(new DefaultName(OSM_NAMESPACE, "nodes"),Long.class,0,Integer.MAX_VALUE,true,null);

        ftb.reset();
        ftb.setSuperType(TYPE_IDENTIFIED);
        ftb.setName(OSM_NAMESPACE, "Way");
        ftb.add(ATT_ID);
        ftb.add(ATT_VERSION);
        ftb.add(ATT_CHANGESET);
        ftb.add(ATT_USER);
        ftb.add(ATT_TIMESTAMP);
        ftb.add(ATT_TAG);
        ftb.add(ATT_WAY_NODES);
        TYPE_WAY = ftb.buildFeatureType();

        //------------------- RELATION MEMBER TYPE -----------------------------
        ATT_MEMBER_ROLE = adb.create(new DefaultName(OSM_NAMESPACE, "role"),String.class,1,1,true,null);
        ATT_MEMBER_TYPE = adb.create(new DefaultName(OSM_NAMESPACE, "type"),MemberType.class,1,1,true,null);
        ATT_MEMBER_REF = adb.create(new DefaultName(OSM_NAMESPACE, "ref"),Long.class,1,1,false,null);

        ftb.reset();
        ftb.setName(OSM_NAMESPACE, "Member");
        ftb.add(ATT_MEMBER_ROLE);
        ftb.add(ATT_MEMBER_TYPE);
        ftb.add(ATT_MEMBER_REF);
        TYPE_RELATION_MEMBER = ftb.buildType();

        //------------------- RELATION TYPE ------------------------------------
        ATT_RELATION_MEMBER = adb.create(TYPE_RELATION_MEMBER,new DefaultName(OSM_NAMESPACE, "members"),0,Integer.MAX_VALUE,true,null);

        ftb.reset();
        ftb.setSuperType(TYPE_IDENTIFIED);
        ftb.setName(OSM_NAMESPACE, "Relation");
        ftb.add(ATT_ID);
        ftb.add(ATT_VERSION);
        ftb.add(ATT_CHANGESET);
        ftb.add(ATT_USER);
        ftb.add(ATT_TIMESTAMP);
        ftb.add(ATT_TAG);
        ftb.add(ATT_RELATION_MEMBER);
        TYPE_RELATION = ftb.buildFeatureType();

        DESC_IDENTIFIED = adb.create( TYPE_IDENTIFIED, TYPE_IDENTIFIED.getName(), 1, 1, true, null);
        DESC_NODE = adb.create( TYPE_NODE, TYPE_NODE.getName(), 1, 1, true, null);
        DESC_WAY = adb.create( TYPE_WAY, TYPE_WAY.getName(), 1, 1, true, null);
        DESC_RELATION = adb.create( TYPE_RELATION, TYPE_RELATION.getName(), 1, 1, true, null);
    }

    private OSMModelConstants(){}

}
