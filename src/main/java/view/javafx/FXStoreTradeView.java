package view.javafx;

import controller.TradeController;
import exception.ViewException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.bean.TradeSessionBean;
import model.bean.CardBean;
import view.IStoreTradeView;

/**
 * Vista JavaFX per la gestione operativa di uno scambio da parte dello Store.
 * Fornisce gli strumenti per verificare l'arrivo dei partecipanti, validare i
 * codici sessione
 * e condurre la fase di ispezione fisica delle carte offerte e richieste.
 */
public class FXStoreTradeView implements IStoreTradeView {
    private TradeController controller;
    private TradeSessionBean currentSession;

    private javafx.stage.Stage stage;

    /**
     * Associa il controller di business alla vista.
     * Funzionalità: Imposta il riferimento al TradeController per la gestione delle
     * azioni.
     * Utility: Implementa il pattern MVC permettendo il distacco tra UI e logica.
     * 
     * @param controller Il controller da associare (cast a TradeController).
     */
    @Override
    public void setController(Object controller) {
        this.controller = (TradeController) controller;
    }

    /**
     * Gestisce la logica di verifica del codice sessione inserito dall'utente.
     * Funzionalità: Valida l'input testuale e inoltra la richiesta di verifica al
     * controller.
     * Utility: Permette allo store di confermare l'identità dei partecipanti allo
     * scambio.
     */
    private void handleVerifyCode() {
        if (sessionCodeField.getText().isEmpty()) {
            showError("Inserire un codice per procedere.");
            return;
        }
        try {
            int code = Integer.parseInt(sessionCodeField.getText());
            if (currentSession != null) {
                controller.verifySessionCode(code, currentSession.getTransactionId());
            }
        } catch (NumberFormatException _) {
            showError("Formato codice non valido.");
        }
    }

    /**
     * Avvia la fase di ispezione fisica delle carte.
     * Funzionalità: Notifica al controller l'inizio della fase di controllo
     * qualità.
     * Utility: Bridge tra azione UI e cambiamento di stato nel database.
     */
    private void handleStartInspection() {
        if (currentSession != null)
            controller.startInspection(currentSession.getTransactionId());
    }

    /**
     * Conferma il superamento con successo dell'ispezione.
     * Funzionalità: Segnala al controller che le carte sono conformi e lo scambio
     * può proseguire.
     */
    private void handlePassInspection() {
        if (currentSession != null)
            controller.passInspection(currentSession.getTransactionId());
    }

    /**
     * Gestisce il fallimento dell'ispezione fisica.
     * Funzionalità: Segnala al controller che le carte non rispettano i criteri,
     * portando all'annullamento.
     */
    private void handleFailInspection() {
        if (currentSession != null)
            controller.failInspection(currentSession.getTransactionId());
    }

    /**
     * Richiede l'annullamento forzato dello scambio corrente.
     * Funzionalità: Inoltra la richiesta di cancellazione al controller.
     */
    private void handleCancelTrade() {
        if (currentSession != null)
            controller.cancelTrade(currentSession.getTransactionId());
    }

    /**
     * Aggiorna la vista con i dettagli di una sessione di scambio specifica.
     * Popola le liste delle carte e attiva gli elementi di controllo in base allo
     * stato attuale dello scambio.
     * 
     * @param sessionBean Il bean contenente i dati della sessione.
     * @param userCode    Parametro non utilizzato in questa vista (ereditato
     *                    dall'interfaccia).
     * @param partnerName Parametro non utilizzato in questa vista (ereditato
     *                    dall'interfaccia).
     */
    @Override
    public void showTradeDetails(TradeSessionBean sessionBean, String userCode, String partnerName) {
        this.currentSession = sessionBean;
        refresh();
    }

