package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianAbstractSource;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreet;
import at.fhv.itm3.s2.roundabout.event.RoundaboutEventFactory;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Model;
import java.util.UUID;

public class PedestrianSource extends PedestrianAbstractSource {

    private PedestrianStreet connectedStreet;
    private RoundaboutSimulationModel model;
    protected RoundaboutEventFactory roundaboutEventFactory;
    private double generateRatio,
            xPositionStart, yPositionStart,
            xPositionEnd, yPositionEnd;

    public PedestrianSource(Model model, String description, boolean showInTrace, PedestrianStreet connectedStreet) {
        this(UUID.randomUUID().toString(), null, model, description, showInTrace, connectedStreet);
    }

    public PedestrianSource(String id, Double generatorExpectation,
                            Model model, String description,
                            boolean showInTrace, PedestrianStreet connectedStreet) {
        super(id, generatorExpectation, model, description, showInTrace);
        this.connectedStreet = connectedStreet;
        this.roundaboutEventFactory = RoundaboutEventFactory.getInstance();
        this.addXPositonStart(xPositionStart);
        this.addXPositonEnd(xPositionEnd);
        this.addYPositonStart(yPositionStart);
        this.addYPositonEnd(yPositionEnd);

        if (model instanceof RoundaboutSimulationModel) {
            this.model = (RoundaboutSimulationModel)model;
        } else {
            throw new IllegalArgumentException("No suitable model for RoundaboutSource");
        }
    }

    public void startGeneratingPedestrians(double afterModelTimeUnits) {
        //PedestrianGenerateEvent event = this.roundaboutEventFactory.createPedestrianGenerateEvent(model);
        //event.schedule(this, new TimeSpan(afterModelTimeUnits)); //TODO
    }

    public PedestrianStreet getConnectedStreet() {
        return connectedStreet;
    }

    public double getGenerateRatio() {
        return Double.compare(generateRatio, 0.0) == 0 ? 1.0 : generateRatio;
    }

    public void addGenerateRatio(Double ratio) {
        this.generateRatio += ratio;
    }


    public double getXPositionStart() {
        return this.xPositionStart;
    }

    public void addXPositonStart(Double xPositionStart) {
        this.xPositionStart = xPositionStart;
    }

    public double getXPositionEnd() {
        return this.xPositionEnd;
    }

    public void addXPositonEnd(Double xPositionEnd) {
        this.xPositionEnd = xPositionEnd;
    }

    public double getYPositionStart() {
        return this.yPositionStart;
    }

    public void addYPositonStart(Double yPositionStart) {
        this.yPositionStart = yPositionStart;
    }

    public double getYPositionEnd() {
        return this.yPositionEnd;
    }

    public void addYPositonEnd(Double yPositionEnd) {
        this.yPositionEnd += yPositionEnd;
    }

    /**
     * needed for integration in to the very first basis framework
     */
    @Override
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DTO toDTO() {
        return null;
    }
}
