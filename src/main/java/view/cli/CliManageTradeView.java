package view.cli;

import controller.ManageTradeController;
import model.bean.ProposalBean;
import view.IManageTradeView;

import java.util.List;

public class CliManageTradeView implements IManageTradeView {
    private String currentUsername;
    private ManageTradeController manageController;
    // callbacks
    private java.util.function.Consumer<String> onAcceptCallback;
    private java.util.function.Consumer<String> onDeclineCallback;
    private java.util.function.Consumer<String> onCancelCallback;
    private java.util.function.Consumer<String> onTradeClickCallback;
    private java.util.function.Consumer<String> onTradeNowClickCallback;
    private java.util.List<model.bean.ProposalBean> lastPending = new java.util.ArrayList<>();
    private java.util.List<model.bean.ProposalBean> lastConcluded = new java.util.ArrayList<>();

    @Override
    public void display() {
        // Simplified flow: show only pending proposals; prompt 0 to go back or a number to view
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        try {
            // Ensure latest pending proposals are loaded
            if (manageController != null) {
                manageController.loadAndDisplayTrades(this);
            }

            while (true) {
                // Display only pending proposals (minimal view as requested)
                System.out.println("=== PROPOSTE IN ATTESA ===");
                if (lastPending == null || lastPending.isEmpty()) {
                    System.out.println("(nessuna proposta in attesa)");
                } else {
                    for (int i = 0; i < lastPending.size(); i++) {
                        model.bean.ProposalBean p = lastPending.get(i);
                        String other;
                        boolean incoming = false;
                        if (currentUsername != null) {
                            incoming = currentUsername.equals(p.getToUser());
                            other = currentUsername.equals(p.getFromUser()) ? p.getToUser() : p.getFromUser();
                        } else {
                            other = p.getFromUser() != null ? p.getFromUser() : p.getToUser();
                        }
                        String direction = incoming ? "(ricevuta)" : "(inviata)";
                        System.out.printf("%d) %s %s - %s %n", i + 1, p.getProposalId(), direction, other);
                    }
                }

                System.out.println();
                System.out.println("0) Torna all'homepage");
                System.out.print("Inserisci numero di proposta da visualizzare: ");

                String line;
                try {
                    line = scanner.nextLine();
                } catch (Exception ex) {
                    // EOF -> return to caller
                    return;
                }
                if (line == null) return;
                line = line.trim();
                if (line.isEmpty()) continue;

                if ("0".equals(line)) {
                    if (manageController != null) {
                        manageController.navigateToHome();
                    }
                    return;
                }

                int idx;
                try {
                    idx = Integer.parseInt(line) - 1;
                } catch (NumberFormatException ex) {
                    System.out.println("Input non valido. Inserisci 0 o il numero della proposta.");
                    continue;
                }

                if (lastPending == null || idx < 0 || idx >= lastPending.size()) {
                    System.out.println("Numero proposta non valido.");
                    continue;
                }

                // Show proposal details
                model.bean.ProposalBean sel = lastPending.get(idx);
                System.out.println("\n=== OVERVIEW PROPOSTA: " + sel.getProposalId() + " ===");
                System.out.println("Proposer: " + sel.getFromUser());
                System.out.println("Receiver: " + sel.getToUser());
                System.out.println();

                System.out.println("== CARTE OFFERTE DA " + sel.getFromUser() + " ==");
                if (sel.getOffered() == null || sel.getOffered().isEmpty()) {
                    System.out.println("(nessuna carta)");
                } else {
                    for (model.bean.CardBean cb : sel.getOffered()) {
                        System.out.printf(" - %s x%d\n", cb.getName(), cb.getQuantity());
                    }
                }

                System.out.println();
                System.out.println("== CARTE RICHIESTE IN CAMBIO ==");
                if (sel.getRequested() == null || sel.getRequested().isEmpty()) {
                    System.out.println("(nessuna carta)");
                } else {
                    for (model.bean.CardBean cb : sel.getRequested()) {
                        System.out.printf(" - %s x%d\n", cb.getName(), cb.getQuantity());
                    }
                }

                boolean isReceived = currentUsername != null && currentUsername.equals(sel.getToUser());
                if (isReceived) {
                    // Offer accept/reject/back
                    System.out.println();
                    System.out.println("1) Accetta");
                    System.out.println("2) Rifiuta");
                    System.out.println("0) Indietro");
                    System.out.print("Scelta: ");
                    String choice2 = scanner.nextLine();
                    if (choice2 == null) return;
                    choice2 = choice2.trim();
                    switch (choice2) {
                        case "1" -> onAcceptTradeProposal(sel.getProposalId());
                        case "2" -> onDeclineTradeProposal(sel.getProposalId());
                        default -> {/* back */}
                    }
                } else {
                    // Sender: only back
                    System.out.println();
                    System.out.println("0) Indietro");
                    System.out.print("Premi INVIO per tornare...");
                    scanner.nextLine();
                }
                // After action, reload pending list
                if (manageController != null) manageController.loadAndDisplayTrades(this);
            }
        } finally {
            // do not close System.in
        }
    }

    @Override
    public void close() {
        // nothing to close for CLI
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("[ERROR] " + errorMessage);
    }

