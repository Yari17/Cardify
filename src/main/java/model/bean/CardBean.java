package model.bean;

import model.domain.CardGameType;

public class CardBean {
    private String id;
    private String name;
    private String imageUrl;
    private CardGameType gameType;
    private int quantity; // Per i binder - quante copie possiede l'utente
    private boolean isTradable; // Per i binder - se la carta è disponibile per il trade

    public CardBean(String id, String name, String imageUrl, CardGameType gameType) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.gameType = gameType;
        this.quantity = 1; // Default: 1 copia
        this.isTradable = false; // Default: non disponibile per trade
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
}
