package org.example.config;

import java.util.Scanner;

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

    /**
     * Interactive configuration menu.
     * Asks user to choose GUI type and persistence type.
     * @return array [viewType, daoType]
     */
    public static String[] interactiveConfiguration() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       CARDIFY - Configurazione         ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        // Choose GUI type
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

        // Choose persistence type
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

        // Don't close scanner as it's System.in
        return new String[]{viewType, daoType};
    }
}
