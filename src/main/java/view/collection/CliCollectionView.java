package view.collection;

import controller.CollectionController;
import model.bean.CardBean;
import model.domain.Binder;
import model.domain.card.Card;
import model.domain.card.CardProvider;
import view.InputManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * CLI implementation of the Collection View.
 * Provides text-based interface for managing card collections.
 */
public class CliCollectionView implements ICollectionView {
    private static final Logger LOGGER = Logger.getLogger(CliCollectionView.class.getName());
    private static final String SEPARATOR = "━".repeat(80);
    private static final String THIN_SEPARATOR = "─".repeat(80);

    private final InputManager inputManager;
    private CollectionController controller;
    private Map<String, Binder> currentBinders;
    private CardProvider currentCardProvider;
    private String username;
    private boolean saveButtonVisible;

    public CliCollectionView(InputManager inputManager) {
        this.inputManager = inputManager;
        this.saveButtonVisible = false;
    }

    public void setController(CollectionController controller) {
        this.controller = controller;
    }

    @Override
    public void setWelcomeMessage(String username) {
        this.username = username;
    }

    @Override
    public void displayCollection(Map<String, Binder> bindersBySet, CardProvider cardProvider) {
        this.currentBinders = bindersBySet;
        this.currentCardProvider = cardProvider;

        clearScreen();
        showCollectionHeader();

        if (bindersBySet.isEmpty()) {
            showEmptyState();
        } else {
            showSetsOverview();
        }

        showMainMenu();
    }

    @Override
    public void updateCardInSet(String setId, String cardId) {
        // In CLI, non è necessario aggiornare un singolo elemento
        // Il refresh avviene solo quando si salva o si ricarica
        LOGGER.info(() -> "Card updated in cache: " + cardId + " in set " + setId);
    }

    @Override
    public void setSaveButtonVisible(boolean visible) {
        this.saveButtonVisible = visible;
    }

    @Override
    public void showSuccess(String message) {
        System.out.println("\n✓ " + message);
        pause();
    }

    @Override
    public void showError(String message) {
        System.out.println("\n✗ ERRORE: " + message);
        pause();
    }

    @Override
    public void display() {
        // Main loop già gestito dal menu
    }

    @Override
    public void close() {
        // CLI non ha risorse da chiudere
    }

    // ==================== PRIVATE METHODS ====================

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void showCollectionHeader() {
        System.out.println(SEPARATOR);
        System.out.println("   📚  COLLEZIONE DI " + (username != null ? username.toUpperCase() : "UTENTE"));
        System.out.println(SEPARATOR);
    }

    private void showEmptyState() {
        System.out.println("\n   Nessun set nella collezione.");
        System.out.println("   Aggiungi il tuo primo set per iniziare!\n");
    }

    private void showSetsOverview() {
        System.out.println();
        int index = 1;
        for (Map.Entry<String, Binder> entry : currentBinders.entrySet()) {
            Binder binder = entry.getValue();
            try {
                List<Card> allCards = currentCardProvider.searchPokemonSet(entry.getKey());
                int totalCards = allCards.size();
                int ownedCards = binder.getCardCount();
                int missingCards = totalCards - ownedCards;

                System.out.printf("   [%d] %s - %d carte possedute, \u001B[31m%d mancanti\u001B[0m%n",
                    index++, binder.getSetName(), ownedCards, missingCards);
            } catch (Exception e) {
                // Fallback se non si riesce a caricare il totale
                System.out.printf("   [%d] %s - %d carte possedute%n",
                    index++, binder.getSetName(), binder.getCardCount());
            }
        }
        System.out.println();
    }

