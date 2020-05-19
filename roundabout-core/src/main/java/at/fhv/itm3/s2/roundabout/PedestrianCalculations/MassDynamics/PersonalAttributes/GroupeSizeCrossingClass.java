package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.Category;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianBehaviour;
import at.fhv.itm3.s2.roundabout.entity.PedestrianSink;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GroupeSizeCrossingClass {
    List<Category> categoryList = new LinkedList<>();

    SupportiveCalculations calculations = new SupportiveCalculations();
    LinkedList<PedestrianStreetSection> listOfCheckedStreets = new LinkedList<>();
    Integer numberOfPedestrian;

    public GroupeSizeCrossingClass(){
        // 0 is not an option as  the pedestrian it self will always be considered
        categoryList.add(new Category("1",0, 1,0.5));
        categoryList.add(new Category("2",categoryList.get(categoryList.size()-1).getUpperLimit(),3,0.64));
        categoryList.add(new Category("3",categoryList.get(categoryList.size()-1).getUpperLimit(),4,0.74));
        categoryList.add(new Category("4",categoryList.get(categoryList.size()-1).getUpperLimit(),5,0.78));
        categoryList.add(new Category(">4",categoryList.get(categoryList.size()-1).getUpperLimit(), Double.MAX_VALUE,0.84));

    }
    public double getProbability (String type){
        for ( int i = 0; i < categoryList.size(); ++i) {
            if ( categoryList.get(i).getTypeKey().equals(type))
                return categoryList.get(i).getProbabilityForCrossingIllegal();
        }
        throw new IllegalArgumentException("Category does not exist for MassDynamic.");
    }

    public double getProbability (Pedestrian pedestrian){
        if(!(pedestrian.getPedestrianBehaviour() instanceof PedestrianBehaviour)){
            throw new IllegalArgumentException("PedestrianBehaviour not instance of PedestrianBehaviour .");
        }

        // check all of  those that are currently on the crossing or on one of  its waiting  areas
        if (!(pedestrian.getNextSection().getStreetSection() instanceof PedestrianStreetSection)) {
            throw new IllegalStateException("Section not instance of PedestrianStreetSection");
        }
        PedestrianStreetSection crossing = (PedestrianStreetSection) pedestrian.getNextSection().getStreetSection();
        this.numberOfPedestrian = crossing.getPedestrianQueue().size();
        this.listOfCheckedStreets.clear();
        this.listOfCheckedStreets.add(crossing);


        // run through all previous and following connected street sections up to 8m distance
        getAllPedestrianFromPreviousStreets(  crossing, crossing);
        getAllPedestrianFromFollowingStreets( crossing, crossing);

        return getProbability(getTypeByDetermineValue(numberOfPedestrian));
    }

    public String getTypeByDetermineValue (double valueToDetermineClass){
        for ( int i = 0; i < categoryList.size(); ++i) {
            if ( valueToDetermineClass > categoryList.get(i).getLowerLimit() && valueToDetermineClass <= categoryList.get(i).getUpperLimit()){
                return categoryList.get(i).getTypeKey();
            }
        }
        throw new IllegalArgumentException("Category does not exist for MassDynamic.");
    }


    boolean getAllPedestrianFromStreet(PedestrianStreetSection currentStreetSection,
                                       PedestrianStreetSection crossingRef ){

        if(currentStreetSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_SINK)) return false;

        boolean noPedestrian = true;
        boolean pedestrianOutOfRange = false;
        for(IPedestrian pedestrian: currentStreetSection.getPedestrianQueue()){
            if( !(pedestrian instanceof Pedestrian) ){
                throw new IllegalArgumentException("Pedestrian is not an instance of Pedestrian");
            }
            noPedestrian = false;
            // calculate forces
            if( checkForCrossingInRemainingRouteAndShortestDistance((Pedestrian) pedestrian, crossingRef)){
                ++numberOfPedestrian;
            } else {
                pedestrianOutOfRange = true; // no need to  check next sections
            }
        }
        listOfCheckedStreets.add( currentStreetSection );

        if ( noPedestrian || ! pedestrianOutOfRange ) {
            // no Pedestrian to estimate distance or all existing pedestrian where in range
            return true;
        }
        return false;
    }


    PedestrianStreetSectionAndPortPair getCurrentSectionData(Pedestrian pedestrian, PedestrianStreetSection currentSection) {
        for(PedestrianStreetSectionAndPortPair sectionAndPortPair : pedestrian.getRoute().getRoute()){
            PedestrianStreetSection section = (PedestrianStreetSection) sectionAndPortPair.getStreetSection();
            if(section.equals(currentSection)){
                return sectionAndPortPair;
            }
        }
        throw new IllegalStateException("Section not part of Route.");
    }

    IConsumer getNextSection(Pedestrian pedestrian, PedestrianStreetSection currentSection) {
        IConsumer section = null;
        boolean stopLooping = false;

        for(PedestrianStreetSectionAndPortPair sectionAndPortPair : pedestrian.getRoute().getRoute()){
            section = sectionAndPortPair.getStreetSection();

            if (stopLooping) return section;
            if(section.equals(currentSection)){
                stopLooping = true;
            }
        }
        throw new IllegalStateException("Section not part of Route.");
    }

    boolean checkForCrossingInRemainingRouteAndShortestDistance(Pedestrian pedestrian, PedestrianStreetSection crossingRef) {
        double distance = pedestrian.getRemainingDistanceToCurrentNextSubsoil();

        if (pedestrian.getNextSection() == null) return false;

        if( !(( pedestrian.getNextSection().getStreetSection() instanceof PedestrianStreetSection ||
                pedestrian.getNextSection().getStreetSection() instanceof PedestrianSink)) &&
               (pedestrian.getCurrentSection().getStreetSection()instanceof PedestrianStreetSection ||
                pedestrian.getCurrentSection().getStreetSection()instanceof PedestrianSink)) {
            throw new IllegalArgumentException("Section is not an instance of PedestrianStreetSection");
        }
        IConsumer currentSection = pedestrian.getCurrentSection().getStreetSection();
        IConsumer nextSection = getNextSection(pedestrian, (PedestrianStreetSection) currentSection);
        PedestrianStreetSectionAndPortPair currentData = getCurrentSectionData(pedestrian, (PedestrianStreetSection) currentSection);

        while ( nextSection instanceof PedestrianStreetSection){
            if(nextSection.equals(crossingRef)) {
                return calculations.val1LowerOrAlmostEqual(distance, pedestrian.getMaxDistanceForWaitingArea());
            }
            distance += getWalkingLengthAcrossSection(currentData);
            currentSection = nextSection;
            nextSection = getNextSection(pedestrian, (PedestrianStreetSection) currentSection);
            currentData = getCurrentSectionData(pedestrian, (PedestrianStreetSection) currentSection);
        }
        return  false;
    }

    double getWalkingLengthAcrossSection(PedestrianStreetSectionAndPortPair currentData) {
        // getPort between Section
        PedestrianPoint beginToNext = currentData.getExitPort().getLocalBeginOfStreetPort();
        PedestrianPoint endToNext = currentData.getExitPort().getLocalEndOfStreetPort();
        PedestrianPoint beginToPrev = currentData.getEnterPort().getLocalBeginOfStreetPort();
        PedestrianPoint endToPrev = currentData.getEnterPort().getLocalEndOfStreetPort();

        Vector2d beginToNextVec = new Vector2d(beginToNext.getX(), beginToNext.getY());
        Vector2d endToNextVec = new Vector2d(endToNext.getX(), endToNext.getY());
        Vector2d beginToPrevVec = new Vector2d(beginToPrev.getX(), beginToPrev.getY());
        Vector2d endToPrevVec = new Vector2d(endToPrev.getX(),  endToPrev.getY());

        Vector2d midPosNext = new Vector2d(beginToNextVec);
        midPosNext.sub(endToNextVec);
        midPosNext.scale(0.5);
        endToNextVec.add(midPosNext);
        midPosNext = endToNextVec;

        Vector2d midPosPrev = new Vector2d(beginToPrevVec);
        midPosPrev.sub(endToPrevVec);
        midPosPrev.scale(0.5);
        endToPrevVec.add(midPosPrev);
        midPosPrev = endToPrevVec;

        midPosPrev.sub(midPosNext);
        return midPosPrev.length();
    }

    public void getAllPedestrianFromPreviousStreets(PedestrianStreetSection section,
                                                    PedestrianStreetSection crossingRef) {
        List<PedestrianStreetSection> listOfStreetSectionsInRange = new ArrayList<>();
        listOfStreetSectionsInRange.add(section);

        while( !listOfStreetSectionsInRange.isEmpty() ){
            PedestrianStreetSection currentStreetSection = listOfStreetSectionsInRange.remove(listOfStreetSectionsInRange.size()-1);
            List<PedestrianConnectedStreetSections> previousConnector = currentStreetSection.getPreviousStreetConnector();

            for( PedestrianConnectedStreetSections previousStreetSectionPair : previousConnector ) {
                if( previousStreetSectionPair.getFromStreetSection().equals(currentStreetSection) ) {
                    IConsumer previousSection = previousStreetSectionPair.getToStreetSection(); // from is always current section

                    if (previousSection == null && previousStreetSectionPair.getToSource() != null) continue; // is a source
                    if( !(previousSection instanceof PedestrianStreetSection) ){
                        throw new IllegalArgumentException("Section is not an instance of PedestrianStreetSection");
                    }

                    if ( listOfCheckedStreets.contains(previousSection) ) continue;

                    if (getAllPedestrianFromStreet((PedestrianStreetSection) previousSection,  crossingRef)){
                        listOfStreetSectionsInRange.add((PedestrianStreetSection)previousSection);
                    }
                }
            }
        }
    }

    void getAllPedestrianFromFollowingStreets(PedestrianStreetSection section,
                                              PedestrianStreetSection crossingRef) {
        List<PedestrianStreetSection> listOfStreetSectionsInRange = new ArrayList<>();
        listOfStreetSectionsInRange.add(section);

        while( !listOfStreetSectionsInRange.isEmpty() ){
            PedestrianStreetSection currentStreetSection = listOfStreetSectionsInRange.remove(listOfStreetSectionsInRange.size()-1);
            List<PedestrianConnectedStreetSections> previousConnector = currentStreetSection.getPreviousStreetConnector();

            for( PedestrianConnectedStreetSections previousStreetSectionPair : previousConnector ) {
                if( previousStreetSectionPair.getFromStreetSection().equals(currentStreetSection) ) {
                    IConsumer previousSection = previousStreetSectionPair.getToStreetSection(); // from is always current section

                    if (previousSection == null && previousStreetSectionPair.getToSource() != null) continue; // is a source
                    if( !(previousSection instanceof PedestrianStreetSection) ){
                        throw new IllegalArgumentException("Section is not an instance of PedestrianStreetSection");
                    }

                    if ( listOfCheckedStreets.contains(previousSection) ) continue;

                    if (getAllPedestrianFromStreet((PedestrianStreetSection) previousSection,  crossingRef)){
                        listOfStreetSectionsInRange.add((PedestrianStreetSection)previousSection);
                    }
                }
            }
        }
    }
}
