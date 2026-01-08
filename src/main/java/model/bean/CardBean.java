package model.bean;

import model.domain.CardGameType;

public class CardBean {
    private String id;
    private String name;
    private String imageUrl;
    private CardGameType gameType;

    public CardBean(String id, String name, String imageUrl, CardGameType gameType) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.gameType = gameType;
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
}
