package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.RoundaboutCar;
import at.fhv.itm3.s2.roundabout.entity.StreetSection;
import javafx.scene.paint.Color;
import org.python.modules._systemrestart;

import javax.vecmath.Vector2d;

public class PedestrianUIUtils {
    public static double MAIN_WINDOW_WIDTH = 1500;
    public static double MAIN_WINDOW_HEIGHT = 1000;
    public static double SCALE_FACTOR = 0.3;
    public static double PEDESTRIAN_WIDTH = 30;
    public static double MAX_VEHICLE_STREET_LENGTH = 800;
    public static double VEHICLE_STREET_SCALEFACTOR = 100;
    public static double amountCrossWalkPanels = 0.5;
    public static double VEHICLE_WIDTH = 40;
    public static double relativeObjectLabelPositionX = 10;
    public static double relativeObjectLabelPositionY = 10;
    public static int maximumNumberCarsPerStreet = 1;
    public static int minTimeInMsToWaitForExecute = 1000;





    public static double GetMaxedUiStreetLength(StreetSection streetSection){
        return Math.min(streetSection.getLength() * VEHICLE_STREET_SCALEFACTOR , MAX_VEHICLE_STREET_LENGTH);
    }

    public static double GetCarPositionRelativeToWidth(CarStreetUI carStreetUI, StreetSection section){
        if (section instanceof StreetSection) {
            StreetSection streetSection = section;
            boolean carDrivesAlongYAxis = streetSection.checkCarDrivesAlongYAxis();
            if (carDrivesAlongYAxis){
                double pos = carStreetUI.getX() + (carStreetUI.getWidth()/2) - PedestrianUIUtils.VEHICLE_WIDTH/2;
                return pos;
            } else {
                double pos =  carStreetUI.getY() + (carStreetUI.getHeight()/2) - PedestrianUIUtils.VEHICLE_WIDTH/2;
                return pos;
            }
        }
        throw new IllegalArgumentException();
    }

    public static double GetMaxedUiCarPosition(RoundaboutCar roundaboutCar, StreetSection section, CarStreetUI carStreetUI){
        if (section instanceof StreetSection) {
            StreetSection streetSection = section;
            boolean carDrivesAlongYAxis = streetSection.checkCarDrivesAlongYAxis();

            double remainingLengthOfCurrentSection = roundaboutCar.getRemainingLengthOfCurrentSection();
            double pedestrianStreetWidth = streetSection.getPedestrianCrossingWidthInM();

            double distToCrossing = remainingLengthOfCurrentSection - pedestrianStreetWidth;
            //car is on vertical axis
            Vector2d vecDirection = new Vector2d();
            if (carDrivesAlongYAxis) {
                if (streetSection.doesHavePedestrianCrossingToEnter()) {
                    if (streetSection.getPedestrianCrossingEntryAtBeginning()) {
                        vecDirection = new Vector2d(0, 1);
                        // front of car = remaining length
                    } else {
                        vecDirection = new Vector2d(0, -1);
                        // back of car
                        distToCrossing += roundaboutCar.getLength();
                    }
                } else if (streetSection.doesHavePedestrianCrossingThatHasBeenLeftBefore()) {
                    if (streetSection.getPedestrianCrossingExitAtBeginning()) {
                        vecDirection = new Vector2d(0, -1);
                        // back of car
                        distToCrossing = streetSection.getLength() - distToCrossing;
                        distToCrossing -= roundaboutCar.getLength();
                    } else {
                        vecDirection = new Vector2d(0, 1);
                        // front of car
                        distToCrossing = streetSection.getLength() - distToCrossing;
                    }
                }
            } else {
                if (streetSection.doesHavePedestrianCrossingToEnter()) {
                    if (streetSection.getPedestrianCrossingEntryAtBeginning()) {
                        vecDirection = new Vector2d(1, 0);
                        // back of car
                        distToCrossing += roundaboutCar.getLength();
                    } else {
                        vecDirection = new Vector2d(-1, 0);
                        // front of car = remaining length
                    }
                } else if (streetSection.doesHavePedestrianCrossingThatHasBeenLeftBefore()) {
                    if (streetSection.getPedestrianCrossingExitAtBeginning()) {
                        vecDirection = new Vector2d(-1, 0);
                        // front of car
                        distToCrossing = streetSection.getLength() - distToCrossing;
                    } else {
                        vecDirection = new Vector2d(1, 0);
                        // get back
                        distToCrossing = streetSection.getLength() - distToCrossing;
                        distToCrossing -= roundaboutCar.getLength();
                    }
                }
            }

            double ratio = distToCrossing / streetSection.getLength();
            double pos;
             if ( carDrivesAlongYAxis) {
                 distToCrossing *= vecDirection.getY();
                 distToCrossing *= Math.abs(ratio);
                 pos = carStreetUI.getY() + distToCrossing;
             } else {
                 distToCrossing *= vecDirection.getX();
                 distToCrossing *= Math.abs(ratio);
                 pos = carStreetUI.getX() + distToCrossing;
             }
            return pos;
        }
        throw new IllegalArgumentException();
    }
}
