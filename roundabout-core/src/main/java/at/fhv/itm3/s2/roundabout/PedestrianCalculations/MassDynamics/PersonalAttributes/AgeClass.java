package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes;

public class AgeClass {

    public AgeClass(double valueToDetermeClass) {}

    public double getProbability (double age){
        if( age < 16 ){
            return 0.5;
        } else if ( age >= 16 && age < 35) {
            return 0.68;
        } else if (age >= 35 && age < 60) {
            return 0.53;
        }// else if ( age >= 60) {
        return 0.44;
    }
}
