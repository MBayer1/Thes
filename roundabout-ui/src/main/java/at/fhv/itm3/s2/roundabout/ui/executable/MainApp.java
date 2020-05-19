package at.fhv.itm3.s2.roundabout.ui.executable;


import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.controller.CarController;
import at.fhv.itm3.s2.roundabout.model.RoundaboutSimulationModel;
import at.fhv.itm3.s2.roundabout.entity.*;
import at.fhv.itm3.s2.roundabout.ui.controllers.MainViewController;
import at.fhv.itm3.s2.roundabout.ui.pedestrianUi.PedestrianUIMain;
import at.fhv.itm3.s2.roundabout.ui.pedestrianUi.PedestrianUIUtils;
import at.fhv.itm3.s2.roundabout.ui.util.ViewLoader;
import at.fhv.itm3.s2.roundabout.util.ConfigParser;
import at.fhv.itm3.s2.roundabout.util.dto.ModelConfig;
import at.fhv.itm3.s2.roundabout.util.dto.*;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimClock;
import desmoj.core.simulator.TimeInstant;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is Utility class which starts the whole application.
 */
public class MainApp extends Application {

    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DEFAULT_TITLE = "TRAFSIM";

    private static final int DEFAULT_WIDTH = 1460;
    private static final int DEFAULT_HEIGHT = 750;

    private static final String PATH_TO_DEFAULT_CSS_FILE = "/at/fhv/itm3/s2/roundabout/ui/css/main.css";
    private static final String PATH_TO_MODEL_FILE = "/at/fhv/itm3/s2/roundabout/model/model_dornbirn_sued_with_intersection_and_pedestrian.xml"; // use for gui test
    //private static final String PATH_TO_MODEL_FILE = "/at/fhv/itm3/s2/roundabout/model/model_dornbirn_sued_with_intersection_and_pedestrian_NotConnected.xml";
    //private static final String PATH_TO_MODEL_FILE = "/at/fhv/itm3/s2/roundabout/model/model_dornbirn_sued_with_intersection_and_pedestrian_NoTrafficLight.xml";

    private static final double EXPERIMENT_STOP_TIME = 60 * 60 * 24 * 1; // equates to number of days in seconds, minutes * seconds * hours * days
     //private static final double EXPERIMENT_STOP_TIME = 60 * 60 ; // equates to number of days in seconds, minutes * seconds * hours * days
    private static final TimeUnit EXPERIMENT_TIME_UNIT = TimeUnit.SECONDS;

    private static final boolean IS_TRACE_ENABLED = false;
    private static final boolean IS_DEBUG_ENABLED = false;

