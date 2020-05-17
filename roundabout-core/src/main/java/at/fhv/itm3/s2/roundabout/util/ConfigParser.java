package at.fhv.itm3.s2.roundabout.util;

import at.fhv.itm14.trafsim.model.entities.AbstractConsumer;
import at.fhv.itm14.trafsim.model.entities.AbstractProducer;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.entities.intersection.FixedCirculationController;
import at.fhv.itm14.trafsim.model.entities.intersection.Intersection;
import at.fhv.itm14.trafsim.model.entities.intersection.IntersectionConnection;
import at.fhv.itm14.trafsim.model.entities.intersection.IntersectionPhase;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.IntersectionController;
import at.fhv.itm3.s2.roundabout.controller.PedestrianRouteController;
import at.fhv.itm3.s2.roundabout.controller.RouteController;
import at.fhv.itm3.s2.roundabout.entity.*;
import at.fhv.itm3.s2.roundabout.entity.PedestrianRoute;
import at.fhv.itm3.s2.roundabout.entity.Route;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import at.fhv.itm3.s2.roundabout.util.dto.*;
import at.fhv.itm3.s2.roundabout.util.dto.Component;
import at.fhv.itm3.s2.roundabout.util.dto.StreetNeighbour;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;

import javax.xml.bind.JAXB;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toMap;

public class ConfigParser {
    private static final String SIMULATION_SEED = "SIMULATION_SEED";
    private static final String MIN_TIME_BETWEEN_CAR_ARRIVALS = "MIN_TIME_BETWEEN_CAR_ARRIVALS";
    private static final String MAX_TIME_BETWEEN_CAR_ARRIVALS = "MAX_TIME_BETWEEN_CAR_ARRIVALS";
    private static final String MIN_DISTANCE_FACTOR_BETWEEN_CARS = "MIN_DISTANCE_FACTOR_BETWEEN_CARS";
    private static final String MAX_DISTANCE_FACTOR_BETWEEN_CARS = "MAX_DISTANCE_FACTOR_BETWEEN_CARS";
    private static final String MAIN_ARRIVAL_RATE_FOR_ONE_WAY_STREETS = "MAIN_ARRIVAL_RATE_FOR_ONE_WAY_STREETS";
    private static final String STANDARD_CAR_ACCELERATION_TIME = "STANDARD_CAR_ACCELERATION_TIME";
    private static final String MIN_CAR_LENGTH = "MIN_CAR_LENGTH";
    private static final String MAX_CAR_LENGTH = "MAX_CAR_LENGTH";
    private static final String EXPECTED_CAR_LENGTH = "EXPECTED_CAR_LENGTH";
    private static final String MIN_TRUCK_LENGTH = "MIN_TRUCK_LENGTH";
    private static final String MAX_TRUCK_LENGTH = "MAX_TRUCK_LENGTH";
    private static final String EXPECTED_TRUCK_LENGTH = "EXPECTED_TRUCK_LENGTH";
    private static final String CAR_RATIO_PER_TOTAL_VEHICLE = "CAR_RATIO_PER_TOTAL_VEHICLE";
    private static final String JAM_INDICATOR_IN_SECONDS = "JAM_INDICATOR_IN_SECONDS";

    private static final String MIN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS = "MIN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS";
    private static final String MAX_TIME_BETWEEN_PEDESTRIAN_ARRIVALS = "MAX_TIME_BETWEEN_PEDESTRIAN_ARRIVALS";
    private static final String MEAN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS = "MEAN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS";
    private static final String MIN_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN = "MIN_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN";
    private static final String MAX_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN = "MAX_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN";
    private static final String MIN_PEDESTRIAN_GROUP_SIZE = "MIN_PEDESTRIAN_GROUP_SIZE";
    private static final String MAX_PEDESTRIAN_GROUP_SIZE = "MAX_PEDESTRIAN_GROUP_SIZE";
    private static final String MIN_PEDESTRIAN_STREET_LENGTH = "MIN_PEDESTRIAN_STREET_LENGTH";
    private static final String MIN_PEDESTRIAN_STREET_WIDTH = "MIN_PEDESTRIAN_STREET_WIDTH";

    private static final String MIN_PEDESTRIAN_RELAXING_TIME = "MIN_PEDESTRIAN_RELAXING_TIME";
    private static final String MAX_PEDESTRIAN_RELAXING_TIME = "MAX_PEDESTRIAN_RELAXING_TIME";
    private static final String EXPECTED_PEDESTRIAN_RELAXING_TIME = "EXPECTED_PEDESTRIAN_RELAXING_TIME";
    private static final String MIN_PEDESTRIAN_SIZE_RADIUS = "MIN_PEDESTRIAN_SIZE_RADIUS";
    private static final String MAX_PEDESTRIAN_SIZE_RADIUS = "MAX_PEDESTRIAN_SIZE_RADIUS";
    private static final String EXPECTED_PEDESTRIAN_SIZE_RADIUS = "EXPECTED_PEDESTRIAN_SIZE_RADIUS";
    private static final String MIN_PEDESTRIAN_MIN_GAP = "MIN_PEDESTRIAN_MIN_GAP";
    private static final String MAX_PEDESTRIAN_MIN_GAP = "MAX_PEDESTRIAN_MIN_GAP";
    private static final String EXPECTED_PEDESTRIAN_MIN_GAP = "EXPECTED_PEDESTRIAN_MIN_GAP";
    private static final String MIN_PEDESTRIAN_PREFERRED_SPEED = "MIN_PEDESTRIAN_PREFERRED_SPEED";
    private static final String MAX_PEDESTRIAN_PREFERRED_SPEED = "MAX_PEDESTRIAN_PREFERRED_SPEED";
    private static final String EXPECTED_PEDESTRIAN_PREFERRED_SPEED = "EXPECTED_PEDESTRIAN_PREFERRED_SPEED";

    private static final String MIN_PEDESTRIAN_GENDER = "MIN_PEDESTRIAN_GENDER";
    private static final String MAX_PEDESTRIAN_GENDER = "MAX_PEDESTRIAN_GENDER";
    private static final String EXPECTED_PEDESTRIAN_GENDER = "EXPECTED_PEDESTRIAN_GENDER";
    private static final String MIN_PEDESTRIAN_AGE_RANGE_GROUP = "MIN_PEDESTRIAN_AGE_RANGE_GROUP";
    private static final String MAX_PEDESTRIAN_AGE_RANGE_GROUP = "MAX_PEDESTRIAN_AGE_RANGE_GROUP";
    private static final String EXPECTED_PEDESTRIAN_AGE_RANGE_GROUP = "EXPECTED_PEDESTRIAN_AGE_RANGE_GROUP";
    private static final String MIN_PEDESTRIAN_PSYCHOLOGICAL_NATURE = "MIN_PEDESTRIAN_PSYCHOLOGICAL_NATURE";
    private static final String MAX_PEDESTRIAN_PSYCHOLOGICAL_NATURE = "MAX_PEDESTRIAN_PSYCHOLOGICAL_NATURE";
    private static final String EXPECTED_PEDESTRIAN_PSYCHOLOGICAL_NATURE = "EXPECTED_PEDESTRIAN_PSYCHOLOGICAL_NATURE";
    private static final String MAX_DISTANCE_FOR_WAITING_AREA = "MAX_DISTANCE_FOR_WAITING_AREA";


    private static final String INTERSECTION_SIZE = "INTERSECTION_SIZE";
    private static final String INTERSECTION_SERVICE_DELAY = "INTERSECTION_SERVICE_DELAY";
    private static final String CONTROLLER_GREEN_DURATION = "CONTROLLER_GREEN_DURATION";
    private static final String CONTROLLER_YELLOW_DURATION = "CONTROLLER_YELLOW_DURATION";
    private static final String CONTROLLER_PHASE_SHIFT_TIME = "CONTROLLER_PHASE_SHIFT_TIME";

    private static final String SFM_DEGREE_OF_ACCURACY = "SFM_DEGREE_OF_ACCURACY";

