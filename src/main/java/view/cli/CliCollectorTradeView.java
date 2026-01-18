package view.cli;

import controller.LiveTradeController;
import config.InputManager;
import model.bean.TradeTransactionBean;
import view.ICollectorTradeView;

import java.util.List;


public class CliCollectorTradeView implements ICollectorTradeView {

    private static final String CONTROLLER_NOT_CONNECTED = "Controller non connesso";
    private static final String BACK_TO_LIST = "0) Torna alla lista di scambi";
    private static final String PROMPT_CHOICE = "Scelta: ";
    private static final String INVALID_OPTION = "Opzione non valida.";
    private static final String INPUT_INVALID_NUMBER_MSG = "Input non valido. Inserisci il numero dello scambio o 0 per tornare.";

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
            java.util.List<TradeTransactionBean> combined = buildCombinedListAndPrint();

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

            TradeTransactionBean chosen = selectTradeFromCombined(sel, combined);
            if (chosen != null) {
                displayTrade(chosen);
            }
            // after viewing details, loop back to the lists
        }
    }

    // Parse selection string and return the selected TradeTransactionBean or null (and print message)
    private TradeTransactionBean selectTradeFromCombined(String sel, java.util.List<TradeTransactionBean> combined) {
        if (!isPositiveInteger(sel)) {
            System.out.println(INPUT_INVALID_NUMBER_MSG);
            return null;
        }
        int n = Integer.parseInt(sel);
        if (n <= 0 || n > combined.size()) {
            System.out.println("Numero non valido. Riprova.");
            return null;
        }
        return combined.get(n - 1);
    }

    // Build combined list and print entries; returns the combined list in the same order printed
    private java.util.List<TradeTransactionBean> buildCombinedListAndPrint() {
        java.util.List<TradeTransactionBean> combined = new java.util.ArrayList<>();
        int idx = printScheduledAndCollect(combined);
        printCompletedAndCollect(combined, idx);
        return combined;
    }

    private int printScheduledAndCollect(java.util.List<TradeTransactionBean> combined) {
        System.out.printf("%n=== SCHEDULED TRADES ===%n");
        int idx = 0;
        if (scheduledCache != null && !scheduledCache.isEmpty()) {
            for (TradeTransactionBean t : scheduledCache) {
                String status = t.getStatus() != null ? t.getStatus().toUpperCase() : "";
                if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) continue;
                combined.add(t);
                idx++;
                System.out.printf("%d) tx-%d: %s vs %s @ %s%n", idx, t.getTransactionId(), t.getProposerId(), t.getReceiverId(), t.getStoreId());
            }
        }
        if (idx == 0) System.out.println("(nessuno)");
        return idx;
    }

    private void printCompletedAndCollect(java.util.List<TradeTransactionBean> combined, int startIdx) {
        System.out.printf("%n=== COMPLETED/CANCELLED TRADES ===%n");
        int idx = startIdx;
        if (completedCache != null && !completedCache.isEmpty()) {
            for (TradeTransactionBean t : completedCache) {
                combined.add(t);
                idx++;
                System.out.printf("%d) tx-%d: %s vs %s @ %s [%s]%n", idx, t.getTransactionId(), t.getProposerId(), t.getReceiverId(), t.getStoreId(), t.getStatus() != null ? t.getStatus() : "?");
            }
        } else {
            System.out.println("(nessuno)");
        }
    }

    private boolean isPositiveInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        if (!s.matches("\\d+")) return false;
        // simple guard against exceedingly long numbers
        return s.length() <= 10;
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
        if (currentUsername != null) {
            if (currentUsername.equals(t.getProposerId()) && t.isProposerArrived()) {
                System.out.println("Il tuo session code: " + t.getProposerSessionCode());
            } else if (currentUsername.equals(t.getReceiverId()) && t.isReceiverArrived()) {
                System.out.println("Il tuo session code: " + t.getReceiverSessionCode());
            }
        }

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
            loopReturnToList();
        } else if (!userArrived) {
            // user hasn't confirmed presence yet: show only back + confirm
            loopConfirmPresence(t);
        } else {
            // user already arrived: only back to list
            loopReturnToList();
        }
    }

    private void loopReturnToList() {
        while (true) {
            System.out.println("\n" + BACK_TO_LIST);
            System.out.print(PROMPT_CHOICE);
            String c = inputManager.readString();
            if (c != null && c.trim().equals("0")) return;
            System.out.println(INVALID_OPTION);
        }
    }

    private void loopConfirmPresence(TradeTransactionBean t) {
        boolean keepAsking = true;
        while (keepAsking) {
            System.out.println("\n" + BACK_TO_LIST);
            System.out.println("1) Conferma presenza");
            System.out.print(PROMPT_CHOICE);
            String c = inputManager.readString();
            if (c == null) {
                System.out.println(INVALID_OPTION);
                continue;
            }
            c = c.trim();
            if (c.equals("0")) return;
            if (c.equals("1")) {
                confirmPresenceAction(t);
                // after confirming (success or not), stop asking and return to list
                keepAsking = false;
            } else {
                System.out.println(INVALID_OPTION);
            }
        }
    }

    private void confirmPresenceAction(TradeTransactionBean t) {
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
