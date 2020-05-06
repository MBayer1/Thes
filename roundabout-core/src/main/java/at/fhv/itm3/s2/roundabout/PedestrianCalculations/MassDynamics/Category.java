package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics;

public class Category {
    String typeKey;
    double lowerLimit;
    double upperLimit;
    double propabilityForCrossingIllegal;

    public Category (String typeKey, double lowerLimit,  double upperLimit, double probabilityForCrossingIllegal) {
        this.typeKey = typeKey;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.propabilityForCrossingIllegal = probabilityForCrossingIllegal;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    public double getPropabilityForCrossingIllegal() {
        return propabilityForCrossingIllegal;
    }

    public String getTypeKey() {
        return typeKey;
    }
}
