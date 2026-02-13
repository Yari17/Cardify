package model.dao.demo;

import model.dao.INotificationDao;
import model.domain.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione in-memory del DAO per le notifiche (modalità Demo).
 * Gestisce l'invio e la lettura degli avvisi utente durante la sessione.
 */
public class DemoNotificationDao implements INotificationDao {

    /** Archivio locale delle notifiche. */
    private final List<Notification> notifications = new ArrayList<>();
    /** Contatore per la generazione di ID univoci. */
    private int idCounter = 1;

    @Override
    public void addNotification(Notification notification) {
        if (notification == null) {
            return;
        }

        // Assegna ID univoco se non già presente
        int id = notification.getId() > 0 ? notification.getId() : idCounter++;

        // Crea nuova istanza con ID assegnato
        Notification withId = new Notification(id, notification.getUserId(),
                notification.getMessage(),
                notification.isRead());

        notifications.add(withId);
    }

    @Override
    public List<Notification> getUnreadNotifications(String userId) {
        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }

        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.getUserId().equals(userId) && !n.isRead()) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public void markAsRead(int notificationId) {
        for (Notification n : notifications) {
            if (n.getId() == notificationId) {
                n.setRead(true);
                break;
            }
        }
    }
}
