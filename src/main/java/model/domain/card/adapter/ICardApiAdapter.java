package model.domain.card.adapter;

import model.domain.card.Card;

import java.util.List;
import java.util.Map;

public interface ICardApiAdapter<T> {
    List<Card> searchCardsByName(String name);
    List<Card> searchSet(String setID);
    Object getSetDetails(String setID);
    Card getCardById(String id);
    T getCardDetails(String id);
    Map<String,String> getAllSets();
}
