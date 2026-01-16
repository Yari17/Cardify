package view.cli;

import controller.LiveTradeController;
import config.InputManager;
import model.bean.TradeTransactionBean;
import view.ICollectorTradeView;


public class CliCollectorTradeView implements ICollectorTradeView {

    private static final String CONTROLLER_NOT_CONNECTED = "Controller non connesso";

    private final InputManager inputManager;
    private LiveTradeController controller;
    // lazily created to avoid write-only warning when analyzer can't see usages
    private CliManageTradeView manageView;

    public CliCollectorTradeView(InputManager inputManager) {
        if (inputManager == null) {
            throw new IllegalArgumentException("InputManager cannot be null");
        }
        this.inputManager = inputManager;
    }

    public void setController(LiveTradeController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        System.out.printf("%n╔════════════════════════════════════╗%n");
        System.out.printf("║          TRADE PAGE                ║%n");
        System.out.printf("╚════════════════════════════════════╝%n");

        if (controller != null) {
            // controller will delegate to ManageTradeController via ApplicationController wiring
            controller.loadTrades();
        } else {
            System.out.println("⚠ Controller non connesso, impossibile caricare gli scambi.");
        }

        System.out.printf("%nMenu:%n");
        System.out.println("1. Torna alla Homepage");
        System.out.println("2. Vai alla Collezione");
        System.out.println("3. Logout");

        System.out.print("Scegli un'opzione: ");
        String choice = inputManager.readString();

        switch (choice) {
            case "1" -> {
                if (controller != null) controller.navigateToHome();
                else System.out.println(CONTROLLER_NOT_CONNECTED);
            }
            case "2" -> {
                if (controller != null) controller.navigateToCollection();
                else System.out.println(CONTROLLER_NOT_CONNECTED);
            }
            case "3" -> {
                if (controller != null) controller.onLogoutRequested();
                else System.out.println(CONTROLLER_NOT_CONNECTED);
            }
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
    public void refresh() {

        // For CLI, refreshing a trade view will attempt to reload trades via controller if available
        if (controller != null) {
            controller.loadTrades();
        }
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("ERROR: " + errorMessage);

    }

    @Override
    public void displayTrade(TradeTransactionBean t) {
        if (t == null) {
            System.out.println("Nessuna informazione sullo scambio disponibile.");
            return;
        }

        // Use printf with %n for platform-specific line separator
        System.out.printf("%n=== OVERVIEW SCAMBIO: tx-%d ===%n", t.getTransactionId());
        System.out.println("Proposer: " + (t.getProposerId() != null ? t.getProposerId() : "?"));
        System.out.println("Receiver: " + (t.getReceiverId() != null ? t.getReceiverId() : "?"));
        System.out.println("Store: " + (t.getStoreId() != null ? t.getStoreId() : "?"));
        System.out.println("Date: " + (t.getTradeDate() != null ? t.getTradeDate() : "?"));
        System.out.println("Status: " + (t.getStatus() != null ? t.getStatus() : "?"));

        printOffered(t);
        printRequested(t);

        // delegate interactive loop to helper to reduce cognitive complexity of this method
        handleInteractiveOptions(t);
    }

    @Override
    public void setUsername(String username) {
        // forward to manage view so CLI manage list can format correctly
        if (manageView == null) {
            manageView = new CliManageTradeView();
        }
        manageView.setUsername(username);
    }

    @Override
    public void displayIspection() {
        // For CLI we simply instruct the store operator to inspect and then wait for confirmation
        System.out.printf("%n--- ISPEZIONE CARTE (Store) ---%n");
        System.out.println("Per favore ispeziona le carte fisiche nel negozio e poi premi INVIO per confermare.");
        System.out.print("Premi INVIO quando l'ispezione è completa...");
        inputManager.readString(); // wait for Enter or input
        // After local confirmation, call onIspectionComplete with a placeholder username (store operator) if available
        onIspectionComplete("store");
    }

    public void onIspectionComplete(String username) {
        System.out.println("Ispezione completata da: " + (username != null ? username : "<unknown>"));
    }

    public void onTradeComplete(String id) {
        System.out.println("Lo scambio " + (id != null ? id : "<id>") + " è stato completato.");
    }

    @Override
    public void displayScheduledTrades(java.util.List<TradeTransactionBean> scheduled) {
        System.out.printf("%n=== SCHEDULED TRADES ===%n");
        if (scheduled == null || scheduled.isEmpty()) {
            System.out.println("No scheduled trades.");
            return;
        }
        for (int i = 0; i < scheduled.size(); i++) {
            TradeTransactionBean t = scheduled.get(i);
            System.out.printf("%d) tx-%d: %s vs %s @ %s%n", i + 1,
                    t.getTransactionId(),
                     t.getProposerId(), t.getReceiverId(), t.getStoreId());
        }
    }

    // Extracted helper to handle the interactive loop from displayTrade to reduce method complexity
    private void handleInteractiveOptions(TradeTransactionBean t) {
        while (true) {
            System.out.printf("%nAzioni:%n");
            System.out.println("1. Conferma la tua presenza");
            System.out.println("2. Richiedi ispezione (store)");
            System.out.println("3. Chiudi overview");
            System.out.print("Scegli un'opzione: ");
            String choice = inputManager.readString();
            switch (choice) {
                case "1" -> {
                    if (controller != null) {
                        int code = controller.confirmPresence(t.getTransactionId());
                        if (code > 0) System.out.println("Presenza confermata. Codice: " + code);
                        else System.out.println("Errore durante la conferma della presenza.");
                    } else System.out.println(CONTROLLER_NOT_CONNECTED);
                }

                case "2" -> displayIspection();

                case "3" -> {
                    return;
                }
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    // Helper to print offered cards - extracted to reduce cognitive complexity of displayTrade
    private void printOffered(TradeTransactionBean t) {
        System.out.printf("%nCarte offerte:%n");
        if (t.getOffered() == null || t.getOffered().isEmpty()) {
            System.out.println(" - (nessuna)");
        } else {
            t.getOffered().forEach(cb -> System.out.println(" - " + cb.getName() + " x" + cb.getQuantity()));
        }
    }

    // Helper to print requested cards - extracted to reduce cognitive complexity of displayTrade
    private void printRequested(TradeTransactionBean t) {
        System.out.printf("%nCarta richiesta:%n");
        if (t.getRequested() == null || t.getRequested().isEmpty()) {
            System.out.println(" - (nessuna)");
        } else {
            t.getRequested().forEach(cb -> System.out.println(" - " + cb.getName() + " x" + cb.getQuantity()));
        }
    }

}
