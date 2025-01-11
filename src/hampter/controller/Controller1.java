package hampter.controller;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import hampter.logic.Logic;
import hampter.logic.ScreenShot;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

public class Controller1 {

    @FXML
    private Slider sliderH;
    @FXML
    private Slider sliderV;
    @FXML
    private BorderPane borderPane;
    @FXML
    private HBox colorContainer;
    @FXML
    private VBox buttonsContainer;
    @FXML
    private StackPane addColor;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TilePane tilePane;
    private int width = 3;
    private int height = 3;
    private int sliderMaxSize = 25;
    private Paint selectedColor = null;
    private int counter = -1;
    private static final int SQUARE_SIZE = 50;
    private static final Color BACKGROUND_COLOR = Color.rgb(85, 85, 85);
    private static final Color IMMOVABLE_COLOR = Color.BLACK;

    public void initialize() throws FileNotFoundException {
        setupSliders();
        setupBoard();
        setupButtons();
        setupColors();
    }

    public void populateBoard() {
        tilePane.getChildren().clear();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Rectangle rectangle = makeNewBlankRectangle();
                tilePane.getChildren().add(rectangle);
            }
        }
        Stage stage = (Stage) borderPane.getScene().getWindow();
        stage.sizeToScene();
    }

    private void extendOrShrinkBoard(boolean isHorizontal, int count) {
        if (isHorizontal) {
            this.tilePane.setPrefColumns(width);
            this.tilePane.setMaxWidth(1 + (SQUARE_SIZE + 10) * width);
            this.tilePane.setMinWidth(1 + (SQUARE_SIZE + 10) * width);
            for (int i = height; i > 0; i--) {
                for (int j = 0; j < Math.abs(count); j++) {
                    if (count > 0) {
                        tilePane.getChildren().add(i * (width - count), makeNewBlankRectangle());
                    } else {
                        tilePane.getChildren().remove(i * (width - count) - 1);
                    }
                }
            }
        } else {
            for (int i = 0; i < Math.abs(count) * width; i++) {
                if (count > 0) {
                    tilePane.getChildren().add(0, makeNewBlankRectangle());
                } else {
                    tilePane.getChildren().removeFirst();
                }
            }
        }
        Stage stage = (Stage) borderPane.getScene().getWindow();
        stage.sizeToScene();
    }

    private Rectangle makeNewBlankRectangle() {
        Rectangle rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, BACKGROUND_COLOR);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(3);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        return rectangle;
    }

    private void addColorToColorContainer(Color color) {
        Rectangle rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, color);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(3);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        rectangle.setId(counter + "");
        StackPane colorPicker = addColor;
        colorContainer.getChildren().removeLast();
        colorContainer.getChildren().addAll(rectangle, colorPicker);
        counter++;
    }

    public void handleMouseClick(Event event) {
        if (event.getTarget() instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) event.getTarget();
            if (rectangle.getParent() instanceof StackPane) {
                colorPicker.show();
                return;
            }
            if (rectangle.getParent() instanceof HBox) {
                selectedColor = rectangle.getFill();
                return;
            }
            if (selectedColor != null) {
                rectangle.setFill(selectedColor);
            }
        } else if (event.getTarget() instanceof Line) {
            colorPicker.show();
        }
    }

    private int[][] getBoard() {
        int[][] board = new int[height][width];
        ArrayList<Paint> colors = new ArrayList<>();
        colors.add(IMMOVABLE_COLOR);
        colors.add(BACKGROUND_COLOR);
        int i = 0;
        for (Node rectangle : this.tilePane.getChildren()) {
            Paint color = ((Rectangle) rectangle).getFill();
            int number = colors.indexOf(color);
            if (number == -1) {
                colors.add(color);
                number = colors.size() - 1;
            }
            board[i / width][i % width] = number - 1;
            i++;
        }
        return board;
    }

    private void setupSliders() {
        sliderH.setMax(sliderMaxSize);
        sliderH.setValue(width - 1);
        sliderH.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sliderH.setValue(Math.round(new_val.doubleValue()));
                int oldWidth = width;
                width = 1 + (int) Math.round(new_val.doubleValue());
                extendOrShrinkBoard(true, width - oldWidth);
            }
        });
        sliderH.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double currentValue = sliderH.getValue();
            if (deltaY < 0) {
                sliderH.setValue(currentValue + 1);
            } else {
                sliderH.setValue(currentValue - 1);
            }
            width = 1 + (int) sliderH.getValue();
        });
        sliderV.setMax(sliderMaxSize);
        sliderV.setValue(sliderMaxSize - height + 1);
        sliderV.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sliderV.setValue(Math.round(new_val.doubleValue()));
                int oldHeight = height;
                height = 1 + sliderMaxSize - (int) Math.round(new_val.doubleValue());
                extendOrShrinkBoard(false, height - oldHeight);
            }
        });
        sliderV.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double currentValue = sliderV.getValue();
            if (deltaY > 0) {
                sliderV.setValue(currentValue + 1);
            } else {
                sliderV.setValue(currentValue - 1);
            }
            height = 1 + sliderMaxSize - (int) sliderV.getValue();
        });
    }

    private void setupBoard() {
        this.tilePane.setPrefColumns(width);
        this.tilePane.setMaxWidth(1 + (SQUARE_SIZE + 10) * width);
        this.tilePane.setMinWidth(1 + (SQUARE_SIZE + 10) * width);
    }

    private void setupButtons() {
        ImageView startImage = new ImageView(
                new Image(getClass().getResource("../images/start.png").toExternalForm(), false));
        startImage.setPreserveRatio(true);
        startImage.setFitHeight(SQUARE_SIZE);
        startImage.setFitWidth(SQUARE_SIZE);
        startImage.setPickOnBounds(true);
        startImage.setOnMouseClicked((event) -> {
            Logic.solveBoard(getBoard());
        });

        ImageView restartImage = new ImageView(
                new Image(getClass().getResource("../images/restart.png").toExternalForm(), false));
        restartImage.setPreserveRatio(true);
        restartImage.setFitHeight(SQUARE_SIZE);
        restartImage.setFitWidth(SQUARE_SIZE);
        restartImage.setPickOnBounds(true);
        restartImage.setOnMouseClicked((event) -> {
            int[][] grid = ScreenShot.takeScreenShot();
            clearBoard();
            setHeight(grid.length);
            setWidth(grid[0].length);
            ObservableList<Node> list = this.tilePane.getChildren();
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int rgb = grid[i][j];
                    double red = ((rgb >> 16) & 0xFF) / 255.0;
                    double green = ((rgb >> 8) & 0xFF) / 255.0;
                    double blue = (rgb & 0xFF) / 255.0;
                    Color color = rgb == -2 ? BACKGROUND_COLOR
                            : rgb == -1 ? IMMOVABLE_COLOR : new Color(red, green, blue, 1.0);
                    ((Rectangle) (list.get(i * width + j))).setFill(color);
                }
            }
            // TODO
        });
        buttonsContainer.getChildren().addAll(startImage, restartImage);
    }

    private void setupColors() {
        Rectangle rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, BACKGROUND_COLOR);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(3);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        Line plusPart1 = new Line(0, 0, 0, 30);
        plusPart1.setStrokeWidth(5);
        plusPart1.setStrokeLineCap(StrokeLineCap.ROUND);
        Line plusPart2 = new Line(0, 0, 30, 0);
        plusPart2.setStrokeWidth(5);
        plusPart2.setStrokeLineCap(StrokeLineCap.ROUND);
        addColor.getChildren().addAll(rectangle, plusPart1, plusPart2);
        colorPicker.setOnAction(event -> {
            addColorToColorContainer(colorPicker.getValue());
            selectedColor = colorPicker.getValue();
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.sizeToScene();
        });

        addColorToColorContainer(IMMOVABLE_COLOR);
        addColorToColorContainer(BACKGROUND_COLOR);
    }

    private void clearBoard() {
        for (Node rectangle : this.tilePane.getChildren()) {
            ((Rectangle) rectangle).setFill(BACKGROUND_COLOR);
        }
    }

    private void setWidth(int width) {
        sliderH.setValue(width - 1);
    }

    private void setHeight(int height) {
        sliderV.setValue(sliderMaxSize - height + 1);
    }
}
