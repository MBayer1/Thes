package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.Category;

import java.util.LinkedList;
import java.util.List;

public class DangerSenseClass {

    List<Category> categoryList = new LinkedList<>();
    public DangerSenseClass(){
        categoryList.add(new Category("veryHeight",0.,2,0.5));
        categoryList.add(new Category("high",2.,14,0.51));
        categoryList.add(new Category("medium",14,16,0.57));
        categoryList.add(new Category("low",16,101,0.7));
    }

    public double getProbability (String type){
        for ( int i = 0; i < categoryList.size(); ++i) {
            if ( categoryList.get(i).getTypeKey().equals(type))
                return categoryList.get(i).getPropabilityForCrossingIllegal();
        }
        throw new IllegalArgumentException("Category does not exist for MassDynamic.");
    }


    public String getTypeByDetermineValue (double valueToDetermineClass){
        for ( int i = 0; i < categoryList.size(); ++i) {
            if ( valueToDetermineClass >= categoryList.get(i).getLowerLimit() && valueToDetermineClass < categoryList.get(i).getUpperLimit()){
                return categoryList.get(i).getTypeKey();
            }
        }
        throw new IllegalArgumentException("Category does not exist for MassDynamic.");
    }
}
