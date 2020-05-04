package at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;

import javax.vecmath.Vector2d;
import java.awt.geom.Point2D;

public class ForceCalcTestEnvironment {

    SupportiveCalculations calculations = new SupportiveCalculations();
    final private Double R = 20.0; // cm
    final private Double U0AlphaBeta = 1000.0; // (cm/s)^2
    Double pedestrianFieldOfViewRadius = 800.0;

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
        Vector2d betaGlobalPos = new Vector2d(10,5);
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
        //(relative) step lengths of the pedestrians according to their current velocities
        double time = 0;
        double timeSpendInSystemBeta = 300;
        double waledDistanceBeta = 15000;
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
        Vector2d sumForce = new Vector2d(0,0);
        PedestrianPoint pedestrianGlobPos = new PedestrianPoint(0,100);
        PedestrianPoint VehicleFront = new PedestrianPoint();
        PedestrianPoint VeicleBack = new PedestrianPoint();
        PedestrianPoint globalPositionOfVehicle = new PedestrianPoint(0,0);
        PedestrianPoint globalAimOfVehicle = new PedestrianPoint(0,100);
        double vehicleLength = 500; //cm


        // check if it is in range
        if ( checkPedestrianInRangeFront(pedestrianGlobPos, VehicleFront,vehicleLength) ){
            sumForce.add(calculateRepulsiveForceAgainstVehicles( pedestrianGlobPos, globalPositionOfVehicle, globalAimOfVehicle));
        }

        // check if it is in range
        if (checkPedestrianInRangeBack(pedestrianGlobPos, VeicleBack, vehicleLength)) {
            sumForce.add(calculateRepulsiveForceAgainstVehicles(pedestrianGlobPos, globalPositionOfVehicle, globalAimOfVehicle));
        }

