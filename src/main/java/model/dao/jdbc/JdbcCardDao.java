package model.dao.jdbc;

import com.google.gson.*;
import model.dao.ICardDao;
import model.domain.card.Card;
import model.domain.card.CardProvider;
import model.domain.card.PokemonCard;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ICardDao that uses JDBC for caching card data.
 * Fetched cards are persisted to the database.
 */
public class JdbcCardDao implements ICardDao {
    private static final Logger LOGGER = Logger.getLogger(JdbcCardDao.class.getName());

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final CardProvider cardProvider;
    private final Gson gson;

    public JdbcCardDao(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.cardProvider = new CardProvider();

        // Configure Gson with custom deserializer
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Card.class, new CardDeserializer())
                .create();

        initializeDatabase();
    }

    // Duplicate deserializer for isolation (or could be moved to shared utility)
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
            return context.deserialize(json, Card.class);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    private void initializeDatabase() {
        String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS cards_cache (
                        id VARCHAR(255) PRIMARY KEY,
                        json_data LONGTEXT NOT NULL,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                """;

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error initializing cards_cache table", e);
        }
    }

    private void saveToCache(Card card) {
        String sql = "INSERT INTO cards_cache (id, json_data) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE json_data = VALUES(json_data)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String json = gson.toJson(card);
            pstmt.setString(1, card.getId());
            pstmt.setString(2, json);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to cache card: {0}", card.getId());
            LOGGER.log(Level.WARNING, "Exception: ", e);
        }
    }

    private Card getFromCache(String cardId) {
        String sql = "SELECT json_data FROM cards_cache WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cardId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String json = rs.getString("json_data");
                return gson.fromJson(json, Card.class);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error reading from card cache", e);
        }
        return null;
    }

    @Override
    public Card getCard(String cardId, String gameType) {
        if (cardId == null)
            return null;

        // Try DB cache
        Card cached = getFromCache(cardId);
        if (cached != null) {
            LOGGER.log(Level.INFO, "JDBC Cache hit for card: {0}", cardId);
            return cached;
        }

        // Fetch from SDK
        LOGGER.log(Level.INFO, "JDBC Cache miss for card: {0}. Fetching...", cardId);
        Card card = cardProvider.getCardDetails(gameType, cardId);

        if (card != null) {
            saveToCache(card); // Persist
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
        LOGGER.log(Level.INFO, "Searching cards: {0}", query);
        List<Card> results = cardProvider.searchCardsByName(gameType, query);

        if (results != null) {
            for (Card card : results) {
                // Determine if we should check DB before saving?
                // Given search results might be newer, blindly updating is fine or checking
                // existence.
                // UPSERT handles it.
                saveToCache(card);
            }
        }
        return results;
    }

    @Override
    public List<Card> getSetCards(String setId, String gameType) {
        LOGGER.log(Level.INFO, "Fetching set cards for: {0}", setId);
        List<Card> cards = cardProvider.searchSet(gameType, setId);

        if (cards != null) {
            for (Card card : cards) {
                saveToCache(card);
            }
        }
        return cards;
    }

    @Override
    public Map<String, String> getAllSets(String gameType) {
        if ("pokemon".equals(gameType)) {
            return cardProvider.getPokemonSets();
        }
        return java.util.Collections.emptyMap();
    }
}
