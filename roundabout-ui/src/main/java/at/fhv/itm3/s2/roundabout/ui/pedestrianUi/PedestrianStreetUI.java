package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import javafx.scene.paint.Color;

public class PedestrianStreetUI extends StreetSectionUI {

    private final static Color pedestrianStreetColor =  Color.rgb(0xE2, 0xEF,0xD9);

    public PedestrianStreetUI(PedestrianStreetSection pedestrianStreetSection) {
        super(pedestrianStreetSection, pedestrianStreetColor);
    }
}
