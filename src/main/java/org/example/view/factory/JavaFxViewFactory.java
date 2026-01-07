package org.example.view.factory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.HomePageController;
import org.example.controller.LoginController;
import org.example.controller.RegistrationController;
import org.example.view.homepage.IHomePageView;
import org.example.view.homepage.JavaFxHomePageView;
import org.example.view.login.ILoginView;
import org.example.view.registration.IRegistrationView;
import org.example.view.login.JavaFxLoginView;
import org.example.view.registration.JavaFxRegistrationView;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating JavaFX-based views.
 * Implements the Abstract Factory pattern.
 * Throws exceptions if view creation fails - no fallback mechanisms.
 */
public class JavaFxViewFactory implements IViewFactory {
    private static final Logger LOGGER = Logger.getLogger(JavaFxViewFactory.class.getName());

    @Override
    public ILoginView createLoginView(LoginController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof JavaFxLoginView fxController)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not JavaFxLoginView. Found: " + controllerClass
                );
            }

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify login");
            fxController.setStage(stage);

            // Inject application controller into view
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load LoginPage.fxml", e);
            throw new IllegalStateException("Cannot create LoginView: FXML file not found or invalid", e);
        }
    }

    @Override
    public IRegistrationView createRegistrationView(RegistrationController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegistrationDialog.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof JavaFxRegistrationView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not JavaFxRegistrationView. Found: " + controllerClass
                );
            }

            JavaFxRegistrationView fxController = (JavaFxRegistrationView) fxmlController;
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify - Registrazione");
            fxController.setStage(stage);

            // Inject application controller into view
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load RegistrationDialog.fxml", e);
            throw new IllegalStateException("Cannot create RegistrationView: FXML file not found or invalid", e);
        }
    }

    @Override
    public IHomePageView createHomePageView(HomePageController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof JavaFxHomePageView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not JavaFxHomePageView. Found: " + controllerClass
                );
            }

            JavaFxHomePageView fxController = (JavaFxHomePageView) fxmlController;
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify - Home");
            fxController.setStage(stage);

            // Inject application controller into view
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load HomePage.fxml", e);
            throw new IllegalStateException("Cannot create HomePageView: FXML file not found or invalid", e);
        }
    }
}
