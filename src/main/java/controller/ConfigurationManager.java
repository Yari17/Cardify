package controller;

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

    private String mapInterfaceChoice(String choice) {
        return switch (choice) {
            case "1" -> "JavaFX";
            case "2" -> "CLI";
            default -> null;
        };
    }
}

