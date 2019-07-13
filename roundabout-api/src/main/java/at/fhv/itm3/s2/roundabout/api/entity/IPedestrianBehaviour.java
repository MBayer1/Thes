package at.fhv.itm3.s2.roundabout.api.entity;

public interface IPedestrianBehaviour {

    /**
     * Returns riding speed of {@code this} pedestrian.
     *
     * @return the speed of the pedestrian.
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

    //TODO Parameters setget

}
