/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2003 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2010, Geomatys
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
package org.geotoolkit.map;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.style.DefaultDescription;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.util.SimpleInternationalString;

import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.geotoolkit.util.ArgumentChecks.*;

/**
 * Utility class to create MapLayers, MapContexts and Elevation models from different sources.
 * This class is thread safe.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public final class MapBuilder {

    private MapBuilder(){}

    /**
     * Create a Default Mapcontext object using coordinate reference system : CRS:84.
     */
    public static MapContext createContext(){
        return createContext(DefaultGeographicCRS.WGS84);
    }

    /**
     * Create a Default Mapcontext object with the given coordinate reference system.
     * The crs is not used for renderering, it is only used when calling the getEnvelope
     * method.
     */
    public static MapContext createContext(final CoordinateReferenceSystem crs){
        return new DefaultMapContext(crs);
    }

    /**
     * Create a Default MapItem object. It can be used to group layers.
     */
    public static MapItem createItem(){
        return new DefaultMapItem();
    }

    /**
     * Create an empty map layer without any datas. It can be usefull in different
     * kind of applications, like holding a space in the mapcontext for a layer
     * when a datastore is unavailable.
     */
    public static EmptyMapLayer createEmptyMapLayer(){
        final Hints hints = new Hints();
        hints.put(Hints.STYLE_FACTORY, MutableStyleFactory.class);
        final MutableStyleFactory factory = (MutableStyleFactory)FactoryFinder.getStyleFactory(hints);
        return new EmptyMapLayer(factory.style());
    }

    /**
     * Create a default feature maplayer with a featurecollection and a style.
     */
    public static FeatureMapLayer createFeatureLayer(final FeatureCollection<? extends Feature> collection, final MutableStyle style){
        return new DefaultFeatureMapLayer(collection, style);
    }

    /**
     * Create a default coverage maplayer with a gridCoverage, a style and the grid name.
     */
    public static CoverageMapLayer createCoverageLayer(final GridCoverage2D grid, final MutableStyle style, final String name){
        return createCoverageLayer(new SimpleCoverageReader(grid), style, name);
    }

    /**
     * Create a default coverage maplayer with a coverageReader, a style and the grid name.
     */
    public static CoverageMapLayer createCoverageLayer(final GridCoverageReader reader, final MutableStyle style, final String name){
        ensureNonNull("name", name);
        final CoverageMapLayer layer = new DefaultCoverageMapLayer(reader, style, new DefaultName(name) );
        layer.setDescription(new DefaultDescription(new SimpleInternationalString(name), new SimpleInternationalString(name)));
        return layer;
    }

    /**
     * Create a default elevation model based on a grid coverage reader.
     *
     * @param grid : Coverage reader holding elevation values
     * @return ElevationModel
     */
    public static ElevationModel createElevationModel(final GridCoverageReader grid){
        FilterFactory FF = FactoryFinder.getFilterFactory(null);
        return new DefaultElevationModel(grid, FF.literal(0),FF.literal(1));
    }

    /**
     * Create a default elevation model based on a grid coverage reader.
     *
     * @param grid : Coverage reader holding elevation values
     * @param offset : expression used to modified on the fly the elevation value
     * @param scale : a multiplication factor to use on the coverage values
     * @return ElevationModel
     */
    public static ElevationModel createElevationModel(final GridCoverageReader grid, final Expression offset, final Expression scale){
        return new DefaultElevationModel(grid, offset,scale);
    }


    private static class SimpleCoverageReader extends GridCoverageReader{

        private final GridCoverage2D coverage;

        public SimpleCoverageReader(final GridCoverage2D coverage){
            this.coverage = coverage;
        }

        @Override
        public List<String> getCoverageNames() throws CoverageStoreException, CancellationException {
            return Collections.emptyList();
        }

        @Override
        public GeneralGridGeometry getGridGeometry(final int i) throws CoverageStoreException, CancellationException {
            return (GeneralGridGeometry) coverage.getGridGeometry();
        }

        @Override
        public List<GridSampleDimension> getSampleDimensions(final int i) throws CoverageStoreException, CancellationException {
            return Collections.singletonList(coverage.getSampleDimension(i));
        }

        @Override
        public GridCoverage read(final int i, final GridCoverageReadParam gcrp) throws CoverageStoreException, CancellationException {
            return coverage;
        }

    }

}
