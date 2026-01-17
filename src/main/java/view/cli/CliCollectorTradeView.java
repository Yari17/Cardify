package view.cli;

import controller.LiveTradeController;
import config.InputManager;
import model.bean.TradeTransactionBean;
import view.ICollectorTradeView;

import java.util.List;


public class CliCollectorTradeView implements ICollectorTradeView {

    private static final String CONTROLLER_NOT_CONNECTED = "Controller non connesso";

    private final InputManager inputManager;
    private LiveTradeController controller;
    // lazily created to avoid write-only warning when analyzer can't see usages
    private CliManageTradeView manageView;
    // caches for lists provided by controller
    private java.util.List<TradeTransactionBean> scheduledCache = new java.util.ArrayList<>();
    private java.util.List<TradeTransactionBean> completedCache = new java.util.ArrayList<>();
    // currently logged-in username (collector)
    private String currentUsername;

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
            // controller will load and then call back displayScheduledTrades/displayCompletedTrades
            controller.loadTrades();
        } else {
            System.out.println("⚠ Controller non connesso, impossibile caricare gli scambi.");
        }

        // Render combined lists and prompt user to select a trade to view.
        renderCombinedTradeListLoop();
    }

    // Loop that renders scheduled and completed trades into a single numbered list and prompts selection
    private void renderCombinedTradeListLoop() {
        while (true) {
            // Build combined list: scheduled (non-completed) first, then completed/canceled
            java.util.List<TradeTransactionBean> combined = new java.util.ArrayList<>();

            System.out.printf("%n=== SCHEDULED TRADES ===%n");
            int idx = 0;
            if (scheduledCache != null && !scheduledCache.isEmpty()) {
                for (TradeTransactionBean t : scheduledCache) {
                    // ensure we only show non-completed in scheduled
                    String status = t.getStatus() != null ? t.getStatus().toUpperCase() : "";
                    if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) continue;
                    combined.add(t);
                    idx++;
                    System.out.printf("%d) tx-%d: %s vs %s @ %s%n", idx, t.getTransactionId(), t.getProposerId(), t.getReceiverId(), t.getStoreId());
                }
            }
            if (idx == 0) System.out.println("(nessuno)");

            System.out.printf("%n=== COMPLETED/CANCELLED TRADES ===%n");
            if (completedCache != null && !completedCache.isEmpty()) {
                for (TradeTransactionBean t : completedCache) {
                    combined.add(t);
                    idx++;
                    System.out.printf("%d) tx-%d: %s vs %s @ %s [%s]%n", idx, t.getTransactionId(), t.getProposerId(), t.getReceiverId(), t.getStoreId(), t.getStatus() != null ? t.getStatus() : "?");
                }
            } else {
                System.out.println("(nessuno)");
            }

            System.out.println();
            System.out.println("0) Torna all'homepage");
            System.out.print("Seleziona scambio da visualizzare: ");
            String sel = inputManager.readString();
            if (sel == null) sel = "";
            sel = sel.trim();
            if (sel.equals("0")) {
                if (controller != null) controller.navigateToHome();
                return;
            }
            try {
                int n = Integer.parseInt(sel);
                if (n <= 0 || n > combined.size()) {
                    System.out.println("Numero non valido. Riprova.");
                    continue;
                }
                TradeTransactionBean chosen = combined.get(n - 1);
                displayTrade(chosen);
                // after viewing details, loop back to the lists
            } catch (NumberFormatException ex) {
                System.out.println("Input non valido. Inserisci il numero dello scambio o 0 per tornare.");
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

        // If the current user already confirmed presence, show their session code
        try {
            if (currentUsername != null) {
                if (currentUsername.equals(t.getProposerId()) && t.isProposerArrived()) {
                    System.out.println("Il tuo session code: " + t.getProposerSessionCode());
                } else if (currentUsername.equals(t.getReceiverId()) && t.isReceiverArrived()) {
                    System.out.println("Il tuo session code: " + t.getReceiverSessionCode());
                }
            }
        } catch (Exception ignored) {}

        printOffered(t);
        printRequested(t);

        // delegate interactive loop to helper to reduce cognitive complexity of this method
        handleInteractiveOptions(t);
    }

    @Override
    public void setUsername(String username) {
        // forward to manage view so CLI manage list can format correctly
        this.currentUsername = username;
        if (manageView == null) manageView = new CliManageTradeView();
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
        // Cache scheduled list for later rendering
        this.scheduledCache = scheduled != null ? new java.util.ArrayList<>(scheduled) : new java.util.ArrayList<>();
    }

    @Override
    public void displayCompletedTrades(List<TradeTransactionBean> completedTrades) {
        // Cache completed list for later rendering
        this.completedCache = completedTrades != null ? new java.util.ArrayList<>(completedTrades) : new java.util.ArrayList<>();
    }

    // Extracted helper to handle the interactive loop from displayTrade to reduce method complexity
    private void handleInteractiveOptions(TradeTransactionBean t) {
        // Determine trade lifecycle
        String status = t.getStatus() != null ? t.getStatus().toUpperCase() : "";
        boolean isFinal = "COMPLETED".equals(status) || "CANCELLED".equals(status);

        // Determine if current user is proposer or receiver and arrival flag
        boolean isProposer = currentUsername != null && currentUsername.equals(t.getProposerId());
        boolean isReceiver = currentUsername != null && currentUsername.equals(t.getReceiverId());
        boolean userArrived = (isProposer && t.isProposerArrived()) || (isReceiver && t.isReceiverArrived());

        if (isFinal) {
            // Completed or cancelled: only allow returning to list
            while (true) {
                System.out.println("\n0) Torna alla lista di scambi");
                System.out.print("Scelta: ");
                String c = inputManager.readString();
                if (c != null && c.trim().equals("0")) return;
                System.out.println("Opzione non valida.");
            }
        }

        // Scheduled flow
        if (!userArrived) {
            // user hasn't confirmed presence yet: show only back + confirm
            while (true) {
                System.out.println("\n0) Torna alla lista di scambi");
                System.out.println("1) Conferma presenza");
                System.out.print("Scelta: ");
                String c = inputManager.readString();
                if (c == null) { System.out.println("Opzione non valida."); continue; }
                c = c.trim();
                if (c.equals("0")) return;
                if (c.equals("1")) {
                    if (controller != null) {
                        int code = controller.confirmPresence(t.getTransactionId());
                        if (code > 0) {
                            System.out.println("Presenza confermata. Codice: " + code);
                        } else {
                            System.out.println("Errore durante la conferma della presenza.");
                        }
                    } else {
                        System.out.println(CONTROLLER_NOT_CONNECTED);
                    }
                    // after confirming (success or not), return to list
                    return;
                }
                System.out.println("Opzione non valida.");
            }
        } else {
            // user already arrived: only back to list
            while (true) {
                System.out.println("\n0) Torna alla lista di scambi");
                System.out.print("Scelta: ");
                String c = inputManager.readString();
                if (c != null && c.trim().equals("0")) return;
                System.out.println("Opzione non valida.");
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