    private static final Map<String, String> MODEL_PARAMETERS = new HashMap<>();
    private static final Map<String, Map<String, RoundaboutSource>> SOURCE_REGISTRY = new HashMap<>(); // componentId, sectionId, section
    private static final Map<String, Map<String, RoundaboutSink>> SINK_REGISTRY = new HashMap<>();
    private static final Map<String, Map<String, StreetSection>> SECTION_REGISTRY = new HashMap<>();
    private static final Map<String, Map<String, PedestrianStreetSection>> PEDESTRIAN_SECTION_REGISTRY = new HashMap<>();
    private static final Map<String, Map<String, PedestrianSource>> PEDESTRIAN_SOURCE_REGISTRY = new HashMap<>(); // componentId, sectionId, section
    private static final Map<String, Map<String, PedestrianSink>> PEDESTRIAN_SINK_REGISTRY = new HashMap<>();
    private static final Map<AbstractSource, Map<RoundaboutSink, Route>> ROUTE_REGISTRY = new HashMap<>(); // source, sink, route
    private static final Map<PedestrianAbstractSource, Map<PedestrianSink, PedestrianRoute>> PEDESTRIAN_ROUTE_REGISTRY = new HashMap<>(); // source, sink, route
    private static final Map<String, Intersection> INTERSECTION_REGISTRY = new HashMap<>(); // componentId, intersection

    private static final Map<IConsumer , at.fhv.itm3.s2.roundabout.entity.StreetNeighbour> STREET_NEIGHBOURS_REGISTRY = new HashMap<>(); //needed for Gui

    private static final double DEFAULT_ROUTE_RATIO = 0.0;
    private static SupportiveCalculations calc = new SupportiveCalculations();

    private static final Comparator<Track> TRACK_COMPARATOR = Comparator.comparingLong(Track::getOrder);
    private static final Function<Connector, List<Track>> SORTED_TRACK_EXTRACTOR =
            co -> co.getTrack().stream().sorted(TRACK_COMPARATOR).collect(Collectors.toList());

    private static final Function<PedestrianConnector, List<PedestrianTrack>> PEDESTRIAN_TRACK_EXTRACTOR =
            co -> co.getPedestrianTrack().stream().collect(Collectors.toList());

    private String filename;

    public ConfigParser(String filename) {
        this.filename = filename;
    }

    public ModelConfig loadConfig() throws ConfigParserException {
        File configFile = new File(filename);
        if (!configFile.exists()) {
            configFile = new File(getClass().getResource(filename).getPath());
            if (!configFile.exists()) {
                throw new ConfigParserException("No such config file " + filename);
            }
        }
        return JAXB.unmarshal(configFile, ModelConfig.class);
    }

    public IModelStructure initRoundaboutStructure(ModelConfig modelConfig, Experiment experiment) {
        final Map<String, String> parameters = handleParameters(modelConfig);

        final List<Component> components = modelConfig.getComponents().getComponent();
        final List<Source> modelSources = components.stream().map(Component::getSources).map(Sources::getSource).flatMap(Collection::stream).collect(Collectors.toList());
        final List<Double> generatorExpectations = modelSources.stream().map(Source::getGeneratorExpectation).filter(Objects::nonNull).sorted().collect(Collectors.toList());

        // Compatibility for the rest of structure is achieved via insertion of property.
        final double generatorExpectationMedian = calculateMedian(generatorExpectations);
        parameters.put(MAX_TIME_BETWEEN_CAR_ARRIVALS, String.valueOf(generatorExpectationMedian));

        final RoundaboutSimulationModel model = new RoundaboutSimulationModel(
            extractParameter(parameters::get, Long::valueOf, SIMULATION_SEED),
            null,
            modelConfig.getName(),
            false,
            false,
            extractParameter(parameters::get, Double::valueOf, MIN_TIME_BETWEEN_CAR_ARRIVALS),
            extractParameter(parameters::get, Double::valueOf, MAX_TIME_BETWEEN_CAR_ARRIVALS),
            extractParameter(parameters::get, Double::valueOf, MIN_DISTANCE_FACTOR_BETWEEN_CARS),
            extractParameter(parameters::get, Double::valueOf, MAX_DISTANCE_FACTOR_BETWEEN_CARS),
            extractParameter(parameters::get, Double::valueOf, MAIN_ARRIVAL_RATE_FOR_ONE_WAY_STREETS),
            extractParameter(parameters::get, Double::valueOf, STANDARD_CAR_ACCELERATION_TIME),
            extractParameter(parameters::get, Double::valueOf, MIN_CAR_LENGTH),
            extractParameter(parameters::get, Double::valueOf, MAX_CAR_LENGTH),
            extractParameter(parameters::get, Double::valueOf, EXPECTED_CAR_LENGTH),
            extractParameter(parameters::get, Double::valueOf, MIN_TRUCK_LENGTH),
            extractParameter(parameters::get, Double::valueOf, MAX_TRUCK_LENGTH),
            extractParameter(parameters::get, Double::valueOf, EXPECTED_TRUCK_LENGTH),
            extractParameter(parameters::get, Double::valueOf, CAR_RATIO_PER_TOTAL_VEHICLE),
            extractParameter(parameters::get, Double::valueOf, JAM_INDICATOR_IN_SECONDS),

            extractParameter(parameters::get, Double::valueOf, MIN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS),
            extractParameter(parameters::get, Double::valueOf, MAX_TIME_BETWEEN_PEDESTRIAN_ARRIVALS),
            extractParameter(parameters::get, Double::valueOf, MIN_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN),
            extractParameter(parameters::get, Double::valueOf, MAX_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN),

            extractParameter(parameters::get, Long::valueOf, MIN_PEDESTRIAN_GROUP_SIZE),
            extractParameter(parameters::get, Long::valueOf, MAX_PEDESTRIAN_GROUP_SIZE),
            extractParameter(parameters::get, Double::valueOf, MIN_PEDESTRIAN_STREET_LENGTH),
            extractParameter(parameters::get, Double::valueOf, MIN_PEDESTRIAN_STREET_WIDTH),
            extractParameter(parameters::get, Double::valueOf, SFM_DEGREE_OF_ACCURACY),

            extractParameter(parameters::get, Double::valueOf, MIN_PEDESTRIAN_RELAXING_TIME),
            extractParameter(parameters::get, Double::valueOf, MAX_PEDESTRIAN_RELAXING_TIME),
            extractParameter(parameters::get, Double::valueOf, EXPECTED_PEDESTRIAN_RELAXING_TIME),
            extractParameter(parameters::get, Double::valueOf, MIN_PEDESTRIAN_SIZE_RADIUS),
            extractParameter(parameters::get, Double::valueOf, MAX_PEDESTRIAN_SIZE_RADIUS),
            extractParameter(parameters::get, Double::valueOf, EXPECTED_PEDESTRIAN_SIZE_RADIUS),
            extractParameter(parameters::get, Double::valueOf, MIN_PEDESTRIAN_MIN_GAP),
            extractParameter(parameters::get, Double::valueOf, MAX_PEDESTRIAN_MIN_GAP),
            extractParameter(parameters::get, Double::valueOf, EXPECTED_PEDESTRIAN_MIN_GAP),
            extractParameter(parameters::get, Double::valueOf, MIN_PEDESTRIAN_PREFERRED_SPEED),
            extractParameter(parameters::get, Double::valueOf, MAX_PEDESTRIAN_PREFERRED_SPEED),
            extractParameter(parameters::get, Double::valueOf, EXPECTED_PEDESTRIAN_PREFERRED_SPEED),

            extractParameter(parameters::get, Double::valueOf, MAX_DISTANCE_FOR_WAITING_AREA)
        );

        model.connectToExperiment(experiment);  // ! - Should be done before anything else.
        model.initMassDynamic();

        final IModelStructure modelStructure = new ModelStructure(model, parameters);

        // Handling model components.
        handleComponents(modelStructure, modelConfig.getComponents());

        if (modelConfig.getComponents().getConnectors() != null) {
            if (modelConfig.getComponents().getComponent() != null)
                handleConnectors(null, modelConfig.getComponents().getConnectors());
            if (modelConfig.getComponents().getPedestrianConnectors() != null)
                handlePedestrianConnectors(null, modelConfig.getComponents().getPedestrianConnectors());
        }

        // Adding intersections.
        modelStructure.addIntersections(INTERSECTION_REGISTRY.values());

        // Handling and adding routes.
        final List<Route> routes = handleRoutes(modelConfig).values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        modelStructure.addRoutes(routes);

        final List<PedestrianRoute> pedestrianRoutes = handlePedestrianRoutes(modelConfig).values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        modelStructure.addPedestrianRoutes(pedestrianRoutes);
        initGlobalCoordinates(modelConfig);

        RouteController.getInstance(model).setRoutes(modelStructure.getRoutes());
        PedestrianRouteController.getInstance(model).setRoutes(modelStructure.getPedestrianRoutes());
        model.registerModelStructure(modelStructure);
        return modelStructure;
    }

