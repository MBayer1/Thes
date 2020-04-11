package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm3.s2.roundabout.SocialForceModelCalculation.SupportiveCalculations;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;

import javax.vecmath.Vector2d;

public class AccelarationFOrceTMp {

    public AccelarationFOrceTMp () {
        getAccelerationForceToTarget();
    }

    public void getAccelerationForceToTarget(){
        SupportiveCalculations calculations = new SupportiveCalculations();

        Vector2d currentSpeedVector = new Vector2d(0,0);//pedestrian.getCurrentSpeed(),0.0);
        Vector2d currentPositionVector = new Vector2d(0,0);//pedestrian.getCurrentGlobalPosition().getX(), pedestrian.getCurrentGlobalPosition().getY());

/*        if (! (pedestrian.getCurrentSection().getStreetSection() instanceof PedestrianStreetSection)) {throw new IllegalStateException("Section not instance of PedestrianStreetSection.");}        PedestrianStreetSection section = (PedestrianStreetSection)pedestrian.getCurrentSection().getStreetSection();*/

            //nextDestinationVector = nextDestinationVector - currentPositionVector
            PedestrianPoint subGoal = new PedestrianPoint(10,10);//pedestrian.getNextSubGoal(); // global  coordinates without any obstacle etc. = exit-point of  section -> always calc new since real aim is afterwards change so is current position
            Vector2d preferredSpeedVector = calculations.getVector( currentPositionVector.getX(), currentPositionVector.getY(),
                    subGoal.getX() ,//0 //+ section.getGlobalCoordinateOfSectionOrigin().getX(),
                    subGoal.getY() );//0 //+ section.getGlobalCoordinateOfSectionOrigin().getY());

            Double preferredSpeedValue = preferredSpeedVector.length();
            preferredSpeedVector.scale(1/preferredSpeedValue);
            preferredSpeedVector.scale(/*pedestrian.*/calculatePreferredSpeed()); //v_alpha * e_alpha(t)

            preferredSpeedVector.sub(currentSpeedVector);
            preferredSpeedVector.scale(1/2.2); //model.getRandomPedestrianRelaxingTimeTauAlpha());

            //return preferredSpeedVector;
        }

    public double calculatePreferredSpeed() {
        double preferredSpeed = 5; // m/s
        double maxPreferredSpeed = 8;
        double timeRelatedParameterValueNForSpeedCalculation = 1;

        double averageSpeed = 0;//getTimeSpentInSystem() == 0 ? 0 : walkedDistance / getTimeSpentInSystem();
        double timeRelatedParameter = averageSpeed / preferredSpeed;
        timeRelatedParameter = timeRelatedParameterValueNForSpeedCalculation - timeRelatedParameter;

        double part1 = (1 - timeRelatedParameter) * preferredSpeed;
        double part2 = timeRelatedParameter * maxPreferredSpeed;

        return part1 + part2;
    }

}
