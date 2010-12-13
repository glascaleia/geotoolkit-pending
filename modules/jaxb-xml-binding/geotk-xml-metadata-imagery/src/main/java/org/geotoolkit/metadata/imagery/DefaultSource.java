/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008-2010, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2010, Geomatys
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

package org.geotoolkit.metadata.imagery;

import javax.xml.bind.annotation.XmlRootElement;
import org.geotoolkit.xml.Namespaces;

/**
 * The Source implementation extract from ISO 19115-2 extending the one from ISO 19115.
 *
 * @author Guilhem Legal (Geomatys)
 *
 */
@XmlRootElement(name = "LE_Source", namespace = Namespaces.GMI)
public class DefaultSource extends org.geotoolkit.metadata.iso.lineage.DefaultSource {

}
