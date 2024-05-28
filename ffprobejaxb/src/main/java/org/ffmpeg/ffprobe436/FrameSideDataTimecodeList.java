//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.12.31 at 03:48:01 PM CET 
//


package org.ffmpeg.ffprobe436;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for frameSideDataTimecodeList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="frameSideDataTimecodeList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="timecode" type="{http://www.ffmpeg.org/schema/ffprobe}frameSideDataTimecodeType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "frameSideDataTimecodeList", propOrder = {
    "timecode"
})
public class FrameSideDataTimecodeList {

    protected List<FrameSideDataTimecodeType> timecode;

    /**
     * Gets the value of the timecode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the timecode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTimecode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FrameSideDataTimecodeType }
     * 
     * 
     */
    public List<FrameSideDataTimecodeType> getTimecode() {
        if (timecode == null) {
            timecode = new ArrayList<FrameSideDataTimecodeType>();
        }
        return this.timecode;
    }

}