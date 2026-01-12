package view.trade;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import controller.TradeController;

import java.util.logging.Logger;

public class FXTradeView implements ITradeView {
    private static final Logger LOGGER = Logger.getLogger(FXTradeView.class.getName());

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Button homeButton;

    @FXML
    private Button collectionButton;

    @FXML
    private Button tradeButton;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox logoutButtonContainer;

    @FXML
    private javafx.scene.control.ListView<model.bean.TradeBean> pendingTradesList;

    @FXML
    private javafx.scene.control.ListView<model.bean.TradeBean> scheduledTradesList;

    private TradeController controller;
    private Stage stage;

    public FXTradeView() {
        // FXML fields will be injected by FXMLLoader
    }

    public void initialize() {
        // Init CellFactories for ListViews
        setupCellFactory(pendingTradesList);
        setupCellFactory(scheduledTradesList);
    }

    private void setupCellFactory(javafx.scene.control.ListView<model.bean.TradeBean> listView) {
        listView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(model.bean.TradeBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    StringBuilder text = new StringBuilder();
                    if (item.getStatus() == model.domain.TradeStatus.PENDING) {
                        text.append("Scambio #").append(item.getId())
                                .append("\nDa: ").append(item.getSenderUsername())
                                .append("\nOfferta: ").append(item.getOfferedCardsNames());
                    } else if (item.getStatus() == model.domain.TradeStatus.ACCEPTED) {
                        text.append("Scambio #").append(item.getId())
                                .append("\nCon: ").append(item.getReceiverUsername())
                                .append("\nData: ").append(item.getScheduledDate())
                                .append("\nLuogo: ").append(item.getStoreLocation());
                    } else {
                        text.append("Scambio #").append(item.getId()).append(" - ").append(item.getStatus());
                    }
                    setText(text.toString());
                    setStyle("-fx-text-fill: white; -fx-padding: 10;");
                }
            }
        });
    }

    public void setController(TradeController controller) {
        this.controller = controller;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void display() {
        if (stage != null) {
            stage.show();
            if (controller != null) {
                controller.loadTrades();
            }
        } else {
            LOGGER.warning("Stage not set, cannot display");
        }
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }

    @Override
    public void setUsername(String username) {
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }

    @Override
    public void displayTrades(java.util.List<model.bean.TradeBean> pendingTrades,
            java.util.List<model.bean.TradeBean> scheduledTrades) {
        javafx.application.Platform.runLater(() -> {
            if (pendingTradesList != null) {
                pendingTradesList.getItems().clear();
                if (pendingTrades != null) {
                    pendingTradesList.getItems().addAll(pendingTrades);
                }
            }
            if (scheduledTradesList != null) {
                scheduledTradesList.getItems().clear();
                if (scheduledTrades != null) {
                    scheduledTradesList.getItems().addAll(scheduledTrades);
                }
            }
        });
    }

    @FXML
    private void onHomeClicked() {
        if (controller != null) {
            controller.navigateToHome();
        }
    }

    @FXML
    private void onCollectionClicked() {
        if (controller != null) {
            controller.navigateToCollection();
        }
    }

    @FXML
    private void onLogoutClicked() {
        if (controller != null) {
            controller.onLogoutRequested();
        }
    }

    @FXML
    private void onNavButtonHoverEnter(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            container.setStyle(
                    "-fx-background-color: rgba(41, 182, 246, 0.2); " +
                            "-fx-background-radius: 8; " +
                            "-fx-scale-x: 1.1; " +
                            "-fx-scale-y: 1.1;");
        }
    }

    @FXML
    private void onNavButtonHoverExit(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            container.setStyle(
                    "-fx-cursor: hand; " +
                            "-fx-padding: 8; " +
                            "-fx-background-color: transparent; " +
                            "-fx-scale-x: 1.0; " +
                            "-fx-scale-y: 1.0;");
        }
    }
}
