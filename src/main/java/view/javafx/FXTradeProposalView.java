package view.javafx;

import controller.TradeProposalController;
import exception.ViewException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import view.ITradeProposalView;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import model.bean.CardBean;
import model.bean.UserBean;

import javafx.scene.control.Dialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

/**
 * Vista JavaFX dedicata alla creazione di una nuova proposta di scambio.
 * Implementa un wizard interattivo che consente di selezionare le carte da
 * offrire
 * dalla propria collezione e di scegliere i dettagli dell'appuntamento (sede e
 * data).
 */
public class FXTradeProposalView implements ITradeProposalView {
    private static final String FIELD_LABEL_CLASS = "field-label";
    private static final String TEXT_FILL_WHITE_STYLE = "-fx-text-fill: white;";
    private TradeProposalController controller;
    private Stage stage;

    @FXML
    private BorderPane tradeProposalRoot;
    @FXML
    private Button closeBtn;
    @FXML
    private Button sendTradeRequestBtn;
    @FXML
    private FlowPane offerContainer;
    @FXML
    private VBox receiveContainer;
    @FXML
    private ImageView targetCardImage;
    @FXML
    private TilePane collectionGrid;
    @FXML
    private ScrollPane collectionScroll;

    @Override
    public void setController(Object controller) {
        this.controller = (TradeProposalController) controller;
    }

