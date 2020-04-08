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

        if( pedestrian.getCurrentNextGlobalAim() == null) {
            setNewGoal(pedestrian, forces);
            timeToDestination = pedestrian.getTimeToNextGlobalSubGoalByCoordinates(pedestrian.getCurrentNextGlobalAim());
        }

        if (calc.almostEqual(pedestrian.getCurrentSpeed(), 0)) {
            pedestrian.setCurrentSpeed(pedestrian.getPreferredSpeed()); // reset
        }

        pedestrian.updateWalkedDistance(); // adding distance before it is walked at it will reach its destination.
        pedestrian.setLastUpdateTime(roundaboutSimulationModel.getCurrentTime());

        if ( pedestrian.checkExitPortIsReached() && // check if section will be changed
             pedestrian.checkGlobalGoalIsReached()) {
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
                    } else pedestrian.setCurrentSpeed(pedestrian.getPreferredSpeed());
                    //nextStreetSection.handleJamTrafficLight();
                }
            }

            if (freeToGo) {
                // destination of the current street section is reached move to next section
                Point transferPos = pedestrian.transferToNextPortPos();
                pedestrian.moveOneSectionForward(transferPos);
                setNewGoal(pedestrian, forces);
            }
            timeToDestination = pedestrian.getTimeToNextGlobalSubGoalByCoordinates(pedestrian.getCurrentNextGlobalAim());
        }

        pedestrian.setCurrentGlobalPosition(pedestrian.getCurrentNextGlobalAim());
        pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                pedestrian, new TimeSpan(timeToDestination, roundaboutSimulationModel.getModelTimeUnit()));
    }

    void setNewGoal( Pedestrian pedestrian, Vector2d forces ){
        // set as first aim the exit point of the street section
        pedestrian.setCurrentNextGlobalAim();
        pedestrian.setExitPointOnPort( pedestrian.getCurrentNextGlobalAim() );
        PedestrianStreet section = ((PedestrianStreet)pedestrian.getCurrentSection().getStreetSection());

        for (IPedestrian otherPedestrian : section.getPedestrianQueue()){
            if(otherPedestrian.equals(pedestrian)) continue;

            if (! (otherPedestrian instanceof Pedestrian )) {
                throw new IllegalArgumentException( "Other pedestrian not instance of Pedestrian." );
            }

            // if an other pedestrian does already have a defined "current global aim" and
            // does have an intersection with the current aim of the current pedestrian
            // the "current global aim" of the current pedestrian is cut down to it.
            // Otherwise the end of the section is the aim.
            // Afterwards the time until this destination is reached is calculated.
            double minGab = pedestrian.getMinGapForPedestrian();
            if( ((Pedestrian)otherPedestrian).getCurrentNextGlobalAim() != null) {
                Point curGlobPos = pedestrian.getCurrentGlobalPosition();
                Point goal = checkAndSetAimWithinSection(forces, section, pedestrian);

                if(calc.checkLinesIntersectionByCoordinates_WithinSegment(goal.getX(), goal.getY(),
                        pedestrian.getCurrentGlobalPosition(),
                        pedestrian.getCurrentGlobalPosition().getX() + forces.getX(), pedestrian.getCurrentGlobalPosition().getY() + forces.getY(),
                        otherPedestrian.getCurrentGlobalPosition(),
                        ((Pedestrian) otherPedestrian).getCurrentNextGlobalAim())) {
                    // there is a crossing
                    // crossing - the size of the current pedestrian = new destination aim
                    if (calc.getDistanceByCoordinates(goal.getX() - minGab, goal.getY() - minGab, curGlobPos.getX(), curGlobPos.getY() )
                            < calc.getDistanceByCoordinates(goal.getX() + minGab, goal.getY() + minGab, curGlobPos.getX(), curGlobPos.getY() )) {
                        goal.setLocation(goal.getX() - minGab, goal.getY() - minGab);
                    } else {
                        goal.setLocation(goal.getX() + minGab, goal.getY() + minGab);
                    }
                    pedestrian.setCurrentNextGlobalAim(goal);
                }
            }

        }
    }

    Point checkAndSetAimWithinSection(Vector2d forces, PedestrianStreet section, Pedestrian pedestrian) {
        // this is needed to ensure every aim is within a street Section
        //use circumference as max length. (2(a+b))
        double circumference = (section.getLengthX() + section.getLengthY())*2;
        forces.scale(circumference);

        if (! (section instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException ("Type mismatch.");
        }

        Point cornerLeftBottomGlobal = ((PedestrianStreetSection) section).getGlobalCoordinateOfSectionOrigin();
        Point cornerLeftUpGlobal = new Point( (int)cornerLeftBottomGlobal.getX(), (int) (cornerLeftBottomGlobal.getY() +  section.getLengthY()));
        Point cornerRightBottomGlobal = new Point( (int) (cornerLeftBottomGlobal.getX() +  section.getLengthX()), (int) cornerLeftBottomGlobal.getY());
        Point cornerRightUpGlobal = new Point( (int) (cornerLeftBottomGlobal.getX() +  section.getLengthX()), (int) (cornerLeftBottomGlobal.getY() +  section.getLengthY()));
        int borderLineNr = 0;

        boolean withinSection = true;
        double intersectionX, intersectionY = intersectionX = 0;
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersectionX, intersectionY,
                pedestrian.getCurrentGlobalPosition(),
                pedestrian.getCurrentGlobalPosition().getX() + forces.getX(), pedestrian.getCurrentGlobalPosition().getY() + forces.getY(),
                cornerLeftBottomGlobal, cornerLeftUpGlobal
                )){
            withinSection = false;
            borderLineNr = 1;
        } else
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersectionX, intersectionY,
                pedestrian.getCurrentGlobalPosition(),
                pedestrian.getCurrentGlobalPosition().getX() + forces.getX(), pedestrian.getCurrentGlobalPosition().getY() + forces.getY(),
                cornerLeftBottomGlobal, cornerRightBottomGlobal
        )){
            withinSection = false;
            borderLineNr = 2;
        } else
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersectionX, intersectionY,
                pedestrian.getCurrentGlobalPosition(),
                pedestrian.getCurrentGlobalPosition().getX() + forces.getX(), pedestrian.getCurrentGlobalPosition().getY() + forces.getY(),
                cornerLeftUpGlobal, cornerRightUpGlobal
        )){
            withinSection = false;
            borderLineNr = 3;
        } else
        if(calc.checkLinesIntersectionByCoordinates_WithinSegment(
                intersectionX, intersectionY,
                pedestrian.getCurrentGlobalPosition(),
                pedestrian.getCurrentGlobalPosition().getX() + forces.getX(), pedestrian.getCurrentGlobalPosition().getY() + forces.getY(),
                cornerRightBottomGlobal, cornerRightUpGlobal
        )){
            withinSection = false;
            borderLineNr = 4;
        }


        if ( withinSection ) return pedestrian.getCurrentNextGlobalAim();

        if( section.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)) {
            boolean crossingAllowed = false;
            // in this case it might be possible to cross over border
            if(((PedestrianStreetSection) section).getFlexiBorderAlongX() && borderLineNr == 2 && borderLineNr == 3){
                crossingAllowed = true;
            } else if (borderLineNr == 1 && borderLineNr == 4){
                crossingAllowed = true;
            }

            if(crossingAllowed) return pedestrian.getCurrentGlobalPosition();
        }

        // overwrite wall intersection as new aim -> not crossing wall
        double minGab = pedestrian.getMinGapForPedestrian();
        Point curGlobPos = pedestrian.getCurrentGlobalPosition();
        if (calc.getDistanceByCoordinates(intersectionX - minGab, intersectionX - minGab, curGlobPos.getX(), curGlobPos.getY() )
                < calc.getDistanceByCoordinates(intersectionX + minGab, intersectionX + minGab, curGlobPos.getX(), curGlobPos.getY() )) {
            return new Point((int) (intersectionX - minGab), (int)(intersectionY - minGab));
        } else {
            return new Point((int) (intersectionX + minGab), (int) (intersectionY + minGab ));
        }
    }
}