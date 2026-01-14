package view.factory;

import controller.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.collection.FXCollectionView;
import view.collection.ICollectionView;
import view.collectorhomepage.FXCollectorHPView;
import view.collectorhomepage.ICollectorHPView;
import view.login.ILoginView;
import view.login.FXLoginView;
import view.registration.IRegistrationView;
import view.registration.FXRegistrationView;
import view.storehomepage.IStoreHPView;
import view.storehomepage.FXStoreHPView;
import view.trade.FXTradeView;
import view.trade.ITradeView;
import view.negotiation.INegotiationView;

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

    @Override
    public ICollectionView createCollectionView(CollectionController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectionPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof FXCollectionView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXCollectionView. Found: " + controllerClass
                );
            }

            FXCollectionView fxController = (FXCollectionView) fxmlController;
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify - My Collection");
            fxController.setStage(stage);

            // Set controller
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load CollectionPage.fxml", e);
            throw new IllegalStateException("Cannot create CollectionView: FXML file not found or invalid", e);
        }
    }

    @Override
    public ITradeView createTradeView(TradeController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TradeManagerPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof FXTradeView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXTradeView. Found: " + controllerClass
                );
            }

            FXTradeView fxController = (FXTradeView) fxmlController;
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify - Trade");
            fxController.setStage(stage);

            // Set controller
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load TradeManagerPage.fxml", e);
            throw new IllegalStateException("Cannot create TradeView: FXML file not found or invalid", e);
        }
    }

    @Override
    public INegotiationView createNegotiationView(NegotiationController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TradeNegotiationPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (!(fxmlController instanceof INegotiationView)) {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXNegotiationView. Found: " + controllerClass
                );
            }

            INegotiationView fxController = (INegotiationView) fxmlController;
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cardify - Trade Negotiation");
            fxController.setStage(stage);

            // Set controller
            fxController.setController(controller);

            return fxController;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load TradeNegotiationPage.fxml", e);
            throw new IllegalStateException("Cannot create NegotiationView: FXML file not found or invalid", e);
        }
    }
}
