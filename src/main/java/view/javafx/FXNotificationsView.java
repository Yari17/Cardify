package view.javafx;

import controller.NotificationsController;
import exception.ViewException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.bean.NotificationBean;
import view.INotificationsView;
import java.io.IOException;
import java.util.List;

/**
 * Vista JavaFX per la visualizzazione delle notifiche di sistema.
 * Viene visualizzata come finestra modale "AlwaysOnTop" per informare l'utente
 * riguardo a eventi importanti come nuove proposte o cambiamenti di stato negli
 * scambi.
 */
public class FXNotificationsView implements INotificationsView {
    private Stage stage;
    private NotificationsController controller;

    @FXML
    private VBox notificationList;
    @FXML
    private Button continueBtn;

    @Override
    public void setController(NotificationsController controller) {
        this.controller = controller;
    }

    private List<NotificationBean> pendingNotifications;

    @Override
    public void displayNotifications(List<NotificationBean> notifications) {
        this.pendingNotifications = notifications;
        if (notificationList != null) {
            renderNotifications();
        }
    }

    /**
     * Renderizza la lista delle notifiche nella UI.
     * Pulisce la lista esistente e aggiunge le nuove label.
     */
    private void renderNotifications() {
        if (pendingNotifications == null || notificationList == null)
            return;

        notificationList.getChildren().clear();
        for (NotificationBean n : notificationsFromBuffer()) {
            Label label = new Label("• " + n.getMessage());
            label.getStyleClass().add("notification-item");
            notificationList.getChildren().add(label);
        }
    }

    private List<NotificationBean> notificationsFromBuffer() {
        return pendingNotifications != null ? pendingNotifications : List.of();
    }

    /**
     * Inizializza e visualizza la finestra modale delle notifiche.
     * Configura la scena con stile "APPLICATION_MODAL" e carica il layout FXML
     * dedicato.
     */
    @Override
    public void display() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NotifyPage.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            stage.setTitle("Notifiche");
            stage.setScene(new Scene(root, 400, 500));

            // Aggiungi CSS se disponibile
            java.net.URL cssUrl = getClass().getResource("/styles/login.css");
            if (cssUrl != null) {
                stage.getScene().getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.show();
        } catch (IOException e) {
            throw new ViewException("Failed to load Notifications View", e);
        }
    }

    @FXML
    public void initialize() {
        if (continueBtn != null) {
            continueBtn.setOnAction(e -> {
                if (controller != null) {
                    controller.handleContinue();
                }
            });
        }
        if (pendingNotifications != null) {
            renderNotifications();
        }
    }

    @Override
    public void setController(Object controller) {
        if (controller instanceof NotificationsController c) {
            this.controller = c;
        }
    }

    @Override
    public void refresh() {
        if (controller != null) {
            // Logica per ricaricare le notifiche se necessario
        }
    }

    @Override
    public void showError(String message) {
        // Implementa visualizzazione errore se necessario, mantenendo minimale per ora
        System.err.println("Errore: " + message);
    }

    @Override
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
