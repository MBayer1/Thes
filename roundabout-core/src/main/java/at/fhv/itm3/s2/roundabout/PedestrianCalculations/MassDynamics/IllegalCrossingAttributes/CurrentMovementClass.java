package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianBehaviour;
import at.fhv.itm3.s2.roundabout.api.entity.CurrentMovementPedestrian;

import java.util.HashMap;
import java.util.Map;

public class CurrentMovementClass {
    Map<CurrentMovementPedestrian, Double> categoryList = new HashMap<>();

    public CurrentMovementClass(){
        categoryList.put(CurrentMovementPedestrian.Walking,0.5);
        categoryList.put(CurrentMovementPedestrian.WalkingAfterRunning,0.54);
        categoryList.put(CurrentMovementPedestrian.RunningAfterWalking,0.45);
        categoryList.put(CurrentMovementPedestrian.Running,0.6);
    }

    public double getProbability (CurrentMovementPedestrian type){
        return categoryList.get(type);
    }

    public double getProbability (Pedestrian pedestrian){
        if(!(pedestrian.getPedestrianBehaviour() instanceof PedestrianBehaviour)){
            throw new IllegalArgumentException("PedestrianBehaviour not instance of PedestrianBehaviour .");
        }
        return getProbability(((PedestrianBehaviour) pedestrian.getPedestrianBehaviour()).getCurrentMovmentClass());
    }

}
