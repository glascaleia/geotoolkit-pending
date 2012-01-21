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
package org.geotoolkit.data;

import java.io.Serializable;
import java.util.Map;
import org.geotoolkit.factory.Factory;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.metadata.iso.quality.DefaultConformanceResult;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.storage.DataStoreException;
import org.opengis.metadata.quality.ConformanceResult;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;

/**
 * A best of toolkit for DataStoreFactory implementors.
 * <p>
 * Will also allow me to mess with the interface API without breaking every
 * last DataStoreFactory out there.
 * </p>
 * <p>
 * The default implementations often hinge around the use of
 * getParameterInfo and the correct use of Param by your subclass.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public abstract class AbstractDataStoreFactory extends Factory implements DataStoreFactory {

    /** parameter for namespace of the datastore */
    public static final ParameterDescriptor<String> NAMESPACE =
             new DefaultParameterDescriptor<String>("namespace","Namespace prefix",String.class,null,false);


    /** Default Implementation abuses the naming convention.
     * <p>
     * Will return <code>Foo</code> for
     * <code>org.geotoolkit.data.foo.FooFactory</code>.
     * </p>
     * @return return display name based on class name
     */
    @Override
    public String getDisplayName() {
        String name = this.getClass().getName();

        name = name.substring(name.lastIndexOf('.'));
        if (name.endsWith("Factory")) {
            name = name.substring(0, name.length() - 7);
        }
        return name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public DataStore createDataStore(final Map<String, ? extends Serializable> params) throws DataStoreException {
        try{
            return createDataStore(FeatureUtilities.toParameter(params,getParametersDescriptor()));
        }catch(InvalidParameterValueException ex){
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public DataStore createNewDataStore(final Map<String, ? extends Serializable> params) throws DataStoreException {
        try{
            return createNewDataStore(FeatureUtilities.toParameter(params,getParametersDescriptor()));
        }catch(InvalidParameterValueException ex){
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean canProcess(final Map<String, ? extends Serializable> params) {
        try{
            return canProcess(FeatureUtilities.toParameter(params,getParametersDescriptor()));
        }catch(InvalidParameterValueException ex){
            return false;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean canProcess(final ParameterValueGroup params) {
        final ConformanceResult result = Parameters.isValid(params, getParametersDescriptor());
        return (result != null) && Boolean.TRUE.equals(result.pass());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ConformanceResult availability() {
        DefaultConformanceResult result =  new DefaultConformanceResult();
        result.setPass(Boolean.TRUE);
        return result;
    }


}
