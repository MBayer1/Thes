package at.fhv.itm3.s2.roundabout.api.entity;

public class NeededSpaceForVehicle {
    private Double percentageOfVehicleThatCanLeave;
    private Double lengthOfMergeFactor;
    private Double neededSpaceForVehicle;

    public  NeededSpaceForVehicle(Double lengthOfMergeFactor,
                                  Double neededSpaceForVehicle) {
        this(0.0, lengthOfMergeFactor, neededSpaceForVehicle);
    }


    public  NeededSpaceForVehicle(Double percentageOfVehicleThatCanLeav,
                                  Double lengthOfMergeFactor,
                                  Double neededSpaceForVehicle) {
        setPercentageOfVehicleThatCanLeave(percentageOfVehicleThatCanLeav);
        setLengthOfMergeFactor(lengthOfMergeFactor);
        setNeededSpaceForVehicle(neededSpaceForVehicle);
    }

    /**
     * @param percentageOfVehicleThatCanLeave percentage of a vehicle that can leave the current section
     */
    public void setPercentageOfVehicleThatCanLeave(Double percentageOfVehicleThatCanLeave) {
        this.percentageOfVehicleThatCanLeave = percentageOfVehicleThatCanLeave;
    }

    /**
     * @param lengthOfMergeFactor total needed space in a previous roundabout section for a vehicle to enter roundabout
     */
    public void setLengthOfMergeFactor(Double lengthOfMergeFactor) {
        this.lengthOfMergeFactor = lengthOfMergeFactor;
    }

    /**
     * @param neededSpaceForVehicle total needed space for a vehicle to leave section fully, car length + comfort distance
     */
    public void setNeededSpaceForVehicle(Double neededSpaceForVehicle) {
        this.neededSpaceForVehicle = neededSpaceForVehicle;
    }

    /**
     * @return percentage of a vehicle that can leave the current section
     */
    public Double getNeededSpaceForVehicle() {
        return neededSpaceForVehicle;
    }

    /**
     * @return total needed space in a previous roundabout section for a vehicle to enter roundabout
     */
    public Double getLengthOfMergeFactor() {
        return lengthOfMergeFactor;
    }

    /**
     * @return total needed space for a vehicle to leave section fully, car length + comfort distance
    */
    public Double getPercentageOfVehicleThatCanLeave() {
        return percentageOfVehicleThatCanLeave;
    }
}
