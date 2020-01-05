package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.statistics.StopWatch;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Count;
import desmoj.core.statistic.Tally;

import java.awt.*;
import java.util.Iterator;

public class Pedestrian implements IPedestrian {

    private final Car car; //keep existing structure as dummy for some specific function considered simulation
    private final Point currentPosition;
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

    private double lastUpdateTime;

    private PedestrianStreetSection lastSection;
    private PedestrianStreetSectionAndPortPair currentSection;
    private PedestrianStreetSectionAndPortPair nextSection;
    private PedestrianStreetSectionAndPortPair sectionAfterNextSection;
    private Double currentSpeed;
    private Double walkedDistance;
    private double timeRelatedParameterFactorForSpeedCalculation;
    private SupportiveCalculations calc;

    public Pedestrian(Model model, Point currentPosition, IPedestrianBehaviour pedestrianBehaviour, IPedestrianRoute  route){
        this(model, currentPosition, pedestrianBehaviour, route, 1.0);
    }


    public Pedestrian(Model model, Point currentPosition, IPedestrianBehaviour pedestrianBehaviour, IPedestrianRoute route,
                      double timeRelatedParameterFactorForSpeedCalculation)
            throws IllegalArgumentException {

        this.currentPosition = currentPosition;

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
        } else {
            throw new IllegalArgumentException("Route should not be null.");
        }

        this.setLastUpdateTime(getRoundaboutModel().getCurrentTime());

        // Extended of Pedestrian speed -> also include stress factor.
        double val1 = getRoundaboutModel().getRandomPreferredSpeed();
        double val2 = getRoundaboutModel().getRandomPreferredSpeed();
        this.preferredSpeed = Math.min(val1, val2);
        this.maxPreferredSpeed = Math.max(val1, val2);
        this.walkedDistance = 0.0;

        this.timeRelatedParameterFactorForSpeedCalculation = timeRelatedParameterFactorForSpeedCalculation;

        this.car = new Car(model, "dummy", false);

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

        this.currentSpeed = 0.0;
        this.minGapForPedestrian = 40;
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


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean nextSubGoalIsReached() {
        return checkExitPortIsReached();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTimeToNextSubGoal(Point currentPosition) {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Point getCurrentPosition() {
        return currentPosition;
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
        return car.getTimeSpentInSystem();
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
        double averageSpeed = walkedDistance/ getTimeSpentInSystem();

        double timeRelatedParameter = averageSpeed/preferredSpeed;
        timeRelatedParameter = timeRelatedParameterFactorForSpeedCalculation - timeRelatedParameter;

        double part1 = (1-timeRelatedParameter);
        part1 *= preferredSpeed;

        double part2 = timeRelatedParameter * maxPreferredSpeed;

        return part1 + part2;
    }

    public boolean checkExitPortIsReached(double x, double y){
        Point pos = new Point();
        pos.setLocation(x, y);
        return checkExitPortIsReached(pos);
    }

    public boolean checkExitPortIsReached(){ return checkExitPortIsReached(currentPosition);}

    public boolean checkExitPortIsReached(Point pedestrianPosition){
        // Port is along y axis
        if(currentSection.getExitPort().getBeginOfStreetPort().getX() == currentSection.getExitPort().getEndOfStreetPort().getX()){
            if(    ((calc.LowerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getY() + minGapForPedestrian), pedestrianPosition.getY(), 10e-1) &&
                    calc.BiggerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getY() - minGapForPedestrian), pedestrianPosition.getY(), 10e-1))
                    ||
                    calc.LowerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getY() + minGapForPedestrian), pedestrianPosition.getY(), 10e-1) &&
                    calc.BiggerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getY() - minGapForPedestrian), pedestrianPosition.getY(), 10e-1))

                    &&
                    calc.AlmostEqual(currentSection.getExitPort().getBeginOfStreetPort().getX(), pedestrianPosition.getX(), 1.0)
                    ){
                return true;
            }
        } // Port is along x axis
        else {
            if(    ((calc.LowerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getX() + minGapForPedestrian), pedestrianPosition.getX(), 10e-1) &&
                    calc.BiggerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getX() - minGapForPedestrian), pedestrianPosition.getX(), 10e-1))
                    ||
                    calc.LowerOrAlmostEqual((currentSection.getExitPort().getEndOfStreetPort().getX() + minGapForPedestrian), pedestrianPosition.getX(), 10e-1) &&
                            calc.BiggerOrAlmostEqual((currentSection.getExitPort().getBeginOfStreetPort().getX() - minGapForPedestrian), pedestrianPosition.getX(), 10e-1))

                    &&
                    calc.AlmostEqual(currentSection.getExitPort().getBeginOfStreetPort().getY(), pedestrianPosition.getY(), 1.0)
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
            if( checkExitPortIsReached(beginX, currentPosition.getY())){
                pos.setLocation(beginX, currentPosition.getY());
            }
            else if (Math.abs(currentPosition.getY()-beginY) < Math.abs(currentPosition.getY()-endY)){ // check if it is closer to end or begin
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
            if( checkExitPortIsReached(currentPosition.getX(), beginY)){
                pos.setLocation(currentPosition.getX(), beginY);
            }
            else if (Math.abs(currentPosition.getX()-beginX) < Math.abs(currentPosition.getX()-endX)){ // check if it is closer to end or begin
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


    public void updateWalkedDistance( double distance){ this.walkedDistance += distance; }

    public Double getWalkedDistance() {
        return walkedDistance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double GetSocialForceVector(){

        getRoundaboutModel().getSFM_DegreeOfAccuracy();




        //TODO summiere paramerter.


        return 0;
    }



}
