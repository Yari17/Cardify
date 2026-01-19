package view.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Objects;

import controller.CollectorHPController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.bean.CardBean;
import view.ICollectorHPView;

public class FXCollectorHPView implements ICollectorHPView {
    private static final Logger LOGGER = Logger.getLogger(FXCollectorHPView.class.getName());
    private static final String POPULAR_CARDS_LABEL = "Popular Cards (" + config.AppConfig.DEFAULT_SET_ID + ")";
    private static final String NO_IMAGE_STYLE = "-fx-text-fill: #9CA3AF; -fx-font-size: 12px;";
    private static final String HOVER_STYLE = "-fx-background-color: rgba(41, 182, 246, 0.2); -fx-background-radius: 8; -fx-scale-x: 1.1; -fx-scale-y: 1.1;";
    private static final String NORMAL_STYLE = "-fx-cursor: hand; -fx-padding: 8; -fx-background-color: transparent; -fx-scale-x: 1.0; -fx-scale-y: 1.0;";
    private static final String VARIANT_LABEL_STYLE = "-fx-background-color: #AB47BC; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px;";
    private static final String EFFECT_LITERAL = "effect";
    private static final String EXCEPTION_LITERAL = "Exception";

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> gameComboBox;

    @FXML 
    private ComboBox<String> expansionComboBox;

    @FXML
    private ComboBox<String> languageComboBox;

    @FXML 
    private CheckBox mintCheckBox;

    @FXML
    private CheckBox nearMintCheckBox;

    @FXML
    private CheckBox excellentCheckBox;

    @FXML
    private CheckBox goodCheckBox;

    @FXML
    private CheckBox playedCheckBox;

    @FXML 
    private Slider priceSlider;

    @FXML 
    private FlowPane cardsFlowPane;

    @FXML
    private Button applyFiltersButton;

    @FXML
    private VBox initialViewBox;

    @FXML
    private VBox cardsViewBox;

    @FXML
    private Button viewPopularCardsButton;

    @FXML
    private MenuButton setFilterButton;

    @FXML
    private Button homepageButton;

    @FXML
    private Button collectionButton;

    @FXML
    private Button tradeButton;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox logoutButtonContainer;

    private CollectorHPController controller;
    private Stage stage;
    private Map<String, String> setsIdToNameMap; 

    
    private List<CardBean> allCards;
    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 20;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label pageLabel;

    @FXML
    private void initialize() {
        if (languageComboBox != null) {
            languageComboBox.getItems().addAll("All", "English", "Italian", "Japanese", "French", "German");
            languageComboBox.setValue("All");
        }
    }

    @Override
    public void setController(CollectorHPController controller) {
        this.controller = controller;
        if (controller != null) {
            showWelcomeMessage(controller.getUsername());
        }
        LOGGER.info("Controller set in FXCollectorHPView");
    }

    @Override
    public void display() {
        if (stage != null) {
            stage.show();
        } else {
            LOGGER.warning("Stage not set, cannot display");
        }
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }

    @Override
    public void showWelcomeMessage(String username) {
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }

    @Override
    public void showCardOverview(CardBean card) {
        Dialog<Void> dialog = createBaseDialog();
        VBox content = createDialogContent(card);
        ScrollPane scrollPane = createDialogScrollPane(content);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }

