/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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
package org.geotoolkit.xml.parameter;

import javax.xml.stream.XMLStreamException;
import org.geotoolkit.parameter.Parameter;
import org.geotoolkit.parameter.ParameterGroup;
import org.geotoolkit.xml.StaxStreamWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.xml.parameter.ParameterConstants.*;

/**
 * <p>This class provides a GeneralParameterValue writing method.</p>
 * 
 * @author Samuel Andrés
 * @module pending
 */
public class ParameterValueWriter extends StaxStreamWriter {

    /**
     * <p>This method writes an XML document containing a GeneralParameterValue</p>
     *
     * @param generalParameterValue
     */
    public void write(GeneralParameterValue generalParameterValue) throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.setDefaultNamespace(URI_PARAMETER);
        this.writeGeneralParameterValue(generalParameterValue);
        writer.writeEndDocument();
        writer.flush();
    }

    /**
     * <p>This method writes a GenaralParameterValue.</p>
     *
     * @param generalParameterValue
     * @throws XMLStreamException
     */
    private void writeGeneralParameterValue(
            GeneralParameterValue generalParameterValue)
            throws XMLStreamException {

        writer.writeStartElement(URI_PARAMETER,
                generalParameterValue.getDescriptor().getName().getCode().replace(' ', '_'));

        if (generalParameterValue instanceof Parameter) {
            this.writeParameterValue((Parameter) generalParameterValue);
        } else if (generalParameterValue instanceof ParameterGroup) {
            this.writeParameterValueGroup((ParameterGroup) generalParameterValue);
        }
        writer.writeEndElement();
    }

    /**
     * <p>This method writes a ParameterValue.</p>
     *
     * @param parameter
     * @throws XMLStreamException
     */
    private void writeParameterValue(ParameterValue parameter)
            throws XMLStreamException {
        writer.writeCharacters(parameter.getValue().toString());
    }

    /**
     * <p>This method writes a ParameterValueGroup.</p>
     *
     * @param parameterGroup
     * @throws XMLStreamException
     */
    private void writeParameterValueGroup(ParameterValueGroup parameterGroup)
            throws XMLStreamException {
        for (GeneralParameterValue value : parameterGroup.values()) {
            this.writeGeneralParameterValue(value);
        }
    }
}