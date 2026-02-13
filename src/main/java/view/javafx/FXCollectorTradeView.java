package view.javafx;

import controller.TradeController;
import exception.ViewException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import model.bean.TradeSessionBean;
import view.ICollectorTradeView;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Vista JavaFX per il monitoraggio e la gestione degli scambi lato
 * Collezionista.
 * Questa classe gestisce l'interfaccia grafica per visualizzare le liste degli
 * scambi
 * e i dettagli delle singole sessioni, permettendo all'utente di interagire con
 * il sistema
 * in modo intuitivo. È fondamentale per garantire un'esperienza utente fluida
 * durante
 * la fase di scambio fisico presso lo store.
 */
public class FXCollectorTradeView implements ICollectorTradeView {
    private TradeController controller;
    private Stage stage;
    private TradeSessionBean currentSession;

    private static final String EMPTY_CODE_PLACEHOLDER = "------";


    @FXML
    private Button homeBtn;
    @FXML
    private Button collectionBtn;
    @FXML
    private Button tradeBtn;
    @FXML
    private Button proposalBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private ImageView homeIcon;
    @FXML
    private ImageView collectionIcon;
    @FXML
    private ImageView tradeIcon;
    @FXML
    private ImageView proposalIcon;
    @FXML
    private ImageView logoutIcon;
    @FXML
    private VBox profileBox;
    @FXML
    private ImageView avatarImage;
    @FXML
    private Label usernameLabel;
    @FXML
    private VBox contentArea;
    @FXML
    private VBox activeTradesContainer;
    @FXML
    private VBox completedTradesContainer;

