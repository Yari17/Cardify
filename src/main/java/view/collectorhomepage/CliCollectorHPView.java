package view.collectorhomepage;

import controller.CollectorHPController;
import model.bean.CardBean;
import view.InputManager;

import java.util.List;

public class CliCollectorHPView implements ICollectorHPView {
    private final InputManager inputManager;
    private CollectorHPController controller;

    public CliCollectorHPView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void setController(CollectorHPController controller) {
        this.controller = controller;
    }

    @Override
    public String getSearchQuery() {
        System.out.print("Inserisci la query di ricerca: ");
        return inputManager.readString();
    }

    @Override
    public void display() {
        if (controller == null) {
            System.out.println("ERROR: Controller not set");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== CARDIFY HOME PAGE ===");
            System.out.println("1. Visualizza carte popolari");
            System.out.println("2. Gestisci collezione");
            System.out.println("3. Effettua scambio");
            System.out.println("4. Visualizza profilo");
            System.out.println("5. Logout");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");

            String choice = inputManager.readString();

            switch (choice) {
                case "1":
                    System.out.println("\nVisualizzazione carte popolari già caricata all'avvio.");
                    System.out.print("Premi INVIO per continuare...");
                    inputManager.readString();
                    break;
                case "2":
                    System.out.println("Gestisci collezione selezionato.");
                    break;
                case "3":
                    System.out.println("Effettua scambio selezionato.");
                    break;
                case "4":
                    if (controller != null) {
                        showWelcomeMessage(controller.getUsername());
                    }
                    break;
                case "5":
                    if (controller != null) {
                        controller.onLogoutRequested();
                    }
                    running = false;
                    break;
                case "0":
                    if (controller != null) {
                        controller.onExitRequested();
                    }
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

    @Override
    public void displayCards(List<CardBean> cards) {
        if (cards == null || cards.isEmpty()) {
            System.out.println("\nNessuna carta disponibile.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        POPULAR CARDS                               ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println("\nTotale carte: " + cards.size());

        for (int i = 0; i < cards.size(); i++) {
            CardBean card = cards.get(i);
            System.out.println("\n" + (i + 1) + ". ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("   Nome:      " + card.getName());
            System.out.println("   ID:        " + card.getId());
            System.out.println("   Gioco:     " + card.getGameType());
            System.out.println("   Immagine:  " + (card.getImageUrl() != null ? "✓ Disponibile" : "✗ Non disponibile"));

            if ((i + 1) % 10 == 0 && (i + 1) < cards.size()) {
                System.out.print("\nPremi INVIO per vedere altre carte...");
                inputManager.readString();
            }
        }

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.print("\nPremi INVIO per continuare...");
        inputManager.readString();
    }
}
