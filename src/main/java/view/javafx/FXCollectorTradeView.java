package view.javafx;

import controller.LiveTradeController;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.geometry.Insets;
import view.ICollectorTradeView;

public class FXCollectorTradeView implements ICollectorTradeView {
    private static final Logger LOGGER = Logger.getLogger(FXCollectorTradeView.class.getName());

    // Constants to avoid duplicated literals
    private static final String NAV_SELECTED = "nav-selected";
    private static final String CELL_SECONDARY = "cell-secondary";
    private static final String BG_COLOR_STYLE = "-fx-background-color: #1E2530;";
    private static final String UNABLE_APPLY_MSG = "Unable to apply theme stylesheet: ";
    private static final String TEXT_FILL_WHITE = "-fx-text-fill: white;";

    private static final int ICON_W = 26;
    private static final int ICON_H = 26;

    private static final String PRESENCE_CONFIRMED_MSG = "Presenza confermata. Codice: ";

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
    private ListView<model.bean.TradeTransactionBean> scheduledTradesList;

    @FXML
    private Button liveTradeButton;

    @FXML
    private Button manageTradesButton;

    @FXML
    private ListView<model.bean.TradeTransactionBean> completedTradesList; // Nuova ListView per gli scambi conclusi

    // Navigation label fields to allow marking selected state
    @FXML
    private Label homeLabel;

    @FXML
    private Label collectionLabel;

    @FXML
    private Label liveTradeLabel;

    @FXML
    private Label manageTradesLabel;

    @FXML
    private Label logoutLabel;

    private LiveTradeController controller;
    private Stage stage;
    private boolean storeMode = false; // quando true la view si comporta come interfaccia dello store
    private String currentUsername;

    public FXCollectorTradeView() {
        // FXML fields will be injected by FXMLLoader
    }

    // Utility to load image from resource path or URL. Returns null on failure.
    private javafx.scene.image.Image loadImageResource(String path, double w, double h) {
        try {
            if (path == null) return null;
            if (path.startsWith("/")) {
                java.net.URL res = getClass().getResource(path);
                if (res != null) return new javafx.scene.image.Image(res.toExternalForm(), w, h, true, true);
            }
            return new javafx.scene.image.Image(path, w, h, true, true);
        } catch (Exception ex) {
            LOGGER.fine(() -> "Failed to load image resource: " + path + " => " + ex.getMessage());
            return null;
        }
    }

