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

    private static final int SQUARE_SIZE = 50;
    private static final Color BACKGROUND_COLOR = Color.rgb(85, 85, 85);
    private static final Color IMMOVABLE_COLOR = Color.BLACK;

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

    private int width = 0;
    private int height = 0;
    private int sliderMaxSize = 25;
    private Paint selectedColor = null;
    private int counter = -1;

    public void initialize() throws FileNotFoundException {
        setupSliders();
        setupButtons();
        setupColors();
    }

    public void setupBoard() {
        resetBoard();
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

    private void setupSliders() {
        this.sliderH.setMax(sliderMaxSize - 1);
        this.sliderH.setValue(width - 1);
        this.sliderH.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sliderH.setValue(Math.round(new_val.doubleValue()));
                int oldWidth = width;
                width = 1 + (int) Math.round(new_val.doubleValue());
                extendOrShrinkBoard(true, width - oldWidth);
            }
        });
        this.sliderH.setOnScroll(event -> this.sliderH.setValue(sliderH.getValue() + (event.getDeltaY() < 0 ? 1 : -1)));
        sliderV.setMax(sliderMaxSize - 1);
        this.sliderV.setValue(sliderMaxSize - height);
        this.sliderV.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sliderV.setValue(Math.round(new_val.doubleValue()));
                int oldHeight = height;
                height = sliderMaxSize - (int) Math.round(new_val.doubleValue());
                extendOrShrinkBoard(false, height - oldHeight);
            }
        });
        this.sliderV.setOnScroll(event -> this.sliderV.setValue(sliderV.getValue() + (event.getDeltaY() > 0 ? 1 : -1)));
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
        this.borderPane.getScene().getWindow().sizeToScene();
    }

    private Rectangle makeNewBlankRectangle() {
        Rectangle rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, BACKGROUND_COLOR);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(3);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        return rectangle;
    }

    private void setupButtons() {
        ImageView startImage = setupImage("start");
        startImage.setOnMouseClicked((event) -> Logic.solveBoard(getBoard()));

        ImageView restartImage = setupImage("restart");
        restartImage.setOnMouseClicked((event) -> resetBoard());

        ImageView screenshotImage = setupImage("screenshot");
        screenshotImage.setOnMouseClicked((event) -> setBoardFromScreenShot());

        buttonsContainer.getChildren().addAll(startImage, restartImage, screenshotImage);
    }

    private ImageView setupImage(String fileName) {
        ImageView image = new ImageView(
                new Image(getClass().getResource("../images/" + fileName + ".png").toExternalForm(), false));
        image.setPreserveRatio(true);
        image.setFitHeight(SQUARE_SIZE);
        image.setFitWidth(SQUARE_SIZE);
        image.setPickOnBounds(true);
        return image;
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

    private void resetBoard() {
        setHeight(3);
        setWidth(3);
        for (Node rectangle : this.tilePane.getChildren()) {
            ((Rectangle) rectangle).setFill(BACKGROUND_COLOR);
        }
    }

    private void setBoardFromScreenShot() {
        int[][] grid = ScreenShot.takeScreenShot();
        clearBoard();
        setHeight(grid.length);
        setWidth(grid[0].length);
        ArrayList<Color> colors = new ArrayList<>();
        ObservableList<Node> list = this.tilePane.getChildren();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = grid[i][j];
                double red = ((rgb >> 16) & 0xFF) / 255.0;
                double green = ((rgb >> 8) & 0xFF) / 255.0;
                double blue = (rgb & 0xFF) / 255.0;
                Color color = rgb == -2 ? BACKGROUND_COLOR
                        : rgb == -1 ? IMMOVABLE_COLOR : new Color(red, green, blue, 1.0);
                if (rgb != -1 && rgb != -2 && !colors.contains(color)) {
                    colors.add(color);
                }
                ((Rectangle) (list.get(i * width + j))).setFill(color);
            }
        }
        for (Color color : colors) {
            addColorToColorContainer(color);
        }
        this.borderPane.getScene().getWindow().sizeToScene();
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

    private void clearBoard() {
        for (Node rectangle : this.tilePane.getChildren()) {
            ((Rectangle) rectangle).setFill(BACKGROUND_COLOR);
        }
    }

    private void setWidth(int width) {
        this.sliderH.setValue(width - 1);
    }

    private void setHeight(int height) {
        this.sliderV.setValue(sliderMaxSize - height);
    }
}
