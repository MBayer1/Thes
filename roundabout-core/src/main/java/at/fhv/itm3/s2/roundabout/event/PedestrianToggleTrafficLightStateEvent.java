package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreet;
import at.fhv.itm3.s2.roundabout.api.entity.Street;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class PedestrianToggleTrafficLightStateEvent extends Event<PedestrianStreet> {
    /**
     * A reference to the {@link RoundaboutSimulationModel} the {@link PedestrianToggleTrafficLightStateEvent} is part of.
     */
    private RoundaboutSimulationModel roundaboutSimulationModel;

    /**
     * Instance of {@link RoundaboutEventFactory} for creating new events.
     * (protected because of testing)
     */
    protected PedestrianEventFactory pedestrianEventFactory;

    /**
     * Constructs a new {@link PedestrianToggleTrafficLightStateEvent}.
     *
     * @param model       the model this event belongs to.
     * @param name        this event's name.
     * @param showInTrace flag to indicate if this event shall produce output for the trace.
     */
    public PedestrianToggleTrafficLightStateEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);

        pedestrianEventFactory = PedestrianEventFactory.getInstance();

        if (model instanceof RoundaboutSimulationModel) {
            roundaboutSimulationModel = (RoundaboutSimulationModel) model;
        } else {
            throw new IllegalArgumentException("No suitable model given over.");
        }
    }

    /**
     * Toggles the traffic lights state "from free to go" to "stop" and vice versa.
     *
     * @param donorStreet the street car will move away from.
     * @throws SuspendExecution Marker exception for Quasar (inherited).
     */
    @Override
    public void eventRoutine(PedestrianStreet donorStreet) throws SuspendExecution {
        donorStreet.setTrafficLightFreeToGo( !donorStreet.isTrafficLightFreeToGo() );

        // cyclic traffic light
        if (donorStreet.isTrafficLightFreeToGo()) {
            // triggered to green
            pedestrianEventFactory.createToggleTrafficLightStateEvent(roundaboutSimulationModel).schedule(
                    donorStreet,
                    new TimeSpan(
                            donorStreet.getGreenPhaseDurationOfTrafficLight(),
                            roundaboutSimulationModel.getModelTimeUnit()
                    )
            );
        } else {
            pedestrianEventFactory.createToggleTrafficLightStateEvent(roundaboutSimulationModel).schedule(
                    donorStreet,
                    new TimeSpan(
                            donorStreet.getRedPhaseDurationOfTrafficLight(),
                            roundaboutSimulationModel.getModelTimeUnit()
                    )
            );
        }

    }
}