    @Override
    public void displayTrades(List<ProposalBean> pending, List<ProposalBean> scheduled) {
        // Cache lists; do not print here to avoid duplicate output. display() prints pending.
        this.lastPending = pending != null ? new java.util.ArrayList<>(pending) : new java.util.ArrayList<>();
        this.lastConcluded = scheduled != null ? new java.util.ArrayList<>(scheduled) : new java.util.ArrayList<>();
    }

    @Override
    public void refresh() {
        // Intentionally no-op for CLI to avoid duplicate printing; display() controls output.
    }

    public void setManageController(ManageTradeController controller) {
        this.manageController = controller;
    }

    @Override
    public void registerOnAccept(java.util.function.Consumer<String> onAccept) { this.onAcceptCallback = onAccept; }

    @Override
    public void registerOnDecline(java.util.function.Consumer<String> onDecline) { this.onDeclineCallback = onDecline; }

    @Override
    public void registerOnCancel(java.util.function.Consumer<String> onCancel) { this.onCancelCallback = onCancel; }

    @Override
    public void registerOnTradeClick(java.util.function.Consumer<String> onTradeClick) { this.onTradeClickCallback = onTradeClick; }

    @Override
    public void registerOnTradeNowClick(java.util.function.Consumer<String> onTradeNowClick) { this.onTradeNowClickCallback = onTradeNowClick; }

    public void onAcceptTradeProposal(String id) {
        if (id == null) return;
        if (onAcceptCallback != null) { onAcceptCallback.accept(id); return; }
        if (manageController == null) { System.out.println("[CLI] No manage controller wired - cannot accept proposal"); return; }
        boolean ok = manageController.acceptProposal(id);
        java.util.logging.Logger.getLogger(CliManageTradeView.class.getName()).info(() -> "onAcceptTradeProposal called for id=" + id + " result=" + ok);
        System.out.println(ok ? "\n✓ Proposta accettata: " + id : "\n✗ Fallito nell'accettare la proposta: " + id);
        System.out.print("Premi INVIO per continuare...");
        try { new java.util.Scanner(System.in).nextLine(); } catch (Exception ignored) {}
        // refresh view after accept
        manageController.loadAndDisplayTrades(this);
    }

    public void onCancelTradeProposal(String id) {
        // The controller currently has no explicit 'cancel' operation; map cancel to decline
        if (id == null) return;
        if (onCancelCallback != null) { onCancelCallback.accept(id); return; }
        if (manageController == null) { System.out.println("[CLI] No manage controller wired - cannot cancel proposal"); return; }
        System.out.println("[CLI] Canceling proposal (mapped to decline): " + id);
        boolean ok = manageController.declineProposal(id);
        System.out.println(ok ? "[CLI] Proposal canceled/rejected: " + id : "[CLI] Failed to cancel proposal: " + id);
    }

    public void onDeclineTradeProposal(String id) {
        if (id == null) return;
        if (onDeclineCallback != null) { onDeclineCallback.accept(id); return; }
        if (manageController == null) { System.out.println("[CLI] No manage controller wired - cannot decline proposal"); return; }
        boolean ok = manageController.declineProposal(id);
        java.util.logging.Logger.getLogger(CliManageTradeView.class.getName()).info(() -> "onDeclineTradeProposal called for id=" + id + " result=" + ok);
        System.out.println(ok ? "\n✓ Proposta rifiutata: " + id : "\n✗ Fallito nel rifiutare la proposta: " + id);
        System.out.print("Premi INVIO per continuare...");
        try { new java.util.Scanner(System.in).nextLine(); } catch (Exception ignored) {}
        // refresh view after decline
        manageController.loadAndDisplayTrades(this);
    }

    public void onTradeClick(String id) {
        if (id == null) return;
        if (onTradeClickCallback != null) { onTradeClickCallback.accept(id); return; }
        if (manageController == null) { System.out.println("[CLI] No manage controller wired - cannot initiate trade"); return; }
        boolean ok = manageController.initiateTrade(id);
        System.out.println(ok ? "[CLI] Started trade flow for proposal: " + id : "[CLI] Failed to start trade flow for proposal: " + id);
    }

    public void onTradeNowClick(String id) {
        if (id == null) return;
        if (onTradeNowClickCallback != null) { onTradeNowClickCallback.accept(id); return; }
        onTradeClick(id);
    }

    private String formatPending(ProposalBean t) {
        if (t == null) return "<invalid>";
        String other = null;
        boolean incoming = false;
        // Determine other/incoming using from/to fields
        if (currentUsername != null) {
            incoming = currentUsername.equals(t.getToUser());
            other = currentUsername.equals(t.getFromUser()) ? t.getToUser() : t.getFromUser();
        }
        String arrow = incoming ? "<-" : "->";
        if (other == null) return formatTrade(t);
        return String.format("%s %s [%s]", arrow, other, t.getStatus());
    }

    private String formatTrade(ProposalBean t) {
        String participants = (t.getFromUser() == null ? "" : t.getFromUser()) + " vs " + (t.getToUser() == null ? "" : t.getToUser());
        String status = t.getStatus() != null ? t.getStatus() : "";
        return String.format("%s - %s %s", t.getProposalId(), participants, status);
    }

    public void setUsername(String username) {
        this.currentUsername = username;
    }
}
