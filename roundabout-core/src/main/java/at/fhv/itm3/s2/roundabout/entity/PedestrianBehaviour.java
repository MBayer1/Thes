package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm3.s2.roundabout.api.entity.CurrentMovementPedestrian;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrianBehaviour;

import javax.vecmath.Vector2d;

public class PedestrianBehaviour implements IPedestrianBehaviour {

    private final Double preferredSpeed;
    private final Double maxPreferredSpeed;
    private Vector2d previousSFMVector;
    private double currentSpeed;
    private final Double maxDistanceForWaitingArea;
    private double minDistanceToNextPedestrian;
    private double radiusOfPedestrian;
    private double accelerationFactor;
    private String genderClass;
    private String psychologicalNature;
    private String ageClass;
    private String dangerSenseClass;
    private String statusOfWalking;
    private CurrentMovementPedestrian currentMovementClass;


    public PedestrianBehaviour(double speed, double minDistanceToNextPedestrian, double radiusOfPedestrian,
                               String gender, String psychologicalNature, String ageRangeGroup,
                               String dangerSenseClass, Double preferredSpeed, Double maxPreferredSpeed,
                               Double maxDistanceForWaitingArea){
        this(speed, minDistanceToNextPedestrian, radiusOfPedestrian, 1, gender, psychologicalNature, ageRangeGroup, dangerSenseClass,
                preferredSpeed, maxPreferredSpeed, maxDistanceForWaitingArea);
    }

    public PedestrianBehaviour(double speed, double minDistanceToNextPedestrian, double radiusOfPedestrian, double accelerationFactor,
                               String gender, String psychologicalNature, String ageRangeGroup,
                               String dangerSenseClass, Double preferredSpeed, Double maxPreferredSpeed,
                               Double maxDistanceForWaitingArea)
            throws IllegalArgumentException {
        setCurrentSpeed(speed);
        this.genderClass = gender;
        this.psychologicalNature = psychologicalNature;
        this.ageClass = ageRangeGroup;
        this.dangerSenseClass = dangerSenseClass;
        this.minDistanceToNextPedestrian = minDistanceToNextPedestrian;
        this.radiusOfPedestrian = radiusOfPedestrian;
        this.accelerationFactor = accelerationFactor;
        this.dangerSenseClass = dangerSenseClass;
        // Extended of Pedestrian speed -> also include stress factor.
        this.preferredSpeed = preferredSpeed;
        this.maxPreferredSpeed = maxPreferredSpeed;
        this.maxDistanceForWaitingArea = maxDistanceForWaitingArea;
        this.currentSpeed = preferredSpeed;
        this.previousSFMVector = new Vector2d(0,0);
        this.currentMovementClass = CurrentMovementPedestrian.Walking;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentSpeed(double speed)
            throws IllegalArgumentException {
        if (speed >= 0) {
            this.currentSpeed = speed;
        } else {
            throw new IllegalArgumentException("Speed should be greater or equal than 0");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAccelerationFactor() {
        return accelerationFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAccelerationFactor(double accelerationFactor) {
        this.accelerationFactor = accelerationFactor;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public double getMinDistanceToNextPedestrian() {
        return minDistanceToNextPedestrian;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public double getRadiusOfPedestrian(){
        return radiusOfPedestrian;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calcGapForPedestrian() {
        return minDistanceToNextPedestrian + radiusOfPedestrian;
    }


    public String getAgeClass() {
        return ageClass;
    }

    public String getGenderClass() {
        return genderClass;
    }

    public String getPsychologicalNature() {
        return psychologicalNature;
    }

    public String getDangerSenseClass() {
        return dangerSenseClass;
    }

    public String getStatusOfWalking(){return statusOfWalking;}

    public void setStatusOfWalking( String statusOfWalking) {this.statusOfWalking = statusOfWalking;}

    public Double getMaxDistanceForWaitingArea() {
        return maxDistanceForWaitingArea;
    }

    public Double getPreferredSpeed() {
        return preferredSpeed;
    }

    public Vector2d getPreviousSFMVector() {
        return previousSFMVector;
    }

    public void setPreviousSFMVector(Vector2d previousSFMVector) {
        this.previousSFMVector = previousSFMVector;
    }

    public Double getMaxPreferredSpeed() {
        return maxPreferredSpeed;
    }

    public CurrentMovementPedestrian getCurrentMovmentClass() {
        return currentMovementClass;
    }

    public void setCurrentMovmentClass(CurrentMovementPedestrian currentMovementClass) {
        this.currentMovementClass = currentMovementClass;
    }
}


