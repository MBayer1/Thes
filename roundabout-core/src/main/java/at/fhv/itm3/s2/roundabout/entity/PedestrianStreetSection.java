package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.IntersectionController;
import at.fhv.itm3.s2.roundabout.event.RoundaboutEventFactory;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PedestrianStreetSection extends PedestrianStreet {

    private final double length;
    private final double width;

    // next two values are for the controlling of a traffic light [checking for jam/ needed for optimization]
    private double currentWaitingTime;
    private double currentTimeLastMovement;

    private final LinkedList<IPedestrian> pedestrianQueue;
    private final Map<IPedestrian, Point> pedestrianPositions;
    private final List<Rectangle> obsticalRectangel;
    private final List<Circle> obsticalCircle;

    private IStreetConnector nextStreetConnector;
    private IStreetConnector previousStreetConnector;

    private IntersectionController intersectionController;

    public PedestrianStreetSection(
            double length,
            double width,
            Model model,
            String modelDescription,
            boolean showInTrace
    ) {
        this(UUID.randomUUID().toString(), length, width, model, modelDescription, showInTrace);
    }

    public PedestrianStreetSection(
            String id,
            double length,
            double width,
            Model model,
            String modelDescription,
            boolean showInTrace
    ) {
        this(
                id, length, width, model, modelDescription, showInTrace,
                false, null, null, null
        );
    }

    public PedestrianStreetSection(
            double length,
            double width,
            Model model,
            String modelDescription,
            boolean showInTrace,
            boolean trafficLightActive,
            Long greenPhaseDuration,
            Long redPhaseDuration
    ) {
        this(
                UUID.randomUUID().toString(), length, width, model, modelDescription, showInTrace,
                trafficLightActive, null, greenPhaseDuration, redPhaseDuration
        );
    }

    public PedestrianStreetSection(
            String id,
            double length,
            double width,
            Model model,
            String modelDescription,
            boolean showInTrace,
            boolean trafficLightActive,
            Long minGreenPhaseDuration,
            Long greenPhaseDuration,
            Long redPhaseDuration
    ) {
        super(
                id,
                model,
                modelDescription,
                showInTrace,
                trafficLightActive,
                minGreenPhaseDuration,
                greenPhaseDuration,
                redPhaseDuration
        );

        this.length = length;
        this.width = width;

        this.pedestrianQueue = new LinkedList<>();
        this.pedestrianPositions = new HashMap<>();
        this.intersectionController = IntersectionController.getInstance();

        this.obsticalCircle = new ArrayList<>();
        this.obsticalRectangel = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPedestrian(IPedestrian iPedestrian) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePedestrian(IPedestrian iPedestrian) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObsticalCircle(double radius, Point midPoint) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObsticalPolygone( List<Point> cornerPoints) {

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<IPedestrian, Point> getPedestrianPositions(IPedestrian iPedestrian) {
//TODO
        return this.pedestrianPositions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IPedestrian> getPedestrianQueue()
            throws IllegalStateException {
        if (pedestrianQueue == null) {
            throw new IllegalStateException("carQueue in section cannot be null");
        }

        return Collections.unmodifiableList(pedestrianQueue);
    }

    private RoundaboutSimulationModel getRoundaboutModel() {
        final Model model = getModel();
        if (model instanceof RoundaboutSimulationModel) {
            return (RoundaboutSimulationModel) model;
        } else {
            throw new IllegalArgumentException("Not suitable roundaboutSimulationModel.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        final List<IPedestrian> pedestrianQueue = getPedestrianQueue();
        return pedestrianQueue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStreetConnector getNextStreetConnector() {
        return nextStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStreetConnector getPreviousStreetConnector() {
        return previousStreetConnector;
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
        this.nextStreetConnector = nextStreetConnector;
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
     * Needed to integrate for the very first basis framework
     */
    @Override
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {return;}

    /**
     * {@inheritDoc}
     * Needed to integrate in the very first basis framework
     */
    @Override
    public void carEnter(Car car) {return;}

    /**
     * {@inheritDoc}
     */
    @Override
    public DTO toDTO() {
        return null;
    }


}
