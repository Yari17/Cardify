package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce la connettività JDBC al database relazionale MySql.
 * Implementa il pattern Singleton per centralizzare il caricamento del driver
 * e fornire connessioni attive ai componenti DAO.
 */
public class DBConnector {

    private static DBConnector instance;

    private static final String URL = "jdbc:mysql://localhost:3306/Cardify";
    private static final String USER = "root";

    private static final String PASSWORD = "password";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private DBConnector() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Database non trovato: " + e.getMessage());
        }
    }

    /**
     * Inizializza l'istanza Singleton se non già presente.
     *
     * @return L'istanza Singleton di DBConnector.
     * @throws SQLException Se il driver del database non viene trovato.
     */
    private static DBConnector getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnector();
        }
        return instance;
    }

    /**
     * Fornisce una nuova connessione verso il database specificato.
     * Questa funzione è fondamentale per consentire ai vari componenti del sistema
     * (come i DAO) di interagire con lo strato di persistenza.
     * 
     * Delega l'inizializzazione sicura del driver al metodo helper
     * {@link #getInstance()}
     * per garantire che le risorse statiche siano opportunamente caricate prima di
     * tentare l'apertura della connessione tramite DriverManager.
     *
     * @return Una connessione attiva verso il database.
     * @throws SQLException Se si verifica un errore durante l'apertura della
     *                      connessione o l'inizializzazione del driver.
     */
    public static Connection getConnection() throws SQLException {
        getInstance(); // Inizializza il driver se necessario
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
