package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.statistics.StopWatch;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.*;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Count;
import desmoj.core.statistic.Tally;

import javax.vecmath.Vector2d;
import java.util.Iterator;

public class Pedestrian extends Entity implements IPedestrian {

    private final Car car; //keep existing structure as dummy for some specific function considered simulation
    private final Double preferredSpeed;
    private final Double maxPreferredSpeed;
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
    private Double currentSpeed;
    private Double walkedDistance;
    private double timeRelatedParameterValueNForSpeedCalculation;
    private PedestrianPoint currentNextGlobalAim;
    private Vector2d previousSFMVector;

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
        double val1 = getRoundaboutModel().getRandomPedestrianPreferredSpeed();
        double val2 = getRoundaboutModel().getRandomPedestrianPreferredSpeed();
        this.preferredSpeed = Math.min(val1, val2);
        this.maxPreferredSpeed = Math.max(val1, val2);
        this.walkedDistance = 0.0;
        this.timeRelatedParameterValueNForSpeedCalculation = timeRelatedParameterValueNForSpeedCalculation;

        this.setLastUpdateTime(getRoundaboutModel().getCurrentTime());

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

        this.currentSpeed = this.getPreferredSpeed();
        // coordinates are always at center of pedestrian, min gab simulates als the radius of pedestrian

        this.pedestriansQueueToEnterCounter = new Count(model, "Roundabout counter", false, false);
        this.pedestriansQueueToEnterCounter.reset();
        this.pedestriansQueueToEnterTime = new Tally(model, "Roundabout time", false, false);
        this.pedestriansQueueToEnterTimeStopWatch = new StopWatch(model);

        this.previousSFMVector = new Vector2d(0,0);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianPoint getNextSubGoal() {
        return getGlobalPositionOnExitPort();
    }

