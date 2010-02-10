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

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;

import org.geotoolkit.style.CollectionChangeEvent;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.StyleListener;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class WeakStyleListener extends WeakListener<StyleListener> implements StyleListener {


    public WeakStyleListener(StyleListener listener, Object src) {
        super(listener,src);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final StyleListener listener = listenerRef.get();
        if (listener != null) {
            listener.propertyChange(evt);
        }else{
            removeListener();
        }
    }

    @Override
    public void featureTypeStyleChange(CollectionChangeEvent<MutableFeatureTypeStyle> evt) {
        final StyleListener listener = listenerRef.get();
        if (listener != null) {
            listener.featureTypeStyleChange(evt);
        }else{
            removeListener();
        }
    }

    @Override
    protected void removeListener() {
        try {
            Method method = src.getClass().getMethod("removeListener", StyleListener.class);
            method.invoke(src, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
