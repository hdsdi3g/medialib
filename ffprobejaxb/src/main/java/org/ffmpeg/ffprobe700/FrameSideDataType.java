//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2024.04.17 at 09:30:15 PM CEST 
//


package org.ffmpeg.ffprobe700;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for frameSideDataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="frameSideDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="timecodes" type="{http://www.ffmpeg.org/schema/ffprobe}frameSideDataTimecodeList" minOccurs="0"/&gt;
 *         &lt;element name="components" type="{http://www.ffmpeg.org/schema/ffprobe}frameSideDataComponentList" minOccurs="0"/&gt;
 *         &lt;element name="side_datum" type="{http://www.ffmpeg.org/schema/ffprobe}frameSideDatumType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="side_data_type" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="side_data_size" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="timecode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "frameSideDataType", propOrder = {
    "timecodes",
    "components",
    "sideDatum"
})
public class FrameSideDataType {

    protected FrameSideDataTimecodeList timecodes;
    protected FrameSideDataComponentList components;
    @XmlElement(name = "side_datum")
    protected List<FrameSideDatumType> sideDatum;
    @XmlAttribute(name = "side_data_type")
    protected String sideDataType;
    @XmlAttribute(name = "side_data_size")
    protected Integer sideDataSize;
    @XmlAttribute(name = "timecode")
    protected String timecode;

    /**
     * Gets the value of the timecodes property.
     * 
     * @return
     *     possible object is
     *     {@link FrameSideDataTimecodeList }
     *     
     */
    public FrameSideDataTimecodeList getTimecodes() {
        return timecodes;
    }

    /**
     * Sets the value of the timecodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link FrameSideDataTimecodeList }
     *     
     */
    public void setTimecodes(FrameSideDataTimecodeList value) {
        this.timecodes = value;
    }

    /**
     * Gets the value of the components property.
     * 
     * @return
     *     possible object is
     *     {@link FrameSideDataComponentList }
     *     
     */
    public FrameSideDataComponentList getComponents() {
        return components;
    }

    /**
     * Sets the value of the components property.
     * 
     * @param value
     *     allowed object is
     *     {@link FrameSideDataComponentList }
     *     
     */
    public void setComponents(FrameSideDataComponentList value) {
        this.components = value;
    }

    /**
     * Gets the value of the sideDatum property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sideDatum property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSideDatum().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FrameSideDatumType }
     * 
     * 
     */
    public List<FrameSideDatumType> getSideDatum() {
        if (sideDatum == null) {
            sideDatum = new ArrayList<FrameSideDatumType>();
        }
        return this.sideDatum;
    }

    /**
     * Gets the value of the sideDataType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSideDataType() {
        return sideDataType;
    }

    /**
     * Sets the value of the sideDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSideDataType(String value) {
        this.sideDataType = value;
    }

    /**
     * Gets the value of the sideDataSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSideDataSize() {
        return sideDataSize;
    }

    /**
     * Sets the value of the sideDataSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSideDataSize(Integer value) {
        this.sideDataSize = value;
    }

    /**
     * Gets the value of the timecode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimecode() {
        return timecode;
    }

    /**
     * Sets the value of the timecode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimecode(String value) {
        this.timecode = value;
    }

}