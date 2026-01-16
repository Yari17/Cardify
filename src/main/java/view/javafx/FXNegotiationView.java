package view.javafx;

import controller.NegotiationController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;
import model.bean.CardBean;
import model.bean.ProposalBean;
import view.INegotiationView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class FXNegotiationView implements INegotiationView {
    // Simple logger for view-level diagnostics
    private static final Logger LOGGER = Logger.getLogger(FXNegotiationView.class.getName());

    @FXML
    private ListView<CardBean> requestedList;
    @FXML
    private ListView<CardBean> inventoryList;
    @FXML
    private ListView<CardBean> proposedList;
    @FXML
    private javafx.scene.control.ComboBox<String> storeComboBox;
    @FXML
    private javafx.scene.control.TextField meetingDateField; // format YYYY-MM-DD
    @FXML
    private javafx.scene.control.TextField meetingTimeField; // format HH:mm (optional)
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    private NegotiationController controller;
    private Stage stage;

    private Consumer<CardBean> onPropose;
    private Consumer<CardBean> onUnpropose;
    private Consumer<ProposalBean> onConfirm;
    @Override
    public void setStage(Stage stage) { this.stage = stage; }

    @FXML
    private void initialize() {
        // Setup cell factories for lists to show thumbnail, name and quantity
        if (inventoryList != null) inventoryList.setCellFactory(lv -> new CardBeanCell(true));
        if (requestedList != null) requestedList.setCellFactory(lv -> new CardBeanCell(false));
        if (proposedList != null) proposedList.setCellFactory(lv -> new CardBeanCell(false));

        // populate storeComboBox lazily - controller will provide list via setController/start
        if (storeComboBox != null) {
            storeComboBox.getItems().clear();
        }

        if (addButton != null) addButton.setOnAction(e -> handleAdd());
        if (removeButton != null) removeButton.setOnAction(e -> handleRemove());
        if (confirmButton != null) confirmButton.setOnAction(e -> handleConfirm());
        if (cancelButton != null) cancelButton.setOnAction(e -> close());

        // Double-click inventory to add
        if (inventoryList != null) {
            inventoryList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) handleAdd();
            });
        }

        // Double-click proposed to remove
        if (proposedList != null) {
            proposedList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) handleRemove();
            });
        }

        // Disabilita il pulsante Confirm se non ci sono carte proposte (UX improvement)
        try {
            if (confirmButton != null && proposedList != null) {
                // Bind the disable property to the emptiness of the proposed list
                confirmButton.disableProperty().bind(Bindings.isEmpty(proposedList.getItems()));
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "Failed to bind confirm button disable property: " + ex.getMessage());
        }
    }

    @Override
    public void showInventory(List<CardBean> inventory) {
        inventoryList.getItems().clear();
        if (inventory != null) {
            // Use copies to keep original beans intact; ensure quantity is set
            for (CardBean cb : inventory) {
                CardBean copy = new CardBean(cb);
                inventoryList.getItems().add(copy);
            }
        }
    }

    @Override
    public void showRequested(List<CardBean> requested) {
        requestedList.getItems().clear();
        if (requested != null) {
            for (CardBean cb : requested) {
                CardBean copy = new CardBean(cb);
                // The requested card in a proposal must be exactly one unit
                copy.setQuantity(1);
                requestedList.getItems().add(copy);
            }
        }
    }

    @Override
    public void showProposed(List<CardBean> proposed) {
        proposedList.getItems().clear();
        if (proposed != null) proposedList.getItems().addAll(proposed);
    }

    @Override
    public void registerOnCardProposed(Consumer<CardBean> onPropose) { this.onPropose = onPropose; }

    @Override
    public void registerOnCardUnproposed(Consumer<CardBean> onUnpropose) { this.onUnpropose = onUnpropose; }

    @Override
    public void registerOnConfirmRequested(Consumer<ProposalBean> onConfirm) { this.onConfirm = onConfirm; }

    @Override
    public void setAvailableStores(List<String> storeUsernames) {
        if (storeComboBox != null) {
            storeComboBox.getItems().clear();
            if (storeUsernames != null) storeComboBox.getItems().addAll(storeUsernames);
            if (!storeComboBox.getItems().isEmpty()) storeComboBox.getSelectionModel().selectFirst();
        }
    }

    @Override
    public void setMeetingDateHint(String dateHint) {
        if (meetingDateField != null && (meetingDateField.getText() == null || meetingDateField.getText().isEmpty())) {
            meetingDateField.setText(dateHint);
        }
    }

    @Override
    public void showConfirmationResult(boolean success, String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(success ? javafx.scene.control.Alert.AlertType.INFORMATION : javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(success ? "Proposta inviata" : "Errore invio proposta");
            alert.setHeaderText(message != null ? message : (success ? "Operazione completata" : "Si è verificato un errore"));
            alert.showAndWait();
            if (success && stage != null) {
                stage.close();
            }
        });
    }

    @Override
    public void display() {
        if (stage != null) stage.show();
    }

    @Override
    public void close() {
        if (stage != null) stage.close();
    }

    @Override
    public void showError(String errorMessage) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(errorMessage != null ? errorMessage : "Si è verificato un errore");
            alert.showAndWait();
        });
    }

    @Override
    public void setController(NegotiationController controller) {
        this.controller = controller;
    }

    @Override
    public void refresh() {
        javafx.application.Platform.runLater(() -> {
            try {
                if (inventoryList != null) inventoryList.refresh();
                if (requestedList != null) requestedList.refresh();
                if (proposedList != null) proposedList.refresh();
            } catch (Exception ex) {
                LOGGER.fine(() -> "NegotiationView refresh failed: " + ex.getMessage());
            }
        });
    }

    // METODI GET: lettura degli input correnti dalla UI per il controller
    @Override
    public List<CardBean> getProposedCards() {
        return new ArrayList<>(proposedList.getItems());
    }

    @Override
    public List<CardBean> getRequestedCards() {
        return new ArrayList<>(requestedList.getItems());
    }

    @Override
    public String getSelectedStore() {
        return storeComboBox != null ? storeComboBox.getValue() : null;
    }

    @Override
    public String getMeetingDateInput() {
        return meetingDateField != null ? meetingDateField.getText() : null;
    }

    @Override
    public String getMeetingTimeInput() {
        return meetingTimeField != null ? meetingTimeField.getText() : null;
    }

    private void handleAdd() {
        CardBean sel = inventoryList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (sel.getQuantity() <= 0) return; // nothing to add

        // Decrement inventory quantity
        sel.setQuantity(sel.getQuantity() - 1);
        inventoryList.refresh();

        // Add to proposed: aggregate by id
        CardBean existing = proposedList.getItems().stream()
                .filter(c -> c.getId().equals(sel.getId()))
                .findFirst().orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
            proposedList.refresh();
        } else {
            CardBean newBean = new CardBean(sel);
            newBean.setQuantity(1);
            proposedList.getItems().add(newBean);
        }

        // update cell styles via refresh
        inventoryList.refresh();
        proposedList.refresh();

        if (onPropose != null) onPropose.accept(sel);
    }

    private void handleRemove() {
        CardBean sel = proposedList.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        // Decrement or remove from proposed
        if (sel.getQuantity() > 1) {
            sel.setQuantity(sel.getQuantity() - 1);
            proposedList.refresh();
        } else {
            proposedList.getItems().remove(sel);
        }

        // Return one to inventory: find matching inventory item and increment
        CardBean inv = inventoryList.getItems().stream()
                .filter(c -> c.getId().equals(sel.getId()))
                .findFirst().orElse(null);
        if (inv != null) {
            inv.setQuantity(inv.getQuantity() + 1);
            inventoryList.refresh();
        }

        if (onUnpropose != null) onUnpropose.accept(sel);
    }

    private void handleConfirm() {
        // Validate meeting info (store and date)
        String meetingPlace = storeComboBox != null ? storeComboBox.getValue() : null;
        String meetingDate = meetingDateField != null ? meetingDateField.getText() : null;

        // simple date validation: YYYY-MM-DD and strictly after today
        boolean dateOk = true;
        if (meetingDate == null || meetingDate.trim().isEmpty()) dateOk = false;
        else {
            try {
                java.time.LocalDate d = java.time.LocalDate.parse(meetingDate.trim());
                if (!d.isAfter(java.time.LocalDate.now())) dateOk = false;
            } catch (Exception ex) {
                LOGGER.fine(() -> "Invalid meeting date input in FXNegotiationView: " + ex.getMessage());
                dateOk = false;
            }
        }

        if (meetingPlace == null || meetingPlace.trim().isEmpty()) {
            // indicate error to user (simple close or ignored)
            // showConfirmationResult used for messages; reuse to indicate error
            if (onConfirm != null&&stage != null) {

                    javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    a.setTitle("Errore proposta");
                    a.setHeaderText("Seleziona uno store per effettuare lo scambio");
                    a.showAndWait();

            }
            return;
        }
        if (!dateOk) {
            if (stage != null) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                a.setTitle("Errore data");
                a.setHeaderText("Inserisci una data valida successiva a oggi (YYYY-MM-DD)");
                a.showAndWait();
            }
            return;
        }

        ProposalBean bean = new ProposalBean();
        // build offered as-is (may contain quantities >1)
        bean.setOffered(new ArrayList<>(proposedList.getItems()));
        // ensure each requested card is exactly one unit when sending proposal
        List<CardBean> requestedCopies = new ArrayList<>();
        for (CardBean cb : requestedList.getItems()) {
            CardBean copy = new CardBean(cb);
            copy.setQuantity(1);
            requestedCopies.add(copy);
        }
        bean.setRequested(requestedCopies);
        bean.setFromUser(controller != null ? controller.getProposerUsername() : null);
        bean.setToUser(controller != null ? controller.getTargetOwnerUsername() : null);
        bean.setMeetingPlace(meetingPlace);
        bean.setMeetingDate(meetingDate.trim());
        // read optional time if present and validate basic HH:mm format
        String meetingTime = meetingTimeField != null ? meetingTimeField.getText() : null;
        if (meetingTime != null) meetingTime = meetingTime.trim();
        if (meetingTime != null && !meetingTime.isEmpty()) {
            // basic validation HH:mm
            try {
                java.time.LocalTime.parse(meetingTime);
                bean.setMeetingTime(meetingTime);
            } catch (Exception ex) {
                if (stage != null) {
                    javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    a.setTitle("Errore orario");
                    a.setHeaderText("Inserisci un orario valido nel formato HH:mm");
                    a.showAndWait();
                }
                return;
            }
        }
        if (onConfirm != null) onConfirm.accept(bean);
    }

    // Custom cell shows thumbnail, name and quantity and allows styling when quantity==0
    private static class CardBeanCell extends ListCell<CardBean> {
        private final HBox container = new HBox(8);
        private final ImageView thumb = new ImageView();
        private final Text name = new Text();
        private final Text qty = new Text();
        private final boolean showZeroWarning;

        CardBeanCell(boolean showZeroWarning) {
            this.showZeroWarning = showZeroWarning;
            thumb.setFitWidth(40);
            thumb.setFitHeight(60);
            thumb.setPreserveRatio(true);
            HBox.setHgrow(name, Priority.ALWAYS);
            container.getChildren().addAll(thumb, name, qty);
        }

        @Override
        protected void updateItem(CardBean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setStyle("");
            } else {
                // load thumbnail if available
                try {
                    if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                        Image img = new Image(item.getImageUrl(), 40, 60, true, true, true);
                        if (!img.isError()) thumb.setImage(img);
                        else {
                            java.io.InputStream is = getClass().getResourceAsStream("/icons/nocardimage.svg");
                            if (is != null) thumb.setImage(new Image(is));
                        }
                    } else {
                        java.io.InputStream is = getClass().getResourceAsStream("/icons/nocardimage.svg");
                        if (is != null) thumb.setImage(new Image(is));
                    }
                } catch (Exception ex) {
                    LOGGER.fine(() -> "Failed to load thumbnail for negotiation cell: " + ex.getMessage());
                    java.io.InputStream is = getClass().getResourceAsStream("/icons/nocardimage.svg");
                    if (is != null) {
                        try { thumb.setImage(new Image(is)); } catch (Exception innerEx) { LOGGER.fine(() -> "Fallback image failed: " + innerEx.getMessage()); }
                    }
                }

                name.setText(item.getName() != null ? item.getName() : item.getId());
                qty.setText(" x" + Math.max(0, item.getQuantity()));

                // style when quantity is zero (light red background)
                if (showZeroWarning && item.getQuantity() <= 0) {
                    container.setStyle("-fx-background-color: rgba(255,80,80,0.12); -fx-padding:4; -fx-background-radius:4;");
                } else {
                    container.setStyle("");
                }

                setGraphic(container);
            }
        }
    }
}
