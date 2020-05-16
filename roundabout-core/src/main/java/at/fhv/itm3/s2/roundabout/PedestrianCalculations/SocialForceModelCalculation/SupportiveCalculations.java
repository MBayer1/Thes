package at.fhv.itm3.s2.roundabout.PedestrianCalculations.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreetSectionPort;

import javax.vecmath.Vector2d;

public class SupportiveCalculations {

    public Vector2d getVector(Vector2d startPoint, Vector2d endPoint){
        return getVector(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }

    public Vector2d getVector(PedestrianPoint startPoint, PedestrianPoint endPoint){
        return getVector(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }

    public Vector2d getVector(double startPointX, double startPointY, double endPointX, double endPointY){
            return new Vector2d(endPointX-startPointX, endPointY-startPointY); // head -  tail
    }

    public Vector2d getUnitVector( Vector2d vector) {
        return  getUnitVector(vector.getX(), vector.getY());
    }

    public Vector2d getUnitVector( double vectorX,	double vectorY)
    {
        double fraction = Math.sqrt(Math.pow(vectorX, 2) + Math.pow(vectorY, 2));
        if (fraction == 0) return new Vector2d(0.,0.);
        double returnXTmp = vectorX / fraction;
        double returnYTmp = vectorY / fraction;
        return new Vector2d(returnXTmp, returnYTmp);
    }

    public Vector2d getUnitNormalVector(double vectorX,	double vectorY)
    {
        // 90° counterclockwise

        Vector2d newVec = getUnitVector(vectorX, vectorY);

        double temp = newVec.getX();
        double X = newVec.getY();
        double Y = temp;

        newVec = new Vector2d(X * (-1), Y);
        return newVec;
    }


    public Vector2d getUnitNormalVector( Vector2d vector) {
        return  getUnitNormalVector(vector.getX(), vector.getY());
    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(PedestrianPoint intersection,
                                                                     PedestrianPoint lineStart1,
                                                                     double lineEndX1, double lineEndY1,
                                                                     PedestrianPoint lineStart2,
                                                                     PedestrianPoint lineEnd2
    ) {
        return checkLinesIntersectionByCoordinates_WithinSegment(intersection,
                lineStart1.getX(), lineStart1.getY(), lineEndX1, lineEndY1,
                lineStart2.getX(), lineStart2.getY(), lineEnd2.getX(), lineEnd2.getY());

    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(PedestrianPoint intersection,
                                                                     PedestrianPoint lineStart1,
                                                                     PedestrianPoint lineEnd1,
                                                                     PedestrianPoint lineStart2,
                                                                     PedestrianPoint lineEnd2
    ) {
        return checkLinesIntersectionByCoordinates_WithinSegment(intersection,
                lineStart1.getX(), lineStart1.getY(), lineEnd1.getX(), lineEnd1.getY(),
                lineStart2.getX(), lineStart2.getY(), lineEnd2.getX(), lineEnd2.getY());

    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(PedestrianPoint intersection,
                                                                     double lineStartX1, double lineStartY1,
                                                                     double lineEndX1, double lineEndY1,
                                                                     PedestrianPoint lineStart2,
                                                                     PedestrianPoint lineEnd2
    ) {
        return checkLinesIntersectionByCoordinates_WithinSegment(intersection,
                        lineStartX1, lineStartY1,
                        lineEndX1, lineEndY1,
                lineStart2.getX(), lineStart2.getY(), lineEnd2.getX(), lineEnd2.getY());

    }

    public boolean checkLinesIntersectionByCoordinates_WithinSegment(PedestrianPoint intersection,
                                                                     double lineStartX1, double lineStartY1,
                                                                     double lineEndX1, double lineEndY1,
                                                                     double lineStartX2, double lineStartY2, //global
                                                                     double lineEndX2, double lineEndY2
    ) {

        if ((lineStartX1 == lineStartX2) && (lineStartY1 == lineStartY2) &&
                (lineEndX1 == lineEndX2) && (lineEndY1 == lineEndY2)) {
            throw new IllegalArgumentException("Lines are identical.");
        }

        //linear equation: y=m*x+d -> note special case: parallel to y-axis     -> y(x) = const, always
        //1. set linear equation in linear equation -> m1*x+d1 = m2*x+d2        -> x = (d2-d1)/(m1-m2)
        double dSlope1 = (lineEndY1 - lineStartY1) / (lineEndX1 - lineStartX1);         //m1
        double dYIntercept1 = lineEndY1 - (lineEndX1 * dSlope1);                        //d1
        double dSlope2 = (lineEndY2 - lineStartY2) / (lineEndX2 - lineStartX2);         //m2
        double dYIntercept2 = lineEndY2 - (lineEndX2 * dSlope2);                        //d2

        // both parallel y-axis(inf) or both along x axis (0)
        if ((Double.isInfinite(dSlope1) && Double.isInfinite(dSlope2)) ||
                (almostEqual(dSlope1, 0) && almostEqual(dSlope2, 0))) {
            return false;
        }

        //check if parallel to y-axis
        if (almostEqual(lineEndX1, lineStartX1)) {
            intersection.setLocation(lineEndX1, lineEndX1 * dSlope2 + dYIntercept2);
        } else if (almostEqual(lineEndX2, lineStartX2)) {
            intersection.setLocation(lineEndX2, lineEndX2 * dSlope1 + dYIntercept1);
        } else{

            // if the slope is the same the lines are parallel and never cross another
            if (almostEqual(dSlope1, dSlope2)) {
                return false;
            }
            double tmpX = (dYIntercept2 - dYIntercept1) / (dSlope1 - dSlope2);
            intersection.setLocation(tmpX, tmpX * dSlope1 + dYIntercept1);
        }

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

    public PedestrianPoint getLinesIntersectionByCoordinates(	     PedestrianStreetSectionPort port,
                                                         double dLineStartX2, double dLineStartY2,
                                                         double dLineEndX2, double dLineEndY2) {
        return getLinesIntersectionByCoordinates( port.getLocalBeginOfStreetPort().getX(), port.getLocalBeginOfStreetPort().getY(),
                port.getLocalEndOfStreetPort().getX(), port.getLocalEndOfStreetPort().getY(),
                dLineStartX2, dLineStartY2, dLineEndX2, dLineEndY2);
    }

    public PedestrianPoint getLinesIntersectionByCoordinates(	     double dLineStartX1, double dLineStartY1,
                                                                           double dLineEndX1, double dLineEndY1,
                                                                           double dLineStartX2, double dLineStartY2,
                                                                           double dLineEndX2, double dLineEndY2) {
        Vector2d returnIntersection = new Vector2d();
        if(getLinesIntersectionByCoordinates(	returnIntersection,
                dLineStartX1, dLineStartY1, dLineEndX1, dLineEndY1,
                dLineStartX2, dLineStartY2, dLineEndX2, dLineEndY2)){
            return new PedestrianPoint(returnIntersection.getX(), returnIntersection.getY());
        }
        return null;
        //throw new IllegalArgumentException("Two lines do not have any intersection");
    }

    public PedestrianPoint getLinesIntersectionByCoordinates(	     PedestrianPoint lineStart1,
                                                                      PedestrianPoint lineEnd1,
                                                                      PedestrianPoint lineStart2,
                                                                      PedestrianPoint lineEnd2) {
        Vector2d returnIntersection = new Vector2d();
        if(getLinesIntersectionByCoordinates(	returnIntersection,
                lineStart1.getX(), lineStart1.getY(), lineEnd1.getX(), lineEnd1.getY(),
                lineStart2.getX(), lineStart2.getY(), lineEnd2.getX(), lineEnd2.getY())){
            return new PedestrianPoint(returnIntersection.getX(), returnIntersection.getY());
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

    public double getDistanceByCoordinates(    PedestrianPoint pos1,
                                               PedestrianPoint pos2)
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
            return Math.abs(Math.sqrt((Math.pow(dPosX1 - (dAxisCenterX), 2) + Math.pow(dPosY1 - dAxisCenterY, 2))));
        } else {
            return Math.abs(Math.sqrt(Math.pow(dPosX1 - dPosX2, 2) + Math.pow(dPosY1 - dPosY2, 2)));
        }
    }

    public boolean checkWallIntersectionWithinPort (PedestrianStreetSectionPort port, PedestrianPoint intersection) {
        return checkWallIntersectionWithinPort(port.getLocalBeginOfStreetPort().getX(), port.getLocalBeginOfStreetPort().getY(),
                port.getLocalEndOfStreetPort().getX(), port.getLocalEndOfStreetPort().getY(), intersection);
    }


    public boolean checkWallIntersectionWithinPort (double portBeginX, double portBeginY, double portEndX, double portEndY, PedestrianPoint wallIntersection) {
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

    public PedestrianPoint shiftIntersection (PedestrianStreetSectionPort port, PedestrianPoint intersection) {
        return shiftIntersection(port, intersection, 0);
    }

    public PedestrianPoint shiftIntersection (PedestrianStreetSectionPort port, PedestrianPoint intersection, double minGabToWall) {
        return shiftIntersection(port.getLocalBeginOfStreetPort().getX(), port.getLocalBeginOfStreetPort().getY(),
                port.getLocalEndOfStreetPort().getX(), port.getLocalEndOfStreetPort().getY(), intersection, minGabToWall);
    }

    public PedestrianPoint shiftIntersection( double portBeginX, double portBeginY, double portEndX, double portEndY, PedestrianPoint wallIntersection){
        return shiftIntersection(portBeginX, portBeginY, portEndX, portEndY, wallIntersection, 0);
    }

    public PedestrianPoint shiftIntersection( double portBeginX, double portBeginY, double portEndX, double portEndY, PedestrianPoint wallIntersection, double minGabToWall) {
        // PedestrianPoint within the port gab
        // get closer corner of port
        if (getDistanceByCoordinates(portBeginX, portBeginY, wallIntersection.getX(), wallIntersection.getY()) <
                getDistanceByCoordinates(portEndX, portEndY, wallIntersection.getX(), wallIntersection.getY())){
            // closer to the begin of the port
            wallIntersection.setLocation(portBeginX, portBeginY);
        }else {
            // closer to the end of the port
            wallIntersection.setLocation(portEndX, portEndY);
        }
        if (minGabToWall != 0) wallIntersection = shiftIntersectionSub(portBeginX, portBeginY, portEndX, portEndY, wallIntersection, minGabToWall);
        return wallIntersection;
    }

    public PedestrianPoint shiftIntersectionSub( double portBeginX, double portBeginY, double portEndX, double portEndY, PedestrianPoint wallIntersection, double minGabToWall) {
        if( almostEqual(portBeginX, portEndX) ) { // port along y side
            if( val1LowerOrAlmostEqual(portBeginY, portEndY)) {
                if( Math.abs(wallIntersection.getY()- portEndY) < Math.abs(wallIntersection.getY()- portBeginY)){
                    wallIntersection.setLocation(wallIntersection.getX(), portEndY - minGabToWall);
                } else {
                    wallIntersection.setLocation(wallIntersection.getX(), portBeginY + minGabToWall);
                }
            } else {
                if( Math.abs(wallIntersection.getY()- portEndY) < Math.abs(wallIntersection.getY()- portBeginY)){
                    wallIntersection.setLocation(wallIntersection.getX(), portEndY + minGabToWall);
                } else {
                    wallIntersection.setLocation(wallIntersection.getX(), portBeginY - minGabToWall);
                }
            }
        } else { // port along x side/aches
            if( val1LowerOrAlmostEqual(portBeginX, portEndX)) {
                if( Math.abs(wallIntersection.getX()- portEndX) < Math.abs(wallIntersection.getX()- portBeginX)){
                    wallIntersection.setLocation(portEndX - minGabToWall, wallIntersection.getY());
                } else {
                    wallIntersection.setLocation(portBeginX + minGabToWall, wallIntersection.getY());
                }
            } else {
                if( Math.abs(wallIntersection.getX()- portEndX) < Math.abs(wallIntersection.getX()- portBeginX)){
                    wallIntersection.setLocation(portEndX - minGabToWall, wallIntersection.getY());
                } else {
                    wallIntersection.setLocation(portBeginX + minGabToWall, wallIntersection.getY());
                }
            }
        }
        return wallIntersection;
    }

    public PedestrianPoint shiftPointToEllipse(Vector2d point, double semiaxesBig, double semiaxesSmall) {
        //https://mathworld.wolfram.com/Ellipse-LineIntersection.html

        /*              (x,y)      Point  from outside the ellipse
                        Line running  then through center of ellipse
                        a          Bigger SemiAxes
                        b          SmallerSemiaxes

                        fraction =  (a*b)/(sqrt(a^2 * y^2 + b^2 * x^2))

                        x = +- fraction * x
                        y = +- fraction * y

        * */

        double fraction = ((Math.pow(semiaxesBig,2) * Math.pow(point.getY(),2)) + ((Math.pow(semiaxesSmall,2) * Math.pow(point.getX(),2))));
        fraction = Math.sqrt(fraction);
        fraction = (semiaxesSmall*semiaxesBig)/fraction;

        // Both intersections
        double tmpX1 = fraction * point.getX();
        double tmpY1 = fraction * point.getY();
        double tmpX2 = tmpX1 * (-1);
        double tmpY2 = tmpY1 * (-1);

        if (getDistanceByCoordinates(point.getX(), point.getY(),tmpX1, tmpY1) <
                getDistanceByCoordinates(point.getX(), point.getY(),tmpX2, tmpY2)){
            return new PedestrianPoint(tmpX1, tmpY1);
        }
        return new PedestrianPoint(tmpX2, tmpY2);
    }

    public boolean checkPointOutsideEllipse(Vector2d center, PedestrianPoint point,
                                            double semiaxesBig, double semiaxesSmaller){
        //https://www.geeksforgeeks.org/check-if-a-point-is-inside-outside-or-on-the-ellipse/

        //(x-h)^2/a^2 + (y-k)^2/b^2 <= 1

        double h = center.getX();
        double k = center.getY();
        double x = point.getX();
        double y = point.getY();
        double a = semiaxesBig; // along x axis
        double b = semiaxesSmaller; // along y axis

        // checking the equation of
        // ellipse with the given point
        int p = ((int)Math.pow((x - h), 2) / (int)Math.pow(a, 2))
                + ((int)Math.pow((y - k), 2) / (int)Math.pow(b, 2));


        if (p> 1) {
            //System.out.println("Outside");
            return true;
        }else if (p == 1) {
            //System.out.println("On the ellipse");

        }else {
            //System.out.println("Inside");
        }
        return false;
    }
}

