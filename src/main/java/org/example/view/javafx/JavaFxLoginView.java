package org.example.view.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.config.AppConfig;
import org.example.controller.RegistrationController;
import org.example.model.dao.UserDao;
import org.example.view.ILoginView;
import org.example.view.IRegistrationView;

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
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label persistenceLabel;

    // Reference to application controller (pure Java controller)
    private org.example.controller.LoginController appController;

    // Stage is set by the factory after loading the FXML
    private Stage stage;

    // UserDao reference needed for registration
    private UserDao userDao;

    @FXML
    private void initialize() {
        // Set the persistence type label dynamically
        if (persistenceLabel != null) {
            persistenceLabel.setText(AppConfig.getPersistenceLabel());
        }
    }

    @FXML
    private void onLoginClicked(ActionEvent event) {
        try {
            if (appController != null) {
                // Delegate to application controller
                appController.onLoginRequested();
            } else {
                LOGGER.warning("Application controller not set on JavaFxLoginView");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in login handler", e);
        }
    }

    @FXML
    private void onRegisterClicked(ActionEvent event) {
        try {
            if (userDao == null) {
                showInputError("UserDao non disponibile per la registrazione");
                LOGGER.warning("UserDao not available for registration");
                return;
            }

            // Open registration dialog
            JavaFxViewFactory viewFactory = new JavaFxViewFactory();
            IRegistrationView registrationView = viewFactory.createRegistrationView();
            RegistrationController registrationController = new RegistrationController(registrationView, userDao);
            registrationView.setController(registrationController);
            registrationController.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception opening registration dialog", e);
        }
    }

    // --- ILoginView implementation (UI-only) ---

    @Override
    public String getUsername() {
        return usernameField != null ? usernameField.getText() : "";
    }

    @Override
    public String getPassword() {
        return passwordField != null ? passwordField.getText() : "";
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
    public void setController(org.example.controller.LoginController controller) {
        this.appController = controller;
        // Extract and store the UserDao from the LoginController
        if (controller != null) {
            this.userDao = controller.getUserDao();
        }
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
    void setStage(Stage stage) {
        this.stage = stage;
    }
}
