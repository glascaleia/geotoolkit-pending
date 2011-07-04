/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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
package org.geotoolkit.metadata.dimap;

import java.util.Collection;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import javax.media.jai.Warp;
import javax.media.jai.WarpAffine;

import org.geotoolkit.coverage.Category;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.TypeMap;
import org.geotoolkit.lang.Static;
import org.geotoolkit.metadata.iso.DefaultIdentifier;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.acquisition.DefaultAcquisitionInformation;
import org.geotoolkit.metadata.iso.acquisition.DefaultInstrument;
import org.geotoolkit.metadata.iso.acquisition.DefaultOperation;
import org.geotoolkit.metadata.iso.citation.DefaultCitation;
import org.geotoolkit.metadata.iso.citation.DefaultCitationDate;
import org.geotoolkit.metadata.iso.citation.DefaultContact;
import org.geotoolkit.metadata.iso.citation.DefaultOnlineResource;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;
import org.geotoolkit.metadata.iso.constraint.DefaultLegalConstraints;
import org.geotoolkit.metadata.iso.content.DefaultImageDescription;
import org.geotoolkit.metadata.iso.distribution.DefaultFormat;
import org.geotoolkit.metadata.iso.identification.AbstractIdentification;
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.geotoolkit.metadata.iso.lineage.DefaultAlgorithm;
import org.geotoolkit.metadata.iso.lineage.DefaultLineage;
import org.geotoolkit.metadata.iso.lineage.DefaultProcessStep;
import org.geotoolkit.metadata.iso.lineage.DefaultProcessing;
import org.geotoolkit.metadata.iso.quality.DefaultDataQuality;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.operation.transform.WarpTransform2D;
import org.geotoolkit.util.NumberRange;
import org.geotoolkit.util.SimpleInternationalString;
import org.geotoolkit.util.logging.Logging;

import org.opengis.coverage.SampleDimensionType;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.lineage.Lineage;
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.geotoolkit.metadata.dimap.DimapConstants.*;
import static org.geotoolkit.util.DomUtilities.*;

