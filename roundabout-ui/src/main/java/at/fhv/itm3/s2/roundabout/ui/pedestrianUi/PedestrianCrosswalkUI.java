package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import javafx.scene.paint.Color;

public class PedestrianCrosswalkUI extends StreetSectionUI {

    private final static Color pedestrianCrosswalkColor =  Color.GRAY;

    public PedestrianCrosswalkUI(PedestrianStreetSection pedestrianStreetSection) {
        super(pedestrianStreetSection, pedestrianCrosswalkColor);
    }
}
