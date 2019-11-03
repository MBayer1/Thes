package at.fhv.itm3.s2.roundabout.api.entity;

import desmoj.core.simulator.Model;

import java.util.UUID;

public abstract class PedestrianAbstractSink extends PedestrianStreet implements IPedestrianCountable {

    public PedestrianAbstractSink(String id, Model model, String string, boolean bln) {
        super(id, model, string, bln);
    }

    public PedestrianAbstractSink(Model model, String string, boolean bln) {
        this(UUID.randomUUID().toString(), model, string, bln);
    }

    /**
     * Used for statistical values. Returns the mean time used for crossing
     * a roundabout.
     *
     * @return  the mean time of all pedestrians used to pass a roundabout as model time units
     */
    public abstract double getMeanPassTimeForEnteredPedestrians();

    /**
     * Used for statistical values. Returns the mean time the pedestrians spent in the system.
     *
     * @return  the mean time of all pedestrians spent in the system as model time units
     */
    public abstract double getMeanTimeSpentInSystemForEnteredPedestrians();

    /**
     *  Used for statistical values. Returns the mean time spent waiting per stop.
     *
     * @return  the mean time of all pedestrians spent waiting per stop as model time units
     */
    public abstract double getMeanWaitingTimePerStopForEnteredPedestrians();

    /**
     * Used for statistical values. Returns the mean number of stops the pedestrians had to make.
     *
     * @return  the mean number of stops the pedestrians had to made as int
     */
    public abstract double getMeanStopCountForEnteredPedestrians();

    /**
     * Used for statistical values. Returns the mean time used for passing an intersection.
     *
     * @return  the mean time the pedestrians used for passing an intersection as model time units
     */
    public abstract double getMeanIntersectionPassTimeForEnteredPedestrians();
}
