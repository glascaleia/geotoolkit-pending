/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.vividsolutions.jts.triangulate.quadedge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.algorithm.NotRepresentableException;

/**
 * Models a site (node) in a {@link QuadEdgeSubdivision}. 
 * The sites can be points on a lineString representing a
 * linear site. 
 * The vertex can be considered as a vector with a norm, length, inner product, cross
 * product, etc. Additionally, point relations (e.g., is a point to the left of a line, the circle
 * defined by this point and two others, etc.) are also defined in this class.
 * 
 * @author David Skea
 * @author Martin Davis
 * @module pending
 */
public class Vertex 
{
    public static final int LEFT        = 0;
    public static final int RIGHT       = 1;
    public static final int BEYOND      = 2;
    public static final int BEHIND      = 3;
    public static final int BETWEEN     = 4;
    public static final int ORIGIN      = 5;
    public static final int DESTINATION = 6;

    private Coordinate      p;
    // private int edgeNumber = -1;

    public Vertex(final double _x, final double _y) {
        p = new Coordinate(_x, _y);
    }

    public Vertex(final double _x, final double _y, final double _z) {
        p = new Coordinate(_x, _y, _z);
    }

    public Vertex(final Coordinate _p) {
        p = new Coordinate(_p);
    }

    public double getX() {
        return p.x;
    }

    public double getY() {
        return p.y;
    }

    public double getZ() {
        return p.z;
    }

    public void setZ(final double _z) {
        p.z = _z;
    }

    public Coordinate getCoordinate() {
        return p;
    }

    public String toString() {
        return "POINT (" + p.x + " " + p.y + ")";
    }

