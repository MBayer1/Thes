package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.PedestrianController;
import at.fhv.itm3.s2.roundabout.controller.PedestrianRouteController;
import at.fhv.itm3.s2.roundabout.controller.RouteController;
import at.fhv.itm3.s2.roundabout.entity.*;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.awt.*;
import java.util.List;


public class PedestrianGenerateEvent extends Event<PedestrianAbstractSource> {

    SupportiveCalculations calc;

    /**
     * A reference to the {@link RoundaboutSimulationModel} the {@link PedestrianReachedAimEvent} is part of.
     */
    private RoundaboutSimulationModel roundaboutSimulationModel;

    /**
     * Instance of {@link PedestrianEventFactory} for creating new events.
     */
    protected PedestrianEventFactory pedestrianEventFactory;

    /**
     * Instance of {@link RouteController} for creating new routes.
     * (protected because of testing)
     */
    protected PedestrianRouteController routeController;

    /**
     * Constructs a new {@link PedestrianReachedAimEvent}.
     *
     * @param model the model this event belongs to.
     * @param name this event's name.
     * @param showInTrace flag to indicate if this event shall produce output for the trace.
     */
    public PedestrianGenerateEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);

        pedestrianEventFactory = PedestrianEventFactory.getInstance();

        if (model instanceof RoundaboutSimulationModel) {
            roundaboutSimulationModel = (RoundaboutSimulationModel)model;
        } else {
            throw new IllegalArgumentException("No suitable model given over.");
        }
        routeController = PedestrianRouteController.getInstance(roundaboutSimulationModel);
    }

    /**
     * The event routine describes the generation (arrival) of a new pedestrian.
     *
     * A new pedestrian is generated and added to the given section. A new {@link PedestrianReachedAimEvent} is
     * scheduled for the time the pedestrian needs to traverse this section at optimal conditions, which means
     * that the pedestrian knows how long it needs to reach the end of this section if it can walk to the end of
     * it without the need to stop and after this time the section checks if a pedestrian could leave the section.
     * At the end the event routine schedules a new {@link PedestrianGenerateEvent} with a normally distributed time.
     *
     * @param source instance of {@link PedestrianAbstractSource} in which the pedestrian is generated
     */
    @Override
    public void eventRoutine(PedestrianAbstractSource source) {
        final IConsumer nextSection = source.getConnectedStreet();
        final IPedestrianRoute route = routeController.getRandomRoute(source);
        Point global = ((PedestrianStreetSection) nextSection).getGlobalCoordinateOfSectionOrigin();

        if ( nextSection instanceof  PedestrianStreetSection) {
            if( !(((PedestrianStreetSection) nextSection).getNextStreetConnector() instanceof PedestrianStreetConnector)) {
                throw new IllegalArgumentException("connector not instance of PedestrianConnectedStreetSections");
            }
            PedestrianConnectedStreetSections connectorPair = ((PedestrianStreetConnector)(((PedestrianStreetSection) nextSection).getNextStreetConnector())).getConnectorBySection(nextSection);

            Point start = connectorPair.getPortOfFromStreetSection().getBeginOfStreetPort();
            Point end = connectorPair.getPortOfFromStreetSection().getEndOfStreetPort();
            Point entryPoint = new Point();
            if( calc.almostEqual(end.getX(), start.getX()) ){
                double entryY = roundaboutSimulationModel.getRandomEntryPoint(
                                                                    Math.min(end.getY(), start.getY()),
                                                                    Math.max(end.getY(), start.getY()));
                entryPoint.setLocation(start.getX() + global.getX(), entryY + global.getY());

            } else {
                double entryX = roundaboutSimulationModel.getRandomEntryPoint(
                        Math.min(end.getX(), start.getX()),
                        Math.max(end.getX(), start.getX()));
                entryPoint.setLocation(start.getX() + global.getX(), entryX + global.getY());

            }
            final PedestrianBehaviour behaviour = new PedestrianBehaviour(
                    roundaboutSimulationModel.getRandomPedestrianPreferredSpeed(),
                    0.5,
                    0.5,
                    1,
                    1, //TODO
                    roundaboutSimulationModel.getRandomPedestrianGender(),
                    roundaboutSimulationModel.getRandomPedestrianPsychologicalNature(),
                    roundaboutSimulationModel.getRandomPedestrianAgeGroupe());
            final Pedestrian pedestrian = new Pedestrian(roundaboutSimulationModel, entryPoint, behaviour, route);
            final Car car = new Car(roundaboutSimulationModel, "", false);
            PedestrianController.addCarMapping(car, pedestrian);
            pedestrian.enterSystem();
            ((PedestrianStreet)nextSection).addPedestrian(pedestrian, entryPoint);

            // schedule next events
            final double traverseTime = pedestrian.getTimeToNextSubGoal();
            final PedestrianReachedAimEvent pedestrianReachedAimEvent = pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel);
            pedestrianReachedAimEvent.schedule((PedestrianStreet) nextSection, new TimeSpan(traverseTime, roundaboutSimulationModel.getModelTimeUnit()));

            final PedestrianGenerateEvent pedestrianGenerateEvent = pedestrianEventFactory.createPedestrianGenerateEvent(roundaboutSimulationModel);

            final double minTimeBetweenPedestrianArrivals = roundaboutSimulationModel.getMinTimeBetweenPedestrianArrivals();
            final double meanTimeBetweenPedestrianArrivals = roundaboutSimulationModel.getMeanTimeBetweenPedestrianArrivals();

            final double randomTimeUntilPedestrianArrival = roundaboutSimulationModel.getRandomTimeBetweenPedestrianArrivals();
            final double generatorExpectationShift = source.getGeneratorExpectation() - meanTimeBetweenPedestrianArrivals;

            final double shiftedTimeUntilPedestrianArrival = randomTimeUntilPedestrianArrival + generatorExpectationShift;
            final double actualTimeUntilPedestrianArrival = Math.max(shiftedTimeUntilPedestrianArrival, minTimeBetweenPedestrianArrivals);

            pedestrianGenerateEvent.schedule(source, new TimeSpan(actualTimeUntilPedestrianArrival, roundaboutSimulationModel.getModelTimeUnit()));
        } else {
            throw new IllegalStateException("NextSection should be of type PedestrianStreet");
        }
    }
}