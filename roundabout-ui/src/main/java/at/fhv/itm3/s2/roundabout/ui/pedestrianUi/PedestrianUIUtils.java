package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.RoundaboutCar;
import at.fhv.itm3.s2.roundabout.entity.StreetSection;
import javafx.scene.paint.Color;

import javax.vecmath.Vector2d;

public class PedestrianUIUtils {
    public static double MAIN_WINDOW_WIDTH = 2000;
    public static double MAIN_WINDOW_HEIGHT = 1000;
    public static double SCALE_FACTOR = 0.2;
    public static double PEDESTRIAN_WIDTH = 10;
    public static Color PEDESTRIAN_COLOR = Color.BLUE;
    public static double MAX_VEHICLE_STREET_LENGTH = 600;
    public static double VEHICLE_STREET_SCALEFACTOR = 100;
    public static double amountCrossWalkPanels = 0.5;
    public static double VEHICLE_WIDTH = 40;
    public static double relativeObjectLabelPositionX = 10;
    public static double relativeObjectLabelPositionY = 10;


    public static double GetMaxedUiStreetLength(StreetSection streetSection){
        return Math.min(streetSection.getLength() * VEHICLE_STREET_SCALEFACTOR , MAX_VEHICLE_STREET_LENGTH);
    }

    public static double GetCarPositionRelativeToWidth(CarStreetUI carStreetUI, RoundaboutCar roundaboutCar, StreetSection section){
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

    public static double GetMaxedUiCarPosition(RoundaboutCar roundaboutCar, StreetSection section){
        if (section instanceof StreetSection) {
            StreetSection streetSection = section;
            boolean carDrivesAlongYAxis = streetSection.checkCarDrivesAlongYAxis();

            //car is on vertical axis
            Vector2d vecDirection = new Vector2d();
            if (carDrivesAlongYAxis) {
                if (streetSection.doesHavePedestrianCrossingToEnter()) {
                    if (streetSection.getPedestrianCrossingEntryAtBeginning()) {
                        vecDirection = new Vector2d(0, 1);
                    } else {
                        vecDirection = new Vector2d(0, -1);
                    }
                } else if (streetSection.doesHavePedestrianCrossingThatHasBeenLeftBefore()) {
                    if (streetSection.getPedestrianCrossingExitAtBeginning()) {
                        vecDirection = new Vector2d(0, -1);
                    } else {
                        vecDirection = new Vector2d(0, 1);
                    }
                }
            } else {
                if (streetSection.doesHavePedestrianCrossingToEnter()) {
                    if (streetSection.getPedestrianCrossingEntryAtBeginning()) {
                        vecDirection = new Vector2d(1, 0);
                    } else {
                        vecDirection = new Vector2d(-1, 0);
                    }
                } else if (streetSection.doesHavePedestrianCrossingThatHasBeenLeftBefore()) {
                    if (streetSection.getPedestrianCrossingExitAtBeginning()) {
                        vecDirection = new Vector2d(-1, 0);
                    } else {
                        vecDirection = new Vector2d(1, 0);
                    }
                }
            }

            double pos = roundaboutCar.getRemainingLengthOfCurrentSectionInCm();

            if (vecDirection.getY() > 0 || vecDirection.getX() > 0) {
                // position of back of car
                pos = streetSection.getLength() - pos - roundaboutCar.getLengthInCM();
            }
            //else  position of front of car
            //pos = roundaboutCar.getRemainingLengthOfCurrentSectionInCm();

            return Math.min(pos, MAX_VEHICLE_STREET_LENGTH);
        }
        throw new IllegalArgumentException();
    }
}
