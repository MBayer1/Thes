package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.AbstractProducer;
import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class PedestrianRoute implements IPedestrianRoute {

    private List<PedestrianStreetSectionAndPortPair> route;
    private PedestrianAbstractSource source;
    private Double ratio;

    public PedestrianRoute() {
        this(null, new ArrayList<>(), 1.0);
    }

    public PedestrianRoute(PedestrianAbstractSource source,
                           List<PedestrianStreetSectionAndPortPair>  route,
                           Double ratio) {
        this.route = route;
        this.source = source;
        this.ratio = ratio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PedestrianStreetSectionAndPortPair> getRoute() {
        return Collections.unmodifiableList(route);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getSectionAt(int index) {
        if (index >= route.size()) {
            throw new IllegalArgumentException("Index value for accessing a section in a route is too big.");
        }
        return route.get(index);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getSectionAt(PedestrianStreet pedestrianStreet){
        for (PedestrianStreetSectionAndPortPair routePart : route ){
            if (routePart.getStreetSection().equals(pedestrianStreet)) return routePart;
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IConsumer getNextStreetSectionParameter(IConsumer pedestrianStreet ) {
        boolean hit = false;
        for (PedestrianStreetSectionAndPortPair routePart : route ){
            if ( hit ) return  routePart.getStreetSection();
            if (routePart.getStreetSection().equals(pedestrianStreet)) hit = true;
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionPort getEnterPortOfSectionAt(int index) {
        if (index >= route.size()) {
            throw new IllegalArgumentException("Index value for accessing a section in a route is too big.");
        }
        return route.get(index).getEnterPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionPort getExitPortOfSectionAt(int index) {
        if (index >= route.size()) {
            throw new IllegalArgumentException("Index value for accessing a section in a route is too big.");
        }
        return route.get(index).getExitPort();
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getStartSection() {
        return !isEmpty() ? route.get(0) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianStreetSectionAndPortPair getDestinationSection() {
        return !isEmpty() ? route.get(route.size() - 1) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfSections() {
        return route.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSection(PedestrianStreetSectionAndPortPair section) {
        // Adds as a last element to list.
        route.add(section);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PedestrianAbstractSource getSource() {
        return this.source;
    }


    /**
     * {@inheritDoc}
     */
    public void setSource(AbstractProducer source){
        if (source instanceof AbstractSource)
            this.source = (PedestrianAbstractSource) source;

        else throw new IllegalArgumentException(
                "source is not instance of AbstractSource."
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractSink getSink() {
        final PedestrianStreetSectionAndPortPair destinationSection = this.getDestinationSection();
        if (destinationSection.getStreetSection() instanceof AbstractSink) {
            return (AbstractSink) destinationSection.getStreetSection();
        } else {
            throw new IllegalArgumentException("Destination section is not an instance of Sink.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getRatio() {
        return ratio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return route.isEmpty();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOfSection(PedestrianStreetSectionAndPortPair streetSection) {
        if (!route.contains(streetSection)) {
            throw new IllegalArgumentException("Track must be part of the route");
        }
        return route.indexOf(streetSection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains (PedestrianStreetSectionAndPortPair section) {
        if (!(section.getStreetSection() instanceof Street)) {
           throw new IllegalStateException("All previous IConsumer should be of type Street");
        }
        return IntStream.range(0, route.size()).anyMatch(i -> route.get(i).getStreetSection().equals(section));
    }
}