    /**
     * Sincronizza lo stato dei componenti grafici con i dati della sessione.
     * Funzionalità: Aggiorna label di stato, pulsanti e liste delle carte.
     * Utility: Punto centrale di aggiornamento della UI per mantenere la coerenza
     * dei dati visualizzati.
     * Delega gli aggiornamenti specifici ai metodi helper updateButtonsState(),
     * updateVerificationStatus() e updateCardLists().
     */
    @Override
    public void refresh() {
        if (currentSession == null || tradeStatusLabel == null) {
            return; // UI non pronta o sessione mancante
        }

        tradeStatusLabel.setText("Stato: " + currentSession.getStatus());

        updateButtonsState(currentSession.getStatus());
        updateVerificationStatus(currentSession);

        // Mostra sempre i dettagli nell'area dedicata
        if (detailsArea != null)
            detailsArea.setVisible(true);
        updateCardLists(currentSession);
    }

    /**
     * Aggiorna lo stato di arrivo dei partecipanti nella UI.
     * Funzionalità: Modifica il testo e il colore delle label in base allo stato di
     * arrivo memorizzato nel bean.
     * Utility: Permette all'operatore di sapere a colpo d'occhio chi è presente
     * allo store.
     * 
     * @param session Il bean della sessione di scambio da cui leggere lo stato.
     */
    private void updateVerificationStatus(TradeSessionBean session) {
        if (proposerStatusLabel != null) {
            String proposerName = session.getProposerId();
            proposerStatusLabel
                    .setText(proposerName + ": " + (session.isProposerArrived() ? "Arrivato" : "Non Arrivato"));
            proposerStatusLabel
                    .setStyle(session.isProposerArrived() ? "-fx-text-fill: #28a745;" : "-fx-text-fill: #ff6b6b;");
        }
        if (receiverStatusLabel != null) {
            String receiverName = session.getReceiverId();
            receiverStatusLabel
                    .setText(receiverName + ": " + (session.isReceiverArrived() ? "Arrivato" : "Non Arrivato"));
            receiverStatusLabel
                    .setStyle(session.isReceiverArrived() ? "-fx-text-fill: #28a745;" : "-fx-text-fill: #ff6b6b;");
        }
    }

    /**
     * Regola l'abilitazione dei pulsanti in base al workflow dello scambio.
     * Funzionalità: Disabilita o abilita i pulsanti d'azione (verifica, ispezione,
     * annullamento)
     * seguendo lo stato corrente della transazione.
     * Utility: Garantisce che l'operatore segua la sequenza corretta di operazioni,
     * prevenendo errori.
     * 
     * @param status La stringa che rappresenta lo stato attuale dello scambio.
     */
    private void updateButtonsState(String status) {
        if (verifyCodeButton == null)
            return;

        boolean isWaiting = "WAITING_FOR_ARRIVAL".equals(status) || "PARTIALLY_ARRIVED".equals(status);
        boolean bothArrived = "BOTH_ARRIVED".equals(status);
        boolean inspectionPhase = "INSPECTION_PHASE".equals(status);

        verifyCodeButton.setDisable(!isWaiting);
        sessionCodeField.setDisable(!isWaiting);

        if (startInspectionButton != null)
            startInspectionButton.setDisable(!bothArrived);

        if (passInspectionButton != null)
            passInspectionButton.setDisable(!inspectionPhase);
        if (failInspectionButton != null)
            failInspectionButton.setDisable(!inspectionPhase);

        if (cancelTradeButton != null) {
            boolean canCancel = !"COMPLETED".equals(status) && !"CANCELLED".equals(status)
                    && !"EXPIRED".equals(status);
            cancelTradeButton.setDisable(!canCancel);
        }
    }

