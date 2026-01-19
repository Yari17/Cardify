package config;

import java.io.File;

public final class DatabaseConfig {
    private DatabaseConfig() {
    }

    
    public static final String JSON_DIR = "database";
    public static final String JSON_FILE_NAME = "users.json";
    public static final String JSON_FILE_PATH = JSON_DIR + File.separator + JSON_FILE_NAME;

    
    public static final String BINDERS_JSON_FILE_NAME = "binders.json";
    public static final String BINDERS_JSON_PATH = JSON_DIR + File.separator + BINDERS_JSON_FILE_NAME;

    
    public static final String JDBC_URL = "jdbc:mysql:";
    public static final String JDBC_USER = "root";
    public static final String JDBC_PASSWORD = "password";
}
