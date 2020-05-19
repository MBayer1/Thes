package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.statistics.StopWatch;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation.*;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Count;
import desmoj.core.statistic.Tally;

import javax.vecmath.Vector2d;
import java.util.Iterator;

public class Pedestrian extends Entity implements IPedestrian {

    private final Car car; //keep existing structure as dummy for some specific function considered simulation
    private final IPedestrianRoute route;
    private final IPedestrianBehaviour pedestrianBehaviour;
    private final Iterator<PedestrianStreetSectionAndPortPair> routeIterator;
    private final StopWatch pedestrianStopWatch; // this was prepared for waiting areas
    private final Count pedestrianCounter; // this was prepared for waiting areas
    private final Tally pedestrianAreaTime; // this was prepared for waiting areas
    private final StopWatch pedestrianCrossingStopWatch;
    private final Count pedestrianCrossingCounter;
    private final Tally pedestrianCrossingTime;
    private double currentTimeSpendInSystem;

    private double lastUpdateTime;
    private PedestrianPoint currentGlobalPosition;
    private PedestrianPoint currentLocalPosition;

    public final Count pedestriansQueueToEnterCounter;
    public final Tally pedestriansQueueToEnterTime;
    public final StopWatch pedestriansQueueToEnterTimeStopWatch;

    private PedestrianStreetSection lastSection;
    private PedestrianStreetSectionAndPortPair currentSection;
    private PedestrianStreetSectionAndPortPair nextSection;
    private PedestrianStreetSectionAndPortPair sectionAfterNextSection;
    private Double walkedDistance;
    private double timeRelatedParameterValueNForSpeedCalculation;
    private PedestrianPoint currentNextGlobalAim;

    private Double startOfWalking;
    private Double futureEndOfWalking;
    private Double walkingSpeedByStartOfWalking;

    private Double enterSystemTime;
    private Double leaveSystemTime;

    SupportiveCalculations calc = new SupportiveCalculations();

    public Pedestrian(Model model, String name, boolean showInTrace, PedestrianPoint currentGlobalPosition, IPedestrianBehaviour pedestrianBehaviour, IPedestrianRoute route) {
        this(model, name, showInTrace, currentGlobalPosition, pedestrianBehaviour, route, 1.0);
    }

