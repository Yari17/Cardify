package controller;

import config.InputManager;
import view.cli.CliConfigurationView;
import view.IConfigurationView;

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
            case "1" -> "JSON";    
            case "2" -> "DEMO";    
            default -> "JSON";     
        };
    }
}

