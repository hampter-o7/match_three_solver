package hampter.java.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import hampter.java.logic.Logic;
import hampter.java.logic.ProcessScreenshot;
import hampter.java.util.Swap;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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

    private static final int LEFT_UPPER_SCREENSHOT_X = 538;
    private static final int LEFT_UPPER_SCREENSHOT_Y = 119;
    private static final int SCREENSHOT_WIDTH = 1381 - LEFT_UPPER_SCREENSHOT_X;
    private static final int SCREENSHOT_HEIGHT = 960 - LEFT_UPPER_SCREENSHOT_Y;
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
    @FXML
    private RadioMenuItem leftMenuItem;
    @FXML
    private RadioMenuItem rightMenuItem;

    private int width = 0;
    private int height = 0;
    private int sliderMaxSize = 25;
    private Paint selectedColor = null;
    private AtomicInteger squareSize = new AtomicInteger(-1);
    private int[] leftBottomCornerCords = new int[2];
    private boolean isTest = false;
    private MouseButton primaryButton = null;
    private String fileName = "config.properties";

    public void initialize() throws IOException {
        setupSliders();
        setupButtons();
        setupColors();
        setupSettings();
        resetBoard();
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
        sliderH.setOnScroll(event -> sliderH.setValue(sliderH.getValue() + (event.getDeltaY() < 0 ? 1 : -1)));

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
                    } else
                        boardTilePane.getChildren().remove((height - i + 1) * width);
                }
            }
        } else {
            for (int i = 0; i < Math.abs(count) * width; i++) {
                if (count > 0) {
                    boardTilePane.getChildren().add(0, makeNewBlankRectangle());
                } else
                    boardTilePane.getChildren().removeFirst();
            }
        }
        Platform.runLater(() -> borderPane.getScene().getWindow().sizeToScene());
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
        CheckBox autoSolve = new CheckBox("Auto solve");
        autoSolve.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
        autoSolve.setOnAction(e -> writeConfigFile("autosolve", autoSolve.isSelected() ? "true" : "false"));

        ImageView startImage = setupImage("start");
        startImage.setOnMouseClicked((event) -> colorInSolutionLines());

        ImageView restartImage = setupImage("restart");
        restartImage.setOnMouseClicked((event) -> resetBoard());

        ImageView screenshotImage = setupImage("screenshot");
        screenshotImage.setOnMouseClicked((event) -> setBoardFromScreenShot());

        buttonsContainer.getChildren().addAll(autoSolve, startImage, restartImage, screenshotImage);

        Platform.runLater(
                () -> buttonsContainer.getScene().setOnMouseClicked(mouseEvent -> handleMouseClick(mouseEvent)));
    }

    private ImageView setupImage(String fileName) {
        ImageView image = new ImageView(
                new Image(getClass().getResource("/hampter/resources/images/" + fileName + ".png").toExternalForm()));
        image.setPreserveRatio(true);
        image.setFitHeight(SQUARE_SIZE);
        image.setFitWidth(SQUARE_SIZE);
        image.setPickOnBounds(true);
        image.disabledProperty()
                .addListener((obs, oldVal, newVal) -> image.setEffect(newVal ? new ColorAdjust(-1, 0, 0.3, 0) : null));
        return image;
    }

    private void colorInSolutionLines() {
        buttonsContainer.setDisable(true);
        verticalLinesTilePane.setVisible(false);
        horizontalLinesTilePane.setVisible(false);
        new Thread(() -> {
            ArrayList<Swap> swaps = Logic.solveBoard(getBoard());
            buttonsContainer.setDisable(false);
            if (swaps.isEmpty())
                return;
            Platform.runLater(() -> {
                setupLines();
                verticalLinesTilePane.setVisible(true);
                horizontalLinesTilePane.setVisible(true);
                for (Swap swap : swaps) {
                    if (swap.isDown()) {
                        StackPane stackPane = (StackPane) verticalLinesTilePane.getChildren()
                                .get(swap.getX() * width + swap.getY());
                        ((Rectangle) stackPane.getChildren().get(1)).setFill(Color.RED);
                        ((Label) stackPane.getChildren().get(2)).setText(Integer.toString(swaps.indexOf(swap) + 1));
                    } else {
                        StackPane stackPane = (StackPane) horizontalLinesTilePane.getChildren()
                                .get(swap.getX() * (width - 1) + swap.getY());
                        ((Rectangle) stackPane.getChildren().get(1)).setFill(Color.RED);
                        ((Label) stackPane.getChildren().get(2)).setText(Integer.toString(swaps.indexOf(swap) + 1));
                    }
                }
                if (!isTest && leftBottomCornerCords[0] != 0 &&
                        ((CheckBox) buttonsContainer.getChildren().get(0)).isSelected()) {
                    performAllMouseClicks(swaps);
                }
            });
        }).start();
    }

    private void setupLines() {
        verticalLinesTilePane.getChildren().clear();
        verticalLinesTilePane.setPrefColumns(width);
        verticalLinesTilePane.setMaxWidth(1 + (SQUARE_SIZE + 10) * width);
        verticalLinesTilePane.setMinWidth(1 + (SQUARE_SIZE + 10) * width);
        for (int i = 0; i < (height - 1) * width; i++) {
            verticalLinesTilePane.getChildren().add(makeNewBlankLine(false));
        }
        horizontalLinesTilePane.getChildren().clear();
        horizontalLinesTilePane.setPrefColumns(width - 1);
        horizontalLinesTilePane.setMaxWidth(1 + (SQUARE_SIZE + 10) * (width - 1));
        horizontalLinesTilePane.setMinWidth(1 + (SQUARE_SIZE + 10) * (width - 1));
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
        Label label = new Label();
        label.disableProperty().bind(stackPane.disableProperty().not());
        label.setOpacity(1);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        stackPane.getChildren().addAll(rectangle, line, label);
        return stackPane;
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

    private void performAllMouseClicks(ArrayList<Swap> swaps) {
        ((Stage) borderPane.getScene().getWindow()).setIconified(true);
        Timeline timeline = new Timeline();
        int delay = 100;

        for (Swap swap : swaps) {
            int x1 = leftBottomCornerCords[0] + squareSize.get() / 2 + swap.getY() * squareSize.get();
            int y1 = leftBottomCornerCords[1] - squareSize.get() / 2 - (height - 1 - swap.getX()) * squareSize.get();

            int x2 = x1 + (swap.isDown() ? 0 : squareSize.get());
            int y2 = y1 + (swap.isDown() ? squareSize.get() : 0);

            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> clickMouse(x1, y1)));
            delay += 100;

            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> clickMouse(x2, y2)));
            delay += 1200;
        }

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> {
            ((Stage) borderPane.getScene().getWindow()).setIconified(false);
        }));

        timeline.play();
    }

    private void clickMouse(int x, int y) {
        Robot robot = new Robot();
        robot.mouseMove(x, y);
        robot.mouseClick(primaryButton);
    }

    private void setBoardFromScreenShot() {
        buttonsContainer.setDisable(true);
        BufferedImage screenShot = takeScreenShot();
        Task<int[][]> task = new Task<>() {
            @Override
            protected int[][] call() {
                return ProcessScreenshot.processScreenshot(isTest, screenShot, BACKGROUND, IMMOVABLE,
                        leftBottomCornerCords, squareSize, LEFT_UPPER_SCREENSHOT_X, LEFT_UPPER_SCREENSHOT_Y);
            }
        };

        task.setOnSucceeded(e -> {
            int[][] grid = task.getValue();
            clearBoard();
            setHeight(grid.length);
            setWidth(grid[0].length);

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
                if (!color.equals(BACKGROUND_COLOR) && !color.equals(IMMOVABLE_COLOR)) {
                    addColorToColorContainer(color);
                }
            }

            colorInSolutionLines();
            borderPane.getScene().getWindow().sizeToScene();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private BufferedImage takeScreenShot() {
        // WritableImage screenCapture = robot.getScreenCapture(null,
        // Screen.getPrimary().getBounds());
        // BufferedImage image = null;
        // try {
        // image = ImageIO.read(new File("inputs/1600x900res.png"));
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // BufferedImage grayscale = GrayScale.applyGrayScale(image, GrayScale.AVERAGE,
        // true);
        // BufferedImage thresholdGradientImage =
        // EdgeDetection.getThresholdGradient(grayscale, EdgeDetection.SOBEL,
        // EdgeDetection.PRESET_ALPHA, EdgeDetection.PRESET_BETA, true);
        // contour(dilate(thresholdGradientImage));

        if (isTest) {
            try {
                return ImageIO.read(new File("inputs/input2.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            buttonsContainer.getScene().getWindow().setOpacity(0);
            Robot robot = new Robot();
            WritableImage screenCapture = robot.getScreenCapture(null,
                    new Rectangle2D(LEFT_UPPER_SCREENSHOT_X, LEFT_UPPER_SCREENSHOT_Y, SCREENSHOT_WIDTH,
                            SCREENSHOT_HEIGHT));
            buttonsContainer.getScene().getWindow().setOpacity(1);
            return SwingFXUtils.fromFXImage(screenCapture, null);
        }
        return null;
    }

    private void clearBoard() {
        for (Node rectangle : boardTilePane.getChildren())
            ((Rectangle) rectangle).setFill(BACKGROUND_COLOR);
    }

    private void setWidth(int width) {
        sliderH.setValue(width - 1);
    }

    private void setHeight(int height) {
        sliderV.setValue(sliderMaxSize - height);
    }

    private void handleMouseClick(Event event) {
        if (event.getTarget() instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) event.getTarget();
            if (rectangle.getParent() instanceof StackPane) {
                colorPicker.show();
            } else if (rectangle.getParent() instanceof HBox) {
                selectedColor = rectangle.getFill();
            } else if (selectedColor != null) {
                verticalLinesTilePane.setVisible(false);
                horizontalLinesTilePane.setVisible(false);
                rectangle.setFill(selectedColor);
            }
        } else if (event.getTarget() instanceof Line)
            colorPicker.show();
    }

    private void resetBoard() {
        resetColorContainer();
        setHeight(3);
        setWidth(3);
        verticalLinesTilePane.setVisible(false);
        horizontalLinesTilePane.setVisible(false);
        for (Node rectangle : boardTilePane.getChildren()) {
            ((Rectangle) rectangle).setFill(BACKGROUND_COLOR);
        }
    }

    private void resetColorContainer() {
        while (colorContainer.getChildren().size() != 3) {
            colorContainer.getChildren().remove(2);
        }
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

    private void setupSettings() throws IOException {
        ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            primaryButton = ((RadioMenuItem) newValue).getText().toLowerCase().equals("left") ? MouseButton.PRIMARY
                    : MouseButton.SECONDARY;
            writeConfigFile("primary", ((RadioMenuItem) newValue).getText().toLowerCase());
        });
        leftMenuItem.setToggleGroup(group);
        rightMenuItem.setToggleGroup(group);

        File file = new File(fileName);
        if (file.createNewFile())
            createConfigFile(file);
        readConfigFile(file);
    }

    private void createConfigFile(File file) throws IOException {
        FileWriter writer = new FileWriter(file, true);
        writer.write("primary-left\nautoSolve-true");
        writer.close();
    }

    private void readConfigFile(File file) throws IOException {
        for (String line : Files.readAllLines(file.toPath())) {
            line = line.toLowerCase();
            if (line.startsWith("primary")) {
                switch (line.split("-")[1]) {
                    case "left":
                        primaryButton = MouseButton.PRIMARY;
                        leftMenuItem.setSelected(true);
                        break;
                    case "right":
                        primaryButton = MouseButton.SECONDARY;
                        rightMenuItem.setSelected(true);
                        break;
                }
            } else if (line.startsWith("autosolve")) {
                switch (line.split("-")[1]) {
                    case "true":
                        ((CheckBox) buttonsContainer.getChildren().get(0)).fire();
                        break;
                }
            }
        }
    }

    private void writeConfigFile(String setting, String updatedValue) {
        File file = new File(fileName);
        List<String> lines = null;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            if (line.toLowerCase().startsWith(setting.toLowerCase())) {
                updatedLines.add(setting + "-" + updatedValue);
            } else {
                updatedLines.add(line);
            }
        }
        try {
            Files.write(file.toPath(), updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
