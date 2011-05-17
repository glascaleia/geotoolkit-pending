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

//jaxB import
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

// Constellation dependencies
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.gml.xml.v311.DefinitionType;
import org.geotoolkit.util.ComparisonMode;
import org.opengis.observation.Phenomenon;


/**
 * Implementation of an entry representing a {@linkplain Phenomenon phenomenon}.
 * 
 * @version $Id: PhenomenonType.java 1286 2009-01-22 15:28:09Z glegal $
 * @author Antoine Hnawia
 * @author Guilhem Legal
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Phenomenon")
@XmlRootElement(name = "phenomenon")
@XmlSeeAlso({ CompoundPhenomenonType.class })
public class PhenomenonType extends DefinitionType implements Phenomenon {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5140595674231914861L;

    
    /**
     * Empty constructor used by JAXB.
     */
    protected PhenomenonType(){}
    
    /**
     * Construit un nouveau phénomène du nom spécifié.
     *
     * @param id L'identifiant de ce phenomene.
     * @param name Le nom du phénomène.
     */
    public PhenomenonType(final String id, final String name) {
        super(id, name, null);
        
    }

    /**
     * 
     * Construit un nouveau phénomène du nom spécifié.
     * 
     * 
     * @param id L'identifiant de ce phenomene.
     * @param name Le nom du phénomène.
     * @param description La description de ce phénomène, ou {@code null}.
     */
    public PhenomenonType(final String id, final String name, final String description ) {
        super(id, name, description);
    }

    /**
     * Retourne l'identifiant du phénomène.
    
    public String getId() {
        return id;
    }

    /**
     * Retoune la description du phénomène.
     
    public String getDescription() {
        return description;
    }
    
    /**
     * Retourne le nom du phenomene (une URN le plus souvent).
     
    public String getPhenomenonName(){
        return name;
    } 
    
    /**
     * Retourne un code représentant ce phenomene.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (object == this) {
            return true;
        } else return (object instanceof PhenomenonType && super.equals(object, mode));
    }
    
    /**
     * Retourne une chaine de charactere representant le phenomene.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
