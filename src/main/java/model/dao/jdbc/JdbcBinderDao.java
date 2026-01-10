package model.dao.jdbc;

import model.dao.IBinderDao;
import model.domain.Binder;
import model.exception.DataPersistenceException;

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

    public JdbcBinderDao(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
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

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createBindersTable);
            LOGGER.info("Binders table initialized");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error initializing binders database", e);
        }
    }

    @Override
    public Optional<Binder> get(long id) {
        String sql = "SELECT * FROM binders WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToBinder(rs));
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to get binder with id: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public List<Binder> getAll() {
        String sql = "SELECT * FROM binders";
        List<Binder> binders = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                binders.add(mapResultSetToBinder(rs));
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
            pstmt.setTimestamp(6, binder.getLastModified() != null ? Timestamp.valueOf(binder.getLastModified()) : null);

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                binder.setId(generatedKeys.getLong(1));
            }

            LOGGER.info(() -> "Binder saved: " + binder.getSetName());
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

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

            LOGGER.info(() -> "Binder updated: " + binder.getSetName());
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
                LOGGER.warning(() -> "No binder found to delete with id: " + binder.getId());
            } else {
                LOGGER.info(() -> "Binder deleted: " + binder.getId());
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
        String sql = "SELECT * FROM binders WHERE owner = ?";
        List<Binder> binders = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, owner);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                binders.add(mapResultSetToBinder(rs));
            }

            LOGGER.info(() -> "Retrieved " + binders.size() + " binders for owner: " + owner);
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to get binders for owner: " + owner, e);
        }

        return binders;
    }

    @Override
    public void addCardToBinder(String binderId, String cardId) {
        // TODO: Implementare quando avremo la tabella delle carte nei binder
        LOGGER.warning("addCardToBinder not yet implemented - requires cards table");
    }

    @Override
    public void createBinder(String owner, String setId, String setName) {
        Binder binder = new Binder(owner, setId, setName);
        save(binder);
    }
}
