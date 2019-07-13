package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm14.trafsim.model.entities.IProducer;

import java.awt.*;

public interface IPedestrian {

    /**
     * Returns the last update time.
     * This value will be changed every time pedestrian attributes will be somehow modified.
     *
     * @return last update time.
     */
    double getLastUpdateTime();

    /**
     * Sets the last update time.
     *
     * @param lastUpdateTime time value to be set.
     * @throws IllegalArgumentException when given time is not greater than 0.
     */
    void setLastUpdateTime(double lastUpdateTime)
    throws IllegalArgumentException;

    /**
     * Calculates the time the pedestrian needs to traverse the current {@link Street} it is standing on.
     *
     * @return the traverse time in model time units.
     */
    double getTimeToTraverseCurrentSection();

    /**
     * Calculates the time the pedestrian needs to traverse a given {@link Street}.
     *
     * @param section the {@link Street} we are interested in how long the pedestrian needs to traverse it.
     * @return the traverse time in model time units.
     */
    double getTimeToTraverseSection(IConsumer section);

    /**
     * Calculates the time the pedestrian needs until it has moved away from its current spot.
     *
     * @return the transition time in model time units.
     */
    double getTransitionTime();

    /**
     * Returns actual current Position of {@code this} pedestrian in current {@Link PedestrianStreet}.
     *
     * @return the coordinates of the center of the pedestrian.
     */
    public Point getCurrentPosition();
    /**
     * Returns (reference) pedestrian driver behavior {@link IDriverBehaviour}.
     *
     * @return instance of {@link IDriverBehaviour}.
     */
    IPedestrianBehaviour getPedestrianBehaviour();

    /**
     * Returns predefined pedestrian route.
     *
     * @return pedestrian route in form of {@link IRoute}.
     */
    IRoute getRoute();

    /**
     * Returns a reference to the last {@link IProducer} present in the pedestrian route,
     * where the pedestrian currently belongs to.
     *
     * @return reference to {@link IProducer} where pedestrian was last located
     */
    IConsumer getLastSection();

    /**
     * Return a reference to a current {@link Street} present in pedestrian route,
     * where pedestrian currently belongs to.
     *
     * @return reference to {@link Street} where pedestrian is currently located.
     */
    IConsumer getCurrentSection();

    /**
     * Pedestrian will be logically traversed to next (following) {@link Street} in predefined route.
     */
    void traverseToNextSection();

    /**
     * Returns reference to the next {@link Street} scheduled
     * in pedestrian pre-calculated route.
     *
     * @return reference to next {@link Street}.
     */
    IConsumer getNextSection();

    /**
     * Returns reference to the section after the next {@link IConsumer} scheduled
     * in pedestrian pre-calculated route.
     *
     * @return  reference to section after next {@link IConsumer}
     */
    IConsumer getSectionAfterNextSection();

    /**
     * Return the last available section specified in pedestrian route.
     *
     * @return reference to last instance of {@link Street} in route.
     */
    IConsumer getDestination();

    /**
     * Used for statistical values. Starts a stopwatch to determine the
     * overall time spent in the system. Delegates the call to the Trafsim pedestrian object.
     */
    void enterSystem();

    /**
     * Used for statistical values. Stops the stopwatch that is used for
     * determining the overall time spent in the system. Delegates the call to the Trafsim pedestrian object.
     *
     * @return  the time spent in the system as model time units
     */
    double leaveSystem();

    /**
     * Used for statistical values. Starts a stopwatch to determine the
     * time spent on the roundabout.
     */
    void enterRoundabout();

    /**
     * Used for statistical values. Stops the stopwatch that is used for
     * determining the time spent on the roundabout.
     */
    void leaveRoundabout();

    /**
     * Used for statistical values. Returns the mean time used for crossing
     * a roundabout.
     *
     * @return  the mean time the pedestrian used to pass a roundabout as model time units
     */
    double getMeanRoundaboutPassTime();

    /**
     * Used for statistical values. Returns the number of roundabouts crossed.
     *
     * @return  the number of roundabouts crossed as int
     */
    long getRoundaboutPassedCount();

    /**
     * Used for statistical values. Starts a stopwatch that is used for
     * determining the time spent on an intersection. Delegates the call to the Trafsim pedestrian object.
     */
    void enterIntersection();

    /**
     * Used for statistical values. Stops the stopwatch that is used for
     * determining the time spent on the intersection. Delegates the call to the Trafsim pedestrian object.
     */
    void leaveIntersection();

    /**
     * Used for statistical values. Starts a stopwatch that is used for
     * determining the time spent waiting. Delegates the call to the Trafsim pedestrian object.
     */
    void startWaiting();

    /**
     * Used for statistical values. Stops the stopwatch that is used for
     * determining the time spent waiting. Delegates the call to the Trafsim pedestrian object.
     */
    void stopWaiting();

    /**
     * Used for statistical values. Checks if the pedestrian is currently waiting by checking if the
     * corresponding stopwatch is running. Delegates the call to the Trafsim pedestrian object.
     *
     * @return  true if the corresponding stopwatch is running (startWaiting was called before withouth calling
     * stopWaiting afterwards), else false
     */
    boolean isWaiting();

    /**
     * Used for statistical values. Returns the time the pedestrian has spent in the system.
     * Delegates the call to the Trafsim pedestrian object.
     *
     * @return  the time the pedestrian has spent in the system as model time units
     */
    double getTimeSpentInSystem();

    /**
     *  Used for statistical values. Returns the mean time spent waiting.
     *  Delegates the call to the Trafsim pedestrian object.
     *
     * @return  the mean time the pedestrian spent waiting per stop as model time units
     */
    double getMeanWaitingTime();

    /**
     * Used for statistical values. Returns the number of stops the pedestrian has to made.
     * Delegates the call to the Trafsim pedestrian object.
     *
     * @return  the number of stops the pedestrian has to made as int
     */
    long getStopCount();

    /**
     * Used for statistical values. Returns the number of intersections the pedestrian crossed.
     * Delegates the call to the Trafsim pedestrian object.
     *
     * @return  the number of intersections the pedestrian crossed as int
     */
    long getIntersectionPassedCount();

    /**
     * Used for statistical values. Returns the mean time used for passing and intersection.
     * Delegates the call to the Trafsim pedestrian object.
     *
     * @return  the mean time the pedestrian used for passing an intersection as model time units
     */
    double getMeanIntersectionPassTime();

    /**
     * Returns the distance the pedestrian can lay back in a given time.
     *
     * @param time  the time as model time unit
     * @return      the distance as model length unit
     */
    double getCoveredDistanceInTime(double time);

    /**
     *
     */
    //TODO
    double GetSocialForceVector();
}
