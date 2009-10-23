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
package org.geotoolkit.display.exception;

/**
 * Exception that may be thrown by a portraying operation.
 * 
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @module pending
 */
public final class PortrayalException extends Exception{

    private static final String ERROR = "Portrayal exception";
    private static final long serialVersionUID = 3200411272785006830L;
    
    public PortrayalException(String message){
        super(ERROR +" : "+ message);
    }

    public PortrayalException(Throwable throwable){
        super(ERROR+" : " + throwable.getMessage(), throwable);
    }
    
    public PortrayalException(String message, Throwable throwable){
        super(ERROR +" : "+ ((message != null) ? message : "no message"), throwable);
    }
}
