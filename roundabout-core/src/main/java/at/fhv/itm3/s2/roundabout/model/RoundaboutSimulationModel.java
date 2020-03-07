package at.fhv.itm3.s2.roundabout.model;

import at.fhv.itm14.trafsim.model.ModelFactory;
import at.fhv.itm14.trafsim.model.entities.OneWayStreet;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import desmoj.core.dist.ContDist;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;

import java.util.concurrent.TimeUnit;

public class RoundaboutSimulationModel extends Model {

    private static final long DEFAULT_SIMULATION_SEED = 1L;


    // Vehicle
    private static final Double DEFAULT_MIN_TIME_BETWEEN_CAR_ARRIVALS = 3.5;
    private static final Double DEFAULT_MAX_TIME_BETWEEN_CAR_ARRIVALS = 10.0;
    private static final Double DEFAULT_MIN_DISTANCE_FACTOR_BETWEEN_CARS = 0.0;
    private static final Double DEFAULT_MAX_DISTANCE_FACTOR_BETWEEN_CARS = 1.0;
    private static final Double DEFAULT_MAIN_ARRIVAL_RATE_FOR_ONE_WAY_STREETS = 1.0;
    private static final Double DEFAULT_STANDARD_CAR_ACCELERATION_TIME = 2.0;
    private static final Double DEFAULT_MIN_CAR_LENGTH = 3.0;
    private static final Double DEFAULT_MAX_CAR_LENGTH = 19.5;
    private static final Double DEFAULT_EXPECTED_CAR_LENGTH = 4.5;
    private static final Double DEFAULT_MIN_TRUCK_LENGTH = 3.0;
    private static final Double DEFAULT_MAX_TRUCK_LENGTH = 19.5;
    private static final Double DEFAULT_EXPECTED_TRUCK_LENGTH = 4.5;
    private static final Double DEFAULT_CAR_RATIO_PER_TOTAL_VEHICLE = 0.8;
    private static final Double DEFAULT_JAM_INDICATOR_IN_SECONDS = 5.0;

    private static final Double VEHICLE_LENGTH_STEP_SIZE = 0.1;

    private static final Double DEFAULT_MIN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS = 3.5;
    private static final Double DEFAULT_MAX_TIME_BETWEEN_PEDESTRIAN_ARRIVALS = 10.0;
    private static final Double DEFAULT_MEAN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS = 6.0;
    private static final Double DEFAULT_MIN_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN = 3.5;
    private static final Double DEFAULT_MAX_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN = 10.0;
    private static final Long DEFAULT_MIN_PEDESTRIAN_GROUP_SIZE = 2L;
    private static final Long DEFAULT_MAX_PEDESTRIAN_GROUP_SIZE = 30L;
    private static final Double DEFAULT_MIN_PEDESTRIAN_STREET_LENGTH = 10.0;
    private static final Double DEFAULT_MIN_PEDESTRIAN_STREET_WIDTH = 10.0;
    private static final Double DEFAULT_SFM_DEGREE_OF_ACCURACY = 10e-8;

    private static final Double DEFAULT_MIN_PEDESTRIAN_RELAXING_TIME = 2.2-0.5;
    private static final Double DEFAULT_MAX_PEDESTRIAN_RELAXING_TIME = 2.2+0.5;
    private static final Double DEFAULT_EXPECTED_PEDESTRIAN_RELAXING_TIME = 2.2;
    private static final Double DEFAULT_MIN_PEDESTRIAN_PREFERRED_SPEED = 1.34-0.26;
    private static final Double DEFAULT_MAX_PEDESTRIAN_PREFERRED_SPEED = 1.34+0.26;
    private static final Double DEFAULT_EXPECTED_PEDESTRIAN_PREFERRED_SPEED = 1.34;


    private final Long simulationSeed;

    private final Double minDistanceFactorBetweenCars;
    private final Double maxDistanceFactorBetweenCars;
    private final Double minTimeBetweenCarArrivals;
    private final Double maxTimeBetweenCarArrivals;
    private final Double meanTimeBetweenCarArrivals;
    private final Double mainArrivalRateForOneWayStreets;
    private final Double standardCarAccelerationTime;
    private final Double minCarLength;
    private final Double maxCarLength;
    private final Double expectedCarLength;
    private final Double minTruckLength;
    private final Double maxTruckLength;
    private final Double expectedTruckLength;
    private final Double carRatioPerTotalVehicle;
    private final Double jamIndicatorInSeconds;