    /**
     * Popola graficamente i pannelli delle carte offerte e richieste.
     * Funzionalità: Svuota i container correnti e li riempie con nuovi nodi per
     * ogni carta.
     * Utility: Mostra visivamente l'oggetto dello scambio allo store.
     * Delega la generazione del nodo grafico della carta al metodo helper
     * createCardNode().
     * 
     * @param session Il bean della sessione contenente le liste delle carte.
     */
    private void updateCardLists(TradeSessionBean session) {
        if (offeredCardsPane != null) {
            offeredCardsPane.getChildren().clear();
            session.getOffered().forEach(c -> offeredCardsPane.getChildren().add(createCardNode(c)));
        }
        if (requestedCardsPane != null) {
            requestedCardsPane.getChildren().clear();
            session.getRequested().forEach(c -> requestedCardsPane.getChildren().add(createCardNode(c)));
        }
    }

    /**
     * Crea un nodo grafico (VBox) per visualizzare una carta.
     * Funzionalità: Costruisce un componente con immagine e nome della carta.
     * Utility: Garantisce uniformità visiva nelle liste delle carte.
     * 
     * @param card Il bean della carta da visualizzare.
     * @return Un oggetto Node configurato per la UI.
     */
    private Node createCardNode(CardBean card) {
        VBox cardBox = new VBox(5);
        cardBox.setAlignment(javafx.geometry.Pos.CENTER);
        cardBox.setPrefWidth(120);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);

