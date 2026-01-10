package view.collectorhomepage;

import controller.CollectorHPController;
import model.bean.CardBean;
import view.InputManager;

import java.util.List;
import java.util.Map;

public class CliCollectorHPView implements ICollectorHPView {
    private final InputManager inputManager;
    private CollectorHPController controller;
    private List<CardBean> currentCards;
    private Map<String, String> availableSets; // ID -> Nome del set

    // Campi per gestire la ricerca unificata
    private SearchType currentSearchType = SearchType.BY_NAME;
    private String currentSearchQuery = "";

    public CliCollectorHPView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void setController(CollectorHPController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        if (controller == null) {
            System.out.println("ERROR: Controller not set");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== CARDIFY HOME PAGE ===");
            System.out.println("1. Visualizza carte popolari");
            System.out.println("2. Cerca carte per nome");
            System.out.println("3. Cerca carte per set");
            System.out.println("4. Gestisci collezione");
            System.out.println("5. Effettua scambio");
            System.out.println("6. Visualizza profilo");
            System.out.println("7. Logout");
            System.out.println("0. Esci");
            System.out.print("Scegli un'opzione: ");

            String choice = inputManager.readString();

            switch (choice) {
                case "1":
                    showPopularCardsMenu();
                    break;
                case "2":
                    searchByNameMenu();
                    break;
                case "3":
                    searchBySetMenu();
                    break;
                case "4":
                    System.out.println("Gestisci collezione selezionato.");
                    break;
                case "5":
                    System.out.println("Effettua scambio selezionato.");
                    break;
                case "6":
                    if (controller != null) {
                        showWelcomeMessage(controller.getUsername());
                    }
                    break;
                case "7":
                    if (controller != null) {
                        controller.onLogoutRequested();
                    }
                    running = false;
                    break;
                case "0":
                    if (controller != null) {
                        controller.onExitRequested();
                    }
                    running = false;
                    break;
                default:
                    System.out.println("Opzione non valida. Riprova.");
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void showWelcomeMessage(String username) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   Benvenuto in CARDIFY, " + username + "!   ║");
        System.out.println("╚════════════════════════════════════╝");
    }

    @Override
    public void showCardOverview(CardBean card) {

    }

    @Override
    public void displayCards(List<CardBean> cards) {
        this.currentCards = cards;

        if (cards == null || cards.isEmpty()) {
            System.out.println("\nNessuna carta disponibile.");
            return;
        }

        // Sistema di paginazione interattivo
        int cardsPerPage = 10;
        int currentPage = 0;
        int totalPages = (int) Math.ceil((double) cards.size() / cardsPerPage);
        boolean browsing = true;

        while (browsing) {
            // Mostra header
            System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                        CARTE TROVATE                               ║");
            System.out.println("╚════════════════════════════════════════════════════════════════════╝");
            System.out.println("Totale carte: " + cards.size() + " | Pagina " + (currentPage + 1) + " di " + totalPages);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Calcola range per la pagina corrente
            int start = currentPage * cardsPerPage;
            int end = Math.min(start + cardsPerPage, cards.size());

            // Mostra le carte della pagina corrente
            for (int i = start; i < end; i++) {
                CardBean card = cards.get(i);
                System.out.println("\n" + (i + 1) + ". ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("   Nome:      " + card.getName());
                System.out.println("   ID:        " + card.getId());
                System.out.println("   Gioco:     " + card.getGameType());
                System.out.println("   Immagine:  " + (card.getImageUrl() != null ? "✓ Disponibile" : "✗ Non disponibile"));
            }

            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Mostra opzioni disponibili
            System.out.println("\n📋 OPZIONI:");
            System.out.println("   • Inserisci il numero (1-" + cards.size() + ") per vedere i dettagli della carta");
            if (currentPage < totalPages - 1) {
                System.out.println("   • Premi 'N' per vedere le carte successive");
            }
            if (currentPage > 0) {
                System.out.println("   • Premi 'P' per vedere le carte precedenti");
            }
            System.out.println("   • Premi '0' per tornare alla homepage");
            System.out.print("\n➤ Scelta: ");

            String choice = inputManager.readString().trim();

            if ("0".equals(choice)) {
                browsing = false;
            } else if ("N".equalsIgnoreCase(choice)) {
                if (currentPage < totalPages - 1) {
                    currentPage++;
                } else {
                    System.out.println("⚠ Sei già all'ultima pagina");
                    System.out.print("Premi INVIO per continuare...");
                    inputManager.readString();
                }
            } else if ("P".equalsIgnoreCase(choice)) {
                if (currentPage > 0) {
                    currentPage--;
                } else {
                    System.out.println("⚠ Sei già alla prima pagina");
                    System.out.print("Premi INVIO per continuare...");
                    inputManager.readString();
                }
            } else {
                // Prova a interpretare come numero di carta
                try {
                    int cardIndex = Integer.parseInt(choice) - 1;
                    if (cardIndex >= 0 && cardIndex < cards.size()) {
                        CardBean selectedCard = cards.get(cardIndex);
                        showCardDetails(selectedCard);
                    } else {
                        System.out.println("⚠ Numero carta non valido. Deve essere tra 1 e " + cards.size());
                        System.out.print("Premi INVIO per continuare...");
                        inputManager.readString();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠ Input non valido. Inserisci un numero, N, P o 0");
                    System.out.print("Premi INVIO per continuare...");
                    inputManager.readString();
                }
            }
        }
    }

    private void showCardDetails(CardBean card) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      DETTAGLI CARTA                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println("\n📇 Nome:        " + card.getName());
        System.out.println("🆔 ID:          " + card.getId());
        System.out.println("🎮 Gioco:       " + card.getGameType());
        System.out.println("🖼️  Immagine:    " + (card.getImageUrl() != null ? card.getImageUrl() : "Non disponibile"));
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.print("\nPremi INVIO per tornare alla lista...");
        inputManager.readString();
    }

    private void showPopularCardsMenu() {
        if (currentCards == null) {
            System.out.println("\n🔄 Caricamento carte popolari in corso...");
            if (controller != null) {
                controller.loadPopularCards();
            }
        }

        // displayCards gestisce tutta la navigazione e visualizzazione
        if (currentCards != null && !currentCards.isEmpty()) {
            displayCards(currentCards);
        } else {
            System.out.println("\n⚠ Nessuna carta disponibile.");
            System.out.print("Premi INVIO per continuare...");
            inputManager.readString();
        }
    }

    private void searchByNameMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RICERCA PER NOME                               ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");

        System.out.print("\nInserisci il nome della carta da cercare: ");
        String cardName = inputManager.readString().trim();

        if (cardName.isEmpty()) {
            System.out.println("⚠ Nome carta non valido");
            System.out.print("Premi INVIO per continuare...");
            inputManager.readString();
            return;
        }

        // Imposta il tipo di ricerca e la query
        currentSearchType = SearchType.BY_NAME;
        currentSearchQuery = cardName;

        System.out.println("\n✓ Ricerca impostata - Tipo: BY_NAME, Query: " + cardName);
        System.out.println("⚠ Funzionalità di ricerca per nome non ancora implementata nel controller");

        // TODO: Il controller dovrebbe implementare un metodo che chiama
        // view.getSearchQuery() e view.getSearchType() e poi esegue la ricerca appropriata

        System.out.print("\nPremi INVIO per continuare...");
        inputManager.readString();
    }

    private void searchBySetMenu() {
        if (availableSets == null || availableSets.isEmpty()) {
            System.out.println("\n⚠ Set non ancora caricati. Attendi un momento...");
            System.out.print("Premi INVIO per continuare...");
            inputManager.readString();
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      RICERCA PER SET                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");

        // Converti la mappa in una lista per permettere la selezione numerica
        List<Map.Entry<String, String>> setsList = new java.util.ArrayList<>(availableSets.entrySet());

        System.out.println("\nSet disponibili (" + setsList.size() + " totali):");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Mostra i set in gruppi di 20
        int pageSize = 20;
        int currentPage = 0;
        int totalPages = (int) Math.ceil((double) setsList.size() / pageSize);

        boolean browsing = true;
        while (browsing) {
            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, setsList.size());

            for (int i = start; i < end; i++) {
                Map.Entry<String, String> entry = setsList.get(i);
                System.out.printf("%3d. %-50s (ID: %s)%n",
                    i + 1,
                    entry.getValue(),
                    entry.getKey());
            }

            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Pagina " + (currentPage + 1) + " di " + totalPages);
            System.out.println("\nOpzioni:");
            System.out.println("- Inserisci il numero del set per vedere le carte");
            if (currentPage < totalPages - 1) {
                System.out.println("- Premi 'N' per la pagina successiva");
            }
            if (currentPage > 0) {
                System.out.println("- Premi 'P' per la pagina precedente");
            }
            System.out.println("- Premi 0 per tornare al menu principale");
            System.out.print("Scelta: ");

            String choice = inputManager.readString().trim();

            if ("0".equals(choice)) {
                browsing = false;
            } else if ("N".equalsIgnoreCase(choice) && currentPage < totalPages - 1) {
                currentPage++;
            } else if ("P".equalsIgnoreCase(choice) && currentPage > 0) {
                currentPage--;
            } else {
                try {
                    int setIndex = Integer.parseInt(choice) - 1;
                    if (setIndex >= 0 && setIndex < setsList.size()) {
                        Map.Entry<String, String> selectedSet = setsList.get(setIndex);
                        String setId = selectedSet.getKey();
                        String setName = selectedSet.getValue();

                        // Imposta il tipo di ricerca e la query
                        currentSearchType = SearchType.BY_SET;
                        currentSearchQuery = setId;

                        System.out.println("\n🔄 Caricamento carte dal set: " + setName + " (" + setId + ")");

                        if (controller != null) {
                            controller.loadCardsFromSet(setId);
                        }

                        // displayCards gestisce la visualizzazione e la navigazione
                        if (currentCards != null && !currentCards.isEmpty()) {
                            displayCards(currentCards);
                        }
                    } else {
                        System.out.println("⚠ Numero set non valido. Riprova.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠ Input non valido. Inserisci un numero o N/P.");
                }
            }
        }
    }

    @Override
    public void displayAvailableSets(Map<String, String> setsMap) {
        if (setsMap != null && !setsMap.isEmpty()) {
            this.availableSets = setsMap;
            System.out.println("✓ Caricati " + setsMap.size() + " set disponibili");
        } else {
            System.out.println("⚠ Nessun set disponibile");
        }
    }
}
