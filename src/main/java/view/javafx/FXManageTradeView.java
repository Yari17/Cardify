package view.javafx;

import controller.ManageTradeController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.geometry.Insets;
import javafx.scene.image.Image;

import model.bean.ProposalBean;
import view.IManageTradeView;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * FXManageTradeView
 * =================
 * Questa classe è la vista JavaFX per la sezione "Manage Trades".
 * Contiene la logica esclusivamente di presentazione: costruisce le celle
 * della lista, mostra i dialog con i dettagli delle proposte e invoca
 * il controller applicativo (`ManageTradeController`) per le operazioni di
 * accettazione/rifiuto/avvio dello scambio.
 *
 * NOTE SULLA RESPONSABILITÀ:
 * - La vista è 'passiva': non deve accedere a DAO o a logica di persistenza.
 * - Il controller applicativo è l'Information Expert per le operazioni sui dati.
 */

public class FXManageTradeView implements IManageTradeView {
    private static final Logger LOGGER = Logger.getLogger(FXManageTradeView.class.getName());

    // repeated literal constants
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String CSS_TRADE_LIST_CELL = "trade-list-cell";
    private static final String BG_COLOR_STYLE = "-fx-background-color: #0F1720; -fx-padding: 14;";
    private static final String TEXT_FILL_WHITE = "-fx-text-fill: white;";
    private static final int CARD_IMG_W = 120;
    private static final int CARD_IMG_H = 80;
    // store current logged username for cell rendering
    private String currentUsername;

    @FXML
    private ListView<ProposalBean> pendingTradesList;

    @FXML
    private ListView<ProposalBean> concludedTradesList;

    // Navbar controls referenced by FXML (ensure fx:id bindings resolve)
    @FXML
    private javafx.scene.control.Button homeButton;

    @FXML
    private javafx.scene.control.Button collectionButton;

    @FXML
    private javafx.scene.control.Button liveTradeButton;

    @FXML
    private javafx.scene.control.Button tradeButton;

    @FXML
    private javafx.scene.control.Button logoutButton;

    @FXML
    private javafx.scene.layout.VBox logoutButtonContainer;

    @FXML
    private javafx.scene.image.ImageView profileImageView;

    @FXML
    private javafx.scene.control.Label usernameLabel;

    // Manage controller for accept/decline operations
    private ManageTradeController manageController; // retained for backward compatibility
    // callback consumers registered by controller
    private java.util.function.Consumer<String> onAcceptCallback;
    private java.util.function.Consumer<String> onDeclineCallback;
    private java.util.function.Consumer<String> onCancelCallback;
    private java.util.function.Consumer<String> onTradeClickCallback;
    private java.util.function.Consumer<String> onTradeNowClickCallback;
    private Stage stage;
    // Keep last shown lists to allow refresh() to re-render them
    private java.util.List<model.bean.ProposalBean> lastPending = new java.util.ArrayList<>();
    private java.util.List<model.bean.ProposalBean> lastConcluded = new java.util.ArrayList<>();

    public FXManageTradeView() {
        // FXML will inject fields
    }

    @FXML
    public void initialize() {
        // Metodo chiamato da FXMLLoader dopo l'iniezione degli elementi FXML.
        // Qui si registra la factory per le celle della ListView, che costruirà
        // la rappresentazione grafica di ogni proposta.
        if (pendingTradesList != null) setupCellFactory(pendingTradesList);
        if (concludedTradesList != null) setupCellFactory(concludedTradesList);
    }

    private void setupCellFactory(ListView<ProposalBean> listView) {
        // Configura la cellFactory per la ListView.
        // Se la ListView è la lista di "concludedTrades" la cella sarà in sola lettura.
        boolean readOnly = listView == concludedTradesList;
        listView.setCellFactory(lv -> {
            // accede a lv.getItems() solo per evitare warning di analyzers che
            // non riconoscono l'uso riflessivo da FXML; la factory ritorna la cella.
            lv.getItems();
            return new TradeListCell(readOnly);
        });
    }

