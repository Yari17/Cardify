package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    // Istanza statica privata (Singleton)
    private static DBConnector instance;
    // L'oggetto Connection che rimarrà sempre aperto e unico
    private final Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/cardify"; // Il tuo DB
    private static final String USER = "root";
    //si lascia la password hardcoded per facilitare lo sviluppo locale, ovviemente in produzione andrebbe gestita in modo sicuro
    private static final String PASSWORD="password";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    // 1. Costruttore privato: impedisce l'istanziazione diretta dall'esterno
    private DBConnector() throws SQLException {
        try {
            // STEP 1: Carica il driver JDBC
            Class.forName(DRIVER_CLASS);

            // STEP 2: si collega al database
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);//NOSONAR per evitare false positive su hardcoded credentials

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Database non trovato: " + e.getMessage());
        }
    }

    // Metodo statico per ottenere l'unica istanza
    public static synchronized DBConnector getInstance() throws SQLException {
        // Se l'istanza non esiste o la connessione è chiusa, viene ricreata
        if (instance == null || instance.connection.isClosed()) {
            instance = new DBConnector();
        }
        return instance;
    }
    // Getter per utilizzare la connessione nelle altre classi
    public Connection getConnection() {
        return connection;
    }
}
