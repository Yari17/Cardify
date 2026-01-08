package model.domain;

import model.domain.card.Card;

import java.util.ArrayList;
import java.util.List;

public class Binder {
    private int id;
    private String ownerUsername;
    private CardGameType type; 
    private List<Card> cards;

    public Binder(CardGameType type) {
        this.type = type;
        this.cards = new ArrayList<>();
    }
}
