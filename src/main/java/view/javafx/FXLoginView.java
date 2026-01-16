package view.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import config.AppConfig;
import controller.LoginController;
import model.bean.UserBean;
import view.ILoginView;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FXLoginView implements ILoginView {
    private static final Logger LOGGER = Logger.getLogger(FXLoginView.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Label persistenceLabel;

    
    private LoginController loginController;

    
    private Stage stage;
    @FXML
    private void initialize() {
        
        if (persistenceLabel != null) {
            persistenceLabel.setText(AppConfig.getPersistenceLabel());
        }
    }

    @FXML
    private void onLoginClicked() {
        try {
            if (loginController != null) {
                
                loginController.onLoginRequested();
            } else {
                LOGGER.warning("Application controller not set on FXLoginView");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in login handler", ex);
        }
    }

    @FXML
    private void onRegisterClicked() {
        try {
            if (loginController != null) {
                
                loginController.onRegisterRequested();
            } else {
                LOGGER.warning("Application controller not set on FXLoginView");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unhandled exception opening registration dialog", ex);
        }
    }

    

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

    @Override
    public void showError(String errorMessage) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(errorMessage);
        } else {
            LOGGER.fine("messageLabel is null while trying to show error");
        }
    }
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void refresh() {
        // Default FX view refresh: run on UI thread. Currently no extra state to update here.
        javafx.application.Platform.runLater(() -> {
            // no-op: concrete view will override if needed
        });
    }
}
