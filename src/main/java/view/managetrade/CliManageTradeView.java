package view.managetrade;

import controller.ManageTradeController;
import model.bean.ProposalBean;

import java.util.List;

public class CliManageTradeView implements IManageTradeView{
    private String currentUsername;
    private ManageTradeController manageController;

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
        System.out.println("--- Scambi In Attesa ---");
        if (pending == null || pending.isEmpty()) {
            System.out.println("(nessuna proposta in attesa)");
        } else {
            for (ProposalBean t : pending) {
                System.out.println(formatPending(t));
            }
        }

        System.out.println("\n--- Scambi Programmati ---");
        if (scheduled == null || scheduled.isEmpty()) {
            System.out.println("(nessuno scambio programmato)");
        } else {
            for (ProposalBean t : scheduled) {
                System.out.println(formatTrade(t));
            }
        }
    }

    @Override
    public void setManageController(ManageTradeController controller) {
        this.manageController = controller;
    }

    @Override
    public void onAcceptTradeProposal(String id) {
        if (manageController == null) {
            System.out.println("[CLI] No manage controller wired - cannot accept proposal");
            return;
        }
        boolean ok = manageController.acceptProposal(id);
        System.out.println(ok ? "[CLI] Proposal accepted: " + id : "[CLI] Failed to accept proposal: " + id);
    }

    @Override
    public void onCancelTradeProposal(String id) {
        // The controller currently has no explicit 'cancel' operation; map cancel to decline
        if (manageController == null) {
            System.out.println("[CLI] No manage controller wired - cannot cancel proposal");
            return;
        }
        System.out.println("[CLI] Canceling proposal (mapped to decline): " + id);
        boolean ok = manageController.declineProposal(id);
        System.out.println(ok ? "[CLI] Proposal canceled/rejected: " + id : "[CLI] Failed to cancel proposal: " + id);
    }

    @Override
    public void onDeclineTradeProposal(String id) {
        if (manageController == null) {
            System.out.println("[CLI] No manage controller wired - cannot decline proposal");
            return;
        }
        boolean ok = manageController.declineProposal(id);
        System.out.println(ok ? "[CLI] Proposal declined: " + id : "[CLI] Failed to decline proposal: " + id);
    }

    @Override
    public void onTradeClick(String id) {
        // In CLI we delegate to the manageController to initiate the trade flow; this keeps view/controller decoupled
        if (manageController == null) {
            System.out.println("[CLI] No manage controller wired - cannot initiate trade");
            return;
        }
        boolean ok = manageController.initiateTrade(id);
        System.out.println(ok ? "[CLI] Started trade flow for proposal: " + id : "[CLI] Failed to start trade flow for proposal: " + id);
    }

    @Override
    public void onTradeNowClick(String id) {
        // For testing - same as onTradeClick
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
