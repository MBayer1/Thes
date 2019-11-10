package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import javax.xml.bind.annotation.XmlAttribute;

public class PedestrianRoute implements IDTO {
    private String fromComponentId,fromSourceId;

    private String toComponentId, toSinkId;

    private Double ratio;

    @XmlAttribute
    public Double getRatio() {
        return ratio;
    }

    public void setRatio(Double ratio) {
        this.ratio = ratio;
    }

    @XmlAttribute
    public String getFromComponentId() {
        return fromComponentId;
    }

    public void setFromComponentId(String fromComponentId) {
        this.fromComponentId = fromComponentId;
    }

    @XmlAttribute
    public String getFromSourceId() {
        return fromSourceId;
    }

    public void setFromSourceId(String fromSourceId) {
        this.fromSourceId = fromSourceId;
    }

    @XmlAttribute
    public String getToComponentId() {
        return toComponentId;
    }

    public void setToComponentId(String toComponentId) {
        this.toComponentId = toComponentId;
    }

    @XmlAttribute
    public String getToSinkId() {
        return toSinkId;
    }

    public void setToSinkId(String toSinkId) {
        this.toSinkId = toSinkId;
    }
}
