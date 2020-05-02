package at.fhv.itm3.s2.roundabout.api.entity;

import desmoj.core.simulator.Model;

public class TrafficLight {
    private Boolean active;
    private Boolean isFreeToGo;
    private Boolean triggersByJam;
    private Long minGreenPhaseDuration;
    private Long greenPhaseDuration;
    private Long redPhaseDuration;
    private Double redPhaseStartTimeStamp;
    Model model;


    public TrafficLight(Boolean active, Boolean isTrafficJam, Long minGreenPhaseDuration, Long redPhaseDuration, Model model) {
        this(active, true, minGreenPhaseDuration, null, redPhaseDuration, model);
    }

    public TrafficLight(
            Boolean active,
            Long greenPhaseDuration,
            Long redPhaseDuration,
            Model model
    ) {
        this(active, true, null, greenPhaseDuration, redPhaseDuration, model);
    }

    public TrafficLight(
            Boolean active,
            Long minGreenPhaseDuration,
            Long greenPhaseDuration,
            Long redPhaseDuration,
            Model model
    ) {
        this(active, true, minGreenPhaseDuration, greenPhaseDuration, redPhaseDuration, model);
    }


    public TrafficLight(
            Boolean active,
            Boolean isFreeToGo,
            Long minGreenPhaseDuration,
            Long greenPhaseDuration,
            Long redPhaseDuration,
            Model model
    ) {

        this.active = active;
        this.isFreeToGo = isFreeToGo;

        if (greenPhaseDuration == null && active) {
            this.triggersByJam = true;
        } else {
            this.triggersByJam = false;
        }

        if (redPhaseDuration == null) {
            active = false;
        }

        this.minGreenPhaseDuration = minGreenPhaseDuration;
        this.greenPhaseDuration = greenPhaseDuration;
        this.redPhaseDuration = redPhaseDuration;

        this.redPhaseStartTimeStamp = 0.;
        this.model = model;
    }

    public boolean isActive() {
        return active != null ? active : false;
    }

    public boolean isFreeToGo() {
        return isFreeToGo != null ? isFreeToGo : true;
    }

    public void setFreeToGo(boolean isFreeToGo) {
        if (!this.isActive()) {
            throw new IllegalStateException("cannot set state of inactive traffic light");
        }

        this.isFreeToGo = isFreeToGo;
    }

    public boolean isTriggeredByJam() {
        return triggersByJam != null ? triggersByJam : false;
    }

    public long getGreenPhaseDuration() {
        return greenPhaseDuration != null ? greenPhaseDuration : 0;
    }

    public long getRedPhaseDuration() {
        return redPhaseDuration != null ? redPhaseDuration : 0;
    }

    public double getMinGreenPhaseDuration() {
        return minGreenPhaseDuration != null ? minGreenPhaseDuration : 0;
    }

    public double getRemainingRedPhase () {
        if (this.redPhaseStartTimeStamp.equals(0)) {
            throw new IllegalStateException("Traffic light is not red or does not use TimeStamp");
        }
        if(isActive() && !isFreeToGo){
            //red Phase
            double end = this.model.currentModel().getExperiment().getSimClock().getTime().getTimeAsDouble(
                    this.model.currentModel().getExperiment().getReferenceUnit());
            return (redPhaseDuration - (end-this.redPhaseStartTimeStamp));
        }
        return 0.;
    }

    public void setRedPhaseStartTimeStamp() {
        this.redPhaseStartTimeStamp = this.model.currentModel().presentTime().getTimeAsDouble();
    }

    public void resetRedPhaseStartTimeStamp() {
        this.redPhaseStartTimeStamp = 0.;
    }
}
