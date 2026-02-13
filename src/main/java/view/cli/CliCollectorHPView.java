package view.cli;

import controller.CollectorHPController;
import model.bean.CardBean;
import view.ICollectorHPView;

import java.util.List;
import java.util.Scanner;

/**
 * Implementazione CLI della Home Page per i collezionisti.
 * Fornisce un'interfaccia a menu per navigare tra la ricerca carte, la
 * collezione e gli scambi.
 */
public class CliCollectorHPView implements ICollectorHPView {
    private CollectorHPController controller;
    private final Scanner sc = new Scanner(System.in);
    private String currentCardNameFilter = "";
    private String currentSetFilter = null;

    /**
     * Avvia il loop del menu principale della Home Page.
     * Gestisce la navigazione verso le diverse sezioni dell'applicazione.
     */
    @Override
    public void display() {
        boolean running = true;

        // Caricamento iniziale delle carte nella community
        refresh();

        while (running) {
            System.out.println("\n=== Collector Homepage ===");
            System.out.println("1) Cerca carte (community)");
            System.out.println("2) Collezione");
            System.out.println("3) Trade");
            System.out.println("4) Gestisci Proposte");
            System.out.println("5) Logout");
            System.out.print("Scegli: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> searchCards();
                case "2" -> {
                    if (controller != null)
                        controller.goToCollection();
                    running = false;
                }
                case "3" -> {
                    if (controller != null)
                        controller.goToTrade();
                    running = false;
                }
                case "4" -> {
                    if (controller != null)
                        controller.manageProposals();
                    running = false;
                }
                case "5" -> {
                    if (controller != null)
                        controller.logout();
                    running = false;
                }
                default -> System.out.println("Opzione non valida");
            }
        }
    }

    /**
     * Gestisce la procedura di ricerca delle carte nella community.
     * Permette di inserire filtri per nome e set prima di visualizzare i risultati.
     */
    private void searchCards() {
        System.out.print("Inserisci nome carta (vuoto per tutte): ");
        String cardName = sc.nextLine().trim();

        String setCode = inputSetCode();

        // Salva i filtri correnti
        currentCardNameFilter = cardName;
        currentSetFilter = (setCode != null && setCode.isEmpty()) ? null : setCode;

        if (controller != null) {
            // Utilizzo del metodo performSearch che sfrutta i getter dell'interfaccia (Pull
            // Model)
            List<CardBean> results = controller.performSearch();

            if (!results.isEmpty()) {
                selectCardDetails(results);
            }
        }
    }

    /**
     * Gestisce l'input per il codice del set, offrendo la possibilità di
     * visualizzare la lista dei set disponibili.
     * 
     * @return Il codice del set inserito o null.
     */
    private String inputSetCode() {
        System.out.print("Inserisci codice set (vuoto per tutti, '?' per lista): ");
        String setCode = sc.nextLine().trim();

        if ("?".equals(setCode) && controller != null) {
            java.util.Map<String, String> sets = controller.loadAvailableSets();
            if (sets.isEmpty()) {
                System.out.println("Nessun set disponibile.");
            } else {
                System.out.println("--- Set Disponibili ---");
                sets.forEach((code, name) -> System.out.println(code + ": " + name));
            }
            System.out.print("Inserisci codice set: ");
            setCode = sc.nextLine().trim();
        }
        return setCode;
    }

    /**
     * Gestisce la selezione di una carta dalla lista dei risultati per
     * visualizzarne i dettagli.
     * 
     * @param results La lista di carte risultanti dalla ricerca.
     */
    private void selectCardDetails(List<CardBean> results) {
        System.out.print("Inserisci numero carta per dettagli (0 per tornare): ");
        try {
            String input = sc.nextLine().trim();
            int idx = Integer.parseInt(input);
            if (idx > 0 && idx <= results.size()) {
                controller.openCardDetails(results.get(idx - 1));
            }
        } catch (NumberFormatException _) {
            System.out.println("Input non valido.");
        }
    }

    @Override
    public void close() {
        // nessuna operazione per la CLI
    }

    @Override
    public void refresh() {
        if (controller != null) {
            System.out.println("\nCaricamento carte della community...");
            List<CardBean> cards = controller.searchCards(currentCardNameFilter, currentSetFilter);
            displayCardList(cards);
        }
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("Errore: " + errorMessage);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (CollectorHPController) controller;
    }

    @Override
    public String getCardNameFilter() {
        return currentCardNameFilter;
    }

    @Override
    public String getSetFilter() {
        return currentSetFilter;
    }

    @Override
    public void displayCardList(List<CardBean> cardList) {
        if (cardList == null || cardList.isEmpty()) {
            System.out.println("Nessuna carta trovata.");
            return;
        }

        System.out.println("\n--- Carte trovate: " + cardList.size() + " ---");
        for (int i = 0; i < cardList.size(); i++) {
            CardBean card = cardList.get(i);
            System.out.printf("%d) %s (Owner: %s, Qty: %d)%n",
                    i + 1,
                    card.getName(),
                    card.getOwner() != null ? card.getOwner() : "?",
                    card.getQuantity());
        }
        System.out.println("----------------------------");
    }

    @Override
    public void displayCardOverview(CardBean card) {

        System.out.println("\n========== DETTAGLI CARTA ==========");
        System.out.println("Nome: " + card.getName());
        System.out.println("ID: " + card.getId());
        System.out.println("Gioco: " + (card.getGameType() != null ? card.getGameType().name() : "N/A"));

        if (card.getDetails() instanceof model.domain.PokemonCardDetails details) {
            System.out.println("HP: " + details.getHp());
            System.out.println("Stage: " + details.getStage());
            System.out.println("Rarità: " + details.getRarity());
        }

        System.out.println("Quantità: " + card.getQuantity());
        System.out.println("Collezionista: " + (card.getOwner() != null ? card.getOwner() : "Sconosciuto"));
        System.out.println("=====================================");

        System.out.print("Vuoi proporre uno scambio? (s/n): ");
        String choice = sc.nextLine().trim().toLowerCase();
        if ("s".equals(choice)) {
            onProposeTrade(card);
        }
    }

    /**
     * Gestisce l'avvio del flusso di creazione proposta.
     * Passa la carta target al controller tramite temporary data e naviga alla
     * vista proposta.
     */
    @Override
    public void onProposeTrade(CardBean card) {
        if (card != null && controller != null) {
            // Passa il CardBean target al controller applicativo via temporary data
            controller.getApplicationController()
                    .setTemporaryData("PROPOSAL_TARGET_CARD", card);

            // Naviga alla vista di creazione proposta
            controller.getApplicationController()
                    .navigateTO(model.domain.enumerations.ViewPage.PROPOSAL);
        } else {
            System.out.println("Errore: nessuna carta selezionata per la proposta.");
        }
    }

}
