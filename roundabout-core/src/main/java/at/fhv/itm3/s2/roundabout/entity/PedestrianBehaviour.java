package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm3.s2.roundabout.api.entity.AgeRangeGroup;
import at.fhv.itm3.s2.roundabout.api.entity.Gender;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrianBehaviour;
import at.fhv.itm3.s2.roundabout.api.entity.PsychologicalNature;

public class PedestrianBehaviour implements IPedestrianBehaviour {

    private double speed;
    private double minDistanceToNextPedestrian;
    private double radiusOfPedestrian;
    private double accelerationFactor;
    private Gender gender;
    private PsychologicalNature psychologicalNature;
    private AgeRangeGroup ageRangeGroup;


    public PedestrianBehaviour(double speed, double minDistanceToNextPedestrian, double radiusOfPedestrian,
                               Gender gender, PsychologicalNature psychologicalNature, AgeRangeGroup ageRangeGroup){
        this(speed, minDistanceToNextPedestrian, radiusOfPedestrian, 1, gender, psychologicalNature, ageRangeGroup);
    }

    public PedestrianBehaviour(double speed, double minDistanceToNextPedestrian, double radiusOfPedestrian, double accelerationFactor,
                               Gender gender, PsychologicalNature psychologicalNature, AgeRangeGroup ageRangeGroup)
            throws IllegalArgumentException {
        setSpeed(speed);
        setGender(gender);
        setPsychologicalNature(psychologicalNature);
        setAgeRangeGroup(ageRangeGroup);
        this.minDistanceToNextPedestrian = minDistanceToNextPedestrian;
        this.radiusOfPedestrian = radiusOfPedestrian;
        this.accelerationFactor = accelerationFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSpeed() {
        return speed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeed(double speed)
            throws IllegalArgumentException {
        if (speed >= 0) {
            this.speed = speed;
        } else {
            throw new IllegalArgumentException("Speed should be greater or equal than 0");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Gender getGender() {
        return gender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGender(Gender gender)
            throws IllegalArgumentException {
        this.gender = gender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PsychologicalNature getPsychologicalNature() {
        return psychologicalNature;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPsychologicalNature(PsychologicalNature psychologicalNature)
            throws IllegalArgumentException {
        this.psychologicalNature = psychologicalNature;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgeRangeGroup getAgeRangeGroup() {
        return ageRangeGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgeRangeGroup(AgeRangeGroup ageRangeGroup)
            throws IllegalArgumentException {
        this.ageRangeGroup = ageRangeGroup;


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
}


