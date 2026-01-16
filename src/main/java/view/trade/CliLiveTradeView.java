package view.trade;

import controller.LiveTradeController;
import config.InputManager;
import model.bean.TradeTransactionBean;
import view.managetrade.CliManageTradeView;


public class CliLiveTradeView implements ILiveTradeView {

    private final InputManager inputManager;
    private LiveTradeController controller;
    private final CliManageTradeView manageView = new CliManageTradeView();

    public CliLiveTradeView(InputManager inputManager) {
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
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║          TRADE PAGE                ║");
        System.out.println("╚════════════════════════════════════╝");

        if (controller != null) {
            // controller will delegate to ManageTradeController via ApplicationController wiring
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
            case "1" -> {
                if (controller != null) controller.navigateToHome(); else System.out.println("Controller non connesso");
            }
            case "2" -> {
                if (controller != null) controller.navigateToCollection(); else System.out.println("Controller non connesso");
            }
            case "3" -> {
                if (controller != null) controller.onLogoutRequested(); else System.out.println("Controller non connesso");
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
    public void showError(String errorMessage) {

    }

    @Override
    public void displayTrade(TradeTransactionBean t) {
        if (t == null) {
            System.out.println("Nessuna informazione sullo scambio disponibile.");
            return;
        }

        System.out.println("\n=== OVERVIEW SCAMBIO: " + (t.getProposalId() != null ? t.getProposalId() : "<unknown>") + " ===");
        System.out.println("Proposer: " + (t.getProposerId() != null ? t.getProposerId() : "?"));
        System.out.println("Receiver: " + (t.getReceiverId() != null ? t.getReceiverId() : "?"));
        System.out.println("Store: " + (t.getStoreId() != null ? t.getStoreId() : "?"));
        System.out.println("Date: " + (t.getTradeDate() != null ? t.getTradeDate() : "?"));
        System.out.println("Status: " + (t.getStatus() != null ? t.getStatus() : "?"));

        System.out.println("\nCarte offerte:");
        if (t.getOffered() == null || t.getOffered().isEmpty()) {
            System.out.println(" - (nessuna)");
        } else {
            t.getOffered().forEach(cb -> System.out.println(" - " + cb.getName() + " x" + cb.getQuantity()));
        }

        System.out.println("\nCarta richiesta:");
        if (t.getRequested() == null || t.getRequested().isEmpty()) {
            System.out.println(" - (nessuna)");
        } else {
            t.getRequested().forEach(cb -> System.out.println(" - " + cb.getName() + " x" + cb.getQuantity()));
        }

        // Simple interactive options for CLI trade page
        while (true) {
            System.out.println("\nAzioni:");
            System.out.println("1. Conferma la tua presenza");
            System.out.println("2. Richiedi ispezione (store)");
            System.out.println("3. Chiudi overview");
            System.out.print("Scegli un'opzione: ");
            String choice = inputManager.readString();
            switch (choice) {
                case "1" -> {
                    onConfirmPresence(t.getProposalId());
                }
                case "2" -> {
                    displayIspection();
                }
                case "3" -> {
                    return;
                }
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    @Override
    public void setUsername(String username) {
        // forward to manage view so CLI manage list can format correctly
        manageView.setUsername(username);
    }

    @Override
    public void onConfirmPresence(String id) {
        if (id == null) {
            System.out.println("Proposal id mancante");
            return;
        }
        if (controller == null) {
            System.out.println("Controller non connesso: impossibile confermare presenza");
            return;
        }
        int code = controller.confirmPresence(id);
        if (code > 0) {
            System.out.println("Presenza confermata. Codice: " + code);
        } else {
            System.out.println("Errore durante la conferma della presenza.");
        }
    }

    @Override
    public void displayIspection() {
        // For CLI we simply instruct the store operator to inspect and then wait for confirmation
        System.out.println("\n--- ISPEZIONE CARTE (Store) ---");
        System.out.println("Per favore ispeziona le carte fisiche nel negozio e poi premi INVIO per confermare.");
        System.out.print("Premi INVIO quando l'ispezione è completa...");
        inputManager.readString(); // wait for Enter or input
        // After local confirmation, call onIspectionComplete with a placeholder username (store operator) if available
        onIspectionComplete("store");
    }

    @Override
    public void onIspectionComplete(String username) {
        System.out.println("Ispezione completata da: " + (username != null ? username : "<unknown>"));
    }

    @Override
    public void onTradeComplete(String id) {
        System.out.println("Lo scambio " + (id != null ? id : "<id>") + " è stato completato.");
    }

    @Override
    public void displayScheduledTrades(java.util.List<TradeTransactionBean> scheduled) {
        System.out.println("\n=== SCHEDULED TRADES ===");
        if (scheduled == null || scheduled.isEmpty()) {
            System.out.println("No scheduled trades.");
            return;
        }
        for (int i = 0; i < scheduled.size(); i++) {
            TradeTransactionBean t = scheduled.get(i);
            System.out.printf("%d) %s: %s vs %s @ %s\n", i+1,
                    t.getProposalId() != null ? t.getProposalId() : ("tx-" + t.getTransactionId()),
                    t.getProposerId(), t.getReceiverId(), t.getStoreId());
        }
    }


}
