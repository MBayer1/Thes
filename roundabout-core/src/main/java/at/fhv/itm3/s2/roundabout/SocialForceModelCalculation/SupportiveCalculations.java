package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreetSectionPort;

import javax.vecmath.Vector2d;
import java.awt.*;

public class SupportiveCalculations {
    
    public Vector2d getVector(double startPointX, double startPointY, double endPointX, double endPointY){
            return new Vector2d(endPointX-startPointX, endPointY-startPointY);
    }

    public Vector2d getUnitVector( Vector2d vector) {
        return  getUnitVector(vector.getX(), vector.getY());
    }

    public Vector2d getUnitVector( double vectorX,	double vectorY)
    {
        double returnXTmp = vectorX / Math.sqrt(Math.pow(vectorX, 2) + Math.pow(vectorY, 2));
        double returnYTmp = vectorY / Math.sqrt(Math.pow(vectorX, 2) + Math.pow(vectorY, 2));
        return new Vector2d(returnXTmp, returnYTmp);
    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(Point intersection,
                                                                     Point lineStart1,
                                                                     double lineEndX1, double lineEndY1,
                                                                     Point lineStart2,
                                                                     Point lineEnd2
    ) {
        return checkLinesIntersectionByCoordinates_WithinSegment(intersection,
                lineStart1.getX(), lineStart1.getY(), lineEndX1, lineEndY1,
                lineStart2.getX(), lineStart2.getY(), lineEnd2.getX(), lineEnd2.getY());

    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(Point intersection,
                                                                     Point lineStart1,
                                                                     Point lineEnd1,
                                                                     Point lineStart2,
                                                                     Point lineEnd2
    ) {
        return checkLinesIntersectionByCoordinates_WithinSegment(intersection,
                lineStart1.getX(), lineStart1.getY(), lineEnd1.getX(), lineEnd1.getY(),
                lineStart2.getX(), lineStart2.getY(), lineEnd2.getX(), lineEnd2.getY());

    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(Point intersection,
                                                                     double lineStartX1, double lineStartY1,
                                                                     double lineEndX1, double lineEndY1,
                                                                     Point lineStart2,
                                                                     Point lineEnd2
    ) {
        return checkLinesIntersectionByCoordinates_WithinSegment(intersection,
                        lineStartX1, lineStartY1,
                        lineEndX1, lineEndY1,
                lineStart2.getX(), lineStart2.getY(), lineEnd2.getX(), lineEnd2.getY());

    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(Point intersection,
                                                                     double lineStartX1, double lineStartY1,
                                                                     double lineEndX1, double lineEndY1,
                                                                     double lineStartX2, double lineStartY2,
                                                                     double lineEndX2, double lineEndY2
    ){

        if((lineStartX1 == lineStartX2) && (lineStartY1 == lineStartY2) &&
                (lineEndX1 == lineEndX2) && (lineEndY1 == lineEndY2)){
            throw new IllegalArgumentException("Lines are identical.");
        }

        //linear equation: y=m*x+d -> note special case: parallel to y-axis     -> y(x) = const, always
        //1. set linear equation in linear equation -> m1*x+d1 = m2*x+d2        -> x = (d2-d1)/(m1-m2)
        double dSlope1 = (lineEndY1-lineStartY1)/(lineEndX1-lineStartX1);	        //m1
        double dYIntercept1 = lineEndY1-(lineEndX1*dSlope1);						//d1
        double dSlope2 = (lineEndY2-lineStartY2)/(lineEndX2-lineStartX2);	        //m2
        double dYIntercept2 = lineEndY2-(lineEndX2*dSlope2);						//d2

        // both parallel y-axis
        if( (Double.isInfinite(dSlope1) && Double.isInfinite(dSlope2)) ||
             ( (Double.isInfinite(dSlope1) || Double.isInfinite(dSlope2)) &&
              (almostEqual(dSlope1, 0) || almostEqual(dSlope2, 0)))) {
            return false;
        }

        //check if parallel to y-axis
        if (almostEqual(lineEndX1,lineStartX1)){
            intersection.setLocation(lineEndX1, lineEndX1* dSlope2 + dYIntercept2);
        }
        if (almostEqual(lineEndX2,lineStartX2)){
            intersection.setLocation(lineEndX2, lineEndX2* dSlope1 + dYIntercept1);
        }

        // if the slope is the same the lines are parallel and never cross another
        if(almostEqual(dSlope1, dSlope2)){
            return false;
        }

        double tmpX = (dYIntercept2-dYIntercept1)/(dSlope1-dSlope2);
        intersection.setLocation(tmpX, tmpX* dSlope1 + dYIntercept1);

        //Intersection have to be on the line segment
        if((((intersection.getX()>=lineStartX1) && (intersection.getX()<=lineEndX1)) ||
                ((intersection.getX()<=lineStartX1) && (intersection.getX()>=lineEndX1))) &&
                (((intersection.getX()>=lineStartX2) && (intersection.getX()<=lineEndX2)) ||
                        ((intersection.getX()<=lineStartX2) && (intersection.getX()>=lineEndX2)))){
            return true;
        }

        return false;
    }

    public boolean almostEqual(double dVal1, double dVal2){
        return almostEqual(dVal1,dVal2, 10e-8);
    }

    public boolean val1Lower(double dVal1, double dVal2){
        return val1Lower(dVal1,dVal2, 10e-8);
    }

    public boolean val1Bigger(double dVal1, double dVal2){
        return val1Bigger(dVal1,dVal2, 10e-8);
    }

    public boolean val1Lower(double dVal1, double dVal2, double SFM_DegreeOfAccuracy) {
        return ((dVal1 < dVal2) || almostEqual(dVal1,dVal2, SFM_DegreeOfAccuracy));
    }

    public boolean val1Bigger(double dVal1, double dVal2, double SFM_DegreeOfAccuracy) {
        return ((dVal1 > dVal2) || almostEqual(dVal1,dVal2, SFM_DegreeOfAccuracy));
    }

    public boolean val1LowerOrAlmostEqual(double dVal1, double dVal2){
        return val1LowerOrAlmostEqual(dVal1,dVal2, 10e-8);
    }

    public boolean val1BiggerOrAlmostEqual(double dVal1, double dVal2){
        return val1BiggerOrAlmostEqual(dVal1,dVal2, 10e-8);
    }

    public boolean val1LowerOrAlmostEqual(double dVal1, double dVal2, double SFM_DegreeOfAccuracy) {
        return ((dVal1 < dVal2) || almostEqual(dVal1,dVal2, SFM_DegreeOfAccuracy));
    }

    public boolean val1BiggerOrAlmostEqual(double dVal1, double dVal2, double SFM_DegreeOfAccuracy) {
        return ((dVal1 > dVal2) || almostEqual(dVal1,dVal2, SFM_DegreeOfAccuracy));
    }

    public boolean almostEqual(double dVal1, double dVal2, double SFM_DegreeOfAccuracy)
    {
        return (Math.round(Math.abs(dVal1-dVal2)) < SFM_DegreeOfAccuracy);

    }

    public Point getLinesIntersectionByCoordinates(	     PedestrianStreetSectionPort port,
                                                         double dLineStartX2, double dLineStartY2,
                                                         double dLineEndX2, double dLineEndY2) {
        return getLinesIntersectionByCoordinates( port.getLocalBginOfStreetPort().getX(), port.getLocalBginOfStreetPort().getY(),
                port.getLocalEndOfStreetPort().getX(), port.getLocalEndOfStreetPort().getY(),
                dLineStartX2, dLineStartY2, dLineEndX2, dLineEndY2);
    }

    public Point getLinesIntersectionByCoordinates(	     double dLineStartX1, double dLineStartY1,
                                                         double dLineEndX1, double dLineEndY1,
                                                         double dLineStartX2, double dLineStartY2,
                                                         double dLineEndX2, double dLineEndY2) {
        Vector2d returnIntersection = new Vector2d();
        if(getLinesIntersectionByCoordinates(	returnIntersection,
                dLineStartX1, dLineStartY1, dLineEndX1, dLineEndY1,
                dLineStartX2, dLineStartY2, dLineEndX2, dLineEndY2)){
            return new Point((int)returnIntersection.getX(), (int)returnIntersection.getY());
        }
        return null;
        //throw new IllegalArgumentException("Two lines do not have any intersection");
    }

    public boolean getLinesIntersectionByCoordinates(	Vector2d returnIntersection,
                                            double dLineStartX1, double dLineStartY1,
                                            double dLineEndX1, double dLineEndY1,
                                            double dLineStartX2, double dLineStartY2,
                                            double dLineEndX2, double dLineEndY2)
    {
        double dReturnX, dReturnY;

        if (almostEqual(dLineStartX1, dLineStartX2) && almostEqual(dLineStartY1, dLineStartY2) &&
                almostEqual(dLineEndX1, dLineEndX2) && almostEqual(dLineEndY1, dLineEndY2)) {
            // exactly the same line
            return false;
        }

        // linear equation: y=m*x+d -> note spacial case: parallel to y-axis -> y(x) = const, always
        // 1. set linear equation in linear equation -> m1*x+d1 = m2*x+d2  -> x = (d2-d1)/(m1-m2)
        double dSlope1 = (dLineEndY1 - dLineStartY1) / (dLineEndX1 - dLineStartX1);			// m1
        //if(Double.isInfinite(dSlope1)) return false;
        double dYIntercept1 = dLineEndY1 - (dLineEndX1 * dSlope1);							// d1
        double dSlope2 = (dLineEndY2 - dLineStartY2) / (dLineEndX2 - dLineStartX2);			// m2
        //if(Double.isInfinite(dSlope2)) return false;
        double dYIntercept2 = dLineEndY2 - (dLineEndX2 * dSlope2);							// d2

        // check if parallel to y-axis
        if (almostEqual(dLineEndX1, dLineStartX1)) {
            dReturnX = dLineEndX1;
            dReturnY = dReturnX * dSlope2 + dYIntercept2;
        }
        if (almostEqual(dLineEndX2, dLineStartX2)) {
            dReturnX = dLineEndX2;
            dReturnY = dReturnX * dSlope1 + dYIntercept1;
        }

        // if the slope is the same the lines are parallel and never cross another
        if (almostEqual(dSlope1, dSlope2)) {
            return false;
        }

        if(Double.isInfinite(dSlope1)) {
            dReturnX = dLineEndX1;
            dReturnY = dReturnX * dSlope2 + dYIntercept2;
        } else if(Double.isInfinite(dSlope2)) {
            dReturnX = dLineEndX2;
            dReturnY = dReturnX * dSlope1 + dYIntercept1;
        } else {
            dReturnX = (dYIntercept2 - dYIntercept1) / (dSlope1 - dSlope2);
            dReturnY = dReturnX * dSlope1 + dYIntercept1;
        }

        returnIntersection.set(dReturnX, dReturnY);

        return true;
    }

    public double getDistanceByCoordinates(    Point pos1,
                                               Point pos2)
    {
        return getDistanceByCoordinates( pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY(),0,0);
    }

    public double getDistanceByCoordinates(    double dPosX1, double dPosY1,
                                        double dPosX2, double dPosY2)
    {
        return getDistanceByCoordinates( dPosX1,dPosY1, dPosX2, dPosY2,0,0);
    }

    public double getDistanceByCoordinates(    double dPosX1, double dPosY1,
                                        double dPosX2, double dPosY2,
                                         double dAxisCenterX, double dAxisCenterY)
    {
        if (dPosX2 == 0 && dPosY2 == 0) {
            return Math.sqrt((Math.pow(dPosX1 - (dAxisCenterX), 2) + Math.pow(dPosY1 - dAxisCenterY, 2)));
        } else {
            return Math.sqrt(Math.pow(dPosX1 - dPosX2, 2) + Math.pow(dPosY1 - dPosY2, 2));
        }
    }

    public boolean checkWallIntersectionWithinPort (PedestrianStreetSectionPort port, Point intersection) {
        return checkWallIntersectionWithinPort(port.getLocalBginOfStreetPort().getX(), port.getLocalBginOfStreetPort().getY(),
                port.getLocalEndOfStreetPort().getX(), port.getLocalEndOfStreetPort().getY(), intersection);
    }


    public boolean checkWallIntersectionWithinPort (double portBeginX, double portBeginY, double portEndX, double portEndY, Point wallIntersection) {
        if ( almostEqual(portBeginX, portEndX)) {
            if (! almostEqual(portBeginX, wallIntersection.getX())) return false; // intersection has to be on the right wall
            if (    (val1Bigger(portBeginY, wallIntersection.getY()) &&
                    val1Lower(portEndY, wallIntersection.getY()) )
                    ||
                    (val1Lower(portBeginY, wallIntersection.getY()) &&
                            val1Bigger(portEndY, wallIntersection.getY()))
                    ) {
                return true;
            }
        } else { //if(almostEqual(portBeginY, portEndY)) {
            if (! almostEqual(portBeginY, wallIntersection.getY())) return false; // intersection has to be on the right wall
            if (    (val1Bigger(portBeginX, wallIntersection.getX()) &&
                    val1Lower(portEndX, wallIntersection.getX()) )
                    ||
                    (val1Lower(portBeginX, wallIntersection.getX()) &&
                            val1Bigger(portEndX, wallIntersection.getX()))
                    ) {
                return true;
            }
        }
        return false;
    }

    public void shiftIntersection (PedestrianStreetSectionPort port, Point intersection) {
        shiftIntersection(port, intersection, 0);
    }

    public void shiftIntersection (PedestrianStreetSectionPort port, Point intersection, double minGabToWall) {
        shiftIntersection(port.getLocalBginOfStreetPort().getX(), port.getLocalBginOfStreetPort().getY(),
                port.getLocalEndOfStreetPort().getX(), port.getLocalEndOfStreetPort().getY(), intersection, minGabToWall);
    }

    public void shiftIntersection( double portBeginX, double portBeginY, double portEndX, double portEndY, Point wallIntersection){
        shiftIntersection(portBeginX, portBeginY, portEndX, portEndY, wallIntersection, 0);
    }

    public void shiftIntersection( double portBeginX, double portBeginY, double portEndX, double portEndY, Point wallIntersection, double minGabToWall) {
        // point within the port gab
        // get closer corner of port
        if (getDistanceByCoordinates(portBeginX, portBeginY, wallIntersection.getX(), wallIntersection.getY()) <
                getDistanceByCoordinates(portEndX, portEndY, wallIntersection.getX(), wallIntersection.getY())){
            // closer to the begin of the port
            wallIntersection.setLocation(portBeginX, portBeginY);
        }else {
            // closer to the end of the port
            wallIntersection.setLocation(portEndX, portEndY);
        }
        if (minGabToWall != 0) shiftIntersectionSub(portBeginX, portBeginY, portEndX, portEndY, wallIntersection, minGabToWall);
    }

    void shiftIntersectionSub( double portBeginX, double portBeginY, double portEndX, double portEndY, Point wallIntersection, double minGabToWall) {
        if( almostEqual(portBeginX, portEndX) ) { // port along y side
            if( val1LowerOrAlmostEqual(portBeginY, portEndY)) {
                wallIntersection.setLocation(wallIntersection.getX(), wallIntersection.getY() + minGabToWall);
            } else {
                wallIntersection.setLocation(wallIntersection.getX(), wallIntersection.getY() - minGabToWall);
            }
        } else { // port along x side/aches
            if( val1LowerOrAlmostEqual(portBeginX, portEndX)) {
                wallIntersection.setLocation(wallIntersection.getX() + minGabToWall, wallIntersection.getY());
            } else {
                wallIntersection.setLocation(wallIntersection.getX() - minGabToWall, wallIntersection.getY());
            }
        }
    }
}

