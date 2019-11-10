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
     * Calculates the the next subgoal {@link Point} considered.
     *
     * @return the next subgoal.
     */
    Point getNextSubGoal();


    /**
     * checks if the the next subgoal {@link Point} is reached
     *
     * @return true when subgoal is reached, otherwise false
     */
    boolean nextSubGoalIsReached();


    /**
     * Calculates the time the pedestrian needs to traverse to reach the next subgoal {@link Point}.
     *
     * @param  currentPosition contains current coordinates of pedestrian at {@PedestrianStreetSection}
     * @return the traverse time in model time units.
     */
    double getTimeToNextSubGoal(Point currentPosition);


    /**
     * Returns actual current Position of {@code this} pedestrian in current {@Link PedestrianStreet}.
     *
     * @return the coordinates of the center of the pedestrian.
     */
    Point getCurrentPosition();


    /**
     * Returns (reference) pedestrian driver behavior {@link IDriverBehaviour}.
     *
     * @return instance of {@link IDriverBehaviour}.
     */
    IPedestrianBehaviour getPedestrianBehaviour();

    /**
     * Returns predefined pedestrian route.
     *
     * @return pedestrian route in form of {@link IPedestrianRoute}.
     */
    IPedestrianRoute getRoute();

    /**
     * Return a reference to a current {@link PedestrianStreet} present in pedestrian route,
     * where pedestrian currently belongs to.
     *
     * @return reference to {@link PedestrianStreet} where pedestrian is currently located.
     */
    PedestrianStreetSectionPortPair getCurrentSection();

    /**
     * Returns reference to the next {@link PedestrianStreet} scheduled
     * in pedestrian pre-calculated route.
     *
     * @return reference to next {@link PedestrianStreet}.
     */
    PedestrianStreetSectionPortPair getNextSection();

    /**
     * Returns reference to the section after the next {@link PedestrianStreetSectionPortPair} scheduled
     * in pedestrian pre-calculated route.
     *
     * @return  reference to section after next {@link PedestrianStreetSectionPortPair}
     */
    PedestrianStreetSectionPortPair getSectionAfterNextSection();

    /**
     * Return the last available section specified in pedestrian route.
     *
     * @return reference to last instance of {@link PedestrianStreet} in route.
     */
    PedestrianStreetSectionPortPair getDestination();


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
     * time spent on the pedestrian area.
     */
    void enterPedestrianArea();

    /**
     * Used for statistical values. Stops the stopwatch that is used for
     * determining the time spent on the pedestrian area.
     */
    void leavePedestrianArea();

    /**
     * Used for statistical values. Returns the mean time spend on the pedestrian area.
     *
     * @return  the mean time the pedestrian used to pass a street crossing as model time units
     */
    double getMeanStreetAreaPassTime();

    /**
     * Used for statistical values. Starts a stopwatch to determine the
     * time spent on the pedestrian crossing.
     */
    void enterPedestrianCrossing();


    /**
     * Used for statistical values. Stops the stopwatch that is used for
     * determining the time spent on the pedestrian crossing.
     */
    void leavePedestrianCrossing();

    /**
     * Used for statistical values. Returns the mean time used for crossing
     * a pedestrian crossing.
     *
     * @return  the mean time the pedestrian used to pass a street crossing as model time units
     */
    double getMeanStreetCrossingPassTime();


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