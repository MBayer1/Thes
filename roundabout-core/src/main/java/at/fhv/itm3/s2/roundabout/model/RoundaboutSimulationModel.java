package at.fhv.itm3.s2.roundabout.model;

import at.fhv.itm14.trafsim.model.ModelFactory;
import at.fhv.itm14.trafsim.model.entities.OneWayStreet;
import at.fhv.itm3.s2.roundabout.api.entity.IModelStructure;
import at.fhv.itm3.s2.roundabout.api.entity.Street;
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

    private static final Double DEFAULT_PEDESTRIAN_MIN_ARRIVAL_RATE = 0.1;
    private static final Double DEFAULT_PEDESTRIAN_MAX_ARRIVAL_RATE = 1.0;
    private static final Long DEFAULT_PEDESTRIAN_MIN_GROUPE_SIZE = 2L;
    private static final Long DEFAULT_PEDESTRIAN_MAX_GROUPE_SIZE = 30L;
    private static final Double DEFAULT_PEDESTRIAN_MIN_STREET_LENGTH = 1.0;
    private static final Double DEFAULT_PEDESTRIAN_MIN_STREET_WIDTH = 1.0;
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

    private final Double minPedestrianArrivalRate;
    private final Double maxPedestrianArrivalRate;
    private final Long minPedestrianGroupeSize;
    private final Long maxPedestrianGroupeSize;
    private final Double minPedestrianStreetLength;
    private final Double maxPedestrianStreetWidth;
    private final Double SFM_DegreeOfAccuracy;
    private final Double minPedestrianRelaxingTimeTauAlpha;
    private final Double maxPedestrianRelaxingTimeTauAlpha;
    private final Double expectedPedestrianRelaxingTimeTauAlpha;
    private final Double minPedestrianPreferredSpeed;
    private final Double maxPedestrianPreferredSpeed;
    private final Double expectedPedestrianPreferredSpeed;


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





    /////////////////////////////////////////////////////////////////////////////////
    ////Social Force Model Calculations:

    /**
     * Random number stream used to draw a time between two car arrivals.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal relaxingTimeTauAlpha;

    /**
     * Random number stream used to draw a time between two car arrivals.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal preferredSpeed;





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
            DEFAULT_JAM_INDICATOR_IN_SECONDS,

            DEFAULT_PEDESTRIAN_MIN_ARRIVAL_RATE, DEFAULT_PEDESTRIAN_MAX_ARRIVAL_RATE,
            DEFAULT_PEDESTRIAN_MIN_GROUPE_SIZE, DEFAULT_PEDESTRIAN_MAX_GROUPE_SIZE,
            DEFAULT_PEDESTRIAN_MIN_STREET_LENGTH, DEFAULT_PEDESTRIAN_MIN_STREET_WIDTH,
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
            Double jamIndicatorInSeconds
    ) {
        this(
            simulationSeed, model, name, showInReport, showInTrace, minTimeBetweenCarArrivals,
            maxTimeBetweenCarArrivals, minDistanceFactorBetweenCars, maxDistanceFactorBetweenCars,
            mainArrivalRateForOneWayStreets, standardCarAccelerationTime,
            minCarLength, maxCarLength, expectedCarLength, minTruckLength, maxTruckLength,
            expectedTruckLength, carRatioPerTotalVehicle, jamIndicatorInSeconds,

            DEFAULT_PEDESTRIAN_MIN_ARRIVAL_RATE, DEFAULT_PEDESTRIAN_MAX_ARRIVAL_RATE,
            DEFAULT_PEDESTRIAN_MIN_GROUPE_SIZE, DEFAULT_PEDESTRIAN_MAX_GROUPE_SIZE,
            DEFAULT_PEDESTRIAN_MIN_STREET_LENGTH, DEFAULT_PEDESTRIAN_MIN_STREET_WIDTH,
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
        Double pedestrianMinArrivalRate,
        Double pedestrianMaxArrivalRate,
        Long pedestrianMinGroupeSize,
        Long pedestrianMaxGroupeSize,
        Double pedestrianMinStreetLength,
        Double pedestrianMaxStreetWidth,
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

        this.minPedestrianArrivalRate = pedestrianMinArrivalRate;
        this.maxPedestrianArrivalRate = pedestrianMaxArrivalRate;
        this.minPedestrianGroupeSize = pedestrianMinGroupeSize;
        this.maxPedestrianGroupeSize = pedestrianMaxGroupeSize;
        this.minPedestrianStreetLength = pedestrianMinStreetLength;
        this.maxPedestrianStreetWidth = pedestrianMaxStreetWidth;
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

        relaxingTimeTauAlpha = new ContDistNormal(
                this,
                "relaxingTimeTauAlpha",
                2.2,
                relaxingTimeTauAlphaDeviation.getLeft(),
                relaxingTimeTauAlphaDeviation.getRight(),
                true,
                false
        );
        relaxingTimeTauAlpha.setSeed(simulationSeed);


        // calculate the standard deviation (of skew normal distribution) for the preferred Speed of Pedestrians (m/s)
        final StandardDeviation preferredSpeedDeviation = StandardDeviation.calculate(
                1.34-0.26, 1.34+0.26, 1.34, 0.01
        );

        preferredSpeed  = new ContDistNormal(
                this,
                "preferredSpeed",
                1.34,
                preferredSpeedDeviation.getLeft(),
                preferredSpeedDeviation.getRight(),
                true,
                false
        );
        relaxingTimeTauAlpha.setSeed(simulationSeed);


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
     * Returns a sample of the random stream {@link ContDistUniform} used to determine the time between car arrivals.
     *
     * @return a {@code timeBetweenCarArrivals} sample as double.
     */
    public double getRandomTimeBetweenCarArrivals() {
        final double value = timeBetweenCarArrivals.sample();
        return Math.max(Math.min(value, maxTimeBetweenCarArrivals), minDistanceFactorBetweenCars);
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


    /////////////////////////////////////////////////////////////////////////////////////////////
    //Social Force Model Calculations:
    public Double getSFM_DegreeOfAccuracy () {return SFM_DegreeOfAccuracy;}

    public Double getRandomPreferredSpeed () { //TODO COMMENT
        return Math.max(Math.min(preferredSpeed.sample(), maxPedestrianPreferredSpeed), minPedestrianPreferredSpeed);
    }

    public Double getRandomRelaxingTimeTauAlpha () {
        return Math.max(Math.min(relaxingTimeTauAlpha.sample(), maxPedestrianRelaxingTimeTauAlpha), minPedestrianRelaxingTimeTauAlpha);
    }




}
