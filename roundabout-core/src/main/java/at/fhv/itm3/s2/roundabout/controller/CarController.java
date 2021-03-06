package at.fhv.itm3.s2.roundabout.controller;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.entity.ICar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class CarController {

    private final static Map<Car, ICar> carToICarMap = new HashMap<>();
    private final static Map<ICar, Car> iCarToCarMap = new HashMap<>();
    private final static Map<IConsumer, List<ICar>> lostCarsMap = new HashMap<>();
    private final static List<ICar> iCars = new LinkedList<>();

    private CarController() {
    }

    public static void addCarMapping(Car car, ICar iCar) {
        iCars.add(iCar);
        carToICarMap.put(car, iCar);
        iCarToCarMap.put(iCar, car);
    }

    public static void removeCarMapping(Car car) {
        final ICar iCar = carToICarMap.get(car);
        carToICarMap.remove(car);
        iCarToCarMap.remove(iCar);
        iCars.remove(iCar);
    }

    public static void removeCarMapping(ICar iCar) {
        final Car car = iCarToCarMap.get(iCar);
        iCarToCarMap.remove(iCar);
        carToICarMap.remove(car);
        iCars.remove(iCar);
    }

    public static ICar getICar(Car car) {
        if (!carToICarMap.containsKey(car)) {
            throw new IllegalArgumentException("carToICarMap does not contain car");
        }

        return carToICarMap.get(car);
    }

    public static Car getCar(ICar car) {
        if (!iCarToCarMap.containsKey(car)) {
            throw new IllegalArgumentException("iCarToCarMap does not contain car");
        }

        return iCarToCarMap.get(car);
    }

    public static void addLostCar(IConsumer consumer, ICar car) {
        if (!lostCarsMap.containsKey(consumer)) {
            lostCarsMap.put(consumer, new LinkedList<>());
        }
        lostCarsMap.get(consumer).add(car);
    }

    public static int getNrOfCars() {
        return iCars.size();
    }

    public static void clear() {
        carToICarMap.clear();
        iCarToCarMap.clear();
        lostCarsMap.clear();
        iCars.clear();
    }

    public static List<ICar> getICars() {
        return iCars;
    }
}
