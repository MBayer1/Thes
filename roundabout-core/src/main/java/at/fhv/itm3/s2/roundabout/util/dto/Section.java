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
    private Long minSizeOfPedestriansForTrafficLightTriggeredByJam;
    private String pedestrianCrossingIDRef, pedestrianCrossingComponentIDRef;
    private Integer pedestrianCrossingIDRefEnterHigh;
    private String pedestrianCrossingRefLinkedAtBegin;
    private Boolean flexiBorderAlongX;
    private Boolean useMassDynamic;

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
    public Double getLength() {
        return lengthX;
    }

    public void setLength(Double lengthX) {
        this.lengthX = lengthX;
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
    public Long getGreenPhaseDuration() { return greenPhaseDuration; }

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
    public Long getMinSizeOfPedestriansForTrafficLightTriggeredByJam() {
        return minSizeOfPedestriansForTrafficLightTriggeredByJam;
    }

    public void setMinSizeOfPedestriansForTrafficLightTriggeredByJam(Long minSizeOfPedestriansForTrafficLightTriggeredByJam) {
        this.minSizeOfPedestriansForTrafficLightTriggeredByJam = minSizeOfPedestriansForTrafficLightTriggeredByJam;
    }

    @XmlAttribute
    public String getPedestrianCrossingIDRef() {
        return pedestrianCrossingIDRef;
    }

    public void setPedestrianCrossingIDRef(String pedestrianCrossingIDRef) {
        this.pedestrianCrossingIDRef = pedestrianCrossingIDRef;
    }

    @XmlAttribute
    public Integer getPedestrianCrossingIDRefEnterHigh() {
        return pedestrianCrossingIDRefEnterHigh;
    }

    public void setPedestrianCrossingIDRefEnterHigh( Integer pedestrianCrossingIDRefEnterHigh) {
        this.pedestrianCrossingIDRefEnterHigh = pedestrianCrossingIDRefEnterHigh;
    }

    @XmlAttribute
    public Integer getPedestrianCrossingIDRefExitHigh() {
        return pedestrianCrossingIDRefEnterHigh;
    }

    public void setPedestrianCrossingIDRefExitHigh( Integer pedestrianCrossingIDRefEnterHigh) {
        this.pedestrianCrossingIDRefEnterHigh = pedestrianCrossingIDRefEnterHigh;
    }

    @XmlAttribute
    public String getPedestrianCrossingRefLinkedAtBegin() {
        return pedestrianCrossingRefLinkedAtBegin;
    }

    public void setPedestrianCrossingRefLinkedAtBegin( String pedestrianCrossingRefLinkedAtBegin ) {
        this.pedestrianCrossingRefLinkedAtBegin = pedestrianCrossingRefLinkedAtBegin;
    }

    @XmlAttribute
    public String getPedestrianCrossingComponentIDRef() {
        return pedestrianCrossingComponentIDRef;
    }

    public void setPedestrianCrossingComponentIDRef(String pedestrianCrossingComponentIDRef) {
        this.pedestrianCrossingComponentIDRef = pedestrianCrossingComponentIDRef;
    }

    @XmlAttribute
    public PedestrianConsumerType getPedestrianSectionType() {
        return pedestrianSectionType;
    }

    public void setPedestrianSectionType(PedestrianConsumerType pedestrianSectionType) {
        this.pedestrianSectionType = pedestrianSectionType;
    }

    @XmlAttribute
    public Boolean getFlexiBorderAlongX() {
        return flexiBorderAlongX;
    }

    public void setFlexiBorderAlongX(Boolean flexiBorderAlongX) {
        this.flexiBorderAlongX = flexiBorderAlongX;
    }



    @XmlAttribute
    public Boolean getUseMassDynamic() {
        return useMassDynamic;
    }

    public void setUseMassDynamic(Boolean useMassDynamic) {
        this.useMassDynamic = useMassDynamic;
    }

}