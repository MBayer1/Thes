package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;

public class AccelerationForceToTarget {
    SupportiveCalculations calculations = new SupportiveCalculations();

    public Vector2d getAccelerationForceToTarget(RoundaboutSimulationModel model, Pedestrian pedestrian){
        Vector2d currentSpeedVector = calculations.getUnitVector(pedestrian.getPreviousSFMVector());
        currentSpeedVector.scale(pedestrian.getCurrentSpeed());
        Vector2d currentPositionVector = new Vector2d(pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());


        if (! (pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreetSection)) {
            throw new IllegalStateException("Section not instance of PedestrianStreetSection.");
        }

        PedestrianStreetSection section = (PedestrianStreetSection)pedestrian.getCurrentSection().getStreetSection();

        //nextDestinationVector = nextDestinationVector - currentPositionVector
        PedestrianPoint subGoal = pedestrian.getNextSubGoal(); // global  coordinates without any obstacle etc. = exit-point of  section -> always calc new since real aim is afterwards change so is current position
        Vector2d preferredSpeedVector = calculations.getVector( currentPositionVector.getX(), currentPositionVector.getY(),
                                                        subGoal.getX() + section.getGlobalCoordinateOfSectionOrigin().getX(),
                                                        subGoal.getY() + section.getGlobalCoordinateOfSectionOrigin().getY());

        Double preferredSpeedValue = preferredSpeedVector.length();
        preferredSpeedVector.scale(1/preferredSpeedValue);
        preferredSpeedVector.scale(pedestrian.calculatePreferredSpeed()); //v_alpha * e_alpha(t)

        preferredSpeedVector.sub(currentSpeedVector);
        preferredSpeedVector.scale(1/model.getRandomPedestrianRelaxingTimeTauAlpha());

        pedestrian.setPreviousSFMVector(preferredSpeedVector);
        return preferredSpeedVector;
    }
}

