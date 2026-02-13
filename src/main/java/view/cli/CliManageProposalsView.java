package view.cli;

import controller.ManageProposalsController;
import model.bean.ProposalBean;
import model.bean.CardBean;
import view.IManageProposalsView;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Implementazione CLI per la gestione delle proposte di scambio.
 * Questa classe gestisce l'interfaccia a riga di comando per visualizzare e
 * interagire
 * con le proposte ricevute e inviate, utilizzando un modello PUSH con cache
 * locale.
 */
public class CliManageProposalsView implements IManageProposalsView {
    private ManageProposalsController controller;
    private final Scanner sc = new Scanner(System.in);

    // Cache locale dei dati (PUSHed dal controller)
    private List<ProposalBean> receivedProposals = Collections.emptyList();
    private List<ProposalBean> sentProposals = Collections.emptyList();
    private List<ProposalBean> completedProposals = Collections.emptyList();

    @Override
    public void setController(Object controller) {
        this.controller = (ManageProposalsController) controller;
    }

    /**
     * Avvia il ciclo principale di interazione CLI per la gestione delle proposte.
     * Funzionalità: mostra il menu principale e gestisce l'input dell'utente per
     * navigare nelle sottosezioni.
     * Utility: fornisce il punto di accesso testuale per tutte le operazioni sulle
     * proposte.
     * Delega la visualizzazione delle sottosezioni ai metodi helper
     * showReceivedMenu(), showSentMenu() e showHistoryMenu().
     */
    @Override
    public void display() {
        boolean exit = false;
        // Al setView il controller ha già caricato i dati, quindi le cache sono pronte.

        while (!exit) {
            System.out.println("\n=== GESTIONE PROPOSTE ===");
            System.out.println("1) Proposte Ricevute [" + receivedProposals.size() + "]");
            System.out.println("2) Proposte Inviate [" + sentProposals.size() + "]");
            System.out.println("3) Storico Proposte [" + completedProposals.size() + "]");
            System.out.println("0) Indietro");
            System.out.print("Scegli: ");

            String input = sc.nextLine().trim();
            switch (input) {
                case "1" -> showReceivedMenu();
                case "2" -> showSentMenu();
                case "3" -> showHistoryMenu();
                case "0" -> {
                    exit = true;
                    if (controller != null)
                        controller.goToHomepage();
                }
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    // --- Metodi Interfaccia (PUSH Updates) ---

    /**
     * Aggiorna la cache locale delle proposte ricevute in attesa.
     * 
     * @param proposals La lista di bean delle proposte ricevute.
     */
    @Override
    public void showReceivedPendingProposals(List<ProposalBean> proposals) {
        this.receivedProposals = proposals != null ? proposals : Collections.emptyList();
    }

    /**
     * Aggiorna la cache locale delle proposte inviate in attesa.
     * 
     * @param proposals La lista di bean delle proposte inviate.
     */
    @Override
    public void showSentPendingProposals(List<ProposalBean> proposals) {
        this.sentProposals = proposals != null ? proposals : Collections.emptyList();
    }

    /**
     * Aggiorna la cache locale delle proposte completate.
     * 
     * @param proposals La lista di bean delle proposte storiche.
     */
    @Override
    public void showCompletedProposals(List<ProposalBean> proposals) {
        this.completedProposals = proposals != null ? proposals : Collections.emptyList();
    }

    /**
     * Richiede un aggiornamento dei dati (non necessario in questa implementazione
     * CLI).
     */
    @Override
    public void refresh() {
        // non necessario in cli
    }

    /**
     * Mostra il menu per la gestione delle proposte ricevute.
     * Permette all'utente di selezionare una proposta per accettarla o rifiutarla.
     */
    private void showReceivedMenu() {
        printList("PROPOSTE RICEVUTE", receivedProposals);

        if (receivedProposals.isEmpty())
            return;

        System.out.print("Inserisci il numero della proposta da gestire (o invio per tornare): ");
        String input = sc.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < receivedProposals.size()) {
                    handleProposalAction(receivedProposals.get(index), index);
                } else {
                    System.out.println("Numero non valido.");
                }
            } catch (NumberFormatException _) {
                System.out.println("Input non valido. Inserisci un numero.");
            }
        }
    }

