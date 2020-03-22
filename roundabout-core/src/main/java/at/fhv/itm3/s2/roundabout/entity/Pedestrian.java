package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.statistics.StopWatch;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.*;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Count;
import desmoj.core.statistic.Tally;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Iterator;

public class Pedestrian extends Entity implements IPedestrian {

    private final Car car; //keep existing structure as dummy for some specific function considered simulation
    private final Double preferredSpeed;
    private final Double maxPreferredSpeed;
    private final IPedestrianRoute route;
    private final IPedestrianBehaviour pedestrianBehaviour;
    private final Iterator<PedestrianStreetSectionAndPortPair> routeIterator;
    private final StopWatch pedestrianStopWatch;
    private final Count pedestrianCounter;
    private final Tally pedestrianAreaTime;
    private final StopWatch pedestrianCrossingStopWatch;
    private final Count pedestrianCrossingCounter;
    private final Tally pedestrianCrossingTime;
    private final int minGapForPedestrian; // TODO
    private double currentTimeSpendInSystem;

    private double lastUpdateTime;
    private Point currentGlobalPosition;

    private PedestrianStreetSection lastSection;
    private PedestrianStreetSectionAndPortPair currentSection;
    private PedestrianStreetSectionAndPortPair nextSection;
    private PedestrianStreetSectionAndPortPair sectionAfterNextSection;
    private Double currentSpeed;
    private Double walkedDistance;
    private double timeRelatedParameterValueNForSpeedCalculation;
    private Point currentNextGlobalAim;
    private Point exitPointOnPort;
    SupportiveCalculations calc = new SupportiveCalculations();

    public Pedestrian(Model model, String name, boolean showInTrace, Point currentGlobalPosition, IPedestrianBehaviour pedestrianBehaviour, IPedestrianRoute  route){
        this(model, name, showInTrace, currentGlobalPosition, pedestrianBehaviour, route, 1.0);
    }

