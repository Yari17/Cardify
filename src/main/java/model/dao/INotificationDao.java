package model.dao;

import model.domain.Notification;
import java.util.List;

/**
 * Interfaccia per la persistenza delle notifiche utente.
 * Gestisce la creazione di avvisi e il tracciamento dello stato di lettura.
 */
public interface INotificationDao {
    /**
     * Crea e invia una nuova notifica a un utente.
     * 
     * @param notification L'oggetto notifica da persistere.
     */
    void addNotification(Notification notification);

    /**
     * Recupera tutte le notifiche non ancora lette per un determinato utente.
     * 
     * @param userId ID dell'utente destinatario.
     * @return Lista di notifiche non lette.
     */
    List<Notification> getUnreadNotifications(String userId);

    /**
     * Aggiorna lo stato di una notifica impostandola come letta.
     * 
     * @param notificationId ID della notifica da aggiornare.
     */
    void markAsRead(int notificationId);
}
