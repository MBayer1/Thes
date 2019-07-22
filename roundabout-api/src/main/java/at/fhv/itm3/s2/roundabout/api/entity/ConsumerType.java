package at.fhv.itm3.s2.roundabout.api.entity;

public enum ConsumerType {
    // types for vehicle traffic
    ROUNDABOUT_INLET, ROUNDABOUT_EXIT, ROUNDABOUT_SECTION, STREET_SECTION, INTERSECTION,


    // special types for pedestrians
    PEDESTRIAN_STREET_SECTION, PEDESTRIAN_CROSSING, PEDESTRIAN_WAITING_AREA_CROSSING
}
