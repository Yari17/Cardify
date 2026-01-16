package view.factory;

import controller.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.javafx.FXCollectionView;
import view.ICollectionView;
import view.javafx.FXCollectorHPView;
import view.ICollectorHPView;
import view.javafx.FXLoginView;
import view.ILoginView;
import view.javafx.FXManageTradeView;
import view.IManageTradeView;
import view.INegotiationView;
import view.javafx.FXRegistrationView;
import view.IRegistrationView;
import view.javafx.FXStoreHPView;
import view.IStoreHPView;
import view.javafx.FXLiveTradeView;
import view.ILiveTradeView;

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

            if (fxmlController instanceof FXLoginView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify login");
                fxController.setStage(stage);

                fxController.setController(controller);

                return fxController;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXLoginView. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load LoginPage.fxml", ex);
            throw new IllegalStateException("Cannot create LoginView: FXML file not found or invalid", ex);
        }
    }

    @Override
    public IRegistrationView createRegistrationView(RegistrationController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegistrationDialog.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (fxmlController instanceof FXRegistrationView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - Registrazione");
                fxController.setStage(stage);

                fxController.setController(controller);

                return fxController;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXRegistrationView. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load RegistrationDialog.fxml", ex);
            throw new IllegalStateException("Cannot create RegistrationView: FXML file not found or invalid", ex);
        }
    }

    @Override
    public ICollectorHPView createCollectorHomePageView(CollectorHPController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectorHomePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (fxmlController instanceof FXCollectorHPView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - Collezionista Home");
                fxController.setStage(stage);

                fxController.setController(controller);

                return fxController;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXCollectorHPView. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load CollectorHomePage.fxml", ex);
            throw new IllegalStateException("Cannot create CollectorHomePageView: FXML file not found or invalid", ex);
        }
    }

    @Override
    public IStoreHPView createStoreHomePageView(StoreHPController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreHomePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (fxmlController instanceof FXStoreHPView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - Store Home");
                fxController.setStage(stage);

                fxController.setController(controller);

                return fxController;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXStoreHPView. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load StoreHomePage.fxml", ex);
            throw new IllegalStateException("Cannot create StoreHomePageView: FXML file not found or invalid", ex);
        }
    }

    @Override
    public ICollectionView createCollectionView(CollectionController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectionPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (fxmlController instanceof FXCollectionView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - My Collection");
                fxController.setStage(stage);

                // Set controller
                fxController.setController(controller);

                return fxController;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not FXCollectionView. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load CollectionPage.fxml", ex);
            throw new IllegalStateException("Cannot create CollectionView: FXML file not found or invalid", ex);
        }
    }

    @Override
    public ILiveTradeView createTradeView(LiveTradeController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LiveTradePage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (fxmlController instanceof ILiveTradeView tradeView) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - Trade");

                // Wire known concrete implementation
                if (tradeView instanceof FXLiveTradeView fxLegacy) {
                    fxLegacy.setStage(stage);
                    fxLegacy.setController(controller);
                }

                // Notify view of username via controller.setView when appropriate
                // controller.setView will set username on the view
                controller.setView(tradeView);

                return tradeView;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not a LiveTrade view. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load LiveTradePage.fxml", ex);
            throw new IllegalStateException("Cannot create TradeView: FXML file not found or invalid", ex);
        }
    }

    @Override
    public IManageTradeView createManageTradeView(ManageTradeController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManageTradesPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (fxmlController instanceof IManageTradeView manageView) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - Manage Trades");

                if (manageView instanceof FXManageTradeView fxManage) {
                    fxManage.setStage(stage);
                    // Ensure FX view has a reference to its controller so navigation handlers work
                    fxManage.setManageController(controller);
                    // register controller callbacks to decouple implementation
                    manageView.registerOnAccept(controller::acceptProposal);
                    manageView.registerOnDecline(controller::declineProposal);
                    manageView.registerOnCancel(controller::declineProposal);
                    manageView.registerOnTradeClick(controller::initiateTrade);
                    manageView.registerOnTradeNowClick(controller::initiateTrade);
                }

                return manageView;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not a ManageTrade view. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load ManageTradesPage.fxml", ex);
            throw new IllegalStateException("Cannot create ManageTradeView: FXML file not found or invalid", ex);
        }
    }

    @Override
    public INegotiationView createNegotiationView(NegotiationController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TradeNegotiationPage.fxml"));
            Parent root = loader.load();
            Object fxmlController = loader.getController();

            if (fxmlController instanceof INegotiationView fxController) {
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Cardify - Trade Negotiation");
                fxController.setStage(stage);

                // Set controller
                fxController.setController(controller);

                return fxController;
            } else {
                String controllerClass = fxmlController != null ? fxmlController.getClass().getName() : "null";
                throw new IllegalStateException(
                    "FXML controller is not a Negotiation view. Found: " + controllerClass
                );
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load TradeNegotiationPage.fxml", ex);
            throw new IllegalStateException("Cannot create NegotiationView: FXML file not found or invalid", ex);
        }
    }

}

