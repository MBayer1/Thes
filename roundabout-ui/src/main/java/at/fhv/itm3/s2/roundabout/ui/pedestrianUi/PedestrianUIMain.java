package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm14.trafsim.model.entities.IConsumer;
import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
import at.fhv.itm3.s2.roundabout.entity.RoundaboutCar;
import at.fhv.itm3.s2.roundabout.entity.StreetSection;
import at.fhv.itm3.s2.roundabout.util.ConfigParser;
import desmoj.core.simulator.SimClock;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static javafx.scene.paint.Color.BROWN;

public class PedestrianUIMain extends ScrollPane implements IPedestrianUIMain {
    private  int width;
    private int height;
    private int positionX;
    private int positionY;
    private double centerX;
    private double centerY;

    private ConfigParser configParser = null;

    //pedestrian maps
    private Map<String, Map<String, PedestrianUI>> pedestrianMap = new HashMap<String, Map<String, PedestrianUI>>();
    private Map<String, PedestrianUI> globalPedestrianMap = new HashMap<String, PedestrianUI>();

    //car maps
    private Map<StreetSection, CarStreetUI> carStreetUIMap = new HashMap<>();
    private Map<StreetSection, Map<String, CarUI>> localCarUIMap = new HashMap<>();

    Pane canvas = new Pane();

