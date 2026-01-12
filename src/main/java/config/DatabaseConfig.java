package config;

import java.io.File;

public final class DatabaseConfig {
    private DatabaseConfig() {
    }

    // JSON Configuration
    public static final String JSON_DIR = "database";
    public static final String JSON_FILE_NAME = "users.json";
    public static final String JSON_FILE_PATH = JSON_DIR + File.separator + JSON_FILE_NAME;

    // Binders JSON Configuration
    public static final String BINDERS_JSON_FILE_NAME = "binders.json";
    public static final String BINDERS_JSON_PATH = JSON_DIR + File.separator + BINDERS_JSON_FILE_NAME;

    // Cards JSON Configuration
    public static final String CARDS_JSON_FILE_NAME = "cards.json";
    public static final String CARDS_JSON_PATH = JSON_DIR + File.separator + CARDS_JSON_FILE_NAME;

    // JDBC Configuration
    public static final String JDBC_URL = "jdbc:mysql:";
    public static final String JDBC_USER = "root";
    public static final String JDBC_PASSWORD = "password";
}
