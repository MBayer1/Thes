package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.CarController;
import at.fhv.itm3.s2.roundabout.controller.IntersectionController;
import at.fhv.itm3.s2.roundabout.event.CarCouldLeaveSectionEvent;
import at.fhv.itm3.s2.roundabout.event.RoundaboutEventFactory;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.*;

public class StreetSection extends Street {

    private final double length;

    // next two values are for the controlling of a traffic light [checking for jam/ needed for optimization]
    private double currentWaitingTime;
    private double currentTimeLastMovement;

    private final double noLeavingSection = 0.0; // 0%
    private final double fullyLeavingSection = 100;

    private final LinkedList<ICar> carQueue;
    private final Map<ICar, VehicleOnStreetSection> carPositions;

    private IStreetConnector nextStreetConnector;
    private IStreetConnector previousStreetConnector;

    private IntersectionController intersectionController;

    private double percentageOfVehicleThatCanLeave; // as trafsim should not be modified, to pass to carEnter the percentage of the Car this variable is used as a workaround.

    public StreetSection(
        double length,
        Model model,
        String modelDescription,
        boolean showInTrace
    ) {
        this(UUID.randomUUID().toString(), length, model, modelDescription, showInTrace);
    }

    public StreetSection(
        String id,
        double length,
        Model model,
        String modelDescription,
        boolean showInTrace
    ) {
        this(
            id, length, model, modelDescription, showInTrace,
            false, null, null, null
        );
    }

    public StreetSection(
        double length,
        Model model,
        String modelDescription,
        boolean showInTrace,
        boolean trafficLightActive,
        Long greenPhaseDuration,
        Long redPhaseDuration
    ) {
        this(
            UUID.randomUUID().toString(), length, model, modelDescription, showInTrace,
            trafficLightActive, null, greenPhaseDuration, redPhaseDuration
        );
    }