    private void showMainMenu() {
        while (true) {
            System.out.println(THIN_SEPARATOR);
            System.out.println("OPZIONI:");
            System.out.println("  1) Gestisci Set");
            System.out.println("  2) Aggiungi Nuovo Set");
            if (saveButtonVisible) {
                System.out.println("  3) 💾 SALVA Modifiche Pendenti");
            }
            System.out.println("  0) Torna alla Home");
            System.out.print("\nScelta: ");

            String choice = inputManager.readString().trim();

            switch (choice) {
                case "1" -> manageSets();
                case "2" -> addNewSet();
                case "3" -> {
                    if (saveButtonVisible && controller != null) {
                        controller.saveChanges();
                    }
                }
                case "0" -> {
                    if (controller != null) {
                        controller.navigateToHome();
                    }
                    return;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void manageSets() {
        if (currentBinders == null || currentBinders.isEmpty()) {
            showError("Nessun set disponibile.");
            return;
        }

        clearScreen();
        showCollectionHeader();
        showSetsOverview();

        System.out.println("Seleziona un set da gestire (0 per tornare):");
        System.out.print("Scelta: ");

        String choice = inputManager.readString().trim();

        if ("0".equals(choice)) {
            displayCollection(currentBinders, currentCardProvider);
            return;
        }

        try {
            int index = Integer.parseInt(choice) - 1;
            List<Map.Entry<String, Binder>> bindersList = new ArrayList<>(currentBinders.entrySet());

            if (index >= 0 && index < bindersList.size()) {
                Map.Entry<String, Binder> entry = bindersList.get(index);
                manageSet(entry.getKey(), entry.getValue());
            } else {
                showError("Scelta non valida.");
            }
        } catch (NumberFormatException e) {
            showError("Inserisci un numero valido.");
        }
    }

    private void manageSet(String setId, Binder binder) {
        while (true) {
            clearScreen();
            System.out.println(SEPARATOR);
            System.out.println("   📦  SET: " + binder.getSetName());
            System.out.println(SEPARATOR);

            try {
                List<Card> allCards = currentCardProvider.searchPokemonSet(setId);
                Map<String, CardBean> ownedCardsMap = new java.util.HashMap<>();
                for (CardBean card : binder.getCards()) {
                    ownedCardsMap.put(card.getId(), card);
                }

                showSetStatistics(binder, allCards.size());
                showCardsInSet(allCards, ownedCardsMap);

                System.out.println(THIN_SEPARATOR);
                System.out.println("OPZIONI:");
                System.out.println("  1) Aggiungi Carta");
                System.out.println("  2) Rimuovi Carta");
                System.out.println("  3) Gestisci Carta (quantità/scambiabile)");
                System.out.println("  4) Elimina Set");
                System.out.println("  0) Torna Indietro");
                System.out.print("\nScelta: ");

                String choice = inputManager.readString().trim();

                switch (choice) {
                    case "1" -> addCardToSet(setId, allCards, ownedCardsMap);
                    case "2" -> removeCardFromSet(setId, allCards);
                    case "3" -> manageCardDetails(setId, allCards, ownedCardsMap);
                    case "4" -> {
                        if (confirmDeleteSet(binder.getSetName())) {
                            if (controller != null) {
                                controller.deleteBinder(setId);
                            }
                            return;
                        }
                    }
                    case "0" -> {
                        displayCollection(currentBinders, currentCardProvider);
                        return;
                    }
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                showError("Errore nel caricamento delle carte del set.");
                return;
            }
        }
    }

    private void showSetStatistics(Binder binder, int totalCards) {
        System.out.println("\nStatistiche:");
        System.out.printf("  Carte possedute: %d / %d%n", binder.getCardCount(), totalCards);

        if (totalCards > 0) {
            double percentage = (double) binder.getCardCount() / totalCards * 100;
            System.out.printf("  Completamento: %.1f%%%n", percentage);
        }

        long tradableCount = binder.getCards().stream()
            .filter(CardBean::isTradable)
            .count();
        System.out.printf("  Carte scambiabili: %d%n", tradableCount);
        System.out.println();
    }

    private void showCardsInSet(List<Card> allCards, Map<String, CardBean> ownedCardsMap) {
        System.out.println("Carte del set:");
        System.out.println(THIN_SEPARATOR);

        int count = 0;
        for (Card card : allCards) {
            CardBean owned = ownedCardsMap.get(card.getId());
            String status = owned != null ?
                String.format("[✓] x%d%s", owned.getQuantity(), owned.isTradable() ? " 🔄" : "") :
                "[ ]";

            System.out.printf("  %-6s %s%n", status, card.getName());
            count++;

            // Paginazione ogni 15 carte
            if (count % 15 == 0 && count < allCards.size()) {
                System.out.print("\n[Premi INVIO per continuare...]");
                inputManager.readString();
            }
        }
        System.out.println();
    }

    private void addCardToSet(String setId, List<Card> allCards, Map<String, CardBean> ownedCardsMap) {
        System.out.println("\nCarte non possedute:");
        List<Card> notOwnedCards = allCards.stream()
            .filter(card -> !ownedCardsMap.containsKey(card.getId()))
            .toList();

        if (notOwnedCards.isEmpty()) {
            System.out.println("  Possiedi già tutte le carte di questo set!");
            pause();
            return;
        }

        for (int i = 0; i < notOwnedCards.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, notOwnedCards.get(i).getName());
            if ((i + 1) % 15 == 0 && i < notOwnedCards.size() - 1) {
                System.out.print("\n[Premi INVIO per continuare...]");
                inputManager.readString();
            }
        }

        System.out.print("\nSeleziona carta da aggiungere (0 per annullare): ");
        String choice = inputManager.readString().trim();

        if ("0".equals(choice)) return;

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < notOwnedCards.size()) {
                Card selectedCard = notOwnedCards.get(index);
                if (controller != null) {
                    controller.addCardToSet(setId, selectedCard);
                    System.out.println("\n✓ Carta aggiunta! (Ricorda di salvare)");
                    pause();
                }
            } else {
                showError("Scelta non valida.");
            }
        } catch (NumberFormatException e) {
            showError("Inserisci un numero valido.");
        }
    }

    private void removeCardFromSet(String setId, List<Card> allCards) {
        Binder binder = currentBinders.get(setId);
        if (binder == null || binder.getCards().isEmpty()) {
            System.out.println("\nNessuna carta da rimuovere.");
            pause();
            return;
        }

        System.out.println("\nCarte possedute:");
        List<CardBean> ownedCards = new ArrayList<>(binder.getCards());

        for (int i = 0; i < ownedCards.size(); i++) {
            CardBean card = ownedCards.get(i);
            System.out.printf("  [%d] %s (x%d)%n", i + 1, card.getName(), card.getQuantity());
        }

        System.out.print("\nSeleziona carta da rimuovere (0 per annullare): ");
        String choice = inputManager.readString().trim();

        if ("0".equals(choice)) return;

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < ownedCards.size()) {
                CardBean selectedCard = ownedCards.get(index);

                // Trova la carta completa
                Card fullCard = allCards.stream()
                    .filter(c -> c.getId().equals(selectedCard.getId()))
                    .findFirst()
                    .orElse(null);

                if (fullCard != null && controller != null) {
                    controller.removeCardFromSet(setId, fullCard);
                    System.out.println("\n✓ Carta rimossa! (Ricorda di salvare)");
                    pause();
                }
            } else {
                showError("Scelta non valida.");
            }
        } catch (NumberFormatException e) {
            showError("Inserisci un numero valido.");
        }
    }

