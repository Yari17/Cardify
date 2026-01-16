package view.registration;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import config.AppConfig;
import controller.RegistrationController;
import model.bean.UserBean;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FXRegistrationView implements IRegistrationView {
    private static final Logger LOGGER = Logger.getLogger(FXRegistrationView.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private Label messageLabel;
    @FXML private Label persistenceLabel;

    
    private RegistrationController appController;

    
    private Stage stage;

    @FXML
    private void initialize() {
        
        if (persistenceLabel != null) {
            persistenceLabel.setText(AppConfig.getPersistenceLabel());
        }

        
        if (userTypeComboBox != null) {
            userTypeComboBox.getItems().addAll(
                UserBean.USER_TYPE_COLLECTOR,
                UserBean.USER_TYPE_STORE
            );
            
            userTypeComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onRegisterClicked() {
        try {
            if (appController != null) {
                
                appController.onRegisterRequested();
            } else {
                LOGGER.warning("Application controller not set on FXRegistrationView");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in registration handler", ex);
        }
    }

    

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

    @Override
    public void showError(String errorMessage) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(errorMessage);
        } else {
            LOGGER.fine("messageLabel is null while trying to show error");
        }
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
