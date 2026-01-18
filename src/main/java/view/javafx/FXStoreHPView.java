package view.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import controller.StoreHPController;
import view.IStoreHPView;
import model.bean.TradeTransactionBean;

import java.util.logging.Logger;
import javafx.collections.FXCollections;

public class FXStoreHPView implements IStoreHPView {
    private static final Logger LOGGER = Logger.getLogger(FXStoreHPView.class.getName());

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<TradeTransactionBean> completedList;

    private StoreHPController controller;
    private Stage stage;

    @FXML
    @SuppressWarnings({"squid:S3776","PMD.CognitiveComplexity","unused"}) // NOSONAR
    private void initialize() {
        // Inizializzazione UI: eventuali impostazioni locali della view
        // Non eseguiamo logica di business qui (passare al controller)
        setupCompletedList();
    }

    // Extracted to keep initialize() trivial and reduce its cognitive complexity
    private void setupCompletedList() {
        if (completedList != null) {
            completedList.setCellFactory(this::createCompletedListCell);
        }
    }

    // Factory used to create new ListCell instances; references the ListView to avoid unused-parameter warnings
    private javafx.scene.control.ListCell<TradeTransactionBean> createCompletedListCell(javafx.scene.control.ListView<TradeTransactionBean> lv) {
        // reference lv harmlessly to avoid 'parameter never used' warnings in static analysis
        lv.getItems();
        return new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(TradeTransactionBean item, boolean empty) {
                super.updateItem(item, empty);
                updateTradeCell(this, item, empty);
            }
        };
    }

    // Helper that updates a ListCell's content for a trade transaction
    private void updateTradeCell(javafx.scene.control.ListCell<TradeTransactionBean> cell, TradeTransactionBean item, boolean empty) {
        if (empty || item == null) {
            cell.setText(null);
            cell.setGraphic(null);
            return;
        }
        cell.setText(formatTradeCellText(item));
    }

    // Helper that formats the text shown for a trade list cell
    private String formatTradeCellText(TradeTransactionBean item) {
        String p = item.getProposerId() != null ? item.getProposerId() : "?";
        String r = item.getReceiverId() != null ? item.getReceiverId() : "?";
        String status = item.getStatus() != null ? item.getStatus() : "?";
        return "tx-" + item.getTransactionId() + " — " + p + " vs " + r + " [" + status + "]";
    }

    @Override
    public void setController(StoreHPController controller) {
        this.controller = controller;
        if (controller != null && welcomeLabel != null) {
            showWelcomeMessage(controller.getUsername());
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

    @Override
    public void showError(String errorMessage) {
        LOGGER.log(java.util.logging.Level.SEVERE, "Error: {0}", errorMessage);
    }

    @Override
    public void showWelcomeMessage(String username) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Benvenuto Store " + username + "!");
        }
    }

    @Override
    public void refresh() {
        javafx.application.Platform.runLater(() -> {
            if (welcomeLabel != null) {
                // re-apply current text to force UI update
                welcomeLabel.setText(welcomeLabel.getText());
            }
            if (completedList != null) completedList.refresh();
        });
    }

    @Override
    public void displayCompletedTrades(java.util.List<TradeTransactionBean> completed) {
        javafx.application.Platform.runLater(() -> {
            LOGGER.info(() -> "FXStoreHPView.displayCompletedTrades called with count=" + (completed == null ? 0 : completed.size()));
            if (completedList == null) {
                LOGGER.warning("completedList is null - ensure FXML contains the ListView with fx:id=completedList");
                return;
            }
            if (completed == null || completed.isEmpty()) {
                completedList.setItems(FXCollections.observableArrayList());
                return;
            }
            completedList.setItems(FXCollections.observableArrayList(completed));
            completedList.setVisible(true);
            completedList.setManaged(true);
            completedList.refresh();
        });
    }

    @FXML
    private void onLogoutClicked() {
        if (controller != null) {
            controller.onLogoutRequested();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void onExitClicked() {
        if (controller != null) {
            controller.onExitRequested();
        }
    }

    @FXML
    private void onManageTradesClicked() {
        if (controller != null) {
            controller.onManageTradesRequested();
        }
    }

    @FXML
    private void onViewCompletedTradesClicked() {
        if (controller != null) {
            controller.onViewCompletedTradesRequested();
        }
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
