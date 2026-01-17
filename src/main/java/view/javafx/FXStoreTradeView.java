package view.javafx;


import controller.LiveTradeController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.bean.CardBean;
import model.bean.TradeTransactionBean;
import view.IStoreTradeView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FXStoreTradeView implements IStoreTradeView {
    private static final Logger LOGGER = Logger.getLogger(FXStoreTradeView.class.getName());

    private LiveTradeController controller;
    private Stage stage;

    @FXML
    private ListView<TradeTransactionBean> scheduledList;

    @FXML
    private ListView<TradeTransactionBean> inProgressList;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    @FXML
    private Label messageLabel;

    private static final String INSPECTION_PASSED = "INSPECTION_PASSED";
    private static final String CANCELLED = "CANCELLED";
    private static final String INSPECTION_CANCELLED_MSG = "Lo scambio è stato annullato durante l'ispezione";

    public FXStoreTradeView() {
        // default constructor for FXMLLoader
    }

    @Override
    public void setController(LiveTradeController controller) {
        this.controller = controller;
    }


    @FXML
    private void initialize() {
        // Wire controls using small helpers to reduce cognitive complexity
        if (refreshButton != null) setRefreshAction(refreshButton);
        if (backButton != null) backButton.setOnAction(e -> { if (controller != null) controller.navigateBackToStoreHome(); });

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        setupListView(scheduledList, fmt);
        setupListView(inProgressList, fmt);

        // Carica entrambe le liste all'avvio (non solo su refresh)
        if (controller != null) {
            controller.loadStoreScheduledTrades();
            controller.loadStoreInProgressTrades();
        }
    }

    // Helper: set refresh action concise
    private void setRefreshAction(Button btn) {
        btn.setOnAction(e -> {
            if (controller != null) {
                controller.loadStoreScheduledTrades();
                controller.loadStoreInProgressTrades();
            }
        });
    }

    // Helper: common ListView setup (click-to-open + cell factory)
    private void setupListView(ListView<TradeTransactionBean> listView, DateTimeFormatter fmt) {
        if (listView == null) return;
        listView.setOnMouseClicked(evt -> { if (evt.getClickCount() == 2) { TradeTransactionBean sel = listView.getSelectionModel().getSelectedItem(); if (sel != null) displayTrade(sel); } });
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TradeTransactionBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                String p = item.getProposerId() != null ? item.getProposerId() : "?";
                String r = item.getReceiverId() != null ? item.getReceiverId() : "?";
                String dt = "TBD";
                LocalDateTime ld = item.getTradeDate();
                if (ld != null) dt = ld.format(fmt);
                setText("Scambio tra " + p + " e " + r + " — " + dt);
            }
        });
    }

    @Override
    public void displayScheduledTrades(java.util.List<TradeTransactionBean> scheduled) {
        Platform.runLater(() -> {
            LOGGER.info(() -> "FXStoreTradeView.displayScheduledTrades called with count=" + (scheduled == null ? 0 : scheduled.size()));
            if (scheduledList == null) {
                LOGGER.severe("scheduledList ListView is null - FXML injection failed or view not initialized");
                if (messageLabel != null) messageLabel.setText("Errore UI: lista scambi non inizializzata");
                return;
            }
            scheduledList.getItems().clear();
            if (scheduled != null && !scheduled.isEmpty()) {
                scheduledList.setItems(FXCollections.observableArrayList(scheduled));
                // log IDs for debugging
                StringBuilder ids = new StringBuilder();
                for (TradeTransactionBean t : scheduled) ids.append(t.getTransactionId()).append(',');
                LOGGER.info(() -> "Scheduled trades IDs: " + ids);
                // Ensure the list is visible and layout is refreshed
                scheduledList.setVisible(true);
                scheduledList.setManaged(true);
                scheduledList.requestLayout();
                scheduledList.refresh();
                if (messageLabel != null) messageLabel.setText("Caricati " + scheduled.size() + " scambi");
                LOGGER.fine(() -> "Added " + scheduled.size() + " trades to scheduledList");
            } else {
                scheduledList.setItems(FXCollections.observableArrayList());
                scheduledList.setVisible(true);
                scheduledList.setManaged(true);
                if (messageLabel != null) messageLabel.setText("Nessun scambio programmato al momento");
                LOGGER.info("No scheduled trades to display");
            }
        });
    }

    @Override
    public void displayCompletedTrades(List<TradeTransactionBean> trades) {
        Platform.runLater(() -> {
            LOGGER.info(() -> "FXStoreTradeView.displayCompletedTrades called with count=" + (trades == null ? 0 : trades.size()));
            if (scheduledList == null) {
                LOGGER.severe("scheduledList ListView is null - cannot display completed trades");
                if (messageLabel != null) messageLabel.setText("Errore UI: lista scambi non inizializzata");
                return;
            }

            // Filter only COMPLETED or CANCELLED trades for display
            java.util.List<TradeTransactionBean> filtered = new java.util.ArrayList<>();
            if (trades != null && !trades.isEmpty()) {
                for (TradeTransactionBean t : trades) {
                    if (t == null) continue;
                    String s = t.getStatus();
                    if (s == null) continue;
                    if ("COMPLETED".equalsIgnoreCase(s) || "CANCELLED".equalsIgnoreCase(s)) filtered.add(t);
                }
            }

            scheduledList.getItems().clear();
            if (!filtered.isEmpty()) {
                scheduledList.setItems(FXCollections.observableArrayList(filtered));
                scheduledList.setVisible(true);
                scheduledList.setManaged(true);
                scheduledList.requestLayout();
                scheduledList.refresh();
                if (messageLabel != null) messageLabel.setText("Caricati " + filtered.size() + " scambi conclusi");
                LOGGER.fine(() -> "FXStoreTradeView: showing " + filtered.size() + " completed/cancelled trades");
            } else {
                scheduledList.setItems(FXCollections.observableArrayList());
                if (messageLabel != null) messageLabel.setText("Nessuno scambio concluso al momento");
                LOGGER.info("FXStoreTradeView.displayCompletedTrades: no completed/cancelled trades to display");
            }
        });
    }

    @Override
    public void displayInProgressTrades(java.util.List<TradeTransactionBean> inProgress) {
        Platform.runLater(() -> {
            if (inProgressList == null) return;
            inProgressList.getItems().clear();
            if (inProgress != null && !inProgress.isEmpty()) {
                inProgressList.setItems(FXCollections.observableArrayList(inProgress));
                inProgressList.setVisible(true);
                inProgressList.setManaged(true);
                inProgressList.requestLayout();
                inProgressList.refresh();
            } else {
                inProgressList.setItems(FXCollections.observableArrayList());
                inProgressList.setVisible(true);
                inProgressList.setManaged(true);
            }
        });
    }

    // Diagnostic helper called by the view factory to check whether FXML fields were injected
    public boolean isInitialized() {
        return scheduledList != null && refreshButton != null && backButton != null;
    }

    @Override
    public void displayTrade(TradeTransactionBean transaction) {
        // Build and show a modal dialog with trade details, session-code form and refresh
        Platform.runLater(() -> openTradeDialog(transaction));
    }

    // Extracted dialog builder to reduce method size
    private void openTradeDialog(TradeTransactionBean transaction) {
        try {
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Gestisci scambio - Store");

            Button backBtn = new Button("Torna indietro"); backBtn.setOnAction(e -> dialog.close());
            Label statusLabel = new Label(); statusLabel.setWrapText(true); updateStatusLabel(statusLabel, transaction.getStatus());
            TextArea details = new TextArea(); details.setEditable(false); details.setWrapText(true); details.setText(buildDetailsText(transaction));
            Label info = new Label(buildInfoText(transaction)); info.setWrapText(true);

            VBox inspectionBox = new VBox(8); inspectionBox.setVisible(false); inspectionBox.setManaged(false);
            Button concludeBtn = new Button("Concludi scambio"); concludeBtn.setVisible(false); concludeBtn.setManaged(false);
            concludeBtn.setOnAction(ev -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di concludere lo scambio e procedere allo scambio delle carte?", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> rr = a.showAndWait();
                if (rr.isPresent() && rr.get() == ButtonType.YES) {
                    boolean res = controller.concludeTrade(transaction.getTransactionId());
                    if (res) {
                        showMessage("Scambio concluso con successo");
                        TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                        if (updated != null) applyTradeUpdate(updated, info, details, statusLabel, concludeBtn);
                        concludeBtn.setDisable(true);
                    } else {
                        showError("Impossibile concludere lo scambio");
                    }
                }
            });

            Button refreshBtn = new Button("Refresh");
            refreshBtn.setOnAction(e -> {
                if (controller == null) { showError("Controller non disponibile"); return; }
                TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                if (updated != null) applyTradeUpdate(updated, info, details, statusLabel, concludeBtn);
                else showError("Impossibile aggiornare lo scambio");
            });

            // inspection / scheduled handling
            if (INSPECTION_PASSED.equals(transaction.getStatus())) {
                concludeBtn.setVisible(true); concludeBtn.setManaged(true);
                inspectionBox.getChildren().setAll(concludeBtn, refreshBtn);
                inspectionBox.setVisible(true); inspectionBox.setManaged(true);
            } else if ("INSPECTION_PHASE".equals(transaction.getStatus())) {
                inspectionBox.setVisible(true); inspectionBox.setManaged(true);
                inspectionBox.getChildren().setAll(createInspectionControls(transaction, info, details, statusLabel, concludeBtn, refreshBtn));
            } else {
                // scheduled: session code form
                Label proposerLabel = new Label("Inserisci session code di " + transaction.getProposerId());
                TextField proposerField = new TextField(); proposerField.setPromptText("Codice proposer");
                Label receiverLabel = new Label("Inserisci session code di " + transaction.getReceiverId());
                TextField receiverField = new TextField(); receiverField.setPromptText("Codice receiver");
                Button searchBtn = new Button("Convalida codici");
                searchBtn.setOnAction(e -> {
                    if (controller == null) { showError("Controller non disponibile"); return; }
                    String ptxt = proposerField.getText().trim(); String rtxt = receiverField.getText().trim();
                    int pcode, rcode;
                    try { pcode = Integer.parseInt(ptxt); rcode = Integer.parseInt(rtxt); } catch (NumberFormatException _) { showError("Entrambi i session code devono essere numeri interi"); return; }
                    TradeTransactionBean found = controller.fetchTradeBySessionCodes(pcode, rcode);
                    if (found == null) { showError("Nessuno scambio trovato per questa coppia di codici"); return; }
                    int txId = found.getTransactionId();
                    boolean okP = controller.verifySessionCode(txId, pcode); boolean okR = controller.verifySessionCode(txId, rcode);
                    if (!okP || !okR) { showError("Uno o entrambi i codici non sono validi"); return; }
                    TradeTransactionBean updated = controller.refreshTradeStatus(txId);
                    if (updated == null) { showError("Impossibile aggiornare lo scambio dopo la convalida"); return; }
                    applyTradeUpdate(updated, info, details, statusLabel, concludeBtn);
                    inspectionBox.setVisible(true); inspectionBox.setManaged(true);
                    inspectionBox.getChildren().setAll(createInspectionControls(updated, info, details, statusLabel, concludeBtn, refreshBtn));
                    if (INSPECTION_PASSED.equals(updated.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }
                });

                VBox form = new VBox(10, backBtn, statusLabel, info, proposerLabel, proposerField, receiverLabel, receiverField, new HBox(8, searchBtn, refreshBtn), details, inspectionBox);
                form.setStyle("-fx-padding: 10;");
                Scene scene = new Scene(form, 520, 520);
                dialog.setScene(scene);
                dialog.show();
                return;
            }

            VBox root = new VBox(10, backBtn, statusLabel, info, details, inspectionBox);
            root.setStyle("-fx-padding: 10;");
            Scene scene = new Scene(root, 520, 520);
            dialog.setScene(scene);
            dialog.show();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unable to show trade dialog", ex);
        }
    }

    // Apply an updated trade bean to the UI components
    private void applyTradeUpdate(TradeTransactionBean updated, Label info, TextArea details, Label statusLabel, Button concludeBtn) {
        if (updated == null) return;
        info.setText(buildInfoText(updated));
        details.setText(buildDetailsText(updated));
        updateStatusLabel(statusLabel, updated.getStatus());
        if (INSPECTION_PASSED.equals(updated.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }
        if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
    }

    // Build inspection control buttons and wire them to update the provided UI nodes
    private List<javafx.scene.Node> createInspectionControls(TradeTransactionBean txn, Label info, TextArea details, Label statusLabel, Button concludeBtn, Button refreshBtn) {
        Button proposerConfirm = new Button("Proponente: Conferma ispezione");
        Button receiverConfirm = new Button("Ricevente: Conferma ispezione");
        Button proposerProblem = new Button("Proponente: Segnala problema");
        Button receiverProblem = new Button("Ricevente: Segnala problema");

        if (Boolean.TRUE.equals(txn.getProposerInspectionOk())) proposerConfirm.setDisable(true);
        if (Boolean.TRUE.equals(txn.getReceiverInspectionOk())) receiverConfirm.setDisable(true);
        if (Boolean.FALSE.equals(txn.getProposerInspectionOk())) { proposerProblem.setDisable(true); proposerConfirm.setDisable(true); }
        if (Boolean.FALSE.equals(txn.getReceiverInspectionOk())) { receiverProblem.setDisable(true); receiverConfirm.setDisable(true); }

        proposerConfirm.setOnAction(ev -> {
            boolean ok = controller.recordInspectionResult(txn.getTransactionId(), txn.getProposerId(), true);
            if (ok) { proposerConfirm.setDisable(true); proposerProblem.setDisable(true); }
            TradeTransactionBean u2 = controller.refreshTradeStatus(txn.getTransactionId());
            if (u2 != null) applyTradeUpdate(u2, info, details, statusLabel, concludeBtn);
        });

        receiverConfirm.setOnAction(ev -> {
            boolean ok = controller.recordInspectionResult(txn.getTransactionId(), txn.getReceiverId(), true);
            if (ok) { receiverConfirm.setDisable(true); receiverProblem.setDisable(true); }
            TradeTransactionBean u2 = controller.refreshTradeStatus(txn.getTransactionId());
            if (u2 != null) applyTradeUpdate(u2, info, details, statusLabel, concludeBtn);
        });

        proposerProblem.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di segnalare un problema per le carte del proponente? Questo annullerà lo scambio.", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> res = a.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.YES) {
                boolean ok = controller.recordInspectionResult(txn.getTransactionId(), txn.getProposerId(), false);
                if (ok) { proposerProblem.setDisable(true); proposerConfirm.setDisable(true); TradeTransactionBean u2 = controller.refreshTradeStatus(txn.getTransactionId()); if (u2 != null) applyTradeUpdate(u2, info, details, statusLabel, concludeBtn); }
            }
        });

        receiverProblem.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di segnalare un problema per le carte del ricevente? Questo annullerà lo scambio.", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> res = a.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.YES) {
                boolean ok = controller.recordInspectionResult(txn.getTransactionId(), txn.getReceiverId(), false);
                if (ok) { receiverProblem.setDisable(true); receiverConfirm.setDisable(true); TradeTransactionBean u2 = controller.refreshTradeStatus(txn.getTransactionId()); if (u2 != null) applyTradeUpdate(u2, info, details, statusLabel, concludeBtn); }
            }
        });

        if (INSPECTION_PASSED.equals(txn.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }

        return java.util.List.of(proposerConfirm, proposerProblem, receiverConfirm, receiverProblem, concludeBtn, refreshBtn);
    }

    // Mostra direttamente i dettagli dello scambio in corso senza richiedere session code
    public void displayInProgressTrade(TradeTransactionBean transaction) {
        Platform.runLater(() -> {
            try {
                Stage dialog = new Stage();
                dialog.initOwner(stage);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setTitle("Dettagli scambio in corso");
                Label statusLabel = new Label();
                statusLabel.setWrapText(true);
                updateStatusLabel(statusLabel, transaction.getStatus());
                TextArea details = new TextArea();
                details.setEditable(false);
                details.setWrapText(true);
                details.setText(buildDetailsText(transaction));
                Label info = new Label(buildInfoText(transaction));
                info.setWrapText(true);
                Button refreshBtn = new Button("Refresh");
                refreshBtn.setOnAction(e -> {
                    if (controller == null) { showError("Controller non disponibile"); return; }
                    TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                    if (updated != null) {
                        info.setText(buildInfoText(updated));
                        details.setText(buildDetailsText(updated));
                        updateStatusLabel(statusLabel, updated.getStatus());
                    } else {
                        showError("Impossibile aggiornare lo scambio");
                    }
                });
                VBox root = new VBox(10, statusLabel, info, details, refreshBtn);
                root.setStyle("-fx-padding: 10;");
                Scene scene = new Scene(root, 520, 400);
                dialog.setScene(scene);
                dialog.show();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Unable to show in-progress trade dialog", ex);
            }
        });
    }

    // Metodo di utilità per aggiornare la label di stato in base allo status
    private void updateStatusLabel(Label statusLabel, String status) {
        if (statusLabel == null) return;
        if (status == null) {
            statusLabel.setText("");
            return;
        }
        switch (status) {
            case "PARTIALLY_ARRIVED" -> statusLabel.setText("Aspettando il secondo collezionista");
            case "INSPECTION_PHASE" -> statusLabel.setText("Assicurati che le carte dello scambio siano originali e in buone condizioni");
            case INSPECTION_PASSED -> statusLabel.setText("L'ispezione è stata completata con successo, procedi con lo scambio");
            default -> statusLabel.setText("");
        }
    }

    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> {
            if (messageLabel != null) messageLabel.setText(message);
        });
    }

    @Override
    public void display() {
        if (stage != null) stage.show();
    }

    @Override
    public void close() { if (stage != null) stage.close(); }

    @Override
    public void refresh() { Platform.runLater(() -> { if (scheduledList != null) scheduledList.refresh(); }); }

    @Override
    public void showError(String errorMessage) { LOGGER.log(Level.SEVERE, "StoreTradeView error: {0}", errorMessage); }

    @Override
    public void setStage(Stage stage) { this.stage = stage; }

    // Helper to build a compact info string for the store
    private String buildInfoText(TradeTransactionBean t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Proposer: ").append(t.getProposerId()).append(t.isProposerArrived() ? " (arrivato)" : " (in attesa)");
        if (t.getProposerSessionCode() > 0) sb.append(" - code: ").append(t.getProposerSessionCode());
        sb.append("\n");
        sb.append("Receiver: ").append(t.getReceiverId()).append(t.isReceiverArrived() ? " (arrivato)" : " (in attesa)");
        if (t.getReceiverSessionCode() > 0) sb.append(" - code: ").append(t.getReceiverSessionCode());
        sb.append("\nStatus: ").append(t.getStatus() != null ? t.getStatus() : "?");
        return sb.toString();
    }

    // Helper to build details text (cards) for display
    private String buildDetailsText(TradeTransactionBean t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Carte offerte:\n");
        if (t.getOffered() == null || t.getOffered().isEmpty()) sb.append(" - (nessuna)\n");
        else for (CardBean cb : t.getOffered()) sb.append(" - ").append(cb.getName()).append(" x").append(cb.getQuantity()).append("\n");
        sb.append("\nCarta richiesta:\n");
        if (t.getRequested() == null || t.getRequested().isEmpty()) sb.append(" - (nessuna)\n");
        else for (CardBean cb : t.getRequested()) sb.append(" - ").append(cb.getName()).append(" x").append(cb.getQuantity()).append("\n");
        return sb.toString();
    }

}
