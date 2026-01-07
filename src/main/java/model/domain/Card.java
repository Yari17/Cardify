package model.domain;
public abstract class Card {
    protected int id;
    protected String name;
    protected String imageUrl;
    protected CardGameType gameType; 

    
    public abstract String getSpecificDetails();
    public abstract CardGameType getCardType();
}
