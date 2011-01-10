/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004 - 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.display2d.style.renderer;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.media.jai.Histogram;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.RenderedOp;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.processing.CoverageProcessingException;
import org.geotoolkit.coverage.processing.Operations;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.style.CachedRasterSymbolizer;
import org.geotoolkit.display2d.style.CachedSymbolizer;
import org.geotoolkit.display2d.style.raster.ShadedReliefOp;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.internal.coverage.CoverageUtilities;
import org.geotoolkit.internal.image.ColorUtilities;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.operation.transform.LinearTransform;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.style.function.Categorize;
import org.geotoolkit.style.function.Interpolate;
import org.geotoolkit.style.function.InterpolationPoint;
import org.geotoolkit.util.converter.Classes;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.ContrastMethod;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.SelectedChannelType;
import org.opengis.style.ShadedRelief;

/**
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class DefaultRasterSymbolizerRenderer extends AbstractCoverageSymbolizerRenderer<CachedRasterSymbolizer>{


    public DefaultRasterSymbolizerRenderer(final CachedRasterSymbolizer symbol, final RenderingContext2D context){
        super(symbol,context);
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void portray(final ProjectedCoverage projectedCoverage) throws PortrayalException{

        double[] resolution = renderingContext.getResolution();
        final Envelope bounds = new GeneralEnvelope(renderingContext.getCanvasObjectiveBounds());
        resolution = checkResolution(resolution,bounds);
        final GridCoverageReadParam param = new GridCoverageReadParam();
        
        param.setEnvelope(bounds);
        param.setResolution(resolution);

        GridCoverage2D dataCoverage;
        GridCoverage2D elevationCoverage;
        try {
            dataCoverage = projectedCoverage.getCoverage(param);
            elevationCoverage = projectedCoverage.getElevationCoverage(param);
        } catch (CoverageStoreException ex) {
            throw new PortrayalException(ex);
        }

        if(dataCoverage == null){
            LOGGER.log(Level.WARNING, "Requested an area where no coverage where found.");
            return;
        }

        final CoordinateReferenceSystem coverageCRS = dataCoverage.getCoordinateReferenceSystem();
        try{
            final CoordinateReferenceSystem candidate2D = CRSUtilities.getCRS2D(coverageCRS);
            if(!CRS.equalsIgnoreMetadata(candidate2D,renderingContext.getObjectiveCRS2D()) ){

                dataCoverage = (GridCoverage2D) Operations.DEFAULT.resample(dataCoverage.view(ViewType.NATIVE), renderingContext.getObjectiveCRS2D());
                
                if(dataCoverage != null){
                    dataCoverage = dataCoverage.view(ViewType.RENDERED);
                }
            }
        } catch (CoverageProcessingException ex) {
            monitor.exceptionOccured(ex, Level.WARNING);
            return;
        } catch(Exception ex){
            //several kind of errors can happen here, we catch anything to avoid blocking the map component.
            monitor.exceptionOccured(
                new IllegalStateException("Coverage is not in the requested CRS, found : " +
                "\n"+ coverageCRS +
                " was expecting : \n" +
                renderingContext.getObjectiveCRS() +
                "\nOriginal Cause:"+ ex.getMessage(), ex), Level.WARNING);
            return;
        }

        if(dataCoverage == null){
            LOGGER.log(Level.WARNING, "Reprojected coverage is null.");
            return;
        }

        //we must switch to objectiveCRS for grid coverage
        renderingContext.switchToObjectiveCRS();

        final RenderedImage img = applyStyle(dataCoverage, symbol.getSource(), hints);
        final MathTransform2D trs2D = dataCoverage.getGridGeometry().getGridToCRS2D(PixelOrientation.UPPER_LEFT);
        if(trs2D instanceof AffineTransform){
            g2d.setComposite(symbol.getJ2DComposite());
            g2d.drawRenderedImage(img, (AffineTransform)trs2D);
        }else if (trs2D instanceof LinearTransform) {
            final LinearTransform lt = (LinearTransform) trs2D;
            final int col = lt.getMatrix().getNumCol();
            final int row = lt.getMatrix().getNumRow();
            //TODO using only the first parameters of the linear transform
            throw new PortrayalException("Could not render image, GridToCRS is a not an AffineTransform, found a " + trs2D.getClass());
        }else{
            throw new PortrayalException("Could not render image, GridToCRS is a not an AffineTransform, found a " + trs2D.getClass() );
        }

        //draw the relief shading ----------------------------------------------
        final ShadedRelief relief = symbol.getSource().getShadedRelief();
        if(relief != null && elevationCoverage != null){
            elevationCoverage = elevationCoverage.view(ViewType.GEOPHYSICS);
            final MathTransform2D eleTrs2D = elevationCoverage.getGridGeometry().getGridToCRS2D();
            if(eleTrs2D instanceof AffineTransform){
                RenderedImage shadowImage = elevationCoverage.getRenderedImage();
                shadowImage = shadowed(shadowImage);

                if(shadowImage.getColorModel() != null && shadowImage.getSampleModel() != null){
                    //TODO should check this differently, dont know how yet.
//                    System.out.println("shadow seems valid");
//                    System.out.println("shadow color model : " +shadowImage.getColorModel());
//                    System.out.println("shadow sample model : " +shadowImage.getSampleModel());
//                    System.out.println("transform :" + eleTrs2D);

                    g2d.setComposite(GO2Utilities.ALPHA_COMPOSITE_1F);
//                    g2.setComposite(AlphaComposite.SrcAtop);
                    g2d.drawRenderedImage(shadowImage, (AffineTransform)eleTrs2D);
                }

            }else{
                throw new PortrayalException("Could not render elevation model, GridToCRS is a not an AffineTransform, found a " + eleTrs2D.getClass() );
            }

        }

        //draw the border if there is one---------------------------------------
        CachedSymbolizer outline = symbol.getOutLine();
        if(outline != null){
            GO2Utilities.portray(projectedCoverage, outline, renderingContext);
        }

        renderingContext.switchToDisplayCRS();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Renderedmage JAI image operations ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private static RenderedImage applyStyle(GridCoverage2D coverage, final RasterSymbolizer styleElement,
                final RenderingHints hints) throws PortrayalException {

        coverage = coverage.view(ViewType.RENDERED);
        RenderedImage image = coverage.getRenderedImage();

        //band select ----------------------------------------------------------
        final int nbDim = coverage.getNumSampleDimensions();
        if(nbDim > 1){

            //we can change sample dimension only if we have more then one available.
            final ChannelSelection selections = styleElement.getChannelSelection();
            final SelectedChannelType channel = selections.getGrayChannel();
            final SelectedChannelType[] channels = selections.getRGBChannels();
            final int[] indices;

            if(channel != null){
                indices = new int[]{
                    Integer.valueOf(channel.getChannelName())
                };
            }else{
                if (image.getColorModel().hasAlpha()) {
                    indices = new int[]{
                        Integer.valueOf(channels[0].getChannelName()),
                        Integer.valueOf(channels[1].getChannelName()),
                        Integer.valueOf(channels[2].getChannelName()),
                        // Here we suppose that the transparent band is the last one. This is the
                        // default behaviour with standard java.
                        image.getSampleModel().getNumBands() - 1
                        };
                } else {
                    indices = new int[]{
                        Integer.valueOf(channels[0].getChannelName()),
                        Integer.valueOf(channels[1].getChannelName()),
                        Integer.valueOf(channels[2].getChannelName())
                        };
                }
            }
            image = selectBand(image, indices);
        }

        //Recolor coverage -----------------------------------------------------
        final ColorMap recolor = styleElement.getColorMap();
        if(recolor != null && recolor.getFunction() != null){
            final Function fct = recolor.getFunction();
            image = recolor(image,fct);
        }

        //shaded relief---------------------------------------------------------
        //handle by the J2DGraphicUtilities

        //contrast enhancement -------------------------------------------------
        final ContrastEnhancement ce = styleElement.getContrastEnhancement();
        if(ce != null && image.getColorModel() instanceof ComponentColorModel){

            // histogram/normalize adjustment ----------------------------------
            final ContrastMethod method = ce.getMethod();
            if(ContrastMethod.HISTOGRAM.equals(method)){
                image = equalize(image);
            }else if(ContrastMethod.NORMALIZE.equals(method)){
                image = normalize(image);
            }

            // gamma correction ------------------------------------------------
            final Double gamma = ce.getGammaValue().evaluate(null, Double.class);
            if(gamma != null && gamma != 1){
                //Specification : page 35
                // A “GammaValue” tells how much to brighten (values greater than 1.0) or dim (values less than 1.0) an image.
                image = brigthen(image, (int)( (gamma-1)*255f ) );
            }

        }

        return image;

    }

    private static RenderedImage shadowed(final RenderedImage img){
        final ParameterBlock pb = new ParameterBlock();
        pb.setSource(img, 0);
//        return JAI.create("ShadedRelief", pb, null);

        return new ShadedReliefOp(img, null, null, null);
    }

    private static RenderedImage selectBand(final RenderedImage image, final int[] indices){
        if(image.getSampleModel().getNumBands() < indices.length){
            //not enough bands in the image
            LOGGER.log(Level.WARNING, "Raster Style define more bands than the data");
            return image;
        }else{
            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(indices);
            return JAI.create("bandSelect",pb);
        }
    }

    private static RenderedImage recolor(final RenderedImage image, final Function function){

        final int visibleBand = CoverageUtilities.getVisibleBand(image);
        final ColorModel candidate = image.getColorModel();

        /*
         * Extracts the ARGB codes from the ColorModel and invokes the
         * transformColormap(...) method.
         */
        final int[] ARGB;
        if(candidate instanceof IndexColorModel){
            final IndexColorModel colors = (IndexColorModel) candidate;
            final int mapSize = colors.getMapSize();
            ARGB = new int[mapSize];
            colors.getRGBs(ARGB);

        }else if(candidate instanceof ComponentColorModel){
            final ComponentColorModel colors = (ComponentColorModel) candidate;
            final int nbbit = colors.getPixelSize();
            final int mapSize = 1 << nbbit;
            ARGB = new int[mapSize];

            for(int j=0; j<mapSize;j++){
                int v = j*255/mapSize;
                int a = 255 << 24;
                int r = v << 16;
                int g = v <<  8;
                int b = v <<  0;
                ARGB[j] = a|r|g|b;
            }
        }else{
            // Current implementation supports only sources that use of index color model
            // and component color model
            throw new IllegalArgumentException(Errors.format(Errors.Keys.ILLEGAL_CLASS_$2,
                    Classes.getClass(candidate), IndexColorModel.class));
        }

        transformColormap(ARGB, function);

        /*
         * Gives the color model to the image layout and creates a new image using the Null
         * operation, which merely propagates its first source along the operation chain
         * unmodified (except for the ColorModel given in the layout in this case).
         */
        final ColorModel model = ColorUtilities.getIndexColorModel(ARGB, 1, visibleBand, -1);
        final ImageLayout layout = new ImageLayout().setColorModel(model);
        return new NullOpImage(image, layout, null, OpImage.OP_COMPUTE_BOUND);
    }

    private static int[] transformColormap(final int[] ARGB, final Function function){

        if( function == null || !( (function instanceof Interpolate) || (function instanceof Categorize)) ){
            //no function or unknown type, return the original sampleDimension
            return ARGB;
        }

        if(function instanceof Interpolate){
            final Interpolate interpole = (Interpolate) function;
            final List<InterpolationPoint> points = interpole.getInterpolationPoints();
            final double[] SE_VALUES = new double[points.size()];
            final int[] SE_ARGB = new int[points.size()];
            for(int i=0,n=points.size();i<n;i++){
                final InterpolationPoint point = points.get(i);
                SE_VALUES[i] = point.getData();
                SE_ARGB[i] = point.getValue().evaluate(null, Color.class).getRGB();
            }

            int lastStep = -1;
            int lastColor = -1;
            for(int k=0;k<SE_VALUES.length;k++){
                final double geoValue = SE_VALUES[k];
                final int currentColor = SE_ARGB[k];
                final int currentStep = (int)geoValue;

                //first element, dont interpolate colors
                if(k == 0){
                    lastColor = currentColor;
                    lastStep = -1;
                }

                final int stepInterval  = currentStep - lastStep;
                final int lastAlpha     = (lastColor>>>24) & 0xFF;
                final int lastRed       = (lastColor>>>16) & 0xFF;
                final int lastGreen     = (lastColor>>> 8) & 0xFF;
                final int lastBlue      = (lastColor>>> 0) & 0xFF;
                final int alphaInterval = ((currentColor>>>24) & 0xFF) - lastAlpha;
                final int redInterval   = ((currentColor>>>16) & 0xFF) - lastRed;
                final int greenInterval = ((currentColor>>> 8) & 0xFF) - lastGreen;
                final int blueInterval  = ((currentColor>>> 0) & 0xFF) - lastBlue;
                for(int i=lastStep+1 ; (i<=currentStep && i<ARGB.length) ; i++){
                    //calculate interpolated color
                    final int relativePosition = i-lastStep;
                    final double pourcent = (double)( (double)relativePosition / (double)stepInterval);
                    int a = lastAlpha + (int)(pourcent*alphaInterval);
                    int r = lastRed   + (int)(pourcent*redInterval);
                    int g = lastGreen + (int)(pourcent*greenInterval);
                    int b = lastBlue  + (int)(pourcent*blueInterval);
                    a <<= 24;
                    r <<= 16;
                    g <<=  8;
                    b <<=  0;
                    ARGB[i] = a|r|g|b;
                }

                lastStep = (int) currentStep;
                lastColor = currentColor;

                //last element, fill the remaining cell with the color
                if(k == SE_VALUES.length-1){
                    for(int i=lastStep ; i<ARGB.length ; i++){
                        ARGB[i] = currentColor;
                    }
                }

            }

        }else if(function instanceof Categorize){
            final Categorize categorize = (Categorize) function;
            final Map<Expression,Expression> categorizes = categorize.getThresholds();
            final List<Expression> keys = new ArrayList<Expression>(categorizes.keySet());
            final double[] SE_VALUES = new double[keys.size()];
            final int[] SE_ARGB = new int[keys.size()];

            final Set<Entry<Expression,Expression>> entries = categorizes.entrySet();

            int l=0;
            for(Entry<Expression,Expression> entry : entries){
                if(l==0){
                    SE_VALUES[0] = Double.NEGATIVE_INFINITY;
                    SE_ARGB[0] = entry.getValue().evaluate(null, Color.class).getRGB();
                }else{
                    SE_VALUES[l] = entry.getKey().evaluate(null, Double.class);
                    SE_ARGB[l] = entry.getValue().evaluate(null, Color.class).getRGB();
                }
                l++;
            }

            int step = 0;
            for(int k=0;k<SE_VALUES.length-1;k++){
                final double geoValue = SE_VALUES[k+1];
                int color = SE_ARGB[k];
                int sampleValue = (int) geoValue;

                for(int i=step ; (i<sampleValue && i<ARGB.length) ; i++){
                    ARGB[i] = color;
                }

                step = (int) sampleValue;
                if(step < 0) step = 0;

                //we are on the last element, fill the remaining cell with the color
                if(k == SE_VALUES.length-2){
                    color = SE_ARGB[k+1];
                    for(int i=step ; i<ARGB.length ; i++){
                        ARGB[i] = color;
                    }
                }

            }
        }

        return ARGB;
    }

    private static RenderedImage equalize(final RenderedImage source) {
        int sum = 0;
        byte[] cumulative = new byte[256];
        int array[] = getHistogram(source);

        float scale = 255.0F / (float) (source.getWidth() *
                                        source.getHeight());

        for ( int i = 0; i < 256; i++ ) {
            sum += array[i];
            cumulative[i] = (byte)((sum * scale) + .5F);
        }

        LookupTableJAI lookup = new LookupTableJAI(cumulative);

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(source);
        pb.add(lookup);

        return JAI.create("lookup", pb, null);
    }

    private static RenderedImage normalize(final RenderedImage source) {

        double[] mean = new double[] { 128.0,128.0,128.0 };
        double[] stDev = new double[] { 34.0,34.0,34.0 };
        float[][] CDFnorm = new float[3][];
        CDFnorm[0] = new float[256];
        CDFnorm[1] = new float[256];
        CDFnorm[2] = new float[256];

        double mu0 = mean[0];
        double mu1 = mean[1];
        double mu2 = mean[2];

        double twoSigmaSquared0 = 2.0*stDev[0]*stDev[0];
        double twoSigmaSquared1 = 2.0*stDev[1]*stDev[1];
        double twoSigmaSquared2 = 2.0*stDev[2]*stDev[2];

        CDFnorm[0][0] = (float)Math.exp(-mu0*mu0/twoSigmaSquared0);
        CDFnorm[1][0] = (float)Math.exp(-mu1*mu1/twoSigmaSquared1);
        CDFnorm[2][0] = (float)Math.exp(-mu2*mu2/twoSigmaSquared2);

        for ( int i = 1; i < 256; i++ ) {
            double deviation0 = i - mu0;
            double deviation1 = i - mu1;
            double deviation2 = i - mu2;
            CDFnorm[0][i] = CDFnorm[0][i-1] + (float)Math.exp(-deviation0*deviation0/twoSigmaSquared0);
            CDFnorm[1][i] = CDFnorm[1][i-1] + (float)Math.exp(-deviation1*deviation1/twoSigmaSquared1);
            CDFnorm[2][i] = CDFnorm[2][i-1] + (float)Math.exp(-deviation2*deviation2/twoSigmaSquared2);
        }

        for ( int i = 0; i < 256; i++ ) {
            CDFnorm[0][i] /= CDFnorm[0][255];
            CDFnorm[1][i] /= CDFnorm[1][255];
            CDFnorm[2][i] /= CDFnorm[2][255];
        }

        int[] bins = { 256 };
        double[] low = { 0.0D };
        double[] high = { 256.0D };

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(source);
        pb.add(null);
        pb.add(1);
        pb.add(1);
        pb.add(bins);
        pb.add(low);
        pb.add(high);

        RenderedOp fmt = JAI.create("histogram", pb, null);

        return JAI.create("matchcdf", fmt, CDFnorm);
    }

    private static int[] getHistogram(final RenderedImage image) {
        // set up the histogram
        final int[] bins = { 256 };
        final double[] low = { 0.0D };
        final double[] high = { 256.0D };

        final ParameterBlock pb = new ParameterBlock();
        pb.addSource(image);
        pb.add(null);
        pb.add(1);
        pb.add(1);
        pb.add(bins);
        pb.add(low);
        pb.add(high);

        final RenderedOp op = JAI.create("histogram", pb, null);
        final Histogram histogram = (Histogram) op.getProperty("histogram");

        // get histogram contents
        final int[] local_array = new int[histogram.getNumBins(0)];
        for ( int i = 0; i < histogram.getNumBins(0); i++ ) {
            local_array[i] = histogram.getBinSize(0, i);
        }

        return local_array;
    }

    private static RenderedImage brigthen(final RenderedImage image,final int brightness) throws PortrayalException{
        final ColorModel model = image.getColorModel();

        if(model instanceof IndexColorModel){
            //no contrast enhance for indexed colormap
            return image;
        }else if(model instanceof ComponentColorModel){

            byte[][] lut = new byte[3][256];
            byte[][] newlut = new byte[3][256];

            // initialize lookup table
            for ( int i = 0; i < 256; i++ ) {
               lut[0][i] = (byte) i;
               lut[1][i] = (byte) i;
               lut[2][i] = (byte) i;
            }

            for (int i = 0; i < 256; i++ ) {
                int red   = (int)lut[0][i]&0xFF;
                int green = (int)lut[1][i]&0xFF;
                int blue  = (int)lut[2][i]&0xFF;
                newlut[0][i] = clamp(red   + brightness);
                newlut[1][i] = clamp(green + brightness);
                newlut[2][i] = clamp(blue  + brightness);
            }

            return colorize(image,newlut);

        }else{
            throw new PortrayalException("Unsupported image color model, found :" + model.getClass());
        }

    }

    private static byte clamp(final int v) {
        if ( v > 255 ) {
            return (byte)255;
        } else if ( v < 0 ) {
            return (byte)0;
        } else {
            return (byte)v;
        }
    }

    private static RenderedImage colorize(final RenderedImage image, final byte[][] lt) {
        LookupTableJAI lookup = new LookupTableJAI(lt);
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(image);
        pb.add(lookup);
        return JAI.create("lookup", pb, null);
    }

}
