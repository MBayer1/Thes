package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm14.trafsim.model.entities.intersection.Intersection;
import desmoj.core.simulator.Model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IModelStructure {

    /**
     * Add a collection of street connectors for vehicle-streets to the structure.
     * Structure setup needs to be done before.
     *
     * @param streetConnector street connectors to be added
     */
    void addStreetConnectors(Collection<? extends IStreetConnector> streetConnector);

    /**
     * Add a collection of street connectors for pedestrians-street for the structure.
     * Structure setup needs to be done before.
     *
     * @param {@Link IPedestrianStreetConnector} street connectors to be added
     */
    void addPedestrianStreetConnectors(Collection<? extends IPedestrianStreetConnector> pedestrianStreetConnector);

    /**
     * Add a collection of routes for vehicle to the structure.
     * Structure setup needs to be done before.
     *
     * @param routes routes to be added
     */
    void addRoutes(Collection<? extends IRoute> routes);

    /**
     * Add a collection of routes for pedestrians to the structure.
     * Structure setup needs to be done before.
     *
     * @param routes routes to be added
     */
    void addPedestrianRoutes(Collection<? extends IPedestrianRoute> routes);

    /**
     * Add a collection of intersections to the structure.
     * Structure setup needs to be done before.
     *
     * @param intersections intersections to be added
     */
    void addIntersections(Collection<? extends Intersection> intersections);

    /**
     * Add a collection of streets for vehicle to the structure.
     * Structure setup needs to be done before.
     *
     * @param streets streets to be added
     */
    void addStreets(Collection<? extends Street> streets);

    /**
     * Add a collection of streets for pedestrian to the structure.
     * Structure setup needs to be done before.
     *
     * @param streets streets to be added
     */
    void addPedestrianStreets(Collection<? extends PedestrianStreet> streets);

    /**
     * Adds a collection of sinks for vehicles to the structure.
     *
     * @param sinks The sinks to be added.
     */
    void addSinks(Collection<? extends AbstractSink> sinks);

    /**
     * Adds a collection of sinks for pedestrians to the structure.
     *
     * @param sinks The sinks to be added.
     */
    void addPedestrianSinks(Collection<? extends PedestrianAbstractSink> sinks);

    /**
     * Adds a collection of sources for vehicles to the structure.
     *
     * @param sources The source to be added.
     */
    void addSources(Collection<? extends AbstractSource> sources);

    /**
     * Adds a collection of sources for pedestrian to the structure.
     *
     * @param sources The source to be added.
     */
    void addPedestrianSources(Collection<? extends PedestrianAbstractSource> sources);


    /**
     * Add a configuration parameter to the structure.
     * Known parameters: //TODO full list of parameters
     *
     * @param key   key of the parameter
     * @param value value of the parameter
     */
    void addParameter(String key, String value);

    /**
     * Get all street connectors for vehicle of the structure.
     *
     * @return street connectors of structure
     */
    Set<IStreetConnector> getStreetConnectors();

    /**
     * Get all street connectors for pedestrians of the structure.
     *
     * @return street connectors of structure
     */
    Set<IPedestrianStreetConnector> getPedestrianStreetConnectors();

    /**
     * Get all vehicle-routes of the structure.
     *
     * @return routes of structure
     */
    Map<AbstractSource, List<IRoute>> getRoutes();

    /**
     * Get all pedestrian-routes of the structure.
     *
     * @return routes of structure
     */
    Map<PedestrianAbstractSource, List<IPedestrianRoute>> getPedestrianRoutes();

    /**
     * Get all vehicle-intersections of the structure.
     *
     * @return intersections of structure
     */
    Set<Intersection> getIntersections();

    /**
     * Get all vehicle-streets of the structure.
     *
     * @return streets of structure
     */
    Set<Street> getStreets();

    /**
     * Get all pedestrian-streets of the structure.
     *
     * @return streets of structure
     */
    Set<PedestrianStreet> getPedestrianStreets();

    /**
     * Gets all vehicle-sinks from the structure.
     */
    Set<AbstractSink> getSinks();

    /**
     * Gets all pedestrian-sinks from the structure.
     */
    Set<PedestrianAbstractSink> getPedestrianSinks();

    /**
     * Gets all vehicle-sources from the structure.
     */
    Set<AbstractSource> getSources();

    /**
     * Gets all pedestrian-sources from the structure.
     */
    Set<PedestrianAbstractSource> getPedestrianSources();

    /**
     * Gets all roundabout inlets as {@link Street}
     */
    Set<Street> getRoundaboutInlets();

    /**
     * Get all parameters of the structure.
     *
     * @return parameters of structure
     */
    Map<String, String> getParameters();

    /**
     * Returns value associated with the given key.
     *
     * @param key key of the parameter.
     * @return value of the parameter.
     */
    String getParameter(String key);

    /**
     * Get simulation model of structure.
     *
     * @return simulation model of structure
     */
    Model getModel();
}
