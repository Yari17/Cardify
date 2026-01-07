package org.example.view.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.view.ILoginView;
import org.example.view.IRegistrationView;
import org.example.view.IViewFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaFxViewFactory implements IViewFactory {
    private static final Logger LOGGER = Logger.getLogger(JavaFxViewFactory.class.getName());

    @Override
    public ILoginView createLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginPage.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof JavaFxLoginView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify Login");
                fxController.setStage(stage);
                return fxController;
            } else {
                LOGGER.severe("Loaded FXML controller is not JavaFxLoginView");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load LoginPage.fxml", e);
        }
        return createFallbackLoginView();
    }

    @Override
    public IRegistrationView createRegistrationView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegistrationDialog.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof JavaFxRegistrationView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - Registrazione");
                fxController.setStage(stage);
                return fxController;
            } else {
                LOGGER.severe("Loaded FXML controller is not JavaFxRegistrationView");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load RegistrationDialog.fxml", e);
        }
        return createFallbackRegistrationView();
    }

    private ILoginView createFallbackLoginView() {
        return new ILoginView() {
            @Override public String getUsername() { return ""; }
            @Override public String getPassword() { return ""; }
            @Override public void showInputError(String message) { LOGGER.info("showInputError: " + message); }
            @Override public void showSuccess(String message) { LOGGER.info("showSuccess: " + message); }
            @Override public void setController(org.example.controller.LoginController controller) { /* no-op */ }
            @Override public void display() { /* no-op */ }
            @Override public void close() { /* no-op */ }
        };
    }

    private IRegistrationView createFallbackRegistrationView() {
        return new IRegistrationView() {
            @Override public String getUsername() { return ""; }
            @Override public String getPassword() { return ""; }
            @Override public void showInputError(String message) { LOGGER.info("showInputError: " + message); }
            @Override public void showSuccess(String message) { LOGGER.info("showSuccess: " + message); }
            @Override public void setController(org.example.controller.RegistrationController controller) { /* no-op */ }
            @Override public void display() { /* no-op */ }
            @Override public void close() { /* no-op */ }
        };
    }
}
