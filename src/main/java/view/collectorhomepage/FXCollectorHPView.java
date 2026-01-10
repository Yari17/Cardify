package view.collectorhomepage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import controller.CollectorHPController;
import model.bean.CardBean;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FXCollectorHPView implements ICollectorHPView {
    private static final Logger LOGGER = Logger.getLogger(FXCollectorHPView.class.getName());

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> gameComboBox;

    @SuppressWarnings("unused") // Will be used when expansion filtering is implemented
    @FXML
    private ComboBox<String> expansionComboBox;

    @FXML
    private ComboBox<String> languageComboBox;

    @SuppressWarnings("unused") // Will be used when ITCGCard condition filtering is implemented
    @FXML
    private CheckBox mintCheckBox;

    @SuppressWarnings("unused")
    @FXML
    private CheckBox nearMintCheckBox;

    @SuppressWarnings("unused")
    @FXML
    private CheckBox excellentCheckBox;

    @SuppressWarnings("unused")
    @FXML
    private CheckBox goodCheckBox;

    @SuppressWarnings("unused")
    @FXML
    private CheckBox playedCheckBox;

    @SuppressWarnings("unused") // Will be used when price filtering is implemented
    @FXML
    private Slider priceSlider;

    @SuppressWarnings("unused") // Will be used to display cards
    @FXML
    private FlowPane cardsFlowPane;

    @SuppressWarnings("unused")
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
    private Button collectionButton;

    @FXML
    private Button tradeButton;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox logoutButtonContainer;

    private CollectorHPController controller;
    private Stage stage;
    private Map<String, String> setsIdToNameMap; // ID -> Nome del set

    public FXCollectorHPView() {
        // FXML fields will be injected by FXMLLoader
    }

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
        // Crea un dialog modale per mostrare i dettagli della carta
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Dettagli Carta");
        dialog.initOwner(stage);

        // Crea il contenuto del dialog con scroll per contenuti lunghi
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setStyle("-fx-background-color: #1E2530;");

        // Titolo con nome carta
        Label titleLabel = new Label(card.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Game type badge
        Label gameTypeLabel = new Label(card.getGameType().toString());
        gameTypeLabel.setStyle("-fx-background-color: #29B6F6; -fx-text-fill: white; " +
                              "-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");

        // Immagine della carta (più grande)
        ImageView cardImageView = new ImageView();
        cardImageView.setFitWidth(300);
        cardImageView.setFitHeight(420);
        cardImageView.setPreserveRatio(true);

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            try {
                Image cardImage = new Image(card.getImageUrl(), true);
                cardImageView.setImage(cardImage);
            } catch (Exception e) {
                LOGGER.warning("Failed to load card image in dialog");
            }
        }

        // ID della carta
        Label idLabel = new Label("ID: " + card.getId());
        idLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        content.getChildren().addAll(titleLabel, gameTypeLabel, cardImageView, idLabel);

        // Dettagli specifici per PokemonCard
        if (card instanceof model.bean.PokemonCardBean pokemonCard) {
            VBox detailsBox = new VBox(10);
            detailsBox.setStyle("-fx-background-color: rgba(41, 182, 246, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

            // HP e Tipo
            if (pokemonCard.getHp() != null || (pokemonCard.getTypes() != null && !pokemonCard.getTypes().isEmpty())) {
                HBox hpTypeBox = new HBox(20);
                hpTypeBox.setAlignment(javafx.geometry.Pos.CENTER);

                if (pokemonCard.getHp() != null) {
                    Label hpLabel = new Label("❤️ HP: " + pokemonCard.getHp());
                    hpLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 16px; -fx-font-weight: bold;");
                    hpTypeBox.getChildren().add(hpLabel);
                }

                if (pokemonCard.getTypes() != null && !pokemonCard.getTypes().isEmpty()) {
                    Label typeLabel = new Label("⚡ " + String.join(", ", pokemonCard.getTypes()));
                    typeLabel.setStyle("-fx-text-fill: #FFA726; -fx-font-size: 16px; -fx-font-weight: bold;");
                    hpTypeBox.getChildren().add(typeLabel);
                }

                detailsBox.getChildren().add(hpTypeBox);
            }

            // Stage e Evoluzione
            if (pokemonCard.getStage() != null || pokemonCard.getEvolveFrom() != null) {
                HBox stageBox = new HBox(15);
                stageBox.setAlignment(javafx.geometry.Pos.CENTER);

                if (pokemonCard.getStage() != null) {
                    Label stageLabel = new Label("Stage: " + pokemonCard.getStage());
                    stageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                    stageBox.getChildren().add(stageLabel);
                }

                if (pokemonCard.getEvolveFrom() != null) {
                    Label evolveLabel = new Label("Evolve da: " + pokemonCard.getEvolveFrom());
                    evolveLabel.setStyle("-fx-text-fill: #66BB6A; -fx-font-size: 14px;");
                    stageBox.getChildren().add(evolveLabel);
                }

                detailsBox.getChildren().add(stageBox);
            }

            // Rarità
            if (pokemonCard.getRarity() != null) {
                Label rarityLabel = new Label("⭐ Rarità: " + pokemonCard.getRarity());
                rarityLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
                detailsBox.getChildren().add(rarityLabel);
            }

            // Set
            if (pokemonCard.getSetName() != null) {
                Label setLabel = new Label("📦 Set: " + pokemonCard.getSetName());
                setLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
                detailsBox.getChildren().add(setLabel);
            }

            // Illustratore
            if (pokemonCard.getIllustrator() != null) {
                Label illustratorLabel = new Label("🎨 Illustratore: " + pokemonCard.getIllustrator());
                illustratorLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-style: italic;");
                detailsBox.getChildren().add(illustratorLabel);
            }

            // Categoria
            if (pokemonCard.getCategory() != null) {
                Label categoryLabel = new Label("📋 Categoria: " + pokemonCard.getCategory());
                categoryLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
                detailsBox.getChildren().add(categoryLabel);
            }

            content.getChildren().add(detailsBox);

            // Debolezze e Costo Ritirata
            if ((pokemonCard.getWeaknesses() != null && !pokemonCard.getWeaknesses().isEmpty()) ||
                pokemonCard.getRetreat() != null) {

                VBox weaknessBox = new VBox(10);
                weaknessBox.setStyle("-fx-background-color: rgba(255, 152, 0, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

                // Debolezze
                if (pokemonCard.getWeaknesses() != null && !pokemonCard.getWeaknesses().isEmpty()) {
                    Label weaknessTitleLabel = new Label("⚠️ Debolezze");
                    weaknessTitleLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 14px; -fx-font-weight: bold;");
                    weaknessBox.getChildren().add(weaknessTitleLabel);

                    for (Map<String, String> weakness : pokemonCard.getWeaknesses()) {
                        String type = weakness.get("type");
                        String value = weakness.get("value");

                        Label weaknessLabel = new Label("🔸 " + type + " " + value);
                        weaknessLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                        weaknessBox.getChildren().add(weaknessLabel);
                    }
                }

                // Costo ritirata
                if (pokemonCard.getRetreat() != null) {
                    String retreatText = "🏃 Costo Ritirata: ";
                    for (int i = 0; i < pokemonCard.getRetreat(); i++) {
                        retreatText += "⚪ ";
                    }
                    retreatText += "(" + pokemonCard.getRetreat() + ")";

                    Label retreatLabel = new Label(retreatText);
                    retreatLabel.setStyle("-fx-text-fill: #FFB74D; -fx-font-size: 13px; -fx-font-weight: bold;");
                    weaknessBox.getChildren().add(retreatLabel);
                }

                content.getChildren().add(weaknessBox);
            }

            // Descrizione
            if (pokemonCard.getDescription() != null && !pokemonCard.getDescription().isEmpty()) {
                VBox descriptionBox = new VBox(5);
                descriptionBox.setStyle("-fx-background-color: rgba(76, 175, 80, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

                Label descTitleLabel = new Label("📖 Descrizione");
                descTitleLabel.setStyle("-fx-text-fill: #66BB6A; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label descTextLabel = new Label(pokemonCard.getDescription());
                descTextLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-wrap-text: true;");
                descTextLabel.setMaxWidth(350);
                descTextLabel.setWrapText(true);

                descriptionBox.getChildren().addAll(descTitleLabel, descTextLabel);
                content.getChildren().add(descriptionBox);
            }

            // Attacchi
            if (pokemonCard.getAttacks() != null && !pokemonCard.getAttacks().isEmpty()) {
                VBox attacksBox = new VBox(10);
                attacksBox.setStyle("-fx-background-color: rgba(239, 83, 80, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

                Label attacksTitleLabel = new Label("⚔️ Attacchi (" + pokemonCard.getAttacks().size() + ")");
                attacksTitleLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 14px; -fx-font-weight: bold;");
                attacksBox.getChildren().add(attacksTitleLabel);

                for (Map<String, Object> attack : pokemonCard.getAttacks()) {
                    VBox attackBox = new VBox(3);
                    attackBox.setStyle("-fx-padding: 5; -fx-border-color: rgba(239, 83, 80, 0.3); " +
                                     "-fx-border-width: 0 0 0 3; -fx-border-insets: 0;");

                    String attackName = attack.get("name") != null ? attack.get("name").toString() : "Unknown";
                    String damage = attack.get("damage") != null ? attack.get("damage").toString() : "";

                    Label nameLabel = new Label(attackName + (damage.isEmpty() ? "" : " - " + damage));
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

                    attackBox.getChildren().add(nameLabel);

                    // Costo energetico
                    if (attack.get("cost") != null) {
                        Label costLabel = new Label("💎 Costo: " + attack.get("cost").toString());
                        costLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
                        attackBox.getChildren().add(costLabel);
                    }

                    // Effetto
                    if (attack.get("effect") != null && !attack.get("effect").toString().isEmpty()) {
                        Label effectLabel = new Label(attack.get("effect").toString());
                        effectLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-wrap-text: true;");
                        effectLabel.setMaxWidth(330);
                        effectLabel.setWrapText(true);
                        attackBox.getChildren().add(effectLabel);
                    }

                    attacksBox.getChildren().add(attackBox);
                }

                content.getChildren().add(attacksBox);
            }

            // Legalità e Regulation Mark
            VBox legalBox = new VBox(8);
            legalBox.setStyle("-fx-background-color: rgba(100, 181, 246, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

            Label legalTitleLabel = new Label("⚖️ Legalità e Regolamento");
            legalTitleLabel.setStyle("-fx-text-fill: #64B5F6; -fx-font-size: 14px; -fx-font-weight: bold;");
            legalBox.getChildren().add(legalTitleLabel);

            HBox legalStatusBox = new HBox(15);
            legalStatusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            if (pokemonCard.getLegalStandard() != null) {
                Label standardLabel = new Label(pokemonCard.getLegalStandard() ? "✅ Standard" : "❌ Standard");
                standardLabel.setStyle("-fx-text-fill: " + (pokemonCard.getLegalStandard() ? "#66BB6A" : "#EF5350") +
                                     "; -fx-font-size: 12px; -fx-font-weight: bold;");
                legalStatusBox.getChildren().add(standardLabel);
            }

            if (pokemonCard.getLegalExpanded() != null) {
                Label expandedLabel = new Label(pokemonCard.getLegalExpanded() ? "✅ Expanded" : "❌ Expanded");
                expandedLabel.setStyle("-fx-text-fill: " + (pokemonCard.getLegalExpanded() ? "#66BB6A" : "#EF5350") +
                                     "; -fx-font-size: 12px; -fx-font-weight: bold;");
                legalStatusBox.getChildren().add(expandedLabel);
            }

            if (!legalStatusBox.getChildren().isEmpty()) {
                legalBox.getChildren().add(legalStatusBox);
            }

            if (pokemonCard.getRegulationMark() != null) {
                Label regMarkLabel = new Label("📍 Regulation Mark: " + pokemonCard.getRegulationMark());
                regMarkLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
                legalBox.getChildren().add(regMarkLabel);
            }

            if (legalBox.getChildren().size() > 1) {
                content.getChildren().add(legalBox);
            }

            // Varianti
            if (pokemonCard.getVariantHolo() != null || pokemonCard.getVariantReverse() != null ||
                pokemonCard.getVariantNormal() != null || pokemonCard.getVariantFirstEdition() != null) {

                VBox variantsBox = new VBox(8);
                variantsBox.setStyle("-fx-background-color: rgba(156, 39, 176, 0.1); -fx-padding: 15; -fx-background-radius: 10;");

                Label variantsTitleLabel = new Label("✨ Varianti Disponibili");
                variantsTitleLabel.setStyle("-fx-text-fill: #AB47BC; -fx-font-size: 14px; -fx-font-weight: bold;");
                variantsBox.getChildren().add(variantsTitleLabel);

                FlowPane variantsFlow = new FlowPane();
                variantsFlow.setHgap(10);
                variantsFlow.setVgap(5);

                if (Boolean.TRUE.equals(pokemonCard.getVariantNormal())) {
                    Label normalLabel = new Label("⭐ Normal");
                    normalLabel.setStyle("-fx-background-color: #AB47BC; -fx-text-fill: white; " +
                                       "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px;");
                    variantsFlow.getChildren().add(normalLabel);
                }

                if (Boolean.TRUE.equals(pokemonCard.getVariantHolo())) {
                    Label holoLabel = new Label("💫 Holo");
                    holoLabel.setStyle("-fx-background-color: #AB47BC; -fx-text-fill: white; " +
                                     "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px;");
                    variantsFlow.getChildren().add(holoLabel);
                }

                if (Boolean.TRUE.equals(pokemonCard.getVariantReverse())) {
                    Label reverseLabel = new Label("🔄 Reverse");
                    reverseLabel.setStyle("-fx-background-color: #AB47BC; -fx-text-fill: white; " +
                                        "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px;");
                    variantsFlow.getChildren().add(reverseLabel);
                }

                if (Boolean.TRUE.equals(pokemonCard.getVariantFirstEdition())) {
                    Label firstEdLabel = new Label("1️⃣ First Edition");
                    firstEdLabel.setStyle("-fx-background-color: #AB47BC; -fx-text-fill: white; " +
                                        "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px;");
                    variantsFlow.getChildren().add(firstEdLabel);
                }

                if (!variantsFlow.getChildren().isEmpty()) {
                    variantsBox.getChildren().add(variantsFlow);
                    content.getChildren().add(variantsBox);
                }
            }
        }

        // Informazioni inventario - RIMOSSO quantità posseduta
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(javafx.geometry.Pos.CENTER);

        if (card.isTradable()) {
            Label tradableLabel = new Label("🔄 Disponibile per scambio");
            tradableLabel.setStyle("-fx-text-fill: #66BB6A; -fx-font-size: 14px; -fx-font-weight: bold;");
            infoBox.getChildren().add(tradableLabel);
        }

        if (!infoBox.getChildren().isEmpty()) {
            content.getChildren().add(infoBox);
        }

        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Applica stili al dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: #1E2530;");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/theme.css").toExternalForm()
        );

        // Imposta dimensioni minime per il dialog
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setMaxHeight(700);

        // Mostra il dialog (non bloccante rispetto alla finestra principale)
        dialog.show();
    }

    @Override
    public void displayCards(List<CardBean> cards) {
        if (cards == null || cards.isEmpty()) {
            LOGGER.warning("No cards to display");
            return;
        }

        // Assicurati che il box delle carte sia visibile
        Platform.runLater(() -> {
            initialViewBox.setVisible(false);
            initialViewBox.setManaged(false);
            cardsViewBox.setVisible(true);
            cardsViewBox.setManaged(true);

            cardsFlowPane.getChildren().clear();

            for (CardBean card : cards) {
                VBox cardContainer = createCardView(card);
                cardsFlowPane.getChildren().add(cardContainer);
            }
        });
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

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(card.getImageUrl(), true);
                imageView.setImage(image);
            } catch (Exception _) {
                LOGGER.warning(() -> "Failed to load image for card: " + card.getName());
                imageView.setImage(null);
            }
        }

        Label nameLabel = new Label(card.getName());
        nameLabel.getStyleClass().add("card-name");
        nameLabel.setStyle("-fx-wrap-text: true; -fx-max-width: 180;");

        Label idLabel = new Label("ID: " + card.getId());
        idLabel.getStyleClass().add("card-id");

        cardBox.getChildren().addAll(gameTypeLabel, imageView, nameLabel, idLabel);

        // Aggiungi evento click per aprire dialog dettagli
        cardBox.setOnMouseClicked(_ -> showCardDetailsDialog(card));

        return cardBox;
    }

    @FXML
    private void onLogoutClicked() {
        if (controller != null) {
            controller.onLogoutRequested();
        }
    }

    /**
     * Mostra una finestra modale con i dettagli della carta selezionata.
     */
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

        // Nascondi il box iniziale e mostra il box delle carte
        initialViewBox.setVisible(false);
        initialViewBox.setManaged(false);
        cardsViewBox.setVisible(true);
        cardsViewBox.setManaged(true);

        // Cerca le carte per nome
        String searchName = query.trim();
        LOGGER.info("Searching for cards with name: " + searchName);
        controller.searchCardsByName(searchName);
    }

    @FXML
    private void onViewPopularCardsClicked() {
        if (controller != null) {
            // Nascondi il box iniziale e mostra il box delle carte
            initialViewBox.setVisible(false);
            initialViewBox.setManaged(false);
            cardsViewBox.setVisible(true);
            cardsViewBox.setManaged(true);

            // Carica le carte popolari
            controller.loadPopularCards();
        }
    }

    @Override
    public void displayAvailableSets(Map<String, String> setsMap) {
        LOGGER.info("displayAvailableSets called with map: " + (setsMap != null ? setsMap.size() + " sets" : "null"));

        if (setsMap == null || setsMap.isEmpty()) {
            LOGGER.warning("No sets available - map is " + (setsMap == null ? "null" : "empty"));
            return;
        }

        // Salva la mappa per usarla quando l'utente seleziona un set
        this.setsIdToNameMap = setsMap;

        LOGGER.info("Set map contents: " + setsMap);

        Platform.runLater(() -> {
            LOGGER.info("Running on JavaFX thread - updating MenuButton");

            if (setFilterButton == null) {
                LOGGER.severe("setFilterButton is NULL!");
                return;
            }

            setFilterButton.getItems().clear();

            // Opzione "Popular Cards"
            MenuItem popularItem = new MenuItem("Popular Cards (sv08.5)");
            popularItem.setOnAction(_ -> onSetSelected("Popular Cards (sv08.5)"));
            setFilterButton.getItems().add(popularItem);

            LOGGER.info("Added 'Popular Cards' option");

            // Aggiungi i set mostrando il nome leggibile
            int count = 0;
            for (Map.Entry<String, String> entry : setsMap.entrySet()) {
                String displayName = entry.getValue(); // Mostra solo il nome
                MenuItem setItem = new MenuItem(displayName);
                setItem.setOnAction(_ -> onSetSelected(displayName));
                setFilterButton.getItems().add(setItem);
                count++;
                if (count <= 5) { // Log solo i primi 5 per non sovraccaricare
                    LOGGER.info("Added set: " + entry.getKey() + " -> " + displayName);
                }
            }

            LOGGER.info("Total sets added to MenuButton: " + count);
            LOGGER.info("MenuButton items count: " + setFilterButton.getItems().size());
        });
    }

    private void onSetSelected(String selectedSetName) {
        if (selectedSetName == null || selectedSetName.isEmpty() || controller == null) {
            return;
        }

        // Aggiorna il testo del bottone con il set selezionato
        setFilterButton.setText(selectedSetName);

        // Nascondi il box iniziale e mostra il box delle carte
        initialViewBox.setVisible(false);
        initialViewBox.setManaged(false);
        cardsViewBox.setVisible(true);
        cardsViewBox.setManaged(true);

        // Caso speciale per le carte popolari
        if (selectedSetName.equals("Popular Cards (sv08.5)")) {
            LOGGER.info("Selected popular cards set");
            controller.loadCardsFromSet("sv08.5");
            return;
        }

        // Trova l'ID corrispondente al nome selezionato
        if (setsIdToNameMap != null) {
            String setId = setsIdToNameMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(selectedSetName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

            if (setId != null) {
                LOGGER.info("Set selected - Query: " + setId + " (" + selectedSetName + ")");
                // Carica le carte del set
                controller.loadCardsFromSet(setId);
            } else {
                LOGGER.warning("Set ID not found for: " + selectedSetName);
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onNavButtonHoverEnter(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            container.setStyle(
                "-fx-background-color: rgba(41, 182, 246, 0.2); " +
                "-fx-background-radius: 8; " +
                "-fx-scale-x: 1.1; " +
                "-fx-scale-y: 1.1;"
            );
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
                "-fx-scale-y: 1.0;"
            );
        }
    }
}
