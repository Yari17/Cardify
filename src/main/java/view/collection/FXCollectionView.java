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
import model.domain.Card;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
//Controller grafico del caso d'uso Gestisci Collezione
public class FXCollectionView implements ICollectionView {
    private static final Logger LOGGER = Logger.getLogger(FXCollectionView.class.getName());
    private static final String CARD_HOVER_STYLE = "card-hover";

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private VBox setsContainer;

    @FXML
    private Button saveButton;

    private CollectionController controller;
    private Stage stage;
    // GUI must not hold DAO references. Controller supplies setCardsMap via displayCollection.
    private Map<String, List<Card>> setCardsMap;

    // Track currently displayed binders to enable partial refresh
    private Map<String, Binder> currentBinders;

    // Pagination fields - track current page for each set
    private Map<String, Integer> setCurrentPages;
    private static final int CARDS_PER_PAGE = 20;

    public FXCollectionView() {
        // FXML fields will be injected by FXMLLoader
        this.currentBinders = new HashMap<>();
        this.setCurrentPages = new HashMap<>();
    }

    public void setController(CollectionController controller) {
        this.controller = controller;
        if (controller != null) {
            LOGGER.info("Controller set in FXCollectionView");
        }
    }

    public void setWelcomeMessage(String username) {
        if (usernameLabel != null) {
            usernameLabel.setText(username);
        }
    }

    /**
     * Visualizza la collezione organizzata per set con le carte
     */
    public void displayCollection(Map<String, Binder> bindersBySet, Map<String, List<model.domain.Card>> setCardsMap) {
        if (setsContainer == null) {
            LOGGER.warning("setsContainer is null");
            return;
        }

        // Store current binders and setCardsMap for partial refresh
        this.currentBinders = new HashMap<>(bindersBySet);
        // keep reference to setCards for later updates
        this.setCardsMap = (setCardsMap != null) ? new HashMap<>(setCardsMap) : new HashMap<>();
        Map<String, List<Card>> effectiveSetCards = this.setCardsMap;

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

            List<Card> allCards = effectiveSetCards.getOrDefault(setId, java.util.Collections.emptyList());
            VBox setSection = createSetSection(setId, binder, allCards);
            setsContainer.getChildren().add(setSection);
        }

        // Se non ci sono set, mostra un placeholder con pulsante
        if (bindersBySet.isEmpty()) {
            VBox emptyState = createEmptyState();
            setsContainer.getChildren().add(emptyState);
        }

        LOGGER.log(Level.INFO,"Displayed collection with {0} sets", bindersBySet.size());
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
    private VBox createSetSection(String setId, Binder binder, List<Card> allCards) {
        VBox setSection = new VBox(15);
        setSection.setPadding(new Insets(20));
        setSection.getStyleClass().add("set-section");
        setSection.setUserData(setId);

        // Header del set
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label setNameLabel = new Label(binder.getSetName());
        setNameLabel.getStyleClass().add("set-name-label");

        // Carica tutte le carte del set per calcolare le mancanti
        int totalCards = (allCards != null) ? allCards.size() : 0;

        int ownedCards = binder.getCardCount();
        int missingCards = Math.max(0, totalCards - ownedCards);

        Label statsLabel = new Label(ownedCards + " carte possedute");
        statsLabel.getStyleClass().add("set-stats-label");

        Label missingLabel = new Label(missingCards + " mancanti");
        missingLabel.setStyle("-fx-text-fill: #ed4747; -fx-font-size: 16px; -fx-font-weight: bold;");

        // If there are no cards available for this set (persistence/provider returned nothing),
        // make it explicit in the UI so user knows why the grid is empty.
        if (totalCards == 0) {
            Label noCardsNote = new Label("Nessuna carta disponibile per questo set (dati non caricati)");
            noCardsNote.setStyle("-fx-text-fill: #cfcfcf; -fx-font-size: 12px;");
            VBox.setMargin(noCardsNote, new Insets(6,0,0,0));
            // We'll add this below next to the header (after header is assembled)
            header.getChildren().add(noCardsNote);
        }

        // Spacer per spingere il bottone elimina a destra
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bottone elimina set
        Button deleteButton = createDeleteButton(setId, binder.getSetName());

        header.getChildren().addAll(setNameLabel, statsLabel, missingLabel, spacer, deleteButton);

        // Griglia delle carte (paginata)
        FlowPane cardsGrid = new FlowPane();
        cardsGrid.setHgap(15);
        cardsGrid.setVgap(15);
        cardsGrid.setPrefWrapLength(Region.USE_COMPUTED_SIZE);
        cardsGrid.setUserData("cardsGrid_" + setId); // Identificatore per trovare la griglia

        // Pagination controls
        HBox paginationControls = createPaginationControls(setId, allCards, binder);

        // Initialize to page 0
        setCurrentPages.put(setId, 0);

        // Load first page of cards
        try {
            loadCardsPage(setId, allCards, binder, cardsGrid, paginationControls);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading cards for set: " + setId, ex);
        }

        setSection.getChildren().addAll(header, cardsGrid, paginationControls);

        return setSection;
    }

