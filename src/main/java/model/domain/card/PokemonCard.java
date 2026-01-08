package model.domain.card;

import model.domain.CardGameType;

import java.util.ArrayList;
import java.util.List;

public class PokemonCard extends Card {
    private String hp;
    private List<String> types;
    private List<Attack> attacks;
    private String evolvesFrom;

    public PokemonCard(String id, String name) {
        super(id, name, CardGameType.POKEMON);
        this.types = new ArrayList<>();
        this.attacks = new ArrayList<>();
    }

    @Override
    public String getSpecificDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Pokemon Card Details:\n");
        details.append("Name: ").append(name).append("\n");
        details.append("HP: ").append(hp != null ? hp : "N/A").append("\n");
        details.append("Type(s): ").append(types).append("\n");
        if (evolvesFrom != null && !evolvesFrom.isEmpty()) {
            details.append("Evolves from: ").append(evolvesFrom).append("\n");
        }
        details.append("Rarity: ").append(rarity).append("\n");
        details.append("Set: ").append(setName).append("\n");
        if (attacks != null && !attacks.isEmpty()) {
            details.append("Attacks:\n");
            for (Attack attack : attacks) {
                details.append("  - ").append(attack.getName())
                       .append(" (").append(attack.getDamage()).append(")\n");
            }
        }
        return details.toString();
    }

    public String getHp() {
        return hp;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<Attack> getAttacks() {
        return attacks;
    }

    public void setAttacks(List<Attack> attacks) {
        this.attacks = attacks;
    }

    public String getEvolvesFrom() {
        return evolvesFrom;
    }

    public void setEvolvesFrom(String evolvesFrom) {
        this.evolvesFrom = evolvesFrom;
    }

    @Override
    public String toString() {
        return String.format("%s - HP:%s %s (%s)",
            name, hp != null ? hp : "?", types, setName);
    }

    public static class Attack {
        private String name;
        private String damage;
        private String text;

        public Attack() {}

        public Attack(String name, String damage) {
            this.name = name;
            this.damage = damage;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDamage() {
            return damage;
        }

        public void setDamage(String damage) {
            this.damage = damage;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return name + " - " + damage;
        }
    }
}
