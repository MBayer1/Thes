package at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.ICar;
import at.fhv.itm3.s2.roundabout.api.entity.Street;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.entity.RoundaboutCar;
import at.fhv.itm3.s2.roundabout.entity.StreetSection;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;
import java.awt.geom.Point2D;

public class RepulsiveForceAgainstVehicles {
    final private Double Av_RepulsivePotential = 1.29;
    final private Double Bv_RepulsivePotential = 0.96;

    PedestrianPoint globalPositionOfVehicleFront;
    PedestrianPoint globalPositionOfVehicleBack;
    PedestrianPoint globalAimOfVehicle;
    PedestrianPoint globalPositionOfVehicle;

    PedestrianPoint globalPositionOfVehicleFront2;
    PedestrianPoint globalPositionOfVehicleBack2;
    PedestrianPoint globalAimOfVehicle2;
    PedestrianPoint globalPositionOfVehicle2;

    SupportiveCalculations calculations = new SupportiveCalculations();

    public Vector2d getRepulsiveForceAgainstVehicles( RoundaboutSimulationModel model,
                                                      Pedestrian pedestrian) {
        IConsumer section = pedestrian.getCurrentSection().getStreetSection();
        if ( ! (section instanceof PedestrianStreetSection) ) {
            throw new IllegalArgumentException("Street section is not an instance of PedestrianStreetSection");
        }
        PedestrianStreetSection currentSection = (PedestrianStreetSection) section;
        Vector2d sumForce = new Vector2d(0,0);

        if( !currentSection.getVehicleStreetList().isEmpty()){
            // there are vehicle street section connected to it.
            // now it has to be verifies weather there a vehicle in range on them
            for ( Street vehicleStreet : currentSection.getVehicleStreetList() ) {
                if ( vehicleStreet.getFirstCar() != null) {
                    // global position of vehicle and aim
                    ICar car = vehicleStreet.getFirstCar();
                    if( car instanceof RoundaboutCar) {
                        if( vehicleStreet instanceof StreetSection) {
                            getVehicleData((StreetSection)vehicleStreet, (PedestrianStreetSection)section,
                                    (RoundaboutCar) car);

                            // check if it is in range
                            if ( checkPedestrianInRangeFront(model, pedestrian, globalPositionOfVehicleFront, ((RoundaboutCar)car).getLengthInCM()) ){
                                sumForce.add(calculateRepulsiveForceAgainstVehicles( pedestrian,
                                        globalPositionOfVehicleFront, globalPositionOfVehicleBack, (RoundaboutCar) car,
                                        globalPositionOfVehicle, globalAimOfVehicle));
                            }
                        }
                    }
                }

                // check also next street sections as they are after the crossing
                for ( IConsumer nextVehicleStreet : vehicleStreet.getNextStreetConnector().getNextConsumers()) {
                    if (nextVehicleStreet instanceof Street) {
                        ICar car = ((Street) nextVehicleStreet).getLastCar();
                        if (car != null) {
                            getVehicleDataBack(((RoundaboutCar)car));
                            // check if it is in range
                            if (checkPedestrianInRangeBack(model, pedestrian, globalPositionOfVehicleFront2, ((RoundaboutCar)car).getLengthInCM())) {
                                sumForce.add(calculateRepulsiveForceAgainstVehicles(pedestrian,
                                        globalPositionOfVehicleFront2, globalPositionOfVehicleBack2, (RoundaboutCar) car,
                                        globalPositionOfVehicle2, globalAimOfVehicle2));
                            }
                        }
                    }
                }
            }

        }

        if(Double.isNaN(sumForce.getX()) || Double.isNaN(sumForce.getY()) ){
            throw new IllegalStateException("Vector calculation  error: ForceAgainstVehicle.");
        }
        return sumForce;
    }

