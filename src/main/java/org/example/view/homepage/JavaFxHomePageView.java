package org.example.view.homepage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.controller.HomePageController;

import java.util.logging.Logger;

/**
 * JavaFX implementation of the HomePage view.
 * This class acts as both the FXML controller and the view adapter.
 */
public class JavaFxHomePageView implements IHomePageView {
    private static final Logger LOGGER = Logger.getLogger(JavaFxHomePageView.class.getName());

    @FXML
    private Label welcomeLabel;

    private HomePageController controller;
    private Stage stage;

    @FXML
    private void initialize() {
        // Initialize JavaFX components
    }

    @Override
    public void setController(HomePageController controller) {
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

    /**
     * Package-visible method to set the stage.
     * Called by the factory after loading FXML.
     * @param stage the stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