    private class TradeListCell extends javafx.scene.control.ListCell<ProposalBean> {
        private final boolean readOnly;

        TradeListCell(boolean readOnly) {
            this.readOnly = readOnly;
        }

        // Helper interno per caricare immagini usate nella rappresentazione della cella.
        // Usare FXManageTradeView.class.getResource per risolvere la risorsa nel JAR.
        private Image loadImageResource(String path, double width, double height) {
            try {
                if (path == null) return null;
                if (path.startsWith("/")) {
                    java.net.URL res = FXManageTradeView.class.getResource(path);
                    if (res != null) return new Image(res.toExternalForm(), width, height, true, true);
                }
                return new Image(path, width, height, true, true);
            } catch (Exception ex) {
                LOGGER.fine(() -> "Failed to load image resource in cell: " + path + " => " + ex.getMessage());
                return null;
            }
        }

        @Override
        protected void updateItem(ProposalBean item, boolean empty) {
            // Questo metodo viene chiamato ogni volta che la cella deve aggiornare il suo contenuto.
            // Se l'item è vuoto o nullo, pulisce la cella; altrimenti costruisce la grafica della cella.
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().remove(CSS_TRADE_LIST_CELL);
                return;
            }

            // Determina se la proposta è in ingresso (cioè rivolta all'utente corrente)
            OtherInfo info = determineOtherAndIncoming(item);

            // Costruisce il Node rappresentante la cella ed lo imposta
            javafx.scene.Node cellGraphic = createCellGraphic(item, info);
            setText(null);
            setGraphic(cellGraphic);
            if (!getStyleClass().contains(CSS_TRADE_LIST_CELL)) getStyleClass().add(CSS_TRADE_LIST_CELL);

            // Aggiunge classi di stile in base allo stato della proposta (per esempio "rejected" o "expired")
            String ps = item.getStatus();
            getStyleClass().removeAll("rejected", "expired");
            if (ps != null) {
                if (ps.equalsIgnoreCase(STATUS_REJECTED)) getStyleClass().add("rejected");
                else if (ps.equalsIgnoreCase(STATUS_EXPIRED)) getStyleClass().add("expired");
            }
        }

