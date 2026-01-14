package view.storehomepage;

import controller.StoreHPController;
import config.InputManager;

@SuppressWarnings("java:S106")
public class CliStoreHPView implements IStoreHPView {
    private final InputManager inputManager;
    private StoreHPController controller;

    public CliStoreHPView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void setController(StoreHPController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        if (controller == null) {
            System.out.println("ERROR: Controller not set");
            return;
        }

        showWelcomeMessage(controller.getUsername());

        boolean running = true;
        while (running) {
            System.out.println("\n=== CARDIFY STORE HOME PAGE ===");
            System.out.println("1. Crea Evento");
            System.out.println("2. Visualizza Scambi in Sospeso");
            System.out.println("3. Registra Deposito");
            System.out.println("4. Registra Ritiro");
            System.out.println("5. Logout");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");

            String choice = inputManager.readString();

            switch (choice) {
                case "1":
                    System.out.println("Crea Evento selezionato.");
                    break;
                case "2":
                    System.out.println("Visualizza Scambi in Sospeso selezionato.");
                    break;
                case "3":
                    System.out.println("Registra Deposito selezionato.");
                    break;
                case "4":
                    System.out.println("Registra Ritiro selezionato.");
                    break;
                case "5":
                    controller.onLogoutRequested();
                    running = false;
                    break;
                case "0":
                    close();
                    running = false;
                    break;
                default:
                    System.out.println("Opzione non valida. Riprova.");
            }
        }
    }

    @Override
    public void close() {
        controller.onExitRequested();
    }

    @Override
    public void showWelcomeMessage(String username) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   Benvenuto STORE " + username + "!      ║");
        System.out.println("╚════════════════════════════════════╝");
    }
}