    /**
     * Inizializza e visualizza la pagina di creazione della proposta.
     * Configura il controller, carica il layout FXML e gestisce l'integrazione con
     * lo stage principale.
     */
    @Override
    public void display() {
        Runnable show = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TradeProposalPage.fxml"));
                loader.setController(this);
                Parent root = loader.load();

                // Setup UI
                setupUI();

                Scene scene = new Scene(root);
                java.net.URL cssUrl = getClass().getResource("/styles/trade-proposal.css");
                if (cssUrl != null)
                    scene.getStylesheets().add(cssUrl.toExternalForm());

                Optional<Window> existing = Window.getWindows().stream().filter(Window::isShowing).findFirst();
                if (existing.isPresent() && existing.get() instanceof Stage existingStage) {
                    existingStage.setScene(scene);
                    existingStage.setTitle("Propose Trade");
                    this.stage = existingStage;
                } else {
                    Stage st = new Stage();
                    st.setScene(scene);
                    st.setTitle("Propose Trade");
                    st.setWidth(1280);
                    st.setHeight(800);
                    st.centerOnScreen();
                    st.show();
                    this.stage = st;
                }
            } catch (IOException e) {
                System.err.println("Unable to load TradeProposalPage.fxml: " + e.getMessage());
                throw new ViewException("Failed to load Trade Proposal View", e);
            }
        };

        if (Platform.isFxApplicationThread())
            show.run();
        else
            Platform.runLater(show);
    }

    /**
     * Configura i componenti dell'interfaccia utente.
     * Imposta i gestori degli eventi per i pulsanti e inizializza la vista
     * con i dati correnti se il controller è disponibile.
     */
    private void setupUI() {
        if (closeBtn != null) {
            closeBtn.setOnAction(e -> {
                if (controller != null)
                    controller.goBack();
            });
        }

        if (sendTradeRequestBtn != null) {
            sendTradeRequestBtn.setOnAction(e -> handleSendTradeRequest());
        }

        if (controller != null) {
            CardBean target = controller.getTargetCard();
            if (target != null && targetCardImage != null && target.getImageUrl() != null) {
                try {
                    targetCardImage.setImage(new Image(target.getImageUrl(), true));
                } catch (Exception _) {
                    // Ignora il fallimento del caricamento dell'immagine
                }
            }

            // Caricamento iniziale dei dati
            refresh();
        }
    }

    @Override
    public void refresh() {
        if (controller != null) {
            controller.refresh();
        }
    }

    /**
     * Aggiorna la griglia delle carte disponibili nella collezione.
     * 
     * @param myCards La lista delle carte possedute dall'utente.
     */
    private void updateCollectionGrid(List<CardBean> myCards) {
        if (collectionGrid == null)
            return;
        collectionGrid.getChildren().clear();

        if (myCards.isEmpty()) {
            Label placeholder = new Label("Nessuna carta trovata nella tua collezione.");
            placeholder.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 14px;");
            collectionGrid.getChildren().add(placeholder);
            return;
        }

        // Inizializza le quantità originali se necessario (solitamente fatto una volta
        // ma innocuo se ripetuto)
        for (CardBean c : myCards) {
            controller.setOriginalQuantity(c.getId(), c.getQuantity());
        }

        for (CardBean card : myCards) {
            VBox cardItem = createCollectionCardItem(card);
            collectionGrid.getChildren().add(cardItem);
        }
    }

    /**
     * Crea un elemento grafico per una singola carta della collezione.
     * 
     * @param card Il bean della carta da visualizzare.
     * @return Un VBox contenente l'immagine e la quantità della carta.
     */
    private VBox createCollectionCardItem(CardBean card) {
        VBox item = new VBox(5);
        item.setAlignment(javafx.geometry.Pos.CENTER);
        item.setPrefSize(100, 140);
        item.getStyleClass().add("collection-card-item");
        item.setUserData(card.getId());

        // Immagine
        ImageView iv = new ImageView();
        iv.setFitWidth(80);
        iv.setFitHeight(112);
        iv.setPreserveRatio(true);
        if (card.getImageUrl() != null) {
            try {
                iv.setImage(new Image(card.getImageUrl(), true));
            } catch (Exception _) {
                // Ignora errore caricamento immagine
            }
        }
        item.getChildren().add(iv);

        // Utilizzo remainingQuantity pre-calcolato dal Controller
        int remaining = card.getRemainingQuantity();

        Label qtyLbl = new Label("x" + remaining);
        qtyLbl.setStyle(TEXT_FILL_WHITE_STYLE);
        item.getChildren().add(qtyLbl);

        if (remaining == 0) {
            item.setOpacity(0.5);
            item.setDisable(true);
        }

        item.setOnMouseClicked(e -> {
            if (remaining > 0) {
                controller.addOfferedCard(card.getId(), controller.getOfferedQuantity(card.getId()) + 1);
                refresh(); // Logica di re-rendering
            }
        });

        return item;
    }

    /**
     * Aggiorna il contenitore delle carte offerte.
     * Pulisce il contenitore e lo ripopola con le carte correntemente offerte.
     * 
     * @param offeredCards La lista delle carte offerte.
     */
    private void updateOfferContainer(List<CardBean> offeredCards) {
        if (offerContainer == null)
            return;
        offerContainer.getChildren().clear();

        // Aggiungi placeholder
        addOfferPlaceholder();

        // Aggiungi elementi offerti
        if (offeredCards != null) {
            for (CardBean card : offeredCards) {
                VBox offerItem = createOfferItem(card);
                offerContainer.getChildren().add(0, offerItem); // Prepend prima del placeholder
            }
        }
    }

    /**
     * Aggiunge un placeholder visivo per indicare dove aggiungere nuove carte.
     */
    private void addOfferPlaceholder() {
        StackPane placeholder = new StackPane();
        placeholder.setPrefSize(100, 140);
        placeholder.setStyle(
                "-fx-border-color: #3b3f46; -fx-border-style: dashed; -fx-border-radius: 8; -fx-border-width: 2;");

        Label lbl = new Label("+");
        lbl.setStyle("-fx-text-fill: #5b626e; -fx-font-size: 32px; -fx-font-weight: bold;");

        placeholder.getChildren().add(lbl);
        offerContainer.getChildren().add(placeholder);
    }

    /**
     * Crea un elemento grafico per una carta offerta.
     * 
     * @param card Il bean della carta offerta.
     * @return Un VBox contenente l'immagine e la quantità offerta.
     */
    private VBox createOfferItem(CardBean card) {
        String cId = card.getId();
        int qty = card.getQuantity();

        VBox item = new VBox(5);
        item.setAlignment(javafx.geometry.Pos.CENTER);
        item.getStyleClass().add("offered-card-item");
        item.setUserData(cId);

        if (card.getImageUrl() != null) {
            try {
                ImageView iv = new ImageView(new Image(card.getImageUrl(), true));
                iv.setFitWidth(80);
                iv.setFitHeight(112);
                iv.setPreserveRatio(true);
                item.getChildren().add(iv);
            } catch (Exception _) {
                // Ignora fallimento caricamento immagine
            }
        }

        Label lbl = new Label(card.getName() != null ? card.getName() : cId);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-wrap-text: true; -fx-text-alignment: center;");

        Label qLbl = new Label("x" + qty);
        qLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        item.getChildren().addAll(lbl, qLbl);
        item.setOnMouseClicked(e -> {
            // Qui dobbiamo ancora chiamare il controller per l'azione
            if (controller != null) {
                controller.removeOfferedCard(cId);
            }
        });
        return item;
    }

    /**
     * Gestisce l'azione di invio della proposta di scambio.
     * Delega la finalizzazione al controller.
     */
    private void handleSendTradeRequest() {
        if (controller != null) {
            controller.finalizeProposal();
        }
    }

    @Override
    public void showAvailableItems(List<CardBean> cards) {
        // Aggiorna la griglia delle carte disponibili
        Runnable r = () -> updateCollectionGrid(cards);
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    @Override
    public void showOfferedItems(List<CardBean> cards) {
        Runnable r = () -> updateOfferContainer(cards);
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    @Override
    public void showTargetItem(CardBean card) {
        // Aggiorna l'immagine della carta target
        Runnable r = () -> {
            if (targetCardImage != null && card != null && card.getImageUrl() != null) {
                try {
                    targetCardImage.setImage(new Image(card.getImageUrl(), true));
                } catch (Exception _) {
                    // Ignora fallimento caricamento immagine
                }
            }
        };
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    /**
     * Mostra una finestra di dialogo per la selezione dei dettagli dell'incontro.
     * Consente all'utente di scegliere lo store ospitante, la data e l'orario del
     * trade.
     * 
     * @param stores    Lista dei negozi disponibili per l'incontro.
     * @param onConfirm Azione da eseguire alla conferma dei dettagli (passa lo
     *                  store e il timestamp).
     */
    @Override
    public void showMeetingDialog(List<UserBean> stores, BiConsumer<UserBean, LocalDateTime> onConfirm) {
        Runnable r = () -> {
            Dialog<Void> dialog = createBaseDialog();
            ButtonType startButtonType = new ButtonType("Confirm Proposal", ButtonData.OK_DONE);
            ButtonType backButtonType = new ButtonType("Back", ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(backButtonType, startButtonType);

            styleDialogButtons(dialog, startButtonType, backButtonType);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(15);
            grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

            // Controls
            ComboBox<UserBean> storeCombo = createStoreComboBox(stores);
            DatePicker datePicker = createDatePicker();
            ComboBox<LocalTime> timeCombo = createTimeComboBox();

            addControlsToGrid(grid, storeCombo, datePicker, timeCombo);
            dialog.getDialogPane().setContent(grid);

            javafx.scene.Node confirmBtnNode = dialog.getDialogPane().lookupButton(startButtonType);
            setupValidation(confirmBtnNode, storeCombo, datePicker, timeCombo);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == startButtonType) {
                    processDialogResult(storeCombo, datePicker, timeCombo, onConfirm);
                }
                return null;
            });

            dialog.showAndWait();
        };

        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    // ... Helper Dialog ...

    /**
     * Crea e configura il dialog di base.
     * 
     * @return Una nuova istanza di Dialog configurata.
     */
    private Dialog<Void> createBaseDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Select Meeting Details");
        dialog.setHeaderText("Choose a store and time for the trade meeting");
        java.net.URL cssUrl = getClass().getResource("/styles/style.css");
        if (cssUrl != null) {
            dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }
        return dialog;
    }

    /**
     * Applica gli stili CSS ai pulsanti del dialog.
     * 
     * @param dialog   Il dialog contenente i pulsanti.
     * @param startBtn Il tipo di pulsante di conferma.
     * @param backBtn  Il tipo di pulsante annulla/indietro.
     */
    private void styleDialogButtons(Dialog<Void> dialog, ButtonType startBtn, ButtonType backBtn) {
        javafx.scene.Node confirmBtnNode = dialog.getDialogPane().lookupButton(startBtn);
        if (confirmBtnNode != null)
            confirmBtnNode.getStyleClass().add("dialog-btn-confirm");
        javafx.scene.Node backBtnNode = dialog.getDialogPane().lookupButton(backBtn);
        if (backBtnNode != null)
            backBtnNode.getStyleClass().add("dialog-btn-back");
    }

    /**
     * Crea un ComboBox per la selezione dello store.
     * 
     * @param stores Lista degli store disponibili.
     * @return ComboBox configurato.
     */
    private ComboBox<UserBean> createStoreComboBox(List<UserBean> stores) {
        ComboBox<UserBean> storeCombo = new ComboBox<>();
        storeCombo.getItems().addAll(stores);
        storeCombo.setPromptText("Select Store");
        storeCombo.setPrefWidth(250);
        storeCombo.setConverter(new StringConverter<UserBean>() {
            @Override
            public String toString(UserBean user) {
                return user != null ? user.getUsername() : "";
            }

            @Override
            public UserBean fromString(String string) {
                return null;
            }
        });
        return storeCombo;
    }

    /**
     * Crea un DatePicker per la selezione della data.
     * Configura la day cell factory per disabilitare date passate.
     * 
     * @return DatePicker configurato.
     */
    private DatePicker createDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("dd/MM/yyyy");
        datePicker.setPrefWidth(250);
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && !date.isAfter(java.time.LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-opacity: 0.5;");
                }
            }
        });
        return datePicker;
    }

    /**
     * Crea un ComboBox per la selezione dell'orario.
     * Popola gli orari dalle 09:00 alle 20:00 con intervalli di 30 minuti.
     * 
     * @return ComboBox configurato con gli orari.
     */
    private ComboBox<LocalTime> createTimeComboBox() {
        ComboBox<LocalTime> timeCombo = new ComboBox<>();
        for (int h = 9; h < 20; h++) {
            timeCombo.getItems().add(LocalTime.of(h, 0));
            timeCombo.getItems().add(LocalTime.of(h, 30));
        }
        timeCombo.setPromptText("Time");
        timeCombo.setPrefWidth(250);
        return timeCombo;
    }

    /**
     * Aggiunge i controlli al layout a griglia.
     * 
     * @param grid  Il pannello GridPane.
     * @param store Il nodo per la selezione dello store.
     * @param date  Il nodo per la selezione della data.
     * @param time  Il nodo per la selezione dell'orario.
     */
    private void addControlsToGrid(GridPane grid, javafx.scene.Node store, javafx.scene.Node date,
            javafx.scene.Node time) {
        Label storeLbl = new Label("Store:");
        storeLbl.getStyleClass().add(FIELD_LABEL_CLASS);
        Label dateLbl = new Label("Date:");
        dateLbl.getStyleClass().add(FIELD_LABEL_CLASS);
        Label timeLbl = new Label("Time:");
        timeLbl.getStyleClass().add(FIELD_LABEL_CLASS);

        grid.add(storeLbl, 0, 0);
        grid.add(store, 1, 0);
        grid.add(dateLbl, 0, 1);
        grid.add(date, 1, 1);
        grid.add(timeLbl, 0, 2);
        grid.add(time, 1, 2);
    }

    /**
     * Configura la logica di validazione per abilitare il pulsante di conferma.
     * Il pulsante viene abilitato solo quando tutti i campi sono valorizzati.
     * 
     * @param confirmBtn Il pulsante di conferma.
     * @param store      Il ComboBox dello store.
     * @param date       Il DatePicker.
     * @param time       Il ComboBox dell'orario.
     */
    private void setupValidation(javafx.scene.Node confirmBtn, ComboBox<UserBean> store, DatePicker date,
            ComboBox<LocalTime> time) {
        javafx.beans.value.ChangeListener<Object> validator = (obs, oldVal, newVal) -> {
            // DatePicker già impedisce selezione date passate tramite setDayCellFactory
            boolean valid = store.getValue() != null
                    && date.getValue() != null
                    && time.getValue() != null;
            if (confirmBtn != null)
                confirmBtn.setDisable(!valid);
        };
        store.valueProperty().addListener(validator);
        date.valueProperty().addListener(validator);
        time.valueProperty().addListener(validator);
        validator.changed(null, null, null);
    }

    /**
     * Elabora il risultato del dialog e invoca la callback di conferma.
     * 
     * @param store     ComboBox dello store.
     * @param date      DatePicker.
     * @param time      ComboBox dell'orario.
     * @param onConfirm Callback da invocare con i dati selezionati.
     */
    private void processDialogResult(ComboBox<UserBean> store, DatePicker date, ComboBox<LocalTime> time,
            BiConsumer<UserBean, LocalDateTime> onConfirm) {
        UserBean selectedStore = store.getValue();
        java.time.LocalDate d = date.getValue();
        LocalTime t = time.getValue();
        if (selectedStore != null && d != null && t != null) {
            LocalDateTime dt = LocalDateTime.of(d, t);
            onConfirm.accept(selectedStore, dt);
        }
    }

    @Override
    public void showError(String errorMessage) {
        Runnable r = () -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    @Override
    public void close() {
        Runnable r = () -> {
            if (stage != null) {
                stage.hide();
            }
        };
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    @Override
    public void showSuccessMessage(String message) {
        Runnable r = () -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Applica CSS se disponibile
            java.net.URL cssUrl = getClass().getResource("/styles/style.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                alert.getDialogPane().getStyleClass().add("dialog-pane");
            }

            alert.showAndWait();
        };

        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            // Blocca finché l'alert non è chiuso
            Platform.runLater(r);
        }
    }
}
