package view.javafx;


import controller.LiveTradeController;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.util.Duration;
import javafx.util.Callback;

public class FXStoreTradeView implements IStoreTradeView {
    private static final Logger LOGGER = Logger.getLogger(FXStoreTradeView.class.getName());

    private LiveTradeController controller;
    private Stage stage;
    
    private boolean pendingSummaryUpdate = false;

    
    private static final String CARICATI_PREFIX = "Caricati ";
    private static final String INSPECTION_PHASE = "INSPECTION_PHASE";
    private static final String COMPLETED = "COMPLETED";
    private static final String CONTROLLER_NOT_AVAILABLE = "Controller non disponibile";
    private static final String PADDING_STYLE = "-fx-padding: 10;";

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



    @Override
    public void setController(LiveTradeController controller) {
        this.controller = controller;
        
        javafx.application.Platform.runLater(() -> {
            try {
                if (this.controller != null) {
                    this.controller.loadStoreScheduledTrades();
                    this.controller.loadStoreInProgressTrades();
                }
            } catch (Exception ex) {
                LOGGER.fine(() -> "setController initial load failed: " + ex.getMessage());
            }
        });
    }


    @FXML
    private void initialize() {
        
        if (refreshButton != null) setRefreshAction(refreshButton);
        if (backButton != null) backButton.setOnAction(e -> { e.consume(); if (controller != null) controller.navigateBackToStoreHome(); });

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        setupListView(scheduledList, fmt);
        setupListView(inProgressList, fmt);

        
        if (controller != null) {
            controller.loadStoreScheduledTrades();
            controller.loadStoreInProgressTrades();
        }
    }

    
    private void setRefreshAction(Button btn) {
        btn.setOnAction(e -> {
            e.consume();
            if (controller != null) {
                controller.loadStoreScheduledTrades();
                controller.loadStoreInProgressTrades();
            }
        });
    }

    
    private void setupListView(ListView<TradeTransactionBean> listView, DateTimeFormatter fmt) {
        if (listView == null) return;
        setupClickHandler(listView);
        listView.setCellFactory(createCellFactory(fmt));
    }

    private void setupClickHandler(ListView<TradeTransactionBean> listView) {
        listView.setOnMouseClicked(evt -> {
            handleListMouseClick(evt, listView);
            evt.consume();
        });
    }

