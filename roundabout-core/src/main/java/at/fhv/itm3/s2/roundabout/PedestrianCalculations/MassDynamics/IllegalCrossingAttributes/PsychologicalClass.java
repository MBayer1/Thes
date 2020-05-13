package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.Category;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianBehaviour;

import java.util.LinkedList;
import java.util.List;

public class PsychologicalClass {

    List<Category> categoryList = new LinkedList<>();
    public PsychologicalClass(){
        categoryList.add(new Category("veryHeight",-1,10,0.5));
        categoryList.add(new Category("high",categoryList.get(categoryList.size()-1).getUpperLimit(),40,0.69));
        categoryList.add(new Category("medium",categoryList.get(categoryList.size()-1).getUpperLimit(),90,0.91));
        categoryList.add(new Category("low",categoryList.get(categoryList.size()-1).getUpperLimit(),100,0.97));
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
        return this.getProbability(((PedestrianBehaviour)(pedestrian.getPedestrianBehaviour())).getPsychologicalNature());
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