    private final Double minDistanceFactorBetweenPedestrians;
    private final Double maxDistanceFactorBetweenPedestrians;
    private final Double minTimeBetweenPedestrianArrivals;
    private final Double maxTimeBetweenPedestrianArrivals;
    private final Double meanTimeBetweenPedestrianArrivals;
    private final Long minPedestrianGroupeSize;
    private final Long maxPedestrianGroupeSize;
    private final Double minPedestrianStreetLength;
    private final Double minPedestrianStreetWidth;
    private final Double SFM_DegreeOfAccuracy;
    private final Double minPedestrianRelaxingTimeTauAlpha;
    private final Double maxPedestrianRelaxingTimeTauAlpha;
    private final Double expectedPedestrianRelaxingTimeTauAlpha;
    private final Double minPedestrianPreferredSpeed;
    private final Double maxPedestrianPreferredSpeed;
    private final Double expectedPedestrianPreferredSpeed;
    public final Double pedestrianFieldOfViewRadius = 800.0; //cm
    public final Double pedestrianFieldOfViewDegree = 170.0; // Degree
    public final Double getPedestrianFieldOfViewWeakeningFactor = 0.1; // Value between 0 and 1

    //Simulation
    private IModelStructure modelStructure;

    /**
     * Random number stream used to calculate a random route ratio.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform randomRouteRatioFactor;

    /**
     * Random number stream used to calculate a distance between two cars.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform distanceFactorBetweenCars;

    /**
     * Random number stream used to calculate a length of a car.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal lengthOfCar;

    /**
     * Random number stream used to calculate a length of a truck.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal lengthOfTruck;

    /**
     * Random number stream used to calculate a length of a vehicle.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform typeOfVehicle;

    /**
     * Random number stream used to calculate a time between car arrivals on one {@link OneWayStreet}.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDist timeBetweenCarArrivalsOnOneWayStreets;

    /**
     * Random number stream used to draw a time between two car arrivals.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal timeBetweenCarArrivals;

    /**
     * Random number stream used to draw a time between two pedestrians arrivals.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal timeBetweenPedestrianArrivals;

    /**
     * Random number stream used to calculate a distance between two pedestrians.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform distanceFactorBetweenPedestrians;

    /////////////////////////////////////////////////////////////////////////////////
    ////Social Force Model Calculations:

    /**
     * Random number stream used to draw a time between two car arrivals.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal pedestrianRelaxingTimeTauAlpha;

    /**
     * Random number stream used to draw a time between two car arrivals.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal pedestrianPreferredSpeed;

    /**
     * Random number stream used to define gender
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal pedestrianGender;

    /**
     * Random number stream used to define age group
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal pedestrianAgeRangeGroup;

    /**
     * Random number stream used to define psychological nature
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal pedestrianPsychologicalNature;

    /**
     * Random number stream used to define entry point
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform pedestrianEntryPoint;



    /**
     * Constructs a new RoundaboutSimulationModel
     *
     * @param model the model this model is part of (set to null when there is no such model)
     * @param name this model's name
     * @param showInReport flag to indicate if this model shall produce output to the report file
     * @param showInTrace flag to indicate if this model shall produce output to the trace file
     */
    public RoundaboutSimulationModel(
        Model model,
        String name,
        boolean showInReport,
        boolean showInTrace
    ) {
        this(
            model, name, showInReport, showInTrace,
            DEFAULT_MIN_TIME_BETWEEN_CAR_ARRIVALS, DEFAULT_MAX_TIME_BETWEEN_CAR_ARRIVALS
        );
    }

