package model.api;

import model.domain.Card;

import java.util.List;
import java.util.Map;

public interface ICardProvider {
    public List<Card> searchSet(String setId);
    public List<Card> searchCardsByName(String cardName);
    public <T extends Card> T getCardDetails(String cardId);
    public Map<String, String> getAllSets();
}
