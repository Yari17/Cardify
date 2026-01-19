package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    
    private static DBConnector instance;
    
    private final Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/Cardify"; 
    private static final String USER = "root";
    
    private static final String PASSWORD="password";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    
    private DBConnector() throws SQLException {
        try {
            
            Class.forName(DRIVER_CLASS);

            
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Database non trovato: " + e.getMessage());
        }
    }

    
    public static synchronized DBConnector getInstance() throws SQLException {
        
        if (instance == null || instance.connection.isClosed()) {
            instance = new DBConnector();
        }
        return instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
}