    public Pedestrian(Model model, String name, boolean showInTrace, Point currentGlobalPosition, IPedestrianBehaviour pedestrianBehaviour,
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
            this.exitPointOnPort = null;
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
        this.pedestrianCounter.reset();
        this.pedestrianCrossingTime = new Tally(model, "Roundabout time", false, false);
        this.pedestrianCrossingTime.reset();

        this.currentSpeed = this.getPreferredSpeed();
        this.minGapForPedestrian = 5; //cm
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
    public Point getNextSubGoal() {
        return getPositionOnExitPort();
    }

    public Point getGlobalNextSubGoal() {
        if (! (getCurrentSection().getStreetSection() instanceof PedestrianStreetSection )) {
            throw new IllegalArgumentException("Street section not instance of PedestrianStreetSection.");
        }

        Point exitPoint = getPositionOnExitPort();
        Point global = ((PedestrianStreetSection) getCurrentSection().getStreetSection()).getGlobalCoordinateOfSectionOrigin();
        Point globalGoal = new Point((int)(exitPoint.getX() + global.getX()),
                (int)(exitPoint.getY() + global.getY()));
        return globalGoal;
    }

    public void setGlobalNextSubGoal ( ){
        this.getGlobalNextSubGoal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTimeToNextSubGoal() {
        Point goal = getNextSubGoal();
        if (goal == null) return 0;
        Point origin = ((PedestrianStreetSection)getCurrentSection().getStreetSection()).getGlobalCoordinateOfSectionOrigin();

        return calc.getDistanceByCoordinates(goal.getX() + origin.getX(),
                goal.getY() + origin.getY(),
                currentGlobalPosition.getX(),
                currentGlobalPosition.getY()) * getCurrentSpeed();
    }

    public double getTimeToNextGlobalSubGoalByCoordinates(Point goal) {

        if ( goal == null) {
            throw new IllegalArgumentException("No gaol defined.");
        }

        Point origin = ((PedestrianStreetSection)getCurrentSection().getStreetSection()).getGlobalCoordinateOfSectionOrigin();

        return calc.getDistanceByCoordinates(goal.getX() + origin.getX(),
                goal.getY() + origin.getY(),
                currentGlobalPosition.getX(),
                currentGlobalPosition.getY()) * calculatePreferredSpeed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point getCurrentGlobalPosition() {
        return currentGlobalPosition;
    }

    public Point getCurrentLocalPosition() {
        IConsumer section = getCurrentSection().getStreetSection();
        if ( !(section instanceof PedestrianStreet)) {
            throw new IllegalArgumentException("Section not instance of PedestrianStreet.");
        }

        Point globalOffset = ((PedestrianStreetSection)(getCurrentSection().getStreetSection())).getGlobalCoordinateOfSectionOrigin();
        Point localPos = new Point( (int) (currentGlobalPosition.getX()- globalOffset.getX()),
                                    (int)(currentGlobalPosition.getY() - globalOffset.getY()));
        return localPos;
    }

    public void setCurrentGlobalPosition(Point currentGlobalPosition) {
        if(currentGlobalPosition == null) {
            throw new IllegalArgumentException("there is no global position set");
        }
        this.currentGlobalPosition = currentGlobalPosition;
    }

    public void setExitPointOnPort (Point exitPointOnPort) {
        this.exitPointOnPort = exitPointOnPort;
    }

    public Point getExitPointOnPort () {
        return this.exitPointOnPort;
    }

    public Point getClosestExitPointOfCurrentSection(){
        PedestrianStreetSection currentSection = (PedestrianStreetSection)this.getCurrentSection().getStreetSection();
        for ( PedestrianConnectedStreetSections connectedStreetSections : currentSection.getNextStreetConnector()){
            if ( connectedStreetSections.getToStreetSection().equals(nextSection.getStreetSection()) ) {
                PedestrianStreetSectionPort port = connectedStreetSections.getPortOfToStreetSection();
                double onBorderX, onBorderY;
                Point localPos = getCurrentLocalPosition();
                if( calc.almostEqual(port.getBeginOfStreetPort().getX(), port.getEndOfStreetPort().getX()) ) { // port along y side
                    onBorderX = 0; // Min of border
                    onBorderY = localPos.getY();
                } else { // port along x side
                    onBorderX = localPos.getX();
                    onBorderY = 0; // Min of border
                }

                Point intersection = calc.getLinesIntersectionByCoordinates( port, localPos.getX(), localPos.getY(), onBorderX, onBorderY);

                if ( !calc.checkWallIntersectionWithinPort( port, intersection ) )
                    calc.shiftIntersection( port, intersection, getMinGapForPedestrian());


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
    public void enterSystem() { car.enterSystem(); }

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
        double res = this.pedestrianCrossingStopWatch.stop();
        this.pedestrianCrossingTime.update(new TimeSpan(res));
    }

    public boolean isPedestrianCrossingStopWatchActive () {
        return this.pedestrianCrossingStopWatch.isRunning();
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
        this.pedestrianStopWatch.start();
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

    public long getStopCount() {
        return car.getStopCount();
    }

    public double getCoveredDistanceInTime(double time) {
        return time * pedestrianBehaviour.getSpeed();
    }

    public double getCurrentSpeed() {return currentSpeed;}

    public void setCurrentSpeed( double currentSpeed) { this.currentSpeed = currentSpeed;}

    public double getPreferredSpeed() {return this.preferredSpeed;}

    public double calculatePreferredSpeed(){
        double averageSpeed = getTimeSpentInSystem() == 0 ? 0 : walkedDistance/ getTimeSpentInSystem();
        double timeRelatedParameter = averageSpeed/preferredSpeed;
        timeRelatedParameter = timeRelatedParameterValueNForSpeedCalculation - timeRelatedParameter;

        double part1 = (1-timeRelatedParameter) * preferredSpeed;
        double part2 = timeRelatedParameter * maxPreferredSpeed;

        return part1 + part2;
    }

    public boolean checkExitPortIsReached(double x, double y){
        Point pos = new Point();
        pos.setLocation(x, y);
        return checkExitPortIsReached(pos);
    }

    public Point transferToNextPortPos () {
        PedestrianStreetSectionPort exitPort = currentSection.getExitPort();
        PedestrianStreetSectionPort enterPort = nextSection.getEnterPort();
        double high = 0;
        Point pos = null;

        // exit Port along x axis
        if ( calc.almostEqual(exitPort.getBeginOfStreetPort().getX(), exitPort.getEndOfStreetPort().getX())) {
            //start lower value than end
            if( calc.val1LowerOrAlmostEqual(enterPort.getBeginOfStreetPort().getY(), enterPort.getEndOfStreetPort().getY())) {
                high = exitPointOnPort.getY() - enterPort.getBeginOfStreetPort().getY();
                pos = new Point( (int) enterPort.getBeginOfStreetPort().getX() , (int) (enterPort.getBeginOfStreetPort().getY() + high) );
            } else {// end lower value than start
                high = exitPointOnPort.getY() - enterPort.getEndOfStreetPort().getY();
                pos = new Point( (int) enterPort.getEndOfStreetPort().getX() , (int) (enterPort.getEndOfStreetPort().getY() + high) );
            }
        } else { // exit Port along y axis
            if( calc.val1LowerOrAlmostEqual(enterPort.getBeginOfStreetPort().getX(), enterPort.getEndOfStreetPort().getX())) {
                high = exitPointOnPort.getX() - enterPort.getBeginOfStreetPort().getX();
                pos = new Point( (int) (enterPort.getBeginOfStreetPort().getX() + high) , (int) enterPort.getBeginOfStreetPort().getY() );
            } else {// end lower value than start
                high = exitPointOnPort.getX() - enterPort.getEndOfStreetPort().getX();
                pos = new Point( (int) (enterPort.getEndOfStreetPort().getX() + high) , (int) enterPort.getEndOfStreetPort().getY() );
            }
        }
        return pos;
    }

    public boolean checkGlobalGoalIsReached(){
        if ( currentGlobalPosition == null ) {
            throw new IllegalArgumentException(" no pedestrianPosition passed.");
        }

        if (!( currentSection.getStreetSection() instanceof PedestrianStreetSection) ) {
            throw new IllegalArgumentException(" Section not instance of PedestrianStreetSection");
        }

        Point globalCoord = ((PedestrianStreetSection) currentSection.getStreetSection()).getGlobalCoordinateOfSectionOrigin();

        Point localPos = new Point( (int) (currentGlobalPosition.getX() - globalCoord.getX()),
                                    (int) (currentGlobalPosition.getY() - globalCoord.getY()));
        return checkExitPortIsReached(localPos);
    }

    public boolean checkExitPortIsReached(){
        if ( exitPointOnPort == null ) {
            throw new IllegalArgumentException(" no pedestrianPosition passed.");
        }
        return checkExitPortIsReached(exitPointOnPort);
    }

    public boolean checkExitPortIsReached(Point pedestrianPosition){
        // Port is along y axis
        if(currentSection.getExitPort().getBeginOfStreetPort().getX() == currentSection.getExitPort().getEndOfStreetPort().getX()){
            if ( pedestrianPosition == null ) {
                throw new IllegalArgumentException(" no pedestrianPosition passed.");
            }

            if(    ((calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getY() + minGapForPedestrian),
                            pedestrianPosition.getY(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getY() - minGapForPedestrian),
                            pedestrianPosition.getY(), 10e-1))
                    ||
                    calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getY() + minGapForPedestrian), pedestrianPosition.getY(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getY() - minGapForPedestrian), pedestrianPosition.getY(), 10e-1))

                    &&
                    calc.almostEqual(currentSection.getExitPort().getBeginOfStreetPort().getX(), pedestrianPosition.getX(), 1.0)
                    ){
                return true;
            }
        } // Port is along x axis
        else {
            if(    ((calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getX() + minGapForPedestrian), pedestrianPosition.getX(), 10e-1) &&
                    calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getX() - minGapForPedestrian), pedestrianPosition.getX(), 10e-1))
                    ||
                    calc.val1LowerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getX() + minGapForPedestrian), pedestrianPosition.getX(), 10e-1) &&
                            calc.val1BiggerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getX() - minGapForPedestrian), pedestrianPosition.getX(), 10e-1))

                    &&
                    calc.almostEqual(currentSection.getExitPort().getBeginOfStreetPort().getY(), pedestrianPosition.getY(), 1.0)
                    ){
                return true;
            }
        }
        return false;
    }

    public Point getPositionOnExitPort(){
        // if an orthogonal form Sink Port to Point match the intersection of the orthogonal is the enter point.
        // else check if it is above or beneath  - subtract pedestrian size
        Point pos = new Point();

        double beginX = currentSection.getExitPort().getBeginOfStreetPort().getX();
        double beginY = currentSection.getExitPort().getBeginOfStreetPort().getY();
        double endX = currentSection.getExitPort().getEndOfStreetPort().getX();
        double endY = currentSection.getExitPort().getEndOfStreetPort().getY();


        // Port is along y axis
        if( beginX == endX){
            if( checkExitPortIsReached(beginX, currentGlobalPosition.getY())){
                pos.setLocation(beginX, currentGlobalPosition.getY());
            }
            else if (Math.abs(currentGlobalPosition.getY()-beginY) < Math.abs(currentGlobalPosition.getY()-endY)){ // check if it is closer to end or begin
                //closer to begin exit port
                if(Math.abs((beginY - minGapForPedestrian)- endY) < Math.abs((beginY + minGapForPedestrian)- endY))
                    pos.setLocation(beginX, beginY - minGapForPedestrian);
                else
                    pos.setLocation(beginX, beginY + minGapForPedestrian);
            } else {
                //closer to end of exit Port
                if(Math.abs((endY - minGapForPedestrian)- beginY) < Math.abs((endY + minGapForPedestrian)- beginY))
                    pos.setLocation(endX, endY - minGapForPedestrian);
                else
                    pos.setLocation(endX, endY + minGapForPedestrian);
            }

        } // Port is along x axis
        else {
            if( checkExitPortIsReached(currentGlobalPosition.getX(), beginY)){
                pos.setLocation(currentGlobalPosition.getX(), beginY);
            }
            else if (Math.abs(currentGlobalPosition.getX()-beginX) < Math.abs(currentGlobalPosition.getX()-endX)){ // check if it is closer to end or begin
                //closer to begin exit port
                if(Math.abs((beginX - minGapForPedestrian)- endX) < Math.abs((beginX + minGapForPedestrian)- endX))
                    pos.setLocation(beginX - minGapForPedestrian, beginY);
                else
                    pos.setLocation(beginX + minGapForPedestrian, beginY);
            } else {
                //closer to end of exit Port
                if(Math.abs((endX - minGapForPedestrian)- beginX) < Math.abs((endX + minGapForPedestrian)- beginX))
                    pos.setLocation(endX - minGapForPedestrian, endY);
                else
                    pos.setLocation(endX + minGapForPedestrian, endY);
            }
        }

        return pos;
    }

    public void updateWalkedDistance( ){
        this.walkedDistance += calc.getDistanceByCoordinates(currentGlobalPosition.getX(), currentGlobalPosition.getY(),
                getGlobalNextSubGoal().getX(), getGlobalNextSubGoal().getY());
    }

    public Double getWalkedDistance() {
        return walkedDistance;
    }


    public Point getGlobalCoordinatesOfCurrentSection(){
        return ((PedestrianStreetSection)(((PedestrianStreetSectionAndPortPair)(currentSection.getStreetSection())).getStreetSection())).getGlobalCoordinateOfSectionOrigin();
    }

    public void moveOneSectionForward(Point newLocalPos) {
        ((PedestrianStreet) currentSection.getStreetSection()).removePedestrian(this);
        ((PedestrianStreet) nextSection.getStreetSection()).addPedestrian(this, newLocalPos);

        currentSection = nextSection;
        nextSection = sectionAfterNextSection;
        sectionAfterNextSection = retrieveNextRouteSection();

        Point globalCoordinateOfSectionOrigin = getGlobalCoordinatesOfCurrentSection();
        setCurrentGlobalPosition(new Point (
                (int) (newLocalPos.getX() + globalCoordinateOfSectionOrigin.getX()),
                (int) (newLocalPos.getY() + globalCoordinateOfSectionOrigin.getY())));
    }

    public void setCurrentNextGlobalAim(Point currentNextGlobalAim) {
        this.currentNextGlobalAim = currentNextGlobalAim;
    }

    public Point getCurrentNextGlobalAim() {
        return currentNextGlobalAim;
    }

    public Car getCarDummy() {
        return this.car;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2d getSocialForceVector(){

        getRoundaboutModel().getSFM_DegreeOfAccuracy();
        Vector2d sumForce = new Vector2d();
        AccelerationForceToTarget accelerationForceToTarget = new AccelerationForceToTarget();
        RepulsiveForceAgainstVehicles repulsiveForceAgainstVehicles = new RepulsiveForceAgainstVehicles();
        RepulsiveForceAgainstObstacles repulsiveForceAgainstObstacles = new RepulsiveForceAgainstObstacles();
        RepulsiveForceAgainstOtherPedestrians repulsiveForceAgainstOtherPedestrians = new RepulsiveForceAgainstOtherPedestrians();

        Vector2d tmp = accelerationForceToTarget.getAccelerationForceToTarget(getRoundaboutModel(), this);
        sumForce.add(tmp);
        tmp = repulsiveForceAgainstOtherPedestrians.getRepulsiveForceAgainstAllOtherPedestrians(getRoundaboutModel(), this, getNextSubGoal());
        sumForce.add(tmp);
        //tmp = repulsiveForceAgainstVehicles.getRepulsiveForceAgainstVehicles(getRoundaboutModel(), this);
        sumForce.add(tmp); //todo
        tmp = repulsiveForceAgainstObstacles.getRepulsiveForceAgainstAllObstacles(getRoundaboutModel(), this);
        sumForce.add(tmp);

        return sumForce;
    }

    public int getMinGapForPedestrian() {
        return minGapForPedestrian;
    }
}
