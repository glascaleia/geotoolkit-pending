/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2010, Geomatys
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
package org.geotoolkit.index.quadtree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * @author Tommaso Nolli
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class Node {

    private Envelope bounds;
    protected int numShapesId;
    protected int[] shapesId;
    protected final List<Node> subNodes;
    protected Node parent;
    private boolean visited = false;
    private boolean childrenVisited = false;
    protected int id;

    public Node(Envelope bounds, int id, Node parent) {
        this.parent = parent;
        this.id = id;
        this.bounds = new Envelope(bounds);
        this.subNodes = new ArrayList<Node>(4);
        this.shapesId = new int[4];
        Arrays.fill(this.shapesId, -1);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return Returns the bounds.
     */
    public Envelope getBounds() {
        return this.bounds;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param bounds The bounds to set.
     */
    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return Returns the numSubNodes.
     */
    public int getNumSubNodes() {
        return this.subNodes.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the number of records stored.
     */
    public int getNumShapeIds() {
        return this.numShapesId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param node
     * @throws NullPointerException DOCUMENT ME!
     */
    public void addSubNode(Node node) {
        if (node == null) {
            throw new NullPointerException("Cannot add null to subnodes");
        }

        this.subNodes.add(node);
    }

    /**
     * Removes a subnode
     * 
     * @param node The subnode to remove
     * @return true if the subnode has been removed
     */
    public boolean removeSubNode(Node node) {
        return this.subNodes.remove(node);
    }

    public void clearSubNodes() {
        this.subNodes.clear();
    }

    /**
     * Gets the Node at the requested position
     * 
     * @param pos The position
     * @return A Node
     * @throws StoreException DOCUMENT ME!
     */
    public Node getSubNode(int pos) throws StoreException {
        return this.subNodes.get(pos);
    }

    /**
     * Add a shape id
     * 
     * @param id
     */
    public void addShapeId(int id) {
        if (this.shapesId.length == this.numShapesId) {
            // Increase the array
            final int[] newIds = new int[this.numShapesId * 2];
            Arrays.fill(newIds, -1);
            System.arraycopy(this.shapesId, 0, newIds, 0, this.numShapesId);
            this.shapesId = newIds;
        }

        this.shapesId[this.numShapesId] = id;
        this.numShapesId++;
    }

    /**
     * Gets a shape id
     * 
     * @param pos The position
     * @return The shape id (or recno) at the requested position
     * @throws ArrayIndexOutOfBoundsException DOCUMENT ME!
     */
    public int getShapeId(int pos) {
        if (pos >= this.numShapesId) {
            throw new ArrayIndexOutOfBoundsException("Requsted " + pos
                    + " but size = " + this.numShapesId);
        }

        return this.shapesId[pos];
    }

    /**
     * Sets the shape ids
     * @param ids
     */
    public void setShapesId(int[] ids) {
        if (ids == null) {
            this.numShapesId = 0;
        } else {
            this.shapesId = ids;
            this.numShapesId = 0;

            for(int i : ids) {
                if (i == -1) {
                    break;
                }
                this.numShapesId++;
            }
        }
    }

    /**
     * DOCUMENT ME!
     * @return Returns the shapesId.
     */
    public int[] getShapesId() {
        return this.shapesId;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Node getSibling() throws StoreException {
        if (parent == null || id == parent.getNumSubNodes() - 1)
            return null;
        return parent.getSubNode(id + 1);
    }

    public boolean isChildrenVisited() {
        return childrenVisited;
    }

    public void setChildrenVisited(boolean childrenVisited) {
        this.childrenVisited = childrenVisited;
    }

    public Node copy() throws IOException {
        return new Node(bounds, id, parent);
    }
}