    public boolean equals(final Vertex _x) {
        if (p.x == _x.getX() && p.y == _x.getY()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean equals(final Vertex _x, final double tolerance) {
        if (p.distance(_x.getCoordinate()) < tolerance) {
            return true;
        } else {
            return false;
        }
    }

    public int classify(final Vertex p0, final Vertex p1) {
        Vertex p2 = this;
        Vertex a = p1.sub(p0);
        Vertex b = p2.sub(p0);
        double sa = a.crossProduct(b);
        if (sa > 0.0)
            return LEFT;
        if (sa < 0.0)
            return RIGHT;
        if ((a.getX() * b.getX() < 0.0) || (a.getY() * b.getY() < 0.0))
            return BEHIND;
        if (a.magn() < b.magn())
            return BEYOND;
        if (p0.equals(p2))
            return ORIGIN;
        if (p1.equals(p2))
            return DESTINATION;
        return BETWEEN;
    }

    /**
     * Computes the cross product k = u X v.
     * 
     * @param v a vertex
     * @return returns the magnitude of u X v
     */
    double crossProduct(final Vertex v) {
        return (p.x * v.getY() - p.y * v.getX());
    }

    /**
     * Computes the inner or dot product
     * 
     * @param v, a vertex
     * @return returns the dot product u.v
     */
    double dot(final Vertex v) {
        return (p.x * v.getX() + p.y * v.getY());
    }

    /**
     * Computes the scalar product c(v)
     * 
     * @param v, a vertex
     * @return returns the scaled vector
     */
    Vertex times(final double c) {
        return (new Vertex(c * p.x, c * p.y));
    }

    /* Vector addition */
    Vertex sum(final Vertex v) {
        return (new Vertex(p.x + v.getX(), p.y + v.getY()));
    }

    /* and subtraction */
    Vertex sub(final Vertex v) {
        return (new Vertex(p.x - v.getX(), p.y - v.getY()));
    }

    /* magnitude of vector */
    double magn() {
        return (Math.sqrt(p.x * p.x + p.y * p.y));
    }

    /* returns k X v (cross product). this is a vector perpendicular to v */
    Vertex cross() {
        return (new Vertex(p.y, -p.x));
    }

    /** ************************************************************* */
    /***********************************************************************************************
     * Geometric primitives /
     **********************************************************************************************/

    /**
     * Computes twice the area of the oriented triangle (a, b, c), i.e., the area is positive if the
     * triangle is oriented counterclockwise.
     */
    private final double triArea(final Vertex a, final Vertex b, final Vertex c) {
        return (b.p.x - a.p.x) * (c.p.y - a.p.y) 
             - (b.p.y - a.p.y) * (c.p.x - a.p.x);
    }

    /**
     * Tests if this is inside the circle defined by the points a, b, c. This test uses simple
     * double-precision arithmetic, and thus may not be robust.
     * 
     * @param a
     * @param b
     * @param c
     * @return true if this point is inside the circle defined by the points a, b, c
     */
    public final boolean inCircle(final Vertex a, final Vertex b, final Vertex c) {
      Vertex d = this;
      boolean isInCircle = 
      	        (a.p.x * a.p.x + a.p.y * a.p.y) * triArea(b, c, d)
              - (b.p.x * b.p.x + b.p.y * b.p.y) * triArea(a, c, d)
              + (c.p.x * c.p.x + c.p.y * c.p.y) * triArea(a, b, d)
              - (d.p.x * d.p.x + d.p.y * d.p.y) * triArea(a, b, c) 
              > 0;
      return isInCircle;
    }
    
/*
  public boolean OLDinCircle(Vertex a, Vertex b, Vertex c) {
      Vertex d = this;
      boolean isInCircle = (a.getX() * a.getX() + a.getY() * a.getY()) * triArea(b, c, d)
              - (b.getX() * b.getX() + b.getY() * b.getY()) * triArea(a, c, d)
              + (c.getX() * c.getX() + c.getY() * c.getY()) * triArea(a, b, d)
              - (d.getX() * d.getX() + d.getY() * d.getY()) * triArea(a, b, c) 
              > 0;

      // boolean isInCircleRobust = checkRobustInCircle(a.p, b.p, c.p, p, isInCircle);

      // if (! isInCircle)
      // System.out.println(WKTWriter.toLineString(new CoordinateArraySequence(new Coordinate[] {
      // a.p, b.p, c.p, p })));

      return isInCircle;
  }
*/

    /**
     * Tests whether the triangle formed by this vertex and two
     * other vertices is in CCW orientation.
     * 
     * @param b a vertex
     * @param c a vertex
     * @returns true if the triangle is oriented CCW
     */
    public final boolean isCCW(final Vertex b, final Vertex c) 
    {
    	// is equal to the signed area of the triangle
    	
      return (b.p.x - p.x) * (c.p.y - p.y) 
      - (b.p.y - p.y) * (c.p.x - p.x) > 0;
      
      // original rolled code
      //boolean isCCW = triArea(this, b, c) > 0;
      //return isCCW;
      
        /*
         // MD - used to check for robustness of triArea 
        boolean isCCW = triArea(this, b, c) > 0;
        boolean isCCWRobust = CGAlgorithms.orientationIndex(p, b.p, c.p) == CGAlgorithms.COUNTERCLOCKWISE; 
        if (isCCWRobust != isCCW)
        	System.out.println("CCW failure");
        return isCCW;
        //*/
    }

    public final boolean rightOf(final QuadEdge e) {
        return isCCW(e.dest(), e.orig());
    }

    public final boolean leftOf(final QuadEdge e) {
        return isCCW(e.orig(), e.dest());
    }

    private HCoordinate bisector(final Vertex a, final Vertex b) {
        // returns the perpendicular bisector of the line segment ab
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        HCoordinate l1 = new HCoordinate(a.getX() + dx / 2.0, a.getY() + dy / 2.0, 1.0);
        HCoordinate l2 = new HCoordinate(a.getX() - dy + dx / 2.0, a.getY() + dx + dy / 2.0, 1.0);
        return new HCoordinate(l1, l2);
    }

    private double distance(final Vertex v1, final Vertex v2) {
        return Math.sqrt(Math.pow(v2.getX() - v1.getX(), 2.0)
                + Math.pow(v2.getY() - v1.getY(), 2.0));
    }

    /**
     * Computes the value of the ratio of the circumradius to shortest edge. If smaller than some
     * given tolerance B, the associated triangle is considered skinny. For an equal lateral
     * triangle this value is 0.57735. The ratio is related to the minimum triangle angle theta by:
     * circumRadius/shortestEdge = 1/(2sin(theta)).
     * 
     * @param b second vertex of the triangle
     * @param c third vertex of the triangle
     * @return ratio of circumradius to shortest edge.
     */
    public double circumRadiusRatio(final Vertex b, final Vertex c) {
        Vertex x = this.circleCenter(b, c);
        double radius = distance(x, b);
        double edgeLength = distance(this, b);
        double el = distance(b, c);
        if (el < edgeLength) {
            edgeLength = el;
        }
        el = distance(c, this);
        if (el < edgeLength) {
            edgeLength = el;
        }
        return radius / edgeLength;
    }

    /**
     * returns a new vertex that is mid-way between this vertex and another end point.
     * 
     * @param a the other end point.
     * @return the point mid-way between this and that.
     */
    public Vertex midPoint(final Vertex a) {
        double xm = (p.x + a.getX()) / 2.0;
        double ym = (p.y + a.getY()) / 2.0;
        double zm = (p.z + a.getZ()) / 2.0;
        return new Vertex(xm, ym, zm);
    }

    /**
     * Computes the centre of the circumcircle of this vertex and two others.
     * 
     * @param b
     * @param c
     * @return the Coordinate which is the circumcircle of the 3 points.
     */
    public Vertex circleCenter(final Vertex b, final Vertex c) {
        Vertex a = new Vertex(this.getX(), this.getY());
        // compute the perpendicular bisector of cord ab
        HCoordinate cab = bisector(a, b);
        // compute the perpendicular bisector of cord bc
        HCoordinate cbc = bisector(b, c);
        // compute the intersection of the bisectors (circle radii)
        HCoordinate hcc = new HCoordinate(cab, cbc);
        Vertex cc = null;
        try {
            cc = new Vertex(hcc.getX(), hcc.getY());
        } catch (NotRepresentableException nre) {
            System.err.println("a: " + a + "  b: " + b + "  c: " + c);
            System.err.println(nre);
        }
        return cc;
    }

    /**
     * For this vertex enclosed in a triangle defined by three verticies v0, v1 and v2, interpolate
     * a z value from the surrounding vertices.
     */
    public double interpolateZValue(final Vertex v0, final Vertex v1, final Vertex v2) {
        double x0 = v0.getX();
        double y0 = v0.getY();
        double a = v1.getX() - x0;
        double b = v2.getX() - x0;
        double c = v1.getY() - y0;
        double d = v2.getY() - y0;
        double det = a * d - b * c;
        double dx = this.getX() - x0;
        double dy = this.getY() - y0;
        double t = (d * dx - b * dy) / det;
        double u = (-c * dx + a * dy) / det;
        double z = v0.getZ() + t * (v1.getZ() - v0.getZ()) + u * (v2.getZ() - v0.getZ());
        return z;
    }

    /**
     * Interpolates the Z value of a point enclosed in a 3D triangle.
     */
    public static double interpolateZ(final Coordinate p, final Coordinate v0, final Coordinate v1, final Coordinate v2) {
        double x0 = v0.x;
        double y0 = v0.y;
        double a = v1.x - x0;
        double b = v2.x - x0;
        double c = v1.y - y0;
        double d = v2.y - y0;
        double det = a * d - b * c;
        double dx = p.x - x0;
        double dy = p.y - y0;
        double t = (d * dx - b * dy) / det;
        double u = (-c * dx + a * dy) / det;
        double z = v0.z + t * (v1.z - v0.z) + u * (v2.z - v0.z);
        return z;
    }

    /**
     * Computes the interpolated Z-value for a point p lying on the segment p0-p1
     * 
     * @param p
     * @param p0
     * @param p1
     * @return
     */
    public static double interpolateZ(final Coordinate p, final Coordinate p0, final Coordinate p1) {
        double segLen = p0.distance(p1);
        double ptLen = p.distance(p0);
        double dz = p1.z - p0.z;
        double pz = p0.z + dz * (ptLen / segLen);
        return pz;
    }

    // /**
    // * Checks if the computed value for isInCircle is correct, using double-double precision
    // * arithmetic.
    // *
    // * @param a
    // * @param b
    // * @param c
    // * @param p
    // * @param nonRobustInCircle
    // * @return the robust value
    // */
    // private boolean checkRobustInCircle(Coordinate a, Coordinate b, Coordinate c, Coordinate p,
    // boolean nonRobustInCircle) {
    // // *
    // boolean isInCircleDD = inCircleDD(a, b, c, p);
    // boolean isInCircleCC = inCircleCC(a, b, c, p);
    //
    // Coordinate circumCentre = Triangle.circumcentre(a, b, c);
    // System.out.println("p radius diff a = "
    // + (p.distance(circumCentre) - a.distance(circumCentre)) / a.distance(circumCentre));
    //
    // if (nonRobustInCircle != isInCircleDD || nonRobustInCircle != isInCircleCC) {
    // System.out.println("inCircle robustness failure (double result = " + nonRobustInCircle
    // + ", DD result = " + isInCircleDD + ", CC result = " + isInCircleCC + ")");
    // System.out.println(WKTWriter.toLineString(new CoordinateArraySequence(new Coordinate[]{
    // a,
    // b,
    // c,
    // p})));
    // System.out.println("Circumcentre = " + WKTWriter.toPoint(circumCentre)
    // + " radius = " + a.distance(circumCentre));
    // System.out.println("p radius diff a = "
    // + (p.distance(circumCentre) - a.distance(circumCentre)));
    // System.out.println("p radius diff b = "
    // + (p.distance(circumCentre) - b.distance(circumCentre)));
    // System.out.println("p radius diff c = "
    // + (p.distance(circumCentre) - c.distance(circumCentre)));
    // }
    // return isInCircleDD;
    // }

    // /**
    // * Computes the inCircle test using the circumcentre. In general this doesn't appear to be any
    // * more robust than the standard calculation. However, there is at least one case where the
    // test
    // * point is far enough from the circumcircle that this test gives the correct answer.
    // LINESTRING
    // * (1507029.9878 518325.7547, 1507022.1120341457 518332.8225183258, 1507029.9833 518325.7458,
    // * 1507029.9896965567 518325.744909031)
    // *
    // * @param a
    // * @param b
    // * @param c
    // * @param p
    // * @return
    // */
    // private static boolean inCircleCC(Coordinate a, Coordinate b, Coordinate c, Coordinate p) {
    // Coordinate cc = Triangle.circumcentre(a, b, c);
    // double ccRadius = a.distance(cc);
    // double pRadiusDiff = p.distance(cc) - ccRadius;
    // return pRadiusDiff <= 0;
    // }
    //
    // private static boolean inCircleDD(Coordinate a, Coordinate b, Coordinate c, Coordinate p) {
    // DoubleDouble px = new DoubleDouble(p.x);
    // DoubleDouble py = new DoubleDouble(p.y);
    // DoubleDouble ax = new DoubleDouble(a.x);
    // DoubleDouble ay = new DoubleDouble(a.y);
    // DoubleDouble bx = new DoubleDouble(b.x);
    // DoubleDouble by = new DoubleDouble(b.y);
    // DoubleDouble cx = new DoubleDouble(c.x);
    // DoubleDouble cy = new DoubleDouble(c.y);
    //
    // DoubleDouble aTerm = (ax.multiply(ax).add(ay.multiply(ay))).multiply(triAreaDD(
    // bx,
    // by,
    // cx,
    // cy,
    // px,
    // py));
    // DoubleDouble bTerm = (bx.multiply(bx).add(by.multiply(by))).multiply(triAreaDD(
    // ax,
    // ay,
    // cx,
    // cy,
    // px,
    // py));
    // DoubleDouble cTerm = (cx.multiply(cx).add(cy.multiply(cy))).multiply(triAreaDD(
    // ax,
    // ay,
    // bx,
    // by,
    // px,
    // py));
    // DoubleDouble pTerm = (px.multiply(px).add(py.multiply(py))).multiply(triAreaDD(
    // ax,
    // ay,
    // bx,
    // by,
    // cx,
    // cy));
    //
    // DoubleDouble sum = aTerm.subtract(bTerm).add(cTerm).subtract(pTerm);
    // boolean isInCircle = sum.doubleValue() > 0;
    //
    // return isInCircle;
    // }

    // /**
    // * Computes twice the area of the oriented triangle (a, b, c), i.e., the area is positive if
    // the
    // * triangle is oriented counterclockwise.
    // */
    // private static DoubleDouble triAreaDD(DoubleDouble ax, DoubleDouble ay, DoubleDouble bx,
    // DoubleDouble by, DoubleDouble cx, DoubleDouble cy) {
    // return (bx.subtract(ax).multiply(cy.subtract(ay)).subtract(by.subtract(ay).multiply(
    // cx.subtract(ax))));
    // }

}
