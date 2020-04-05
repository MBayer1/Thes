package at.fhv.itm3.s2.roundabout.api.entity;

import java.awt.*;
import java.io.*;

public class PedestrianStreetSectionPort implements Serializable{
    // Information in cm
    Point beginOfStreetPort;
    Point endOfStreetPort;


    public PedestrianStreetSectionPort(Integer startX, Integer startY, Integer endX, Integer endY){
        this(new Point( startX, startY), new Point(endX, endY));
    }

    public PedestrianStreetSectionPort(Point beginOfStreetPort, Point endOfStreetPort){
        this.beginOfStreetPort = beginOfStreetPort;
        this.endOfStreetPort = endOfStreetPort;
    }

    public Point getGlobalBeginOfStreetPort() {
        return beginOfStreetPort;
    }

    public Point getGlobalEndOfStreetPort() {
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
