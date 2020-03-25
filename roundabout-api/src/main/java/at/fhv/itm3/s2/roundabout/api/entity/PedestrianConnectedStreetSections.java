package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;

public class PedestrianConnectedStreetSections {
    IConsumer fromStreetSection;
    PedestrianStreetSectionPort portOfFromStreetSection;

    IConsumer toStreetSection;
    PedestrianStreetSectionPort portOfToStreetSection;
    PedestrianAbstractSource toSource;


    public PedestrianConnectedStreetSections(IConsumer fromStreetSection,
                                             PedestrianStreetSectionPort portOfFromStreetSection,
                                             IConsumer toStreetSection,
                                             PedestrianStreetSectionPort portOfToStreetSection
                                            ){
        this.fromStreetSection = fromStreetSection;
        this.portOfFromStreetSection = portOfFromStreetSection;
        this.toStreetSection = toStreetSection;
        this.portOfToStreetSection = portOfToStreetSection;
        this.toSource = null;
    }

    public IConsumer getFromStreetSection() {
        return fromStreetSection;
    }

    public IConsumer getToStreetSection() {
        return toStreetSection;
    }

    public void setToSource( PedestrianAbstractSource toSource) {
        this.toSource = toSource;
    }

    public PedestrianAbstractSource getToSource () { return this.toSource;}

    public PedestrianStreetSectionPort getPortOfFromStreetSection() {
        return portOfFromStreetSection;
    }

    public PedestrianStreetSectionPort getPortOfToStreetSection() {
        return portOfToStreetSection;
    }
}
