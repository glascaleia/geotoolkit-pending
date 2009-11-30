/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source: /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/primitive/CurveImpl.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotoolkit.geometry.isoonjts.spatialschema.geometry.primitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.JTSGeometry;
import org.geotoolkit.geometry.isoonjts.JTSUtils;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.AbstractJTSGeometry;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.NotifyingArrayList;
import org.geotoolkit.geometry.isoonjts.spatialschema.geometry.geometry.JTSLineString;

import org.geotoolkit.internal.jaxb.CurveArrayType;
import org.geotoolkit.util.Utilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.complex.CompositeCurve;
import org.opengis.geometry.coordinate.LineString;
import org.opengis.geometry.coordinate.ParamForPoint;
import org.opengis.geometry.coordinate.Position;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.CurveSegment;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.geometry.primitive.CurveBoundary;

/**
 * Simple implementation of the Curve interface that does not implement any
 * of the (hard) math functions like intersection, etc.  A curve consists of
 * any number of CurveSegment objects (such as LineStrings) that must be
 * connected end-to-end.
 * @module pending
 */
public class JTSCurve extends AbstractJTSGeometry implements Curve {
    /**
     * Component parts of the Curve.  Each element must implement CurveSegment.
     */
    private List<CurveSegment> curveSegments;

    /**
     * Creates a new {@code JTSCurve}.
     */
    public JTSCurve() {
        this(null);
    }

