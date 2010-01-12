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

package org.geotoolkit.data;

import java.util.EventObject;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 *
 * @todo work in progress
 * @author Johann Sorel (Geomatys)
 */
public class StorageManagementEvent extends EventObject{

    public static enum Type{
        ADD,
        UPDATE,
        DELETE
    };

    private final Type type;
    private final Name name;
    private final FeatureType oldType;
    private final FeatureType newType;

    private StorageManagementEvent(Object source, Type type, Name name, FeatureType oldtype, FeatureType newtype){
        super(source);

        if(type == null){
            throw new NullPointerException("Type can not be null.");
        }
        if(name == null){
            throw new NullPointerException("Name can not be null.");
        }
        if(oldtype == null){
            throw new NullPointerException("Old feature type can not be null.");
        }
        if(newtype == null){
            throw new NullPointerException("New feature type can not be null.");
        }


        this.type = type;
        this.name = name;
        this.oldType = oldtype;
        this.newType = newtype;
    }

    public Type getType() {
        return type;
    }

    public Name getFeatureTypeName() {
        return name;
    }

    public FeatureType getNewFeatureType() {
        return newType;
    }

    public FeatureType getOldFeatureType() {
        return oldType;
    }

    public static StorageManagementEvent createAddEvent(Object source, Name name, FeatureType type){
        return new StorageManagementEvent(source, Type.ADD, name, type, type);
    }

    public static StorageManagementEvent createUpdateEvent(Object source, Name name, FeatureType oldType, FeatureType newType){
        return new StorageManagementEvent(source, Type.UPDATE, name, oldType, newType);
    }

    public static StorageManagementEvent createDeleteEvent(Object source, Name name, FeatureType type){
        return new StorageManagementEvent(source, Type.DELETE, name, type, type);
    }

    public static StorageManagementEvent resetSource(Object source, StorageManagementEvent event){
        return new StorageManagementEvent(source, event.type, event.name, event.oldType, event.newType);
    }

}
