package model.bean;

import java.util.logging.Logger;
import model.domain.enumerations.CardGameType;
import model.domain.ICardDetails;

/**
 * Bean per il trasporto dei dati di una carta tra i vari livelli
 * dell'applicazione.
 * Utilizzato principalmente per la visualizzazione nelle View e il passaggio
 * di informazioni semplificate dal Controller.
 */
public class CardBean {
    /** Logger per la classe. */
    private static final Logger LOGGER = Logger.getLogger(CardBean.class.getName());

    /** Identificativo unico della carta. */
    private String id;
    /** Nome della carta. */
    private String name;
    /** URL dell'immagine della carta. */
    private String imageUrl;
    /** Tipo di gioco (es. POKEMON). */
    private CardGameType gameType;
    /** Quantità totale posseduta nel contesto corrente. */
    private int quantity;
    /** Indica se la carta è disponibile per lo scambio. */
    private boolean isTradable;
    /** Stato della carta (es. Nuova, Usata). */
    private String status;
    /** Nome utente del proprietario. */
    private String owner;
    /** ID del set di appartenenza. */
    private String setId;
    /** Dettagli specifici del gioco (implementazione di ICardDetails). */
    private ICardDetails details;
    /**
     * Quantità disponibile per nuove offerte (esclude quelle già impegnate in
     * scambi).
     */
    private int remainingQuantity;

    /**
     * Costruttore vuoto.
     * Unico costruttore disponibile come richiesto.
     */
    public CardBean() {
        this.id = null;
        this.name = null;
        this.imageUrl = null;
        this.gameType = null;
        this.quantity = 1;
        this.isTradable = false;
        this.status = "";
        this.owner = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public CardGameType getGameType() {
        return gameType;
    }

    public void setGameType(CardGameType gameType) {
        this.gameType = gameType;
    }

    public void setGameType(String gameTypeName) {
        if (gameTypeName == null) {
            this.gameType = null;
            return;
        }
        try {
            this.gameType = CardGameType.valueOf(gameTypeName);
        } catch (Exception _) { // Ignora eccezione come nel codice originale
            LOGGER.fine(() -> "Failed to parse gameType: " + gameTypeName);
            this.gameType = null;
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isTradable() {
        return isTradable;
    }

    public void setTradable(boolean tradable) {
        isTradable = tradable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "";
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public ICardDetails getDetails() {
        return details;
    }

    public void setDetails(ICardDetails details) {
        this.details = details;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}
