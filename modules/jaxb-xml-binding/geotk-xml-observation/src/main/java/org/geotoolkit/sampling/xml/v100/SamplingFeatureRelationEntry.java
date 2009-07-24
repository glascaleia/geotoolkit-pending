/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.sampling.xml.v100;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.opengis.observation.sampling.SamplingFeatureRelation;
import org.opengis.util.GenericName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlType(name="SamplingFeatureRelation")
public class SamplingFeatureRelationEntry implements SamplingFeatureRelation {
    
    // JAXBISSUE private GenericNameEntry role;
    
    private SamplingFeatureEntry target;
    
    @XmlTransient
    private String name;
    /**
     * Constructeur vide utilisé par JAXB
     */
    private SamplingFeatureRelationEntry() {}
    
    /**
     */
    public SamplingFeatureRelationEntry(String name, SamplingFeatureEntry target) {
        this.name = name;
        //this.role   = role;
        this.target = target;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GenericName getRole(){
        throw new UnsupportedOperationException("Not supported yet.");
        //return role;
    }

    public String getName() {
        return name;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeatureEntry getTarget(){
        return target;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SamplingFeatureRelationEntry other = (SamplingFeatureRelationEntry) obj;
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("SamplingFeatureRelationEntry[");
        if (name != null) {
            s.append("name:").append(name).append('\n');
        }
        if (target != null) {
            s.append("target:").append(target).append('\n');
        }
        return s.toString();
    }

}
