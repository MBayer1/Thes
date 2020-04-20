package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import javafx.scene.paint.Color;

public class PedestrianStreetUI extends StreetSectionUI {

    private final static Color pedestrianStreetColor =  Color.RED;

    public PedestrianStreetUI(PedestrianStreetSection pedestrianStreetSection) {
        super(pedestrianStreetSection, pedestrianStreetColor);
    }
}