    /**
     * Creates a new {@code JTSCurve}.
     * @param crs
     */
    public JTSCurve(final CoordinateReferenceSystem crs) {
        super(crs);
        curveSegments = new NotifyingArrayList<CurveSegment>(this);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public CurveBoundary getBoundary() {
        return (CurveBoundary) super.getBoundary();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final List<CurveSegment> getSegments() {
        return curveSegments;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final DirectPosition getStartPoint() {
        return (curveSegments.get(0)).getStartPoint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final DirectPosition getEndPoint() {
        return (curveSegments.get(curveSegments.size() - 1)).getEndPoint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public double [] getTangent(double s) {
        // PENDING(CSD): Implement me!
        return new double[0];
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final double getStartParam() {
        return 0.0;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final double getEndParam() {
        return this.length(0, getEndConstructiveParam());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final double getStartConstructiveParam() {
        return 0.0;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final double getEndConstructiveParam() {
        return curveSegments.size();
    }

    // We choose to parameterize the curve as a function from the interval
    // [0, n] to R_n, where interval [i, i+1] corresponds to curve i, and
    // the in between points are mapped linearly between the constructive params
    // of curve i, i.e. C(i+d) = C_i((1-d)*scp + d*ecp), 0 <= d < 1, scp is the
    // start constructive param of curve segment i, ecp is the end constructive
    // param of curve segment i.
    @Override
    public final DirectPosition forConstructiveParam(double cp) {
        int n = curveSegments.size();
        int i = (int) cp;
        if (i < 0) {
            i = 0;
        }
        else if (i > n) {
            i = n;
        }
        if (i == n) {
            return (curveSegments.get(n - 1)).getEndPoint();
        }
        else {
            CurveSegment cs = curveSegments.get(i);
            double d = cp - i; // 0 <= d < 1
            return cs.forConstructiveParam(
                (1-d) * cs.getStartConstructiveParam() +
                  d   * cs.getEndConstructiveParam());
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final DirectPosition forParam(double s) {
        return null;
    }

    /**
     * Not implemented.  Always just returns null.
     */
    @Override
    public ParamForPoint getParamForPoint(DirectPosition p) {
        return null;
    }

    /**
     * Not implemented.  Always returns zero.
     */
    @Override
    public double length(Position point1, Position point2) {
        return 0.0;
    }

    /**
     * Not implemented.  Always returns 0.
     * This wouldn't be hard to implement, though.  You'd just sum over the
     * CurveSegments that comprise this object.
     */
    @Override
    public double length(double cparam1, double cparam2) {
        return 0.0;
    }

    /**
     * Not fully implemented.  Returns null, unless all CurveSegments are LineStrings,
     * in which case it returns a concatenation of all the LineStrings.
     * In future versions this could be implemented by delegating to the comprising segments.
     */
    @Override
    public LineString asLineString(double maxSpacing, double maxOffset) {
    	int count = curveSegments.size();
    	if (count == 1) {
    		Object segment1 = curveSegments.get(0);
    		if (segment1 instanceof LineString) {
    			return (LineString) segment1;
    		}
    	} else if (count > 0) {
			boolean allLineString = true;
			JTSLineString lsi = new JTSLineString();
			LineString ls = null;
			List retList = lsi.getControlPoints().positions();
			Object lastPoint = null;
			List segList = null;
			for (int i = 0; i < count && allLineString; i++) {
	    		Object segment = curveSegments.get(0);
	    		if (segment instanceof LineString) {
	    			segList = ((LineString) segment).getControlPoints().positions();
	    			if (segList.get(0).equals(lastPoint)) {
	    				retList.remove(retList.size() - 1);
	    			}
	    			retList.addAll(segList);
	    			lastPoint = retList.get(retList.size() - 1);
	    		} else {
	    			allLineString = false;
	    		}
			}
			if (allLineString) {
				return lsi;
			}
    	}
        return null;
    }

    /**
     * Returns null.
     */
    @Override
    public CompositeCurve getComposite() {
        return null;
    }

    @Override
    public int getOrientation() {
        return +1;
    }

    /**
     * Returns "this".  Should return the containing primitive, if any.
     */
    @Override
    public Curve getPrimitive() {
        return this;
    }

    /**
     * Not implemented.  Always returns an empty set.
     */
    @Override
    public Set getContainedPrimitives() {
        return Collections.EMPTY_SET;
    }

    /**
     * Not implemented (and probably never will be since traversal of this
     * association would require a lot of extra work).  Always returns an
     * empty set.
     */
    @Override
    public Set getContainingPrimitives() {
        return Collections.EMPTY_SET;
    }

    /**
     * Not implemented (and probably never will be since traversal of this
     * association would require a lot of extra work).  Always returns an
     * empty set.
     */
    @Override
    public Set getComplexes() {
        return Collections.EMPTY_SET;
    }

    /**
     * Not implemented.  Returns null.
     */
    @Override
    public OrientableCurve[] getProxy() {
        return null;
    }

    @XmlElement(name="segments", namespace="http://www.opengis.net/gml")
    public CurveArrayType getCurveArray() {
        return new CurveArrayType(curveSegments);
    }

    public void setCurveArray(CurveArrayType array) {
        curveSegments = new NotifyingArrayList<CurveSegment>(this);
        for (CurveSegment c : array.getCurveSegments())  {
            JTSLineString line = (JTSLineString) c;
            JTSLineString newLine = new JTSLineString(getCoordinateReferenceSystem());
            for (Position p : line.getPositions()) {
                if (p instanceof GeneralDirectPosition) {
                    ((GeneralDirectPosition) p).setCoordinateReferenceSystem(getCoordinateReferenceSystem());
                    newLine.getPositions().add(p);
                }
            }
            curveSegments.add(newLine);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected com.vividsolutions.jts.geom.Geometry computeJTSPeer() {
        // For each segment that comprises us, get the JTS peer.
        int n = curveSegments.size();
        ArrayList allCoords = new ArrayList();
        for (int i=0; i<n; i++) {
            JTSGeometry g = (JTSGeometry) curveSegments.get(i);
            com.vividsolutions.jts.geom.LineString jts =
                (com.vividsolutions.jts.geom.LineString) g.getJTSGeometry();
            int m = jts.getNumPoints();
            for (int j=0; j<m; j++) {
                allCoords.add(jts.getCoordinateN(j));
            }
            if (i != (n-1))
                allCoords.remove(allCoords.size()-1);
        }
        com.vividsolutions.jts.geom.Coordinate [] coords =
            new com.vividsolutions.jts.geom.Coordinate[allCoords.size()];
        allCoords.toArray(coords);
        return JTSUtils.GEOMETRY_FACTORY.createLineString(coords);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("segments:");
        if(!curveSegments.isEmpty()){
            sb.append("\n");
            for(CurveSegment seg : curveSegments){
                sb.append(seg.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object instanceof JTSCurve && super.equals(object)) {
            JTSCurve that = (JTSCurve) object;
            return Utilities.equals(this.curveSegments, that.curveSegments);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 89 * hash + (this.curveSegments != null ? this.curveSegments.hashCode() : 0);
        return hash;
    }


}
