//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.12.31 at 03:48:29 PM CET 
//


package org.ffmpeg.ffprobe611;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for pixelFormatsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="pixelFormatsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pixel_format" type="{http://www.ffmpeg.org/schema/ffprobe}pixelFormatType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pixelFormatsType", propOrder = {
    "pixelFormat"
})
public class PixelFormatsType {

    @XmlElement(name = "pixel_format")
    protected List<PixelFormatType> pixelFormat;

    /**
     * Gets the value of the pixelFormat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pixelFormat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPixelFormat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PixelFormatType }
     * 
     * 
     */
    public List<PixelFormatType> getPixelFormat() {
        if (pixelFormat == null) {
            pixelFormat = new ArrayList<PixelFormatType>();
        }
        return this.pixelFormat;
    }

}
