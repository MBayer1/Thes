package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
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
        IConsumer currentSection = pedestrian.getCurrentSection().getStreetSection();
        if(((PedestrianStreet)currentSection).getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_SINK)) {
            return;
        }
        if (!(pedestrian instanceof Pedestrian)) {
            throw new IllegalArgumentException("Pedestrian not instance of Pedestrian.");
        }

        // pedestrian reached new aim
        if (pedestrian.getCurrentNextGlobalAim() != null) {
            // pedestrian did not move to next section yet
            pedestrian.updateWalkedDistance(); // adding distance before it is walked at it will reach its destination.
            pedestrian.setLastUpdateTime(roundaboutSimulationModel.getCurrentTime());
            if(! (pedestrian.getCurrentSection().getStreetSection() instanceof  PedestrianSink)) {
                pedestrian.setCurrentGlobalPosition(pedestrian.getCurrentNextGlobalAim());
            }
            pedestrian.setCurrentNextGlobalAim(null); // redefine next goal in next round
        }

        // check for waiting are before crossing
        if (pedestrian.checkForWaitingArea()){
            pedestrian.setCurrentPreferredSpeedToUse(0);
        } else if (calc.almostEqual(pedestrian.getCurrentPreferredSpeedToUse(), 0)) {
            pedestrian.setCurrentPreferredSpeedToUse(pedestrian.getPreferredSpeed()); // reset
        }

        Vector2d forces = pedestrian.getSocialForceVector(); //set time when next update.
        double timeToDestination = 0.0;

        // since all sup-aims are within one street section it is enough to check all other pedestrian on the same section
        if (!(pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreet)) {
            throw new IllegalArgumentException("Street not instance of PedestrianStreet.");
        }

        if (pedestrian.getCurrentNextGlobalAim() == null) {
            // consider intersection to other pedestrian etc.
            // -> not the clear goal on exit PedestrianPoint like it is considered in force toward aim
            // pedestrian newly arrived at current section
            pedestrian.setNewGoal(forces);
            timeToDestination = pedestrian.getTimeToNextGlobalSubGoal();
        }

        boolean movedToNextSection = false;

        if (pedestrian.checkExitPortIsReached()) { // check if section will be changed
            // set to next section
            if (!(currentSection instanceof PedestrianStreet)) {
                throw new IllegalArgumentException("Street not instance of PedestrianStreet.");
            }

            PedestrianStreet nextStreetSection = ((PedestrianStreet) (pedestrian.getNextSection().getStreetSection()));
            if (nextStreetSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_SINK)) {
                if (!(nextStreetSection instanceof PedestrianSink)) {
                    throw new IllegalArgumentException("Street not instance of PedestrianSink.");
                }
            } else if (!(nextStreetSection instanceof PedestrianStreet)) {
                throw new IllegalArgumentException("Street not instance of PedestrianStreet.");
            }

            boolean freeToGo = true;
            // when the next section is a pedestrian crossing and does have a crossing light the light stage has to be checked
            if (nextStreetSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)) {
                // solely on a crossing can be a traffic light. -> check in Parser
                if (nextStreetSection.isTrafficLightActive()) {
                    if (!nextStreetSection.isTrafficLightFreeToGo()) {
                        freeToGo = false;
                        timeToDestination = ((PedestrianStreet) currentSection).getRemainingRedPhase();
                        //nextStreetSection.handleJamTrafficLight();
                    } else {
                        timeToDestination = pedestrian.getTimeToNextGlobalSubGoal();
                    }
                }
            }

            if (freeToGo) {
                // destination of the current street section is reached move to next section
                PedestrianPoint transferPos = pedestrian.transferToNextPortPos();
                pedestrian.moveOneSectionForward(transferPos);
                timeToDestination = 0; // define proper next aim in next loop
                movedToNextSection = true;
            }
        }

        if (timeToDestination == 0 && !movedToNextSection && pedestrian.getCurrentSpeed() != 0) {
            //danger of endlessly looping catch
            throw new IllegalStateException("pedestrian is theoretically moving, but already reached destination.");
        }

        // set planed time of movement
        pedestrian.setWalkingTimeStamps(timeToDestination);

        // schedule next event
        pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                pedestrian, new TimeSpan(timeToDestination, roundaboutSimulationModel.getModelTimeUnit()));

        Pedestrian pedestrianToEnter = null;
        if (((PedestrianStreetSection) currentSection).reCheckPedestrianCanEnterSection(pedestrianToEnter)) { // after some movements recheck pedestrians in queue
            pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                    pedestrianToEnter, new TimeSpan(0, roundaboutSimulationModel.getModelTimeUnit()));
        }
    }

}