    public Map<String, Map<String, RoundaboutSource>> getSourceRegistry() {
        return Collections.unmodifiableMap(SOURCE_REGISTRY);
    }

    public Map<String, Map<String, StreetSection>> getSectionRegistry() {
        return Collections.unmodifiableMap(SECTION_REGISTRY);
    }

    public Map<String, Map<String, RoundaboutSink>> getSinkRegistry() {
        return Collections.unmodifiableMap(SINK_REGISTRY);
    }


    public Map<String, Map<String, PedestrianSource>> getPedestrianSourceRegistry() {
        return Collections.unmodifiableMap(PEDESTRIAN_SOURCE_REGISTRY);
    }

    public Map<String, Map<String, PedestrianStreetSection>> getPedestrianSectionRegistry() {
        return Collections.unmodifiableMap(PEDESTRIAN_SECTION_REGISTRY);
    }

    public Map<String, Map<String, PedestrianSink>> getPedestrianSinkRegistry() {
        return Collections.unmodifiableMap(PEDESTRIAN_SINK_REGISTRY);
    }

    private Map<String, String> handleParameters(ModelConfig modelConfig) {
        final Consumer<Parameter> parameterRegistrator = p -> MODEL_PARAMETERS.put(p.getName(), p.getValue());
        modelConfig.getParameters().getParameter().forEach(parameterRegistrator);

        final List<Component> componentList = modelConfig.getComponents().getComponent();
        for (Component component : componentList) {
            if (component.getParameters() != null) {
                component.getParameters().getParameter().forEach(parameterRegistrator);
            }
        }
        return MODEL_PARAMETERS;
    }

    private void handleComponents(IModelStructure modelStructure, Components modelComponents) {
        for (Component component : modelComponents.getComponent()) {
            switch (component.getType()) {
                case ROUNDABOUT: {
                    handleRoundabout(modelStructure, component);
                    break;
                }

                case INTERSECTION: {
                    handleIntersection(modelStructure, component);
                    break;
                }

                case PEDESTRIANWALKINGAREA: {
                    handlePedestrianWalkingArea(modelStructure, component);
                    break;
                }

                default: throw new IllegalArgumentException("Unknown component type detected.");
            }
        }
    }

    private void handlePedestrianWalkingArea(IModelStructure modelStructure, Component roundaboutComponent){
        final Model model = modelStructure.getModel();

        // Handle configuration.
        final Map<String, PedestrianStreetSection> sections = handlePedestrianSections(
                roundaboutComponent.getId(),
                roundaboutComponent.getSections(),
                model
        );

        final Map<String, PedestrianSink> sinks = handlePedestrianSinks(
                roundaboutComponent.getId(),
                roundaboutComponent.getSinks(),
                model
        );

        final Map<String, PedestrianSource> sources = handlePedestrianSources(
                roundaboutComponent.getId(),
                roundaboutComponent.getSources(),
                model
        );

        final Map<String, PedestrianStreetConnector> connectors = handlePedestrianConnectors(
                roundaboutComponent.getId(),
                roundaboutComponent.getPedestrianConnectors()
        );

        modelStructure.addPedestrianStreets(sections.values());
        modelStructure.addPedestrianStreetConnectors(connectors.values());
        modelStructure.addPedestrianSources(sources.values());
        modelStructure.addPedestrianSinks(sinks.values());
    }

    private void handleRoundabout(IModelStructure modelStructure, Component roundaboutComponent) {
        final Model model = modelStructure.getModel();

        // Handle configuration.
        final Map<String, StreetSection> sections = handleSections(
            roundaboutComponent.getId(),
            roundaboutComponent.getSections(),
            model
        );

        final Map<String, RoundaboutSink> sinks = handleSinks(
            roundaboutComponent.getId(),
            roundaboutComponent.getSinks(),
            model
        );

        final Map<String, RoundaboutSource> sources = handleSources(
            roundaboutComponent.getId(),
            roundaboutComponent.getSources(),
            model
        );

        final Map<String, StreetConnector> connectors = handleConnectors(
            roundaboutComponent.getId(),
            roundaboutComponent.getConnectors()
        );

        modelStructure.addStreets(sections.values());
        modelStructure.addStreetConnectors(connectors.values());
        modelStructure.addSources(sources.values());
        modelStructure.addSinks(sinks.values());
    }

    private void handleIntersection(IModelStructure modelStructure, Component intersectionComponent) {
        final Model model = modelStructure.getModel();

        // Handle the rest of configuration.
        final Map<String, StreetSection> sections = handleSections(
            intersectionComponent.getId(),
            intersectionComponent.getSections(),
            model
        );

        final Map<String, RoundaboutSink> sinks = handleSinks(
            intersectionComponent.getId(),
            intersectionComponent.getSinks(),
            model
        );

        final Map<String, RoundaboutSource> sources = handleSources(
            intersectionComponent.getId(),
            intersectionComponent.getSources(),
            model
        );

        // Init intersection.
        final RoundaboutIntersection intersection = new RoundaboutIntersection(
            model,
            intersectionComponent.getName(),
            false,
            Integer.parseInt(modelStructure.getParameter(INTERSECTION_SIZE))
        );
        intersection.setServiceDelay(
            extractParameter(modelStructure::getParameter, Double::valueOf, INTERSECTION_SERVICE_DELAY)
        );

        final List<IntersectionPhase> phases = handlePhases(
            intersectionComponent.getId(),
            intersection,
            intersectionComponent.getConnectors(),
            extractParameter(modelStructure::getParameter, Double::valueOf, CONTROLLER_YELLOW_DURATION)
        );

        final FixedCirculationController ic = new FixedCirculationController(
            model,
            intersection.getName(),
            false,
            intersection,
            phases,
            extractParameter(modelStructure::getParameter, Double::valueOf, CONTROLLER_GREEN_DURATION),
            extractParameter(modelStructure::getParameter, Double::valueOf, CONTROLLER_PHASE_SHIFT_TIME)
        );
        intersection.attachController(ic);

        modelStructure.addStreets(sections.values());
        modelStructure.addSources(sources.values());
        modelStructure.addSinks(sinks.values());
        // Registering intersection.
        INTERSECTION_REGISTRY.put(intersectionComponent.getId(), intersection);
    }

    private Map<String, PedestrianSource> handlePedestrianSources(String scopeComponentId, Sources sources, Model model)throws IllegalStateException  {
            return sources.getSource().stream().collect(toMap(
                    Source::getId,
                    so -> {
                        final PedestrianStreetSection street = resolvePedestrianSection(scopeComponentId, so.getSectionId());
                        final PedestrianSource source = new PedestrianSource(so.getId(), so.getGeneratorExpectation(),
                                model, so.getId(), false, street);
                        if (!PEDESTRIAN_SOURCE_REGISTRY.containsKey(scopeComponentId)) {
                            PEDESTRIAN_SOURCE_REGISTRY.put(scopeComponentId, new HashMap<>());
                        }

                        PEDESTRIAN_SOURCE_REGISTRY.get(scopeComponentId).put(so.getId(), source);
                        return source;
                    }
            ));
    }

