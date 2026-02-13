package controller;

import model.bean.NotificationBean;
import model.bean.mapper.NotificationMapper;
import model.domain.Notification;
import model.domain.User;
import model.notification.NotificationService;
import view.INotificationsView;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller per la gestione e visualizzazione delle notifiche dell'utente.
 * Coordina il recupero delle notifiche non lette tramite il servizio di
 * notifica
 * e l'aggiornamento della relativa vista.
 */
public class NotificationsController {
    /** Utente di cui gestire le notifiche. */
    private final User currentUser;
    /** Vista associata per la visualizzazione delle notifiche. */
    private INotificationsView view;
    /**
     * Servizio per l'interazione con la persistenza e la logica delle notifiche.
     */
    private final NotificationService notificationService;

    /**
     * Costruttore del controller delle notifiche.
     * 
     * @param currentUser L'utente attualmente autenticato.
     */
    public NotificationsController(User currentUser) {
        this.currentUser = currentUser;
        this.notificationService = new NotificationService();
    }

    /**
     * Associa la vista al controller e avvia il caricamento iniziale delle
     * notifiche.
     * Delega il recupero effettivo dei dati al metodo helper
     * {@link #loadNotifications()}.
     * 
     * @param view L'istanza della vista da associare.
     */
    public void setView(INotificationsView view) {
        this.view = view;
        loadNotifications();
    }

    /**
     * Recupera le notifiche non lette dal servizio e le invia alla vista per la
     * visualizzazione.
     * Questo metodo helper garantisce che la vista mostri sempre lo stato più
     * recente disponibile.
     */
    private void loadNotifications() {
        List<Notification> unread = notificationService.getUnreadNotifications(currentUser);
        List<NotificationBean> unreadBeans = new ArrayList<>();
        if (unread != null) {
            for (Notification n : unread) {
                unreadBeans.add(NotificationMapper.toBean(n));
            }
        }
        if (view != null) {
            view.displayNotifications(unreadBeans);
        }
    }

    /**
     * Gestisce l'azione di proseguimento dell'utente.
     * Segna tutte le notifiche correnti come lette tramite il servizio e chiude la
     * vista.
     * Utile per pulire l'elenco delle notifiche dopo che l'utente le ha visionate.
     */
    public void handleContinue() {
        // Segna tutte le notifiche come lette per svuotare l'elenco
        List<Notification> unread = notificationService.getUnreadNotifications(currentUser);
        for (Notification n : unread) {
            notificationService.markAsRead(n.getId());
        }

        if (view != null) {
            view.close();
        }
    }
}
