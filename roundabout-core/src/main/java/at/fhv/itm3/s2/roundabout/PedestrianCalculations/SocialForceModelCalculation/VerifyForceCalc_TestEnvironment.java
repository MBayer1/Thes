package at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Point2D;

public class VerifyForceCalc_TestEnvironment {

    SupportiveCalculations calculations = new SupportiveCalculations();
    final private Double R = 20.0; // cm
    final private Double U0AlphaBeta = 1000.0; // (cm/s)^2
    Double pedestrianFieldOfViewRadius = 800.0;

    public VerifyForceCalc_TestEnvironment() {
        getAccelerationForceToTarget();
    }


    public void getAccelerationForceToTarget(){
        //Vector2d accelerationForce = ownVersionAccelerationForce();
        //Vector2d forceAgainstPedestrian = getForceAgainstPedestrian();
        //Vector2d forceAgainstWallsObsticals = getForceAgainstWallsObstacles();
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
        double lengthBetweenPolandGoal = preferredSpeedValue;

        preferredSpeedVector.scale(1/preferredSpeedValue);
        preferredSpeedVector.scale(/*pedestrian.*/ownVersionAccelerationForce_calculatePreferredSpeed()); //v_alpha * e_alpha(t)

        preferredSpeedVector.sub(currentSpeedVector);
        preferredSpeedVector.scale(1/2.2); //model.getRandomPedestrianRelaxingTimeTauAlpha());

        double tmp = preferredSpeedVector.length();
        if (lengthBetweenPolandGoal < tmp){
            String da = "error";
        }

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
        Vector2d betaGlobalPos = new Vector2d(0,20);
        Vector2d alphaGlobalPos = new Vector2d(0,0);

        Vector2d betaGoal = new Vector2d(50,50);
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
        Double traveledPathWithinTOfBeta = 1.38889;

        //small half axis of the ellipse
        Vector2d nextDestinationVectorAlphaSubTravelPathBeta = new Vector2d(vectorBetweenBothPedestrian);
        Vector2d betaData = new Vector2d(preferredDirectionOfBeta);
        betaData.scale(traveledPathWithinTOfBeta);
        nextDestinationVectorAlphaSubTravelPathBeta.sub(betaData);

        Double smallHalfAxisOfEllipse = Math.sqrt( Math.pow(vectorBetweenBothPedestrian.length()
                + nextDestinationVectorAlphaSubTravelPathBeta.length(),2)) -
                Math.pow(traveledPathWithinTOfBeta,2);

        // Repulsive force against other pedestrians
        // V_alphaBeta(t0)* e^(-b/sigma)
        Double exponent = smallHalfAxisOfEllipse/(-2);  // is 2b --> and we need b
        exponent /= sigma;
        exponent = Math.exp(exponent);
        Double repulsiveForce = VAlphaBeta * exponent;

        vectorBetweenBothPedestrian = calculations.getUnitVector(vectorBetweenBothPedestrian);
        vectorBetweenBothPedestrian.scale(repulsiveForce);
        //vectorBetweenBothPedestrian.negate();
        return vectorBetweenBothPedestrian;
    }

    public Vector2d getForceAgainstWallsObstacles(){
        PedestrianPoint pedestrianPos = new PedestrianPoint(1400,500);
        PedestrianPoint pedestrianGoal = new PedestrianPoint(1500,500);
        //Rectangle with 1500*1000
        PedestrianPoint wallIntersection1 = new PedestrianPoint(0, 500);
        PedestrianPoint wallIntersection2 = new PedestrianPoint(1500,500);
        PedestrianPoint wallIntersection3 = new PedestrianPoint(1200,0);
        PedestrianPoint wallIntersection4 = new PedestrianPoint(1200,1000);

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
        Vector2d force = calculations.getUnitVector(vectorBetweenPedestrianAndObstacle);

        force.scale(U0AlphaBeta * expo);
        //force.negate();
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
        PedestrianPoint pedestrianGlobPos = new PedestrianPoint(600,900);
        PedestrianPoint VehicleFront = new PedestrianPoint(0,600);
        double vehicleLength = 400; //cm
        PedestrianPoint VehicleBack = new PedestrianPoint(0,VehicleFront.getY()+vehicleLength);
        PedestrianPoint globalPositionOfVehicle = new PedestrianPoint(0,VehicleFront.getY()+((VehicleBack.getY()-VehicleFront.getY())/2));
        PedestrianPoint globalAimOfVehicle = new PedestrianPoint(0,-1000);


        // check if it is in range
        if ( checkPedestrianInRangeFront(pedestrianGlobPos, VehicleFront, vehicleLength) ){
            sumForce.add(calculateRepulsiveForceAgainstVehicles( pedestrianGlobPos, globalPositionOfVehicle, globalAimOfVehicle,
                    VehicleFront, VehicleBack));
        }

        // check if it is in range
        if (checkPedestrianInRangeBack(pedestrianGlobPos, VehicleBack, vehicleLength)) {
            sumForce.add(calculateRepulsiveForceAgainstVehicles(pedestrianGlobPos, globalPositionOfVehicle, globalAimOfVehicle,
                    VehicleFront, VehicleBack));
        }

        return sumForce;
    }

