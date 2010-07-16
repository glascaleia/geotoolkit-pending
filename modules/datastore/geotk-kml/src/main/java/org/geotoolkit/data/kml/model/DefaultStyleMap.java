/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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
package org.geotoolkit.data.kml.model;

import java.util.List;
import org.geotoolkit.data.kml.xsd.SimpleType;
import static java.util.Collections.*;

/**
 *
 * @author Samuel Andrés
 */
public class DefaultStyleMap extends DefaultAbstractStyleSelector implements StyleMap {

    private List<Pair> pairs;

    /**
     * 
     */
    public DefaultStyleMap() {
        this.pairs = EMPTY_LIST;
    }

    /**
     *
     * @param objectSimpleExtensions
     * @param idAttributes
     * @param abstractStyleSelectorSimpleExtensions
     * @param abstractStyleSelectorObjectExtensions
     * @param pairs
     * @param styleMapSimpleExtensions
     * @param styleMapObjectExtensions
     */
    public DefaultStyleMap(List<SimpleType> objectSimpleExtensions,
            IdAttributes idAttributes,
            List<SimpleType> abstractStyleSelectorSimpleExtensions,
            List<AbstractObject> abstractStyleSelectorObjectExtensions,
            List<Pair> pairs,
            List<SimpleType> styleMapSimpleExtensions,
            List<AbstractObject> styleMapObjectExtensions) {
        super(objectSimpleExtensions, idAttributes,
                abstractStyleSelectorSimpleExtensions,
                abstractStyleSelectorObjectExtensions);
        this.pairs = pairs;
        if (styleMapSimpleExtensions != null) {
            this.extensions().simples(Extensions.Names.STYLE_MAP).addAll(styleMapSimpleExtensions);
        }
        if (styleMapObjectExtensions != null) {
            this.extensions().complexes(Extensions.Names.STYLE_MAP).addAll(styleMapObjectExtensions);
        }
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public List<Pair> getPairs() {
        return this.pairs;
    }

    /**
     *
     * @{@inheritDoc }
     */
    @Override
    public void setPairs(List<Pair> pairs) {
        this.pairs = pairs;
    }
}
