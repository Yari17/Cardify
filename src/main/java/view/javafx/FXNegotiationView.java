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
    
    private static final Logger LOGGER = Logger.getLogger(FXNegotiationView.class.getName());
    private static final String NOCARDIMAGE_ICONS_PATH = "/icons/nocardimage.svg";

    @FXML
    private ListView<CardBean> requestedList;
    @FXML
    private ListView<CardBean> inventoryList;
    @FXML
    private ListView<CardBean> proposedList;
    @FXML
    private javafx.scene.control.ComboBox<String> storeComboBox;
    @FXML
    private javafx.scene.control.TextField meetingDateField; 
    @FXML
    private javafx.scene.control.TextField meetingTimeField; 
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

    public void setStage(Stage stage) { this.stage = stage; }

    @FXML
    private void initialize() {
        setupCellFactories();
        setupButtons();
        setupDoubleClickHandlers();
        setupBindings();
    }

    private void setupCellFactories() {
        if (inventoryList != null) inventoryList.setCellFactory(lv -> {
            
            lv.getProperties();
            return new CardBeanCell(true);
        });
        if (requestedList != null) requestedList.setCellFactory(lv -> {
            lv.getProperties();
            return new CardBeanCell(false);
        });
        if (proposedList != null) proposedList.setCellFactory(lv -> {
            lv.getProperties();
            return new CardBeanCell(false);
        });

        if (storeComboBox != null) {
            storeComboBox.getItems().clear();
        }
    }

    private void setupButtons() {
        Button[] buttons = new Button[]{addButton, removeButton, confirmButton, cancelButton};
        Runnable[] actions = new Runnable[]{this::handleAdd, this::handleRemove, this::handleConfirm, this::close};
        for (int i = 0; i < buttons.length; i++) {
            bindButtonToHandler(buttons[i], actions[i]);
        }
    }

    private void bindButtonToHandler(Button btn, Runnable action) {
        if (btn == null || action == null) return;
        btn.setOnAction(e -> {
            
            e.consume();
            action.run();
        });
    }

    private void setupDoubleClickHandlers() {
        if (inventoryList != null) {
            inventoryList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) handleAdd();
            });
        }

        if (proposedList != null) {
            proposedList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) handleRemove();
            });
        }
    }

    private void setupBindings() {
        
        try {
            if (confirmButton != null && proposedList != null) {
                
                confirmButton.disableProperty().bind(Bindings.isEmpty(proposedList.getItems()));
            }
        } catch (Exception _) {
            LOGGER.fine(() -> "Failed to bind confirm button disable property");
        }
    }

    @Override
    public void showInventory(List<CardBean> inventory) {
        inventoryList.getItems().clear();
        if (inventory != null) {
            
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
    public void showAvailableStores(List<String> storeUsernames) {
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
            String headerText;
            if (message != null) headerText = message;
            else if (success) headerText = "Operazione completata";
            else headerText = "Si è verificato un errore";
            alert.setHeaderText(headerText);
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
            } catch (Exception _) {
                LOGGER.fine(() -> "NegotiationView refresh failed");
            }
        });
    }

    private void handleAdd() {
        CardBean sel = inventoryList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (sel.getQuantity() <= 0) return; 

        
        sel.setQuantity(sel.getQuantity() - 1);
        inventoryList.refresh();

        
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

        
        inventoryList.refresh();
        proposedList.refresh();

        if (onPropose != null) onPropose.accept(sel);
    }

    private void handleRemove() {
        CardBean sel = proposedList.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        
        if (sel.getQuantity() > 1) {
            sel.setQuantity(sel.getQuantity() - 1);
            proposedList.refresh();
        } else {
            proposedList.getItems().remove(sel);
        }

        
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
        
        String meetingPlace = storeComboBox != null ? storeComboBox.getValue() : null;
        String meetingDate = meetingDateField != null ? meetingDateField.getText() : null;
        String meetingDateTrimmed = meetingDate == null ? null : meetingDate.trim();

        boolean dateOk = validateMeetingDate(meetingDateTrimmed);

        if (!ensureMeetingPlace(meetingPlace)) return;
        if (!dateOk) {
            if (stage != null) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                a.setTitle("Errore data");
                a.setHeaderText("Inserisci una data valida successiva a oggi (YYYY-MM-DD)");
                a.showAndWait();
            }
            return;
        }

        ProposalBean bean = buildProposalBean(meetingPlace, meetingDateTrimmed);

        
        String meetingTime = meetingTimeField != null ? meetingTimeField.getText() : null;
        if (!processMeetingTime(bean, meetingTime)) return;
        if (onConfirm != null) onConfirm.accept(bean);
    }

    private boolean ensureMeetingPlace(String meetingPlace) {
        if (meetingPlace == null || meetingPlace.trim().isEmpty()) {
            if (onConfirm != null && stage != null) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                a.setTitle("Errore proposta");
                a.setHeaderText("Seleziona uno store per effettuare lo scambio");
                a.showAndWait();
            }
            return false;
        }
        return true;
    }

    private boolean processMeetingTime(ProposalBean bean, String meetingTime) {
        if (meetingTime == null) return true;
        meetingTime = meetingTime.trim();
        if (meetingTime.isEmpty()) return true;
        String parsed = validateAndParseTime(meetingTime);
        if (parsed == null) {
            if (stage != null) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                a.setTitle("Errore orario");
                a.setHeaderText("Inserisci un orario valido nel formato HH:mm");
                a.showAndWait();
            }
            return false;
        }
        bean.setMeetingTime(parsed);
        return true;
    }

    private ProposalBean buildProposalBean(String meetingPlace, String meetingDateTrimmed) {
        ProposalBean bean = new ProposalBean();
        
        bean.setOffered(new ArrayList<>(proposedList.getItems()));
        
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
        bean.setMeetingDate(meetingDateTrimmed);
        return bean;
    }

    private boolean validateMeetingDate(String meetingDateTrimmed) {
        if (meetingDateTrimmed == null || meetingDateTrimmed.isEmpty()) return false;
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(meetingDateTrimmed);
            return d.isAfter(java.time.LocalDate.now());
        } catch (Exception _) {
            LOGGER.fine(() -> "Invalid meeting date input in FXNegotiationView");
            return false;
        }
    }

    private String validateAndParseTime(String meetingTime) {
        try {
            java.time.LocalTime.parse(meetingTime);
            return meetingTime;
        } catch (Exception _) {
            return null;
        }
    }

    
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
                
                loadThumbnail(item);
                name.setText(item.getName() != null ? item.getName() : item.getId());
                qty.setText(" x" + Math.max(0, item.getQuantity()));
                if (showZeroWarning && item.getQuantity() <= 0) {
                    container.setStyle("-fx-background-color: rgba(255,80,80,0.12); -fx-padding:4; -fx-background-radius:4;");
                } else {
                    container.setStyle("");
                }
                setGraphic(container);
            }
        }

        private void loadThumbnail(CardBean item) {
            try {
                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    Image img = new Image(item.getImageUrl(), 40, 60, true, true, true);
                    if (!img.isError()) { thumb.setImage(img); return; }
                }
                java.io.InputStream is = getClass().getResourceAsStream(NOCARDIMAGE_ICONS_PATH);
                if (is != null) trySetImageFromStream(is);
            } catch (Exception _) {
                LOGGER.fine(() -> "Failed to load thumbnail for negotiation cell");
                java.io.InputStream is = getClass().getResourceAsStream(NOCARDIMAGE_ICONS_PATH);
                if (is != null) trySetImageFromStream(is);
            }
        }

        private void trySetImageFromStream(java.io.InputStream is) {
            try {
                thumb.setImage(new Image(is));
            } catch (Exception _) {
                LOGGER.fine(() -> "Fallback image failed");
             }
          }
      }
  }
