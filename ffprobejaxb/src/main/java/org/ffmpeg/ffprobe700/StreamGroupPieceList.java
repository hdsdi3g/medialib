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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for streamGroupPieceList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="streamGroupPieceList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="piece" type="{http://www.ffmpeg.org/schema/ffprobe}streamGroupPieceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "streamGroupPieceList", propOrder = {
    "piece"
})
public class StreamGroupPieceList {

    protected List<StreamGroupPieceType> piece;

    /**
     * Gets the value of the piece property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the piece property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPiece().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StreamGroupPieceType }
     * 
     * 
     */
    public List<StreamGroupPieceType> getPiece() {
        if (piece == null) {
            piece = new ArrayList<StreamGroupPieceType>();
        }
        return this.piece;
    }

}
