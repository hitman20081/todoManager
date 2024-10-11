package github.hitman20081.todomanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("primary.fxml"));
        Scene scene = new Scene(root);

        // Set the default size for the stage
        stage.setWidth(800);  // Set the default width
        stage.setHeight(550); // Set the default height

        // Set the scene and show the stage
        stage.setScene(scene);
        stage.setTitle("To-Do Manager");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
