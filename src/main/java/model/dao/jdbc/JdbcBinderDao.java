package model.dao.jdbc;

import model.dao.IBinderDao;
import model.domain.Binder;
import exception.DataPersistenceException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcBinderDao implements IBinderDao {
    private static final Logger LOGGER = Logger.getLogger(JdbcBinderDao.class.getName());
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final model.dao.ICardDao cardDao;

    public JdbcBinderDao(String jdbcUrl, String dbUser, String dbPassword, model.dao.ICardDao cardDao) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.cardDao = cardDao;
        initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    private void initializeDatabase() {
        String createBindersTable = """
                    CREATE TABLE IF NOT EXISTS binders (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        owner VARCHAR(255) NOT NULL,
                        set_id VARCHAR(255),
                        set_name VARCHAR(255),
                        set_logo VARCHAR(255),
                        created_at TIMESTAMP,
                        last_modified TIMESTAMP
                    )
                """;

        String createBinderCardsTable = """
                    CREATE TABLE IF NOT EXISTS binder_cards (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        binder_id BIGINT NOT NULL,
                        card_id VARCHAR(255) NOT NULL,
                        card_name VARCHAR(255),
                        card_image_url VARCHAR(512),
                        game_type VARCHAR(50),
                        quantity INT DEFAULT 1,
                        is_tradable BOOLEAN DEFAULT FALSE,
                        FOREIGN KEY (binder_id) REFERENCES binders(id) ON DELETE CASCADE
                    )
                """;

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createBindersTable);
            stmt.execute(createBinderCardsTable);
            LOGGER.info("Binders and binder_cards tables initialized");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error initializing binders database", e);
        }
    }

    @Override
    public Optional<Binder> get(long id) {
        String sql = "SELECT id, owner, set_id, set_name, set_logo, created_at, last_modified FROM binders WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Binder binder = mapResultSetToBinder(rs);
                loadBinderCards(conn, binder);
                return Optional.of(binder);
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to get binder with id: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public List<Binder> getAll() {
        String sql = "SELECT id, owner, set_id, set_name, set_logo, created_at, last_modified FROM binders";
        List<Binder> binders = new ArrayList<>();

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Binder binder = mapResultSetToBinder(rs);
                loadBinderCards(conn, binder);
                binders.add(binder);
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to get all binders", e);
        }

        return binders;
    }

    @Override
    public void save(Binder binder) {
        String sql = """
                    INSERT INTO binders (owner, set_id, set_name, set_logo, created_at, last_modified)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, binder.getOwner());
            pstmt.setString(2, binder.getSetId());
            pstmt.setString(3, binder.getSetName());
            pstmt.setString(4, binder.getSetLogo());
            pstmt.setTimestamp(5, binder.getCreatedAt() != null ? Timestamp.valueOf(binder.getCreatedAt()) : null);
            pstmt.setTimestamp(6,
                    binder.getLastModified() != null ? Timestamp.valueOf(binder.getLastModified()) : null);

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                binder.setId(generatedKeys.getLong(1));
            }

            LOGGER.log(Level.INFO, "Binder saved: {0}", binder.getSetName());
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to save binder: " + binder.getSetName(), e);
        }
    }

    @Override
    public void update(Binder binder, String[] params) {
        String sql = """
                    UPDATE binders
                    SET owner = ?, set_id = ?, set_name = ?, set_logo = ?, last_modified = ?
                    WHERE id = ?
                """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, binder.getOwner());
                pstmt.setString(2, binder.getSetId());
                pstmt.setString(3, binder.getSetName());
                pstmt.setString(4, binder.getSetLogo());
                pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setLong(6, binder.getId());

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataPersistenceException("No binder found with id: " + binder.getId(),
                            new SQLException("Update affected 0 rows"));
                }
            }

            // Update cards
            updateBinderCards(conn, binder);

            conn.commit();
            LOGGER.log(Level.INFO, "Binder updated: {0}", binder.getSetName());
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to update binder: " + binder.getSetName(), e);
        }
    }

    @Override
    public void delete(Binder binder) {
        String sql = "DELETE FROM binders WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, binder.getId());
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted == 0) {
                LOGGER.log(Level.WARNING, "No binder found to delete with id: {0}", binder.getId());
            } else {
                LOGGER.log(Level.INFO, "Binder deleted: {0}", binder.getId());
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to delete binder: " + binder.getId(), e);
        }
    }

    private Binder mapResultSetToBinder(ResultSet rs) throws SQLException {
        Binder binder = new Binder();
        binder.setId(rs.getLong("id"));
        binder.setOwner(rs.getString("owner"));
        binder.setSetId(rs.getString("set_id"));
        binder.setSetName(rs.getString("set_name"));
        binder.setSetLogo(rs.getString("set_logo"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            binder.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp lastModified = rs.getTimestamp("last_modified");
        if (lastModified != null) {
            binder.setLastModified(lastModified.toLocalDateTime());
        }

        return binder;
    }

    @Override
    public List<Binder> getUserBinders(String owner) {
        String sql = "SELECT id, owner, set_id, set_name, set_logo, created_at, last_modified FROM binders WHERE owner = ?";
        List<Binder> binders = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, owner);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Binder binder = mapResultSetToBinder(rs);
                loadBinderCards(conn, binder);
                binders.add(binder);
            }

            LOGGER.log(Level.INFO, "Retrieved {0} binders for owner: {1}", new Object[] { binders.size(), owner });
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to get binders for owner: " + owner, e);
        }

        return binders;
    }

    @Override
    public void createBinder(String owner, String setId, String setName) {
        Binder binder = new Binder(owner, setId, setName);
        save(binder);
    }

    @Override
    public void deleteBinder(String binderId) {
        try {
            long id = Long.parseLong(binderId);
            String sql = "DELETE FROM binders WHERE id = ?";

            try (Connection conn = getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setLong(1, id);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted == 0) {
                    LOGGER.log(Level.WARNING, "No binder found to delete with id: {0}", binderId);
                } else {
                    LOGGER.log(Level.INFO, "Binder deleted: {0}", binderId);
                }
            }
        } catch (NumberFormatException e) {
            throw new DataPersistenceException("Invalid binder ID format: " + binderId, e);
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to delete binder: " + binderId, e);
        }
    }

    /**
     * Loads all cards belonging to a binder from the database.
     * Uses CardDao to fetch full card details (ensuring caching).
     */
    private void loadBinderCards(Connection conn, Binder binder) throws SQLException {
        String sql = "SELECT id, binder_id, card_id, card_name, card_image_url, game_type, quantity, is_tradable FROM binder_cards WHERE binder_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, binder.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String cardId = rs.getString("card_id");
                String gameTypeStr = rs.getString("game_type");

                // Fetch full card details from CardDao (Cache -> SDK)
                model.domain.card.Card fullCard = null;
                if (cardDao != null) {
                    fullCard = cardDao.getCard(cardId, gameTypeStr);
                }

                model.bean.CardBean cardBean;
                if (fullCard != null) {
                    // Create Bean from full cached card
                    cardBean = new model.bean.CardBean(
                            fullCard.getId(),
                            fullCard.getName(),
                            fullCard.getImageUrl(),
                            fullCard.getGameType());
                } else {
                    // Fallback to DB data if CardDao fails or returns null
                    LOGGER.log(Level.WARNING, "Card not found in CardDao/SDK: {0}. Using DB fallback.", cardId);
                    cardBean = new model.bean.CardBean(
                            cardId,
                            rs.getString("card_name"),
                            rs.getString("card_image_url"),
                            model.domain.CardGameType.valueOf(gameTypeStr));
                }

                // Set ownership details
                cardBean.setQuantity(rs.getInt("quantity"));
                cardBean.setTradable(rs.getBoolean("is_tradable"));

                binder.addCard(cardBean);
            }
        }
    }

    /**
     * Updates the cards in a binder by deleting all existing cards and inserting
     * the current ones.
     */
    private void updateBinderCards(Connection conn, Binder binder) throws SQLException {
        // Delete existing cards
        String deleteSql = "DELETE FROM binder_cards WHERE binder_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setLong(1, binder.getId());
            pstmt.executeUpdate();
        }

        // Insert current cards
        String insertSql = """
                    INSERT INTO binder_cards (binder_id, card_id, card_name, card_image_url, game_type, quantity, is_tradable)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            for (model.bean.CardBean card : binder.getCards()) {
                pstmt.setLong(1, binder.getId());
                pstmt.setString(2, card.getId());
                pstmt.setString(3, card.getName());
                pstmt.setString(4, card.getImageUrl());
                pstmt.setString(5, card.getGameType().toString());
                pstmt.setInt(6, card.getQuantity());
                pstmt.setBoolean(7, card.isTradable());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}
