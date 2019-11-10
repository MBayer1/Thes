package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import javax.xml.bind.annotation.XmlAttribute;

public class PedestrianTrack implements IDTO {

    private String fromSectionId, fromComponentId;
    private PedestrianConsumerType fromSectionType;
    private String fromXPortPositionStart, fromYPortPositionStart,
            fromXPortPositionEnd, fromYPortPositionEnd;

    private String toSectionId, toComponentId;
    private PedestrianConsumerType toSectionType;
    private String toXPortPositionStart, toYPortPositionStart,
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
    public String getFromXPortPositionStart() {
        return fromXPortPositionStart;
    }

    public void setFromXPortPositionStart(String fromXPortPositionStart) {
        this.fromXPortPositionStart = fromXPortPositionStart;
    }

    @XmlAttribute
    public String getFromYPortPositionStart() {
        return fromYPortPositionStart;
    }

    public void setFromYPortPositionStart(String fromYPortPositionStart) {
        this.fromYPortPositionStart = fromYPortPositionStart;
    }

    @XmlAttribute
    public String getFromXPortPositionEnd() {
        return fromXPortPositionEnd;
    }

    public void setFromXPortPositionEnd (String fromXPortPositionEnd) {
        this.fromXPortPositionEnd = fromXPortPositionEnd;
    }

    @XmlAttribute
    public String getFromYPortPositionEnd() {
        return fromYPortPositionEnd;
    }

    public void setFromYPortPositionEnd(String fromYPortPositionEnd) {
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
    public String getToXPortPositionStart() {
        return toXPortPositionStart;
    }

    public void setToXPortPositionStart(String toXPortPositionStart) {
        this.toXPortPositionStart = toXPortPositionStart;
    }

    @XmlAttribute
    public String getToYPortPositionStart() {
        return toYPortPositionStart;
    }

    public void setToYPortPositionStart(String toYPortPositionStart) {
        this.toYPortPositionStart = toYPortPositionStart;
    }

    @XmlAttribute
    public String getToXPortPositionEnd() {
        return toXPortPositionEnd;
    }

    public void setToXPortPositionEnd(String toXPortPositionEnd) {
        this.toXPortPositionEnd = toXPortPositionEnd;
    }

    @XmlAttribute
    public String getToYPortPositionEnd() {
        return toYPortPositionEnd;
    }

    public void setToYPortPositionEnd(String toYPortPositionEnd) {
        this.toYPortPositionEnd = toYPortPositionEnd;
    }


}
