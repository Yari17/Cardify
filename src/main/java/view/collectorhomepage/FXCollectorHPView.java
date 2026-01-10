package view.collectorhomepage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
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
    private Label welcomeLabel;

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
    private ComboBox<String> setComboBox;

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

    // Campi per gestire la ricerca unificata
    private SearchType currentSearchType = SearchType.BY_NAME;
    private String currentSearchQuery = "";

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
        if (controller != null && welcomeLabel != null) {
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
        if (welcomeLabel != null) {
            welcomeLabel.setText("Benvenuto, " + username + "!");
        }
    }

    @Override
    public void showCardOverview(CardBean card) {

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

        // Hover effect is handled by CSS
        return cardBox;
    }

    @FXML
    private void onLogoutClicked() {
        if (controller != null) {
            controller.onLogoutRequested();
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

        // Imposta il tipo di ricerca e la query
        currentSearchType = SearchType.BY_NAME;
        currentSearchQuery = query.trim();

        LOGGER.info("Search by name: " + currentSearchQuery);

        // Il controller chiamerà getSearchQuery() e getSearchType()
        // per ottenere i parametri di ricerca
        // TODO: Implementare la ricerca per nome nel controller
        LOGGER.warning("Search by name not yet implemented in controller");
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
            LOGGER.info("Running on JavaFX thread - updating ComboBox");

            if (setComboBox == null) {
                LOGGER.severe("setComboBox is NULL!");
                return;
            }

            setComboBox.getItems().clear();
            setComboBox.getItems().add("Popular Cards (sv08.5)");

            LOGGER.info("Added 'Popular Cards' option");

            // Aggiungi i set mostrando il nome leggibile
            int count = 0;
            for (Map.Entry<String, String> entry : setsMap.entrySet()) {
                String displayName = entry.getValue(); // Mostra solo il nome
                setComboBox.getItems().add(displayName);
                count++;
                if (count <= 5) { // Log solo i primi 5 per non sovraccaricare
                    LOGGER.info("Added set: " + entry.getKey() + " -> " + displayName);
                }
            }

            LOGGER.info("Total sets added to ComboBox: " + count);
            LOGGER.info("ComboBox items count: " + setComboBox.getItems().size());
        });
    }

    @FXML
    private void onSetSelected() {
        String selectedSetName = setComboBox.getValue();
        if (selectedSetName == null || selectedSetName.isEmpty() || controller == null) {
            return;
        }

        // Caso speciale per le carte popolari
        if (selectedSetName.equals("Popular Cards (sv08.5)")) {
            currentSearchType = SearchType.BY_SET;
            currentSearchQuery = "sv08.5";
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
                // Imposta il tipo di ricerca e la query (ID del set)
                currentSearchType = SearchType.BY_SET;
                currentSearchQuery = setId;

                LOGGER.info("Set selected - Type: BY_SET, Query: " + setId + " (" + selectedSetName + ")");

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
