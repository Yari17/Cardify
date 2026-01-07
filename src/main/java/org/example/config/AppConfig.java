package org.example.config;

/**
 * Central configuration for the application.
 * Stores runtime settings like persistence type.
 */
public final class AppConfig {
    private AppConfig() {} // Prevent instantiation

    // Application modes
    public static final String VIEW_TYPE_JAVAFX = "javafx";
    public static final String VIEW_TYPE_CLI = "cli";
    public static final String DAO_TYPE_JSON = "json";
    public static final String DAO_TYPE_JDBC = "jdbc";

    // Default values
    public static final String DEFAULT_VIEW_TYPE = VIEW_TYPE_JAVAFX;
    public static final String DEFAULT_DAO_TYPE = DAO_TYPE_JSON;

    // Argument prefixes
    public static final String ARG_PREFIX_VIEW = "view=";
    public static final String ARG_PREFIX_DAO = "dao=";

    // Runtime persistence type (set by Main)
    private static String currentPersistenceType = DEFAULT_DAO_TYPE;

    /**
     * Set the current persistence type for the application.
     * @param type "json" or "jdbc"
     */
    public static void setPersistenceType(String type) {
        currentPersistenceType = type;
    }

    /**
     * Get the current persistence type.
     * @return "json" or "jdbc"
     */
    public static String getPersistenceType() {
        return currentPersistenceType;
    }

    /**
     * Get a human-readable label for the current persistence type.
     * @return Display label for persistence type
     */
    public static String getPersistenceLabel() {
        return DAO_TYPE_JDBC.equals(currentPersistenceType)
            ? "Persistenza: Database (JDBC)"
            : "Persistenza: File System (JSON)";
    }
}

