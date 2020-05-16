package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.Pedestrian;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;

public class PedestrianUI extends Circle {

    private Label pedestrianNameLabel = null;
    private Pane canvas = null;
    private double relativeLabelPositionX = 10;
    private double relativeLabelPositionY = 10;

    public PedestrianUI(Pane canvas, double x, double y, String pedestrianName){
        super(x - (PedestrianUIUtils.PEDESTRIAN_WIDTH/2), x - (PedestrianUIUtils.PEDESTRIAN_WIDTH/2), PedestrianUIUtils.PEDESTRIAN_WIDTH);
        setFill(Color.color(Math.random(), Math.random(), Math.random()));
        this.canvas = canvas;
        pedestrianNameLabel = new Label(pedestrianName);
        pedestrianNameLabel.setLayoutX(x + relativeLabelPositionX);
        pedestrianNameLabel.setLayoutX(y + relativeLabelPositionY);
        pedestrianNameLabel.setFont(new Font("Arial", 30));
        pedestrianNameLabel.setScaleY(-1);
    }

    public void addToContainer(){
        canvas.getChildren().add(this);
        canvas.getChildren().add(pedestrianNameLabel);
    }

    public void updateInContainer(double x, double y){
        this.relocate(x, y);
        pedestrianNameLabel.relocate(x + relativeLabelPositionX, y + relativeLabelPositionY);
    }

    public void removeFromContainer(){
        canvas.getChildren().remove(this);
        canvas.getChildren().remove(pedestrianNameLabel);
    }
}
