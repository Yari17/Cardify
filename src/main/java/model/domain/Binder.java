package model.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Entità di dominio che rappresenta un raccoglitore (Binder) di carte.
 * Un binder è associato a un utente e a uno specifico set di un gioco di carte.
 */
public class Binder {
    /** ID unico del binder. */
    String id;
    /** Nome dell'utente proprietario. */
    private String owner;
    /** ID del set associato. */
    private String setID;
    /** Nome visualizzabile del set. */
    private String setName;
    /** Lista delle carte effettivamente possedute in questo binder. */
    private List<CollectionItem> ownedCards;

    /**
     * Costruttore vuoto per la deserializzazione.
     */
    public Binder() {
    }

    /**
     * Costruttore completo del Binder.
     * 
     * @param owner      Proprietario.
     * @param setID      Codice del set.
     * @param setName    Nome del set.
     * @param ownedCards Carte possedute.
     */
    public Binder(String owner, String setID, String setName, List<CollectionItem> ownedCards) {
        this.owner = owner;
        this.setID = setID;
        this.setName = setName;
        this.ownedCards = ownedCards;
    }

    public String getOwner() {
        return owner;
    }

    public String getSetID() {
        return setID;
    }

    public String getSetName() {
        return setName;
    }

    public List<CollectionItem> getOwnedCards() {
        return ownedCards;
    }

    /**
     * Aggiunge una carta al binder.
     * Se la carta è già presente nell'elenco delle possedute, ne incrementa la
     * quantità
     * delegando l'operazione a {@link CollectionItem#increment()}.
     * Altrimenti, aggiunge un nuovo record con quantità iniziale pari a 1.
     * 
     * @param card L'entità Card da aggiungere.
     */
    public void addCard(Card card) {
        if (card == null)
            return;
        if (ownedCards == null)
            ownedCards = new ArrayList<>();

        String targetId = card.getCardID();
        for (CollectionItem ci : ownedCards) {
            Card c = ci.getCard();
            if (c != null && targetId != null && targetId.equals(c.getCardID())) {
                ci.increment();
                return;
            }
        }
        // Non trovata -> aggiungi come nuovo item con quantità 1
        ownedCards.add(new CollectionItem(card, 1));
    }

    /**
     * Rimuove o decrementa la presenza di una carta nel binder.
     * Delega l'operazione a {@link CollectionItem#decrement()}.
     * 
     * Nota: l'oggetto non viene rimosso fisicamente dalla lista per permettere
     * alla vista di mostrarlo con quantità zero. La persistenza filtrerà gli item
     * vuoti.
     * 
     * @param card La carta da decrementare.
     */
    public void removeCard(Card card) {
        if (card == null || ownedCards == null)
            return;
        String targetId = card.getCardID();
        for (CollectionItem ci : ownedCards) {
            Card c = ci.getCard();
            if (c != null && targetId != null && targetId.equals(c.getCardID())) {
                ci.decrement();
                return;
            }
        }
    }

}
