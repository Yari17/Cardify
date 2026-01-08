package view.collectorhomepage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import controller.CollectorHomePageController;
import model.domain.CardGameType;
import model.domain.card.Card;
import model.domain.card.MagicCard;
import model.domain.card.PokemonCard;
import model.service.CardApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaFxCollectorHomePageView implements ICollectorHomePageView {
    private static final Logger LOGGER = Logger.getLogger(JavaFxCollectorHomePageView.class.getName());

    @FXML
    private Label welcomeLabel;

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

    private CollectorHomePageController controller;
    private Stage stage;
    private CardApiService cardApiService;

    public JavaFxCollectorHomePageView() {
        this.cardApiService = new CardApiService();
    }

    @FXML
    private void initialize() {
        if (gameComboBox != null) {
            gameComboBox.getItems().addAll("All", "Magic: The Gathering", "Pokemon TCG");
            gameComboBox.setValue("All");
        }

        if (languageComboBox != null) {
            languageComboBox.getItems().addAll("All", "English", "Italian", "Japanese", "French", "German");
            languageComboBox.setValue("All");
        }


        loadDefaultCards();
    }

    @Override
    public void setController(CollectorHomePageController controller) {
        this.controller = controller;
        if (controller != null && welcomeLabel != null) {
            showWelcomeMessage(controller.getUsername());
        }
    }

    @Override
    public String getSearchQuery() {
        return searchField != null ? searchField.getText() : "";
    }

    @Override
    public void showCards(List<Card> cards) {
        if (cardsFlowPane == null) {
            LOGGER.warning("cardsFlowPane is null, cannot show cards");
            return;
        }

        Platform.runLater(() -> {
            cardsFlowPane.getChildren().clear();

            for (Card card : cards) {
                VBox cardBox = createCardView(card);
                cardsFlowPane.getChildren().add(cardBox);
            }
        });
    }

    @Override
    public void showCardDetails(Card card) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Card Details");
        alert.setHeaderText(card.getName());
        alert.setContentText(card.getSpecificDetails());
        alert.showAndWait();
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
        if (welcomeLabel != null) {
            welcomeLabel.setText("Benvenuto, " + username + "!");
        }
    }

    @FXML
    private void onLogoutClicked() {
        if (controller != null) {
            controller.onLogoutRequested();
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
        loadDefaultCards();
    }

    @FXML
    private void onSearchClicked() {
        String query = getSearchQuery();
        if (query != null && !query.trim().isEmpty()) {
            performSearch(query.trim());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void loadDefaultCards() {
        new Thread(() -> {
            try {
                List<Card> allCards = new ArrayList<>();

                LOGGER.info("Fetching Magic cards...");
                List<Card> magicCards = cardApiService.searchCardsByType("Creature", CardGameType.MAGIC);
                allCards.addAll(magicCards.stream().limit(10).toList());

                LOGGER.info("Fetching Pokemon cards...");
                List<Card> pokemonCards = cardApiService.searchCardsByType("Fire", CardGameType.POKEMON);
                allCards.addAll(pokemonCards.stream().limit(10).toList());

                LOGGER.info("Total cards fetched: " + allCards.size());

                showCards(allCards);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading default cards", e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to load cards");
                    alert.setContentText("Could not fetch cards from API: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void performSearch(String query) {
        new Thread(() -> {
            try {
                List<Card> allCards = new ArrayList<>();

                List<Card> pokemonResults = cardApiService.searchCardsByName(query, CardGameType.POKEMON);
                List<Card> magicResults = cardApiService.searchCardsByName(query, CardGameType.MAGIC);

                allCards.addAll(pokemonResults.stream().limit(10).toList());
                allCards.addAll(magicResults.stream().limit(10).toList());

                showCards(allCards);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error searching cards", e);
            }
        }).start();
    }

    private VBox createCardView(Card card) {
        VBox cardBox = new VBox(10);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setStyle("-fx-background-color: #1A1D21; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
                        "-fx-cursor: hand;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(210);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(card.getImageUrl(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                LOGGER.warning("Could not load image for card: " + card.getName());
            }
        }

        Label nameLabel = new Label(card.getName());
        nameLabel.setStyle("-fx-text-fill: white; " +
                          "-fx-font-size: 14px; " +
                          "-fx-font-weight: bold; " +
                          "-fx-wrap-text: true; " +
                          "-fx-text-alignment: center;");
        nameLabel.setMaxWidth(150);
        nameLabel.setWrapText(true);

        String infoText = (card.getSetName() != null ? card.getSetName() : "Unknown") +
                         " • " +
                         (card.getRarity() != null ? card.getRarity() : "Common");
        Label infoLabel = new Label(infoText);
        infoLabel.setStyle("-fx-text-fill: lightgray; -fx-font-size: 12px;");
        infoLabel.setMaxWidth(150);
        infoLabel.setWrapText(true);

        String gameType = card instanceof PokemonCard ? "Pokemon" : "Magic";
        Label gameTypeLabel = new Label(gameType);
        gameTypeLabel.setStyle("-fx-background-color: " +
                              (card instanceof PokemonCard ? "#FFD700" : "#4169E1") + "; " +
                              "-fx-text-fill: white; " +
                              "-fx-font-size: 10px; " +
                              "-fx-padding: 3 8 3 8; " +
                              "-fx-background-radius: 3;");

        cardBox.getChildren().addAll(imageView, nameLabel, infoLabel, gameTypeLabel);

        cardBox.setOnMouseClicked(event -> showCardDetails(card));

        cardBox.setOnMouseEntered(event ->
            cardBox.setStyle("-fx-background-color: #2A3441; " +
                           "-fx-background-radius: 10; " +
                           "-fx-padding: 15; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 8); " +
                           "-fx-cursor: hand;")
        );

        cardBox.setOnMouseExited(event ->
            cardBox.setStyle("-fx-background-color: #1A1D21; " +
                           "-fx-background-radius: 10; " +
                           "-fx-padding: 15; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
                           "-fx-cursor: hand;")
        );

        return cardBox;
    }
}
