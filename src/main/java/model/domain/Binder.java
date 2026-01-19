package model.domain;

import model.bean.CardBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Binder {
    private long id;
    private String owner;
    private String setId;
    private String setName;
    private List<CardBean> cards;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;

    public Binder() {
        this.cards = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    public Binder(String owner, String setId, String setName) {
        this();
        this.owner = owner;
        this.setId = setId;
        this.setName = setName;
    }

    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public List<CardBean> getCards() {
        return new ArrayList<>(cards);
    }

    public void setCards(List<CardBean> cards) {
        this.cards = cards != null ? new ArrayList<>(cards) : new ArrayList<>();
        updateLastModified();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    
    public void addCard(CardBean card) {
        if (card == null) return;

        
        for (CardBean cb : this.cards) {
            if (cb != null && cb.getId() != null && cb.getId().equals(card.getId())) {
                int newQty = cb.getQuantity() + (card.getQuantity() > 0 ? card.getQuantity() : 1);
                cb.setQuantity(newQty);
                updateLastModified();
                return;
            }
        }

        
        this.cards.add(new CardBean(card));
        updateLastModified();
    }

    public void removeCard(String cardId) {
        this.cards.removeIf(card -> card.getId().equals(cardId));
        updateLastModified();
    }

    public int getCardCount() {
        return this.cards.size();
    }

    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }
}
