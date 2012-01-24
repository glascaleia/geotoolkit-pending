/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotoolkit.index.tree;

import java.awt.geom.Rectangle2D;
import org.geotoolkit.util.ArgumentChecks;

/**Create a {@code CoupleNode2D}.
 *
 * @author Rémi Maréchal (Geomatys).
 */
public class CoupleNode2D implements Couple<Node2D>{

    private final Node2D node1;
    private final Node2D node2;

    /**Create a couple of two {@code Node2D}.
     * 
     * @param node1
     * @param node2 
     * @throws IllegalArgumentException if node1 or node2 are null.
     */
    public CoupleNode2D(final Node2D node1, final Node2D node2) {
        ArgumentChecks.ensureNonNull("create couplenode2D : node1", node1);
        ArgumentChecks.ensureNonNull("create couplenode2D : node2", node2);
        this.node1 = node1;
        this.node2 = node2;
    }

    /**
     * @return node1.
     */
    public Node2D getObject1() {
        return node1;
    }

    /**
     * @return node2.
     */
    public Node2D getObject2() {
        return node2;
    }

    /**
     * @return sum of two Node2D boundary.
     */
    public double getPerimeter() {
        final Rectangle2D rectO1 = getObject1().getBoundary().getBounds2D();
        final Rectangle2D rectO2 = getObject2().getBoundary().getBounds2D();
        return 2*(rectO1.getWidth() + rectO1.getHeight() + rectO2.getWidth() + rectO2.getHeight());
    }

    /**
     * @return true if the two Node2D intersect them else false.
     */
    public boolean intersect() {
        return getObject1().getBoundary().intersects(getObject2().getBoundary().getBounds2D());
    }

    /**
     * @return  Euclidean distance between two Node2D centroids.
     */
    public double getDistance() {
        return TreeUtils.getDistanceBetweenTwoBound2D(getObject1().getBoundary().getBounds2D(), getObject2().getBoundary().getBounds2D());
    }
    
    /**
     * @return  Overlaps area between two Node2D.
     */
    public double getOverlaps(){
        final Rectangle2D over = getObject1().getBoundary().getBounds2D().createIntersection(getObject2().getBoundary().getBounds2D());
        return over.getWidth()*over.getHeight();
    }
}
