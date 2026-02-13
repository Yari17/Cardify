package view.javafx;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import exception.ViewException;
import controller.CollectorHPController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.bean.CardBean;
import view.ICollectorHPView;
import javafx.scene.control.ScrollPane;
import javafx.scene.Parent;

import model.domain.enumerations.ViewPage;

/**
 * Rappresenta la Home Page per gli utenti Collezionisti in ambiente JavaFX.
 * Gestisce la navigazione principale, la ricerca di carte con filtri per set e
 * parole chiave,
 * e la visualizzazione dettagliata delle carte (Card Overview).
 */
public class FXCollectorHPView implements ICollectorHPView {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(FXCollectorHPView.class.getName());

    private CollectorHPController homepageController;

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
    private HBox searchBox;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchBtn;
    @FXML
    private Button filterBtn;

    private String selectedSetCode = null; // Currently selected set filter
    private javafx.scene.layout.TilePane cardGrid;

    @FXML
    private VBox profileBox;
    @FXML
    private ImageView avatarImage;
    @FXML
    private Label usernameLabel;

    @FXML
    private Pane contentArea;

    private Label pageTitle;
    private VBox homeContentBox;

    @FXML
    private javafx.scene.layout.FlowPane offerContainer;

    // mantiene un riferimento allo stage per poterlo chiudere
    private Stage stage;

