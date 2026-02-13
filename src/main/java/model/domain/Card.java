package model.domain;

import model.domain.enumerations.CardGameType;

/**
 * Rappresenta un'entità Carta generica nel sistema.
 * Contiene i dati di base comuni a tutti i giochi (noma, ID, immagine).
 */
public class Card {
    /** Nome della carta. */
    private String cardName;
    /** Identificativo univoco della carta (es. ID API). */
    private String cardID;
    /** ID del set di appartenenza. */
    private String setID;
    /** URL dell'immagine della carta. */
    private String image;
    /** Tipo di gioco (es. POKEMON). */
    private CardGameType gameType;
    /**
     * Dettagli specifici del gioco (non persistiti direttamente con l'oggetto
     * Card).
     */
    private ICardDetails details;

    /** Costruttore vuoto. */
    public Card() {
    }

    /**
     * Costruttore completo per la carta.
     * 
     * @param cardName Nome della carta.
     * @param cardID   ID univoco.
     * @param setID    ID del set.
     * @param image    URL immagine.
     * @param gameType Tipo di gioco.
     */
    public Card(String cardName, String cardID, String setID, String image, CardGameType gameType) {
        this.gameType = gameType;
        this.cardName = cardName;
        this.cardID = cardID;
        this.setID = setID;
        this.image = image;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setGameType(CardGameType gameType) {
        this.gameType = gameType;
    }

    public void setSetID(String setID) {
        this.setID = setID;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCardID() {
        return cardID;
    }

    public CardGameType getGameType() {
        return gameType;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardSetID() {
        return setID;
    }

    public String getCardImage() {
        return image;
    }

    public ICardDetails getDetails() {
        return details;
    }

    public void setDetails(ICardDetails details) {
        this.details = details;
    }
}
