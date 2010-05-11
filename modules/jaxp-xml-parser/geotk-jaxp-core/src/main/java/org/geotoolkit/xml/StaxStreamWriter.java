/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
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

package org.geotoolkit.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;


/**
 * An abstract class for all stax writer.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public abstract class StaxStreamWriter {

    protected XMLStreamWriter writer;
    
    /**
     * Store the output stream if it was generated by the parser itself.
     * It will closed on the dispose method or when a new input is set.
     */
    private OutputStream targetStream;

    public StaxStreamWriter(){
    }

    /**
     * Acces the underlying stax writer.
     */
    public XMLStreamWriter getWriter(){
        return writer;
    }

    /**
     * close potentiel previous stream and cache if there are some.
     * This way the writer can be reused for a different output later.
     */
    public void reset() throws IOException, XMLStreamException{
        if(targetStream != null){
            targetStream.close();
            targetStream = null;
        }
        if(writer != null){
            writer.close();
            writer = null;
        }
    }

    /**
     * Release potentiel locks or opened stream.
     * Must be called when the writer is not needed anymore.
     * It should not be used after this method has been called.
     */
    public void dispose() throws IOException, XMLStreamException{
        reset();
    }

    /**
     * Set the output for this writer.
     * Handle types are :
     * - java.io.File
     * - java.io.Writer
     * - java.io.OutputStream
     * - javax.xml.stream.XMLStreamWriter
     * - javax.xml.transform.Result
     * 
     * @param output
     * @throws IOException
     * @throws XMLStreamException
     */
    public void setOutput(Object output) throws IOException, XMLStreamException{
        reset();

        if(output instanceof XMLStreamWriter){
            writer = (XMLStreamWriter) output;
            return;
        }

        if(output instanceof File){
            targetStream = new FileOutputStream((File)output);
            output = targetStream;
        }

        writer = toWriter(output);
    }

    /**
     * Write a new tag with the text corresponding to the given value.
     * The tag won't be written if the value is null.
     * @param namespace : namespace of the wanted tag
     * @param localName : local name of the wanted tag
     * @param value : text value to write
     * @throws XMLStreamException
     */
    protected void writeSimpleTag(String namespace, String localName, Object value) throws XMLStreamException{
        if(value != null){
            writer.writeStartElement(namespace, localName);
            writer.writeCharacters(value.toString());
            writer.writeEndElement();
        }
    }

    /**
     * Creates a new XMLStreamWriter.
     * @param output
     * @return XMLStreamWriter
     * @throws XMLStreamException if the output is not handled
     */
    private static final XMLStreamWriter toWriter(Object output)
            throws XMLStreamException{
        final XMLOutputFactory XMLfactory = XMLOutputFactory.newInstance();
        XMLfactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        if(output instanceof OutputStream){
            return XMLfactory.createXMLStreamWriter((OutputStream)output);
        }else if(output instanceof Result){
            return XMLfactory.createXMLStreamWriter((Result)output);
        }else if(output instanceof Writer){
            return XMLfactory.createXMLStreamWriter((Writer)output);
        }else{
            throw new XMLStreamException("Output type is not supported : "+ output);
        }
    }

}