/**
 * Utility class to access usable objects from a dimap file.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public final class DimapAccessor extends Static {

    private static final Logger LOGGER = Logging.getLogger(DimapAccessor.class);

    private DimapAccessor() {
    }

    /**
     * Read the Coordinate Reference System of the grid.
     * Those informations are provided by the CoordinateReferenceSystem tag.
     *
     * @param doc
     * @return CoordinateReferenceSystem
     * @throws NoSuchAuthorityCodeException
     * @throws FactoryException
     */
    public static CoordinateReferenceSystem readCRS(final Element doc) throws NoSuchAuthorityCodeException, FactoryException{
        final Element ele = firstElement(doc, TAG_CRS);
        final Element code = firstElement(ele, TAG_HORIZONTAL_CS_CODE);
        return CRS.decode(code.getTextContent());
    }

    /**
     * Read the Grid to CRS transform.
     * Those informations are provided by the Geoposition tag.
     *
     * @param doc
     * @return AffineTransform
     * @throws FactoryException
     * @throws TransformException
     */
    public static AffineTransform readGridToCRS(final Element doc) throws FactoryException, TransformException{
        final Element ele = firstElement(doc, TAG_GEOPOSITION);
        final Element insert = firstElement(ele, TAG_GEOPOSITION_INSERT);
        final Element points = firstElement(ele, TAG_GEOPOSITION_POINTS);
        final Element affine = firstElement(ele, TAG_GEOPOSITION_AFFINE);

        if(insert != null){
            // X = ULXMAP + XDIM * i
            // Y = ULYMAP - YDIM * j
            final double ulx = textValueSafe(insert, TAG_ULXMAP, Double.class);
            final double uly = textValueSafe(insert, TAG_ULYMAP, Double.class);
            final double xdim = textValueSafe(insert, TAG_XDIM, Double.class);
            final double ydim = textValueSafe(insert, TAG_YDIM, Double.class);
            return new AffineTransform(xdim, 0, 0, -ydim, ulx, uly);
        }else if(affine != null){
            // X (CRS) = X0 + X1 * X(Data) + X2 * Y(Data)
            // Y (CRS) = Y0 + Y1 * X(Data) + Y2 * Y(Data)
            final double x0 = textValueSafe(affine, TAG_AFFINE_X0, Double.class);
            final double x1 = textValueSafe(affine, TAG_AFFINE_X1, Double.class);
            final double x2 = textValueSafe(affine, TAG_AFFINE_X2, Double.class);
            final double y0 = textValueSafe(affine, TAG_AFFINE_Y0, Double.class);
            final double y1 = textValueSafe(affine, TAG_AFFINE_Y1, Double.class);
            final double y2 = textValueSafe(affine, TAG_AFFINE_Y2, Double.class);
            return new AffineTransform(x0, y0, x1, y1, x2, y2);
        }else if(points != null){
            // transformation in not accurate if the method has been defined.
            // read the points and calculate an average transform from them.
            final NodeList tiePoints = ele.getElementsByTagName(TAG_TIE_POINT);
            final List<Point2D> sources = new ArrayList<Point2D>();
            final List<Point2D> dests = new ArrayList<Point2D>();

            for(int i=0,n=tiePoints.getLength();i<n;i++){
                final Element vertex = (Element) tiePoints.item(i);
                final double coordX = textValueSafe(vertex, TAG_TIE_POINT_CRS_X, Double.class);
                final double coordY = textValueSafe(vertex, TAG_TIE_POINT_CRS_Y, Double.class);
                final int dataY = textValueSafe(vertex, TAG_TIE_POINT_DATA_X, Integer.class);
                final int dataX = textValueSafe(vertex, TAG_TIE_POINT_DATA_Y, Integer.class);
                sources.add(new Point2D.Double(dataX,dataY));
                dests.add(new Point2D.Double(coordX,coordY));
            }

            final WarpTransform2D warptrs = new WarpTransform2D(
                    sources.toArray(new Point2D[sources.size()]),
                    dests.toArray(new Point2D[dests.size()]), 1);

            final Warp warp = warptrs.getWarp();
            if(warp instanceof WarpAffine){
                final WarpAffine wa = (WarpAffine) warp;
                return wa.getTransform();
            }else{
                throw new TransformException("Wrap transform is not affine.");
            }
        }else{
            throw new TransformException("Geopositioning type unknowned.");
        }

    }

    /**
     * Read the raster dimension from the document. This include number of rows,
     * columns and bands.
     * Those informations are provided by the Raster_dimensions tag.
     *
     * @param doc
     * @return int[] 0:rows, 1:cols, 2:bands
     */
    public static int[] readRasterDimension(final Element doc){
        final Element ele = firstElement(doc, TAG_RASTER_DIMENSIONS);
        final int rows = textValueSafe(ele, TAG_NROWS, Integer.class);
        final int cols = textValueSafe(ele, TAG_NCOLS, Integer.class);
        final int bands = textValueSafe(ele, TAG_NBANDS, Integer.class);
        return new int[]{rows,cols,bands};
    }

    /**
     * Read the coverage sample dimensions.
     * Those informations are provided by the Image_display tag.
     *
     * @param parent
     * @return GridSampleDimension
     */
    public static int[] readColorBandMapping(final Element parent){
        final Element ele = firstElement(parent, TAG_IMAGE_DISPLAY);
        if(ele == null) return null;
        final Element displayOrder = firstElement(ele, TAG_BAND_DISPLAY_ORDER);
        if(displayOrder == null) return null;

        //those parameters are mandatory
        final int red = textValueSafe(displayOrder, TAG_RED_CHANNEL, Integer.class);
        final int green = textValueSafe(displayOrder, TAG_GREEN_CHANNEL, Integer.class);
        final int blue = textValueSafe(displayOrder, TAG_BLUE_CHANNEL, Integer.class);

        return new int[]{red-1,green-1,blue-1};
    }

    /**
     * Read the coverage sample dimensions.
     * Those informations are provided by dimap tags :
     * - Image_Interpretation for description and sample to geophysic.
     * - Image_display for special values.
     * - Raster_Encoding for sample model bytes encoding
     *
     * @param doc
     * @return GridSampleDimension
     */
    public static GridSampleDimension[] readSampleDimensions(final Element doc, final String coverageName,final int nbbands){

        // read raster encoding informations -----------------------------------
        final Element nodeEncoding = firstElement(doc, TAG_RASTER_ENCODING);
        final int nbits         = textValueSafe(nodeEncoding, TAG_NBITS, Integer.class);
        final String byteOrder  = textValueSafe(nodeEncoding, TAG_BYTEORDER, String.class);
        final String dataType   = textValueSafe(nodeEncoding, TAG_DATA_TYPE, String.class);
        final Integer skip      = textValueSafe(nodeEncoding, TAG_SKIP_BYTES, Integer.class);
        final String layout     = textValueSafe(nodeEncoding, TAG_BANDS_LAYOUT, String.class);
        final SampleDimensionType dimensionType = TypeMap.getSampleDimensionType(DataType.valueOf(dataType).getNumberSet(),nbits);


        // read special values -------------------------------------------------
        final Element nodeDisplay   = firstElement(doc, TAG_IMAGE_DISPLAY);
        final Element nodeBandOrder = firstElement(nodeDisplay, TAG_BAND_DISPLAY_ORDER);
        final Integer red   = textValueSafe(nodeBandOrder, TAG_RED_CHANNEL, Integer.class);
        final Integer green = textValueSafe(nodeBandOrder, TAG_GREEN_CHANNEL, Integer.class);
        final Integer blue  = textValueSafe(nodeBandOrder, TAG_BLUE_CHANNEL, Integer.class);


        // read band statistics ------------------------------------------------
        final NodeList nodeStats = nodeDisplay.getElementsByTagName(TAG_BAND_STATISTICS);
        final Map<Integer,NumberRange> valueRanges = new HashMap<Integer, NumberRange>();
        for(int i=0,n=nodeStats.getLength(); i<n ;i++){
            final Element bandStat = (Element) nodeStats.item(i);
            final double stxMin     = textValueSafe(bandStat, TAG_STX_MIN, Double.class);
            final double stxMax     = textValueSafe(bandStat, TAG_STX_MAX, Double.class);
            final double stxMean    = textValueSafe(bandStat, TAG_STX_MEAN, Double.class);
            final double stxStdv    = textValueSafe(bandStat, TAG_STX_STDV, Double.class);
            final double stxLinMin  = textValueSafe(bandStat, TAG_STX_LIN_MIN, Double.class);
            final double stxLinMax  = textValueSafe(bandStat, TAG_STX_LIN_MAX, Double.class);
            final int bandIndex     = textValueSafe(bandStat, TAG_BAND_INDEX, Integer.class);
            valueRanges.put(bandIndex, NumberRange.create(stxMin, stxMax));
        }

        // read dimensions -----------------------------------------------------
        final Element nodeInterpretation = firstElement(doc, TAG_IMAGE_INTERPRETATION);
        final NodeList spectrals = nodeInterpretation.getElementsByTagName(TAG_SPECTRAL_BAND_INFO);
        final Map<Integer,GridSampleDimension> dimensions = new HashMap<Integer, GridSampleDimension>();

        for(int i=0,n=spectrals.getLength(); i<n ;i++){
            final Element spectre = (Element) spectrals.item(i);

            /*
            This record provides the unit of the physical value resulting from data radiometric count
            to physical measure conversion such as Illumination or height :
            L = X/A + B

            - L is the resulting physical value expressed in PHYSICAL_UNIT
            - X is the radiometric value at a given pixel location as stored in the raster file (unitless).
            - A is the gain (PHYSICAL_GAIN)
            - B is the bias (PHYSICAL_BIAS)
             */

            final int bandIndex     = textValueSafe(spectre, TAG_BAND_INDEX, Integer.class);
            final String bandDesc   = textValueSafe(spectre, TAG_BAND_DESCRIPTION, String.class);
            final String physicUnit = textValueSafe(spectre, TAG_PHYSICAL_UNIT, String.class);
            final double physicGain = textValueSafe(spectre, TAG_PHYSICAL_GAIN, Double.class);
            final double physicBias = textValueSafe(spectre, TAG_PHYSICAL_BIAS, Double.class);

            Unit unit = null;
            try{
                Unit.valueOf(physicUnit.trim());
            }catch(Exception ex){
                //catch anything, this doesn't always throw parse exception
                unit = Unit.ONE;
            }

            //range is in geophysic values, can not use it, todo convert it.
            final NumberRange range = valueRanges.get(bandIndex);

            final Category[] cats = new Category[]{
                new Category("vals", null, Integer.MIN_VALUE,Integer.MAX_VALUE,1/physicGain, physicBias)
            };

            final GridSampleDimension dim = new GridSampleDimension(bandDesc, cats, unit);
            dimensions.put(bandIndex, dim);
        }

        final GridSampleDimension[] dims = new GridSampleDimension[nbbands];
        for(int i=0; i<nbbands; i++){
            GridSampleDimension dim = dimensions.get(i+1);
            if(dim == null){
                //no information on this band, create an empty one
                dim = new GridSampleDimension(String.valueOf(i+1));
            }
            dims[i] = dim;
        }

        return dims;
    }

    /**
     * @return DatasetName from Dataset_ID tag.
     */
    public static String readDatasetName(final Element doc){
        final Element datasetID = firstElement(doc, TAG_DATASET_ID);
        final String name = textValueSafe(datasetID, TAG_DATASET_NAME, String.class);
        return name;
    }

    /**
     * Converts the given dimap document in a metadata object.
     * Since there is no one to one relation between ISO 19115 and Dimap,
     * the returned metadata is a best effort relation.
     *
     * @param doc
     * @param metadata : metadata to fill, if null it will create one.
     * @return Metadata, never null
     */
    public static DefaultMetadata fillMetadata(final Element doc, DefaultMetadata metadata) {

        if(metadata == null){
            metadata = new DefaultMetadata();
        }else{
            //to ensure we don't modify the original
            metadata = new DefaultMetadata(metadata);
        }

        
        //default values
        metadata.setCharacterSet(CharacterSet.UTF_8);
        metadata.setLanguage(Locale.ENGLISH);
        metadata.setDateStamp(new Date());



        //<xsd:element minOccurs="0" ref="Dataset_Id"/> ------------------------
        final Element datasetID = firstElement(doc, TAG_DATASET_ID);
        if(datasetID != null){
            //MAPPING
            //DATASET_NAME  → Dataset title (MD_Metadata.fileIdentifier)
            //COPYRIGHT     → RestrictionCode ( MD_Metadata > MD_Constraints > MD_LegalConstraints.accessConstraints)

            final String name = textValueSafe(datasetID, TAG_DATASET_NAME, String.class);
            final String copyright = textValueSafe(datasetID, TAG_DATASET_COPYRIGHT, String.class);

            if(name != null){
                metadata.setFileIdentifier(name);
            }

            if(copyright != null){
                final DefaultLegalConstraints constraints = new DefaultLegalConstraints();
                final Restriction restric = Restriction.COPYRIGHT;
                constraints.setAccessConstraints(Collections.singleton(restric));
                metadata.getMetadataConstraints().add(constraints);
            }

        }

        //<xsd:element minOccurs="0" ref="Dataset_Frame"/> ---------------------
        //has been set from the geotiff informations
        final Element datasetFrame = firstElement(doc, TAG_DATASET_FRAME);
        if(datasetFrame != null){
            //MAPPING
            //FRAME_LON                         → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.cornerPoints )
            //FRAME_LAT                         → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.cornerPoints )
            //FRAME_ROW                         → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.cornerPoints )
            //FRAME_COL                         → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.cornerPoints )
            //FRAME_X                           → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.cornerPoints )
            //FRAME_Y                           → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.cornerPoints )
            //FRAME_LON (Scene Center)          → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.centerPoints )
            //FRAME_LAT (Scene Center)          → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.centerPoints )
            //FRAME_ROW (Scene Center)          → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.centerPoints )
            //FRAME_COL (Scene Center)          → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.centerPoints )
            //SCENE_ORIENTATION (Scene Center)  → ( MD_Metadata > MD_SpatialRepresentation > MD_GridSpatialReprensentation > MD_Georectified.centerPoints )
        }

        //<xsd:element minOccurs="0" ref="Dataset_Use"/> -----------------------

        //<xsd:element minOccurs="0" ref="Production"/> ------------------------
        // can be changed in a Responsible party information
        final Element production = firstElement(doc, TAG_PRODUCTION);
        if(production != null){
            //MAPPING
            //PRODUCT_TYPE            → Type of product ( MD_Metada > identificationInfo > MD_DataIdentification.citation > CI_Citation.presentationForm > CI_PresentationFormCode )
            //PRODUCT_INFO            → Product title (MD_Metadata > identificationInfo > MD_DataIdentification.citation > CI_Citation.title)
            //DATASET_PRODUCER_NAME   → Producer Name (MD_Metadata > identificationInfo >MD_DataIdentification.citation > CI_Citation > CI_ResponsibleParty.organisationName )
            //DATASET_PRODUCER_URL    → URL Producer (MD_Metada > identificationInfo > /MD_DataIdentification.citation >CI_Citation > CI_ResponsibleParty > CI_Contact > CI_Address.electronicMailAddress
            //DATASET_PRODUCTION_DATE → Date de production (MD_Metadata > identificationInfo > MD_DataIdentification.citation > CI_Citation > CI_Date.date
            final String jobId          = textValueSafe(production, TAG_JOB_ID, String.class);
            final String productType    = textValueSafe(production, TAG_PRODUCT_TYPE, String.class);
            final String productInfo    = textValueSafe(production, TAG_PRODUCT_INFO, String.class);
            final String producerName   = textValueSafe(production, TAG_DATASET_PRODUCER_NAME, String.class);
            final Element producerEle = firstElement(production, TAG_DATASET_PRODUCER_URL);
            URI producerURL = null;
            try {
                producerURL = new URI(producerEle.getAttribute(ATT_HREF));
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            final Date productionDate = textValueSafe(production, TAG_DATASET_PRODUCTION_DATE, Date.class);

            final DefaultOnlineResource online = new DefaultOnlineResource(producerURL);
            final DefaultContact contact = new DefaultContact();
            contact.setOnlineResource(online);
            final DefaultResponsibleParty responsibleParty = new DefaultResponsibleParty(Role.ORIGINATOR);
            responsibleParty.setOrganisationName(new SimpleInternationalString(producerName));
            responsibleParty.setContactInfo(contact);
            final DefaultCitation citation = new DefaultCitation();
            citation.getPresentationForms().add(PresentationForm.valueOf(productType));
            citation.setTitle(new SimpleInternationalString(productInfo));
            citation.getCitedResponsibleParties().add(responsibleParty);
            citation.getDates().add(new DefaultCitationDate(productionDate, DateType.CREATION));
            final AbstractIdentification identification = getIdentification(metadata);
            identification.setCitation(citation);

            final Element facility = firstElement(production, TAG_PRODUCTION_FACILITY);
            if(facility != null){
                //SOFTWARE_NAME       → Software name (DQ_DataQuality > LI_Lineage > LI_ProcessStepL.E_ProcessStep > LE_Processing > CI_Citation.title)
                //SOFTWARE_VERSION    → Software version (DQ_DataQuality > LI_Lineage > LI_ProcessStepL.E_ProcessStep > LE_Processing > CI_Citation.edition)
                //PROCESSING_CENTER   → Processing center (DQ_DataQuality > LI_Lineage > LI_ProcessStepL.E_ProcessStep > LE_Processing > CI_Citation.citedResponsibleParty > CI_ResponsibleParty.OrganisationName)
                final String softwareName = textValueSafe(facility, TAG_PRODUCTION_FACILITY_SOFTWARE_NAME, String.class);
                final String softwareVersion = textValueSafe(facility, TAG_PRODUCTION_FACILITY_SOFTWARE_VERSION, String.class);
                final String productionCenter = textValueSafe(facility, TAG_PRODUCTION_FACILITY_PROCESSING_CENTER, String.class);


                final DefaultCitation softCitation = new DefaultCitation();
                softCitation.setTitle(new SimpleInternationalString(softwareName));
                softCitation.setEdition(new SimpleInternationalString(softwareVersion));

                if(productionCenter != null){
                    final DefaultResponsibleParty softResposibleParty = new DefaultResponsibleParty();
                    softResposibleParty.setOrganisationName(new SimpleInternationalString(productionCenter));
                    softCitation.getCitedResponsibleParties().add(softResposibleParty);
                }

                final DefaultProcessStep step = getProcessStep(metadata);
                DefaultProcessing processing = (DefaultProcessing) step.getProcessingInformation();
                if(processing == null){
                    processing = new DefaultProcessing();
                    step.setProcessingInformation(processing);
                }
                processing.getSoftwareReferences().add(softCitation);
            }

        }

        //<xsd:element minOccurs="0" ref="Dataset_Components"/> ----------------

        //<xsd:element minOccurs="0" ref="Quality_Assessment"/> ----------------
        final Element qualityAssessment = firstElement(doc, TAG_QUALITY_ASSESSMENT);
        if(qualityAssessment != null){
        }

        //<xsd:element minOccurs="0" ref="Coordinate_Reference_System"/> -------
        //has been set from the geotiff informations
        final Element datasetCRS = firstElement(doc, TAG_CRS);
        if(datasetCRS != null){
            //MAPPING
            //GEO_TABLES            → ( MD_METADATA > MD_ReferenceSystem.referenceSystemIdentifier >  RS_identifier.codeSpace and version )
            //HORIZONTAL_CS_CODE    → Reference Projection Système code (MD_Metadata > referenceSystemInfo > MD_ReferenceSystem.referenceSystemIdentifier > RS_Identifier.codeSpace)
            //HORIZONTAL_CS_TYPE    → ?
            //HORIZONTAL_CS_NAME    → Reference Projection Système name (MD_Metadata > referenceSystemInfo > MD_ReferenceSystem.referenceSystemIdentifier > RS_ReferenceSystem.name)

            //we use the code here to recreate a CoordinateReferenceSystem
            //final CoordinateReferenceSystem crs = readCRS(doc);
            //metadata.getReferenceSystemInfo().add(crs);
        }


        //<xsd:element minOccurs="0" ref="Raster_CS"/> -------------------------
        //has been set from the geotiff informations
        final Element rasterCS = firstElement(doc, TAG_RASTER_CS);
        if(rasterCS != null){
            //MAPPING
            //RASTER_CS_TYPE    → ?
            //PIXEL_ORIGIN      → ?
        }

        //<xsd:element minOccurs="0" ref="Geoposition"/> -----------------------
        //has been set from the geotiff informations
        final Element datasetGeoposition = firstElement(doc, TAG_GEOPOSITION);
        if(datasetGeoposition != null){
            //MAPPING
            //ULXMAP    → ?
            //ULYMAP    → ?
            //XDIM      → X Resolution (MD_Metadata > spatialRepresentationInfo> MD_GridSpatialRepresentation.axisDimensionsProperties > MD_Dimension.resolution)
            //YDIM      → Y Resolution (MD_Metadata > spatialRepresentationInfo> MD_GridSpatialRepresentation.axisDimensionsProperties > MD_Dimension.resolution)
        }

        //<xsd:element minOccurs="0" ref="Map_Declination"/> -------------------
        //<xsd:element minOccurs="0" ref="Raster_Dimensions"/> -----------------
        //has been set from the geotiff informations
        final Element rasterDim = firstElement(doc, TAG_RASTER_DIMENSIONS);
        if(rasterDim != null){
            //MAPPING
            //NCOLS     → Number of COLUMN (MD_Metadata > spatialRepresentationInfo > MD_GridSpatialRepresentation.axisDimensionsProperties >MD_Dimension.dimensionSize)
            //NROWS     → Number of ROWS (MD_Metadata > spatialRepresentationInfo > MD_GridSpatialRepresentation.axisDimensionsProperties >MD_Dimension.dimensionSize)
            //NBANDS    → ?
        }

        //<xsd:element minOccurs="0" ref="Raster_Encoding"/> -------------------
        final Element rasterEncoding = firstElement(doc, TAG_RASTER_ENCODING);
        if(rasterEncoding != null){
            //MAPPING
            //NBITS               → ?
            //BYTEORDER           → ?
            //COMPRESSION_NAME    → ?
            //DATA_TYPE           → ?
        }

        //<xsd:element minOccurs="0" ref="Data_Processing"/> -------------------
        final Element dataProcessing = firstElement(doc, TAG_DATA_PROCESSING);
        if(dataProcessing != null){
            //MAPPING
            //PROCESSING_LEVEL            → ( DQ_DATAQUALITY > LI_LINEAGE > LI_Source.LE_Source.processedLEvel > MD_Identifier.abstract )
            //GEOMETRIC_PROCESSING        → ( DQ_DATAQUALITY > LI_LINEAGE > LI_Source.LE_Source.processedLEvel > MD_Identifier.abstract )
            //RADIOMETRIC_PROCESSING      → ( DQ_DATAQUALITY > LI_LINEAGE > LI_Source.LE_Source.processedLEvel > MD_Identifier.abstract )
            //this does not exist in the ISO 19115 specification
//            final String processingLevel        = textValueSafe(dataProcessing, TAG_DATA_PROCESSING_PROCESSING_LEVEL, String.class);
//            final String geometricProcessing    = textValueSafe(dataProcessing, TAG_DATA_PROCESSING_GEOMETRIC_PROCESSING, String.class);
//            final String radiometricProcessing  = textValueSafe(dataProcessing, TAG_DATA_PROCESSING_RADIOMETRIC_PROCESSING, String.class);

            //MEAN_RECTIFICATION_ELEVATION→ ?
            //BAND_INDEX                  → ?
            //LOW_THRESHOLD               → ?
            //HIGH_THRESHOLD              → ?
            //LINE_SHIFT                  → ?
            //DECOMPRESSION_TYPE          → ?
            //KERNEL_ID                   → ?
            //KERNEL_DATE                 → ?
            //SAMPLING_STEP_X             → ?
            //SAMPLING_STEP_Y             → ?
            //SWIR_BAND_REGISTRATION_FLAG → ?
            //X_BANDS_REGISTRATION_FLAG   → ?
            //ALGORITHM_TYPE              → ( DQ_DATAQUALITY > LI_LINEAGE > LI_ProcessStep.LE_ProcessStep > LE_Processing > LE_Algorithm.description )
            //ALGORITHM_NAME              → ( DQ_DATAQUALITY > LI_LINEAGE > LI_ProcessStep.LE_ProcessStep > LE_Processing > LE_Algorithm > CI_Citation.title )
            //ALGORITHM_ACTIVATION        → ? no match but somewhere here ( DQ_DATAQUALITY > LI_LINEAGE > LI_ProcessStep.LE_ProcessStep > LE_Processing > LE_Algorithm )
            final String algoType               = textValueSafe(dataProcessing, TAG_DATA_PROCESSING_ALGORITHM_TYPE, String.class);
            final String algoName               = textValueSafe(dataProcessing, TAG_DATA_PROCESSING_ALGORITHM_NAME, String.class);
            //final String algoActivation         = textValueSafe(dataProcessing, TAG_DATA_PROCESSING_ALGORITHM_ACTIVATION, String.class);

            final DefaultCitation citation = new DefaultCitation();
            citation.setTitle(new SimpleInternationalString(algoName));

            final DefaultAlgorithm algo = new DefaultAlgorithm();
            algo.setDescription(new SimpleInternationalString(algoType));
            algo.setCitation(citation);

            final DefaultProcessStep step = getProcessStep(metadata);
            DefaultProcessing processing = (DefaultProcessing) step.getProcessingInformation();
            if(processing == null){
                processing = new DefaultProcessing();
                step.setProcessingInformation(processing);
            }
            processing.getAlgorithms().add(algo);
        }

        //<xsd:element minOccurs="0" ref="Data_Access"/> -----------------------
        final Element dataAccess = firstElement(doc, TAG_DATA_ACCESS);
        if(dataAccess != null){
            //MAPPING
            //DATA_FILE_ORGANISATION    → ?
            //DATA_FILE_FORMAT          → Data Format (MD_Metadata > IdentificationInfo > DataIdentification.resourceFormat > MD_Format.name et MD_Format.version)
            //DATA_FILE_FORMAT_DESC*    →
            //DATA_FILE_PATH            → ?
            final Element formatTag = firstElement(dataAccess, TAG_DATA_FILE_FORMAT);
            final String version = formatTag.getAttribute(ATT_VERSION);
            final String formatName = formatTag.getTextContent();

            final DefaultFormat format = new DefaultFormat();
            format.setName(new SimpleInternationalString(formatName));
            format.setVersion(new SimpleInternationalString(version));

            final AbstractIdentification idf = getIdentification(metadata);
            idf.getResourceFormats().add(format);
        }


        //<xsd:element minOccurs="0" ref="Image_Display"/> ---------------------
        final Element imageDisplay = firstElement(doc, TAG_IMAGE_DISPLAY);
        //has been set from the geotiff informations
        if(imageDisplay != null){
            //MAPPING
            //ULXMAP    → ?
            //ULYMAP    → ?
            //XDIM      → X Resolution (MD_Metadata > spatialRepresentationInfo> MD_GridSpatialRepresentation.axisDimensionsProperties > MD_Dimension.resolution)
            //YDIM      → Y Resolution (MD_Metadata > spatialRepresentationInfo> MD_GridSpatialRepresentation.axisDimensionsProperties > MD_Dimension.resolution)
        }

        //<xsd:element minOccurs="0" ref="Image_Interpretation"/> --------------
        final Element imageInter = firstElement(doc, TAG_IMAGE_INTERPRETATION);
        if(imageInter != null){
            //MAPPING
            //BAND_DESCRIPTION            → ?
            //PHYSICAL_UNIT               → ?
            //PHYSICAL_BIAS               → ?
            //PHYSICAL_GAIN               → ?
            //PHYSICAL_CALIBRATION_DATE   → ?
            //BAND_INDEX                  → ?
            //DATA_STRIP_ID               → ?
        }


        //<xsd:element minOccurs="0" ref="Dataset_Sources"/> -------------------
        // could be mapped to Aquisition informations
        final Element datasetSources = firstElement(doc, TAG_DATASET_SOURCES);
        if(datasetSources != null){
            //MAPPING
            //SOURCE_TYPE *             → ?
            //SOURCE_ID *               → ?
            //SOURCE_DESCRIPTION *      → Abstract ( MD_Metadata > identificationInfo > MD_DataIdentification.abstract )
            //GRID_REFERENCE            → ?
            //SHIFT_VALUE               → ?
            //IMAGING_DATE              → Acquisition date ( MI_AcquisitionInformation > MI_Operation.citation > MD_Citation.date)
            //IMAGING_TIME              → ?
            //MISSION                   → Mission ( MI_AcquisitionInformation > MI_Operation.description)
            //MISSION_INDEX             → Mission index (MI_AcquisitionInformation > MI_Operation.description.identifier > MD_identifier.code)
            //INSTRUMENT                → instrument ( MI_AcquisitionInformation > MI_Instrument.description.type )
            //INSTRUMENT_INDEX          → instrument description ( MI_AcquisitionInformation > MI_Instrument.type.description )
            //SENSOR_CODE
            // 3 following has no direct mapping nor approximative
            //SCENE_PROCESSING_LEVEL    → ( MD_Metadata > MD_ContentInformation.MD_CoverageDescription.MD_ImageDescription )
            //INCIDENCE_ANGLE           → ( MD_Metadata > MD_ContentInformation.MD_CoverageDescription.MD_ImageDescription )
            //VIEWING_ANGLE             → ( MD_Metadata > MD_ContentInformation.MD_CoverageDescription.MD_ImageDescription )
            //SUN_AZIMUTH               → Sun Azimut ( MD_Metadata > MD_ContentInformation > MD_CoverageDescription > MD_ImageDescription.illuminationAzimutAngle)
            //SUN_ELEVATION             → Sun elevation ( MD_Metadata > MD_ContentInformation > MD_CoverageDescription > MD_ImageDescription.illuminationElevationAngle)
            //REVOLUTION_NUMBER         → ?
            //COMPRESSION_MODE          → ?
            //DIRECT_PLAYBACK_INDICATOR → ?
            //REFOCUSING_STEP_NUM       → ?
            //SWATH_MODE                → ?
            final Element sourceInfo = firstElement(datasetSources, TAG_SOURCE_INFORMATION);
            final String sourceDesc = textValueSafe(sourceInfo, TAG_SOURCE_DESCRIPTION, String.class);
            final Element sceneSource = firstElement(datasetSources, TAG_SCENE_SOURCE);
            final Date imagingDate = textValueSafe(sceneSource, TAG_SCENE_IMAGING_DATE, Date.class);
            final String mission = textValueSafe(sceneSource, TAG_SCENE_MISSION, String.class);
            final String missionIndex = textValueSafe(sceneSource, TAG_SCENE_MISSION_INDEX, String.class);
            final String instrumentName = textValueSafe(sceneSource, TAG_SCENE_INSTRUMENT, String.class);
            final String instrumentIndex = textValueSafe(sceneSource, TAG_SCENE_INSTRUMENT_INDEX, String.class);
            final String processingLevel = textValueSafe(sceneSource, TAG_SCENE_PROCESSING_LEVEL, String.class);
            final String incidenceAngle = textValueSafe(sceneSource, TAG_SCENE_INCIDENCE_ANGLE, String.class);
            final String viewingAngle = textValueSafe(sceneSource, TAG_SCENE_VIEWING_ANGLE, String.class);
            final Double sunAzimuth = textValueSafe(sceneSource, TAG_SCENE_SUN_AZIMUTH, Double.class);
            final Double sunElevation = textValueSafe(sceneSource, TAG_SCENE_SUN_ELEVATION, Double.class);


            final AbstractIdentification idf = getIdentification(metadata);
            idf.setAbstract(new SimpleInternationalString(sourceDesc));

            final DefaultCitation citation = new DefaultCitation();
            citation.getDates().add(new DefaultCitationDate(imagingDate, DateType.CREATION));

            final DefaultIdentifier missinId = new DefaultIdentifier();
            missinId.setCode(missionIndex);

            final DefaultOperation operation = new DefaultOperation();
            operation.setCitation(citation);
            operation.setDescription(new SimpleInternationalString(mission));
            operation.setIdentifier(missinId);


            final DefaultIdentifier instrumentId = new DefaultIdentifier();
            instrumentId.setCode(instrumentIndex);

            final DefaultInstrument instrument = new DefaultInstrument();
            instrument.setDescription(new SimpleInternationalString(instrumentName));
            instrument.setIdentifier(instrumentId);


            final DefaultAcquisitionInformation acqInfo = new DefaultAcquisitionInformation();
            acqInfo.getOperations().add(operation);
            acqInfo.getInstruments().add(instrument);

            final DefaultImageDescription contentInfo = new DefaultImageDescription();
            contentInfo.setIlluminationAzimuthAngle(sunAzimuth);
            contentInfo.setIlluminationElevationAngle(sunElevation);

            metadata.getAcquisitionInformation().add(acqInfo);
            metadata.getContentInfo().add(contentInfo);

        }

        //Satellite_Time -------------------------------------------------------
        final Element satelliteTime = firstElement(doc, TAG_SATELLITE_TIME);
        if(satelliteTime != null){
            //MAPPING
            //UT_DATE         → ?
            //CLOCK_VALUE     → ?
            //CLOCK_PERIOD    → ?
            //BOARD_TIME      → ?
            //TAI_TUC         → ?
        }

        //<xsd:element minOccurs="0" ref="Vector_Attributes"/> -----------------

        return metadata;
    }

    private static DefaultProcessStep getProcessStep(final DefaultMetadata metadata){
        final DefaultLineage lineage = getLineage(metadata);

        final Collection<ProcessStep> steps = lineage.getProcessSteps();

        if(steps.isEmpty()){
            final DefaultProcessStep step = new DefaultProcessStep();
            steps.add(step);
            return step;
        }else{
            final List<ProcessStep> copies = new ArrayList<ProcessStep>(steps);
            final ProcessStep step = copies.get(0);
            if(step instanceof DefaultProcessStep){
                return (DefaultProcessStep) step;
            }else{
                final DefaultProcessStep copy = DefaultProcessStep.castOrCopy(step);
                copies.set(0, copy);
                //copy and replace collection
                lineage.setProcessSteps(copies);
                return copy;
            }
        }
    }

    private static DefaultLineage getLineage(final DefaultMetadata metadata){
        final DefaultDataQuality quality = getDataQuality(metadata);

        Lineage lineage = quality.getLineage();

        if(lineage == null){
            lineage = new DefaultLineage();
            quality.setLineage(lineage);
        }

        if(lineage instanceof DefaultLineage){
            return (DefaultLineage) lineage;
        }else{
            final DefaultLineage copy = DefaultLineage.castOrCopy(lineage);
            quality.setLineage(lineage);
            return copy;
        }
        
    }

    private static DefaultDataQuality getDataQuality(final DefaultMetadata metadata){
        final Collection<DataQuality> qualities = metadata.getDataQualityInfo();

        if(qualities.isEmpty()){
            final DefaultDataQuality quality = new DefaultDataQuality();
            metadata.getDataQualityInfo().add(quality);
            return quality;
        }else{
            final List<DataQuality> copies = new ArrayList<DataQuality>(qualities);
            final DataQuality quality = copies.get(0);
            if(quality instanceof DefaultDataQuality){
                return (DefaultDataQuality) quality;
            }else{
                final DefaultDataQuality copy = DefaultDataQuality.castOrCopy(quality);
                copies.set(0, copy);
                //copy and replace collection
                metadata.setDataQualityInfo(copies);
                return copy;
            }
        }

    }

    private static AbstractIdentification getIdentification(final DefaultMetadata metadata){
        final Collection<Identification> ids = metadata.getIdentificationInfo();

        if(ids.isEmpty()){
            final DefaultDataIdentification id = new DefaultDataIdentification();
            ids.add(id);
            return id;
        }else{
            final List<Identification> copies = new ArrayList<Identification>(ids);
            final Identification id = copies.get(0);
            if(id instanceof DefaultDataIdentification){
                return (DefaultDataIdentification) id;
            }else{
                final AbstractIdentification copy = DefaultDataIdentification.castOrCopy(id);
                copies.set(0, copy);
                //copy and replace collection
                metadata.setIdentificationInfo(copies);
                return copy;
            }
        }

    }

}
