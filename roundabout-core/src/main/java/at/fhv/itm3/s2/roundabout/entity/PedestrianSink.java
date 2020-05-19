package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.PedestrianController;
import desmoj.core.simulator.Model;

import java.util.*;
import java.util.List;

public class PedestrianSink extends PedestrianAbstractSink {

    private List<PedestrianConnectedStreetSections> previousStreetConnector = new LinkedList<>();

    private double meanTimeSpentOnCrossing;
    private double meanTimeSpentInSystem;
    private double meanWaitingTimePerStop;
    private double meanStopCount;
    private double meanTimeOnPedestrianArea;
    private double meanTimeSpentInWaitingPuffer;
    private double meanWaitingTimeBeforeEnteringSystem; //pedestriansQueueTo
    private double meanEventTimeGap;

    public PedestrianSink(Model owner, String name, boolean showInTrace) {
        this(UUID.randomUUID().toString(), owner, name, showInTrace);
    }

    public PedestrianSink(String id, Model owner, String name, boolean showInTrace) {
        super(id, owner, name, showInTrace);

        this.meanTimeSpentOnCrossing = 0;
        this.meanTimeSpentInSystem = 0;
        this.meanWaitingTimePerStop = 0;
        this.meanStopCount = 0;
        this.meanTimeOnPedestrianArea = 0;
        this.meanWaitingTimeBeforeEnteringSystem = 0;
        this.meanEventTimeGap = 0;
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
        iPedestrian.leavePedestrianArea();
        //addPedestrian(iPedestrian, new PedestrianPoint(0,0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPedestrian(IPedestrian iPedestrian, PedestrianPoint position) {
        iPedestrian.leaveSystem();
        iPedestrian.leavePedestrianArea();
        iPedestrian.leavePedestrianCrossing();
        iPedestrian.endPedestrianWaiting();

        incrementEnteredPedestrianCounter();
        updateStats(iPedestrian);

        // needed for adaption to the trafsim framework
        if (! (iPedestrian instanceof Pedestrian)){
            throw new IllegalStateException("Pedestrian not instance of Pedestrian.");
        }
        PedestrianController.removeCarMapping(((Pedestrian)iPedestrian).getCarDummy());

        pedestrianObserver.notifyObservers(iPedestrian);
        incrementLeftPedestrianCounter();

        // call carDelivered events for last section, so the car position
        // of the current car (that has just left the last section successfully
        // can be removed (saves memory)
        // caution! that requires to call traverseToNextSection before calling this method
        this.carDelivered(null, ((Pedestrian) iPedestrian).getCarDummy(), true);
    }

    public void updateStats(IPedestrian pedestrian) {
        // to avoid double overflow, as the sum of all the values over a long simulation time might cause this, the current average is stored directly
        double dPreviousRate = ((double)getNrOfEnteredPedestrians()-1)/ (double) getNrOfEnteredPedestrians();
        meanTimeSpentOnCrossing = meanTimeSpentOnCrossing * dPreviousRate + pedestrian.getMeanStreetCrossingPassTime()/ getNrOfEnteredPedestrians();
        meanTimeSpentInSystem = meanTimeSpentInSystem * dPreviousRate + pedestrian.getTimeSpentInSystem()/ getNrOfEnteredPedestrians();
        meanWaitingTimePerStop = meanWaitingTimePerStop * dPreviousRate + pedestrian.getMeanWaitingTime()/ getNrOfEnteredPedestrians();
        meanStopCount = meanStopCount * dPreviousRate + pedestrian.getStopCount()/ getNrOfEnteredPedestrians();
        meanTimeOnPedestrianArea = meanTimeOnPedestrianArea * dPreviousRate + pedestrian.getMeanStreetAreaPassTime()/ getNrOfEnteredPedestrians();
        meanTimeSpentInWaitingPuffer = meanTimeSpentInSystem - meanTimeOnPedestrianArea;

        if ( !(pedestrian instanceof Pedestrian)) {
            throw new IllegalStateException("type mismatch");
        }
        meanWaitingTimeBeforeEnteringSystem = meanWaitingTimeBeforeEnteringSystem * dPreviousRate + ((Pedestrian) pedestrian).getMeanWaitingBeforeEnteringTime() / getNrOfEnteredPedestrians();
        meanEventTimeGap = meanEventTimeGap * dPreviousRate + ((Pedestrian) pedestrian).getMeanTimeEventGap() / getNrOfEnteredPedestrians();
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
    public void addCircularObstacle (double radius, PedestrianPoint midPoint){
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addPolygonObstacle( List<PedestrianPoint> cornerPoints) {
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<IPedestrian> getPedestrianQueue(){
        return null;
    }


    public Map<IPedestrian, PedestrianPoint> getPedestrianPositions()
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
    public List<PedestrianConnectedStreetSections> getNextStreetConnector() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PedestrianConnectedStreetSections> getPreviousStreetConnector() {
        return previousStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPreviousStreetConnector( PedestrianConnectedStreetSections previousStreetConnector) {
        this.previousStreetConnector.add(previousStreetConnector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNextStreetConnector( PedestrianConnectedStreetSections nextStreetConnector ) { }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianPoint getPedestrianPosition(IPedestrian iPedestrian) {
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
        return meanTimeSpentOnCrossing;
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
    public double getMeanIntersectionPassTimeForEnteredPedestrians() { return meanTimeOnPedestrianArea;
    }

    public double getMeanTimeSpentInWaitingPuffer() {return meanTimeSpentInWaitingPuffer;}


    public double getMeanTimeBetweenEventCall() {return meanEventTimeGap;}

}
