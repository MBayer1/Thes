package at.fhv.itm3.s2.roundabout.entity;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.DangerSenseClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.PsychologicalClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes.AgeClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes.GenderClass;
import at.fhv.itm3.s2.roundabout.api.entity.IPedestrianBehaviour;

public class PedestrianBehaviour implements IPedestrianBehaviour {

    private double speed;
    private double minDistanceToNextPedestrian;
    private double radiusOfPedestrian;
    private double accelerationFactor;
    private GenderClass genderClass;
    private PsychologicalClass psychologicalNature;
    private AgeClass ageClass;
    private DangerSenseClass dangerSenseClass;


    public PedestrianBehaviour(double speed, double minDistanceToNextPedestrian, double radiusOfPedestrian,
                               GenderClass gender, PsychologicalClass psychologicalNature, AgeClass ageRangeGroup,
                               DangerSenseClass dangerSenseClass){
        this(speed, minDistanceToNextPedestrian, radiusOfPedestrian, 1, gender, psychologicalNature, ageRangeGroup, dangerSenseClass);
    }

    public PedestrianBehaviour(double speed, double minDistanceToNextPedestrian, double radiusOfPedestrian, double accelerationFactor,
                               GenderClass gender, PsychologicalClass psychologicalNature, AgeClass ageRangeGroup,
                               DangerSenseClass dangerSenseClass)
            throws IllegalArgumentException {
        setSpeed(speed);
        this.genderClass = gender;
        this.psychologicalNature = psychologicalNature;
        this.ageClass = ageRangeGroup;
        this.dangerSenseClass = dangerSenseClass;
        this.minDistanceToNextPedestrian = minDistanceToNextPedestrian;
        this.radiusOfPedestrian = radiusOfPedestrian;
        this.accelerationFactor = accelerationFactor;
        this.dangerSenseClass = dangerSenseClass;
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


