package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;

public class PedestrianStreetReferenceForVehicleStreet {

    private final PedestrianStreet pedestrianCrossing;
    private final Double highOfEntry;// from start point of entry port in cm = center of street
    private final Boolean linkedAtBegin; // closer to the origin 0
    private PedestrianPoint globalPositionOfStreetAndCrossingIntersectionInCM;

    public PedestrianStreetReferenceForVehicleStreet(PedestrianStreet pedestrianCrossing, double highOfEntry,
                                                     boolean linkedAtBegin){
        this.pedestrianCrossing = pedestrianCrossing;
        this.highOfEntry = highOfEntry;
        this.linkedAtBegin = linkedAtBegin;
        this.globalPositionOfStreetAndCrossingIntersectionInCM = new PedestrianPoint(0,0);
    }

    public PedestrianPoint getGlobalPositionOfStreetAndCrossingIntersectionInCM() {
        return globalPositionOfStreetAndCrossingIntersectionInCM;
    }

    public void setGlobalPositionOfStreetAndCrossingIntersectionInCM(PedestrianPoint globalPositionOfStreetAndCrossingIntersection) {
        this.globalPositionOfStreetAndCrossingIntersectionInCM = globalPositionOfStreetAndCrossingIntersection;
    }

    public double getLocalHighOfEntry() {
        return highOfEntry;
    }

    public PedestrianStreet getPedestrianCrossing() {
        return pedestrianCrossing;
    }

    public boolean getLinkedAtBegin() {
        return linkedAtBegin;
    }

    public double getLengthForVehicleToPass() {
        for ( PedestrianConnectedStreetSections connectedStreetSections : pedestrianCrossing.getNextStreetConnector() ) {
            // just check whether the ports are along the x or y axis. this is the side the car is not crossing.
            if ( carDrivesAlongYAxis(connectedStreetSections) ){
                return pedestrianCrossing.getLengthY(); // port along y axis. and car has to traverse this length -> it enters along x axis
            }
            return pedestrianCrossing.getLengthX();
        }
        return  0.0;
    }

    public boolean  carDrivesAlongYAxis(){
        for ( PedestrianConnectedStreetSections connectedStreetSections : pedestrianCrossing.getNextStreetConnector() ) {
            return almostEqual(connectedStreetSections.getPortOfFromStreetSection().getLocalBeginOfStreetPort().getX(),
                    connectedStreetSections.getPortOfFromStreetSection().getLocalEndOfStreetPort().getX());
        }
        return false;
    }

    public boolean  carDrivesAlongYAxis(PedestrianConnectedStreetSections connectedStreetSections ){
        return almostEqual ( connectedStreetSections.getPortOfFromStreetSection().getLocalBeginOfStreetPort().getX(),
                connectedStreetSections.getPortOfFromStreetSection().getLocalEndOfStreetPort().getX());
    }

    private boolean almostEqual (double dVal1, double dVal2) {
        return (Math.round(Math.abs(dVal1-dVal2)) <  10e-8);
    }

}