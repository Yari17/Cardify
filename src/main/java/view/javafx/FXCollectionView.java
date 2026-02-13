package view.javafx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import controller.CollectionController;
import exception.ViewException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.bean.BinderBean;
import model.bean.CardBean;

import view.ICollectionView;

/**
 * Vista JavaFX dedicata alla gestione della collezione dell'utente.
 * Permette di visualizzare, creare ed eliminare raccoglitori virtuali,
 * oltre a gestire la quantità di carte possedute per ogni set tramite
 * un'interfaccia a griglia.
 */
public class FXCollectionView implements ICollectionView {
    private static final Logger LOGGER = Logger.getLogger(FXCollectionView.class.getName());
    private static final String FORCE_OWNED_KEY = "forceOwned";

    private Stage stage;
    private CollectionController controller;

    @FXML
    private VBox root;
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
    private VBox profileBox;
    @FXML
    private ImageView avatarImage;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button createBinderBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private VBox binderContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label emptyStateLabel;

    public FXCollectionView() {
        // costruttore di default
    }

    @FXML
    public void initialize() {
        wireNavbar();
        loadIcons();
        setupPersistenceSwitch();
        setupUserInfo();
        setupMainButtons();
        updateEmptyState();
    }

    /**
     * Collega i pulsanti della navbar alle rispettive azioni di navigazione.
     */
    private void wireNavbar() {
        if (homeBtn != null)
            homeBtn.setOnAction(e -> navigateTo(model.domain.enumerations.ViewPage.COLLECTOR_HOMEPAGE));

        if (collectionBtn != null)
            collectionBtn.getStyleClass().add("active-nav");

        if (tradeBtn != null)
            tradeBtn.setOnAction(e -> navigateTo(model.domain.enumerations.ViewPage.COLLECTOR_TRADE));

        if (proposalBtn != null)
            proposalBtn.setOnAction(e -> navigateTo(model.domain.enumerations.ViewPage.MANAGE_PROPOSAL));

        if (logoutBtn != null)
            logoutBtn.setOnAction(e -> {
                if (controller != null && controller.getAppController() != null) {
                    controller.getAppController().setCurrentUser(null);
                    controller.getAppController().navigateTO(model.domain.enumerations.ViewPage.LOGIN);
                }
            });
    }

    private void navigateTo(model.domain.enumerations.ViewPage page) {
        if (controller != null && controller.getAppController() != null)
            controller.getAppController().navigateTO(page);
    }

    /**
     * Carica le icone per i pulsanti della navbar.
     */
    private void loadIcons() {
        loadIcon(homeIcon, "/icons/homepage.png");
        loadIcon(collectionIcon, "/icons/collection.png");
        loadIcon(tradeIcon, "/icons/trade.png");
        loadIcon(proposalIcon, "/icons/manageproposals.png");
        loadIcon(logoutIcon, "/icons/logout.png");
    }

    private void loadIcon(ImageView view, String path) {
        if (view == null)
            return;
        try {
            view.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception _) {
            // Ignora icona mancante
        }
    }

    /**
     * Configura lo switch per la persistenza (JDBC/JSON).
     * Aggiunge un ComboBox alla toolbar se il pulsante createBinderBtn è presente.
     */
    private void setupPersistenceSwitch() {
        if (createBinderBtn == null)
            return;

        javafx.scene.control.ComboBox<String> persistenceSwitch = new javafx.scene.control.ComboBox<>();
        persistenceSwitch.getItems().addAll("JDBC", "JSON");
        persistenceSwitch.setValue(config.AppConfig.getBinderPersistenceType().name());

        persistenceSwitch.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #2fb4f5; " +
                        "-fx-border-radius: 6; " +
                        "-fx-font-size: 12px; " +
                        "-fx-pref-width: 100;");

