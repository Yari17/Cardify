package model.domain;

/**
 * Rappresenta l'associazione tra una carta e la sua quantità nella collezione.
 */
public class CollectionItem {
    /** Riferimento alla carta. */
    private Card card;
    /** Quantità posseduta. */
    private int quantity;

    public CollectionItem() {
    }

    public CollectionItem(Card card, int quantity) {
        this.card = card;
        this.quantity = quantity;
    }

    /**
     * Verifica se l'item è effettivamente posseduto (quantità > 0).
     */
    public boolean isOwned() {
        return quantity > 0;
    }

    /**
     * Incrementa la quantità posseduta.
     */
    public void increment() {
        this.quantity++;
    }

    /**
     * Decrementa la quantità posseduta, se superiore a zero.
     */
    public void decrement() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public static CollectionItem empty(Card card) {
        return new CollectionItem(card, 0);
    }

    public Card getCard() {
        return card;
    }
}
