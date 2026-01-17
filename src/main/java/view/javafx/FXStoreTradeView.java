package view.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import view.IStoreTradeView;

import java.util.logging.Level;
import java.util.logging.Logger;

import model.bean.TradeTransactionBean;
import controller.LiveTradeController;

import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

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
        // Wire the refresh button to ask the controller to reload scheduled trades
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> {
                if (controller != null) {
                    controller.loadStoreScheduledTrades();
                    controller.loadStoreInProgressTrades();
                }
            });
        }

        if (backButton != null) {
            backButton.setOnAction(e -> {
                if (controller != null) controller.navigateBackToStoreHome();
            });
        }

        // Double-click on a list item to open its details
        if (scheduledList != null) {
            scheduledList.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2) {
                    TradeTransactionBean sel = scheduledList.getSelectionModel().getSelectedItem();
                    if (sel != null) displayTrade(sel);
                }
            });

            // Custom cell display: show "Scambio tra <proposer> e <receiver> — dd/MM/yyyy HH:mm"
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            scheduledList.setCellFactory(lv -> new ListCell<TradeTransactionBean>() {
                @Override
                protected void updateItem(TradeTransactionBean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    String p = item.getProposerId() != null ? item.getProposerId() : "?";
                    String r = item.getReceiverId() != null ? item.getReceiverId() : "?";
                    String dt = "TBD";
                    LocalDateTime ld = item.getTradeDate();
                    if (ld != null) dt = ld.format(fmt);
                    setText("Scambio tra " + p + " e " + r + " — " + dt);
                }
            });
        }
        if (inProgressList != null) {
            inProgressList.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2) {
                    TradeTransactionBean sel = inProgressList.getSelectionModel().getSelectedItem();
                    if (sel != null) displayTrade(sel); // Usa la stessa UI dettagliata
                }
            });
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            inProgressList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(TradeTransactionBean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    String p = item.getProposerId() != null ? item.getProposerId() : "?";
                    String r = item.getReceiverId() != null ? item.getReceiverId() : "?";
                    String dt = "TBD";
                    LocalDateTime ld = item.getTradeDate();
                    if (ld != null) dt = ld.format(fmt);
                    setText("Scambio tra " + p + " e " + r + " — " + dt);
                }
            });
        }
        // Carica entrambe le liste all'avvio (non solo su refresh)
        if (controller != null) {
            controller.loadStoreScheduledTrades();
            controller.loadStoreInProgressTrades();
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
                scheduledList.setItems(FXCollections.observableArrayList(scheduled));
                // log IDs for debugging
                StringBuilder ids = new StringBuilder();
                for (TradeTransactionBean t : scheduled) ids.append(t.getTransactionId()).append(',');
                LOGGER.info(() -> "Scheduled trades IDs: " + ids.toString());
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
        Platform.runLater(() -> {
            try {
                Stage dialog = new Stage();
                dialog.initOwner(stage);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setTitle("Gestisci scambio - Store");

                Button backBtn = new Button("Torna indietro");
                backBtn.setOnAction(e -> dialog.close());

                Label statusLabel = new Label();
                statusLabel.setWrapText(true);
                updateStatusLabel(statusLabel, transaction.getStatus());

                TextArea details = new TextArea();
                details.setEditable(false);
                details.setWrapText(true);
                details.setText(buildDetailsText(transaction));

                Label info = new Label(buildInfoText(transaction));
                info.setWrapText(true);

                VBox inspectionBox = new VBox(8);
                inspectionBox.setVisible(false);
                inspectionBox.setManaged(false);

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
                            model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                            if (updated != null) {
                                info.setText(buildInfoText(updated));
                                details.setText(buildDetailsText(updated));
                                updateStatusLabel(statusLabel, updated.getStatus());
                            }
                            concludeBtn.setDisable(true);
                        } else {
                            showError("Impossibile concludere lo scambio");
                        }
                    }
                });

                Button refreshBtn = new Button("Refresh");
                refreshBtn.setOnAction(e -> {
                    if (controller == null) { showError("Controller non disponibile"); return; }
                    model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                    if (updated != null) {
                        info.setText(buildInfoText(updated));
                        details.setText(buildDetailsText(updated));
                        updateStatusLabel(statusLabel, updated.getStatus());
                    } else {
                        showError("Impossibile aggiornare lo scambio");
                    }
                });

                // Se lo scambio è in inspection passed, mostra solo conclude e refresh
                if (INSPECTION_PASSED.equals(transaction.getStatus())) {
                    concludeBtn.setVisible(true);
                    concludeBtn.setManaged(true);
                    inspectionBox.getChildren().clear();
                    inspectionBox.getChildren().addAll(concludeBtn, refreshBtn);
                    inspectionBox.setVisible(true);
                    inspectionBox.setManaged(true);
                } else if ("INSPECTION_PHASE".equals(transaction.getStatus())) {
                    // Mostra i bottoni di ispezione e conclude solo se inspection passed
                    inspectionBox.setVisible(true);
                    inspectionBox.setManaged(true);
                    Button proposerConfirm = new Button("Proponente: Conferma ispezione");
                    Button receiverConfirm = new Button("Ricevente: Conferma ispezione");
                    Button proposerProblem = new Button("Proponente: Segnala problema");
                    Button receiverProblem = new Button("Ricevente: Segnala problema");
                    if (Boolean.TRUE.equals(transaction.getProposerInspectionOk())) proposerConfirm.setDisable(true);
                    if (Boolean.TRUE.equals(transaction.getReceiverInspectionOk())) receiverConfirm.setDisable(true);
                    if (Boolean.FALSE.equals(transaction.getProposerInspectionOk())) { proposerProblem.setDisable(true); proposerConfirm.setDisable(true); }
                    if (Boolean.FALSE.equals(transaction.getReceiverInspectionOk())) { receiverProblem.setDisable(true); receiverConfirm.setDisable(true); }
                    proposerConfirm.setOnAction(ev -> {
                        boolean ok = controller.recordInspectionResult(transaction.getTransactionId(), transaction.getProposerId(), true);
                        if (ok) {
                            proposerConfirm.setDisable(true);
                            proposerProblem.setDisable(true);
                        }
                        model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                        if (updated != null) {
                            info.setText(buildInfoText(updated));
                            details.setText(buildDetailsText(updated));
                            updateStatusLabel(statusLabel, updated.getStatus());
                            if (INSPECTION_PASSED.equals(updated.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }
                            if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                        }
                    });
                    receiverConfirm.setOnAction(ev -> {
                        boolean ok = controller.recordInspectionResult(transaction.getTransactionId(), transaction.getReceiverId(), true);
                        if (ok) {
                            receiverConfirm.setDisable(true);
                            receiverProblem.setDisable(true);
                        }
                        model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                        if (updated != null) {
                            info.setText(buildInfoText(updated));
                            details.setText(buildDetailsText(updated));
                            updateStatusLabel(statusLabel, updated.getStatus());
                            if (INSPECTION_PASSED.equals(updated.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }
                            if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                        }
                    });
                    proposerProblem.setOnAction(ev -> {
                        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di segnalare un problema per le carte del proponente? Questo annullerà lo scambio.", ButtonType.YES, ButtonType.NO);
                        Optional<ButtonType> res = a.showAndWait();
                        if (res.isPresent() && res.get() == ButtonType.YES) {
                            boolean ok = controller.recordInspectionResult(transaction.getTransactionId(), transaction.getProposerId(), false);
                            if (ok) {
                                proposerProblem.setDisable(true);
                                proposerConfirm.setDisable(true);
                                model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                                if (updated != null) {
                                    info.setText(buildInfoText(updated));
                                    details.setText(buildDetailsText(updated));
                                    updateStatusLabel(statusLabel, updated.getStatus());
                                    if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                                }
                            }
                        }
                    });
                    receiverProblem.setOnAction(ev -> {
                        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di segnalare un problema per le carte del ricevente? Questo annullerà lo scambio.", ButtonType.YES, ButtonType.NO);
                        Optional<ButtonType> res = a.showAndWait();
                        if (res.isPresent() && res.get() == ButtonType.YES) {
                            boolean ok = controller.recordInspectionResult(transaction.getTransactionId(), transaction.getReceiverId(), false);
                            if (ok) {
                                receiverProblem.setDisable(true);
                                receiverConfirm.setDisable(true);
                                model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
                                if (updated != null) {
                                    info.setText(buildInfoText(updated));
                                    details.setText(buildDetailsText(updated));
                                    updateStatusLabel(statusLabel, updated.getStatus());
                                    if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                                }
                            }
                        }
                    });
                    inspectionBox.getChildren().clear();
                    inspectionBox.getChildren().addAll(proposerConfirm, proposerProblem, receiverConfirm, receiverProblem, concludeBtn, refreshBtn);
                } else {
                    // Programmato: mostra UI con session code e searchBtn come prima
                    Label proposerLabel = new Label("Inserisci session code di " + transaction.getProposerId());
                    TextField proposerField = new TextField();
                    proposerField.setPromptText("Codice proposer");
                    Label receiverLabel = new Label("Inserisci session code di " + transaction.getReceiverId());
                    TextField receiverField = new TextField();
                    receiverField.setPromptText("Codice receiver");
                    Button searchBtn = new Button("Ottieni informazioni scambio");
                    searchBtn.setOnAction(e -> {
                        if (controller == null) { showError("Controller non disponibile"); return; }
                        String ptxt = proposerField.getText().trim();
                        String rtxt = receiverField.getText().trim();
                        int pcode;
                        int rcode;
                        try {
                            pcode = Integer.parseInt(ptxt);
                            rcode = Integer.parseInt(rtxt);
                        } catch (NumberFormatException _) {
                            showError("Entrambi i session code devono essere numeri interi");
                            return;
                        }
                        model.bean.TradeTransactionBean found = controller.fetchTradeBySessionCodes(pcode, rcode);
                        if (found == null) {
                            showError("Nessuno scambio trovato per questa coppia di codici");
                            return;
                        }
                        info.setText(buildInfoText(found));
                        details.setText(buildDetailsText(found));
                        updateStatusLabel(statusLabel, found.getStatus());
                        inspectionBox.setVisible(true);
                        inspectionBox.setManaged(true);
                        // Mostra i controlli di ispezione/conclusione come sopra
                        Button proposerConfirm = new Button("Proponente: Conferma ispezione");
                        Button receiverConfirm = new Button("Ricevente: Conferma ispezione");
                        Button proposerProblem = new Button("Proponente: Segnala problema");
                        Button receiverProblem = new Button("Ricevente: Segnala problema");
                        if (Boolean.TRUE.equals(found.getProposerInspectionOk())) proposerConfirm.setDisable(true);
                        if (Boolean.TRUE.equals(found.getReceiverInspectionOk())) receiverConfirm.setDisable(true);
                        if (Boolean.FALSE.equals(found.getProposerInspectionOk())) { proposerProblem.setDisable(true); proposerConfirm.setDisable(true); }
                        if (Boolean.FALSE.equals(found.getReceiverInspectionOk())) { receiverProblem.setDisable(true); receiverConfirm.setDisable(true); }
                        proposerConfirm.setOnAction(ev -> {
                            boolean ok = controller.recordInspectionResult(found.getTransactionId(), found.getProposerId(), true);
                            if (ok) {
                                proposerConfirm.setDisable(true);
                                proposerProblem.setDisable(true);
                            }
                            model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(found.getTransactionId());
                            if (updated != null) {
                                info.setText(buildInfoText(updated));
                                details.setText(buildDetailsText(updated));
                                updateStatusLabel(statusLabel, updated.getStatus());
                                if (INSPECTION_PASSED.equals(updated.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }
                                if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                            }
                        });
                        receiverConfirm.setOnAction(ev -> {
                            boolean ok = controller.recordInspectionResult(found.getTransactionId(), found.getReceiverId(), true);
                            if (ok) {
                                receiverConfirm.setDisable(true);
                                receiverProblem.setDisable(true);
                            }
                            model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(found.getTransactionId());
                            if (updated != null) {
                                info.setText(buildInfoText(updated));
                                details.setText(buildDetailsText(updated));
                                updateStatusLabel(statusLabel, updated.getStatus());
                                if (INSPECTION_PASSED.equals(updated.getStatus())) { concludeBtn.setVisible(true); concludeBtn.setManaged(true); }
                                if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                            }
                        });
                        proposerProblem.setOnAction(ev -> {
                            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di segnalare un problema per le carte del proponente? Questo annullerà lo scambio.", ButtonType.YES, ButtonType.NO);
                            Optional<ButtonType> res = a.showAndWait();
                            if (res.isPresent() && res.get() == ButtonType.YES) {
                                boolean ok = controller.recordInspectionResult(found.getTransactionId(), found.getProposerId(), false);
                                if (ok) {
                                    proposerProblem.setDisable(true);
                                    proposerConfirm.setDisable(true);
                                    model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(found.getTransactionId());
                                    if (updated != null) {
                                        info.setText(buildInfoText(updated));
                                        details.setText(buildDetailsText(updated));
                                        updateStatusLabel(statusLabel, updated.getStatus());
                                        if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                                    }
                                }
                            }
                        });
                        receiverProblem.setOnAction(ev -> {
                            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Confermi di segnalare un problema per le carte del ricevente? Questo annullerà lo scambio.", ButtonType.YES, ButtonType.NO);
                            Optional<ButtonType> res = a.showAndWait();
                            if (res.isPresent() && res.get() == ButtonType.YES) {
                                boolean ok = controller.recordInspectionResult(found.getTransactionId(), found.getReceiverId(), false);
                                if (ok) {
                                    receiverProblem.setDisable(true);
                                    receiverConfirm.setDisable(true);
                                    model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(found.getTransactionId());
                                    if (updated != null) {
                                        info.setText(buildInfoText(updated));
                                        details.setText(buildDetailsText(updated));
                                        updateStatusLabel(statusLabel, updated.getStatus());
                                        if (CANCELLED.equals(updated.getStatus())) showMessage(INSPECTION_CANCELLED_MSG);
                                    }
                                }
                            }
                        });
                        if (INSPECTION_PASSED.equals(found.getStatus())) {
                            concludeBtn.setVisible(true); concludeBtn.setManaged(true);
                        } else {
                            concludeBtn.setVisible(false); concludeBtn.setManaged(false);
                        }
                        inspectionBox.getChildren().clear();
                        inspectionBox.getChildren().addAll(proposerConfirm, proposerProblem, receiverConfirm, receiverProblem, concludeBtn, refreshBtn);
                    });
                    VBox root = new VBox(10, backBtn, statusLabel, info,
                            proposerLabel, proposerField,
                            receiverLabel, receiverField,
                            new HBox(8, searchBtn, refreshBtn), details, inspectionBox);
                    root.setStyle("-fx-padding: 10;");
                    Scene scene = new Scene(root, 520, 520);
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
        });
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
                    model.bean.TradeTransactionBean updated = controller.refreshTradeStatus(transaction.getTransactionId());
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
    private String buildInfoText(model.bean.TradeTransactionBean t) {
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
    private String buildDetailsText(model.bean.TradeTransactionBean t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Carte offerte:\n");
        if (t.getOffered() == null || t.getOffered().isEmpty()) sb.append(" - (nessuna)\n");
        else for (model.bean.CardBean cb : t.getOffered()) sb.append(" - ").append(cb.getName()).append(" x").append(cb.getQuantity()).append("\n");
        sb.append("\nCarta richiesta:\n");
        if (t.getRequested() == null || t.getRequested().isEmpty()) sb.append(" - (nessuna)\n");
        else for (model.bean.CardBean cb : t.getRequested()) sb.append(" - ").append(cb.getName()).append(" x").append(cb.getQuantity()).append("\n");
        return sb.toString();
    }
}
