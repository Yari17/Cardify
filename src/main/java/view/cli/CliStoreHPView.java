package view.cli;

import java.util.List;
import view.IStoreHPView;
import controller.StoreHPController;

/**
 * Implementazione CLI della Dashboard per i negozi (Store).
 * Monitora gli scambi in corso, programmati e conclusi attraverso menu
 * testuali.
 */
public class CliStoreHPView implements IStoreHPView {

    private StoreHPController controller;
    private java.util.Scanner scanner;
    private String storeName;
    private List<model.bean.TradeSessionBean> ongoingTrades;
    private List<model.bean.TradeSessionBean> scheduledTrades;
    private List<model.bean.TradeSessionBean> historyTrades;
    private boolean running = true;

    public CliStoreHPView() {
        this.scanner = new java.util.Scanner(System.in);
    }

    @Override
    public void display() {
        if (controller != null) {
            controller.loadStoreData();
        }

        while (running) {
            printMenu();
            String choice = scanner.nextLine();
            handleChoice(choice);
        }
    }

    /**
     * Stampa a video il menu della dashboard dello store.
     */
    private void printMenu() {
        System.out.println("\n=== DASHBOARD STORE (" + (storeName != null ? storeName : "N/D") + ") ===");
        System.out
                .println("1) Visualizza Scambi in Corso (" + (ongoingTrades != null ? ongoingTrades.size() : 0) + ")");
        System.out.println(
                "2) Visualizza Scambi Programmati (" + (scheduledTrades != null ? scheduledTrades.size() : 0) + ")");
        System.out.println("3) Visualizza Storico Scambi (" + (historyTrades != null ? historyTrades.size() : 0) + ")");
        System.out.println("4) Aggiorna dati");
        System.out.println("0) Logout");
        System.out.print("Scelta: ");
    }

    /**
     * Gestisce la scelta dell'utente nel menu principale.
     * 
     * @param choice L'opzione selezionata.
     */
    private void handleChoice(String choice) {
        switch (choice) {
            case "1":
                showTradeList("IN CORSO", ongoingTrades, true);
                break;
            case "2":
                showTradeList("PROGRAMMATI", scheduledTrades, true);
                break;
            case "3":
                showTradeList("CONCLUSI", historyTrades, false);
                break;
            case "4":
                refresh();
                break;
            case "0":
                running = false;
                if (controller != null)
                    controller.logout();
                break;
            default:
                System.out.println("Scelta non valida.");
        }
    }

    /**
     * Mostra una lista di scambi filtrata per tipologia.
     * 
     * @param title       Il titolo della sezione.
     * @param trades      La lista degli scambi da mostrare.
     * @param interactive Se true, permette di selezionare uno scambio per i
     *                    dettagli.
     */
    private void showTradeList(String title, List<model.bean.TradeSessionBean> trades, boolean interactive) {
        System.out.println("\n--- " + title + " ---");
        if (trades == null || trades.isEmpty()) {
            System.out.println("Nessuno scambio presente.");
            return;
        }

        for (int i = 0; i < trades.size(); i++) {
            model.bean.TradeSessionBean t = trades.get(i);
            System.out.printf("%d) ID: %d | Data: %s | %s <-> %s%n",
                    (i + 1), t.getTransactionId(), t.getTradeDate(), t.getProposerId(), t.getReceiverId());
        }

        if (interactive) {
            System.out.println("\nSeleziona il numero dello scambio per i dettagli (o 0 per tornare indietro):");
            try {
                String input = scanner.nextLine();
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < trades.size() && controller != null) {
                    running = false; // esce dal ciclo per navigare
                    controller.openTradeDetails(trades.get(idx));
                }
            } catch (NumberFormatException _) {
                // ignora
            }
        }
    }

    @Override
    public void close() {
        running = false;
    }

    @Override
    public void refresh() {
        if (controller != null)
            controller.loadStoreData();
        System.out.println("Dati aggiornati.");
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("ERRORE: " + errorMessage);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (StoreHPController) controller;
    }

    @Override
    public void setStoreName(String name) {
        this.storeName = name;
    }

    @Override
    public void showOngoingTrades(List<model.bean.TradeSessionBean> trades) {
        this.ongoingTrades = trades;
    }

    @Override
    public void showScheduledTrades(List<model.bean.TradeSessionBean> trades) {
        this.scheduledTrades = trades;
    }

    @Override
    public void showHistoryTrades(List<model.bean.TradeSessionBean> trades) {
        this.historyTrades = trades;
    }
}
