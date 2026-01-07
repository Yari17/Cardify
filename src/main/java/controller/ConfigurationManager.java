package controller;

import config.AppConfig;
import view.InputManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * ConfigurationManager gestisce la configurazione iniziale dell'applicazione.
 * Responsabilità: raccogliere le scelte dell'utente per interfaccia e persistenza.
 * Principio GRASP: Information Expert - conosce come configurare l'app.
 */
public class ConfigurationManager {
    private static final Map<String, String> INTERFACE_OPTIONS = new HashMap<>();
    private static final Map<String, String> PERSISTENCE_OPTIONS = new HashMap<>();

    static {
        INTERFACE_OPTIONS.put("1", "JavaFX");
        INTERFACE_OPTIONS.put("2", "CLI");

        PERSISTENCE_OPTIONS.put("1", AppConfig.DAO_TYPE_JSON);
        PERSISTENCE_OPTIONS.put("2", AppConfig.DAO_TYPE_JDBC);
    }

    private final InputManager inputManager;

    public ConfigurationManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    /**
     * Chiede all'utente di scegliere l'interfaccia.
     * @return "JavaFX" o "CLI"
     */
    public String chooseInterface() {
        return promptChoice(
            "=== CARDIFY - Scegli Interfaccia ===",
            new String[]{"JavaFX (Interfaccia Grafica)", "CLI (Interfaccia Testuale)"},
            INTERFACE_OPTIONS,
            choice -> System.out.println("Interfaccia selezionata: " + choice)
        );
    }

    /**
     * Chiede all'utente di scegliere il tipo di persistenza.
     */
    public void choosePersistence() {
        String persistenceType = promptChoice(
            "=== CARDIFY - Scegli Persistenza ===",
            new String[]{"JSON (File System)", "JDBC (Database)"},
            PERSISTENCE_OPTIONS,
            choice -> System.out.println("Persistenza selezionata: " + choice)
        );
        AppConfig.setPersistenceType(persistenceType);
    }

    /**
     * Template method per gestire le scelte dell'utente.
     * Elimina la duplicazione tra chooseInterface e choosePersistence.
     */
    private String promptChoice(String title, String[] options, Map<String, String> mapping, Consumer<String> onSuccess) {
        while (true) {
            System.out.println("\n" + title);
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ") " + options[i]);
            }
            System.out.print("Scelta (1-" + options.length + "): ");

            String choice = inputManager.readString().trim();
            String result = mapping.get(choice);

            if (result != null) {
                onSuccess.accept(result);
                return result;
            }
            System.out.println("Scelta non valida. Riprova.");
        }
    }
}

