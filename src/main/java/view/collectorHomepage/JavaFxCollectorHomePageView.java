package view.collectorhomepage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import controller.CollectorHomePageController;

import java.util.logging.Logger;

public class JavaFxCollectorHomePageView implements ICollectorHomePageView {
    private static final Logger LOGGER = Logger.getLogger(JavaFxCollectorHomePageView.class.getName());

    @FXML
    private Label welcomeLabel;

    private CollectorHomePageController controller;
    private Stage stage;

    @FXML
    private void initialize() {
        
    }

    @Override
    public void setController(CollectorHomePageController controller) {
        this.controller = controller;
        if (controller != null && welcomeLabel != null) {
            showWelcomeMessage(controller.getUsername());
        }
    }

    @Override
    public void display() {
        if (stage != null) {
            stage.show();
        } else {
            LOGGER.warning("Stage not set, cannot display");
        }
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }

    @Override
    public void showWelcomeMessage(String username) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Benvenuto, " + username + "!");
        }
    }

    @FXML
    private void onLogoutClicked() {
        if (controller != null) {
            controller.onLogoutRequested();
        }
    }

    @FXML
    private void onExitClicked() {
        if (controller != null) {
            controller.onExitRequested();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
