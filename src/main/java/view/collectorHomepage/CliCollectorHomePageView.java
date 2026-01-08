package view.collectorhomepage;

import controller.CollectorHomePageController;
import model.domain.card.Card;
import view.InputManager;

import java.util.List;

public class CliCollectorHomePageView implements ICollectorHomePageView {
    private final InputManager inputManager;
    private CollectorHomePageController controller;

    public CliCollectorHomePageView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void setController(CollectorHomePageController controller) {
        this.controller = controller;
    }

    @Override
    public String getSearchQuery() {
        return "";
    }

    @Override
    public void showCards(List<Card> cards) {

    }

    @Override
    public void showCardDetails(Card card) {

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
            System.out.println("\n=== CARDIFY HOME PAGE ===");
            System.out.println("1. Gestisci collezione");
            System.out.println("2. Effettua scambio");
            System.out.println("3. Visualizza profilo");
            System.out.println("4. Logout");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");

            String choice = inputManager.readString();

            switch (choice) {
                case "1":
                    System.out.println("Gestisci collezione selezionato.");
                    break;
                case "2":
                    System.out.println("Effettua scambio selezionato.");
                    break;
                case "3":
                    System.out.println("Profilo utente: " + controller.getUsername());
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
        System.out.println("║   Benvenuto in CARDIFY, " + username + "!   ║");
        System.out.println("╚════════════════════════════════════╝");
    }
}
