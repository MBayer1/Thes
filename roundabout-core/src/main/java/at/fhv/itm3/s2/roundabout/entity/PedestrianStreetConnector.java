package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.entity.*;

import java.util.*;
import java.util.List;

public class PedestrianStreetConnector implements IPedestrianStreetConnector {

    private final String id;
    private final List<PedestrianConnectedStreetSections> sectionPairs;

    public PedestrianStreetConnector( String id) {
        this(id, new LinkedList());
    }

    public PedestrianStreetConnector( String id, List<PedestrianConnectedStreetSections> connectorList) {
        this.id = id;
        this.sectionPairs = connectorList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PedestrianConnectedStreetSections> getSectionPairs() {
        return sectionPairs;
    }

    public boolean doesContainTwoConnectedStreetSections( PedestrianStreet section1, PedestrianStreet section2) {
        if (sectionPairs != null || !sectionPairs.isEmpty()) {
            for (PedestrianConnectedStreetSections pair : sectionPairs) {
                if ((pair.getFromStreetSection().equals(section1) && pair.getToStreetSection().equals(section2)) ||
                        (pair.getFromStreetSection().equals(section2) && pair.getToStreetSection().equals(section1))) {
                    return true;
                }

            }
        }
        return false;
    }


    public PedestrianConnectedStreetSections GetConnectedStreetSectionInfo( PedestrianStreet section1, PedestrianStreet section2) throws IllegalStateException {
        if (sectionPairs != null || !sectionPairs.isEmpty()) {
            for (PedestrianConnectedStreetSections pair : sectionPairs) {
                if ((pair.getFromStreetSection().equals(section1) && pair.getToStreetSection().equals(section2)) ||
                        (pair.getFromStreetSection().equals(section2) && pair.getToStreetSection().equals(section1))) {
                    return  pair;
                }
            }
        }
        throw new IllegalArgumentException("Street section pair does not exist.");
    }

    public PedestrianConnectedStreetSections getConnectorBySection (IConsumer section) {
        for (PedestrianConnectedStreetSections pair : sectionPairs) {
            if(pair.getFromStreetSection().equals(section)){
                return pair;
            }

        }
        return null;
    }

    public void addConnector ( PedestrianConnectedStreetSections pedestrianConnectedStreetSections ) {
        sectionPairs.add(pedestrianConnectedStreetSections);
    }

}
