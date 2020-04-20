package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

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
import java.awt.*;
import java.awt.geom.Point2D;

public class RepulsiveForceAgainstVehicles {
    final private Double sigma = 30.0; // in centimeter
    final private Double VAlphaBeta = 210.0; // (cm / s)^2
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
                if ( vehicleStreet.getFirstCar() != null) {
                    // global position of vehicle and aim
                    ICar car = vehicleStreet.getFirstCar();
                    if( car instanceof RoundaboutCar) {
                        if( vehicleStreet instanceof StreetSection) {
                            PedestrianPoint globalPositionOfVehicleFront = null;
                            PedestrianPoint globalPositionOfVehicleBack = null;
                            PedestrianPoint globalAimOfVehicle = null;

                            getVehicleData(globalPositionOfVehicleFront, globalPositionOfVehicleBack,
                                    globalAimOfVehicle,
                                    (StreetSection)vehicleStreet, (PedestrianStreetSection)section,
                                    (RoundaboutCar) car);

                            // check if it is in range
                            if ( checkPedestrianInRangeFront(model, pedestrian, globalPositionOfVehicleFront, car.getLength()) ){
                                sumForce.add(calculateRepulsiveForceAgainstVehicles( pedestrian,
                                        globalPositionOfVehicleFront, globalPositionOfVehicleBack, globalAimOfVehicle, (RoundaboutCar) car));
                            }

                            // check also next street sections as they are after the crossing
                            for ( IConsumer nextVehicleStreet : vehicleStreet.getNextStreetConnector().getNextConsumers()) {
                                if (nextVehicleStreet instanceof Street) {
                                    if (((Street) nextVehicleStreet).getLastCar() != null) {

                                        getVehicleData(globalPositionOfVehicleFront, globalPositionOfVehicleBack,
                                                globalAimOfVehicle,
                                                (StreetSection)nextVehicleStreet, (PedestrianStreetSection)section,
                                                (RoundaboutCar) vehicleStreet.getLastCar());

                                        // check if it is in range
                                        if (checkPedestrianInRangeBack(model, pedestrian, globalPositionOfVehicleFront, car.getLength())) {
                                            sumForce.add(calculateRepulsiveForceAgainstVehicles(pedestrian,
                                                    globalPositionOfVehicleFront, globalPositionOfVehicleBack, globalAimOfVehicle, (RoundaboutCar) car));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        return sumForce;
    }

    void getVehicleData(PedestrianPoint globalPositionOfVehicleFront,
                        PedestrianPoint globalPositionOfVehicleBack,
                        PedestrianPoint globalAimOfVehicle,
                        StreetSection vehicleStreet, PedestrianStreetSection section,
                        RoundaboutCar car) {

        double remainingLength = car.getRemainingLengthOfCurrentSection();
        //global position of vehicle
        int crossingWidth = (int)Math.round(vehicleStreet.getPedestrianCrossingWidth());

        int tmpX = (int)section.getGlobalCoordinateOfSectionOrigin().getX();
        int tmpY = (int)section.getGlobalCoordinateOfSectionOrigin().getY();

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
                                                           PedestrianPoint globalAimOfVehicle, RoundaboutCar car) {
        Vector2d vecVehicleFront = new Vector2d(globalPositionOfVehicleFront.getX(), globalPositionOfVehicleFront.getY());
        Vector2d vecVehicleBack = new Vector2d(globalPositionOfVehicleBack.getX(), globalPositionOfVehicleBack.getY());
        Vector2d personPos = new Vector2d(pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());
        Vector2d vecVehicleFrontAim = new Vector2d(globalAimOfVehicle.getX(), globalAimOfVehicle.getY());

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = car.getDriverBehaviour().getSpeed() / 3.6; // speed in km/h -> change to m/s := 1000/(60*60)

        // preparation
        Vector2d vecVehiclePosToAim = calculations.getVector(vecVehicleFront, vecVehicleFrontAim);
        vecVehiclePosToAim.scale(traveledPathWithinTOfBeta);
        vecVehicleBack.add(vecVehiclePosToAim);
        vecVehicleFront.add(vecVehiclePosToAim);

        // calc 2b //small half axis of the ellipse
        Vector2d part1 = new Vector2d(personPos);
        part1.sub(vecVehicleBack);
        Vector2d part2 = new Vector2d(personPos);
        part2.sub(vecVehicleFront);
        Vector2d part3 = new Vector2d(vecVehicleBack);
        part3.sub(vecVehicleFront);

        double b = Math.pow((part1.length()+part2.length()),2) - Math.pow(part3.length(),2);
        b = Math.sqrt(b);
        // calc
        b /= 2;

        // exponent (-B*b)
        double exponent = Bv_RepulsivePotential * (-1) * b;
        exponent = Math.exp(exponent);

        // n_Vector
        Vector2d n_vec = new Vector2d(0,0);




        //A*expo(-B*b)*n
        n_vec.scale(exponent*Av_RepulsivePotential);

        return n_vec;
    }

    private PedestrianPoint getNormVecAlongTangentOfElipse(){
        //http://www.nabla.hr/Z_MemoHU-029.htm



        return null;
    }
}
