/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotoolkit.index.tree;

import java.util.List;

/**Define a generic Tree.
 *
 * @author Rémi Maréchal       (Geomatys).
 * @author Yohann Sorel        (Geomatys).
 * @author Martin Desruisseaux (Geomatys).
 */
public interface Tree<B> {

    /**Find some {@code Entry} which intersect regionSearch parameter 
     * and add them into result {@code List} parameter.
     * 
     * <blockquote><font size=-1>
     * <strong>NOTE: if no result found, the list passed in parameter is unchanged.</strong> 
     * </font></blockquote>
     * 
     * @param regionSearch Define the region to find Shape within tree.
     * @param result List of Entr(y)(ies).
     */
    void search(B regionSearch, List<B> result);

    /**Insert a {@code Entry} into Rtree.
     * 
     * @param Entry to insert into tree.
     */
    void insert(B entry);

    /**Find a {@code Entry} into the tree and delete it.
     * 
     * @param Entry to delete.
     */
    void delete(B entry);

    /**
     * @return max number authorized by tree cells.
     */
    int getMaxElements();

    /**
     * @return tree trunk.
     */
    Node2D getRoot();
    
    /**
     * Affect a new root {@Node}.
     * @param root new root.
     */
    void setRoot(Node2D root);
    
    /**
     * Create a node in accordance with this RTree properties.
     * 
     * @param tree pointer on Tree.
     * @param parent pointer on parent {@code Node2D}.
     * @param children sub {@code Node2D}.
     * @param entries {@code List<Shape>} to add in this node. 
     * @return 
     */
    Node2D createNode(Tree tree, Node2D parent, List<Node2D> listChildren, List<B> listEntries);
}
