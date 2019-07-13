package at.fhv.itm3.s2.roundabout.api.entity;

public interface IPedestrianCountable {

    /**
     * Returns the number of pedestrians that have entered the component.
     *
     * @return the number of pedestrians as int.
     */
    long getNrOfEnteredPedestrians();

    /**
     * Returns the number of cars that have left the component.
     *
     * @return the number of pedestrians as int.
     */
    long getNrOfLeftPedestrians();

    /**
     * Returns the number of pedestrians that were lost in the component.
     *
     * @return the number of pedestrians as int.
     */
    long getNrOfLostPedestrians();
}
