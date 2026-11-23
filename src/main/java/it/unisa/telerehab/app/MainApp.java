package it.unisa.telerehab.app;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point of the Telerehab Session Manager.
 *
 * <p>For now it only opens the "Hello world" window used in the Pre-Game to check
 * that Maven, JavaFX, FXML and the medsim simulator work on every machine.</p>
 */
public class MainApp extends Application {

    private static final String MAIN_VIEW = "/it/unisa/telerehab/ui/main-view.fxml";
    private static final String WINDOW_TITLE = "Telerehab Session Manager";
    private static final double WINDOW_WIDTH = 640;
    private static final double WINDOW_HEIGHT = 420;

    @Override
    public void start(Stage stage) throws IOException {
        URL viewUrl = MainApp.class.getResource(MAIN_VIEW);
        if (viewUrl == null) {
            throw new IllegalStateException("FXML not found: " + MAIN_VIEW);
        }
        Parent root = FXMLLoader.load(viewUrl);
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
