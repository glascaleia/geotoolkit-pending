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
package org.geotoolkit.data.atom;

import java.util.List;
import org.geotoolkit.data.atom.model.AtomEmail;
import org.geotoolkit.data.atom.model.AtomLink;
import org.geotoolkit.data.atom.model.AtomPersonConstruct;

/**
 *
 * @author Samuel Andrés
 */
public interface AtomFactory {

    /**
     *
     * @param href
     * @param rel
     * @param type
     * @param hreflang
     * @param title
     * @param length
     * @return
     */
    public AtomLink createAtomLink(String href, String rel, String type,
            String hreflang, String title, String length);

    /**
     *
     * @return
     */
    public AtomLink createAtomLink();

    /**
     *
     * @param params
     * @return
     */
    public AtomPersonConstruct createAtomPersonConstruct(List<Object> params);

    /**
     *
     * @return
     */
    public AtomPersonConstruct createAtomPersonConstruct();

    /**
     *
     * @param address
     * @return
     */
    public AtomEmail createAtomEmail(String address);

    /**
     *
     * @return
     */
    public AtomEmail createAtomEmail();
}
