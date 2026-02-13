package model.domain;

/**
 * Rappresenta un utente generico del sistema Cardify.
 */
public class User {
    /** Identificativo unico dell'utente. */
    String username;
    /** Password dell'utente (testo in chiaro per questa fase demo). */
    String password;
    /** Ruolo dell'utente (es. COLLECTOR, STORE). */
    String userType;

    /**
     * Costruttore completo dell'utente.
     * 
     * @param username Identificativo.
     * @param password Credenziali.
     * @param userType Ruolo assegnato.
     */
    public User(String username, String password, String userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }
}