    public PedestrianUIMain(int posX, int posY, int width, int height, ConfigParser configParser){
        super();
        this.width = width;
        this.height = height;
        this.positionX = posX;
        this.positionY = posY;
        this.configParser = configParser;

        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);

        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);

        Scale scale = new Scale();
        scale.setX(PedestrianUIUtils.SCALE_FACTOR);
        scale.setY(-PedestrianUIUtils.SCALE_FACTOR);


        scale.pivotYProperty().bind(Bindings.createDoubleBinding(() ->
                        canvas.getBoundsInLocal().getMinY() + canvas.getBoundsInLocal().getHeight()/2,
                canvas.boundsInLocalProperty()));

        canvas.getTransforms().add(scale);

        setLayoutX(posX);
        setLayoutY(posY);
        setStyle("-fx-background-color: red;");
        pannableProperty().set(true);
        setPannable(true);
        traverseComponents(configParser);

        centerCanvas(canvas);
        setContent(canvas);
    }

    private void traverseComponents(ConfigParser configParser){
        Map<String, Map<String, PedestrianStreetSection>> pedestrianStreetSection =  configParser.getPedestrianSectionRegistry();
        for (Map<String, PedestrianStreetSection> pedestrianStreetCompoenent : pedestrianStreetSection.values()){
            traverseStreets(pedestrianStreetCompoenent);
        }
    }

    private void traverseStreets(Map<String, PedestrianStreetSection> pedestrianStreetCompoenent){
        for (PedestrianStreetSection pedestrianStreetSection : pedestrianStreetCompoenent.values()){
            pedestrianStreetSection.getPedestrianQueue();

            StreetSectionUI streetSectionUI = null;
            switch (pedestrianStreetSection.getPedestrianConsumerType()){
                case PEDESTRIAN_STREET_SECTION:
                    streetSectionUI = new PedestrianStreetUI(pedestrianStreetSection);
                    break;

                case PEDESTRIAN_CROSSING:
                    streetSectionUI = new PedestrianCrosswalkUI(pedestrianStreetSection, canvas);
                    double width = GetStreetWidth(
                            pedestrianStreetSection.getEnteringVehicleStreetList(),
                            pedestrianStreetSection.getLeavingVehicleStreetList(),
                            pedestrianStreetSection);
                    AddVehicleStreets(canvas, pedestrianStreetSection.getEnteringVehicleStreetList(), pedestrianStreetSection, width);
                    AddVehicleStreets(canvas, pedestrianStreetSection.getLeavingVehicleStreetList(), pedestrianStreetSection, width);
                    break;
            }
            pedestrianMap.put(pedestrianStreetSection.getId(), new HashMap<String, PedestrianUI>());

            canvas.getChildren().add(streetSectionUI);
            streetSectionUI.toBack();
        }
//        addPedestrian();
    }

    @Override
    public void addPedestrian(IPedestrian pedestrian){
        if (pedestrian instanceof Pedestrian) {
            Pedestrian pedestrianInstance = ((Pedestrian)pedestrian);
            PedestrianPoint pedestrianPoint = pedestrianInstance.getCurrentGlobalPosition();
            globalPedestrianMap.put(pedestrianInstance.getName(), new PedestrianUI(canvas, pedestrianPoint.getX(), pedestrianPoint.getY(), pedestrianInstance.getName()));
            PedestrianUI pedestrianUI = globalPedestrianMap.get(pedestrianInstance.getName());

            Platform.setImplicitExit(false);
            Platform.runLater(new Runnable(){
                @Override
                public void run()
                {
                    pedestrianUI.addToContainer();
                }
            });
        }else {
            throw new IllegalArgumentException("Not suitable Pedestrian.");
        }
    }

    @Override
    public void updatePedestrian(IPedestrian pedestrian) {
        if (pedestrian instanceof Pedestrian) {
            Pedestrian pedestrianInstance = ((Pedestrian)pedestrian);
            boolean insection =  ((PedestrianStreetSection)(pedestrianInstance.getCurrentSection().getStreetSection())).checkPedestrianIsWithinSection(pedestrian);
            if (insection){
                PedestrianPoint pedestrianPoint = pedestrianInstance.getCurrentGlobalPosition();
                PedestrianUI pedestrianUI = globalPedestrianMap.get(pedestrianInstance.getName());
                if (pedestrianUI!=null){
                    Platform.setImplicitExit(false);
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            pedestrianUI.updateInContainer(pedestrianPoint.getX(), pedestrianPoint.getY());
                        }
                    });
                }
            }

        }else {
            throw new IllegalArgumentException("Not suitable Pedestrian.");
        }
    }

    @Override
    public void removePedestrian(IPedestrian pedestrian) {
        if (pedestrian instanceof Pedestrian) {
            Pedestrian pedestrianInstance = ((Pedestrian)pedestrian);
            PedestrianUI pedestrianUI = globalPedestrianMap.get(pedestrianInstance.getName());

            Platform.setImplicitExit(false);
            Platform.runLater(new Runnable(){
                @Override
                public void run()
                {
                     pedestrianUI.removeFromContainer();
                }
            });
            globalPedestrianMap.remove(pedestrianInstance.getName());
        }else {
            throw new IllegalArgumentException("Not suitable Pedestrian.");
        }
    }

    @Override
    public void addCar(ICar car, IConsumer consumerStreetSection) {
        if (car instanceof RoundaboutCar) {
            RoundaboutCar roundaboutCar = (RoundaboutCar) car;
            if (consumerStreetSection instanceof StreetSection) {
                StreetSection streetSection = (StreetSection) consumerStreetSection;

                CarStreetUI carStreetUI = carStreetUIMap.get(streetSection);
                Map<String, CarUI> streetCarMap = localCarUIMap.get(streetSection);

                if (streetCarMap.size() < PedestrianUIUtils.maximumNumberCarsPerStreet) {
                    String carId = roundaboutCar.getOldImplementationCar().getName();

                    boolean carDrivesAlongYAxis = streetSection.checkCarDrivesAlongYAxis();

                    if (carDrivesAlongYAxis) {
                        streetCarMap.put(carId, new CarUI(
                                PedestrianUIUtils.GetCarPositionRelativeToWidth(carStreetUI, streetSection),
                                PedestrianUIUtils.GetMaxedUiCarPosition(roundaboutCar, streetSection, carStreetUI),
                                PedestrianUIUtils.VEHICLE_WIDTH,
                                roundaboutCar.getLength(),
                                roundaboutCar,
                                canvas,
                                carId));
                    } else {
                        streetCarMap.put(carId, new CarUI(
                                carStreetUI.getX() + PedestrianUIUtils.GetMaxedUiCarPosition(roundaboutCar, streetSection, carStreetUI),
                                PedestrianUIUtils.GetCarPositionRelativeToWidth(carStreetUI, streetSection),
                                roundaboutCar.getLength(),
                                PedestrianUIUtils.VEHICLE_WIDTH,
                                roundaboutCar,
                                canvas,
                                carId));
                    }

                    CarUI ui = streetCarMap.get(carId);


                    Platform.runLater(new Runnable(){
                        @Override
                        public void run()
                        {
                            ui.addToContainer();
                        }
                    });

                }
            }
        }
    }

    @Override
    public void updateCar(ICar car, IConsumer consumerStreetSection) {
        if (car instanceof RoundaboutCar){

            RoundaboutCar roundaboutCar = (RoundaboutCar)car;
            if (consumerStreetSection instanceof StreetSection ) {
                StreetSection streetSection = (StreetSection) consumerStreetSection;

                CarStreetUI carStreetUI = carStreetUIMap.get(streetSection);
                Map<String, CarUI> streetCarMap = localCarUIMap.get(streetSection);

                String carId = roundaboutCar.getOldImplementationCar().getName();
                CarUI carUi = streetCarMap.get(carId);

                if (carUi!=null){
                    boolean carDrivesAlongYAxis = streetSection.checkCarDrivesAlongYAxis();

                    double x;
                    double y;

                    if (carDrivesAlongYAxis) {
                        x = PedestrianUIUtils.GetCarPositionRelativeToWidth(carStreetUI, streetSection);
                        y = PedestrianUIUtils.GetMaxedUiCarPosition(roundaboutCar, streetSection, carStreetUI);

                    } else {
                        x = PedestrianUIUtils.GetMaxedUiCarPosition(roundaboutCar, streetSection, carStreetUI);
                        y = PedestrianUIUtils.GetCarPositionRelativeToWidth(carStreetUI, streetSection);
                    }

                    Platform.setImplicitExit(false);
                    Platform.runLater(() -> carUi.updateInContainer(x, y));
                }
            }
        }
    }

    @Override
    public void removeCar(ICar car, IConsumer consumerStreetSection) {
        if (car instanceof RoundaboutCar){
            RoundaboutCar roundaboutCar = (RoundaboutCar)car;
            if (consumerStreetSection instanceof StreetSection ) {
                StreetSection streetSection = (StreetSection) consumerStreetSection;

                Map<String, CarUI> streetCarMap = localCarUIMap.get(streetSection);
                if (streetCarMap.size()>0){
                    String carId = roundaboutCar.getOldImplementationCar().getName();
                    CarUI carUi = streetCarMap.get(carId);

                    if (carUi!=null){
                        Platform.setImplicitExit(false);
                        Platform.runLater(() -> carUi.removeFromContainer());
                    }
                }
            }
        }
    }

    private double GetStreetWidth(LinkedList<Street> entryVehicleStreetList, LinkedList<Street> exitVehicleStreetList, PedestrianStreetSection pedestrianStreetSection)
    {
        double streetWidth;
        if (pedestrianStreetSection.isFlexiBorderAlongX()){
              streetWidth = pedestrianStreetSection.getLengthX();
        } else {
            streetWidth = pedestrianStreetSection.getLengthY();
        }

        double calculatedStreetWidth;

        if (entryVehicleStreetList.size() > exitVehicleStreetList.size()){
            calculatedStreetWidth =  streetWidth / entryVehicleStreetList.size();
        } else {
            calculatedStreetWidth =  streetWidth / exitVehicleStreetList.size();
        }
        return calculatedStreetWidth;
    }

    private void AddVehicleStreets(Pane canvas, LinkedList<Street> vehicleStreetList, PedestrianStreetSection pedestrianStreetSection, double streetWidth){
        if (vehicleStreetList!=null){
            for (Street street : vehicleStreetList){
                if (street instanceof StreetSection){
                    StreetSection streetsection = (StreetSection)street;
                    PedestrianPoint vehicleStreetPoint = streetsection.getGlobalCoordinateOfCrossingIntersection();
                    PedestrianPoint crosswalkStreetPoint = pedestrianStreetSection.getGlobalCoordinateOfSectionOrigin();

                    CarStreetUI carStreetUI = null;

                    //from left
                    if (crosswalkStreetPoint.getX() == vehicleStreetPoint.getX()){
                        carStreetUI = new CarStreetUI(
                                vehicleStreetPoint.getX() - PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                vehicleStreetPoint.getY() - (streetWidth/2),
                                PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                streetWidth,
                                streetsection.getId()
                        );
                    }

                    //from right
                    if (crosswalkStreetPoint.getX() + pedestrianStreetSection.getLengthX() == vehicleStreetPoint.getX()){
                        double borderX = crosswalkStreetPoint.getX() + pedestrianStreetSection.getLengthX();
                        carStreetUI = new CarStreetUI(
                                borderX,
                                vehicleStreetPoint.getY() - (streetWidth/2),
                                PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                streetWidth,
                                streetsection.getId()
                        );
                    }

                    //from bottom
                    if (crosswalkStreetPoint.getY() == vehicleStreetPoint.getY()){
                        carStreetUI = new CarStreetUI(
                                vehicleStreetPoint.getX() - (streetWidth/2),
                                vehicleStreetPoint.getY()+ pedestrianStreetSection.getLengthY(),
                                streetWidth,
                                PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                streetsection.getId()
                        );
                    }

                    //from top
                    if (crosswalkStreetPoint.getY() + pedestrianStreetSection.getLengthY() == vehicleStreetPoint.getY()){
                        carStreetUI = new CarStreetUI(
                                vehicleStreetPoint.getX() - (streetWidth/2),
                                crosswalkStreetPoint.getY() - PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                streetWidth,
                                PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                streetsection.getId()
                        );
                    }

                    canvas.getChildren().add(carStreetUI);
                    carStreetUIMap.put(streetsection, carStreetUI);
                    localCarUIMap.put(streetsection, new Hashtable<>());
                }
            }
        }
    }

    private void centerCanvas(Pane nonCenteredCanvas){
        double w2 = nonCenteredCanvas.getBoundsInParent().getMaxX();
        double h2 = nonCenteredCanvas.getBoundsInParent().getMaxY();
        double x2 = ((PedestrianUIUtils.MAIN_WINDOW_WIDTH - w2) / 2);
        double y2 = ((PedestrianUIUtils.MAIN_WINDOW_HEIGHT - h2) / 2);
        nonCenteredCanvas.setTranslateX(x2);
        nonCenteredCanvas.setTranslateY(-y2);
    }
}

