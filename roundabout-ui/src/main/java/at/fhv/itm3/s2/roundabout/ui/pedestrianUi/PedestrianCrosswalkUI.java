package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PedestrianCrosswalkUI extends StreetSectionUI {

    private final static Color pedestrianCrosswalkColor =  Color.GRAY;


    public PedestrianCrosswalkUI(PedestrianStreetSection pedestrianStreetSection, Pane canvas) {
        super(pedestrianStreetSection, pedestrianCrosswalkColor);
        Rectangle rectangle = new Rectangle(30, 30, 50, 50);
        rectangle.setFill(Color.GREEN);
        canvas.getChildren().add(rectangle);
    }
//    public PedestrianCrosswalkUI(CarStreetUI carStreetUI, Pane canvas) {
//        super(pedestrianStreetSection, pedestrianCrosswalkColor);
//        Rectangle rectangle = new Rectangle(30, 30, 50, 50);
//        rectangle.setFill(Color.GREEN);
//        canvas.getChildren().add(rectangle);
//    }
}
