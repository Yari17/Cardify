package model.domain;

import model.bean.CardBean;

public class Card {
    protected String id;
    protected String name;
    protected String imageUrl;
    protected CardGameType gameType;

    public Card(String id, String name, String imageUrl, CardGameType gameType) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.gameType = gameType;
    }

    public  CardBean toBean(){
        return new CardBean(
            this.id,
            this.name,
            this.imageUrl,
            this.gameType
        );
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public CardGameType getGameType() {
        return gameType;
    }
}
