package model.domain.card;

import model.domain.CardGameType;

public class MagicCard extends Card {
    private String manaCost;
    private String type;
    private String text;
    private String power;
    private String toughness;

    public MagicCard(String id, String name) {
        super(id, name, CardGameType.MAGIC);
    }

    @Override
    public String getSpecificDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Magic Card Details:\n");
        details.append("Name: ").append(name).append("\n");
        details.append("Mana Cost: ").append(manaCost != null ? manaCost : "N/A").append("\n");
        details.append("Type: ").append(type).append("\n");
        details.append("Rarity: ").append(rarity).append("\n");
        details.append("Set: ").append(setName).append("\n");
        if (power != null && toughness != null) {
            details.append("P/T: ").append(power).append("/").append(toughness).append("\n");
        }
        details.append("Text: ").append(text != null ? text : "N/A").append("\n");
        return details.toString();
    }


    public String getManaCost() {
        return manaCost;
    }

    public void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    @Override
    public String toString() {
        return String.format("%s - %s [%s] (%s)",
            name, manaCost != null ? manaCost : "", type, setName);
    }
}
