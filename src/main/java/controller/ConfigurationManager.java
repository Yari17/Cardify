package controller;

import config.AppConfig;
import view.InputManager;
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

    public void choosePersistence() {
        String choice = view.getPersistenceChoice();
        String persistenceType = mapPersistenceChoice(choice);
        if (persistenceType != null) {
            AppConfig.setPersistenceType(persistenceType);
        }
    }

    public void applyConfiguration() {
        choosePersistence();
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
            case "1" -> AppConfig.DAO_TYPE_JSON;
            case "2" -> AppConfig.DAO_TYPE_JDBC;
            default -> null;
        };
    }
}

