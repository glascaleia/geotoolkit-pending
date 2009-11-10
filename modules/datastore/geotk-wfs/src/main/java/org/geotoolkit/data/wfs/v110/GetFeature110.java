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

package org.geotoolkit.data.wfs.v110;

import org.geotoolkit.data.wfs.AbstractGetFeature;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class GetFeature110 extends AbstractGetFeature{

    public GetFeature110(String serverURL){
        super(serverURL,"1.1.0");
    }
}
