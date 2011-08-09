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
package org.geotoolkit.process.vector.nearest;

import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.vector.VectorDescriptor;
import org.geotoolkit.process.vector.VectorProcessUtils;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Identifier;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 * Process return the nearest Feature(s) form a FeatureCollection to a geometry
 * @author Quentin Boileau
 * @module pending
 */
public class Nearest extends AbstractProcess {

    private static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));

    /**
     * Default constructor
     */
    public Nearest(final ParameterValueGroup input) {
        super(NearestDescriptor.INSTANCE,input);
    }


    /**
     *  {@inheritDoc }
     */
    @Override
    public ParameterValueGroup call() throws ProcessException{
        try {
            fireStartEvent(new ProcessEvent(this));
            final FeatureCollection<Feature> inputFeatureList = Parameters.value(NearestDescriptor.FEATURE_IN, inputParameters);
            final Geometry interGeom = Parameters.value(NearestDescriptor.GEOMETRY_IN, inputParameters);

            final FeatureCollection resultFeatureList =
                    new NearestFeatureCollection(inputFeatureList.subCollection(nearestQuery(inputFeatureList, interGeom)));

            outputParameters.parameter(VectorDescriptor.FEATURE_OUT.getName().getCode()).setValue(resultFeatureList);
            fireEndEvent(new ProcessEvent(this, null,100));
            return outputParameters;

        } catch (NoSuchAuthorityCodeException ex) {
            fireFailEvent(new ProcessEvent(this, null,0, ex));
            throw new ProcessException(ex.getMessage(), this, ex);
        } catch (FactoryException ex) {
            fireFailEvent(new ProcessEvent(this, null,0, ex));
            throw new ProcessException(ex.getMessage(), this, ex);
        } catch (DataStoreException ex) {
            fireFailEvent(new ProcessEvent(this, null,0, ex));
            throw new ProcessException(ex.getMessage(), this, ex);
        } catch (MismatchedDimensionException ex) {
            fireFailEvent(new ProcessEvent(this, null,0, ex));
            throw new ProcessException(ex.getMessage(), this, ex);
        } catch (TransformException ex) {
            fireFailEvent(new ProcessEvent(this, null,0, ex));
            throw new ProcessException(ex.getMessage(), this, ex);
        }
        
    }

    /**
     * Create a query to filter nearest feature to the geometry
     * @param original
     * @param geom
     * @return nearest query filter
     */
    private Query nearestQuery(final FeatureCollection<Feature> original, final Geometry geom)
            throws FactoryException, MismatchedDimensionException, TransformException {

        CoordinateReferenceSystem geomCrs = JTS.findCoordinateReferenceSystem(geom);

        if (geomCrs == null) {
            geomCrs = original.getFeatureType().getCoordinateReferenceSystem();
        }

        double dist = Double.POSITIVE_INFINITY;
        final Collection<Identifier> listID = new ArrayList<Identifier>();

        final FeatureIterator<Feature> iter = original.iterator(null);
        try {
            while (iter.hasNext()) {
                final Feature feature = iter.next();
                for (Property property : feature.getProperties()) {
                    if (property.getDescriptor() instanceof GeometryDescriptor) {

                        Geometry featureGeom = (Geometry) property.getValue();
                        final GeometryDescriptor geomDesc = (GeometryDescriptor) property.getDescriptor();
                        final CoordinateReferenceSystem featureGeomCRS = geomDesc.getCoordinateReferenceSystem();

                        //re-project feature geometry into input geometry CRS
                        featureGeom = VectorProcessUtils.repojectGeometry(geomCrs, featureGeomCRS, featureGeom);
                        
                        final double computedDist = geom.distance((Geometry) property.getValue());

                        if (computedDist < dist) {
                            listID.clear();
                            dist = computedDist;
                            listID.add(feature.getIdentifier());

                        } else {
                            if (computedDist == dist) {
                                listID.add(feature.getIdentifier());
                            }
                        }
                    }
                }
            }
        } finally {
            iter.close();
        }

        final Set<Identifier> setID = new HashSet<Identifier>();
        for (Identifier id : listID) {
            setID.add(id);
        }

        final Filter filter = FF.id(setID);
        return QueryBuilder.filtered(new DefaultName("nearest"), filter);

    }
}
