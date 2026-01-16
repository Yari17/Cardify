package view.storehomepage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import controller.StoreHPController;

import java.util.logging.Logger;

public class FXStoreHPView implements IStoreHPView {
    private static final Logger LOGGER = Logger.getLogger(FXStoreHPView.class.getName());

    @FXML
    private Label welcomeLabel;

    private StoreHPController controller;
    private Stage stage;

    @FXML
    private void initialize() {
        //TODO any initialization if needed
    }

    @Override
    public void setController(StoreHPController controller) {
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
    public void showError(String errorMessage) {
        LOGGER.severe("Error: " + errorMessage);
    }

    @Override
    public void showWelcomeMessage(String username) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Benvenuto Store " + username + "!");
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
