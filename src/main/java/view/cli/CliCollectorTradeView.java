package view.cli;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import controller.TradeController;
import model.bean.TradeSessionBean;
import view.ICollectorTradeView;

/**
 * Implementazione CLI per la gestione degli scambi dal punto di vista del
 * Collezionista.
 * Questa classe gestisce la visualizzazione delle liste degli scambi e la
 * procedura
 * di conferma presenza presso lo store fisico. È essenziale per fornire
 * un'interfaccia
 * testuale coerente con le funzionalità di monitoraggio delle transazioni.
 */
public class CliCollectorTradeView implements ICollectorTradeView {

    private TradeController controller;
    private final Scanner scanner = new Scanner(System.in);
    private boolean isRunning = false;
    private TradeSessionBean currentSession;

    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";

    /**
     * Associa il controller alla vista.
     * Utilizzato per stabilire il legame tra la vista e la logica di business
     * (Pattern MVC).
     * 
     * @param controller Il controller da associare.
     */
    @Override
    public void setController(Object controller) {
        this.controller = (TradeController) controller;
    }

    /**
     * Avvia il ciclo principale di visualizzazione della vista CLI.
     * Gestisce l'interazione continua con l'utente finché non viene richiesta
     * l'uscita.
     * Utility: fornisce il punto di ingresso per navigare tra gli scambi del
     * collezionista.
     * Delega la visualizzazione del menu a displayMenu() e la gestione dell'input a
     * handleMenuChoice().
     */
    @Override
    public void display() {
        isRunning = true;
        while (isRunning) {
            // Logica di auto-refresh: ricarica sempre gli scambi all'inizio del ciclo
            if (controller != null) {
                controller.loadTrades();
            }
            displayMenu();
            String input = scanner.nextLine().trim();
            handleMenuChoice(input);
        }
    }

    /**
     * Stampa a video il menu principale degli scambi.
     * Funzionalità: mostra le opzioni di navigazione e l'elenco riassuntivo.
     * Utility: permette all'utente di scegliere tra tornare indietro o visualizzare
     * i dettagli di uno scambio.
     */
    private void displayMenu() {
        System.out.println("\n=== I MIEI SCAMBI ===");
        // Opzione 1 rimossa perché l'auto-aggiornamento è attivo
        System.out.println("0) Indietro");

        System.out.print("Scelta (0, o ID Scambio per dettagli): ");
    }

    /**
     * Gestisce la scelta effettuata dall'utente nel menu principale.
     * Funzionalità: indirizza il flusso basandosi sull'input testuale.
     * Utility: centralizza la logica di navigazione.
     * Delega l'apertura dei dettagli a tryOpenTradeDetails().
     * 
     * @param input La stringa inserita dall'utente.
     */
    private void handleMenuChoice(String input) {
        if ("0".equals(input)) {
            isRunning = false;
            if (controller != null) {
                controller.goToStoreHomepage();
            }
        } else if ("1".equals(input)) {
            // Supporto legacy se l'utente digita 1, ricarica semplicemente
            System.out.println("Lista aggiornata.");
        } else {
            tryOpenTradeDetails(input);
        }
    }

    /**
     * Tenta di aprire la visualizzazione dettagliata di uno scambio specifico.
     * Funzionalità: valida l'input numerico e richiede i dettagli al controller.
     * 
     * @param input L'ID dello scambio inserito come stringa.
     */
    private void tryOpenTradeDetails(String input) {
        try {
            int tradeId = Integer.parseInt(input);
            if (controller != null) {
                controller.openTradeDetails(tradeId);
            }
        } catch (NumberFormatException _) {
            System.out.println("Input non valido.");
        }
    }

