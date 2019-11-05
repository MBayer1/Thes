package at.fhv.itm3.s2.roundabout.SocialForceModelCalculation;

import javax.vecmath.Vector2d;

public class SupportiveCalculations {
    
    public Vector2d GetVector(double startPointX, double startPointY, double endPointX, double endPointY){
            return new Vector2d(endPointX-startPointX, endPointY-startPointY);
    }

    
    public boolean CheckLinesIntersectionByCoordinates(double intersectionX, double intersectionY,
                                                       double lineStartX1,   double lineStartY1, 
                                                       double lineEndX1, double lineEndY1, 
                                                       double lineStartX2, double lineStartY2,
                                                       double lineEndX2, double lineEndY2    
    ){

        if((lineStartX1 == lineStartX2) && (lineStartY1 == lineStartY2) &&
                (lineEndX1 == lineEndX2) && (lineEndY1 == lineEndY2)){
            throw new IllegalArgumentException("Lines are identical.");
        }

        //linear equation: y=m*x+d -> note special case: parallel to y-axis -> y(x) = const, always
        //1. set linear equation in linear equation -> m1*x+d1 = m2*x+d2  -> x = (d2-d1)/(m1-m2)
        double dSlope1 = (lineEndY1-lineStartY1)/(lineEndX1-lineStartX1);	//m1
        double dYIntercept1 = lineEndY1-(lineEndX1*dSlope1);						//d1
        double dSlope2 = (lineEndY2-lineStartY2)/(lineEndX2-lineStartX2);	//m2
        double dYIntercept2 = lineEndY2-(lineEndX2*dSlope2);						//d2

        //check if parallel to y-axis
        if (AlmostEqual(lineEndX1,lineStartX1)){
            intersectionX = lineEndX1;
            intersectionY = intersectionX* dSlope2 + dYIntercept2;
        }
        if (AlmostEqual(lineEndX2,lineStartX2)){
            intersectionX = lineEndX2;
            intersectionY = intersectionX* dSlope1 + dYIntercept1;
        }

        // if the slope is the same the lines are parallel and never cross another
        if(AlmostEqual(dSlope1, dSlope2)){
            return false;
        }

        intersectionX = (dYIntercept2-dYIntercept1)/(dSlope1-dSlope2);
        intersectionY = intersectionX* dSlope1 + dYIntercept1;

        //Intersection have to be on the line segment
        if((((intersectionX>=lineStartX1) && (intersectionX<=lineEndX1)) ||
                ((intersectionX<=lineStartX1) && (intersectionX>=lineEndX1))) &&
                (((intersectionX>=lineStartX2) && (intersectionX<=lineEndX2)) ||
                        ((intersectionX<=lineStartX2) && (intersectionX>=lineEndX2)))){
            return true;
        }

        return false;
    }

    public boolean AlmostEqual(double dVal1, double dVal2){
        return AlmostEqual(dVal1,dVal2, 10e-8);
    }

    public boolean LowerOrAlmostEqual(double dVal1, double dVal2){
        return LowerOrAlmostEqual(dVal1,dVal2, 10e-8);
    }

    public boolean BiggerOrAlmostEqual(double dVal1, double dVal2){
        return BiggerOrAlmostEqual(dVal1,dVal2, 10e-8);
    }

    public boolean LowerOrAlmostEqual(double dVal1, double dVal2, double SFM_DegreeOfAccuracy) {
        return ((dVal1 < dVal1) || AlmostEqual(dVal1,dVal2, SFM_DegreeOfAccuracy));
    }

    public boolean BiggerOrAlmostEqual(double dVal1, double dVal2, double SFM_DegreeOfAccuracy) {
        return ((dVal1 > dVal1) || AlmostEqual(dVal1,dVal2, SFM_DegreeOfAccuracy));
    }

    public boolean AlmostEqual(double dVal1, double dVal2, double SFM_DegreeOfAccuracy)
    {
        return (GetRoundValue(Math.abs(dVal1-dVal2), SFM_DegreeOfAccuracy) < SFM_DegreeOfAccuracy);

    }

    public double GetRoundValue(double dValue){
        return GetRoundValue(dValue, 10e-8);
    }

    public double GetRoundValue(double dValue, double SFM_DegreeOfAccuracy){

        double dDegreeOfAccuracy = SFM_DegreeOfAccuracy;

        return Math.round(dValue * SFM_DegreeOfAccuracy) / SFM_DegreeOfAccuracy;
    }



    boolean GetLinesIntersectionByCoordinates(	Vector2d returnIntersection,
                                            double dLineStartX1, double dLineStartY1,
                                            double dLineEndX1, double dLineEndY1,
                                            double dLineStartX2, double dLineStartY2,
                                            double dLineEndX2, double dLineEndY2)
    {
        double dReturnX, dReturnY;
        if (AlmostEqual(dLineStartX1, dLineStartX2) && AlmostEqual(dLineStartY1, dLineStartY2) &&
                AlmostEqual(dLineEndX1, dLineEndX2) && AlmostEqual(dLineEndY1, dLineEndY2)) {
            // exactly the same line
            return false;
        }

        // linear equation: y=m*x+d -> note spacial case: parallel to y-axis -> y(x) = const, always
        // 1. set linear equation in linear equation -> m1*x+d1 = m2*x+d2  -> x = (d2-d1)/(m1-m2)
        double dSlope1 = (dLineEndY1 - dLineStartY1) / (dLineEndX1 - dLineStartX1);			// m1
        if(Double.isInfinite(dSlope1)) return false;
        double dYIntercept1 = dLineEndY1 - (dLineEndX1 * dSlope1);							// d1
        double dSlope2 = (dLineEndY2 - dLineStartY2) / (dLineEndX2 - dLineStartX2);			// m2
        if(Double.isInfinite(dSlope2)) return false;
        double dYIntercept2 = dLineEndY2 - (dLineEndX2 * dSlope2);							// d2

        // check if parallel to y-axis
        if (AlmostEqual(dLineEndX1, dLineStartX1)) {
            dReturnX = dLineEndX1;
            dReturnY = dReturnX * dSlope2 + dYIntercept2;
        }
        if (AlmostEqual(dLineEndX2, dLineStartX2)) {
            dReturnX = dLineEndX2;
            dReturnY = dReturnX * dSlope1 + dYIntercept1;
        }

        // if the slope is the same the lines are parallel and never cross another
        if (AlmostEqual(dSlope1, dSlope2)) {
            return false;
        }

        dReturnX = (dYIntercept2 - dYIntercept1) / (dSlope1 - dSlope2);
        dReturnY = dReturnX * dSlope1 + dYIntercept1;

        returnIntersection.set(dReturnX, dReturnY);

        return true;
    }


}

