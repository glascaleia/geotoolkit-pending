/*$************************************************************************************************
 **
 ** $Id: 
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/trunk/geoapi/src/main/java/org/opengis/services/PlatformNeutralServiceSpecification.java $
 **
 ** Copyright (C) 2004-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/

package org.opengis.service;

import org.opengis.annotation.UML;
import static org.opengis.annotation.Obligation.*;
import static org.opengis.annotation.Specification.*;

/**
 * Provides the abstract definition of a specific type of service but does not specify the implementation of the service.
 * 
 * @author <A HREF="http://www.opengeospatial.org/standards/as# 02-112">ISO 19119</A>
 * @author Guilhem Legal
 * 
 * @module pending
 * * @since GeoAPI 2.1
 */
//@UML(identifier="SV_PlatformNeutralServiceSpecification", specification=ISO_19119)
public interface PlatformNeutralServiceSpecification extends ServiceSpecification {
    
    /**
     * 
     */
    //@UML(identifier="serviceType", obligation=MANDATORY, specification=ISO_19119)
    ServiceType getServiceType();
    
    /**
     * 
     */
    //@UML(identifier="implSpec", obligation=MANDATORY, specification=ISO_19119)
    PlatformSpecificServiceSpecification getImplSpec();

}
