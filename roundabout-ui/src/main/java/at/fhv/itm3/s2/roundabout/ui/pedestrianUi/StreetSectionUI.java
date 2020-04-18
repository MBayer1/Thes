package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import javafx.scene.shape.Rectangle;

public class StreetSectionUI extends Rectangle {
    public StreetSectionUI(double x, double y, double width, double height){
        super(width, height);
        setX(x);
        setY(y);
        super.setStyle("-fx-fill: red; -fx-stroke: black; -fx-stroke-width: 5;");
    }
}
