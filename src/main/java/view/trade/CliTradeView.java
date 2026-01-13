package view.trade;

import controller.TradeController;
import view.InputManager;

@SuppressWarnings("java:S106")
public class CliTradeView implements ITradeView {

    private final InputManager inputManager;
    private TradeController controller;

    public CliTradeView(InputManager inputManager) {
        if (inputManager == null) {
            throw new IllegalArgumentException("InputManager cannot be null");
        }
        this.inputManager = inputManager;
    }

    public void setController(TradeController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║          TRADE PAGE                ║");
        System.out.println("╚════════════════════════════════════╝");

        if (controller != null) {
            controller.loadTrades();
        } else {
            System.out.println("⚠ Controller non connesso, impossibile caricare gli scambi.");
        }

        System.out.println("\nMenu:");
        System.out.println("1. Torna alla Homepage");
        System.out.println("2. Vai alla Collezione");
        System.out.println("3. Logout");

        System.out.print("Scegli un'opzione: ");
        String choice = inputManager.readString();

        switch (choice) {
            case "1" -> controller.navigateToHome();

            case "2" -> controller.navigateToCollection();

            case "3" -> controller.onLogoutRequested();


            default -> {
                System.out.println("Opzione non valida.");
                display();
            }
        }
    }

    @Override
    public void close() {
        // Nothing to close in CLI
    }

    @Override
    public void setUsername(String username) {
        // Username already shown in display
    }

    @Override
    public void displayTrades(java.util.List<model.bean.TradeBean> pendingTrades,
                              java.util.List<model.bean.TradeBean> scheduledTrades) {
        System.out.println("\n=== SCAMBI IN ATTESA (PENDING) ===");
        if (pendingTrades == null || pendingTrades.isEmpty()) {
            System.out.println("Nessuno scambio in attesa.");
        } else {
            for (model.bean.TradeBean trade : pendingTrades) {
                System.out.println("- Scambio #" + trade.getId() + " da " + trade.getSenderUsername() + " (Offerta: "
                        + trade.getOfferedCardsNames() + ")");
            }
        }

        System.out.println("\n=== SCAMBI PROGRAMMATI (SCHEDULED) ===");
        if (scheduledTrades == null || scheduledTrades.isEmpty()) {
            System.out.println("Nessuno scambio programmato.");
        } else {
            for (model.bean.TradeBean trade : scheduledTrades) {
                System.out.println("- Scambio #" + trade.getId() + " con " + trade.getReceiverUsername() + " (Data: "
                        + trade.getScheduledDate() + " @ " + trade.getStoreLocation() + ")");
            }
        }
    }
}
