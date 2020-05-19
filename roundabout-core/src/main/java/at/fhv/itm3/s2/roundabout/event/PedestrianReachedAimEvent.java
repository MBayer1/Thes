package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
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


public class PedestrianReachedAimEvent extends Event<Pedestrian> {
    private SupportiveCalculations calc = new SupportiveCalculations();
    private final Integer minTimeBetweenEventCall = 2; //sec = Simulation Time Unit

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
     * If the given section has a car at its exit PedestrianPoint, it is checked if this car could leave this //TODO
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
        if (!(pedestrian instanceof Pedestrian)) {
            throw new IllegalArgumentException("Pedestrian not instance of Pedestrian.");
        }
        // since all sup-aims are within one street section it is enough to check all other pedestrian on the same section
        PedestrianStreetSection currentSection = getCurrentSectionAndCheckForSink(pedestrian);
        if (currentSection == null) return; // reached sink

        if(!currentSection.checkPedestrianIsWithinSection(pedestrian) &&
                !currentSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)) {
            throw new IllegalArgumentException("Pedestrian out of section. Not possible.");
        }

        // check for waiting are before crossing
        if (pedestrian.checkForWaitingArea() ){
            pedestrian.setCurrentSpeed(0);
            pedestrian.startWaiting();
        } else if (calc.almostEqual(pedestrian.getCurrentSpeed(), 0)) {
            pedestrian.setCurrentSpeed(pedestrian.getPreferredSpeed()); // reset
            pedestrian.endPedestrianWaiting();
        }

        double timeToDestination = 0.0;
        PedestrianPoint lastGlobalAim = pedestrian.getCurrentNextGlobalAim();

        // pedestrian reached new partial-aim
        if (pedestrian.getCurrentNextGlobalAim() != null) {
            // pedestrian did not move to next section yet
            pedestrian.updateWalkedDistance(); // adding distance before it is walked at it will reach its destination.
            pedestrian.setLastUpdateTime(roundaboutSimulationModel.getCurrentTime());
            if(! (pedestrian.getCurrentSection().getStreetSection() instanceof  PedestrianSink)) {
                pedestrian.setCurrentGlobalPosition(pedestrian.getCurrentNextGlobalAim());
            }
            pedestrian.setCurrentNextGlobalAim(null); // redefine next goal in next round
        }

        boolean movedToNextSection = false;
        if (pedestrian.checkExitPortIsReached()) { // check if section will be changed
            // move to next section
            PedestrianStreet nextStreetSection = (PedestrianStreet) (pedestrian.getNextSection().getStreetSection());
            boolean keepWalking = false;

            // special case traffic light
            if (nextStreetSection.isTrafficLightActive() && !nextStreetSection.isTrafficLightFreeToGo()) {
                //not freeToGo
                if (nextStreetSection.useMassDynamic()){
                    pedestrian.setCurrentNextGlobalAim(lastGlobalAim);
                    if(roundaboutSimulationModel.massDynamic.doCrossing(pedestrian)){
                        keepWalking = true;
                    } else {
                        pedestrian.setCurrentSpeed(0); // not walking
                        pedestrian.startWaiting();
                        // Event call delay must not be below minTimeBetweenEventCall
                        timeToDestination = minTimeBetweenEventCall;
                    }
                    pedestrian.setCurrentNextGlobalAim(null);
                } else {
                    timeToDestination =  currentSection.getRemainingRedPhase();
                    //nextStreetSection.handleJamTrafficLight(); //  todo
                }
            } else {
                keepWalking = true;
            }

            if ( keepWalking ) {
                // destination of the current street section is reached move to next section
                PedestrianPoint transferPos = pedestrian.transferToNextPortPos();
                pedestrian.moveOneSectionForward(transferPos);
                timeToDestination = 0; // define proper next aim in next loop
                movedToNextSection = true;
            }
        }

        if (pedestrian.getCurrentNextGlobalAim() == null &&
                !(pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianSink)) { // when current section is a sink pedestrian left system
            // consider intersection to other pedestrian etc.
            // -> not the clear goal on exit PedestrianPoint like it is considered in force toward aim
            // pedestrian newly arrived at current section
            Vector2d forces = pedestrian.getSocialForceVector(); //set time when next update.
            pedestrian.setNewGoal(forces);
            timeToDestination = pedestrian.getTimeToNextSubGoal();

            // Event call delay must not be below minTimeBetweenEventCall
            if (timeToDestination < minTimeBetweenEventCall) timeToDestination = minTimeBetweenEventCall;
        }

        if (timeToDestination == 0 && !movedToNextSection && pedestrian.getCurrentSpeed() != 0) {
            //danger of endlessly looping catch
            throw new IllegalStateException("pedestrian is theoretically moving, but already reached destination.");
        }

        // set planed time of movement
        pedestrian.setWalkingTimeStamps(timeToDestination);
        pedestrian.addEventGap(timeToDestination);

        // schedule next event
        try {
            pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                    pedestrian, new TimeSpan(timeToDestination, roundaboutSimulationModel.getModelTimeUnit()));
        } catch (Exception e){
            throw new IllegalArgumentException("Pedestrian Event went wrong.");
        }

        Pedestrian pedestrianToEnter = currentSection.reCheckPedestrianCanEnterSection();

        if ( pedestrianToEnter != null) { // after some movements recheck pedestrians in queue
            pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                    pedestrianToEnter, new TimeSpan(0, roundaboutSimulationModel.getModelTimeUnit()));
        }
    }


    PedestrianStreetSection getCurrentSectionAndCheckForSink( Pedestrian pedestrian) {
        if (!(pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreet)) {
            throw new IllegalArgumentException("Street not instance of PedestrianStreet.");
        }

        IConsumer currentSectionTmp = pedestrian.getCurrentSection().getStreetSection();
        if(currentSectionTmp instanceof PedestrianSink) {
            return null;
        }

        if (!(currentSectionTmp instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException("Street not instance of PedestrianStreet.");
        }

        return (PedestrianStreetSection) currentSectionTmp;
    }
}