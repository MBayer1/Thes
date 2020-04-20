package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class StreetSectionUI extends Rectangle {
    public String uuid = null;

    public StreetSectionUI(PedestrianStreetSection pedestrianStreetSection, Color color){
        super(pedestrianStreetSection.getLengthX(), pedestrianStreetSection.getLengthY());
        setX(pedestrianStreetSection.getGlobalCoordinateOfSectionOrigin().getX());
        setY(pedestrianStreetSection.getGlobalCoordinateOfSectionOrigin().getY());
        setFill(color);
        this.uuid = pedestrianStreetSection.getId();
        //super.setStyle("-fx-fill: red; -fx-stroke: black; -fx-stroke-width: 5;");
    }
}