    boolean checkPedestrianInRangeFront(PedestrianPoint pedestriancurGlobPos, PedestrianPoint globalPositionOfVehicle, double vehicleLength){
        if ( calculations.val1LowerOrAlmostEqual( Point2D.distance(   pedestriancurGlobPos.getX(),
                pedestriancurGlobPos.getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                pedestrianFieldOfViewRadius + vehicleLength/2)) {
            return true;
        }
        return false;
    }

    boolean checkPedestrianInRangeBack( PedestrianPoint pedestriancurGlobPos, PedestrianPoint globalPositionOfVehicle, double vehicleLength){
        if ( calculations.val1LowerOrAlmostEqual( Point2D.distance(   pedestriancurGlobPos.getX(),
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
                                                           PedestrianPoint globalAimOfVehicle,
                                                           PedestrianPoint globalPositionOfVehicleFront,
                                                           PedestrianPoint globalPositionOfVehicleBack
                                                           ) {

        final Double Av_RepulsivePotential = 1.29;
        final Double Bv_RepulsivePotential = 0.96;

        double carSpeed = 50.; //km/h
        Vector2d vecVehicleFront = new Vector2d(globalPositionOfVehicleFront.getX(), globalPositionOfVehicleFront.getY());
        Vector2d vecVehicleBack = new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY());
        Vector2d vecVehicleGlobPos = new Vector2d(globalPositionOfVehicle.getX(), globalPositionOfVehicle.getY());
        Vector2d vecVehicleGlobGoal = new Vector2d(globalAimOfVehicle.getX(), globalAimOfVehicle.getY());
        Vector2d personPos = new Vector2d(pedestrianCurGlobPos.getX(), pedestrianCurGlobPos.getY());

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = 20.;//carSpeed*1000/(60*60); // speed in km/h -> change to m/s := 1000/(60*60)

        // preparation
       Vector2d vecVehicleDrivingDirection = new Vector2d(vecVehicleGlobGoal);
       vecVehicleDrivingDirection.sub(vecVehicleGlobPos);
       vecVehicleDrivingDirection = calculations.getUnitVector(vecVehicleDrivingDirection);
       vecVehicleDrivingDirection.scale(traveledPathWithinTOfBeta);

        Vector2d vecVehicleFrontFuture = new Vector2d(vecVehicleFront);
        vecVehicleFrontFuture.add(vecVehicleDrivingDirection);
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
        b /= 2;

        // exponent (-B*b)
        double exponent = Bv_RepulsivePotential * (-1) * b;
        exponent = Math.exp(exponent);

        // n_Vector
        Vector2d n_vec = getNormVexAlongTangentOfEllipse(pedestrianCurGlobPos, vehicleFrontFuture, globalPositionOfVehicleBack);

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

        Vector2d vecGlobalPedestrianAlphaPoint = new Vector2d(globalPedestrianAlphaPoint.getX(), globalPedestrianAlphaPoint.getY());// globalPedestrianAlphaPoint.getY());
        new Vector2d(globalPedestrianAlphaPoint.getX(), globalPedestrianAlphaPoint.getY());
        Vector2d vecGlobalPositionOfVehicleBack = new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY());
        Vector2d vecGlobalPositionOfVehicleFrontFuture = new Vector2d(globalPositionOfVehicleFrontFuture.getX(), globalPositionOfVehicleFrontFuture.getY());

        boolean vehicleLengthAlongX = calculations.almostEqual(vecGlobalPositionOfVehicleBack.getY(), vecGlobalPositionOfVehicleFrontFuture.getY());

        Vector2d vecVehicleBackToPedestrianPoint = new Vector2d(vecGlobalPositionOfVehicleBack);
        vecVehicleBackToPedestrianPoint.sub(vecGlobalPedestrianAlphaPoint);
        Vector2d vecVehicleFutureFrontToPedestrianPoint = new Vector2d(vecGlobalPositionOfVehicleFrontFuture);
        vecVehicleFutureFrontToPedestrianPoint.sub(vecGlobalPedestrianAlphaPoint);
        Vector2d vecFoci = new Vector2d(vecGlobalPositionOfVehicleFrontFuture);
        vecFoci.sub(vecGlobalPositionOfVehicleBack);

        Vector2d vecFociForShift = calculations.getUnitVector(vecFoci.getX(), vecFoci.getY());
        double distnaceToCenter = (vecFoci.length()/2);
        vecFociForShift.scale(distnaceToCenter);

        PedestrianPoint center = new PedestrianPoint(
                vecGlobalPositionOfVehicleBack.getX() + vecFociForShift.getX(),
                vecGlobalPositionOfVehicleBack.getY() + vecFociForShift.getY());
        /*
        Major radi:
        (Length from Any point along Ellipse to both foci - foci Length) /2 = from Focipoint to Bigger radius x point - foci Length
        Minor radii:
        pythagoras:  hypothenuse = (Length from Any point along Ellipse to both foci/2);
                     a = Focilenth
                     b = minor radii
        */
        double lengthFromFociToPoints = vecVehicleBackToPedestrianPoint.length() + vecVehicleFutureFrontToPedestrianPoint.length();
        double fociLenth = vecFoci.length()/2;

        double majorRadiiLength = ((lengthFromFociToPoints - fociLenth)/2) + fociLenth;
        double minorRadiiLength = Math.pow((lengthFromFociToPoints / 2),2);
        minorRadiiLength -= Math.pow(fociLenth, 2);
        minorRadiiLength = Math.sqrt(minorRadiiLength);

        if(majorRadiiLength <  minorRadiiLength) {
            throw new IllegalStateException("something wrong in Force against Vehicle Calculation.");
        }

        //when bigger radius of ellipse is along x axis then
        //xAxisIntersectionOfTangent => x = a^2/x_ellipse
        double xValue;

        //Shift Ellipse to center
        if(!vehicleLengthAlongX) {
           // swap axis
            xValue = vecGlobalPedestrianAlphaPoint.getY() - center.getY();
        } else {
            xValue = vecGlobalPedestrianAlphaPoint.getX() - center.getX();
        }

        //get Y value of Point along circle with the radii of major radii of ellipse and x value of pedestrian pos.
        // pythagoras: hypotenuse = major radii; a = x
        double valueA = Math.pow(majorRadiiLength,2);
        valueA -= Math.pow(xValue,2);
        valueA = Math.sqrt(valueA);

        double xAxisIntersectionOfTangent = Math.pow(valueA,2)/ xValue;
        Vector2d tangentVec;

        if (!calculations.almostEqual(xValue,0)) {
            if (Double.isNaN(xAxisIntersectionOfTangent) || Double.isInfinite(xAxisIntersectionOfTangent)) {
                throw new IllegalStateException("something wrong in Force against Vehicle Calculation.");
            }
            if (!vehicleLengthAlongX) {
                tangentVec = new Vector2d(0, xAxisIntersectionOfTangent);
            } else {
                tangentVec = new Vector2d(xAxisIntersectionOfTangent, 0);
            }
            tangentVec.sub(vecGlobalPedestrianAlphaPoint);

            // Turn Vec
            tangentVec = calculations.getUnitNormalVector(tangentVec);
        } else {
            tangentVec = new Vector2d(1,0);
        }

        // check vec is directed toward center
        Vector2d test1 = new Vector2d(vecGlobalPositionOfVehicleFrontFuture);
        test1.sub(vecGlobalPedestrianAlphaPoint);
        test1.add(tangentVec);
        Vector2d test2 = new Vector2d(vecGlobalPositionOfVehicleFrontFuture);
        test2.sub(vecGlobalPedestrianAlphaPoint);
        test2.sub(tangentVec);

        if(test1.length() > test2.length()) {
            tangentVec.scale(-1);
        }
        return tangentVec;
    }

}
