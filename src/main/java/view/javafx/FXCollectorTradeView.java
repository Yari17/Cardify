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
    private ListView<model.bean.TradeTransactionBean> scheduledTradesList;

    @FXML
    private ListView<model.bean.TradeTransactionBean> completedTradesList; 

    
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
    private static final boolean STORE_MODE = false; 
    private String currentUsername;


    @FXML
    public void initialize() {
        if (scheduledTradesList != null) {
            setTradeListCellFactory(scheduledTradesList, false);
        }
        if (completedTradesList != null) {
            setTradeListCellFactory(completedTradesList, true);
        }
        
        if (controller != null) {
            controller.loadScheduledTrades();
            controller.loadCollectorCompletedTrades();
        }
    }

    
    private void setTradeListCellFactory(javafx.scene.control.ListView<model.bean.TradeTransactionBean> listView, boolean isCompleted) {
        if (listView == null) return;
        listView.setCellFactory(lv -> {
            
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
                    setGraphic(createTradeCellContent(item, isCompleted));
                }
            };
        });
    }

    
    private Node createTradeCellContent(TradeTransactionBean item, boolean isCompleted) {
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

        VBox rightBox = buildRightBox(item, isCompleted);
        root.getChildren().add(rightBox);

        return root;
    }

    private ImageView buildIcon() {
        try {
            java.net.URL res = getClass().getResource("/icons/trade.png");
            if (res == null) return null;
            javafx.scene.image.Image img = new javafx.scene.image.Image(res.toExternalForm(), ICON_W, ICON_H, true, true);
            ImageView icon = new ImageView(img);
            icon.getStyleClass().add("trade-direction-icon");
            return icon;
        } catch (Exception ex) {
            LOGGER.fine(() -> "Failed to load trade icon resource: " + ex.getMessage());
            return null;
        }
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

    private VBox buildRightBox(TradeTransactionBean item, boolean isCompleted) {
        
        Label badge = new Label(item.getStatus() != null ? item.getStatus().toUpperCase() : "UNKNOWN");
        badge.getStyleClass().addAll("status-badge");
        applyBadgeStyle(badge, item.getStatus());

        int offeredCount = item.getOffered() != null ? item.getOffered().size() : 0;
        int requestedCount = item.getRequested() != null ? item.getRequested().size() : 0;
        Label counts = new Label("Offerte: " + offeredCount + "  •  Richieste: " + requestedCount);
        counts.getStyleClass().add(CELL_SECONDARY);

        HBox actions = buildActions(item, isCompleted);

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

    private HBox buildActions(TradeTransactionBean item, boolean isCompleted) {
        HBox actions = new HBox(6);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        
        if (!isCompleted) {
            Button tradeNowBtn = new Button("Trade Now");
            tradeNowBtn.getStyleClass().add("button-accent");
            tradeNowBtn.setOnAction(evt -> { displayTrade(item); evt.consume(); });
            actions.getChildren().add(tradeNowBtn);
        }

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
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void display() {
        if (stage != null) {
            stage.show();
        } else {
            LOGGER.warning("Stage not set, cannot display");
        }
        
        markNavSelectedLive();
    }

    private void markNavSelectedLive() {
        
        removeNavSelectedFrom(homeLabel);
        removeNavSelectedFrom(collectionLabel);
        removeNavSelectedFrom(liveTradeLabel);
        removeNavSelectedFrom(manageTradesLabel);
        removeNavSelectedFrom(logoutLabel);

        
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
        /* not used */
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

    
    private void navigationToManageFromLive() {
        try {
            if (controller != null) {
                
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

    
    private VBox createActionArea(TradeTransactionBean transaction, Label statusLabel, Button refreshBtn, Label date) {
        if (STORE_MODE) {
            return createStoreActionArea(transaction, statusLabel, refreshBtn);
        }
        return createCollectorActionArea(transaction, date);
    }

    
    private VBox createStoreActionArea(TradeTransactionBean transaction, Label statusLabel, Button refreshBtn) {
        VBox actionArea = new VBox(6);
        actionArea.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        boolean nobodyArrived = (transaction.getProposerSessionCode() == 0 && transaction.getReceiverSessionCode() == 0);
        if (nobodyArrived) {
            actionArea.getChildren().add(createStoreNobodyArrivedArea(transaction, statusLabel, refreshBtn));
            return actionArea;
        }
        
        actionArea.getChildren().addAll(statusLabel, refreshBtn);
        return actionArea;
    }

    private VBox createStoreNobodyArrivedArea(TradeTransactionBean transaction, Label statusLabel, Button refreshBtn) {
        VBox node = new VBox(6);
        Label msg = new Label("Nessuno dei due collezionisti è ancora arrivato in negozio, quando arriverà il primo ti fornirà il suo session code da inserire qui");
        msg.setWrapText(true);
        msg.setStyle(TEXT_FILL_WHITE);
        node.getChildren().add(msg);

        javafx.scene.control.TextField codeField = new javafx.scene.control.TextField();
        codeField.setPromptText("Inserisci session code");
        node.getChildren().add(codeField);

        HBox buttons = new HBox(8);
        Button submit = new Button("Conferma codice");
        buttons.getChildren().addAll(submit, refreshBtn);
        node.getChildren().add(buttons);

        submit.setOnAction(ev -> handleSubmitSessionCode(ev, codeField, submit, transaction, statusLabel, node));
        return node;
    }

    
    private VBox createCollectorActionArea(TradeTransactionBean transaction, Label date) {
        VBox actionArea = new VBox(6);
        actionArea.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        boolean userIsProposer = currentUsername != null && currentUsername.equals(transaction.getProposerId());
        boolean userIsReceiver = currentUsername != null && currentUsername.equals(transaction.getReceiverId());

        if ((userIsProposer && transaction.isProposerArrived()) || (userIsReceiver && transaction.isReceiverArrived())) {
            int code = userIsProposer ? transaction.getProposerSessionCode() : transaction.getReceiverSessionCode();
            Label codeLabel = new Label(PRESENCE_CONFIRMED_MSG + (code > 0 ? code : "---") + "\nMostra questo codice allo store manager per confermare la tua presenza allo scambio");
            codeLabel.setStyle(TEXT_FILL_WHITE);
            actionArea.getChildren().add(codeLabel);
            return actionArea;
        }

        Button confirmBtn = new Button("Conferma la tua presenza");
        confirmBtn.setOnAction(ev -> handleConfirmPresence(ev, transaction, date, actionArea));
        actionArea.getChildren().add(confirmBtn);
        return actionArea;
    }

    
    private void handleConfirmPresence(javafx.event.ActionEvent ev, TradeTransactionBean transaction, Label date, VBox actionArea) {
        if (controller == null) { showError("Controller non connesso"); ev.consume(); return; }
        int code = controller.confirmPresence(transaction.getTransactionId());
        if (code > 0) {
            Label codeLabel = new Label(PRESENCE_CONFIRMED_MSG + code + "\nMostra questo codice allo store manager per confermare la tua presenza allo scambio");
            codeLabel.setStyle(TEXT_FILL_WHITE);
            actionArea.getChildren().add(codeLabel);
            model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
            processPresenceConfirmed(updated, transaction, date);
        } else {
            
            if (ev.getSource() instanceof Button b) b.setText("Errore durante la conferma");
        }
        ev.consume();
    }

    
    private void handleSubmitSessionCode(javafx.event.ActionEvent ev, javafx.scene.control.TextField codeField, Button submit, TradeTransactionBean transaction, Label statusLabel, VBox actionArea) {
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
    }

    private void processPresenceConfirmed(model.bean.TradeTransactionBean updated, TradeTransactionBean transaction, Label date) {
        if (updated != null) {
            try { date.setText((updated.getTradeDate() != null ? updated.getTradeDate().toLocalDate().toString() : "TBD") + " • " + (updated.getStoreId() != null ? updated.getStoreId() : "TBD")); } catch (Exception _) {
                // Ignoriamo eccezioni minori qui: l'aggiornamento della label della data non deve interrompere il flusso
                // dell'interfaccia utente. Il blocco è intenzionalmente vuoto per resilienza UI.
            }
        }
        displayScheduledTrades(java.util.List.of(updated != null ? updated : transaction));
        refresh();
    }

    private HBox buildTradeLists(TradeTransactionBean transaction) {
        
        Label youGiveLabel = new Label("Stai cedendo");
        youGiveLabel.setStyle(TEXT_FILL_WHITE + " -fx-font-weight: bold;");
        javafx.scene.control.ListView<String> offeredList = new javafx.scene.control.ListView<>();
        if (transaction.getOffered() != null) {
            for (CardBean cb : transaction.getOffered()) offeredList.getItems().add(cb.getName() + " x" + cb.getQuantity());
        }
        VBox left = new VBox(6, youGiveLabel, offeredList);
        left.setPrefWidth(320);

        
        Label youGetLabel = new Label("Avrai in cambio");
        youGetLabel.setStyle(TEXT_FILL_WHITE + " -fx-font-weight: bold;");
        javafx.scene.control.ListView<String> requestedList = new javafx.scene.control.ListView<>();
        if (transaction.getRequested() != null) {
            for (CardBean cb : transaction.getRequested()) requestedList.getItems().add(cb.getName() + " x" + cb.getQuantity());
        }
        VBox right = new VBox(6, youGetLabel, requestedList);
        right.setPrefWidth(320);

        HBox wrapper = new HBox(12, left, right);
        wrapper.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return wrapper;
    }

    @Override
    public void displayIspection() {
        
        javafx.application.Platform.runLater(() -> showModalDialog("Ispezione", "Inspect cards in store (UI pending)"));
    }

    @Override
    public void onIspectionComplete(String username) {
        
        javafx.application.Platform.runLater(() -> showModalDialog("Ispezione completata", "Ispezione completata da: " + (username != null ? username : "?")));
    }

    @Override
    public void displayScheduledTrades(java.util.List<model.bean.TradeTransactionBean> scheduled) {
        javafx.application.Platform.runLater(() -> {
            if (scheduledTradesList == null) return;
            scheduledTradesList.getItems().clear();
            if (scheduled == null || scheduled.isEmpty()) return;
            for (model.bean.TradeTransactionBean t : scheduled) {
                if (isFinalizedStatus(t)) continue; 
                scheduledTradesList.getItems().add(t);
            }
        });
    }

    
    private boolean isFinalizedStatus(TradeTransactionBean t) {
        if (t == null) return false;
        String status = t.getStatus();
        if (status == null) return false;
        return status.equalsIgnoreCase("COMPLETED") || status.equalsIgnoreCase("CANCELLED");
    }

    @Override
    public void displayCompletedTrades(java.util.List<model.bean.TradeTransactionBean> completedTrades) {
        if (completedTradesList == null) return;
        javafx.application.Platform.runLater(() -> {
            
            try {
                if (completedTrades == null || completedTrades.isEmpty()) {
                    LOGGER.info(() -> "FXCollectorTradeView.displayCompletedTrades: received 0 completed trades");
                } else {
                    StringBuilder ids = new StringBuilder();
                    for (model.bean.TradeTransactionBean t : completedTrades) ids.append(t.getTransactionId()).append(',');
                    String idsStr = ids.toString();
                    LOGGER.info(() -> "FXCollectorTradeView.displayCompletedTrades: received " + completedTrades.size() + " completed trades ids=" + idsStr);
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

    @Override
    public void onTradeComplete(String tradeId) {
        
        javafx.application.Platform.runLater(() -> showModalDialog("Trade completato", "Trade " + (tradeId != null ? tradeId : "<id>") + " completato"));
    }

    
    private void showModalDialog(String title, String message) {
        final int MODAL_WIDTH = 360;
        final int MODAL_HEIGHT = 120;
        Stage dlg = new Stage();
        dlg.initOwner(stage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.setTitle(title);
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));
        Label msg = new Label(message);
        msg.setStyle(TEXT_FILL_WHITE);
        Button ok = new Button("OK");
        ok.setOnAction(e -> { dlg.close(); e.consume(); });
        box.getChildren().addAll(msg, ok);
        box.setStyle(BG_COLOR_STYLE);
        Scene s = new Scene(box, MODAL_WIDTH, MODAL_HEIGHT);
        try {
            java.net.URL res = getClass().getResource(view.IView.themeCssPath());
            if (res != null) s.getStylesheets().add(res.toExternalForm());
        } catch (Exception ex) {
            LOGGER.fine(() -> UNABLE_APPLY_MSG + ex.getMessage());
        }
        dlg.setScene(s);
        dlg.showAndWait();
    }

 }