        private javafx.scene.Node createCellGraphic(ProposalBean item, OtherInfo info) {
            // Build a richer cell UI: left icon, center text block (title + subtitle), right actions + status badge
            HBox root = new HBox(12);
            root.getStyleClass().add(CSS_TRADE_LIST_CELL);
            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Icon
            ImageView dir = loadDirectionIcon(info.incoming);
            if (dir != null) {
                dir.setFitWidth(28);
                dir.setFitHeight(28);
                dir.getStyleClass().add("trade-direction-icon");
                root.getChildren().add(dir);
            }

            // Text block: title and subtitle
            VBox textBlock = new VBox(4);
            String title = item.getProposalId() != null ? item.getProposalId() : "Proposal";
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("trade-cell-label");

            // extract from/to display to avoid nested ternary
            String fromDisplay = item.getFromUser() == null ? "?" : item.getFromUser();
            String toDisplay = item.getToUser() == null ? "?" : item.getToUser();

            // subtitle shows participants and meeting info
            String participants = fromDisplay + " → " + toDisplay;
            Label subtitle = new Label(participants);
            subtitle.getStyleClass().add("cell-secondary");

            // small meeting info (date/time/place) as secondary
            String meetingDateDisplay = item.getMeetingDate() != null ? item.getMeetingDate() : "TBD";
            String meetingTimeDisplay = item.getMeetingTime() != null ? item.getMeetingTime() : "";
            String meetingPlaceDisplay = item.getMeetingPlace() != null ? item.getMeetingPlace() : "TBD";
            String meet = meetingDateDisplay + (meetingTimeDisplay.isEmpty() ? "" : " " + meetingTimeDisplay) + " • " + meetingPlaceDisplay;
            Label meetingLabel = new Label(meet);
            meetingLabel.getStyleClass().add("cell-secondary");

            textBlock.getChildren().addAll(titleLabel, subtitle, meetingLabel);
            root.getChildren().add(textBlock);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            root.getChildren().add(spacer);

            // Status badge
            String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "UNKNOWN";
            Label badge = new Label(status);
            badge.getStyleClass().addAll("status-badge");
            switch (status) {
                case "ACCEPTED" -> badge.getStyleClass().add("badge-accepted");
                case STATUS_PENDING -> badge.getStyleClass().add("badge-pending");
                case STATUS_REJECTED -> badge.getStyleClass().add("badge-rejected");
                case STATUS_EXPIRED -> badge.getStyleClass().add("badge-expired");
                default -> badge.getStyleClass().add("badge-unknown");
            }

            // Actions container
            HBox actions = new HBox(8);
            actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            Button viewBtn = new Button("View");
            viewBtn.getStyleClass().add("button-view");
            viewBtn.setOnAction(evt -> { showProposalDetails(item); evt.consume(); });
            actions.getChildren().add(viewBtn);

            // Se la proposta è in entrata, è in stato PENDING e la lista non è readOnly,
            // mostra i pulsanti Accept / Decline.
            boolean showIncomingActions = !readOnly && info.incoming && STATUS_PENDING.equals(status);
            if (showIncomingActions) {
                Button acceptBtn = new Button("Accept");
                acceptBtn.getStyleClass().add("button-accept");
                acceptBtn.setOnAction(e -> {
                    onAcceptTradeProposal(item.getProposalId());
                    e.consume();
                });

                Button declineBtn = new Button("Decline");
                declineBtn.getStyleClass().add("button-decline");
                declineBtn.setOnAction(e -> {
                    onDeclineTradeProposal(item.getProposalId());
                    e.consume();
                });

                actions.getChildren().addAll(acceptBtn, declineBtn);
            }

            root.getChildren().addAll(badge, actions);

            return root;
        }

        private ImageView loadDirectionIcon(boolean incoming) {
            // Utilizza il helper di classe per caricare l'immagine in modo consistente.
            String iconPath = incoming ? "/icons/receive.png" : "/icons/sent.png";
            Image img = loadImageResource(iconPath, 20, 20);
            if (img == null) return null;
            ImageView iv = new ImageView(img);
            iv.getStyleClass().add("trade-direction-icon");
            return iv;
        }

        /**
         * Determina se la proposta è "incoming" rispetto all'utente corrente
         * e restituisce anche l'username dell'altra parte coinvolta nello scambio.
         */
        private OtherInfo determineOtherAndIncoming(ProposalBean item) {
            OtherInfo r = new OtherInfo();
            if (item == null || currentUsername == null) return r;
            String from = item.getFromUser();
            String to = item.getToUser();
            if (currentUsername.equals(to)) {
                r.incoming = true;
                r.other = from;
            } else {
                r.incoming = false;
                r.other = to != null ? to : from;
            }
            return r;
        }

        /**
         * Apre un dialog modale con i dettagli della proposta.
         * Recupera dal controller la Proposal domain object (se disponibile)
         * e costruisce la UI per mostrare offerte, richieste e informazioni di meeting.
         */
        private void showProposalDetails(ProposalBean item) {
            if (item == null) return;
            model.domain.Proposal p = manageController != null ? manageController.getProposalById(item.getProposalId()) : null;

            javafx.application.Platform.runLater(() -> {
                Stage dialog = new Stage();
                dialog.initOwner(stage);
                dialog.initModality(Modality.WINDOW_MODAL);
                dialog.setTitle("Proposal details: " + item.getProposalId());

                // Root layout
                javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
                // Imposta lo stile del pannello principale del dialog
                root.setStyle(BG_COLOR_STYLE);

                Node header = buildHeader(item);
                root.setTop(header);

                VBox centerBox = buildCenterBox(p, item);
                ScrollPane sp = new ScrollPane(centerBox);
                sp.setFitToWidth(true);
                sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                root.setCenter(sp);

                HBox footer = buildFooter(dialog);
                root.setBottom(footer);

                Scene scene = new Scene(root, 840, 520);
                // Applica il foglio di stile dell'applicazione, se presente
                try {
                    java.net.URL res = getClass().getResource(view.IView.themeCssPath());
                    if (res != null) scene.getStylesheets().add(res.toExternalForm());
                } catch (Exception ex) {
                    LOGGER.fine(() -> "Unable to apply theme stylesheet in proposal dialog: " + ex.getMessage());
                }

                dialog.setScene(scene);
                dialog.showAndWait();
            });
        }

