package org.example.view.registration;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.config.AppConfig;
import org.example.controller.RegistrationController;
import org.example.model.bean.UserBean;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX Graphic Controller implementing IRegistrationView.
 * This class acts as both the FXML controller and the view adapter.
 * - Handles UI widgets and events
 * - Delegates business logic to the Application Controller
 */
public class JavaFxRegistrationView implements IRegistrationView {
    private static final Logger LOGGER = Logger.getLogger(JavaFxRegistrationView.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private Label messageLabel;
    @FXML private Label persistenceLabel;

    // Reference to the pure-Java application controller
    private RegistrationController appController;

    // Stage set by the factory after loading FXML
    private Stage stage;

    @FXML
    private void initialize() {
        // Set the persistence type label dynamically
        if (persistenceLabel != null) {
            persistenceLabel.setText(AppConfig.getPersistenceLabel());
        }

        // Initialize userType ComboBox
        if (userTypeComboBox != null) {
            userTypeComboBox.getItems().addAll(
                UserBean.USER_TYPE_COLLECTOR,
                UserBean.USER_TYPE_STORE
            );
            // Set default selection
            userTypeComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onRegisterClicked() {
        try {
            if (appController != null) {
                // Delegate the action to application controller
                appController.onRegisterRequested();
            } else {
                LOGGER.warning("Application controller not set on JavaFxRegistrationView");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in registration handler", e);
        }
    }

    // --- IRegistrationView implementation ---

    @Override
    public UserBean getUserData() {
        String username = usernameField != null ? usernameField.getText() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        String userType = userTypeComboBox != null && userTypeComboBox.getValue() != null
            ? userTypeComboBox.getValue()
            : UserBean.USER_TYPE_COLLECTOR;

        return new UserBean(username, password, userType);
    }

    @Override
    public void showInputError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(message);
        } else {
            LOGGER.fine("messageLabel is null while trying to show error");
        }
    }

    @Override
    public void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText(message);
        } else {
            LOGGER.fine("messageLabel is null while trying to show success");
        }
    }

    @Override
    public void setController(RegistrationController controller) {
        this.appController = controller;
    }

    @Override
    public void display() {
        if (stage != null) {
            stage.show();
        } else if (usernameField != null && usernameField.getScene() != null) {
            Stage s = (Stage) usernameField.getScene().getWindow();
            if (s != null) {
                s.show();
            }
        } else {
            LOGGER.fine("Unable to show stage - stage not set");
        }
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        } else if (usernameField != null && usernameField.getScene() != null) {
            Stage s = (Stage) usernameField.getScene().getWindow();
            if (s != null) {
                s.close();
            }
        } else {
            LOGGER.fine("Unable to close stage - stage not set");
        }
    }

    // Package-visible so a factory can set the stage after loading the FXML
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}