    private Map<String, RoundaboutSource> handleSources(String scopeComponentId, Sources sources, Model model) {
        return sources.getSource().stream().collect(toMap(
                Source::getId,
                so -> {
                    final Street street = resolveSection(scopeComponentId, so.getSectionId());
                    final RoundaboutSource source = new RoundaboutSource(so.getId(), so.getGeneratorExpectation(), model, so.getId(), false, street);
                    if (!SOURCE_REGISTRY.containsKey(scopeComponentId)) {
                        SOURCE_REGISTRY.put(scopeComponentId, new HashMap<>());
                    }

                    SOURCE_REGISTRY.get(scopeComponentId).put(so.getId(), source);
                    return source;
                }
        ));
    }

    private Map<String, PedestrianSink> handlePedestrianSinks(String scopeComponentId, Sinks sinks, Model model) {
        return sinks.getSink().stream().collect(toMap(
                Sink::getId,
                sk -> {
                    final PedestrianSink sink = new PedestrianSink(sk.getId(), model, sk.getId(), false);
                    if (!PEDESTRIAN_SINK_REGISTRY.containsKey(scopeComponentId)) {
                        PEDESTRIAN_SINK_REGISTRY.put(scopeComponentId, new HashMap<>());
                    }

                    PEDESTRIAN_SINK_REGISTRY.get(scopeComponentId).put(sk.getId(), sink);
                    return sink;
                }
        ));
    }

    private Map<String, RoundaboutSink> handleSinks(String scopeComponentId, Sinks sinks, Model model) {
        return sinks.getSink().stream().collect(toMap(
            Sink::getId,
            sk -> {
                final RoundaboutSink sink = new RoundaboutSink(sk.getId(), model, sk.getId(), false);
                if (!SINK_REGISTRY.containsKey(scopeComponentId)) {
                    SINK_REGISTRY.put(scopeComponentId, new HashMap<>());
                }

                SINK_REGISTRY.get(scopeComponentId).put(sk.getId(), sink);
                return sink;
            }
        ));
    }

