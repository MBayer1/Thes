package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;

public class PedestrianConnectedStreetSections {
    IConsumer fromStreetSection;
    PedestrianStreetSectionPort portOfFromStreetSection;

    IConsumer toStreetSection;
    PedestrianStreetSectionPort portOfToStreetSection;


    public PedestrianConnectedStreetSections(IConsumer fromStreetSection,
                                             PedestrianStreetSectionPort portOfFromStreetSection,
                                             IConsumer toStreetSection,
                                             PedestrianStreetSectionPort portOfToStreetSection
                                            ){
        this.fromStreetSection = fromStreetSection;
        this.portOfFromStreetSection = portOfFromStreetSection;
        this.toStreetSection = toStreetSection;
        this.portOfToStreetSection = portOfToStreetSection;
    };

    public IConsumer getFromStreetSection() {
        return fromStreetSection;
    }

    public IConsumer getToStreetSection() {
        return toStreetSection;
    }

    public PedestrianStreetSectionPort getPortOfFromStreetSection() {
        return portOfFromStreetSection;
    }

    public PedestrianStreetSectionPort getPortOfToStreetSection() {
        return portOfToStreetSection;
    }
}
