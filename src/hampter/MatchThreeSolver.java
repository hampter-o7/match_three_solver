package hampter;

import java.io.IOException;

import hampter.controller.Controller1;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MatchThreeSolver extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/scene1.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Match three solver");
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setOnShown(windowEvent -> {
            Controller1 controller = loader.getController();
            controller.populateBoard();
            scene.setOnMouseClicked(mouseEvent -> {
                controller.handleMouseClick(mouseEvent);
            });
        });
        stage.show();
    }
}