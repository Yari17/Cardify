package view.managetrade;

import controller.ManageTradeController;
import javafx.fxml.FXML;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import model.bean.ProposalBean;

import java.util.List;
import java.util.logging.Logger;

public class FXManageTradeView implements IManageTradeView {
    private static final Logger LOGGER = Logger.getLogger(FXManageTradeView.class.getName());

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
    private ManageTradeController manageController;
    private Stage stage;

    public FXManageTradeView() {
        // FXML will inject fields
    }

    @FXML
    public void initialize() {
        if (pendingTradesList != null) setupCellFactory(pendingTradesList);
        if (concludedTradesList != null) setupCellFactory(concludedTradesList);
    }

    private void setupCellFactory(ListView<ProposalBean> listView) {
        boolean readOnly = listView == concludedTradesList;
        listView.setCellFactory(lv -> new TradeListCell(readOnly));
    }

    private class TradeListCell extends javafx.scene.control.ListCell<ProposalBean> {
        private final boolean readOnly;

        TradeListCell(boolean readOnly) {
            this.readOnly = readOnly;
        }

        @Override
        protected void updateItem(ProposalBean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().remove("trade-list-cell");
                return;
            }

            OtherInfo info = determineOtherAndIncoming(item);

            javafx.scene.Node cellGraphic = createCellGraphic(item, info);
            setText(null);
            setGraphic(cellGraphic);
            if (!getStyleClass().contains("trade-list-cell")) getStyleClass().add("trade-list-cell");

            // add status-specific classes from ProposalBean.status
            String ps = item.getStatus();
            getStyleClass().removeAll("rejected", "expired");
            if (ps != null) {
                if (ps.equalsIgnoreCase("REJECTED")) getStyleClass().add("rejected");
                else if (ps.equalsIgnoreCase("EXPIRED")) getStyleClass().add("expired");
            }
        }

        private javafx.scene.Node createCellGraphic(ProposalBean item, OtherInfo info) {
            // Build a richer cell UI: left icon, center text block (title + subtitle), right actions + status badge
            HBox root = new HBox(12);
            root.getStyleClass().add("trade-list-cell");
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

            // subtitle shows participants and meeting info
            String participants = (item.getFromUser() == null ? "?" : item.getFromUser()) + " → " + (item.getToUser() == null ? "?" : item.getToUser());
            Label subtitle = new Label(participants);
            subtitle.getStyleClass().add("cell-secondary");

            // small meeting info (date/place) as secondary
            String meet = (item.getMeetingDate() != null ? item.getMeetingDate() : "TBD") + " • " + (item.getMeetingPlace() != null ? item.getMeetingPlace() : "TBD");
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
                case "PENDING" -> badge.getStyleClass().add("badge-pending");
                case "REJECTED" -> badge.getStyleClass().add("badge-rejected");
                case "EXPIRED" -> badge.getStyleClass().add("badge-expired");
                default -> badge.getStyleClass().add("badge-unknown");
            }

            // Actions container
            HBox actions = new HBox(8);
            actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            Button viewBtn = new Button("View");
            viewBtn.getStyleClass().add("button-view");
            viewBtn.setOnAction(evt -> showProposalDetails(item));
            actions.getChildren().add(viewBtn);

            boolean showIncomingActions = !readOnly && info.incoming && status.equals("PENDING");
            if (showIncomingActions) {
                Button acceptBtn = new Button("Accept");
                acceptBtn.getStyleClass().add("button-accept");
                acceptBtn.setOnAction(e -> onAcceptTradeProposal(item.getProposalId()));

                Button declineBtn = new Button("Decline");
                declineBtn.getStyleClass().add("button-decline");
                declineBtn.setOnAction(e -> onDeclineTradeProposal(item.getProposalId()));

                actions.getChildren().addAll(acceptBtn, declineBtn);
            }

            root.getChildren().addAll(badge, actions);

