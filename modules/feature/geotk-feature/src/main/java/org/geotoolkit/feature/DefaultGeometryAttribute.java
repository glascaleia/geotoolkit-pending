/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.feature;

import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.util.Utilities;

import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;


/**
 * TODO: rename to GeometricAttribute Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p>
 * <p>
 * </p>
 * <p>
 * Example Use:
 *
 * <pre><code>
 *         GeometryAttributeType x = new GeometryAttributeType( ... );
 *         TODO code example
 * </code></pre>
 *
 * </p>
 *
 * @author Leprosy
 * @since 0.3 TODO: test wkt geometry parse.
 */
public class DefaultGeometryAttribute extends DefaultAttribute implements GeometryAttribute {

    /**
     * bounds, derived
     */
    protected BoundingBox bounds;

    public DefaultGeometryAttribute(final Object content, final GeometryDescriptor descriptor,
            final Identifier id){
        super(content, descriptor, id);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public GeometryType getType() {
        return (GeometryType) super.getType();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public GeometryDescriptor getDescriptor() {
        return (GeometryDescriptor) super.getDescriptor();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Geometry getValue() {
        return (Geometry) super.getValue();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setValue(final Object newValue) throws IllegalArgumentException, IllegalStateException{
        setValue((Geometry) newValue);
    }

    public void setValue(final Geometry geometry) {
        super.setValue(geometry);
    }

    /**
     * Set the bounds for the contained geometry.
     */
    @Override
    public synchronized void setBounds(final BoundingBox bbox) {
        bounds = bbox;
    }

    /**
     * Returns the non null envelope of this attribute. If the attribute's
     * geometry is <code>null</code> the returned Envelope
     * <code>isNull()</code> is true.
     *
     * @return
     */
    @Override
    public synchronized BoundingBox getBounds() {
        if (bounds == null) {
            final JTSEnvelope2D bbox = new JTSEnvelope2D(getType().getCoordinateReferenceSystem());
            final Geometry geom = (Geometry) getValue();
            if (geom != null) {
                bbox.expandToInclude(geom.getEnvelopeInternal());
            } else {
                bbox.setToNull();
            }
            bounds = bbox;
        }
        return bounds;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DefaultGeometryAttribute)) {
            return false;
        }

        final DefaultGeometryAttribute att = (DefaultGeometryAttribute) o;

        //JD: since Geometry does not implement equals(Object) "properly",( ie
        // if you dont call equals(Geomtery) two geometries which are equal
        // will not be equal) we dont call super.equals()

        if (!Utilities.equals(descriptor, att.descriptor)) {
            return false;
        }

        if (!Utilities.equals(id, att.id)) {
            return false;
        }

        if (value != null && att.value != null) {
            //another lovley jts thing... comparing geometry collections that
            // arent multi point/line/poly throws an exception, so we nee dto
            // that comparison
            if (att.value instanceof GeometryCollection &&
                    !(att.value instanceof MultiPoint) &&
                    !(att.value instanceof MultiLineString) &&
                    !(att.value instanceof MultiPolygon)) {

                if (value instanceof GeometryCollection) {
                    //compare the two collections
                    final GeometryCollection c1 = (GeometryCollection) value;
                    final GeometryCollection c2 = (GeometryCollection) att.value;

                    if (c1.getNumGeometries() != c2.getNumGeometries()) {
                        return false;
                    }

                    for (int i = 0; i < c1.getNumGeometries(); i++) {
                        final Geometry g1 = c1.getGeometryN(i);
                        final Geometry g2 = c2.getGeometryN(i);

                        if (!g1.equals(g2)) {
                            return false;
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
            if (!((Geometry) value).equals((Geometry) att.value)) {
                return false;
            }
        } else {
            return Utilities.deepEquals(value, this.value);
        }

        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = descriptor.hashCode();

        if (id != null) {
            hash += 37 * id.hashCode();
        }

        return hash;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append(":");
        sb.append(getDescriptor().getName().getLocalPart());
        final CoordinateReferenceSystem crs = getDescriptor().getType().getCoordinateReferenceSystem();
        if (!getDescriptor().getName().getLocalPart().equals(getDescriptor().getType().getName().getLocalPart()) ||
                id != null || crs != null) {
            sb.append("<");
            sb.append(getDescriptor().getType().getName().getLocalPart());
            if (id != null) {
                sb.append(" id=");
                sb.append(id);
            }
            if (crs != null) {
                sb.append(" crs=");
                sb.append(crs);
            }
            if (id != null) {
                sb.append(" id=");
                sb.append(id);
            }
            sb.append(">");
        }
        sb.append("=");
        sb.append(value);
        return sb.toString();
    }
}
