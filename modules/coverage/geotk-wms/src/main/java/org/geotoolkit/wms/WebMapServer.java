

package org.geotoolkit.wms;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.wms.v111.GetMap111;
import org.geotoolkit.wms.v130.GetMap130;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WMSBindingUtilities;
import org.geotoolkit.wms.xml.WMSVersion;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class WebMapServer {

    private final WMSVersion version;
    private final URL serverURL;
    private AbstractWMSCapabilities capabilities;

    public WebMapServer(URL serverURL, String version) {
        final WMSVersion vers;
        if(version.equals("1.1.1")){
            vers = WMSVersion.v111;
        }else if(version.equals("1.3.0")){
            vers = WMSVersion.v130;
        }else{
            throw new IllegalArgumentException("Unknowned version : "+ version);
        }

        this.version = vers;
        this.serverURL = serverURL;
    }

    public WebMapServer(URL serverURL, WMSVersion version) {
        this.version = version;
        this.serverURL = serverURL;
    }

    public AbstractWMSCapabilities getCapabilities() throws MalformedURLException {
        if(capabilities == null){
            StringBuilder sb = new StringBuilder();
            sb.append(new GetCapabilities(serverURL.toString(), version.getCode()).getURL().toString());  
            
            try {
                URL url = new URL(sb.toString());
                capabilities = WMSBindingUtilities.unmarshall(url, version);
                Logger.getLogger(WebMapServer.class.getName()).log(Level.INFO, "GetCapabilities request and marshalling succeed, url was : " + sb.toString());
            } catch (Exception ex) {
                Logger.getLogger(WebMapServer.class.getName()).log(Level.SEVERE, "GetCapabilities request failed, probably the url requested  is malformed : " + sb.toString(), ex);
            }

        }
        return capabilities;
    }

    public WMSVersion getVersion() {
        return version;
    }

    public GetMapRequest createGetMap(){
        switch(version){
            case v111 : return new GetMap111(serverURL.toString());
            case v130 : return new GetMap130(serverURL.toString());
            default: throw new IllegalArgumentException("Version was not defined");
        }
    }

}
