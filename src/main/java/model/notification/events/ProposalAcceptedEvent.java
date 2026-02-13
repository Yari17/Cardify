package model.notification.events;

import model.domain.User;
import model.notification.NotificationEvent;

/**
 * Evento che segnala l'accettazione di una proposta di scambio.
 * 
 * Implementa il principio **Information Expert** fornendo il messaggio di
 * conferma
 * e identificando il proponente originale come destinatario della notifica.
 */
public class ProposalAcceptedEvent extends NotificationEvent {
    private final User proposer;
    private final User receiver;

    /**
     * Crea un nuovo evento di accettazione proposta.
     * 
     * @param proposer L'utente che ha inviato la proposta (destinatario della
     *                 notifica).
     * @param receiver L'utente che ha accettato la proposta.
     */
    public ProposalAcceptedEvent(User proposer, User receiver) {
        this.proposer = proposer;
        this.receiver = receiver;
    }

    @Override
    public String getRecipientUsername() {
        return proposer.getUsername();
    }

    @Override
    public String getMessage() {
        return receiver.getUsername() + " ha accettato la tua proposta.";
    }
}