        String imageUrl = card.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                if (!imageUrl.endsWith(".webp") && !imageUrl.endsWith(".png") && !imageUrl.endsWith(".jpg")) {
                    imageUrl += "/high.webp";
                }
                imageView.setImage(new Image(imageUrl, true));
            } catch (Exception _) {
                // Errore caricamento immagine ignorato
            }
        }

        Label nameLabel = new Label(card.getName());
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-text-alignment: center;");

        cardBox.getChildren().addAll(imageView, nameLabel);
        return cardBox;
    }

    /**
     * Chiude la finestra operativa dello store.
     * Funzionalità: Invoca la chiusura dello stage in modo thread-safe.
     * Utility: Permette di terminare l'interazione con l'interfaccia corrente.
     */
    @Override
    public void close() {
        if (stage != null) {
            Platform.runLater(stage::close);
        }
    }

    /**
     * Visualizza un messaggio di errore all'utente.
     * Funzionalità: Mostra una finestra di dialogo Alert di tipo ERROR.
     * Utility: Fornisce feedback immediato su operazioni fallite o input errati.
     * 
     * @param errorMessage Il testo dell'errore da visualizzare.
     */
    @Override
    public void showError(String errorMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setContentText(errorMessage);
        alert.show();
    }

    /**
     * Notifica visivamente la validazione corretta di un codice cliente.
     * Funzionalità: Mostra un Alert informativo con il codice validato.
     * 
     * @param code Il codice numerico che è stato verificato.
     */
    @Override
    public void registerCodeValidation(int code) {
        Runnable r = () -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Codice Verificato");
            alert.setHeaderText(null);
            alert.setContentText("Il codice cliente " + code + " è valido!");
            alert.showAndWait();
        };
        runInPlatform(r);
    }

    /**
     * Notifica visivamente il successo dell'ispezione delle carte.
     * Funzionalità: Mostra un Alert informativo che conferma la validità degli
     * oggetti di scambio.
     */
    @Override
    public void registerInspectionSuccess() {
        Runnable r = () -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Ispezione Superata");
            alert.setHeaderText(null);
            alert.setContentText("L'ispezione è stata completata con successo.\nLo scambio verrà finalizzato.");
            alert.showAndWait();
        };
        runInPlatform(r);
    }

    /**
     * Notifica visivamente il fallimento dell'ispezione delle carte.
     * Funzionalità: Mostra un Alert di avvertimento che segnala l'annullamento
     * dello scambio.
     */
    @Override
    public void registerInspectionFail() {
        Runnable r = () -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Ispezione Fallita");
            alert.setHeaderText(null);
            alert.setContentText("L'ispezione è fallita. Lo scambio verrà annullato.");
            alert.showAndWait();
        };
        runInPlatform(r);
    }

    @Override
    public void onFinalizeTrade() {
        Runnable r = () -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Scambio Completato");
            alert.setHeaderText(null);
            alert.setContentText("Lo scambio è stato finalizzato con successo!\nGli inventari sono stati aggiornati.");
            alert.showAndWait();
            // Dopo l'OK, il controller potrebbe già aver navigato o resetterà la vista
        };
        runInPlatform(r);
    }

    //TUTTO IL CODICE FXML/////////////////////////

    @FXML
    private Label tradeStatusLabel;
    @FXML
    private TextField sessionCodeField;
    @FXML
    private Button verifyCodeButton;
    @FXML
    private Button startInspectionButton;
    @FXML
    private Button passInspectionButton;
    @FXML
    private Button failInspectionButton;
    @FXML
    private Button backButton;
    @FXML
    private Button cancelTradeButton;
    @FXML
    private Label proposerStatusLabel;
    @FXML
    private Label receiverStatusLabel;
    @FXML
    private javafx.scene.control.Button logoutBtn;
    @FXML
    private javafx.scene.layout.VBox detailsArea;

    @FXML
    private javafx.scene.layout.TilePane offeredCardsPane;
    @FXML
    private javafx.scene.layout.TilePane requestedCardsPane;

    /**
     * Gestisce la visualizzazione della scena operativa.
     * Funzionalità: Avvia il caricamento dello stage JavaFX assicurandosi di essere
     * sul thread corretto.
     * Utility: Entry point per l'attivazione della vista dello store.
     * Delega la configurazione effettiva dello stage al metodo helper showWindow().
     */
    @Override
    public void display() {
        if (Platform.isFxApplicationThread()) {
            showWindow();
        } else {
            Platform.runLater(this::showWindow);
        }
    }

    /**
     * Carica il layout FXML e configura lo Stage.
     * Funzionalità: Inizializza FXMLLoader, carica la risorsa e imposta la scena.
     * Utility: Gestisce la creazione fisica della finestra grafica.
     *
     * @throws ViewException In caso di errori durante il caricamento del file FXML.
     */
    private void showWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/StoreTradePage.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            this.stage = new Stage();
            stage.setTitle("Cardify - Gestione Scambio Store");
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(800);
            stage.show();
        } catch (java.io.IOException e) {
            throw new ViewException("Errore nel caricamento della vista Store Trade", e);
        }
    }

    /**
     * Inizializza il controller della vista JavaFX.
     * Funzionalità: Configura gli event handler per i componenti della UI e avvia
     * il primo refresh.
     * Utility: Assicura che l'interfaccia sia reattiva e popolate correttamente al
     * momento del caricamento.
     * Delega la gestione delle azioni ai metodi helper handleVerifyCode(),
     * handleStartInspection(), ecc.,
     * e l'aggiornamento grafico al metodo refresh().
     */
    @FXML
    public void initialize() {
        if (verifyCodeButton != null)
            verifyCodeButton.setOnAction(e -> handleVerifyCode());
        if (startInspectionButton != null)
            startInspectionButton.setOnAction(e -> handleStartInspection());
        if (passInspectionButton != null)
            passInspectionButton.setOnAction(e -> handlePassInspection());
        if (failInspectionButton != null)
            failInspectionButton.setOnAction(e -> handleFailInspection());
        if (backButton != null)
            backButton.setOnAction(e -> controller.goToStoreHomepage());
        if (cancelTradeButton != null)
            cancelTradeButton.setOnAction(e -> handleCancelTrade());

        refresh();
    }

    /**
     * Metodo di utility per eseguire codice sul thread JavaFX Application.
     * Funzionalità: Incapsula la logica di Platform.runLater().
     * Utility: Previene eccezioni di threading durante l'aggiornamento della UI da
     * sotto-thread.
     * 
     * @param r La task Runnable da eseguire.
     */
    private void runInPlatform(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
