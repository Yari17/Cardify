package view;

import controller.NotificationsController;
import model.bean.NotificationBean;
import java.util.List;

/**
 * Interfaccia per la visualizzazione delle notifiche di sistema.
 */
public interface INotificationsView extends IView {
    /**
     * Associa il controller specifico per la gestione delle notifiche.
     * (Specializzazione del metodo generico di IView).
     */
    void setController(NotificationsController controller);

    /** Mostra l'elenco delle notifiche all'utente. */
    void displayNotifications(List<NotificationBean> notifications);
}
