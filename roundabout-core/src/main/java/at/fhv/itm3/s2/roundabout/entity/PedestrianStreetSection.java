package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.IntersectionController;
import at.fhv.itm3.s2.roundabout.event.PedestrianEventFactory;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PedestrianStreetSection extends PedestrianStreet {
    private final double lengthX;
    private final double lengthY;
    private final PedestrianConsumerType consumerType;
    private PedestrianPoint globalCoordinateOfSectionOrigin;
    private Long minSizeOfPedestriansForTrafficLightTriggeredByJam;
    private boolean flexiBorderAlongX = true; // needed for type PedestrianCrossing
    SupportiveCalculations calculations = new SupportiveCalculations();

    public List<PedestrianWaitingListElement> pedestriansQueueToEnter;


    // next two values are for the controlling of a traffic light [checking for jam/ needed for optimization]
    private double currentWaitingTime;
    private double currentTimeLastMovement;

    private LinkedList<IPedestrian> pedestrianQueue;
    private Map<IPedestrian, PedestrianPoint> pedestrianPositions;

    private LinkedList<Circle> circularObsticals; //TODO include those obstacles in to calculations
    private LinkedList<Polygon> polygoneObstical; //TODO include those obstacles in to calculations

    private LinkedList<Street> leavingVehicleStreetList;
    private LinkedList<Street> enteringVehicleStreetList;

    private List<PedestrianConnectedStreetSections> nextStreetConnector = new LinkedList<>();
    private List<PedestrianConnectedStreetSections> previousStreetConnector = new LinkedList<>();

    private IntersectionController intersectionController;

    public PedestrianStreetSection(
            double lengthX,
            double lengthY,
            Model model,
            String modelDescription,
            boolean showInTrace
    ) {
        this(UUID.randomUUID().toString(), lengthX, lengthY, PedestrianConsumerType.PEDESTRIAN_STREET_SECTION, model, modelDescription, showInTrace, false);
    }

    public PedestrianStreetSection(
            double lengthX,
            double lengthY,
            PedestrianConsumerType consumerType,
            Model model,
            String modelDescription,
            boolean showInTrace
    ) {
        this(UUID.randomUUID().toString(), lengthX, lengthY, consumerType, model, modelDescription, showInTrace, false);
    }

    public PedestrianStreetSection(
            String id,
            double lengthX,
            double lengthY,
            PedestrianConsumerType consumerType,
            Model model,
            String modelDescription,
            boolean showInTrace,
            boolean useMassDynamic
    ) {
        this(
                id, lengthX, lengthY, consumerType, model, modelDescription, showInTrace,
                false, null, null, null, null, null,  useMassDynamic
        );
    }

    public PedestrianStreetSection(
            double lengthX,
            double lengthY,
            PedestrianConsumerType consumerType,
            Model model,
            String modelDescription,
            boolean showInTrace,
            boolean trafficLightActive,
            Long greenPhaseDuration,
            Long redPhaseDuration,
            Long minSizeOfPedestriansForTrafficLightTriggeredByJam,
            boolean useMassDynamic
    ) {
        this(
                UUID.randomUUID().toString(), lengthX, lengthY, consumerType, model, modelDescription, showInTrace,
                trafficLightActive, null, greenPhaseDuration, redPhaseDuration,
                minSizeOfPedestriansForTrafficLightTriggeredByJam,                 null, useMassDynamic
        );
    }

    public PedestrianStreetSection(
            String id,
            double lengthX,
            double lengthY,
            PedestrianConsumerType consumerType,
            Model model,
            String modelDescription,
            boolean showInTrace,
            boolean trafficLightActive,
            Long minGreenPhaseDuration,
            Long greenPhaseDuration,
            Long redPhaseDuration,
            Long minSizeOfPedestriansForTrafficLightTriggeredByJam,
            PedestrianPoint globalCoordinateForCenter,
            boolean useMassDynamic
    ) {
        super(
                id,
                model,
                modelDescription,
                showInTrace,
                useMassDynamic ? true : trafficLightActive,
                minGreenPhaseDuration,
                greenPhaseDuration,
                redPhaseDuration,
                useMassDynamic
        );

        this.lengthX = lengthX;
        this.lengthY = lengthY;
        this.minSizeOfPedestriansForTrafficLightTriggeredByJam = minSizeOfPedestriansForTrafficLightTriggeredByJam;

        this.consumerType = consumerType;

        this.pedestrianQueue = new LinkedList<>();
        this.pedestrianPositions = new HashMap<>();
        this.circularObsticals = new LinkedList<>(); //TODO handling and including to calculations
        this.polygoneObstical = new LinkedList<>(); //TODO handling and including to calculations

        this.intersectionController = IntersectionController.getInstance();
        this.globalCoordinateOfSectionOrigin = globalCoordinateForCenter;

        this.enteringVehicleStreetList = new LinkedList<>();
        this.leavingVehicleStreetList = new LinkedList<>();
        //pedestriansQueueToEnter = Collections.synchronizedList(new ArrayList<PedestrianWaitingListElement>()); TODO
        this.pedestriansQueueToEnter = new CopyOnWriteArrayList<PedestrianWaitingListElement>();

        if(this.isTrafficLightActive() && !this.isTrafficLightTriggeredByJam()) {
           // RoundaboutEventFactory.getInstance().createToggleTrafficLightStateEvent(getRoundaboutModel()).schedule(
           //         this, TODO
           //         new TimeSpan(greenPhaseDuration)
           // );
        }

        if (useMassDynamic) this.setTrafficLightFreeToGo(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLengthX() {
        return lengthX;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double getLengthY() {
        return lengthY;
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


    public void setGlobalCoordinateOfSectionOrigin(PedestrianPoint globalCoordinateOfSectionOrigin) {
        this.globalCoordinateOfSectionOrigin = globalCoordinateOfSectionOrigin;
    }

    public PedestrianPoint getGlobalCoordinateOfSectionOrigin() {

        return this.globalCoordinateOfSectionOrigin;
    }

    public LinkedList<Street> getEnteringVehicleStreetList() {
        return enteringVehicleStreetList;
    }

    public LinkedList<Street> getLeavingVehicleStreetList() {
        return leavingVehicleStreetList;
    }

    public void addEnteringVehicleStreetList(Street street) {
        if(enteringVehicleStreetList == null) {
            enteringVehicleStreetList = new LinkedList<>();
        }
        enteringVehicleStreetList.add(street);
    }

    public void addLeavingVehicleStreetList(Street street) {
        if(leavingVehicleStreetList == null) {
            leavingVehicleStreetList = new LinkedList<>();
        }
        leavingVehicleStreetList.add(street);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addPedestrian(IPedestrian iPedestrian, PedestrianPoint position) {
        if (pedestrianQueue == null) {
            throw new IllegalStateException("pedestrianQueue in section cannot be null");
        }

        if (pedestrianPositions == null) {
            throw new IllegalStateException("pedestrianPositions in section cannot be null");
        }

        pedestrianQueue.addLast(iPedestrian);
        pedestrianPositions.put(iPedestrian, position);
        incrementEnteredPedestrianCounter();

        if (!(iPedestrian instanceof Pedestrian)){
            throw new IllegalArgumentException("pedestrian not instance of pedestrian.");
        }

        iPedestrian.enterPedestrianArea();
        if (this.getPedestrianConsumerType() == PedestrianConsumerType.PEDESTRIAN_CROSSING) {
            iPedestrian.enterPedestrianCrossing();
        } else if (this.getPedestrianConsumerType() == PedestrianConsumerType.PEDESTRIAN_STREET_SECTION) {
            iPedestrian.leavePedestrianCrossing();

        }

        pedestrianObserver.notifyObservers(iPedestrian);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleJamTrafficLight(){
        // TODO add pedestrian crossing button
        if (this.isTrafficLightActive() && this.isTrafficLightTriggeredByJam()) {
            // for this the area in front of the crossing should be limited as an waiting in front of the crossing

            final boolean isActualGreenPhaseBiggerThanMin = (getRoundaboutModel().getCurrentTime() - getGreenPhaseStart()) > getMinGreenPhaseDurationOfTrafficLight();
            if( (getPedestrianQueue().size() >= minSizeOfPedestriansForTrafficLightTriggeredByJam) && isActualGreenPhaseBiggerThanMin) {
                // trigger red
                PedestrianEventFactory.getInstance().createToggleTrafficLightStateEvent(getRoundaboutModel()).schedule(
                        this,
                        new TimeSpan(0, getRoundaboutModel().getModelTimeUnit())
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCircularObstacle (double radius, PedestrianPoint midPoint){
        //TODO
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPolygonObstacle( List<PedestrianPoint> cornerPoints) {
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

    public Map<IPedestrian, PedestrianPoint> getPedestrianPositions()
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
    public List<PedestrianConnectedStreetSections> getNextStreetConnector() {
        return nextStreetConnector;
    }

    public PedestrianConnectedStreetSections getConnectorByNextSection (IConsumer section) {
        for (PedestrianConnectedStreetSections pair : nextStreetConnector) {
            if(pair.getToStreetSection().equals(section)){
                return pair;
            }

        }
        return null;
    }

    public PedestrianConnectedStreetSections getConnectorByPreviousSection(IConsumer section) {
        for (PedestrianConnectedStreetSections pair : previousStreetConnector) {
            if(pair.getToStreetSection().equals(section)){
                return pair;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNextStreetConnector(PedestrianConnectedStreetSections streetConnector) {
        this.nextStreetConnector.add(streetConnector);
    }

    public long getMinSizeOfPedestriansForTrafficLightTriggeredByJam() {
        return minSizeOfPedestriansForTrafficLightTriggeredByJam;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PedestrianConnectedStreetSections> getPreviousStreetConnector() {
        return previousStreetConnector;
    }

    public PedestrianConnectedStreetSections getPreviousStreetConnectorToSource() {
        for (PedestrianConnectedStreetSections connector : previousStreetConnector) {
            if(connector.getToSource() != null) {
                return connector;
            }
        }
        throw new IllegalStateException("there is no previous source.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPreviousStreetConnector(PedestrianConnectedStreetSections previousStreetConnector) {
        this.previousStreetConnector.add(previousStreetConnector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianPoint getPedestrianPosition(IPedestrian iPedestrian) {

        if (pedestrianPositions == null) {
            throw new IllegalStateException("pedestrianPositions in section cannot be null");
        }

        return Collections.unmodifiableMap(pedestrianPositions).get(iPedestrian);
    }

    public boolean checkPedestrianIsWithinSection( IPedestrian iPedestrian ) {

        if ( !(iPedestrian instanceof Pedestrian) ) {
            throw new IllegalStateException("Pedestrian is not instance of Pedestrian");
        }

        if(     calculations.val1BiggerOrAlmostEqual( iPedestrian.getCurrentGlobalPosition().getX(), getGlobalCoordinateOfSectionOrigin().getX() ) &&
                calculations.val1LowerOrAlmostEqual( iPedestrian.getCurrentGlobalPosition().getX(), getGlobalCoordinateOfSectionOrigin().getX() + getLengthX()) &&
                calculations.val1BiggerOrAlmostEqual( iPedestrian.getCurrentGlobalPosition().getY(), getGlobalCoordinateOfSectionOrigin().getY() ) &&
                calculations.val1LowerOrAlmostEqual( iPedestrian.getCurrentGlobalPosition().getY(), getGlobalCoordinateOfSectionOrigin().getY() + getLengthY())
                ) return true;

        return false;
    }

    public boolean checkPointIsWithinSectionAndNotWithinAPort(PedestrianPoint globalPosition) {
        boolean outside = false;

        if(     calculations.val1BiggerOrAlmostEqual( globalPosition.getX(), getGlobalCoordinateOfSectionOrigin().getX() ) &&
                calculations.val1LowerOrAlmostEqual( globalPosition.getX(), getGlobalCoordinateOfSectionOrigin().getX() + getLengthX()) &&
                calculations.val1BiggerOrAlmostEqual( globalPosition.getY(), getGlobalCoordinateOfSectionOrigin().getY() ) &&
                calculations.val1LowerOrAlmostEqual( globalPosition.getY(), getGlobalCoordinateOfSectionOrigin().getY() + getLengthY())
                ) outside = true;

        if ( !outside ) outside = checkPointAlongPort(globalPosition);
        return outside;
    }

    public boolean checkPointAlongPort(PedestrianPoint globalPosition) {
        // except it is within an port gab, then it will take enter or exit port depending which is closer.
        for ( PedestrianConnectedStreetSections connected : this.getNextStreetConnector()) {
            if (connected.getFromStreetSection().equals(this)) {
                double sectionOriginX = getGlobalCoordinateOfSectionOrigin().getX();
                double sectionOriginY = getGlobalCoordinateOfSectionOrigin().getY();

                PedestrianStreetSectionPort localPort = connected.getPortOfFromStreetSection();
                PedestrianStreetSectionPort globalPort = new PedestrianStreetSectionPort(
                        localPort.getLocalBeginOfStreetPort().getX() + sectionOriginX,
                        localPort.getLocalBeginOfStreetPort().getY() + sectionOriginY,
                        localPort.getLocalEndOfStreetPort().getX() + sectionOriginX,
                        localPort.getLocalEndOfStreetPort().getY() + sectionOriginY);

                if (calculations.checkWallIntersectionWithinPort(globalPort, globalPosition)){
                    return true;
                }
            }
        }

        for ( PedestrianConnectedStreetSections connected : this.getPreviousStreetConnector()) {
            if (connected.getFromStreetSection().equals(this)) {
                double sectionOriginX = getGlobalCoordinateOfSectionOrigin().getX();
                double sectionOriginY = getGlobalCoordinateOfSectionOrigin().getY();

                PedestrianStreetSectionPort localPort = connected.getPortOfFromStreetSection();
                PedestrianStreetSectionPort globalPort = new PedestrianStreetSectionPort(
                        localPort.getLocalBeginOfStreetPort().getX() + sectionOriginX,
                        localPort.getLocalBeginOfStreetPort().getY() + sectionOriginY,
                        localPort.getLocalEndOfStreetPort().getX() + sectionOriginX,
                        localPort.getLocalEndOfStreetPort().getY() + sectionOriginY);

                if (calculations.checkWallIntersectionWithinPort(globalPort, globalPosition)){
                    return true;
                }
            }
        }
        return false;
    }

    public void setPedestrianPosition(Pedestrian pedestrian, PedestrianPoint globalPos) {
        if(pedestrianPositions.get(pedestrian) == null) {
            if(this.pedestriansQueueToEnter.contains(pedestrian)) {
                throw new IllegalStateException("Pedestrian did not enter system, yet but is waiting.");
            }
            throw new IllegalStateException("Pedestrian does not exist.");
        }
        pedestrianPositions.get(pedestrian).setLocation(globalPos);
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

    public void setFlexiBorderAlongX(boolean flexiBorderAlongX) {
        this.flexiBorderAlongX = flexiBorderAlongX;
    }

    public boolean isFlexiBorderAlongX(){ return this.flexiBorderAlongX;}

    public void addPedestriansQueueToEnter(Pedestrian pedestrian, PedestrianPoint globalEnterPoint, PedestrianStreetSection section){

        if(pedestriansQueueToEnter.size() >= 1000) { //TODO
            //throw new IllegalStateException("lower generation  of pedestrians que is too long.");
            double ad = 1;
        }

        pedestrian.enteringWaitingQue();
        synchronized(pedestriansQueueToEnter) {
            section.pedestriansQueueToEnter.add(new PedestrianWaitingListElement(pedestrian, globalEnterPoint, section));
        }
    }

    public Pedestrian reCheckPedestrianCanEnterSection() {
        /* TODo
        Pedestrian pedestrian = null;
        synchronized(pedestriansQueueToEnter) {
            for (PedestrianWaitingListElement pedestrianToEnter : pedestriansQueueToEnter) {
                // due to list fifo is considered
                if (!(pedestrianToEnter.getPedestrian() instanceof Pedestrian)) {
                    throw new IllegalStateException("type mismatch");
                }

                if (checkPedestrianCanEnterSection(pedestrianToEnter)) {
                    pedestrian = (Pedestrian) pedestrianToEnter.getPedestrian();
                    this.addPedestrian(pedestrianToEnter.getPedestrian(), pedestrianToEnter.getGlobalEnterPoint());
                    ((Pedestrian) pedestrianToEnter.getPedestrian()).setCurrentLocalPosition(); // do this after adding to street section

                    pedestriansQueueToEnter.remove(pedestrianToEnter);
                    pedestrian.leavingWaitingQue();
                    pedestriansQueueToEnter.notifyAll();

                    if (((Pedestrian) pedestrianToEnter.getPedestrian()).pedestriansQueueToEnterTimeStopWatch.isRunning()) {
                        double res = ((Pedestrian) pedestrianToEnter.getPedestrian()).pedestriansQueueToEnterTimeStopWatch.stop();
                        ((Pedestrian) pedestrianToEnter.getPedestrian()).pedestriansQueueToEnterTime.update(new TimeSpan(res));
                    }
                    return pedestrian;
                }
            }
        }
        return pedestrian;*/
        return null;
    }

    public boolean checkPedestrianCanEnterSection (PedestrianWaitingListElement element) {
        if (element.getPedestrian() instanceof Pedestrian && element.getSectionToMoveTo() instanceof PedestrianStreetSection){
            return checkPedestrianCanEnterSection((Pedestrian) element.getPedestrian(), element.getGlobalEnterPoint(), (PedestrianStreetSection) element.getSectionToMoveTo());
        }
        throw new IllegalStateException( "type mismatch");
    }

    public boolean checkPedestrianCanEnterSection (Pedestrian pedestrian, PedestrianPoint globalEnterPoint, PedestrianStreetSection section) {
        for ( IPedestrian otherPedestrian : pedestrianQueue) {
            if (!(otherPedestrian instanceof Pedestrian )) {
                throw new IllegalStateException("type mismatch");
            }
            double dist = calculations.getDistanceByCoordinates(pedestrian.getCurrentGlobalPosition(),  otherPedestrian.getCurrentGlobalPosition());
            if (dist < Math.max(pedestrian.getMinGapForPedestrian(),((Pedestrian) otherPedestrian).getMinGapForPedestrian())) {
                addPedestriansQueueToEnter(pedestrian, globalEnterPoint, section);
                return false;
            }
        }
        return true;
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public DTO toDTO() {
        return null;
    }
}
