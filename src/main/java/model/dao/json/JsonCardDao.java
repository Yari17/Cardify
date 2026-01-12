package model.dao.json;

import com.google.gson.*;
import model.dao.ICardDao;
import model.domain.card.Card;
import model.domain.card.CardProvider;
import model.domain.card.PokemonCard;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ICardDao that persists fetched cards to a JSON file.
 * Uses a local file to cache card data between application restarts.
 */
public class JsonCardDao implements ICardDao {
    private static final Logger LOGGER = Logger.getLogger(JsonCardDao.class.getName());

    private final CardProvider cardProvider;
    private final String jsonFilePath;
    private final Gson gson;

    // In-memory cache loaded from JSON
    private final Map<String, Card> cardCache;
    // Map to track if a set was fully loaded/cached?
    // For simplicity, we just cache individual cards.
    // Can we cache lists of cards for sets?
    // Let's cache set lists as a Map<SetId, List<String (CardIds)>> to save space,
    // but for JSON simplicity we can just cache the full structure if size isn't
    // huge.
    // Given the previous requirement "maintain accessed info", we'll just cache
    // what we fetch.

    private static class CardsData {
        Map<String, Card> cards = new ConcurrentHashMap<>();
        // we could cache sets too, but re-fetching set from IDs is fast if cards are
        // cached.
    }

    private final CardsData data;

    public JsonCardDao(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        this.cardProvider = new CardProvider();
        this.cardCache = new ConcurrentHashMap<>();
        this.data = new CardsData();

        // Configure Gson with custom deserializer for polymorphism
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Card.class, new CardDeserializer())
                .create();

        loadFromJson();
    }

    private static class CardDeserializer implements JsonDeserializer<Card> {
        @Override
        public Card deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement gameTypeElem = jsonObject.get("gameType");

            if (gameTypeElem != null) {
                String gameType = gameTypeElem.getAsString();
                if (config.AppConfig.POKEMON_GAME.equals(gameType)) {
                    return context.deserialize(json, PokemonCard.class);
                }
            }
            // Default to base Card or try to infer?
            // If gameType is missing, it might be a base card or issue.
            return context.deserialize(json, Card.class);
        }
    }

    private void loadFromJson() {
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            return;
        }

        try (Reader reader = new FileReader(file)) {
            CardsData loadedData = gson.fromJson(reader, CardsData.class);
            if (loadedData != null && loadedData.cards != null) {
                data.cards.putAll(loadedData.cards);
                cardCache.putAll(loadedData.cards);
                LOGGER.log(Level.INFO, "Loaded {0} cards from JSON cache.", cardCache.size());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading cards JSON", e);
        }
    }

    private synchronized void saveToJson() {
        // Ensure directory exists
        File file = new File(jsonFilePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save cards to JSON", e);
        }
    }

    @Override
    public Card getCard(String cardId, String gameType) {
        if (cardId == null)
            return null;

        // Check cache
        if (cardCache.containsKey(cardId)) {
            LOGGER.log(Level.INFO, "JSON Cache hit for card: {0}", cardId);
            return cardCache.get(cardId);
        }

        // Fetch from SDK
        LOGGER.log(Level.INFO, "JSON Cache miss for card: {0}. Fetching...", cardId);
        Card card = cardProvider.getCardDetails(gameType, cardId);

        if (card != null) {
            cardCache.put(cardId, card);
            data.cards.put(cardId, card);
            saveToJson(); // Persist update
        }
        return card;
    }

    @Override
    public PokemonCard getPokemonCard(String cardId) {
        Card card = getCard(cardId, config.AppConfig.POKEMON_GAME);
        return (card instanceof PokemonCard p) ? p : null;
    }

    @Override
    public List<Card> searchCards(String query, String gameType) {
        // Search is always live from SDK to get latest results,
        // but we cache the details of found cards.
        LOGGER.log(Level.INFO, "Searching cards: {0}", query);
        List<Card> results = cardProvider.searchCardsByName(gameType, query);

        if (results != null) {
            boolean changed = false;
            for (Card card : results) {
                if (!cardCache.containsKey(card.getId())) {
                    cardCache.put(card.getId(), card);
                    data.cards.put(card.getId(), card);
                    changed = true;
                }
            }
            if (changed)
                saveToJson();
        }
        return results;
    }

    @Override
    public List<Card> getSetCards(String setId, String gameType) {
        // We don't cache "Set -> List<Card>" explicitly in JSON to avoid duplication
        // if cards belong to multiple sets (unlikely but possible) or stale lists.
        // We fetch from SDK and cache the individual cards.
        // Optimization: We could have a separate cache for SetID -> List<CardID>

        LOGGER.log(Level.INFO, "Fetching set cards for: {0}", setId);
        List<Card> cards = cardProvider.searchSet(gameType, setId);

        if (cards != null) {
            boolean changed = false;
            for (Card card : cards) {
                if (!cardCache.containsKey(card.getId())) {
                    cardCache.put(card.getId(), card);
                    data.cards.put(card.getId(), card);
                    changed = true;
                }
            }
            if (changed)
                saveToJson();
        }
        return cards;
    }

    @Override
    public Map<String, String> getAllSets(String gameType) {
        // Sets list is also fetched live
        if ("pokemon".equals(gameType)) {
            return cardProvider.getPokemonSets();
        }
        return java.util.Collections.emptyMap();
    }
}
