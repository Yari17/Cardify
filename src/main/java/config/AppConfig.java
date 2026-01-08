package config;


public final class AppConfig {
    private AppConfig() {} 

    

    public static final String DAO_TYPE_JSON = "json";
    public static final String DAO_TYPE_JDBC = "jdbc";
    public static final String DEFAULT_DAO_TYPE = DAO_TYPE_JSON;

    public static final String API_KEY = "fe40ebec-dddc-446e-98e0-9a4348f2cd35";


    private static String currentPersistenceType = DEFAULT_DAO_TYPE;

    public static void setPersistenceType(String type) {
        currentPersistenceType = type;
    }

    public static String getPersistenceType() {
        return currentPersistenceType;
    }

    public static String getPersistenceLabel() {
        return DAO_TYPE_JDBC.equals(currentPersistenceType)
            ? "Persistenza: Database (JDBC)"
            : "Persistenza: File System (JSON)";
    }
}
