/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008-2012, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2012, Geomatys
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
package org.geotoolkit.index.tree;

import java.util.List;
import org.opengis.geometry.Envelope;

/**Create a {@code Node}.
 *
 * <B> : Entries type stocked in {@code Node}.
 * 
 * @author Rémi Marechal (Geomatys).
 */
public interface Node<N extends Node<N>> {
    
    List<N> getChildren();
    
    /**A leaf is a {@code Node} at extremity of {@code Tree} which contains only entries.
     * 
     * @return true if it is  a leaf else false (branch).
     */
    boolean isLeaf();
    
    /**
     * @return true if {@code Node} contains nothing else false.
     */
    boolean isEmpty();
    
    /**
     * @return true if node elements number equals or overflow max elements
     *         number autorized by {@code Tree} else false. 
     */
    boolean isFull();
    
    /**
     * <blockquote><font size=-1>
     * <strong>NOTE: if boundary is null, method re-compute all subnode boundary.</strong> 
     * </font></blockquote>
     * @return boundary.
     */
    Envelope getBoundary();
    
    /**
     * @return entries.
     */
    List<Envelope> getEntries();

    /**
     * @return {@code AbstractNode} parent pointer.
     */
    Node getParent();

    /**
     * @return {@code Tree} pointer.
     */
    Tree getTree();
}
