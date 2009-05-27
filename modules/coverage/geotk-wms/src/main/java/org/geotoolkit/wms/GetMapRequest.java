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
package org.geotoolkit.wms;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.opengis.geometry.Envelope;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface GetMapRequest {

    String[] getLayers();

    void setLayers(String ... layers);

    Envelope getEnvelope();
    
    void setEnvelope(Envelope env);

    Dimension getDimension();

    void setDimension(Dimension dim);

    String getFormat();

    void setFormat(String format);

    String getExceptions();

    void setExceptions(String ex);

    String[] getStyles();

    void setStyles(String ... styles);

    String getSld();

    void setSld(String sld);

    String getSldBody();

    void setSldBody(String sldBody);
    
    boolean getTransparent();

    void setTransparent(boolean transparent);

    Map<String,String> dimensions();

    URL getURL() throws MalformedURLException;
    
}
