package view.factory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.CollectorHPController;
import controller.LoginController;
import controller.RegistrationController;
import controller.StoreHPController;
import view.collectorhomepage.FXCollectorHPView;
import view.collectorhomepage.ICollectorHPView;
import view.login.ILoginView;
import view.login.FXLoginView;
import view.registration.IRegistrationView;
import view.registration.FXRegistrationView;
import view.storehomepage.IStoreHPView;
import view.storehomepage.FXStoreHPView;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FXViewFactory implements IViewFactory {
    private static final Logger LOGGER = Logger.getLogger(FXViewFactory.class.getName());

    @Override
    public ILoginView createLoginView(LoginController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof FXLoginView fxController)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXLoginView. Found: " + controllerClass
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegistrationDialog.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof FXRegistrationView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXRegistrationView. Found: " + controllerClass
                );
            }

            FXRegistrationView fxController = (FXRegistrationView) fxmlController;
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
    public ICollectorHPView createCollectorHomePageView(CollectorHPController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectorHomePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof FXCollectorHPView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXCollectorHPView. Found: " + controllerClass
                );
            }

            FXCollectorHPView fxController = (FXCollectorHPView) fxmlController;
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
    public IStoreHPView createStoreHomePageView(StoreHPController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreHomePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof FXStoreHPView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXStoreHPView. Found: " + controllerClass
                );
            }

            FXStoreHPView fxController = (FXStoreHPView) fxmlController;
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
