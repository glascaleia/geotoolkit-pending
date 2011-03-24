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
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.vector.VectorDescriptor;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Identifier;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Process return the nearest Feature(s) form a FeatureCollection to a geometry
 * @author Quentin Boileau
 * @module pending
 */
public class Nearest extends AbstractProcess {

    private static final GeometryFactory GF = new GeometryFactory();
    private static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
                                                new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));
    ParameterValueGroup result;

    /**
     * Default constructor
     */
    public Nearest() {
        super(NearestDescriptor.INSTANCE);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ParameterValueGroup getOutput() {
        return result;
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public void run() {
        try {
            final FeatureCollection<Feature> inputFeatureList = Parameters.value(NearestDescriptor.FEATURE_IN, inputParameters);
            final Geometry interGeom = Parameters.value(NearestDescriptor.GEOMETRY_IN, inputParameters);
            
            final NearestFeatureCollection resultFeatureList = 
                    new NearestFeatureCollection(inputFeatureList.subCollection(nearestQuery(inputFeatureList, interGeom)), interGeom);

            result = super.getOutput();
            result.parameter(VectorDescriptor.FEATURE_OUT.getName().getCode()).setValue(resultFeatureList);
        } catch (DataStoreException ex) {
            Logger.getLogger(Nearest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Create a query to filter nearest feature to the geometry
     * @param original
     * @param geom
     * @return nearest query filter
     */
    static Query nearestQuery (final FeatureCollection<Feature> original, final Geometry geom){

        double dist = Double.POSITIVE_INFINITY;
        final Collection<Identifier> listID = new ArrayList<Identifier>();

        final FeatureIterator<Feature> iter = original.iterator(null);
        while (iter.hasNext()) {
            final Feature feature = iter.next();
            for (Property property : feature.getProperties()) {
                if (property.getDescriptor() instanceof GeometryDescriptor) {

                    final double computedDist = geom.distance((Geometry) property.getValue());
                    
                    if(computedDist < dist){
                        listID.clear();
                        dist = computedDist;
                        listID.add(feature.getIdentifier());

                    }else {
                        if(computedDist == dist){
                            listID.add(feature.getIdentifier());
                        }
                    }
                }
            }
        }

        final Set<Identifier> setID = new HashSet<Identifier>();
        for (Identifier id : listID) {
            setID.add(id);
        }

        final Filter filter = FF.id(setID);
        return QueryBuilder.filtered(new DefaultName("nearest"), filter);

    }
}
