package at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreetSectionPort;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConnectedStreetSections;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;

public class RepulsiveForceAgainstObstacles {

    final private Double R = 20.0; // cm
    final private Double U0AlphaBeta = 1000.0; // (cm/s)^2
    SupportiveCalculations calculations = new SupportiveCalculations();
    RoundaboutSimulationModel model = null;

    public Vector2d getRepulsiveForceAgainstAllObstacles( RoundaboutSimulationModel model,
                                                          Pedestrian pedestrian){
        Vector2d sumForce = new Vector2d(0,0);
        this.model = model;

        // Get all walls of the current sections.
        forceAgainstWalls( pedestrian, sumForce );

        // Get all obstacles (non walls)
        // TODO for later extensions

        if(Double.isNaN(sumForce.getX()) || Double.isNaN(sumForce.getY()) ){
            throw new IllegalStateException("Vector calculation  error: ForceAgainstObstacle.");
        }

        return sumForce;
    }

    void forceAgainstWalls(Pedestrian pedestrian, Vector2d sumForce ){

        IConsumer section = pedestrian.getCurrentSection().getStreetSection();
        if ( ! (section instanceof PedestrianStreetSection) ) {
            throw new IllegalArgumentException("Street section is not an instance of PedestrianStreetSection");
        }
        PedestrianStreetSection currentSection = (PedestrianStreetSection) section;

        // get closed point to all 4 walls.
         /*                            2 (xPerson/ yMax)
                                 ____________________
                                |                    |
                                |                    |
              1 (xMin/ yPerson) |          P         |  3 (xMax/ yPerson)
                                |                    |
                                |                    |
                                |____________________|
                                       4 (xPerson/ yMin)
           */
        double pedestrianX = pedestrian.getCurrentGlobalPosition().getX();
        double pedestrianY = pedestrian.getCurrentGlobalPosition().getY();
        double sectionOriginX = currentSection.getGlobalCoordinateOfSectionOrigin().getX();
        double sectionOriginY = currentSection.getGlobalCoordinateOfSectionOrigin().getY();

        // person positions - border intersections = global coordinates
        PedestrianPoint wallIntersection1 = new PedestrianPoint(sectionOriginX, pedestrianY);
        PedestrianPoint wallIntersection2 = new PedestrianPoint(pedestrianX,  sectionOriginY + currentSection.getLengthY());
        PedestrianPoint wallIntersection3 = new PedestrianPoint(sectionOriginX + currentSection.getLengthX(), pedestrianY);
        PedestrianPoint wallIntersection4 = new PedestrianPoint(pedestrianX,  sectionOriginY);

        // when pedestrian is outside the street section set corner as intersection
        if ( calculations.val1LowerOrAlmostEqual(pedestrianX, sectionOriginX )) {
            wallIntersection2.setX(sectionOriginX);
            wallIntersection4.setX(sectionOriginX);
        } else if (calculations.val1BiggerOrAlmostEqual(pedestrianX, sectionOriginX + currentSection.getLengthX())) {
            wallIntersection2.setX(sectionOriginX + currentSection.getLengthX());
            wallIntersection4.setX(sectionOriginX + currentSection.getLengthX());
        }

        if ( calculations.val1LowerOrAlmostEqual(pedestrianY, sectionOriginY )) {
            wallIntersection1.setY(sectionOriginY);
            wallIntersection3.setY(sectionOriginY);
        } else if (calculations.val1BiggerOrAlmostEqual(pedestrianY, sectionOriginY + currentSection.getLengthY())) {
            wallIntersection1.setY(sectionOriginY + currentSection.getLengthY());
            wallIntersection3.setY(sectionOriginY + currentSection.getLengthY());
        }

        // except it is within an port gab, then it will take enter or exit port depending which is closer.
        for ( PedestrianConnectedStreetSections connected : ((PedestrianStreetSection) section).getNextStreetConnector()) {
            if (connected.getFromStreetSection().equals(currentSection)) {
                PedestrianStreetSectionPort localPort = connected.getPortOfFromStreetSection();
                PedestrianStreetSectionPort globalPort = new PedestrianStreetSectionPort(
                        localPort.getLocalBeginOfStreetPort().getX() + sectionOriginX,
                        localPort.getLocalBeginOfStreetPort().getY() + sectionOriginY,
                        localPort.getLocalEndOfStreetPort().getX() + sectionOriginX,
                        localPort.getLocalEndOfStreetPort().getY() + sectionOriginY);

                if ( calculations.checkWallIntersectionWithinPort( globalPort, wallIntersection1 ) )
                    calculations.shiftIntersection(globalPort, wallIntersection1);
                if ( calculations.checkWallIntersectionWithinPort( globalPort, wallIntersection2) )
                    calculations.shiftIntersection( globalPort, wallIntersection2);

                if ( calculations.checkWallIntersectionWithinPort( globalPort, wallIntersection3) )
                    calculations.shiftIntersection( globalPort, wallIntersection3);

                if ( calculations.checkWallIntersectionWithinPort( globalPort, wallIntersection4) )
                    calculations.shiftIntersection( globalPort, wallIntersection4);

            }
        }

        for ( PedestrianConnectedStreetSections connected : ((PedestrianStreetSection) section).getPreviousStreetConnector()) {
            if (connected.getToStreetSection() == null) continue;
            if (connected.getToStreetSection().equals(currentSection)) {
                if (connected.getFromStreetSection().equals(currentSection)) {
                    PedestrianStreetSectionPort port = connected.getPortOfFromStreetSection();

                    if ( !calculations.checkWallIntersectionWithinPort( port, wallIntersection1 ) )
                        calculations.shiftIntersection( port, wallIntersection1, pedestrian.getMinGapForPedestrian());
                    if ( !calculations.checkWallIntersectionWithinPort( port, wallIntersection2 ) )
                        calculations.shiftIntersection( port, wallIntersection2, pedestrian.getMinGapForPedestrian());
                    if ( !calculations.checkWallIntersectionWithinPort( port, wallIntersection3 ) )
                        calculations.shiftIntersection( port, wallIntersection3, pedestrian.getMinGapForPedestrian());
                    if ( !calculations.checkWallIntersectionWithinPort( port, wallIntersection4 ) )
                        calculations.shiftIntersection( port, wallIntersection4, pedestrian.getMinGapForPedestrian());
                }
            }
        }

        addForce( pedestrian, sumForce, wallIntersection1, wallIntersection2, wallIntersection3, wallIntersection4);
    }

