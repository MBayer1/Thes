package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.Model;

import javax.vecmath.Vector2d;
import java.util.function.IntToDoubleFunction;

public class AccelerationForceToTarget {

    public RoundaboutSimulationModel model;
    public Pedestrian pedestrian;

    public AccelerationForceToTarget(RoundaboutSimulationModel model, Pedestrian pedestrian){
        this.model = model;
        this.pedestrian = pedestrian;
    }

    public Vector2d getAccelerationForceToTarget(){

        Vector2d currentSpeedVector = new Vector2d(pedestrian.getCurrentSpeed(),0.0);
        Vector2d currentPositionVector = new Vector2d(pedestrian.getCurrentPosition().getX(), pedestrian.getCurrentPosition().getY());


        Vector2d preferredSpeedVector = new Vector2d(pedestrian.getNextSubGoal().getX(), pedestrian.getNextSubGoal().getY()); //nextDestinationVector
        preferredSpeedVector.sub(currentPositionVector); //nextDestinationVector - currentPositionVector


        Double preferredSpeedValue = preferredSpeedVector.length();
        preferredSpeedVector.scale(1/preferredSpeedValue);
        preferredSpeedVector.scale(pedestrian.calculatePreferredSpeed()); //v_alpha * e_alpha(t)


        preferredSpeedVector.sub(currentSpeedVector);
        preferredSpeedVector.scale(1/model.getRandomRelaxingTimeTauAlpha());

        return preferredSpeedVector;
    }
}

