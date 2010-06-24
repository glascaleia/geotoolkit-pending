package org.geotoolkit.data.atom.xml;

import java.net.URI;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geotoolkit.data.atom.model.AtomEmail;
import org.geotoolkit.data.atom.model.AtomLink;
import org.geotoolkit.data.atom.model.AtomPersonConstruct;
import org.geotoolkit.xml.StaxStreamWriter;
import static org.geotoolkit.data.atom.xml.AtomModelConstants.*;

/**
 *
 * @author Samuel Andrés
 */
public class AtomWriter extends StaxStreamWriter {

    public AtomWriter() {
        super();
    }

    public void setWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    /**
     *
     * @param author
     * @throws XMLStreamException
     */
    public void writeAuthor(AtomPersonConstruct author) throws XMLStreamException {
        writer.writeStartElement(URI_ATOM, TAG_AUTHOR);
        if (author.getParams() != null) {
            for (Object param : author.getParams()) {
                if (param instanceof String) {
                    this.writeName((String) param);
                } else if (param instanceof URI) {
                    this.writeUri((URI) param);
                } else if (param instanceof AtomEmail) {
                    this.writeEmail((AtomEmail) param);
                }
            }
        }
        writer.writeEndElement();
    }

    /**
     *
     * @param link
     * @throws XMLStreamException
     */
    public void writeLink(AtomLink link) throws XMLStreamException {
        writer.writeStartElement(URI_ATOM, TAG_LINK);
        if (link.getHref() != null) {
            writer.writeAttribute(ATT_HREF, link.getHref());
        }
        if (link.getRel() != null) {
            writer.writeAttribute(ATT_REL, link.getRel());
        }
        if (link.getType() != null) {
            writer.writeAttribute(ATT_TYPE, link.getType());
        }
        if (link.getHreflang() != null) {
            writer.writeAttribute(ATT_HREFLANG, link.getHreflang());
        }
        if (link.getTitle() != null) {
            writer.writeAttribute(ATT_TITLE, link.getTitle());
        }
        if (link.getLength() != null) {
            writer.writeAttribute(ATT_LENGTH, link.getLength());
        }
        writer.writeEndElement();
    }

    /**
     *
     * @param name
     * @throws XMLStreamException
     */
    private void writeName(String name) throws XMLStreamException {
        writer.writeStartElement(URI_ATOM, TAG_NAME);
        writer.writeCharacters(name);
        writer.writeEndElement();
    }

    /**
     *
     * @param uri
     * @throws XMLStreamException
     */
    private void writeUri(URI uri) throws XMLStreamException {
        writer.writeStartElement(URI_ATOM, TAG_URI);
        writer.writeCharacters(uri.toString());
        writer.writeEndElement();
    }

    /**
     * 
     * @param email
     * @throws XMLStreamException
     */
    private void writeEmail(AtomEmail email) throws XMLStreamException {
        writer.writeStartElement(URI_ATOM, TAG_EMAIL);
        writer.writeCharacters(email.getAddress());
        writer.writeEndElement();
    }
}
