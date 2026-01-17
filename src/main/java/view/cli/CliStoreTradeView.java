package view.cli;

import controller.LiveTradeController;
import config.InputManager;
import model.bean.TradeTransactionBean;
import view.IStoreTradeView;

import java.util.List;


public class CliStoreTradeView implements IStoreTradeView {

    private static final String CONTROLLER_NOT_CONNECTED = "Controller non connesso";

    private final InputManager inputManager;
    private LiveTradeController controller;
    private java.util.List<TradeTransactionBean> lastScheduled = new java.util.ArrayList<>();
    private java.util.List<TradeTransactionBean> lastInProgress = new java.util.ArrayList<>();
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

        if (controller != null) {
            // Do not auto-load lists here. The user selects which section to view first.
        } else {
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
            if (section == null) section = "";
            section = section.trim();
            if (section.equals("0")) {
                if (controller != null) controller.navigateBackToStoreHome();
                running = false;
                break;
            }
            if (section.equals("1")) {
                // Show scheduled trades and allow selecting one
                showScheduled = true;
                showInProgress = false;
                if (controller != null) controller.loadStoreScheduledTrades();
                // Allow the DAO callback displayScheduledTrades to render the list or 'none' message
                // after it populates lastScheduled. If there are no items, skip selection prompt.
                if (lastScheduled.isEmpty()) {
                    continue;
                }
                System.out.print("Seleziona scambio (numero) o 0 per tornare alla sezione: ");
                String sel = inputManager.readString();
                if (sel == null) sel = ""; sel = sel.trim();
                if (sel.equals("0")) continue;
                int selIdx;
                try { selIdx = Integer.parseInt(sel); } catch (NumberFormatException ex) { System.out.println("Input non valido."); continue; }
                if (selIdx <= 0 || selIdx > lastScheduled.size()) { System.out.println("Numero scambio non valido."); continue; }
                TradeTransactionBean selected = lastScheduled.get(selIdx - 1);
                displayTrade(selected);
                // after overview, reload both lists
                if (controller != null) { controller.loadStoreScheduledTrades(); controller.loadStoreInProgressTrades(); }
                continue;
            }
            if (section.equals("2")) {
                // Show in-progress/active trades and allow selecting one
                showInProgress = true;
                showScheduled = false;
                if (controller != null) controller.loadStoreInProgressTrades();
                // Let displayInProgressTrades print list or 'none' message; skip selection if empty
                if (lastInProgress.isEmpty()) {
                    continue;
                }
                System.out.print("Seleziona scambio attivo (numero) o 0 per tornare alla sezione: ");
                String sel = inputManager.readString();
                if (sel == null) sel = ""; sel = sel.trim();
                if (sel.equals("0")) continue;
                int selIdx;
                try { selIdx = Integer.parseInt(sel); } catch (NumberFormatException ex) { System.out.println("Input non valido."); continue; }
                if (selIdx <= 0 || selIdx > lastInProgress.size()) { System.out.println("Numero scambio non valido."); continue; }
                TradeTransactionBean selected = lastInProgress.get(selIdx - 1);
                displayTrade(selected);
                if (controller != null) { controller.loadStoreScheduledTrades(); controller.loadStoreInProgressTrades(); }
                continue;
            }
            System.out.println("Opzione non valida. Riprova.");
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
        // Interactive CLI implementation: show details and allow store actions including code validation
        if (t == null) {
            System.out.println("Nessuna informazione sullo scambio disponibile.");
            return;
        }

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
        // If trade already COMPLETED or CANCELLED, just show info and exit.
        String status = t.getStatus();
        if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            System.out.println("(Lo scambio è nello stato: " + status + ")");
            System.out.print("Premi INVIO per tornare...");
            inputManager.readString();
            return;
        }