    void getVehicleDataBack ( RoundaboutCar car ) {
        globalPositionOfVehicleBack2 = new PedestrianPoint(0,0);
        globalPositionOfVehicle2 = new PedestrianPoint(0,0);
        globalPositionOfVehicleFront2 = new PedestrianPoint(0,0);
        globalAimOfVehicle2 = new PedestrianPoint(0,0);

        Vector2d vccGlobalPositionOfVehicle = new Vector2d(globalAimOfVehicle.getX(),  globalAimOfVehicle.getX());
        double carLength = car.getLengthInCM();

        if(!(car.getCurrentSection() instanceof StreetSection)) {
            throw new IllegalStateException("Type miss match");
        }
        double streetPosLength = ((StreetSection)(car.getCurrentSection())).getLength() - car.getRemainingLengthOfCurrentSection();

        Vector2d direction = new Vector2d(globalPositionOfVehicleFront.getX(), globalPositionOfVehicleFront.getY());
        direction.sub(new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY()));
        direction = calculations.getUnitVector(direction);

        Vector2d tmp = new Vector2d(direction);
        tmp.scale(streetPosLength);
        vccGlobalPositionOfVehicle.add(tmp);
        globalPositionOfVehicleFront2.setLocation(vccGlobalPositionOfVehicle.getX(), vccGlobalPositionOfVehicle.getY());

        tmp = new Vector2d(direction);
        tmp.scale(streetPosLength - carLength);
        vccGlobalPositionOfVehicle = new Vector2d(globalAimOfVehicle.getX(),  globalAimOfVehicle.getX());
        vccGlobalPositionOfVehicle.add(tmp);
        globalPositionOfVehicleBack2.setLocation(vccGlobalPositionOfVehicle.getX(), vccGlobalPositionOfVehicle.getY());

        tmp = new Vector2d(direction);
        tmp.scale(car.getRemainingLengthOfCurrentSection());
        vccGlobalPositionOfVehicle = new Vector2d(globalPositionOfVehicleFront2.getX(), globalPositionOfVehicleFront2.getY());
        vccGlobalPositionOfVehicle.add(tmp);
        globalAimOfVehicle2.setLocation(vccGlobalPositionOfVehicle.getX(), vccGlobalPositionOfVehicle.getY());

