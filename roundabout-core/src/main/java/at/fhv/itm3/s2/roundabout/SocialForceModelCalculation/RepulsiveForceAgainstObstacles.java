package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Point2D;

public class RepulsiveForceAgainstObstacles {

    final private Double R = 20.0; // cm
    final private Double U0AlphaBeta = 1000.0; // (cm/s)^2
    SupportiveCalculations calculations;

    public Vector2d getRepulsiveForceAgainstAllObstacles(RoundaboutSimulationModel model,
                                                         Pedestrian pedestrian, Vector2d destination){
        Vector2d sumForce = new Vector2d(0,0);

        for() {
            Point obstaclePosition =; // TODO

            // Everything in Range? --> 8 m
            if (calculations.AlmostEqual( Point2D.distance(destination.x, destination.y, obstaclePosition.x, obstaclePosition.y) ,
                    model.pedestrianFieldOfViewRadius)) {
                Double weightingFactor = 1.;

                Vector2d force = getRepulsiveForceAgainstObstacle(model, pedestrian, obstaclePosition);

                Vector2d destinationAndForce = destination;

                // Check Field of View --> 170°
                if (calculations.BiggerOrAlmostEqual(destinationAndForce.dot(force),  //A ⋅ B = ||A|| * ||B|| * cos θ
                        force.length() * Math.cos(model.pedestrianFieldOfViewDegree/2))) {
                    weightingFactor = model.getPedestrianFieldOfViewWeakeningFactor;
                }
                force.scale(weightingFactor);
                sumForce.add(force);
            }
        }

        return sumForce;
    }

    public Vector2d getRepulsiveForceAgainstObstacle(RoundaboutSimulationModel model,
                                                     Pedestrian pedestrian,
                                                     Point obstaclePosition) {

        //vectorBetweenBothPedestrian
        Vector2d vectorBetweenBothPedestrian = new Vector2d(pedestrian.getCurrentPosition().x, pedestrian.getCurrentPosition().y);
        vectorBetweenBothPedestrian.sub(new Vector2d(obstaclePosition.getX(), obstaclePosition.getY()));

        vectorBetweenBothPedestrian.scale(-1*vectorBetweenBothPedestrian.length());


        // Repulsive force against other pedestrians
        Double exponent = vectorBetweenBothPedestrian.length()/R;  // is 2b --> and we need b
        exponent *= -1;
        exponent = Math.exp(exponent);

        Double repulsiveForce = U0AlphaBeta * exponent;

        vectorBetweenBothPedestrian.scale(repulsiveForce);
        vectorBetweenBothPedestrian.negate();

        return vectorBetweenBothPedestrian;
    }

}
