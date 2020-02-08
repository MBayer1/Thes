package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.AbstractProSumer;
import at.fhv.itm3.s2.roundabout.api.util.observable.ObserverType;
import at.fhv.itm3.s2.roundabout.api.util.observable.RoundaboutObservable;
import desmoj.core.simulator.Model;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class PedestrianStreet extends AbstractProSumer implements IPedestrianCountable {

    private final String id;
    private long enteredPedestriansCounter;
    private long leftPedestriansCounter;
    private long lostPedestriansCounter;
    private TrafficLight trafficLight;

    private double greenPhaseStart;

    protected Observable pedestrianObserver;
    protected Observable enteredPedestrianObserver;
    protected Observable leftPedestrianObserver;
    protected Observable lostPedestrianObserver;
    protected Observable pedestrianPositionObserver;
    protected Observable trafficLightObserver;

    public PedestrianStreet(Model owner, String name, boolean showInTrace) {
        this(UUID.randomUUID().toString(), owner, name, showInTrace);
    }

    public PedestrianStreet(String id, Model owner, String name, boolean showInTrace) {
        this(id, owner, name, showInTrace, false, null, null, null);
    }

    public PedestrianStreet(
        Model owner,
        String name,
        boolean showInTrace,
        boolean trafficLightActive,
        boolean isJamTrafficLight,
        Long minGreenPhaseDuration,
        Long redPhaseDuration
    ) {
        this(UUID.randomUUID().toString(),
        owner,
        name,
        showInTrace,
        trafficLightActive,
        minGreenPhaseDuration,
        null,
        redPhaseDuration);
    }

    public PedestrianStreet(
        Model owner,
        String name,
        boolean showInTrace,
        boolean trafficLightActive,
        Long greenPhaseDuration,
        Long redPhaseDuration
    ) {
        this(
            UUID.randomUUID().toString(),
            owner,
            name,
            showInTrace,
            trafficLightActive,
            null,
            greenPhaseDuration,
            redPhaseDuration
        );
    }

    public PedestrianStreet(
        String id,
        Model owner,
        String name,
        boolean showInTrace,
        boolean trafficLightActive,
        Long minGreenPhaseDuration,
        Long greenPhaseDuration,
        Long redPhaseDuration
    ) {
        super(owner, name, showInTrace);

        this.id = id;
        this.enteredPedestriansCounter = 0;
        this.leftPedestriansCounter = 0;
        this.lostPedestriansCounter = 0;

        this.trafficLight = new TrafficLight(trafficLightActive, minGreenPhaseDuration, greenPhaseDuration, redPhaseDuration);
        this.greenPhaseStart = 0.0;

        this.pedestrianObserver = new RoundaboutObservable();
        this.enteredPedestrianObserver = new RoundaboutObservable();
        this.leftPedestrianObserver = new RoundaboutObservable();
        this.lostPedestrianObserver = new RoundaboutObservable();
        this.pedestrianPositionObserver = new RoundaboutObservable();
        this.trafficLightObserver = new RoundaboutObservable();

        addObserver(
            ObserverType.PEDESTRIAN_LOST,
            (o, arg) ->  System.out.println(String.format("Street \"%s\" pedestrians lost: %s", id, arg))
        );
    }

    public String getId() {
        return id;
    }

    /**
     * Handles active traffic light to red if there is a jam in the next street section {@link PedestrianStreet}
     */
    public void handleJamTrafficLight() {
    }

    /**
     * Gets total pedestrian counter passed into {@code this} {@link PedestrianStreet}.
     *
     * @return total pedestrian counter.
     */
    @Override
    public long getNrOfEnteredPedestrians() {
        return enteredPedestriansCounter;
    }

    /**
     * Gets total pedestrian counter passed from {@code this} {@link PedestrianStreet}.
     *
     * @return total pedestrian counter.
     */
    @Override
    public long getNrOfLeftPedestrians() { return leftPedestriansCounter; }

    /**
     * Gets total pedestrian counter lost in {@code this} {@link PedestrianStreet}.
     *
     * @return total pedestrian counter.
     */
    @Override
    public long getNrOfLostPedestrians() {
        return lostPedestriansCounter;
    }

    /**
     * Internal method for counter incrementation.
     */
    protected void incrementEnteredPedestrianCounter() {
        this.enteredPedestriansCounter++;
        this.enteredPedestrianObserver.notifyObservers(this.enteredPedestriansCounter);
    }

    /**
     * Internal method for counter incrementation.
     */
    protected void incrementLeftPedestrianCounter() {
        this.leftPedestriansCounter++;
        this.leftPedestrianObserver.notifyObservers(this.leftPedestriansCounter);
    }

    /**
     * Internal method for counter incrementation.
     */
    protected void incrementLostPedestrianCounter() {
        this.lostPedestriansCounter++;
        this.lostPedestrianObserver.notifyObservers(this.lostPedestrianObserver);
    }

    /**
     * Gets physical length of the street section.
     *
     * @return The length in meters.
     */
    public abstract double getLengthX( );

    /**
     * Gets physical width of the street section.
     *
     * @return The width in meters.
     */
    public abstract double getLengthY();


    /**
     * Get type of PedestrianStreetSection
     *
     * @return type of PedestrianStreetSection as {@PedestrianConsumerType}.
     */
    public abstract PedestrianConsumerType getPedestrianConsumerType();


    /**
     * When true border of PedestrianStreet can be overstepped
     *
     * @return The width in meters.
     */
    public abstract boolean isPedestrianCrossing();

    /**
     * Adds a new pedestrian to the street section.
     *
     * @param iPedestrian The pedestrian to add.
     * @param position location where pedestrians stands
     */
    public abstract void addPedestrian(IPedestrian iPedestrian, Point position);

    /**
     * Adds a new circularly obstical on current {@link PedestrianStreet}
     *
     * @param radius radius of the circularly optical The pedestrian to add.
     * @param midPoint coordinates of the circle center
     */
    public abstract void addCircularObstacle(double radius, Point midPoint);


    /**
     * Adds a new polygonal obstical on current {@link PedestrianStreet}
     *
     * @param cornerPoints list of corner coordinates of the obstical in the according Order to another.
     */
    public abstract void addPolygonObstacle( List<Point> cornerPoints);


    /**
     * Returns pedestrian queue of this {@link PedestrianStreet}.
     *
     * @return unmodifiable pedestrian queue.
     * @throws IllegalStateException in case if queue equals null.
     */
    public abstract List<IPedestrian> getPedestrianQueue()
    throws IllegalStateException;

    /**
     * Removes the first pedestrian of the queue and returns the first pedestrian.
     *
     * @return removed pedestrian.
     */
    public abstract void removePedestrian(IPedestrian iPedestrian);

    /**
     * Checks if the street section is empty.
     *
     * @return True if street section is empty.
     */
    public abstract boolean isEmpty();

    /**
     * Gets the next street connector if available.
     *
     * @return reference to next {@link IPedestrianStreetConnector}.
     */
    public abstract IPedestrianStreetConnector getNextStreetConnector();

    /**
     * Gets the previous street connector if available.
     *
     * @return reference to previous {@link IPedestrianStreetConnector}.
     */
    public abstract IPedestrianStreetConnector getPreviousStreetConnector();

    /**
     * Sets the previous street connector
     *
     * @param previousStreetConnector street connector to be set
     */
    public abstract void setPreviousStreetConnector(IPedestrianStreetConnector previousStreetConnector);

    /**
     *  Sets the next street connector
     *
     * @param nextStreetConnector street connector to be set
     */
    public abstract void setNextStreetConnector(IPedestrianStreetConnector nextStreetConnector);

    /**
     * Gets all pedestrian positions of the street section.
     *
     * @return unmodifiable map of pedestrian positions.
     */
    public abstract Point getPedestrianPosition (IPedestrian iPedestrian);

    /**
     * Returns if traffic light at end of the street is active or not.
     *
     * @return true = active
     */
    public boolean isTrafficLightActive() {
        return trafficLight.isActive();
    }

    /**
     * Returns if traffic light at end of the street is triggered by traffic jam. if not it is cyclic.
     *
     * @return true = active
     */
    public boolean isTrafficLightTriggeredByJam() {
        return trafficLight.isTriggeredByJam();
    }

    /**
     * Indicates whether the traffic light at the end of the street signals "free to go" (true) or "stop" (false), if it is active.
     * Otherwise it will always return true.
     *
     *  @return true = free to go
     */
    public boolean isTrafficLightFreeToGo() {
        return !trafficLight.isActive() || trafficLight.isFreeToGo();
    }

    /**
     * Sets the traffic light state (free to go = true, stop = false).
     *
     * @param isFreeToGo set true if pedestrians are free to go
     * @throws IllegalStateException if traffic light is inactive
     */
    public void setTrafficLightFreeToGo(boolean isFreeToGo) throws IllegalStateException {
        trafficLight.setFreeToGo(isFreeToGo);
        trafficLightObserver.notifyObservers(isFreeToGo);
    }

    /**
     * Getter for red phase duration of traffic light
     *
     * @return the duration of the red light
     */
    public long getRedPhaseDurationOfTrafficLight() { return this.trafficLight.getRedPhaseDuration(); }

    /**
     * Getter for green phase duration of cyclic traffic light
     *
     * @return the duration of the green light
     */
    public long getGreenPhaseDurationOfTrafficLight() { return this.trafficLight.getGreenPhaseDuration(); }

    /**
     * Sends notifications for traffic light state.
     * Is designed to be started in the beginning of simulation.
     */
    public void initTrafficLight() {
        if (isTrafficLightActive()) {
            trafficLightObserver.notifyObservers();
        }
    }

    /**
     * Sets TimeStamp of green phase Start - needed for jam traffic lights
     */
    public void setGreenPhaseStart( double greenPhaseStart ) { this.greenPhaseStart = greenPhaseStart; }

    /**
     * Get TimeStamp of green phase Start - needed for jam traffic lights
     *
     * @return returns start TimeStamp as double of green phase of jam traffic lights
     */
    public double getGreenPhaseStart( ) { return this.greenPhaseStart; }

    /**
     * Getter for min green phase duration of jam traffic light
     *
     * @return the duration of the min green light
     */
    public double getMinGreenPhaseDurationOfTrafficLight(){ return this.trafficLight.getMinGreenPhaseDuration(); }

    /**
     * Helper method that registers typed observers.
     *
     * @param observerType type of observer to be registered.
     * @param o observer.
     */
    public synchronized void addObserver(ObserverType observerType, Observer o) {
        switch (observerType) {
            case CAR_ENTERED: enteredPedestrianObserver.addObserver(o); break;
            case CAR_ENTITY: pedestrianObserver.addObserver(o); break;
            case CAR_LEFT: leftPedestrianObserver.addObserver(o); break;
            case CAR_POSITION: pedestrianPositionObserver.addObserver(o); break;
            case TRAFFIC_LIGHT: trafficLightObserver.addObserver(o); break;
        }
    }
}
