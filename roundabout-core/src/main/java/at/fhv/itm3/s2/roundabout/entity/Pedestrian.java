package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.AbstractProSumer;
import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.statistics.StopWatch;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrian;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrianBehaviour;
import at.fhv.itm3.s2.roundabout.api.entity.IRoute;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreet;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import at.fhv.itm3.s2.roundabout.util.dto.PedestrianConnector;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Count;
import desmoj.core.statistic.Tally;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Iterator;

public class Pedestrian implements IPedestrian {

    private final Car car; //keep existing structure as dummy for some specific function considered simulation
    private final Point currentPosition;
    private final Double preferredSpeed;
    private final Double maxPreferredSpeed;
    private final IRoute route;
    private final IPedestrianBehaviour pedestrianBehaviour;
    private final Iterator<IConsumer> routeIterator;
    private final StopWatch pedestrianStopWatch;
    private final Count pedestrianCounter;
    private final Tally pedestrianAreaTime;
    private final StopWatch pedestrianCrossingStopWatch;
    private final Count pedestrianCrossingCounter;
    private final Tally pedestrianCrossingTime;

    private double lastUpdateTime;

    private IConsumer lastSection;
    private IConsumer currentSection;
    private IConsumer nextSection;
    private IConsumer sectionAfterNextSection;
    private Double currentSpeed;
    private Double walkedDistance;
    private double timeRelatedParameterFactorForSpeedCalculation;

    public Pedestrian(Model model, Point currentPosition, IPedestrianBehaviour pedestrianBehaviour, IRoute route){
        this(model, currentPosition, pedestrianBehaviour, route, 1.0);
    }


    public Pedestrian(Model model, Point currentPosition, IPedestrianBehaviour pedestrianBehaviour, IRoute route,
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
        return null; //TODO
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
    public IRoute getRoute() {
        return route;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IConsumer getCurrentSection() {
        return currentSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IConsumer getNextSection() {
        return nextSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IConsumer getSectionAfterNextSection() {
        return sectionAfterNextSection;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public IConsumer getDestination() {
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

    private IConsumer retrieveNextRouteSection() {
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

    public void updateWalkedDistance( double distance){ this.walkedDistance += distance; }

    public Vector2d getVectorToDestination(){


    }

    private Vector2d calculateDestinationVector(){
        if(!(route.getStartSection() instanceof PedestrianStreetSection)) throw new IllegalArgumentException("Type mismatch.");
        PedestrianStreetSection streetSection = (PedestrianStreetSection) route.getStartSection();

        Vector2d enterCoordinates = new Vector2d();
        Vector2d currentPositionVector = new Vector2d(getCurrentPosition().getX(), getCurrentPosition().getY());

        PedestrianConnector Connector =

        while(!(streetSection instanceof PedestrianSink)) {
            getRoundaboutModel().


        }

    }

    public void updateDestinationVector(){

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
