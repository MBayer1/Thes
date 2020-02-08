package at.fhv.itm3.s2.roundabout.api.entity;

import java.awt.*;

public class PedestrianStreetReferenceForVehicleStreet {

    private final PedestrianStreet pedestrianCrossing;
    private final int highOfEntry;
    private final boolean linkedAtBegin;

    public PedestrianStreetReferenceForVehicleStreet(PedestrianStreet pedestrianCrossing, int highOfEntry, boolean linkedAtBegin){
        this.pedestrianCrossing = pedestrianCrossing;
        this.highOfEntry = highOfEntry;
        this.linkedAtBegin = linkedAtBegin;
    }

    public int getHighOfEntry() {
        return highOfEntry;
    }

    public PedestrianStreet getPedestrianCrossing() {
        return pedestrianCrossing;
    }

    public boolean getLinkedAtBegin() {
        return linkedAtBegin;
    }
}
