//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.12.31 at 03:48:29 PM CET 
//


package org.ffmpeg.ffprobe611;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for programType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="programType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tags" type="{http://www.ffmpeg.org/schema/ffprobe}tagsType" minOccurs="0"/&gt;
 *         &lt;element name="streams" type="{http://www.ffmpeg.org/schema/ffprobe}streamsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="program_id" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="program_num" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="nb_streams" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="pmt_pid" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="pcr_pid" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "programType", propOrder = {
    "tags",
    "streams"
})
public class ProgramType {

    protected TagsType tags;
    protected StreamsType streams;
    @XmlAttribute(name = "program_id", required = true)
    protected int programId;
    @XmlAttribute(name = "program_num", required = true)
    protected int programNum;
    @XmlAttribute(name = "nb_streams", required = true)
    protected int nbStreams;
    @XmlAttribute(name = "pmt_pid", required = true)
    protected int pmtPid;
    @XmlAttribute(name = "pcr_pid", required = true)
    protected int pcrPid;

    /**
     * Gets the value of the tags property.
     * 
     * @return
     *     possible object is
     *     {@link TagsType }
     *     
     */
    public TagsType getTags() {
        return tags;
    }

    /**
     * Sets the value of the tags property.
     * 
     * @param value
     *     allowed object is
     *     {@link TagsType }
     *     
     */
    public void setTags(TagsType value) {
        this.tags = value;
    }

    /**
     * Gets the value of the streams property.
     * 
     * @return
     *     possible object is
     *     {@link StreamsType }
     *     
     */
    public StreamsType getStreams() {
        return streams;
    }

    /**
     * Sets the value of the streams property.
     * 
     * @param value
     *     allowed object is
     *     {@link StreamsType }
     *     
     */
    public void setStreams(StreamsType value) {
        this.streams = value;
    }

    /**
     * Gets the value of the programId property.
     * 
     */
    public int getProgramId() {
        return programId;
    }

    /**
     * Sets the value of the programId property.
     * 
     */
    public void setProgramId(int value) {
        this.programId = value;
    }

    /**
     * Gets the value of the programNum property.
     * 
     */
    public int getProgramNum() {
        return programNum;
    }

    /**
     * Sets the value of the programNum property.
     * 
     */
    public void setProgramNum(int value) {
        this.programNum = value;
    }

    /**
     * Gets the value of the nbStreams property.
     * 
     */
    public int getNbStreams() {
        return nbStreams;
    }

    /**
     * Sets the value of the nbStreams property.
     * 
     */
    public void setNbStreams(int value) {
        this.nbStreams = value;
    }

    /**
     * Gets the value of the pmtPid property.
     * 
     */
    public int getPmtPid() {
        return pmtPid;
    }

    /**
     * Sets the value of the pmtPid property.
     * 
     */
    public void setPmtPid(int value) {
        this.pmtPid = value;
    }

    /**
     * Gets the value of the pcrPid property.
     * 
     */
    public int getPcrPid() {
        return pcrPid;
    }

    /**
     * Sets the value of the pcrPid property.
     * 
     */
    public void setPcrPid(int value) {
        this.pcrPid = value;
    }

}
