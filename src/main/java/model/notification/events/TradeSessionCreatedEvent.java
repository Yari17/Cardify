package model.notification.events;

import model.domain.TradeSession;
import model.notification.NotificationEvent;

import java.time.format.DateTimeFormatter;

/**
 * Evento generato quando viene creata una nuova sessione di scambio tra due
 * collezionisti.
 * 
 * Implementa il principio **Information Expert** notificando il negozio
 * selezionato
 * dell'appuntamento programmato, includendo i nomi dei partecipanti e la
 * data/ora.
 */
public class TradeSessionCreatedEvent extends NotificationEvent {
    private final TradeSession session;

    /**
     * Crea un nuovo evento di creazione sessione di scambio.
     * 
     * @param session La sessione di scambio appena creata.
     */
    public TradeSessionCreatedEvent(TradeSession session) {
        this.session = session;
    }

    @Override
    public String getRecipientUsername() {
        return session.getStoreId();
    }

    @Override
    public String getMessage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
        String dateTime = session.getTradeDate() != null
                ? session.getTradeDate().format(formatter)
                : "data da definire";

        return "Ci sono nuovi scambi programmati: Scambio tra "
                + session.getProposerId() + " e " + session.getReceiverId()
                + " il " + dateTime;
    }
}
