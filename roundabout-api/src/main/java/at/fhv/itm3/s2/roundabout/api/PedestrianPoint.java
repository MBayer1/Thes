package at.fhv.itm3.s2.roundabout.api;

import java.io.Serializable;

public class PedestrianPoint implements Serializable{
    double x; //cm
    double y; //cm

    public PedestrianPoint () {
        setX(0);
        setY(0);
    }

    public PedestrianPoint (PedestrianPoint point) {
        setX(point.getX());
        setY(point.getY());
    }

    public PedestrianPoint (double x, double y) {
        setX(x);
        setY(y);
    }

    public void setLocation (double x, double y) {
        setX(x);
        setY(y);
    }

    public void setLocation (PedestrianPoint point) {
        setX(point.getX());
        setY(point.getY());
    }

    public void setX(double x) {
        this.x = Math.round(x * 100.0) / 100.0;
    }

    public void setY(double y) {
        this.y = Math.round(y * 100.0) / 100.0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
