package at.fhv.itm3.s2.roundabout.api.entity;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;

import java.awt.*;
import java.io.*;

public class PedestrianStreetSectionPort implements Serializable{
    // Information in cm
    PedestrianPoint beginOfStreetPort;
    PedestrianPoint endOfStreetPort;


    public PedestrianStreetSectionPort(double startX, double startY, double endX, double endY){
        this(new PedestrianPoint( startX, startY), new PedestrianPoint(endX, endY));
    }

    public PedestrianStreetSectionPort(PedestrianPoint beginOfStreetPort, PedestrianPoint endOfStreetPort){
        this.beginOfStreetPort = beginOfStreetPort;
        this.endOfStreetPort = endOfStreetPort;
    }

    public PedestrianPoint getLocalBeginOfStreetPort() {
        return beginOfStreetPort;
    }

    public PedestrianPoint getLocalEndOfStreetPort() {
        return endOfStreetPort;
    }

    //Overriding method to create a deep copy of an object.
    public PedestrianStreetSectionPort deepCopy() throws Exception
    {
        //Serialization of object
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(this);

        //De-serialization of object
        ByteArrayInputStream bis = new   ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        PedestrianStreetSectionPort copied = (PedestrianStreetSectionPort) in.readObject();
        return copied;
    }
}
