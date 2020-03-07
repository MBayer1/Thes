package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.AbstractProducer;
import at.fhv.itm14.trafsim.model.entities.IConsumer;

import java.util.Collection;

public interface IPedestrianRoute {

    /**
     * Returns the route as unmodifiable list of {@link PedestrianStreetSectionAndPortPair}.
     *
     * @return the route as unmodifiable list of {@link PedestrianStreetSectionAndPortPair}.
     */
    Collection<PedestrianStreetSectionAndPortPair> getRoute();

    /**
     * Returns an {@link PedestrianStreetSectionAndPortPair} at the given index of the route.
     *
     * @param index the index the {@link PedestrianStreetSectionAndPortPair} should be returned from the route.
     * @return an {@link PedestrianStreetSectionAndPortPair} located at given index.
     */
    PedestrianStreetSectionAndPortPair getSectionAt(int index);

    /**
     * Returns an {@link IConsumer} at the given index of the route.
     *
     * @param  the current {@link PedestrianStreet} to get the next one that should be returned from the route.
     * @return an {@link PedestrianStreet} located at given next index of the current one.
     */
    public IConsumer getNextStreetSectionParameter(IConsumer pedestrianStreet );

    /**
     * Returns an {@link PedestrianStreetSectionAndPortPair} at the given index of the route.
     *
     * @param index the index the {@link PedestrianStreet} should be returned from the route.
     * @return an {@link PedestrianStreetSectionAndPortPair} located at given index.
     */
    PedestrianStreetSectionAndPortPair getSectionAt(PedestrianStreet pedestrianStreet);

    /**
     * Returns a start {@link PedestrianStreetSectionAndPortPair} of the route.
     *
     * @return start {@link PedestrianStreetSectionAndPortPair} of the route if present, otherwise null.
     */
    PedestrianStreetSectionAndPortPair getStartSection();

    /**
     * Returns a destination {@link PedestrianStreetSectionAndPortPair} of the route.
     *
     * @return start {@link PedestrianStreetSectionAndPortPair} of the route if present, otherwise null.
     */
    PedestrianStreetSectionAndPortPair getDestinationSection();

    /**
     * Returns the number of {@link PedestrianStreetSectionAndPortPair} in the route.
     *
     * @return the number of {@link PedestrianStreetSectionAndPortPair} in the route as int.
     */
    int getNumberOfSections();

    /**
     * Adds a new {@link PedestrianStreetSectionAndPortPair} to the route.
     *
     * @param section the section that is added to the route at the end included its enter and exit port.
     */
    void addSection(PedestrianStreetSectionAndPortPair section);

    /**
     * Checks if there are {@link PedestrianStreetSectionAndPortPair}s in the route defined.
     *
     * @return true if there are {@link PedestrianStreetSectionAndPortPair} in the route, otherwise false.
     */
    boolean isEmpty();

    /**
     * Returns the index in the route of given {@link Street}
     *
     * @param streetSection is the {@link PedestrianStreetSectionAndPortPair} from which the index should be returned
     * @return the index of streetSection in the route
     */
     int getIndexOfSection(PedestrianStreetSectionAndPortPair streetSection);

    /**
     * Returns the {@link AbstractProducer} source of the route
     *
     * @return  the source of the route as {@link AbstractProducer}
     */
    AbstractProducer getSource();

    /**
     * set the {@link AbstractProducer} source of the route
     *
     */
    void setSource(AbstractProducer source);

    /**
     * Returns the {@link PedestrianStreetSectionAndPortPair} sink of the route
     *
     * @return  the sink of the route as {@link PedestrianStreetSectionAndPortPair}
     */
    AbstractSink getSink();

    /**
     * Returns the ratio of the traffic flow compared to the main traffic flow.
     *
     * @return ratio of traffic flow
     */
    Double getRatio();

    /**
    * Checks if there is a specific {@link PedestrianStreetSectionAndPortPair}s already in the route.
    *
    * @return true if there is a specific {@link Street} in the route, otherwise false.
    */
    boolean contains(PedestrianStreetSectionAndPortPair section);


    /**
     * Get the entry port of a {@PedestrianStreetSectionPort} specific {@link PedestrianStreetSectionAndPortPair} in the route.
     *
     * @return the entry port of a specific {@link PedestrianStreetSectionAndPortPair} in the route.
     */
    PedestrianStreetSectionPort getEnterPortOfSectionAt(int index);


    /**
     * Get the exit port of a {@PedestrianStreetSectionPort} specific {@link PedestrianStreetSectionAndPortPair} in the route.
     *
     * @return the exit port of a specific {@link PedestrianStreetSectionAndPortPair} in the route.
     */
    PedestrianStreetSectionPort getExitPortOfSectionAt(int index);
}
