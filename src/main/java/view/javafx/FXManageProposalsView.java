package view.javafx;

import controller.ManageProposalsController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.bean.ProposalBean;
import view.IManageProposalsView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Vista JavaFX dedicata alla gestione delle proposte di scambio.
 * Consente all'utente di visualizzare le proposte ricevute, quelle inviate e lo
 * storico
 * dei completamenti, offrendo azioni per accettare o rifiutare le richieste in
 * sospeso.
 */
public class FXManageProposalsView implements IManageProposalsView {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(FXManageProposalsView.class.getName());
    private ManageProposalsController manageProposalsController;

    @FXML
    private HBox navbar;
    @FXML
    private HBox menuBox;
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
    private ScrollPane receivedScrollPane;
    @FXML
    private VBox receivedProposalsList;
    @FXML
    private ScrollPane sentScrollPane;
    @FXML
    private VBox sentProposalsList;
    @FXML
    private ScrollPane completedScrollPane;
    @FXML
    private VBox completedProposalsList;

    private Stage stage;

    /**
     * Carica il layout FXML per la gestione delle proposte e inizializza la
     * visualizzazione.
     * Funzionalità: carica il file FXML, configura la barra di navigazione e popola
     * le informazioni utente.
     * Utility: funge da punto di ingresso principale per la gestione delle proposte
     * in JavaFX.
     * Delega la configurazione delle icone a setIcons(), il binding dei pulsanti a
     * wireNavbarButtons(),
     * l'inizializzazione delle info utente a setupUserInfo() e la configurazione
     * dello stage a setupScene().
     * Infine, avvia il caricamento iniziale dei dati tramite refresh().
     */
    @Override
    public void display() {
        Runnable show = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManageProposalsPage.fxml"));
                loader.setController(this);
                VBox root = loader.load();

                setIcons();
                wireNavbarButtons();
                setupUserInfo();
                setupScene(root);

                // Carica le proposte dopo che la UI è stata configurata
                refresh();

            } catch (IOException e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Impossibile caricare ManageProposalsPage.fxml", e);
            }
        };

        if (Platform.isFxApplicationThread())
            show.run();
        else
            Platform.runLater(show);
    }

    private void wireNavbarButtons() {
        setupNavButton(homeBtn, () -> manageProposalsController.goToHomepage());
        setupNavButton(collectionBtn, () -> manageProposalsController.goToCollection());
        setupNavButton(tradeBtn, () -> manageProposalsController.goToTrade());

        if (proposalBtn != null) {
            proposalBtn.getStyleClass().add("active-nav");
        }

        setupNavButton(logoutBtn, () -> manageProposalsController.logout());
    }

    private void setupNavButton(Button btn, Runnable action) {
        if (btn != null) {
            btn.setOnAction(e -> {
                if (manageProposalsController != null) {
                    action.run();
                }
            });
        }
    }

    private void setupUserInfo() {
        if (manageProposalsController != null && manageProposalsController.getSessionUser() != null) {
            usernameLabel.setText(manageProposalsController.getSessionUser().getUsername());
            try {
                avatarImage.setImage(new Image(getClass().getResourceAsStream("/icons/collectorpp.png")));
            } catch (Exception _) {
                // Ignora errore caricamento avatar
            }
        }
    }

    private void setupScene(VBox root) {
        Scene scene = new Scene(root);
        java.net.URL cssUrl = getClass().getResource("/styles/style.css");
        if (cssUrl != null)
            scene.getStylesheets().add(cssUrl.toExternalForm());

        Optional<Window> existing = Window.getWindows().stream().filter(Window::isShowing).findFirst();
        if (existing.isPresent() && existing.get() instanceof Stage existingStage) {
            existingStage.setScene(scene);
            existingStage.setTitle("Manage Proposals");
            this.stage = existingStage;
        } else {
            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle("Manage Proposals");
            st.setWidth(1280);
            st.setHeight(800);
            st.centerOnScreen();
            st.show();
            this.stage = st;
        }
    }

    /**
     * Richiede al controller di caricare e inviare i dati delle proposte.
     * Funzionalità: invoca il metodo loadProposals() del controller per innescare
     * il modello PUSH.
     * Utility: assicura che la vista sia popolata con i dati più recenti dal
     * database.
     */
    @Override
    public void refresh() {
        if (manageProposalsController != null) {
            manageProposalsController.loadProposals();
        }
    }

    /**
     * Visualizza un messaggio di errore all'utente (non ancora implementato
     * graficamente).
     * 
     * @param errorMessage Il contenuto del messaggio di errore.
     */
    @Override
    public void showError(String errorMessage) {
        // Implementazione futura della visualizzazione errori
    }

    /**
     * Aggiorna la lista delle proposte ricevute in attesa.
     * Funzionalità: svuota il container e aggiunge una riga per ogni proposta
     * ricevuta.
     * Permette all'utente di vedere chi gli ha inviato proposte e agire di
     * conseguenza.
     * Delega la creazione grafica della riga al metodo helper createProposalRow().
     * 
     * @param proposals La lista di bean delle proposte ricevute.
     */
    @Override
    public void showReceivedPendingProposals(List<ProposalBean> proposals) {
        if (receivedProposalsList == null)
            return;

        Platform.runLater(() -> {
            receivedProposalsList.getChildren().clear();
            for (ProposalBean proposal : proposals) {
                HBox row = createProposalRow(proposal, true, false);
                receivedProposalsList.getChildren().add(row);
            }
        });
    }

    /**
     * Aggiorna la lista delle proposte inviate dall'utente corrente.
     * Funzionalità: ricostruisce il container con le proposte inviate in stato
     * PENDING.
     * Permette all'utente di vedere le proposte inviate e agire di conseguenza.
     * Delega la creazione grafica della riga al metodo helper createProposalRow().
     * 
     * @param proposals La lista di bean delle proposte inviate.
     */
    @Override
    public void showSentPendingProposals(List<ProposalBean> proposals) {
        if (sentProposalsList == null)
            return;

        Platform.runLater(() -> {
            sentProposalsList.getChildren().clear();
            for (ProposalBean proposal : proposals) {
                HBox row = createProposalRow(proposal, false, false);
                sentProposalsList.getChildren().add(row);
            }
        });
    }

    /**
     * Aggiorna lo storico delle proposte completate (accettate, rifiutate o
     * scadute).
     * Funzionalità: popola il container dello storico con i bean forniti.
     * Permette all'utente di vedere le proposte storiche e agire di conseguenza.
     * Delega la creazione grafica della riga al metodo helper createProposalRow().
     * 
     * @param proposals La lista di bean delle proposte storiche.
     */
    @Override
    public void showCompletedProposals(List<ProposalBean> proposals) {
        if (completedProposalsList == null)
            return;

        Platform.runLater(() -> {
            completedProposalsList.getChildren().clear();
            for (ProposalBean proposal : proposals) {
                HBox row = createProposalRow(proposal, false, true);
                completedProposalsList.getChildren().add(row);
            }
        });
    }

    private HBox createProposalRow(ProposalBean proposal, boolean showActions, boolean showStatus) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 5; -fx-padding: 10;");

        // Icona che mostra se la proposta è stata inviata o ricevuta
        if (showStatus) {
            ImageView icon = new ImageView();
            icon.setFitWidth(24);
            icon.setFitHeight(24);
            // Utilizzo un flag pre-calcolato dal Controller
            boolean isSent = proposal.isSentByMe();
            try {
                String iconPath = isSent ? "/icons/sent.png" : "/icons/receive.png";
                icon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
            } catch (Exception _) {
                // Icona non disponibile
            }
            row.getChildren().add(icon);
        }

        // Informazioni proposta
        VBox info = new VBox(5);
        info.setStyle("-fx-text-fill: white;");

        // Utilizzo un flag pre-calcolato per evitare business logic nella View
        String direction = proposal.isSentByMe()
                ? "To: " + proposal.getToUser()
                : "From: " + proposal.getFromUser();
        Label directionLabel = new Label(direction);
        directionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        String cardsInfo = String.format("Offering: %d card(s) | Requesting: %d card(s)",
                proposal.getOffered().size(), proposal.getRequested().size());
        Label cardsLabel = new Label(cardsInfo);
        cardsLabel.setStyle("-fx-text-fill: #cccccc;");

        info.getChildren().addAll(directionLabel, cardsLabel);

        // Spaziatore
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(info, spacer);

        // Etichetta dello stato per le proposte completate
        if (showStatus) {
            Label statusLabel = new Label(proposal.getStatus());
            String statusColor = "ACCEPTED".equals(proposal.getStatus()) ? "#4CAF50" : "#f44336";
            statusLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", statusColor));
            row.getChildren().add(statusLabel);
        }

        // Button per visualizzare la proposta (sempre presente)
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> showProposalDetails(proposal));
        row.getChildren().add(viewBtn);

        // Button per accettare o rifiutare la proposta (presenti solo per le proposte
        // in sospeso ricevute)
        if (showActions) {
            Button acceptBtn = new Button("Accept");
            acceptBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
            acceptBtn.setOnAction(e -> manageProposalsController.acceptProposal(proposal.getProposalId()));

            Button rejectBtn = new Button("Reject");
            rejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
            rejectBtn.setOnAction(e -> manageProposalsController.rejectProposal(proposal.getProposalId()));

            row.getChildren().addAll(acceptBtn, rejectBtn);
        }

        return row;
    }

    private void showProposalDetails(ProposalBean proposal) {
        // Creazione di una finestra di dialogo per mostrare i dettagli della proposta
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Proposal Details");
        alert.setHeaderText("Trade Proposal #" + proposal.getProposalId());

        // Utilizzo metodo Bean per generazione descrizione formattata
        String content = proposal.getFormattedDetails();

        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Associa il controller alla vista.
     * 
     * @param controller L'istanza del controller da sottoscrivere.
     */
    @Override
    public void setController(Object controller) {
        this.manageProposalsController = (ManageProposalsController) controller;
    }

    /**
     * Visualizza un dialogo di conferma post-accettazione proposta.
     * Funzionalità: mostra all'utente che l'azione ha avuto successo e offre
     * un'opzione di navigazione.
     * Utility: garantisce un feedback immediato sull'esito dell'interazione.
     * 
     * @param onNavigate Callback da eseguire per la navigazione verso la sezione
     *                   Trade.
     */
    @Override
    public void showProposalAcceptedDialog(Runnable onNavigate) {
        Runnable r = () -> {
            javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Richiesta Accettata");
            dialog.setHeaderText("Scambio Confermato");
            dialog.setContentText(
                    "Hai accettato la richiesta di scambio, vai alla sezione trade per maggiori dettagli");

            javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType("OK",
                    javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            javafx.scene.control.ButtonType tradeButtonType = new javafx.scene.control.ButtonType("Vai a Trade",
                    javafx.scene.control.ButtonBar.ButtonData.OTHER);

            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, tradeButtonType);

            // Accedi ai pulsanti per lo styling
            javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
            if (okButton != null)
                okButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");

            javafx.scene.Node tradeButton = dialog.getDialogPane().lookupButton(tradeButtonType);
            if (tradeButton != null)
                tradeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");

            // Applica stile
            java.net.URL cssUrl = getClass().getResource("/styles/login.css");
            if (cssUrl != null)
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());

            Optional<javafx.scene.control.ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == tradeButtonType && onNavigate != null) {
                onNavigate.run();
            }
        };

        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    private void setIcons() {
        try {
            homeIcon.setImage(new Image(getClass().getResourceAsStream("/icons/homepage.png")));
        } catch (Exception _) {
            // Ignora icona mancante
        }
        try {
            collectionIcon.setImage(new Image(getClass().getResourceAsStream("/icons/collection.png")));
        } catch (Exception _) {
            // Ignora icona mancante
        }
        try {
            tradeIcon.setImage(new Image(getClass().getResourceAsStream("/icons/trade.png")));
        } catch (Exception _) {
            // Ignora icona mancante
        }
        try {
            proposalIcon.setImage(new Image(getClass().getResourceAsStream("/icons/manageproposals.png")));
        } catch (Exception _) {
            // Ignora icona mancante
        }
        try {
            logoutIcon.setImage(new Image(getClass().getResourceAsStream("/icons/logout.png")));
        } catch (Exception _) {
            // Ignora icona mancante
        }
    }

    /**
     * Chiude la finestra corrente.
     * Funzionalità: invoca il metodo hide() sullo stage, gestendo eventuali
     * eccezioni di stato.
     * Utility: permette al controller di terminare il ciclo di vita della vista.
     */
    @Override
    public void close() {
        Runnable r = () -> {
            if (stage != null) {
                try {
                    stage.hide();
                } catch (Exception _) {
                    // Stage potrebbe essere null o già chiuso
                }
            }
        };
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }
}
