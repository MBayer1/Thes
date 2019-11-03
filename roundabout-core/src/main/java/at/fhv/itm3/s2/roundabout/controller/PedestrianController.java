package at.fhv.itm3.s2.roundabout.controller;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrian;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrian;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class PedestrianController {

    private final static Map<Car, IPedestrian> carToIPedestrianMap  = new HashMap<>();
    private final static Map<IPedestrian, Car> iPedestrianToCarMap = new HashMap<>();
    private final static Map<IConsumer, List<IPedestrian>> lostCarsMap = new HashMap<>();
    private final static List<IPedestrian> IPedestrians = new LinkedList<>();

    private PedestrianController() {
    }

    public static void addCarMapping(Car car, IPedestrian IPedestrian) {
        IPedestrians.add(IPedestrian);
        carToIPedestrianMap.put(car, IPedestrian);
        iPedestrianToCarMap.put(IPedestrian, car);
    }

    public static void removeCarMapping(Car car) {
        final IPedestrian IPedestrian = carToIPedestrianMap.get(car);
        carToIPedestrianMap.remove(car);
        iPedestrianToCarMap.remove(IPedestrian);
        IPedestrians.remove(IPedestrian);
    }

    public static void removeCarMapping(IPedestrian iPedestrian) {
        final Car car = iPedestrianToCarMap.get(iPedestrian);
        iPedestrianToCarMap.remove(iPedestrian);
        carToIPedestrianMap.remove(car);
        IPedestrians.remove(iPedestrian);
    }

    public static IPedestrian getIPedestrian(Car car) {
        if (!carToIPedestrianMap.containsKey(car)) {
            throw new IllegalArgumentException("carToIPedestrianMap does not contain car");
        }

        return carToIPedestrianMap.get(car);
    }

    public static Car getCar(IPedestrian iPedestrian) {
        if (!iPedestrianToCarMap.containsKey(iPedestrian)) {
            throw new IllegalArgumentException("IPedestrianToCarMap does not contain car");
        }

        return iPedestrianToCarMap.get(iPedestrian);
    }

    public static void addLostCar(IConsumer consumer, IPedestrian car) {
        if (!lostCarsMap.containsKey(consumer)) {
            lostCarsMap.put(consumer, new LinkedList<>());
        }
        lostCarsMap.get(consumer).add(car);
    }

    public static int getNrOfPedestrians() {
        return IPedestrians.size();
    }

    public static void clear() {
        carToIPedestrianMap.clear();
        iPedestrianToCarMap.clear();
        lostCarsMap.clear();
        IPedestrians.clear();
    }

    public static List<IPedestrian> getIPedestrians() {
        return IPedestrians;
    }
}
