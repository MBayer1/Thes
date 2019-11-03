package at.fhv.itm3.s2.roundabout.api.entity;

import java.util.Collection;
import java.util.List;

public interface IPedestrianStreetConnector {

    /**
     * Returns connector id.
     */
    String getId();

    /**
     * Gets all Pairs of Street Sections for pedestrians and there individual Ports.
     *
     * @return The connected {@link PedestrianConnectedStreetSections}s as {@link Collection}.
     */
    List<PedestrianConnectedStreetSections> getSectionPairs();


}
