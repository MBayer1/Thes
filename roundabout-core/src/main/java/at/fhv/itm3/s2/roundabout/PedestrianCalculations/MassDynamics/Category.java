package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics;

public class Category {
    String typeKey;
    double lowerLimit;
    double upperLimit;
    double probabilityForCrossingIllegal;

    public Category (String typeKey, double lowerLimit,  double upperLimit, double probabilityForCrossingIllegal) {
        this.typeKey = typeKey;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.probabilityForCrossingIllegal = probabilityForCrossingIllegal;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    public double getProbabilityForCrossingIllegal() {
        return probabilityForCrossingIllegal;
    }

    public String getTypeKey() {
        return typeKey;
    }
}
