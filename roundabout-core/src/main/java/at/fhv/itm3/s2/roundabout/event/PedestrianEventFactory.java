package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

public class PedestrianEventFactory {
    /**
     * Holds a static instance of a {@link PedestrianEventFactory} object.
     */
    private static PedestrianEventFactory instance;

    /**
     * Private constructor so it isn't possible to create a new
     * {@link PedestrianEventFactory} object via the constructor.
     * Use {@link #getInstance()} instead.
     */
    private PedestrianEventFactory() {
    }

    /**
     * Returns a singleton instance of a {@link PedestrianEventFactory}.
     *
     * @return a {@link PedestrianEventFactory} instance.
     */
    public static PedestrianEventFactory getInstance() {
        if (instance == null) {
            instance = new PedestrianEventFactory();
        }
        return instance;
    }

    /**
     * Creates a new {@link PedestrianGenerateEvent} within the given model.
     *
     * @param model the model the event is part of.
     * @return the newly created {@link PedestrianGenerateEvent}.
     */
    public PedestrianGenerateEvent createPedestrianGenerateEvent(RoundaboutSimulationModel model) {
        return new PedestrianGenerateEvent(model, "PedestrianGenerateEvent", true);
    }


    /**
     * Creates a new {@link PedestrianGenerateEvent} within the given model.
     *
     * @param model the model the event is part of.
     * @return the newly created {@link PedestrianGenerateEvent}.
     */
    public PedestrianReachedAimEvent pedestrianReachedAimEvent(RoundaboutSimulationModel model) {
        return new PedestrianReachedAimEvent(model, "PedestrianGenerateEvent", true);
    }

    /**
     * Creates a new {@link ToggleTrafficLightStateEvent} within the given model.
     *
     * @param model the model the event is part of.
     * @return the newly created {@link ToggleTrafficLightStateEvent}.
     */
    public ToggleTrafficLightStateEvent createToggleTrafficLightStateEvent(RoundaboutSimulationModel model) {
        return new ToggleTrafficLightStateEvent(model, "ToggleTrafficLightStateEvent", true);
    }
}