        private Node buildHeader(ProposalBean item) {
            HBox header = new HBox(12);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            // Intestazione: mostra l'ID della proposta e lo stato con un badge colorato
            Label id = new Label("Proposal: " + item.getProposalId());
            id.setStyle(TEXT_FILL_WHITE + " -fx-font-size: 16px; -fx-font-weight: bold;");

            String statusText = item.getStatus() != null ? item.getStatus() : "UNKNOWN";
            Label status = new Label(statusText);
            // Costruisce lo stile del badge (colore variabile a seconda dello stato)
            String badgeStyle = "-fx-padding: 4 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-text-fill: white;";
            String color;
            switch (statusText.toUpperCase()) {
                case "ACCEPTED" -> color = "#10B981";
                case STATUS_PENDING -> color = "#F59E0B";
                case STATUS_REJECTED -> color = "#EF4444";
                case STATUS_EXPIRED -> color = "#6B7280";
                default -> color = "#374151";
            }
            badgeStyle = String.format("%s -fx-background-color: %s;", badgeStyle, color);
            status.setStyle(badgeStyle);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            header.getChildren().addAll(id, spacer, status);
            return header;
        }

        private VBox buildCenterBox(model.domain.Proposal p, ProposalBean item) {
            VBox centerBox = new VBox(12);
            centerBox.setPadding(new Insets(10));

            // Sezioni principali: partecipanti e meeting
            String[] participants = resolveParticipants(p, item);
            Label participantsLabel = new Label("Proposer: " + (participants[0] != null ? participants[0] : "<unknown>") + "   •   Receiver: " + (participants[1] != null ? participants[1] : "<unknown>"));
            participantsLabel.setStyle("-fx-text-fill: #CAD5E0; -fx-font-size: 13px;");

            String[] meeting = resolveMeeting(p, item);
            Label meetingLabel = new Label("Meeting: " + meeting[0] + " @ " + meeting[1]);
            meetingLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

            centerBox.getChildren().addAll(participantsLabel, meetingLabel);

            Label offeredLabel = new Label("Offered");
            offeredLabel.setStyle("-fx-text-fill: #A7F3D0; -fx-font-weight: bold; -fx-font-size: 14px;");
            javafx.scene.layout.FlowPane offeredPane = new javafx.scene.layout.FlowPane(10, 10);
            offeredPane.setPrefWrapLength(700);

            Label requestedLabel = new Label("Requested");
            requestedLabel.setStyle("-fx-text-fill: #BFDBFE; -fx-font-weight: bold; -fx-font-size: 14px;");
            javafx.scene.layout.FlowPane requestedPane = new javafx.scene.layout.FlowPane(10, 10);
            requestedPane.setPrefWrapLength(700);

            if (p != null && p.getCardsOffered() != null && !p.getCardsOffered().isEmpty()) populateCardPane(offeredPane, p.getCardsOffered());
            if (p != null && p.getCardsRequested() != null && !p.getCardsRequested().isEmpty()) populateCardPane(requestedPane, p.getCardsRequested());

            VBox sections = new VBox(8);
            sections.getChildren().addAll(offeredLabel, offeredPane, requestedLabel, requestedPane);
            centerBox.getChildren().add(sections);
            return centerBox;
        }