    private Map<String, StreetSection> handleSections(String scopeComponentId, Sections sections, Model model) {
        final double maxTruckLength = Double.parseDouble(MODEL_PARAMETERS.get(MAX_TRUCK_LENGTH));
        final double maxDistanceFactorBetweenCars = Double.parseDouble(MODEL_PARAMETERS.get(MAX_DISTANCE_FACTOR_BETWEEN_CARS));
        final double minStreetLength = maxTruckLength + maxDistanceFactorBetweenCars * 2;

        return sections.getSection().stream().collect(toMap(
                Section::getId,
                s -> {
                    if(s.getLengthX() < minStreetLength) {
                        throw new IllegalArgumentException(
                             "Street must not be smaller than the biggest vehicle incl. distance to other vehicles"
                        );
                    }

                    final boolean isTrafficLightActive = s.getIsTrafficLightActive() != null ? s.getIsTrafficLightActive() : false;
                    final PedestrianStreet pedestrianStreetSectionRef = s.getPedestrianCrossingIDRef() != null ?
                            resolvePedestrianSection(s.getPedestrianCrossingComponentIDRef(), s.getPedestrianCrossingIDRef()) : null;

                    // a traffic light can currently solely be on a pedestrian crossing section
                    if( isTrafficLightActive && pedestrianStreetSectionRef != null && !(pedestrianStreetSectionRef.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)) ) {
                        throw new IllegalArgumentException("A pedestrian traffic light can currently solely be on a pedestrian crossing section.");
                    }

                    PedestrianStreetReferenceForVehicleStreet pedestrianStreetRefEnter = null;
                    PedestrianStreetReferenceForVehicleStreet pedestrianStreetRefExit = null;

                    if ( pedestrianStreetSectionRef != null) {
                        if(s.getPedestrianCrossingIDRefEnterHigh() != null) {
                            pedestrianStreetRefEnter =
                                    new PedestrianStreetReferenceForVehicleStreet(pedestrianStreetSectionRef,
                                            s.getPedestrianCrossingIDRefEnterHigh(), s.getPedestrianCrossingRefLinkedAtBegin().equals("y") ? true : false);
                        }
                        if(s.getPedestrianCrossingIDRefExitHigh() != null) {
                            pedestrianStreetRefExit =
                                    new PedestrianStreetReferenceForVehicleStreet(pedestrianStreetSectionRef,
                                            s.getPedestrianCrossingIDRefExitHigh(), s.getPedestrianCrossingRefLinkedAtBegin().equals("y") ? true : false);
                        }
                    }

                    final StreetSection streetSection = new StreetSection(
                            s.getId(),
                            s.getLengthX(),
                            model,
                            s.getId(),
                            false,
                            isTrafficLightActive,
                            s.getMinGreenPhaseDuration(),
                            s.getGreenPhaseDuration(),
                            s.getRedPhaseDuration(),
                            pedestrianStreetRefEnter,
                            pedestrianStreetRefExit
                    );

                    if ( (pedestrianStreetSectionRef instanceof PedestrianStreetSection) &&
                            pedestrianStreetSectionRef.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING) &&
                        pedestrianStreetRefEnter != null) {
                        ((PedestrianStreetSection) pedestrianStreetSectionRef).addEnteringVehicleStreetList(streetSection);
                    }

                    if ( (pedestrianStreetSectionRef instanceof PedestrianStreetSection) &&
                            pedestrianStreetSectionRef.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING) &&
                            pedestrianStreetRefExit != null) {
                        ((PedestrianStreetSection) pedestrianStreetSectionRef).addLeavingVehicleStreetList(streetSection);
                    }

                    if (!SECTION_REGISTRY.containsKey(scopeComponentId)) {
                        SECTION_REGISTRY.put(scopeComponentId, new HashMap<>());
                    }

                    SECTION_REGISTRY.get(scopeComponentId).put(s.getId(), streetSection);
                    return streetSection;
                }
        ));
    }

    private Map<String, PedestrianStreetSection> handlePedestrianSections(String scopeComponentId, Sections sections, Model model) {
        return sections.getSection().stream().collect(toMap(
                Section::getId,
                s -> {
                    if(s.getLengthX() < Double.parseDouble(MODEL_PARAMETERS.get(MIN_PEDESTRIAN_STREET_LENGTH))) {
                        throw new IllegalArgumentException(
                              "Street must not be smaller than a pedestrian or the minimum of a pedestrian street."
                        );
                    }

                    if(s.getLengthY() < Double.parseDouble(MODEL_PARAMETERS.get(MIN_PEDESTRIAN_STREET_WIDTH))) {
                        throw new IllegalArgumentException(
                                "Street must not be smaller than a pedestrian or the minimum of a pedestrian street."
                        );
                    }

                    final boolean useMassDynamic = s.getUseMassDynamic() != null ? s.getUseMassDynamic() : false;
                    final boolean isTrafficLightActive = s.getIsTrafficLightActive() != null ? s.getIsTrafficLightActive() : false;

                    final PedestrianStreetSection pedestrianStreetSection = new PedestrianStreetSection(
                            s.getId(),
                            s.getLengthX(),
                            s.getLengthY(),
                            s.getPedestrianSectionType(),
                            model,
                            s.getId(),
                            false,
                            isTrafficLightActive,
                            s.getMinGreenPhaseDuration(),
                            s.getGreenPhaseDuration(),
                            s.getRedPhaseDuration(),
                            s.getMinSizeOfPedestriansForTrafficLightTriggeredByJam(),
                            null,
                            useMassDynamic
                    );

                    if (pedestrianStreetSection.getPedestrianConsumerType().equals(PedestrianConsumerType.PEDESTRIAN_CROSSING)) {
                        pedestrianStreetSection.setFlexiBorderAlongX(s.getFlexiBorderAlongX() != null ? s.getFlexiBorderAlongX() : false);
                    }

                    if (!PEDESTRIAN_SECTION_REGISTRY.containsKey(scopeComponentId)) {
                        PEDESTRIAN_SECTION_REGISTRY.put(scopeComponentId, new HashMap<>());
                    }

                    PEDESTRIAN_SECTION_REGISTRY.get(scopeComponentId).put(s.getId(), pedestrianStreetSection);
                    return pedestrianStreetSection;
                }
        ));
    }

 private Map<String, PedestrianStreetConnector> handlePedestrianConnectors(String scopeComponentId, PedestrianConnectors connectors)
    throws IllegalStateException{
           return connectors.getPedestrianConnector().stream().collect(toMap(
                PedestrianConnector::getId,
                co -> {
                    final List<PedestrianTrack> pedestrianTrackList = PEDESTRIAN_TRACK_EXTRACTOR.apply(co);
                    List<PedestrianConnectedStreetSections> pairs = new LinkedList<>();

                    for(PedestrianTrack track : pedestrianTrackList) {
                        String componentID1 = track.getFromComponentId() != null ? track.getFromComponentId() : scopeComponentId;
                        PedestrianStreetSection pedestrianStreetSectionFrom = (PedestrianStreetSection)resolvePedestrianStreet(componentID1, track.getFromSectionId());
                        PedestrianStreetSectionPort streetSectionPortFrom = new PedestrianStreetSectionPort(
                                track.getFromXPortPositionStart(), track.getFromYPortPositionStart(),
                                track.getFromXPortPositionEnd(), track.getFromYPortPositionEnd());

                        String componentID2 = track.getToComponentId() != null ? track.getToComponentId() : scopeComponentId;
                        PedestrianStreetSection pedestrianStreetSectionTo = null;
                        PedestrianStreetSectionPort streetSectionPortTo = new PedestrianStreetSectionPort(
                                track.getToXPortPositionStart(), track.getToYPortPositionStart(),
                                track.getToXPortPositionEnd(), track.getToYPortPositionEnd());

                        PedestrianConnectedStreetSections connector;
                        if( !track.getToSectionType().equals(PedestrianConsumerType.PEDESTRIAN_SINK) ) {
                            pedestrianStreetSectionTo = (PedestrianStreetSection) resolvePedestrianStreet(componentID2, track.getToSectionId());
                            // from is always current section
                            PedestrianConnectedStreetSections connector2 = new PedestrianConnectedStreetSections(
                                    pedestrianStreetSectionTo, streetSectionPortTo, pedestrianStreetSectionFrom, streetSectionPortFrom);

                            if( track.getFromSectionType().equals(PedestrianConsumerType.PEDESTRIAN_SOURCE) ) {
                                PedestrianSource pedestrianSource = resolvePedestrianSource(componentID1, track.getFromSectionId());
                                connector2.setToSource( pedestrianSource );
                            }

                            pedestrianStreetSectionTo.addPreviousStreetConnector(connector2);

                            connector = new PedestrianConnectedStreetSections(pedestrianStreetSectionFrom, streetSectionPortFrom,
                                    pedestrianStreetSectionTo, streetSectionPortTo);
                        } else {
                            connector = new PedestrianConnectedStreetSections(pedestrianStreetSectionFrom, streetSectionPortFrom,
                                    resolvePedestrianSink(componentID2, track.getToSectionId()), streetSectionPortTo);
                        }

                        if( !track.getFromSectionType().equals(PedestrianConsumerType.PEDESTRIAN_SOURCE) ) {
                            pedestrianStreetSectionFrom.addNextStreetConnector(connector);
                        }

                        if (checkForExistingPedestrianStreetSectionPair(pairs, pedestrianStreetSectionFrom,
                                pedestrianStreetSectionTo)) {
                            throw new IllegalArgumentException("Pedestrian street sections pair does already exist");
                        }

                        pairs.add(connector);
                    }

                    PedestrianStreetConnector streetConnector = new PedestrianStreetConnector(co.getId(), pairs);
                    return streetConnector;
                }
        ));
    }


    private boolean checkForExistingPedestrianStreetSectionPair(List<PedestrianConnectedStreetSections> sectionPair,
                                                                PedestrianStreet fromSection, PedestrianStreet toSection){

        for (PedestrianConnectedStreetSections pair : sectionPair){
            if (    ((fromSection == null && pair.getFromStreetSection() == null) ||
                    ((fromSection != null && pair.getFromStreetSection() != null) && (pair.getFromStreetSection().equals(fromSection)) )) &&

                    ( (toSection == null && pair.getToStreetSection() == null) ||
                    ((toSection != null && pair.getToStreetSection() != null) && pair.getToStreetSection().equals(toSection)))) {
                return  true;
            }
        }
        return false;
    }

    private Map<IConsumer, at.fhv.itm3.s2.roundabout.entity.StreetNeighbour> handleStreetNeighbours(StreetNeighbours streetNeighbours){
        if(SECTION_REGISTRY == null) return null; //no lamda as not a functional interface

        Map<IConsumer , at.fhv.itm3.s2.roundabout.entity.StreetNeighbour> neighMap = new HashMap<>();

        for(StreetNeighbour sn : streetNeighbours.getNeighbourList()){
            String baseComp = sn.getBaseStreetComponent();
            String base = sn.getBaseStreet();

            String neighComp1 = sn.getNeighbouringStreetComponent1();
            String neigh1 = sn.getNeighbouringStreet1();
            String neighComp2 = sn.getNeighbouringStreetComponent2();
            String neigh2 = sn.getNeighbouringStreet2();

            neighMap.put(resolveSection(baseComp,base),
                    new at.fhv.itm3.s2.roundabout.entity.StreetNeighbour(
                            resolveStreet(neighComp1,neigh1), resolveStreet(neighComp2, neigh2)
                    ));
        }
        return neighMap;
    }

    private Map<String, StreetConnector> handleConnectors(String scopeComponentId, Connectors connectors) {
        return connectors.getConnector().stream().collect(toMap(
                Connector::getId,
                co -> {
                    final Collection<IConsumer> previousSections = new LinkedHashSet<>();
                    final Collection<IConsumer> nextSections = new LinkedHashSet<>();

                    final List<Consumer<StreetConnector>> trackInitializers = new LinkedList<>();
                    final List<Track> trackList = SORTED_TRACK_EXTRACTOR.apply(co);
                    for (Track track : trackList) {
                        final String fromComponentId = track.getFromComponentId() != null ? track.getFromComponentId() : scopeComponentId;
                        final Street fromSection = resolveStreet(fromComponentId, track.getFromSectionId());
                        if (!previousSections.contains(fromSection)) previousSections.add(fromSection);

                        final String toComponentId = track.getToComponentId() != null ? track.getToComponentId() : scopeComponentId;
                        final Street toSection = resolveStreet(toComponentId, track.getToSectionId());
                        if (!nextSections.contains(toSection)) nextSections.add(toSection);

                        trackInitializers.add(connector -> connector.initializeTrack(
                                fromSection, track.getFromSectionType(), toSection, track.getToSectionType()
                        ));
                    }

                    final StreetConnector streetConnector = new StreetConnector(co.getId(), previousSections, nextSections);
                    trackInitializers.forEach(streetConnectorConsumer -> streetConnectorConsumer.accept(streetConnector));

                    return streetConnector;
                }
        ));
    }

    private List<IntersectionPhase> handlePhases(
        String intersectionId,
        RoundaboutIntersection intersection,
        Connectors connectors,
        Double controllerYellowDuration
    ) {
        final int numberOfPhases = connectors.getConnector().size();
        final List<IntersectionPhase> phases = new ArrayList<>(numberOfPhases);

        final Map<String, Integer> directionMap = new HashMap<>();

        final Collection<IConsumer> previousSections = new LinkedHashSet<>();
        final Collection<IConsumer> nextSections = new LinkedHashSet<>();

        final IntersectionController intersectionController = IntersectionController.getInstance();

        connectors.getConnector().stream().sorted(Comparator.comparing(Connector::getId)).forEach(connector -> {
            final IntersectionPhase phase = new IntersectionPhase(1.0 / numberOfPhases, controllerYellowDuration);
            connector.getTrack().forEach(track -> {
                // In direction.
                final String fromSectionId = track.getFromSectionId();
                final Street fromSection = resolveStreet(intersectionId, fromSectionId);
                if (!directionMap.containsKey(fromSectionId)) {
                    directionMap.put(fromSectionId, directionMap.size());

                    // Handling section -> intersections connectors, should be done only once per section.
                    // Connection intersection -> section is handled via connection queue.
                    previousSections.add(fromSection);
                    nextSections.add(intersection);
                }
                final int inDirection = directionMap.get(fromSectionId);
                intersectionController.setIntersectionInDirectionMapping(intersection, fromSection, inDirection);

                // Out direction.
                final String toSectionId = track.getToSectionId();
                final Street toSection = resolveStreet(intersectionId, toSectionId);
                if (!directionMap.containsKey(toSectionId)) {
                    directionMap.put(toSectionId, directionMap.size());
                }
                final int outDirection = directionMap.get(toSectionId);
                intersectionController.setIntersectionOutDirectionMapping(intersection, toSection, outDirection);

                // Connection queue.
                if (fromSection == null || toSection == null) {
                    throw new IllegalArgumentException(String.format(
                        "Please check if \"from\" section: \"%s\" and \"to\" section: \"%s\" were declared correctly." +
                        "(Connector id: \"%s\", Track id: \"%s\")",
                        fromSectionId,
                        toSectionId,
                        connector.getId(),
                        track.getOrder()
                    ));
                }

                final AbstractProducer producer = fromSection.toProducer();
                intersection.attachProducer(inDirection, producer);

                final AbstractConsumer consumer = toSection.toConsumer();
                intersection.attachConsumer(outDirection, consumer);

                intersection.createConnectionQueue(
                    producer,
                    new AbstractConsumer[]{consumer},
                    new double[]{0},
                    new double[]{1} // probability should be always 1 in our case?
                );

                phase.addConnection(new IntersectionConnection(inDirection, outDirection));
            });

            phases.add(phase);
        });

        // Connect sections with intersection.
        final StreetConnector streetConnector = new StreetConnector("intersection", previousSections, nextSections);
        previousSections.forEach(fromSection -> streetConnector.initializeTrack(
            fromSection,
            ConsumerType.STREET_SECTION,
            intersection,
            ConsumerType.INTERSECTION
        ));

        return phases;
    }

    private Map<AbstractSource, Map<RoundaboutSink, Route>> handleRoutes(ModelConfig modelConfig) {
        modelConfig.getComponents().getComponent().forEach(component -> {
            // Start generating roots for every vehicle source.
            if(component.getType().equals(ComponentType.INTERSECTION) || component.getType().equals(ComponentType.ROUNDABOUT)) {
                component.getSources().getSource().forEach(sourceDTO -> {
                      final AbstractSource source = SOURCE_REGISTRY.get(component.getId()).get(sourceDTO.getId());
                    final Street connectedStreet = source.getConnectedStreet();

                    final List<IConsumer> route = new LinkedList<>();
                    route.add(connectedStreet);

                    doDepthFirstSearch(source, route, component, modelConfig);
                });
            }
        });
        return ROUTE_REGISTRY;
    }

    private Map<PedestrianAbstractSource, Map<PedestrianSink, PedestrianRoute>> handlePedestrianRoutes(ModelConfig modelConfig) {
        modelConfig.getComponents().getComponent().forEach(component -> {
            // Start generating routs for every source.
            if(component.getType().equals(ComponentType.PEDESTRIANWALKINGAREA)) {
                component.getSources().getSource().forEach(sourceDTO -> {
                    final PedestrianAbstractSource source = PEDESTRIAN_SOURCE_REGISTRY.get(component.getId()).get(sourceDTO.getId());
                    final PedestrianStreetSectionAndPortPair connectedStreet = new PedestrianStreetSectionAndPortPair(source.getConnectedStreet());

                    final List<PedestrianStreetSectionAndPortPair> route = new LinkedList<>();
                    route.add(connectedStreet);
                    // first entry is needed here since sink might be listed before the matching source entry
                    // Note: only one source per street section
                    String firstSectionId = ((PedestrianStreet) route.get(0).getStreetSection()).getId();
                    for (PedestrianConnector connector : component.getPedestrianConnectors().getPedestrianConnector()) {
                        for (PedestrianTrack track : connector.getPedestrianTrack()) {
                            if (track.getFromSectionType().equals(PedestrianConsumerType.PEDESTRIAN_SOURCE) && // source is not listed in route
                                    track.getToSectionId().equals(firstSectionId)) {
                                PedestrianStreetSectionPort enterPort = new PedestrianStreetSectionPort(track.getToXPortPositionStart(),
                                        track.getToYPortPositionStart(), track.getToXPortPositionEnd(), track.getToYPortPositionEnd());
                                route.get(0).setEnterPort(enterPort);
                            }
                        }
                    }
                    doPedestrianDepthFirstSearch(source, route, component, modelConfig);
                });
            }
        });
        return PEDESTRIAN_ROUTE_REGISTRY;
    }

    private PedestrianStreetSection getAStartForGlobalCoordinates(){
        for ( Map.Entry<String, Map<String, PedestrianSource>> sourceRegistry : getPedestrianSourceRegistry().entrySet() ) {
            for ( Map.Entry <String, PedestrianSource> startSource : sourceRegistry.getValue().entrySet() ) {
                if ( startSource.getValue().getConnectedStreet() instanceof  PedestrianStreetSection) {
                    return startSource.getValue().getConnectedStreet();
                }
                else throw new IllegalArgumentException("Street not instance of PedestrianStreetSection.");
            }
        }
        return null;
    }

    private void initGlobalCoordinates(ModelConfig modelConfig){
        if (PEDESTRIAN_SOURCE_REGISTRY.isEmpty()) return;
        // start at any source
        PedestrianStreetSection currentSection = getAStartForGlobalCoordinates();
        currentSection.setGlobalCoordinateOfSectionOrigin(new PedestrianPoint(0,0));
        deepSearchGlobalCoordinates(currentSection);
        initGlobalSource();

        modelConfig.getComponents().getComponent().forEach( component -> {
                if (! component.getType().equals(ComponentType.PEDESTRIANWALKINGAREA)) {
                    for ( Section section : component.getSections().getSection() ) {
                        resolveSection(component.getId(), section.getId()).setGlobalCoordinateOfCrossingIntersection();
                    }
                }
            }
        );
    }

    private void initGlobalSource() {
        PEDESTRIAN_SOURCE_REGISTRY.forEach((componentId, map) -> {
            map.forEach((sourceId, source)->{
                PedestrianStreetSection entrySection = source.getConnectedStreet();
                PedestrianPoint globalCooEntrySection = source.getConnectedStreet().getGlobalCoordinateOfSectionOrigin();
                PedestrianPoint sourceGlobalCooEntrySection = new PedestrianPoint();
                PedestrianConnectedStreetSections connectorPair = entrySection.getPreviousStreetConnectorToSource();
                PedestrianStreetSectionPort port = connectorPair.getPortOfFromStreetSection();

                if( (calc.almostEqual( port.getLocalBeginOfStreetPort().getX(), port.getLocalEndOfStreetPort().getX()) &&
                        (port.getLocalBeginOfStreetPort().getY() < port.getLocalEndOfStreetPort().getY() ))
                 ||
                    (calc.almostEqual( port.getLocalBeginOfStreetPort().getY(), port.getLocalEndOfStreetPort().getY()) &&
                            (port.getLocalBeginOfStreetPort().getX() < port.getLocalEndOfStreetPort().getX() ))
                 ){
                    //start is origin
                    sourceGlobalCooEntrySection.setLocation(
                            globalCooEntrySection.getX() + port.getLocalBeginOfStreetPort().getX(),
                            globalCooEntrySection.getY() + port.getLocalBeginOfStreetPort().getY());
                } else {
                    // end is origin
                    sourceGlobalCooEntrySection.setLocation(
                            globalCooEntrySection.getX() + port.getLocalEndOfStreetPort().getX(),
                            globalCooEntrySection.getY() + port.getLocalEndOfStreetPort().getY());
                }
                source.setGlobalCoordinate(sourceGlobalCooEntrySection);
            });
        });
    }

    private void deepSearchGlobalCoordinates (PedestrianStreetSection currentSection) {
        for ( PedestrianConnectedStreetSections connector: currentSection.getNextStreetConnector()) {
            IConsumer toStreetSection = connector.getToStreetSection();
            if ( toStreetSection instanceof PedestrianSink || toStreetSection instanceof PedestrianSource ) continue;

            if ( !(toStreetSection instanceof PedestrianStreetSection)) {
                throw new IllegalArgumentException("Street not instance of PedestrianStreetSection.");
            }

            if( ((PedestrianStreetSection)toStreetSection).getGlobalCoordinateOfSectionOrigin() == null ) {
                addGlobalCoordinates(   currentSection,
                                        connector.getPortOfFromStreetSection().getLocalBeginOfStreetPort(),
                                        (PedestrianStreetSection)toStreetSection,
                                        connector.getPortOfToStreetSection().getLocalBeginOfStreetPort());

                deepSearchGlobalCoordinates( (PedestrianStreetSection)toStreetSection );
            }
        }
    }

    private void addGlobalCoordinates( PedestrianStreet previousSection,
                                       PedestrianPoint localExitPort,
                                       PedestrianStreet currentSection,
                                       PedestrianPoint localEntryPort){
    // Setup Global Network Coordinates for Pedestrian Street Sections
    // global exit point - local entry point = global origin of entry street section
        if (!(currentSection instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException("Street not instance of PedestrianStreetSection.");
        }

        PedestrianPoint globalOrigin = ((PedestrianStreetSection)previousSection).getGlobalCoordinateOfSectionOrigin();
        PedestrianPoint globalExitPoint = new PedestrianPoint( globalOrigin.getX()+ localExitPort.getX(),
                                            globalOrigin.getY()+ localExitPort.getY());

        if (!(previousSection instanceof PedestrianStreetSection)) {
            throw new IllegalArgumentException("Street not instance of PedestrianStreetSection.");
        }
        PedestrianPoint globalOriginCurrent = new PedestrianPoint(  globalExitPoint.getX() - localEntryPort.getX(),
                                                globalExitPoint.getY() - localEntryPort.getY());
        ((PedestrianStreetSection)currentSection).setGlobalCoordinateOfSectionOrigin(globalOriginCurrent);
    }

    private Street resolveStreet(String componentId, String streetId) {
        final Street resolvedSection = resolveSection(componentId, streetId);
        return resolvedSection != null ? resolvedSection : resolveSink(componentId, streetId);
    }

    private PedestrianStreet resolvePedestrianStreet(String componentId, String streetId) {
        final PedestrianStreet resolvedSection = resolvePedestrianSection(componentId, streetId);
        return resolvedSection != null ? resolvedSection : resolvePedestrianSink(componentId, streetId);
    }

    private StreetSection resolveSection(String componentId, String sectionId) {
        return SECTION_REGISTRY.containsKey(componentId) ? SECTION_REGISTRY.get(componentId).get(sectionId) : null;
    }

    private PedestrianStreetSection resolvePedestrianSection(String componentId, String sectionId) {
        return PEDESTRIAN_SECTION_REGISTRY.containsKey(componentId) ? PEDESTRIAN_SECTION_REGISTRY.get(componentId).get(sectionId) : null;
    }

    private RoundaboutSink resolveSink(String componentId, String sinkId) {
        return SINK_REGISTRY.containsKey(componentId) ? SINK_REGISTRY.get(componentId).get(sinkId) : null;
    }

    private PedestrianSink resolvePedestrianSink(String componentId, String sinkId) {
        return PEDESTRIAN_SINK_REGISTRY.containsKey(componentId) ? PEDESTRIAN_SINK_REGISTRY.get(componentId).get(sinkId) : null;
    }

    private PedestrianSource resolvePedestrianSource(String componentId, String sinkId) {
        return PEDESTRIAN_SOURCE_REGISTRY.containsKey(componentId) ? PEDESTRIAN_SOURCE_REGISTRY.get(componentId).get(sinkId) : null;
    }

    private <K, V, R> R extractParameter(Function<K, V> supplier, Function<V, R> converter, K key)
    throws NullPointerException {
        return converter.apply(supplier.apply(key));
    }

    private Double calculateMedian(List<Double> numbers) {
        int indexLeft = (int) Math.floor((numbers.size() - 1) / 2);
        int indexRight = (int) Math.ceil((numbers.size() - 1) / 2);
        if (indexLeft == indexRight) {
            return numbers.get(indexLeft);
        }
        return (numbers.get(indexLeft) + numbers.get(indexRight)) / 2;
    }

    private void doDepthFirstSearch(
        AbstractSource source,
        List<IConsumer> routeSections,
        Component component,
        ModelConfig modelConfig
    ) {

        if(component.getType() == ComponentType.PEDESTRIANWALKINGAREA) {return;}

        final IConsumer lastConsumer = routeSections.get(routeSections.size() - 1);
        if (!(lastConsumer instanceof Street)) {
            throw new IllegalArgumentException("Only instances of Street class may be included in root.");
        }
        final String currentSectionId = ((Street) lastConsumer).getId();

        // Check each connector.
        for (Connector connector : component.getConnectors().getConnector()) {
            for (Track track : connector.getTrack()) {
                if (track.getFromSectionId().equals(currentSectionId)) {
                    final String toComponentId = track.getToComponentId() != null ? track.getToComponentId() : component.getId();
                    final String toSectionId = track.getToSectionId();
                    final Street toSection = resolveStreet(toComponentId, toSectionId);

                    if (!routeSections.contains(toSection)) {
                        final List<IConsumer> newRouteSections = new LinkedList<>(routeSections);
                        if (component.getType() == ComponentType.INTERSECTION) {
                            newRouteSections.add((IConsumer) INTERSECTION_REGISTRY.get(component.getId()));
                        }
                        newRouteSections.add(toSection);

                        if (toSection instanceof RoundaboutSink) {
                            // Sink is reached -> end of path.
                            final RoundaboutSink sink = (RoundaboutSink) toSection;
                            if (!ROUTE_REGISTRY.containsKey(source)) {
                                ROUTE_REGISTRY.put(source, new HashMap<>());
                            }

                            final Map<RoundaboutSink, Route> targetRoutes = ROUTE_REGISTRY.get(source);
                            if (!targetRoutes.containsKey(sink) || targetRoutes.get(sink).getNumberOfSections() > newRouteSections.size()) {
                                double flowRatio = DEFAULT_ROUTE_RATIO;
                                if (modelConfig.getComponents().getRoutes() != null) {
                                    for (at.fhv.itm3.s2.roundabout.util.dto.Route route : modelConfig.getComponents().getRoutes().getRoute()) {
                                        final AbstractSource routeSource = SOURCE_REGISTRY.get(route.getFromComponentId()).get(route.getFromSourceId());
                                        final RoundaboutSink routeSink = SINK_REGISTRY.get(route.getToComponentId()).get(route.getToSinkId());

                                        if (source.equals(routeSource) && sink.equals(routeSink)) {
                                            flowRatio = route.getRatio();
                                            break;
                                        }
                                    }
                                }
                                targetRoutes.put(sink, new Route(source, newRouteSections, flowRatio));
                            }
                        } else {
                            doDepthFirstSearch(source, newRouteSections, component, modelConfig);
                        }
                    }
                }
            }
        }

        if (modelConfig.getComponents().getConnectors() !=  null) {
            // Check the connectors between networks components (Roundabouts or Intersections)
            for (Connector connector : modelConfig.getComponents().getConnectors().getConnector()) {
                for (Track track : connector.getTrack()) {
                    final String fromComponentId = track.getFromComponentId();
                    final String fromSectionId = track.getFromSectionId();

                    if (fromComponentId.equals(component.getId()) && fromSectionId.equals(currentSectionId)) {
                        for (Component localComponent : modelConfig.getComponents().getComponent()) {
                            final String toComponentId = track.getToComponentId();
                            final String toSectionId = track.getToSectionId();

                            if (toComponentId.equals(localComponent.getId())) {
                                final Street toSection = resolveSection(toComponentId, toSectionId);

                                final List<IConsumer> newRouteSections = new LinkedList<>(routeSections);
                                newRouteSections.add(toSection);

                                doDepthFirstSearch(source, newRouteSections, localComponent, modelConfig);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean pedestrianStreetSectionAndPortPairListContain(List<PedestrianStreetSectionAndPortPair> list, PedestrianStreet section){
        for ( PedestrianStreetSectionAndPortPair PortPair : list) {
            if (PortPair.getStreetSection().equals(section)) return true;
        }
        return false;
    }

    private void doPedestrianDepthFirstSearch(
            PedestrianAbstractSource source,
            List<PedestrianStreetSectionAndPortPair> routeSections,
            Component component,
            ModelConfig modelConfig
    ) {
        if(component.getType() != ComponentType.PEDESTRIANWALKINGAREA) return;
        IConsumer lastConsumer = routeSections.get(routeSections.size() - 1).getStreetSection();
        if (!(lastConsumer instanceof PedestrianStreet)) {
            throw new IllegalArgumentException("Only instances of PedestrianStreet class may be included in root.");
        }
        String currentSectionId = ((PedestrianStreet) lastConsumer).getId();

        // Check each connector.
        for (PedestrianConnector connector : component.getPedestrianConnectors().getPedestrianConnector()) {
            for (PedestrianTrack track : connector.getPedestrianTrack()) {
                currentSectionId = ((PedestrianStreet) routeSections.get(routeSections.size() - 1).getStreetSection()).getId();
                if (track.getFromSectionId().equals(currentSectionId)) {
                    final String toComponentId = track.getToComponentId() != null ? track.getToComponentId() : component.getId();
                    final String toSectionId = track.getToSectionId();
                    final PedestrianStreet toSection = resolvePedestrianStreet(toComponentId, toSectionId);


                    if (!pedestrianStreetSectionAndPortPairListContain(routeSections, toSection)) {
                        PedestrianStreetSectionPort enterPortToSection = new PedestrianStreetSectionPort(track.getToXPortPositionStart(),
                                track.getToYPortPositionStart(), track.getToXPortPositionEnd(), track.getToYPortPositionEnd());
                        PedestrianStreetSectionAndPortPair routInfo = new PedestrianStreetSectionAndPortPair(resolvePedestrianStreet(toComponentId, toSectionId), enterPortToSection);

                        List<PedestrianStreetSectionAndPortPair> newRouteSections = new LinkedList<>();
                        for ( PedestrianStreetSectionAndPortPair pair : routeSections ) {
                            try {
                                //Deep Copy
                                newRouteSections.add(new PedestrianStreetSectionAndPortPair(pair.getStreetSection()));
                                newRouteSections.get(newRouteSections.size()-1).setEnterPort(pair.getEnterPort() != null ? pair.getEnterPort().deepCopy() : null);
                                newRouteSections.get(newRouteSections.size()-1).setExitPort(pair.getExitPort() != null ? pair.getExitPort().deepCopy() : null);
                            } catch ( Exception e) {
                                throw new IllegalStateException("Deep Copy not possible.");
                            }
                        }
                        PedestrianStreetSectionPort exitPortFromSection = new PedestrianStreetSectionPort(track.getFromXPortPositionStart(),
                                track.getFromYPortPositionStart(), track.getFromXPortPositionEnd(), track.getFromYPortPositionEnd());
                        newRouteSections.get(newRouteSections.size() - 1).setExitPort(exitPortFromSection);
                        newRouteSections.add(routInfo);

                        if (toSection instanceof PedestrianSink) {
                            // Sink is reached -> end of path.
                            final PedestrianSink sink = (PedestrianSink) toSection;
                            if (!PEDESTRIAN_ROUTE_REGISTRY.containsKey(source)) {
                                PEDESTRIAN_ROUTE_REGISTRY.put(source, new HashMap<>());
                            }

                            final Map<PedestrianSink, PedestrianRoute> targetRoutes = PEDESTRIAN_ROUTE_REGISTRY.get(source);
                            if (!targetRoutes.containsKey(sink) || targetRoutes.get(sink).getNumberOfSections() > newRouteSections.size()) {
                                double flowRatio = DEFAULT_ROUTE_RATIO;
                                if (modelConfig.getComponents().getPedestrianRoutes() != null) {
                                    for (at.fhv.itm3.s2.roundabout.util.dto.PedestrianRoute route : modelConfig.getComponents().getPedestrianRoutes().getPedestrianRoute()) {
                                        final PedestrianAbstractSource routeSource = PEDESTRIAN_SOURCE_REGISTRY.get(route.getFromComponentId()).get(route.getFromSourceId());
                                        final PedestrianSink routeSink = PEDESTRIAN_SINK_REGISTRY.get(route.getToComponentId()).get(route.getToSinkId());

                                        if (source.equals(routeSource) && sink.equals(routeSink)) {
                                            flowRatio = route.getRatio();
                                            break;
                                        }
                                    }
                                }
                                targetRoutes.put(sink, new PedestrianRoute(source, newRouteSections, flowRatio));
                            }
                        } else {
                            doPedestrianDepthFirstSearch(source, newRouteSections, component, modelConfig);

                        }
                    }
                }
            }
        }

        if (modelConfig.getComponents().getPedestrianConnectors() !=  null) {
            // Check the connectors between networks components (Roundabouts or Intersections)
            for (PedestrianConnector connector : modelConfig.getComponents().getPedestrianConnectors().getPedestrianConnector()) {
                for (PedestrianTrack track : connector.getPedestrianTrack()) {
                    final String fromComponentId = track.getFromComponentId();
                    final String fromSectionId = track.getFromSectionId();

                    if (fromComponentId.equals(component.getId()) && fromSectionId.equals(currentSectionId)) {

                        for (Component localComponent : modelConfig.getComponents().getComponent()) {
                            final String toComponentId = track.getToComponentId();
                            final String toSectionId = track.getToSectionId();

                            if (toComponentId.equals(localComponent.getId())) {
                                PedestrianStreet toSection = resolvePedestrianSection(toComponentId, toSectionId);

                                PedestrianStreetSectionPort enterPortToSection = new PedestrianStreetSectionPort(track.getToXPortPositionStart(),
                                        track.getToYPortPositionStart(), track.getToXPortPositionEnd(), track.getToYPortPositionEnd());

                                PedestrianStreetSectionAndPortPair routInfo = new PedestrianStreetSectionAndPortPair(resolvePedestrianStreet(toComponentId,toSectionId), enterPortToSection);

                                List<PedestrianStreetSectionAndPortPair> newRouteSections = new LinkedList<>(routeSections);
                                PedestrianStreetSectionPort exitPortToSection = new PedestrianStreetSectionPort(track.getFromXPortPositionStart(),
                                        track.getFromYPortPositionStart(), track.getFromXPortPositionEnd(), track.getFromYPortPositionEnd());
                                routeSections.get(routeSections.size() - 1).setExitPort(exitPortToSection);
                                newRouteSections.add(routInfo);

                                doPedestrianDepthFirstSearch(source, newRouteSections, localComponent, modelConfig);
                            }
                        }
                    }
                }
            }
        }
    }
}
