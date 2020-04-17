package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;

import javax.vecmath.Vector2d;

public class ForceCalcTestEnvironment {

    SupportiveCalculations calculations = new SupportiveCalculations();
    final private Double R = 20.0; // cm
    final private Double U0AlphaBeta = 1000.0; // (cm/s)^2

    public ForceCalcTestEnvironment() {
        getAccelerationForceToTarget();
    }


    public void getAccelerationForceToTarget(){
        Vector2d accelerationForce = ownVersionAccelerationForce();
        Vector2d forceAgainstPedestrian = getForceAgainstPedestrian();
        Vector2d forceAgainstWallsObsticals = getForceAgainstWallsObstacles();
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
        Vector2d betaGlobalPos = new Vector2d(30,10);
        Vector2d alphaGlobalPos = new Vector2d(0,0);

        Vector2d betaGoal = new Vector2d(50,100);
        Vector2d alphaGoal = new Vector2d(500,0);

        Double sigma = 30.0; // in centimeter
        Double VAlphaBeta = 210.0; // (cm / s)^2

        //vectorBetweenBothPedestrian
        Vector2d vectorBetweenBothPedestrian = new Vector2d(alphaGlobalPos.getX(), alphaGlobalPos.getY());
        vectorBetweenBothPedestrian.sub(betaGlobalPos);

        //preferredDirectionOfBeta = eBeta
        Vector2d vecPosBeta = new Vector2d(betaGlobalPos.getX(), betaGlobalPos.getY());
        Vector2d vecNextAimBeta = new Vector2d(betaGoal.getX(), betaGoal.getY());

        Vector2d preferredDirectionOfBeta = new Vector2d(vecNextAimBeta);
        preferredDirectionOfBeta.sub(vecPosBeta);
        preferredDirectionOfBeta.scale(1/preferredDirectionOfBeta.length());

        //Traveled path of the walker β within ∆t
        double time = 0;
        double timeSpendInSystemBeta = 300;
        double waledDistanceBeta = 15000;
        double preferedSpeedBeta = 1.4; //cm/s
        Double traveledPathWithinTOfBeta = waledDistanceBeta; // waled distance
        traveledPathWithinTOfBeta /= timeSpendInSystemBeta;//  time spend in system


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

        vectorBetweenBothPedestrian = calculations.getUnitVector(vectorBetweenBothPedestrian);
        vectorBetweenBothPedestrian.scale(repulsiveForce* (-1));
        return vectorBetweenBothPedestrian;
    }

    public Vector2d getForceAgainstWallsObstacles(){
        PedestrianPoint pedestrianPos = new PedestrianPoint(1200,1000);
        PedestrianPoint pedestrianGoal = new PedestrianPoint(1500,400);
        PedestrianPoint wallIntersection1 = new PedestrianPoint(0, 1000);
        PedestrianPoint wallIntersection2 = new PedestrianPoint(1500,1000);
        PedestrianPoint wallIntersection3 = new PedestrianPoint(1200,0);
        PedestrianPoint wallIntersection4 = new PedestrianPoint(1200,1500);

        Double weightingFactor;
        PedestrianPoint dest = pedestrianGoal;//pedestrian.getNextSubGoal();
        Vector2d destination = new Vector2d( dest.getX(), dest.getY());

        Vector2d force1 = getRepulsiveForceAgainstObstacleCalculation( pedestrianGoal, pedestrianPos, wallIntersection1);
        weightingFactor = checkFieldOfView(force1, destination);
        force1.scale(weightingFactor);
        Vector2d force2 = getRepulsiveForceAgainstObstacleCalculation( pedestrianGoal, pedestrianPos, wallIntersection2);
        weightingFactor = checkFieldOfView(force2, destination);
        force2.scale(weightingFactor);
        Vector2d force3 = getRepulsiveForceAgainstObstacleCalculation( pedestrianGoal, pedestrianPos, wallIntersection3);
        weightingFactor = checkFieldOfView(force3, destination);
        force3.scale(weightingFactor);
        Vector2d force4 = getRepulsiveForceAgainstObstacleCalculation( pedestrianGoal, pedestrianPos, wallIntersection4);
        weightingFactor = checkFieldOfView(force4, destination);
        force4.scale(weightingFactor);

        Vector2d force = new Vector2d(0,0);
        force.add(force1);
        force.add(force2);
        force.add(force3);
        force.add(force4);
        return force;
    }

    double checkFieldOfView (Vector2d force, Vector2d destination) {
        //force = new Vector2d(180,0);
        //destination = new Vector2d(0,180);

        if (calculations.val1BiggerOrAlmostEqual(destination.dot(force),  //A ⋅ B = ||A|| * ||B|| * cos θ
                force.length() * Math.cos(Math.toRadians(170/2)))){//model.pedestrianFieldOfViewDegree / 2))) {
            return 1;// model.getPedestrianFieldOfViewWeakeningFactor;
        } else {
            return  0.1;
        }
    }

    Vector2d getRepulsiveForceAgainstObstacleCalculation(    PedestrianPoint goalPedestiran, PedestrianPoint posPedestrian, PedestrianPoint obstaclePosition) {
        if( !checkPedestrianInRange( goalPedestiran, obstaclePosition) ){
            return new Vector2d(0,0 );
        }

        // Distance vector
        Vector2d vectorBetweenPedestrianAndObstacle = new Vector2d(posPedestrian.getX(), posPedestrian.getY());
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

    boolean checkPedestrianInRange( PedestrianPoint goal, PedestrianPoint intersectionPos){
        if ( calculations.val1LowerOrAlmostEqual( calculations.getDistanceByCoordinates(   goal.getX(),
                goal.getY(),
                intersectionPos.getX(),
                intersectionPos.getY()) ,
                800/*model.pedestrianFieldOfViewRadius*/)) {
            return true;
        }
        return false;
    }



    public Vector2d getForceAgainstVehicle(){
        return new Vector2d(0,0);
    }
}