    private Callback<ListView<TradeTransactionBean>, ListCell<TradeTransactionBean>> createCellFactory(DateTimeFormatter fmt) {
        return lv -> {
            
            lv.getProperties();
            return new ListCell<>() {
                @Override
                protected void updateItem(TradeTransactionBean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); return; }
                    setText(buildListCellText(item, fmt));
                }
            };
        };
    }

    
    private void handleListMouseClick(javafx.scene.input.MouseEvent evt, ListView<TradeTransactionBean> listView) {
        if (evt.getClickCount() == 2) {
            TradeTransactionBean sel = listView.getSelectionModel().getSelectedItem();
            if (sel != null) displayTrade(sel);
        }
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
                
                java.util.List<TradeTransactionBean> filtered = filterScheduledTrades(scheduled);
                scheduledList.setItems(FXCollections.observableArrayList(filtered));
                
                StringBuilder ids = new StringBuilder();
                for (TradeTransactionBean t : scheduledList.getItems()) ids.append(t.getTransactionId()).append(',');
                LOGGER.info(() -> "Scheduled trades IDs (filtered): " + ids);
                
                scheduledList.setVisible(true);
                scheduledList.setManaged(true);
                scheduledList.requestLayout();
                scheduledList.refresh();
                
                updateSummaryMessage();
                LOGGER.fine(() -> "Added " + scheduledList.getItems().size() + " trades to scheduledList");
            } else {
                scheduledList.setItems(FXCollections.observableArrayList());
                scheduledList.setVisible(true);
                scheduledList.setManaged(true);
                updateSummaryMessage();
                LOGGER.info("No scheduled trades to display");
            }
        });
    }

    @Override
    public void displayCompletedTrades(List<TradeTransactionBean> trades) {
        Platform.runLater(() -> runDisplayCompletedTrades(trades));
    }

    private void runDisplayCompletedTrades(List<TradeTransactionBean> trades) {
        LOGGER.info(() -> "FXStoreTradeView.displayCompletedTrades called with count=" + (trades == null ? 0 : trades.size()));
        if (scheduledList == null) {
            LOGGER.severe("scheduledList ListView is null - cannot display completed trades");
            if (messageLabel != null) messageLabel.setText("Errore UI: lista scambi non inizializzata");
            return;
        }

        java.util.List<TradeTransactionBean> filtered = filterCompletedTrades(trades);
        showCompletedTradesUI(filtered);
    }

    private void showCompletedTradesUI(java.util.List<TradeTransactionBean> filtered) {
        scheduledList.getItems().clear();
        if (filtered != null && !filtered.isEmpty()) {
            scheduledList.setItems(FXCollections.observableArrayList(filtered));
            scheduledList.setVisible(true);
            scheduledList.setManaged(true);
            scheduledList.requestLayout();
            scheduledList.refresh();
            if (messageLabel != null) messageLabel.setText(CARICATI_PREFIX + filtered.size() + " scambi conclusi");
            LOGGER.fine(() -> "FXStoreTradeView: showing " + filtered.size() + " completed/cancelled trades");
        } else {
            scheduledList.setItems(FXCollections.observableArrayList());
            if (messageLabel != null) messageLabel.setText("Nessuno scambio concluso al momento");
            LOGGER.info("FXStoreTradeView.displayCompletedTrades: no completed/cancelled trades to display");
        }
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
                
                updateSummaryMessage();
            } else {
                inProgressList.setItems(FXCollections.observableArrayList());
                inProgressList.setVisible(true);
                inProgressList.setManaged(true);
                updateSummaryMessage();
            }
        });
    }

    
    public boolean isInitialized() {
        return scheduledList != null && refreshButton != null && backButton != null;
    }

    @Override
    public void displayTrade(TradeTransactionBean transaction) {
        
        Platform.runLater(() -> openTradeDialog(transaction));
    }

    
    private void openTradeDialog(TradeTransactionBean transaction) {
        try {
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Gestisci scambio - Store");

            Button backBtn = new Button("Torna indietro"); backBtn.setOnAction(e -> { e.consume(); dialog.close(); });
            Label statusLabel = new Label(); statusLabel.setWrapText(true); updateStatusLabel(statusLabel, transaction.getStatus());
            TextArea details = new TextArea(); details.setEditable(false); details.setWrapText(true); details.setText(buildDetailsText(transaction));
            Label info = new Label(buildInfoText(transaction)); info.setWrapText(true);

            VBox inspectionBox = new VBox(8); inspectionBox.setVisible(false); inspectionBox.setManaged(false);
            Button concludeBtn = createConcludeButton(transaction, info, details, statusLabel);

            Button refreshBtn = new Button("Refresh");
            refreshBtn.setOnAction(e -> {
                e.consume();
                if (controller == null) { showError(CONTROLLER_NOT_AVAILABLE); return; }
                TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                if (updated != null) applyTradeUpdate(updated, info, details, statusLabel, concludeBtn);
                else showError("Impossibile aggiornare lo scambio");
            });

            if (!INSPECTION_PHASE.equals(transaction.getStatus()) && !INSPECTION_PASSED.equals(transaction.getStatus())) {
                
                showScheduledForm(dialog, transaction);
                return;
            }

            
            if (INSPECTION_PASSED.equals(transaction.getStatus())) {
                concludeBtn.setVisible(true); concludeBtn.setManaged(true);
                inspectionBox.getChildren().setAll(concludeBtn, refreshBtn);
                inspectionBox.setVisible(true); inspectionBox.setManaged(true);
            } else {
                inspectionBox.setVisible(true); inspectionBox.setManaged(true);
                inspectionBox.getChildren().setAll(createInspectionControls(transaction, info, details, statusLabel, concludeBtn, refreshBtn));
            }

            VBox root = createDialogRoot(backBtn, statusLabel, info, details, inspectionBox);
            Scene scene = new Scene(root, 520, 520);
            dialog.setScene(scene);
            dialog.show();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unable to show trade dialog", ex);
        }
    }

    private VBox createDialogRoot(Button backBtn, Label statusLabel, Label info, TextArea details, VBox inspectionBox) {
        VBox root = new VBox(10, backBtn, statusLabel, info, details, inspectionBox);
        root.setStyle(PADDING_STYLE);
        return root;
    }

    
    private void applyTradeUpdate(TradeTransactionBean updated, Label info, TextArea details, Label statusLabel, Button concludeBtn) {
        if (updated == null) return;
        info.setText(buildInfoText(updated));
        details.setText(buildDetailsText(updated));
        updateStatusLabel(statusLabel, updated.getStatus());
        if (INSPECTION_PASSED.equals(updated.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }
        if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
    }

    
    private List<javafx.scene.Node> createInspectionControls(TradeTransactionBean txn, Label info, TextArea details, Label statusLabel, Button concludeBtn, Button refreshBtn) {
        
        Button confirmInspection = createConfirmInspectionButton(txn, info, details, statusLabel, concludeBtn);
        Button proposerProblem = createProblemButton("Proponente: Segnala problema", txn, info, details, statusLabel, concludeBtn, confirmInspection);
        Button receiverProblem = createProblemButton("Ricevente: Segnala problema", txn, info, details, statusLabel, concludeBtn, confirmInspection);

        if (INSPECTION_PASSED.equals(txn.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }

        return java.util.List.of(confirmInspection, proposerProblem, receiverProblem, concludeBtn, refreshBtn);
    }

    
    private String buildListCellText(TradeTransactionBean item, DateTimeFormatter fmt) {
        if (item == null) return null;
        String p = item.getProposerId() != null ? item.getProposerId() : "?";
        String r = item.getReceiverId() != null ? item.getReceiverId() : "?";
        String dt = "TBD";
        java.time.LocalDateTime ld = item.getTradeDate();
        if (ld != null) dt = ld.format(fmt);
        return "Scambio tra " + p + " e " + r + " — " + dt;
    }

    
    private java.util.List<TradeTransactionBean> filterScheduledTrades(java.util.List<TradeTransactionBean> scheduled) {
        if (scheduled == null) return java.util.List.of();
        return scheduled.stream()
                 .filter(java.util.Objects::nonNull)
                 .filter(t -> {
                    String s = t.getStatus() != null ? t.getStatus().toUpperCase() : "";
                    return !(COMPLETED.equals(s) || CANCELLED.equals(s) || INSPECTION_PHASE.equals(s) || INSPECTION_PASSED.equals(s));
                 })
                 .toList();
    }

    
    private java.util.List<TradeTransactionBean> filterCompletedTrades(java.util.List<TradeTransactionBean> trades) {
        if (trades == null) return java.util.List.of();
        return trades.stream()
                .filter(java.util.Objects::nonNull)
                .filter(t -> {
                    String s = t.getStatus();
                    if (s == null) return false;
                    return COMPLETED.equalsIgnoreCase(s) || CANCELLED.equalsIgnoreCase(s);
                })
                .toList();
    }

    
    private void showScheduledForm(Stage dialog, TradeTransactionBean transaction) {
        
        Button backBtn = new Button("Torna indietro");
        backBtn.setOnAction(e -> { e.consume(); dialog.close(); });
        Label statusLabel = new Label(); statusLabel.setWrapText(true); updateStatusLabel(statusLabel, transaction.getStatus());
        TextArea details = new TextArea(); details.setEditable(false); details.setWrapText(true); details.setText(buildDetailsText(transaction));
        Label info = new Label(buildInfoText(transaction)); info.setWrapText(true);
        VBox inspectionBox = new VBox(8); inspectionBox.setVisible(false); inspectionBox.setManaged(false);
        Button concludeBtn = createConcludeButton(transaction, info, details, statusLabel);
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            e.consume();
            if (controller == null) { showError(CONTROLLER_NOT_AVAILABLE); return; }
            TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
            if (updated != null) applyTradeUpdate(updated, info, details, statusLabel, concludeBtn);
            else showError("Impossibile aggiornare lo scambio");
        });

        Label proposerLabel = new Label("Inserisci session code di " + transaction.getProposerId());
        TextField proposerField = new TextField(); proposerField.setPromptText("Codice proposer");
        Label receiverLabel = new Label("Inserisci session code di " + transaction.getReceiverId());
        TextField receiverField = new TextField(); receiverField.setPromptText("Codice receiver");
        Button searchBtn = new Button("Convalida codici");

        VBox form = new VBox();
        ScheduledFormContext ctx = new ScheduledFormContext();
        ctx.form = form; ctx.backBtn = backBtn; ctx.statusLabel = statusLabel; ctx.info = info; ctx.details = details; ctx.inspectionBox = inspectionBox; ctx.concludeBtn = concludeBtn; ctx.refreshBtn = refreshBtn; ctx.searchBtn = searchBtn; ctx.proposerField = proposerField; ctx.receiverField = receiverField;
        searchBtn.setOnAction(e -> { e.consume(); handleValidateSessionCodes(ctx); });

        form.getChildren().setAll(backBtn, statusLabel, info, proposerLabel, proposerField, receiverLabel, receiverField, new HBox(8, searchBtn, refreshBtn), details, inspectionBox);
        form.setStyle(PADDING_STYLE);
        Scene scene = new Scene(form, 520, 520);
        dialog.setScene(scene);
        dialog.show();
    }

    
    private void updateStatusLabel(Label statusLabel, String status) {
        if (statusLabel == null) return;
        if (status == null) {
            statusLabel.setText("");
            return;
        }
        switch (status) {
            case "PARTIALLY_ARRIVED" -> statusLabel.setText("Aspettando il secondo collezionista");
            case INSPECTION_PHASE -> statusLabel.setText("Assicurati che le carte dello scambio siano originali e in buone condizioni");
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
        if (stage != null) {
            stage.show();
            
            javafx.application.Platform.runLater(() -> {
                try {
                    if (controller != null) {
                        controller.loadStoreScheduledTrades();
                        controller.loadStoreInProgressTrades();
                    }
                } catch (Exception ex) {
                    LOGGER.fine(() -> "display initial load failed: " + ex.getMessage());
                }
            });
        }
    }

    @Override
    public void close() { if (stage != null) stage.close(); }

    @Override
    public void refresh() { Platform.runLater(() -> { if (scheduledList != null) scheduledList.refresh(); }); }

    @Override
    public void showError(String errorMessage) { LOGGER.log(Level.SEVERE, "StoreTradeView error: {0}", errorMessage); }


    public void setStage(Stage stage) { this.stage = stage; }

    
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

    
    private void updateSummaryMessage() {
        if (messageLabel == null) return;
        int scheduledCount = (scheduledList != null && scheduledList.getItems() != null) ? scheduledList.getItems().size() : 0;
        int inProgressCount = (inProgressList != null && inProgressList.getItems() != null) ? inProgressList.getItems().size() : 0;
        if (scheduledCount == 0 && inProgressCount == 0) {
            
            
            if (!pendingSummaryUpdate) {
                pendingSummaryUpdate = true;
                PauseTransition pt = new PauseTransition(Duration.millis(120));
                pt.setOnFinished(ev -> {
                    ev.consume();
                    pendingSummaryUpdate = false;
                    updateSummaryMessage();
                });
                pt.play();
            }
            
            messageLabel.setText("Aggiornamento in corso...");
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (scheduledCount > 0) sb.append(CARICATI_PREFIX).append(scheduledCount).append(" scambi");
        if (inProgressCount > 0) {
            if (!sb.isEmpty()) sb.append(" — ");
            sb.append(CARICATI_PREFIX).append(inProgressCount).append(" scambi in corso");
        }
        messageLabel.setText(sb.toString());
    }

    private Button createConcludeButton(TradeTransactionBean transaction, Label info, TextArea details, Label statusLabel) {
        Button concludeBtn = new Button("Concludi scambio");
        concludeBtn.setVisible(false);
        concludeBtn.setManaged(false);
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
            ev.consume();
        });
        return concludeBtn;
    }

    
    private Button createProblemButton(String label, TradeTransactionBean txn, Label info, TextArea details, Label statusLabel, Button concludeBtn, Button confirmInspection) {
        Button b = new Button(label);
        applyInitialProblemButtonState(b, label, txn);
        b.setOnAction(ev -> {
            ev.consume();
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di segnalare un problema per le carte? Questo annullerà lo scambio.", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> res = a.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.YES) {
                performCancelAndRefresh(txn.getTransactionId(), b, confirmInspection, info, details, statusLabel, concludeBtn);
            }
        });
        return b;
    }

    
    private void performCancelAndRefresh(int transactionId, Button callerButton, Button confirmInspection, Label info, TextArea details, Label statusLabel, Button concludeBtn) {
        boolean ok = false;
        try { ok = controller.cancelTrade(transactionId); } catch (Exception ex) { LOGGER.fine(() -> "problem cancel failed: " + ex.getMessage()); }
        if (ok) {
            if (callerButton != null) callerButton.setDisable(true);
            if (confirmInspection != null) confirmInspection.setDisable(true);
            TradeTransactionBean u2 = controller.refreshTradeStatus(transactionId);
            if (u2 != null) applyTradeUpdate(u2, info, details, statusLabel, concludeBtn);
        }
    }

    
    private Button createConfirmInspectionButton(TradeTransactionBean txn, Label info, TextArea details, Label statusLabel, Button concludeBtn) {
        Button b = new Button("Conferma ispezione");
        if (INSPECTION_PASSED.equals(txn.getStatus())) b.setDisable(true);
        b.setOnAction(ev -> {
            boolean ok = false;
            try { ok = controller.markInspectionPassed(txn.getTransactionId()); } catch (Exception ex) { LOGGER.fine(() -> "confirmInspection failed: " + ex.getMessage()); }
            if (ok) {
                b.setDisable(true);
                TradeTransactionBean u2 = controller.refreshTradeStatus(txn.getTransactionId());
                if (u2 != null) applyTradeUpdate(u2, info, details, statusLabel, concludeBtn);
            }
            ev.consume();
        });
        return b;
    }

    private void applyInitialProblemButtonState(Button b, String label, TradeTransactionBean txn) {
        if (b == null || label == null || txn == null) return;
        if (label.startsWith("Proponente") && Boolean.FALSE.equals(txn.getProposerInspectionOk())) b.setDisable(true);
        if (label.startsWith("Ricevente") && Boolean.FALSE.equals(txn.getReceiverInspectionOk())) b.setDisable(true);
    }

    
    private static final class ScheduledFormContext {
        VBox form;
        Button backBtn;
        Label statusLabel;
        Label info;
        TextArea details;
        VBox inspectionBox;
        Button concludeBtn;
        Button refreshBtn;
        Button searchBtn;
        TextField proposerField;
        TextField receiverField;

        ScheduledFormContext() { }
    }

    private void handleValidateSessionCodes(ScheduledFormContext ctx) {
         if (controller == null || ctx == null) { showError(CONTROLLER_NOT_AVAILABLE); return; }
         String ptxt = ctx.proposerField.getText().trim();
         String rtxt = ctx.receiverField.getText().trim();
         int pcode;
         int rcode;
         try { pcode = Integer.parseInt(ptxt); rcode = Integer.parseInt(rtxt); } catch (NumberFormatException _) { showError("Entrambi i session code devono essere numeri interi"); return; }
         TradeTransactionBean found = controller.fetchTradeBySessionCodes(pcode, rcode);
         if (found == null) { showError("Nessuno scambio trovato per questa coppia di codici"); return; }
         int txId = found.getTransactionId();
         boolean okP = controller.verifySessionCode(txId, pcode);
         boolean okR = controller.verifySessionCode(txId, rcode);
         if (!okP || !okR) { showError("Uno o entrambi i codici non sono validi"); return; }
         TradeTransactionBean updated = controller.refreshTradeStatus(txId);
         if (updated == null) { showError("Impossibile aggiornare lo scambio dopo la convalida"); return; }
         applyTradeUpdate(updated, ctx.info, ctx.details, ctx.statusLabel, ctx.concludeBtn);
         ctx.inspectionBox.getChildren().setAll(createInspectionControls(updated, ctx.info, ctx.details, ctx.statusLabel, ctx.concludeBtn, ctx.refreshBtn));
         ctx.inspectionBox.setVisible(true);
         ctx.inspectionBox.setManaged(true);
         ctx.form.getChildren().setAll(ctx.backBtn, ctx.statusLabel, ctx.info, ctx.details, ctx.inspectionBox);
         ctx.searchBtn.setDisable(true);
         if (INSPECTION_PASSED.equals(updated.getStatus())) { ctx.concludeBtn.setVisible(true); ctx.concludeBtn.setManaged(true); }
    }
}
