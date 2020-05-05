package at.fhv.itm3.s2.roundabout.api.entity;

public interface IPedestrianBehaviour {

    /**
     * Returns prefered salking speed of {@code this} pedestrian.
     *
     * @return the prefered speed of the pedestrian.
     */
    double getSpeed();

    /**
     * Sets the riding speed of {@code this} pedestrian.
     *
     * @param speed value to be set.
     * @throws IllegalArgumentException when given speed is not greater than or equal to 0.
     */
    void setSpeed(double speed)
            throws IllegalArgumentException;


    /**
     * AccelerationFactor is used to simulate the acceleration time of a pedestrian in case of jam.
     *
     * @return accelerationFactor
     */
    double getAccelerationFactor();

    /**
     * Sets the accelerationFactor which is used to simulate the acceleration time of a pedestrian in case of jam.
     *
     * @param accelerationFactor value to be set.
     */
    void setAccelerationFactor(double accelerationFactor);



    /**
     * Returns preferred min distance to next pedestrian held by {@code this} driver.
     *
     * @return the min distance to next pedestrian of the driver.
     */
    double getMinDistanceToNextPedestrian();


    /**
     * Returns size(radius) of pedestrian held by {@code this} driver.
     *
     * @return size of pedestrian
     */
    double getRadiusOfPedestrian();

    /**
     * Returns actual distance from middle point of pedestrian to another
     *
     * @return sum of pedestiran min gap to obstical and its size
     */
    double calcGapForPedestrian();

}
