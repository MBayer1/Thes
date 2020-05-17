package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import at.fhv.itm3.s2.roundabout.entity.RoundaboutCar;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class CarUI extends Rectangle {

    private Label carNameLabel = null;
    private Pane canvas = null;

    public CarUI(double x, double y, double width, double height, RoundaboutCar roundaboutCar, Pane canvas){
        super (x, y, width, height);
        setFill(Color.color(Math.random(), Math.random(), Math.random()));
        this.canvas = canvas;
        carNameLabel = new Label(roundaboutCar.getOldImplementationCar().getName());
        carNameLabel.setLayoutX(x +  PedestrianUIUtils.relativeObjectLabelPositionX);
        carNameLabel.setLayoutX(y + PedestrianUIUtils.relativeObjectLabelPositionY);
        carNameLabel.setFont(new Font("Arial", 30));
        carNameLabel.setScaleY(-1);
    }

    public void addToContainer(){
        canvas.getChildren().add(this);
        canvas.getChildren().add(carNameLabel);
    }

    public void updateInContainer(double x, double y){
        this.relocate(x, y);
        carNameLabel.relocate(x + PedestrianUIUtils.relativeObjectLabelPositionX, y + PedestrianUIUtils.relativeObjectLabelPositionY);
    }

    public void removeFromContainer(){
        canvas.getChildren().remove(this);
        canvas.getChildren().remove(carNameLabel);
    }

}
