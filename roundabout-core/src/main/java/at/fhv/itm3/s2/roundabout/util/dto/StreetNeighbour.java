package at.fhv.itm3.s2.roundabout.util.dto;

import at.fhv.itm3.s2.roundabout.api.util.dto.IDTO;

import javax.xml.bind.annotation.XmlAttribute;


public class StreetNeighbour implements IDTO  {
    private String baseStreet;
    private String baseStreetComponent;
    private String neighbouringStreet1;
    private String neighbouringStreetComponent1;
    private String neighbouringStreet2;
    private String neighbouringStreetComponent2;

    @XmlAttribute
    public String getBaseStreet () { return baseStreet;}

    @XmlAttribute
    public String getNeighbouringStreet1() { return neighbouringStreet1;}

    @XmlAttribute
    public String getNeighbouringStreet2 () { return neighbouringStreet2;}

    @XmlAttribute
    public String getBaseStreetComponent () { return baseStreetComponent;}

    @XmlAttribute
    public String getNeighbouringStreetComponent1() { return neighbouringStreetComponent1;}

    @XmlAttribute
    public String getNeighbouringStreetComponent2 () { return neighbouringStreetComponent2;}



}
