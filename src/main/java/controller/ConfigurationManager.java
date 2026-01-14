package controller;

import config.InputManager;
import view.configuration.CliConfigurationView;
import view.configuration.IConfigurationView;

public class ConfigurationManager {
    private final IConfigurationView view;

    public ConfigurationManager(InputManager inputManager) {
        this.view = new CliConfigurationView(inputManager);
        this.view.setController(this);
    }

    public String chooseInterface() {
        view.display();
        String choice = view.getInterfaceChoice();
        return mapInterfaceChoice(choice);
    }

    public String choosePersistence() {
        String choice = view.getPersistenceChoice();
        return mapPersistenceChoice(choice);
    }

    private String mapInterfaceChoice(String choice) {
        return switch (choice) {
            case "1" -> "JavaFX";
            case "2" -> "CLI";
            default -> null;
        };
    }

    private String mapPersistenceChoice(String choice) {
        return switch (choice) {
            case "1" -> "JSON";    // Standard - persistenza su file
            case "2" -> "DEMO";    // Demo - dati in memoria
            default -> "JSON";     // Default a JSON se non specificato
        };
    }
}