        private String[] resolveParticipants(model.domain.Proposal p, ProposalBean item) {
            String proposer = p != null && p.getProposerId() != null ? p.getProposerId() : item.getFromUser();
            String receiver = p != null && p.getReceiverId() != null ? p.getReceiverId() : item.getToUser();
            return new String[]{proposer, receiver};
        }

        private String[] resolveMeeting(model.domain.Proposal p, ProposalBean item) {
            String meetingDate;
            if (item.getMeetingDate() != null) meetingDate = item.getMeetingDate();
            else if (p != null && p.getMeetingDate() != null) meetingDate = p.getMeetingDate();
            else meetingDate = "TBD";

            String meetingPlace;
            if (item.getMeetingPlace() != null) meetingPlace = item.getMeetingPlace();
            else if (p != null && p.getMeetingPlace() != null) meetingPlace = p.getMeetingPlace();
            else meetingPlace = "TBD";

            return new String[]{meetingDate, meetingPlace};
        }

        private void populateCardPane(javafx.scene.layout.FlowPane pane, List<model.domain.Card> cards) {
            for (model.domain.Card c : cards) {
                if (c == null) continue;
                VBox cardBox = new VBox(6);
                cardBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 8;");
                cardBox.setPrefWidth(150);

                ImageView iv = new ImageView();
                iv.setFitWidth(120);
                iv.setFitHeight(80);
                iv.setPreserveRatio(true);
                // Carica immagine della carta con fallback su placeholder
                Image img = null;
                if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) img = loadImageResource(c.getImageUrl(), CARD_IMG_W, CARD_IMG_H);
                if (img == null) img = loadImageResource("/icons/nocardimage.svg", CARD_IMG_W, CARD_IMG_H);
                if (img != null) iv.setImage(img);

                Label name = new Label(c.getName() != null ? c.getName() : c.getId());
                name.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                Label qty = new Label("x" + c.getQuantity());
                qty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

                cardBox.getChildren().addAll(iv, name, qty);
                pane.getChildren().add(cardBox);
            }
        }

        private HBox buildFooter(Stage dialog) {
            HBox footer = new HBox();
            footer.setPadding(new Insets(10, 0, 0, 0));
            footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("button-view");
            closeBtn.setOnAction(evt -> { dialog.close(); evt.consume(); });
            footer.getChildren().add(closeBtn);
            return footer;
        }
    }
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void display() {
        if (stage != null) {
            stage.show();
            // Ask ManageTradeController to load and display trades for this view
            if (manageController != null) manageController.loadAndDisplayTrades(this);
        } else {
            LOGGER.warning("Stage not set, cannot display");
        }
    }

    @Override
    public void close() {
        if (stage != null) stage.close();
    }

    @Override
    public void showError(String errorMessage) {
        LOGGER.log(Level.SEVERE, "Error: {0}", errorMessage);
    }


    public void setManageController(ManageTradeController controller) {
        this.manageController = controller;
    }

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        if (usernameLabel != null) usernameLabel.setText(username);
    }

    @Override
    public void registerOnAccept(java.util.function.Consumer<String> onAccept) { this.onAcceptCallback = onAccept; }

    @Override
    public void registerOnDecline(java.util.function.Consumer<String> onDecline) { this.onDeclineCallback = onDecline; }

    @Override
    public void registerOnCancel(java.util.function.Consumer<String> onCancel) { this.onCancelCallback = onCancel; }

    @Override
    public void registerOnTradeClick(java.util.function.Consumer<String> onTradeClick) { this.onTradeClickCallback = onTradeClick; }

    @Override
    public void registerOnTradeNowClick(java.util.function.Consumer<String> onTradeNowClick) { this.onTradeNowClickCallback = onTradeNowClick; }

    public void onAcceptTradeProposal(String id) {
        if (id == null || manageController == null) return;
        boolean ok = manageController.acceptProposal(id);
        if (ok) {
            if (onAcceptCallback != null) onAcceptCallback.accept(id);
            else manageController.loadAndDisplayTrades(this);
        }
    }

    public void onCancelTradeProposal(String id) {
        // present a confirmation dialog before cancelling (different behavior from decline)
        if (id == null || manageController == null) return;
        javafx.application.Platform.runLater(() -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Conferma annullamento");
            confirm.setHeaderText("Vuoi annullare questa proposta?");
            confirm.setContentText("La proposta verrà rifiutata e non sarà più visibile come in attesa.");
            confirm.initOwner(stage);
            confirm.initModality(Modality.WINDOW_MODAL);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    boolean ok = manageController.declineProposal(id);
                    if (ok) {
                        if (onCancelCallback != null) onCancelCallback.accept(id);
                        else manageController.loadAndDisplayTrades(this);
                    }
                }
            });
        });
    }

    public void onDeclineTradeProposal(String id) {
        if (id == null || manageController == null) return;
        boolean ok = manageController.declineProposal(id);
        if (ok) {
            if (onDeclineCallback != null) onDeclineCallback.accept(id);
            else manageController.loadAndDisplayTrades(this);
        }
    }

    public void onTradeClick(String id) {
        if (id == null || manageController == null) return;
        if (onTradeClickCallback != null) onTradeClickCallback.accept(id);
        else manageController.initiateTrade(id);
    }

    public void onTradeNowClick(String id) {
        // differ from onTradeClick by requesting confirmation before initiating
        if (id == null || manageController == null) return;
        javafx.application.Platform.runLater(() -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Avvia Trade Ora");
            confirm.setHeaderText("Vuoi avviare lo scambio ora?");
            confirm.setContentText("Questo avvierà la procedura di scambio per la proposta selezionata.");
            confirm.initOwner(stage);
            confirm.initModality(Modality.WINDOW_MODAL);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    if (onTradeNowClickCallback != null) onTradeNowClickCallback.accept(id);
                    else manageController.initiateTrade(id);
                }
            });
        });
    }

    @Override
    public void displayTrades(List<ProposalBean> pending, List<ProposalBean> scheduled) {
        // Store provided lists and trigger refresh() so the common refresh entrypoint is used
        this.lastPending = pending != null ? new java.util.ArrayList<>(pending) : new java.util.ArrayList<>();
        this.lastConcluded = scheduled != null ? new java.util.ArrayList<>(scheduled) : new java.util.ArrayList<>();
        // Use refresh entrypoint to update UI
        refresh();
    }

    @Override
    public void refresh() {
        javafx.application.Platform.runLater(() -> {
            if (pendingTradesList != null) {
                pendingTradesList.getItems().clear();
                pendingTradesList.getItems().addAll(this.lastPending);
                pendingTradesList.refresh();
            }
            if (concludedTradesList != null) {
                concludedTradesList.getItems().clear();
                concludedTradesList.getItems().addAll(this.lastConcluded);
                concludedTradesList.refresh();
            }
        });
    }


    @FXML
    private void onHomeClicked() {
        if (manageController != null) manageController.navigateToHome();
    }

    @FXML
    private void onCollectionClicked() {
        if (manageController != null) manageController.navigateToCollection();
    }

    @FXML
    private void onLiveTradeClicked() {
                if (manageController != null) manageController.navigateToLiveTrades();
    }

    @FXML
    private void onLogoutClicked() {
        if (manageController != null) manageController.onLogoutRequested();
    }

    @FXML
    private void onNavButtonHoverEnter(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            container.setStyle(
                    "-fx-background-color: rgba(41, 182, 246, 0.2); " +
                            "-fx-background-radius: 8; " +
                            "-fx-scale-x: 1.1; " +
                            "-fx-scale-y: 1.1;");
        }
    }

    @FXML
    private void onNavButtonHoverExit(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            container.setStyle(
                    "-fx-cursor: hand; " +
                            "-fx-padding: 8; " +
                            "-fx-background-color: transparent; " +
                            "-fx-scale-x: 1.0; " +
                            "-fx-scale-y: 1.0;");
        }
    }

    // OtherInfo moved here so it can be a static-like holder used by cells
    private static class OtherInfo {
        String other = null;
        boolean incoming = false;
    }
}
