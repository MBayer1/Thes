package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.entity.*;

import java.util.*;
import java.util.List;

public class PedestrianStreetConnector implements IPedestrianStreetConnector {

    private final String id;
    private final List<PedestrianConnectedStreetSections> sectionPairs = new LinkedList();


    public PedestrianStreetConnector(List<PedestrianConnectedStreetSections> sectionPairs){
        this(UUID.randomUUID().toString(), sectionPairs);
    }


    public PedestrianStreetConnector(String id, List<PedestrianConnectedStreetSections> sectionPairs) {
        this.id = id;
        this.sectionPairs.addAll(sectionPairs);
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

    public boolean DoesContainTwoConnectedStreetSections( PedestrianStreet section1, PedestrianStreet section2) {
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

}
