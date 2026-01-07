package config;

import java.util.Scanner;

public final class AppConfig {
    private AppConfig() {} 

    
    public static final String VIEW_TYPE_JAVAFX = "javafx";
    public static final String VIEW_TYPE_CLI = "cli";
    public static final String DAO_TYPE_JSON = "json";
    public static final String DAO_TYPE_JDBC = "jdbc";

    
    public static final String DEFAULT_VIEW_TYPE = VIEW_TYPE_JAVAFX;
    public static final String DEFAULT_DAO_TYPE = DAO_TYPE_JSON;

    
    public static final String ARG_PREFIX_VIEW = "view=";
    public static final String ARG_PREFIX_DAO = "dao=";

    
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

    public static String[] interactiveConfiguration() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       CARDIFY - Configurazione         ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        
        String viewType = DEFAULT_VIEW_TYPE;
        System.out.println("Seleziona il tipo di interfaccia:");
        System.out.println("1. JavaFX (Interfaccia Grafica)");
        System.out.println("2. CLI (Linea di Comando)");
        System.out.print("\nScelta (1-2, default 1): ");

        try {
            String viewChoice = scanner.nextLine().trim();
            if ("2".equals(viewChoice)) {
                viewType = VIEW_TYPE_CLI;
                System.out.println("✓ Selezionato: CLI\n");
            } else {
                viewType = VIEW_TYPE_JAVAFX;
                System.out.println("✓ Selezionato: JavaFX\n");
            }
        } catch (Exception e) {
            System.out.println("✓ Usando default: JavaFX\n");
        }

        
        String daoType = DEFAULT_DAO_TYPE;
        System.out.println("Seleziona il tipo di persistenza:");
        System.out.println("1. File System (JSON)");
        System.out.println("2. Database (JDBC/MySQL)");
        System.out.print("\nScelta (1-2, default 1): ");

        try {
            String daoChoice = scanner.nextLine().trim();
            if ("2".equals(daoChoice)) {
                daoType = DAO_TYPE_JDBC;
                System.out.println("✓ Selezionato: JDBC\n");
            } else {
                daoType = DAO_TYPE_JSON;
                System.out.println("✓ Selezionato: JSON\n");
            }
        } catch (Exception e) {
            System.out.println("✓ Usando default: JSON\n");
        }

        System.out.println("Avvio applicazione...\n");
        System.out.println("═══════════════════════════════════════\n");

        
        return new String[]{viewType, daoType};
    }
}
