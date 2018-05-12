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

    private static final Double DEFAULT_MIN_TIME_BETWEEN_CAR_ARRIVALS = 3.5;
    private static final Double DEFAULT_MAX_TIME_BETWEEN_CAR_ARRIVALS = 10.0;
    private static final Double DEFAULT_MIN_DISTANCE_FACTOR_BETWEEN_CARS = 0.0;
    private static final Double DEFAULT_MAX_DISTANCE_FACTOR_BETWEEN_CARS = 1.0;
    private static final Double DEFAULT_MAIN_ARRIVAL_RATE_FOR_ONE_WAY_STREETS = 1.0;
    private static final Double DEFAULT_MIN_CAR_LENGTH = 3.0;
    private static final Double DEFAULT_MAX_CAR_LENGTH = 6.0;
    private static final Double DEFAULT_EXPECTED_CAR_LENGTH = 4.5;
    private static final Double DEFAULT_MIN_TRUCK_LENGTH = 6.0;
    private static final Double DEFAULT_MAX_TRUCK_LENGTH = 19.5;
    private static final Double DEFAULT_EXPECTED_TRUCK_LENGTH = 16.5;
    private static final Double DEFAULT_CAR_RATIO_PER_TOTAL_VEHICLE = 0.8;
    private static final Double DEFAULT_JAM_INDICATOR_IN_SECONDS = 5.0;

    private static final Double DEFAULT_MIN_SPEED = 4.0;
    private static final Double DEFAULT_MAX_SPEED = 9.0;
    private static final Double DEFAULT_EXPECTED_SPEED = 6.0;
    private static final Double DEFAULT_MIN_ACCELERATION_TIME_0_TO_100 = 28.8;
    private static final Double DEFAULT_MAX_ACCELERATION_TIME_0_TO_100 = 64.8;
    private static final Double DEFAULT_EXPECTED_ACCELERATION_TIME_0_TO_100 = 40.0;

    private static final Double DEFAULT_MIN_DISTANCE_TO_NEXT_CAR_MIN = 0.5;
    private static final Double DEFAULT_MAX_DISTANCE_TO_NEXT_CAR_MIN = 1.5;
    private static final Double DEFAULT_EXPECTED_DISTANCE_TO_NEXT_CAR_MIN = 1.0;
    private static final Double DEFAULT_MIN_DISTANCE_TO_NEXT_CAR_MAX = 0.5;
    private static final Double DEFAULT_MAX_DISTANCE_TO_NEXT_CAR_MAX = 1.5;
    private static final Double DEFAULT_EXPECTED_DISTANCE_TO_NEXT_CAR_MAX = 1.0;
    private static final Double DEFAULT_MIN_MERGE_FACTOR = 0.2;
    private static final Double DEFAULT_MAX_MERGE_FACTOR = 1.0;
    private static final Double DEFAULT_EXPECTED_MERGE_FACTOR = 1.1;

    private static final Double VEHICLE_LENGTH_STEP_SIZE = 0.1;
    private static final Double VEHICLE_SPEED_STEP_SIZE = 0.1;
    private static final Double VEHICLE_ACCELERATION_STEP_SIZE = 0.1;
    private static final Double VEHICLE_DISTANCE_TO_NEXT_CAR_STEP_SIZE = 0.1;
    private static final Double VEHICLE_MERGE_FACTOR_STEP_SIZE = 0.1;

    private final Long simulationSeed;
    private final Double minDistanceFactorBetweenCars;
    private final Double maxDistanceFactorBetweenCars;
    private final Double minTimeBetweenCarArrivals;
    private final Double maxTimeBetweenCarArrivals;
    private final Double meanTimeBetweenCarArrivals;
    private final Double mainArrivalRateForOneWayStreets;

    private final Double minCarLength;
    private final Double maxCarLength;
    private final Double expectedCarLength;
    private final Double minTruckLength;
    private final Double maxTruckLength;
    private final Double expectedTruckLength;
    private final Double carRatioPerTotalVehicle;
    private final Double jamIndicatorInSeconds;

    private final Double minVehicleSpeed;
    private final Double maxVehicleSpeed;
    private final Double expectedVehicleSpeed;
    private final Double minAccelerationTime_0To100;
    private final Double maxAccelerationTime_0To100;
    private final Double expectedAccelerationTime_0To100;

    private final Double minDistanceToNextCarMin;
    private final Double maxDistanceToNextCarMin;
    private final Double expectedDistanceToNextCarMin;
    private final Double minDistanceToNextCarMax;
    private final Double maxDistanceToNextCarMax;
    private final Double expectedDistanceToNextCarMax;
    private final Double minMergeFactor;
    private final Double maxMergeFactor;
    private final Double expectedMergeFactor;

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
     * Random number stream used to calculate a preferred speed of a car.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal speedOfCar;

    /**
     * Random number stream used to calculate a acceleration time of a car.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal accelerationTimeOfCar;

    /**
     * Random number stream used to calculate a distance to next car of a car.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal distanceToNextCarMax;

    /**
     * Random number stream used to calculate a distance to next car of a car.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal distanceToNextCarMin;

    /**
     * Random number stream used to calculate a merge factor of a car.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistNormal mergeFactorOfCar;

    /**
     * Random number stream used to gain a value between 0 and 1.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform randomeValue;

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
            DEFAULT_MIN_CAR_LENGTH, DEFAULT_MAX_CAR_LENGTH, DEFAULT_EXPECTED_CAR_LENGTH,
            DEFAULT_MIN_TRUCK_LENGTH, DEFAULT_MAX_TRUCK_LENGTH, DEFAULT_EXPECTED_TRUCK_LENGTH,
            DEFAULT_CAR_RATIO_PER_TOTAL_VEHICLE,
            DEFAULT_JAM_INDICATOR_IN_SECONDS,
            DEFAULT_MIN_SPEED, DEFAULT_MAX_SPEED, DEFAULT_EXPECTED_SPEED,
            DEFAULT_MIN_ACCELERATION_TIME_0_TO_100, DEFAULT_MAX_ACCELERATION_TIME_0_TO_100, DEFAULT_EXPECTED_ACCELERATION_TIME_0_TO_100,
            DEFAULT_MIN_DISTANCE_TO_NEXT_CAR_MIN, DEFAULT_MAX_DISTANCE_TO_NEXT_CAR_MIN, DEFAULT_EXPECTED_DISTANCE_TO_NEXT_CAR_MIN,
            DEFAULT_MIN_DISTANCE_TO_NEXT_CAR_MAX, DEFAULT_MAX_DISTANCE_TO_NEXT_CAR_MAX, DEFAULT_EXPECTED_DISTANCE_TO_NEXT_CAR_MAX,
            DEFAULT_MIN_MERGE_FACTOR, DEFAULT_MAX_MERGE_FACTOR, DEFAULT_EXPECTED_MERGE_FACTOR
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
        Double minDistanceFactorBetweenArrivalCars,
        Double maxDistanceFactorBetweenArrivalCars,
        Double mainArrivalRateForOneWayStreets,

        Double minCarLength,
        Double maxCarLength,
        Double expectedCarLength,
        Double minTruckLength,
        Double maxTruckLength,
        Double expectedTruckLength,
        Double carRatioPerTotalVehicle,
        Double jamIndicatorInSeconds,

        Double minVehicleSpeed,
        Double maxVehicleSpeed,
        Double expectedVehicleSpeed,
        Double minAccelerationTime_0To100,
        Double maxAccelerationTime_0To100,
        Double expectedAccelerationTime_0To100,

        Double minDistanceToNextCarMin,
        Double maxDistanceToNextCarMin,
        Double expectedDistanceToNextCarMin,
        Double minDistanceToNextCarMax,
        Double maxDistanceToNextCarMax,
        Double expectedDistanceToNextCarMax,
        Double minMergeFactor,
        Double maxMergeFactor,
        Double expectedMergeFactor
        ) {
        super(model, name, showInReport, showInTrace);

        this.simulationSeed = simulationSeed;
        this.minTimeBetweenCarArrivals = minTimeBetweenCarArrivals;
        this.maxTimeBetweenCarArrivals = maxTimeBetweenCarArrivals;
        this.meanTimeBetweenCarArrivals = (minTimeBetweenCarArrivals + maxTimeBetweenCarArrivals) / 2;
        this.minDistanceFactorBetweenCars = minDistanceFactorBetweenArrivalCars;
        this.maxDistanceFactorBetweenCars = maxDistanceFactorBetweenArrivalCars;
        this.mainArrivalRateForOneWayStreets = mainArrivalRateForOneWayStreets;
        this.minCarLength = minCarLength;
        this.maxCarLength = maxCarLength;
        this.expectedCarLength = expectedCarLength;
        this.minTruckLength = minTruckLength;
        this.maxTruckLength = maxTruckLength;
        this.expectedTruckLength = expectedTruckLength;
        this.carRatioPerTotalVehicle = carRatioPerTotalVehicle;
        this.jamIndicatorInSeconds = jamIndicatorInSeconds;
        this.minVehicleSpeed = minVehicleSpeed;
        this.maxVehicleSpeed = maxVehicleSpeed;
        this.expectedVehicleSpeed = expectedVehicleSpeed;
        this.minAccelerationTime_0To100 = minAccelerationTime_0To100;
        this.maxAccelerationTime_0To100 = maxAccelerationTime_0To100;
        this.expectedAccelerationTime_0To100 = expectedAccelerationTime_0To100;
        this.minDistanceToNextCarMin = minDistanceToNextCarMin;
        this.maxDistanceToNextCarMin = maxDistanceToNextCarMin;
        this.expectedDistanceToNextCarMin = expectedDistanceToNextCarMin;
        this.minDistanceToNextCarMax = minDistanceToNextCarMax;
        this.maxDistanceToNextCarMax = maxDistanceToNextCarMax;
        this.expectedDistanceToNextCarMax = expectedDistanceToNextCarMax;
        this.minMergeFactor = minMergeFactor;
        this.maxMergeFactor = maxMergeFactor;
        this.expectedMergeFactor = expectedMergeFactor;
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

        // calculate the standard deviation (of skew normal distribution) for car speed
        final StandardDeviation CarSpeedDeviation = StandardDeviation.calculate(
                minVehicleSpeed, maxVehicleSpeed, expectedVehicleSpeed, VEHICLE_SPEED_STEP_SIZE
        );
        speedOfCar = new ContDistNormal(
                this,
                "SpeedOfCar",
                expectedVehicleSpeed,
                CarSpeedDeviation.getLeft(),
                CarSpeedDeviation.getRight(),
                true,
                false
        );
        speedOfCar.setSeed(simulationSeed);

        // calculate the standard deviation (of skew normal distribution) for car acceleration time
        final StandardDeviation carAccelerationTimeDeviation = StandardDeviation.calculate(
                minAccelerationTime_0To100, maxAccelerationTime_0To100, expectedAccelerationTime_0To100, VEHICLE_ACCELERATION_STEP_SIZE
        );
        accelerationTimeOfCar = new ContDistNormal(
                this,
                "AccelerationTimeOfCar",
                expectedAccelerationTime_0To100,
                carAccelerationTimeDeviation.getLeft(),
                carAccelerationTimeDeviation.getRight(),
                true,
                false
        );
        accelerationTimeOfCar.setSeed(simulationSeed);

        // calculate the standard deviation (of skew normal distribution) for car length
        final StandardDeviation distanceToNextCarDeviationMax = StandardDeviation.calculate(
                minDistanceToNextCarMax, maxDistanceToNextCarMax, expectedDistanceToNextCarMax, VEHICLE_DISTANCE_TO_NEXT_CAR_STEP_SIZE
        );
        distanceToNextCarMax = new ContDistNormal(
                this,
                "DistanceToNextVehicleMax",
                expectedCarLength,
                distanceToNextCarDeviationMax.getLeft(),
                distanceToNextCarDeviationMax.getRight(),
                true,
                false
        );
        distanceToNextCarMax.setSeed(simulationSeed);

        // calculate the standard deviation (of skew normal distribution) for car length
        final StandardDeviation distanceToNextCarDeviationMin = StandardDeviation.calculate(
                minDistanceToNextCarMin, expectedDistanceToNextCarMin, expectedDistanceToNextCarMin, VEHICLE_DISTANCE_TO_NEXT_CAR_STEP_SIZE
        );
        distanceToNextCarMin = new ContDistNormal(
                this,
                "DistanceToNextVehicleMin",
                expectedCarLength,
                distanceToNextCarDeviationMin.getLeft(),
                distanceToNextCarDeviationMin.getRight(),
                true,
                false
        );
        distanceToNextCarMin.setSeed(simulationSeed);

        // calculate the standard deviation (of skew normal distribution) for car length
        final StandardDeviation carMergeFactorDeviation = StandardDeviation.calculate(
                minMergeFactor, maxMergeFactor, expectedMergeFactor, VEHICLE_MERGE_FACTOR_STEP_SIZE
        );
        mergeFactorOfCar = new ContDistNormal(
                this,
                "MergeFactorOfVehicle",
                expectedCarLength,
                carMergeFactorDeviation.getLeft(),
                carMergeFactorDeviation.getRight(),
                true,
                false
        );
        mergeFactorOfCar.setSeed(simulationSeed);

        if (mainArrivalRateForOneWayStreets != null) {
            timeBetweenCarArrivalsOnOneWayStreets = ModelFactory.getInstance(this).createContDistConstant(mainArrivalRateForOneWayStreets);
            timeBetweenCarArrivalsOnOneWayStreets.setSeed(simulationSeed);
        }

        randomeValue = new ContDistUniform(
                this,
                "randomValue",
                0.0,
                1.0,
                true,
                false
        );
        randomeValue.setSeed(simulationSeed);

        if (mainArrivalRateForOneWayStreets != null) {
            timeBetweenCarArrivalsOnOneWayStreets = ModelFactory.getInstance(this).createContDistConstant(mainArrivalRateForOneWayStreets);
            timeBetweenCarArrivalsOnOneWayStreets.setSeed(simulationSeed);
        }
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
    public TimeUnit getModelTimeUnit() { return getExperiment().getReferenceUnit(); }

    /**
     * Returns model current time in configured model {@link TimeUnit}.
     *
     * @return model current time.
     */
    public double getCurrentTime() {
        return currentModel().getExperiment().getSimClock().getTime().getTimeAsDouble(getModelTimeUnit());
    }

    /**
     * Provides for time between car arrivals random number stream. - needed for Test
     *
     * @return instance of {@link ContDist}.
     */
    public ContDist getTimeBetweenCarArrivalsOnOneWayStreets() {
        return timeBetweenCarArrivalsOnOneWayStreets;
    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the speed of a vehicle.
     *
     * @return a {@code getRandomTruckLength} sample as double.
     */
    public double getRandomVehicleSpeed() { return Math.max(Math.min(speedOfCar.sample(), maxVehicleSpeed), minVehicleSpeed); }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the acceleration time of a vehicle.
     *
     * @return a {@code getRandomTruckLength} sample as double.
     */
    public double getRandomVehicleAccelerationTime() {
        return Math.max(Math.min(accelerationTimeOfCar.sample(), maxAccelerationTime_0To100), minAccelerationTime_0To100);
    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the distance to the next vehicle.
     *
     * @return a {@code getRandomTruckLength} sample as double.
     */
    public double getRandomMinDistanceToNextVehicle() {
        return Math.max(Math.min(distanceToNextCarMin.sample(), maxDistanceToNextCarMin), minDistanceToNextCarMin);
    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the distance to the next vehicle.
     *
     * @return a {@code getRandomTruckLength} sample as double.
     */
    public double getRandomMaxDistanceToNextVehicle() {
        return Math.max(Math.min(distanceToNextCarMax.sample(), maxDistanceToNextCarMax), minDistanceToNextCarMax);
    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine the merge factor of a vehicle.
     *
     * @return a {@code getRandomTruckLength} sample as double.
     */
    public double getRandomVehicleMergeFactor() {
        return Math.max(Math.min(mergeFactorOfCar.sample(), maxMergeFactor), minMergeFactor);
    }

    /**
     * Returns a sample of the random stream {@link ContDistUniform} used to determ a random value
     *
     * @return a {@code getRandomValue} random value between 0 and 1.
     */
    public double getRandomValue() {
        return randomeValue.sample();
    }
}