    /**
     * Load a specific page of cards for a set
     */
    private void loadCardsPage(String setId, List<Card> allCards, Binder binder, FlowPane cardsGrid,
            HBox paginationControls) {
        int currentPage = setCurrentPages.getOrDefault(setId, 0);
        int totalPages = (int) Math.ceil((double) allCards.size() / CARDS_PER_PAGE);

        if (totalPages == 0)
            totalPages = 1;

        int startIndex = currentPage * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, allCards.size());

        int displayStart = allCards.isEmpty() ? 0 : (startIndex + 1);
        int displayEnd = endIndex;
        LOGGER.log(java.util.logging.Level.INFO,
                "Loading page {0}/{1} for set {2} (cards {3}-{4} of {5})",
                new Object[] { currentPage + 1, totalPages, setId, displayStart, displayEnd, allCards.size() });

        List<Card> pageCards = allCards.subList(startIndex, endIndex);

        // Clear previous cards
        cardsGrid.getChildren().clear();

        // Crea mappa delle carte possedute per accesso rapido
        Map<String, CardBean> ownedCardsMap = binder.getCards().stream()
                .collect(java.util.stream.Collectors.toMap(CardBean::getId, c -> c));

        // Add cards for current page
        for (Card card : pageCards) {
            CardBean ownedCard = ownedCardsMap.get(card.getId());
            boolean isOwned = ownedCard != null;
            VBox cardTile = createCardTile(card, setId, isOwned, ownedCard);
            cardsGrid.getChildren().add(cardTile);
        }

