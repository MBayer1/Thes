package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.entity.AbstractSink;
import at.fhv.itm3.s2.roundabout.api.entity.ICar;
import at.fhv.itm3.s2.roundabout.api.entity.IStreetConnector;
import at.fhv.itm3.s2.roundabout.api.entity.Street;
import at.fhv.itm3.s2.roundabout.controller.CarController;
import desmoj.core.simulator.Model;

import java.util.*;

public class RoundaboutSink extends AbstractSink {

    private IStreetConnector previousStreetConnector;

    private double meanRoundaboutPassTime;
    private double meanTimeSpentInSystem;
    private double meanWaitingTimePerStop;
    private double meanStopCount;
    private double meanIntersectionPassTime;
    private double meanTimeWaitingDueToIllegalCrossingOfPedestrian;

    public RoundaboutSink(Model owner, String name, boolean showInTrace) {
        this(UUID.randomUUID().toString(), owner, name, showInTrace);
    }

    public RoundaboutSink(String id, Model owner, String name, boolean showInTrace) {
        super(id, owner, name, showInTrace);

        this.meanRoundaboutPassTime = 0;
        this.meanTimeSpentInSystem = 0;
        this.meanWaitingTimePerStop = 0;
        this.meanStopCount = 0;
        this.meanIntersectionPassTime = 0;
        this.meanTimeWaitingDueToIllegalCrossingOfPedestrian = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLength() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCar(ICar iCar) {
        iCar.leaveSystem();
        incrementEnteredCarCounter();
        updateStats(iCar);

        IConsumer consumer = iCar.getLastSection();
        if (consumer instanceof Street) {
            Car car = CarController.getCar(iCar);
            ((Street)consumer).carDelivered(null, car, true);
        }
        CarController.removeCarMapping(iCar);
        carObserver.notifyObservers(iCar);
        incrementLeftCarCounter();
    }

    public void updateStats(ICar car) {
        // to avoid double overflow, as the sum of all the values over a long simulation time might cause this, the current average is stored directly
        double dPreviousRate = ((double)getNrOfEnteredCars()-1)/ (double) getNrOfEnteredCars();
        meanRoundaboutPassTime = meanRoundaboutPassTime * dPreviousRate + car.getMeanRoundaboutPassTime()/ getNrOfEnteredCars();
        meanTimeSpentInSystem = meanTimeSpentInSystem * dPreviousRate + car.getTimeSpentInSystem()/ getNrOfEnteredCars();
        meanWaitingTimePerStop = meanWaitingTimePerStop * dPreviousRate + car.getMeanWaitingTime()/ getNrOfEnteredCars();
        meanStopCount = meanStopCount * dPreviousRate + car.getStopCount()/ getNrOfEnteredCars();
        meanIntersectionPassTime = meanIntersectionPassTime * dPreviousRate + car.getMeanIntersectionPassTime()/ getNrOfEnteredCars();
        meanTimeWaitingDueToIllegalCrossingOfPedestrian = meanTimeWaitingDueToIllegalCrossingOfPedestrian * dPreviousRate + car.getMeanTimeWaitingDueToIllegalCrossingOfPedestrian()/ getNrOfEnteredCars();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICar getFirstCar() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICar getLastCar() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICar> getCarQueue()
    throws IllegalStateException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICar removeFirstCar() {
        return null;
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
    public IStreetConnector getNextStreetConnector() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStreetConnector getPreviousStreetConnector() {
        return this.previousStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreviousStreetConnector(IStreetConnector previousStreetConnector) {
        this.previousStreetConnector = previousStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNextStreetConnector(IStreetConnector nextStreetConnector) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<ICar, Double> getCarPositions() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAllCarsPositions() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirstCarOnExitPoint() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean firstCarCouldEnterNextSection() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnoughSpace(double length) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveFirstCarToNextSection() throws IllegalStateException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean carCouldEnterNextSection() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void carEnter(Car car) {
        addCar(CarController.getICar(car));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {

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
    public double getMeanRoundaboutPassTimeForEnteredCars() {
        return meanRoundaboutPassTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanTimeSpentInSystemForEnteredCars() {
        return meanTimeSpentInSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanWaitingTimePerStopForEnteredCars() {
        return meanWaitingTimePerStop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanStopCountForEnteredCars() {
        return meanStopCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanIntersectionPassTimeForEnteredCars() { return meanIntersectionPassTime;
    }

    public double getMeanTimeWaitingDueToIllegalCrossingOfPedestrian() {
        return meanTimeWaitingDueToIllegalCrossingOfPedestrian;
    }
}
