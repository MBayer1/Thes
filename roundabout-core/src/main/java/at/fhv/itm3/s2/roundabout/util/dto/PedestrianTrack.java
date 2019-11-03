package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import javax.xml.bind.annotation.XmlAttribute;

public class PedestrianTrack implements IDTO {

    private String sectionId1, componentId1;
    private PedestrianConsumerType sectionType1;
    private Integer xPortPositionStart1, yPortPositionStart1,
                    xPortPositionEnd1, yPortPositionEnd1;

    private String sectionId2, componentId2;
    private PedestrianConsumerType sectionType2;
    private Integer xPortPositionStart2, yPortPositionStart2,
                    xPortPositionEnd2, yPortPositionEnd2;;


    @XmlAttribute
    public String getSectionId1(){
        return sectionId1;
    }

    public void setSectionId1(String sectionId1) {
        this.sectionId1 = sectionId1;
    }

    @XmlAttribute
    public String getComponentId1() {
        return componentId1;
    }

    public void setComponentId1(String componentId1) {
        this.componentId1 = componentId1;
    }

    @XmlAttribute
    public PedestrianConsumerType getSectionType1() {
        return sectionType1;
    }

    public void setSectionType1(PedestrianConsumerType sectionType1) {
        this.sectionType1 = sectionType1;
    }

    @XmlAttribute
    public Integer getXPortPositionStart1() {
        return xPortPositionStart1;
    }

    public void setXPortPositionStart1(Integer xPortPositionStart1) {
        this.xPortPositionStart1 = xPortPositionStart1;
    }

    @XmlAttribute
    public Integer getYPortPositionStart1() {
        return yPortPositionStart1;
    }

    public void setYPortPositionStart1(Integer yPortPositionStart1) {
        this.yPortPositionStart1 = yPortPositionStart1;
    }

    @XmlAttribute
    public Integer getXPortPositionEnd1() {
        return xPortPositionEnd1;
    }

    public void setXPortPositionEnd1(Integer xPortPositionEnd1) {
        this.xPortPositionEnd1 = xPortPositionEnd1;
    }

    @XmlAttribute
    public Integer getYPortPositionEnd1() {
        return yPortPositionStart1;
    }

    public void setYPortPositionEnd1(Integer yPortPositionEnd1) {
        this.yPortPositionEnd1 = yPortPositionEnd1;
    }



    @XmlAttribute
    public String getSectionId2(){
        return sectionId2;
    }

    public void setSectionId2(String sectionId2) {
        this.sectionId2 = sectionId2;
    }

    @XmlAttribute
    public String getComponentId2() {
        return componentId2;
    }

    public void setComponentId2(String componentId2) {
        this.componentId2 = componentId2;
    }

    @XmlAttribute
    public PedestrianConsumerType getSectionType2() {
        return sectionType2;
    }

    public void setSectionType2(PedestrianConsumerType sectionType2) {
        this.sectionType2 = sectionType2;
    }

    @XmlAttribute
    public Integer getXPortPositionStart2() {
        return xPortPositionStart2;
    }

    public void setXPortPositionStart2(Integer xPortPositionStart2) {
        this.xPortPositionStart2 = xPortPositionStart2;
    }

    @XmlAttribute
    public Integer getYPortPositionStart2() {
        return yPortPositionStart2;
    }

    public void setYPortPositionStart2(Integer yPortPositionStart2) {
        this.yPortPositionStart2 = yPortPositionStart2;
    }

    @XmlAttribute
    public Integer getXPortPositionEnd2() {
        return xPortPositionEnd2;
    }

    public void setXPortPositionEnd2(Integer xPortPositionEnd2) {
        this.xPortPositionEnd2 = xPortPositionEnd2;
    }

    @XmlAttribute
    public Integer getYPortPositionEnd2() {
        return yPortPositionStart2;
    }

    public void setYPortPositionEnd2(Integer yPortPositionEnd2) {
        this.yPortPositionEnd2 = yPortPositionEnd2;
    }


}
