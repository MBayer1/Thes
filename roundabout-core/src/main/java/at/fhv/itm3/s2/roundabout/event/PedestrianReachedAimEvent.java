package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrian;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreet;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianSink;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import javax.vecmath.Vector2d;
import java.awt.*;


public class PedestrianReachedAimEvent extends Event<Pedestrian> {
    private SupportiveCalculations calc = new SupportiveCalculations();

    /**
     * A reference to the {@link RoundaboutSimulationModel} the {@link PedestrianReachedAimEvent} is part of.
     */
    private RoundaboutSimulationModel roundaboutSimulationModel;

    /**
     * Instance of {@link PedestrianEventFactory} for creating new events.
     * (protected because of testing)
     */
    protected PedestrianEventFactory pedestrianEventFactory;

    /**
     * Constructs a new {@link PedestrianReachedAimEvent}.
     *
     * @param model the model this event belongs to.
     * @param name this event's name.
     * @param showInTrace flag to indicate if this event shall produce output for the trace.
     */
    public PedestrianReachedAimEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);

        pedestrianEventFactory = PedestrianEventFactory.getInstance();

        if (model instanceof RoundaboutSimulationModel) {
            roundaboutSimulationModel = (RoundaboutSimulationModel) model;
        } else {
            throw new IllegalArgumentException("No suitable model given over.");
        }
    }

    /**
     * The event routine describes the moving of a car from one section to the next section.
     *
     * If the given section has a car at its exit point, it is checked if this car could leave this //TODO
     * section and enter the next one. If that is true, a new {@link PedestrianReachedAimEvent} for the next
     * section (the section the car enters) is scheduled after the time the car needs to traverse that
     * section. After that the car is moved from this section to the next one.
     * If the current section (the section in which the car was before moving) is not empty, a new
     * CarCouldLeaveSectionEvent is scheduled for the current section after the transition time of the
     * first car (only if this time is passed it makes sense to update all car positions on this time and
     * check if there is another car to leave the section).
     * Moreover for all previous sections of the current section a new {@link PedestrianReachedAimEvent} is immediately
     * scheduled to check if the previous sections have a car which could enter the current section.
     *
     * @param pedestrian the pedestrian that will move over a street (@link PedestrianStreetSection).
     * @throws SuspendExecution Marker exception for Quasar (inherited).
     */
    @Override
    public void eventRoutine(Pedestrian pedestrian) throws SuspendExecution {
        IConsumer currentSection = pedestrian.getCurrentSection().getStreetSection();

        if ( ! (pedestrian instanceof  Pedestrian) ) {
            throw new IllegalArgumentException( "Pedestrian not instance of Pedestrian." );
        }

        Vector2d forces = pedestrian.getSocialForceVector(); //set time when next update.
        double timeToDestination = 0.0;

        // since all sup-aims are within one street section it is enough to check all other pedestrian on the same section
        if (! (pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreet )) {
            throw new IllegalArgumentException( "Street not instance of PedestrianStreet." );
        }

        if (calc.almostEqual(pedestrian.getCurrentSpeed(), 0)) {
            pedestrian.setCurrentSpeed(pedestrian.getPreferredSpeed()); // reset
        }

        if( pedestrian.getCurrentNextGlobalAim() == null) {
            // consider intersection to other pedestrian etc.
            // -> not the clear goal on exit point like it is considered in force toward aim
            // pedestrian newly arrived at current section
            setNewGoal(pedestrian, forces);
            timeToDestination = pedestrian.getTimeToNextGlobalSubGoal();
        }

        boolean movedToNextSection = false;

        if ( pedestrian.checkExitPortIsReached() ) { // check if section will be changed
            // set to next section
            if ( !(currentSection instanceof PedestrianStreet) ){
                throw new IllegalArgumentException( "Street not instance of PedestrianStreet.");
            }

            PedestrianStreet nextStreetSection = ((PedestrianStreet)(pedestrian.getNextSection().getStreetSection()));
            if(nextStreetSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_SINK)) {
                if ( !(nextStreetSection instanceof PedestrianSink) ){
                    throw new IllegalArgumentException( "Street not instance of PedestrianSink.");
                }
            } else if ( !(nextStreetSection instanceof PedestrianStreet) ){
                throw new IllegalArgumentException( "Street not instance of PedestrianStreet.");
            }

            boolean freeToGo = true;
            // when the next section is a pedestrian crossing and does have a crossing light the light stage has to be checked
            if( nextStreetSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)){
                // solely on a crossing can be a traffic light. -> check in Parser
                if( nextStreetSection.isTrafficLightActive() ){
                    if( !nextStreetSection.isTrafficLightFreeToGo()) {
                        pedestrian.setCurrentSpeed(0.0);
                        freeToGo = false;
                        timeToDestination = ((PedestrianStreet) currentSection).getRedPhaseDurationOfTrafficLight();
                        nextStreetSection.handleJamTrafficLight();
                    } else {
                        timeToDestination = pedestrian.getTimeToNextGlobalSubGoal();
                    }

                }
            }

            if (freeToGo) {
                // destination of the current street section is reached move to next section
                Point transferPos = pedestrian.transferToNextPortPos();
                pedestrian.moveOneSectionForward(transferPos);
                timeToDestination = 0; // define proper next aim next loop
                movedToNextSection = true;
            }
        }

        if(timeToDestination == 0 && !movedToNextSection && pedestrian.getCurrentSpeed()!= 0) {
            //danger of endlessly looping catch
            throw new IllegalStateException("pedestrian is theoretically moving, but already reached destination.");
        }

        if(pedestrian.getCurrentNextGlobalAim() != null ) {
            // pedestrian did not move to next section yet
            pedestrian.updateWalkedDistance(); // adding distance before it is walked at it will reach its destination.
            pedestrian.setLastUpdateTime(roundaboutSimulationModel.getCurrentTime());
            pedestrian.setCurrentGlobalPosition(pedestrian.getCurrentNextGlobalAim());
            pedestrian.setCurrentNextGlobalAim(null); // redefine next goal in next round
        }

        pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                pedestrian, new TimeSpan(timeToDestination, roundaboutSimulationModel.getModelTimeUnit()));
    }

    void setNewGoal( Pedestrian pedestrian, Vector2d forces ){
        pedestrian.setCurrentNextGlobalAim();// set as first aim the exit point of the street section
        PedestrianStreet section = ((PedestrianStreet)pedestrian.getCurrentSection().getStreetSection());
        Point curGlobPos = pedestrian.getCurrentGlobalPosition();
        Point globalGoal = checkAndSetAimWithinSection(forces, section, pedestrian);
        double minGab = pedestrian.getMinGapForPedestrian();

        for (IPedestrian otherPedestrian : section.getPedestrianQueue()){ // check for intersections with other pedestrians
            if(otherPedestrian.equals(pedestrian)) continue;
            if (! (otherPedestrian instanceof Pedestrian )) {
                throw new IllegalArgumentException( "Other pedestrian not instance of Pedestrian." );
            }

            // if an other pedestrian does already have a defined "current global aim" and
            // does have an intersection with the current aim of the current pedestrian
            // the "current global aim" of the current pedestrian is cut down to it.
            // Otherwise the end of the section is the aim.
            // Afterwards the time until this destination is reached is calculated.
            if( ((Pedestrian)otherPedestrian).getCurrentNextGlobalAim() != null) {
                if(calc.checkLinesIntersectionByCoordinates_WithinSegment(globalGoal,
                        pedestrian.getCurrentGlobalPosition(),
                        pedestrian.getCurrentGlobalPosition().getX() + forces.getX(), pedestrian.getCurrentGlobalPosition().getY() + forces.getY(),
                        otherPedestrian.getCurrentGlobalPosition(),
                        ((Pedestrian) otherPedestrian).getCurrentNextGlobalAim())) {
                    // there is a crossing
                    if(!checkTimeOfIntersectionMatches(pedestrian, globalGoal, (Pedestrian)otherPedestrian)) continue;

                    // crossing - the size of the current pedestrian = new destination aim
                    if (calc.getDistanceByCoordinates(globalGoal.getX() - minGab, globalGoal.getY() - minGab, curGlobPos.getX(), curGlobPos.getY() )
                            < calc.getDistanceByCoordinates(globalGoal.getX() + minGab, globalGoal.getY() + minGab, curGlobPos.getX(), curGlobPos.getY() )) {
                        globalGoal.setLocation(globalGoal.getX() - minGab, globalGoal.getY() - minGab);
                    } else {
                        globalGoal.setLocation(globalGoal.getX() + minGab, globalGoal.getY() + minGab);
                    }
                    pedestrian.setCurrentNextGlobalAim(globalGoal);
                }
            }

        }
    }

    Boolean checkTimeOfIntersectionMatches(Pedestrian pedestrian, Point intersection, Pedestrian otherPedestrian){
        // get from last update time to time to intersection and
        // compare simulated time of both pedestrians when reaching intersection coordinates.

        double pedestrianReachedIntersection = pedestrian.getLastUpdateTime() +
                (calc.getDistanceByCoordinates(pedestrian.getCurrentGlobalPosition(), intersection)/pedestrian.getCurrentSpeed());
        double otherPedestrianReachedIntersection = otherPedestrian.getLastUpdateTime() +
                (calc.getDistanceByCoordinates(otherPedestrian.getCurrentGlobalPosition(), intersection)/otherPedestrian.getCurrentSpeed());

        // convert distance min gab in to time min time gab
        double tolerance = Math.max(otherPedestrian.getMinGapForPedestrian()/otherPedestrian.getCurrentSpeed(),
                                    pedestrian.getMinGapForPedestrian()/pedestrian.getCurrentSpeed());

        if ( calc.almostEqual(pedestrianReachedIntersection, otherPedestrianReachedIntersection, tolerance)) return true;
        return false;
    }

    Point checkAndSetAimWithinSection(Vector2d forces, PedestrianStreet section, Pedestrian pedestrian) {
        // this is needed to ensure every aim is within a street Section
        // move not further as the distance to direct aim
        double distance = calc.getDistanceByCoordinates(pedestrian.getCurrentGlobalPosition(), pedestrian.getCurrentNextGlobalAim());
        Vector2d uniVecForces = calc.getUnitVector(forces);
        uniVecForces.scale(distance);

        // change aim with consideration of social force since this is the path pedestrian is walking
        Point globalGoal = new Point(
                (int)(pedestrian.getCurrentNextGlobalAim().getX() + uniVecForces.getX()),
                (int)(pedestrian.getCurrentNextGlobalAim().getY() + uniVecForces.getY()));

        if (! (section instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException ("Type mismatch.");
        }

        Point cornerLeftBottomGlobal = ((PedestrianStreetSection) section).getGlobalCoordinateOfSectionOrigin();
        Point cornerLeftUpGlobal = new Point( (int)cornerLeftBottomGlobal.getX(), (int) (cornerLeftBottomGlobal.getY() +  section.getLengthY()));
        Point cornerRightBottomGlobal = new Point( (int) (cornerLeftBottomGlobal.getX() +  section.getLengthX()), (int) cornerLeftBottomGlobal.getY());
        Point cornerRightUpGlobal = new Point( (int) (cornerLeftBottomGlobal.getX() +  section.getLengthX()), (int) (cornerLeftBottomGlobal.getY() +  section.getLengthY()));
        int borderLineNr = 0;

        boolean withinSection = true;
        Point intersection = new Point(0,0);
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersection,
                pedestrian.getCurrentGlobalPosition(),
                globalGoal,
                cornerLeftBottomGlobal, cornerLeftUpGlobal
                )){
            withinSection = false;
            borderLineNr = 1;
        } else
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersection,
                pedestrian.getCurrentGlobalPosition(),
                globalGoal,
                cornerLeftBottomGlobal, cornerRightBottomGlobal
        )){
            withinSection = false;
            borderLineNr = 2;
        } else
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersection,
                pedestrian.getCurrentGlobalPosition(),
                globalGoal,
                cornerLeftUpGlobal, cornerRightUpGlobal
        )){
            withinSection = false;
            borderLineNr = 3;
        } else
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersection,
                pedestrian.getCurrentGlobalPosition(),
                globalGoal,
                cornerRightBottomGlobal, cornerRightUpGlobal
        )){
            withinSection = false;
            borderLineNr = 4;
        }


        pedestrian.setCurrentNextGlobalAim(intersection);
        if ( withinSection ) return globalGoal;

        if( section.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)) {
            boolean crossingAllowed = false;
            // in this case it might be possible to cross over border
            if(((PedestrianStreetSection) section).getFlexiBorderAlongX() && borderLineNr == 2 && borderLineNr == 3){
                crossingAllowed = true;
            } else if (borderLineNr == 1 && borderLineNr == 4){
                crossingAllowed = true;
            }

            if(crossingAllowed) return globalGoal;
        }

        // overwrite wall intersection as new aim -> not crossing wall
        double minGab = pedestrian.getMinGapForPedestrian();
        if (calc.getDistanceByCoordinates(intersection.getX() - minGab, intersection.getX() - minGab, globalGoal.getX(), globalGoal.getY() )
                < calc.getDistanceByCoordinates(intersection.getX() + minGab, intersection.getX() + minGab, globalGoal.getX(), globalGoal.getY() )) {
            return new Point((int) (intersection.getX() - minGab), (int)(intersection.getY() - minGab));
        } else {
            return new Point((int) (intersection.getX() + minGab), (int) (intersection.getY() + minGab ));
        }
    }
}