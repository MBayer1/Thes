package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import java.util.List;

public class PedestrianConnectors implements IDTO {
    private List<PedestrianConnector> pedestrianConnectorList;

    public List<PedestrianConnector> getConnector() {
        return pedestrianConnectorList;
    }

    public void setConnector(List<PedestrianConnector> pedestrianConnectorList) {
        this.pedestrianConnectorList = pedestrianConnectorList;
    }
}
