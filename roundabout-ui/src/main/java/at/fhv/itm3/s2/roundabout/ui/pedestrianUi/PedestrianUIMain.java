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

//        canvas.setStyle("-fx-background-color: black");

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
        System.out.println(canvas.getBoundsInParent());

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
        long startTime = System.nanoTime();
        long estimatedTime = System.nanoTime() - startTime;

        //Drawing a Circle
        Circle circle = new Circle();

        //Setting the position of the circle
        circle.setCenterX(300.0f);
        circle.setCenterY(135.0f);

        //Setting the radius of the circle
        circle.setRadius(25.0f);

        //Setting the color of the circle
        circle.setFill(BROWN);

        //Setting the stroke width of the circle
        circle.setStrokeWidth(20);

        //Creating a Path
        Path path = new Path();

        //Moving to the starting point
        MoveTo moveTo = new MoveTo(0, 0);

        //Creating 1st line
        LineTo line1 = new LineTo(321, 161);


        //Adding all the elements to the path
        path.getElements().add(moveTo);
        path.getElements().add(line1);

        //Creating the path transition
        PathTransition pathTransition = new PathTransition();

        //Setting the duration of the transition
        pathTransition.setDuration(Duration.millis(1000));

        //Setting the node for the transition
        pathTransition.setNode(circle);

        //Setting the path for the transition
        pathTransition.setPath(path);

        //Setting the orientation of the path
        pathTransition.setOrientation(OrientationType.ORTHOGONAL_TO_TANGENT);

        //Setting the cycle count for the transition
        pathTransition.setCycleCount(1);

        //Setting auto reverse value to true
        pathTransition.setAutoReverse(false);

        //Playing the animation

        if (pedestrian instanceof Pedestrian) {
            Pedestrian pedestrianInstance = ((Pedestrian)pedestrian);
            SimClock simClock = pedestrianInstance.getRoundaboutModel().getExperiment().getSimClock();
            TimeUnit test = pedestrianInstance.getRoundaboutModel().getModelTimeUnit();


            PedestrianPoint pedestrianPoint = pedestrianInstance.getCurrentGlobalPosition();
            PedestrianUI pedestrianUI = globalPedestrianMap.get(pedestrianInstance.getName());
            if (pedestrianUI!=null){
                Platform.setImplicitExit(false);
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        pathTransition.play();
                        pedestrianUI.updateInContainer(pedestrianPoint.getX(), pedestrianPoint.getY());
                    }
                });
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
        if (car instanceof RoundaboutCar){
            RoundaboutCar roundaboutCar = (RoundaboutCar)car;
            if (consumerStreetSection instanceof StreetSection){
                StreetSection streetSection = (StreetSection) consumerStreetSection;

                CarStreetUI carStreetUI =  carStreetUIMap.get(streetSection);
                Map<String, CarUI> streetCarMap =  localCarUIMap.get(streetSection);

                String carId = roundaboutCar.getOldImplementationCar().getName();

                boolean carDrivesAlongYAxis = streetSection.checkCarDrivesAlongYAxis();

                CarUI carUI = null;
                if (carDrivesAlongYAxis){
                    //street leads from crossing away
                    carUI = new CarUI(
                            PedestrianUIUtils.GetCarPositionRelativeToWidth(carStreetUI, roundaboutCar, streetSection),
                            carStreetUI.getY() + PedestrianUIUtils.GetMaxedUiCarPosition(roundaboutCar, streetSection),
                            PedestrianUIUtils.VEHICLE_WIDTH,
                            roundaboutCar.getLengthInCM(),
                            roundaboutCar,
                            canvas);
                }else {
                    carUI = new CarUI(
                            carStreetUI.getX() + PedestrianUIUtils.GetMaxedUiCarPosition(roundaboutCar, streetSection),
                            PedestrianUIUtils.GetCarPositionRelativeToWidth(carStreetUI, roundaboutCar, streetSection),
                            roundaboutCar.getLengthInCM(),
                            PedestrianUIUtils.VEHICLE_WIDTH,
                            roundaboutCar,
                            canvas);
                }
                Platform.setImplicitExit(false);
                CarUI finalCarUI = carUI;
                Platform.runLater(new Runnable(){
                    @Override
                    public void run()
                    {
                        finalCarUI.addToContainer();
                    }
                });

                streetCarMap.put(carId, carUI);
            }
        }
    }

    @Override
    public void updateCar(ICar car, IConsumer consumerStreetSection) {

    }

    @Override
    public void removeCar(ICar car, IConsumer consumerStreetSection) {

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
                                vehicleStreetPoint.getY() + PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                streetWidth,
                                PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
                                streetsection.getId()
                        );
                    }

                    //from top
                    if (crosswalkStreetPoint.getY() + pedestrianStreetSection.getLengthY() == vehicleStreetPoint.getY()){
                        double startingY = crosswalkStreetPoint.getY() - pedestrianStreetSection.getLengthY();
                        carStreetUI = new CarStreetUI(
                                vehicleStreetPoint.getX() - (streetWidth/2),
                                startingY - PedestrianUIUtils.GetMaxedUiStreetLength(streetsection),
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
        nonCenteredCanvas.setTranslateY(y2);
    }

    public void validate(){
        centerX = canvas.getPrefWidth()/2;
        centerY = canvas.getPrefHeight()/2;
        centerNodeInScrollPane(this, canvas);
    }

    private void centerNodeInScrollPane(ScrollPane scrollPane, Node node) {
        double h = scrollPane.getContent().getBoundsInLocal().getHeight();
        double y = (node.getBoundsInParent().getMaxY() + node.getBoundsInParent().getMinY()) / 2.0;
        double v = scrollPane.getViewportBounds().getHeight();
        centerY = scrollPane.getVmax() * ((y - 0.5 * v) / (h - v));
        scrollPane.setVvalue(centerY);

        double w = scrollPane.getContent().getBoundsInLocal().getWidth();
        double x =  (node.getBoundsInParent().getMaxX() + node.getBoundsInParent().getMinX()) / 2.0;
        double r = scrollPane.getViewportBounds().getWidth();
        centerX = scrollPane.getHmax() * ((y - 0.5 * v) / (h - v));
        scrollPane.setHvalue(centerX);
    }
}
