package view.collection;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import controller.CollectionController;
import model.bean.CardBean;
import model.domain.Binder;
import model.domain.card.Card;
import model.domain.card.CardProvider;
import org.kordamp.ikonli.javafx.FontIcon;
import view.IView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class FXCollectionView implements IView {
    private static final Logger LOGGER = Logger.getLogger(FXCollectionView.class.getName());

    @FXML
    private Label welcomeLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private VBox setsContainer;

    private CollectionController controller;
    private Stage stage;

    public FXCollectionView() {
        // FXML fields will be injected by FXMLLoader
    }

    public void setController(CollectionController controller) {
        this.controller = controller;
        if (controller != null) {
            LOGGER.info("Controller set in FXCollectionView");
        }
    }

    public void setWelcomeMessage(String username) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("La tua Collezione");
        }
    }

    /**
     * Visualizza la collezione organizzata per set con le carte
     */
    public void displayCollection(Map<String, Binder> bindersBySet, CardProvider cardProvider) {
        if (setsContainer == null) {
            LOGGER.warning("setsContainer is null");
            return;
        }

        setsContainer.getChildren().clear();

        // Pulsante per aggiungere nuovo set (se ci sono binder)
        if (!bindersBySet.isEmpty()) {
            Button addSetButton = createAddSetButton();
            setsContainer.getChildren().add(addSetButton);
        }

        // Per ogni set, mostra le carte
        for (Map.Entry<String, Binder> entry : bindersBySet.entrySet()) {
            String setId = entry.getKey();
            Binder binder = entry.getValue();

            VBox setSection = createSetSection(setId, binder, cardProvider);
            setsContainer.getChildren().add(setSection);
        }

        // Se non ci sono set, mostra un placeholder con pulsante
        if (bindersBySet.isEmpty()) {
            VBox emptyState = createEmptyState();
            setsContainer.getChildren().add(emptyState);
        }

        LOGGER.info("Displayed collection with " + bindersBySet.size() + " sets");
    }

    /**
     * Crea il pulsante per aggiungere un nuovo set
     */
    private Button createAddSetButton() {
        Button button = new Button("+ Aggiungi Nuovo Set");
        button.getStyleClass().add("button-accent");
        button.setStyle("-fx-font-size: 16px; -fx-padding: 15 30;");
        button.setOnAction(e -> showAddSetDialog());

        VBox.setMargin(button, new Insets(0, 0, 20, 0));

        return button;
    }

    /**
     * Crea una sezione per un set con tutte le sue carte
     */
    private VBox createSetSection(String setId, Binder binder, CardProvider cardProvider) {
        VBox setSection = new VBox(15);
        setSection.setPadding(new Insets(20));
        setSection.getStyleClass().add("set-section");

        // Header del set
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label setNameLabel = new Label(binder.getSetName());
        setNameLabel.getStyleClass().add("set-name-label");

        Label statsLabel = new Label(binder.getCardCount() + " carte possedute");
        statsLabel.getStyleClass().add("set-stats-label");

        header.getChildren().addAll(setNameLabel, statsLabel);

        // Griglia delle carte
        FlowPane cardsGrid = new FlowPane();
        cardsGrid.setHgap(15);
        cardsGrid.setVgap(15);
        cardsGrid.setPrefWrapLength(Region.USE_COMPUTED_SIZE);

        // Carica tutte le carte del set
        try {
            List<Card> allCards = cardProvider.searchPokemonSet(setId);

            // Crea mappa delle carte possedute per accesso rapido
            Map<String, CardBean> ownedCardsMap = binder.getCards().stream()
                    .collect(java.util.stream.Collectors.toMap(CardBean::getId, c -> c));

            for (Card card : allCards) {
                CardBean ownedCard = ownedCardsMap.get(card.getId());
                boolean isOwned = ownedCard != null;
                VBox cardTile = createCardTile(card, setId, isOwned, ownedCard);
                cardsGrid.getChildren().add(cardTile);
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading cards for set: " + setId);
        }

        setSection.getChildren().addAll(header, cardsGrid);

        return setSection;
    }

    /**
     * Crea un tile per una singola carta con controlli quantità e scambiabilità
     */
    private VBox createCardTile(Card card, String setId, boolean isOwned, CardBean ownedCard) {
        VBox tile = new VBox();
        tile.setPrefSize(120, 168);  // Dimensione esatta immagine
        tile.setMinSize(120, 168);
        tile.setMaxSize(120, 168);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setSpacing(0);
        tile.setPadding(new Insets(0));

        // Stili base
        if (isOwned) {
            tile.getStyleClass().addAll("card-tile", "card-owned");
        } else {
            tile.getStyleClass().addAll("card-tile", "card-not-owned");
        }

        // Immagine della carta (dimensione standard carta Pokémon)
        ImageView cardImage = new ImageView();
        cardImage.setFitWidth(120);
        cardImage.setFitHeight(168);
        cardImage.setPreserveRatio(true);

        // Carica immagine
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(card.getImageUrl(), true);
                cardImage.setImage(image);
            } catch (Exception e) {
                LOGGER.warning("Failed to load image for card: " + card.getName());
            }
        }

        // Applica opacità se non posseduta
        if (!isOwned) {
            cardImage.setOpacity(0.3);
        }

        // Container per immagine con overlay controlli
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(cardImage);

        // Se NON posseduta: mostra solo pulsante + per aggiungere
        if (!isOwned) {
            Button addButton = createAddButton(setId, card);
            StackPane.setAlignment(addButton, Pos.BOTTOM_CENTER);
            StackPane.setMargin(addButton, new Insets(0, 0, 5, 0));
            imageContainer.getChildren().add(addButton);
        } else {
            // Se posseduta: mostra controlli completi
            VBox controls = createCardControls(setId, card, ownedCard);
            StackPane.setAlignment(controls, Pos.BOTTOM_CENTER);
            StackPane.setMargin(controls, new Insets(0, 0, 5, 0));
            imageContainer.getChildren().add(controls);
        }

        tile.getChildren().add(imageContainer);

        // Hover effect
        tile.setOnMouseEntered(_ -> tile.getStyleClass().add("card-hover"));
        tile.setOnMouseExited(_ -> tile.getStyleClass().remove("card-hover"));

        return tile;
    }

    /**
     * Crea il pulsante + per aggiungere una carta non posseduta
     */
    private Button createAddButton(String setId, Card card) {
        Button addButton = new Button();
        FontIcon plusIcon = new FontIcon("fas-plus-circle");
        plusIcon.setIconSize(28);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#29B6F6"));
        addButton.setGraphic(plusIcon);
        addButton.getStyleClass().add("card-add-button");

        addButton.setOnAction(_ -> {
            if (controller != null) {
                controller.addCardToSet(setId, card);
            }
        });

        return addButton;
    }

    /**
     * Crea i controlli per una carta posseduta (quantità, scambiabile, +/-)
     */
    private VBox createCardControls(String setId, Card card, CardBean ownedCard) {
        VBox controls = new VBox(3);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(5));
        controls.getStyleClass().add("card-controls");

        HBox topRow = new HBox(5);
        topRow.setAlignment(Pos.CENTER);

        // Pulsante - (rimuovi carta)
        Button minusButton = new Button();
        FontIcon minusIcon = new FontIcon("fas-minus-circle");
        minusIcon.setIconSize(20);
        minusIcon.setIconColor(javafx.scene.paint.Color.web("#EF5350"));
        minusButton.setGraphic(minusIcon);
        minusButton.getStyleClass().add("card-control-button");
        minusButton.setOnAction(_ -> {
            if (controller != null) {
                controller.removeCardFromSet(setId, card);
            }
        });

        // Label quantità (usa la quantità reale da CardBean)
        int quantity = ownedCard != null ? ownedCard.getQuantity() : 1;
        Label quantityLabel = new Label(String.valueOf(quantity));
        quantityLabel.getStyleClass().add("card-quantity-label");
        quantityLabel.setStyle("-fx-background-color: #29B6F6; -fx-text-fill: white; " +
                              "-fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold;");

        // Pulsante + (aggiungi altra copia)
        Button plusButton = new Button();
        FontIcon plusIcon = new FontIcon("fas-plus-circle");
        plusIcon.setIconSize(20);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#66BB6A"));
        plusButton.setGraphic(plusIcon);
        plusButton.getStyleClass().add("card-control-button");
        plusButton.setOnAction(_ -> {
            if (controller != null) {
                controller.addCardToSet(setId, card);
            }
        });

        topRow.getChildren().addAll(minusButton, quantityLabel, plusButton);

        // Checkbox scambiabile (usa lo stato reale da CardBean)
        CheckBox tradableCheckbox = new CheckBox("Scambiabile");
        tradableCheckbox.getStyleClass().add("card-tradable-checkbox");
        tradableCheckbox.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");
        if (ownedCard != null) {
            tradableCheckbox.setSelected(ownedCard.isTradable());
        }
        tradableCheckbox.setOnAction(_ -> {
            if (controller != null) {
                controller.toggleCardTradable(setId, card.getId(), tradableCheckbox.isSelected());
            }
        });

        controls.getChildren().addAll(topRow, tradableCheckbox);

        return controls;
    }

    /**
     * Crea lo stato vuoto quando non ci sono set
     */
    private VBox createEmptyState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));

        Label titleLabel = new Label("📚 Nessun set nella collezione");
        titleLabel.getStyleClass().add("text-primary");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Aggiungi il tuo primo set per iniziare!");
        subtitleLabel.getStyleClass().add("text-secondary");
        subtitleLabel.setStyle("-fx-font-size: 16px;");

        Button addButton = new Button("Aggiungi Set");
        addButton.getStyleClass().add("button-accent");
        addButton.setStyle("-fx-font-size: 18px; -fx-padding: 15 40;");
        addButton.setOnAction(_ -> showAddSetDialog());

        emptyState.getChildren().addAll(titleLabel, subtitleLabel, addButton);

        return emptyState;
    }

    /**
     * Mostra il dialog per selezionare un set da aggiungere
     */
    private void showAddSetDialog() {
        if (controller == null) {
            showError("Controller non disponibile");
            return;
        }

        Map<String, String> availableSets = controller.getAvailableSets();

        if (availableSets.isEmpty()) {
            showError("Nessun set disponibile al momento");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Aggiungi Set alla Collezione");
        dialog.setHeaderText("Seleziona un set da aggiungere");

        ComboBox<String> setComboBox = new ComboBox<>();
        availableSets.values().forEach(setComboBox.getItems()::add);
        setComboBox.setPromptText("Seleziona un set...");
        setComboBox.setPrefWidth(400);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Set disponibili:"),
                setComboBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        setComboBox.valueProperty().addListener((_, _, newVal) ->
                okButton.setDisable(newVal == null)
        );

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String selectedSetName = setComboBox.getValue();
            if (selectedSetName != null) {
                String setId = availableSets.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(selectedSetName))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                if (setId != null && controller != null) {
                    controller.createBinder(setId, selectedSetName);
                }
            }
        }
    }

    public void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Successo", message);
    }

    public void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Errore", message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onHomeClicked() {
        LOGGER.info("Home clicked - navigating to homepage");
        if (controller != null) {
            controller.navigateToHome();
        }
    }

    @FXML
    private void onCollectionClicked() {
        LOGGER.info("Already in Collection page");
    }

    @FXML
    private void onTradeClicked() {
        LOGGER.info("Trade clicked - navigating to trade page");
        if (controller != null) {
            controller.navigateToTrade();
        }
    }

    @FXML
    private void onLogoutClicked() {
        LOGGER.info("Logout clicked");
        if (controller != null) {
            controller.onLogoutRequested();
        }
    }

    @FXML
    private void onNavButtonHoverEnter(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            if (!container.getStyle().contains("dropshadow")) {
                container.setStyle(
                    "-fx-background-color: rgba(41, 182, 246, 0.2); " +
                    "-fx-background-radius: 8; " +
                    "-fx-scale-x: 1.1; " +
                    "-fx-scale-y: 1.1;"
                );
            }
        }
    }

    @FXML
    private void onNavButtonHoverExit(MouseEvent event) {
        if (event.getSource() instanceof VBox container) {
            if (!container.getStyle().contains("dropshadow")) {
                container.setStyle("");
            }
        }
    }
}