            return root;
        }

        private ImageView loadDirectionIcon(boolean incoming) {
            try {
                String iconPath = incoming ? "/icons/receive.png" : "/icons/sent.png";
                java.net.URL res = getClass().getResource(iconPath);
                if (res != null) {
                    Image img = new Image(res.toExternalForm(), 20, 20, true, true);
                    ImageView iv = new ImageView(img);
                    iv.getStyleClass().add("trade-direction-icon");
                    return iv;
                }
            } catch (Exception _) {
                // ignore icon load failures
            }
            return null;
        }

        // isMeetingToday not required in ManageTradeView cell rendering
    }

    // Helper to reduce complexity in updateItem
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

    private static class OtherInfo {
        String other = null;
        boolean incoming = false;
    }

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
        LOGGER.severe("Error: " + errorMessage);
    }


    @Override
    public void setManageController(ManageTradeController controller) {
        this.manageController = controller;
    }

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
    }

    @Override
    public void onAcceptTradeProposal(String id) {
        if (id == null || manageController == null) return;
        boolean ok = manageController.acceptProposal(id);
        if (ok) manageController.loadAndDisplayTrades(this);
    }

    @Override
    public void onCancelTradeProposal(String id) {
        if (id == null || manageController == null) return;
        boolean ok = manageController.declineProposal(id);
        if (ok) manageController.loadAndDisplayTrades(this);
    }

    @Override
    public void onDeclineTradeProposal(String id) {
        if (id == null || manageController == null) return;
        boolean ok = manageController.declineProposal(id);
        if (ok) manageController.loadAndDisplayTrades(this);
    }

    @Override
    public void onTradeClick(String id) {
        if (id == null || manageController == null) return;
        manageController.initiateTrade(id);
    }

    @Override
    public void onTradeNowClick(String id) {
        if (id == null || manageController == null) return;
        manageController.initiateTrade(id);
    }

    @Override
    public void displayTrades(List<ProposalBean> pending, List<ProposalBean> scheduled) {
        // keep signature for interface compatibility but ignore scheduled (now shown in LiveTrade view)
        javafx.application.Platform.runLater(() -> {
            if (pendingTradesList != null) {
                pendingTradesList.getItems().clear();
                if (pending != null) pendingTradesList.getItems().addAll(pending);
            }
            if (concludedTradesList != null) {
                concludedTradesList.getItems().clear();
                if (scheduled != null) concludedTradesList.getItems().addAll(scheduled);
            }
        });
    }


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
            root.setStyle("-fx-background-color: #0F1720; -fx-padding: 14;");

            // ----- Header -----
            HBox header = new HBox(12);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label id = new Label("Proposal: " + item.getProposalId());
            id.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            String statusText = item.getStatus() != null ? item.getStatus() : "UNKNOWN";
            Label status = new Label(statusText);
            // Simple badge styles depending on status
            String badgeStyle = "-fx-padding: 4 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-text-fill: white;";
            switch (statusText.toUpperCase()) {
                case "ACCEPTED" -> badgeStyle = badgeStyle + " -fx-background-color: #10B981;"; // green
                case "PENDING" -> badgeStyle = badgeStyle + " -fx-background-color: #F59E0B;"; // amber
                case "REJECTED" -> badgeStyle = badgeStyle + " -fx-background-color: #EF4444;"; // red
                case "EXPIRED" -> badgeStyle = badgeStyle + " -fx-background-color: #6B7280;"; // gray
                default -> badgeStyle = badgeStyle + " -fx-background-color: #374151;";
            }
            status.setStyle(badgeStyle);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            header.getChildren().addAll(id, spacer, status);
            root.setTop(header);

            // ----- Body (center) -----
            VBox centerBox = new VBox(12);
            centerBox.setPadding(new Insets(10));

            String proposer = p != null && p.getProposerId() != null ? p.getProposerId() : item.getFromUser();
            String receiver = p != null && p.getReceiverId() != null ? p.getReceiverId() : item.getToUser();
            Label participants = new Label("Proposer: " + (proposer != null ? proposer : "<unknown>") + "   •   Receiver: " + (receiver != null ? receiver : "<unknown>"));
            participants.setStyle("-fx-text-fill: #CAD5E0; -fx-font-size: 13px;");

            String meetingDate = item.getMeetingDate() != null ? item.getMeetingDate() : (p != null ? p.getMeetingDate() : "TBD");
            String meetingPlace = item.getMeetingPlace() != null ? item.getMeetingPlace() : (p != null ? p.getMeetingPlace() : "TBD");
            Label meeting = new Label("Meeting: " + meetingDate + " @ " + meetingPlace);
            meeting.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

            centerBox.getChildren().addAll(participants, meeting);

            // ----- Offered and Requested sections -----
            Label offeredLabel = new Label("Offered");
            offeredLabel.setStyle("-fx-text-fill: #A7F3D0; -fx-font-weight: bold; -fx-font-size: 14px;");
            javafx.scene.layout.FlowPane offeredPane = new javafx.scene.layout.FlowPane(10, 10);
            offeredPane.setPrefWrapLength(700);

            Label requestedLabel = new Label("Requested");
            requestedLabel.setStyle("-fx-text-fill: #BFDBFE; -fx-font-weight: bold; -fx-font-size: 14px;");
            javafx.scene.layout.FlowPane requestedPane = new javafx.scene.layout.FlowPane(10, 10);
            requestedPane.setPrefWrapLength(700);

            // Populate offered cards
            if (p != null && p.getCardsOffered() != null && !p.getCardsOffered().isEmpty()) {
                for (model.domain.Card c : p.getCardsOffered()) {
                    if (c == null) continue;
                    VBox cardBox = new VBox(6);
                    cardBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 8;");
                    cardBox.setPrefWidth(150);

                    ImageView iv = new ImageView();
                    iv.setFitWidth(120);
                    iv.setFitHeight(80);
                    iv.setPreserveRatio(true);
                    try {
                        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
                            Image img = new Image(c.getImageUrl(), 120, 80, true, true);
                            if (!img.isError()) iv.setImage(img);
                        }
                    } catch (Exception ex) {
                        LOGGER.fine(() -> "Failed to load card image in proposal dialog: " + ex.getMessage());
                    }

                    Label name = new Label(c.getName() != null ? c.getName() : c.getId());
                    name.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                    Label qty = new Label("x" + c.getQuantity());
                    qty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

                    cardBox.getChildren().addAll(iv, name, qty);
                    offeredPane.getChildren().add(cardBox);
                }
            }

            // Populate requested cards
            if (p != null && p.getCardsRequested() != null && !p.getCardsRequested().isEmpty()) {
                for (model.domain.Card c : p.getCardsRequested()) {
                    if (c == null) continue;
                    VBox cardBox = new VBox(6);
                    cardBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 8;");
                    cardBox.setPrefWidth(150);

                    ImageView iv = new ImageView();
                    iv.setFitWidth(120);
                    iv.setFitHeight(80);
                    iv.setPreserveRatio(true);
                    try {
                        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
                            Image img = new Image(c.getImageUrl(), 120, 80, true, true);
                            if (!img.isError()) iv.setImage(img);
                        }
                    } catch (Exception ex) {
                        LOGGER.fine(() -> "Failed to load card image in proposal dialog: " + ex.getMessage());
                    }

                    Label name = new Label(c.getName() != null ? c.getName() : c.getId());
                    name.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                    Label qty = new Label("x" + c.getQuantity());
                    qty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

                    cardBox.getChildren().addAll(iv, name, qty);
                    requestedPane.getChildren().add(cardBox);
                }
            }

            // Sections container
            VBox sections = new VBox(8);
            sections.getChildren().addAll(offeredLabel, offeredPane, requestedLabel, requestedPane);
            centerBox.getChildren().add(sections);

            // Wrap in scroll pane
            ScrollPane sp = new ScrollPane(centerBox);
            sp.setFitToWidth(true);
            sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            root.setCenter(sp);

            // ----- Footer -----
            HBox footer = new HBox();
            footer.setPadding(new Insets(10, 0, 0, 0));
            footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("button-view");
            closeBtn.setOnAction(evt -> dialog.close());
            footer.getChildren().add(closeBtn);
            root.setBottom(footer);

            // Scene
            Scene scene = new Scene(root, 840, 520);
            try {
                java.net.URL res = getClass().getResource("/styles/theme.css");
                if (res != null) scene.getStylesheets().add(res.toExternalForm());
            } catch (Exception ignored) {
                LOGGER.fine(() -> "Unable to apply theme stylesheet in proposal dialog: " + ignored.getMessage());
            }

            dialog.setScene(scene);
            dialog.showAndWait();
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
}
