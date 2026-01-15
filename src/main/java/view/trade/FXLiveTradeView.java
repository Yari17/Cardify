package view.trade;

import controller.LiveTradeController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

import model.bean.TradeTransactionBean;
import model.bean.CardBean;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.geometry.Insets;

public class FXLiveTradeView implements ILiveTradeView {
    private static final Logger LOGGER = Logger.getLogger(FXLiveTradeView.class.getName());

    @FXML
    @SuppressWarnings("unused")
    private Label usernameLabel;

    @FXML
    @SuppressWarnings("unused")
    private ImageView profileImageView;

    @FXML
    @SuppressWarnings("unused")
    private Button homeButton;

    @FXML
    @SuppressWarnings("unused")
    private Button collectionButton;

    @FXML
    @SuppressWarnings("unused")
    private Button tradeButton;

    @FXML
    @SuppressWarnings("unused")
    private Button logoutButton;

    @FXML
    @SuppressWarnings("unused")
    private VBox logoutButtonContainer;

    @FXML
    @SuppressWarnings("unused")
    private ListView<model.bean.TradeTransactionBean> scheduledTradesList;

    @FXML
    @SuppressWarnings("unused")
    private Button liveTradeButton;

    @FXML
    @SuppressWarnings("unused")
    private Button manageTradesButton;

    private LiveTradeController controller;
    private Stage stage;

    public FXLiveTradeView() {
        // FXML fields will be injected by FXMLLoader
    }

