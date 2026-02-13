package view.cli;

import controller.CollectionController;
import model.bean.BinderBean;
import model.bean.CardBean;
import model.domain.enumerations.PersistenceType;
import view.ICollectionView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Implementazione CLI per la gestione della collezione personale.
 * Permette di visualizzare, creare ed eliminare raccoglitori, oltre a gestire
 * le carte al loro interno.
 */
public class CliCollectionView implements ICollectionView {
    private CollectionController controller;
    private final Scanner sc = new Scanner(System.in);
    private boolean isRunning = false;
    private List<BinderBean> currentBinders = new ArrayList<>();

    @Override
    public void display() {
        isRunning = true;
        // Carica i dati iniziali
        if (controller != null)
            controller.loadUserCollection();

        while (isRunning) {
            String currentType = config.AppConfig.getBinderPersistenceType().name();
            System.out.println("\n=== GESTIONE COLLEZIONE ===");
            System.out.println("Persistenza Attuale: " + currentType);
            System.out.println("1) I Miei Raccoglitori");
            System.out.println("2) Nuovo Raccoglitore");
            System.out.println("3) Cambia Persistenza (JDBC/JSON)");
            System.out.println("4) Elimina Raccoglitore");
            System.out.println("5) Gestisci Raccoglitore (Vedi/Aggiungi/Rimuovi Carte)");
            System.out.println("6) Salva Modifiche");
            System.out.println("0) Indietro");
            System.out.print("Scegli: ");

            String input = sc.nextLine().trim();
            switch (input) {
                case "1" -> {
                    if (controller != null)
                        controller.loadUserCollection();
                    printBinders();
                }
                case "2" -> onCreateBinder();
                case "3" -> handleSwitchPersistence();
                case "4" -> handleDeleteBinder();
                case "5" -> handleManageBinder();
                case String s when s.equals("6") && controller != null -> {
                    controller.saveChanges();
                    System.out.println("Modifiche salvate.");
                }
                case "0" -> {
                    isRunning = false;
                    if (controller != null)
                        controller.getAppController().navigateTO(model.domain.enumerations.ViewPage.COLLECTOR_HOMEPAGE);
                }
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    /**
     * Stampa a video la lista dei raccoglitori dell'utente.
     * Mostra ID, nome e numero di carte per ciascun raccoglitore.
     */
    private void printBinders() {
        System.out.println("\n--- I TUOI RACCOGLITORI ---");
        if (currentBinders == null || currentBinders.isEmpty()) {
            System.out.println("Nessun raccoglitore trovato.");
            return;
        }
        for (BinderBean b : currentBinders) {
            System.out.println("- [" + b.getSetID() + "] " + b.getSetName() + " (Carte: "
                    + (b.getOwnedCards() != null ? b.getOwnedCards().size() : 0) + ")");
        }
    }

    /**
     * Gestisce il flusso d'interazione per l'apertura e la gestione di un
     * raccoglitore specifico.
     * Richiede all'utente l'ID del raccoglitore da gestire.
     */
    private void handleManageBinder() {
        if (currentBinders == null || currentBinders.isEmpty()) {
            System.out.println("Nessun raccoglitore caricato. Aggiorna la lista (Opzione 1).");
            return;
        }

        System.out.print("Inserisci Set ID del raccoglitore da gestire: ");
        String setId = sc.nextLine().trim();

        BinderBean selected = currentBinders.stream()
                .filter(b -> b.getSetID().equals(setId))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            System.out.println("Raccoglitore non trovato.");
            return;
        }

        manageBinderLoop(selected);
    }

    /**
     * Loop di gestione interna di un singolo raccoglitore.
     * Permette operazioni di aggiunta/rimozione carte e visualizzazione
     * dell'inventario.
     * 
     * @param binder Il bean del raccoglitore da gestire.
     */
    private void manageBinderLoop(BinderBean binder) {
        boolean managing = true;
        while (managing) {
            // Recupera dati aggiornati delle carte
            List<CardBean> cards = controller.getCompleteBinderCards(binder);

            int ownedCount = (int) cards.stream().filter(c -> c.getQuantity() > 0).count();
            System.out.println("\n--- DETTAGLI RACCOGLITORE: " + binder.getSetName() + " ---");
            System.out.println("Possedute: " + ownedCount + " / " + cards.size());
            System.out.println("Comandi: List (l), Add (add <N>), Remove (remove <N>), Save (s), Back (0)");

            System.out.print("> ");
            String input = sc.nextLine().trim();

            if (input.equals("0")) {
                managing = false;
            } else if (input.equalsIgnoreCase("l")) {
                printCards(cards);
            } else if (input.equalsIgnoreCase("s")) {
                controller.saveChanges();
                System.out.println("Modifiche salvate.");
            } else if (input.toLowerCase().startsWith("add ")) {
                handleCardOperation(binder, cards, input.substring(4).trim(), true);
            } else if (input.toLowerCase().startsWith("remove ")) {
                handleCardOperation(binder, cards, input.substring(7).trim(), false);
            } else {
                System.out.println("Comando non valido.");
            }
        }
        // Ricarica la collezione all'uscita per aggiornare la cache
        controller.loadUserCollection();
    }

    /**
     * Esegue l'operazione di aggiunta o rimozione di una carta dal raccoglitore.
     *
     * @param binder   Il raccoglitore target.
     * @param cards    La lista completa delle carte nel set.
     * @param indexStr L'indice (1-based) della carta inserito dall'utente.
     * @param isAdd    True per aggiungere, false per rimuovere.
     */
    private void handleCardOperation(BinderBean binder, List<CardBean> cards, String indexStr, boolean isAdd) {
        try {
            int index = Integer.parseInt(indexStr);
            if (index >= 1 && index <= cards.size()) {
                String cardId = cards.get(index - 1).getId();
                if (isAdd) {
                    controller.addCardToBinder(binder.getSetID(), cardId);
                    System.out.println("Aggiunta carta: " + cards.get(index - 1).getName());
                } else {
                    controller.removeCardFromBinder(binder.getSetID(), cardId);
                    System.out.println("Rimossa carta: " + cards.get(index - 1).getName());
                }
            } else {
                System.out.println("Indice non valido. Inserisci un numero tra 1 e " + cards.size());
            }
        } catch (NumberFormatException _) {
            System.out.println("Formato non valido. Usa 'add <numero>' o 'remove <numero>'");
        }
    }

    /**
     * Stampa l'elenco delle carte all'interno di un raccoglitore.
     * Mostra la quantità posseduta per ogni carta.
     *
     * @param cards Lista delle carte da visualizzare.
     */
    private void printCards(List<CardBean> cards) {
        for (int i = 0; i < cards.size(); i++) {
            CardBean item = cards.get(i);
            String status = item.getQuantity() > 0 ? "[" + item.getQuantity() + "]" : "[  ]";
            System.out.printf("%d) %-6s %-15s %s%n", (i + 1), status, item.getId(), item.getName());
        }
    }

    /**
     * Gestisce il cambio della modalità di persistenza (JDBC/JSON).
     * Ricarica la collezione dopo il cambio.
     */
    private void handleSwitchPersistence() {
        System.out.println("\nScegli tipo di persistenza:");
        System.out.println("1) JDBC");
        System.out.println("2) JSON");
        System.out.print("Scelta: ");
        String choice = sc.nextLine().trim();

        PersistenceType type = switch (choice) {
            case "1" -> PersistenceType.JDBC;
            case "2" -> PersistenceType.JSON;
            default -> null;
        };

        if (type != null) {
            if (controller != null) {
                controller.switchPersistence(type);
                System.out.println("Persistenza cambiata in " + type);
                controller.loadUserCollection();
            }
        } else {
            System.out.println("Scelta non valida.");
        }
    }

    /**
     * Gestisce la cancellazione di un raccoglitore.
     * Richiede all'utente l'ID del raccoglitore da eliminare.
     */
    private void handleDeleteBinder() {
        System.out.print("Inserisci Set ID del raccoglitore da eliminare: ");
        String id = sc.nextLine().trim();
        if (!id.isEmpty() && controller != null) {
            controller.deleteBinder(id);
            System.out.println("Operazione completata.");
        }
    }

    @Override
    public void close() {
        isRunning = false;
    }

    @Override
    public void refresh() {
        if (controller != null)
            controller.loadUserCollection();
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("ERRORE: " + errorMessage);
    }

    @Override
    public void setController(Object controller) {
        this.controller = (CollectionController) controller;
    }

    @Override
    public void displayUserBinders(List<BinderBean> binders) {
        this.currentBinders = binders;
    }

    @Override
    public void onCreateBinder() {
        if (controller == null)
            return;
        // Chiama il controller per preparare la creazione
        controller.prepareCreateBinder();
    }

    @Override
    public void showAvailableSets(Map<String, String> sets) {
        if (sets == null || sets.isEmpty()) {
            System.out.println("Nessun set disponibile.");
            return;
        }

        System.out.println("Set disponibili:");
        for (Map.Entry<String, String> entry : sets.entrySet()) {
            System.out.println("ID: " + entry.getKey() + " - " + entry.getValue());
        }

        System.out.print("Inserisci ID del set da aggiungere: ");
        String setId = sc.nextLine().trim();

        String setName = sets.get(setId);
        if (setName == null) {
            System.out.println("ID non valido. Uso ID come nome.");
            setName = setId;
        }

        if (controller != null) {
            controller.createNewBinder(setId, setName);
            System.out.println("Raccoglitore creato!");
            controller.loadUserCollection();
        }
    }

    @Override
    public void setSaveButtonVisible(boolean isVisible) {
        if (isVisible) {
            System.out.println("\n[INFO] Modifiche non salvate! Digita '6' per salvare.");
        }
    }

    @Override
    public void onAddCard(CardBean card) {
        if (card != null) {
            System.out.println("[INFO] Aggiunta carta confermata: " + card.getName());
        }
    }

    @Override
    public void onRemoveCard(CardBean card) {
        if (card != null) {
            System.out.println("[INFO] Rimozione carta confermata: " + card.getName());
        }
    }
}
