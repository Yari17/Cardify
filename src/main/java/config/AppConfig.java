package config;

public final class AppConfig {
    private AppConfig() {
    }

    public static final String DAO_TYPE_JSON = "json";
    public static final String DAO_TYPE_JDBC = "jdbc";
    public static final String DAO_TYPE_MEMORY = "demo";
    public static final String DEFAULT_DAO_TYPE = DAO_TYPE_JSON;

    public static final String POKEMON_GAME = "POKEMON";
    public static final String USER_TYPE_COLLECTOR = "Collezionista";
    public static final String USER_TYPE_STORE = "Store";
    public static final String DEFAULT_SET_ID = "sv08.5";

    private static String currentPersistenceType = DEFAULT_DAO_TYPE;

    public static void setPersistenceType(String type) {
        currentPersistenceType = type;
    }

    public static String getPersistenceLabel() {
        return switch (currentPersistenceType) {
            case DAO_TYPE_JDBC -> "Persistenza: Database (JDBC)";
            case DAO_TYPE_MEMORY -> "Persistenza: In-Memory (Demo Mode)";
            default -> "Persistenza: File System (JSON)";
        };
    }
}
