package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm14.trafsim.model.entities.IConsumer;

public class StreetNeighbour {
    private final IConsumer neighbouringStreet1;
    private final IConsumer neighbouringStreet2;

    public StreetNeighbour(IConsumer neighbouringStreet1){
        this(neighbouringStreet1, null);
    }


    public StreetNeighbour(IConsumer neighbouringStreet1, IConsumer neighbouringStreet2){
        this.neighbouringStreet1 = neighbouringStreet1;
        this.neighbouringStreet2 = neighbouringStreet2;
    }

    public IConsumer getNeighbouringStreet1() {
        return neighbouringStreet1;
    }

    public IConsumer getNeighbouringStreet2() {
        return neighbouringStreet2;
    }
}
