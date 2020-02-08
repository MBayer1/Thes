package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConnectedStreetSections;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.*;

public class RepulsiveForceAgainstObstacles {

    final private Double R = 20.0; // cm
    final private Double U0AlphaBeta = 1000.0; // (cm/s)^2
    SupportiveCalculations calculations;
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

    public void forceAgainstWalls( Pedestrian pedestrian, Vector2d sumForce ) {
        IConsumer section = pedestrian.getCurrentSection().getStreetSection();
        if ( ! (section instanceof PedestrianStreetSection) ) {
            throw new IllegalArgumentException("Street section is not an instance of PedestrianStreetSection");
        }
        PedestrianStreetSection currentSection = (PedestrianStreetSection) section;

        forceAgainstWallsIteration( pedestrian, sumForce );
    }

    public void forceAgainstWallsIteration ( Pedestrian pedestrian, Vector2d sumForce ){

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
         Double pedestrianX = pedestrian.getCurrentGlobalPosition().getX();
         Double pedestrianY = pedestrian.getCurrentGlobalPosition().getY();
         Double sectionCenterX = currentSection.getGlobalCoordinateOfSectionOrigin().getX();
         Double sectionCenterY = currentSection.getGlobalCoordinateOfSectionOrigin().getY();
        // person positions - border intersections
        Vector2d wallIntersection1 = calculations.getVector(pedestrianX, pedestrianY, pedestrianX, sectionCenterY);
        Vector2d wallIntersection2 = calculations.getVector(pedestrianX, pedestrianY, pedestrianX, sectionCenterY + currentSection.getLengthY());
        Vector2d wallIntersection3 = calculations.getVector(pedestrianX, pedestrianY, sectionCenterX, pedestrianY);
        Vector2d wallIntersection4 = calculations.getVector(pedestrianX, pedestrianY, sectionCenterX + currentSection.getLengthX(), pedestrianY);

        // except it is within an port gab, then it will take enter or exit port depending which is closer.
        for ( PedestrianConnectedStreetSections connected : ((PedestrianStreetSection) section).getNextStreetConnector().getSectionPairs()) {
            if (connected.getFromStreetSection().equals(currentSection)) {
                double portBeginX  = connected.getPortOfFromStreetSection().getBeginOfStreetPort().getX();
                double portBeginY  = connected.getPortOfFromStreetSection().getBeginOfStreetPort().getY();
                double portEndX  = connected.getPortOfFromStreetSection().getEndOfStreetPort().getX();
                double portEndY  = connected.getPortOfFromStreetSection().getEndOfStreetPort().getY();

                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection1);
                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection2);
                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection3);
                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection3);
            }
        }
        for ( PedestrianConnectedStreetSections connected : ((PedestrianStreetSection) section).getPreviousStreetConnector().getSectionPairs()) {
            if (connected.getToStreetSection().equals(currentSection)) {
                double portBeginX  = connected.getPortOfToStreetSection().getBeginOfStreetPort().getX();
                double portBeginY  = connected.getPortOfToStreetSection().getBeginOfStreetPort().getY();
                double portEndX  = connected.getPortOfToStreetSection().getEndOfStreetPort().getX();
                double portEndY  = connected.getPortOfToStreetSection().getEndOfStreetPort().getY();

                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection1);
                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection2);
                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection3);
                checkWallIntersectionWithinPort(portBeginX, portBeginY, portEndX, portEndY, wallIntersection3);
            }
        }

        // special case of crossings. walls can be attracted or repulsive force
        if ( currentSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING) && !currentSection.checkPedestrianIsWithinSection(pedestrian)  ) {
            // attracted force
            sumForce.sub(wallIntersection1);
            sumForce.sub(wallIntersection2);
            sumForce.sub(wallIntersection3);
            sumForce.sub(wallIntersection4);
        } else {
            // repulsive force
            sumForce.add(wallIntersection1);
            sumForce.add(wallIntersection2);
            sumForce.add(wallIntersection3);
            sumForce.add(wallIntersection4);
        }
    }

    public void checkWallIntersectionWithinPort (double portBeginX, double portBeginY, double portEndX, double portEndY, Vector2d wallIntersection1) {
        if ((calculations.val1Bigger(portBeginX, wallIntersection1.getX()) &&
                calculations.val1Bigger(portBeginY, wallIntersection1.getY()) &&
                calculations.val1Lower(portEndX, wallIntersection1.getX()) &&
                calculations.val1Lower(portEndY, wallIntersection1.getY()))
                ||
                (calculations.val1Lower(portBeginX, wallIntersection1.getX()) &&
                        calculations.val1Lower(portBeginY, wallIntersection1.getY()) &&
                        calculations.val1Bigger(portEndX, wallIntersection1.getX()) &&
                        calculations.val1Bigger(portEndY, wallIntersection1.getY()))
                ) {

            // point within the port gab
            // get closer corner of port
            if (calculations.getDistanceByCoordinates(portBeginX, portBeginY, wallIntersection1.getX(), wallIntersection1.getY()) <
                    calculations.getDistanceByCoordinates(portEndX, portEndY, wallIntersection1.getX(), wallIntersection1.getY()))
                wallIntersection1.set(portBeginX, portBeginY);
            else wallIntersection1.set(portEndX, portEndY);
        }
    }

    public Vector2d getRepulsiveForceAgainstObstacle(   Pedestrian pedestrian,
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

    public Vector2d getRepulsiveForceAgainstObstacleCalculation(    Pedestrian pedestrian,
                                                                    Point obstaclePosition) {
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
}