    /**
     * Constructs a new RoundaboutSimulationModel
     *
     * @param model the model this model is part of (set to null when there is no such model)
     * @param name this model's name
     * @param showInReport flag to indicate if this model shall produce output to the report file
     * @param showInTrace flag to indicate if this model shall produce output to the trace file
     */
    public RoundaboutSimulationModel(
        Model model,
        String name,
        boolean showInReport,
        boolean showInTrace,
        double minTimeBetweenCarArrivals,
        double maxTimeBetweenCarArrivals
    ) {
        this(
            DEFAULT_SIMULATION_SEED, model, name, showInReport, showInTrace,
            minTimeBetweenCarArrivals, maxTimeBetweenCarArrivals,
            DEFAULT_MIN_DISTANCE_FACTOR_BETWEEN_CARS, DEFAULT_MAX_DISTANCE_FACTOR_BETWEEN_CARS,
            DEFAULT_MAIN_ARRIVAL_RATE_FOR_ONE_WAY_STREETS,
            DEFAULT_STANDARD_CAR_ACCELERATION_TIME,
            DEFAULT_MIN_CAR_LENGTH, DEFAULT_MAX_CAR_LENGTH, DEFAULT_EXPECTED_CAR_LENGTH,
            DEFAULT_MIN_TRUCK_LENGTH, DEFAULT_MAX_TRUCK_LENGTH, DEFAULT_EXPECTED_TRUCK_LENGTH,
            DEFAULT_CAR_RATIO_PER_TOTAL_VEHICLE,
            DEFAULT_JAM_INDICATOR_IN_SECONDS
            );
    }

    /**
     * Constructs a new RoundaboutSimulationModel
     *
     * @param simulationSeed simulation seed.
     * @param model the model this model is part of (set to null when there is no such model)
     * @param name this model's name
     * @param showInReport flag to indicate if this model shall produce output to the report file
     * @param showInTrace flag to indicate if this model shall produce output to the trace file
     */
    public RoundaboutSimulationModel(
            Long simulationSeed,
            Model model,
            String name,
            boolean showInReport,
            boolean showInTrace,
            Double minTimeBetweenCarArrivals,
            Double maxTimeBetweenCarArrivals,
            Double minDistanceFactorBetweenCars,
            Double maxDistanceFactorBetweenCars,
            Double mainArrivalRateForOneWayStreets,
            Double standardCarAccelerationTime,
            Double minCarLength,
            Double maxCarLength,
            Double expectedCarLength,
            Double minTruckLength,
            Double maxTruckLength,
            Double expectedTruckLength,
            Double carRatioPerTotalVehicle,
            Double jamIndicatorInSeconds
    ) {
        this(
            simulationSeed, model, name, showInReport, showInTrace, minTimeBetweenCarArrivals,
            maxTimeBetweenCarArrivals, minDistanceFactorBetweenCars, maxDistanceFactorBetweenCars,
            mainArrivalRateForOneWayStreets, standardCarAccelerationTime,
            minCarLength, maxCarLength, expectedCarLength, minTruckLength, maxTruckLength,
            expectedTruckLength, carRatioPerTotalVehicle, jamIndicatorInSeconds,

            DEFAULT_MIN_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN,
            DEFAULT_MAX_DISTANCE_FACTOR_BETWEEN_PEDESTRIAN,
            DEFAULT_MIN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS,
            DEFAULT_MAX_TIME_BETWEEN_PEDESTRIAN_ARRIVALS,
            DEFAULT_MEAN_TIME_BETWEEN_PEDESTRIAN_ARRIVALS,

            DEFAULT_MIN_PEDESTRIAN_GROUP_SIZE, DEFAULT_MAX_PEDESTRIAN_GROUP_SIZE,
            DEFAULT_MIN_PEDESTRIAN_STREET_LENGTH, DEFAULT_MIN_PEDESTRIAN_STREET_WIDTH,
            DEFAULT_SFM_DEGREE_OF_ACCURACY,
            DEFAULT_MIN_PEDESTRIAN_RELAXING_TIME, DEFAULT_MAX_PEDESTRIAN_RELAXING_TIME, DEFAULT_EXPECTED_PEDESTRIAN_RELAXING_TIME,
            DEFAULT_MIN_PEDESTRIAN_PREFERRED_SPEED, DEFAULT_MAX_PEDESTRIAN_PREFERRED_SPEED, DEFAULT_EXPECTED_PEDESTRIAN_PREFERRED_SPEED
        );
    }

