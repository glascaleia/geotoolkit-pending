/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009, Geomatys
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

package org.geotoolkit.data.memory;

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.geotoolkit.feature.LenientFeatureFactory;
import org.geotoolkit.feature.SchemaException;
import org.geotoolkit.geometry.jts.transform.GeometryCSTransformer;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.geometry.jts.transform.CoordinateSequenceMathTransformer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.converter.Classes;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.util.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * Basic support for a  FeatureIterator that reprojects the geometry attribut.
 *
 * @author Chris Holmes
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public abstract class GenericReprojectFeatureIterator<F extends Feature, R extends FeatureIterator<F>>
        implements FeatureIterator<F> {

    protected static final FeatureFactory FF = FactoryFinder
            .getFeatureFactory(new Hints(Hints.FEATURE_FACTORY, LenientFeatureFactory.class));

    protected final R iterator;

    /**
     * Creates a new instance of GenericReprojectFeatureIterator
     *
     * @param iterator FeatureReader to limit
     * @param maxFeatures maximum number of feature
     */
    private GenericReprojectFeatureIterator(final R iterator) {
        this.iterator = iterator;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public F next() throws DataStoreRuntimeException {
        return iterator.next();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() throws DataStoreRuntimeException {
        iterator.close();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean hasNext() throws DataStoreRuntimeException {
        return iterator.hasNext();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(Classes.getShortClassName(this));
        sb.append('\n');
        String subIterator = "\u2514\u2500\u2500" + iterator.toString(); //move text to the right
        subIterator = subIterator.replaceAll("\n", "\n\u00A0\u00A0\u00A0"); //move text to the right
        sb.append(subIterator);
        return sb.toString();
    }

    /**
     * Wrap a FeatureReader with a reprojection.
     *
     * @param <T> extends FeatureType
     * @param <F> extends Feature
     * @param <R> extends FeatureReader<T,F>
     */
    private static final class GenericReprojectFeatureReader<T extends FeatureType, F extends Feature, R extends FeatureReader<T,F>>
            extends GenericReprojectFeatureIterator<F,R> implements FeatureReader<T,F>{

        private final FeatureType schema;
        private final CoordinateReferenceSystem targetCRS;
        private final GeometryCSTransformer transformer;

        private GenericReprojectFeatureReader(R reader, CoordinateReferenceSystem targetCRS) throws FactoryException, SchemaException{
            super(reader);

            if (targetCRS == null) {
                throw new NullPointerException("CRS can not be null.");
            }

            final FeatureType type = reader.getFeatureType();
            this.targetCRS = targetCRS;
            final CoordinateReferenceSystem original = type.getGeometryDescriptor().getCoordinateReferenceSystem();

            if (targetCRS.equals(original)) {
                throw new IllegalArgumentException("CoordinateSystem " + targetCRS + " already used (check before using wrapper)");
            }

            this.schema = FeatureTypeUtilities.transform(type, targetCRS);

            if(original != null){
                //the crs is defined on the feature type
                final CoordinateSequenceMathTransformer trs =
                        new CoordinateSequenceMathTransformer(CRS.findMathTransform(original, targetCRS, true));
                transformer = new GeometryCSTransformer(trs);
            }else{
                transformer = null;
            }
            
        }


        @Override
        public F next() throws DataStoreRuntimeException {
            final Feature next = iterator.next();
            
            final Collection<Property> properties = new ArrayList<Property>();
            for(Property prop : next.getProperties()){
                if(prop instanceof GeometryAttribute){
                    Object value = prop.getValue();
                    if(value != null){
                        //create a new property with the projected type
                        prop = FF.createGeometryAttribute(value, (GeometryDescriptor)
                                schema.getDescriptor(prop.getDescriptor().getName()), null, null);

                        if(transformer != null){
                            //the transform applies to all feature
                            try {
                                prop.setValue(transformer.transform((Geometry) value));
                            } catch (TransformException e) {
                                throw new DataStoreRuntimeException("A transformation exception occurred while reprojecting data on the fly", e);
                            }
                        }else{
                            //each feature has a different CRS.
                            final CoordinateReferenceSystem original;
                            if(value instanceof Geometry){
                                final int srid = ((Geometry)value).getSRID();
                                try {
                                    original = CRS.decode(SRIDGenerator.toSRS(srid, SRIDGenerator.Version.V1));
                                } catch (NoSuchAuthorityCodeException ex) {
                                    throw new DataStoreRuntimeException("An exception occurred while reprojecting data on the fly", ex);
                                } catch (FactoryException ex) {
                                    throw new DataStoreRuntimeException("An exception occurred while reprojecting data on the fly", ex);
                                }
                            }else if(value instanceof org.opengis.geometry.Geometry){
                                original = ((org.opengis.geometry.Geometry)value).getCoordinateReferenceSystem();
                            }else{
                                original = null;
                            }

                            if(original != null){
                                try {
                                    final CoordinateSequenceMathTransformer trs =
                                            new CoordinateSequenceMathTransformer(CRS.findMathTransform(original, targetCRS, true));
                                    final GeometryCSTransformer transformer = new GeometryCSTransformer(trs);
                                    Geometry geom = transformer.transform((Geometry) value);
                                    geom.setSRID(SRIDGenerator.toSRID(targetCRS, SRIDGenerator.Version.V1));
                                    prop.setValue(geom);
                                } catch (Exception e) {
                                    throw new DataStoreRuntimeException("An exception occurred while reprojecting data on the fly", e);
                                }
                            }else{
                                Logging.getLogger(GenericReprojectFeatureIterator.class).log(
                                        Level.WARNING, "A feature in type :"+getFeatureType().getName() +" has no crs.");
                            }
                        }
                        
                    }
                }
                properties.add(prop);
            }
            return (F) FF.createFeature(properties, schema, next.getIdentifier().getID());
        }

        @Override
        public T getFeatureType() {
            return (T) schema;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    /**
     * Wrap a FeatureReader with a reprojection.
     */
    public static <T extends FeatureType, F extends Feature> FeatureReader<T, F> wrap(
            FeatureReader<T, F> reader, CoordinateReferenceSystem crs) throws FactoryException, SchemaException {
        final GeometryDescriptor desc = reader.getFeatureType().getGeometryDescriptor();
        if (desc != null) {
            return new GenericReprojectFeatureReader(reader, crs);
        } else {
            return reader;
        }
    }

}