        return sumForce;
    }

    boolean checkPedestrianInRangeFront(PedestrianPoint pedestriancurGlobPos, PedestrianPoint globalPositionOfVehicle, double vehicleLength){
        if ( calculations.almostEqual( Point2D.distance(   pedestriancurGlobPos.getX(),
                pedestriancurGlobPos.getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                pedestrianFieldOfViewRadius + vehicleLength/2)) {
            return true;
        }
        return false;
    }

    boolean checkPedestrianInRangeBack( PedestrianPoint pedestriancurGlobPos, PedestrianPoint globalPositionOfVehicle, double vehicleLength){
        if ( calculations.almostEqual( Point2D.distance(   pedestriancurGlobPos.getX(),
                pedestriancurGlobPos.getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                pedestrianFieldOfViewRadius/2+ vehicleLength/2)) {
            return true;
        }
        return false;
    }

    public Vector2d calculateRepulsiveForceAgainstVehicles(PedestrianPoint pedestrianCurGlobPos,
                                                           PedestrianPoint globalPositionOfVehicle,
                                                           PedestrianPoint globalAimOfVehicle
                                                           ) {

        final Double Av_RepulsivePotential = 1.29;
        final Double Bv_RepulsivePotential = 0.96;
        PedestrianPoint globalPositionOfVehicleFront = new PedestrianPoint(10,0);
        PedestrianPoint globalPositionOfVehicleBack = new PedestrianPoint(15,0);
        PedestrianPoint pedestriangetCurrentGlobalPosition = new PedestrianPoint(0,0);

        double carSpeed = 50.; //km/h
        Vector2d vecVehicleFront = new Vector2d(globalPositionOfVehicleFront.getX(), globalPositionOfVehicleFront.getY());
        Vector2d vecVehicleBack = new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY());
        Vector2d personPos = new Vector2d(pedestriangetCurrentGlobalPosition.getX(), pedestriangetCurrentGlobalPosition.getY());

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = carSpeed; //car.getDriverBehaviour().getSpeed() / 3.6; // speed in km/h -> change to m/s := 1000/(60*60)

        // preparation
        Vector2d vecVehicleFrontFuture = new Vector2d(vecVehicleFront);
        vecVehicleFrontFuture.scale(traveledPathWithinTOfBeta);
        PedestrianPoint vehicleFrontFuture = new PedestrianPoint(vecVehicleFrontFuture.getX(), vecVehicleFrontFuture.getY());

        // calc 2b //small half axis of the ellipse
        Vector2d part1 = new Vector2d(personPos);
        part1.sub(vecVehicleBack);
        Vector2d part2 = new Vector2d(personPos);
        part2.sub(vecVehicleFrontFuture);
        Vector2d part3 = new Vector2d(vecVehicleBack);
        part3.sub(vecVehicleFrontFuture);

        double b = Math.pow((part1.length()+part2.length()),2) - Math.pow(part3.length(),2);
        b = Math.sqrt(b);
        // calc
        b /= 2;

        // exponent (-B*b)
        double exponent = Bv_RepulsivePotential * (-1) * b;
        exponent = Math.exp(exponent);

        // n_Vector
        Vector2d n_vec = getNormVexAlongTangentOfEllipse(pedestriangetCurrentGlobalPosition, vehicleFrontFuture, globalPositionOfVehicleBack);

        //A*expo(-B*b)*n
        n_vec.scale(exponent*Av_RepulsivePotential);

        return n_vec;
    }

    private Vector2d getNormVexAlongTangentOfEllipse(PedestrianPoint globalPedestrianAlphaPoint,
                                                     PedestrianPoint globalPositionOfVehicleFrontFuture,
                                                     PedestrianPoint globalPositionOfVehicleBack){
        //http://www.nabla.hr/Z_MemoHU-029.htm
        //https://www.khanacademy.org/math/precalculus/x9e81a4f98389efdf:conics/x9e81a4f98389efdf:ellipse-foci/a/ellipse-foci-review
        //https://www.mathopenref.com/coordparamellipse.html

        Vector2d vecGlobalPedestrianAlphaPoint = new Vector2d(globalPedestrianAlphaPoint.getX(), globalPedestrianAlphaPoint.getY());
        Vector2d vecGlobalPositionOfVehicleBack = new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY());
        Vector2d vecGlobalPositionOfVehicleFrontFuture = new Vector2d(globalPositionOfVehicleFrontFuture.getX(), globalPositionOfVehicleFrontFuture.getY());

        Vector2d vecSmallerAxis = new Vector2d(vecGlobalPedestrianAlphaPoint);
        vecSmallerAxis.sub(vecGlobalPositionOfVehicleBack);
        Vector2d vecBiggerAxis = new Vector2d(vecGlobalPedestrianAlphaPoint);
        vecBiggerAxis.sub(vecGlobalPositionOfVehicleFrontFuture);
        Vector2d vecFoci = new Vector2d(vecGlobalPositionOfVehicleBack);
        vecFoci.sub(vecGlobalPositionOfVehicleFrontFuture);

        /*
        double radiusSmallerAxis = vecSmallerAxis.length();
        double radiusBiggerAxis = vecBiggerAxis.length();
        double radiusFoci = vecFoci.length();*/

        double diffAngle = calculations.getAngleDiffBetweenTwoLinesFacingCharCenter(globalPedestrianAlphaPoint,
                globalPositionOfVehicleFrontFuture, globalPositionOfVehicleBack);
        diffAngle /=2;

        // get unitVector from Angle
        // 2D coordinates, with angles measured counterclockwise from x-axis
        /*https://stackoverflow.com/questions/42490604/getting-a-point-from-begginning-coordinates-angle-and-distance
            x(new) = x(old) + distance*cos(angle)
            y(new) = y(old) + distance*sin(angle)
        */
        Vector2d newPointNom;

        double xVal1 = vecSmallerAxis.getX() + Math.cos(diffAngle);
        double yVal1 = vecSmallerAxis.getY() + Math.sin(diffAngle);
        Vector2d newPointNom1= new Vector2d(xVal1, yVal1);

        double xVal2 = vecSmallerAxis.getX() + Math.cos(diffAngle);
        double yVal2 = vecSmallerAxis.getY() + Math.sin(diffAngle);
        Vector2d newPointNom2 = new Vector2d(xVal2, yVal2);

        double diffAngle1 = calculations.getAngleDiffBetweenTwoLinesFacingCharCenter(globalPedestrianAlphaPoint,
                new PedestrianPoint(newPointNom1.getX(), newPointNom1.getY()), globalPositionOfVehicleBack);
        double diffAngle2 = calculations.getAngleDiffBetweenTwoLinesFacingCharCenter(globalPedestrianAlphaPoint,
                new PedestrianPoint(newPointNom2.getX(), newPointNom2.getY()), globalPositionOfVehicleBack);

        if( Math.abs(diffAngle - diffAngle1) < Math.abs(diffAngle - diffAngle2)) {
            newPointNom = newPointNom1;
        } else {
            newPointNom = newPointNom2;
        }

        // get norm Vec
        PedestrianPoint newPointNomData = new PedestrianPoint(newPointNom.getX(), newPointNom.getY());
        PedestrianPoint intersection = calculations.getLinesIntersectionByCoordinates(globalPositionOfVehicleFrontFuture,
                globalPositionOfVehicleBack, globalPedestrianAlphaPoint, newPointNomData);

        Vector2d normVec = calculations.getVector(globalPedestrianAlphaPoint, intersection);
        normVec = calculations.getUnitVector(normVec);
        return normVec;
    }
}
