/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.resources.jaxb.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.geotoolkit.service.ServiceImpl;
import org.opengis.service.Service;

/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL: http://svn.geotools.org/trunk/modules/library/metadata/src/main/java/org/geotools/resources/jaxb/metadata/ServiceAdapter.java $
 * @author Guilhem Legal
 */
public class ServiceAdapter extends XmlAdapter<ServiceAdapter, Service> {
    
    private Service service;
    
    /**
     * Empty constructor for JAXB only.
     */
    private ServiceAdapter() {
    }

    /**
     * Wraps an Service value with a {@code CI_Service} tags at marshalling-time.
     *
     * @param service The service value to marshall.
     */
    protected ServiceAdapter(final Service service) {
        this.service = service;
    }

    /**
     * Returns the Service value covered by a {@code CI_Service} tags.
     *
     * @param value The value to marshall.
     * @return The adapter which covers the service value.
     */
    protected ServiceAdapter wrap(final Service value) {
        return new ServiceAdapter(value);
    }

    /**
     * Returns the {@link ServiceImpl} generated from the service value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "SV_Service")
    public ServiceImpl getService() {
        return (service instanceof ServiceImpl) ?
            (ServiceImpl)service : new ServiceImpl(service);
    }

    /**
     * Sets the value for the {@link ServiceImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setService(final ServiceImpl Service) {
        this.service = Service;
    }

    /**
     * Does the link between service red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this service value.
     * @return A java object which represents the service value.
     */
    @Override
    public Service unmarshal(ServiceAdapter value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.service;
    }

    /**
     * Does the link between java object and the way they will be marshalled into
     * an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The bound type value, here the interface.
     * @return The adapter for this interface.
     */
    @Override
    public ServiceAdapter marshal(Service value) throws Exception {
        return new ServiceAdapter(value);
    }

    
    
}
