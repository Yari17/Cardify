package model.dao.jdbc;

import model.dao.IUserDao;
import model.domain.User;
import model.exception.AuthenticationException;
import model.exception.DataPersistenceException;
import model.exception.UserAlreadyExistsException;
import model.exception.UserNotFoundException;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

public class JdbcUserDao implements IUserDao {
    private static final Logger LOGGER = Logger.getLogger(JdbcUserDao.class.getName());
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
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
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
        String sql = "SELECT  FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getInt("reliability_score"),
                        rs.getInt("review_count")
                );
                user.setUserType(rs.getString("user_type"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to find user: " + name, e);
        }

        return Optional.empty();
    }

    @Override
    public boolean authenticate(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return password.equals(rs.getString("password"));
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

        String sql = "INSERT INTO users (username, password, user_type, reliability_score, review_count) VALUES (?, ?, ?, 0, 0)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, userType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to register user: " + username, e);
        }
    }

    // ========== Implementazione metodi IDao<User> ==========

    @Override
    public Optional<User> get(long id) {
        // Gli User in questo sistema sono identificati principalmente per username
        // Questo metodo cerca per ID se implementato nella tabella
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getInt("reliability_score"),
                    rs.getInt("review_count")
                );
                user.setUserType(rs.getString("user_type"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error getting user by id: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public java.util.List<User> getAll() {
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getInt("reliability_score"),
                    rs.getInt("review_count")
                );
                user.setUserType(rs.getString("user_type"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to get all users", e);
        }

        return users;
    }

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (findByName(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        String sql = "INSERT INTO users (username, user_type, reliability_score, review_count) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getUserType());
            pstmt.setInt(3, user.getReliabilityScore());
            pstmt.setInt(4, user.getReviewCount());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to save user: " + user.getUsername(), e);
        }
    }

    @Override
    public void update(User user, String[] params) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (findByName(user.getUsername()).isEmpty()) {
            throw new UserNotFoundException(user.getUsername());
        }

        String sql = "UPDATE users SET user_type = ?, reliability_score = ?, review_count = ? WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserType());
            pstmt.setInt(2, user.getReliabilityScore());
            pstmt.setInt(3, user.getReviewCount());
            pstmt.setString(4, user.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to update user: " + user.getUsername(), e);
        }
    }

    @Override
    public void delete(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (findByName(user.getUsername()).isEmpty()) {
            throw new UserNotFoundException(user.getUsername());
        }

        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to delete user: " + user.getUsername(), e);
        }
    }
}
