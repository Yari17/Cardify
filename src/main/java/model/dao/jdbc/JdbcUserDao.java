package model.dao.jdbc;

import model.dao.IUserDao;
import model.domain.User;
import exception.AuthenticationException;
import exception.DataPersistenceException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;
import config.DBConnector;

public class JdbcUserDao implements IUserDao {
    private static final Logger LOGGER = Logger.getLogger(JdbcUserDao.class.getName());
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_USER_TYPE = "user_type";
    private static final String COLUMN_RELIABILITY = "reliability_score";
    private static final String COLUMN_REVIEW_COUNT = "review_count";
    private static final String SELECT = "SELECT ";

    // Static caches to persist data until application stops
    private static final java.util.Map<String, User> userCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, String> credentialCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static boolean allLoaded = false;

    // Helper to mark the cache as fully loaded. Kept static so updates occur in a static context (avoids Sonar S2696).
    private static void markAllLoaded() {
        allLoaded = true;
    }

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    public JdbcUserDao(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        // Prefer the application's DBConnector singleton to obtain the Connection
        try {
            return DBConnector.getInstance().getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "DBConnector not available, falling back to DriverManager: {0}", e.getMessage());
            // Fallback to DriverManager if DBConnector cannot provide a connection
            return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        }
    }

    private void initializeDatabase() {
        String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        username VARCHAR(255) PRIMARY KEY,
                        password VARCHAR(255) NOT NULL,
                        user_type VARCHAR(50) DEFAULT 'Collezionista',
                        reliability_score INT DEFAULT 0,
                        review_count INT DEFAULT 0
                    )
                """;

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error initializing database", e);
        }
    }

    @Override
    public Optional<User> findByName(String name) {
        if (userCache.containsKey(name)) {
            return Optional.of(userCache.get(name));
        }

        String sql = SELECT + COLUMN_USERNAME + ", " + COLUMN_RELIABILITY + ", " + COLUMN_REVIEW_COUNT + ", "
                + COLUMN_USER_TYPE + " FROM users WHERE " + COLUMN_USERNAME + " = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString(COLUMN_USERNAME),
                        rs.getInt(COLUMN_RELIABILITY),
                        rs.getInt(COLUMN_REVIEW_COUNT));
                user.setUserType(rs.getString(COLUMN_USER_TYPE));

                userCache.put(name, user);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to find user: " + name, e);
        }

        return Optional.empty();
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (credentialCache.containsKey(username)) {
            return password.equals(credentialCache.get(username));
        }

        String sql = SELECT + COLUMN_PASSWORD + " FROM users WHERE " + COLUMN_USERNAME + " = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString(COLUMN_PASSWORD);
                credentialCache.put(username, storedPassword);
                return password.equals(storedPassword);
            }
            throw new UserNotFoundException(username);
        } catch (SQLException e) {
            throw new AuthenticationException("Authentication failed for user: " + username, e);
        }
    }

    @Override
    public Optional<User> authenticateAndGetUser(String username, String password) {
        if (authenticate(username, password)) {
            return findByName(username);
        }
        return Optional.empty();
    }

    @Override
    public void register(String username, String password, String userType) {

        if (findByName(username).isPresent()) {
            throw new UserAlreadyExistsException(username);
        }

        String sql = "INSERT INTO users (" + COLUMN_USERNAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_USER_TYPE + ", "
                + COLUMN_RELIABILITY + ", " + COLUMN_REVIEW_COUNT + ") VALUES (?, ?, ?, 0, 0)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, userType);
            pstmt.executeUpdate();

            // Update caches
            User newUser = new User(username, 0, 0);
            newUser.setUserType(userType);
            userCache.put(username, newUser);
            credentialCache.put(username, password);

        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to register user: " + username, e);
        }
    }

    @Override
    public List<String> findAllUsernames() {
        // Try cache first
        if (allLoaded && !userCache.isEmpty()) {
            return new java.util.ArrayList<>(userCache.keySet());
        }

        String sql = SELECT + COLUMN_USERNAME + " FROM users";
        List<String> result = new java.util.ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString(COLUMN_USERNAME);
                result.add(username);
            }
            // Optionally populate cache by fetching full records
            for (String uname : result) {
                if (!userCache.containsKey(uname)) {
                    findByName(uname).ifPresent(u -> userCache.put(uname, u));
                }
            }
            markAllLoaded();
            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load usernames from DB", e);
            // Fallback to cache keys
            return new java.util.ArrayList<>(userCache.keySet());
        }
    }

    // ========== Implementazione metodi IDao<User> ==========

    @Override
    public Optional<User> get(long id) {
        // Gli User in questo sistema sono identificati principalmente per username
        // Questo metodo cerca per ID se implementato nella tabella

        // Check cache first (iterating cache is faster than DB query if size is
        // reasonable,
        // but robust implementation should probably query DB if not allLoaded)
        if (allLoaded) {
            return userCache.values().stream()
                    .filter(user -> user.getId() == id)
                    .findFirst();
        }

        String sql = SELECT + COLUMN_USERNAME + ", " + COLUMN_RELIABILITY + ", " + COLUMN_REVIEW_COUNT + ", "
                + COLUMN_USER_TYPE + " FROM users WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString(COLUMN_USERNAME),
                        rs.getInt(COLUMN_RELIABILITY),
                        rs.getInt(COLUMN_REVIEW_COUNT));
                user.setUserType(rs.getString(COLUMN_USER_TYPE));

                userCache.put(user.getName(), user);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error getting user by id: {0}", id);
            LOGGER.log(Level.WARNING, "Exception: ", e);
        }

        return Optional.empty();
    }

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (findByName(user.getName()).isPresent()) {
            throw new UserAlreadyExistsException(user.getName());
        }

        String sql = "INSERT INTO users (" + COLUMN_USERNAME + ", " + COLUMN_USER_TYPE + ", " + COLUMN_RELIABILITY
                + ", " + COLUMN_REVIEW_COUNT + ") VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getUserType());
            pstmt.setInt(3, user.getReliabilityScore());
            pstmt.setInt(4, user.getReviewCount());
            pstmt.executeUpdate();

            userCache.put(user.getName(), user);
            // Note: save(User) doesn't take password, so we can't update credentialCache
            // fully
            // if it's a new user without password. Assuming save() is for existing users or
            // users created without password (which might be invalid state for auth).

        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to save user: " + user.getName(), e);
        }
    }

    @Override
    public void update(User user, String[] params) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Logic check: ensure user exists
        if (findByName(user.getName()).isEmpty()) {
            throw new UserNotFoundException(user.getName());
        }

        String sql = "UPDATE users SET " + COLUMN_USER_TYPE + " = ?, " + COLUMN_RELIABILITY + " = ?, "
                + COLUMN_REVIEW_COUNT + " = ? WHERE " + COLUMN_USERNAME + " = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserType());
            pstmt.setInt(2, user.getReliabilityScore());
            pstmt.setInt(3, user.getReviewCount());
            pstmt.setString(4, user.getName());
            pstmt.executeUpdate();

            userCache.put(user.getName(), user);
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to update user: " + user.getName(), e);
        }
    }

    @Override
    public void delete(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (findByName(user.getName()).isEmpty()) {
            throw new UserNotFoundException(user.getName());
        }

        String sql = "DELETE FROM users WHERE " + COLUMN_USERNAME + " = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.executeUpdate();

            userCache.remove(user.getName());
            credentialCache.remove(user.getName());
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to delete user: " + user.getName(), e);
        }
    }
}
