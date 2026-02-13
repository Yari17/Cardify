package model.notification.events;

import model.domain.TradeSession;
import model.domain.User;
import model.notification.NotificationEvent;

/**
 * Evento generato quando un partecipante arriva fisicamente in negozio per uno
 * scambio.
 * 
 * Implementa il principio **Information Expert** calcolando dinamicamente il
 * destinatario
 * della notifica (l'altro partecipante allo scambio) e fornendo un messaggio di
 * avviso
 * per facilitare l'incontro nel punto vendita.
 */
public class UserInStoreEvent extends NotificationEvent {
    private final TradeSession session;
    private final User arrivedUser;

    /**
     * Crea un nuovo evento di arrivo in negozio.
     * 
     * @param session     La sessione di scambio di riferimento.
     * @param arrivedUser L'utente (collezionista) che è appena stato validato dal
     *                    negozio.
     */
    public UserInStoreEvent(TradeSession session, User arrivedUser) {
        this.session = session;
        this.arrivedUser = arrivedUser;
    }

    @Override
    public String getRecipientUsername() {
        // Il destinatario è l'altro utente nella sessione di scambio
        if (arrivedUser.getUsername().equals(session.getProposerId())) {
            return session.getReceiverId();
        } else {
            return session.getProposerId();
        }
    }

    @Override
    public String getMessage() {
        return arrivedUser.getUsername() + " è arrivato in negozio per lo scambio #"
                + session.getSessionId() + ".";
    }
}
