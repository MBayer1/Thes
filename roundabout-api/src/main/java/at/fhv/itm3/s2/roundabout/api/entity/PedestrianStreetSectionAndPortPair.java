package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;

import java.awt.*;

public class PedestrianStreetSectionAndPortPair {
    // Information in cm
    private PedestrianStreetSectionPort enterPort;
    private PedestrianStreetSectionPort exitPort;
    private IConsumer streetSection;


    public PedestrianStreetSectionAndPortPair(IConsumer streetSection){
         this(streetSection, null, null);
    }

    public PedestrianStreetSectionAndPortPair(IConsumer streetSection, PedestrianStreetSectionPort enterPort){
        this(streetSection, enterPort, null);
    }

    public PedestrianStreetSectionAndPortPair(IConsumer streetSection, PedestrianStreetSectionPort enterPort, PedestrianStreetSectionPort exitPort){
        this.streetSection = streetSection;
        this.enterPort = enterPort;
        this.exitPort = exitPort;
    }

    public IConsumer getStreetSection() { return streetSection;}

    public void setEnterPort(PedestrianStreetSectionPort enterPort) {
        this.enterPort = enterPort;
    }

    public void setExitPort(PedestrianStreetSectionPort exitPort) {
        this.exitPort = exitPort;
    }

    public PedestrianStreetSectionPort getEnterPort() {
        return enterPort;
    }

    public PedestrianStreetSectionPort getExitPort() {
        return exitPort;
    }
}
