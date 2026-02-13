package model.domain;

import model.domain.enumerations.ProposalStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rappresenta una proposta di scambio tra due collezionisti (proponente e
 * ricevente).
 * Include l'elenco delle carte offerte e richieste, lo stato della proposta e
 * i dettagli logistici dell'ultimo appuntamento concordato.
 */
public class Proposal {
    /** Identificativo univoco della proposta. */
    private String id;
    /** L'utente che ha formulato la proposta. */
    private User proposer;
    /** L'utente che riceve la proposta. */
    private User receiver;
    /** Lista di carte che il proponente offre nello scambio. */
    private List<CollectionItem> proposedItems;
    /** Lista di carte che il proponente chiede in cambio. */
    private List<CollectionItem> askedItems;
    /** Stato corrente della proposta (PENDING, ACCEPTED, REJECTED). */
    private ProposalStatus status;
    /** Il negozio fisico concordato per lo scambio. */
    private User meetingStore;
    /** Timestamp di creazione della proposta. */
    private LocalDateTime createdAt;
    /** Timestamp programmato per l'incontro (se accettata). */
    private LocalDateTime scheduledAt;

    /**
     * Costruttore completo della proposta.
     * Inizializza lo stato a PENDING e imposta la data di creazione al momento
     * attuale.
     * 
     * @param id            ID univoco.
     * @param proposer      Utente proponente.
     * @param receiver      Utente ricevente.
     * @param proposedItems Carte offerte.
     * @param askedItems    Carte richieste.
     * @param meetingStore  Negozi di riferimento per l'incontro.
     */
    public Proposal(String id, User proposer, User receiver, List<CollectionItem> proposedItems,
            List<CollectionItem> askedItems, User meetingStore) {
        this.id = id;
        this.proposer = proposer;
        this.receiver = receiver;
        this.proposedItems = proposedItems;
        this.askedItems = askedItems;
        this.meetingStore = meetingStore;
        this.status = ProposalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.scheduledAt = null;
    }

    /**
     * Accetta formalmente la proposta cambiando lo stato in ACCEPTED.
     */
    public void accept() {
        this.status = ProposalStatus.ACCEPTED;
    }

    /**
     * Rifiuta formalmente la proposta cambiando lo stato in REJECTED.
     */
    public void reject() {
        this.status = ProposalStatus.REJECTED;
    }

    /**
     * Restituisce l'identificativo della proposta.
     * 
     * @return L'ID univoco.
     */
    public String getId() {
        return id;
    }

    /**
     * Restituisce l'utente proponente.
     * 
     * @return L'istanza di User proponente.
     */
    public User getProposer() {
        return proposer;
    }

    /**
     * Restituisce l'utente ricevente.
     * 
     * @return L'istanza di User ricevente.
     */
    public User getReceiver() {
        return receiver;
    }

    /**
     * Restituisce la lista di carte offerte.
     * 
     * @return Lista di {@link CollectionItem} proposti.
     */
    public List<CollectionItem> getProposedItems() {
        return proposedItems;
    }

    /**
     * Restituisce la lista di carte richieste.
     * 
     * @return Lista di {@link CollectionItem} chiesti.
     */
    public List<CollectionItem> getAskedItems() {
        return askedItems;
    }

    /**
     * Metodo helper per recuperare il primo item richiesto (supporto legacy).
     * 
     * @return Il primo elemento della lista {@code askedItems} o {@code null}.
     */
    public CollectionItem getAskedItem() {
        return (askedItems != null && !askedItems.isEmpty()) ? askedItems.get(0) : null;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public void setMeetingStore(User meetingStore) {
        this.meetingStore = meetingStore;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public User getMeetingStore() {
        return meetingStore;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
