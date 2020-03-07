package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.*;

public class AccelerationForceToTarget {
    SupportiveCalculations calculations = new SupportiveCalculations();

    public Vector2d getAccelerationForceToTarget(RoundaboutSimulationModel model, Pedestrian pedestrian){

        Vector2d currentSpeedVector = new Vector2d(pedestrian.getCurrentSpeed(),0.0);
        Vector2d currentPositionVector = new Vector2d(pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());


        if (pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreetSection) {

        }

        PedestrianStreetSection section = (PedestrianStreetSection)pedestrian.getCurrentSection().getStreetSection();

        //nextDestinationVector = nextDestinationVector - currentPositionVector
        Point subGoal = pedestrian.getNextSubGoal(); // local coordinates
        Vector2d preferredSpeedVector = calculations.getVector( pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY(),
                                                        subGoal.getX() + section.getGlobalCoordinateOfSectionOrigin().getX(),
                                                        subGoal.getY() + section.getGlobalCoordinateOfSectionOrigin().getY());

        Double preferredSpeedValue = preferredSpeedVector.length();
        preferredSpeedVector.scale(1/preferredSpeedValue);
        preferredSpeedVector.scale(pedestrian.calculatePreferredSpeed()); //v_alpha * e_alpha(t)

        preferredSpeedVector.sub(currentSpeedVector);
        preferredSpeedVector.scale(1/model.getRandomPedestrianRelaxingTimeTauAlpha());

        return preferredSpeedVector;
    }
}

