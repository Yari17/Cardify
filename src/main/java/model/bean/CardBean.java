package model.bean;

import model.domain.CardGameType;

public class CardBean {
    private String id;
    private String name;
    private String imageUrl;
    private CardGameType gameType;
    private int quantity; // Per i binder - quante copie possiede l'utente
    private boolean isTradable; // Per i binder - se la carta è disponibile per il trade
    private String status; // Stato libero per estensioni (es. "", "requested", "reserved")
    private String owner; // Optional: the owner username for display purposes

    // Default constructor required by some JSON deserializers (Gson)
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

    // Copy constructor
    public CardBean(CardBean other) {
        if (other == null) {
            this.id = null;
            this.name = null;
            this.imageUrl = null;
            this.gameType = null;
            this.quantity = 1;
            this.isTradable = false;
            this.status = "";
            this.owner = null;
            return;
        }
        this.id = other.id;
        this.name = other.name;
        this.imageUrl = other.imageUrl;
        this.gameType = other.gameType;
        this.quantity = other.quantity;
        this.isTradable = other.isTradable;
        this.status = other.status;
        this.owner = other.owner;
    }

    public CardBean(String id, String name, String imageUrl, CardGameType gameType) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.gameType = gameType;
        this.quantity = 1; // Default: 1 copia
        this.isTradable = false; // Default: non disponibile per trade
        this.status = ""; // Default empty
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


    public CardGameType getGameType() {
        return gameType;
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
}
