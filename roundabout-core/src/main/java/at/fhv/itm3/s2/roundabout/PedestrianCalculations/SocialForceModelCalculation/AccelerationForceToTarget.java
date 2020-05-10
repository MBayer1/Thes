package at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;

public class AccelerationForceToTarget {
    SupportiveCalculations calculations = new SupportiveCalculations();

    public Vector2d getAccelerationForceToTarget(RoundaboutSimulationModel model, Pedestrian pedestrian){
        Vector2d currentSpeedVector = calculations.getUnitVector(pedestrian.getPreviousSFMVector());
        currentSpeedVector = calculations.getUnitVector(currentSpeedVector);
        currentSpeedVector.scale(pedestrian.getCurrentSpeed());
        Vector2d currentPositionVector = new Vector2d(pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());

        if (! (pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreetSection)) {
            throw new IllegalStateException("Section not instance of PedestrianStreetSection.");
        }

        // global  coordinates without any obstacle etc. = exit-point of  section -> always calc new since real aim is afterwards change so is current position
        PedestrianPoint subGoal = pedestrian.getNextSubGoal();
        double distToGoal = calculations.getDistanceByCoordinates(subGoal, pedestrian.getCurrentGlobalPosition());

        // e(t)
        Vector2d preferredSpeedVector = new Vector2d(subGoal.getX(), subGoal.getY());
        preferredSpeedVector.sub(currentPositionVector);
        double lengthBetweenPosAndGoal = preferredSpeedVector.length();
        if (lengthBetweenPosAndGoal != 0) {
            preferredSpeedVector.scale(1 / lengthBetweenPosAndGoal);
            // preferredSpeed * e(t)
            preferredSpeedVector.scale(pedestrian.calculatePreferredSpeed()); //v_alpha * e_alpha(t)
        } else  {
            preferredSpeedVector.scale(0);
        }

        // weight factor of current SpeedVec
        preferredSpeedVector.scale(distToGoal);
        if(distToGoal < currentSpeedVector.length()) {// -> 50%
            // needed as backup
            // otherwise weight of currentSpeedVector is to high
            currentSpeedVector = calculations.getUnitVector(currentSpeedVector);
            currentSpeedVector.scale(distToGoal / 50);
        }

        // 1/tau (preferred speed - current speed)
        preferredSpeedVector.sub(currentSpeedVector);
        double Tau = model.getRandomPedestrianRelaxingTimeTauAlpha();
        preferredSpeedVector.scale(1/Tau);

        if(Double.isNaN(preferredSpeedVector.getX()) || Double.isNaN(preferredSpeedVector.getY()) ){
            throw new IllegalStateException("Vector calculation  error: AccelerationForce.");
        }

        if (lengthBetweenPosAndGoal < preferredSpeedVector.length()){
            throw new IllegalStateException("Vector calculation error: AccelerationForce runs over the goal.");
        }

        return preferredSpeedVector;
    }
}

