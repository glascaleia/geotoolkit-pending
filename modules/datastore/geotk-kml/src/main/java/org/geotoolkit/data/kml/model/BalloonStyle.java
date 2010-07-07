package org.geotoolkit.data.kml.model;

import java.awt.Color;

/**
 *
 * <p>This interface maps BalloonStyle elements.</p>
 *
 * <pre>
 * &lt;element name="BalloonStyle" type="kml:BalloonStyleType" substitutionGroup="kml:AbstractSubStyleGroup"/>
 *
 * &lt;complexType name="BalloonStyleType" final="#all">
 *  &lt;complexContent>
 *      &lt;extension base="kml:AbstractSubStyleType">
 *          &lt;sequence>
 *              &lt;choice>
 *                  &lt;annotation>
 *                      &lt;documentation>color deprecated in 2.1&lt;/documentation>
 *                  &lt;/annotation>
 *                  &lt;element ref="kml:color" minOccurs="0"/>
 *                  &lt;element ref="kml:bgColor" minOccurs="0"/>
 *              &lt;/choice>
 *              &lt;element ref="kml:textColor" minOccurs="0"/>
 *              &lt;element ref="kml:text" minOccurs="0"/>
 *              &lt;element ref="kml:displayMode" minOccurs="0"/>
 *              &lt;element ref="kml:BalloonStyleSimpleExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *              &lt;element ref="kml:BalloonStyleObjectExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *          &lt;/sequence>
 *      &lt;/extension>
 *  &lt;/complexContent>
 * &lt;/complexType>
 *
 * &lt;element name="BalloonStyleSimpleExtensionGroup" abstract="true" type="anySimpleType"/>
 * &lt;element name="BalloonStyleObjectExtensionGroup" abstract="true" substitutionGroup="kml:AbstractObjectGroup"/>
 * </pre>
 *
 * @author Samuel Andrés
 */
public interface BalloonStyle extends AbstractSubStyle {

    /**
     *
     * @return the background color.
     */
    public Color getBgColor();

    /**
     *
     * @return the text color.
     */
    public Color getTextColor();

    /**
     *
     * @return the text content.
     */
    public Object getText();

    /**
     *
     * @return the display mode
     */
    public DisplayMode getDisplayMode();
    
    /**
     *
     * @param bgColor
     */
    public void setBgColor(Color bgColor);

    /**
     *
     * @param textColor
     */
    public void setTextColor(Color textColor);

    /**
     *
     * @param text
     */
    public void setText(Object text);

    /**
     *
     * @param displayMode
     */
    public void setDisplayMode(DisplayMode displayMode);

}