    @FXML
    public void initialize() {
        if (scheduledTradesList != null) scheduledTradesList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(model.bean.TradeTransactionBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }

                HBox root = new HBox(12);
                root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                root.getStyleClass().add("trade-list-cell");

                // icon
                ImageView icon = null;
                try {
                    java.net.URL res = getClass().getResource("/icons/trade.png");
                    if (res != null) {
                        javafx.scene.image.Image img = new javafx.scene.image.Image(res.toExternalForm(), 26, 26, true, true);
                        icon = new ImageView(img);
                        icon.getStyleClass().add("trade-direction-icon");
                      }
                } catch (Exception err) {LOGGER.log(Level.WARNING, err.getMessage(), err);}
                if (icon != null) root.getChildren().add(icon);

                // Title + meta
                VBox txt = new VBox(4);
                String title = item.getProposalId() != null ? item.getProposalId() : "Scheduled";
                Label titleLabel = new Label(title);
                titleLabel.getStyleClass().add("trade-cell-label");

                String participants = (item.getProposerId() != null ? item.getProposerId() : "?") + " → " + (item.getReceiverId() != null ? item.getReceiverId() : "?");
                Label participantsLabel = new Label(participants);
                participantsLabel.getStyleClass().add("cell-secondary");

                // meeting info
                String dateText = item.getTradeDate() != null ? item.getTradeDate().toLocalDate().toString() : "TBD";
                String storeText = item.getStoreId() != null ? item.getStoreId() : "TBD";
                Label meta = new Label(dateText + " • " + storeText);
                meta.getStyleClass().add("cell-secondary");

                txt.getChildren().addAll(titleLabel, participantsLabel, meta);
                root.getChildren().add(txt);

                Region spacer = new Region(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                root.getChildren().add(spacer);

                // Badge for status
                Label badge = new Label(item.getStatus() != null ? item.getStatus().toUpperCase() : "UNKNOWN");
                badge.getStyleClass().addAll("status-badge");
                switch ((item.getStatus() != null ? item.getStatus().toUpperCase() : "UNKNOWN")) {
                    case "ACCEPTED" -> badge.getStyleClass().add("badge-accepted");
                    case "PENDING" -> badge.getStyleClass().add("badge-pending");
                    case "REJECTED" -> badge.getStyleClass().add("badge-rejected");
                    case "EXPIRED" -> badge.getStyleClass().add("badge-expired");
                    default -> badge.getStyleClass().add("badge-unknown");
                }

                // Card counts
                int offeredCount = item.getOffered() != null ? item.getOffered().size() : 0;
                int requestedCount = item.getRequested() != null ? item.getRequested().size() : 0;
                Label counts = new Label("Offerte: " + offeredCount + "  •  Richieste: " + requestedCount);
                counts.getStyleClass().add("cell-secondary");

                VBox rightBox = new VBox(6);
                rightBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                rightBox.getChildren().addAll(badge, counts);

                // Actions
                HBox actions = new HBox(6);
                actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                Button tradeNowBtn = new Button("Trade Now");
                tradeNowBtn.getStyleClass().add("button-accent");
                tradeNowBtn.setOnAction(evt -> {
                    // Show details dialog (reuse displayTrade)
                    displayTrade(item);
                });
                actions.getChildren().add(tradeNowBtn);

                // Show 'Trade' only on trade day
                boolean isToday = false;
                try {
                    if (item.getTradeDate() != null) {
                        isToday = item.getTradeDate().toLocalDate().equals(java.time.LocalDate.now());
                    }
                } catch (Exception err) {LOGGER.log(Level.WARNING, err.getMessage(), err);}

                if (isToday) {
                    Button tradeBtn = new Button("Trade");
                    tradeBtn.getStyleClass().add("button-filter");
                    tradeBtn.setOnAction(evt -> { if (controller != null) controller.startTrade(item.getProposalId()); });
                    actions.getChildren().add(tradeBtn);
                }

                rightBox.getChildren().add(actions);
                root.getChildren().add(rightBox);

                setGraphic(root);
            }
        });
    }

    public void setController(LiveTradeController controller) {
        this.controller = controller;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void display() {
        if (stage != null) {
            stage.show();
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
    public void showError(String errorMessage) {
        //not implemented
    }

    @Override
    public void setUsername(String username) {
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }


    @FXML
    private void onHomeClicked(javafx.event.ActionEvent ev) {
        if (controller != null) controller.navigateToHome();
    }

    @FXML
    private void onCollectionClicked(javafx.event.ActionEvent ev) {
        if (controller != null) controller.navigateToCollection();
    }

    @FXML
    private void onLiveTradeClicked(javafx.event.ActionEvent ev) {
        if (controller != null) controller.loadScheduledTrades();
    }

    @FXML
    private void onManageTradesClicked(javafx.event.ActionEvent ev) {
        if (controller != null) navigationToManageFromLive();
    }

    // helper to call ApplicationController.navigateToManageTrade via LiveTradeController
    private void navigationToManageFromLive() {
        try {
            if (controller != null) {
                // LiveTradeController delegates to ApplicationController to navigate to Manage Trades
                controller.navigateToManage();
            }
        } catch (Exception e) {
            LOGGER.fine(() -> "Failed to navigate to manage trades: " + e.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked(javafx.event.ActionEvent ev) {
        if (controller != null) controller.onLogoutRequested();
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

    @Override
    public void displayTrade(TradeTransactionBean transaction) {
        if (transaction == null) return;
        javafx.application.Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Trade - " + (transaction.getProposalId() != null ? transaction.getProposalId() : "-"));

            VBox content = new VBox(12);
            content.setPadding(new Insets(16));
            content.setStyle("-fx-background-color: #1E2530;");

            Label hdr = new Label("Scambio tra: " + (transaction.getProposerId() != null ? transaction.getProposerId() : "?") + " e " + (transaction.getReceiverId() != null ? transaction.getReceiverId() : "?"));
            hdr.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            Label store = new Label("Luogo: " + (transaction.getStoreId() != null ? transaction.getStoreId() : "?")); store.setStyle("-fx-text-fill: white;");
            Label date = new Label("Data: " + (transaction.getTradeDate() != null ? transaction.getTradeDate() : "?")); date.setStyle("-fx-text-fill: white;");

            content.getChildren().addAll(hdr, store, date);

            Label offeredLbl = new Label("Carte offerte:"); offeredLbl.setStyle("-fx-text-fill: white;");
            ListView<String> offeredList = new ListView<>();
            if (transaction.getOffered() != null) {
                for (CardBean cb : transaction.getOffered()) offeredList.getItems().add(cb.getName() + " x" + cb.getQuantity());
            }

            Label requestedLbl = new Label("Carta richiesta:"); requestedLbl.setStyle("-fx-text-fill: white;");
            ListView<String> requestedList = new ListView<>();
            if (transaction.getRequested() != null) {
                for (CardBean cb : transaction.getRequested()) requestedList.getItems().add(cb.getName() + " x" + cb.getQuantity());
            }

            HBox lists = new HBox(12, offeredList, requestedList);
            content.getChildren().addAll(offeredLbl, lists, requestedLbl);

            Button confirmBtn = new Button("Conferma la tua presenza");
            confirmBtn.setOnAction(ev -> {
                if (controller != null) {
                    int code = controller.confirmPresence(transaction.getProposalId());
                    if (code > 0) {
                        confirmBtn.setText("Presenza confermata. Codice: " + code);
                        confirmBtn.setDisable(true);
                    } else {
                        confirmBtn.setText("Errore durante la conferma");
                    }
                }
            });
            content.getChildren().add(confirmBtn);

            Scene scene = new Scene(content, 700, 500);
            try {
                java.net.URL res = getClass().getResource("/styles/theme.css");
                if (res != null) scene.getStylesheets().add(res.toExternalForm());
            } catch (Exception e) {
                LOGGER.fine(() -> "Unable to apply theme stylesheet: " + e.getMessage());
            }
            dialog.setScene(scene);
            dialog.showAndWait();
        });
    }

    @Override
    public void onConfirmPresence(String userId) {
        if (userId == null) return;
        javafx.application.Platform.runLater(() -> {
            if (controller != null) {
                int code = controller.confirmPresence(userId);
                // show simple feedback dialog
                Stage dlg = new Stage();
                dlg.initOwner(stage);
                dlg.initModality(Modality.WINDOW_MODAL);
                dlg.setTitle("Presenza");
                VBox box = new VBox(10);
                box.setPadding(new Insets(12));
                Label msg = new Label(code > 0 ? "Presenza confermata. Codice: " + code : "Errore durante la conferma");
                msg.setStyle("-fx-text-fill: white;");
                Button ok = new Button("OK");
                ok.setOnAction(e -> dlg.close());
                box.getChildren().addAll(msg, ok);
                box.setStyle("-fx-background-color: #1E2530;");
                Scene s = new Scene(box, 360, 120);
                try {
                    java.net.URL res = getClass().getResource("/styles/theme.css");
                    if (res != null) s.getStylesheets().add(res.toExternalForm());
                } catch (Exception e) {
                    LOGGER.fine(() -> "Unable to apply theme stylesheet: " + e.getMessage());
                }
                dlg.setScene(s);
                dlg.showAndWait();
            }
        });
    }

    @Override
    public void displayIspection() {
        // Minimal placeholder: show a dialog indicating inspection should happen
        javafx.application.Platform.runLater(() -> {
            Stage dlg = new Stage();
            dlg.initOwner(stage);
            dlg.initModality(Modality.WINDOW_MODAL);
            dlg.setTitle("Ispezione");
            VBox box = new VBox(10);
            box.setPadding(new Insets(12));
            Label msg = new Label("Inspect cards in store (UI pending)");
            msg.setStyle("-fx-text-fill: white;");
            Button ok = new Button("OK"); ok.setOnAction(e -> dlg.close());
            box.getChildren().addAll(msg, ok);
            box.setStyle("-fx-background-color: #1E2530;");
            Scene s = new Scene(box, 360, 120);
            try {
                java.net.URL res = getClass().getResource("/styles/theme.css");
                if (res != null) s.getStylesheets().add(res.toExternalForm());
            } catch (Exception e) {
                LOGGER.fine(() -> "Unable to apply theme stylesheet: " + e.getMessage());
            }
            dlg.setScene(s);
            dlg.showAndWait();
        });
    }

    @Override
    public void onIspectionComplete(String username) {
        // simple feedback; real logic should update domain via controller
        javafx.application.Platform.runLater(() -> {
            Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.WINDOW_MODAL); dlg.setTitle("Ispezione completata");
            VBox box = new VBox(10); box.setPadding(new Insets(12));
            Label msg = new Label("Ispezione completata da: " + (username != null ? username : "?")); msg.setStyle("-fx-text-fill: white;");
            Button ok = new Button("OK"); ok.setOnAction(e -> dlg.close()); box.getChildren().addAll(msg, ok); box.setStyle("-fx-background-color: #1E2530;");
            Scene s = new Scene(box, 360, 120); try { java.net.URL res = getClass().getResource("/styles/theme.css"); if (res != null) s.getStylesheets().add(res.toExternalForm()); } catch (Exception e) { LOGGER.fine(() -> "Unable to apply theme stylesheet: " + e.getMessage()); }
            dlg.setScene(s); dlg.showAndWait();
        });
    }

    @Override
    public void onTradeComplete(String tradeId) {
        // minimal feedback: inform user the trade is complete
        javafx.application.Platform.runLater(() -> {
            Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.WINDOW_MODAL); dlg.setTitle("Trade completato");
            VBox box = new VBox(10); box.setPadding(new Insets(12));
            Label msg = new Label("Trade " + (tradeId != null ? tradeId : "<id>") + " completato"); msg.setStyle("-fx-text-fill: white;");
            Button ok = new Button("OK"); ok.setOnAction(e -> dlg.close()); box.getChildren().addAll(msg, ok); box.setStyle("-fx-background-color: #1E2530;");
            Scene s = new Scene(box, 360, 120); try { java.net.URL res = getClass().getResource("/styles/theme.css"); if (res != null) s.getStylesheets().add(res.toExternalForm()); } catch (Exception e) { LOGGER.fine(() -> "Unable to apply theme stylesheet: " + e.getMessage()); }
            dlg.setScene(s); dlg.showAndWait();
        });
    }

    @Override
    public void displayScheduledTrades(java.util.List<model.bean.TradeTransactionBean> scheduled) {
        javafx.application.Platform.runLater(() -> {
            if (scheduledTradesList != null) {
                scheduledTradesList.getItems().clear();
                if (scheduled != null) scheduledTradesList.getItems().addAll(scheduled);
            }
        });
    }

}
