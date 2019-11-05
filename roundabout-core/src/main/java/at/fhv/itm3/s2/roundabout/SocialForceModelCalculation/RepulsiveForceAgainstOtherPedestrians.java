package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.*;

public class RepulsiveForceAgainstOtherPedestrians {

    public Vector2d getRepulsiveForceAgainstOtherPedestrians(   RoundaboutSimulationModel model,
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

        return null;
    }


}
