package model.notification.events;

import model.notification.NotificationEvent;

/**
 * Evento che segnala la ricezione di una nuova proposta di scambio.
 * 
 * Implementa il principio **Information Expert** incapsulando la logica di
 * notifica
 * per l'utente che deve valutare la proposta.
 */
public class ProposalReceivedEvent extends NotificationEvent {
    private final String receiver;
    private final String proposer;

    /**
     * Crea un nuovo evento di ricezione proposta.
     * 
     * @param receiver Lo username di chi riceve la proposta (destinatario della
     *                 notifica).
     * @param proposer Lo username di chi ha inviato la proposta.
     */
    public ProposalReceivedEvent(String receiver, String proposer) {
        this.receiver = receiver;
        this.proposer = proposer;
    }

    @Override
    public String getRecipientUsername() {
        return receiver;
    }

    @Override
    public String getMessage() {
        return "Hai ricevuto una nuova proposta di scambio da " + proposer + ".";
    }
}
