package model.notification;

import config.AppConfig;
import model.dao.INotificationDao;
import model.dao.factory.DaoFactory;
import model.domain.Notification;
import model.domain.User;

import java.util.List;

/**
 * Servizio dedicato alla gestione e persistenza delle notifiche di sistema.
 * 
 * Implementa il pattern {@link Observer} per reagire agli eventi di notifica
 * generati
 * dai controller di business. Questo approccio garantisce un elevato
 * disaccoppiamento (Low Coupling),
 * permettendo al servizio di concentrarsi esclusivamente sulla persistenza dei
 * dati (High Cohesion).
 */
public class NotificationService implements Observer {
    private final INotificationDao notificationDao;

    /**
     * Inizializza il servizio ottenendo l'istanza corretta del DAO per le
     * notifiche basandosi sul tipo di persistenza configurato.
     */
    public NotificationService() {
        this.notificationDao = DaoFactory.getFactory(AppConfig.getPersistenceType()).createNotificationDao();
    }

    /**
     * Gestisce la ricezione di un evento di notifica e lo persiste nel sistema.
     * 
     * Il metodo sfrutta il polimorfismo del parametro {@link NotificationEvent} per
     * estrarre
     * le informazioni necessarie (destinatario e messaggio) senza conoscere i
     * dettagli specifici dell'evento.
     * 
     * @param event L'evento di notifica da processare.
     */
    @Override
    public void update(NotificationEvent event) {
        if (event == null) {
            return;
        }

        String recipient = event.getRecipientUsername();
        String message = event.getMessage();

        Notification notification = new Notification(recipient, message);
        notificationDao.addNotification(notification);
    }

    /**
     * Recupera l'elenco delle notifiche non lette per un determinato utente.
     * 
     * Utile per aggiornare l'interfaccia utente del destinatario con i nuovi avvisi
     * pendenti.
     * 
     * @param user L'utente per il quale recuperare le notifiche.
     * @return Una lista di notifiche non lette; restituisce una lista vuota se
     *         l'utente è null.
     */
    public List<Notification> getUnreadNotifications(User user) {
        if (user == null) {
            return List.of();
        }
        return notificationDao.getUnreadNotifications(user.getUsername());
    }

    /**
     * Segna una specifica notifica come letta.
     * 
     * @param notificationId L'ID univoco della notifica da aggiornare.
     */
    public void markAsRead(int notificationId) {
        notificationDao.markAsRead(notificationId);
    }
}
