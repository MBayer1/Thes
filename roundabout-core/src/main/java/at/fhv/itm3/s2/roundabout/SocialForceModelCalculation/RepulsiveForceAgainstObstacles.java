package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConnectedStreetSections;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Point2D;

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

        return sumForce;
    }

    void forceAgainstWalls(Pedestrian pedestrian, Vector2d sumForce ){

        IConsumer section = pedestrian.getCurrentSection().getStreetSection();
        if ( ! (section instanceof PedestrianStreetSection) ) {
            throw new IllegalArgumentException("Street section is not an instance of PedestrianStreetSection");
        }
        PedestrianStreetSection currentSection = (PedestrianStreetSection) section;

        // get closed point to all 4 walls.
         /*                             (xPerson/ yMax)
                                 ____________________
                                |                    |
                                |                    |
                (xMin/ yPerson) |          P         |(xMax/ yPerson)
                                |                    |
                                |                    |
                                |____________________|
                                        (xPerson/ yMin)
           */
        Integer pedestrianX = (int) pedestrian.getCurrentGlobalPosition().getX();
        Integer pedestrianY = (int) pedestrian.getCurrentGlobalPosition().getY();
        Integer sectionCenterX = (int) currentSection.getGlobalCoordinateOfSectionOrigin().getX();
        Integer sectionCenterY = (int) currentSection.getGlobalCoordinateOfSectionOrigin().getY();

        // person positions - border intersections
        Point wallIntersection1 = new Point(sectionCenterX, pedestrianY);
        Point wallIntersection2 = new Point(pedestrianX,  sectionCenterY + (int) currentSection.getLengthY());
        Point wallIntersection3 = new Point(sectionCenterX + (int) currentSection.getLengthX(), pedestrianY);
        Point wallIntersection4 = new Point(pedestrianX,  sectionCenterY);

        // except it is within an port gab, then it will take enter or exit port depending which is closer.
        for ( PedestrianConnectedStreetSections connected : ((PedestrianStreetSection) section).getNextStreetConnector()) {
            if (connected.getFromStreetSection().equals(currentSection)) {
                double portBeginX  = connected.getPortOfFromStreetSection().getBeginOfStreetPort().getX();
                double portBeginY  = connected.getPortOfFromStreetSection().getBeginOfStreetPort().getY();
                double portEndX  = connected.getPortOfFromStreetSection().getEndOfStreetPort().getX();
                double portEndY  = connected.getPortOfFromStreetSection().getEndOfStreetPort().getY();

                if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection1 ) )
                    shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection1);
                if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection2 ) )
                    shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection2);
                if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection3 ) )
                    shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection3);
                if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection4 ) )
                    shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection4);
            }
        }

        for ( PedestrianConnectedStreetSections connected : ((PedestrianStreetSection) section).getPreviousStreetConnector()) {
            if (connected.getToStreetSection().equals(currentSection)) {
                if (connected.getFromStreetSection().equals(currentSection)) {
                    double portBeginX  = connected.getPortOfFromStreetSection().getBeginOfStreetPort().getX();
                    double portBeginY  = connected.getPortOfFromStreetSection().getBeginOfStreetPort().getY();
                    double portEndX  = connected.getPortOfFromStreetSection().getEndOfStreetPort().getX();
                    double portEndY  = connected.getPortOfFromStreetSection().getEndOfStreetPort().getY();

                    if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection1 ) )
                        shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection1);
                    if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection2 ) )
                        shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection2);
                    if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection3 ) )
                        shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection3);
                    if ( checkWallIntersectionWithinPort( portBeginX, portBeginY, portEndX, portEndY, wallIntersection4 ) )
                        shiftIntersection( portBeginX, portBeginY, portEndX, portEndY, wallIntersection4);
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
                    Point wallIntersection1, Point wallIntersection2,
                    Point wallIntersection3, Point wallIntersection4) {

        IConsumer section = pedestrian.getCurrentSection().getStreetSection();
        if ( ! (section instanceof PedestrianStreetSection) ) {
            throw new IllegalArgumentException("Street section is not an instance of PedestrianStreetSection");
        }
        PedestrianStreetSection currentSection = (PedestrianStreetSection) section;

        // special case of crossings. walls can be attracted or repulsive force
        Vector2d wallIntersection1force = getRepulsiveForceAgainstObstacleCalculation( pedestrian, new Point( (int) Math.round(wallIntersection1.getX()), (int) Math.round(wallIntersection1.getY())));
        Vector2d wallIntersection2force = getRepulsiveForceAgainstObstacleCalculation( pedestrian, new Point( (int) Math.round(wallIntersection2.getX()), (int) Math.round(wallIntersection2.getY())));
        Vector2d wallIntersection3force = getRepulsiveForceAgainstObstacleCalculation( pedestrian, new Point( (int) Math.round(wallIntersection3.getX()), (int) Math.round(wallIntersection3.getY())));
        Vector2d wallIntersection4force = getRepulsiveForceAgainstObstacleCalculation( pedestrian, new Point( (int) Math.round(wallIntersection4.getX()), (int) Math.round(wallIntersection4.getY())));

        if ( checkAttractingForce( currentSection, pedestrian ) ) {// attracted force
            sumForce.sub(wallIntersection1force);
            sumForce.sub(wallIntersection2force);
            sumForce.sub(wallIntersection3force);
            sumForce.sub(wallIntersection4force);
        } else {// repulsive force
            sumForce.add(wallIntersection1force);
            sumForce.add(wallIntersection2force);
            sumForce.add(wallIntersection3force);
            sumForce.add(wallIntersection4force);
        }
    }

    boolean checkWallIntersectionWithinPort (double portBeginX, double portBeginY, double portEndX, double portEndY, Point wallIntersection) {
        if ( calculations.almostEqual(portBeginX, portEndX)) {
            if (    (calculations.val1Bigger(portBeginY, wallIntersection.getY()) &&
                    calculations.val1Lower(portEndY, wallIntersection.getY()) )
                    ||
                    (calculations.val1Lower(portBeginY, wallIntersection.getY()) &&
                    calculations.val1Bigger(portEndY, wallIntersection.getY()))
                    ) {
                return true;
            }
        } else {//calculations.almostEqual(portBeginY, portEndY)
            if (    (calculations.val1Bigger(portBeginX, wallIntersection.getX()) &&
                    calculations.val1Lower(portEndX, wallIntersection.getX()) )
                    ||
                    (calculations.val1Lower(portBeginX, wallIntersection.getX()) &&
                    calculations.val1Bigger(portEndX, wallIntersection.getX()))
                    ) {
                return true;
            }
        }
        return false;
    }

    void shiftIntersection( double portBeginX, double portBeginY, double portEndX, double portEndY, Point wallIntersection){
        // point within the port gab
        // get closer corner of port
        if (calculations.getDistanceByCoordinates(portBeginX, portBeginY, wallIntersection.getX(), wallIntersection.getY()) <
                calculations.getDistanceByCoordinates(portEndX, portEndY, wallIntersection.getX(), wallIntersection.getY()))
            wallIntersection.setLocation(portBeginX, portBeginY);
        else wallIntersection.setLocation(portEndX, portEndY);
    }

    Vector2d getRepulsiveForceAgainstObstacle(   Pedestrian pedestrian,
                                                 Point obstaclePosition) {
        Double weightingFactor;
        Point dest = pedestrian.getNextSubGoal();
        Vector2d destination = new Vector2d( dest.getX(), dest.getY());
        Vector2d force = getRepulsiveForceAgainstObstacleCalculation( pedestrian, obstaclePosition);

        if ( model.equals(null)) {
            throw new IllegalArgumentException("model must not be null");
        }

        // Check Field of View --> 170°
        if (calculations.val1BiggerOrAlmostEqual(destination.dot(force),  //A ⋅ B = ||A|| * ||B|| * cos θ
                force.length() * Math.cos(model.pedestrianFieldOfViewDegree / 2))) {
            weightingFactor = model.getPedestrianFieldOfViewWeakeningFactor;
        } else {
            weightingFactor = 0.0;
        }
        force.scale(weightingFactor);
        return force;
    }

    Vector2d getRepulsiveForceAgainstObstacleCalculation(    Pedestrian pedestrian,
                                                             Point obstaclePosition) {
        if( !checkPedestrianInRange( pedestrian, obstaclePosition) ){
            return new Vector2d(0,0 );
        }

        // Distance vector
        Vector2d vectorBetweenPedestrianAndObstacle = new Vector2d(pedestrian.getCurrentGlobalPosition().x, pedestrian.getCurrentGlobalPosition().y);
        vectorBetweenPedestrianAndObstacle.sub(new Vector2d(obstaclePosition.getX(), obstaclePosition.getY()));
        Double distanceBetweenPedestrianAndObstacle = vectorBetweenPedestrianAndObstacle.length();

        // Repulsive force
        Double expo = (-1 * (distanceBetweenPedestrianAndObstacle/ R));
        expo = Math.exp(expo);
        Vector2d force = vectorBetweenPedestrianAndObstacle;

        force.scale(U0AlphaBeta * expo);
        force.negate();
        return force;
    }

    boolean checkPedestrianInRange( Pedestrian pedestrian, Point intersectionPos){
        if ( calculations.almostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
                pedestrian.getCurrentGlobalPosition().getY(),
                intersectionPos.getX(),
                intersectionPos.getY()) ,
                model.pedestrianFieldOfViewRadius)) {
            return true;
        }
        return false;
    }
}
