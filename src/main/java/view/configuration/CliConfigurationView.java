package view.configuration;

import controller.ConfigurationManager;
import view.InputManager;

public class CliConfigurationView implements IConfigurationView {
    private final InputManager inputManager;
    private ConfigurationManager controller;
    private String interfaceChoice;
    private String persistenceChoice;

    public CliConfigurationView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void setController(ConfigurationManager controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        displayInterfaceSelection();
        // Persistenza JSON usata di default - nessuna selezione richiesta
    }

    private void displayInterfaceSelection() {
        while (true) {
            System.out.println("\n=== CARDIFY - Scegli Interfaccia ===");
            System.out.println("1) JavaFX (Interfaccia Grafica)");
            System.out.println("2) CLI (Interfaccia Testuale)");
            System.out.print("Scelta (1-2): ");

            this.interfaceChoice = inputManager.readString().trim();

            if ("1".equals(interfaceChoice) || "2".equals(interfaceChoice)) {
                String interfaceType = "1".equals(interfaceChoice) ? "JavaFX" : "CLI";
                showInterfaceSelected(interfaceType);
                break;
            } else {
                showInvalidChoice();
            }
        }
    }

    @Override
    public String getInterfaceChoice() {
        return interfaceChoice;
    }

    @Override
    public String getPersistenceChoice() {
        return persistenceChoice;
    }

    @Override
    public void showInterfaceSelected(String interfaceType) {
        System.out.println("Interfaccia selezionata: " + interfaceType);
    }

    @Override
    public void showPersistenceSelected(String persistenceType) {
        System.out.println("Persistenza selezionata: " + persistenceType);
    }

    @Override
    public void showInvalidChoice() {
        System.out.println("Scelta non valida. Riprova.");
    }

    @Override
    public void close() {
    }
}
