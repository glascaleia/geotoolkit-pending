/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotoolkit.index.tree;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.geotoolkit.gui.swing.tree.Trees;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.NumberRange;
import org.geotoolkit.util.collection.NotifiedCheckedList;
import org.geotoolkit.util.converter.Classes;

/**Create a Node adapting with 2DEuclidean dimension datas.
 * 
 * @author Rémi Marechal (Geomatys)
 * @author Johann Sorel  (Geomatys)
 */
public class Node2D extends Node{

    protected Shape boundary;
    private Node2D parent;
    private final Tree tree;
    
    private final List<Node2D> children = new CrossList<Node2D>(Node2D.class) {

        @Override
        protected void notifyAdd(Node2D e, int i) {
            super.notifyAdd(e, i);
            clearBounds();
        }

        @Override
        protected void notifyAdd(Collection<? extends Node2D> clctn, NumberRange<Integer> nr) {
            super.notifyAdd(clctn, nr);
            clearBounds();
        }

        @Override
        protected void notifyRemove(Node2D e, int i) {
            super.notifyRemove(e, i);
            clearBounds();
        }

        @Override
        protected void notifyRemove(Collection<? extends Node2D> clctn, NumberRange<Integer> nr) {
            super.notifyRemove(clctn, nr);
            clearBounds();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            clearBounds();
        }
    };
    
    private final List<Shape> entries = new NotifiedCheckedList<Shape>(Shape.class) {
        @Override
        protected void notifyAdd(Shape e, int i) {
            clearBounds();
        }
        @Override
        protected void notifyAdd(Collection<? extends Shape> clctn, NumberRange<Integer> nr) {
            clearBounds();
        }
        @Override
        protected void notifyRemove(Shape e, int i) {
            clearBounds();
        }
        @Override
        protected void notifyRemove(Collection<? extends Shape> clctn, NumberRange<Integer> nr) {
            clearBounds();
        }
    };
    
    /**Create an empty {@code Node2D}.
     * 
     * @param tree 
     */
    public Node2D(final Tree tree) {
        this(tree, null, null, null);
    }
    
    /**Create {@code Node2D}.
     * 
     * @param tree pointer on {@code Tree}.
     * @param parent pointer on {@code Node2D} parent.
     * @param children subNode.
     * @param entries data(s) to add.
     * @throws IllegalArgumentException if tree pointer is null.
     */
    public Node2D(final Tree tree, final Node2D parent, final List<Node2D> children, final List<Shape> entries) {
        ArgumentChecks.ensureNonNull("tree", tree);
        this.tree = tree;
        this.parent = parent;
        if(children!=null){
            for(Node2D n2d : children){
                n2d.setParent(this);
            }
            this.children.addAll(children);
        }
        if(entries!=null)this.entries.addAll(entries);
    }
    
    /**
     * @return subNode.
     */
    public List<Node2D> getChildren() {
        return children;
    }

    /**A leaf is a {@code Node2D} at extremity of {@code Tree} which contains only entries.
     * 
     * @return true if it is  a leaf else false (branch).
     */
    public boolean isLeaf(){
        return getChildren().isEmpty();
    }
    
    /**
     * @return true if {@code Node2D} contains nothing else false.
     */
    public boolean isEmpty(){
        return (getChildren().isEmpty() && getEntries().isEmpty());
    }
    
    public boolean isFull(){
        return (getChildren().size()+getEntries().size())>=getTree().getMaxElements();
    }
    /**
     * @return entries.
     */
    public List<Shape> getEntries() {
        return entries;
    }

    /**
     * @return {@code Node2D} parent pointer.
     */
    public Node2D getParent() {
        return parent;
    }

    /**
     * @return {@code Tree} pointer.
     */
    public Tree getTree() {
        return tree;
    }
    
    /**
     * @return {@code Node2D} area.
     */
    public double getArea(){
        final Rectangle2D rect = getBoundary().getBounds2D();
        return rect.getWidth()*rect.getHeight();
    }
    
    /**Affect a new parent pointer.
     * 
     * @param parent 
     */
    public void setParent(final Node2D parent){
        this.parent = parent;
    }
    
    /**
     * Affect a {@code null} on boundary.
     */
    protected final void clearBounds() {
        boundary=null;
        fireCollectionEvent();
    }

    /**
     * Compute {@code Node2D} boundary. 
     */
    protected void calculateBounds(){
        for(Shape ent2D : getEntries()){
            addBound(ent2D);
        }
        for(Node2D n2D : getChildren()){
            addBound(n2D.getBoundary());
        }
    }

    /**Update boundary size from shape.
     * 
     * @param shape 
     */
    protected void addBound(final Shape shape){
        
        if(boundary==null){
            boundary = (Rectangle2D)shape.getBounds2D().clone();
        }else{
            final Rectangle2D bf = new Rectangle2D.Double();
            final Rectangle2D rect = shape.getBounds2D();
            final Rectangle2D rBound = boundary.getBounds2D();
            bf.setFrameFromDiagonal(Math.min(rect.getMinX(), rBound.getMinX()), 
                                    Math.min(rect.getMinY(), rBound.getMinY()), 
                                    Math.max(rect.getMaxX(), rBound.getMaxX()), 
                                    Math.max(rect.getMaxY(), rBound.getMaxY()));
            boundary = bf;
        }
    }
    
    /**
     * <blockquote><font size=-1>
     * <strong>NOTE: if boundary is null, method re-compute all subnode boundary.</strong> 
     * </font></blockquote>
     * @return boundary.
     */
    public Shape getBoundary() {
        if(boundary==null){
            calculateBounds();
        }
        return boundary;
    }
    
    /**
     * {@inheritDoc}. 
     */
    @Override
    public String toString() {
        final Collection col = new ArrayList(entries);
        col.addAll(children);
        String strparent =  (parent == null)?"null":String.valueOf(parent.hashCode()); 
        return Trees.toString(Classes.getShortClassName(this)+" : "+this.hashCode()+" parent : "+strparent, col);
    }
}
