package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import java.util.List;

public class PedestrianConnectors implements IDTO {
    private List<PedestrianConnector> pedestrianConnectorList;

    public List<PedestrianConnector> getPedestrianConnector() {
        return pedestrianConnectorList;
    }

    public void setPedestrianConnector(List<PedestrianConnector> pedestrianConnectorList) {
        this.pedestrianConnectorList = pedestrianConnectorList;
    }
}
