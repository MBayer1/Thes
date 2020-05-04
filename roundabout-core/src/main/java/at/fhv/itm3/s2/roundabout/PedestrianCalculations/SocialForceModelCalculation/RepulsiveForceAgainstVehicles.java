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
                PedestrianPoint globalPositionOfVehicleFront = null;
                PedestrianPoint globalPositionOfVehicleBack = null;
                PedestrianPoint globalAimOfVehicle = null;

                if ( vehicleStreet.getFirstCar() != null) {
                    // global position of vehicle and aim
                    ICar car = vehicleStreet.getFirstCar();
                    if( car instanceof RoundaboutCar) {
                        if( vehicleStreet instanceof StreetSection) {
                            getVehicleData(globalPositionOfVehicleFront, globalPositionOfVehicleBack,
                                    globalAimOfVehicle,
                                    (StreetSection)vehicleStreet, (PedestrianStreetSection)section,
                                    (RoundaboutCar) car);

                            // check if it is in range
                            if ( checkPedestrianInRangeFront(model, pedestrian, globalPositionOfVehicleFront, ((RoundaboutCar)car).getLengthInCM()) ){
                                sumForce.add(calculateRepulsiveForceAgainstVehicles( pedestrian,
                                        globalPositionOfVehicleFront, globalPositionOfVehicleBack, (RoundaboutCar) car));
                            }
                        }
                    }
                }

                // check also next street sections as they are after the crossing
                for ( IConsumer nextVehicleStreet : vehicleStreet.getNextStreetConnector().getNextConsumers()) {
                    if (nextVehicleStreet instanceof Street) {
                        ICar car = ((Street) nextVehicleStreet).getLastCar();
                        if (car != null) {
                            getVehicleData(globalPositionOfVehicleFront, globalPositionOfVehicleBack,
                                    globalAimOfVehicle,
                                    (StreetSection)nextVehicleStreet, (PedestrianStreetSection)section,
                                    (RoundaboutCar) car);

                            // check if it is in range
                            if (checkPedestrianInRangeBack(model, pedestrian, globalPositionOfVehicleFront, ((RoundaboutCar)car).getLengthInCM())) {
                                sumForce.add(calculateRepulsiveForceAgainstVehicles(pedestrian,
                                        globalPositionOfVehicleFront, globalPositionOfVehicleBack, (RoundaboutCar) car));
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

    void getVehicleData(PedestrianPoint globalPositionOfVehicleFront,
                        PedestrianPoint globalPositionOfVehicleBack,
                        PedestrianPoint globalAimOfVehicle,
                        StreetSection vehicleStreet, PedestrianStreetSection section,
                        RoundaboutCar car) {

        double remainingLength = car.getRemainingLengthOfCurrentSection();
        double crossingWidth = vehicleStreet.getPedestrianCrossingWidth();
        boolean carDrivesAlongYAxis = vehicleStreet.checkCarDrivesAlongYAxis();
        double tmpX = section.getGlobalCoordinateOfSectionOrigin().getX();
        double tmpY = section.getGlobalCoordinateOfSectionOrigin().getY();



        tmpY += vehicleStreet.getPedestrianCrossingEntryHigh();

        if( vehicleStreet.getPedestrianCrossingEntryAtBeginning() ) {
            tmpX -= remainingLength;
        } else {
            tmpX += remainingLength + crossingWidth;
        }
        globalPositionOfVehicleFront.setLocation(tmpX, tmpY);

        // global destination of vehicle
        if ( vehicleStreet.getPedestrianCrossingEntryAtBeginning() ) {
            tmpX -= (int) Math.round(section.getLengthX() + car.getLength());
            //globalPositionOfVehicleBack.setLocation(tmpX, );
        } else {
            tmpX += (int) Math.round(section.getLengthX() + car.getLength());
        }
        globalAimOfVehicle.setLocation(tmpX, tmpY);
    }

    boolean checkPedestrianInRangeFront( RoundaboutSimulationModel model, Pedestrian pedestrian, PedestrianPoint globalPositionOfVehicle, double vehicleLength){
        if ( calculations.almostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
                pedestrian.getCurrentGlobalPosition().getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                model.pedestrianFieldOfViewRadius + vehicleLength/2)) {
            return true;
        }
        return false;
    }

    boolean checkPedestrianInRangeBack( RoundaboutSimulationModel model, Pedestrian pedestrian, PedestrianPoint globalPositionOfVehicle, double vehicleLength){
        if ( calculations.almostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
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
                                                           RoundaboutCar car) {
        Vector2d vecVehicleFront = new Vector2d(globalPositionOfVehicleFront.getX(), globalPositionOfVehicleFront.getY());
        Vector2d vecVehicleBack = new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY());
        Vector2d personPos = new Vector2d(pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = car.getDriverBehaviour().getSpeed() / 3.6; // speed in km/h -> change to m/s := 1000/(60*60)

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
