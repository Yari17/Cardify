package view.cli;

import controller.NegotiationController;
import model.bean.CardBean;
import model.bean.ProposalBean;
import config.InputManager;
import org.jetbrains.annotations.NotNull;
import java.util.logging.Logger;
import view.INegotiationView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CliNegotiationView implements INegotiationView {
    private static final Logger LOGGER = Logger.getLogger(CliNegotiationView.class.getName());
    private final InputManager inputManager;
    private NegotiationController controller;
    private List<CardBean> inventory = new ArrayList<>();
    private List<CardBean> requested = new ArrayList<>();
    private List<CardBean> proposed = new ArrayList<>();
    private volatile boolean proposalSent = false;
    private List<String> availableStores = new ArrayList<>();
    private String meetingDateHint;
    private Consumer<CardBean> onPropose;
    private Consumer<CardBean> onUnpropose;
    private Consumer<ProposalBean> onConfirm;

    
    private static final String ITEM_LINE_FMT = " %d) %s x%d%n";
    private static final String INVALID_INDEX_MSG = "Invalid index";

    public CliNegotiationView(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public void showInventory(List<CardBean> inventory) {
        this.inventory = inventory != null ? inventory : new ArrayList<>();
    }

    @Override
    public void showRequested(List<CardBean> requested) {
        this.requested = new ArrayList<>();
        if (requested != null) {
            for (CardBean cb : requested) {
                CardBean copy = new CardBean(cb);
                copy.setQuantity(1); 
                this.requested.add(copy);
            }
        }
    }

    @Override
    public void showProposed(List<CardBean> proposed) {
        this.proposed = proposed != null ? proposed : new ArrayList<>();
    }

    @Override
    public void registerOnCardProposed(Consumer<CardBean> onPropose) {
        this.onPropose = onPropose;
    }

    @Override
    public void registerOnCardUnproposed(Consumer<CardBean> onUnpropose) {
        this.onUnpropose = onUnpropose;
    }

    @Override
    public void registerOnConfirmRequested(Consumer<ProposalBean> onConfirm) {
        this.onConfirm = onConfirm;
    }

    @Override
    public void showConfirmationResult(boolean success, String message) {
        System.out.println(success ? "✓ " + message : "✗ " + message);
        if (success) {
            
            proposalSent = true;
        }
    }

    @Override
    public void setController(NegotiationController controller) {
        this.controller = controller;
    }

    @Override
    public void showAvailableStores(List<String> storeUsernames) {
        this.availableStores = storeUsernames != null ? storeUsernames : new ArrayList<>();
    }

    @Override
    public void setMeetingDateHint(String dateHint) {
        this.meetingDateHint = dateHint;
    }

    @Override
    public void display() {
        
        while (true) {
            printNegotiationView();

            System.out.print("Comando: ");
            String line = inputManager.readString();
            if (line == null) line = "";
            line = line.trim();
            
            if (line.isEmpty()) {
                continue;
            }

            String lower = line.toLowerCase();
            if ("0".equals(line)) return;

            if ("1".equals(line)) {
                handleConfirm();
                if (proposalSent) return;
            } else if (lower.startsWith("add,")) {
                String param = line.substring(4).trim();
                handleAdd(param);
            } else if (lower.startsWith("remove,")) {
                String param = line.substring(7).trim();
                handleRemove(param);
            } else {
                System.out.println("Comando non riconosciuto. Usa 0, 1, add,<n> o remove,<n>");
            }
        }
    }

    
    private void printNegotiationView() {
        System.out.println("=== TRADE NEGOTIATION ===");
        printRequestedItems();
        printInventoryItems();
        printProposedItems();
        printCommandsHelp();
    }

    private void printRequestedItems() {
        System.out.println("Requested items:");
        for (int i = 0; i < requested.size(); i++) {
            System.out.printf(ITEM_LINE_FMT, i + 1, requested.get(i).getName(), requested.get(i).getQuantity());
        }
    }

    private void printInventoryItems() {
        System.out.println("Your inventory:");
        for (int i = 0; i < inventory.size(); i++) {
            System.out.printf(ITEM_LINE_FMT, i + 1, inventory.get(i).getName(), inventory.get(i).getQuantity());
        }
    }

    private void printProposedItems() {
        System.out.println("Proposed items:");
        for (int i = 0; i < proposed.size(); i++) {
            System.out.printf(ITEM_LINE_FMT, i + 1, proposed.get(i).getName(), proposed.get(i).getQuantity());
        }
    }

    private void printCommandsHelp() {
        System.out.println("Comandi disponibili:");
        System.out.println("  0) indietro");
        System.out.println("  1) send propose (conferma l'invio della proposta)");
        System.out.println("  add,<numero>     -> aggiungi la carta numero <numero> dall'inventario alla proposta");
        System.out.println("  remove,<numero>  -> rimuovi la carta numero <numero> dalla lista proposta");
    }

    private void handleAdd(String param) {
        if (param == null || param.isEmpty()) { System.out.println("Specificare l'indice della carta da aggiungere (es. add,3)"); return; }
        try {
            int idx = Integer.parseInt(param) - 1;
            if (idx < 0 || idx >= inventory.size()) { System.out.println(INVALID_INDEX_MSG); return; }
            CardBean card = inventory.get(idx);
            String cardId = card.getId();
            int availableQty = card.getQuantity();

            
            int currentProposedQty = proposed.stream()
                    .filter(pb -> pb != null && cardId != null && cardId.equals(pb.getId()))
                    .mapToInt(CardBean::getQuantity)
                    .sum();

            if (currentProposedQty >= availableQty) {
                System.out.println("Non puoi aggiungere oltre " + availableQty + " unità di questa carta.");
                return;
            }

            ensureProposedIsModifiable();

            
            CardBean existing = proposed.stream()
                    .filter(pb -> pb != null && cardId != null && cardId.equals(pb.getId()))
                    .findFirst().orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + 1);
                
                CardBean unit = new CardBean(existing);
                unit.setQuantity(1);
                if (onPropose != null) onPropose.accept(unit);
                System.out.println("Aggiunta: " + existing.getName() + " x1 (totale proposto: " + existing.getQuantity() + ")");
            } else {
                CardBean copy = new CardBean(card);
                copy.setQuantity(1);
                proposed.add(copy);
                if (onPropose != null) onPropose.accept(new CardBean(copy));
                System.out.println("Aggiunta: " + copy.getName() + " x1");
            }
        } catch (NumberFormatException _) {
            System.out.println(INVALID_INDEX_MSG);
        }
    }

    
    private void ensureProposedIsModifiable() {
        if (!(proposed instanceof java.util.ArrayList)) {
            proposed = new java.util.ArrayList<>(proposed);
        }
    }

     private void handleRemove(String param) {
         if (param == null || param.isEmpty()) { System.out.println("Specificare l'indice della carta proposta da rimuovere (es. remove,2)"); return; }
         try {
             int idx = Integer.parseInt(param) - 1;
             if (idx < 0 || idx >= proposed.size()) { System.out.println(INVALID_INDEX_MSG); return; }
             
             if (!(proposed instanceof java.util.ArrayList)) {
                 proposed = new java.util.ArrayList<>(proposed);
             }
            CardBean card = proposed.get(idx);
            if (card.getQuantity() > 1) {
                card.setQuantity(card.getQuantity() - 1);
                
                CardBean unit = new CardBean(card);
                unit.setQuantity(1);
                if (onUnpropose != null) onUnpropose.accept(unit);
                System.out.println("Rimossa: " + card.getName() + " x1 (rimangono: " + card.getQuantity() + ")");
            } else {
                
                CardBean removed = proposed.remove(idx);
                if (onUnpropose != null) onUnpropose.accept(removed);
                System.out.println("Rimossa: " + removed.getName() + " x1");
            }
         } catch (NumberFormatException _) {
             System.out.println(INVALID_INDEX_MSG);
         }
     }

    private void handleConfirm() {
        
        if (proposed == null || proposed.isEmpty()) {
            System.out.println("Devi offrire almeno una carta prima di confermare la proposta.");
            return;
        }
        String chosenStore = selectStore();
        if (chosenStore == null) return;

        String dateIn = readAndValidateDate();
        if (dateIn == null) return;

        String timeIn = readOptionalTime();

        ProposalBean bean = getProposalBean(chosenStore, dateIn, timeIn);
        if (onConfirm != null) onConfirm.accept(bean);
    }

    
    private String selectStore() {
        System.out.println("Available stores:");
        for (int i = 0; i < availableStores.size(); i++) {
            System.out.printf(" %d) %s%n", i + 1, availableStores.get(i));
        }
        System.out.print("Select store index: ");
        String s = inputManager.readString();
        if (s == null) s = "";
        s = s.trim();
        try {
            int idx = Integer.parseInt(s) - 1;
            if (idx >= 0 && idx < availableStores.size()) return availableStores.get(idx);
        } catch (NumberFormatException _) { System.out.println(INVALID_INDEX_MSG); }
        System.out.println("Invalid store selection");
        return null;
    }

    
    private String readAndValidateDate() {
        System.out.print("Enter meeting date (YYYY-MM-DD) [hint: " + (meetingDateHint != null ? meetingDateHint : "tomorrow") + "]: ");
        String dateIn = inputManager.readString();
        if (dateIn == null) dateIn = "";
        dateIn = dateIn.trim();
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(dateIn);
            if (!d.isAfter(java.time.LocalDate.now())) {
                System.out.println("Date must be after today");
                return null;
            }
        } catch (Exception ex) {
            LOGGER.fine(() -> "Invalid date format input in CLI negotiation: " + ex.getMessage());
            System.out.println("Invalid date format");
            return null;
        }
        return dateIn;
    }

    
    private String readOptionalTime() {
        System.out.print("Enter meeting time (HH:mm) [optional, press ENTER to skip]: ");
        String timeIn = inputManager.readString();
        if (timeIn == null) timeIn = "";
        timeIn = timeIn.trim();
        if (timeIn.isEmpty()) return null;
        return timeIn;
    }

    @NotNull
    private ProposalBean getProposalBean(String chosenStore, String dateIn, String timeIn) {
        ProposalBean bean = new ProposalBean();
        bean.setOffered(new ArrayList<>(proposed));
        
        List<CardBean> requestedCopies = new ArrayList<>();
        for (CardBean cb : requested) {
            CardBean copy = new CardBean(cb);
            copy.setQuantity(1);
            requestedCopies.add(copy);
        }
        bean.setRequested(requestedCopies);
        bean.setFromUser(controller != null ? controller.getProposerUsername() : null);
        bean.setToUser(controller != null ? controller.getTargetOwnerUsername() : null);
        bean.setMeetingPlace(chosenStore);
        bean.setMeetingDate(dateIn);
        if (timeIn != null && !timeIn.isEmpty()) {
            try {
                java.time.LocalTime.parse(timeIn);
                bean.setMeetingTime(timeIn);
            } catch (Exception ex) {
                LOGGER.fine(() -> "Invalid meeting time provided in CLI negotiation: " + ex.getMessage());
                System.out.println("Invalid time format, ignoring time");
                bean.setMeetingTime(null);
            }
        }
        return bean;
    }

    @Override
    public void close() {
        /* not used */
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println("[ERROR] " + errorMessage);
    }

    @Override
    public void refresh() {
        /* not used */
    }
}
