/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Geomatys
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
package org.geotoolkit.display.primitive;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Level;

import org.opengis.display.canvas.Canvas;
import org.opengis.display.primitive.Graphic;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotoolkit.display.canvas.AbstractCanvas;
import org.geotoolkit.display.canvas.AbstractReferencedCanvas2D;
import org.geotoolkit.display.canvas.ReferencedCanvas2D;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.display.canvas.VisitFilter;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.factory.AuthorityFactoryFinder;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.util.logging.Logging;


/**
 * A graphic implementation with default support for Coordinate Reference System (CRS) management.
 * This class provides some methods specific to the GeotoolKit implementation of graphic primitive.
 * The {@link org.geotoolkit.display.canvas.ReferencedCanvas} expects instances of this class.
 *
 * @module pending
 * @since 2.3
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public abstract class AbstractReferencedGraphic extends AbstractGraphic implements ReferencedGraphic {
    
    /**
     * An envelope that completly encloses the graphic. Note that there is no guarantee
     * that the returned envelope is the smallest bounding box that encloses the graphic,
     * only that the graphic lies entirely within the indicated envelope.
     * <p>
     * The {@linkplain GeneralEnvelope#getCoordinateReferenceSystem coordinate reference system}
     * of this envelope should always be the {@linkplain #getObjectiveCRS objective CRS}.
     */
    private final GeneralEnvelope envelope;

    /**
     * A typical cell dimension for this graphic, or {@code null} if none.
     *
     * @see #getTypicalCellDimension
     * @see #setTypicalCellDimension
     */
    private double[] typicalCellDimension;

    /**
     * Constructs a new graphic using the specified objective CRS.
     *
     * @param  crs The objective coordinate reference system.
     * @throws IllegalArgumentException if {@code crs} is null.
     *
     * @see #setObjectiveCRS
     * @see #setEnvelope
     * @see #setTypicalCellDimension
     * @see #setZOrderHint
     */
    protected AbstractReferencedGraphic(final AbstractReferencedCanvas2D canvas, final CoordinateReferenceSystem crs)
            throws IllegalArgumentException {
        super(canvas);
        if (crs == null) {
            throw new IllegalArgumentException(Errors.getResources(getLocale())
                      .getString(Errors.Keys.ILLEGAL_ARGUMENT_$2, "crs", crs));
        }
        envelope = new GeneralEnvelope(crs);
        envelope.setToNull();
    }

    @Override
    public AbstractReferencedCanvas2D getCanvas() {
        return (AbstractReferencedCanvas2D) super.getCanvas();
    }

    /**
     * {@inheritDoc }
     * <p>
     * The referenced graphic listen to objective crs changes to update
     * the envelope and fire an event if needed.
     * </p>
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        super.propertyChange(evt);
        
        if(evt.getPropertyName().equals(AbstractReferencedCanvas2D.OBJECTIVE_CRS_PROPERTY)){
            final CoordinateReferenceSystem newCRS = (CoordinateReferenceSystem) evt.getNewValue();
            try {
                setObjectiveCRS(newCRS);
            } catch (TransformException ex) {
                Logging.getLogger(AbstractReferencedGraphic.class).log(Level.WARNING, null, ex);
            }
        }
        
    }
    
    /**
     * Sets the objective coordinate refernece system for this graphic. This method is usually
     * invoked in any of the following cases:
     * <p>
     * <ul>
     *   <li>From the graphic constructor.</li>
     *   <li>When this graphic has just been added to a canvas.</li>
     *   <li>When canvas objective CRS is modified.</li>
     * </ul>
     * <p>
     * This method transforms the {@linkplain #getEnvelope envelope} if needed. If a
     * subclass need to transform some additional internal data, it should override the
     * {@link #transform} method.
     * <p>
     * This method fires a {@value org.geotoolkit.display.canvas.DisplayObject#OBJECTIVE_CRS_PROPERTY}
     * property change event.
     *
     * @param  crs The new objective CRS.
     * @throws TransformException If this method do not accept the new CRS. In such case,
     *         this method should keep the old CRS and leaves this graphic in a consistent state.
     */
    protected void setObjectiveCRS(CoordinateReferenceSystem newCRS) throws TransformException {
        final CoordinateReferenceSystem oldCRS = this.envelope.getCoordinateReferenceSystem();
        newCRS = ((ReferencedCanvas2D)canvas).getObjectiveCRS2D();
        if (newCRS == null) {
            throw new IllegalArgumentException(Errors.getResources(getLocale()).getString(Errors.Keys.ILLEGAL_ARGUMENT_$2, "crs", newCRS));
        }

        synchronized (getTreeLock()) {
            if (CRS.equalsIgnoreMetadata(oldCRS, newCRS)) {
                /*
                 * If the new CRS is equivalent to the old one (except for metadata), then there
                 * is no need to apply any transformation. Just set the new CRS.  Note that this
                 * step may throws an IllegalArgumentException if the given CRS doesn't have the
                 * expected number of dimensions (actually it should never happen, since we just
                 * said that this CRS is equivalent to the previous one).
                 */
                envelope.setCoordinateReferenceSystem(newCRS);
            } else {
                GeneralEnvelope oldEnv = new GeneralEnvelope(envelope);

                /*
                 * If a coordinate transformation is required, gets the math transform preferably
                 * from the Canvas that own this graphic (in order to use any user supplied hints).
                 */
                final MathTransform transform;
                transform = getMathTransform(oldCRS, newCRS, "setObjectiveCRS");
                if (!transform.isIdentity()) {
                    /*
                     * Transforms the envelope, but do not modify yet the 'envelope' field.
                     * This change will be commited only after all computations have been successful.
                     */
                    final GeneralEnvelope newEnvelope;
                    final DirectPosition origin;
                    if (envelope.isNull() || envelope.isInfinite()) {
                        origin = new GeneralDirectPosition(oldCRS);
                        newEnvelope = new GeneralEnvelope(envelope);
                    } else {
                        origin = envelope.getMedian();
                        envelope.reduceToDomain(false);
                        /*
                         * Fix the envelope coordinates to the maximum dimensions allowed in the CRS definition.
                         * This envelope is just used for the canvas, the data values are not changed. The only
                         * risk to do so is to loose the graphic object if requesting a bounding box outside the
                         * allowed bounds, which case should not occur.
                         */
                        newEnvelope = CRS.transform(transform, envelope);
                    }
                    newEnvelope.setCoordinateReferenceSystem(newCRS);
                    /*
                     * Transforms the cell dimension. Only after all computations are successful,
                     * commit the changes to the 'envelope' and typicalCellDimension' class fields.
                     */
                    double[] cellDimension = typicalCellDimension;
                    if (cellDimension != null) {
                        cellDimension = CRS.deltaTransform(transform, origin, cellDimension);
                        for (int i = 0; i < cellDimension.length; i++) {
                            cellDimension[i] = Math.abs(cellDimension[i]);
                        }
                    }
                    envelope.setEnvelope(newEnvelope);
                    propertyListeners.firePropertyChange(ENVELOPE_PROPERTY, oldEnv, envelope);
                    typicalCellDimension = cellDimension;
                }
            }
            propertyListeners.firePropertyChange(OBJECTIVE_CRS_PROPERTY, oldCRS, newCRS);
        }
    }

    /**
     * Constructs a transform between two coordinate reference systems.
     *
     * @param  sourceCRS The source coordinate reference system.
     * @param  targetCRS The target coordinate reference system.
     * @param  sourceMethodName The caller method name, for logging purpose only.
     * @return A transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws TransformException if the transform can't be created.
     */
    private MathTransform getMathTransform(final CoordinateReferenceSystem sourceCRS,
                                           final CoordinateReferenceSystem targetCRS,
                                           final String sourceMethodName)
            throws TransformException{

        try {
            final Canvas owner = getCanvas();
            if (owner instanceof AbstractReferencedCanvas2D) {
                return ((AbstractReferencedCanvas2D) owner).getMathTransform(sourceCRS, targetCRS,
                       AbstractReferencedGraphic.class, sourceMethodName);
            } else {
                return AuthorityFactoryFinder.getCoordinateOperationFactory(null)
                       .createOperation(sourceCRS, targetCRS).getMathTransform();
            }
        } catch (FactoryException exception) {
            throw new TransformException(Errors.getResources(getLocale()).getString(
                        Errors.Keys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception);
        }
    }

    /**
     * Returns an envelope that completly encloses the graphic. Note that there is no guarantee
     * that the returned envelope is the smallest bounding box that encloses the graphic, only
     * that the graphic lies entirely within the indicated envelope.
     * <p>
     * The default implementation returns a {@linkplain GeneralEnvelope#setToNull null envelope}.
     * Subclasses should compute their envelope and invoke {@link #setEnvelope} as soon as they can.
     *
     * @see #setEnvelope
     */
    @Override
    public Envelope getEnvelope() {
        synchronized (getTreeLock()) {
            return new GeneralEnvelope(envelope);
        }
    }

    /**
     * Set the envelope for this graphic. Subclasses should invokes this method as soon as they
     * known their envelope.
     * <p>
     * This method fires a {@value org.geotoolkit.display.canvas.DisplayObject#ENVELOPE_PROPERTY}
     * property change event.
     *
     * @throws TransformException if the specified envelope can't be transformed to the
     *         {@linkplain #getObjectiveCRS objective CRS}.
     */
    protected void setEnvelope(final Envelope newEnvelope) throws TransformException {
        synchronized (getTreeLock()) {                        
            final GeneralEnvelope old = new GeneralEnvelope(envelope);
            this.envelope.setEnvelope(newEnvelope);
            setObjectiveCRS(getCanvas().getObjectiveCRS());
            propertyListeners.firePropertyChange(ENVELOPE_PROPERTY, old, envelope);
        }
    }

    @Override
    public boolean intersects(final Envelope candidate){

        final GeneralEnvelope copy = new GeneralEnvelope(this.envelope);
        copy.reduceToDomain(false);
        
        if(CRS.equalsIgnoreMetadata(copy.getCoordinateReferenceSystem(),
                                    candidate.getCoordinateReferenceSystem())){
            //same crs for both envelope, we can directly try a contain.
            return copy.intersects(candidate, true);            
        }else{
            //envelope have different projection, we need to reproject them
            //try reproject data envelope
            try {
                final MathTransform trs = CRS.findMathTransform(
                        copy.getCoordinateReferenceSystem(), 
                        candidate.getCoordinateReferenceSystem(), true);
                final GeneralEnvelope projEnv = CRS.transform(trs,copy);
                return projEnv.intersects(candidate, true);
            } catch (FactoryException ex) {
                getLogger().log(Level.WARNING, "",ex);
                //we failed this way, this may happen since reprojection from one
                //crs to the other is not always possible.
            }catch (TransformException ex) {
                getLogger().log(Level.WARNING, "",ex);
                //we failed this way, this may happen since reprojection from one
                //crs to the other is not always possible.
            }
            
            //We could not reproject the envelope one way, try the other way
            try {
                final MathTransform trs = CRS.findMathTransform(
                        candidate.getCoordinateReferenceSystem(),
                        copy.getCoordinateReferenceSystem(), 
                        true);
                final GeneralEnvelope projEnv = CRS.transform(trs,candidate);
                return projEnv.intersects(copy, true);
            } catch (FactoryException ex) {
                getLogger().log(Level.WARNING, "",ex);
                //we failed this way, this may happen since reprojection from one
                //crs to the other is not always possible.
            }catch (TransformException ex) {
                getLogger().log(Level.WARNING, "",ex);
                //we failed this way, this may happen since reprojection from one
                //crs to the other is not always possible.
            }
            //every reprojection failed, we assume we can paint the graphic
            //in such an area (the symbolizer renderer should take care of the problem,
            //so return true
            return true;
        }
               
    }
    
    /**
     * Use to grab an ReferencedGraphic at some position.
     * 
     * @param point : point in display crs
     * @return ReferencedGraphic, can be this object or a child object
     */
    @Override
    public abstract List<Graphic> getGraphicAt(RenderingContext context, SearchArea mask, VisitFilter filter, List<Graphic> graphics);

}
