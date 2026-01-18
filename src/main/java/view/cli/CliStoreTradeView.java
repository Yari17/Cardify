package view.cli;

import controller.LiveTradeController;
import config.InputManager;
import model.bean.TradeTransactionBean;
import view.IStoreTradeView;

import java.util.List;


public class CliStoreTradeView implements IStoreTradeView {

    // Status constants to avoid duplicated literals
    private static final String STATUS_INSPECTION_PASSED = "INSPECTION_PASSED";
    private static final String STATUS_INSPECTION_PHASE = "INSPECTION_PHASE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PREFIX = "(Lo scambio è nello stato: ";
    private static final String CHOICE_PROMPT = "Scelta: ";
    private static final String INVALID_CHOICE = "Scelta non valida";
    private static final String UPDATED_PREFIX = "Aggiornato: ";
    private static final String NULL_LITERAL = "<null>";

    private final InputManager inputManager;
    private LiveTradeController controller;
    private final java.util.List<TradeTransactionBean> lastScheduled = new java.util.ArrayList<>();
    private final java.util.List<TradeTransactionBean> lastInProgress = new java.util.ArrayList<>();
    // Flags to control whether incoming DAO-driven display calls should render immediately
    private boolean showScheduled = false;
    private boolean showInProgress = false;

    public CliStoreTradeView(InputManager inputManager) {
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
        System.out.println("****************TRADE PAGE **********************");

        if (controller == null) {
            System.out.println("⚠ Controller non connesso, impossibile caricare gli scambi.");
        }

        boolean running = true;
        while (running) {
            // Section selection menu: choose which list to view
            System.out.println();
            System.out.println("1) Scambi programmati");
            System.out.println("2) Scambi attivi");
            System.out.println("0) Torna alla Homepage");
            System.out.print("Scegli una sezione: ");
            String section = inputManager.readString();
            if (section == null) {
                section = "";
            }
            section = section.trim();

            switch (section) {
                case "0":
                    if (controller != null) controller.navigateBackToStoreHome();
                    running = false;
                    break;
                case "1":
                    handleScheduledSection();
                    break;
                case "2":
                    handleInProgressSection();
                    break;
                default:
                    System.out.println("Opzione non valida. Riprova.");
            }
        }
    }

    // Extracted handlers to reduce the size and complexity of display()
    private void handleScheduledSection() {
        showScheduled = true;
        showInProgress = false;
        if (controller != null) controller.loadStoreScheduledTrades();
        // The callback to displayScheduledTrades will populate lastScheduled; if empty, nothing to select
        if (lastScheduled.isEmpty()) {
            return;
        }
        System.out.print("Seleziona scambio (numero) o 0 per tornare alla sezione: ");
        String sel = inputManager.readString();
        if (sel == null) {
            sel = "";
        }
        sel = sel.trim();
        if (sel.equals("0")) return;
        int selIdx;
        try {
            selIdx = Integer.parseInt(sel);
        } catch (NumberFormatException _) {
            System.out.println("Input non valido.");
            return;
        }
        if (selIdx <= 0 || selIdx > lastScheduled.size()) {
            System.out.println("Numero scambio non valido.");
            return;
        }
        TradeTransactionBean selected = lastScheduled.get(selIdx - 1);
        displayTrade(selected);
        // after overview, reload both lists
        if (controller != null) {
            controller.loadStoreScheduledTrades();
            controller.loadStoreInProgressTrades();
        }
    }

    private void handleInProgressSection() {
        showInProgress = true;
        showScheduled = false;
        if (controller != null) controller.loadStoreInProgressTrades();
        if (lastInProgress.isEmpty()) {
            return;
        }
        System.out.print("Seleziona scambio attivo (numero) o 0 per tornare alla sezione: ");
        String sel = inputManager.readString();
        if (sel == null) {
            sel = "";
        }
        sel = sel.trim();
        if (sel.equals("0")) return;
        int selIdx;
        try {
            selIdx = Integer.parseInt(sel);
        } catch (NumberFormatException _) {
            System.out.println("Input non valido.");
            return;
        }
        if (selIdx <= 0 || selIdx > lastInProgress.size()) {
            System.out.println("Numero scambio non valido.");
            return;
        }
        TradeTransactionBean selected = lastInProgress.get(selIdx - 1);
        displayTrade(selected);
        if (controller != null) {
            controller.loadStoreScheduledTrades();
            controller.loadStoreInProgressTrades();
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

        printTradeOverview(t);

        String status = t.getStatus();
        if (STATUS_COMPLETED.equals(status) || STATUS_CANCELLED.equals(status)) {
            System.out.println(STATUS_PREFIX + status + ")");
            System.out.print("Premi INVIO per tornare...");
            inputManager.readString();
            return;
        }

        if (STATUS_INSPECTION_PHASE.equals(status)) {
            // interactive inspection menu for inspection phase
            handleInspectionPhase(t);
            return;
        }

        if (STATUS_INSPECTION_PASSED.equals(status)) {
            presentConcludeMenu(t);
            return;
        }

        // Otherwise offer to validate session codes
        handleSessionCodeValidation();
    }

    // Small extraction to reduce method size
    private void printTradeOverview(TradeTransactionBean t) {
        System.out.printf("%n=== OVERVIEW SCAMBIO: tx-%d ===%n", t.getTransactionId());
        System.out.println("Proposer: " + (t.getProposerId() != null ? t.getProposerId() : "?"));
        System.out.println("Receiver: " + (t.getReceiverId() != null ? t.getReceiverId() : "?"));
        System.out.println("Store: " + (t.getStoreId() != null ? t.getStoreId() : "?"));
        System.out.println("Date: " + (t.getTradeDate() != null ? t.getTradeDate() : "?"));
        System.out.println("Status: " + (t.getStatus() != null ? t.getStatus() : "?"));

        System.out.println();
        System.out.println("Carte offerte:");
        if (t.getOffered() == null || t.getOffered().isEmpty()) System.out.println(" - (nessuna)");
        else t.getOffered().forEach(cb -> System.out.println(" - " + cb.getName() + " x" + cb.getQuantity()));
        System.out.println();
        System.out.println("Carte richieste:");
        if (t.getRequested() == null || t.getRequested().isEmpty()) System.out.println(" - (nessuna)");
        else t.getRequested().forEach(cb -> System.out.println(" - " + cb.getName() + " x" + cb.getQuantity()));
        System.out.println();
    }

    // Handles loops and choices while in INSPECTION_PHASE
    private void handleInspectionPhase(TradeTransactionBean t) {
        String status = t.getStatus();
        while (true) {
            System.out.println(STATUS_PREFIX + status + ")");
            System.out.println("1) nessun problema con le carte dei collezionisti");
            System.out.println("2) ispezione NON passata per carte del proposer");
            System.out.println("3) ispezione NON passata per carte del receiver");
            System.out.println("0) torna indietro");
            System.out.print(CHOICE_PROMPT);
            String choice = inputManager.readString();
            if (choice == null) choice = "";
            choice = choice.trim();
            if (choice.equals("0")) return;
            TradeTransactionBean updated = processInspectionChoiceFromBean(t, choice);
            if (updated != null) {
                t = updated;
                status = t.getStatus();
            }
            if (STATUS_COMPLETED.equalsIgnoreCase(status) || STATUS_CANCELLED.equalsIgnoreCase(status)) {
                System.out.println("Risultato: scambio ora in stato " + status + ". Premere INVIO per tornare.");
                inputManager.readString();
                return;
            }
            if (STATUS_INSPECTION_PASSED.equalsIgnoreCase(status)) {
                presentConcludeMenu(t);
                return;
            }
        }
    }

    private TradeTransactionBean processInspectionChoiceFromBean(TradeTransactionBean t, String choice) {
        if (choice == null) return t;
        switch (choice) {
            case "1":
                if (controller != null) {
                    controller.recordInspectionResult(t.getTransactionId(), t.getProposerId(), true);
                    controller.recordInspectionResult(t.getTransactionId(), t.getReceiverId(), true);
                }
                break;
            case "2":
                if (controller != null) controller.recordInspectionResult(t.getTransactionId(), t.getProposerId(), false);
                break;
            case "3":
                if (controller != null) controller.recordInspectionResult(t.getTransactionId(), t.getReceiverId(), false);
                break;
            default:
                System.out.println(INVALID_CHOICE);
                return t;
        }
        return controller != null ? controller.refreshTradeStatus(t.getTransactionId()) : t;
    }

    private void presentConcludeMenu(TradeTransactionBean t) {
        String status = t.getStatus();
        while (true) {
            System.out.println(STATUS_PREFIX + status + ")");
            System.out.println("1) esegui scambio");
            System.out.println("0) torna alla homepage");
            System.out.print(CHOICE_PROMPT);
            String choice2 = inputManager.readString();
            if (choice2 == null) choice2 = "";
            choice2 = choice2.trim();
            if (choice2.equals("0")) {
                if (controller != null) controller.navigateBackToStoreHome();
                return;
            }
            if (choice2.equals("1")) {
                executeTradeConcludeAction(t);
                return;
            } else {
                System.out.println(INVALID_CHOICE);
            }
        }
    }

    private void executeTradeConcludeAction(TradeTransactionBean t) {
        if (controller != null) {
            boolean res2 = controller.concludeTrade(t.getTransactionId());
            if (res2) System.out.println("Scambio concluso con successo.");
            else System.out.println("Impossibile concludere lo scambio.");
            TradeTransactionBean upd2 = controller.refreshTradeStatus(t.getTransactionId());
            if (upd2 != null) System.out.printf("Status aggiornato: %s%n", upd2.getStatus());
            inputManager.readString();
        } else {
            System.out.println("Controller non disponibile.");
        }
    }

    // Handles session-code validation flow (the default path when entering displayTrade)
    private void handleSessionCodeValidation() {
        System.out.println("Inserisci i session code per validare la coppia e avviare l'ispezione:");
        System.out.print("Codice proposer: ");
        String ptxt = inputManager.readString();
        if (ptxt == null) ptxt = "";
        ptxt = ptxt.trim();
        System.out.print("Codice receiver: ");
        String rtxt = inputManager.readString();
        if (rtxt == null) rtxt = "";
        rtxt = rtxt.trim();

        int pcode;
        try {
            pcode = Integer.parseInt(ptxt);
        } catch (NumberFormatException _) {
            System.out.println("Codici non validi, devono essere numeri interi");
            return;
        }

        int rcode;
        try {
            rcode = Integer.parseInt(rtxt);
        } catch (NumberFormatException _) {
            System.out.println("Codici non validi, devono essere numeri interi");
            return;
        }

        TradeTransactionBean found;
        try {
            found = controller.fetchTradeBySessionCodes(pcode, rcode);
        } catch (Exception ex) {
            System.out.println("Errore nella ricerca della transazione: " + ex.getMessage());
            return;
        }
        if (found == null) {
            System.out.println("Nessuno scambio trovato per questa coppia di codici");
            return;
        }

        int txId = found.getTransactionId();
        boolean okP = controller.verifySessionCode(txId, pcode);
        boolean okR = controller.verifySessionCode(txId, rcode);
        if (!okP || !okR) {
            System.out.println("Uno o entrambi i codici non sono validi");
            return;
        }

        TradeTransactionBean updated = controller.refreshTradeStatus(txId);
        if (updated == null) {
            System.out.println("Impossibile aggiornare lo scambio dopo la convalida");
            return;
        }
        System.out.println("Codici convalidati. Stato aggiornato: " + updated.getStatus());

        if (STATUS_INSPECTION_PHASE.equals(updated.getStatus()) || STATUS_INSPECTION_PASSED.equals(updated.getStatus())) {
            // Reuse inspection actions loop to allow marking results
            // If cancelled/completed inside the loop, the helper will return
            handleInspectionActionsAfterValidation(txId);
        }
    }

    private void handleInspectionActionsAfterValidation(int txId) {
        TradeTransactionBean updated = controller.refreshTradeStatus(txId);
        while (true) {
            System.out.println("\n--- Azioni Ispezione ---");
            System.out.println("1) nessun problema con le carte dei collezionisti");
            System.out.println("2) ispezione NON passata per carte del proposer");
            System.out.println("3) ispezione NON passata per carte del receiver");
            System.out.println("0) Torna indietro");
            System.out.print(CHOICE_PROMPT);
            String choice = inputManager.readString();
            if (choice == null) choice = "";
            choice = choice.trim();
            updated = processInspectionChoiceFromTx(txId, updated, choice);
            if (updated != null && (STATUS_COMPLETED.equalsIgnoreCase(updated.getStatus()) || STATUS_CANCELLED.equalsIgnoreCase(updated.getStatus()))) {
                System.out.println("Risultato: scambio ora in stato " + updated.getStatus() + ". Premere INVIO per tornare.");
                inputManager.readString();
                return;
            }
        }
    }

    private TradeTransactionBean processInspectionChoiceFromTx(int txId, TradeTransactionBean updated, String choice) {
        if (choice == null) return updated;
        // Ensure updated is available
        if (updated == null) {
            updated = controller != null ? controller.refreshTradeStatus(txId) : null;
            if (updated == null) {
                System.out.println("Errore: transazione non reperibile");
                return null;
            }
        }
        switch (choice) {
            case "1":
                controller.recordInspectionResult(txId, updated.getProposerId(), true);
                controller.recordInspectionResult(txId, updated.getReceiverId(), true);
                break;
            case "2":
                controller.recordInspectionResult(txId, updated.getProposerId(), false);
                break;
            case "3":
                controller.recordInspectionResult(txId, updated.getReceiverId(), false);
                break;
            case "0":
                return updated;
            default:
                System.out.println(INVALID_CHOICE);
                return updated;
        }
        updated = controller.refreshTradeStatus(txId);
        System.out.println(UPDATED_PREFIX + (updated != null ? updated.getStatus() : NULL_LITERAL));
        return updated;
    }

    @Override
    public void displayInProgressTrades(List<TradeTransactionBean> inProgress) {
        // Update cache then render via helper
        lastInProgress.clear();
        if (inProgress != null) {
            for (TradeTransactionBean t : inProgress) {
                if (t == null) continue;
                String s = t.getStatus() != null ? t.getStatus() : "?";
                boolean isInspection = STATUS_INSPECTION_PHASE.equalsIgnoreCase(s) || STATUS_INSPECTION_PASSED.equalsIgnoreCase(s);
                if (isInspection) lastInProgress.add(t);
            }
        }
        if (!showInProgress) return;
        printInProgressList();
    }

    private void printInProgressList() {
        System.out.printf("%n=== ACTIVE TRADES ===%n");
        if (lastInProgress.isEmpty()) {
            System.out.println("Nessuno scambio in corso al momento.");
            return;
        }
        int count = 0;
        for (TradeTransactionBean t : lastInProgress) {
            count++;
            System.out.printf("%d) tx-%d: %s vs %s @ %s [%s]%n", count, t.getTransactionId(), t.getProposerId(), t.getReceiverId(), t.getStoreId(), t.getStatus() != null ? t.getStatus() : "?");
        }
    }
    @Override
    public void showMessage(String message) {
        // CLI: display the message to the user
        if (message != null) System.out.println(message);
    }

    @Override
    public void displayScheduledTrades(List<TradeTransactionBean> scheduled) {
         // Always update cached list, but render only if user requested the scheduled section
         lastScheduled.clear();
         if (scheduled != null) {
             for (TradeTransactionBean t : scheduled) {
                 if (t == null) continue;
                 String s = t.getStatus() != null ? t.getStatus() : "?";
                 boolean isExcluded = STATUS_COMPLETED.equalsIgnoreCase(s) || STATUS_CANCELLED.equalsIgnoreCase(s) || STATUS_INSPECTION_PHASE.equalsIgnoreCase(s) || STATUS_INSPECTION_PASSED.equalsIgnoreCase(s);
                 if (!isExcluded) {
                     lastScheduled.add(t);
                 }
             }
         }
         if (!showScheduled) return;
         printScheduledList();
     }

    private void printScheduledList() {
        System.out.printf("%n=== SCHEDULED TRADES ===%n");
        if (lastScheduled.isEmpty()) {
            System.out.println("Nessun scambio programmato.");
            return;
        }
        int displayIndex = 0;
        for (TradeTransactionBean t : lastScheduled) {
            displayIndex++;
            System.out.printf("%d) tx-%d: %s vs %s @ %s [%s]%n", displayIndex, t.getTransactionId(), t.getProposerId(), t.getReceiverId(), t.getStoreId(), t.getStatus() != null ? t.getStatus() : "?");
        }
    }
    @Override
    public void displayCompletedTrades(List<TradeTransactionBean> trades) {
        // Intentionally left empty
    }
}

