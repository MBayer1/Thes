package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrian;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianConsumerType;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreet;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import javax.vecmath.Vector2d;
import java.awt.*;


public class PedestrianReachedAimEvent extends Event<Pedestrian> {
    private SupportiveCalculations calc;

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
        // check if the check if there
        if (! (pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreet )) {
            throw new IllegalArgumentException( "Street not instance of PedestrianStreet." );
        }

        for (IPedestrian otherPedestrian : ((PedestrianStreet)pedestrian.getCurrentSection().getStreetSection()).getPedestrianQueue()){
            if(otherPedestrian.equals(pedestrian)) continue;

            if (! (otherPedestrian instanceof Pedestrian )) {
                throw new IllegalArgumentException( "Other pedestrian not instance of Pedestrian." );
            }

            // aim is reached -> update data
            pedestrian.updateWalkedDistance();
            pedestrian.setCurrentGlobalPosition();
            pedestrian.setLastUpdateTime(roundaboutSimulationModel.getCurrentTime());

            // destination of the current street section is reached
            if(pedestrian.checkExitPortIsReached()){
                PedestrianStreet nextStreetSection = ((PedestrianStreet)(pedestrian.getNextSection().getStreetSection()));
                // when the next section is a pedestrian crossing and does have a crossing light the light stage has to be checked
                if( nextStreetSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)){
                    if ( !(currentSection instanceof PedestrianStreet) ){
                        throw new IllegalArgumentException( "Street not instance of PedestrianStreet.");
                    }

                    // solely on a crossing can be a traffic light. -> check in Parser
                    if( nextStreetSection.isTrafficLightActive() ){
                        if( !nextStreetSection.isTrafficLightFreeToGo()) {
                            pedestrian.setCurrentSpeed(0.0);
                        } else pedestrian.setCurrentSpeed(pedestrian.getPreferredSpeed());
                        //nextStreetSection.handleJamTrafficLight();
                    }
                }
                // set to next section

                timeToDestination = pedestrian.getTimeToNextGlobalSubGoalByCoordinates(pedestrian.getCurrentNextGlobalAim());

            } else {

                // if an other pedestrian does already have a defined "current global aim" and
                // does have an intersection with the current aim of the current pedestrian
                // the "current global aim" of the current pedestrian is cut down to it.
                // Otherwise the end of the section is the aim.
                // Afterwards the time until this destination is reached is calculated.
                if( ((Pedestrian)otherPedestrian).getCurrentNextGlobalAim() != null) {
                    double goalX, goalY = goalX = 0.0;
                    if(calc.checkLinesIntersectionByCoordinates(goalX, goalY,
                            pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY(),
                            pedestrian.getCurrentGlobalPosition().getX() + forces.getX(), pedestrian.getCurrentGlobalPosition().getY() + forces.getY(),
                            otherPedestrian.getCurrentGlobalPosition().getX(), otherPedestrian.getCurrentGlobalPosition().getY(),
                            ((Pedestrian) otherPedestrian).getCurrentNextGlobalAim().getX(),((Pedestrian) otherPedestrian).getCurrentNextGlobalAim().getY())) {
                        // there is a crossing
                        // crossing - the size of the current pedestrian = new destination aim
                        goalX -= pedestrian.getMinGapForPedestrian();
                        goalY -= pedestrian.getMinGapForPedestrian();
                        pedestrian.setCurrentNextGlobalAim(new Point ((int)goalX, (int)goalY));
                    }
                }
                timeToDestination = pedestrian.getTimeToNextGlobalSubGoalByCoordinates(pedestrian.getCurrentNextGlobalAim());
            }
        }

        pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                pedestrian, new TimeSpan(timeToDestination, roundaboutSimulationModel.getModelTimeUnit())
        );
    }

}