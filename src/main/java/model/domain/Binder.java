package model.domain;

import model.bean.CardBean;

import java.util.ArrayList;
import java.util.List;

public class Binder {
    private int id;
    private String ownerUsername;
    private CardGameType type; 
    private List<CardBean> ITCGCards;

    public Binder(CardGameType type) {
        this.type = type;
        this.ITCGCards = new ArrayList<>();
    }
}