        Vector2d globalFrontVehicleVec = new Vector2d(globalPositionOfVehicleFront2.getX(), globalPositionOfVehicleFront2.getY());
        Vector2d globalPositionOfVehicleVec = new Vector2d(globalFrontVehicleVec);
        globalPositionOfVehicleVec.sub(new Vector2d(globalPositionOfVehicleBack2.getX(), globalPositionOfVehicleBack2.getY()));
        globalPositionOfVehicleVec.scale(0.5);
        globalFrontVehicleVec.sub(globalPositionOfVehicleVec);
        globalPositionOfVehicle2.setLocation(globalFrontVehicleVec.getX(), globalFrontVehicleVec.getY());
    }

    void getVehicleData(StreetSection vehicleStreet, PedestrianStreetSection section,
                        RoundaboutCar car) {

        globalPositionOfVehicleBack= new PedestrianPoint(0,0);
        globalPositionOfVehicle= new PedestrianPoint(0,0);
        globalPositionOfVehicleFront= new PedestrianPoint(0,0);
        globalAimOfVehicle = new PedestrianPoint(0,0);

        double carLength = car.getLengthInCM();
        double remainingLength = car.getRemainingLengthOfCurrentSection();
        double crossingWidth = vehicleStreet.getPedestrianCrossingWidth();
        boolean carDrivesAlongYAxis = vehicleStreet.checkCarDrivesAlongYAxis();

        double originXGlobal = section.getGlobalCoordinateOfSectionOrigin().getX();
        double originYGlobal = section.getGlobalCoordinateOfSectionOrigin().getY();

        if (carDrivesAlongYAxis) {
            originXGlobal += vehicleStreet.getPedestrianCrossingEntryHigh();
            if( vehicleStreet.getPedestrianCrossingEntryAtBeginning() ) {
                globalAimOfVehicle.setLocation(originXGlobal, originYGlobal + crossingWidth);
                originYGlobal -= remainingLength;
                globalPositionOfVehicleBack.setLocation(originXGlobal, originYGlobal-carLength);
            } else {
                globalAimOfVehicle.setLocation(originXGlobal, originYGlobal );
                originYGlobal += remainingLength + crossingWidth;
                globalPositionOfVehicleBack.setLocation(originXGlobal, originYGlobal+carLength);
            }
        } else {
            originYGlobal += vehicleStreet.getPedestrianCrossingEntryHigh();
            if( vehicleStreet.getPedestrianCrossingEntryAtBeginning() ) {
                globalAimOfVehicle.setLocation(originXGlobal + crossingWidth, originYGlobal );
                originXGlobal -= remainingLength;
                globalPositionOfVehicleBack.setLocation(originXGlobal-carLength, originYGlobal);
            } else {
                globalAimOfVehicle.setLocation(originXGlobal ,originYGlobal );
                originXGlobal += remainingLength + crossingWidth;
                globalPositionOfVehicleBack.setLocation(originXGlobal+carLength, originYGlobal);
            }
        }

        globalPositionOfVehicleFront.setLocation(originXGlobal, originYGlobal);

        Vector2d globalFrontVehicleVec = new Vector2d(globalPositionOfVehicleFront.getX(), globalPositionOfVehicleFront.getY());
        Vector2d globalPositionOfVehicleVec = new Vector2d(globalFrontVehicleVec);
        globalPositionOfVehicleVec.sub(new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY()));
        globalPositionOfVehicleVec.scale(0.5);
        globalFrontVehicleVec.sub(globalPositionOfVehicleVec);
        globalPositionOfVehicle.setLocation(globalFrontVehicleVec.getX(), globalFrontVehicleVec.getY());
    }

    boolean checkPedestrianInRangeFront( RoundaboutSimulationModel model,
                                         Pedestrian pedestrian,
                                         PedestrianPoint globalPositionOfVehicle,
                                         double vehicleLength){
        if ( calculations.val1LowerOrAlmostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
                pedestrian.getCurrentGlobalPosition().getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                model.pedestrianFieldOfViewRadius + vehicleLength/2)) {
            return true;
        }
        return false;
    }

    boolean checkPedestrianInRangeBack( RoundaboutSimulationModel model,
                                        Pedestrian pedestrian,
                                        PedestrianPoint globalPositionOfVehicle,
                                        double vehicleLength){
        if ( calculations.val1LowerOrAlmostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
                pedestrian.getCurrentGlobalPosition().getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                (model.pedestrianFieldOfViewRadius/2)+vehicleLength/2)) {
            return true;
        }
        return false;
    }

    public Vector2d calculateRepulsiveForceAgainstVehicles(Pedestrian pedestrian, PedestrianPoint globalPositionOfVehicleFront,
                                                           PedestrianPoint globalPositionOfVehicleBack,
                                                           RoundaboutCar car,
                                                           PedestrianPoint globalPositionOfVehicle, PedestrianPoint globalAimOfVehicle) {
        Vector2d vecVehicleFront = new Vector2d(globalPositionOfVehicleFront.getX(), globalPositionOfVehicleFront.getY());
        Vector2d vecVehicleBack = new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY());
        Vector2d vecVehicleGlobPos = new Vector2d(globalPositionOfVehicle.getX(), globalPositionOfVehicle.getY());
        Vector2d vecVehicleGlobGoal = new Vector2d(globalAimOfVehicle.getX(), globalAimOfVehicle.getY());
        Vector2d personPos = new Vector2d(pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());

        //Traveled path of the walker β within ∆t
        double speed = car.getDriverBehaviour().getSpeed();
        double kmhToMs = 1000./(60*60);
        double traveledPathWithinTOfBeta = speed / kmhToMs; // speed in km/h -> change to m/s := 1000/(60*60)

        // preparation
        Vector2d vecVehicleDrivingDirection = new Vector2d(vecVehicleGlobGoal);
        vecVehicleDrivingDirection.sub(vecVehicleGlobPos);
        vecVehicleDrivingDirection = calculations.getUnitVector(vecVehicleDrivingDirection);
        vecVehicleDrivingDirection.scale(traveledPathWithinTOfBeta);

        if( Double.isNaN(vecVehicleDrivingDirection.length())) {
            throw new IllegalStateException("Something wrong in Force against vehicle calculation.");
        }

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
        // calc
        b /= 2;

        // exponent (-B*b)
        double exponent = Bv_RepulsivePotential * (-1) * b;
        exponent = Math.exp(exponent);

        // n_Vector
        Vector2d n_vec = getNormVexAlongTangentOfEllipse(pedestrian.getCurrentGlobalPosition(), vehicleFrontFuture, globalPositionOfVehicleBack);

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
