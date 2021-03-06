/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
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
package org.geotoolkit.display.canvas.control;

import java.util.logging.Level;

/**
 * TODO: document me, pretty please.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class StopOnErrorMonitor extends FailOnErrorMonitor {

    private Exception lastError = null;

    public Exception getLastException() {
        return lastError;
    }

    @Override
    public void renderingStarted() {
        lastError = null;
        super.renderingStarted();
    }

    @Override
    public void exceptionOccured(final Exception ex, final Level level) {
        lastError = ex;
        //request stop rendering
        stopRendering();
    }
}
