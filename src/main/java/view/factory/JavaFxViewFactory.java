package view.factory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.CollectorHomePageController;
import controller.LoginController;
import controller.RegistrationController;
import controller.StoreHomePageController;
import view.collectorhomepage.ICollectorHomePageView;
import view.collectorhomepage.JavaFxCollectorHomePageView;
import view.login.ILoginView;
import view.login.JavaFxLoginView;
import view.registration.IRegistrationView;
import view.registration.JavaFxRegistrationView;
import view.storehomepage.IStoreHomePageView;
import view.storehomepage.JavaFxStoreHomePageView;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

            
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load RegistrationDialog.fxml", e);
            throw new IllegalStateException("Cannot create RegistrationView: FXML file not found or invalid", e);
        }
    }

    @Override
    public ICollectorHomePageView createCollectorHomePageView(CollectorHomePageController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectorHomePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof JavaFxCollectorHomePageView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not JavaFxCollectorHomePageView. Found: " + controllerClass
                );
            }

            JavaFxCollectorHomePageView fxController = (JavaFxCollectorHomePageView) fxmlController;
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify - Collezionista Home");
            fxController.setStage(stage);

            
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load CollectorHomePage.fxml", e);
            throw new IllegalStateException("Cannot create CollectorHomePageView: FXML file not found or invalid", e);
        }
    }

    @Override
    public IStoreHomePageView createStoreHomePageView(StoreHomePageController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StoreHomePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof JavaFxStoreHomePageView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not JavaFxStoreHomePageView. Found: " + controllerClass
                );
            }

            JavaFxStoreHomePageView fxController = (JavaFxStoreHomePageView) fxmlController;
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify - Store Home");
            fxController.setStage(stage);

            
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load StoreHomePage.fxml", e);
            throw new IllegalStateException("Cannot create StoreHomePageView: FXML file not found or invalid", e);
        }
    }
}
