package model.dao.jdbc;

import config.DBConnector;
import exception.DaoException;
import exception.UserAlreadyExistsException;
import model.dao.IUserDao;
import model.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementazione JDBC del DAO per la gestione degli utenti.
 * Utilizza SQL standard per interagire con una base di dati relazionale.
 */
public class JdbcUserDao implements IUserDao {

    /**
     * Autentica l'utente verificando username e password sul database.
     * Utilizza un PreparedStatement per prevenire attacchi di SQL Injection.
     * 
     * @param username Nome utente fornito.
     * @param password Password fornita.
     * @return L'oggetto {@link User} se trovato e autenticato, null altrimenti.
     * @throws IllegalArgumentException In caso di errori di comunicazione con il
     *                                  database.
     */
    @Override
    public User authenticateAndGetUser(String username, String password) {
        String sql = "SELECT username, password, userType FROM users WHERE username = ? AND password = ?";
        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String un = rs.getString("username");
                        String pw = rs.getString("password");
                        String userType = rs.getString("userType");
                        return new User(un, pw, userType);
                    } else {
                        return null;
                    }
                }
            }

        } catch (SQLException e) {
            throw new IllegalArgumentException("Errore del database durante authenticateAndGetUser: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Registra un nuovo profilo utente previa verifica dell'unicità dello username.
     * Se lo username è già presente, solleva un'eccezione specifica.
     * 
     * @param username Nome utente desiderato.
     * @param password Password scelta.
     * @param userType Tipo di profilo (COLLECTOR, STORE).
     * @throws UserAlreadyExistsException Se lo username è già registrato.
     * @throws DaoException               In caso di errori tecnici del database.
     */
    @Override
    public void register(User user) {
        // Verifica disponibilità username prima dell'inserimento
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, userType) VALUES (?, ?, ?)";

        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setString(1, user.getUsername());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count > 0) {
                            throw new UserAlreadyExistsException(
                                    "L'utente con username " + user.getUsername() + " esiste già");
                        }
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, user.getUsername());
                    insertStmt.setString(2, user.getPassword());
                    insertStmt.setString(3, user.getUserType());
                    insertStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore del database durante la registrazione: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera l'elenco di tutti gli account registrati come negozi.
     * 
     * @return Lista di oggetti {@link User} filtrati per tipo 'STORE'.
     * @throws DaoException In caso di errori durante la query.
     */
    @Override
    public List<User> getStores() {
        String sql = "SELECT username, password, userType FROM users WHERE userType = 'STORE'";
        List<User> stores = java.util.Collections.emptyList();
        try {
            Connection conn = DBConnector.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    stores = new java.util.ArrayList<>();
                    while (rs.next()) {
                        String un = rs.getString("username");
                        String pw = rs.getString("password");
                        String type = rs.getString("userType");
                        stores.add(new User(un, pw, type));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore del database durante il recupero degli store: " + e.getMessage(), e);
        }
        return stores;
    }

}
