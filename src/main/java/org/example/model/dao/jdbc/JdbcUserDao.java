package org.example.model.dao.jdbc;

import org.example.model.domain.User;
import org.example.model.dao.UserDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
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
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (username, reliability_score, review_count) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setInt(2, 0);
            pstmt.setInt(3, 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findByName(String name) {
        String sql = "SELECT * FROM users WHERE username = ?";

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
            throw new RuntimeException("Failed to find user", e);
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
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
            throw new RuntimeException("Failed to retrieve users", e);
        }

        return users;
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET reliability_score = ?, review_count = ? WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 0);
            pstmt.setInt(2, 0);
            pstmt.setString(3, user.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(String name) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
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
        } catch (SQLException e) {
            throw new RuntimeException("Authentication failed", e);
        }

        return false;
    }

    @Override
    public void register(String username, String password, String userType) {
        // Check if user already exists
        if (findByName(username).isPresent()) {
            throw new IllegalArgumentException("Username già esistente");
        }

        String sql = "INSERT INTO users (username, password, user_type, reliability_score, review_count) VALUES (?, ?, ?, 0, 0)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, userType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register user", e);
        }
    }
}

