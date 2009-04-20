
package org.geotoolkit.wms;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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

    Map<String,String> dimensions();

    URL getURL() throws MalformedURLException;
    
}
