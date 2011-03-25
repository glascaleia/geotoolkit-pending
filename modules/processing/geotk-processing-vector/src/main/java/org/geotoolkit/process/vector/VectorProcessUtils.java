/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.process.vector;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.ListIterator;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.geotoolkit.factory.AuthorityFactoryFinder;
import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.AttributeTypeBuilder;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.lang.Static;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;

/**
 * Set of function and methods used by vector process
 * @author Quentin Boileau
 * @module pending
 */
@Static
public final class VectorProcessUtils {

    private VectorProcessUtils() {
    }

    /**
     * Change the geometry descriptor to Geometry.
     * @param oldFeatureType FeatureType
     * @param class the new type of geometry
     * @return newFeatureType FeatureType
     */
    public static FeatureType changeFeatureType(final FeatureType oldFeatureType, final Class clazz) {

        AttributeDescriptorBuilder descBuilder;
        AttributeTypeBuilder typeBuilder;

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();

        ftb.copy(oldFeatureType);

        final ListIterator<PropertyDescriptor> ite = ftb.getProperties().listIterator();

        while (ite.hasNext()) {

            final PropertyDescriptor desc = ite.next();
            if (desc instanceof GeometryDescriptor) {

                final GeometryType type = (GeometryType) desc.getType();

                descBuilder = new AttributeDescriptorBuilder();
                typeBuilder = new AttributeTypeBuilder();
                descBuilder.copy((AttributeDescriptor) desc);
                typeBuilder.copy(type);
                typeBuilder.setBinding(clazz);
                descBuilder.setType(typeBuilder.buildGeometryType());
                final PropertyDescriptor newDesc = descBuilder.buildDescriptor();
                ite.set(newDesc);
            }
        }

        return ftb.buildFeatureType();
    }

    /**
     * Create a custom projection (Conic or Mercator) for the geometry using the
     * geometry envelope.
     * @param geomEnvelope Geometry bounding envelope
     * @param longLatCRS WGS84 projection
     * @param unit unit wanted for the geometry
     * @return MathTransform
     * @throws NoSuchIdentifierException
     * @throws FactoryException
     */
    public static MathTransform changeProjection(final Envelope geomEnvelope, final GeographicCRS longLatCRS,
            final Unit<Length> unit) throws FactoryException {

        //collect data to create the projection
        final double centerMeridian = geomEnvelope.getWidth() / 2 + geomEnvelope.getMinX();
        final double centerParallal = geomEnvelope.getHeight() / 2 + geomEnvelope.getMinY();
        final double northParallal = geomEnvelope.getMaxY() - geomEnvelope.getHeight() / 3;
        final double southParallal = geomEnvelope.getMinY() + geomEnvelope.getHeight() / 3;

        boolean conicProjection = true;
        //if the geomery is near the equator we use the mercator projection
        if (geomEnvelope.getMaxY() > 0 && geomEnvelope.getMinY() < 0) {
            conicProjection = false;
        }
        //conicProjection = true;

        //create geometry lambert projection or mercator projection
        final Ellipsoid ellipsoid = longLatCRS.getDatum().getEllipsoid();
        double semiMajorAxis = ellipsoid.getSemiMajorAxis();
        double semiMinorAxis = ellipsoid.getSemiMinorAxis();

        final Unit<Length> projectionUnit = ellipsoid.getAxisUnit();
        //check for unit conversion
        if (unit != projectionUnit) {
            final  UnitConverter converter = projectionUnit.getConverterTo(unit);
            semiMajorAxis = converter.convert(semiMajorAxis);
            semiMinorAxis = converter.convert(semiMinorAxis);
        }

        final MathTransformFactory f = AuthorityFactoryFinder.getMathTransformFactory(null);
        ParameterValueGroup p;
        if (conicProjection) {

            p = f.getDefaultParameters("Albers_Conic_Equal_Area");
            p.parameter("semi_major").setValue(semiMajorAxis);
            p.parameter("semi_minor").setValue(semiMinorAxis);
            p.parameter("central_meridian").setValue(centerMeridian);
            p.parameter("standard_parallel_1").setValue(northParallal);
            p.parameter("standard_parallel_2").setValue(southParallal);
        } else {

            p = f.getDefaultParameters("Mercator_2SP");
            p.parameter("semi_major").setValue(semiMajorAxis);
            p.parameter("semi_minor").setValue(semiMinorAxis);
            p.parameter("central_meridian").setValue(centerMeridian);
            p.parameter("standard_parallel_1").setValue(centerParallal);
        }

        return f.createParameterizedTransform(p);
    }

    /**
     * Compute clipping between the feature geometry and the clipping geometry
     * @param featureGeometry Geometry
     * @param clippingGeometry Geometry
     * @return the intersection Geometry
     * If featureGeometry didn't intersect clippingGeometry the function return null;
     */
    public static Geometry testClipping(final Geometry featureGeometry, final Geometry clippingGeometry) {
        if(featureGeometry == null || clippingGeometry == null) return null;

        if(featureGeometry.intersects(clippingGeometry)){
            return featureGeometry.intersection(clippingGeometry);
        }else{
            return null;
        }
    }

    /**
     * Compute difference between the feature's geometry and the geometry
     * @param featureGeometry Geometry
     * @param diffGeometry Geometry
     * @return the computed geometry. Return the featureGeometry if there is no intersections
     * between geometries. And return null if the featureGeometry is contained into
     * the diffGeometry
     */
    public static Geometry testDifference(final Geometry featureGeometry, final Geometry diffGeometry) {
        if(featureGeometry == null || diffGeometry == null) return null;

        if(featureGeometry.intersects(diffGeometry)){
            if(diffGeometry.contains(featureGeometry)){
                return null;
            }else{
                return featureGeometry.difference(diffGeometry);
            }
        }else{
            return featureGeometry;
        }
    }







}
