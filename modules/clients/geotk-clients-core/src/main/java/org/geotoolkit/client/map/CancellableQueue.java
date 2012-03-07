/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Geomatys
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
package org.geotoolkit.client.map;

import java.util.concurrent.ArrayBlockingQueue;
import org.geotoolkit.util.Cancellable;

/**
 * Cancellable queue.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class CancellableQueue<T> extends ArrayBlockingQueue<T> implements Cancellable{

    private boolean cancelled = false;
    
    public CancellableQueue(int capacity) {
        super(capacity);
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
}
