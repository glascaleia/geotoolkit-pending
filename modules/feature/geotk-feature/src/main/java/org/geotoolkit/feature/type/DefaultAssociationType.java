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
package org.geotoolkit.feature.type;

import java.util.List;

import org.geotoolkit.util.Utilities;

import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

/**
 * Default implementation of a association type
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class DefaultAssociationType extends DefaultPropertyType<AssociationType> implements AssociationType {

    protected final AttributeType relatedType;

    public DefaultAssociationType(final Name name, final AttributeType referenceType, final boolean isAbstract,
            final List<Filter> restrictions, final AssociationType superType, final InternationalString description){
        super(name, referenceType.getBinding(), isAbstract, restrictions, superType, description);
        this.relatedType = referenceType;

        if (relatedType == null) {
            throw new NullPointerException("relatedType");
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeType getRelatedType() {
        return relatedType;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ relatedType.hashCode();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DefaultAssociationType)) {
            return false;
        }

        AssociationType ass /*(tee hee)*/ = (AssociationType) other;

        return super.equals(ass) && Utilities.equals(relatedType, ass.getRelatedType());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append("; relatedType=[").append(relatedType).append("]").toString();
    }
}
