package model.dao;

import model.domain.card.Card;
import model.domain.card.PokemonCard;

import java.util.List;

/**
 * Interface for Card Data Access Object.
 * Handles the retrieval of Card data from various sources (cache, API, etc.).
 */
public interface ICardDao {

    /**
     * Gets the full details of a card by its ID.
     *
     * @param cardId   the unique identifier of the card
     * @param gameType the game type (e.g. "pokemon")
     * @return the Card object, or null if not found
     */
    Card getCard(String cardId, String gameType);

    /**
     * Convenience method for Pokemon cards.
     */
    PokemonCard getPokemonCard(String cardId);

    /**
     * Searches for cards matching the query.
     *
     * @param query    the search term
     * @param gameType the game type
     * @return list of matching cards
     */
    List<Card> searchCards(String query, String gameType);

    /**
     * Retrieves all cards in a specific set.
     *
     * @param setId    the unique identifier of the set
     * @param gameType the game type
     * @return list of cards in the set
     */
    List<Card> getSetCards(String setId, String gameType);

    /**
     * Retrieves all available sets for a game.
     *
     * @param gameType the game type (e.g. "pokemon")
     * @return map of set ID to set name
     */
    java.util.Map<String, String> getAllSets(String gameType);
}