        persistenceSwitch.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white;");
                }
            }
        });

        persistenceSwitch.setOnAction(e -> {
            if (controller != null) {
                String selected = persistenceSwitch.getValue();
                if (selected != null) {
                    controller.switchPersistence(model.domain.enumerations.PersistenceType.valueOf(selected));
                }
            }
        });

        if (createBinderBtn.getParent() instanceof Pane toolbar) {
            int index = toolbar.getChildren().indexOf(createBinderBtn);
            toolbar.getChildren().add(index + 1, persistenceSwitch);
            if (toolbar instanceof HBox) {
                HBox.setMargin(persistenceSwitch, new Insets(0, 0, 0, 10));
            }
        }
    }

    /**
     * Imposta le informazioni dell'utente (username e avatar).
     */
    private void setupUserInfo() {
        if (controller != null && controller.getSessionUser() != null) {
            if (usernameLabel != null)
                usernameLabel.setText(controller.getSessionUser().getUsername());
            try {
                if (avatarImage != null)
                    avatarImage.setImage(new Image(getClass().getResourceAsStream("/icons/collectorpp.png")));
            } catch (Exception _) {
                // Ignora errore caricamento avatar
            }
        }
    }

    /**
     * Configura i listener per i pulsanti principali (Crea binder, Salva).
     */
    private void setupMainButtons() {
        if (createBinderBtn != null)
            createBinderBtn.setOnAction(e -> onCreateBinder());

        if (binderContainer != null)
            binderContainer.getChildren()
                    .addListener((javafx.collections.ListChangeListener<Node>) c -> updateEmptyState());

        if (saveBtn != null) {
            saveBtn.setOnAction(e -> {
                if (controller != null)
                    controller.saveChanges();
            });
        }
    }

    /**
     * Aggiorna la visibilità del messaggio di stato vuoto in base al contenuto del
     * container dei raccoglitori.
     */
    private void updateEmptyState() {
        boolean empty = binderContainer == null || binderContainer.getChildren().isEmpty();
        if (emptyStateLabel != null) {
            emptyStateLabel.setVisible(empty);
            emptyStateLabel.setManaged(empty);
        }
        if (scrollPane != null) {
            scrollPane.setStyle(
                    "-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background: transparent;");
            scrollPane.setVisible(!empty);
            scrollPane.setManaged(!empty);
        }
    }

    @Override
    public void onCreateBinder() {
        if (controller == null)
            return;
        // Chiama il controller che recupererà i set e invocherà showAvailableSets
        controller.prepareCreateBinder();
    }

    @Override
    public void showAvailableSets(Map<String, String> availableSets) {
        if (availableSets == null || availableSets.isEmpty()) {
            showError("Nessun set disponibile.");
            return;
        }

        // Prepara la lista di scelte (ID - Nome)
        List<String> choices = new ArrayList<>();
        for (Map.Entry<String, String> entry : availableSets.entrySet()) {
            choices.add(entry.getKey() + " - " + entry.getValue());
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Nuovo Raccoglitore");
        dialog.setHeaderText("Seleziona il Set per il nuovo raccoglitore");
        dialog.setContentText("Set:");

        // Applica foglio di stile se presente
        java.net.URL cssUrl = getClass().getResource("/styles/style.css");
        if (cssUrl != null) {
            dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            // Estrae l'ID dalla stringa selezionata "ID - Nome"
            String[] parts = selected.split(" - ");
            String setId = parts[0];
            String setName = availableSets.get(setId);
            if (setName == null)
                setName = setId;

            if (controller != null) {
                controller.createNewBinder(setId, setName);
                controller.loadUserCollection();
            }
        });
    }

    /**
     * Inizializza e visualizza la pagina della collezione caricando il file FXML.
     * Configura lo stage, carica i fogli di stile CSS e avvia il caricamento dei
     * dati dal controller.
     */
    @Override
    public void display() {
        Runnable show = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CollectionPage.fxml"));
                loader.setController(this);
                VBox rootNode = loader.load();

                Scene scene = new Scene(rootNode);
                java.net.URL cssUrl = getClass().getResource("/styles/style.css");
                if (cssUrl != null)
                    scene.getStylesheets().add(cssUrl.toExternalForm());

                rootNode.setStyle("-fx-background-color: #1E2532;");

                configureStage(scene);
                loadCollectionData();

            } catch (IOException e) {
                System.err.println("Unable to load CollectionPage.fxml: " + e.getMessage());
                throw new ViewException("Failed to load Collection View", e);
            }
        };
        runFx(show);
    }

    private void runFx(Runnable r) {
        if (Platform.isFxApplicationThread())
            r.run();
        else
            Platform.runLater(r);
    }

    private void configureStage(Scene scene) {
        Optional<Window> existing = Window.getWindows().stream().filter(Window::isShowing).findFirst();
        if (existing.isPresent() && existing.get() instanceof Stage existingStage) {
            existingStage.setScene(scene);
            existingStage.setTitle("Collection");
            this.stage = existingStage;
        } else {
            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle("Collezione");
            st.setWidth(1280);
            st.setHeight(800);
            st.centerOnScreen();
            st.show();
            this.stage = st;
        }
    }

    private void loadCollectionData() {
        Platform.runLater(() -> {
            if (controller != null) {
                try {
                    controller.loadUserCollection();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error loading user collection", e);
                }
            }
        });
    }

    public void show() {
        display();
    }

    /**
     * Crea la vista per un singolo raccoglitore.
     * 
     * @param binder Il bean del raccoglitore.
     * @return Il nodo grafico contenente header, griglia carte e paginazione.
     */
    private Node createBinderView(BinderBean binder) {
        VBox container = new VBox();
        container.setSpacing(10);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: #2C3A4F; -fx-background-radius: 15;");

        List<CardBean> completeList = controller.getCompleteBinderCards(binder);

        container.getChildren().add(createBinderHeader(binder, completeList));

        TilePane grid = new TilePane();
        grid.setPrefColumns(6);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPrefTileWidth(180);
        grid.setPrefTileHeight(252);

        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER);

        setupPagination(completeList, grid, footer);

        container.getChildren().addAll(grid, footer);
        return container;
    }

    private void setupPagination(List<CardBean> items, TilePane grid, HBox footer) {
        Button prev = new Button("Precedente");
        Button next = new Button("Successivo");
        Label pageLbl = new Label();
        pageLbl.setStyle("-fx-text-fill: white;");
        footer.getChildren().addAll(prev, pageLbl, next);

        final int pageSize = 12;
        final int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / pageSize));
        final int[] pageIndex = { 1 };

        Runnable update = () -> {
            grid.getChildren().clear();
            int start = (pageIndex[0] - 1) * pageSize;
            int end = Math.min(start + pageSize, items.size());
            List<CardBean> page = start >= end ? List.of() : items.subList(start, end);
            for (CardBean ci : page)
                grid.getChildren().add(createCardNode(ci));

            pageLbl.setText("Pagina " + pageIndex[0] + " di " + totalPages);
            prev.setDisable(pageIndex[0] <= 1);
            next.setDisable(pageIndex[0] >= totalPages);
        };

        prev.setOnAction(e -> {
            if (pageIndex[0] > 1) {
                pageIndex[0]--;
                update.run();
            }
        });
        next.setOnAction(e -> {
            if (pageIndex[0] < totalPages) {
                pageIndex[0]++;
                update.run();
            }
        });

        update.run();
    }

    private HBox createBinderHeader(BinderBean binder, List<CardBean> completeList) {
        HBox header = new HBox(8);
        Label title = new Label(binder.getSetName());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        long owned = completeList.stream().filter(c -> c.getQuantity() > 0).count();
        long missing = completeList.size() - owned;

        Label ownedLbl = new Label(owned + " carte possedute");
        ownedLbl.setStyle("-fx-text-fill: #2fb4f5;");
        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: rgba(255,255,255,0.6);");
        Label missingLbl = new Label(missing + " mancanti");
        missingLbl.setStyle("-fx-text-fill: #ed4747;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button delBtn = createDeleteButton(binder);

        HBox stats = new HBox(6, ownedLbl, sep, missingLbl);
        stats.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        header.getChildren().addAll(title, stats, spacer, delBtn);
        return header;
    }

    private Button createDeleteButton(BinderBean binder) {
        Button delBtn = new Button();
        try {
            ImageView delIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/delete.png")));
            delIcon.setFitWidth(24);
            delIcon.setFitHeight(24);
            delBtn.setGraphic(delIcon);
        } catch (Exception _) {
            delBtn.setText("DEL");
        }
        delBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        delBtn.setOnMouseEntered(e -> delBtn.setOpacity(0.7));
        delBtn.setOnMouseExited(e -> delBtn.setOpacity(1.0));
        delBtn.setOnAction(e -> confirmAndDeleteBinder(binder));
        return delBtn;
    }

    private void confirmAndDeleteBinder(BinderBean binder) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Elimina Raccoglitore");
        alert.setHeaderText("Cancellazione Raccoglitore");
        alert.setContentText("Sei sicuro di voler eliminare il raccoglitore?");

        java.net.URL cssUrl = getClass().getResource("/styles/login.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }

        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK && controller != null) {
            controller.deleteBinder(binder.getSetID());
        }
    }

    /**
     * Crea un nodo grafico per rappresentare una singola carta.
     * Include l'immagine della carta, il nome e un overlay interattivo per la
     * gestione della quantità.
     * 
     * @param ci Il bean della carta da visualizzare.
     * @return Una StackPane contenente tutti gli elementi grafici della carta.
     */
    private Node createCardNode(CardBean ci) {
        StackPane p = new StackPane();
        p.setPrefSize(180, 252);
        p.setStyle(
                "-fx-background-color: #2C3A4F; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 8;");

        p.getChildren().add(createCardImage(ci));

        Label nameLabel = new Label(ci.getName());
        nameLabel.setStyle(
                "-fx-text-fill: white; -fx-font-size: 11px; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 2 4; -fx-background-radius: 4;");
        nameLabel.setMaxWidth(170);
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(javafx.geometry.Pos.CENTER);
        StackPane.setAlignment(nameLabel, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setMargin(nameLabel, new Insets(0, 0, 2, 0));
        p.getChildren().add(nameLabel);

        StackPane overlay = new StackPane();
        p.getChildren().add(overlay);
        updateCardOverlay(overlay, p, ci);

        return p;
    }

    private ImageView createCardImage(CardBean ci) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(252);
        imageView.setPreserveRatio(false);

        String imageUrl = ci.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            javafx.concurrent.Task<Image> loadTask = new javafx.concurrent.Task<>() {
                @Override
                protected Image call() {
                    try {
                        return new Image(imageUrl, true);
                    } catch (Exception _) {
                        return null;
                    }
                }
            };
            loadTask.setOnSucceeded(e -> {
                Image img = loadTask.getValue();
                if (img != null && !img.isError())
                    imageView.setImage(img);
            });
            new Thread(loadTask).start();
        }
        return imageView;
    }

    @Override
    public void close() {
        if (stage != null) {
            try {
                stage.hide();
            } catch (Exception _) {
                // Ignore errore chiusura stage
            }
        }
    }

    @Override
    public void refresh() {
        if (controller != null)
            controller.loadUserCollection();
    }

    @Override
    public void showError(String errorMessage) {
        System.err.println(errorMessage);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (CollectionController) controller;
    }

    @Override
    public void displayUserBinders(List<BinderBean> binders) {
        if (binderContainer == null)
            return;
        binderContainer.getChildren().clear();
        for (BinderBean bb : binders) {
            binderContainer.getChildren().add(createBinderView(bb));
        }
        updateEmptyState();
    }

    @Override
    public void setSaveButtonVisible(boolean isVisible) {
        if (saveBtn != null) {
            saveBtn.setVisible(isVisible);
            saveBtn.setManaged(isVisible);
        }
    }

    @Override
    public void onAddCard(model.bean.CardBean card) {
        if (card != null) {
            String msg = "Carta aggiunta: " + card.getName();
            System.out.println("[FXCollectionView] " + msg);
        }
    }

    @Override
    public void onRemoveCard(model.bean.CardBean card) {
        if (card != null) {
            String msg = "Carta rimossa: " + card.getName();
            System.out.println("[FXCollectionView] " + msg);
        }
    }

    private void updateCardOverlay(StackPane overlay, StackPane rootNode, CardBean ci) {
        overlay.getChildren().clear();
        boolean forceOwned = FORCE_OWNED_KEY.equals(rootNode.getUserData());
        boolean isEffectivelyOwned = ci.getQuantity() > 0 || forceOwned;

        rootNode.setOpacity(isEffectivelyOwned ? 1.0 : 0.4);

        if (isEffectivelyOwned) {
            updateOwnedOverlay(overlay, rootNode, ci);
        } else {
            updateUnownedOverlay(overlay, rootNode, ci);
        }
    }

    private void updateOwnedOverlay(StackPane overlay, StackPane rootNode, CardBean ci) {
        Label qtyBadge = new Label("x" + ci.getQuantity());
        qtyBadge.setStyle(
                "-fx-background-color: #2fb4f5; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;");
        StackPane.setAlignment(qtyBadge, javafx.geometry.Pos.TOP_LEFT);
        StackPane.setMargin(qtyBadge, new Insets(8));

        StackPane dimmer = new StackPane();
        dimmer.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        dimmer.setVisible(false);

        HBox controlsBox = new HBox(16);
        controlsBox.setAlignment(javafx.geometry.Pos.CENTER);
        controlsBox.setVisible(false);

        Button addBtn = createControlButton("/icons/add.png", "+", e -> modifyCardQuantity(ci, 1, rootNode, overlay));
        Button removeBtn = createControlButton("/icons/minus.png", "-",
                e -> modifyCardQuantity(ci, -1, rootNode, overlay));

        controlsBox.getChildren().addAll(removeBtn, addBtn);
        overlay.getChildren().addAll(dimmer, controlsBox, qtyBadge);

        bindHoverEffects(rootNode, dimmer, controlsBox);
    }

    private void bindHoverEffects(StackPane rootNode, StackPane dimmer, HBox controls) {
        boolean isHovering = rootNode.isHover();
        dimmer.setVisible(isHovering);
        controls.setVisible(isHovering);
        rootNode.setOnMouseEntered(e -> {
            dimmer.setVisible(true);
            controls.setVisible(true);
        });
        rootNode.setOnMouseExited(e -> {
            dimmer.setVisible(false);
            controls.setVisible(false);
        });
    }

    private void updateUnownedOverlay(StackPane overlay, StackPane rootNode, CardBean ci) {
        Button addBtn = new Button("+");
        addBtn.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand;");
        addBtn.setOnAction(e -> modifyCardQuantity(ci, 1, rootNode, overlay));

        StackPane.setAlignment(addBtn, javafx.geometry.Pos.CENTER);
        overlay.getChildren().add(addBtn);

        rootNode.setOnMouseEntered(null);
        rootNode.setOnMouseExited(null);
        rootNode.setUserData(null);
    }

    private void modifyCardQuantity(CardBean ci, int delta, StackPane rootNode, StackPane overlay) {
        if (controller != null) {
            if (delta > 0)
                controller.addCardToBinder(ci.getSetId(), ci.getId());
            else
                controller.removeCardFromBinder(ci.getSetId(), ci.getId());

            // Aggiorna la quantità nel bean per sincronizzare l'UI
            ci.setQuantity(Math.max(0, ci.getQuantity() + delta));

            // Controller aggiorna sessionBinders in memoria.
            // Al prossimo refresh la View rifletterà lo stato aggiornato.
            if (ci.getQuantity() == 0)
                rootNode.setUserData(FORCE_OWNED_KEY);
            updateCardOverlay(overlay, rootNode, ci);
        }
    }

    private Button createControlButton(String iconPath, String fallbackText,
            javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button();
        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitWidth(24);
            icon.setFitHeight(24);
            btn.setGraphic(icon);
        } catch (Exception _) {
            btn.setText(fallbackText);
        }
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        btn.setOnAction(action);
        return btn;
    }
}
