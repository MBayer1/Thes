package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.CarController;
import at.fhv.itm3.s2.roundabout.controller.IntersectionController;
import at.fhv.itm3.s2.roundabout.controller.PedestrianController;
import at.fhv.itm3.s2.roundabout.event.CarCouldLeaveSectionEvent;
import at.fhv.itm3.s2.roundabout.event.RoundaboutEventFactory;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import com.ctreber.acearth.util.Coordinate;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import javafx.geometry.Pos;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PedestrianStreetSection extends PedestrianStreet {

    private final double length;
    private final double width;
    private final PedestrianConsumerType consumerType;


    // next two values are for the controlling of a traffic light [checking for jam/ needed for optimization]
    private double currentWaitingTime;
    private double currentTimeLastMovement;

    private final LinkedList<IPedestrian> pedestrianQueue;
    private final Map<IPedestrian, Point> pedestrianPositions;

    private final LinkedList<Circle> circularObsticals; //TODO include those obstacles in to calculations
    private final LinkedList<Polygon> polygoneObstical; //TODO include those obstacles in to calculations

    private IPedestrianStreetConnector nextStreetConnector;
    private IPedestrianStreetConnector previousStreetConnector;

    private IntersectionController intersectionController;

    public PedestrianStreetSection(
            double length,
            double width,
            Model model,
            String modelDescription,
            boolean showInTrace
    ) {
        this(UUID.randomUUID().toString(), length, width, PedestrianConsumerType.PEDESTRIAN_STREET_SECTION, model, modelDescription, showInTrace);
    }

    public PedestrianStreetSection(
            double length,
            double width,
            PedestrianConsumerType consumerType,
            Model model,
            String modelDescription,
            boolean showInTrace
    ) {
        this(UUID.randomUUID().toString(), length, width, consumerType, model, modelDescription, showInTrace);
    }

    public PedestrianStreetSection(
            String id,
            double length,
            double width,
            PedestrianConsumerType consumerType,
            Model model,
            String modelDescription,
            boolean showInTrace
    ) {
        this(
                id, length, width, consumerType, model, modelDescription, showInTrace,
                false, null, null, null
        );
    }

    public PedestrianStreetSection(
            double length,
            double width,
            PedestrianConsumerType consumerType,
            Model model,
            String modelDescription,
            boolean showInTrace,
            boolean trafficLightActive,
            Long greenPhaseDuration,
            Long redPhaseDuration
    ) {
        this(
                UUID.randomUUID().toString(), length, width, consumerType, model, modelDescription, showInTrace,
                trafficLightActive, null, greenPhaseDuration, redPhaseDuration
        );
    }

    public PedestrianStreetSection(
            String id,
            double length,
            double width,
            PedestrianConsumerType consumerType,
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

        this.consumerType = consumerType;

        this.pedestrianQueue = new LinkedList<>();
        this.pedestrianPositions = new HashMap<>();
        this.circularObsticals = new LinkedList<>(); //TODO handling and including to calculations
        this.polygoneObstical = new LinkedList<>(); //TODO handling and including to calculations

        this.intersectionController = IntersectionController.getInstance();

        if(this.isTrafficLightActive() && !this.isTrafficLightTriggeredByJam()) {
           // RoundaboutEventFactory.getInstance().createToggleTrafficLightStateEvent(getRoundaboutModel()).schedule(
           //         this, TODO
           //         new TimeSpan(greenPhaseDuration)
           // );
        }
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
    public PedestrianConsumerType getPedestrianConsumerType() { return consumerType;}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPedestrianCrossing() { return consumerType.equals(PedestrianConsumerType.PEDESTRIAN_CROSSING); }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addPedestrian(IPedestrian iPedestrian, Point position) {
        if (pedestrianQueue == null) {
            throw new IllegalStateException("pedestrianQueue in section cannot be null");
        }

        if (pedestrianPositions == null) {
            throw new IllegalStateException("pedestrianPositions in section cannot be null");
        }

        pedestrianQueue.addLast(iPedestrian);
        pedestrianPositions.put(iPedestrian, position);
        incrementEnteredPedestrianCounter();


        if (this.getPedestrianConsumerType() == PedestrianConsumerType.PEDESTRIAN_CROSSING) {
            iPedestrian.enterPedestrianCrossing();
            iPedestrian.leavePedestrianArea();
        } else
        if (this.getPedestrianConsumerType() == PedestrianConsumerType.PEDESTRIAN_STREET_SECTION) {
            iPedestrian.enterPedestrianArea();
            iPedestrian.leavePedestrianCrossing();
        }


        // call carDelivered events for last section, so the car position
        // of the current car (that has just left the last section successfully
        // can be removed (saves memory)
        // caution! that requires to call traverseToNextSection before calling this method
        Car car = PedestrianController.getCar(iPedestrian);
        IConsumer consumer = iPedestrian.getDestination();
        if (consumer instanceof Street) {
            ((Street)consumer).carDelivered(null, car, true);
        }

        pedestrianObserver.notifyObservers(iPedestrian);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleJamTrafficLight(){
        if (this.isTrafficLightActive() && this.isTrafficLightTriggeredByJam()) {

        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addCircularObstacle (double radius, Point midPoint){
        //TODO
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addPolygonObstacle( List<Point> cornerPoints) {
        //TODO
        return;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<IPedestrian> getPedestrianQueue()
            throws IllegalStateException {
        if (pedestrianQueue == null) {
            throw new IllegalStateException("pedestrianQueue in section cannot be null");
        }
        return pedestrianQueue;
    }


    public Map<IPedestrian, Point> getPedestrianPositions()
            throws IllegalStateException {
        if (pedestrianPositions == null) {
            throw new IllegalStateException("pedestrianPositions in section cannot be null");
        }

        return Collections.unmodifiableMap(pedestrianPositions);
     }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removePedestrian(IPedestrian iPedestrian)
            throws IllegalStateException {
        incrementLeftPedestrianCounter();

        if (!pedestrianQueue.remove(iPedestrian)) {
            throw new IllegalStateException("Pedestrian does not exist on this PedestrianStreetSection.");
        }
        if (pedestrianPositions.remove(iPedestrian).equals(null)) {
            throw new IllegalStateException("Pedestrian does not exist on this PedestrianStreetSection.");
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
    public IPedestrianStreetConnector getNextStreetConnector() {
        return nextStreetConnector;
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
        this.nextStreetConnector = nextStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getPedestrianPosition(IPedestrian iPedestrian) {

        if (pedestrianPositions == null) {
            throw new IllegalStateException("pedestrianPositions in section cannot be null");
        }

        return Collections.unmodifiableMap(pedestrianPositions).get(iPedestrian);
    }


    private Point calculateSocialForceVector() {
         return new Point(0,0   ); //TODO
    }

    private Point calculateNextPlanedPositonOfPedestrian(){
        return new Point(0,0   ); //TODO
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
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {
        return;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void carEnter(Car car) {
        // this method is only used by an Intersection object
        // and this has to call this method always even if there is not
        // enough space for another car (because otherwise a RuntimeException
        // is thrown). So the check if there is enough space for this car
        // is made here and if there isn't enough space than a car is lost
        // and the counter is incremented
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
}
