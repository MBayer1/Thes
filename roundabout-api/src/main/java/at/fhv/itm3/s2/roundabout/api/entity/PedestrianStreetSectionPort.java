package at.fhv.itm3.s2.roundabout.api.entity;

import java.awt.*;

public class PedestrianStreetSectionPort {
    // Information in cm
    Point beginOfStreetPort;
    Point endOfStreetPort;


    public PedestrianStreetSectionPort(Integer startX, Integer startY, Integer endX, Integer endY){
        this(new Point( startX, startY), new Point(endX, endY));
    }

    public PedestrianStreetSectionPort(Point beginOfStreetPort, Point endOfStreetPort){
        this.beginOfStreetPort = beginOfStreetPort;
        this.endOfStreetPort = endOfStreetPort;
    }

    public Point getBeginOfStreetPort() {
        return beginOfStreetPort;
    }

    public Point getEndOfStreetPort() {
        return endOfStreetPort;
    }
}