    /**
     * Inizializza e visualizza la pagina principale degli scambi del collezionista.
     * Funzionalità: carica il file FXML della lista scambi, configura i componenti
     * e imposta la scena.
     * Utility: funge da punto di ingresso principale per la navigazione degli
     * scambi in JavaFX.
     * Delega l'esecuzione sul thread JavaFX a runInPlatform(), la configurazione
     * delle icone a setIcons(),
     * il binding dei pulsanti a wireNavbarButtons(), l' caricamento dell'avatar a
     * loadAvatar()
     * e la configurazione dello stage a setupStage().
     */
    @Override
    public void display() {
        Runnable show = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectorTradeListPage.fxml"));
                loader.setController(this);
                VBox root = loader.load();

                setIcons();
                wireNavbarButtons();
                loadAvatar();

                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/trade.css").toExternalForm());

                URL loginCss = getClass().getResource("/styles/login.css");
                if (loginCss != null)
                    scene.getStylesheets().add(loginCss.toExternalForm());

                setupStage(scene);

                if (controller != null) {
                    controller.loadTrades();
                }

            } catch (IOException e) {
                throw new ViewException("Failed to load Collector Trade View", e);
            }
        };

        runInPlatform(show);
    }

    private void runInPlatform(Runnable r) {
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    private void loadAvatar() {
        if (controller != null && controller.getSessionUser() != null) {
            usernameLabel.setText(controller.getSessionUser().getUsername());
            try {
                avatarImage.setImage(new Image(getClass().getResourceAsStream("/icons/collectorpp.png")));
            } catch (Exception _) {
                // Ignora errore caricamento avatar
            }
        }
    }

    private void setupStage(Scene scene) {
        Optional<Window> existing = Window.getWindows().stream().filter(Window::isShowing).findFirst();
        if (existing.isPresent() && existing.get() instanceof Stage existingStage) {
            existingStage.setScene(scene);
            existingStage.setTitle("Scambi");
            this.stage = existingStage;
        } else {
            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle("Scambi");
            st.setWidth(1280);
            st.setHeight(800);
            st.centerOnScreen();
            st.show();
            this.stage = st;
        }
    }

    private void setIcons() {
        loadImage(homeIcon, "/icons/homepage.png");
        loadImage(collectionIcon, "/icons/collection.png");
        loadImage(tradeIcon, "/icons/trade.png");
        loadImage(proposalIcon, "/icons/manageproposals.png");
        loadImage(logoutIcon, "/icons/logout.png");
    }

    private void loadImage(ImageView view, String path) {
        try {
            view.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception _) {
            // Ignora icona mancante
        }
    }

    private void wireNavbarButtons() {
        if (homeBtn != null)
            homeBtn.setOnAction(e -> controller.goToCollectorHomepage());
        if (collectionBtn != null)
            collectionBtn.setOnAction(e -> controller.goToCollection());
        if (tradeBtn != null) {
            tradeBtn.getStyleClass().add("active-nav");
        }
        if (proposalBtn != null)
            proposalBtn.setOnAction(e -> controller.manageProposals());
        if (logoutBtn != null)
            logoutBtn.setOnAction(e -> controller.logout());
    }

    /**
     * Aggiorna le liste degli scambi attivi e completati nella UI.
     * Funzionalità: invoca l'aggiornamento dei container VBox con i nuovi dati dei
     * bean.
     * Assicura che l'utente visualizzi sempre lo stato più recente degli
     * scambi.
     * Delega l'esecuzione sul thread JavaFX a runInPlatform() e l'aggiornamento dei
     * singoli container a updateTradeList().
     * 
     * @param activeTrades    Elenco degli scambi in corso.
     * @param completedTrades Elenco degli scambi terminati.
     */
    @Override
    public void showTradeLists(List<model.bean.TradeSessionBean> activeTrades,
            List<model.bean.TradeSessionBean> completedTrades) {
        Runnable r = () -> {
            if (activeTradesContainer == null || completedTradesContainer == null)
                return;

            updateTradeList(activeTradesContainer, activeTrades, true, "Nessuno scambio attivo.");
            updateTradeList(completedTradesContainer, completedTrades, false, "Nessuno scambio concluso.");
        };
        runInPlatform(r);
    }

    private void updateTradeList(VBox container, List<model.bean.TradeSessionBean> trades, boolean isActive,
            String emptyMsg) {
        container.getChildren().clear();
        if (trades != null && !trades.isEmpty()) {
            for (model.bean.TradeSessionBean trade : trades) {
                container.getChildren().add(createTradeItem(trade, isActive));
            }
        } else {
            Label placeholder = new Label(emptyMsg);
            placeholder.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
            container.getChildren().add(placeholder);
        }
    }

    private HBox createTradeItem(TradeSessionBean trade, boolean isActive) {
        HBox item = new HBox(20);
        item.getStyleClass().add("trade-item");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String currentUsername = (controller != null && controller.getSessionUser() != null)
                ? controller.getSessionUser().getUsername()
                : "";
        String otherUser = trade.getProposerId().equals(currentUsername) ? trade.getReceiverId()
                : trade.getProposerId();

        VBox infoBox = new VBox(5);
        Label userLabel = new Label("Utente: " + otherUser);
        userLabel.getStyleClass().add("trade-user-label");

        String formattedDate = "N/A";
        String formattedTime = "";
        if (trade.getTradeDate() != null) {
            formattedDate = trade.getTradeDate().toLocalDate().toString();
            formattedTime = trade.getTradeDate().toLocalTime().toString();
        }

        String details = String.format("Luogo: %s | Data: %s %s",
                trade.getStoreId(),
                formattedDate,
                formattedTime);
        Label infoLabel = new Label(details);
        infoLabel.getStyleClass().add("trade-info-label");

        Label statusLabel = new Label("Status: " + trade.getStatus());
        statusLabel.getStyleClass().add("trade-status-label");
        if (trade.getStatus().contains("WAITING"))
            statusLabel.getStyleClass().add("status-waiting");
        else if (trade.getStatus().contains("COMPLETED"))
            statusLabel.getStyleClass().add("status-completed");
        else if (trade.getStatus().contains("CANCEL"))
            statusLabel.getStyleClass().add("status-cancelled");

        infoBox.getChildren().addAll(userLabel, infoLabel, statusLabel);

        Button actionBtn = null;
        if (isActive) {
            String status = trade.getStatus();
            if ("WAITING_FOR_ARRIVAL".equals(status)) {
                actionBtn = new Button("Inizia scambio");
                actionBtn.getStyleClass().add("button-start-trade");
                actionBtn.setOnAction(e -> handleTradeAction(trade));
            } else if ("PARTIALLY_ARRIVED".equals(status) || "BOTH_ARRIVED".equals(status) ||
                    "INSPECTION_PHASE".equals(status) || "INSPECTION_PASSED".equals(status)) {
                actionBtn = new Button("Vai allo scambio");
                actionBtn.getStyleClass().add("button-go-to-trade");
                actionBtn.setOnAction(e -> handleTradeAction(trade));
            }
        }

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        item.getChildren().add(infoBox);
        item.getChildren().add(spacer);
        if (actionBtn != null) {
            item.getChildren().add(actionBtn);
        }

        return item;
    }

    @FXML
    private Label lblTradeStatus;
    @FXML
    private Label lblMeetingInfo;
    @FXML
    private Label lblSessionCode;
    @FXML
    private Button btnConfirmPresence;
    @FXML
    private FlowPane offeredCardsContainer;
    @FXML
    private FlowPane requestedCardsContainer;
    @FXML
    private Label lblPartnerName;
    @FXML
    private Label lblInstruction; // Inietta nuova label per istruzioni

    private void showInstructionLabel(boolean show) {
        if (lblInstruction != null) {
            lblInstruction.setVisible(show);
            lblInstruction.setManaged(show);
            lblInstruction.setText(
                    "Fornisci il tuo codice sessione allo store, sarà lui a verificare la tua presenza allo scambio");
        }
    }

    /**
     * Mostra il dettaglio di una specifica sessione di scambio.
     * Funzionalità: carica il layout FXML dedicato e popola i dati relativi ai
     * partecipanti,
     * alle carte offerte/richieste e allo stato dell'appuntamento.
     * Fornisce all'utente tutte le informazioni necessarie per finalizzare
     * lo scambio fisico.
     * Delega la configurazione UI a setupTradeSessionUI() e il popolamento delle
     * carte a populateCards().
     * 
     * @param sessionBean Il bean contenente i dati della sessione di scambio.
     * @param userCode    Il codice sessione dell'utente (se già generato).
     * @param partnerName Il nome dell'altro utente coinvolto nello scambio.
     */
    @Override
    public void showTradeDetails(TradeSessionBean sessionBean, String userCode, String partnerName) {
        this.currentSession = sessionBean; // Memorizza bean corrente
        Runnable r = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectorTradePage.fxml"));
                loader.setController(this);
                VBox root = loader.load();

                setIcons();
                wireNavbarButtons();
                loadAvatar();

                setupTradeSessionUI(sessionBean, userCode, partnerName);

                populateCards(offeredCardsContainer, sessionBean.getOffered());
                populateCards(requestedCardsContainer, sessionBean.getRequested());

                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/trade.css").toExternalForm());
                URL loginCss = getClass().getResource("/styles/style.css");
                if (loginCss != null)
                    scene.getStylesheets().add(loginCss.toExternalForm());

                if (stage != null) {
                    stage.setScene(scene);
                    stage.show();
                }

            } catch (IOException e) {
                throw new ViewException("Failed to load Collector Trade Details View", e);
            }
        };
        runInPlatform(r);
    }

    private void setupTradeSessionUI(TradeSessionBean sessionBean, String userCode, String partnerName) {
        lblTradeStatus.setText("Status: " + sessionBean.getStatus());
        lblMeetingInfo.setText(sessionBean.getStoreId() + " - " +
                (sessionBean.getTradeDate() != null ? sessionBean.getTradeDate().toString().replace("T", " ") : ""));

        if (userCode != null) {
            lblSessionCode.setText(userCode);
        } else {
            lblSessionCode.setText(EMPTY_CODE_PLACEHOLDER);
        }

        // Logica di controllo: vogliamo mostrare "Ottieni Codice" se non ne abbiamo
        // ancora uno.
        // Se ne abbiamo uno ma non siamo arrivati, mostriamo le istruzioni.
        boolean hasCode = userCode != null && !userCode.isEmpty() && !userCode.equals(EMPTY_CODE_PLACEHOLDER);
        boolean isArrived = false;
        if (controller != null && controller.getSessionUser() != null) {
            String myUser = controller.getSessionUser().getUsername();
            String prop = sessionBean.getProposerId();
            if (myUser.equals(prop))
                isArrived = sessionBean.isProposerArrived();
            else
                isArrived = sessionBean.isReceiverArrived();
        }

        btnConfirmPresence.setVisible(!hasCode);
        btnConfirmPresence.setVisible(!hasCode);
        // Riutilizza il pulsante "btnConfirmPresence" come "Ottieni Codice"
        btnConfirmPresence.setText("Ottieni Codice Sessione");

        btnConfirmPresence.setOnAction(e -> {
            int code = controller.retrieveSessionCodeById(sessionBean.getTransactionId());
            if (code > 0) {
                lblSessionCode.setText(String.valueOf(code));
                btnConfirmPresence.setVisible(false);
                // Forza refresh o mostra istruzioni
                showInstructionLabel(true);
            }
        });

        showInstructionLabel(hasCode && !isArrived);

        if (partnerName != null) {
            lblPartnerName.setText("Carte richieste (" + partnerName + ")");
        }
    }

    private void populateCards(FlowPane container, List<model.bean.CardBean> cards) {
        if (container == null)
            return;
        container.getChildren().clear();
        if (cards == null)
            return;

        for (model.bean.CardBean card : cards) {
            VBox cardItem = new VBox(5);
            cardItem.setAlignment(javafx.geometry.Pos.CENTER);
            cardItem.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 5; -fx-padding: 5;");

            ImageView imgView = new ImageView();
            imgView.setFitWidth(80);
            imgView.setFitHeight(112);
            try {
                if (card.getImageUrl() != null && !card.getImageUrl().isEmpty())
                    imgView.setImage(new Image(card.getImageUrl()));
            } catch (Exception _) {
                // Ignora errore caricamento immagine
            }

            Label nameLbl = new Label(card.getName());
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");
            nameLbl.setWrapText(true);
            nameLbl.setMaxWidth(80);

            cardItem.getChildren().addAll(imgView, nameLbl);
            container.getChildren().add(cardItem);
        }
    }

    private void handleTradeAction(model.bean.TradeSessionBean trade) {
        if (controller != null) {
            // Aggiornato per chiamare l'overload che prende direttamente l'int, rimuovendo
            // la necessità
            // di un ProposalBean temporaneo
            controller.openTradeDetails(trade.getTransactionId());
        }
    }

    /**
     * Chiude la visualizzazione nascondendo lo stage.
     * Funzionalità: invoca il metodo hide() sullo stage corrente.
     * Delega l'esecuzione sul thread JavaFX a runInPlatform().
     */
    @Override
    public void close() {
        Runnable r = () -> {
            if (stage != null)
                stage.hide();
        };
        runInPlatform(r);
    }

    /**
     * Associa il controller alla vista.
     * 
     * @param controller Il controller da associare.
     */
    @Override
    public void setController(Object controller) {
        this.controller = (TradeController) controller;
    }

    /**
     * Richiede un aggiornamento dei dati (non implementato per questa vista).
     */
    @Override
    public void refresh() {
        // Intenzionalmente vuoto: non usato in questa vista
    }

    /**
     * Conferma la presenza fisica allo store e richiede la generazione del codice.
     * Funzionalità: comunica al controller l'arrivo dell'utente per la sessione
     * corrente.
     * Permette l'avanzamento dello scambio alla fase fisica.
     * Delega la logica di visualizzazione delle istruzioni a
     * showInstructionLabel().
     */
    @Override
    public void registerConfirmPresence() {
        // Aggiorna la UI per mostrare la conferma (chiamato dal controller)
        if (currentSession != null && controller != null) {
            Runnable r = () -> {
                // Usa il getter safe per ottenere il codice senza rigenerarlo
                int code = controller.getUserSessionCode(currentSession.getTransactionId());
                if (code > 0) {
                    if (lblSessionCode != null) {
                        lblSessionCode.setText(String.valueOf(code));
                    }
                    if (btnConfirmPresence != null) {
                        btnConfirmPresence.setVisible(false);
                    }
                    showInstructionLabel(true);
                }
            };
            runInPlatform(r);
        }
    }

    /**
     * Visualizza il codice sessione corrente nella UI.
     * Funzionalità: mostra il codice se presente e attiva le istruzioni correlate.
     * Delega la visualizzazione delle istruzioni a showInstructionLabel().
     */
    @Override
    public void showSessionCode() {
        // Mostra il codice sessione corrente
        Runnable r = () -> {
            if (lblSessionCode != null) {
                String code = lblSessionCode.getText();
                if (code != null && !code.equals(EMPTY_CODE_PLACEHOLDER) && !code.isEmpty()) {
                    // Codice già presente, mostra istruzioni
                    showInstructionLabel(true);
                }
            }
        };
        runInPlatform(r);
    }

    /**
     * Visualizza un messaggio di errore (non implementato per questa vista).
     * 
     * @param errorMessage Il messaggio d'errore.
     */
    @Override
    public void showError(String errorMessage) {
        // Intenzionalmente vuoto: non usato in questa vista
    }
}
