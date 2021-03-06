/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.renderer.style;

import java.awt.Graphics2D;
import java.awt.Shape;

import org.opengis.filter.expression.Expression;

/**
 * Symbol handler for a Mark.
 * @module pending
 */
public interface MarkFactory {
    /**
     * Turns the specified URL into an Shape, eventually using the Feature
     * attributes to evaluate the expression, or returns <code>null</code> if
     * the factory cannot evaluate this symbolUrl.
     * 
     * @param symbolUrl
     *            the expression that will return the symbol name. Once
     *            evaluated the expression should return something like
     *            <code>plainName</code> or like <code>protocol://path</code>.
     *            See the actual implementations for details on the kind of
     *            supported name.
     * @param candidate
     *            The feature that will be used to evaluate the symbolURL
     *            expression (or to extract data from it, think complex attributes, in that
     *            case a visit to the expression and some direct attribute value extraction 
     *            might be needed instead)
     * 
     */
    public Shape getShape(Graphics2D graphics, Expression symbolUrl, Object candidate) throws Exception;
}
