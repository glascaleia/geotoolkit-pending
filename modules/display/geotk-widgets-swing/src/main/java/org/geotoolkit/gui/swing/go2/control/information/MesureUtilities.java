/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Johann Sorel
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
package org.geotoolkit.gui.swing.go2.control.information;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.GeodeticCalculator;
import org.geotoolkit.factory.AuthorityFactoryFinder;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.util.logging.Logging;

import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class MesureUtilities {

    private static final Logger LOGGER = Logging.getLogger(MesureUtilities.class);

    public static double calculateLenght(final Geometry geom, final CoordinateReferenceSystem geomCRS, final Unit<Length> unit){

        if(geom == null || !(geom instanceof LineString)) return 0;

        final LineString line = (LineString) geom;

        Coordinate[] coords = line.getCoordinates();

        try {
            final GeodeticCalculator calculator = new GeodeticCalculator(geomCRS);
            final GeneralDirectPosition pos = new GeneralDirectPosition(geomCRS);

            double lenght = 0;
            for(int i=0,n=coords.length-1;i<n;i++){
                Coordinate coord1 = coords[i];
                Coordinate coord2 = coords[i+1];

                pos.ordinates[0] = coord1.x;
                pos.ordinates[1] = coord1.y;
                calculator.setStartingPosition(pos);
                pos.ordinates[0] = coord2.x;
                pos.ordinates[1] = coord2.y;
                calculator.setDestinationPosition(pos);

                lenght += calculator.getOrthodromicDistance();
            }

            if(!SI.METRE.equals(unit)){
                UnitConverter converter = SI.METRE.getConverterTo(unit);
                lenght = converter.convert(lenght);
            }

            return lenght;

        } catch (MismatchedDimensionException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (TransformException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        return 0;
    }

    public static double calculateArea(final Geometry geom, final CoordinateReferenceSystem geomCRS, final Unit unit){

        if(geom == null || !(geom instanceof Polygon)) return 0;

        try {
            Envelope env = JTS.toEnvelope(geom);

            final GeographicCRS geoCRS = CRSUtilities.getStandardGeographicCRS2D(geomCRS);

            final MathTransform step0 = CRS.findMathTransform(geomCRS, geoCRS,true);
            Envelope genv = JTS.transform(env, step0);

            double centerMeridian = genv.getWidth()/2 + genv.getMinX();
            double northParallal = genv.getMaxY() - genv.getHeight()/3 ;
            double southParallal = genv.getMinY() + genv.getHeight()/3 ;

            final Ellipsoid ellipsoid = geoCRS.getDatum().getEllipsoid();

            MathTransformFactory f = AuthorityFactoryFinder.getMathTransformFactory(null);

            ParameterValueGroup p;
            p = f.getDefaultParameters("Albers_Conic_Equal_Area");
            p.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis());
            p.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis());
            p.parameter("central_meridian").setValue(centerMeridian);
            p.parameter("standard_parallel_1").setValue(northParallal);
            p.parameter("standard_parallel_2").setValue(southParallal);

            MathTransform step1 = CRS.findMathTransform(geomCRS, geoCRS);
            MathTransform step2 = f.createParameterizedTransform(p);
            MathTransform trs = f.createConcatenatedTransform(step1, step2);
            
            Geometry calculatedGeom = JTS.transform(geom, trs);
            double area = calculatedGeom.getArea();

            if(unit != SI.SQUARE_METRE){
                UnitConverter converter = SI.SQUARE_METRE.getConverterTo(unit);
                area = converter.convert(area);
            }

            return area;

        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (MismatchedDimensionException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (TransformException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } 

        return 0;        
    }


}
