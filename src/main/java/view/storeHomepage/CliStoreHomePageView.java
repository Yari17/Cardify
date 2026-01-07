package view.storeHomepage;

import controller.StoreHomePageController;
import view.InputManager;

public class CliStoreHomePageView implements IStoreHomePageView {
    private final InputManager inputManager;
    private StoreHomePageController controller;

    public CliStoreHomePageView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void setController(StoreHomePageController controller) {
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
            System.out.println("1. Gestisci inventario");
            System.out.println("2. Visualizza ordini");
            System.out.println("3. Gestisci profilo store");
            System.out.println("4. Logout");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");

            String choice = inputManager.readString();

            switch (choice) {
                case "1":
                    System.out.println("Funzionalità in sviluppo...");
                    break;
                case "2":
                    System.out.println("Funzionalità in sviluppo...");
                    break;
                case "3":
                    System.out.println("Profilo Store: " + controller.getUsername());
                    break;
                case "4":
                    controller.onLogoutRequested();
                    running = false;
                    break;
                case "0":
                    controller.onExitRequested();
                    running = false;
                    break;
                default:
                    System.out.println("Opzione non valida. Riprova.");
            }
        }
    }

    @Override
    public void close() {
        
    }

    @Override
    public void showWelcomeMessage(String username) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   Benvenuto STORE " + username + "!      ║");
        System.out.println("╚════════════════════════════════════╝");
    }
}
