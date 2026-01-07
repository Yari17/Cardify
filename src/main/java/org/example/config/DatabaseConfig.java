package org.example.config;

import java.io.File;

/**
 * Database and persistence configuration constants.
 */
public final class DatabaseConfig {
    private DatabaseConfig() {} // Prevent instantiation

    // JSON Database
    public static final String JSON_DIR = "database";
    public static final String JSON_FILE_NAME = "users.json";
    public static final String JSON_FILE_PATH = JSON_DIR + File.separator + JSON_FILE_NAME;

    // JDBC Database
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/cardify";
    public static final String JDBC_USER = "root";
    public static final String JDBC_PASSWORD = "password";
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
}

