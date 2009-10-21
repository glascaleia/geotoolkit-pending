/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.filter.text.ecql;

import org.geotoolkit.filter.text.commons.Language;
import org.geotoolkit.filter.text.cql2.CQLExistenceTest;


/**
 * Test for ECQL Existence Predicate
 *
 * <p>
 * Same to CQL
 * </p>
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @module pending
 * @since 2.6
 */
public class ECQLExistenceTest extends CQLExistenceTest {

    public ECQLExistenceTest(){
        super();
        language = Language.ECQL;
    }
}
