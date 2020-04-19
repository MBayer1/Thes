package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.PedestrianStreetSectionPort;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;

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
            return Math.sqrt((Math.pow(dPosX1 - (dAxisCenterX), 2) + Math.pow(dPosY1 - dAxisCenterY, 2)));
        } else {
            return Math.sqrt(Math.pow(dPosX1 - dPosX2, 2) + Math.pow(dPosY1 - dPosY2, 2));
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

    public void shiftIntersection (PedestrianStreetSectionPort port, PedestrianPoint intersection) {
        shiftIntersection(port, intersection, 0);
    }

    public void shiftIntersection (PedestrianStreetSectionPort port, PedestrianPoint intersection, double minGabToWall) {
        shiftIntersection(port.getLocalBeginOfStreetPort().getX(), port.getLocalBeginOfStreetPort().getY(),
                port.getLocalEndOfStreetPort().getX(), port.getLocalEndOfStreetPort().getY(), intersection, minGabToWall);
    }

    public void shiftIntersection( double portBeginX, double portBeginY, double portEndX, double portEndY, PedestrianPoint wallIntersection){
        shiftIntersection(portBeginX, portBeginY, portEndX, portEndY, wallIntersection, 0);
    }

    public void shiftIntersection( double portBeginX, double portBeginY, double portEndX, double portEndY, PedestrianPoint wallIntersection, double minGabToWall) {
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
        if (minGabToWall != 0) shiftIntersectionSub(portBeginX, portBeginY, portEndX, portEndY, wallIntersection, minGabToWall);
    }

    public void shiftIntersectionSub( double portBeginX, double portBeginY, double portEndX, double portEndY, PedestrianPoint wallIntersection, double minGabToWall) {
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


    double getAngleDiffBetweenTwoLinesFacingCharCenter(
            PedestrianPoint pointOnEllipse,
            PedestrianPoint foci1,
            PedestrianPoint foci2){

        Vector2d vec1 = getVector(pointOnEllipse, foci1);
        Vector2d vec2 = getVector(pointOnEllipse, foci2);

        double dAngle1 = getAngleInDegFromPosition(pointOnEllipse.getX(), pointOnEllipse.getY(), foci1.getX(), foci1.getY()); // we asume this angle as Startangle
        double dAngle2 = getAngleInDegFromPosition(pointOnEllipse.getX(), pointOnEllipse.getY(), foci2.getX(), foci2.getY());

        //verify
        double ax = pointOnEllipse.getX();
        double ay = pointOnEllipse.getY();
        double bx1 = foci1.getX();
        double by1 = foci1.getY();
        double bx2 = foci2.getX();
        double by2 = foci2.getY();
        double angle1 = Math.atan2( ax*by1 - ay * bx1, ax*bx1 + ay * by1 );
        double angle2 = Math.atan2( ax*by2 - ay * bx2, ax*bx2 + ay * by2 );

        if(almostEqual(dAngle1, dAngle2)){
            throw new IllegalStateException("Angle difference can not be calculated. Both angle basis have the same angle.");
        }

        double dAngle;
        // get Angle Anticlockwise, started from Line1
        if(dAngle1 <=  dAngle2) {
            dAngle = dAngle2 - dAngle1; //Anticlockwise direction
            //if (!((dAngle1 <= dAngleCompare) && (dAngleCompare <= dAngle2) )) dAngle = 360-dAngle;  // we have to take the clockwise angle

        } else { //dAngle > dAngle2
            dAngle = 360 - dAngle1 + dAngle2; //Anticlockwise direction
            /*if(!((dAngleCompare < 360 && dAngleCompare > dAngle1) ||dAngle < dAngle2)){
                dAngle = 360-dAngle;  // we have to take the clockwise angle
            }*/
        }

        return dAngle;
    }

    Vector2d getVectorFromAngleInDegAndLength (double dAngle, double dLength){
        double dVecX, dVecY;

        double dAngleInRad = Math.toRadians(dAngle);
        dVecX = Math.cos(dAngleInRad); // is univec
        dVecY = Math.sin(dAngleInRad); // is univec

        dVecX *= dLength;
        dVecY *= dLength;

        return new Vector2d(dVecX, dVecY);
    }

    int getQuadrantOfDegreeAngle( double dAngleInDeg )
    {
        while( dAngleInDeg < 0) dAngleInDeg += 360;
        while( dAngleInDeg > 360) dAngleInDeg -= 360;

        if( val1Lower( dAngleInDeg, 90)){ // first qadrant
            return 1;
        } else if ( val1Lower( dAngleInDeg, 180 ) && val1BiggerOrAlmostEqual( dAngleInDeg, 90) ) { // second quadrant
            return 2;
        } else if ( val1Lower( dAngleInDeg, 270 ) && val1BiggerOrAlmostEqual( dAngleInDeg, 180) ) { // third quadrant
            return 3;
        } else if ( val1Lower( dAngleInDeg, 360 ) && val1BiggerOrAlmostEqual( dAngleInDeg, 270) ) {	// fourth quadrant
            return 4;
        }
        throw new IllegalStateException("Quadrant of angle could not be calculated.");
    }

    double getAngleInDegFromPosition(double dX, double dY, double dAxisCenterX, double dAxisCenterY)
    {
        double dAlpha, dHypotenuse, dOppositeSide;

        // do not use almost equal! as long there is some sort of differentce is it fine
        // (especially needed fo step 2b: TransPoint might not be fare frome center)
        if(dX == dAxisCenterX && dY == dAxisCenterY) return 0.;

        dHypotenuse = Math.sqrt(Math.pow(dX - dAxisCenterX, 2) + Math.pow(dY - dAxisCenterY, 2));
        dOppositeSide = Math.abs(dY - dAxisCenterY);
        dAlpha = dOppositeSide / dHypotenuse;
        dAlpha = Math.toDegrees(Math.asin(dAlpha));

        dX = dX-dAxisCenterX;
        dY = dY-dAxisCenterY;

        if (val1LowerOrAlmostEqual(dX, 0) && dY > 0) {  // second quadrant
            if (almostEqual(dAlpha, 0))
                return 90.0;
            else if (almostEqual(dAlpha, Math.PI))
                return 180.0;
            else
                return 180 - dAlpha;
        } else if (val1LowerOrAlmostEqual(dX, 0) && val1LowerOrAlmostEqual(dY, 0)) {  // third quadrant
            if (almostEqual(dAlpha, 0))
                return 180.0;
            else if (almostEqual(dAlpha, Math.PI))
                return 270.0;
            else
                return 180 + dAlpha;
        } else if (val1BiggerOrAlmostEqual(dX, 0) && dY < 0) {  // forth quadrant
            if (almostEqual(dAlpha, 0))
                return 270.0;
            else if (almostEqual(dAlpha, Math.PI))
                return 0.0;
            else
                return 360 - dAlpha;
        } else {
            if (almostEqual(dAlpha, 0))
                return 0.0;
            else if (almostEqual(dAlpha, Math.PI))
                return 90.0;
            else
                return dAlpha;
        }
    }
}

