package model.bean;

import model.domain.CardGameType;


public class MagicCardBean extends CardBean {

    private String manaCost;
    private String cardType;
    private String oracleText;
    private String flavorText;
    private String power;
    private String toughness;
    private String setCode;
    private String rarity;

    public MagicCardBean() {
        super(null, null, null, CardGameType.MAGIC);
    }

    public MagicCardBean(String id, String name, String imageUrl) {
        super(id, name, imageUrl, CardGameType.MAGIC);
    }

    // Getters & Setters

    public String getManaCost() {
        return manaCost;
    }

    public void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getOracleText() {
        return oracleText;
    }

    public void setOracleText(String oracleText) {
        this.oracleText = oracleText;
    }

    public String getFlavorText() {
        return flavorText;
    }

    public void setFlavorText(String flavorText) {
        this.flavorText = flavorText;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getToughness() {
        return toughness;
    }

    public void setToughness(String toughness) {
        this.toughness = toughness;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String setCode) {
        this.setCode = setCode;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    @Override
    public String toString() {
        return "MagicCardBean{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", manaCost='" + manaCost + '\'' +
                ", cardType='" + cardType + '\'' +
                '}';
    }
}

