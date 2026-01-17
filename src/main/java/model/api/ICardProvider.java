package model.api;

import model.domain.Card;

import java.util.List;
import java.util.Map;

import exception.ConnectionException;

public interface ICardProvider {
    public List<Card> searchSet(String setId) throws ConnectionException;
    public List<Card> searchCardsByName(String cardName) throws ConnectionException;
    public <T extends Card> T getCardDetails(String cardId) throws ConnectionException;
    public Map<String, String> getAllSets() throws ConnectionException;
}
