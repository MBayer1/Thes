package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import javax.xml.bind.annotation.XmlAttribute;

public class Section implements IDTO {
    private String id;
    private Double lengthX;
    private Double lengthY;
    private Integer order;
    private Boolean isTrafficLightActive;
    private Long minGreenPhaseDuration;
    private Long greenPhaseDuration;
    private Long redPhaseDuration;
    private String pedestrianCrossingIDRef, pedestrianCrossingComponentIDRef; // TODO
    private Integer pedestrianCrossingIDRefEnterHigh;
    private Boolean pedestrianCrossingRefLinkedAtBegin;

    //pedestrian
    private PedestrianConsumerType pedestrianSectionType;
    private Integer pedestrianLength, pedestrianWidth;


    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute
    public Double getLengthX() {
        return lengthX;
    }

    public void setLengthX(Double lengthX) {
        this.lengthX = lengthX;
    }

    @XmlAttribute
    public Double getLengthY() {
        return lengthY;
    }

    public void setLengthY(Double lengthY) {
        this.lengthY = lengthY;
    }

    @XmlAttribute
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @XmlAttribute
    public Boolean getIsTrafficLightActive() { return isTrafficLightActive; }

    public void setIsTrafficLightActive(Boolean isTrafficLightActive) {
        this.isTrafficLightActive = isTrafficLightActive;
    }

    @XmlAttribute
    public  Long getGreenPhaseDuration() { return greenPhaseDuration; }

    private void setGreenPhaseDuration(Long greenPhaseDuration) {this.greenPhaseDuration = greenPhaseDuration; }

    @XmlAttribute
    public  Long getRedPhaseDuration() { return  redPhaseDuration; }

    public  void setRedPhaseDuration(Long redPhaseDuration) { this.redPhaseDuration = redPhaseDuration; }

    @XmlAttribute
    public Long getMinGreenPhaseDuration() {
        return minGreenPhaseDuration;
    }

    public void setMinGreenPhaseDuration(Long minGreenPhaseDuration) {
        this.minGreenPhaseDuration = minGreenPhaseDuration;
    }

    @XmlAttribute
    public String getPedestrianCrossingIDRef() {
        return pedestrianCrossingIDRef;
    }

    @XmlAttribute
    public Integer getPedestrianCrossingIDRefEnterHigh() {
        if (pedestrianCrossingIDRefEnterHigh != null) return pedestrianCrossingIDRefEnterHigh;
        return pedestrianCrossingIDRefEnterHigh;
    }

    @XmlAttribute
    public Boolean getPedestrianCrossingRefLinkedAtBegin() {
        return pedestrianCrossingRefLinkedAtBegin;
    }


    public void setPedestrianCrossingIDRef(String pedestrianCrossingIDRef) {
        this.pedestrianCrossingIDRef = pedestrianCrossingIDRef;
    }

    public String getPedestrianCrossingComponentIDRef() {
        return pedestrianCrossingIDRef;
    }

    public void setPedestrianCrossingComponentIDRef(String pedestrianCrossingComponentIDRef) {
        this.pedestrianCrossingComponentIDRef = pedestrianCrossingComponentIDRef;
    }


    // Pedestrian Part

    @XmlAttribute
    public PedestrianConsumerType getPedestrianSectionType() {
        return pedestrianSectionType;
    }

    public void setPedestrianSectionType(PedestrianConsumerType pedestrianSectionType) {
        this.pedestrianSectionType = pedestrianSectionType;
    }

    @XmlAttribute
    public Integer getPedestrianLength() {
        return pedestrianLength;
    }


    public void setPedestrianLength(Integer pedestrianLength) {
        this.pedestrianLength = pedestrianLength;
    }

    @XmlAttribute
    public Integer getPedestrianWidth() {
        return pedestrianWidth;
    }

    public void setPedestrianWidth(Integer pedestrianWidth) {
        this.pedestrianWidth = pedestrianWidth;
    }
}