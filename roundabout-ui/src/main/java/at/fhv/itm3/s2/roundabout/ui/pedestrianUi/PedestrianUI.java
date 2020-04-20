package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;

public class PedestrianUI extends Circle {

    public PedestrianUI(double x, double y){
        super(x, y, PedestrianUIUtils.PEDESTRIAN_WIDTH);
        setFill(PedestrianUIUtils.PEDESTRIAN_COLOR);
    }
}