    /**
     * Default (empty) constructor for this utility class.
     */
    public MainApp() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage initStage) {
        try {
            ViewLoader<MainViewController> viewLoader = ViewLoader.loadView(MainViewController.class);
            Parent mainStage = (Parent) viewLoader.loadNode();

            final MainViewController mainViewController = viewLoader.getController();
            prepareNewStage(mainStage).show();

            final ConfigParser configParser = new ConfigParser(PATH_TO_MODEL_FILE);
            final ModelConfig modelConfig = configParser.loadConfig();

            final Experiment experiment = new Experiment("Pedestrian experiment");
            experiment.setShowProgressBar(false);
            configParser.initRoundaboutStructure(modelConfig, experiment);

            // pedestrian gui
            PedestrianUIMain pedestrianUIMain = new PedestrianUIMain(0, 0, 200, 600, configParser);
            ((RoundaboutSimulationModel)(experiment.getModel())).setPedestrianUIMain(pedestrianUIMain);
            Scene scene = new Scene(pedestrianUIMain, PedestrianUIUtils.MAIN_WINDOW_WIDTH, PedestrianUIUtils.MAIN_WINDOW_HEIGHT);
            initStage.setScene(scene);
            initStage.show();

            mainViewController.generateComponentStatContainers(
                modelConfig.getComponents().getComponent(),
                configParser.getSectionRegistry(),
                configParser.getSinkRegistry(),
                configParser.getPedestrianSectionRegistry(),
                configParser.getPedestrianSinkRegistry()
            );

            mainViewController.setStartRunnable(initExperimentRunnable(
                experiment,
                mainViewController::getCurrentSimSpeed,
                mainViewController::setProgress,
                    modelConfig,
                    configParser
            ));
            mainViewController.setFinishRunnable(() -> {
                if (IS_TRACE_ENABLED || IS_DEBUG_ENABLED) {
                    // Should be wrapped into if guard to prevent NPE when trace / debug are disabled above.
                    experiment.report();
                }
                experiment.finish();
                CarController.clear();
            });
            mainViewController.setPauseRunnable(experiment::stop);
            mainViewController.setDoStepRunnable(() -> {
                final double stopTime = experiment.getSimClock().getTime().getTimeAsDouble(experiment.getReferenceUnit()) + 1;
                experiment.stop(new TimeInstant(stopTime, experiment.getReferenceUnit()));
                experiment.proceed();
            });
            mainViewController.setProceedRunnable(() -> {
                experiment.stop(new TimeInstant(EXPERIMENT_STOP_TIME, experiment.getReferenceUnit()));
                experiment.proceed();
            });

        } catch (Throwable t) {
            LOGGER.error("Error occurred during start of the application.", t);
        }
    }

    private Runnable initExperimentRunnable(
        Experiment experiment,
        Supplier<Double> executionSpeedRateSupplier,
        Consumer<Double> progressConsumer,
        ModelConfig modelConfig,
        ConfigParser configParser
    ) {
        return () -> {
            Experiment.setReferenceUnit(EXPERIMENT_TIME_UNIT);

            // Just to be sure everything is initialised as expected.
            final Model model = experiment.getModel();
            model.reset();
            model.init();

            experiment.stop(new TimeInstant(
                EXPERIMENT_STOP_TIME,
                experiment.getReferenceUnit()
            ));

            if (IS_TRACE_ENABLED) {
                experiment.tracePeriod(
                    new TimeInstant(0),
                    new TimeInstant(70, experiment.getReferenceUnit())
                );
            }

            if (IS_DEBUG_ENABLED) {
                experiment.tracePeriod(
                    new TimeInstant(0),
                    new TimeInstant(70, experiment.getReferenceUnit())
                );
            }

            final SimClock simClock = experiment.getSimClock();
            simClock.addObserver((o, arg) -> {
                final double progress = simClock.getTime().getTimeAsDouble(experiment.getReferenceUnit()) / EXPERIMENT_STOP_TIME;
                progressConsumer.accept(progress);
            });

            //set real time, default value is 0
            experiment.setExecutionSpeedRate(executionSpeedRateSupplier.get());

            final long time = System.currentTimeMillis();
            System.out.println("Start: " + System.currentTimeMillis());

            // Starting experiment
            experiment.start();

            final long finishTime = System.currentTimeMillis();
            System.out.println("Logic Stop: " + finishTime);
            System.out.println("Time:" + (finishTime - time));

            double meanAddWait = 0;
            double maxAddWait = 0;
            double minAddWait = Double.MAX_VALUE;
            int cntCarTmp = 0;
            int cntCar = 0;
            double timeBetweenEventCall = 0;
            // collect all additional waiting times for vehicle due to illegal crossing of pedestrians.
            for ( Component component : modelConfig.getComponents().getComponent() ) {
                if(component.getType().equals(ComponentType.INTERSECTION) || component.getType().equals(ComponentType.ROUNDABOUT)) {
                    for ( Section sectionDTO : component.getSections().getSection()) {
                        StreetSection section = configParser.getSectionRegistry().get(component.getId()).get(sectionDTO.getId());
                        if (section.doesHavePedestrianCrossingToEnter()) {
                            cntCarTmp = 0;
                            for (ICar icar : section.getCarQueue()) {
                                if ( icar instanceof RoundaboutCar) {
                                        RoundaboutCar car = (RoundaboutCar) icar;
                                        ++cntCarTmp;
                                        double dPreviousRate = ((double) cntCarTmp - 1) / (double) cntCarTmp;
                                        maxAddWait = Math.max(maxAddWait, car.getMinTimeWaitingDueToIllegalCrossingOfPedestrian());
                                        minAddWait = Math.max(minAddWait, car.getMaxTimeWaitingDueToIllegalCrossingOfPedestrian());
                                        meanAddWait = meanAddWait * dPreviousRate + car.getMeanTimeWaitingDueToIllegalCrossingOfPedestrian() / cntCarTmp;
                                        cntCar += section.getNrOfEnteredCars();
                                    }
                                }
                            }
                    }
                }
            }

             for ( Component component : modelConfig.getComponents().getComponent() ) {
                if(component.getType().equals(ComponentType.INTERSECTION) || component.getType().equals(ComponentType.ROUNDABOUT)) {
                    for ( Sink sectionDTO : component.getSinks().getSink()) {
                        RoundaboutSink section = configParser.getSinkRegistry().get(component.getId()).get(sectionDTO.getId());
                        if (section.getNrOfEnteredCars() == 0) continue;

                        maxAddWait = Math.max(maxAddWait, section.getMinTimeWaitingDueToIllegalCrossingOfPedestrian());
                        minAddWait = Math.min(minAddWait, section.getMaxTimeWaitingDueToIllegalCrossingOfPedestrian());

                        double dPreviousRate = ((double)section.getNrOfEnteredCars()-1)/ (double) section.getNrOfEnteredCars();
                        meanAddWait = meanAddWait * dPreviousRate + section.getMeanTimeWaitingDueToIllegalCrossingOfPedestrian()/ section.getNrOfEnteredCars();
                    }
                }
            }

            System.out.println("------------------" );
            System.out.println("Additional waiting times for vehicle due to illegal crossing of pedestrians.: " + meanAddWait);
            System.out.println("Min additional waiting times for vehicle due to illegal crossing of pedestrians.: " + minAddWait);
            System.out.println("Max additional waiting times for vehicle due to illegal crossing of pedestrians.: " + maxAddWait);
            System.out.println("Number of Vehicle that crossed pedestrianCrossings: " + cntCar);
            System.out.println("------------------" );

            double cntPedestrian = 0;
            double meanTimeGapBetEvent_ReachedAim = 0;
            double meanTimeGapBetEvent_Generation = 0;
            for ( Component component : modelConfig.getComponents().getComponent() ) {
                if(component.getType().equals(ComponentType.PEDESTRIANWALKINGAREA) ) {
                    for ( Sink sectionDTO : component.getSinks().getSink()) {
                        PedestrianSink section = configParser.getPedestrianSinkRegistry().get(component.getId()).get(sectionDTO.getId());

                        double dPreviousRate = ((double)section.getNrOfEnteredPedestrians()-1)/ (double) section.getNrOfEnteredPedestrians();
                        meanTimeGapBetEvent_ReachedAim = meanTimeGapBetEvent_ReachedAim * dPreviousRate + section.getMeanTimeBetweenEventCall_ReachedAim()/ section.getNrOfEnteredPedestrians();
                        meanTimeGapBetEvent_Generation = meanTimeGapBetEvent_Generation * dPreviousRate + section.getMeanTimeBetweenEventCall_Generation()/ section.getNrOfEnteredPedestrians();
                    }
                }
            }

            for ( Component component : modelConfig.getComponents().getComponent() ) {
                if(component.getType().equals(ComponentType.PEDESTRIANWALKINGAREA)) {
                    for ( Section sectionDTO : component.getSections().getSection()) {
                        PedestrianStreetSection section = configParser.getPedestrianSectionRegistry().get(component.getId()).get(sectionDTO.getId());
                        if( section.isPedestrianCrossing() ) {
                            cntPedestrian += section.getNrOfEnteredPedestrians();
                        }
                    }
                }
            }
            System.out.println("Number of Pedestrians that crosses illegal.: " + cntPedestrian);
            System.out.println("Generation Rate of Pedestrian.: " + meanTimeGapBetEvent_Generation);
        };
    }

    /**
     * Displays the new stage for the application.
     *
     * @param pane node to be shown.
     * @return returns instance of the stage
     */
    private Stage prepareNewStage(Parent pane) {
        Scene scene = new Scene(pane, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.getStylesheets().add(PATH_TO_DEFAULT_CSS_FILE);

        Stage primaryStage = new Stage();
        primaryStage.setScene(scene);
        primaryStage.setTitle(DEFAULT_TITLE);

        return primaryStage;
    }

    /**
     * Default main method. Starts "this" application.
     *
     * @param args the command line arguments passed to the application.
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