    public PedestrianPoint getGlobalNextSubGoal() {
        return currentNextGlobalAim;
        /*
        if (!(getCurrentSection().getStreetSection() instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException("Street section not instance of PedestrianStreetSection.");
        }

        PedestrianPoint exitPoint = getGlobalPositionOnExitPort();
        PedestrianPoint global = ((PedestrianStreetSection) getCurrentSection().getStreetSection()).getGlobalCoordinateOfSectionOrigin();
        PedestrianPoint globalGoal = new PedestrianPoint((int) (exitPoint.getX() + global.getX()),
                (int) (exitPoint.getY() + global.getY()));
        return globalGoal;*/

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTimeToNextSubGoal() {
        if (currentNextGlobalAim == null) return 0;
        return calc.getDistanceByCoordinates(currentNextGlobalAim.getX(),
                currentNextGlobalAim.getY(),
                currentGlobalPosition.getX(),
                currentGlobalPosition.getY()) * getCurrentSpeed();
    }

    public double getTimeToNextGlobalSubGoalByCoordinates(PedestrianPoint goalLocal) {
        if (goalLocal == null) {
            throw new IllegalArgumentException("No gaol defined.");
        }

        PedestrianPoint origin = ((PedestrianStreetSection) getCurrentSection().getStreetSection()).getGlobalCoordinateOfSectionOrigin();

        return calc.getDistanceByCoordinates(goalLocal.getX() + origin.getX(),
                goalLocal.getY() + origin.getY(),
                currentGlobalPosition.getX(),
                currentGlobalPosition.getY()) * calculatePreferredSpeed();
    }

    public double getTimeToNextGlobalSubGoal() {
        if (currentNextGlobalAim == null) {
            throw new IllegalArgumentException("No gaol defined.");
        }
        return calc.getDistanceByCoordinates(currentNextGlobalAim.getX(),
                currentNextGlobalAim.getY(),
                currentGlobalPosition.getX(),
                currentGlobalPosition.getY()) * calculatePreferredSpeed();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianPoint getCurrentGlobalPosition() {
        return currentGlobalPosition;
    }

    public PedestrianPoint getCurrentLocalPosition() {
        return this.currentLocalPosition;
    }

    public void setCurrentGlobalPosition(PedestrianPoint currentGlobalPosition) {
        if (currentGlobalPosition == null) {
            throw new IllegalArgumentException("there is no global position set");
        }
        this.currentGlobalPosition = currentGlobalPosition;
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
        PedestrianStreetSection currentSection = (PedestrianStreetSection) this.getCurrentSection().getStreetSection();
        for (PedestrianConnectedStreetSections connectedStreetSections : currentSection.getNextStreetConnector()) {
            if (connectedStreetSections.getToStreetSection().equals(nextSection.getStreetSection())) {
                PedestrianStreetSectionPort localPort = connectedStreetSections.getPortOfFromStreetSection();
                double onBorderX, onBorderY;
                PedestrianPoint localPos = getCurrentLocalPosition();
                if (calc.almostEqual(localPort.getLocalBeginOfStreetPort().getX(), localPort.getLocalEndOfStreetPort().getX())) { // port along y side
                    onBorderX = localPort.getLocalBeginOfStreetPort().getX(); // Min of border
                    onBorderY = localPos.getY();
                } else { // port along x side
                    onBorderX = localPos.getX();
                    onBorderY = localPort.getLocalBeginOfStreetPort().getY(); // Min of border
                }

                PedestrianPoint intersection = calc.getLinesIntersectionByCoordinates(localPort, localPos.getX(), localPos.getY(), onBorderX, onBorderY);

                if (intersection == null || !calc.checkWallIntersectionWithinPort(localPort, intersection)) {
                    if (intersection == null) intersection = localPos;
                    calc.shiftIntersection(localPort, intersection, getMinGapForPedestrian());
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

    private RoundaboutSimulationModel getRoundaboutModel() {
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
        car.enterSystem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double leaveSystem() {
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
        //this.pedestrianStopWatch.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leavePedestrianArea() {
        double res = this.pedestrianStopWatch.stop();
        this.pedestrianAreaTime.update(new TimeSpan(res));
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
        return time * pedestrianBehaviour.getSpeed();
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public double getPreferredSpeed() {
        return this.preferredSpeed;
    }

    public double calculatePreferredSpeed() {
        double averageSpeed = getTimeSpentInSystem() == 0 ? 0 : walkedDistance / getTimeSpentInSystem();
        double timeRelatedParameter = averageSpeed / preferredSpeed;
        timeRelatedParameter = timeRelatedParameterValueNForSpeedCalculation - timeRelatedParameter;

        double part1 = (1 - timeRelatedParameter) * preferredSpeed;
        double part2 = timeRelatedParameter * maxPreferredSpeed;

        return part1 + part2;
    }

    public PedestrianPoint transferToNextPortPos() {
        PedestrianStreetSectionPort exitPort = currentSection.getExitPort();
        PedestrianStreetSectionPort enterPort = nextSection.getEnterPort();
        double high = 0;
        PedestrianPoint pos = null;
        PedestrianPoint exitPointOnPort = getClosestExitPointOfCurrentSectionGlobal();

        // exit Port along x axis
        if (calc.almostEqual(exitPort.getLocalBeginOfStreetPort().getX(), exitPort.getLocalEndOfStreetPort().getX())) {
            //start lower value than end
            if (calc.val1LowerOrAlmostEqual(enterPort.getLocalBeginOfStreetPort().getY(), enterPort.getLocalEndOfStreetPort().getY())) {
                high = exitPointOnPort.getY() - enterPort.getLocalBeginOfStreetPort().getY();
                pos = new PedestrianPoint(enterPort.getLocalBeginOfStreetPort().getX(), enterPort.getLocalBeginOfStreetPort().getY() + high);
            } else {// end lower value than start
                high = exitPointOnPort.getY() - enterPort.getLocalEndOfStreetPort().getY();
                pos = new PedestrianPoint(enterPort.getLocalEndOfStreetPort().getX(), enterPort.getLocalEndOfStreetPort().getY() + high);
            }
        } else { // exit Port along y axis
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

    public boolean checkGlobalGoalIsReached() {
        if (currentGlobalPosition == null) {
            throw new IllegalArgumentException(" no pedestrianPosition passed.");
        }

        if (!(currentSection.getStreetSection() instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException(" Section not instance of PedestrianStreetSection");
        }

        double distance = calc.getDistanceByCoordinates(currentNextGlobalAim.getX(), currentNextGlobalAim.getY(),
                currentGlobalPosition.getX(), currentGlobalPosition.getY());

        return calc.almostEqual(distance, 0);
    }


    public boolean checkExitPortIsReached(){
        return checkExitPortIsReached(this.currentLocalPosition);
    }

    public boolean checkExitPortIsReached(double x, double y) {
        PedestrianPoint pos = new PedestrianPoint(x, y);
        return checkExitPortIsReached(pos);
    }

    public boolean checkExitPortIsReached(PedestrianPoint localPedestrianPosition) {
        // do not check for min gab as this is considered at getGlobalPositionOnExitPort()
        if (localPedestrianPosition == null) {
            throw new IllegalArgumentException(" no pedestrianPosition passed.");
        }

        // Port is along y axis
        if (calc.almostEqual(currentSection.getExitPort().getLocalBeginOfStreetPort().getX(),
                currentSection.getExitPort().getLocalEndOfStreetPort().getX())) {

            boolean da1 = calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getLocalBeginOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1);
            boolean da2 = calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getLocalEndOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1);
            boolean da = da1 && da2;

            boolean dd1 = calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getLocalEndOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1);
            boolean dd2 = calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getLocalBeginOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1);
            boolean dd = dd1 && dd2;

            boolean ddd = calc.almostEqual(currentSection.getExitPort().getLocalBeginOfStreetPort().getX(), localPedestrianPosition.getX(), 1.0);

            if (( ( calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getLocalBeginOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getLocalEndOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1))
                    ||
                    calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getLocalEndOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getLocalBeginOfStreetPort().getY()), localPedestrianPosition.getY(), 10e-1))

                    &&
                    calc.almostEqual(currentSection.getExitPort().getLocalBeginOfStreetPort().getX(), localPedestrianPosition.getX(), 1.0)
                    ) {
                return true;
            }
        } // Port is along x axis
        else {
            if (((calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getLocalBeginOfStreetPort().getX()), localPedestrianPosition.getX(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getLocalEndOfStreetPort().getX()), localPedestrianPosition.getX(), 10e-1))
                    ||
                    calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getLocalEndOfStreetPort().getX()), localPedestrianPosition.getX(), 10e-1) &&
                            calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getLocalBeginOfStreetPort().getX()), localPedestrianPosition.getX(), 10e-1))

                    &&
                    calc.almostEqual(currentSection.getExitPort().getLocalBeginOfStreetPort().getY(), localPedestrianPosition.getY(), 1.0)
                    ) {
                return true;
            }
        }
        return false;
    }

    public void setPositionOnExitPort() {
        getGlobalPositionOnExitPort();
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
                if (globalBeginY < globalEndY ) {
                    pos.setLocation(globalEndX, globalEndY + minGapForPedestrian);
                }else {
                    pos.setLocation(globalEndX, globalEndY - minGapForPedestrian);
                }
            }

        } // Port is along x axis
        else {
            if ( (globalBeginX < globalEndX && currentGlobalPosition.getX() >= (globalBeginX + minGapForPedestrian) &&
                    currentGlobalPosition.getX() <= (globalEndX - getMinGapForPedestrian()) ) ||
                    (globalBeginX > globalEndX && currentGlobalPosition.getX() >= (globalEndX + minGapForPedestrian) &&
                            currentGlobalPosition.getX() <= (globalBeginX - getMinGapForPedestrian()) )   ) {
                //pedestrian high is withing port?
                pos.setLocation(currentGlobalPosition.getX(), globalBeginY);
            } else
            if (Math.abs(currentGlobalPosition.getX() - globalBeginX) < Math.abs(currentGlobalPosition.getX() - globalEndX)) { // check if it is closer to end or begin
                //closer to begin exit port
                //now moving intersection into the port about the min Gap
                if (globalBeginX < globalEndX ) {
                    pos.setLocation(globalBeginX, globalBeginY + minGapForPedestrian);
                }else {
                    pos.setLocation(globalBeginX, globalBeginY - minGapForPedestrian);
                }
            } else {
                //closer to end of exit Port
                //now moving intersection into the port about the min Gap
                if (globalBeginX < globalEndX ) {
                    pos.setLocation(globalEndX, globalEndY + minGapForPedestrian);
                }else {
                    pos.setLocation(globalEndX, globalEndY - minGapForPedestrian);
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

        PedestrianPoint globalCoordinateOfSectionOrigin = getGlobalCoordinatesOfCurrentSection();
        setCurrentGlobalPosition(new PedestrianPoint(
                newLocalPos.getX() + globalCoordinateOfSectionOrigin.getX(),
                newLocalPos.getY() + globalCoordinateOfSectionOrigin.getY()));
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
        return previousSFMVector;
    }

    public void setPreviousSFMVector(Vector2d previousSFMVector) {
        this.previousSFMVector = previousSFMVector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2d getSocialForceVector() {

        getRoundaboutModel().getSFM_DegreeOfAccuracy();
        Vector2d sumForce = new Vector2d();
        AccelerationForceToTarget accelerationForceToTarget = new AccelerationForceToTarget();
        RepulsiveForceAgainstVehicles repulsiveForceAgainstVehicles = new RepulsiveForceAgainstVehicles();
        RepulsiveForceAgainstObstacles repulsiveForceAgainstObstacles = new RepulsiveForceAgainstObstacles();
        RepulsiveForceAgainstOtherPedestrians repulsiveForceAgainstOtherPedestrians = new RepulsiveForceAgainstOtherPedestrians();

        Vector2d tmp = accelerationForceToTarget.getAccelerationForceToTarget(getRoundaboutModel(), this);
        sumForce.add(tmp);
        /*tmp = repulsiveForceAgainstOtherPedestrians.getRepulsiveForceAgainstAllOtherPedestrians(getRoundaboutModel(), this, getNextSubGoal());
        sumForce.add(tmp);
        tmp = repulsiveForceAgainstObstacles.getRepulsiveForceAgainstAllObstacles(getRoundaboutModel(), this);
        sumForce.add(tmp);
        //tmp = repulsiveForceAgainstVehicles.getRepulsiveForceAgainstVehicles(getRoundaboutModel(), this);
        //sumForce.add(tmp);*/
        this.setPreviousSFMVector(sumForce);
        return sumForce;
    }

    public double getMinGapForPedestrian() {
        return pedestrianBehaviour.calcGapForPedestrian();
    }

}
