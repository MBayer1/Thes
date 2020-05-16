package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.api.PedestrianPoint;
import at.fhv.itm3.s2.roundabout.api.entity.*;
import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import at.fhv.itm3.s2.roundabout.entity.PedestrianStreetSection;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.awt.*;
import java.io.Console;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.awt.Color.*;
import static javafx.scene.paint.Color.BROWN;

public class PedestrianUIMain extends ScrollPane implements IPedestrianUIMain {
    private  int width;
    private int height;
    private int positionX;
    private int positionY;
    private double centerX;
    private double centerY;

    private ConfigParser configParser = null;

    private Map<String, StreetSectionUI> streetUIMap = new HashMap<String, StreetSectionUI>();
    private Map<String, Map<String, PedestrianUI>> pedestrianMap = new HashMap<String, Map<String, PedestrianUI>>();
    private Map<String, PedestrianUI> globalPedestrianMap = new HashMap<String, PedestrianUI>();
    private Map<String, CarStreetUI> carStreetUIMap = new HashMap<>();


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

        canvas.setStyle("-fx-background-color: black");

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

            streetUIMap.put(pedestrianStreetSection.getId(), streetSectionUI);
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
                    //from upside
                    if (crosswalkStreetPoint.getX() == vehicleStreetPoint.getX()){
                        carStreetUI = new CarStreetUI(
                                vehicleStreetPoint.getX() + (streetWidth/2),
                                vehicleStreetPoint.getY() + street.getLength(),
                                width,
                                street.getLength(),
                                streetsection.getId()
                        );
                        canvas.getChildren().add(carStreetUI);
                        carStreetUIMap.put(streetsection.getId(), carStreetUI);
                    }
                    //from bottom
                    if (crosswalkStreetPoint.getX() + pedestrianStreetSection.getLengthY() == vehicleStreetPoint.getX())

                    //from left
                    if (crosswalkStreetPoint.getY() == vehicleStreetPoint.getY()){

                    }

                    //from left
                    if (crosswalkStreetPoint.getY() + pedestrianStreetSection.getLengthX() == vehicleStreetPoint.getY()){

                    }

                    int test  = 5;
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