        // If trade already in inspection or completed, show inspection menu instead of just waiting
        if ("INSPECTION_PHASE".equals(status)) {
            // interactive inspection menu for inspection phase
            while (true) {
                System.out.println("(Lo scambio è nello stato: " + status + ")");
                System.out.println("1) nessun problema con le carte dei collezionisti");
                System.out.println("2) ispezione NON passata per carte del proposer");
                System.out.println("3) ispezione NON passata per carte del receiver");
                System.out.println("0) torna indietro");
                System.out.print("Scelta: ");
                String choice = inputManager.readString();
                if (choice == null) choice = "";
                choice = choice.trim();
                if (choice.equals("0")) {
                    return; // go back to scheduled/in-progress listing
                }
                boolean actionResult = false;
                switch (choice) {
                    case "1" -> {
                        if (controller != null) {
                            controller.recordInspectionResult(t.getTransactionId(), t.getProposerId(), true);
                            controller.recordInspectionResult(t.getTransactionId(), t.getReceiverId(), true);
                            actionResult = true;
                        }
                    }
                    case "2" -> {
                        if (controller != null) actionResult = controller.recordInspectionResult(t.getTransactionId(), t.getProposerId(), false);
                    }
                    case "3" -> {
                        if (controller != null) actionResult = controller.recordInspectionResult(t.getTransactionId(), t.getReceiverId(), false);
                    }
                    default -> System.out.println("Scelta non valida");
                }
                TradeTransactionBean updated = controller != null ? controller.refreshTradeStatus(t.getTransactionId()) : null;
                if (updated != null) {
                    t = updated;
                    status = t.getStatus();
                }
                if ("COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                    System.out.println("Risultato: scambio ora in stato " + status + ". Premere INVIO per tornare.");
                    inputManager.readString();
                    return;
                }
                // If after the inspection action the status moved to INSPECTION_PASSED, present conclude menu
                if ("INSPECTION_PASSED".equalsIgnoreCase(status)) {
                    // present conclude menu (same as dedicated branch)
                    while (true) {
                        System.out.println("(Lo scambio è nello stato: " + status + ")");
                        System.out.println("1) esegui scambio");
                        System.out.println("0) torna alla homepage");
                        System.out.print("Scelta: ");
                        String choice2 = inputManager.readString();
                        if (choice2 == null) choice2 = "";
                        choice2 = choice2.trim();
                        if (choice2.equals("0")) {
                            if (controller != null) controller.navigateBackToStoreHome();
                            return;
                        }
                        if (choice2.equals("1")) {
                            if (controller != null) {
                                boolean res2 = controller.concludeTrade(t.getTransactionId());
                                if (res2) System.out.println("Scambio concluso con successo.");
                                else System.out.println("Impossibile concludere lo scambio.");
                                TradeTransactionBean upd2 = controller.refreshTradeStatus(t.getTransactionId());
                                if (upd2 != null) System.out.printf("Status aggiornato: %s%n", upd2.getStatus());
                                inputManager.readString();
                                return;
                            } else {
                                System.out.println("Controller non disponibile.");
                            }
                        } else {
                            System.out.println("Scelta non valida");
                        }
                    }
                }
                 // continue looping while still in inspection phase
             }
         } else if ("INSPECTION_PASSED".equals(status)) {
             // After inspection passed, store can conclude the trade
             while (true) {
                 System.out.println("(Lo scambio è nello stato: " + status + ")");
                 System.out.println("1) esegui scambio");
                 System.out.println("0) torna alla homepage");
                 System.out.print("Scelta: ");
                 String choice = inputManager.readString();
                 if (choice == null) choice = "";
                 choice = choice.trim();
                 if (choice.equals("0")) {
                     if (controller != null) controller.navigateBackToStoreHome();
                     return;
                 }
                 if (choice.equals("1")) {
                     if (controller != null) {
                         boolean res = controller.concludeTrade(t.getTransactionId());
                         if (res) {
                             System.out.println("Scambio concluso con successo.");
                         } else {
                             System.out.println("Impossibile concludere lo scambio.");
                         }
                         // refresh and show final status
                         TradeTransactionBean updated = controller.refreshTradeStatus(t.getTransactionId());
                         if (updated != null) {
                             System.out.printf("Status aggiornato: %s%n", updated.getStatus());
                         }
                         inputManager.readString();
                         return;
                     } else {
                         System.out.println("Controller non disponibile.");
                     }
                 } else {
                     System.out.println("Scelta non valida");
                 }
             }
         }

        // Otherwise offer to validate session codes
        System.out.println("Inserisci i session code per validare la coppia e avviare l'ispezione:");
        System.out.print("Codice proposer: ");
        String ptxt = inputManager.readString().trim();
        System.out.print("Codice receiver: ");
        String rtxt = inputManager.readString().trim();

        int pcode, rcode;
        try {
            pcode = Integer.parseInt(ptxt);
            rcode = Integer.parseInt(rtxt);
        } catch (NumberFormatException ex) {
            System.out.println("Codici non validi, devono essere numeri interi");
            return;
        }