    public Pedestrian(Model model, String name, boolean showInTrace, PedestrianPoint currentGlobalPosition, IPedestrianBehaviour pedestrianBehaviour,
                      IPedestrianRoute route, double timeRelatedParameterValueNForSpeedCalculation)
            throws IllegalArgumentException {
        super(model, "name", showInTrace);

        this.currentGlobalPosition = currentGlobalPosition;

        if (pedestrianBehaviour != null) {
            this.pedestrianBehaviour = pedestrianBehaviour;
        } else {
           throw new IllegalArgumentException("Driver behaviour should not be null.");
        }

        if (route != null) {
            this.route = route;
            this.routeIterator = route.getRoute().iterator();
            // The below order is important!
            this.lastSection = null;
            this.currentSection = retrieveNextRouteSection();
            this.nextSection = retrieveNextRouteSection();
            this.sectionAfterNextSection = retrieveNextRouteSection();
            this.currentNextGlobalAim = null;
        } else {
            throw new IllegalArgumentException("Route should not be null.");
        }

        this.currentTimeSpendInSystem = 0.0;
        this.car = new Car(model, "pedestrianDummy", false);

        // Extended of Pedestrian speed -> also include stress factor.
        this.walkedDistance = 0.0;
        this.timeRelatedParameterValueNForSpeedCalculation = timeRelatedParameterValueNForSpeedCalculation;

        this.setLastUpdateTime();

        this.pedestrianStopWatch = new StopWatch(model);
        this.pedestrianCounter = new Count(model, "Roundabout counter", false, false);
        this.pedestrianCounter.reset();
        this.pedestrianAreaTime = new Tally(model, "Roundabout time", false, false);
        this.pedestrianAreaTime.reset();

        this.pedestrianCrossingStopWatch = new StopWatch(model);
        this.pedestrianCrossingCounter = new Count(model, "Roundabout counter", false, false);
        this.pedestrianCrossingCounter.reset();
        this.pedestrianCrossingTime = new Tally(model, "Roundabout time", false, false);
        this.pedestrianCrossingTime.reset();

        // coordinates are always at center of pedestrian, min gab simulates als the radius of pedestrian

        this.pedestriansQueueToEnterCounter = new Count(model, "Roundabout counter", false, false);
        this.pedestriansQueueToEnterCounter.reset();
        this.pedestriansQueueToEnterTime = new Tally(model, "Roundabout time", false, false);
        this.pedestriansQueueToEnterTimeStopWatch = new StopWatch(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastUpdateTime(double lastUpdateTime)
            throws IllegalArgumentException {
        if (lastUpdateTime >= 0) {
            this.lastUpdateTime = lastUpdateTime;
            currentTimeSpendInSystem += Math.abs(this.lastUpdateTime - lastUpdateTime);
        } else {
            throw new IllegalArgumentException("last update time must be positive");
        }
    }

    public void setLastUpdateTime(){
        setLastUpdateTime(getRoundaboutModel().getCurrentTime());
    }

    public void setFirstLastUpdateTime(){
        lastUpdateTime = getRoundaboutModel().getCurrentTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianPoint getNextSubGoal() {
        return getGlobalPositionOnExitPort();
    }

    public PedestrianPoint getGlobalNextSubGoal() {
        return currentNextGlobalAim;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTimeToNextSubGoal() {
        if (currentNextGlobalAim == null) {
            throw new IllegalArgumentException("No gaol defined.");
        }

        double calcResult = calc.getDistanceByCoordinates(currentNextGlobalAim.getX(),
                currentNextGlobalAim.getY(),
                currentGlobalPosition.getX(),
                currentGlobalPosition.getY()) / pedestrianBehaviour.getCurrentSpeed();

        if (Double.isInfinite(calcResult)) {
            calcResult = 0; // no current speed = waiting
        } else
        if(calcResult < 1 ) { // 1 time unit, in this case 1sec
            //throw new  IllegalStateException("Something went wrong. Suspicious transfer time of Pedestrian.");
        }

        return calcResult;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianPoint getCurrentGlobalPosition() {
        return currentGlobalPosition;
    }

    public double getRemainingDistanceToCurrentNextSubsoil() {
        if(currentNextGlobalAim == null) {
            setCurrentNextGlobalAim();
        }

        double distance = calc.getDistanceByCoordinates(currentGlobalPosition, currentNextGlobalAim);
        double walkedTime = getRoundaboutModel().getCurrentTime();
        walkedTime -= startOfWalking;
        double calculatedDistance = walkedTime * walkingSpeedByStartOfWalking;
        distance -= calculatedDistance;
        return Math.max(distance,0);
    }

    public PedestrianPoint getCurrentLocalPosition() {
        if (this.currentLocalPosition == null) {
            setCurrentLocalPosition();
        }
        return this.currentLocalPosition;
    }

    public void setCurrentGlobalPosition(PedestrianPoint currentGlobalPosition) {
        if (currentGlobalPosition == null) {
            throw new IllegalArgumentException("there is no global position set");
        }
        this.currentGlobalPosition = currentGlobalPosition;
        getRoundaboutModel().getPedestrianUIMain().updatePedestrian(this);
        setCurrentLocalPosition();

        IConsumer section = getCurrentSection().getStreetSection();
        if (!(section instanceof PedestrianStreet)) {
            throw new IllegalArgumentException("Section not instance of PedestrianStreet.");
        }
        ((PedestrianStreetSection) (section)).setPedestrianPosition(this, currentGlobalPosition);
    }

    public void setCurrentLocalPosition() {
        IConsumer section = getCurrentSection().getStreetSection();
        if (!(section instanceof PedestrianStreet)) {
            throw new IllegalArgumentException("Section not instance of PedestrianStreet.");
        }
        PedestrianPoint globalOffset = ((PedestrianStreetSection) (section)).getGlobalCoordinateOfSectionOrigin();
        PedestrianPoint localPos = new PedestrianPoint(currentGlobalPosition.getX() - globalOffset.getX(),
                currentGlobalPosition.getY() - globalOffset.getY());
        this.currentLocalPosition = localPos;
    }

    public PedestrianPoint getClosestExitPointOfCurrentSectionGlobal() {
        if(currentSection.getStreetSection() instanceof PedestrianSink){
            // special case
            throw new IllegalStateException("There is no next Goal as pedestrian left system.");
        }

        PedestrianStreetSection currentSection = (PedestrianStreetSection) this.getCurrentSection().getStreetSection();
        for (PedestrianConnectedStreetSections connectedStreetSections : currentSection.getNextStreetConnector()) {
            if (connectedStreetSections.getToStreetSection().equals(nextSection.getStreetSection())) {
                PedestrianStreetSectionPort localPort = connectedStreetSections.getPortOfFromStreetSection();
                double onBorderX, onBorderY;
                PedestrianPoint localPos = getCurrentLocalPosition();
                if (calc.almostEqual(localPort.getLocalBeginOfStreetPort().getX(), localPort.getLocalEndOfStreetPort().getX())) { // port along y side
                    onBorderX = localPort.getLocalBeginOfStreetPort().getX();
                    onBorderY = localPos.getY();
                } else { // port along x side
                    onBorderX = localPos.getX();
                    onBorderY = localPort.getLocalBeginOfStreetPort().getY();
                }

                PedestrianPoint intersection = calc.getLinesIntersectionByCoordinates(localPort,
                        localPos.getX(), localPos.getY(), onBorderX, onBorderY);
                if (intersection == null) intersection = new PedestrianPoint(onBorderX, onBorderY);  //localPos;

                if (!calc.checkWallIntersectionWithinPort(localPort, intersection)) {
                    intersection = calc.shiftIntersection(localPort, intersection, getMinGapForPedestrian());
                }

                PedestrianPoint globalBase = currentSection.getGlobalCoordinateOfSectionOrigin();
                intersection.setLocation(intersection.getX()+ globalBase.getX(),intersection.getY() + globalBase.getY());
                return intersection;
            }
        }
        throw new IllegalArgumentException("Pedestrian cannot leave section.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPedestrianBehaviour getPedestrianBehaviour() {
        return pedestrianBehaviour;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPedestrianRoute getRoute() {
        return route;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getCurrentSection() {
        return currentSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getNextSection() {
        return nextSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getSectionAfterNextSection() {
        return sectionAfterNextSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getDestination() {
        return route.getDestinationSection();
    }

    public RoundaboutSimulationModel getRoundaboutModel() {
        final Model model = car.getModel();
        if (model instanceof RoundaboutSimulationModel) {
            return (RoundaboutSimulationModel) model;
        } else {
            throw new IllegalArgumentException("Not suitable roundaboutSimulationModel.");
        }
    }

    private PedestrianStreetSectionAndPortPair retrieveNextRouteSection() {
        return routeIterator.hasNext() ? routeIterator.next() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterSystem() {
        getRoundaboutModel().getPedestrianUIMain().addPedestrian(this);
        car.enterSystem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double leaveSystem() {
        getRoundaboutModel().getPedestrianUIMain().removePedestrian(this);
        return car.leaveSystem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterPedestrianCrossing() {
        this.pedestrianCrossingCounter.update();
        this.pedestrianCrossingStopWatch.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leavePedestrianCrossing() {
        if (this.pedestrianCrossingStopWatch.isRunning()) {
            double res = this.pedestrianCrossingStopWatch.stop();
            this.pedestrianCrossingTime.update(new TimeSpan(res));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanStreetCrossingPassTime() {
        return this.pedestrianCrossingTime.getObservations() <= 0L ? 0.0D : this.pedestrianCrossingTime.getMean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterPedestrianArea() {
        this.pedestrianCounter.update();
        if(!this.pedestrianStopWatch.isRunning()) {
            this.pedestrianStopWatch.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leavePedestrianArea() {
        if(pedestrianStopWatch.isRunning()) {
            double res = this.pedestrianStopWatch.stop();
            this.pedestrianAreaTime.update(new TimeSpan(res));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMeanStreetAreaPassTime() {
        return this.pedestrianAreaTime.getObservations() <= 0L ? 0.0D : this.pedestrianAreaTime.getMean();
    }

    public void startWaiting() {
        if (!isWaiting()) {
            car.startWaiting();
        }
        if (!this.pedestrianStopWatch.isRunning()) {
            this.pedestrianStopWatch.start();
        }
    }

    public void stopWaiting() {
        if (this.pedestrianStopWatch.isRunning()) {
            this.pedestrianStopWatch.stop();
        } else {
            car.stopWaiting();
        }
    }

    public boolean isWaiting() {
        return car.isWaiting();
    }

    public double getTimeSpentInSystem() {
        return currentTimeSpendInSystem;
    }

    public double getMeanWaitingTime() {
        return car.getMeanWaitingTime();
    }

    public double getWaitingBeforeEnteringCount() {
        return this.pedestriansQueueToEnterCounter.getValue();
    }

    public double getMeanWaitingBeforeEnteringsTime() {
        return this.pedestriansQueueToEnterTime.getObservations() <= 0L ? 0.0D : this.pedestriansQueueToEnterTime.getMean();
    }

    public long getStopCount() {
        return car.getStopCount();
    }

    public double getCoveredDistanceInTime(double time) {
        return time * pedestrianBehaviour.getCurrentSpeed();
    }

    public double getCurrentSpeed() {
        return pedestrianBehaviour.getCurrentSpeed();
    }

    public void setCurrentSpeed(double currentSpeed) {
        if (!(pedestrianBehaviour instanceof PedestrianBehaviour)) {
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }

        CurrentMovementPedestrian previousMovement = ((PedestrianBehaviour) pedestrianBehaviour).getCurrentMovmentClass();
        CurrentMovementPedestrian currentMovement = Math.abs(currentSpeed - getPreferredMaxSpeed()) <
                Math.abs(currentSpeed - getPreferredMaxSpeed())
                ? CurrentMovementPedestrian.Walking : CurrentMovementPedestrian.Running;

        CurrentMovementPedestrian newValue = CurrentMovementPedestrian.Walking; // previousMovment && currentMovement are WALKING
        if ( previousMovement.equals(CurrentMovementPedestrian.Running) && currentMovement.equals(CurrentMovementPedestrian.Running)) {
            newValue = CurrentMovementPedestrian.Running;
        } else if (previousMovement.equals(CurrentMovementPedestrian.Running) && currentMovement.equals(CurrentMovementPedestrian.Walking)){
            newValue = CurrentMovementPedestrian.WalkingAfterRunning;
        } else if(previousMovement.equals(CurrentMovementPedestrian.Walking) && currentMovement.equals(CurrentMovementPedestrian.Running)) {
            newValue = CurrentMovementPedestrian.RunningAfterWalking;
        }

        ((PedestrianBehaviour)pedestrianBehaviour).setCurrentMovmentClass(newValue);
        pedestrianBehaviour.setCurrentSpeed(currentSpeed);
    }

    public CurrentMovementPedestrian getCurrentMovementType () {
        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }
        return ((PedestrianBehaviour)pedestrianBehaviour).getCurrentMovmentClass();
    }

    public double getPreferredSpeed() {
        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }
        return ((PedestrianBehaviour)pedestrianBehaviour).getPreferredSpeed();
    }

    public double getPreferredMaxSpeed() {
        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }
        return ((PedestrianBehaviour)pedestrianBehaviour).getMaxPreferredSpeed();
    }

    public double calculatePreferredSpeed() {
        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }

        double averageSpeed = getTimeSpentInSystem() == 0 ? 0 : walkedDistance / getTimeSpentInSystem();
        double timeRelatedParameter = averageSpeed / pedestrianBehaviour.getCurrentSpeed();
        timeRelatedParameter = timeRelatedParameterValueNForSpeedCalculation - timeRelatedParameter;

        double part1 = (1 - timeRelatedParameter) * pedestrianBehaviour.getCurrentSpeed();
        double part2 = timeRelatedParameter * ((PedestrianBehaviour)pedestrianBehaviour).getMaxPreferredSpeed();
        if( Double.isNaN(part1)) part1 = 0;
        if( Double.isNaN(part2)) part2 = 0;

        pedestrianBehaviour.setCurrentSpeed(part1 + part2);
        walkingSpeedByStartOfWalking = pedestrianBehaviour.getCurrentSpeed();
        return pedestrianBehaviour.getCurrentSpeed();
    }

    public PedestrianPoint transferToNextPortPos() {
        PedestrianStreetSectionPort exitPort = currentSection.getExitPort();
        PedestrianStreetSectionPort enterPort = nextSection.getEnterPort();
        double high = 0;
        PedestrianPoint pos = null;
        PedestrianPoint exitPointOnPort = getClosestExitPointOfCurrentSectionGlobal();

        // exit Port along y axis
        if (calc.almostEqual(exitPort.getLocalBeginOfStreetPort().getX(), exitPort.getLocalEndOfStreetPort().getX())) {
            //start lower value than end
            if (calc.val1LowerOrAlmostEqual(enterPort.getLocalBeginOfStreetPort().getY(), enterPort.getLocalEndOfStreetPort().getY())) {
                high = exitPointOnPort.getY() - enterPort.getLocalBeginOfStreetPort().getY();
                pos = new PedestrianPoint(enterPort.getLocalBeginOfStreetPort().getX(), enterPort.getLocalBeginOfStreetPort().getY() + high);
            } else {// end lower value than start
                high = exitPointOnPort.getY() - enterPort.getLocalEndOfStreetPort().getY();
                pos = new PedestrianPoint(enterPort.getLocalEndOfStreetPort().getX(), enterPort.getLocalEndOfStreetPort().getY() + high);
            }
        } else { // exit Port along x axis
            if (calc.val1LowerOrAlmostEqual(enterPort.getLocalBeginOfStreetPort().getX(), enterPort.getLocalEndOfStreetPort().getX())) {
                high = exitPointOnPort.getX() - enterPort.getLocalBeginOfStreetPort().getX();
                pos = new PedestrianPoint(enterPort.getLocalBeginOfStreetPort().getX() + high, enterPort.getLocalBeginOfStreetPort().getY());
            } else {// end lower value than start
                high = exitPointOnPort.getX() - enterPort.getLocalEndOfStreetPort().getX();
                pos = new PedestrianPoint(enterPort.getLocalEndOfStreetPort().getX() + high, enterPort.getLocalEndOfStreetPort().getY());
            }
        }
        return pos;
    }

    public boolean checkForWaitingArea() {

        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }

        if ((nextSection.getStreetSection() instanceof PedestrianStreetSection) &&
                ((PedestrianStreetSection)(nextSection.getStreetSection())).isTrafficLightActive() &&
                !((PedestrianStreetSection)(nextSection.getStreetSection())).useMassDynamic()) {
            if (!((PedestrianStreetSection)(nextSection.getStreetSection())).isTrafficLightFreeToGo()) {
                double  distance = calc.getDistanceByCoordinates(currentGlobalPosition, getNextSubGoal());
                if (distance < ((PedestrianBehaviour)pedestrianBehaviour).getMaxDistanceForWaitingArea()) return true;
            }
        }
        return false;
    }

    public boolean checkExitPortIsReached(){
        setCurrentLocalPosition();
        return checkExitPortIsReached(this.currentLocalPosition);
    }

    public boolean checkExitPortIsReached(PedestrianPoint localPedestrianPosition) {
        // do not check for min gab as this is considered at getGlobalPositionOnExitPort()
        if (localPedestrianPosition == null) {
            throw new IllegalArgumentException(" no pedestrianPosition passed.");
        }

        PedestrianPoint localBegin = currentSection.getExitPort().getLocalBeginOfStreetPort();
        PedestrianPoint localEnd = currentSection.getExitPort().getLocalEndOfStreetPort();

        // Port is along y axis
        if (calc.almostEqual(localBegin.getX(), localEnd.getX())) {
            if (( ( calc.val1LowerOrAlmostEqual((localBegin.getY()), localPedestrianPosition.getY(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((localEnd.getY()), localPedestrianPosition.getY(), 10e-1))
                    ||
                    calc.val1LowerOrAlmostEqual((localEnd.getY()), localPedestrianPosition.getY(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((localBegin.getY()), localPedestrianPosition.getY(), 10e-1))

                    &&
                    calc.almostEqual(localBegin.getX(), localPedestrianPosition.getX(), 1.0)
                    ) {
                return true;
            }
        } // Port is along x axis
        else {
            if (((calc.val1LowerOrAlmostEqual((localBegin.getX()), localPedestrianPosition.getX(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((localEnd.getX()), localPedestrianPosition.getX(), 10e-1))
                    ||
                    calc.val1LowerOrAlmostEqual((localEnd.getX()), localPedestrianPosition.getX(), 10e-1) &&
                            calc.val1BiggerOrAlmostEqual((localBegin.getX()), localPedestrianPosition.getX(), 10e-1))

                    &&
                    calc.almostEqual(localBegin.getY(), localPedestrianPosition.getY(), 1.0)
                    ) {
                return true;
            }
        }
        return false;
    }

    public PedestrianPoint getGlobalPositionOnExitPort() {
        // if an orthogonal form Sink Port to PedestrianPoint match the intersection of the orthogonal is the enter PedestrianPoint.
        // else check if it is above or beneath  - subtract pedestrian size
        PedestrianPoint pos = new PedestrianPoint();

        double globalX = ((PedestrianStreetSection)(currentSection.getStreetSection())).getGlobalCoordinateOfSectionOrigin().getX();
        double globalY = ((PedestrianStreetSection)(currentSection.getStreetSection())).getGlobalCoordinateOfSectionOrigin().getY();
        double globalBeginX = currentSection.getExitPort().getLocalBeginOfStreetPort().getX() + globalX;
        double globalBeginY = currentSection.getExitPort().getLocalBeginOfStreetPort().getY() + globalY;
        double globalEndX = currentSection.getExitPort().getLocalEndOfStreetPort().getX() + globalX;
        double globalEndY = currentSection.getExitPort().getLocalEndOfStreetPort().getY() + globalY;
        double minGapForPedestrian = getMinGapForPedestrian();

        // Port is along y axis
       if (calc.almostEqual(globalBeginX,globalEndX)) {
            if ( (globalBeginY < globalEndY && currentGlobalPosition.getY() >= (globalBeginY + minGapForPedestrian) &&
                    currentGlobalPosition.getY() <= (globalEndY - minGapForPedestrian) ) ||
                 (globalBeginY > globalEndY && currentGlobalPosition.getY() >= (globalEndY + minGapForPedestrian) &&
                         currentGlobalPosition.getY() <= (globalBeginY - minGapForPedestrian) )   ) {
                //pedestrian high is withing port?
                pos.setLocation(globalBeginX, currentGlobalPosition.getY());
            } else
            if (Math.abs(currentGlobalPosition.getY() - globalBeginY) < Math.abs(currentGlobalPosition.getY() - globalEndY)) { // check if it is closer to end or begin
                //closer to begin exit port
                //now moving intersection into the port about the min Gap
                if (globalBeginY < globalEndY ) {
                    pos.setLocation(globalBeginX, globalBeginY + minGapForPedestrian);
                }else {
                    pos.setLocation(globalBeginX, globalBeginY - minGapForPedestrian);
                }
            } else {
                //closer to end of exit Port
                //now moving intersection into the port about the min Gap
                if (globalBeginY > globalEndY ) {
                    pos.setLocation(globalEndX, globalEndY + minGapForPedestrian);
                }else {
                    pos.setLocation(globalEndX, globalEndY - minGapForPedestrian);
                }
            }

        } // Port is along x axis
        else {
           if ( (globalBeginX < globalEndX && currentGlobalPosition.getX() >= (globalBeginX + minGapForPedestrian) &&
                   currentGlobalPosition.getX() <= (globalEndX - minGapForPedestrian) ) ||
                   (globalBeginX > globalEndX && currentGlobalPosition.getX() >= (globalEndX + minGapForPedestrian) &&
                           currentGlobalPosition.getX() <= (globalBeginX - minGapForPedestrian) )   ) {
               //pedestrian high is withing port?
               pos.setLocation(globalBeginX, currentGlobalPosition.getX());
           } else
           if (Math.abs(currentGlobalPosition.getX() - globalBeginX) < Math.abs(currentGlobalPosition.getX() - globalEndX)) { // check if it is closer to end or begin
               //closer to begin exit port
               //now moving intersection into the port about the min Gap
               if (globalBeginX < globalEndX ) {
                   pos.setLocation(globalBeginX  + minGapForPedestrian, globalBeginY);
               }else {
                   pos.setLocation(globalBeginX  - minGapForPedestrian, globalBeginY);
               }
           } else {
               //closer to end of exit Port
               //now moving intersection into the port about the min Gap
               if (globalBeginX > globalEndX ) {
                   pos.setLocation(globalEndX + minGapForPedestrian, globalEndY);
               }else {
                   pos.setLocation(globalEndX - minGapForPedestrian, globalEndY);
               }
           }
        }

        return pos;
    }

    public void updateWalkedDistance() {
        this.walkedDistance += calc.getDistanceByCoordinates(currentGlobalPosition.getX(), currentGlobalPosition.getY(),
                getGlobalNextSubGoal().getX(), getGlobalNextSubGoal().getY());
    }

    public Double getWalkedDistance() {
        return walkedDistance;
    }


    public PedestrianPoint getGlobalCoordinatesOfCurrentSection() {
        return ((PedestrianStreetSection) currentSection.getStreetSection()).getGlobalCoordinateOfSectionOrigin();
    }

    public void moveOneSectionForward(PedestrianPoint newLocalPos) {
        ((PedestrianStreet) currentSection.getStreetSection()).removePedestrian(this);
        ((PedestrianStreet) nextSection.getStreetSection()).addPedestrian(this, newLocalPos);

        currentSection = nextSection;
        nextSection = sectionAfterNextSection;
        sectionAfterNextSection = retrieveNextRouteSection();

        if(! (currentSection.getStreetSection() instanceof PedestrianSink)) {
            PedestrianPoint globalCoordinateOfSectionOrigin = getGlobalCoordinatesOfCurrentSection();
            setCurrentGlobalPosition(new PedestrianPoint(
                    newLocalPos.getX() + globalCoordinateOfSectionOrigin.getX(),
                    newLocalPos.getY() + globalCoordinateOfSectionOrigin.getY()));

        }
    }


    public void setCurrentNextGlobalAim() {
        setCurrentNextGlobalAim( getClosestExitPointOfCurrentSectionGlobal());
    }
    public void setCurrentNextGlobalAim(PedestrianPoint currentNextGlobalAim) {
        this.currentNextGlobalAim = currentNextGlobalAim;
    }

    public PedestrianPoint getCurrentNextGlobalAim() {
        return currentNextGlobalAim;
    }

    public Car getCarDummy() {
        return this.car;
    }

    public Vector2d getPreviousSFMVector() {
        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }

        return ((PedestrianBehaviour)pedestrianBehaviour).getPreviousSFMVector();
    }

    public void setPreviousSFMVector(Vector2d previousSFMVector) {
        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }

        ((PedestrianBehaviour)pedestrianBehaviour).setPreviousSFMVector(previousSFMVector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2d getSocialForceVector() {

        getRoundaboutModel().getSFM_DegreeOfAccuracy();
        Vector2d sumForce = new Vector2d();
        AccelerationForceToTarget accelerationForceToTarget = new AccelerationForceToTarget();
        RepulsiveForceAgainstOtherPedestrians repulsiveForceAgainstOtherPedestrians = new RepulsiveForceAgainstOtherPedestrians();
        RepulsiveForceAgainstObstacles repulsiveForceAgainstObstacles = new RepulsiveForceAgainstObstacles();
        RepulsiveForceAgainstVehicles repulsiveForceAgainstVehicles = new RepulsiveForceAgainstVehicles();


        Vector2d tmp = accelerationForceToTarget.getAccelerationForceToTarget(getRoundaboutModel(), this);
        sumForce.add(tmp);
        tmp = repulsiveForceAgainstOtherPedestrians.getRepulsiveForceAgainstAllOtherPedestrians(getRoundaboutModel(), this, getNextSubGoal());
        sumForce.add(tmp);
        tmp = repulsiveForceAgainstObstacles.getRepulsiveForceAgainstAllObstacles(getRoundaboutModel(), this);
        sumForce.add(tmp);
        tmp = repulsiveForceAgainstVehicles.getRepulsiveForceAgainstVehicles(getRoundaboutModel(), this);
        sumForce.add(tmp);
        this.setPreviousSFMVector(sumForce);
        return sumForce;
    }

    public double getMinGapForPedestrian() {
        return pedestrianBehaviour.calcGapForPedestrian();
    }

    public double getStartOfWalking(){
        return startOfWalking;

    }

    public double getFutureEndOfWalking(){
        return futureEndOfWalking;
    }

    public void setWalkingTimeStamps( double timeOfDestination ){
        this.startOfWalking = getRoundaboutModel().getCurrentTime();
        this.futureEndOfWalking = this.startOfWalking + timeOfDestination;
    }

    public void setNewGoal( Vector2d forces ){
        this.setCurrentNextGlobalAim();// set as first aim the exit PedestrianPoint of the street section
        PedestrianStreet section = ((PedestrianStreet)this.getCurrentSection().getStreetSection());
        PedestrianPoint curGlobPos = this.getCurrentGlobalPosition();
        PedestrianPoint globalGoal = checkAndSetAimWithinSection(forces, section);

        for (IPedestrian otherPedestrian : section.getPedestrianQueue()){ // check for intersections with other pedestrians
            if(otherPedestrian.equals(this)) continue;
            if (! (otherPedestrian instanceof Pedestrian )) {
                throw new IllegalArgumentException( "Other pedestrian not instance of Pedestrian." );
            }
            double minGab = Math.max(this.getMinGapForPedestrian(), ((Pedestrian) otherPedestrian).getMinGapForPedestrian());

            // if an other pedestrian does already have a defined "current global aim" and
            // does have an intersection with the current aim of the current pedestrian
            // the "current global aim" of the current pedestrian is cut down to it.
            // Otherwise the end of the section is the aim.
            // Afterwards the time until this destination is reached is calculated.
            if( ((Pedestrian)otherPedestrian).getCurrentNextGlobalAim() != null) {
                if(calc.checkLinesIntersectionByCoordinates_WithinSegment(globalGoal,
                        this.getCurrentGlobalPosition(),
                        this.getCurrentGlobalPosition().getX() + forces.getX(), this.getCurrentGlobalPosition().getY() + forces.getY(),
                        otherPedestrian.getCurrentGlobalPosition(),
                        ((Pedestrian) otherPedestrian).getCurrentNextGlobalAim())) {
                    // there is a crossing
                    if(!checkTimeOfIntersectionMatches( globalGoal, (Pedestrian)otherPedestrian)) continue;

                    // crossing - the size of the current pedestrian = new destination aim
                    if (calc.getDistanceByCoordinates(globalGoal.getX() - minGab, globalGoal.getY() - minGab, curGlobPos.getX(), curGlobPos.getY() )
                            < calc.getDistanceByCoordinates(globalGoal.getX() + minGab, globalGoal.getY() + minGab, curGlobPos.getX(), curGlobPos.getY() )) {
                        globalGoal.setLocation(globalGoal.getX() - minGab, globalGoal.getY() - minGab);
                    } else {
                        globalGoal.setLocation(globalGoal.getX() + minGab, globalGoal.getY() + minGab);
                    }
                    this.setCurrentNextGlobalAim(globalGoal);
                }
            }
        }
    }

    Boolean checkTimeOfIntersectionMatches( PedestrianPoint intersection, Pedestrian otherPedestrian){
        // get from last update time to time to intersection and
        // compare simulated time of both pedestrians when reaching intersection coordinates.
        double pedestrianReachedIntersectionTime = this.getLastUpdateTime() +
                (calc.getDistanceByCoordinates(this.getCurrentGlobalPosition(), intersection)/this.getCurrentSpeed());
        double otherPedestrianReachedIntersectionTime = otherPedestrian.getLastUpdateTime() +
                (calc.getDistanceByCoordinates(otherPedestrian.getCurrentGlobalPosition(), intersection)/otherPedestrian.getCurrentSpeed());

        // convert distance min gab in to time min time gab
        double tolerance = Math.max(otherPedestrian.getMinGapForPedestrian()/otherPedestrian.getCurrentSpeed(),
                this.getMinGapForPedestrian()/this.getCurrentSpeed());

        if ( calc.almostEqual(pedestrianReachedIntersectionTime, otherPedestrianReachedIntersectionTime, tolerance)) {
            return true;
        }
        return false;
    }


    PedestrianPoint checkAndSetAimWithinSection(Vector2d forces, PedestrianStreet section) {
        // this is needed to ensure every aim is within a street Section
        // move not further as the distance to direct aim
        double distance = calc.getDistanceByCoordinates(this.getCurrentGlobalPosition(), this.getCurrentNextGlobalAim());
        Vector2d uniVecForces = calc.getUnitVector(forces);
        uniVecForces.scale(distance);
        PedestrianPoint globPedPos = this.getCurrentGlobalPosition();

        // change aim with consideration of social force since this is the path pedestrian is walking
        PedestrianPoint globalGoal = new PedestrianPoint(
                globPedPos.getX() + uniVecForces.getX(),
                globPedPos.getY() + uniVecForces.getY());

        if (! (section instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException ("Type mismatch.");
        }

        PedestrianPoint cornerLeftBottomGlobal = ((PedestrianStreetSection) section).getGlobalCoordinateOfSectionOrigin();
        PedestrianPoint cornerLeftUpGlobal = new PedestrianPoint( cornerLeftBottomGlobal.getX(), cornerLeftBottomGlobal.getY() +  section.getLengthY());
        PedestrianPoint cornerRightBottomGlobal = new PedestrianPoint( cornerLeftBottomGlobal.getX() +  section.getLengthX(), cornerLeftBottomGlobal.getY());
        PedestrianPoint cornerRightUpGlobal = new PedestrianPoint( cornerLeftBottomGlobal.getX() +  section.getLengthX(), cornerLeftBottomGlobal.getY() +  section.getLengthY());
        int borderLineNr = 0;
        PedestrianPoint intersection = new PedestrianPoint(globalGoal.getX(), globalGoal.getY());

        if(! goalWithinSection(cornerLeftBottomGlobal, cornerRightUpGlobal, globalGoal)){
            // not within section
            boolean withinSection = true;
            if( calc.checkLinesIntersectionByCoordinates_WithinSegment(
                    intersection,
                    globPedPos,
                    globalGoal,
                    cornerLeftBottomGlobal, cornerLeftUpGlobal
            )){
                if( calc.getDistanceByCoordinates(globPedPos, intersection) >
                        calc.getDistanceByCoordinates(globalGoal, intersection)) {
                    withinSection = false;
                    borderLineNr = 1;
                }
            }
            if(withinSection &&
                    calc.checkLinesIntersectionByCoordinates_WithinSegment(
                            intersection,
                            globPedPos,
                            globalGoal,
                            cornerLeftBottomGlobal, cornerRightBottomGlobal
                    )){
                if( calc.getDistanceByCoordinates(globPedPos, intersection) >
                        calc.getDistanceByCoordinates(globalGoal, intersection)) {
                    withinSection = false;
                    borderLineNr = 2;
                }
            }
            if(withinSection &&
                    calc.checkLinesIntersectionByCoordinates_WithinSegment(
                            intersection,
                            globPedPos,
                            globalGoal,
                            cornerLeftUpGlobal, cornerRightUpGlobal
                    )){
                if( calc.getDistanceByCoordinates(globPedPos, intersection) >
                        calc.getDistanceByCoordinates(globalGoal, intersection)) {
                    withinSection = false;
                    borderLineNr = 3;
                }
            }
            if(withinSection &&
                    calc.checkLinesIntersectionByCoordinates_WithinSegment(
                            intersection,
                            globPedPos,
                            globalGoal,
                            cornerRightBottomGlobal, cornerRightUpGlobal
                    )){
                if( calc.getDistanceByCoordinates(globPedPos, intersection) >
                        calc.getDistanceByCoordinates(globalGoal, intersection)) {
                    withinSection = false;
                    borderLineNr = 4;
                }
            }

            this.setCurrentNextGlobalAim(intersection);
            if ( withinSection ) return globalGoal;
        }

        if( section.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)) {
            boolean crossingAllowed = false;
            // in this case it might be possible to cross over border
            if(((PedestrianStreetSection) section).isFlexiBorderAlongX() && borderLineNr == 2 && borderLineNr == 3){
                crossingAllowed = true;
            } else if (borderLineNr == 1 && borderLineNr == 4){
                crossingAllowed = true;
            }

            if(crossingAllowed) return globalGoal;
        }

        /*
        // overwrite wall intersection as new aim -> not crossing wall
        double minGab = pedestrian.getMinGapForPedestrian();
        if (calc.getDistanceByCoordinates(intersection.getX() - minGab, intersection.getX() - minGab, globalGoal.getX(), globalGoal.getY() )
                < calc.getDistanceByCoordinates(intersection.getX() + minGab, intersection.getX() + minGab, globalGoal.getX(), globalGoal.getY() )) {
            return new PedestrianPoint( intersection.getX() - minGab, intersection.getY() - minGab);
        } else {
            return new PedestrianPoint( intersection.getX() + minGab, intersection.getY() + minGab );
        }*/
        return intersection;
    }

    public boolean goalWithinSection (PedestrianPoint cornerLeftBottomGlobal, PedestrianPoint cornerRightUpGlobal,
                                      PedestrianPoint goal){
        boolean checkX = calc.val1LowerOrAlmostEqual(cornerLeftBottomGlobal.getX(),goal.getX()) &&
                calc.val1BiggerOrAlmostEqual(cornerRightUpGlobal.getX(), goal.getX());
        boolean checkY= calc.val1LowerOrAlmostEqual(cornerLeftBottomGlobal.getY(),goal.getY()) &&
                calc.val1BiggerOrAlmostEqual(cornerRightUpGlobal.getY(), goal.getY());

        if(checkX && checkY) return true;
        return false;
    }

    public double getMaxDistanceForWaitingArea(){
        if(!(pedestrianBehaviour instanceof PedestrianBehaviour)){
            throw new IllegalStateException("Type miss match: behaviour not instance of PedestrianBehaviour.");
        }
        return ((PedestrianBehaviour)pedestrianBehaviour).getMaxDistanceForWaitingArea();
    }

    public void setWalkingSpeedByStartOfWalking ( double walkingSpeedByStartOfWalking) {
        this.walkingSpeedByStartOfWalking = walkingSpeedByStartOfWalking;
    }

    public double getWalkingSpeedByStartOfWalking () {
        return this.walkingSpeedByStartOfWalking;
    }

}
