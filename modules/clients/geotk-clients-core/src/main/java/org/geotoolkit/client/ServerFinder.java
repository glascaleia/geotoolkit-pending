/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2012, Geomatys
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
package org.geotoolkit.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.spi.ServiceRegistry;
import org.geotoolkit.factory.DynamicFactoryRegistry;
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.internal.LazySet;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.logging.Logging;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Enable programs to find all available server implementations.
 *
 * <p>
 * In order to be located by this finder servers must provide an
 * implementation of the {@link ServerFactory} interface.
 * </p>
 *
 * <p>
 * In addition to implementing this interface datasouces should have a services
 * file:<br/><code>META-INF/services/org.geotoolkit.client.ServerFactory</code>
 * </p>
 *
 * <p>
 * The file should contain a single line which gives the full name of the
 * implementing class.
 * </p>
 *
 * <p>
 * Example:<br/><code>org.geotoolkit.client.mytype.MyTypeServerFactory</code>
 * </p>
 * @module pending
 */
public final class ServerFinder{

    /** The logger for the server module. */
    private static final Logger LOGGER = Logging.getLogger(ServerFinder.class);
    /**
     * The service registry for this manager. Will be initialized only when
     * first needed.
     */
    private static FactoryRegistry registry;

    private ServerFinder() {}

    /**
     * @see ServerFinder#getServer(java.util.Map) 
     * Get a server wich has a single parameter. 
     * This is a utility method that will redirect to getServer(java.util.Map)
     */
    public static synchronized Server get(
            final String key, final Serializable value) throws DataStoreException{
        return get(Collections.singletonMap(key, value));
    }

    /**
     * Checks each available server implementation in turn and returns the
     * first one which claims to support the resource identified by the params
     * object.
     *
     * @param params
     *            A Map object which contains a defenition of the resource to
     *            connect to.
     *
     * @return The first server which claims to process the required
     *         resource, returns null if none can be found.
     *
     * @throws IOException
     *             If a suitable loader can be found, but it can not be attached
     *             to the specified resource without errors.
     */
    public static synchronized Server get(
            final Map<String, Serializable> params) throws DataStoreException {
        final Iterator<ServerFactory> ps = getAvailableFactories();


        DataStoreException canProcessButNotAvailable = null;
        while (ps.hasNext()) {
            final ServerFactory fac = (ServerFactory) ps.next();
            boolean canProcess = false;
            try {
                canProcess = fac.canProcess(params);
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Problem asking " + fac.getDisplayName() + " if it can process request:" + t, t);
                // Protect against Servers that don't carefully code
                // canProcess
                continue;
            }
            if (canProcess) {
                boolean isAvailable = false;
                try {
                    isAvailable = fac.availability().pass();
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Difficulity checking if " + fac.getDisplayName() + " is available:" + t, t);
                    // Protect against Servers that don't carefully code
                    // isAvailable
                    continue;
                }
                if (isAvailable) {
                    try {
                        return fac.create(params);
                    } catch (DataStoreException couldNotConnect) {
                        canProcessButNotAvailable = couldNotConnect;
                        LOGGER.log(Level.WARNING, fac.getDisplayName() + " should be used, but could not connect", couldNotConnect);
                    }
                } else {
                    canProcessButNotAvailable = new DataStoreException(
                            fac.getDisplayName() + " should be used, but is not availble. Have you installed the required drivers or jar files?");
                    LOGGER.log(Level.WARNING, fac.getDisplayName() + " should be used, but is not availble", canProcessButNotAvailable);
                }
            }
        }
        if (canProcessButNotAvailable != null) {
            throw canProcessButNotAvailable;
        }
        return null;
    }

