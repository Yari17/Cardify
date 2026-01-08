package view.collectorhomepage;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import controller.CollectorHPController;
import model.bean.CardBean;

import java.util.List;
import java.util.logging.Logger;

public class FXCollectorHPView implements ICollectorHPView {
    private static final Logger LOGGER = Logger.getLogger(FXCollectorHPView.class.getName());

    @FXML
    private Label welcomeLabel;

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

    private CollectorHPController controller;
    private Stage stage;

    public FXCollectorHPView() {
        // FXML fields will be injected by FXMLLoader
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
    }

    @Override
    public void setController(CollectorHPController controller) {
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
    public void displayCards(List<CardBean> cards) {
        if (cardsFlowPane == null) {
            LOGGER.warning("cardsFlowPane is null, cannot display cards");
            return;
        }

        Platform.runLater(() -> {
            cardsFlowPane.getChildren().clear();

            for (CardBean card : cards) {
                VBox cardContainer = createCardView(card);
                cardsFlowPane.getChildren().add(cardContainer);
            }
        });
    }

    private VBox createCardView(CardBean card) {
        VBox cardBox = new VBox(10);
        cardBox.setStyle(
            "-fx-background-color: #1F2933; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
            "-fx-cursor: hand;"
        );
        cardBox.setPrefWidth(200);

        Label gameTypeLabel = new Label(card.getGameType().toString());
        gameTypeLabel.setStyle(
            "-fx-text-fill: #4CAF50; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: rgba(76, 175, 80, 0.2); " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 5 10 5 10;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(card.getImageUrl(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                LOGGER.warning(() -> "Failed to load image for card: " + card.getName());
                imageView.setImage(null);
            }
        }

        Label nameLabel = new Label(card.getName());
        nameLabel.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-wrap-text: true; " +
            "-fx-max-width: 180;"
        );

        Label idLabel = new Label("ID: " + card.getId());
        idLabel.setStyle(
            "-fx-text-fill: lightgray; " +
            "-fx-font-size: 12px;"
        );

        cardBox.getChildren().addAll(gameTypeLabel, imageView, nameLabel, idLabel);

        cardBox.setOnMouseEntered(e ->
            cardBox.setStyle(cardBox.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;")
        );
        cardBox.setOnMouseExited(e ->
            cardBox.setStyle(cardBox.getStyle() + "-fx-scale-x: 1.0; -fx-scale-y: 1.0;")
        );

        return cardBox;
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
        LOGGER.info("Apply filters - ITCGCard management not yet implemented");
    }

    @FXML
    private void onSearchClicked() {
        LOGGER.info("Search clicked - ITCGCard management not yet implemented");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
