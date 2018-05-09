package at.fhv.itm3.s2.roundabout.api.entity;

public class VehicleOnStreetSection {
    private Double vehiclePositionOnStreetSection;
    private Double percentageOfVehicleLength;

    public VehicleOnStreetSection(Double vehiclePositionOnStreetSection, Double percentageOfVehicleLength) {
        setVehiclePositionOnStreetSection(vehiclePositionOnStreetSection);
        setPercentageOfVehicleLength(percentageOfVehicleLength);
    }

    public Double getVehiclePositionOnStreetSection() {return vehiclePositionOnStreetSection;}
    public Double getPercentageOfVehicleLength () {return  percentageOfVehicleLength;}

    /**
     * @return Position of vehicle on street section (@Link Street).
     * */
    public void setVehiclePositionOnStreetSection( Double vehiclePositionOnStreetSection)
    throws IllegalArgumentException {
        if(vehiclePositionOnStreetSection >= 0) {
            this.vehiclePositionOnStreetSection = vehiclePositionOnStreetSection;
        } else {
            throw new IllegalArgumentException("Position of vehicle on StreetSection must be positive.");
        }
    }

    /**
     * Due to this a vehicle can be on multiple (two) street sections (@Link Street) as the same time
     *
     * @return Percentage of vehicle on current street section (@Link Street).
     * */
    public void setPercentageOfVehicleLength ( Double percentageOfVehicleLength) throws IllegalArgumentException {
        if (percentageOfVehicleLength >= 0) {
            this.percentageOfVehicleLength = percentageOfVehicleLength;
        } else {
            throw new IllegalArgumentException("Percentage of vehicle on StreetSection must be positive.");
        }
    }
}
