package view.javafx;

import controller.StoreHPController;
import exception.ViewException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.bean.TradeSessionBean;

import view.IStoreHPView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.List;

/**
 * Vista JavaFX della Home Page specifica per gli utenti di tipo Store.
 * Funge da dashboard principale per il negozio, visualizzando la lista degli
 * scambi
 * in corso, programmati e storici, permettendo la navigazione verso i dettagli
 * di gestione.
 */
public class FXStoreHPView implements IStoreHPView {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(FXStoreHPView.class.getName());
    private StoreHPController controller;
    private Stage stage;

    @FXML
    private Label welcomeLabel;
    @FXML
    private ListView<TradeSessionBean> ongoingList;
    @FXML
    private ListView<TradeSessionBean> scheduledList;
    @FXML
    private ListView<TradeSessionBean> historyList;

    @FXML
    private javafx.scene.control.Button logoutBtn;

    @Override
    public void display() {
        if (Platform.isFxApplicationThread()) {
            showWindow();
        } else {
            Platform.runLater(this::showWindow);
        }
    }

    /**
     * Inizializza e visualizza la finestra del dashboard dello store.
     * Configura la scena JavaFX, carica i fogli di stile CSS e richiede i dati al
     * controller.
     */
    private void showWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreHomepage.fxml"));
            loader.setController(this);
            VBox root = loader.load();

            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/store.css").toExternalForm());

            stage = new Stage();
            stage.setTitle("Cardify - Store Dashboard");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            setupListFactories();

            if (logoutBtn != null) {
                logoutBtn.setOnAction(e -> {
                    if (controller != null)
                        controller.logout();
                });
            }

            if (controller != null) {
                controller.loadStoreData();
            }

        } catch (IOException e) {
            throw new ViewException("Impossibile caricare Store Dashboard View", e);
        }
    }

    private static final String DATE_PLACEHOLDER = "Da definire";
    private static final String CELL_FORMAT = "ID: %d | %s <-> %s%nData: %s";
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm");

    private void setupListFactories() {
        if (ongoingList != null)
            ongoingList.setCellFactory(createOngoingCellFactory());
        if (scheduledList != null)
            scheduledList.setCellFactory(createScheduledCellFactory());
        if (historyList != null)
            historyList.setCellFactory(createHistoryCellFactory());
    }

    /**
     * Crea una factory di celle per la lista dello storico scambi.
     * Mostra ID, partecipanti e data.
     *
     * @return La Callback per la creazione delle celle.
     */
    private javafx.util.Callback<ListView<TradeSessionBean>, ListCell<TradeSessionBean>> createHistoryCellFactory() {
        return param -> new ListCell<>() {
            @Override
            protected void updateItem(TradeSessionBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String dateStr = item.getTradeDate() != null ? item.getTradeDate().format(DATE_FORMATTER)
                            : DATE_PLACEHOLDER;
                    String text = String.format(CELL_FORMAT,
                            item.getTransactionId(), item.getProposerId(), item.getReceiverId(), dateStr);
                    setText(text);
                    setGraphic(null);
                }
            }
        };
    }

    /**
     * Crea una fabbrica di celle per la lista degli scambi in corso.
     * Ogni cella include informazioni sintetiche sullo scambio e un pulsante per
     * accedere alla gestione dettagliata.
     * 
     * @return Una callback per personalizzare il rendering degli elementi nella
     *         ListView.
     */
    private javafx.util.Callback<ListView<TradeSessionBean>, ListCell<TradeSessionBean>> createOngoingCellFactory() {
        return param -> new ListCell<>() {
            @Override
            protected void updateItem(TradeSessionBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    String dateStr = item.getTradeDate() != null ? item.getTradeDate().format(DATE_FORMATTER)
                            : DATE_PLACEHOLDER;
                    String infoText = String.format(CELL_FORMAT,
                            item.getTransactionId(), item.getProposerId(), item.getReceiverId(), dateStr);
                    Label infoLabel = new Label(infoText);
                    infoLabel.setStyle("-fx-text-fill: #e0e0e0;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button manageBtn = new Button("Gestisci scambio");
                    manageBtn.getStyleClass().add("manage-btn");
                    manageBtn.setOnAction(e -> {
                        if (controller != null) {
                            controller.openTradeDetails(item);
                        }
                    });

                    container.getChildren().addAll(infoLabel, spacer, manageBtn);
                    setGraphic(container);
                    setText(null);
                }
            }
        };
    }

    /**
     * Crea una factory di celle per la lista degli scambi programmati.
     * Simile a quella degli scambi in corso, mostra dettagli e pulsante.
     *
     * @return La Callback per la creazione delle celle.
     */
    private javafx.util.Callback<ListView<TradeSessionBean>, ListCell<TradeSessionBean>> createScheduledCellFactory() {
        return param -> new ListCell<>() {
            @Override
            protected void updateItem(TradeSessionBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    String dateStr = item.getTradeDate() != null ? item.getTradeDate().format(DATE_FORMATTER)
                            : DATE_PLACEHOLDER;
                    String infoText = String.format(CELL_FORMAT,
                            item.getTransactionId(), item.getProposerId(), item.getReceiverId(), dateStr);
                    Label infoLabel = new Label(infoText);
                    infoLabel.setStyle("-fx-text-fill: #e0e0e0;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button detailsBtn = new Button("Vedi Dettagli");
                    detailsBtn.getStyleClass().add("details-btn");
                    detailsBtn.setOnAction(e -> {
                        if (controller != null) {
                            controller.openTradeDetails(item);
                        }
                    });

                    container.getChildren().addAll(infoLabel, spacer, detailsBtn);
                    setGraphic(container);
                    setText(null);
                }
            }
        };
    }

    @Override
    public void setStoreName(String name) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Benvenuto " + name);
        }
    }

    /**
     * Aggiorna la lista degli scambi in corso nella dashboard.
     *
     * @param trades Lista dei bean degli scambi in corso.
     */
    @Override
    public void showOngoingTrades(List<TradeSessionBean> trades) {
        if (ongoingList != null) {
            ongoingList.getItems().setAll(trades);
        }
    }

    /**
     * Aggiorna la lista degli scambi programmati nella dashboard.
     *
     * @param trades Lista dei bean degli scambi programmati.
     */
    @Override
    public void showScheduledTrades(List<TradeSessionBean> trades) {
        if (scheduledList != null) {
            scheduledList.getItems().setAll(trades);
        }
    }

    /**
     * Aggiorna la lista dello storico scambi nella dashboard.
     *
     * @param trades Lista dei bean degli scambi storici.
     */
    @Override
    public void showHistoryTrades(List<TradeSessionBean> trades) {
        if (historyList != null) {
            historyList.getItems().setAll(trades);
        }
    }

    @Override
    public void close() {
        if (stage != null)
            stage.close();
    }

    @Override
    public void refresh() {
        if (controller != null)
            controller.loadStoreData();
    }

    @Override
    public void showError(String errorMessage) {
        LOGGER.log(java.util.logging.Level.SEVERE, errorMessage);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (StoreHPController) controller;
    }
}
