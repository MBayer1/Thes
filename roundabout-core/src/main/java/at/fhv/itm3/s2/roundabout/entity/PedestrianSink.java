package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import desmoj.core.simulator.Model;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PedestrianSink extends PedestrianAbstractSink {

    private IStreetConnector previousStreetConnector;

    private double meanRoundaboutPassTime;
    private double meanTimeSpentInSystem;
    private double meanWaitingTimePerStop;
    private double meanStopCount;
    private double meanIntersectionPassTime;

    public PedestrianSink(Model owner, String name, boolean showInTrace) {
        this(UUID.randomUUID().toString(), owner, name, showInTrace);
    }

    public PedestrianSink(String id, Model owner, String name, boolean showInTrace) {
        super(id, owner, name, showInTrace);

        this.meanRoundaboutPassTime = 0;
        this.meanTimeSpentInSystem = 0;
        this.meanWaitingTimePerStop = 0;
        this.meanStopCount = 0;
        this.meanIntersectionPassTime = 0;
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
    public double getWidth() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPedestrian(IPedestrian iPedestrian) {
        iPedestrian.leaveSystem();
        incrementEnteredPedestrianCounter();
        updateStats(iPedestrian);

        IConsumer consumer = iPedestrian.getLastSection();
        incrementLeftPedestrianCounter();
    }

    public void updateStats(IPedestrian car) {
        // to avoid double overflow, as the sum of all the values over a long simulation time might cause this, the current average is stored directly
        double dPreviousRate = ((double)getNrOfEnteredPedestrians()-1)/ (double) getNrOfEnteredPedestrians();
        meanRoundaboutPassTime = meanRoundaboutPassTime * dPreviousRate + car.getMeanRoundaboutPassTime()/ getNrOfEnteredPedestrians();
        meanTimeSpentInSystem = meanTimeSpentInSystem * dPreviousRate + car.getTimeSpentInSystem()/ getNrOfEnteredPedestrians();
        meanWaitingTimePerStop = meanWaitingTimePerStop * dPreviousRate + car.getMeanWaitingTime()/ getNrOfEnteredPedestrians();
        meanStopCount = meanStopCount * dPreviousRate + car.getStopCount()/ getNrOfEnteredPedestrians();
        meanIntersectionPassTime = meanIntersectionPassTime * dPreviousRate + car.getMeanIntersectionPassTime()/ getNrOfEnteredPedestrians();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IPedestrian> getPedestrianQueue()
    throws IllegalStateException {
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
    public boolean isFull() {
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
    public double getMeanRoundaboutPassTimeForEnteredPedestrians() {
        return meanRoundaboutPassTime;
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
    public double getMeanIntersectionPassTimeForEnteredPedestrians() { return meanIntersectionPassTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObsticalCircle(double radius, Point midPoint){} //TODO

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObsticalPolygone( List<Point> cornerPoints){} //TODO

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePedestrian(IPedestrian iPedestrian){}

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<IPedestrian, Point> getPedestrianPositions(IPedestrian iPedestrian){
        return null;
    }

    /**
     * needed for integration in to the very first basis framework
     */
    @Override
    public void carEnter(Car car) {
        return;
    }

    /**
     * needed for integration in to the very first basis framework
     */
    @Override
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {
        return;
    }
}
