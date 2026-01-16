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
        // CLI will call displayTrades explicitly when needed
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
        // Cache and use refresh entrypoint for consistent behavior with FX views
        this.lastPending = pending != null ? new java.util.ArrayList<>(pending) : new java.util.ArrayList<>();
        this.lastConcluded = scheduled != null ? new java.util.ArrayList<>(scheduled) : new java.util.ArrayList<>();
        refresh();
    }

    @Override
    public void refresh() {
        System.out.println("--- Scambi In Attesa ---");
        if (lastPending == null || lastPending.isEmpty()) {
            System.out.println("(nessuna proposta in attesa)");
        } else {
            for (ProposalBean t : lastPending) System.out.println(formatPending(t));
        }

        System.out.println("\n--- Scambi Programmati ---");
        if (lastConcluded == null || lastConcluded.isEmpty()) {
            System.out.println("(nessuno scambio programmato)");
        } else {
            for (ProposalBean t : lastConcluded) System.out.println(formatTrade(t));
        }
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
        System.out.println(ok ? "[CLI] Proposal accepted: " + id : "[CLI] Failed to accept proposal: " + id);
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
        System.out.println(ok ? "[CLI] Proposal declined: " + id : "[CLI] Failed to decline proposal: " + id);
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
