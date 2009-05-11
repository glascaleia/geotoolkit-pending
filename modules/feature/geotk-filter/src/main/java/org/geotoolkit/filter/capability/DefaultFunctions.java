/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotoolkit.filter.capability;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.capability.Functions;

/**
 * Immutable functions.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultFunctions implements Functions{

    private final Map<String,FunctionName> functions = new HashMap<String, FunctionName>();

    public DefaultFunctions(FunctionName[] functions) {
        if(functions == null || functions.length == 0){
            throw new IllegalArgumentException("Functions must not be null or empty");
        }
        for(FunctionName fn : functions){
            this.functions.put(fn.getName(), fn);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<FunctionName> getFunctionNames() {
        return functions.values();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FunctionName getFunctionName(String name) {
        return functions.get(name);
    }

}
