/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Johann Sorel
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
package org.geotoolkit.gui.swing.style;

import org.opengis.util.InternationalString;
import org.opengis.style.Description;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.filter.visitor.IsStaticExpressionVisitor;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyleFactory;

import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import static org.geotoolkit.util.ArgumentChecks.*;

/**
 * Style element editor
 * 
 * @param T : style element class edited
 * @author Johann Sorel
 * @module pending
 */
public abstract class StyleElementEditor<T> extends JPanel {

    private static MutableStyleFactory STYLE_FACTORY = null;
    private static FilterFactory2 FILTER_FACTORY = null;

    public StyleElementEditor(){}
    
    /**
     * Style element nearly always have an Expression field
     * the layer is used to fill the possible attribut in the expression editor
     * @param layer
     */
    public void setLayer(final MapLayer layer){}
    
    /**
     * Layer used for expression edition in the style element
     * @return MapLayer
     */
    public MapLayer getLayer(){
        return null;
    }
    
    /**
     * the the edited object
     * @param target : object to edit
     */
    public abstract void parse(T target);
    
    /**
     * return the edited object if there is one.
     * Id no edited object has been set this will create a new one.
     * @return T object
     */
    public abstract T create();
    
    public void apply(){
    }
    
    protected synchronized static final MutableStyleFactory getStyleFactory(){
        if(STYLE_FACTORY == null){
            final Hints hints = new Hints();
            hints.put(Hints.STYLE_FACTORY, MutableStyleFactory.class);
            STYLE_FACTORY = (MutableStyleFactory)FactoryFinder.getStyleFactory(hints);
        }
        return STYLE_FACTORY;
    }

    protected synchronized static final FilterFactory2 getFilterFactory(){
        if(FILTER_FACTORY == null){
            final Hints hints = new Hints();
            hints.put(Hints.FILTER_FACTORY, FilterFactory2.class);
            FILTER_FACTORY = (FilterFactory2) FactoryFinder.getFilterFactory(hints);
        }
        return FILTER_FACTORY;
    }
    
    protected boolean isStatic(final Expression exp){
        ensureNonNull("expression", exp);
        return (Boolean) exp.accept(IsStaticExpressionVisitor.VISITOR, null);
    }
    
    /**
     * Will popup a small dialog with this style editor.
     */
    public T show(final MapLayer layer, final T target){
        setLayer(layer);
        parse(target);

        JDialog dialog = new JDialog();
        dialog.setContentPane(this);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        return create();
    }

    protected static String descriptionTitleText(final Description desc){
        if(desc == null){
            return "";
        }else{
            final InternationalString str = desc.getTitle();
            if(str != null){
                return str.toString();
            }else{
                return "";
            }
        }
    }
    
    protected static String descriptionAbstractText(final Description desc){
        if(desc == null){
            return "";
        }else{
            final InternationalString str = desc.getAbstract();
            if(str != null){
                return str.toString();
            }else{
                return "";
            }
        }
    }
    
}
