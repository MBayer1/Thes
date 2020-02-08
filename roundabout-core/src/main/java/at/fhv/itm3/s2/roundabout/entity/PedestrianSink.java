package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.CarController;
import at.fhv.itm3.s2.roundabout.controller.PedestrianController;
import desmoj.core.simulator.Model;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PedestrianSink extends PedestrianAbstractSink {

    private IPedestrianStreetConnector previousStreetConnector;

    private double meanPedestrianCrossingTime;
    private double meanTimeSpentInSystem;
    private double meanWaitingTimePerStop;
    private double meanStopCount;
    private double meanPedestrianAreaTime;


    public PedestrianSink(Model owner, String name, boolean showInTrace) {
        this(UUID.randomUUID().toString(), owner, name, showInTrace);
    }

    public PedestrianSink(String id, Model owner, String name, boolean showInTrace) {
        super(id, owner, name, showInTrace);

        this.meanPedestrianCrossingTime = 0;
        this.meanTimeSpentInSystem = 0;
        this.meanWaitingTimePerStop = 0;
        this.meanStopCount = 0;
        this.meanPedestrianAreaTime = 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double getLengthX() {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double getLengthY() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianConsumerType getPedestrianConsumerType() { return PedestrianConsumerType.PEDESTRIAN_SINK;}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPedestrianCrossing() { return false;}



    public void addPedestrian(IPedestrian iPedestrian){
        addPedestrian(iPedestrian, new Point(0,0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPedestrian(IPedestrian iPedestrian, Point position) {

        iPedestrian.leaveSystem();
        iPedestrian.leavePedestrianArea();
        iPedestrian.leavePedestrianCrossing();

        incrementEnteredPedestrianCounter();

        updateStats(iPedestrian);

        // needed for adaption to the trafsim framework
        addCar(CarController.getICar(PedestrianController.getCar(iPedestrian)));


        pedestrianObserver.notifyObservers(iPedestrian);
        incrementLeftPedestrianCounter();
    }

    public void updateStats(IPedestrian pedestrian) {
        // to avoid double overflow, as the sum of all the values over a long simulation time might cause this, the current average is stored directly
        double dPreviousRate = ((double)getNrOfEnteredPedestrians()-1)/ (double) getNrOfEnteredPedestrians();
        meanPedestrianCrossingTime = meanPedestrianCrossingTime * dPreviousRate + pedestrian.getMeanStreetCrossingPassTime()/ getNrOfEnteredPedestrians();
        meanTimeSpentInSystem = meanTimeSpentInSystem * dPreviousRate + pedestrian.getTimeSpentInSystem()/ getNrOfEnteredPedestrians();
        meanWaitingTimePerStop = meanWaitingTimePerStop * dPreviousRate + pedestrian.getMeanWaitingTime()/ getNrOfEnteredPedestrians();
        meanStopCount = meanStopCount * dPreviousRate + pedestrian.getStopCount()/ getNrOfEnteredPedestrians();
        meanPedestrianAreaTime = meanPedestrianAreaTime * dPreviousRate + pedestrian.getMeanStreetAreaPassTime()/ getNrOfEnteredPedestrians();
    }

    public void addCar(ICar iCar) {
        iCar.leaveSystem();
        IConsumer consumer = iCar.getLastSection();
        if (consumer instanceof Street) {
            Car car = CarController.getCar(iCar);
            ((Street)consumer).carDelivered(null, car, true);
        }
        CarController.removeCarMapping(iCar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleJamTrafficLight(){ return; }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addCircularObstacle (double radius, Point midPoint){
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addPolygonObstacle( List<Point> cornerPoints) {
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<IPedestrian> getPedestrianQueue(){
        return null;
    }


    public Map<IPedestrian, Point> getPedestrianPositions()
            throws IllegalStateException {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removePedestrian(IPedestrian iPedestrian)
            throws IllegalStateException {
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPedestrianStreetConnector getNextStreetConnector() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPedestrianStreetConnector getPreviousStreetConnector() {
        return previousStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreviousStreetConnector(IPedestrianStreetConnector previousStreetConnector) {
        this.previousStreetConnector = previousStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNextStreetConnector(IPedestrianStreetConnector nextStreetConnector) {
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getPedestrianPosition(IPedestrian iPedestrian) {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void carEnter(Car car) {
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull() {
        // this method is only used by an Intersection object
        // so it is necessary that this always returns false
        // because a RuntimeException is thrown when this is true
        // (check if car can really enter the section is made in
        // method carEnter(Car car))
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DTO toDTO() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanPassTimeForEnteredPedestrians() {
        return meanPedestrianCrossingTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanTimeSpentInSystemForEnteredPedestrians() {
        return meanTimeSpentInSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanWaitingTimePerStopForEnteredPedestrians() {
        return meanWaitingTimePerStop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanStopCountForEnteredPedestrians() {
        return meanStopCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanIntersectionPassTimeForEnteredPedestrians() { return meanPedestrianAreaTime;
    }


}