    /**
     * Constructs a new RoundaboutSimulationModel
     *
     * @param simulationSeed simulation seed.
     * @param model the model this model is part of (set to null when there is no such model)
     * @param name this model's name
     * @param showInReport flag to indicate if this model shall produce output to the report file
     * @param showInTrace flag to indicate if this model shall produce output to the trace file
     */
    public RoundaboutSimulationModel(
        Long simulationSeed,
        Model model,
        String name,
        boolean showInReport,
        boolean showInTrace,
        Double minTimeBetweenCarArrivals,
        Double maxTimeBetweenCarArrivals,
        Double minDistanceFactorBetweenCars,
        Double maxDistanceFactorBetweenCars,
        Double mainArrivalRateForOneWayStreets,
        Double standardCarAccelerationTime,
        Double minCarLength,
        Double maxCarLength,
        Double expectedCarLength,
        Double minTruckLength,
        Double maxTruckLength,
        Double expectedTruckLength,
        Double carRatioPerTotalVehicle,
        Double jamIndicatorInSeconds,

        Double minDistanceFactorBetweenPedestrians,
        Double maxDistanceFactorBetweenPedestrians,
        Double minTimeBetweenPedestrianArrivals,
        Double maxTimeBetweenPedestrianArrivals,
        Double meanTimeBetweenPedestrianArrivals,

        Long minPedestrianGroupSize,
        Long maxPedestrianGroupSize,
        Double minPedestrianStreetLength,
        Double minPedestrianStreetWidth,
        Double SFM_DegreeOfAccuracy,

        Double minPedestrianRelaxingTimeTauAlpha,
        Double maxPedestrianRelaxingTimeTauAlpha,
        Double expectedPedestrianRelaxingTimeTauAlpha,
        Double minPedestrianPreferredSpeed,
        Double maxPedestrianPreferredSpeed,
        Double expectedPedestrianPreferredSpeed
        ) {
        super(model, name, showInReport, showInTrace);

        this.simulationSeed = simulationSeed;
        this.minTimeBetweenCarArrivals = minTimeBetweenCarArrivals;
        this.maxTimeBetweenCarArrivals = maxTimeBetweenCarArrivals;
        this.meanTimeBetweenCarArrivals = (minTimeBetweenCarArrivals + maxTimeBetweenCarArrivals) / 2;
        this.minDistanceFactorBetweenCars = minDistanceFactorBetweenCars;
        this.maxDistanceFactorBetweenCars = maxDistanceFactorBetweenCars;
        this.mainArrivalRateForOneWayStreets = mainArrivalRateForOneWayStreets;
        this.standardCarAccelerationTime = standardCarAccelerationTime;
        this.minCarLength = minCarLength;
        this.maxCarLength = maxCarLength;
        this.expectedCarLength = expectedCarLength;
        this.minTruckLength = minTruckLength;
        this.maxTruckLength = maxTruckLength;
        this.expectedTruckLength = expectedTruckLength;
        this.carRatioPerTotalVehicle = carRatioPerTotalVehicle;
        this.jamIndicatorInSeconds = jamIndicatorInSeconds;

        this.minDistanceFactorBetweenPedestrians = minDistanceFactorBetweenPedestrians;
        this.maxDistanceFactorBetweenPedestrians = maxDistanceFactorBetweenPedestrians;
        this.minTimeBetweenPedestrianArrivals = minTimeBetweenPedestrianArrivals;
        this.maxTimeBetweenPedestrianArrivals = maxTimeBetweenPedestrianArrivals;
        this. meanTimeBetweenPedestrianArrivals = meanTimeBetweenPedestrianArrivals;

        this.minPedestrianGroupeSize = minPedestrianGroupSize;
        this.maxPedestrianGroupeSize = maxPedestrianGroupSize;
        this.minPedestrianStreetLength = minPedestrianStreetLength;
        this.minPedestrianStreetWidth = minPedestrianStreetWidth;
        this.SFM_DegreeOfAccuracy = SFM_DegreeOfAccuracy;

        this.minPedestrianRelaxingTimeTauAlpha = minPedestrianRelaxingTimeTauAlpha;
        this.maxPedestrianRelaxingTimeTauAlpha = maxPedestrianRelaxingTimeTauAlpha;
        this.expectedPedestrianRelaxingTimeTauAlpha = expectedPedestrianRelaxingTimeTauAlpha;
        this.minPedestrianPreferredSpeed = minPedestrianPreferredSpeed;
        this.maxPedestrianPreferredSpeed = maxPedestrianPreferredSpeed;
        this.expectedPedestrianPreferredSpeed = expectedPedestrianPreferredSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "Roundabout simulation model";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInitialSchedules() {
        if (modelStructure != null) {
            modelStructure.getIntersections().forEach(is -> is.getController().start());
            modelStructure.getRoutes().keySet().forEach(so -> so.startGeneratingCars(0));
            modelStructure.getStreets().forEach(Street::initTrafficLight);

            modelStructure.getPedestrianRoutes().keySet().forEach(so -> so.startGeneratingPedestrians(0));
            modelStructure.getPedestrianStreets().forEach(PedestrianStreet::initTrafficLight);
        } else {
            throw new IllegalArgumentException("Model structure should not be null!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        getExperiment().setSeedGenerator(simulationSeed);

        randomRouteRatioFactor = new ContDistUniform(
            this,
            "RandomRouteRatioFactor",
            0,
            1,
            true,
            false
        );
        randomRouteRatioFactor.setSeed(simulationSeed);

        distanceFactorBetweenCars = new ContDistUniform(
            this,
            "DistanceFactorBetweenCarsStream",
            minDistanceFactorBetweenCars,
            maxDistanceFactorBetweenCars,
            true,
            false
        );
        distanceFactorBetweenCars.setSeed(simulationSeed);

        timeBetweenCarArrivals = new ContDistNormal(
            this,
            "TimeBetweenCarArrivalsStream",
                getMeanTimeBetweenCarArrivals(),
                getStdDeviationTimeBetweenCarArrivals(),
            true,
            false
        );
        timeBetweenCarArrivals.setSeed(simulationSeed);


        timeBetweenPedestrianArrivals = new ContDistNormal(
                this,
                "TimeBetweenPedestrianArrivalsStream",
                getMeanTimeBetweenPedestrianArrivals(),
                getStdDeviationTimeBetweenPedestrianArrivals(),
                true,
                false
        );
        timeBetweenPedestrianArrivals.setSeed(simulationSeed);

        // calculate the standard deviation (of skew normal distribution) for car length
        final StandardDeviation carLengthDeviation = StandardDeviation.calculate(
            minCarLength, maxCarLength, expectedCarLength, VEHICLE_LENGTH_STEP_SIZE
        );
        lengthOfCar = new ContDistNormal(
            this,
            "LengthOfCar",
            expectedCarLength,
            carLengthDeviation.getLeft(),
            carLengthDeviation.getRight(),
            true,
            false
        );
        lengthOfCar.setSeed(simulationSeed);

        // calculate the standard deviation (of skew normal distribution) for truck length
        final StandardDeviation truckLengthDeviation = StandardDeviation.calculate(
            minTruckLength, maxTruckLength, expectedTruckLength, VEHICLE_LENGTH_STEP_SIZE
        );
        lengthOfTruck = new ContDistNormal(
            this,
            "LengthOfTruck",
            expectedTruckLength,
            truckLengthDeviation.getLeft(),
            truckLengthDeviation.getRight(),
            true,
            false
        );
        lengthOfTruck.setSeed(simulationSeed);

        if (carRatioPerTotalVehicle > 1.0) {
            throw new IllegalArgumentException("carRatioPerTotalVehicle must smaller or equals 1.");
        }
        typeOfVehicle = new ContDistUniform(
            this,
            "LengthOfVehicle",
            0.0,
            1.0,
            true,
            false
        );
        typeOfVehicle.setSeed(simulationSeed);

        if (mainArrivalRateForOneWayStreets != null) {
            timeBetweenCarArrivalsOnOneWayStreets = ModelFactory.getInstance(this).createContDistConstant(mainArrivalRateForOneWayStreets);
            timeBetweenCarArrivalsOnOneWayStreets.setSeed(simulationSeed);
        }


        //////////////////////////////////////////////////////////////////
        //Social Force Model Calculations:

        // calculate the standard deviation (of skew normal distribution) for relaxing Time for walking pedestrian
        final StandardDeviation relaxingTimeTauAlphaDeviation = StandardDeviation.calculate(
                2.2-0.5, 2.2+0.5, 2.2, 0.1
        );

        pedestrianRelaxingTimeTauAlpha = new ContDistNormal(
                this,
                "pedestrianRelaxingTimeTauAlpha",
                2.2,
                relaxingTimeTauAlphaDeviation.getLeft(),
                relaxingTimeTauAlphaDeviation.getRight(),
                true,
                false
        );
        pedestrianRelaxingTimeTauAlpha.setSeed(simulationSeed);


        // calculate the standard deviation (of skew normal distribution) for the preferred Speed of Pedestrians (m/s)
        final StandardDeviation preferredSpeedDeviation = StandardDeviation.calculate(
                1.34-0.26, 1.34+0.26, 1.34, 0.01
        );

        pedestrianPreferredSpeed = new ContDistNormal(
                this,
                "pedestrianPreferredSpeed",
                1.34,
                preferredSpeedDeviation.getLeft(),
                preferredSpeedDeviation.getRight(),
                true,
                false
        );
        pedestrianRelaxingTimeTauAlpha.setSeed(simulationSeed);

        // calculate the standard deviation (of normal distribution) for determine gender
        final StandardDeviation genderDeviation = StandardDeviation.calculate(
                0, 100, 31.9, 1
        );

        pedestrianGender = new ContDistNormal(
                this,
                "gender",
                2.2,
                genderDeviation.getLeft(),
                genderDeviation.getRight(),
                true,
                false
        );
        pedestrianGender.setSeed(simulationSeed);

        // calculate the standard deviation (of normal distribution) to define age range group
        final StandardDeviation AgeRangeGroupDeviation = StandardDeviation.calculate(
                0, 100, 3.6+35.1, 0.1
        );

        pedestrianAgeRangeGroup= new ContDistNormal(
                this,
                "pedestrianAgeRangeGroup",
                2.2,
                AgeRangeGroupDeviation.getLeft(),
                AgeRangeGroupDeviation.getRight(),
                true,
                false
        );
        pedestrianAgeRangeGroup.setSeed(simulationSeed);


        // calculate the standard deviation (of normal distribution) to define psychological nature
        final StandardDeviation PsychologicalNatureDeviation = StandardDeviation.calculate(
                0, 100, 4+13+13, 0.1
        );

        pedestrianPsychologicalNature= new ContDistNormal(
                this,
                "pedestrianAgeRangeGroup",
                2.2,
                PsychologicalNatureDeviation.getLeft(),
                PsychologicalNatureDeviation.getRight(),
                true,
                false
        );
        pedestrianPsychologicalNature.setSeed(simulationSeed);


        pedestrianEntryPoint = new ContDistUniform(
                this,
                "pedestrianEntryPoint",
                0,
                1,
                true,
                false
        );
        pedestrianEntryPoint.setSeed(simulationSeed);

    }

    /**
     * Registers structure of the model for init scheduling.
     * @param modelStructure structure to be registered.
     */
    public void registerModelStructure(IModelStructure modelStructure) {
        this.modelStructure = modelStructure;
    }

    /**
     * Returns a sample of the random stream {@link ContDistUniform} used to determine the random route ratio factor.
     *
     * @return a {@code randomRouteRatioFactor} sample as double.
     */
    public double getRandomRouteRatioFactor() {
        return randomRouteRatioFactor.sample();
    }

    /**
     * Returns a sample of the random stream {@link ContDistUniform} used to determine the distance factor between cars.
     *
     * @return a {@code distanceFactorBetweenCars} sample as double.
     */
    public double getRandomDistanceFactorBetweenCars() {
        return distanceFactorBetweenCars.sample();
    }

    /**
     * Returns a jam indicator value in seconds.
     *
     * @return a jam indicator.
     */
    public double getJamIndicatorInSeconds() {
        return jamIndicatorInSeconds;
    }

    /**
     * Returns a min value of time between car arrivals.
     *
     * @return min time between car arrivals.
     */
    public double getMinTimeBetweenCarArrivals() {
        return minTimeBetweenCarArrivals;
    }

    /**
     * Returns a max value of time between car arrivals.
     *
     * @return max time between car arrivals.
     */
    public double getMaxTimeBetweenCarArrivals() {
        return maxTimeBetweenCarArrivals;
    }

    /**
     * Returns a mean value of time between car arrivals (is calculated based on min and max).
     *
     * @return mean time between car arrivals.
     */
    public double getMeanTimeBetweenCarArrivals() {
        return meanTimeBetweenCarArrivals;
    }

    /**
     * Returns standard deviation between car arrivals.
     * @return standard deviation value.
     */
    public double getStdDeviationTimeBetweenCarArrivals() {
        return Math.abs(getMaxTimeBetweenCarArrivals() - getMeanTimeBetweenCarArrivals());
    }

    /**
     * Returns standard deviation between pedestrian arrivals.
     * @return standard deviation value.
     */
    public double getStdDeviationTimeBetweenPedestrianArrivals() {
        return Math.abs(getMaxTimeBetweenCarArrivals() - getMeanTimeBetweenCarArrivals());
    }

    /**
     * Returns a sample of the random stream {@link ContDistUniform} used to determine the time between car arrivals.
     *
     * @return a {@code timeBetweenCarArrivals} sample as double.
     */
    public double getRandomTimeBetweenCarArrivals() {
        final double value = timeBetweenCarArrivals.sample();
        return Math.max(Math.min(value, maxTimeBetweenCarArrivals), minDistanceFactorBetweenCars);
    }

    /**
     * Returns a sample of the random stream {@link ContDistUniform} used to determine the time between car arrivals.
     *
     * @return a {@code timeBetweenCarArrivals} sample as double.
     */
    public double getRandomTimeBetweenPedestrianArrivals() {
        final double value = timeBetweenPedestrianArrivals.sample();
        return Math.max(Math.min(value, maxTimeBetweenPedestrianArrivals), minDistanceFactorBetweenPedestrians);
    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the length of a vehicle
     *
     * @return a {@code getRandomVehicleLength} sample as double.
     */
    public double getRandomVehicleLength() {
        return (typeOfVehicle.sample() <= carRatioPerTotalVehicle) ? getRandomCarLength() : getRandomTruckLength();
    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the length of a car.
     *
     * @return a {@code getRandomCarLength} sample as double.
     */
    public double getRandomCarLength() {
        return Math.max(Math.min(lengthOfCar.sample(), maxCarLength), minCarLength);
    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the length of a truck.
     *
     * @return a {@code getRandomTruckLength} sample as double.
     */
    public double getRandomTruckLength() {
        return Math.max(Math.min(lengthOfTruck.sample(), maxTruckLength), minTruckLength);
    }

    /**
     * Returns max possible length of vehicle.
     *
     * @return max vehicle length.
     */
    public double getMaxVehicleLength() {
        return Math.max(maxCarLength, maxTruckLength);
    }

    /**
     * Returns configured model {@link TimeUnit}.
     *
     * @return configured model {@link TimeUnit}.
     */
    public TimeUnit getModelTimeUnit() {
        return getExperiment().getReferenceUnit();
    }

    /**
     * Returns model current time in configured model {@link TimeUnit}.
     *
     * @return model current time.
     */
    public double getCurrentTime() {
        return currentModel().getExperiment().getSimClock().getTime().getTimeAsDouble(getModelTimeUnit());
    }

    /**
     * Provides for time between car arrivals random number stream.
     *
     * @return instance of {@link ContDist}.
     */
    public ContDist getTimeBetweenCarArrivalsOnOneWayStreets() {
        return timeBetweenCarArrivalsOnOneWayStreets;
    }

    /**
     * This method returns the acceleration time which is not influenced by driver behaviour.
     *
     * @return standardCarAccelerationTime
     */
    public Double getStandardCarAccelerationTime() {
        return standardCarAccelerationTime;
    }

////////////////////////////////////////////////////////////////////////////////////////////
    //Pedestrian
    /**
     * This method returns the min arrival rate of pedestrians
     *
     * @return minDistanceFactorBetweenPedestrians
     */
    public Double getMinDistanceFactorBetweenPedestrian() {
        return minDistanceFactorBetweenPedestrians;
    }

    /**
     * This method returns the min arrival rate of pedestrians
     *
     * @return maxDistanceFactorBetweenPedestrians
     */
    public Double getMaxDistanceFactorBetweenPedestrians() {
        return maxDistanceFactorBetweenPedestrians;
    }

    /**
     * This method returns the min arrival rate of pedestrians
     *
     * @return minTimeBetweenPedestrianArrivals
     */
    public Double getMinTimeBetweenPedestrianArrivals() {
        return minTimeBetweenPedestrianArrivals;
    }

    /**
     * This method returns the min arrival rate of pedestrians
     *
     * @return maxTimeBetweenPedestrianArrivals
     */
    public Double getMaxTimeBetweenPedestrianArrivals() {
        return maxTimeBetweenPedestrianArrivals;
    }

    /**
     * This method returns the min arrival rate of pedestrians
     *
     * @return meanTimeBetweenPedestrianArrivals
     */
    public Double getMeanTimeBetweenPedestrianArrivals() {
        return meanTimeBetweenPedestrianArrivals;
    }

    /**
     * This method returns the minimum street Length
     *
     * @return minPedestrianStreetLength
     */
    public Double getMinPedestrianStreetLength() {
        return minPedestrianStreetLength;
    } // TODO

    /**
     * This method returns the minimum street width
     *
     * @return minPedestrianStreetWidth
     */

    public Double getMinPedestrianStreetWidth() {
        return minPedestrianStreetWidth;
    } // TODO

    /**
     * Returns a sample of the random stream {@link ContDistUniform} used to determine the distance factor between cars.
     *
     * @return a {@code distanceFactorBetweenCars} sample as double.
     */
    public double getRandomDistanceFactorBetweenPedestrains() {
        return distanceFactorBetweenPedestrians.sample();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //Social Force Model Calculations:

    /**
     * This method returns the Accuracy for Calculations.
     *
     * @return returns the Accuracy for Calculations
     */
    public Double getSFM_DegreeOfAccuracy () {return SFM_DegreeOfAccuracy;}

    /**
     * Random number stream used to calculate a the preferred waling speed of an pedestrian.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    public Double getRandomPedestrianPreferredSpeed() {
        return Math.max(Math.min(pedestrianPreferredSpeed.sample(), maxPedestrianPreferredSpeed), minPedestrianPreferredSpeed);
    }

    /**
     * Random number stream used to calculate the "delay" to reach new defined walking speed
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    public Double getRandomPedestrianRelaxingTimeTauAlpha() {
        return Math.max(Math.min(pedestrianRelaxingTimeTauAlpha.sample(), maxPedestrianRelaxingTimeTauAlpha), minPedestrianRelaxingTimeTauAlpha);
    }

    /**
     * Random number to define the Gender
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    public Gender getRandomPedestrianGender() {
        int maxValue = Gender.values().length -1;
        int minValue = 0;
        long val = Math.round(Math.max(Math.min(pedestrianGender.sample(), maxValue), minValue));
        return Gender.values()[(int)val];
    }

    /**
     * Random number stream to define age range group
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    public AgeRangeGroup getRandomPedestrianAgeGroupe() {
        int maxValue = AgeRangeGroup.values().length -1;
        int minValue = 0;
        double val = Math.round(Math.max(Math.min(pedestrianAgeRangeGroup.sample(),  maxValue), minValue));
        return AgeRangeGroup.values()[(int)val];
    }

    /**
     * Random number stream to define psychological nature
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    public PsychologicalNature getRandomPedestrianPsychologicalNature() {
        int maxValue = PsychologicalNature.values().length -1;
        int minValue = 0;
        double val = Math.round(Math.max(Math.min(pedestrianPsychologicalNature.sample(),  maxValue), minValue));
        return PsychologicalNature.values()[(int)val];
    }

    /**
     * Random number stream to define the entry point
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    public double getRandomEntryPoint(double maxValue, double minValue) {
        return Math.max(Math.min(pedestrianEntryPoint.sample(),  maxValue), minValue);

    }
}
