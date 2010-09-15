/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.style;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.geotoolkit.internal.ReferenceQueueConsumer;
import org.geotoolkit.util.Disposable;

import org.opengis.style.Symbolizer;

/**
 * Listener for Rule.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public interface RuleListener extends PropertyChangeListener{
    
    /**
     * Called when a change occurs in the living symbolizer collection.
     */
    void symbolizerChange(CollectionChangeEvent<Symbolizer> event);

    /**
     * Weak rule listener. Use it when you are not
     * sure that the listener will be correctly removed by your class.
     */
    public static final class Weak extends WeakReference<RuleListener> implements RuleListener,Disposable{

        private final MutableRule source;

        public Weak(MutableRule source, RuleListener ref) {
            super(ref, ReferenceQueueConsumer.DEFAULT.queue);
            this.source = source;
        }

        @Override
        public void dispose() {
            source.removeListener(this);
        }

        @Override
        public void symbolizerChange(CollectionChangeEvent<Symbolizer> event) {
            final RuleListener listener = get();
            if (listener != null) {
                listener.symbolizerChange(event);
            }
            //if the listener is null, that means we are in the reference queue and it will be disposed soon.
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final RuleListener listener = get();
            if (listener != null) {
                listener.propertyChange(evt);
            }
            //if the listener is null, that means we are in the reference queue and it will be disposed soon.
        }

    }

}
