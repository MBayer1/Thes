package at.fhv.itm3.s2.roundabout.api.entity;

public class PedestrianStreetReferenceForVehicleStreet {

    private final PedestrianStreet pedestrianCrossing;
    private final Integer highOfEntry;
    private final Boolean linkedAtBegin;

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

    public double getLenghtForVehicleToPass () {
        for ( PedestrianConnectedStreetSections connectedStreetSections : pedestrianCrossing.getNextStreetConnector() ) {
            // just check whether the ports are along the x or y axis. this is the side the car is not crossing.
            if ( almostEqual ( connectedStreetSections.getPortOfFromStreetSection().getBeginOfStreetPort().getX(),
                    connectedStreetSections.getPortOfFromStreetSection().getEndOfStreetPort().getX())){
                return pedestrianCrossing.getLengthX();
            }
            return pedestrianCrossing.getLengthY();
        }
        return  0.0;
    }

    private boolean almostEqual (double dVal1, double dVal2) {
        return (Math.round(Math.abs(dVal1-dVal2)) <  10e-8);
    }

}