    @FXML
    public void initialize() {
        if (scheduledTradesList != null) {
            scheduledTradesList.setCellFactory(lv -> {
                // reference lv to avoid 'parameter never used' warnings
                lv.getItems();
                return new ListCell<>() {
                    @Override
                    protected void updateItem(TradeTransactionBean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }
                        setGraphic(createTradeCellContent(item));
                    }
                };
            });
        }
        if (completedTradesList != null) {
            completedTradesList.setCellFactory(lv -> {
                // reference lv to avoid 'parameter never used' warnings
                lv.getItems();
                return new ListCell<>() {
                    @Override
                    protected void updateItem(TradeTransactionBean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }
                        setGraphic(createTradeCellContent(item));
                    }
                };
            });
        }
        // Carica automaticamente sia gli scambi programmati che quelli conclusi all'apertura della pagina
        if (controller != null) {
            controller.loadScheduledTrades();
            controller.loadCollectorCompletedTrades();
        }
    }

    // Extracted helper to construct the Node used as ListCell graphic
    private Node createTradeCellContent(TradeTransactionBean item) {
        HBox root = new HBox(12);
        root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        root.getStyleClass().add("trade-list-cell");

        ImageView icon = buildIcon();
        if (icon != null) root.getChildren().add(icon);

        Node textVBox = buildTextVBox(item);
        root.getChildren().add(textVBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        root.getChildren().add(spacer);

        VBox rightBox = buildRightBox(item);
        root.getChildren().add(rightBox);

        return root;
    }

    private ImageView buildIcon() {
        javafx.scene.image.Image img = loadImageResource("/icons/trade.png", ICON_W, ICON_H);
        if (img == null) return null;
        ImageView icon = new ImageView(img);
        icon.getStyleClass().add("trade-direction-icon");
        return icon;
    }

    private VBox buildTextVBox(TradeTransactionBean item) {
        VBox txt = new VBox(4);
        String title = "tx-" + item.getTransactionId();
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("trade-cell-label");

        String participants = (item.getProposerId() != null ? item.getProposerId() : "?") + " → " + (item.getReceiverId() != null ? item.getReceiverId() : "?");
        Label participantsLabel = new Label(participants);
        participantsLabel.getStyleClass().add(CELL_SECONDARY);

        String dateText = item.getTradeDate() != null ? item.getTradeDate().toLocalDate().toString() : "TBD";
        String storeText = item.getStoreId() != null ? item.getStoreId() : "TBD";
        Label meta = new Label(dateText + " • " + storeText);
        meta.getStyleClass().add(CELL_SECONDARY);

        txt.getChildren().addAll(titleLabel, participantsLabel, meta);
        return txt;
    }

    private VBox buildRightBox(TradeTransactionBean item) {
        // Badge
        Label badge = new Label(item.getStatus() != null ? item.getStatus().toUpperCase() : "UNKNOWN");
        badge.getStyleClass().addAll("status-badge");
        applyBadgeStyle(badge, item.getStatus());

        int offeredCount = item.getOffered() != null ? item.getOffered().size() : 0;
        int requestedCount = item.getRequested() != null ? item.getRequested().size() : 0;
        Label counts = new Label("Offerte: " + offeredCount + "  •  Richieste: " + requestedCount);
        counts.getStyleClass().add(CELL_SECONDARY);

        HBox actions = buildActions(item);

        VBox rightBox = new VBox(6);
        rightBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(badge, counts, actions);
        return rightBox;
    }

    private void applyBadgeStyle(Label badge, String statusRaw) {
        if (badge == null) return;
        String status = statusRaw != null ? statusRaw.toUpperCase() : "UNKNOWN";
        switch (status) {
            case "ACCEPTED" -> badge.getStyleClass().add("badge-accepted");
            case "PENDING" -> badge.getStyleClass().add("badge-pending");
            case "REJECTED" -> badge.getStyleClass().add("badge-rejected");
            case "EXPIRED" -> badge.getStyleClass().add("badge-expired");
            default -> badge.getStyleClass().add("badge-unknown");
        }
    }

    private boolean isTradeToday(TradeTransactionBean item) {
        try {
            return item != null && item.getTradeDate() != null && item.getTradeDate().toLocalDate().equals(java.time.LocalDate.now());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }
    }

    private HBox buildActions(TradeTransactionBean item) {
        HBox actions = new HBox(6);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button tradeNowBtn = new Button("Trade Now");
        tradeNowBtn.getStyleClass().add("button-accent");
        tradeNowBtn.setOnAction(evt -> { displayTrade(item); evt.consume(); });
        actions.getChildren().add(tradeNowBtn);

        if (isTradeToday(item)) {
            Button tradeBtn = new Button("Trade");
            tradeBtn.getStyleClass().add("button-filter");
            tradeBtn.setOnAction(evt -> {
                if (controller != null) controller.startTrade(String.valueOf(item.getTransactionId()));
                evt.consume();
            });
            actions.getChildren().add(tradeBtn);
        }
        return actions;
    }

    public void setController(LiveTradeController controller) {
        this.controller = controller;
    }
    @Override
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
        // mark this view as the selected nav item
        markNavSelectedLive();
    }

    private void markNavSelectedLive() {
        // remove selected style from all
        removeNavSelectedFrom(homeLabel);
        removeNavSelectedFrom(collectionLabel);
        removeNavSelectedFrom(liveTradeLabel);
        removeNavSelectedFrom(manageTradesLabel);
        removeNavSelectedFrom(logoutLabel);

        // this view represents the Live Trades navigation
        addNavSelectedTo(liveTradeLabel);
    }

    private void addNavSelectedTo(Label l) {
        if (l != null && !l.getStyleClass().contains(NAV_SELECTED)) {
            l.getStyleClass().add(NAV_SELECTED);
        }
    }

    private void removeNavSelectedFrom(Label l) {
        if (l != null) {
            l.getStyleClass().removeIf(s -> s.equals(NAV_SELECTED));
        }
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }

    @Override
    public void refresh() {

        javafx.application.Platform.runLater(() -> {
            try {
                if (scheduledTradesList != null) scheduledTradesList.refresh();
            } catch (Exception ex) {
                LOGGER.fine(() -> "LiveTradeView refresh failed: " + ex.getMessage());
            }
        });
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
        this.currentUsername = username;
    }


    @FXML
    private void onHomeClicked() {
        if (controller != null) controller.navigateToHome();
    }

    @FXML
    private void onCollectionClicked() {
        if (controller != null) controller.navigateToCollection();
    }

    @FXML
    private void onLiveTradeClicked() {
        if (controller != null) controller.loadScheduledTrades();
    }

    @FXML
    private void onManageTradesClicked() {
        if (controller != null) navigationToManageFromLive();
    }

    // helper to call ApplicationController.navigateToManageTrade via LiveTradeController
    private void navigationToManageFromLive() {
        try {
            if (controller != null) {
                // LiveTradeController delegates to ApplicationController to navigate to Manage Trades
                controller.navigateToManage();
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "Failed to navigate to manage trades: " + ex.getMessage());
        }
    }

    @FXML
    private void onLogoutClicked() {
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
            dialog.setTitle("Trade - tx-" + transaction.getTransactionId());

            Scene scene = createTradeDialogScene(transaction);

            dialog.setScene(scene);
            dialog.showAndWait();
        });
    }

    // Extracted helper to create the Scene used to display trade details
    private Scene createTradeDialogScene(TradeTransactionBean transaction) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setStyle(BG_COLOR_STYLE);

        Label hdr = new Label("Scambio tra: " + (transaction.getProposerId() != null ? transaction.getProposerId() : "?") + " e " + (transaction.getReceiverId() != null ? transaction.getReceiverId() : "?"));
        hdr.setStyle(TEXT_FILL_WHITE + " -fx-font-size: 16px; -fx-font-weight: bold;");
        Label store = new Label("Luogo: " + (transaction.getStoreId() != null ? transaction.getStoreId() : "?")); store.setStyle(TEXT_FILL_WHITE);
        String dateStr = "?";
        if (transaction.getTradeDate() != null) {
            java.time.LocalDateTime dt = transaction.getTradeDate();
            dateStr = dt.toLocalDate().toString() + (dt.toLocalTime() != null ? " " + dt.toLocalTime().toString().substring(0,5) : "");
        }
        Label date = new Label("Data: " + dateStr); date.setStyle(TEXT_FILL_WHITE);

        // Label stato e bottone refresh (definiti una sola volta)
        Label statusLabel = new Label("Stato: " + (transaction.getStatus() != null ? transaction.getStatus() : "--"));
        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
        Button refreshBtn = new Button("Aggiorna");
        refreshBtn.setStyle("-fx-font-size: 16px;");
        refreshBtn.getStyleClass().add("button-accent");
        refreshBtn.setOnAction(ev -> {
            TradeTransactionBean refreshed = controller.refreshTradeStatus(transaction.getTransactionId());
            if (refreshed != null) {
                statusLabel.setText("Stato: " + (refreshed.getStatus() != null ? refreshed.getStatus() : "--"));
            } else {
                statusLabel.setText("Stato: Errore");
            }
            ev.consume();
        });
        HBox statusBox = new HBox(12, statusLabel, refreshBtn);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        content.getChildren().addAll(hdr, store, date, statusBox);

        HBox lists = buildTradeLists(transaction);
        content.getChildren().addAll(lists);

        // Append the store/collector specific action area (extracted for clarity)
        content.getChildren().add(createActionArea(transaction, statusLabel, refreshBtn, date));

        Scene scene = new Scene(content, 700, 500);
        try {
            java.net.URL res = getClass().getResource(view.IView.themeCssPath());
            if (res != null) scene.getStylesheets().add(res.toExternalForm());
        } catch (Exception ex) {
            LOGGER.fine(() -> UNABLE_APPLY_MSG + ex.getMessage());
        }
        return scene;
    }

    // Extracted action area builder to reduce method size and complexity
    private VBox createActionArea(TradeTransactionBean transaction, Label statusLabel, Button refreshBtn, Label date) {
        VBox actionArea = new VBox(6);
        actionArea.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (storeMode) {
            boolean nobodyArrived = (transaction.getProposerSessionCode() == 0 && transaction.getReceiverSessionCode() == 0);
            if (nobodyArrived) {
                Label msg = new Label("Nessuno dei due collezionisti è ancora arrivato in negozio, quando arriverà il primo ti fornirà il suo session code da inserire qui");
                msg.setWrapText(true);
                msg.setStyle(TEXT_FILL_WHITE);
                actionArea.getChildren().add(msg);

                javafx.scene.control.TextField codeField = new javafx.scene.control.TextField();
                codeField.setPromptText("Inserisci session code");
                actionArea.getChildren().add(codeField);

                HBox buttons = new HBox(8);
                Button submit = new Button("Conferma codice");
                buttons.getChildren().addAll(submit, refreshBtn);
                actionArea.getChildren().add(buttons);

                submit.setOnAction(ev -> {
                    String txt = codeField.getText();
                    try {
                        int code = Integer.parseInt(txt.trim());
                        boolean ok = controller.verifySessionCode(transaction.getTransactionId(), code);
                        if (ok) {
                            model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                            if (updated != null) {
                                statusLabel.setText("Codice valido. Stato corrente: " + (updated.getStatus() != null ? updated.getStatus() : "?"));
                                codeField.setDisable(true);
                                submit.setDisable(true);
                            }
                        } else {
                            Label err = new Label("Codice non valido"); err.setStyle("-fx-text-fill: #ff6b6b;"); actionArea.getChildren().add(err);
                        }
                    } catch (NumberFormatException _) {
                        Label err = new Label("Formato codice non valido"); err.setStyle("-fx-text-fill: #ff6b6b;"); actionArea.getChildren().add(err);
                    }
                    ev.consume();
                });
            } else {
                // Almeno uno è arrivato: mostra informazioni e pulsante refresh
                actionArea.getChildren().addAll(statusLabel, refreshBtn);
            }
        } else {
            boolean userIsProposer = currentUsername != null && currentUsername.equals(transaction.getProposerId());
            boolean userIsReceiver = currentUsername != null && currentUsername.equals(transaction.getReceiverId());

            if ((userIsProposer && transaction.isProposerArrived()) || (userIsReceiver && transaction.isReceiverArrived())) {
                int code = userIsProposer ? transaction.getProposerSessionCode() : transaction.getReceiverSessionCode();
                Label codeLabel = new Label(PRESENCE_CONFIRMED_MSG + (code > 0 ? code : "---") + "\nMostra questo codice allo store manager per confermare la tua presenza allo scambio");
                codeLabel.setStyle(TEXT_FILL_WHITE);
                actionArea.getChildren().add(codeLabel);
            } else {
                Button confirmBtn = new Button("Conferma la tua presenza");
                confirmBtn.setOnAction(ev -> {
                    if (controller == null) { showError("Controller non connesso"); ev.consume(); return; }
                    int code = controller.confirmPresence(transaction.getTransactionId());
                    if (code > 0) {
                        Label codeLabel = new Label(PRESENCE_CONFIRMED_MSG + code + "\nMostra questo codice allo store manager per confermare la tua presenza allo scambio");
                        codeLabel.setStyle(TEXT_FILL_WHITE);
                        actionArea.getChildren().add(codeLabel);
                        model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                        if (updated != null) {
                            try { date.setText((updated.getTradeDate() != null ? updated.getTradeDate().toLocalDate().toString() : "TBD") + " • " + (updated.getStoreId() != null ? updated.getStoreId() : "TBD")); } catch (Exception _) { /* ignore */ }
                        }
                        displayScheduledTrades(java.util.List.of(updated != null ? updated : transaction));
                        refresh();
                    } else {
                        confirmBtn.setText("Errore durante la conferma");
                    }
                    ev.consume();
                });
                actionArea.getChildren().add(confirmBtn);
            }
        }
        return actionArea;
    }

    private HBox buildTradeLists(TradeTransactionBean transaction) {
        // Left column: what the current user is giving (Stai cedendo)
        Label youGiveLabel = new Label("Stai cedendo");
        youGiveLabel.setStyle(TEXT_FILL_WHITE + " -fx-font-weight: bold;");
        ListView<String> offeredList = new ListView<>();
        if (transaction.getOffered() != null) {
            for (CardBean cb : transaction.getOffered()) offeredList.getItems().add(cb.getName() + " x" + cb.getQuantity());
        }
        VBox left = new VBox(6, youGiveLabel, offeredList);
        left.setPrefWidth(320);

        // Right column: what the user will receive (Avrai in cambio)
        Label youGetLabel = new Label("Avrai in cambio");
        youGetLabel.setStyle(TEXT_FILL_WHITE + " -fx-font-weight: bold;");
        ListView<String> requestedList = new ListView<>();
        if (transaction.getRequested() != null) {
            for (CardBean cb : transaction.getRequested()) requestedList.getItems().add(cb.getName() + " x" + cb.getQuantity());
        }
        VBox right = new VBox(6, youGetLabel, requestedList);
        right.setPrefWidth(320);

        HBox wrapper = new HBox(12, left, right);
        wrapper.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return wrapper;
    }



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
            msg.setStyle(TEXT_FILL_WHITE);
            Button ok = new Button("OK"); ok.setOnAction(ex -> { dlg.close(); ex.consume(); }); box.getChildren().addAll(msg, ok); box.setStyle(BG_COLOR_STYLE);
            Scene s = new Scene(box, 360, 120); try { java.net.URL res = getClass().getResource(view.IView.themeCssPath()); if (res != null) s.getStylesheets().add(res.toExternalForm()); } catch (Exception ex) { LOGGER.fine(() -> UNABLE_APPLY_MSG + ex.getMessage()); }
            dlg.setScene(s); dlg.showAndWait();
        });
    }

    public void onIspectionComplete(String username) {
        // simple feedback; real logic should update domain via controller
        javafx.application.Platform.runLater(() -> {
            Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.WINDOW_MODAL); dlg.setTitle("Ispezione completata");
            VBox box = new VBox(10); box.setPadding(new Insets(12));
            Label msg = new Label("Ispezione completata da: " + (username != null ? username : "?")); msg.setStyle(TEXT_FILL_WHITE);
            Button ok = new Button("OK"); ok.setOnAction(ex -> { dlg.close(); ex.consume(); }); box.getChildren().addAll(msg, ok); box.setStyle(BG_COLOR_STYLE);
            Scene s = new Scene(box, 360, 120); try { java.net.URL res = getClass().getResource(view.IView.themeCssPath()); if (res != null) s.getStylesheets().add(res.toExternalForm()); } catch (Exception ex) { LOGGER.fine(() -> UNABLE_APPLY_MSG + ex.getMessage()); }
            dlg.setScene(s); dlg.showAndWait();
        });
    }

    public void onTradeComplete(String tradeId) {
        // minimal feedback: inform user the trade is complete
        javafx.application.Platform.runLater(() -> {
            Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.WINDOW_MODAL); dlg.setTitle("Trade completato");
            VBox box = new VBox(10); box.setPadding(new Insets(12));
            Label msg = new Label("Trade " + (tradeId != null ? tradeId : "<id>") + " completato"); msg.setStyle(TEXT_FILL_WHITE);
            Button ok = new Button("OK"); ok.setOnAction(e -> { dlg.close(); e.consume(); }); box.getChildren().addAll(msg, ok); box.setStyle(BG_COLOR_STYLE);
            Scene s = new Scene(box, 360, 120); try { java.net.URL res = getClass().getResource(view.IView.themeCssPath()); if (res != null) s.getStylesheets().add(res.toExternalForm()); } catch (Exception ex) { LOGGER.fine(() -> UNABLE_APPLY_MSG + ex.getMessage()); }
            dlg.setScene(s); dlg.showAndWait();
        });
    }

    @Override
    public void displayScheduledTrades(java.util.List<model.bean.TradeTransactionBean> scheduled) {
        javafx.application.Platform.runLater(() -> {
            if (scheduledTradesList != null) {
                scheduledTradesList.getItems().clear();
                if (scheduled != null) {
                    for (model.bean.TradeTransactionBean t : scheduled) {
                        String status = t.getStatus();
                        if (status != null && (status.equalsIgnoreCase("COMPLETED") || status.equalsIgnoreCase("CANCELLED"))) {
                            continue; // Non mostrare scambi conclusi tra i programmati
                        }
                        scheduledTradesList.getItems().add(t);
                    }
                }
            }
        });
    }

    @Override
    public void displayCompletedTrades(java.util.List<model.bean.TradeTransactionBean> completedTrades) {
        if (completedTradesList == null) return;
        javafx.application.Platform.runLater(() -> {
            // Diagnostic: log incoming completed trades count
            try {
                if (completedTrades == null || completedTrades.isEmpty()) {
                    LOGGER.info(() -> "FXCollectorTradeView.displayCompletedTrades: received 0 completed trades");
                } else {
                    StringBuilder ids = new StringBuilder();
                    for (model.bean.TradeTransactionBean t : completedTrades) ids.append(t.getTransactionId()).append(',');
                    LOGGER.info(() -> "FXCollectorTradeView.displayCompletedTrades: received " + completedTrades.size() + " completed trades ids=" + ids.toString());
                }
            } catch (Exception ex) {
                LOGGER.fine(() -> "FXCollectorTradeView.displayCompletedTrades logging failed: " + ex.getMessage());
            }

            completedTradesList.getItems().clear();
            if (completedTrades != null && !completedTrades.isEmpty()) {
                completedTradesList.setItems(javafx.collections.FXCollections.observableArrayList(completedTrades));
                completedTradesList.setVisible(true);
                completedTradesList.setManaged(true);
                completedTradesList.requestLayout();
                completedTradesList.refresh();
            } else {
                completedTradesList.setItems(javafx.collections.FXCollections.observableArrayList());
                completedTradesList.setVisible(true);
                completedTradesList.setManaged(true);
            }
        });
    }

}
