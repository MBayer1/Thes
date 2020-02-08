package at.fhv.itm3.s2.roundabout.controller;

import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.simulator.Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PedestrianRouteController {

    private final RoundaboutSimulationModel model;
    private static PedestrianRouteController instance;

    private Map<PedestrianAbstractSource, List<IPedestrianRoute>> routes;
    private List<PedestrianAbstractSource> sources;

    /**
     * Gets all available routes.
     * @return All routes.
     */
    public Map<PedestrianAbstractSource, List<IPedestrianRoute>> getRoutes() {
        return routes;
    }

    /**
     * Sets all possible routes.
     * @param routes The routes which should be available to choose from.
     */
    public void setRoutes(Map<PedestrianAbstractSource, List<IPedestrianRoute>> routes) {
        this.routes = routes;
    }

    /**
     * Gets all sources.
     * @return A list of sources a route can start from.
     */
    public List<PedestrianAbstractSource> getSources() {
        return sources;
    }

    /**
     * Sets all possible sources.
     * @param sources Sets a list of sources, from where a route could start.
     */
    public void setSources(List<PedestrianAbstractSource> sources) {
        this.sources = sources;
    }

    /**
     * Returns a singleton of {@link PedestrianRouteController}.
     *
     * @param model the model the RouteController and its {@link Street}s are part of.
     * @return the singleton RouteController object.
     */
    public static PedestrianRouteController getInstance(RoundaboutSimulationModel model) {
        if (instance == null) {
            instance = new PedestrianRouteController(model);
        }
        return instance;
    }

    /**
     * Private constructor for {@link PedestrianRouteController}. Use getInstance(...) instead.
     *
     * @param model the model the {@link PedestrianRouteController} and its {@link Street}s are part of.
     * @throws IllegalArgumentException when the given model is not of type {@link RoundaboutSimulationModel}.
     */
    private PedestrianRouteController(Model model)
            throws IllegalArgumentException {
        if (model != null && model instanceof RoundaboutSimulationModel) {
            this.model = (RoundaboutSimulationModel) model;
        } else {
            throw new IllegalArgumentException("No suitable model given over.");
        }

        this.routes = new HashMap<>();
        this.sources = new LinkedList<>();
    }

    public IPedestrianRoute getRandomRoute(PedestrianAbstractSource source) {
        if (this.routes.isEmpty()) {
            throw new IllegalStateException("Routes must not be empty.");
        }

        final List<IPedestrianRoute> routes = this.routes.get(source);

        final double totalRatio = routes.stream().mapToDouble(IPedestrianRoute::getRatio).sum();
        final double randomRatio = model.getRandomRouteRatioFactor() * totalRatio;

        double sumRatio = 0;
        for (IPedestrianRoute route : routes) {
            if (route.getRatio() > 0) {
                sumRatio += route.getRatio();
            }

            if (sumRatio > randomRatio) {
                return route;
            }
        }

        throw new IllegalStateException("No route was chosen for source: " + source);
    }
}