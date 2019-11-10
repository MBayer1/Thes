package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import java.util.List;

public class PedestrianRoutes implements IDTO {
    private List<PedestrianRoute> pedestrianRouteList;

    public List<PedestrianRoute> getPedestrianRoute() {
        return pedestrianRouteList;
    }

    public void setPedestrianRoute(List<PedestrianRoute> pedestrianRouteList) {
        this.pedestrianRouteList = pedestrianRouteList;
    }
}
