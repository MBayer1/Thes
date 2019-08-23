package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.List;

public class Connector implements IDTO {
    private String id;
    private List<Track> trackList;
    private Double
            xPositionStart, yPositionStart,
            xPositionEnd, yPositionEnd;

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Track> getTrack() {
        return trackList;
    }

    public void setTrack(List<Track> trackList) {
        this.trackList = trackList;
    }

    // Neeeded for pedestrians
    @XmlAttribute
    public Double getXPositionStart() {
        return xPositionStart;
    }

    public void setXPositonStart(Double xPositionStart) {
        this.xPositionStart = xPositionStart;
    }

    @XmlAttribute
    public Double getYPositonStart() {
        return yPositionStart;
    }

    public void setYPositonStart(Double yPositionStart) {
        this.yPositionStart = yPositionStart;
    }

    @XmlAttribute
    public Double getXPositionEnd() {
        return this.xPositionEnd;
    }

    public void setXPositonEnd(Double xPositionEnd) {
        this.xPositionEnd = xPositionEnd;
    }

    @XmlAttribute
    public Double getYPositonEnd() {
        return yPositionEnd;
    }

    public void setYPositonEnd(Double yPositionEnd) {
        this.yPositionEnd = yPositionEnd;
    }
}