    boolean checkAttractingForce( PedestrianStreetSection section, Pedestrian pedestrian) {
        if ( section.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING) && !section.checkPedestrianIsWithinSection(pedestrian) ) return true;
        return false;
    }

    void addForce ( Pedestrian pedestrian, Vector2d sumForce,
                    PedestrianPoint wallIntersection1, PedestrianPoint wallIntersection2,
                    PedestrianPoint wallIntersection3, PedestrianPoint wallIntersection4) {

        IConsumer section = pedestrian.getCurrentSection().getStreetSection();
        if ( ! (section instanceof PedestrianStreetSection) ) {
            throw new IllegalArgumentException("Street section is not an instance of PedestrianStreetSection");
        }
        PedestrianStreetSection currentSection = (PedestrianStreetSection) section;

        // special case of crossings. walls can be attracted or repulsive force
        Vector2d wallIntersection1force = getRepulsiveForceAgainstObstacle( pedestrian, new PedestrianPoint( wallIntersection1.getX(), wallIntersection1.getY()));
        Vector2d wallIntersection2force = getRepulsiveForceAgainstObstacle( pedestrian, new PedestrianPoint( wallIntersection2.getX(), wallIntersection2.getY()));
        Vector2d wallIntersection3force = getRepulsiveForceAgainstObstacle( pedestrian, new PedestrianPoint( wallIntersection3.getX(), wallIntersection3.getY()));
        Vector2d wallIntersection4force = getRepulsiveForceAgainstObstacle( pedestrian, new PedestrianPoint( wallIntersection4.getX(), wallIntersection4.getY()));

        sumForce.add(wallIntersection1force);
        sumForce.add(wallIntersection2force);
        sumForce.add(wallIntersection3force);
        sumForce.add(wallIntersection4force);

        if ( checkAttractingForce( currentSection, pedestrian ) ) {// attracted force
            sumForce.scale(-1);
        }
    }

    Vector2d getRepulsiveForceAgainstObstacle(   Pedestrian pedestrian,
                                                 PedestrianPoint obstaclePosition) {
        Double weightingFactor;
        PedestrianPoint dest = pedestrian.getNextSubGoal();
        Vector2d destination = new Vector2d( dest.getX(), dest.getY());
        Vector2d force = getRepulsiveForceAgainstObstacleCalculation( pedestrian, obstaclePosition);

        if ( model.equals(null)) {
            throw new IllegalArgumentException("model must not be null");
        }

        // Check Field of View --> 170°
        if (calculations.val1BiggerOrAlmostEqual(destination.dot(force),  //A ⋅ B = ||A|| * ||B|| * cos θ
                force.length() * Math.cos(Math.toRadians(model.pedestrianFieldOfViewDegree / 2)))) {
            // in field of view
            weightingFactor = 1.0;
        } else {
            weightingFactor = model.getPedestrianFieldOfViewWeakeningFactor;
        }
        force.scale(weightingFactor);
        return force;
    }

    Vector2d getRepulsiveForceAgainstObstacleCalculation(    Pedestrian pedestrian,
                                                             PedestrianPoint obstaclePosition) {
        if( !checkPedestrianInRange( pedestrian, obstaclePosition) ){
            return new Vector2d(0,0 );
        }

        // Distance vector
        Vector2d vectorBetweenPedestrianAndObstacle = new Vector2d(pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());
        vectorBetweenPedestrianAndObstacle.sub(new Vector2d(obstaclePosition.getX(), obstaclePosition.getY()));
        Double distanceBetweenPedestrianAndObstacle = vectorBetweenPedestrianAndObstacle.length();

        // Repulsive force
        Double expo = (-1 * (distanceBetweenPedestrianAndObstacle/ R));
        expo = Math.exp(expo);
        Vector2d force = calculations.getUnitVector(vectorBetweenPedestrianAndObstacle);

        force.scale(U0AlphaBeta * expo);
        //force.negate(); not needed as we  use vec from pos sub goal
        return force;
    }

    boolean checkPedestrianInRange( Pedestrian pedestrian, PedestrianPoint intersectionPos){
        if ( calculations.val1LowerOrAlmostEqual( calculations.getDistanceByCoordinates(   pedestrian.getCurrentGlobalPosition().getX(),
                pedestrian.getCurrentGlobalPosition().getY(),
                intersectionPos.getX(),
                intersectionPos.getY()) ,
                model.pedestrianFieldOfViewRadius)) {
            return true;
        }
        return false;
    }
}
