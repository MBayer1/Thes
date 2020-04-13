package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;

public class ForceTestTMP {

    SupportiveCalculations calculations = new SupportiveCalculations();

    public ForceTestTMP() {
        getAccelerationForceToTarget();
    }


    public void getAccelerationForceToTarget(){
        Vector2d accelerationForce = ownVersionAccelerationForce();
        Vector2d forceAgainstPedestrian = getForceAgainstPedestrian();
        Vector2d forceAgainstWallsObsticals = getForceAgainstWallsObsticals();
        Vector2d forceAgainstVehicle = getForceAgainstVehicle();
    }

    public Vector2d ownVersionAccelerationForce(){
        Vector2d currentSpeedVector = new Vector2d(0,0);//pedestrian.getCurrentSpeed(),0.0);
        Vector2d currentPositionVector = new Vector2d(0,0);//pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());
        SupportiveCalculations calculations = new SupportiveCalculations();

        /*        if (! (pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreetSection)) {throw new IllegalStateException("Section not instance of PedestrianStreetSection.");}        PedestrianStreetSection section = (PedestrianStreetSection)pedestrian.getCurrentSection().getStreetSection();*/

        //nextDestinationVector = nextDestinationVector - currentPositionVector
        PedestrianPoint subGoal = new PedestrianPoint(10,10);//pedestrian.getNextSubGoal(); // global  coordinates without any obstacle etc. = exit-point of  section -> always calc new since real aim is afterwards change so is current position
        Vector2d preferredSpeedVector = calculations.getVector( currentPositionVector.getX(), currentPositionVector.getY(),
                subGoal.getX() ,//0 //+ section.getGlobalCoordinateOfSectionOrigin().getX(),
                subGoal.getY() );//0 //+ section.getGlobalCoordinateOfSectionOrigin().getY());

        Double preferredSpeedValue = preferredSpeedVector.length();
        preferredSpeedVector.scale(1/preferredSpeedValue);
        preferredSpeedVector.scale(/*pedestrian.*/ownVersionAccelerationForce_calculatePreferredSpeed()); //v_alpha * e_alpha(t)

        preferredSpeedVector.sub(currentSpeedVector);
        preferredSpeedVector.scale(1/2.2); //model.getRandomPedestrianRelaxingTimeTauAlpha());

        return preferredSpeedVector;
    }

    public double ownVersionAccelerationForce_calculatePreferredSpeed() {
        double preferredSpeed = 5; // m/s
        double maxPreferredSpeed = 8;
        double timeRelatedParameterValueNForSpeedCalculation = 1;

        double averageSpeed = 0;//getTimeSpentInSystem() == 0 ? 0 : walkedDistance / getTimeSpentInSystem();
        double timeRelatedParameter = averageSpeed / preferredSpeed;
        timeRelatedParameter = timeRelatedParameterValueNForSpeedCalculation - timeRelatedParameter;

        double part1 = (1 - timeRelatedParameter) * preferredSpeed;
        double part2 = timeRelatedParameter * maxPreferredSpeed;

        return part1 + part2;
    }

    public Vector2d getForceAgainstPedestrian(){
        Vector2d betaGlobalPos = new Vector2d(0,1100);
        Vector2d alphaGlobalPos = new Vector2d(0,0);

        Vector2d betaGoal = new Vector2d(500,1100);
        Vector2d alphaGoal = new Vector2d(500,0);

        Double sigma = 30.0; // in centimeter
        Double VAlphaBeta = 210.0; // (cm / s)^2


        //vectorBetweenBothPedestrian
        Vector2d posBeta = new Vector2d(betaGlobalPos.getX(), betaGlobalPos.getY());
        Vector2d vectorBetweenBothPedestrian = new Vector2d(alphaGlobalPos.getX(), alphaGlobalPos.getY());
        vectorBetweenBothPedestrian.sub(betaGlobalPos);

        //preferredDirectionOfBeta = eBeta
        Vector2d vecPosBeta = new Vector2d(betaGlobalPos.getX(), betaGlobalPos.getY());
        Vector2d nextAimBeta = betaGoal;
        Vector2d vecNextAimBeta = new Vector2d(nextAimBeta.getX(), nextAimBeta.getY());

        Vector2d preferredDirectionOfBeta = vecNextAimBeta;
        preferredDirectionOfBeta.sub(vecPosBeta);
        Double nextAimBetaLength = preferredDirectionOfBeta.length();
        preferredDirectionOfBeta.scale(1/nextAimBetaLength);

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = nextAimBetaLength;

        //small half axis of the ellipse
        Vector2d betaData = new Vector2d(preferredDirectionOfBeta);
        betaData.scale(traveledPathWithinTOfBeta);
        Vector2d nextDestinationVectorAlphaSubTravelPathBeta = new Vector2d(vectorBetweenBothPedestrian);
        nextDestinationVectorAlphaSubTravelPathBeta.sub(betaData);

        Double smallHalfAxisOfEllipse = Math.sqrt(  Math.pow(vectorBetweenBothPedestrian.length() + nextDestinationVectorAlphaSubTravelPathBeta.length(),2)) -
                Math.pow(traveledPathWithinTOfBeta,2);

        // Repulsive force against other pedestrians
        // V_alphaBeta(t0)* e^(-b/sigma)
        Double exponent = smallHalfAxisOfEllipse/(-2);  // is 2b --> and we need b
        exponent /= sigma;
        exponent = Math.exp(exponent);
        Double repulsiveForce = VAlphaBeta * exponent;

        // - vecAlphaBeta * forcesAgainstBeta
        vectorBetweenBothPedestrian.scale(repulsiveForce);
        vectorBetweenBothPedestrian.negate();

        return vectorBetweenBothPedestrian;
    }

    public Vector2d  getForceAgainstWallsObsticals(){
        return new Vector2d(0,0);
    }
    public Vector2d getForceAgainstVehicle(){
        return new Vector2d(0,0);
    }
}
