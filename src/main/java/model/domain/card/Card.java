package model.domain.card;

import model.domain.CardGameType;

import java.util.Objects;

public abstract class Card {
    protected String id;
    protected String name;
    protected String imageUrl;
    protected CardGameType gameType;
    protected String rarity;
    protected String artist;
    protected String setName;
    protected String number;


    protected Card(String id, String name, CardGameType gameType) {
        this.id = id;
        this.name = name;
        this.gameType = gameType;
    }

    public abstract String getSpecificDetails();

    public CardGameType getCardType() {
        return gameType;
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

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


  }
