/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.jdbc.reverse;

import java.util.ArrayList;
import java.util.Collection;
import org.geotoolkit.jdbc.fid.PrimaryKey;
import org.geotoolkit.util.StringUtilities;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class TableMetaModel {

    final String name;
    ComplexType baseType;
    SimpleFeatureType simpleType;
    FeatureType complexType;
    PrimaryKey key;
    //those are 0:1 relations
    final Collection<RelationMetaModel> importedKeys = new ArrayList<RelationMetaModel>();
    //those are 0:N relations
    final Collection<RelationMetaModel> exportedKeys = new ArrayList<RelationMetaModel>();

    public TableMetaModel(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(name);
        if (!importedKeys.isEmpty()) {
            sb.append("\n Imported Keys \n");
            sb.append(StringUtilities.toStringTree(importedKeys));
        }
        if (!exportedKeys.isEmpty()) {
            sb.append("\n Exported Keys \n");
            sb.append(StringUtilities.toStringTree(exportedKeys));
        }
        return sb.toString();
    }
}