    public StreetSection(
        String id,
        double length,
        Model model,
        String modelDescription,
        boolean showInTrace,
        boolean trafficLightActive,
        Long minGreenPhaseDuration,
        Long greenPhaseDuration,
        Long redPhaseDuration
    ) {
        super(
            id,
            model,
            modelDescription,
            showInTrace,
            trafficLightActive,
            minGreenPhaseDuration,
            greenPhaseDuration,
            redPhaseDuration
        );

        this.length = length;

        this.carQueue = new LinkedList<>();
        this.carPositions = new HashMap<>();
        this.intersectionController = IntersectionController.getInstance();
        this.percentageOfVehicleThatCanLeave = this.noLeavingSection;

        if(this.isTrafficLightActive() && !this.isTrafficLightTriggeredByJam()) {
            RoundaboutEventFactory.getInstance().createToggleTrafficLightStateEvent(getRoundaboutModel()).schedule(
                this,
                new TimeSpan(greenPhaseDuration)
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCar(ICar iCar, double percentageOfCar) {
        if (carQueue == null) {
            throw new IllegalStateException("carQueue in section cannot be null");
        }

        carQueue.addLast(iCar);
        carPositions.put(iCar, new VehicleOnStreetSection(iCar.getVehicleLength() * (percentageOfCar/100), percentageOfCar));
        incrementEnteredCarCounter();

        IStreetConnector connector = null;
        if (previousStreetConnector != null) {
            connector = previousStreetConnector;
        } else if (nextStreetConnector != null) {
            connector = nextStreetConnector;
        }

        if (connector!= null) {
            if (connector.getTypeOfConsumer(this) == ConsumerType.ROUNDABOUT_INLET) {
                iCar.enterRoundabout();
            } else if (connector.getTypeOfConsumer(this) == ConsumerType.ROUNDABOUT_EXIT) {
                iCar.leaveRoundabout();
            }
        }
        eraseMovedPartOfVehicleFromItsEnding(iCar,0);
        carObserver.notifyObservers(iCar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleJamTrafficLight(){
        if (this.isTrafficLightActive() && this.isTrafficLightTriggeredByJam()) {
            final ICar car = getFirstCar();
            if (car != null && this.isTrafficLightFreeToGo()) {
                int idx = car.getRoute().getIndexOfSection(this);
                if (idx + 1 < car.getRoute().getNumberOfSections()) {
                    final IConsumer consumerNext = car.getRoute().getSectionAt(idx + 1);
                    if (!(consumerNext instanceof StreetSection)) {
                        throw new IllegalArgumentException("Failing cast form IConsumer to StreetSection.");
                    }

                    final StreetSection streetSectionNext = (StreetSection) consumerNext;
                    final boolean isWaitingTimeBiggerThanJamIndicator = streetSectionNext.currentWaitingTime > getRoundaboutModel().getJamIndicatorInSeconds();
                    final boolean isActualGreenPhaseBiggerThanMin = (getRoundaboutModel().getCurrentTime() - getGreenPhaseStart()) > getMinGreenPhaseDurationOfTrafficLight();

                    if (!streetSectionNext.isEmpty() && isWaitingTimeBiggerThanJamIndicator && isActualGreenPhaseBiggerThanMin) {
                        // trigger red
                        RoundaboutEventFactory.getInstance().createToggleTrafficLightStateEvent(getRoundaboutModel()).schedule(
                            this,
                            new TimeSpan(0, getRoundaboutModel().getModelTimeUnit())
                        );
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICar removeFirstCar() {
        incrementLeftCarCounter();
        return carQueue.removeFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICar getFirstCar() {
        final List<ICar> carQueue = getCarQueue();

        if (carQueue.size() > 0) {
            return carQueue.get(0);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICar getLastCar() {
        final List<ICar> carQueue = getCarQueue();

        if (carQueue.size() > 0) {
            return carQueue.get(carQueue.size() - 1);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ICar> getCarQueue()
    throws IllegalStateException {
        if (carQueue == null) {
            throw new IllegalStateException("carQueue in section cannot be null");
        }

        return Collections.unmodifiableList(carQueue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        final List<ICar> carQueue = getCarQueue();
        return carQueue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStreetConnector getNextStreetConnector() {
        return nextStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStreetConnector getPreviousStreetConnector() {
        return previousStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreviousStreetConnector(IStreetConnector previousStreetConnector) {
        this.previousStreetConnector = previousStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNextStreetConnector(IStreetConnector nextStreetConnector) {
        this.nextStreetConnector = nextStreetConnector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<ICar, VehicleOnStreetSection> getCarPositions() {
        return Collections.unmodifiableMap(carPositions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAllCarsPositions() throws IllegalStateException {
        final double currentTime = getRoundaboutModel().getCurrentTime();
        final List<ICar> carQueue = getCarQueue();

        // Updating positions for all cars.
        ICar previousCar = null;
        for (ICar currentCar : carQueue) {
            final double carLastUpdateTime = currentCar.getLastUpdateTime();

            if (carLastUpdateTime != currentTime) {
                final IDriverBehaviour carDriverBehaviour = currentCar.getDriverBehaviour();
                final double carSpeed = carDriverBehaviour.getSpeed();
                final double carPosition = getCarPosition(currentCar);

                // Calculate distance to next car / end of street section based on distributed driver behaviour values.
                final double distanceToNextCar = calculateDistanceToNextCar(
                    carDriverBehaviour.getMinDistanceToNextCar(),
                    carDriverBehaviour.getMaxDistanceToNextCar(),
                    getRoundaboutModel().getRandomDistanceFactorBetweenCars()
                );

                // Calculate possible car positions.
                final double maxTheoreticallyPossiblePositionValue = calculateMaxPossibleCarPosition(
                    getLength(), // TODO check min distance is not fulfilled pref section?
                    distanceToNextCar,
                    getCarPosition(previousCar),
                    previousCar
                );

                final double maxActuallyPossiblePositionValue = carPosition + (currentTime - carLastUpdateTime) * carSpeed;

                // Select the new RoundaboutCar position based on previous calculations.
                double newCarPosition = Math.min(
                    maxTheoreticallyPossiblePositionValue,
                    maxActuallyPossiblePositionValue
                );

                if (newCarPosition < carPosition) { // TODO add do not move back as dis to next car is dynamic!!???
                    newCarPosition = carPosition;
                    currentTimeLastMovement = getModel().getExperiment().getSimClock().getTime().getTimeAsDouble();
                    currentWaitingTime = 0; //reset
                } else {
                    currentWaitingTime = getModel().getExperiment().getSimClock().getTime().getTimeAsDouble() - currentTimeLastMovement;
                }

                if (carPosition == newCarPosition && !currentCar.isWaiting()) {
                    currentCar.startWaiting();
                } else if (
                        (carPosition != newCarPosition || carPosition == currentCar.getVehicleLength())
                                && currentCar.isWaiting()
                                && newCarPosition - carPosition > currentCar.getVehicleLength()
                        ) {
                    currentCar.stopWaiting();
                }
                currentCar.setLastUpdateTime(currentTime);

                if (this.getLastCar().equals(currentCar) &&
                    this.carPositions.get(this.getLastCar()).getPercentageOfVehicleLength() < fullyLeavingSection) {
                    double previousPosition = carPositions.get(currentCar).getVehiclePositionOnStreetSection();
                    carPositions.get(currentCar).setVehiclePositionOnStreetSection(newCarPosition);
                    double newPercentage = Math.min(fullyLeavingSection, ((newCarPosition - previousPosition) / currentCar.getVehicleLength()) *100);
                    carPositions.get(currentCar).setPercentageOfVehicleLength(newPercentage);
                    eraseMovedPartOfVehicleFromItsEnding(currentCar, previousPosition);
                } else {
                    carPositions.get(currentCar).setVehiclePositionOnStreetSection(newCarPosition);
                }
            }
            previousCar = currentCar;
        }
        carPositionObserver.notifyObservers();
    }

    void eraseMovedPartOfVehicleFromItsEnding(ICar beginningOfCurrentCar, double previousPositionOnSection){
        // part of the last vehicle is dived over two sections.
        // A movement will move the car part in the previous section:
        double newSpaceForVehicle = this.getCarPositions().get(beginningOfCurrentCar).getVehiclePositionOnStreetSection() - previousPositionOnSection; // shifted distance
        if(newSpaceForVehicle <= 0) return;

        // remove length from ending of current car to receive 100% of car length in sum again (currently it is more)
        ICar lastVehiclePart;
        IConsumer lastStreetSectionOfCurrentVehicle;
        while(newSpaceForVehicle > 0){
            lastStreetSectionOfCurrentVehicle = getLastStreetSectionOfCurrentCar(beginningOfCurrentCar);

            if(!(lastStreetSectionOfCurrentVehicle instanceof Street)){
                throw new IllegalStateException("IConsumer should be of type Street");
            }
            lastVehiclePart = ((Street) lastStreetSectionOfCurrentVehicle).getFirstCar();
            if(lastStreetSectionOfCurrentVehicle.equals(this) && getCarPositions().get(lastVehiclePart).getPercentageOfVehicleLength() == fullyLeavingSection) return;
            double lastVehiclePartLength = lastVehiclePart.getVehiclePercentualLength();

            // rest of the cars in this last section will be updates in an upcoming event
            if(newSpaceForVehicle < lastVehiclePartLength){
                double shiftedPercentageTmp = ((newSpaceForVehicle / lastVehiclePart.getVehicleLength())*100);
                double newPercentageTmp = ((Street) lastStreetSectionOfCurrentVehicle).getCarPositions().get(lastVehiclePart).getPercentageOfVehicleLength();
                ((Street) lastStreetSectionOfCurrentVehicle).getCarPositions().get(lastVehiclePart).setPercentageOfVehicleLength(newPercentageTmp - shiftedPercentageTmp);

                double newPositionTmp = Math.min(((Street) lastStreetSectionOfCurrentVehicle).getLength(), ((Street) lastStreetSectionOfCurrentVehicle).getCarPositions().get(lastVehiclePart).getVehiclePositionOnStreetSection() + newSpaceForVehicle);
                ((Street) lastStreetSectionOfCurrentVehicle).getCarPositions().get(lastVehiclePart).setVehiclePositionOnStreetSection(newPositionTmp);
            } else {
                // call carDelivered events for last section, so the car position
                // of the current car (that has just left the previous section successfully
                // can be removed (saves memory)
                // caution! that requires to call traverseToNextSection before calling this method
                ICar VehicleTmp = ((Street) lastStreetSectionOfCurrentVehicle).removeFirstCar();
                carPositions.remove(VehicleTmp); // do not use carDelivered as is reference to the first part of the vehicle
            }
            newSpaceForVehicle -= lastVehiclePartLength;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IConsumer getLastStreetSectionOfCurrentCar (ICar vehicle){
        ICar previousCarPart = vehicle;
        IConsumer previousStreet = this;
        IConsumer lastSuccessfulPreviousStreet = null;
        if(!(vehicle instanceof RoundaboutCar)){
            throw new IllegalStateException("current ICar should be of type RoundaboutCar");
        }

        while(((RoundaboutCar)previousCarPart).getOldImplementationCar() == ((RoundaboutCar)vehicle).getOldImplementationCar()){
            lastSuccessfulPreviousStreet = previousStreet;
            previousStreet = previousCarPart.getPreviousSection();
            if(previousStreet == null) break;
            if(!(previousStreet instanceof Street)){
                throw new IllegalStateException("Previous IConsumer should be of type Street");
            }
            previousCarPart = ((Street)previousStreet).getFirstCar();
            if(previousCarPart == null) break;
            if (!(previousCarPart instanceof RoundaboutCar)) {
                throw new IllegalStateException("Previous ICar should be of type RoundaboutCar");
            }
        }
        if(lastSuccessfulPreviousStreet == null) {
            int oho = 0;//todo del
        }
        return  lastSuccessfulPreviousStreet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirstCarOnExitPoint() {
        final ICar firstCar = getFirstCar();

        if (firstCar != null && firstCar.getDriverBehaviour() != null && beginningOfVehicleIsOnThisSection(firstCar)) {
            final double distanceToSectionEnd = Math.abs(getLength() - getCarPosition(firstCar));
            return distanceToSectionEnd <= firstCar.getDriverBehaviour().getMaxDistanceToNextCar();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double firstCarCouldEnterNextSection() {
        //update previous section in case a car is solely partly in the next one. it can be changed to be 10%
        updateAllCarsPositions();

        if (isTrafficLightActive() && !isTrafficLightFreeToGo()) {
            return noLeavingSection;
        }

        if (isFirstCarOnExitPoint()) {
            ICar firstCarInQueue = getFirstCar();

            if (firstCarInQueue != null) {
                IConsumer nextConsumer = firstCarInQueue.getNextSection();

                if (nextConsumer == null) { // car at destination
                    return fullyLeavingSection;
                }

                if (nextConsumer instanceof Street) {
                    Street nextStreet = (Street) nextConsumer;
                    NeededSpaceForVehicle spaceDataOfVehicleThatCanEnter = nextStreet.isEnoughSpaceForCarInPercentage(firstCarInQueue);
                    if (spaceDataOfVehicleThatCanEnter.getPercentageOfVehicleThatCanLeave() > 0) {
                        // PRECEDENCE CHECK
                        IStreetConnector nextConnector = getNextStreetConnector();
                        ConsumerType currentConsumerType = nextConnector.getTypeOfConsumer(this);

                        if (nextConnector.isNextConsumerOnSameTrackAsCurrent(this, nextStreet)) {
                            switch (currentConsumerType) {
                                // case 1: car is in the roundabout and wants to remain on the track
                                // (it has precedence)
                                case ROUNDABOUT_SECTION:
                                // case 2: car is on a normal street section and wants to remain on the track
                                case STREET_SECTION:
                                // case 3: car is on a roundabout exit and wants to remain on the track
                                case ROUNDABOUT_EXIT:
                                    return spaceDataOfVehicleThatCanEnter.getPercentageOfVehicleThatCanLeave();
                                // case 4: car wants to enter the roundabout from an inlet
                                // (it has to give precedence to all cars in the roundabout that are on tracks
                                // the car has to cross)
                                case ROUNDABOUT_INLET:
                                    Collection<IConsumer> previousStreets = nextConnector.getPreviousConsumers();
                                    for (IConsumer previousStreet: previousStreets) {
                                        if (!(previousStreet instanceof Street)) {
                                            throw new IllegalStateException("All previous IConsumer should be of type Street");
                                        }
                                        IStreetConnector previousConnectorOfNextSection = nextStreet.getPreviousStreetConnector();
                                        if(previousConnectorOfNextSection == null){
                                            throw new IllegalStateException("Next StreetSection must be part of roundabout.");
                                        }

                                        // part to give precedence to all cars in the roundabout
                                        ((Street)previousStreet).updateAllCarsPositions();
                                        if (((Street)previousStreet).isFirstCarOnExitPoint()) {
                                            firstCarInQueue.startWaiting();
                                            return noLeavingSection;
                                        }

                                        for(IConsumer s : previousConnectorOfNextSection.getPreviousConsumers()){
                                            if(s instanceof Street){
                                                // previous connector has to be inlet as otherwise it is not part of the roundabout
                                                if (previousConnectorOfNextSection.getTypeOfConsumer(s).equals(ConsumerType.ROUNDABOUT_SECTION)){
                                                    // to enter the roundabout (in to the flowing traffic)
                                                    // additional space is needed in previous roundabout sections
                                                    // in case the driver shows a light signal that this one will enter the current street section
                                                    // a roundabout can solely fully or not at all be entered
                                                    ICar firstVehicleOfPrevSection = ((Street) s).getFirstCar();
                                                    if (firstVehicleOfPrevSection != null && this == firstVehicleOfPrevSection.getNextSection()) {
                                                        Map<ICar, VehicleOnStreetSection> carPositions = ((Street) s).getCarPositions();
                                                        double spaceToExitPoint = ((Street) s).getLength() -
                                                                carPositions.get(firstVehicleOfPrevSection).getVehiclePositionOnStreetSection();
                                                        if (spaceToExitPoint < spaceDataOfVehicleThatCanEnter.getLengthOfMergeFactor() ||
                                                                spaceDataOfVehicleThatCanEnter.getPercentageOfVehicleThatCanLeave() != fullyLeavingSection) {
                                                            return fullyLeavingSection;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (nextConnector.isNextConsumerOnSameTrackAsCurrent(previousStreet, nextStreet)) {
                                            break;
                                        }
                                    }
                                    break;
                            }
                        } else {
                            switch (currentConsumerType) {
                                // case 5: car wants to change the track in the roundabout exit
                                // (it has to give precedence to a car on that track)
                                case ROUNDABOUT_EXIT:
                                // case 6: car wants to change the track on a streetsection
                                // (it has to give precedence to a car on that track)
                                case STREET_SECTION:
                                    List<IConsumer> streetsThatHavePrecedence = nextConnector.getPreviousTrackConsumers(nextStreet, currentConsumerType);
                                    for (IConsumer precedenceSection: streetsThatHavePrecedence) {
                                        if (!(precedenceSection instanceof Street)) {
                                            throw new IllegalStateException("All previous IConsumer should be of type Street");
                                        }
                                        ((Street)precedenceSection).updateAllCarsPositions();
                                        if (((Street)precedenceSection).isFirstCarOnExitPoint()) {
                                            firstCarInQueue.startWaiting();
                                            return noLeavingSection;
                                        }
                                    }
                                    break;
                                // case 7: car is on a roundabout inlet and wants to change to another
                                // roundabout section that is not on its track
                                // (it has to give precedence to all cars in the roundabout that are on tracks
                                // the car has to cross and to all cars on the inlets of the track it wants to change to)
                                case ROUNDABOUT_INLET:
                                    List<IConsumer> previousStreets = nextConnector.getPreviousConsumers(ConsumerType.ROUNDABOUT_SECTION);
                                    for (IConsumer previousStreet: previousStreets) {
                                        if (!(previousStreet instanceof Street)) {
                                            throw new IllegalStateException("All previous IConsumer should be of type Street");
                                        }
                                        ((Street)previousStreet).updateAllCarsPositions();
                                        if (((Street)previousStreet).isFirstCarOnExitPoint()) {
                                            firstCarInQueue.startWaiting();
                                            return noLeavingSection;
                                        }
                                        if (nextConnector.isNextConsumerOnSameTrackAsCurrent(previousStreet, nextStreet)) {
                                            break;
                                        }
                                    }
                                    List<IConsumer> inlets = nextConnector.getPreviousTrackConsumers(nextStreet, ConsumerType.ROUNDABOUT_INLET);
                                    for (IConsumer inlet: inlets) {
                                        if (!(inlet instanceof Street)) {
                                            throw new IllegalStateException("All previous IConsumer should be of type Street");
                                        }
                                        ((Street)inlet).updateAllCarsPositions();
                                        if (((Street)inlet).isFirstCarOnExitPoint()) {
                                            firstCarInQueue.startWaiting();
                                            return noLeavingSection;
                                        }
                                    }
                                    break;
                                case ROUNDABOUT_SECTION:
                                    ConsumerType nextConsumerType = nextConnector.getTypeOfConsumer(nextStreet);
                                    List<IConsumer> previousSections;
                                    switch (nextConsumerType) {
                                        // case 8: the car is in the roundabout and wants to change to a roundabout section
                                        // on another track (it has to give precedence to the cars that are on the previous
                                        // sections of this track)
                                        case ROUNDABOUT_SECTION:
                                            previousSections = nextConnector.getPreviousTrackConsumers(nextStreet, ConsumerType.ROUNDABOUT_SECTION);
                                            for (IConsumer previousSection: previousSections) {
                                                if (!(previousSection instanceof Street)) {
                                                    throw new IllegalStateException("All previous IConsumer should be of type Street");
                                                }
                                                ((Street)previousSection).updateAllCarsPositions();
                                                if (((Street)previousSection).isFirstCarOnExitPoint()) {
                                                    firstCarInQueue.startWaiting();
                                                    return noLeavingSection;
                                                }
                                            }
                                            break;
                                        // case 9: the car is in the roundabout and wants to leave the roundabout over an exit
                                        // that lies not on its track (it has to give precedence to all cars in the roundabout that
                                        // are on tracks it has to cross)
                                        case ROUNDABOUT_EXIT:
                                            previousSections = nextConnector.getPreviousConsumers(ConsumerType.ROUNDABOUT_SECTION);
                                            int indexOfCurrentSection = previousSections.indexOf(this);
                                            for (int i = indexOfCurrentSection - 1; i >= 0; i--) {
                                                IConsumer previousSection = previousSections.get(i);
                                                if (!(previousSection instanceof Street)) {
                                                    throw new IllegalStateException("All previous IConsumer should be of type Street");
                                                }
                                                ((Street)previousSection).updateAllCarsPositions();
                                                if (((Street)previousSection).isFirstCarOnExitPoint()) {
                                                    firstCarInQueue.startWaiting();
                                                    return noLeavingSection;
                                                }
                                            }
                                            break;
                                        default:
                                            throw new IllegalStateException("After a ROUNDABOUT_SECTION only another ROUNDABOUT_SECTION or a ROUNDABOUT_EXIT is allowed");
                                    }
                                    break;
                            }
                        }
                        return spaceDataOfVehicleThatCanEnter.getPercentageOfVehicleThatCanLeave();
                    } else {
                        firstCarInQueue.startWaiting();
                    }
                } else if (nextConsumer instanceof RoundaboutIntersection) {
                    final IConsumer consumer = firstCarInQueue.getSectionAfterNextSection();
                    if (consumer != null && consumer instanceof Street) {
                        // Such a trick should block cars from entering into intersection when the target section is full.
                        // At the worse scenario intersection will accumulate cars in queues as it was before.
                        final Street streetAfterIntersection = (Street) consumer;
                        return streetAfterIntersection.isEnoughSpaceForCarInPercentage(firstCarInQueue).getPercentageOfVehicleThatCanLeave();
                    } else {
                        // fallback only in case intersection is connected to intersection,
                        // because Intersection is never full (isFull() of Intersection returns always false)
                        return fullyLeavingSection;
                    }
                }
            }
        }
        return noLeavingSection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NeededSpaceForVehicle isEnoughSpaceForCarInPercentage(ICar car) {
        double distanceToNextVehicle = calculateDistanceToNextCar(
                car.getDriverBehaviour().getMinDistanceToNextCar(),
                car.getDriverBehaviour().getMaxDistanceToNextCar(),
                getRoundaboutModel().getRandomValue());

        NeededSpaceForVehicle neededSpaceData = new NeededSpaceForVehicle(
                car.getDriverBehaviour().getMergeFactor(), // todo check mergefactor default 1
                noLeavingSection);
        if(!beginningOfVehicleIsOnThisSection(car)){
            distanceToNextVehicle = 0;
        }

        final double freeSpace = calculateFreeSpace();
        double fullyNeededSpaceForVehicle = car.getVehicleLength() + distanceToNextVehicle;

        if(fullyNeededSpaceForVehicle < freeSpace) {
            neededSpaceData.setPercentageOfVehicleThatCanLeave(fullyLeavingSection);
        } else {
            double percentageOfVehicle = ((freeSpace-distanceToNextVehicle) / (fullyNeededSpaceForVehicle-distanceToNextVehicle)) *100;
            percentageOfVehicle = Math.floor( percentageOfVehicle * 100) / 100; // round to two decimal places
            neededSpaceData.setPercentageOfVehicleThatCanLeave( percentageOfVehicle );
        }
        return neededSpaceData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean beginningOfVehicleIsOnThisSection(ICar car){
        IConsumer nextSection = car.getNextSection();
        if (nextSection != null && nextSection instanceof Street) {
            if ( ((Street) nextSection).getCarPositions() != null &&  // check for null as sinks reruns it
                    !(((Street) nextSection).getCarPositions().isEmpty()) &&
                    ((Street) nextSection).getCarPositions().get(car) != null ) return false;
        } else {
            throw new IllegalStateException("Next Section must be instance of Street.");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveFirstCarToNextSection(double percentageOfVehicleThatCanLeaveSection)
    throws IllegalStateException {
        ICar firstCar = null;
        if(percentageOfVehicleThatCanLeaveSection == fullyLeavingSection) {
            firstCar = removeFirstCar();
            carPositions.remove(firstCar);
        } else if(percentageOfVehicleThatCanLeaveSection  != noLeavingSection){
            firstCar = new RoundaboutCar(getRoundaboutModel(), getFirstCar());
            ICar firstCarTmp = getFirstCar();// todo del
            if(!(firstCarTmp instanceof  RoundaboutCar)) throw new IllegalStateException("First car must be an instance of RoundaboutCar.");
            CarController.addCarMapping(((RoundaboutCar)firstCar).getOldImplementationCar(), firstCar);
            incrementLeftCarCounter();
        }

        if (firstCar != null) {
            if (!Objects.equals(firstCar.getCurrentSection(), firstCar.getDestination())) {
                IConsumer nextSection = firstCar.getNextSection();
                if (nextSection != null && nextSection instanceof Street) {
                    // this order of calls is important!
                    // Move logically first car to next section.
                    firstCar.traverseToNextSection();
                    // Move physically first car to next section.
                    ((Street) nextSection).addCar(firstCar, percentageOfVehicleThatCanLeaveSection);
                } else if (nextSection != null && nextSection instanceof RoundaboutIntersection) {
                    RoundaboutIntersection intersection = (RoundaboutIntersection) nextSection;
                    Car car = CarController.getCar(firstCar);
                    int outDirection = intersectionController.getOutDirectionOfIConsumer(intersection, firstCar.getSectionAfterNextSection());
                    car.setNextDirection(outDirection);
                    // this is made without the CarDepartureEvent of the existing implementation
                    // because it can not handle traffic jam
                    intersection.carEnter(car, intersectionController.getInDirectionOfIConsumer(intersection, this));
                    firstCar.traverseToNextSection();
                } else {
                    throw new IllegalStateException("Car can not move further. Next section does not exist.");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICar getVehicleOnThisSectionByOldImplementedCar(ICar car){
        if(!(car instanceof RoundaboutCar)) throw new IllegalStateException("Car must be instance of RoundaboutCar");
        for( ICar carCompare: this.getCarQueue()){
            if(carCompare instanceof  RoundaboutCar){
                if(((RoundaboutCar) carCompare).getOldImplementationCar().equals(((RoundaboutCar) car).getOldImplementationCar())){
                    return carCompare;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean carCouldEnterNextSection() {
        throw new IllegalStateException("Street section is not empty, but last car could not be determined.");
    }

    private double getCarPosition(ICar car) {
        if (car != null) {
            return getCarPositions().get(car).getVehiclePositionOnStreetSection();
        }
        return -1;
    }

    private double calculateFreeSpace() {
        updateAllCarsPositions();
        ICar lastCar = getLastCar();
        if (lastCar != null) {
            VehicleOnStreetSection tmo = getCarPositions().get(lastCar); //todo del
            if( getCarPositions().get(lastCar).getPercentageOfVehicleLength() != fullyLeavingSection ){
                return 0; // if last car is solely partial on the section, there is no space for a new one
            }
            final double lastCarPosition = getCarPosition(lastCar);
            ((Street)lastCar.getCurrentSection()).getCarPositions();

            return Math.max(lastCarPosition - lastCar.getVehicleLength(), 0);
        }

        // Otherwise whole section is empty.
        return getLength();
    }

    private static double calculateDistanceToNextCar(
        double carMinDistanceToNextCar,
        double carMaxDistanceToNextCar,
        double randomDistanceFactorBetweenCars
    ) {
        final double carVariationDistanceToNextCar = carMaxDistanceToNextCar - carMinDistanceToNextCar;
        return carMinDistanceToNextCar + carVariationDistanceToNextCar * randomDistanceFactorBetweenCars;
    }

    private static double calculateMaxPossibleCarPosition(
        double lengthInMeters,
        double distanceToNextCar,
        double previousCarPosition,
        ICar previousCar
    ) {
        if (previousCar != null) {
            return previousCarPosition - previousCar.getVehicleLength() - distanceToNextCar;
        } else {
            return lengthInMeters - distanceToNextCar;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void carEnter(Car car){
        ICar iCar = CarController.getICar(car);
        // this method is only used by an Intersection object
        // and this has to call this method always even if there is not
        // enough space for another car (because otherwise a RuntimeException
        // is thrown). So the check if there is enough space for this car
        // is made here and if there isn't enough space than a car is lost
        // and the counter is incremented
        if (percentageOfVehicleThatCanLeave > 0) {
            iCar.traverseToNextSection();
            addCar(iCar, percentageOfVehicleThatCanLeave);
            double traverseTime = iCar.getTimeToTraverseCurrentSection();
            CarCouldLeaveSectionEvent carCouldLeaveSectionEvent = RoundaboutEventFactory.getInstance().createCarCouldLeaveSectionEvent(
                getRoundaboutModel()
            );
            carCouldLeaveSectionEvent.schedule(this, new TimeSpan(traverseTime, getRoundaboutModel().getModelTimeUnit()));
        } else {
            incrementLostCarCounter();
            car.leaveSystem();
            CarController.addLostCar(this, iCar);
        }
    }

    public void carEnter(Car car, double percentageOfVehicleThatCanLeave){
        this.percentageOfVehicleThatCanLeave = percentageOfVehicleThatCanLeave;
        carEnter(car);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull() {
        // this method is only used by an Intersection object
        // so it is necessary that this always returns false
        // because a RuntimeException is thrown when this is true
        // (check if car can really enter the section is made in
        // method carEnter(Car car))
        return false;
    }

    private RoundaboutSimulationModel getRoundaboutModel() {
        final Model model = getModel();
        if (model instanceof RoundaboutSimulationModel) {
            return (RoundaboutSimulationModel) model;
        } else {
            throw new IllegalArgumentException("Not suitable roundaboutSimulationModel.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {
        if (successful) {
            // remove carPosition of car that has just left
            ICar iCar = CarController.getICar(car);
            carPositions.remove(iCar);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DTO toDTO() {
        return null;
    }

}