    /**
     * Inizializza e visualizza la Home Page caricando il relativo file FXML.
     * Configura icone, pulsanti della navbar e le informazioni del profilo utente.
     */
    @Override
    public void display() {
        Runnable show = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectorHomepage.fxml"));
                loader.setController(this);
                VBox root = loader.load();

                setIcons();
                wireButtons();
                wireNavbarButtons(root);
                setUserInfo();
                setUserInfo();
                setupScene(root);

                // Ensure stage is shown
                if (this.stage != null && !this.stage.isShowing()) {
                    this.stage.show();
                    this.stage.toFront();
                }

            } catch (IOException e) {
                System.err.println("CRITICAL ERROR: Unable to load CollectorHomepage.fxml: " + e.getMessage());
                throw new ViewException("Failed to load Collector HP View", e);
            } catch (Exception e) {
                System.err.println("CRITICAL ERROR in display(): " + e.getMessage());
                throw new ViewException("Critical error in Collector HP View", e);
            }
        };

        if (Platform.isFxApplicationThread())
            show.run();
        else
            Platform.runLater(show);
    }

    /**
     * Carica e imposta le icone per i vari elementi della UI.
     * Gestisce eventuali eccezioni di caricamento in modo silenzioso.
     */
    private void setIcons() {
        try {
            homeIcon.setImage(new Image(getClass().getResourceAsStream("/icons/homepage.png")));
        } catch (Exception _) {
            // Ignora: L'icona potrebbe mancare
        }
        try {
            collectionIcon.setImage(new Image(getClass().getResourceAsStream("/icons/collection.png")));
        } catch (Exception _) {
            // Ignora
        }
        try {
            tradeIcon.setImage(new Image(getClass().getResourceAsStream("/icons/trade.png")));
        } catch (Exception _) {
            // Ignora
        }
        try {
            proposalIcon.setImage(new Image(getClass().getResourceAsStream("/icons/manageproposals.png")));
        } catch (Exception _) {
            // Ignora
        }
        try {
            logoutIcon.setImage(new Image(getClass().getResourceAsStream("/icons/logout.png")));
        } catch (Exception _) {
            // Ignora
        }
    }

    /**
     * Collega i pulsanti principali alle rispettive azioni del controller.
     */
    private void wireButtons() {
        if (homeBtn != null) {
            homeBtn.getStyleClass().add("active-nav");
        }
        setupButton(collectionBtn, () -> homepageController.goToCollection());
        setupButton(tradeBtn, () -> homepageController.goToTrade());
        setupButton(proposalBtn, () -> homepageController.manageProposals());
        setupButton(logoutBtn, () -> homepageController.logout());
        setupButton(searchBtn, this::refresh);
        setupButton(filterBtn, this::showSetFilterDialog);
    }

    /**
     * Helper per configurare un pulsante con un'azione.
     * Esegue l'azione solo se il controller è disponibile.
     * 
     * @param btn    Il pulsante da configurare.
     * @param action L'azione da eseguire al click.
     */
    private void setupButton(Button btn, Runnable action) {
        if (btn != null) {
            btn.setOnAction(e -> {
                if (homepageController != null) {
                    action.run();
                }
            });
        }
    }

    /**
     * Collega i pulsanti della navbar caricata dinamicamente.
     * 
     * @param root Il nodo radice contenente la navbar.
     */
    private void wireNavbarButtons(VBox root) {
        try {
            Button navCollection = (Button) root.lookup("#navCollectionBtn");
            if (navCollection != null) {
                navCollection.setOnAction(e -> handleNavCollection());
            }

            Button navHome = (Button) root.lookup("#navHomeBtn");
            if (navHome != null) {
                navHome.setOnAction(e -> {
                    if (homepageController != null)
                        homepageController.goToHomepage();
                });
            }

            Button navStore = (Button) root.lookup("#navStoreBtn");
            if (navStore != null) {
                navStore.setOnAction(e -> {
                    if (homepageController != null)
                        homepageController.goToTrade();
                });
            }

            Button navLogout = (Button) root.lookup("#navLogoutBtn");
            if (navLogout != null) {
                navLogout.setOnAction(e -> {
                    if (homepageController != null)
                        homepageController.logout();
                });
            }
        } catch (Exception _) {
            // Ignora errori di lookup
        }
    }

    private void handleNavCollection() {
        if (homepageController != null) {
            try {
                homepageController.goToCollection();
            } catch (Exception _) {
                // Ignore controller error
            }
        } else {
            fallbackOpenCollection();
        }
    }

    private void fallbackOpenCollection() {
        try {
            new view.javafx.FXCollectionView().show();
        } catch (Exception ex) {
            System.err.println("Fallback open collection failed: " + ex.getMessage());
        }
    }

    /**
     * Imposta le informazioni dell'utente (username e avatar) nella UI.
     */
    private void setUserInfo() {
        if (homepageController != null && homepageController.getSessionUser() != null) {
            usernameLabel.setText(homepageController.getSessionUser().getUsername());
            try {
                avatarImage.setImage(new Image(getClass().getResourceAsStream("/icons/collectorpp.png")));
            } catch (Exception _) {
                // Ignora avatar mancante
            }
        }
    }

    /**
     * Configura la scena principale, inclusa la griglia delle carte e l'area
     * centrale.
     * Cerca uno stage esistente o ne crea uno nuovo.
     * 
     * @param root Il nodo radice dell'interfaccia.
     */
    private void setupScene(VBox root) {
        // Inizializza Grid e ScrollPane
        cardGrid = new javafx.scene.layout.TilePane();
        cardGrid.setHgap(15);
        cardGrid.setVgap(15);
        cardGrid.setPrefColumns(5);
        cardGrid.setPadding(new javafx.geometry.Insets(15));

        ScrollPane scrollPane = new javafx.scene.control.ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Inizializza Titolo Pagina
        pageTitle = new Label("Le carte della community");
        pageTitle.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 0 0 10 15;");

        if (contentArea != null) {
            contentArea.getChildren().clear();
            // Crea un VBox wrapper per titolo + scrollpane
            homeContentBox = new VBox(10);
            homeContentBox.getChildren().addAll(pageTitle, scrollPane);
            // Binding dimensioni
            homeContentBox.prefWidthProperty().bind(contentArea.widthProperty());
            homeContentBox.prefHeightProperty().bind(contentArea.heightProperty());

            contentArea.getChildren().add(homeContentBox);

            // Re-bind scrollpane per essere flessibile dentro il VBox
            VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
            scrollPane.maxWidthProperty().bind(contentArea.widthProperty());
        }

        Scene scene = new Scene(root);
        java.net.URL cssUrl = getClass().getResource("/styles/style.css");
        if (cssUrl != null)
            scene.getStylesheets().add(cssUrl.toExternalForm());

        // Filtra per trovare la finestra principale dell'applicazione, evitando
        // dialoghi/alert
        Optional<Stage> existing = Window.getWindows().stream()
                .filter(Stage.class::isInstance)
                .map(w -> (Stage) w)
                .filter(s -> s.getModality() == javafx.stage.Modality.NONE)
                .filter(s -> s.getOwner() == null) // Main stage usually has no owner
                .findFirst();

        if (existing.isPresent()) {
            this.stage = existing.get();
            this.stage.setScene(scene);
            this.stage.setTitle("Collector Homepage");
            this.stage.show();
            this.stage.toFront();
        } else {
            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle("Collector Homepage");
            st.setWidth(1280);
            st.setHeight(800);
            st.centerOnScreen();
            st.show();
            this.stage = st;
        }

        // Caricamento iniziale
        refresh();
    }

    @Override
    public void refresh() {
        if (homepageController != null) {
            // Aggiorna il titolo basandosi sui filtri (pre-fetch per consistenza UI)
            String filter = searchField != null ? searchField.getText() : "";
            if ((filter == null || filter.isEmpty()) && (selectedSetCode == null)) {
                if (pageTitle != null)
                    pageTitle.setText("Le carte della community");
            } else {
                if (pageTitle != null)
                    pageTitle.setText("Risultato ricerca");
            }

            // Delega la ricerca al controller (Modello PULL)
            homepageController.performSearch();
        } else {
            LOGGER.warning("homepageController is null in refresh()");
        }
    }

    private static final String ALL_SETS_OPTION = "Tutti i set";

    /**
     * Mostra un dialog per filtrare le carte per set.
     * Recupera i set disponibili dal controller e aggiorna la vista alla selezione.
     */
    private void showSetFilterDialog() {
        if (homepageController == null)
            return;

        java.util.Map<String, String> sets = homepageController.loadAvailableSets();
        if (sets == null || sets.isEmpty()) {
            showError("Nessun set disponibile.");
            return;
        }

        // Crea lista opzioni: "Tutti" + nomi set
        java.util.List<String> options = new java.util.ArrayList<>();
        options.add(ALL_SETS_OPTION);
        options.addAll(sets.values());

        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(ALL_SETS_OPTION,
                options);
        dialog.setTitle("Filtra per Set");
        dialog.setHeaderText("Seleziona un set per filtrare le carte");
        dialog.setContentText("Set:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            if (ALL_SETS_OPTION.equals(selected)) {
                selectedSetCode = null;
            } else {
                // Delega al Controller il lookup inverso Map
                selectedSetCode = homepageController.getSetCodeByName(selected);
            }
            refresh();
        });
    }

    @Override
    public void showError(String errorMessage) {
        // Non implementato ancora
    }

    @Override
    public void setController(Object controller) {
        this.homepageController = (CollectorHPController) controller;
    }

    @Override
    public void close() {
        Runnable r = () -> {
            if (stage != null) {
                try {
                    stage.hide();
                } catch (Exception _) {
                    // Ignora errori chiusura stage
                }
            }
        };
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    @Override
    public String getCardNameFilter() {
        return searchField != null ? searchField.getText() : "";
    }

    @Override
    public String getSetFilter() {
        return selectedSetCode;
    }

    @Override
    public void displayCardList(List<CardBean> cardList) {
        Runnable r = () -> {
            if (cardGrid != null) {
                cardGrid.getChildren().clear();
                if (cardList == null || cardList.isEmpty()) {
                    Label empty = new Label("Nessuna carta trovata.");
                    empty.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                    cardGrid.getChildren().add(empty);
                } else {
                    for (CardBean c : cardList) {
                        cardGrid.getChildren().add(createCardNode(c));
                    }
                }
            }
        };
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    /**
     * Ripristina la vista della lista carte dalla schermata di dettaglio.
     */
    private void goBackToCardList() {
        if (contentArea != null && homeContentBox != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(homeContentBox);
            // Re-binding dimensioni per sicurezza
            homeContentBox.prefWidthProperty().bind(contentArea.widthProperty());
            homeContentBox.prefHeightProperty().bind(contentArea.heightProperty());
        }
        refresh();
    }

    /**
     * Crea un nodo grafico per visualizzare una carta nella griglia.
     * 
     * @param card Il bean della carta.
     * @return Il nodo UI (VBox) rappresentante la carta.
     */
    private javafx.scene.Node createCardNode(CardBean card) {
        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(5);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setStyle(
                "-fx-background-color: #2C3A4F; -fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0); -fx-cursor: hand;");
        box.setPrefSize(160, 240);

        // Handler click per aprire i dettagli della carta
        box.setOnMouseClicked(e -> {
            if (homepageController != null) {
                homepageController.openCardDetails(card);
            }
        });

        ImageView iv = new ImageView();
        iv.setFitWidth(140);
        iv.setFitHeight(196);
        iv.setPreserveRatio(true);

        String url = card.getImageUrl();
        if (url != null && !url.isEmpty()) {
            try {
                // Caricamento in background gestito da JavaFX
                Image img = new Image(url, true);
                iv.setImage(img);
            } catch (Exception _) {
                // ignora
            }
        }

        Label nameLbl = new Label(card.getName());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label ownerLbl = new Label("Owner: " + (card.getOwner() != null ? card.getOwner() : "?"));
        ownerLbl.setStyle("-fx-text-fill: #ababab; -fx-font-size: 10px;");

        box.getChildren().addAll(iv, nameLbl, ownerLbl);
        return box;
    }

    @Override
    public void displayCardOverview(CardBean card) {
        Runnable showOverview = () -> {
            try {
                // I dettagli della carta sono già stati recuperati dal controller prima di
                // chiamare questo metodo
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CardOverview.fxml"));
                HBox overviewRoot = loader.load();

                // Carica CSS
                java.net.URL cssUrl = getClass().getResource("/styles/card-overview.css");
                if (cssUrl != null) {
                    overviewRoot.getStylesheets().add(cssUrl.toExternalForm());
                }

                // Popola la UI
                populateCardOverviewData(overviewRoot, card);

                // Configura pulsanti
                setupOverviewButtons(overviewRoot, card);

                // Sostituisci area contenuto
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(overviewRoot);
                    // Binding per riempire l'area
                    overviewRoot.prefWidthProperty().bind(contentArea.widthProperty());
                    overviewRoot.prefHeightProperty().bind(contentArea.heightProperty());
                }
            } catch (IOException e) {
                LOGGER.severe("Impossibile caricare CardOverview.fxml: " + e.getMessage());
            }
        };

        if (Platform.isFxApplicationThread()) {
            showOverview.run();
        } else {
            Platform.runLater(showOverview);
        }
    }

    /**
     * Popola i campi della schermata di dettaglio con i dati della carta.
     * 
     * @param overviewRoot Il nodo radice della vista dettaglio.
     * @param card         Il bean della carta.
     */
    private void populateCardOverviewData(HBox overviewRoot, CardBean card) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(getClass().getName());

        // Log iniziale
        logger.info(() -> "=== Popolamento Card Overview ===");
        logger.info(() -> "Carta: " + card.getName());
        logger.info(() -> "ID: " + card.getId());
        logger.info(() -> "SetID: " + card.getSetId());
        logger.info(() -> "Owner: " + card.getOwner());
        logger.info(() -> "ImageURL presente: " + (card.getImageUrl() != null && !card.getImageUrl().isEmpty()));
        logger.info(() -> "Dettagli (ICardDetails) presenti: " + (card.getDetails() != null));

        // Ottieni elementi FXML
        ImageView cardImage = (ImageView) overviewRoot.lookup("#cardImage");
        ScrollPane detailsScroll = (ScrollPane) overviewRoot.lookup("#detailsScroll");
        VBox detailsContent = (VBox) detailsScroll.getContent();

        Label cardNameLabel = (Label) detailsContent.lookup("#cardNameLabel");
        Label ownerLabel = (Label) detailsContent.lookup("#ownerLabel");

        // Immagine
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            cardImage.setImage(new Image(card.getImageUrl(), true));
            logger.info(() -> "Immagine caricata da URL: " + card.getImageUrl());
        } else {
            logger.warning("URL immagine mancante per la carta: " + card.getName());
        }
        cardNameLabel.setText(card.getName());
        ownerLabel.setText(card.getOwner() != null ? card.getOwner() : "Sconosciuto");

        // Dettagli
        if (card.getDetails() instanceof model.domain.PokemonCardDetails details) {
            logger.info(() -> "Dettagli Pokemon trovati - popolamento campi specifici...");
            populatePokemonDetails(details, detailsContent);
        } else {
            handleMissingDetails(card, detailsContent, logger);
        }
        logger.info(() -> "=== Fine popolamento Card Overview ===");
    }

    private void handleMissingDetails(CardBean card, VBox detailsContent, java.util.logging.Logger logger) {
        logger.warning(() -> "DETTAGLI MANCANTI per " + card.getName()
                + " - I campi specifici (HP, tipo, stage) non saranno visualizzati. Tipo dettagli: "
                + (card.getDetails() != null ? card.getDetails().getClass().getSimpleName() : "null"));

        clearPokemonDetailsFields(detailsContent);
    }

    private void clearPokemonDetailsFields(VBox detailsContent) {
        Label hpLabel = (Label) detailsContent.lookup("#hpLabel");
        HBox typeContainer = (HBox) detailsContent.lookup("#typeContainer");
        Label stageLabel = (Label) detailsContent.lookup("#stageLabel");

        if (hpLabel != null)
            hpLabel.setText("");
        if (typeContainer != null)
            typeContainer.getChildren().clear();
        if (stageLabel != null)
            stageLabel.setVisible(false);
    }

    private void populatePokemonDetails(model.domain.PokemonCardDetails details, Parent overviewRoot) {
        // Lookup all'interno dell'helper per evitare troppi argomenti
        Label hpLabel = (Label) overviewRoot.lookup("#hpLabel");
        HBox typeContainer = (HBox) overviewRoot.lookup("#typeContainer");
        if (hpLabel != null)
            hpLabel.setText(details.getHp() != null ? details.getHp() + " HP" : "");

        if (details.getPrimaryType() != null && typeContainer != null) {
            typeContainer.getChildren().clear();
            ImageView typeIcon = createEnergyIcon(details.getPrimaryType());
            if (typeIcon != null) {
                typeContainer.getChildren().add(typeIcon);
            }
        }

        populateBasicDetails(details, overviewRoot);

        HBox weaknessContainer = (HBox) overviewRoot.lookup("#weaknessContainer");
        VBox attacksContainer = (VBox) overviewRoot.lookup("#attacksContainer");

        if (weaknessContainer != null)
            populateWeaknessIcons(weaknessContainer, details.getWeaknesses());
        if (attacksContainer != null)
            buildAttackBoxes(attacksContainer, details.getAttacks());
    }

    /**
     * Popola i dettagli di base (stage, rarità, illustratore, set, ritirata).
     */
    private void populateBasicDetails(model.domain.PokemonCardDetails details, Parent overviewRoot) {
        Label stageLabel = (Label) overviewRoot.lookup("#stageLabel");
        Label rarityLabel = (Label) overviewRoot.lookup("#rarityLabel");
        Label illustratorLabel = (Label) overviewRoot.lookup("#illustratorLabel");
        Label setNameLabel = (Label) overviewRoot.lookup("#setNameLabel");
        Label retreatLabel = (Label) overviewRoot.lookup("#retreatLabel");

        if (stageLabel != null)
            stageLabel.setText(details.getStage() != null ? details.getStage() : "");
        if (rarityLabel != null)
            rarityLabel.setText(details.getRarity() != null ? details.getRarity() : "N/A");
        if (illustratorLabel != null)
            illustratorLabel.setText(details.getIllustrator() != null ? details.getIllustrator() : "N/A");
        if (setNameLabel != null)
            setNameLabel.setText(details.getSetName() != null ? details.getSetName() : "N/A");
        if (retreatLabel != null)
            retreatLabel.setText(details.getRetreat() != null ? String.valueOf(details.getRetreat()) : "N/A");
    }

    private void setupOverviewButtons(HBox overviewRoot, CardBean card) {
        Button proposeTradeBtn = (Button) overviewRoot.lookup("#proposeTradeBtn");
        Button closeOverviewBtn = (Button) overviewRoot.lookup("#closeBtn");
        Button backBtn = (Button) overviewRoot.lookup("#backBtn");

        if (proposeTradeBtn != null)
            proposeTradeBtn.setOnAction(e -> onProposeTrade(card));
        if (closeOverviewBtn != null)
            closeOverviewBtn.setOnAction(e -> goBackToCardList());
        if (backBtn != null)
            backBtn.setOnAction(e -> goBackToCardList());
    }

    /**
     * Costruisce i box grafici per la lista degli attacchi.
     */
    private void buildAttackBoxes(VBox container, java.util.List<java.util.Map<String, Object>> attacks) {
        if (attacks == null || attacks.isEmpty()) {
            return;
        }
        for (java.util.Map<String, Object> attack : attacks) {
            VBox attackBox = createAttackBox(attack);
            container.getChildren().add(attackBox);
        }
    }

    private VBox createAttackBox(java.util.Map<String, Object> attack) {
        VBox attackBox = new VBox(8);
        attackBox.getStyleClass().add("attack-box");

        // Header: Costo + Nome + Danno
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Costo - display come icone energia
        @SuppressWarnings("unchecked")
        java.util.List<String> cost = (java.util.List<String>) attack.get("cost");
        HBox costBox = new HBox(2);
        costBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (cost != null) {
            for (String energyType : cost) {
                ImageView icon = createEnergyIcon(energyType);
                if (icon != null) {
                    costBox.getChildren().add(icon);
                }
            }
        }

        // Nome
        String name = (String) attack.get("name");
        Label nameLabel = new Label(name != null ? name : "?");
        nameLabel.getStyleClass().add("attack-name");

        // Danno (se presente)
        Object dmg = attack.get("damage");
        if (dmg != null && !dmg.toString().isEmpty()) {
            Label dmgLabel = new Label(dmg.toString());
            dmgLabel.getStyleClass().add("attack-damage");
            header.getChildren().addAll(costBox, nameLabel, new javafx.scene.layout.Region(), dmgLabel);
            HBox.setHgrow(header.getChildren().get(2), javafx.scene.layout.Priority.ALWAYS);
        } else {
            header.getChildren().addAll(costBox, nameLabel);
        }

        attackBox.getChildren().add(header);

        // Effetto
        String effect = (String) attack.get("effect");
        if (effect != null && !effect.isEmpty()) {
            Label effectLabel = new Label(effect);
            effectLabel.getStyleClass().add("attack-effect");
            effectLabel.setWrapText(true);
            attackBox.getChildren().add(effectLabel);
        }
        return attackBox;
    }

    @Override
    public void onProposeTrade(CardBean card) {
        if (homepageController != null) {
            // Passa la carta target tramite i dati temporanei dell'ApplicationController
            homepageController.getApplicationController().setTemporaryData("PROPOSAL_TARGET_CARD", card);
            homepageController.getApplicationController().navigateTO(ViewPage.PROPOSAL);
        }
    }

    // Helper: Crea icona Energia
    private ImageView createEnergyIcon(String type) {
        if (type == null)
            return null;

        String fileName;
        switch (type.toLowerCase()) {
            case "grass" -> fileName = "Icona_Erba_GCC.png";
            case "fire" -> fileName = "Icona_Fuoco_GCC.png";
            case "water" -> fileName = "Icona_Acqua_GCC.png";
            case "lightning" -> fileName = "Icona_Lampo_GCC.png";
            case "psychic" -> fileName = "Icona_Psiche_GCC.png";
            case "fighting" -> fileName = "Icona_Lotta_GCC.png";
            case "darkness" -> fileName = "Icona_Oscurità_GCC.png";
            case "metal" -> fileName = "Icona_Metallo_GCC.png";
            case "fairy" -> fileName = "Icona_Folletto_GCC.png";
            case "colorless" -> fileName = "Icona_Incolore_GCC.png";
            case "dragon" -> fileName = "Icona_Drago_GCC.png";
            case "rainbow" -> fileName = "Icona_Arcobaleno_GCC.png";
            default -> fileName = type.toLowerCase() + ".png";
        }

        String path = "/icons/" + fileName;
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is == null)
                return null;
            ImageView iv = new ImageView(new Image(is));
            iv.setFitWidth(16);
            iv.setFitHeight(16);
            return iv;
        } catch (Exception _) {
            // Icona non trovata
            return null;
        }
    }

    // Helper: Popola Debolezza
    private void populateWeaknessIcons(HBox container, java.util.List<java.util.Map<String, String>> weaknesses) {
        if (container == null)
            return;
        container.getChildren().clear();
        if (weaknesses == null)
            return;

        for (java.util.Map<String, String> w : weaknesses) {
            String type = w.get("type");
            ImageView iv = createEnergyIcon(type);
            if (iv != null) {
                container.getChildren().add(iv);
            }
        }
    }
}
