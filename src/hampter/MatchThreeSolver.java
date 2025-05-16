package hampter;

import java.io.IOException;

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
        Scene scene = new Scene(new FXMLLoader(getClass().getResource("resources/view/scene1.fxml")).load());
        stage.setTitle("Match three solver");
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.show();
    }
}
