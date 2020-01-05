package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianRoute;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Point2D;

public class RepulsiveForceAgainstOtherPedestrians {

    final private Double sigma = 30.0; // in centimeter
    final private Double VAlphaBeta = 210.0; // (cm / s)^2
    SupportiveCalculations calculations;

    public Vector2d getRepulsiveForceAgainstAllOtherPedestrians(RoundaboutSimulationModel model,
                                                              Pedestrian pedestrian, Vector2d destination){

        Vector2d sumForce = new Vector2d(0,0);

        for() {
            Pedestrian pedestrianBeta =; // TODO

            // Everything in Range? --> 8 m
            if (calculations.AlmostEqual( Point2D.distance(destination.x, destination.y,
                    pedestrianBeta.getCurrentPosition().getX(), pedestrianBeta.getCurrentPosition().getY()) ,
                    model.pedestrianFieldOfViewRadius)) {
                Double weightingFactor = 1.;

                Vector2d force = getRepulsiveForceAgainstOtherPedestrian(model, pedestrian, pedestrianBeta);

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


    boolean checkPedestrianInRange(){
        return false;
    }

    public Vector2d getRepulsiveForceAgainstOtherPedestrian(   RoundaboutSimulationModel model,
                                                                Pedestrian pedestrianAlpha, Pedestrian pedestrianBeta){

        //vectorBetweenBothPedestrian
        Vector2d vectorBetweenBothPedestrian = new Vector2d(pedestrianAlpha.getCurrentPosition().x, pedestrianAlpha.getCurrentPosition().y);
        vectorBetweenBothPedestrian.sub(new Vector2d(pedestrianBeta.getCurrentPosition().x, pedestrianBeta.getCurrentPosition().y));

        //preferredDirectionOfBeta = eBeta
        Point posBeta = pedestrianBeta.getCurrentPosition();
        Vector2d vecPosBeta = new Vector2d(posBeta.getX(), posBeta.getY());
        Point nextAimBeta = pedestrianBeta.getNextSubGoal();
        Vector2d vecNextAimBeta = new Vector2d(nextAimBeta.getX(), nextAimBeta.getY());
        vecNextAimBeta.sub(vecPosBeta);
        Double nextAimBetaLength = vecNextAimBeta.length();
        vecPosBeta.scale(1/nextAimBetaLength);
        Vector2d preferredDirectionOfBeta = vecPosBeta;

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = pedestrianBeta.getWalkedDistance();


        //small half axis of the ellipse
        Vector2d betaData = preferredDirectionOfBeta;
        betaData.scale(traveledPathWithinTOfBeta);
        Vector2d nextDestinationVectorAlphaSubTravelPathBeta = vectorBetweenBothPedestrian;
        nextDestinationVectorAlphaSubTravelPathBeta.sub(betaData);

        Double smallHalfAxisOfEllipse = Math.sqrt(  (Math.pow(vectorBetweenBothPedestrian.length() + nextDestinationVectorAlphaSubTravelPathBeta.length(),2)) -
                                                     Math.pow(traveledPathWithinTOfBeta,2));

        // Repulsive force against other pedestrians
        Double exponent = smallHalfAxisOfEllipse/-2;  // is 2b --> and we need b
        exponent /= sigma;
        exponent = Math.exp(exponent);
        Double repulsiveForce = VAlphaBeta * exponent;

        vectorBetweenBothPedestrian.scale(repulsiveForce);
        vectorBetweenBothPedestrian.negate();


        return vectorBetweenBothPedestrian;
    }



}
