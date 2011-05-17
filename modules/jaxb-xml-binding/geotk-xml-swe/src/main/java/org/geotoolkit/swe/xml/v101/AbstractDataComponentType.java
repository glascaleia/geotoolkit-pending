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
package org.geotoolkit.swe.xml.v101;

import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.gml.xml.v311.AbstractGMLType;
import org.geotoolkit.util.Utilities;
import org.geotoolkit.swe.xml.AbstractDataComponent;
import org.geotoolkit.util.ComparisonMode;
/**
 *
 * @version $Id:
 * @author Guilhem Legal
 * @module pending
 */
@XmlSeeAlso({AbstractDataRecordType.class, TimeType.class, BooleanType.class, QuantityType.class, Text.class})
@XmlType(name="AbstractDataComponent")
@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractDataComponentType extends AbstractGMLType implements AbstractDataComponent {
    
    @XmlAttribute
    private Boolean fixed;
    
    /**
     * definition of the record.
     */
    @XmlAttribute
    private URI definition;
    
    /**
     * Constructor used by jaxb.
     */
    AbstractDataComponentType() {}

    public AbstractDataComponentType(final AbstractDataComponent component) {
        super(component);
        if (component != null) {
            if (component.getDefinition() instanceof String) {
                this.definition = URI.create((String)component.getDefinition());
            } else if (component.getDefinition() instanceof URI) {
                this.definition = (URI) component.getDefinition();
            }
            this.fixed      = component.isFixed();
        }
    }

    public AbstractDataComponentType(final String definition) {
        if (definition != null) {
            this.definition = URI.create(definition);
        }
    }
    
    /**
     * a simple constructor used by the sub classes to initialize l'Type.
     */
    public AbstractDataComponentType(final String id, final String definition, final Boolean fixed) {
        super(id);
        if (definition != null) {
            this.definition = URI.create(definition);
        }
        this.fixed      = fixed;
    }

    /**
     * Return the identifier of this data record.
     */
    @Override
    public String getName() {
        if (super.getName() != null) {
            return super.getName();
        }
        return super.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getDefinition() {
        return definition;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isFixed() {
        return fixed;
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(final String definition) {
        this.definition = URI.create(definition);
    }
    
    /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (object == this) {
            return true;
        }
        if (object instanceof AbstractDataComponentType && super.equals(object)) {
            final AbstractDataComponentType that = (AbstractDataComponentType) object;
            return Utilities.equals(this.definition, that.definition) &&
                   Utilities.equals(this.fixed,      that.fixed);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + super.hashCode();
        hash = 53 * hash + (this.fixed != null ? this.fixed.hashCode() : 0);
        hash = 53 * hash + (this.definition != null ? this.definition.hashCode() : 0);
        return hash;
    }

    
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
       
        if (definition != null)
            s.append(" definition = ").append(definition);
        
        s.append(" fixed = ").append(fixed).append('\n');
        return s.toString();
    }

}