    private void manageCardDetails(String setId, List<Card> allCards, Map<String, CardBean> ownedCardsMap) {
        if (ownedCardsMap.isEmpty()) {
            System.out.println("\nNessuna carta posseduta da gestire.");
            pause();
            return;
        }

        System.out.println("\nCarte possedute:");
        List<CardBean> ownedCards = new ArrayList<>(ownedCardsMap.values());

        for (int i = 0; i < ownedCards.size(); i++) {
            CardBean card = ownedCards.get(i);
            System.out.printf("  [%d] %s (x%d) %s%n",
                i + 1, card.getName(), card.getQuantity(),
                card.isTradable() ? "🔄" : "");
        }

        System.out.print("\nSeleziona carta (0 per annullare): ");
        String choice = inputManager.readString().trim();

        if ("0".equals(choice)) return;

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < ownedCards.size()) {
                CardBean selectedCard = ownedCards.get(index);
                manageCardQuantityAndTradable(setId, selectedCard, allCards);
            } else {
                showError("Scelta non valida.");
            }
        } catch (NumberFormatException e) {
            showError("Inserisci un numero valido.");
        }
    }

    private void manageCardQuantityAndTradable(String setId, CardBean cardBean, List<Card> allCards) {
        System.out.println("\n--- " + cardBean.getName() + " ---");
        System.out.println("Quantità attuale: " + cardBean.getQuantity());
        System.out.println("Scambiabile: " + (cardBean.isTradable() ? "Sì" : "No"));

        System.out.println("\n1) Aumenta quantità");
        System.out.println("2) Diminuisci quantità");
        System.out.println("3) Toggle scambiabile");
        System.out.println("0) Annulla");
        System.out.print("\nScelta: ");

        String choice = inputManager.readString().trim();

        if (controller == null) return;

        // Trova la carta completa
        Card fullCard = allCards.stream()
            .filter(c -> c.getId().equals(cardBean.getId()))
            .findFirst()
            .orElse(null);

        if (fullCard == null) return;

        switch (choice) {
            case "1" -> {
                controller.addCardToSet(setId, fullCard);
                System.out.println("✓ Quantità aumentata! (Ricorda di salvare)");
                pause();
            }
            case "2" -> {
                controller.removeCardFromSet(setId, fullCard);
                System.out.println("✓ Quantità diminuita! (Ricorda di salvare)");
                pause();
            }
            case "3" -> {
                boolean newTradable = !cardBean.isTradable();
                controller.toggleCardTradable(setId, cardBean.getId(), newTradable);
                System.out.println("✓ Stato scambiabile aggiornato! (Ricorda di salvare)");
                pause();
            }
            case "0" -> {}
            default -> System.out.println("Scelta non valida.");
        }
    }

    private boolean confirmDeleteSet(String setName) {
        System.out.println("\n⚠️  ATTENZIONE!");
        System.out.println("Stai per eliminare il set: " + setName);
        System.out.println("Questa azione eliminerà il set e tutte le carte associate.");
        System.out.println("L'operazione NON può essere annullata.");
        System.out.print("\nDigita 'CONFERMA' per procedere: ");

        String confirmation = inputManager.readString().trim();
        return "CONFERMA".equals(confirmation);
    }

    private void addNewSet() {
        if (controller == null) {
            showError("Controller non disponibile.");
            return;
        }

        Map<String, String> availableSets = controller.getAvailableSets();

        if (availableSets.isEmpty()) {
            showError("Nessun set disponibile al momento.");
            return;
        }

        clearScreen();
        System.out.println(SEPARATOR);
        System.out.println("   ➕  AGGIUNGI NUOVO SET");
        System.out.println(SEPARATOR);

        List<Map.Entry<String, String>> setsList = new ArrayList<>(availableSets.entrySet());

        for (int i = 0; i < setsList.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, setsList.get(i).getValue());

            if ((i + 1) % 20 == 0 && i < setsList.size() - 1) {
                System.out.print("\n[Premi INVIO per continuare...]");
                inputManager.readString();
            }
        }

        System.out.print("\nSeleziona set da aggiungere (0 per annullare): ");
        String choice = inputManager.readString().trim();

        if ("0".equals(choice)) {
            displayCollection(currentBinders, currentCardProvider);
            return;
        }

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < setsList.size()) {
                Map.Entry<String, String> selectedSet = setsList.get(index);
                controller.createBinder(selectedSet.getKey(), selectedSet.getValue());
            } else {
                showError("Scelta non valida.");
            }
        } catch (NumberFormatException e) {
            showError("Inserisci un numero valido.");
        }
    }

    private void pause() {
        System.out.print("\n[Premi INVIO per continuare...]");
        inputManager.readString();
    }
}
