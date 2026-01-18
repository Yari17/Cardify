package view.cli;

import controller.CollectorHPController;
import model.bean.CardBean;
import config.InputManager;
import view.ICollectorHPView;

import java.util.List;
import java.util.Map;

@SuppressWarnings("java:S106")
public class CliCollectorHPView implements ICollectorHPView {
    private static final String SEPARATOR_LINE = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final String PRESS_ENTER_TO_CONTINUE = "Premi INVIO per continuare...";
    private static final String NO_CARDS_AVAILABLE = "\nNessuna carta disponibile.";
    private static final String HEADER_TOP_BORDER = "\n╔════════════════════════════════════════════════════════════════════╗";
    private static final String HEADER_BOTTOM_BORDER = "╚════════════════════════════════════════════════════════════════════╝";
    private static final String CONTROLLER_NON_DISPONIBILE="Controller non disponibile";

    private final InputManager inputManager;
    private CollectorHPController controller;
    private List<CardBean> currentCards;
    private Map<String, String> availableSets; // ID -> Nome del set

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

        runMainLoop();
    }

    // Extracted main loop to reduce cognitive complexity of display()
    private void runMainLoop() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = inputManager.readString();
            if (choice == null) choice = "";
            choice = choice.trim();
            // Ignore empty lines which may come from piped input or accidental ENTER
            if (choice.isEmpty()) continue;
            // Delegate handling to a separate method to reduce cognitive complexity in display()
            running = handleMainSelection(choice);
        }
    }

    /**
     * Handle the main-menu selection. Returns true if the CLI should continue running,
     * false to stop/exit this view.
     */
    private boolean handleMainSelection(String choice) {
        switch (choice) {
            case "1":
                showPopularCardsMenu();
                return true;
            case "2":
                searchByNameMenu();
                return true;
            case "3":
                searchBySetMenu();
                return true;
            case "4":
                // Delegate navigation to the controller and exit CLI loop so the
                // ApplicationController can display the target view.
                if (controller != null) {
                    controller.navigateToCollection();
                    return false; // stop CLI loop
                } else {
                    System.out.println(CONTROLLER_NON_DISPONIBILE);
                    return true;
                }
            case "5":
                // Manage Trades (corresponds to JavaFX Manage Trades)
                if (controller != null) {
                    controller.navigateToManageTrade();
                    return false; // allow navigationController to display the destination view
                } else {
                    System.out.println(CONTROLLER_NON_DISPONIBILE);
                    return true;
                }
            case "6":
                // Go to Live Trades / Trade page (corresponds to JavaFX Trade)
                if (controller != null) {
                    controller.navigateToTrade();
                    return false;
                } else {
                    System.out.println(CONTROLLER_NON_DISPONIBILE);
                    return true;
                }
            case "7":
                if (controller != null) {
                    controller.onLogoutRequested();
                }
                return false;
            case "0":
                if (controller != null) {
                    controller.onExitRequested();
                }
                return false;
             default:
                 System.out.println("Opzione non valida. Riprova.");
                 return true;
         }
     }

    @Override
    public void close() {
        // Intentionally empty for CLI: no resources to release here.
    }

    @Override
    public void refresh() {
        // CLI view: refresh does not automatically re-render the interactive loop.
        // Consumers can call display() to re-open the interactive menu.
    }

    @Override
    public void setStage(javafx.stage.Stage stage) {
        // CLI does not use JavaFX stages; provided for interface compatibility.
    }

    @Override
    public void showWelcomeMessage(String username) {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   Benvenuto in CARDIFY, " + username + "!   ║");
        System.out.println("╚════════════════════════════════════╝");
    }

    @Override
    public void showCardOverview(CardBean card) {
        // Provide a simple textual overview in CLI by delegating to the detailed view
        if (card == null) {
            System.out.println("Nessuna carta selezionata.");
            return;
        }
        // Reuse the existing detailed card modal logic already used elsewhere in this class
        showCardDetails(card);
    }

    @Override
    public void displayCards(List<CardBean> cards) {
        this.currentCards = cards;

        if (cards == null || cards.isEmpty()) {
            System.out.println(NO_CARDS_AVAILABLE);
            return;
        }

        // Sistema di paginazione interattivo
        int cardsPerPage = 10;
        int currentPage = 0;
        int totalPages = (int) Math.ceil((double) cards.size() / cardsPerPage);
        boolean browsing = true;

        while (browsing) {
            showPage(cards, currentPage, cardsPerPage, totalPages);

            String choice = inputManager.readString().trim();
            int newPage = handleNavigationInput(choice, currentPage, totalPages, cards);

            if (newPage == -2) { // Exit code
                browsing = false;
            } else if (newPage != -1) { // Valid new page
                currentPage = newPage;
            }
        }
    }

    private void showPage(List<CardBean> cards, int currentPage, int cardsPerPage, int totalPages) {
        // Mostra header
        // Mostra header
        System.out.println(HEADER_TOP_BORDER);
        System.out.println("║                        CARTE TROVATE                               ║");
        System.out.println(HEADER_BOTTOM_BORDER);
        System.out.println("Totale carte: " + cards.size() + " | Pagina " + (currentPage + 1) + " di " + totalPages);
        System.out.println(SEPARATOR_LINE);

        // Calcola range per la pagina corrente
        int start = currentPage * cardsPerPage;
        int end = Math.min(start + cardsPerPage, cards.size());

        // Mostra le carte della pagina corrente
        for (int i = start; i < end; i++) {
            CardBean card = cards.get(i);
            System.out.println("\n" + (i + 1) + ". ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("   Nome:      " + card.getName());
            System.out.println("   ID:        " + card.getId());
            // Show owner if present
            if (card.getOwner() != null && !card.getOwner().isBlank()) {
                System.out.println("   Owner:     " + card.getOwner());
            }
            System.out.println("   Gioco:     " + card.getGameType());
            System.out.println("   Immagine:  " + (card.getImageUrl() != null ? "✓ Disponibile" : "✗ Non disponibile"));
        }

        System.out.println("\n" + SEPARATOR_LINE);

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
    }

    /**
     * Handles navigation input.
     * Returns:
     * -2 if user wants to exit (0)
     * -1 if input was handled or invalid (stay on same page)
     * >= 0 for the new page index
     */
    private int handleNavigationInput(String choice, int currentPage, int totalPages, List<CardBean> cards) {
        if ("0".equals(choice)) {
            return -2;
        } else if ("N".equalsIgnoreCase(choice)) {
            if (currentPage < totalPages - 1) {
                return currentPage + 1;
            } else {
                System.out.println("⚠ Sei già all'ultima pagina");
                System.out.print(PRESS_ENTER_TO_CONTINUE);
                inputManager.readString();
                return -1;
            }
        } else if ("P".equalsIgnoreCase(choice)) {
            if (currentPage > 0) {
                return currentPage - 1;
            } else {
                System.out.println("⚠ Sei già alla prima pagina");
                System.out.print(PRESS_ENTER_TO_CONTINUE);
                inputManager.readString();
                return -1;
            }
        } else {
            return tryShowCardDetails(choice, cards);
        }
    }

    private int tryShowCardDetails(String choice, List<CardBean> cards) {
        try {
            int cardIndex = Integer.parseInt(choice) - 1;
            if (cardIndex >= 0 && cardIndex < cards.size()) {
                CardBean selectedCard = cards.get(cardIndex);
                showCardDetails(selectedCard);
            } else {
                System.out.println("⚠ Numero carta non valido. Deve essere tra 1 e " + cards.size());
                System.out.print(PRESS_ENTER_TO_CONTINUE);
                inputManager.readString();
            }
            return -1; // Stay on page after viewing details
        } catch (NumberFormatException _) {
            System.out.println("⚠ Input non valido. Inserisci un numero, N, P o 0");
            System.out.print(PRESS_ENTER_TO_CONTINUE);
            inputManager.readString();
            return -1;
        }
    }

    private void showCardDetails(CardBean card) {
        System.out.println(HEADER_TOP_BORDER);
        System.out.println("║                      DETTAGLI CARTA                                ║");
        System.out.println(HEADER_BOTTOM_BORDER);
        System.out.println("\n📇 Nome:        " + card.getName());
        System.out.println("🆔 ID:          " + card.getId());
        System.out.println("🎮 Gioco:       " + card.getGameType());
        if (card.getOwner() != null && !card.getOwner().isBlank()) {
            System.out.println("👤 Proprietario: " + card.getOwner());
        }
        System.out
                .println("🖼️  Immagine:    " + (card.getImageUrl() != null ? card.getImageUrl() : "Non disponibile"));
        System.out.println("\n" + SEPARATOR_LINE);
        // If the card is tradable and owned by another user, offer to propose a trade
        boolean canPropose = card.isTradable() && controller != null && !controller.getUsername().equals(card.getOwner());
        if (canPropose) {
            System.out.println("Opzioni: 1=Proponi scambio 0=Indietro");
            System.out.print("Scelta: ");
            String choice = inputManager.readString().trim();
            if ("1".equals(choice)) {
                // Delegate to controller to open negotiation (ApplicationController will navigate)

                controller.openNegotiation(card);
                // navigation will take over

            }
        } else {
            System.out.print("\nPremi INVIO per tornare alla lista...");
            inputManager.readString();
        }
    }

    private void showPopularCardsMenu() {
        System.out.println("DEBUG: showPopularCardsMenu called");
        // Always (re)load popular cards when the user selects the popular-cards menu.
        System.out.println("\n🔄 Caricamento carte popolari in corso...");
        if (controller != null) {
            controller.loadPopularCards();
            // controller will call displayCards(...) when data is ready
            return;
        }

        // If controller is not available (shouldn't normally happen), fall back to cached cards
        if (currentCards != null && !currentCards.isEmpty()) {
            displayCards(currentCards);
        } else {
            System.out.println(NO_CARDS_AVAILABLE);
            System.out.print(PRESS_ENTER_TO_CONTINUE);
            inputManager.readString();
        }
    }

    private void printMainMenu() {
        System.out.println("\n=== CARDIFY HOME PAGE ===");
        System.out.println("1. Visualizza carte popolari");
        System.out.println("2. Cerca carte per nome");
        System.out.println("3. Cerca carte per set");
        System.out.println("4. Gestisci collezione");
        System.out.println("5. Gestisci proposte di scambio");
        System.out.println("6. Scambia");
        System.out.println("7. Logout");
        System.out.println("0. Esci");
        System.out.print("Scegli un'opzione: ");
    }

    private void searchByNameMenu() {
        printSearchByNamePrompt();
        String cardName = inputManager.readString().trim();

        if (cardName.isEmpty()) {
            System.out.println("⚠ Nome carta non valido");
            System.out.print(PRESS_ENTER_TO_CONTINUE);
            inputManager.readString();
            return;
        }

        System.out.println("\n✓ Ricerca impostata - Tipo: BY_NAME, Query: " + cardName);

        if (controller != null) {
            System.out.println("🔄 Ricerca in corso...");
            controller.searchCardsByName(cardName);
            // NOTE: controller.searchCardsByName calls view.displayCards()
        } else {
            System.out.println("ERROR: Controller non connesso");
            System.out.print("\n" + PRESS_ENTER_TO_CONTINUE);
            inputManager.readString();
        }
    }

    private void printSearchByNamePrompt() {
        System.out.println(HEADER_TOP_BORDER);
        System.out.println("║                    RICERCA PER NOME                               ║");
        System.out.println(HEADER_BOTTOM_BORDER);
        System.out.print("\nInserisci il nome della carta da cercare: ");
    }

    private void searchBySetMenu() {
        if (availableSets == null || availableSets.isEmpty()) {
            System.out.println("\n⚠ Set non ancora caricati. Attendi un momento...");
            System.out.print(PRESS_ENTER_TO_CONTINUE);
            inputManager.readString();
            return;
        }

        // Converti la mappa in una lista per permettere la selezione numerica
        List<Map.Entry<String, String>> setsList = new java.util.ArrayList<>(availableSets.entrySet());
        int pageSize = 20;
        int currentPage = 0;
        int totalPages = (int) Math.ceil((double) setsList.size() / pageSize);
        boolean browsing = true;

        while (browsing) {
            displaySetsPage(setsList, currentPage, pageSize, totalPages);
            String choice = inputManager.readString().trim();
            int newPage = handleSetNavigationInput(choice, currentPage, totalPages, setsList);

            if (newPage == -2) {
                browsing = false;
            } else if (newPage != -1) {
                currentPage = newPage;
            }
        }
    }

    private void displaySetsPage(List<Map.Entry<String, String>> setsList, int currentPage, int pageSize,
                                 int totalPages) {
        System.out.println(HEADER_TOP_BORDER);
        System.out.println("║                      RICERCA PER SET                              ║");
        System.out.println(HEADER_BOTTOM_BORDER);
        System.out.println("\nSet disponibili (" + setsList.size() + " totali):");
        System.out.println(SEPARATOR_LINE);

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, setsList.size());

        for (int i = start; i < end; i++) {
            Map.Entry<String, String> entry = setsList.get(i);
            System.out.printf("%3d. %-50s (ID: %s)%n", i + 1, entry.getValue(), entry.getKey());
        }

        System.out.println(SEPARATOR_LINE);
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
    }

    private int handleSetNavigationInput(String choice, int currentPage, int totalPages,
                                         List<Map.Entry<String, String>> setsList) {
        if ("0".equals(choice)) {
            return -2;
        } else if ("N".equalsIgnoreCase(choice)) {
            return (currentPage < totalPages - 1) ? currentPage + 1 : -1;
        } else if ("P".equalsIgnoreCase(choice)) {
            return (currentPage > 0) ? currentPage - 1 : -1;
        } else {
            return trySelectSet(choice, setsList);
        }
    }

    private int trySelectSet(String choice, List<Map.Entry<String, String>> setsList) {
        try {
            int setIndex = Integer.parseInt(choice) - 1;
            if (setIndex >= 0 && setIndex < setsList.size()) {
                Map.Entry<String, String> selectedSet = setsList.get(setIndex);
                loadSet(selectedSet);
            } else {
                System.out.println("⚠ Numero set non valido. Riprova.");
            }
        } catch (NumberFormatException _) {
            System.out.println("⚠ Input non valido. Inserisci un numero o N/P.");
        }
        return -1; // Always stay on page or valid transition handled elsewhere (but here we just
        // load and stay)
    }

    private void loadSet(Map.Entry<String, String> selectedSet) {
        String setId = selectedSet.getKey();
        String setName = selectedSet.getValue();
        System.out.println("\n🔄 Caricamento carte dal set: " + setName + " (" + setId + ")");

        if (controller != null) {
            controller.loadCardsFromSet(setId);
        } else if (currentCards != null && !currentCards.isEmpty()) {
            displayCards(currentCards);
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

    @Override
    public void showSuccess(String message) {
        // For CLI, simply print the success message
    }

    @Override
    public void showError(String message) {
        // For CLI, simply print the error message
    }
}
