package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;

import java.awt.*;

public class ConnectorInterfaceGap {

    private final Double xPositionStart, yPositionStart,
            xPositionEnd, yPositionEnd;

    public ConnectorInterfaceGap(PedestrianPoint positionStart, PedestrianPoint positionEnd){
        this(positionStart.getX(), positionStart.getY(), positionEnd.getX(),positionEnd.getY());
    }

    public ConnectorInterfaceGap(Double xPositionStart, Double yPositionStart,
                                 Double xPositionEnd, Double yPositionEnd){
        this.xPositionStart = xPositionStart;
        this.yPositionStart = yPositionStart;
        this.xPositionEnd = xPositionEnd;
        this.yPositionEnd = yPositionEnd;
    }

    public Double getxPositionEnd() {
        return xPositionEnd;
    }

    public Double getxPositionStart() {
        return xPositionStart;
    }

    public Double getyPositionEnd() {
        return yPositionEnd;
    }

    public Double getyPositionStart() {
        return yPositionStart;
    }
}
