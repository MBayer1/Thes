package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import javax.xml.bind.annotation.XmlAttribute;

public class PedestrianTrack implements IDTO {

    private String fromSectionId, fromComponentId;
    private PedestrianConsumerType fromSectionType;
    private Integer fromXPortPositionStart, fromYPortPositionStart,
            fromXPortPositionEnd, fromYPortPositionEnd;

    private String toSectionId, toComponentId;
    private PedestrianConsumerType toSectionType;
    private Integer toXPortPositionStart, toYPortPositionStart,
            toXPortPositionEnd, toYPortPositionEnd;


    @XmlAttribute
    public String getFromSectionId(){
        return fromSectionId;
    }

    public void setFromSectionId(String fromSectionId) {
        this.fromSectionId = fromSectionId;
    }

    @XmlAttribute
    public String getFromComponentId() {
        return fromComponentId;
    }

    public void setFromComponentId(String fromComponentId) {
        this.fromComponentId = fromComponentId;
    }

    @XmlAttribute
    public PedestrianConsumerType getFromSectionType() {
        return fromSectionType;
    }

    public void setFromSectionType(PedestrianConsumerType fromSectionType) {
        this.fromSectionType = fromSectionType;
    }

    @XmlAttribute
    public Integer getFromXPortPositionStart() {
        return fromXPortPositionStart;
    }

    public void setFromXPortPositionStart(Integer fromXPortPositionStart) {
        this.fromXPortPositionStart = fromXPortPositionStart;
    }

    @XmlAttribute
    public Integer getFromYPortPositionStart() {
        return fromYPortPositionStart;
    }

    public void setFromYPortPositionStart(Integer fromYPortPositionStart) {
        this.fromYPortPositionStart = fromYPortPositionStart;
    }

    @XmlAttribute
    public Integer getFromXPortPositionEnd() {
        return fromXPortPositionEnd;
    }

    public void setFromXPortPositionEnd (Integer fromXPortPositionEnd) {
        this.fromXPortPositionEnd = fromXPortPositionEnd;
    }

    @XmlAttribute
    public Integer getFromYPortPositionEnd() {
        return fromYPortPositionEnd;
    }

    public void setFromYPortPositionEnd(Integer fromYPortPositionEnd) {
        this.fromYPortPositionEnd = fromYPortPositionEnd;
    }



    @XmlAttribute
    public String getToSectionId(){
        return toSectionId;
    }

    public void setToSectionId(String toSectionId) {
        this.toSectionId = toSectionId;
    }

    @XmlAttribute
    public String getToComponentId() {
        return toComponentId;
    }

    public void setToComponentId(String toComponentId) {
        this.toComponentId = toComponentId;
    }

    @XmlAttribute
    public PedestrianConsumerType getToSectionType() {
        return toSectionType;
    }

    public void setToSectionType(PedestrianConsumerType toSectionType) {
        this.toSectionType = toSectionType;
    }

    @XmlAttribute
    public Integer getToXPortPositionStart() {
        return toXPortPositionStart;
    }

    public void setToXPortPositionStart(Integer toXPortPositionStart) {
        this.toXPortPositionStart = toXPortPositionStart;
    }

    @XmlAttribute
    public Integer getToYPortPositionStart() {
        return toYPortPositionStart;
    }

    public void setToYPortPositionStart(Integer toYPortPositionStart) {
        this.toYPortPositionStart = toYPortPositionStart;
    }

    @XmlAttribute
    public Integer getToXPortPositionEnd() {
        return toXPortPositionEnd;
    }

    public void setToXPortPositionEnd(Integer toXPortPositionEnd) {
        this.toXPortPositionEnd = toXPortPositionEnd;
    }

    @XmlAttribute
    public Integer getToYPortPositionEnd() {
        return toYPortPositionEnd;
    }

    public void setToYPortPositionEnd(Integer toYPortPositionEnd) {
        this.toYPortPositionEnd = toYPortPositionEnd;
    }


}
