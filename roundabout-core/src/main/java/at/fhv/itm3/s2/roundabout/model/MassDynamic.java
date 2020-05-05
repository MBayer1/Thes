package at.fhv.itm3.s2.roundabout.model;

import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.DangerSenseClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.IllegalCrossingAttributes.PsychologicalClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes.AgeClass;
import at.fhv.itm3.s2.roundabout.PedestrianCalculations.MassDynamics.PersonalAttributes.GenderClass;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;

public class MassDynamic {

    /**
     * Random number stream used to calculate Massdynamic settings.
     * See {@link RoundaboutSimulationModel#init()} method for stream parameters.
     */
    private ContDistUniform genderClass;
    private ContDistUniform ageClass;
    private ContDistUniform dangerSenseClass;
    private ContDistUniform psychologicalClass;

    public MassDynamic (Long simulationSeed, Model model){

        genderClass = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                1.0,
                true,
                false
        );
        genderClass.setSeed(simulationSeed);

        ageClass = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                1.0,
                true,
                false
        );
        ageClass.setSeed(simulationSeed);

        dangerSenseClass = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                1.0,
                true,
                false
        );
        dangerSenseClass.setSeed(simulationSeed);

        psychologicalClass = new ContDistUniform(
                model,
                "genderClass",
                0.0,
                1.0,
                true,
                false
        );
        psychologicalClass.setSeed(simulationSeed);


    }

    /**
     * Returns a sample of the random stream {@link ContDistNormal} used to determine mass dynamic properties
     *
     * @return a sample as the matching class type.
     */
    public GenderClass getRandomGenderClass() {
        return new GenderClass(genderClass.sample());
    }

    public AgeClass getRandomAgeClass() {
        return new AgeClass(ageClass.sample());
    }

    public DangerSenseClass getRandomDangerSenseClass() {
        return new DangerSenseClass(dangerSenseClass.sample());
    }

    public PsychologicalClass getRandomPsychologicalClass() {
        return new PsychologicalClass(psychologicalClass.sample());
    }

}
