package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.Category;

import java.util.LinkedList;
import java.util.List;

public class GenderClass {

    List<Category> categoryList = new LinkedList<>();
    public GenderClass(){
        categoryList.add(new Category("male",0.,33,0.5));
        categoryList.add(new Category("female",33.,35.,0.38));
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
