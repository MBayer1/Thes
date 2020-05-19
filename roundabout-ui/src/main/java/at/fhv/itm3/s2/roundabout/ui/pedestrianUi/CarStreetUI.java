package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import javafx.scene.paint.Color;

public class CarStreetUI extends StreetSectionUI {

    private final static Color carStreetColor =  Color.rgb(0xEF, 0xEF,0xEF);
    private final static String rectStyle = "-fx-stroke: black; -fx-stroke-width: 5;";

    public CarStreetUI(double x, double y, double width, double height, String uuid ){
        super(x, y, width, height, carStreetColor, uuid, rectStyle);
    }
}
