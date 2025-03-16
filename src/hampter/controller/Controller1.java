package hampter.controller;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import hampter.logic.Logic;
import hampter.logic.ScreenShot;
import hampter.util.Swap;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller1 {

    private static final int SQUARE_SIZE = 50;
    private static final int BACKGROUND = 0x555555;
    private static final Color BACKGROUND_COLOR = Color.web(String.format("0x%06X", BACKGROUND));
    private static final int IMMOVABLE = 0x000000;
    private static final Color IMMOVABLE_COLOR = Color.web(String.format("0x%06X", IMMOVABLE));

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
    private TilePane boardTilePane;
    @FXML
    private TilePane verticalLinesTilePane;
    @FXML
    private TilePane horizontalLinesTilePane;

    private int width = 0;
    private int height = 0;
    private int sliderMaxSize = 25;
    private Paint selectedColor = null;
    private int[] squareSize = { -1 };
    private int[] leftBottomCornerCords = new int[2];

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
        sliderH.setMax(sliderMaxSize - 1);
        sliderH.setValue(width - 1);
        sliderH.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sliderH.setValue(Math.round(new_val.doubleValue()));
                int oldWidth = width;
                width = 1 + (int) Math.round(new_val.doubleValue());
                extendOrShrinkBoard(true, width - oldWidth);
            }
        });
        sliderH.setOnScroll(
                event -> sliderH.setValue(sliderH.getValue() + (event.getDeltaY() < 0 ? 1 : -1)));
        sliderV.setMax(sliderMaxSize - 1);
        sliderV.setValue(sliderMaxSize - height);
        sliderV.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sliderV.setValue(Math.round(new_val.doubleValue()));
                int oldHeight = height;
                height = sliderMaxSize - (int) Math.round(new_val.doubleValue());
                extendOrShrinkBoard(false, height - oldHeight);
            }
        });
        sliderV.setOnScroll(event -> sliderV.setValue(sliderV.getValue() + (event.getDeltaY() > 0 ? 1 : -1)));
    }

    private void extendOrShrinkBoard(boolean isHorizontal, int count) {
        if (isHorizontal) {
            boardTilePane.setPrefColumns(width);
            boardTilePane.setMaxWidth(1 + (SQUARE_SIZE + 10) * width);
            boardTilePane.setMinWidth(1 + (SQUARE_SIZE + 10) * width);
            for (int i = height; i > 0; i--) {
                for (int j = 0; j < Math.abs(count); j++) {
                    if (count > 0) {
                        boardTilePane.getChildren().add(i * (width - count), makeNewBlankRectangle());
                    } else {
                        boardTilePane.getChildren().remove((height - i + 1) * width);
                    }
                }
            }
        } else {
            for (int i = 0; i < Math.abs(count) * width; i++) {
                if (count > 0) {
                    boardTilePane.getChildren().add(0, makeNewBlankRectangle());
                } else {
                    boardTilePane.getChildren().removeFirst();
                }
            }
        }
        borderPane.getScene().getWindow().sizeToScene();
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
        startImage.setOnMouseClicked((event) -> colorInSolutionLines());

        ImageView restartImage = setupImage("restart");
        restartImage.setOnMouseClicked((event) -> resetBoard());

        ImageView screenshotImage = setupImage("screenshot");
        screenshotImage.setOnMouseClicked((event) -> setBoardFromScreenShot());

        buttonsContainer.getChildren().addAll(startImage, restartImage, screenshotImage);
    }

    private void colorInSolutionLines() {
        ArrayList<Swap> swaps = Logic.solveBoard(getBoard());
        if (swaps.size() == 0) {
            return;
        }
        verticalLinesTilePane.setDisable(false);
        horizontalLinesTilePane.setDisable(false);
        for (Swap swap : swaps) {
            if (swap.isDown()) {
                StackPane stackPane = (StackPane) verticalLinesTilePane.getChildren()
                        .get(swap.getX() * width + swap.getY());
                ((Rectangle) stackPane.getChildren().get(1)).setFill(Color.RED);
                Label label = new Label(Integer.toString(swaps.indexOf(swap) + 1));
                label.setFont(Font.font("System", FontWeight.BOLD, 18));
                stackPane.getChildren().add(label);
            } else {
                StackPane stackPane = (StackPane) horizontalLinesTilePane.getChildren()
                        .get(swap.getX() * (width - 1) + swap.getY());
                ((Rectangle) stackPane.getChildren().get(1)).setFill(Color.RED);
                Label label = new Label(Integer.toString(swaps.indexOf(swap) + 1));
                label.setFont(Font.font("System", FontWeight.BOLD, 18));
                stackPane.getChildren().add(label);
            }
        }
        performAllMouseClicks(swaps);
    }

    private void performAllMouseClicks(ArrayList<Swap> swaps) {
        Point originalMousePos = MouseInfo.getPointerInfo().getLocation();
        Timeline timeline = new Timeline();
        int delay = 100;
        for (Swap swap : swaps) {
            int x1 = leftBottomCornerCords[0] + squareSize[0] / 2 + swap.getY() * squareSize[0];
            int y1 = leftBottomCornerCords[1] - squareSize[0] / 2 - (height - 1 - swap.getX()) * squareSize[0];

            int x2 = x1 + (swap.isDown() ? 0 : squareSize[0]);
            int y2 = y1 + (swap.isDown() ? squareSize[0] : 0);

            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> clickMouse(x1, y1)));
            delay += 100;

            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> clickMouse(x2, y2)));
            delay += 1200;
        }

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> {
            Robot robot = new Robot();
            robot.mouseMove(originalMousePos.x, originalMousePos.y);
        }));

        timeline.play();
    }

    private void clickMouse(int x, int y) {
        Robot robot = new Robot();
        robot.mouseMove(x, y);
        robot.mouseClick(MouseButton.SECONDARY);
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
        for (Node rectangle : boardTilePane.getChildren()) {
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
        resetColorContainer();
        setHeight(3);
        setWidth(3);
        verticalLinesTilePane.setDisable(true);
        horizontalLinesTilePane.setDisable(true);
        for (Node rectangle : boardTilePane.getChildren()) {
            ((Rectangle) rectangle).setFill(BACKGROUND_COLOR);
        }
    }

    private void resetColorContainer() {
        while (colorContainer.getChildren().size() != 3) {
            colorContainer.getChildren().remove(2);
        }
    }

    private void setBoardFromScreenShot() {
        int[][] grid = ScreenShot.takeScreenShot(false, BACKGROUND, IMMOVABLE, leftBottomCornerCords, squareSize);
        clearBoard();
        setHeight(grid.length);
        setWidth(grid[0].length);
        setupLines();
        ArrayList<Color> colors = new ArrayList<>();
        ObservableList<Node> list = boardTilePane.getChildren();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = grid[i][j];
                Color color = Color.web(String.format("0x%06X", rgb));
                if (!colors.contains(color)) {
                    colors.add(color);
                }
                ((Rectangle) (list.get(i * width + j))).setFill(color);
            }
        }
        resetColorContainer();
        for (Color color : colors) {
            if (color.equals(BACKGROUND_COLOR) || color.equals(IMMOVABLE_COLOR)) {
                continue;
            }
            addColorToColorContainer(color);
        }
        colorInSolutionLines();
        borderPane.getScene().getWindow().sizeToScene();
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
        colorContainer.getChildren().removeLast();
        colorContainer.getChildren().addAll(rectangle, addColor);
    }

    private void clearBoard() {
        for (Node rectangle : boardTilePane.getChildren()) {
            ((Rectangle) rectangle).setFill(BACKGROUND_COLOR);
        }
    }

    private void setWidth(int width) {
        sliderH.setValue(width - 1);
    }

    private void setHeight(int height) {
        sliderV.setValue(sliderMaxSize - height);
    }

    private void setupLines() {
        verticalLinesTilePane.getChildren().clear();
        verticalLinesTilePane.setPrefColumns(width);
        verticalLinesTilePane.setMaxWidth(1 + (SQUARE_SIZE + 10) * width);
        verticalLinesTilePane.setMinWidth(1 + (SQUARE_SIZE + 10) * width);
        verticalLinesTilePane.setDisable(true);
        for (int i = 0; i < (height - 1) * width; i++) {
            verticalLinesTilePane.getChildren().add(makeNewBlankLine(false));
        }
        horizontalLinesTilePane.getChildren().clear();
        horizontalLinesTilePane.setPrefColumns(width - 1);
        horizontalLinesTilePane.setMaxWidth(1 + (SQUARE_SIZE + 10) * (width - 1));
        horizontalLinesTilePane.setMinWidth(1 + (SQUARE_SIZE + 10) * (width - 1));
        horizontalLinesTilePane.setDisable(true);
        for (int i = 0; i < (width - 1) * height; i++) {
            horizontalLinesTilePane.getChildren().add(makeNewBlankLine(true));
        }
    }

    private StackPane makeNewBlankLine(boolean isHorizontal) {
        StackPane stackPane = new StackPane();
        Rectangle rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE, Color.TRANSPARENT);
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setStrokeWidth(3);
        Rectangle line = new Rectangle(SQUARE_SIZE / (isHorizontal ? 1 : 5), SQUARE_SIZE / (isHorizontal ? 5 : 1),
                Color.TRANSPARENT);
        line.setArcHeight(10);
        line.setArcWidth(10);
        stackPane.getChildren().addAll(rectangle, line);
        return stackPane;
    }
}
