package at.fhv.itm3.s2.roundabout.event;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.CarController;
import at.fhv.itm3.s2.roundabout.controller.PedestrianController;
import at.fhv.itm3.s2.roundabout.controller.PedestrianRouteController;
import at.fhv.itm3.s2.roundabout.controller.RouteController;
import at.fhv.itm3.s2.roundabout.entity.*;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.awt.*;

public class PedestrianGenerateEvent extends Event<PedestrianAbstractSource> {

    SupportiveCalculations calc = new SupportiveCalculations();
    Model model;
    String name;
    boolean showInTrace;


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
     *,
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
        this.model = model;
        this.name = name;
        this.showInTrace = showInTrace;
        this.routeController = PedestrianRouteController.getInstance(roundaboutSimulationModel);

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
        final IConsumer currentSection = source.getConnectedStreet();
        final IPedestrianRoute route = routeController.getRandomRoute(source);
        if( !(source instanceof PedestrianSource)) {
            throw new IllegalArgumentException("source is from wrong type.");
        }
        PedestrianPoint global = ((PedestrianSource)source).getGlobalCoordinate();

        if ( currentSection instanceof PedestrianStreetSection ) {
            if (((PedestrianStreetSection) currentSection).getNextStreetConnector() == null) {
                throw new IllegalArgumentException("There are no connected streets");
            }

            PedestrianConnectedStreetSections connectorPair = (((PedestrianStreetSection) currentSection).getPreviousStreetConnector()).get(0);
            if (connectorPair == null) {
                throw new IllegalArgumentException("There is no entry port into system on this Source.");
            }

            PedestrianPoint start = connectorPair.getPortOfFromStreetSection().getLocalBeginOfStreetPort();
            PedestrianPoint end = connectorPair.getPortOfFromStreetSection().getLocalEndOfStreetPort();
            connectorPair.getPortOfToStreetSection().getLocalBeginOfStreetPort();
            connectorPair.getPortOfToStreetSection().getLocalEndOfStreetPort();


            final PedestrianBehaviour behaviour = new PedestrianBehaviour(
                    roundaboutSimulationModel.getRandomPedestrianPreferredSpeed(),
                    roundaboutSimulationModel.getRandomMinGabToPedestrian(),
                    roundaboutSimulationModel.getRandomPedestrianSize_Radius(),
                    roundaboutSimulationModel.getRandomPedestrianGender(),
                    roundaboutSimulationModel.getRandomPedestrianPsychologicalNature(),
                    roundaboutSimulationModel.getRandomPedestrianAgeGroupe());


            PedestrianPoint globalEntryPoint = new PedestrianPoint();
            if (calc.almostEqual(end.getX(), start.getX())) {
                double entryY = roundaboutSimulationModel.getRandomEntryPoint(
                        Math.min(end.getY(), start.getY()) + behaviour.calcGapForPedestrian() ,
                        Math.max(end.getY(), start.getY()) - behaviour.calcGapForPedestrian());
                globalEntryPoint.setLocation(start.getX() + global.getX(), entryY + global.getY());

            } else {
                double entryX = roundaboutSimulationModel.getRandomEntryPoint(
                        Math.min(end.getX(), start.getX()),
                        Math.max(end.getX(), start.getX()));
                globalEntryPoint.setLocation(start.getX() + global.getX(), entryX + global.getY());
            }

            final Pedestrian pedestrian = new Pedestrian(roundaboutSimulationModel, name, showInTrace, globalEntryPoint, behaviour, route);
            PedestrianController.addCarMapping(pedestrian.getCarDummy(), pedestrian);
            final Car car = new Car(roundaboutSimulationModel, "", false);
            CarController.addCarMapping(car, (ICar)pedestrian.getCarDummy());
            if (checkPedestrianCanEnterSystem(pedestrian, globalEntryPoint, (PedestrianStreetSection)currentSection)) {
                pedestrian.enterSystem();
                ((PedestrianStreetSection) currentSection).addPedestrian(pedestrian, globalEntryPoint);
                pedestrian.setCurrentLocalPosition(); // do this after adding to street section

                // schedule next events
                final PedestrianReachedAimEvent pedestrianReachedAimEvent = pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel);
                pedestrianReachedAimEvent.schedule(pedestrian, new TimeSpan(0, roundaboutSimulationModel.getModelTimeUnit()));
            }

            // schedule next events
            final PedestrianGenerateEvent pedestrianGenerateEvent = pedestrianEventFactory.createPedestrianGenerateEvent(roundaboutSimulationModel);

            final double minTimeBetweenPedestrianArrivals = roundaboutSimulationModel.getMinTimeBetweenPedestrianArrivals();
            final double meanTimeBetweenPedestrianArrivals = roundaboutSimulationModel.getMeanTimeBetweenPedestrianArrivals();

            final double randomTimeUntilPedestrianArrival = roundaboutSimulationModel.getRandomTimeBetweenPedestrianArrivals();
            final double generatorExpectationShift = source.getGeneratorExpectation() - meanTimeBetweenPedestrianArrivals;

            final double shiftedTimeUntilPedestrianArrival = randomTimeUntilPedestrianArrival + generatorExpectationShift;
            final double actualTimeUntilPedestrianArrival = Math.max(shiftedTimeUntilPedestrianArrival, minTimeBetweenPedestrianArrivals);

            pedestrianGenerateEvent.schedule(source, new TimeSpan(actualTimeUntilPedestrianArrival, roundaboutSimulationModel.getModelTimeUnit()));

        } else {
            throw new IllegalStateException("CurrentSection should be of type PedestrianStreet");
        }
    }

    private boolean checkPedestrianCanEnterSystem(Pedestrian pedestrian, PedestrianPoint globalEnterPoint, PedestrianStreetSection section) {
        Pedestrian pedestrianToEnter = null;
        if (section.reCheckPedestrianCanEnterSection(pedestrianToEnter)) { // after some movements recheck pedestrians in queue
            pedestrianEventFactory.createPedestrianReachedAimEvent(roundaboutSimulationModel).schedule(
                    pedestrianToEnter, new TimeSpan(10, roundaboutSimulationModel.getModelTimeUnit()));
        }
        if (!section.checkPedestrianCanEnterSection(pedestrian, globalEnterPoint, section)) {
            section.addPedestriansQueueToEnter(pedestrian, globalEnterPoint, section);
            return false;
        }
        return true;
    }
}