package view.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import controller.StoreHPController;
import view.IStoreHPView;

import java.util.logging.Logger;

public class FXStoreHPView implements IStoreHPView {
    private static final Logger LOGGER = Logger.getLogger(FXStoreHPView.class.getName());

    @FXML
    private Label welcomeLabel;

    private StoreHPController controller;
    private Stage stage;

    @FXML
    private void initialize() {
        // Inizializzazione UI: eventuali impostazioni locali della view
        // Non eseguiamo logica di business qui (passare al controller)
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
        LOGGER.log(java.util.logging.Level.SEVERE, "Error: {0}", errorMessage);
    }

    @Override
    public void showWelcomeMessage(String username) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Benvenuto Store " + username + "!");
        }
    }

    @Override
    public void refresh() {
        javafx.application.Platform.runLater(() -> {
            if (welcomeLabel != null) {
                // re-apply current text to force UI update
                welcomeLabel.setText(welcomeLabel.getText());
            }
        });
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

    @FXML
    private void onManageTradesClicked() {
        if (controller != null) {
            controller.onManageTradesRequested();
        }
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
