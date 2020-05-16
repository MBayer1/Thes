package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.Category;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianBehaviour;

import java.util.LinkedList;
import java.util.List;

public class StoppingWhileCrossingClass {

    List<Category> categoryList = new LinkedList<>();

    public StoppingWhileCrossingClass(){
        categoryList.add(new Category("StoppingWhileCrossing",-1,0.156,0.5));
        categoryList.add(new Category("NotStoppingWhileCrossing",categoryList.get(categoryList.size()-1).getUpperLimit(),100,0.36));
    }

    public double getProbability (String type){
        for ( int i = 0; i < categoryList.size(); ++i) {
            if ( categoryList.get(i).getTypeKey().equals(type))
                return categoryList.get(i).getProbabilityForCrossingIllegal();
        }
        throw new IllegalArgumentException("Category does not exist for MassDynamic.");
    }

    public double getProbability (Pedestrian pedestrian){
        if(!(pedestrian.getPedestrianBehaviour() instanceof PedestrianBehaviour)){
            throw new IllegalArgumentException("PedestrianBehaviour not instance of PedestrianBehaviour .");
        }
        return this.getProbability( getTypeByDetermineValue(pedestrian.getRoundaboutModel().getRandomPedestrianStopWhileIllegalCrossing()));
    }

    public String getTypeByDetermineValue (double valueToDetermineClass){
        for ( int i = 0; i < categoryList.size(); ++i) {
            if ( valueToDetermineClass > categoryList.get(i).getLowerLimit() && valueToDetermineClass <= categoryList.get(i).getUpperLimit()){
                return categoryList.get(i).getTypeKey();
            }
        }
        throw new IllegalArgumentException("Category does not exist for MassDynamic.");
    }
}