    private Dialog<Void> createBaseDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Dettagli Carta");
        dialog.initOwner(stage);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E2530;");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setMaxHeight(700);
        return dialog;
    }

    private ScrollPane createDialogScrollPane(VBox content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setContent(content);
        return scrollPane;
    }

    private VBox createDialogContent(CardBean card) {
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-background-color: #1E2530;");

        
        try {
            String owner = card != null ? card.getOwner() : "<null>";
            boolean tradable = card != null && card.isTradable();
            String username = (controller != null) ? controller.getUsername() : "<no-controller>";
            LOGGER.log(java.util.logging.Level.INFO, "Opening card dialog - id: {0}, owner: {1}, tradable: {2}, currentUser: {3}",
                    new Object[] { card != null ? card.getId() : "<null>", owner, tradable, username });
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.FINE, "Failed to log dialog diagnostics: {0}", ex.getMessage());
        }

        content.getChildren().addAll(
                createTitleLabel(card),
                createGameTypeLabel(card),
                createImageContainer(card),
                createIdLabel(card));

        if (card instanceof model.bean.PokemonCardBean pokemonCard) {
            content.getChildren().add(createPokemonDetailsBox(pokemonCard));
        }

        if (card.isTradable()) {
            content.getChildren().add(createTradableInfo());
        }

        
        if (card.getOwner() != null && !card.getOwner().isEmpty() && controller != null
                && !controller.getUsername().trim().equalsIgnoreCase(card.getOwner().trim())
                && card.isTradable()) {
            javafx.scene.control.Button negotiateButton = new javafx.scene.control.Button("Proponi scambio");
            negotiateButton.setOnAction(evt -> {
                try {
                    controller.openNegotiation(card);
                } catch (Exception ex) {
                    LOGGER.warning("Failed to open negotiation: " + ex.getMessage());
                }
            });
            content.getChildren().add(negotiateButton);
        }

        return content;
    }

    private Label createTitleLabel(CardBean card) {
        Label titleLabel = new Label(card.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        return titleLabel;
    }

    private Label createGameTypeLabel(CardBean card) {
        Label gameTypeLabel = new Label(card.getGameType().toString());
        gameTypeLabel.setStyle("-fx-background-color: #29B6F6; -fx-text-fill: white; " +
                "-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
        return gameTypeLabel;
    }

    private Label createIdLabel(CardBean card) {
        Label idLabel = new Label("ID: " + card.getId());
        idLabel.setStyle(NO_IMAGE_STYLE);
        return idLabel;
    }

    private VBox createImageContainer(CardBean card) {
        VBox imageContainer = new VBox(5);
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);

        ImageView cardImageView = new ImageView();
        cardImageView.setFitWidth(300);
        cardImageView.setFitHeight(420);
        cardImageView.setPreserveRatio(true);

        boolean imageLoaded = false;
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            try {
                Image cardImage = new Image(card.getImageUrl(), true);
                if ( !cardImage.isError()) {
                    cardImageView.setImage(cardImage);
                    imageLoaded = true;
                }
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.WARNING, "Failed to load card image in dialog: {0} -> {1}",
                        new Object[]{card.getImageUrl(), ex.getMessage()});
                LOGGER.log(java.util.logging.Level.FINER, EXCEPTION_LITERAL, ex);
            }
        }

        if (!imageLoaded) {
            setupPlaceholderImage(cardImageView, imageContainer);
        } else {
            imageContainer.getChildren().add(cardImageView);
        }
        return imageContainer;
    }

    private void setupPlaceholderImage(ImageView cardImageView, VBox imageContainer) {
        try {
            Image placeholderImage = new Image(getClass().getResourceAsStream("/icons/nocardimage.svg"));
            cardImageView.setImage(placeholderImage);
            cardImageView.setFitWidth(200);
            cardImageView.setFitHeight(280);

            Label noImageLabel = new Label("Sorry, no image available");
            noImageLabel.setStyle(NO_IMAGE_STYLE + " -fx-font-style: italic;");
            imageContainer.getChildren().addAll(cardImageView, noImageLabel);
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to load placeholder image: {0}", ex.getMessage());
            LOGGER.log(java.util.logging.Level.FINER, EXCEPTION_LITERAL, ex);
            imageContainer.getChildren().add(cardImageView);
        }
    }

    private VBox createPokemonDetailsBox(model.bean.PokemonCardBean pokemonCard) {
        VBox detailsBox = new VBox(10);
        detailsBox
                .setStyle("-fx-background-color: rgba(41, 182, 246, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

        addIfNotNull(detailsBox, createHpTypeBox(pokemonCard));
        addIfNotNull(detailsBox, createStageBox(pokemonCard));
        addIfNotNull(detailsBox, createLabel("⭐ Rarità: " + pokemonCard.getRarity(),
                "-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;"));
        addIfNotNull(detailsBox,
                createLabel("📦 Set: " + pokemonCard.getSetName(), "-fx-text-fill: #9CA3AF; -fx-font-size: 13px;"));
        addIfNotNull(detailsBox, createLabel("🎨 Illustratore: " + pokemonCard.getIllustrator(),
                NO_IMAGE_STYLE + " -fx-font-style: italic;"));
        addIfNotNull(detailsBox, createLabel("📋 Categoria: " + pokemonCard.getCategory(),
                NO_IMAGE_STYLE));

        
        addIfNotNull(detailsBox, createWeaknessRetreatBox(pokemonCard));
        addIfNotNull(detailsBox, createDescriptionBox(pokemonCard));
        addIfNotNull(detailsBox, createAttacksBox(pokemonCard));
        addIfNotNull(detailsBox, createLegalBox(pokemonCard));
        addIfNotNull(detailsBox, createVariantsBox(pokemonCard));

        return detailsBox;
    }

    private void addIfNotNull(VBox parent, javafx.scene.Node node) {
        if (node != null) {
            parent.getChildren().add(node);
        }
    }

    private Label createLabel(String text, String style) {
        if (text == null || text.contains("null"))
            return null;
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private HBox createHpTypeBox(model.bean.PokemonCardBean pokemonCard) {
        if (pokemonCard.getHp() == null && (pokemonCard.getTypes() == null || pokemonCard.getTypes().isEmpty()))
            return null;

        HBox box = new HBox(20);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        if (pokemonCard.getHp() != null) {
            box.getChildren().add(createLabel("❤️ HP: " + pokemonCard.getHp(),
                    "-fx-text-fill: #EF5350; -fx-font-size: 16px; -fx-font-weight: bold;"));
        }
        if (pokemonCard.getTypes() != null && !pokemonCard.getTypes().isEmpty()) {
            box.getChildren().add(createLabel("⚡ " + String.join(", ", pokemonCard.getTypes()),
                    "-fx-text-fill: #FFA726; -fx-font-size: 16px; -fx-font-weight: bold;"));
        }
        return box;
    }

    private HBox createStageBox(model.bean.PokemonCardBean pokemonCard) {
        if (pokemonCard.getStage() == null && pokemonCard.getEvolveFrom() == null)
            return null;

        HBox box = new HBox(15);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        if (pokemonCard.getStage() != null) {
            box.getChildren()
                    .add(createLabel("Stage: " + pokemonCard.getStage(), "-fx-text-fill: white; -fx-font-size: 14px;"));
        }
        if (pokemonCard.getEvolveFrom() != null) {
            box.getChildren().add(createLabel("Evolve da: " + pokemonCard.getEvolveFrom(),
                    "-fx-text-fill: #66BB6A; -fx-font-size: 14px;"));
        }
        return box;
    }

    private VBox createWeaknessRetreatBox(model.bean.PokemonCardBean pokemonCard) {
        boolean hasWeakness = pokemonCard.getWeaknesses() != null && !pokemonCard.getWeaknesses().isEmpty();
        boolean hasRetreat = pokemonCard.getRetreat() != null;

        if (!hasWeakness && !hasRetreat)
            return null;

        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: rgba(255, 152, 0, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

        if (hasWeakness) {
            box.getChildren().add(
                    createLabel("⚠️ Debolezze", "-fx-text-fill: #FF9800; -fx-font-size: 14px; -fx-font-weight: bold;"));
            for (Map<String, String> weakness : pokemonCard.getWeaknesses()) {
                box.getChildren().add(createLabel("🔸 " + weakness.get("type") + " " + weakness.get("value"),
                        "-fx-text-fill: white; -fx-font-size: 13px;"));
            }
        }

        if (hasRetreat) {
            String retreatText = "🏃 Costo Ritirata: " + "⚪ ".repeat(pokemonCard.getRetreat()) + "("
                    + pokemonCard.getRetreat() + ")";
            box.getChildren().add(
                    createLabel(retreatText, "-fx-text-fill: #FFB74D; -fx-font-size: 13px; -fx-font-weight: bold;"));
        }
        return box;
    }

    private VBox createDescriptionBox(model.bean.PokemonCardBean pokemonCard) {
        String desc = pokemonCard.getDescription();
        if (desc == null || desc.isEmpty())
            return null;

        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: rgba(76, 175, 80, 0.1); -fx-padding: 15; -fx-background-radius: 10;");
        box.getChildren().add(
                createLabel("📖 Descrizione", "-fx-text-fill: #66BB6A; -fx-font-size: 14px; -fx-font-weight: bold;"));

        Label descLabel = createLabel(desc, "-fx-text-fill: white; -fx-font-size: 13px; -fx-wrap-text: true;");
        descLabel.setMaxWidth(350);
        descLabel.setWrapText(true);
        box.getChildren().add(descLabel);
        return box;
    }

    private VBox createAttacksBox(model.bean.PokemonCardBean pokemonCard) {
        if (pokemonCard.getAttacks() == null || pokemonCard.getAttacks().isEmpty())
            return null;

        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: rgba(239, 83, 80, 0.1); -fx-padding: 15; -fx-background-radius: 10;");
        box.getChildren().add(createLabel("⚔️ Attacchi (" + pokemonCard.getAttacks().size() + ")",
                "-fx-text-fill: #EF5350; -fx-font-size: 14px; -fx-font-weight: bold;"));

        for (Map<String, Object> attack : pokemonCard.getAttacks()) {
            VBox attackBox = new VBox(3);
            attackBox.setStyle(
                    "-fx-padding: 5; -fx-border-color: rgba(239, 83, 80, 0.3); -fx-border-width: 0 0 0 3; -fx-border-insets: 0;");

            String name = attack.get("name") != null ? attack.get("name").toString() : "Unknown";
            String damage = Objects.toString(attack.get("damage"), "");
            attackBox.getChildren().add(createLabel(name + (damage.isEmpty() ? "" : " - " + damage),
                    "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;"));

            if (attack.get("cost") != null) {
                attackBox.getChildren().add(
                        createLabel("💎 Costo: " + attack.get("cost"), "-fx-text-fill: #9CA3AF; -fx-font-size: 11px;"));
            }
            if (attack.get(EFFECT_LITERAL) != null && !attack.get(EFFECT_LITERAL).toString().isEmpty()) {
                Label effect = createLabel(attack.get(EFFECT_LITERAL).toString(),
                        "-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-wrap-text: true;");
                effect.setMaxWidth(330);
                effect.setWrapText(true);
                attackBox.getChildren().add(effect);
            }
            box.getChildren().add(attackBox);
        }
        return box;
    }

    private VBox createLegalBox(model.bean.PokemonCardBean pokemonCard) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: rgba(100, 181, 246, 0.1); -fx-padding: 15; -fx-background-radius: 10;");
        box.getChildren().add(createLabel("⚖️ Legalità e Regolamento",
                "-fx-text-fill: #64B5F6; -fx-font-size: 14px; -fx-font-weight: bold;"));

        HBox statusBox = new HBox(15);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (pokemonCard.getLegalStandard() != null) {
            boolean legal = pokemonCard.getLegalStandard();
            statusBox.getChildren().add(createLabel(legal ? "✅ Standard" : "❌ Standard", "-fx-text-fill: "
                    + (legal ? "#66BB6A" : "#EF5350") + "; -fx-font-size: 12px; -fx-font-weight: bold;"));
        }
        if (pokemonCard.getLegalExpanded() != null) {
            boolean legal = pokemonCard.getLegalExpanded();
            statusBox.getChildren().add(createLabel(legal ? "✅ Expanded" : "❌ Expanded", "-fx-text-fill: "
                    + (legal ? "#66BB6A" : "#EF5350") + "; -fx-font-size: 12px; -fx-font-weight: bold;"));
        }
        if (!statusBox.getChildren().isEmpty())
            box.getChildren().add(statusBox);

        if (pokemonCard.getRegulationMark() != null) {
            box.getChildren().add(createLabel("📍 Regulation Mark: " + pokemonCard.getRegulationMark(),
                    NO_IMAGE_STYLE));
        }
        return box;
    }

    private VBox createVariantsBox(model.bean.PokemonCardBean pokemonCard) {
        if (pokemonCard.getVariantNormal() == null && pokemonCard.getVariantHolo() == null
                && pokemonCard.getVariantReverse() == null && pokemonCard.getVariantFirstEdition() == null)
            return null;

        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: rgba(156, 39, 176, 0.1); -fx-padding: 15; -fx-background-radius: 10;");
        box.getChildren().add(createLabel("✨ Varianti Disponibili",
                "-fx-text-fill: #AB47BC; -fx-font-size: 14px; -fx-font-weight: bold;"));

        FlowPane flow = new FlowPane();
        flow.setHgap(10);
        flow.setVgap(5);

        addVariantBadge(flow, pokemonCard.getVariantNormal(), "⭐ Normal");
        addVariantBadge(flow, pokemonCard.getVariantHolo(), "💫 Holo");
        addVariantBadge(flow, pokemonCard.getVariantReverse(), "🔄 Reverse");
        addVariantBadge(flow, pokemonCard.getVariantFirstEdition(), "1️⃣ First Edition");

        if (!flow.getChildren().isEmpty()) {
            box.getChildren().add(flow);
            return box;
        }
        return null;
    }

    private void addVariantBadge(FlowPane flow, Boolean isAvailable, String text) {
        if (Boolean.TRUE.equals(isAvailable)) {
            Label label = new Label(text);
            label.setStyle(VARIANT_LABEL_STYLE);
            flow.getChildren().add(label);
        }
    }

    private VBox createTradableInfo() {
        VBox box = new VBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.getChildren().add(createLabel("🔄 Disponibile per scambio",
                "-fx-text-fill: #66BB6A; -fx-font-size: 14px; -fx-font-weight: bold;"));
        return box;
    }

    @Override
    public void displayCards(List<CardBean> cards) {
        
        Platform.runLater(() -> {
            
            initialViewBox.setVisible(false);
            initialViewBox.setManaged(false);
            cardsViewBox.setVisible(true);
            cardsViewBox.setManaged(true);

            
            cardsFlowPane.getChildren().clear();

            if (cards == null || cards.isEmpty()) {
                LOGGER.info("No cards to display (empty result)");
                
                Label emptyLabel = new Label("Nessuna carta trovata per il filtro selezionato.");
                emptyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-padding: 20;");
                cardsFlowPane.getChildren().add(emptyLabel);

                
                this.allCards = new ArrayList<>();
                this.currentPage = 0;
                updatePaginationControls(1);
                return;
            }

            
            this.allCards = cards;
            this.currentPage = 0;

            
            showCardsPage(0);
        });
    }

    
    private void showCardsPage(int pageIndex) {
        if (allCards == null || allCards.isEmpty()) {
            return;
        }

        int totalPages = (int) Math.ceil((double) allCards.size() / CARDS_PER_PAGE);

        
        if (pageIndex < 0 || pageIndex >= totalPages) {
            return;
        }

        currentPage = pageIndex;
        int startIndex = currentPage * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, allCards.size());

        LOGGER.log(java.util.logging.Level.INFO,
                "Displaying page {0}/{1} (cards {2}-{3} of {4})",
                new Object[] { currentPage + 1, totalPages, startIndex + 1, endIndex, allCards.size() });

        List<CardBean> pageCards = allCards.subList(startIndex, endIndex);

        Platform.runLater(() -> {
            
            initialViewBox.setVisible(false);
            initialViewBox.setManaged(false);
            cardsViewBox.setVisible(true);
            cardsViewBox.setManaged(true);

            
            cardsFlowPane.getChildren().clear();

            
            for (CardBean card : pageCards) {
                VBox cardContainer = createCardView(card);
                cardsFlowPane.getChildren().add(cardContainer);
            }

            
            updatePaginationControls(totalPages);
        });
    }

    
    private void updatePaginationControls(int totalPages) {
        if (pageLabel != null) {
            pageLabel.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
        }

        if (previousButton != null) {
            previousButton.setDisable(currentPage == 0);
        }

        if (nextButton != null) {
            nextButton.setDisable(currentPage >= totalPages - 1);
        }
    }

    @FXML
    private void onPreviousPage() {
        if (currentPage > 0) {
            showCardsPage(currentPage - 1);
        }
    }

    @FXML
    private void onNextPage() {
        if (allCards != null) {
            int totalPages = (int) Math.ceil((double) allCards.size() / CARDS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                showCardsPage(currentPage + 1);
            }
        }
    }

    private VBox createCardView(CardBean card) {
        VBox cardBox = new VBox(10);
        cardBox.getStyleClass().add("card-container");
        cardBox.setPrefWidth(200);
        cardBox.setCursor(javafx.scene.Cursor.HAND);

        Label gameTypeLabel = new Label(card.getGameType().toString());
        gameTypeLabel.getStyleClass().add("card-game-label");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);

        boolean imageLoaded = false;
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            try {
                
                Image image = new Image(card.getImageUrl(), true);
                if (!image.isError()) {
                    imageView.setImage(image);
                    imageLoaded = true;
                }
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.WARNING, "Failed to load image for card: {0} -> {1}", new Object[]{card.getName(), ex.getMessage()});
                LOGGER.log(java.util.logging.Level.FINER, EXCEPTION_LITERAL, ex);
            }
        }

        
        if (!imageLoaded) {
            try {
                Image placeholderImage = new Image(getClass().getResourceAsStream("/icons/nocardimage.svg"));
                imageView.setImage(placeholderImage);
                imageView.setFitWidth(120);
                imageView.setFitHeight(180);
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.WARNING, "Failed to load placeholder image: {0}", ex.getMessage());
                LOGGER.log(java.util.logging.Level.FINER, EXCEPTION_LITERAL, ex);
            }
        }

        Label nameLabel = new Label(card.getName());
        nameLabel.getStyleClass().add("card-name");
        nameLabel.setStyle("-fx-wrap-text: true; -fx-max-width: 180;");

        Label ownerLabel = null;
        if (card.getOwner() != null && !card.getOwner().isEmpty()) {
            ownerLabel = new Label("Owner: " + card.getOwner());
            ownerLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        }

        Label idLabel = new Label("ID: " + card.getId());
        idLabel.getStyleClass().add("card-id");

        if (ownerLabel != null) {
            cardBox.getChildren().addAll(gameTypeLabel, imageView, nameLabel, ownerLabel, idLabel);
        } else {
            cardBox.getChildren().addAll(gameTypeLabel, imageView, nameLabel, idLabel);
        }

        
        cardBox.setOnMouseClicked(_ -> showCardDetailsDialog(card));

        return cardBox;
    }

    @FXML
    private void onLogoutClicked() {
        if (controller != null) {
            controller.onLogoutRequested();
        }
    }

    
    private void showCardDetailsDialog(CardBean card) {
        if (controller != null) {
            controller.showCardDetails(card);
        }
    }

    @FXML
    private void onCollectionClicked() {
        LOGGER.info("Collection button clicked - navigating to collection page");
        if (controller != null) {
            controller.navigateToCollection();
        }
    }

    @FXML
    private void onTradeClicked() {
        LOGGER.info("Trade button clicked - navigating to trade page");
        if (controller != null) {
            controller.navigateToTrade();
        }
    }

    @FXML
    private void onManageTradesClicked() {
        LOGGER.info("Manage Trades button clicked - navigating to manage trades");
        if (controller != null) {
            controller.navigateToManageTrade();
        }
    }

    @FXML
    private void onExitClicked() {
        if (controller != null) {
            controller.onExitRequested();
        }
    }

    @FXML
    private void onApplyFiltersClicked() {
        LOGGER.info("Apply filters - ITCGCard management not yet implemented");
    }

    @FXML
    private void onSearchClicked() {
        if (controller == null || searchField == null) {
            return;
        }

        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) {
            LOGGER.info("Search query is empty");
            return;
        }

        
        initialViewBox.setVisible(false);
        initialViewBox.setManaged(false);
        cardsViewBox.setVisible(true);
        cardsViewBox.setManaged(true);

        
        String searchName = query.trim();
        LOGGER.log(java.util.logging.Level.INFO, "Searching for cards with name: {0}", searchName);
        controller.searchCardsByName(searchName);
    }

    @FXML
    private void onViewPopularCardsClicked() {
        if (controller != null) {
            
            initialViewBox.setVisible(false);
            initialViewBox.setManaged(false);
            cardsViewBox.setVisible(true);
            cardsViewBox.setManaged(true);

            
            controller.loadPopularCards();
        }
    }

    @Override
    public void displayAvailableSets(Map<String, String> setsMap) {
        LOGGER.log(java.util.logging.Level.INFO, "displayAvailableSets called with map: {0} sets",
                (setsMap != null ? setsMap.size() : "null"));

        if (setsMap == null || setsMap.isEmpty()) {
            LOGGER.log(java.util.logging.Level.WARNING, "No sets available - map is {0}",
                    (setsMap == null ? "null" : "empty"));
            return;
        }

        
        this.setsIdToNameMap = setsMap;

        LOGGER.log(java.util.logging.Level.INFO, "Set map contents: {0}", setsMap);

        Platform.runLater(() -> {
            LOGGER.info("Running on JavaFX thread - updating MenuButton");

            if (setFilterButton == null) {
                LOGGER.severe("setFilterButton is NULL!");
                return;
            }

            setFilterButton.getItems().clear();

            
            MenuItem popularItem = new MenuItem(POPULAR_CARDS_LABEL);
            popularItem.setOnAction(_ -> onSetSelected(POPULAR_CARDS_LABEL));
            setFilterButton.getItems().add(popularItem);

            LOGGER.info("Added 'Popular Cards' option");

            
            int count = 0;
            for (Map.Entry<String, String> entry : setsMap.entrySet()) {
                String displayName = entry.getValue(); 
                MenuItem setItem = new MenuItem(displayName);
                setItem.setOnAction(_ -> onSetSelected(displayName));
                setFilterButton.getItems().add(setItem);
                count++;
                if (count <= 5) { 
                    LOGGER.log(java.util.logging.Level.INFO, "Added set: {0} -> {1}",
                            new Object[] { entry.getKey(), displayName });
                }
            }

            LOGGER.log(java.util.logging.Level.INFO, "Total sets added to MenuButton: {0}", count);
            LOGGER.log(java.util.logging.Level.INFO, "MenuButton items count: {0}", setFilterButton.getItems().size());
        });
    }

    private void onSetSelected(String selectedSetName) {
        if (selectedSetName == null || selectedSetName.isEmpty() || controller == null) {
            return;
        }

        
        setFilterButton.setText(selectedSetName);

        
        initialViewBox.setVisible(false);
        initialViewBox.setManaged(false);
        cardsViewBox.setVisible(true);
        cardsViewBox.setManaged(true);

        
        if (selectedSetName.equals(POPULAR_CARDS_LABEL)) {
            LOGGER.info("Selected popular cards set");
            controller.loadPopularCards();
            return;
        }

        
        if (setsIdToNameMap != null) {
            String setId = setsIdToNameMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(selectedSetName))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (setId != null) {
                LOGGER.log(java.util.logging.Level.INFO, "Set selected - Query: {0} ({1})",
                        new Object[] { setId, selectedSetName });
                
                controller.loadCardsFromSet(setId);
            } else {
                LOGGER.log(java.util.logging.Level.WARNING, "Set ID not found for: {0}", selectedSetName);
            }
        }
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onNavButtonHoverEnter(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            container.setStyle(HOVER_STYLE);
        }
    }

    @FXML
    private void onNavButtonHoverExit(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            container.setStyle(NORMAL_STYLE);
        }
    }

    @Override
    public void showSuccess(String message) {
        javafx.application.Platform.runLater(() -> {
            Dialog<Void> dlg = new Dialog<>();
            dlg.setTitle("Success");
            dlg.initOwner(stage);
            Label lbl = new Label(message != null ? message : "");
            lbl.setStyle("-fx-text-fill: #66BB6A; -fx-font-size: 14px;");
            VBox content = new VBox(lbl);
            content.setPadding(new javafx.geometry.Insets(12));
            dlg.getDialogPane().setContent(content);
            dlg.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
            dlg.show();
        });
    }

    @Override
    public void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            Dialog<Void> dlg = new Dialog<>();
            dlg.setTitle("Errore");
            dlg.initOwner(stage);
            Label lbl = new Label(message != null ? message : "");
            lbl.setStyle("-fx-text-fill: #F44336; -fx-font-size: 14px;");
            VBox content = new VBox(lbl);
            content.setPadding(new javafx.geometry.Insets(12));
            dlg.getDialogPane().setContent(content);
            dlg.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
            dlg.show();
        });
    }

    @Override
    public void refresh() {
        javafx.application.Platform.runLater(() -> {
            try {
                
                if (usernameLabel != null) usernameLabel.setText(usernameLabel.getText());
                
            } catch (Exception ex) {
                LOGGER.fine(() -> "CollectorHPView refresh failed: " + ex.getMessage());
            }
        });
    }
}
