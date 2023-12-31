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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for packetsAndFramesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="packetsAndFramesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="packet" type="{http://www.ffmpeg.org/schema/ffprobe}packetType"/&gt;
 *         &lt;element name="frame" type="{http://www.ffmpeg.org/schema/ffprobe}frameType"/&gt;
 *         &lt;element name="subtitle" type="{http://www.ffmpeg.org/schema/ffprobe}subtitleType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "packetsAndFramesType", propOrder = {
    "packetOrFrameOrSubtitle"
})
public class PacketsAndFramesType {

    @XmlElements({
        @XmlElement(name = "packet", type = PacketType.class),
        @XmlElement(name = "frame", type = FrameType.class),
        @XmlElement(name = "subtitle", type = SubtitleType.class)
    })
    protected List<Object> packetOrFrameOrSubtitle;

    /**
     * Gets the value of the packetOrFrameOrSubtitle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the packetOrFrameOrSubtitle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPacketOrFrameOrSubtitle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FrameType }
     * {@link PacketType }
     * {@link SubtitleType }
     * 
     * 
     */
    public List<Object> getPacketOrFrameOrSubtitle() {
        if (packetOrFrameOrSubtitle == null) {
            packetOrFrameOrSubtitle = new ArrayList<Object>();
        }
        return this.packetOrFrameOrSubtitle;
    }

}
