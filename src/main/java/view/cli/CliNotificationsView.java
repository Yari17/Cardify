package view.cli;

import controller.NotificationsController;
import model.bean.NotificationBean;
import view.INotificationsView;

import java.util.List;

/**
 * Implementazione CLI per la visualizzazione delle notifiche di sistema.
 * Mostra una lista testuale dei messaggi non ancora letti o recenti.
 */
public class CliNotificationsView implements INotificationsView {
    private NotificationsController controller;

    @Override
    /**
     * Imposta il controller specifico per le notifiche.
     * 
     * @param controller Il controller da associare.
     */
    public void setController(NotificationsController controller) {
        this.controller = controller;
    }

    /**
     * Stampa a video la lista delle notifiche fornite dal controller.
     * 
     * @param notifications Lista di oggetti Notification da visualizzare.
     */
    @Override
    public void displayNotifications(List<NotificationBean> notifications) {
        System.out.println("\n=== NOTIFICHE ===");
        if (notifications == null || notifications.isEmpty()) {
            System.out.println("Nessuna nuova notifica.");
        } else {
            for (NotificationBean n : notifications) {
                System.out.println("- " + n.getMessage());
            }
        }
        System.out.println("=================");
    }

    @Override
    public void close() {
        // Nessuna operazione
    }

    @Override
    public void refresh() {
        // Nessuna operazione
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("ERRORE Notifiche: " + errorMessage);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (NotificationsController) controller;
    }

    @Override
    public void display() {
        if (controller != null) {
            // La logica di visualizzazione è gestita dal metodo displayNotifications
        }
    }
}