        // Update pagination controls
        updatePaginationControls(paginationControls, currentPage, totalPages);
    }

    /**
     * Create pagination controls for a set
     */
    private HBox createPaginationControls(String setId, List<Card> allCards, Binder binder) {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));
        controls.setUserData("pagination_" + setId);

        Button prevButton = new Button();
        prevButton.setText("Previous");
        prevButton.setUserData("prevButton");
        prevButton.getStyleClass().add("button-accent");

        FontIcon prevIcon = new FontIcon("fas-arrow-left");
        prevIcon.setIconSize(14);
        prevIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        prevButton.setGraphic(prevIcon);

        Label pageLabel = new Label("Page 1 of 1");
        pageLabel.setUserData("pageLabel");
        pageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button nextButton = new Button();
        nextButton.setText("Next");
        nextButton.setUserData("nextButton");
        nextButton.getStyleClass().add("button-accent");

        FontIcon nextIcon = new FontIcon("fas-arrow-right");
        nextIcon.setIconSize(14);
        nextIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        nextButton.setGraphic(nextIcon);

        // Set button actions
        prevButton.setOnAction(e -> {
            int currentPage = setCurrentPages.getOrDefault(setId, 0);
            if (currentPage > 0) {
                setCurrentPages.put(setId, currentPage - 1);
                FlowPane grid = findCardsGridInSet(setId);
                if (grid != null) {
                    loadCardsPage(setId, allCards, binder, grid, controls);
                }
            }
        });

        nextButton.setOnAction(e -> {
            int currentPage = setCurrentPages.getOrDefault(setId, 0);
            int totalPages = (int) Math.ceil((double) allCards.size() / CARDS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                setCurrentPages.put(setId, currentPage + 1);
                FlowPane grid = findCardsGridInSet(setId);
                if (grid != null) {
                    loadCardsPage(setId, allCards, binder, grid, controls);
                }
            }
        });

        controls.getChildren().addAll(prevButton, pageLabel, nextButton);

        return controls;
    }

    /**
     * Update pagination button states
     */
    private void updatePaginationControls(HBox controls, int currentPage, int totalPages) {
        for (javafx.scene.Node node : controls.getChildren()) {
            if ("prevButton".equals(node.getUserData())) {
                (node).setDisable(currentPage == 0);
            } else if ("nextButton".equals(node.getUserData())) {
                (node).setDisable(currentPage >= totalPages - 1);
            } else if ("pageLabel".equals(node.getUserData())) {
                ((Label) node).setText(String.format("Page %d of %d", currentPage + 1, totalPages));
            }
        }
    }

    /**
     * Find cards grid for a specific set
     */
    private FlowPane findCardsGridInSet(String setId) {
        VBox setSection = findSetSection(setId);
        if (setSection == null)
            return null;

        for (javafx.scene.Node node : setSection.getChildren()) {
            if (node instanceof FlowPane && ("cardsGrid_" + setId).equals(node.getUserData())) {
                return (FlowPane) node;
            }
        }
        return null;
    }

    /**
     * Crea un tile per una singola carta con controlli quantità e scambiabilità
     */
    private VBox createCardTile(Card card, String setId, boolean isOwned, CardBean ownedCard) {
        VBox tile = new VBox();
        tile.setPrefSize(120, 168); // Dimensione esatta immagine
        tile.setMinSize(120, 168);
        tile.setMaxSize(120, 168);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setSpacing(0);
        tile.setPadding(new Insets(0));

        // Store card ID in userData for updates
        tile.setUserData(card.getId());

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

        // Carica immagine con placeholder se non disponibile
        boolean imageLoaded = false;
        String imageUrl = card.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Direct loading (background loading enabled)
                Image image = new Image(imageUrl, true);
                cardImage.setImage(image);
                imageLoaded = true;
            } catch (Exception ex) {
                LOGGER.warning("Failed to load image for card: " + card.getName() + " from URL: " + imageUrl
                        + ". Error: " + ex.getMessage());
            }
        }

        // Se l'immagine non è caricata, mostra placeholder
        if (!imageLoaded) {
            try {
                Image placeholderImage = new Image(getClass().getResourceAsStream("/icons/nocardimage.svg"));
                cardImage.setImage(placeholderImage);
                cardImage.setFitWidth(80);
                cardImage.setFitHeight(120);
            } catch (Exception ex) {
                LOGGER.warning("Failed to load placeholder image: " + ex.getMessage());
            }
        }

        // Applica opacità se non posseduta (slightly obscured to distinguish from owned)
        if (!isOwned) {
            cardImage.setOpacity(0.5);
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
            // Se posseduta: mostra controlli completi SOLO su hover
            VBox controls = createCardControls(setId, card, ownedCard);
            controls.setVisible(false); // Inizialmente nascosti
            StackPane.setAlignment(controls, Pos.BOTTOM_CENTER);
            StackPane.setMargin(controls, new Insets(0, 0, 5, 0));
            imageContainer.getChildren().add(controls);

            // Mostra controlli solo su hover
            tile.setOnMouseEntered(_ -> {
                controls.setVisible(true);
                tile.getStyleClass().add(CARD_HOVER_STYLE);
            });
            tile.setOnMouseExited(_ -> {
                controls.setVisible(false);
                tile.getStyleClass().remove(CARD_HOVER_STYLE);
            });
        }

        tile.getChildren().add(imageContainer);

        // Hover effect per carte non possedute
        if (!isOwned) {
            tile.setOnMouseEntered(_ -> tile.getStyleClass().add(CARD_HOVER_STYLE));
            tile.setOnMouseExited(_ -> tile.getStyleClass().remove(CARD_HOVER_STYLE));
        }

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
     * Crea il pulsante per eliminare un binder con dialog di conferma
     */
    private Button createDeleteButton(String setId, String setName) {
        Button deleteButton = new Button();
        FontIcon trashIcon = new FontIcon("fas-trash-alt");
        trashIcon.setIconSize(20);
        trashIcon.setIconColor(javafx.scene.paint.Color.web("#EF5350"));
        deleteButton.setGraphic(trashIcon);
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-padding: 8; -fx-cursor: hand;");

        deleteButton.setOnAction(_ -> showDeleteConfirmationDialog(setId, setName));

        return deleteButton;
    }

    /**
     * Mostra dialog di conferma per l'eliminazione di un binder
     */
    private void showDeleteConfirmationDialog(String setId, String setName) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Conferma Eliminazione");
        confirmDialog.setHeaderText("Eliminare il set \"" + setName + "\"?");
        confirmDialog.setContentText(
                "Questa azione eliminerà il set e tutte le carte associate.\n" +
                        "L'operazione non può essere annullata.\n\n" +
                        "Sei sicuro di voler continuare?");

        ButtonType buttonTypeYes = new ButtonType("Sì, Elimina", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeNo = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmDialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // Stile per il dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            if (controller != null) {
                controller.deleteBinder(setId);
            }
        }
    }

    /**
     * Crea i controlli per una carta posseduta (quantità, scambiabile, +/-)
     */
    private VBox createCardControls(String setId, Card card, CardBean ownedCard) {
        VBox controls = new VBox(5);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(8));
        controls.setMaxWidth(110);
        controls.getStyleClass().add("card-controls");

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER);

        // Pulsante - (rimuovi carta)
        Button minusButton = new Button();
        FontIcon minusIcon = new FontIcon("fas-minus-circle");
        minusIcon.setIconSize(22);
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
                "-fx-padding: 5 10; -fx-background-radius: 12; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-min-width: 40px;");

        // Pulsante + (aggiungi altra copia)
        Button plusButton = new Button();
        FontIcon plusIcon = new FontIcon("fas-plus-circle");
        plusIcon.setIconSize(22);
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
        tradableCheckbox.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
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
                setComboBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        setComboBox.valueProperty().addListener((_, _, newVal) -> okButton.setDisable(newVal == null));

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

    /**
     * Imposta la visibilità del pulsante Save
     */
    public void setSaveButtonVisible(boolean visible) {
        if (saveButton != null) {
            saveButton.setVisible(visible);
        }
    }

    /**
     * Aggiorna una singola carta nel set senza refreshare tutta la collezione
     */
    /**
     * Aggiorna una singola carta nel set senza refreshare tutta la collezione
     */
    public void updateCardInSet(String setId, String cardId) {
        if (setsContainer == null || currentBinders == null) {
            return;
        }

        VBox setSection = findSetSection(setId);
        if (setSection == null)
            return;

        FlowPane cardsGrid = findCardsGrid(setSection);
        if (cardsGrid == null)
            return;

        VBox cardTile = findCardTile(cardsGrid, cardId);
        if (cardTile == null)
            return;

        updateCardTileInGrid(cardsGrid, cardTile, setId, cardId);
    }

    private VBox findSetSection(String setId) {
        for (javafx.scene.Node node : setsContainer.getChildren()) {
            if (node instanceof VBox setSection && setId.equals(setSection.getUserData())) {
                return setSection;
            }
        }
        return null;
    }

    private FlowPane findCardsGrid(VBox setSection) {
        if (setSection.getChildren().size() > 1 &&
                setSection.getChildren().get(1) instanceof FlowPane cardsGrid) {
            return cardsGrid;
        }
        return null;
    }

    private VBox findCardTile(FlowPane cardsGrid, String cardId) {
        for (javafx.scene.Node cardNode : cardsGrid.getChildren()) {
            if (cardNode instanceof VBox cardTile &&
                    cardId.equals(cardTile.getUserData())) {
                return cardTile;
            }
        }
        return null;
    }

    private void updateCardTileInGrid(FlowPane cardsGrid, VBox oldTile, String setId, String cardId) {
        Binder binder = currentBinders.get(setId);
        if (binder == null)
            return;

        try {
            List<Card> allCards = setCardsMap != null ? setCardsMap.getOrDefault(setId, java.util.Collections.emptyList()) : java.util.Collections.emptyList();
            Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);

            if (card != null) {
                Map<String, CardBean> ownedCardsMap = binder.getCards().stream()
                        .collect(java.util.stream.Collectors.toMap(CardBean::getId, c -> c));

                CardBean ownedCard = ownedCardsMap.get(cardId);
                boolean isOwned = ownedCard != null;

                VBox newCardTile = createCardTile(card, setId, isOwned, ownedCard);
                int index = cardsGrid.getChildren().indexOf(oldTile);
                cardsGrid.getChildren().set(index, newCardTile);

                LOGGER.info(() -> "Updated card " + cardId + " in UI");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to update card in UI", ex);
        }
    }

    @FXML
    private void onSaveClicked() {
        LOGGER.info("Save button clicked");
        if (controller != null) {
            controller.saveChanges();
        }
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
                                "-fx-scale-y: 1.1;");
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

