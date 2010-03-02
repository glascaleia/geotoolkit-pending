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

package org.geotoolkit.data.query;

/**
 * Gives some information about the capabilities of the datastore.
 * Some datastore might not be able to handle sort by parameters for exemple.
 *
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public interface QueryCapabilities {

    /**
     * 
     * @return true if the datastore can handle queries with join operations.
     */
    boolean handleCrossQuery();

}