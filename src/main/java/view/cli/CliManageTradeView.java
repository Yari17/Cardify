package view.cli;

import controller.ManageTradeController;
import model.bean.ProposalBean;
import view.IManageTradeView;

import java.util.List;

public class CliManageTradeView implements IManageTradeView {
    private String currentUsername;
    private ManageTradeController manageController;
    
    private java.util.function.Consumer<String> onAcceptCallback;
    private java.util.function.Consumer<String> onDeclineCallback;
    private java.util.function.Consumer<String> onCancelCallback;
    private java.util.List<model.bean.ProposalBean> lastPending = new java.util.ArrayList<>();

    @Override
    public void display() {
        java.util.Scanner scanner = new java.util.Scanner(System.in); 

        
        if (manageController != null) {
            manageController.loadAndDisplayTrades(this);
        }

        while (true) {
            renderPendingList();

            System.out.println();
            System.out.println("0) Torna all'homepage");
            System.out.print("Inserisci numero di proposta da visualizzare: ");

            String line = readLine(scanner);
            if (line == null) return; 
            line = line.trim();

            if ("0".equals(line)) {
                if (manageController != null) manageController.navigateToHome();
                return;
            }

            ProposalBean sel = selectProposalFromInput(line);
            if (sel == null) {
                
                continue;
            }

            showProposalDetails(sel, scanner);

            
            if (manageController != null) manageController.loadAndDisplayTrades(this);
        }
    }

    
    private void renderPendingList() {
        System.out.println("=== PROPOSTE IN ATTESA ===");
        if (lastPending == null || lastPending.isEmpty()) {
            System.out.println("(nessuna proposta in attesa)");
            return;
        }
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

    private String readLine(java.util.Scanner scanner) {
        
        if (scanner == null) return null;
        if (!scanner.hasNextLine()) return null;
        return scanner.nextLine();
    }

    private ProposalBean selectProposalFromInput(String line) {
        if (line == null || line.isEmpty()) return null;
        int idx = parseIndex(line);
        if (idx < 0 || lastPending == null || idx >= lastPending.size()) {
            System.out.println("Numero proposta non valido.");
            return null;
        }
        return lastPending.get(idx);
    }

    private int parseIndex(String line) {
        if (line == null) return -1;
        
        if (!line.matches("\\d+")) {
            System.out.println("Input non valido. Inserisci 0 o il numero della proposta.");
            return -1;
        }
        
        if (line.length() > 10) {
            System.out.println("Input non valido. Inserisci 0 o il numero della proposta.");
            return -1;
        }
        int parsed = Integer.parseInt(line);
        return parsed - 1;
    }

    
    private void pauseForEnter() {
        java.util.Scanner sc = new java.util.Scanner(System.in);
        if (sc.hasNextLine()) sc.nextLine();
    }

    private void showProposalDetails(ProposalBean sel, java.util.Scanner scanner) {
        System.out.println();
        System.out.println("=== OVERVIEW PROPOSTA: " + sel.getProposalId() + " ===");
        System.out.println("Proposer: " + sel.getFromUser());
        System.out.println("Receiver: " + sel.getToUser());
        System.out.println();

        printOffered(sel);
        System.out.println();
        printRequested(sel);

        boolean isReceived = currentUsername != null && currentUsername.equals(sel.getToUser());
        if (isReceived) {
            handleReceivedChoice(sel, scanner);
        } else {
            
            System.out.println();
            System.out.println("0) Indietro");
            System.out.print("Premi INVIO per tornare...");
            pauseForEnter();
        }
    }

    private void printOffered(ProposalBean sel) {
        System.out.println("== CARTE OFFERTE DA " + sel.getFromUser() + " ==");
        if (sel.getOffered() == null || sel.getOffered().isEmpty()) {
            System.out.println("(nessuna carta)");
            return;
        }
        for (model.bean.CardBean cb : sel.getOffered()) {
            System.out.printf(" - %s x%d%n", cb.getName(), cb.getQuantity());
        }
    }

    private void printRequested(ProposalBean sel) {
        System.out.println("== CARTE RICHIESTE IN CAMBIO =");
        if (sel.getRequested() == null || sel.getRequested().isEmpty()) {
            System.out.println("(nessuna carta)");
            return;
        }
        for (model.bean.CardBean cb : sel.getRequested()) {
            System.out.printf(" - %s x%d%n", cb.getName(), cb.getQuantity());
        }
    }

    private void handleReceivedChoice(ProposalBean sel, java.util.Scanner scanner) {
        System.out.println();
        System.out.println("1) Accetta");
        System.out.println("2) Rifiuta");
        System.out.println("0) Indietro");
        System.out.print("Scelta: ");
        String choice2 = readLine(scanner);
        if (choice2 == null) return;
        choice2 = choice2.trim();
        if ("1".equals(choice2)) onAcceptTradeProposal(sel.getProposalId());
        else if ("2".equals(choice2)) onDeclineTradeProposal(sel.getProposalId());
        
    }

    @Override
    public void close() {
        /* not used */
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("[ERROR] " + errorMessage);
    }

    @Override
    public void displayTrades(List<ProposalBean> pending, List<ProposalBean> scheduled) {
        
        this.lastPending = pending != null ? new java.util.ArrayList<>(pending) : new java.util.ArrayList<>();
    }

    @Override
    public void refresh() {
        /* not used */
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
    public void registerOnTradeClick(java.util.function.Consumer<String> onTradeClick) {
        //no - op
    }

    @Override
    public void registerOnTradeNowClick(java.util.function.Consumer<String> onTradeNowClick) {
        // no - op
    }

    public void onAcceptTradeProposal(String id) {
        if (id == null) return;
        if (onAcceptCallback != null) { onAcceptCallback.accept(id); return; }
        if (manageController == null) { System.out.println("[CLI] No manage controller wired - cannot accept proposal"); return; }
        boolean ok = manageController.acceptProposal(id);
        java.util.logging.Logger.getLogger(CliManageTradeView.class.getName()).info(() -> "onAcceptTradeProposal called for id=" + id + " result=" + ok);
        System.out.println(ok ? "\n✓ Proposta accettata: " + id : "\n✗ Fallito nell'accettare la proposta: " + id);
        System.out.print("Premi INVIO per continuare...");
        pauseForEnter();
        
        manageController.loadAndDisplayTrades(this);
    }

    @SuppressWarnings("unused")
    public void onCancelTradeProposal(String id) {
        
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
        pauseForEnter();
        
        manageController.loadAndDisplayTrades(this);
    }

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
    }

}