        // Find the transaction matching the pair (optional but mirrors FX behavior)
        TradeTransactionBean found = null;
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
        // Validate both codes via controller (domain will update status to INSPECTION_PHASE when appropriate)
        boolean okP = controller.verifySessionCode(txId, pcode);
        boolean okR = controller.verifySessionCode(txId, rcode);
        if (!okP || !okR) {
            System.out.println("Uno o entrambi i codici non sono validi");
            return;
        }

        // Refresh and display updated info
        TradeTransactionBean updated = controller.refreshTradeStatus(txId);
        if (updated == null) {
            System.out.println("Impossibile aggiornare lo scambio dopo la convalida");
            return;
        }
        System.out.println("Codici convalidati. Stato aggiornato: " + updated.getStatus());

        // If now in inspection phase, allow store to mark inspection results
        if ("INSPECTION_PHASE".equals(updated.getStatus()) || "INSPECTION_PASSED".equals(updated.getStatus())) {
            while (true) {
                System.out.println("\n--- Azioni Ispezione ---");
                System.out.println("1) nessun problema con le carte dei collezionisti");
                System.out.println("2) ispezione NON passata per carte del proposer");
                System.out.println("3) ispezione NON passata per carte del receiver");
                System.out.println("0) Torna indietro");
                System.out.print("Scelta: ");
                String choice = inputManager.readString().trim();
                switch (choice) {
                    case "1" -> {
                        controller.recordInspectionResult(txId, updated.getProposerId(), true);
                        controller.recordInspectionResult(txId, updated.getReceiverId(), true);
                        updated = controller.refreshTradeStatus(txId);
                        System.out.println("Aggiornato: " + (updated != null ? updated.getStatus() : "<null>"));
                    }
                    case "2" -> {
                        controller.recordInspectionResult(txId, updated.getProposerId(), false);
                        updated = controller.refreshTradeStatus(txId);
                        System.out.println("Aggiornato: " + (updated != null ? updated.getStatus() : "<null>"));
                        if (updated != null && "CANCELLED".equals(updated.getStatus())) return;
                    }
                    case "3" -> {
                        controller.recordInspectionResult(txId, updated.getReceiverId(), false);
                        updated = controller.refreshTradeStatus(txId);
                        System.out.println("Aggiornato: " + (updated != null ? updated.getStatus() : "<null>"));
                        if (updated != null && "CANCELLED".equals(updated.getStatus())) return;
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Scelta non valida");
                }
                if (updated != null && ("COMPLETED".equalsIgnoreCase(updated.getStatus()) || "CANCELLED".equalsIgnoreCase(updated.getStatus()))) {
                    System.out.println("Risultato: scambio ora in stato " + updated.getStatus() + ". Premere INVIO per tornare.");
                    inputManager.readString();
                    return;
                }
            }
        }
    }

    @Override
    public void displayInProgressTrades(List<TradeTransactionBean> inProgress) {
        // Always update cached list, but render only if user requested the active section
        lastInProgress.clear();
        if (inProgress != null) {
            for (TradeTransactionBean t : inProgress) {
                if (t == null) continue;
                String s = t.getStatus() != null ? t.getStatus() : "?";
                if (!("INSPECTION_PHASE".equalsIgnoreCase(s) || "INSPECTION_PASSED".equalsIgnoreCase(s))) continue;
                lastInProgress.add(t);
            }
        }
        if (!showInProgress) return;
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

    }


    @Override
    public void displayScheduledTrades(List<TradeTransactionBean> scheduled) {
        // Always update cached list, but render only if user requested the scheduled section
        lastScheduled.clear();
        if (scheduled != null) {
            for (TradeTransactionBean t : scheduled) {
                if (t == null) continue;
                String s = t.getStatus() != null ? t.getStatus() : "?";
                // exclude COMPLETED/CANCELLED and INSPECTION_* from scheduled
                if ("COMPLETED".equalsIgnoreCase(s) || "CANCELLED".equalsIgnoreCase(s) || "INSPECTION_PHASE".equalsIgnoreCase(s) || "INSPECTION_PASSED".equalsIgnoreCase(s)) continue;
                lastScheduled.add(t);
            }
        }
        if (!showScheduled) return;
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

    }
}