    /**
     * Gestisce l'azione su una specifica proposta selezionata.
     * Mostra le opzioni Accetta/Rifiuta/Annulla.
     * 
     * @param selected La proposta selezionata.
     * @param index    L'indice della proposta nella lista.
     */
    private void handleProposalAction(ProposalBean selected, int index) {
        String id = selected.getProposalId();
        System.out.println("Hai selezionato la proposta " + (index + 1) + " (ID: " + id + ")");
        System.out.println("1) Accetta");
        System.out.println("2) Rifiuta");
        System.out.println("0) Annulla");
        System.out.print("Scegli: ");

        String action = sc.nextLine().trim();
        if (controller == null)
            return;

        if ("1".equals(action)) {
            controller.acceptProposal(id);
        } else if ("2".equals(action)) {
            controller.rejectProposal(id);
        }
    }

    /**
     * Mostra la lista delle proposte inviate dall'utente.
     */
    private void showSentMenu() {
        printList("PROPOSTE INVIATE", sentProposals);
        if (!sentProposals.isEmpty()) {
            System.out.println("Premi Invio per tornare...");
            sc.nextLine();
        }
    }

    /**
     * Mostra lo storico delle proposte completate (accettate, rifiutate,
     * annullate).
     */
    private void showHistoryMenu() {
        printList("STORICO PROPOSTE", completedProposals);
        if (!completedProposals.isEmpty()) {
            System.out.println("Premi Invio per tornare...");
            sc.nextLine();
        }
    }

    /**
     * Stampa a video una lista di proposte con i relativi dettagli.
     * 
     * @param title     Il titolo della sezione.
     * @param proposals La lista di proposte da stampare.
     */
    private void printList(String title, List<ProposalBean> proposals) {
        System.out.println("\n--- " + title + " ---");
        if (proposals == null || proposals.isEmpty()) {
            System.out.println("Nessuna proposta in questa lista.");
            return;
        }
        for (int i = 0; i < proposals.size(); i++) {
            ProposalBean p = proposals.get(i);
            System.out.println((i + 1) + ") ID: " + p.getProposalId());
            System.out.println("   Da: " + p.getFromUser() + " -> A: " + p.getToUser());
            System.out.println("   Stato: " + p.getStatus());
            System.out.println("   Items Offerti:");
            if (p.getOffered() != null) {
                for (CardBean c : p.getOffered()) {
                    System.out.println("     - " + c.getName() + " (x" + c.getQuantity() + ")");
                }
            }
            System.out.println("   Items Richiesti:");
            if (p.getRequested() != null) {
                for (CardBean c : p.getRequested()) {
                    System.out.println("     - " + c.getName() + " (x" + c.getQuantity() + ")");
                }
            }
            System.out.println(
                    "   Luogo: " + p.getMeetingPlace() + " | Data: " + p.getMeetingDate() + " " + p.getMeetingTime());
            System.out.println("------------------------------------------------");
        }
    }

    /**
     * Visualizza un messaggio di conferma per l'accettazione di una proposta.
     * Funzionalità: stampa l'esito positivo dell'azione e procede con la
     * navigazione.
     * Utility: fornisce feedback testuale immediato al collezionista.
     * 
     * @param onNavigate Callback per gestire la navigazione post-dialogo.
     */
    @Override
    public void showProposalAcceptedDialog(Runnable onNavigate) {
        System.out.println(">> PROPOSTA ACCETTATA CON SUCCESSO! <<");
        if (onNavigate != null) {
            System.out.println("Reindirizzamento...");
            onNavigate.run();
        }
    }

    /**
     * Chiude la visualizzazione (non necessario in cli).
     */
    @Override
    public void close() {
        // Nessuna operazione
    }

    /**
     * Visualizza un messaggio di errore nel terminale.
     * 
     * @param errorMessage Il messaggio di errore da mostrare.
     */
    @Override
    public void showError(String errorMessage) {
        System.out.println("ERRORE: " + errorMessage);
    }
}
