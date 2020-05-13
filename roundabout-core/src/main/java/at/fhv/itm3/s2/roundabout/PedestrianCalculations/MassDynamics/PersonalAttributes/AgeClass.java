package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.Category;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianBehaviour;

import java.util.LinkedList;
import java.util.List;

public class AgeClass {
    List<Category> categoryList = new LinkedList<>();
    public AgeClass(){
        categoryList.add(new Category("lower16",-1,3.6,0.5));
        categoryList.add(new Category("lower35",categoryList.get(categoryList.size()-1).getUpperLimit(),71.3,0.68));
        categoryList.add(new Category("lower60",categoryList.get(categoryList.size()-1).getUpperLimit(),93.2,0.53));
        categoryList.add(new Category("higher60",categoryList.get(categoryList.size()-1).getUpperLimit(),100,0.44));
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
        return this.getProbability(((PedestrianBehaviour)(pedestrian.getPedestrianBehaviour())).getAgeClass());
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
