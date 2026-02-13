package view.cli;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;

import controller.TradeProposalController;
import model.bean.CardBean;
import model.bean.UserBean;
import view.ITradeProposalView;

/**
 * Implementazione CLI della view per la proposta di scambio.
 * Refactored per utilizzare il modello PUSH: i dati vengono ricevuti tramite i
 * metodi
 * dell'interfaccia showAvailableItems, showOfferedItems e showTargetItem.
 */
public class CliTradeProposalView implements ITradeProposalView {

    private TradeProposalController controller;
    private final Scanner scanner;
    private boolean running = false;

    // Stato locale per il rendering (PUSHed dal controller)
    private List<CardBean> availableCards = Collections.emptyList();
    private List<CardBean> offeredCards = Collections.emptyList();
    private CardBean targetCard;

    private static final String MSG_INVALID_INPUT = "Input non valido.";

    public CliTradeProposalView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (TradeProposalController) controller;
    }

    @Override
    public void display() {
        running = true;
        // Al primo avvio, l'updateView() nel setController del controller avrà già
        // popolato lo stato
        // se setView è chiamato prima di display. Altrimenti, refresh potrebbe essere
        // utile.
        // Ma per sicurezza, assumiamo che il controller abbia già spinto i dati o lo
        // farà presto.

        while (running) {
            printDashboard();
            String choice = scanner.nextLine();
            handleChoice(choice);
            // I metodi show... vengono chiamati dal controller in risposta alle azioni,
            // aggiornando lo stato locale per il prossimo ciclo del loop.
        }
    }

    @Override
    public void refresh() {
        // In questo modello PUSH, refresh serve solo a scatenare l'aggiornamento
        // iniziale se necessario, o per gestire aggiornamenti asincroni se la CLI lo
        // supportasse.
        // Qui non facciamo nulla perché è il controller che chiama i show...
    }

    // --- Implementazione Metodi Interfaccia (PUSH) ---

    @Override
    public void showAvailableItems(List<CardBean> cards) {
        this.availableCards = cards != null ? cards : Collections.emptyList();
    }

    @Override
    public void showOfferedItems(List<CardBean> cards) {
        this.offeredCards = cards != null ? cards : Collections.emptyList();
    }

    @Override
    public void showTargetItem(CardBean card) {
        this.targetCard = card;
    }

    // --- Logica Interna CLI ---

    /**
     * Stampa a video la dashboard principale della proposta di scambio.
     * Funzionalità: mostra carte offerte, carta richiesta e menu azioni.
     */
    private void printDashboard() {
        System.out.println("\n=== NUOVA PROPOSTA DI SCAMBIO ===");

        if (targetCard != null) {
            System.out.println("Carta Richiesta: " + targetCard.getName() + " [" + targetCard.getGameType() + "]");
        }

        System.out.println("Carte Offerte: " + offeredCards.size());

        System.out.println("\n--- AZIONI ---");
        System.out.println("1) Visualizza Collezione Disponibile");
        System.out.println("2) Visualizza Carte Offerte");
        System.out.println("3) Aggiungi Carta all'Offerta");
        System.out.println("4) Rimuovi Carta dall'Offerta");
        System.out.println("5) FINALIZZA PROPOSTA (Scegli Store e Data)");
        System.out.println("0) Indietro");
        System.out.print("Scelta: ");
    }

    /**
     * Gestisce la scelta dell'utente dal menu principale.
     * 
     * @param choice La stringa inserita dall'utente.
     */
    private void handleChoice(String choice) {
        switch (choice) {
            case "1" -> showCollectionList();
            case "2" -> showOfferedList();
            case "3" -> addCardFlow();
            case "4" -> removeCardFlow();
            case "5" -> controller.finalizeProposal();
            case "0" -> {
                running = false;
                if (controller != null) {
                    controller.goBack();
                }
            }
            default -> System.out.println("Scelta non valida.");
        }
    }

    /**
     * Mostra l'elenco delle carte disponibili nella collezione dell'utente.
     * Utilizzato per permettere la selezione di carte da aggiungere all'offerta.
     */
    private void showCollectionList() {
        System.out.println("\n=== COLLEZIONE DISPONIBILE ===");
        if (availableCards.isEmpty()) {
            System.out.println("(Nessuna carta disponibile)");
            return;
        }

        for (int i = 0; i < availableCards.size(); i++) {
            CardBean card = availableCards.get(i);
            // Usiamo remainingQuantity che è già calcolato nel bean dal controller
            System.out.printf("%d) %s [%s] - Rimanenti: %d%n",
                    (i + 1),
                    card.getName(),
                    card.getGameType(),
                    card.getRemainingQuantity());
        }
    }

    /**
     * Mostra l'elenco delle carte attualmente offerte nello scambio.
     * Permette all'utente di rivedere cosa sta proponendo.
     */
    private void showOfferedList() {
        System.out.println("\n=== CARTE OFFERTE ===");
        if (offeredCards.isEmpty()) {
            System.out.println("(Nessuna carta offerta)");
            return;
        }

        for (int i = 0; i < offeredCards.size(); i++) {
            CardBean card = offeredCards.get(i);
            System.out.printf("%d) %s [%s] - Quantità: %d%n",
                    (i + 1),
                    card.getName(),
                    card.getGameType(),
                    card.getQuantity());
        }
    }

    /**
     * Avvia il flusso interattivo per aggiungere una carta all'offerta.
     * Richiede all'utente di selezionare una carta dalla collezione.
     */
    private void addCardFlow() {
        if (availableCards.isEmpty()) {
            System.out.println("Nessuna carta disponibile da aggiungere.");
            return;
        }

        showCollectionList();
        System.out.print("Inserisci il numero della carta da aggiungere (0 annulla): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < availableCards.size()) {
                processAddCard(availableCards.get(idx));
            }
        } catch (NumberFormatException _) {
            System.out.println(MSG_INVALID_INPUT);
        }
    }

    /**
     * Elabora l'aggiunta di una specifica carta all'offerta.
     * Richiede la quantità e invoca il controller per aggiornare il bean.
     * 
     * @param selectedCard Il bean della carta selezionata.
     */
    private void processAddCard(CardBean selectedCard) {
        if (selectedCard.getRemainingQuantity() <= 0) {
            System.out.println("Carta non disponibile (quantità esaurita).");
            return;
        }

        System.out.print("Quantità da aggiungere (max " + selectedCard.getRemainingQuantity() + "): ");
        try {
            int qtyToAdd = Integer.parseInt(scanner.nextLine());
            if (qtyToAdd > 0 && qtyToAdd <= selectedCard.getRemainingQuantity()) {
                int currentOffered = 0;
                for (CardBean c : offeredCards) {
                    if (c.getId().equals(selectedCard.getId())) {
                        currentOffered = c.getQuantity();
                        break;
                    }
                }
                controller.addOfferedCard(selectedCard.getId(), currentOffered + qtyToAdd);
                System.out.println("Richiesta inviata...");
            } else {
                System.out.println("Quantità non valida.");
            }
        } catch (NumberFormatException _) {
            System.out.println(MSG_INVALID_INPUT);
        }
    }

    /**
     * Avvia il flusso interattivo per rimuovere una carta dall'offerta.
     * Richiede all'utente di selezionare una carta tra quelle già offerte.
     */
    private void removeCardFlow() {
        if (offeredCards.isEmpty()) {
            System.out.println("Nessuna carta nell'offerta.");
            return;
        }

        showOfferedList();
        System.out.print("Inserisci il numero della carta da rimuovere (0 annulla): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx >= 0 && idx < offeredCards.size()) {
                CardBean cardToRemove = offeredCards.get(idx);
                controller.removeOfferedCard(cardToRemove.getId());
                System.out.println("Richiesta rimozione inviata...");
            }
        } catch (NumberFormatException _) {
            System.out.println(MSG_INVALID_INPUT);
        }
    }

    @Override
    public void showMeetingDialog(List<UserBean> stores, BiConsumer<UserBean, LocalDateTime> onConfirm) {
        System.out.println("\n=== DETTAGLI APPUNTAMENTO ===");
        if (stores == null || stores.isEmpty()) {
            System.out.println("Nessuno Store disponibile per lo scambio. Impossibile procedere.");
            return;
        }

        UserBean selectedStore = selectStore(stores);
        if (selectedStore == null)
            return;

        LocalDate date = selectDate();
        if (date == null)
            return;

        LocalTime time = selectTime();
        if (time == null)
            return;

        System.out.println("Confermi la proposta di scambio presso " + selectedStore.getUsername() + " il " + date
                + " alle " + time + "? (y/n)");
        String confirm = scanner.nextLine();
        if ("y".equalsIgnoreCase(confirm)) {
            onConfirm.accept(selectedStore, LocalDateTime.of(date, time));
            running = false;
        } else {
            System.out.println("Operazione annullata.");
        }
    }

    /**
     * Permette all'utente di selezionare uno store dalla lista disponibile.
     * 
     * @param stores Lista di store abilitati.
     * @return Il bean dello store selezionato, o null se annullato.
     */
    private UserBean selectStore(List<UserBean> stores) {
        System.out.println("Seleziona uno Store dove effettuare lo scambio:");
        for (int i = 0; i < stores.size(); i++) {
            System.out.printf("%d) %s%n", (i + 1), stores.get(i).getUsername());
        }

        while (true) {
            System.out.print("Scelta Store (0 annulla): ");
            try {
                String line = scanner.nextLine();
                int storeIdx = Integer.parseInt(line) - 1;
                if (storeIdx == -1)
                    return null;
                if (storeIdx >= 0 && storeIdx < stores.size()) {
                    return stores.get(storeIdx);
                } else {
                    System.out.println("Store non valido.");
                }
            } catch (NumberFormatException _) {
                System.out.println(MSG_INVALID_INPUT);
            }
        }
    }

    /**
     * Richiede all'utente l'inserimento di una data valida per lo scambio.
     * 
     * @return La data selezionata (LocalDate) o null se annullato.
     */
    private LocalDate selectDate() {
        while (true) {
            System.out.print("Inserisci Data (dd/MM/yyyy) scrivi 0 per annullare: ");
            try {
                String input = scanner.nextLine();
                if ("0".equals(input.trim()))
                    return null;
                LocalDate date = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (!date.isBefore(LocalDate.now())) {
                    return date;
                }
                System.out.println("La data non può essere nel passato.");
            } catch (DateTimeParseException _) {
                System.out.println("Formato data errato. Usa dd/MM/yyyy");
            }
        }
    }

    /**
     * Richiede all'utente l'inserimento di un orario valido per lo scambio.
     * Verifica che l'orario sia entro le ore di apertura (09:00 - 20:00).
     * 
     * @return L'orario selezionato (LocalTime) o null se annullato.
     */
    private LocalTime selectTime() {
        while (true) {
            System.out.print("Inserisci Orario (HH:mm) scrivi 0 per annullare: ");
            try {
                String input = scanner.nextLine();
                if ("0".equals(input.trim()))
                    return null;
                LocalTime time = LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm"));
                if (time.getHour() >= 9 && time.getHour() <= 20) {
                    return time;
                }
                System.out.println("Lo store è aperto dalle 09:00 alle 20:00.");
            } catch (DateTimeParseException _) {
                System.out.println("Formato ora errato. Usa HH:mm");
            }
        }
    }

    @Override
    public void close() {
        running = false;
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("ERRORE: " + errorMessage);
    }

    @Override
    public void showSuccessMessage(String message) {
        System.out.println("\n[SUCCESS] " + message);
    }
}
