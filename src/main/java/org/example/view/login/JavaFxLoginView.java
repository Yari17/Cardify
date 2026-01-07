package org.example.view.login;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.config.AppConfig;
import org.example.controller.LoginController;
import org.example.model.bean.UserBean;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX Graphic Controller implementing ILoginView.
 * This class acts as both the FXML controller and the view adapter.
 * - Handles UI widgets and events
 * - Delegates business logic to the Application Controller
 */
public class JavaFxLoginView implements ILoginView {
    private static final Logger LOGGER = Logger.getLogger(JavaFxLoginView.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Label persistenceLabel;

    // Reference to application controller (pure Java controller)
    private LoginController loginController;

    // Stage is set by the factory after loading the FXML
    private Stage stage;


    @FXML
    private void initialize() {
        // Set the persistence type label dynamically
        if (persistenceLabel != null) {
            persistenceLabel.setText(AppConfig.getPersistenceLabel());
        }
    }

    @FXML
    private void onLoginClicked() {
        try {
            if (loginController != null) {
                // Delegate to application controller
                loginController.onLoginRequested();
            } else {
                LOGGER.warning("Application controller not set on JavaFxLoginView");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in login handler", e);
        }
    }

    @FXML
    private void onRegisterClicked() {
        try {
            if (loginController != null) {
                // Delegate to application controller
                loginController.onRegisterRequested();
            } else {
                LOGGER.warning("Application controller not set on JavaFxLoginView");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception opening registration dialog", e);
        }
    }

    // --- ILoginView implementation (UI-only) ---

    @Override
    public UserBean getUserCredentials() {
        String username = usernameField != null ? usernameField.getText() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        return new UserBean(username, password);
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
    public void setController(LoginController controller) {
        this.loginController = controller;
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

    // Package-visible so factory can set the stage after loading the FXML
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
