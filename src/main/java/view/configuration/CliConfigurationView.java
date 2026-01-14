package view.configuration;

import controller.ConfigurationManager;
import config.InputManager;

@SuppressWarnings("java:S106")
public class CliConfigurationView implements IConfigurationView {
    private final InputManager inputManager;

    private String interfaceChoice;
    private String persistenceChoice;

    public CliConfigurationView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void setController(ConfigurationManager controller) {
        // Controller is not used in this view implementation
    }

    @Override
    public void display() {
        displayInterfaceSelection();
        displayPersistenceSelection();
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

    private void displayPersistenceSelection() {
        while (true) {
            System.out.println("\n=== CARDIFY - Scegli Modalità ===");
            System.out.println("1) Standard (Salvataggio dati permanente)");
            System.out.println("2) Demo (Dati volatili - solo per test)");
            System.out.print("Scelta (1-2): ");

            this.persistenceChoice = inputManager.readString().trim();

            if ("1".equals(persistenceChoice) || "2".equals(persistenceChoice)) {
                String persistenceType = "1".equals(persistenceChoice) ? "Standard (JSON)" : "Demo (In-Memory)";
                showPersistenceSelected(persistenceType);
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
