package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.Car;
import at.fhv.itm14.trafsim.model.events.CarDepartureEvent;
import at.fhv.itm14.trafsim.persistence.model.DTO;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianAbstractSource;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreet;
import at.fhv.itm3.s2.roundabout.event.PedestrianEventFactory;
import at.fhv.itm3.s2.roundabout.event.PedestrianGenerateEvent;
import at.fhv.itm3.s2.roundabout.event.RoundaboutEventFactory;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.awt.*;
import java.util.UUID;

public class PedestrianSource extends PedestrianAbstractSource {

    private PedestrianStreetSection connectedStreet;
    private RoundaboutSimulationModel model;
    protected PedestrianEventFactory pedestrianEventFactory;
    private double generateRatio;
    private PedestrianPoint globalCoordinate;


    public PedestrianSource(Model model, String description, boolean showInTrace, PedestrianStreetSection connectedStreet) {
        this(UUID.randomUUID().toString(), null, model, description, showInTrace, connectedStreet);
    }

    public PedestrianSource(String id, Double generatorExpectation,
                            Model model, String description,
                            boolean showInTrace, PedestrianStreetSection connectedStreet) {
        super(id, generatorExpectation, model, description, showInTrace);
        this.connectedStreet = connectedStreet;
        this.pedestrianEventFactory = PedestrianEventFactory.getInstance();

        if (model instanceof RoundaboutSimulationModel) {
            this.model = (RoundaboutSimulationModel)model;
        } else {
            throw new IllegalArgumentException("No suitable model for RoundaboutSource");
        }
    }

    public void startGeneratingPedestrians(double afterModelTimeUnits) {
        PedestrianGenerateEvent event = this.pedestrianEventFactory.createPedestrianGenerateEvent(model);
        event.schedule(this, new TimeSpan(afterModelTimeUnits));
    }

    public PedestrianStreetSection getConnectedStreet() {
        return connectedStreet;
    }

    public double getGenerateRatio() {
        return Double.compare(generateRatio, 0.0) == 0 ? 1.0 : generateRatio;
    }

    public void addGenerateRatio(Double ratio) {
        this.generateRatio += ratio;
    }

    public void setGlobalCoordinate (PedestrianPoint globalCoordinate) { this.globalCoordinate = globalCoordinate; }

    public PedestrianPoint getGlobalCoordinate() {
        return globalCoordinate;
    }

    /**
     * needed for integration in to the very first basis framework
     */
    @Override
    public void carDelivered(CarDepartureEvent carDepartureEvent, Car car, boolean successful) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DTO toDTO() {
        return null;
    }
}
