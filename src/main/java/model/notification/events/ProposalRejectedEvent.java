package model.notification.events;

import model.domain.User;
import model.notification.NotificationEvent;

/**
 * Evento che segnala il rifiuto di una proposta di scambio.
 * 
 * Implementa il principio **Information Expert** definendo il messaggio di
 * rifiuto
 * e identificando il proponente originale come destinatario della notifica.
 */
public class ProposalRejectedEvent extends NotificationEvent {
    private final User proposer;
    private final User receiver;

    /**
     * Crea un nuovo evento di rifiuto proposta.
     * 
     * @param proposer L'utente che aveva inviato la proposta (destinatario della
     *                 notifica).
     * @param receiver L'utente che ha rifiutato la proposta.
     */
    public ProposalRejectedEvent(User proposer, User receiver) {
        this.proposer = proposer;
        this.receiver = receiver;
    }

    @Override
    public String getRecipientUsername() {
        return proposer.getUsername();
    }

    @Override
    public String getMessage() {
        return receiver.getUsername() + " ha rifiutato la tua proposta.";
    }
}
