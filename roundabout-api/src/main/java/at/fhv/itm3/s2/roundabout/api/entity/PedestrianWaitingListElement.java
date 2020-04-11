package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import sun.swing.SwingUtilities2;

import java.awt.*;

public class PedestrianWaitingListElement {
    IPedestrian pedestrian;
    PedestrianPoint globalEnterPoint;
    PedestrianStreet sectionToMoveTo;

    public PedestrianWaitingListElement(IPedestrian pedestrian, PedestrianPoint globalEnterPoint, PedestrianStreet sectionToMoveTo){
        this.pedestrian = pedestrian;
        this.globalEnterPoint = globalEnterPoint;
        this.sectionToMoveTo = sectionToMoveTo;
    }

    public IPedestrian getPedestrian() {
        return pedestrian;
    }

    public PedestrianStreet getSectionToMoveTo() {
        return sectionToMoveTo;
    }

    public PedestrianPoint getGlobalEnterPoint() {
        return globalEnterPoint;
    }
}
