package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import java.util.List;

public class StreetNeighbours implements IDTO {
    
    private List<StreetNeighbour> neighbourList;

    public List<StreetNeighbour> getNeighbourList() {
        return neighbourList;
    }

    public void setNeighbourList(List<StreetNeighbour> neighbourList) {
        this.neighbourList = neighbourList;
    }
}
