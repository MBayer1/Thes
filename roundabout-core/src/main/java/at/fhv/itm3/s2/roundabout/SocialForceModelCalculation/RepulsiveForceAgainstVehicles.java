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
                            PedestrianPoint globalPositionOfVehicle = null;
                            PedestrianPoint globalAimOfVehicle = null;

                            getVehicleData(globalPositionOfVehicle, globalAimOfVehicle,
                                    (StreetSection)vehicleStreet, (PedestrianStreetSection)section,
                                    (RoundaboutCar) car);

                            // check if it is in range
                            if ( checkPedestrianInRangeFront(model, pedestrian, globalPositionOfVehicle) ){
                                sumForce.add(calculateRepulsiveForceAgainstVehicles( pedestrian, globalPositionOfVehicle, globalAimOfVehicle));
                            }

                            // check also next street sections as they are after the crossing
                            for ( IConsumer nextVehicleStreet : vehicleStreet.getNextStreetConnector().getNextConsumers()) {
                                if (nextVehicleStreet instanceof Street) {
                                    if (((Street) nextVehicleStreet).getLastCar() != null) {

                                        getVehicleData(globalPositionOfVehicle, globalAimOfVehicle,
                                                (StreetSection)nextVehicleStreet, (PedestrianStreetSection)section,
                                                (RoundaboutCar) vehicleStreet.getLastCar());

                                        // check if it is in range
                                        if (checkPedestrianInRangeBack(model, pedestrian, globalPositionOfVehicle)) {
                                            sumForce.add(calculateRepulsiveForceAgainstVehicles(pedestrian, globalPositionOfVehicle, globalAimOfVehicle));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        sumForce = calculations.getUnitVector(sumForce); // the distance is defined  by traveled speed and aim.
        return sumForce;
    }

    void getVehicleData(PedestrianPoint globalPositionOfVehicle, PedestrianPoint globalAimOfVehicle,
                        StreetSection vehicleStreet, PedestrianStreetSection section,
                        RoundaboutCar car) {
        globalPositionOfVehicle = new PedestrianPoint();
        globalAimOfVehicle = new PedestrianPoint();

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
        globalPositionOfVehicle.setLocation(tmpX, tmpY);

        // global destination of vehicle
        if ( vehicleStreet.getPedestrianCrossingEntryAtBeginning() ) {
            tmpX -= (int) Math.round(section.getLengthX() + car.getLength());
        } else {
            tmpX += (int) Math.round(section.getLengthX() + car.getLength());
        }
        globalAimOfVehicle.setLocation(tmpX, tmpY);
    }

    boolean checkPedestrianInRangeFront( RoundaboutSimulationModel model, Pedestrian pedestrian, PedestrianPoint globalPositionOfVehicle){
        if ( calculations.almostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
                pedestrian.getCurrentGlobalPosition().getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                model.pedestrianFieldOfViewRadius)) {
            return true;
        }
        return false;
    }

    boolean checkPedestrianInRangeBack( RoundaboutSimulationModel model, Pedestrian pedestrian, PedestrianPoint globalPositionOfVehicle){
        if ( calculations.almostEqual( Point2D.distance(   pedestrian.getCurrentGlobalPosition().getX(),
                pedestrian.getCurrentGlobalPosition().getY(),
                globalPositionOfVehicle.getX(),
                globalPositionOfVehicle.getY()),
                model.pedestrianFieldOfViewRadius/2)) {
            return true;
        }
        return false;
    }

    public Vector2d calculateRepulsiveForceAgainstVehicles(Pedestrian pedestrian, PedestrianPoint globalPositionOfVehicle, PedestrianPoint globalAimOfVehicle) {
        Vector2d vecPosOfVehicle = new Vector2d(globalPositionOfVehicle.getX(), globalPositionOfVehicle.getY());

        //vectorBetween both components
        Vector2d vectorBetweenBothPedestrian = calculations.getVector(
                pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY(),
                globalPositionOfVehicle.getX(), globalPositionOfVehicle.getY());

        //preferred direction of Vehicle
        Vector2d vecNextAimVehicle = new Vector2d(globalAimOfVehicle.getX(), globalAimOfVehicle.getY());

        Vector2d preferredDirectionOfVehicle = vecPosOfVehicle;
        preferredDirectionOfVehicle.sub(vecNextAimVehicle);       // t is in the estimated future. when reaching destination (expected)
        Double nextAimOfVehicleLength = preferredDirectionOfVehicle.length();
        preferredDirectionOfVehicle.scale(1 / nextAimOfVehicleLength);

        //Traveled path of the walker β within ∆t
        Double traveledPathWithinTOfBeta = nextAimOfVehicleLength;

        //small half axis of the ellipse
        Vector2d vehicleData = preferredDirectionOfVehicle;
        vehicleData.scale(traveledPathWithinTOfBeta);
        Vector2d nextDestinationVectorAlphaSubTravelPathBeta = vectorBetweenBothPedestrian;
        nextDestinationVectorAlphaSubTravelPathBeta.sub(vehicleData);

        Double smallHalfAxisOfEllipse = Math.sqrt((Math.pow(vectorBetweenBothPedestrian.length() + nextDestinationVectorAlphaSubTravelPathBeta.length(), 2)) -
                Math.pow(traveledPathWithinTOfBeta, 2));

        // Repulsive force against other pedestrians
        // V_alphaBeta(t0)* e^(-b/sigma)
        Double exponent = smallHalfAxisOfEllipse / -2;  // is 2b --> and we need b
        exponent /= sigma;
        exponent = Math.exp(exponent);
        Double repulsiveForce = VAlphaBeta * exponent;

        vectorBetweenBothPedestrian.scale(repulsiveForce);
        vectorBetweenBothPedestrian.negate();

        return vectorBetweenBothPedestrian;
    }
}
