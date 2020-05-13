package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.Category;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianBehaviour;

import java.util.LinkedList;
import java.util.List;

public class DangerSenseClass {

    List<Category> categoryList = new LinkedList<>();
    public DangerSenseClass(){
        categoryList.add(new Category("veryHeight",-1,1,0.5));
        categoryList.add(new Category("high",categoryList.get(categoryList.size()-1).getUpperLimit(),13,0.51));
        categoryList.add(new Category("medium",categoryList.get(categoryList.size()-1).getUpperLimit(),16,0.57));
        categoryList.add(new Category("low",categoryList.get(categoryList.size()-1).getUpperLimit(),100,0.7));
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
        return this.getProbability(((PedestrianBehaviour)(pedestrian.getPedestrianBehaviour())).getDangerSenseClass());
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