    /**
     * Visualizza graficamente (in formato testuale) le liste degli scambi attivi e
     * completati.
     * Funzionalità: formatta i dati dei bean in una tabella per facilitare la
     * lettura.
     * Utility: offre una panoramica immediata dello stato di tutte le transazioni
     * dell'utente.
     * Delega la risoluzione del nome dell'altro utente al metodo helper
     * resolveOtherUser().
     * 
     * @param activeTrades    Elenco degli scambi attualmente in corso.
     * @param completedTrades Elenco degli scambi conclusi o annullati.
     */
    @Override
    public void showTradeLists(List<TradeSessionBean> activeTrades, List<TradeSessionBean> completedTrades) {
        System.out.println("\n--- SCAMBI ATTIVI ---");
        if (activeTrades.isEmpty()) {
            System.out.println("   (Nessuno)");
        } else {
            System.out.printf("%-6s | %-15s | %-16s | %s%n", "ID", "Partner", "Data", "Stato");
            System.out.println("---------------------------------------------------------------");
            for (TradeSessionBean t : activeTrades) {
                String other = resolveOtherUser(t);
                System.out.printf("[%4d] | %-15s | %-16s | %s%n",
                        t.getTransactionId(),
                        other,
                        t.getTradeDate().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                        t.getStatus());
            }
        }

        System.out.println("\n--- SCAMBI COMPLETATI ---");
        if (completedTrades.isEmpty()) {
            System.out.println("   (Nessuno)");
        } else {
            System.out.printf("%-6s | %-15s | %-16s%n", "ID", "Partner", "Data");
            System.out.println("------------------------------------------------");
            for (TradeSessionBean t : completedTrades) {
                String other = resolveOtherUser(t);
                System.out.printf("[%4d] | %-15s | %-16s%n",
                        t.getTransactionId(),
                        other,
                        t.getTradeDate().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
            }
        }
    }

    /**
     * Determina lo username della controparte dello scambio.
     * Funzionalità: confronta l'utente in sessione con i partecipanti del bean.
     * Utility: permette di mostrare sempre chi è il "partner" dell'utente corrente.
     * 
     * @param t Il bean della sessione di scambio.
     * @return Lo username dell'altro partecipante.
     */
    private String resolveOtherUser(TradeSessionBean t) {
        if (controller == null || controller.getSessionUser() == null)
            return "N/D";
        String me = controller.getSessionUser().getUsername();
        return t.getProposerId().equals(me) ? t.getReceiverId() : t.getProposerId();
    }

    /**
     * Mostra le informazioni dettagliate di una singola sessione di scambio.
     * Funzionalità: stampa stato, partner e data dello scambio. Se presente, mostra
     * il codice di verifica.
     * Utility: permette all'utente di verificare i dettagli puntuali e procedere
     * con lo scambio fisico.
     * Delega la logica di generazione/richiesta del codice al metodo helper
     * handleCodeGeneration().
     * 
     * @param sessionBean Il bean con i dati dello scambio.
     * @param userCode    Il codice sessione dell'utente (opzionale).
     * @param partnerName Il nome del partner di scambio.
     */
    @Override
    public void showTradeDetails(TradeSessionBean sessionBean, String userCode, String partnerName) {
        // Memorizza bean corrente per operazioni successive
        this.currentSession = sessionBean;

        System.out.println("\n========================================");
        System.out.println("        DETTAGLI SCAMBIO #" + sessionBean.getTransactionId());
        System.out.println("========================================");
        System.out.printf(" Partner: %s%n", partnerName);
        System.out.printf(" Stato:   %s%n", sessionBean.getStatus());
        System.out.printf(" Data:    %s%n",
                sessionBean.getTradeDate().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        System.out.println("----------------------------------------");

        if (userCode != null) {
            System.out.println("\n****************************************");
            System.out.println("   IL TUO CODICE DI SCAMBIO: [" + userCode + "]");
            System.out.println("****************************************");
            System.out.println(" -> Comunica questo codice allo store quando arrivi.");
            System.out.println("\nPremi Invio per tornare...");
            scanner.nextLine();
        } else if (handleCodeGeneration()) {
            // Codice generato correttamente
        }
    }

    /**
     * Gestisce la generazione del codice di sessione quando l'utente conferma di
     * essere arrivato allo store.
     * 
     * @return true se il codice è stato richiesto correttamente.
     */
    private boolean handleCodeGeneration() {
        System.out.println("Codice non ancora generato.");
        System.out.println("Opzioni:");
        System.out.println("1) Genera Codice (Sono arrivato allo store)");
        System.out.println("0) Indietro");

        String choice = scanner.nextLine().trim();
        if ("1".equals(choice)) {
            if (controller != null && currentSession != null) {
                controller.retrieveSessionCodeById(currentSession.getTransactionId());
                // Il controller chiamerà registerConfirmPresence() se successo.
            }
            return true;
        }
        return false;
    }

    /**
     * Segnala alla visualizzazione di chiudersi.
     * Funzionalità: interrompe il ciclo principale della CLI.
     */
    @Override
    public void close() {
        isRunning = false;
    }

    /**
     * Richiede un aggiornamento dei dati al controller.
     * Funzionalità: invoca il ricaricamento degli scambi per mantenere la vista
     * sincronizzata.
     */
    @Override
    public void refresh() {
        if (controller != null) {
            controller.loadTrades();
        }
    }

    /**
     * Conferma la presenza fisica allo store e richiede la generazione del codice.
     * Funzionalità: invoca la logica di persistenza tramite il controller e mostra
     * il risultato.
     * Utility: passo fondamentale per l'avanzamento della transazione in loco.
     */
    @Override
    public void registerConfirmPresence() {
        // Mostra la conferma visiva recuperando il codice sessione (Safe Getter)
        if (currentSession != null && controller != null) {
            int code = controller.getUserSessionCode(currentSession.getTransactionId());
            if (code > 0) {
                System.out.println("\n*** PRESENZA CONFERMATA ***");
                System.out.println("IL TUO CODICE: [" + code + "]");
                System.out.println("Comunica questo codice allo store quando arrivi.");
                System.out.println("******************************\n");
            }
        }
    }

    /**
     * Mostra il codice di sessione già precedentemente generato per la sessione
     * corrente.
     * Funzionalità: recupera e visualizza il codice dal sistema.
     * Utility: utile se l'utente smarrisce il codice o ha bisogno di visualizzarlo
     * nuovamente.
     */
    @Override
    public void showSessionCode() {
        // Delega al controller il recupero del codice sessione
        if (currentSession != null && controller != null) {
            int code = controller.getUserSessionCode(currentSession.getTransactionId());

            if (code > 0) {
                System.out.println("\n*** CODICE SESSIONE ***");
                System.out.println("IL TUO CODICE: [" + code + "]");
                System.out.println("***********************\n");
            } else {
                System.out.println("Nessun codice sessione ancora generato.");
            }
        } else {
            System.out.println("Nessuna sessione attiva.");
        }
    }

    /**
     * Visualizza un messaggio di errore all'utente.
     * 
     * @param errorMessage Il testo dell'errore da mostrare.
     */
    @Override
    public void showError(String errorMessage) {
        System.out.println("ERRORE: " + errorMessage);
    }
}
