package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import javafx.scene.paint.Color;

public class CarStreetUI extends StreetSectionUI {

    private final static Color carStreetColor =  Color.YELLOW;

    public CarStreetUI(PedestrianStreetSection pedestrianStreetSection) {
        super(pedestrianStreetSection, carStreetColor);
    }
}
