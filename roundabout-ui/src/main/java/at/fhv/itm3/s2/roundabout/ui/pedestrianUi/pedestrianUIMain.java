package at.fhv.itm3.s2.roundabout.ui.pedestrianUi;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class pedestrianUIMain extends ScrollPane {
    private  int width;
    private int height;
    private int positionX;
    private int positionY;
    private double centerX;
    private double centerY;

    Pane canvas = new Pane();

    Rectangle rectangle  = new Rectangle(0, 0, 50, 50);


    public pedestrianUIMain(int posX, int posY, int width, int height){
        super();
        this.width = width;
        this.height = height;
        this.positionX = posX;
        this.positionY = posY;

        setVbarPolicy(ScrollBarPolicy.ALWAYS);
        setHbarPolicy(ScrollBarPolicy.ALWAYS);

        setPrefSize(width, height);
        setLayoutX(posX);
        setLayoutY(posY);
        setStyle("-fx-background-color: red;");
        pannableProperty().set(true);
        setPannable(true);

        rectangle.setStyle("-fx-fill: red; -fx-stroke: black; -fx-stroke-width: 5;");
        canvas.setPrefSize(2000, 500);
        canvas.getChildren().add(rectangle);
        setContent(canvas);
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
