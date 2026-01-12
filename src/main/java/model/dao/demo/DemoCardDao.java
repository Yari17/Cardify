package model.dao.demo;

import config.AppConfig;
import model.dao.ICardDao;
import model.domain.card.Card;
import model.domain.card.CardProvider;
import model.domain.card.PokemonCard;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * In-memory implementation of ICardDao.
 * Caches fetched cards in memory. Data is lost on restart.
 */
public class DemoCardDao implements ICardDao {
    private static final Logger LOGGER = Logger.getLogger(DemoCardDao.class.getName());

    private final CardProvider cardProvider;
    private final Map<String, Card> cardCache;
    private final Map<String, List<Card>> setCache;

    public DemoCardDao() {
        this.cardProvider = new CardProvider();
        this.cardCache = new ConcurrentHashMap<>();
        this.setCache = new ConcurrentHashMap<>();
    }

    @Override
    public Card getCard(String cardId, String gameType) {
        if (cardId == null)
            return null;

        return cardCache.computeIfAbsent(cardId, id -> {
            LOGGER.log(java.util.logging.Level.INFO, "Demo Cache miss for card: {0}. Fetching from SDK...", id);
            return cardProvider.getCardDetails(gameType, id);
        });
    }

    @Override
    public PokemonCard getPokemonCard(String cardId) {
        Card card = getCard(cardId, AppConfig.POKEMON_GAME);
        return (card instanceof PokemonCard p) ? p : null;
    }

    @Override
    public List<Card> searchCards(String query, String gameType) {
        LOGGER.log(java.util.logging.Level.INFO, "Searching cards: {0}", query);
        List<Card> results = cardProvider.searchCardsByName(gameType, query);
        if (results != null) {
            results.forEach(c -> cardCache.put(c.getId(), c));
        }
        return results;
    }

    @Override
    public List<Card> getSetCards(String setId, String gameType) {
        if (setId == null)
            return java.util.Collections.emptyList();

        return setCache.computeIfAbsent(setId, id -> {
            LOGGER.log(java.util.logging.Level.INFO, "Demo Cache miss for set: {0}. Fetching from SDK...", id);
            List<Card> cards = cardProvider.searchSet(gameType, id);
            if (cards != null) {
                cards.forEach(c -> cardCache.put(c.getId(), c));
            }
            return cards;
        });
    }

    @Override
    public Map<String, String> getAllSets(String gameType) {
        if (AppConfig.POKEMON_GAME.equals(gameType)) {
            return cardProvider.getPokemonSets();
        }
        return java.util.Collections.emptyMap();
    }
}
