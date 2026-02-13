package view.cli;

import model.bean.TradeSessionBean;
import view.IStoreTradeView;
import controller.TradeController;
import java.util.Scanner;

public class CliStoreTradeView implements IStoreTradeView {

    private TradeController controller;
    private TradeSessionBean currentSession;
    private Scanner scanner;
    private boolean running = false;
    private String partnerName;

    /**
     * Costruttore della vista CLI per la gestione scambi dello store.
     * Inizializza lo scanner per l'input utente.
     */
    public CliStoreTradeView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (TradeController) controller;
    }

    @Override
    public void showTradeDetails(TradeSessionBean sessionBean, String userCode, String partnerName) {
        this.currentSession = sessionBean;
        this.partnerName = partnerName;
    }

    @Override
    public void display() {
        running = true;
        while (running) {
            printSessionDetails();
            printMenu();
            String input = scanner.nextLine();
            handleInput(input);
        }
    }

    /**
     * Stampa i dettagli della sessione di scambio corrente.
     * Mostra ID, partecipanti, stato e liste delle carte.
     */
    private void printSessionDetails() {
        if (currentSession == null) {
            System.out.println("Nessuna sessione di scambio attiva.");
            return;
        }

        System.out.println("\n=== DETTAGLI SCAMBIO ===");
        System.out.println("ID Scambio: " + currentSession.getTransactionId());
        System.out.println("Partner: " + (partnerName != null ? partnerName : "N/D"));
        System.out.println("Status: " + currentSession.getStatus());
        System.out.println("Data: " + currentSession.getTradeDate());

        System.out.println("\n--- Partecipanti ---");
        boolean pArr = currentSession.isProposerArrived();
        boolean rArr = currentSession.isReceiverArrived();
        System.out.printf("Proponente (%s): %s%n", currentSession.getProposerId(), pArr ? "ARRIVATO" : "NON ARRIVATO");
        System.out.printf("Ricevente (%s): %s%n", currentSession.getReceiverId(), rArr ? "ARRIVATO" : "NON ARRIVATO");

        System.out.println("\n--- Carte Offerte ---");
        currentSession.getOffered().forEach(c -> System.out.println("- " + c.getName()));

        System.out.println("\n--- Carte Richieste ---");
        currentSession.getRequested().forEach(c -> System.out.println("- " + c.getName()));
    }

    /**
     * Stampa il menu delle azioni disponibili.
     * Le opzioni sono filtrate in base allo stato dello scambio.
     */
    private void printMenu() {
        System.out.println("\n--- AZIONI ---");
        String status = currentSession != null ? currentSession.getStatus() : null;

        boolean canVerify = "WAITING_FOR_ARRIVAL".equals(status) || "PARTIALLY_ARRIVED".equals(status);
        boolean canInspect = "BOTH_ARRIVED".equals(status);
        boolean inInspection = "INSPECTION_PHASE".equals(status);
        boolean canCancel = !"COMPLETED".equals(status) && !"CANCELLED".equals(status)
                && !"EXPIRED".equals(status);

        if (canVerify)
            System.out.println("1) Verifica Codice Utente");
        if (canInspect)
            System.out.println("2) Avvia Ispezione");
        if (inInspection) {
            System.out.println("3) Ispezione OK (Passa)");
            System.out.println("4) Ispezione FALLITA");
        }
        if (canCancel)
            System.out.println("9) Annulla Scambio");
        System.out.println("0) Indietro / Esci");
        System.out.print("Scelta: ");
    }

    /**
     * Gestisce l'input utente dal menu principale.
     * 
     * @param input L'opzione selezionata.
     */
    private void handleInput(String input) {
        if (currentSession == null) {
            if (input.equals("0")) {
                running = false;
                if (controller != null)
                    controller.goToStoreHomepage();
            }
            return;
        }

        switch (input) {
            case "1":
                handleVerifyCode();
                break;
            case "2":
                handleStartInspection();
                break;
            case "3":
                handlePassInspection();
                break;
            case "4":
                handleFailInspection();
                break;
            case "9":
                handleCancelTrade();
                break;
            case "0":
                handleBack();
                break;
            default:
                System.out.println("Comando non riconosciuto.");
        }
    }

    /**
     * Gestisce l'inserimento e la verifica del codice di un partecipante.
     * Comunica con il controller per validare l'arrivo dell'utente allo store.
     */
    private void handleVerifyCode() {
        System.out.print("Inserisci codice utente: ");
        String codeStr = scanner.nextLine();
        try {
            int code = Integer.parseInt(codeStr);
            if (controller != null)
                controller.verifySessionCode(code, currentSession.getTransactionId());
        } catch (NumberFormatException _) {
            System.out.println("Formato codice non valido.");
        }
    }

    /**
     * Avvia la fase di ispezione fisica delle carte.
     */
    private void handleStartInspection() {
        if (controller != null)
            controller.startInspection(currentSession.getTransactionId());
    }

    /**
     * Segnala il superamento dell'ispezione.
     */
    private void handlePassInspection() {
        if (controller != null)
            controller.passInspection(currentSession.getTransactionId());
    }

    /**
     * Segnala il fallimento dell'ispezione.
     */
    private void handleFailInspection() {
        if (controller != null)
            controller.failInspection(currentSession.getTransactionId());
    }

    /**
     * Annulla lo scambio corrente.
     */
    private void handleCancelTrade() {
        if (controller != null)
            controller.cancelTrade(currentSession.getTransactionId());
    }

    /**
     * Torna al menu precedente.
     */
    private void handleBack() {
        running = false;
        if (controller != null)
            controller.goToStoreHomepage();
    }

    @Override
    public void close() {
        running = false;
    }

    @Override
    public void refresh() {
        // Nella GUI questo aggiorna la vista.
        // Nel ciclo CLI, la prossima iterazione ristamperà i dettagli.
        System.out.println(">> Stato aggiornato. <<");
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("!!! ERRORE: " + errorMessage + " !!!");
    }

    @Override
    public void registerCodeValidation(int code) {
        // View CLI delega verificazione al controller tramite handleVerifyCode()
        System.out.println("Codice registrato: " + code);
    }

    @Override
    public void registerInspectionSuccess() {
        System.out.println("✓ Ispezione completata con successo!");
    }

    @Override
    public void registerInspectionFail() {
        System.out.println("✗ Ispezione fallita!");
    }

    @Override
    public void onFinalizeTrade() {
        System.out.println("Scambio finalizzato!");
    }
}
