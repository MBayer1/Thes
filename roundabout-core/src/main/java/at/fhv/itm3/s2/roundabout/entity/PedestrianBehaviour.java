package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm3.s2.roundabout.api.entity.AgeRangeGroup;
import at.fhv.itm3.s2.roundabout.api.entity.Gender;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrianBehaviour;
import at.fhv.itm3.s2.roundabout.api.entity.PsychologicalNature;

public class PedestrianBehaviour implements IPedestrianBehaviour {

    private double speed;
    private double minDistanceToNextPedestrian;
    private double maxDistanceToNextPedestrian; // TODO
    private double mergeFactor;
    private double accelerationFactor;
    private Gender gender;
    private PsychologicalNature psychologicalNature;
    private AgeRangeGroup ageRangeGroup;


    public PedestrianBehaviour(double speed, double minDistanceToNextPedestrian, double maxDistanceToNextPedestrian, double mergeFactor, double accelerationFactor,
                               Gender gender, PsychologicalNature psychologicalNature, AgeRangeGroup ageRangeGroup)
            throws IllegalArgumentException {
        setSpeed(speed);
        setGender(gender);
        setPsychologicalNature(psychologicalNature);
        setAgeRangeGroup(ageRangeGroup);
        this.minDistanceToNextPedestrian = minDistanceToNextPedestrian;
        this.maxDistanceToNextPedestrian = maxDistanceToNextPedestrian;
        this.mergeFactor = mergeFactor;
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
    public void setMinDistanceToNextPedestrian(double minDistanceToNextPedestrian)
            throws IllegalArgumentException {
        if (minDistanceToNextPedestrian > 0) {
            this.minDistanceToNextPedestrian = minDistanceToNextPedestrian;
        } else {
            throw new IllegalArgumentException("Min distance must be positive");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaxDistanceToNextPedestrian() {
        return maxDistanceToNextPedestrian;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxDistanceToNextPedestrian(double maxDistanceToNextPedestrian)
            throws IllegalArgumentException {
        if (maxDistanceToNextPedestrian > 0) {
            this.maxDistanceToNextPedestrian = maxDistanceToNextPedestrian;
        } else {
            throw new IllegalArgumentException("Max distance must be positive");
        }
    }
}


