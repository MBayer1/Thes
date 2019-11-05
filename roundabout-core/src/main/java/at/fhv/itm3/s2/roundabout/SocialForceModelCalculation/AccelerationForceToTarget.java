package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.Model;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.function.IntToDoubleFunction;

public class AccelerationForceToTarget {

    public Vector2d getAccelerationForceToTarget(RoundaboutSimulationModel model, Pedestrian pedestrian){

        Vector2d currentSpeedVector = new Vector2d(pedestrian.getCurrentSpeed(),0.0);
        Vector2d currentPositionVector = new Vector2d(pedestrian.getCurrentPosition().getX(), pedestrian.getCurrentPosition().getY());


        Point subGoal = pedestrian.getNextSubGoal();
        Vector2d preferredSpeedVector = new Vector2d(subGoal.getX(), subGoal.getY()); //nextDestinationVector
        preferredSpeedVector.sub(currentPositionVector); //nextDestinationVector - currentPositionVector


        Double preferredSpeedValue = preferredSpeedVector.length();
        preferredSpeedVector.scale(1/preferredSpeedValue);
        preferredSpeedVector.scale(pedestrian.calculatePreferredSpeed()); //v_alpha * e_alpha(t)


        preferredSpeedVector.sub(currentSpeedVector);
        preferredSpeedVector.scale(1/model.getRandomRelaxingTimeTauAlpha());

        return preferredSpeedVector;
    }
}

