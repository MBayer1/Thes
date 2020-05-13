package at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.CurrentMovementClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.DangerSenseClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.PsychologicalClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.StoppingWhileCrossingClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes.AgeClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes.GenderClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes.GroupeSizeCrossingClass;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;

public class MassDynamic {

    /**
     * Random number stream used to calculate Massdynamic settings.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform genderClassSeed;
    private ContDistUniform ageClassSeed;
    private ContDistUniform dangerSenseClassSeed;
    private ContDistUniform psychologicalClassSeed;

    private GenderClass genderClass;
    private AgeClass ageClass;
    private DangerSenseClass dangerSenseClass;
    private PsychologicalClass psychologicalClass;

    private GroupeSizeCrossingClass groupeSizeCrossingClass;
    private StoppingWhileCrossingClass stoppingWhileCrossingClass;
    private CurrentMovementClass currentMovementClass;

    public MassDynamic(Long simulationSeed, Model model){

        genderClassSeed = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                100,
                true,
                false
        );
        genderClassSeed.setSeed(simulationSeed);

        ageClassSeed = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                100,
                true,
                false
        );
        ageClassSeed.setSeed(simulationSeed);

        dangerSenseClassSeed = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                100,
                true,
                false
        );
        dangerSenseClassSeed.setSeed(simulationSeed);

        psychologicalClassSeed = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                100.0,
                true,
                false
        );
        psychologicalClassSeed.setSeed(simulationSeed);

        this.genderClass = new GenderClass();
        this.ageClass = new AgeClass();
        this.dangerSenseClass = new DangerSenseClass();
        this.psychologicalClass = new PsychologicalClass();

        this.groupeSizeCrossingClass = new GroupeSizeCrossingClass();
        this.stoppingWhileCrossingClass = new StoppingWhileCrossingClass();
        this.currentMovementClass = new CurrentMovementClass();

    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine mass dynamic properties
     *
     * @return a sample as the matching class type.
     */
    public String getRandomGenderClass() {
        double sample = genderClassSeed.sample();
        return genderClass.getTypeByDetermineValue(sample);
    }

    public String getRandomAgeClass() {
        double sample = ageClassSeed.sample();
        return ageClass.getTypeByDetermineValue(sample);
    }

    public String getRandomDangerSenseClass() {
        double sample = dangerSenseClassSeed.sample();
        return dangerSenseClass.getTypeByDetermineValue(sample);
    }

    public String getRandomPsychologicalClass() {
        double sample = psychologicalClassSeed.sample();
        return psychologicalClass.getTypeByDetermineValue(sample);
    }


    public double getProbability(Pedestrian pedestrian){
        double sum = 0;
        double tmp = ageClass.getProbability(pedestrian);
        sum += tmp;
        tmp = genderClass.getProbability(pedestrian);
        sum += tmp;
        tmp = dangerSenseClass.getProbability(pedestrian);
        sum += tmp;
        tmp = psychologicalClass.getProbability(pedestrian);
        sum += tmp;
        tmp = groupeSizeCrossingClass.getProbability(pedestrian);
        sum += tmp;
        tmp = currentMovementClass.getProbability(pedestrian);
        sum += tmp;
        //tmp = stoppingWhileCrossingClass.getProbability(pedestrian);
        //sum += tmp;
        return sum/7;
    }


    public boolean doCrossing(Pedestrian pedestrian){
        return (getProbability(pedestrian) <
                pedestrian.getRoundaboutModel().getRandomMassDynamicsTriggersEventPedestrians()) ? true : false;
    }
}
