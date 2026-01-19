package view.cli;

import controller.StoreHPController;
import config.InputManager;
import view.IStoreHPView;

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
            System.out.println("1. Gestisci scambi");
            System.out.println("2. Scambi conclusi");
            System.out.println("3. Logout");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");

            String choice = inputManager.readString();

            switch (choice) {
                case "1":
                    
                    if (controller != null) controller.onManageTradesRequested();
                    break;
                case "2":
                    if (controller != null) controller.onViewCompletedTradesRequested();
                    break;
                case "3":
                    if (controller != null) controller.onLogoutRequested();
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
        /* not used */
    }

    @Override
    public void refresh() {
        /* not used */
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("[ERROR] " + errorMessage);
    }

    @Override
    public void showWelcomeMessage(String username) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   Benvenuto STORE " + username + "!      ║");
        System.out.println("╚════════════════════════════════════╝");
    }

    @Override
    public void displayCompletedTrades(java.util.List<model.bean.TradeTransactionBean> completed) {
        System.out.printf("%n=== SCAMBI CONCLUSI ===%n");
        if (completed == null || completed.isEmpty()) {
            System.out.println("Nessuno scambio concluso al momento.");
            return;
        }
        int i = 0;
        for (model.bean.TradeTransactionBean t : completed) {
            if (t != null) {
                String s = t.getStatus();
                if (s == null) s = "?";
                if ("COMPLETED".equalsIgnoreCase(s) || "CANCELLED".equalsIgnoreCase(s)) {
                    i++;
                    System.out.printf("%d) tx-%d: %s vs %s @ %s [%s]%n", i, t.getTransactionId(), t.getProposerId(), t.getReceiverId(), t.getStoreId(), s);
                }
            }
        }
        if (i == 0) System.out.println("Nessuno scambio concluso al momento.");
    }
}
