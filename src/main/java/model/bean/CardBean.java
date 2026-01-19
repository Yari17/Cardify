package model.bean;


import java.util.logging.Logger;
import model.domain.enumerations.CardGameType;

public class CardBean {
    private static final Logger LOGGER = Logger.getLogger(CardBean.class.getName());

    private String id;
    private String name;
    private String imageUrl;
    private CardGameType gameType;
    private int quantity; 
    private boolean isTradable; 
    private String status; 
    private String owner; 

    
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
        } catch (Exception _) {
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
}