    /**
     * Checks each available server implementation in turn and returns the
     * first one which claims to support the resource identified by the params
     * object.
     *
     * @param parameters
     *            A parameter value group with all requiered configuration.
     *
     * @return The first datasource which claims to process the required
     *         resource, returns null if none can be found.
     *
     * @throws IOException
     *             If a suitable loader can be found, but it can not be attached
     *             to the specified resource without errors.
     */
    public static synchronized Server get(
            final ParameterValueGroup parameters) throws DataStoreException {
        final Iterator<ServerFactory> ps = getAvailableFactories();

        DataStoreException canProcessButNotAvailable = null;
        while (ps.hasNext()) {
            final ServerFactory fac = (ServerFactory) ps.next();
            boolean canProcess = false;
            try {
                canProcess = fac.canProcess(parameters);
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Problem asking " + fac.getDisplayName() + " if it can process request:" + t, t);
                // Protect against Servers that don't carefully code
                // canProcess
                continue;
            }
            if (canProcess) {
                boolean isAvailable = false;
                try {
                    isAvailable = fac.availability().pass();
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Difficulity checking if " + fac.getDisplayName() + " is available:" + t, t);
                    // Protect against Servers that don't carefully code
                    // isAvailable
                    continue;
                }
                if (isAvailable) {
                    try {
                        return fac.create(parameters);
                    } catch (DataStoreException couldNotConnect) {
                        canProcessButNotAvailable = couldNotConnect;
                        LOGGER.log(Level.WARNING, fac.getDisplayName() + " should be used, but could not connect", couldNotConnect);
                    }
                } else {
                    canProcessButNotAvailable = new DataStoreException(
                            fac.getDisplayName() + " should be used, but is not availble. Have you installed the required drivers or jar files?");
                    LOGGER.log(Level.WARNING, fac.getDisplayName() + " should be used, but is not availble", canProcessButNotAvailable);
                }
            }
        }
        if (canProcessButNotAvailable != null) {
            throw canProcessButNotAvailable;
        }
        return null;
    }

    /**
     * Finds all implemtaions of ServerFactory which have registered using
     * the services mechanism, regardless weather it has the appropriate
     * libraries on the classpath.
     *
     * @return An iterator over all discovered server factories
     */
    public static synchronized Iterator<ServerFactory> getAllFactories() {
        return getAllFactories(null);
    }

    /**
     * @see ServerFinder#getFactories() 
     * Get all factories of a given class.
     */
    public static synchronized <T extends ServerFactory> Iterator<T> getAllFactories(final Class<T> type){
        final FactoryRegistry serviceRegistry = getServiceRegistry();

        ServiceRegistry.Filter filter = null;
        if(type != null){
            filter = new ServiceRegistry.Filter() {
                @Override
                public boolean filter(Object provider) {
                    return type.isInstance(provider);
                }
            };
        }

        final Iterator<ServerFactory> allDataAccess = serviceRegistry.getServiceProviders(ServerFactory.class, filter, null, null);

        return new LazySet(allDataAccess).iterator();
    }

    /**
     * Finds all implementations of Factory which have registered using
     * the services mechanism, and that have the appropriate libraries on the
     * classpath.
     *
     * @return An iterator over all discovered factories, 
     *          and whose available method returns true.
     */
    public static synchronized Iterator<ServerFactory> getAvailableFactories() {
        return getAvailableFactories(null);
    }

    /**
     * @see ServerFinder#getAvailableFactories()
     * Get all available factories of a given class.
     */
    public static synchronized <T extends ServerFactory> Iterator<T> getAvailableFactories(final Class<T> type) {
        final Iterator<T> allStores = getAllFactories(type);

        final Set<T> availableStores = new HashSet<T>();

        while (allStores.hasNext()) {
            final T dsFactory = allStores.next();
            if (dsFactory.availability().pass()) {
                availableStores.add(dsFactory);
            }
        }
        
        return availableStores.iterator();
    }

    /**
     * Returns the service registry. The registry will be created the first time
     * this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(ServerFinder.class);
        if (registry == null) {
            registry = new DynamicFactoryRegistry(ServerFactory.class);
        }
        return registry;
    }

    /**
     * Scans for factory plug-ins on the application class path. This method is
     * needed because the application class path can theoretically change, or
     * additional plug-ins may become available. Rather than re-scanning the
     * classpath on every invocation of the API, the class path is scanned
     * automatically only on the first invocation. Clients can call this method
     * to prompt a re-scan. Thus this method need only be invoked by
     * sophisticated applications which dynamically make new plug-ins available
     * at runtime.
     */
    public static synchronized void scanForPlugins() {
        getServiceRegistry().scanForPlugins();
    }

}
